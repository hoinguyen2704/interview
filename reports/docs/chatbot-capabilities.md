# Chức năng và luật hoạt động của Chatbot Htech

Tài liệu này mô tả chi tiết năng lực hiện có của chatbot Htech, từ API nhận request, cách phân loại intent, dữ liệu được phép đọc, cơ chế bảo vệ truy vấn, đến cách format câu trả lời cho từng nhóm chức năng.

Phạm vi tài liệu bám theo code trong thư mục `chatbot/`, đặc biệt:

- `chatbot/router/chatbot.js`: route chat chính.
- `chatbot/middleware/optional-auth.js`: xác thực tùy chọn cho guest/user.
- `chatbot/controller/chatbot.js`: điều phối intent, guard, query và response.
- `chatbot/constants/messages.js`: system prompt, planner rule và message fallback.
- `chatbot/db/contract.js`: contract resource/field/view chatbot được phép đọc.
- `chatbot/db/executor.js`: executor SQL read-only.
- `chatbot/prompt/contract-to-prompt.js`: render DB contract vào prompt cho planner.
- `chatbot/prompt/few-shot.js`: ví dụ phân loại intent.
- `chatbot/prompt/plan-validate.js`: validate plan trước khi sinh SQL.
- `chatbot/prompt/plan-to-sql.js`: chuyển plan hợp lệ thành SQL có parameter.
- `chatbot/prompt/recommendation.js`: gợi ý chung, cá nhân hóa và mua kèm.
- `chatbot/prompt/prompt-system.js`: prompt trả lời tự nhiên cho câu ngoài DB.
- `chatbot/router/admin.js` và `chatbot/controller/admin.js`: API cấu hình admin/widget.

## 1. API và entrypoint

### 1.1. Chat endpoint

Route chính:

- Method: `POST`
- Path: `/api/v1/chatbot`
- Middleware: `optionalAuth`
- Handler: `chatbot`

Payload chính:

```json
{
  "prompt": "Laptop dưới 20 triệu có gì?",
  "history": [
    { "role": "user", "content": "Mình cần laptop học tập" },
    { "role": "bot", "content": "Bạn muốn mức giá khoảng bao nhiêu?" }
  ]
}
```

Response thành công thường có dạng:

```json
{
  "answer": "Mình tìm thấy 3 kết quả...",
  "mode": "db",
  "resultCount": 3
}
```

Với recommendation, response có thêm `data`:

```json
{
  "answer": "Mình gợi ý cho bạn...",
  "mode": "recommend",
  "data": {
    "featured": [],
    "topRated": [],
    "flashSales": []
  }
}
```

Với `CHATBOT_DEBUG_RESPONSE=true`, response DB có thêm `debug` gồm plan đã validate, SQL, params, số dòng DB và số dòng sau post-process.

### 1.2. Auth tùy chọn

`optionalAuth` không bắt buộc khách phải đăng nhập:

- Không có `Authorization: Bearer ...`: `req.userId = null`, chatbot vẫn trả lời như guest.
- Token hợp lệ: lấy `decoded.userId` hoặc `decoded.sub` làm `req.userId`.
- Token hết hạn/sai: không trả `401`, chỉ coi request là anonymous.

Các chức năng thường vẫn chạy cho guest, trừ tra cứu đơn hàng. `order_lookup` bắt buộc có user đã xác thực.

### 1.3. Admin và widget config

Admin API nằm dưới `/api/v1/chatbot/admin`.

Các route cần JWT admin:

- `GET /config`: đọc toàn bộ config hiện tại.
- `PUT /config`: cập nhật partial config bằng deep merge.
- `POST /config/reset`: reset về defaults.
- `GET /config/defaults`: xem defaults.
- `GET /models`: xem model catalog.

Route public:

- `GET /widget-config`: trả `bot`, `suggestions`, `isEnabled` cho widget.

Admin auth tự verify HMAC JWT theo `JWT_SECRET_KEY`, hỗ trợ `HS256`, `HS384`, `HS512`, yêu cầu role `ADMIN` trong `role` hoặc `roles`.

## 2. Luồng xử lý tổng quát

1. Frontend gửi `{ prompt, history }` lên `/api/v1/chatbot`.
2. Middleware `optionalAuth` gán `req.userId` nếu token hợp lệ; nếu không thì tiếp tục như guest.
3. Controller đọc runtime config qua `getConfig()`.
4. Nếu `isEnabled === false`, chatbot trả message tạm ngưng và không gọi AI/DB.
5. Prompt rỗng trả `400` với `empty_prompt`.
6. Chatbot log prompt, provider, model và số lượng history.
7. History được cắt theo ngân sách riêng:
   - Planner: tối đa 4 message, 200 ký tự/message, 800 ký tự tổng.
   - Reply tự nhiên: tối đa 6 message, 280 ký tự/message, 1400 ký tự tổng.
8. Chatbot kiểm tra shortcut trước:
   - Chào hỏi ngắn: `chao`, `xinchao`, `hello`, `hi`, `alo`, `banlaai`.
   - Gợi ý mua tiếp/mua kèm: các cụm như `muatiep`, `muakem`, `muathem`, `phukien`, `combo`.
9. Nếu không có shortcut, planner gọi AI để tạo JSON plan.
10. Planner result được normalize intent, entity, resource và plan.
11. Controller xử lý theo intent:
    - Một số intent đi qua path riêng: compare, recommend, complementary, product_detail, support, non_db.
    - Các intent DB thông thường đi qua validate plan -> SQL -> read-only query -> format.
12. Guard theo intent được áp trước khi validate plan:
    - Flash sale luôn thêm điều kiện active và thời gian hiện tại.
    - Coupon luôn chỉ lấy public, active.
    - Order luôn ép scope theo user hiện tại.
    - Variant search có thể tự thêm join/filter category nếu user nhắc loại sản phẩm.
13. DB query chạy trong transaction `BEGIN READ ONLY` với `statement_timeout`.
14. Kết quả được format thành câu trả lời tin cậy.
15. Nếu bật `enableResponseSynthesis`, AI có thể viết lại câu trả lời dựa trên `SAFE_CONTEXT`, nhưng không được thêm fact mới.
16. Response trả về frontend.

## 3. Runtime config và model

Config được merge từ:

- `chatbot/config/defaults.json`
- `chatbot/config/settings.json`

Các nhóm config quan trọng:

- `shopInfo`: tên shop, slogan, hotline, email, website, payment methods.
- `bot`: tên bot, subtitle, welcome message, theme color, avatar.
- `ai.model`: model mặc định.
- `ai.provider`: `openai` hoặc `gemini`.
- `ai.temperature`: độ sáng tạo khi gọi model.
- `ai.enableResponseSynthesis`: bật/tắt bước viết lại câu trả lời.
- `ai.maxProducts`: số item tối đa hiển thị trong câu trả lời.
- `ai.planTimeoutMs`: timeout cho planner/synthesis.
- `ai.maxRetries`: số lần retry.
- `ai.dbTimeoutMs`: timeout DB query.
- `suggestions`: câu gợi ý hiển thị ở widget.
- `isEnabled`: bật/tắt chatbot.

Provider được xác định theo thứ tự:

1. `config.ai.provider`
2. Tên model nếu giống model OpenAI (`gpt-*`, `o*`, `chatgpt-*`)
3. Env `CHATBOT_AI_PROVIDER`, `AI_PROVIDER`, `LLM_PROVIDER`
4. Env model OpenAI nếu có
5. Mặc định `gemini`

Model fallback:

- Gemini fallback mặc định: `gemini-3-flash-preview`.
- OpenAI fallback mặc định: `gpt-5.4-mini`.
- Nếu OpenAI lỗi do thiếu key, timeout, quota/rate limit/billing/token, controller fallback sang Gemini.

## 4. Planner và DB contract

### 4.1. Planner tạo JSON plan, không tạo SQL

Planner nhận system instruction từ `MESSAGES.PLAN_SYSTEM(...)`, gồm:

- Tên shop.
- Schema contract được render bởi `contractToPrompt()`.
- Few-shot examples.
- Danh sách resource được phép dùng.
- Luật phân loại intent.

Planner chỉ được trả JSON theo một trong hai dạng:

```json
{
  "intent": "product_search",
  "resource": "Product",
  "entities": ["samsung", "điện thoại"],
  "joins": [{ "resource": "Category" }, { "resource": "Brand" }],
  "select": ["id", "name", "min_price"],
  "where": [
    { "field": "Category.name", "op": "contains", "value": "điện thoại" }
  ],
  "sort": [{ "field": "min_price", "dir": "asc" }],
  "limit": 5
}
```

Hoặc:

```json
{
  "intent": "non_db",
  "message": "Mình có thể hỗ trợ tìm sản phẩm, voucher, flash sale hoặc tra đơn hàng."
}
```

Planner không được sinh SQL và không được bịa bảng/field ngoài contract.

### 4.2. Normalize planner result

Controller normalize các alias intent:

- `compare`, `product_compare` -> `compare_products`
- `recommend`, `product_recommendation` -> `recommend_products`
- `complementary`, `next_purchase`, `cross_sell`, `related_products` -> `complementary_recommendation`
- `detail` -> `product_detail`
- `search`, `search_products` -> `product_search`
- `product_variant_search` -> `variant_search`
- `flashsale_lookup`, `flash_sale_search` -> `flash_sale_lookup`
- `order_search`, `lookup_order` -> `order_lookup`
- `coupon_search` -> `coupon_lookup`
- `blog_lookup`, `article_search` -> `article_lookup`
- `support`, `handoff_support` -> `support_handoff`

Nếu intent thiếu nhưng plan có dấu hiệu rõ ràng, controller có thể suy luận:

- Có từ 2 entity trở lên -> `compare_products`
- Có mã `ORD-...` -> `order_lookup`
- Resource `ProductVariant` -> `variant_search`
- Resource `FlashSaleItem` hoặc `FlashSale` -> `flash_sale_lookup`
- Resource `Order` -> `order_lookup`
- Resource `Coupon` -> `coupon_lookup`
- Resource `Article` -> `article_lookup`
- Resource `Product` + `limit = 1` hoặc select có `description`, `origin_price`, `spec_summary` -> `product_detail`
- Resource `Product` còn lại -> `product_search`

Nếu không normalize được hoặc intent không nằm trong danh sách hỗ trợ, chatbot trả `non_db`.

### 4.3. Validate plan

`validatePlan(plan)` kiểm tra:

- Resource phải nằm trong `CONTRACT.resources`.
- Resource name/alias được normalize để giảm lỗi chính tả đơn giản.
- Join chỉ được đi theo `relations` khai báo trong contract.
- Tối đa 3 join.
- Select chỉ giữ field hợp lệ.
- Where chỉ giữ filter có field hợp lệ.
- Sort chỉ giữ field hợp lệ, dir chỉ là `asc` hoặc `desc`.
- Limit mặc định 5, tối đa 30.
- Nếu không còn field select hợp lệ, ném lỗi `SELECT_EMPTY`.

### 4.4. SQL generation

`planToSQL(validPlan)` tạo SQL parameterized:

- Base table/view dùng alias `t`.
- Join dùng alias trong contract.
- Select luôn alias field rõ ràng.
- Operator hỗ trợ:
  - `eq`
  - `contains` -> `ILIKE '%value%'`
  - `startswith` -> `ILIKE 'value%'`
  - `gt`, `gte`, `lt`, `lte`
  - `between`
  - `in` -> `ANY($n::<type>[])`
- Params được truyền riêng, không nối trực tiếp vào SQL.

## 5. Resource contract

Chatbot chỉ được query các resource sau.

| Resource | View/Table | Mục đích | Default select | Join |
| --- | --- | --- | --- | --- |
| `Product` | `v_chatbot_products` | Read-model sản phẩm | `id`, `name`, `min_price`, `total_stock`, `category_name` | `Category`, `Brand` |
| `ProductVariant` | `v_chatbot_product_variants` | Biến thể/SKU/cấu hình | `id`, `product_name`, `display_name`, `price`, `stock_quantity` | `Product` |
| `Category` | `categories` | Danh mục | `id`, `name`, `slug` | Không |
| `Brand` | `brands` | Thương hiệu | `id`, `name` | Không |
| `Coupon` | `coupons` | Voucher/mã giảm giá | `code`, `discount_type`, `discount_value`, `end_date`, `status` | Không |
| `FlashSaleItem` | `v_chatbot_flash_sale_items` | Item flash sale | `sale_name`, `product_name`, `display_name`, `flash_price`, `flash_stock`, `end_time` | Không |
| `FlashSale` | `flash_sales` | Chương trình flash sale | `id`, `name`, `start_time`, `end_time`, `status` | Không |
| `Order` | `v_chatbot_orders` | Đơn hàng của user | `order_number`, `created_at`, `order_status`, `payment_status`, `total_amount`, `item_count` | Không |
| `Article` | `articles` | Bài viết/blog/tin tức | `id`, `title`, `slug`, `created_at` | Không |

### 5.1. Product fields chính

`Product` có các field quan trọng:

- `id`, `name`, `slug`, `description`
- `origin_price`, `min_price`, `max_price`
- `total_stock`
- `avg_rating`, `review_count`
- `main_image_url`
- `spec_summary`
- `status`, `is_featured`
- `category_id`, `category_name`
- `brand_id`, `brand_name`
- `created_at`

Luật chung: các truy vấn sản phẩm user-facing nên ưu tiên `status = 'ACTIVE'`.

### 5.2. ProductVariant fields chính

`ProductVariant` phục vụ câu hỏi về SKU/cấu hình:

- `id`, `product_id`, `product_name`, `product_slug`
- `category_id`, `category_name`
- `brand_id`, `brand_name`
- `sku`
- `display_name`
- `variant_signature`
- `attribute_summary`
- `price`, `compare_at_price`
- `stock_quantity`
- `status`
- `created_at`

### 5.3. Coupon fields chính

Coupon dùng để trả voucher public:

- `code`
- `discount_type`
- `discount_value`
- `min_order_value`
- `max_discount_amount`
- `usage_limit`, `used_count`
- `start_date`, `end_date`
- `status`
- `is_public`

Sau khi query, controller còn lọc lại bằng `isCouponCurrentlyAvailable()`.

### 5.4. Order fields chính

Order là dữ liệu nhạy cảm:

- `user_id`, `user_email`
- `order_id`, `order_number`
- `created_at`
- `order_status`, `payment_status`
- `tracking_code`
- `subtotal`, `shipping_fee`, `discount_amount`, `total_amount`
- `item_count`, `item_summary`

Mọi query order đều bị ép filter theo user hiện tại.

## 6. Danh sách intent và mode response

| Intent | Mode | Query DB trực tiếp | Dùng AI planner | Dùng synthesis được |
| --- | --- | --- | --- | --- |
| `non_db` | `non_db` | Không | Có thể | Không |
| `product_search` | `db` | Có | Có | Có |
| `product_detail` | `db` | Có, path riêng | Có | Có |
| `variant_search` | `db` | Có | Có | Không |
| `compare_products` | `db` | Có, path riêng | Có | Có |
| `recommend_products` | `recommend` | Có, query riêng | Có/shortcut | Có |
| `complementary_recommendation` | `recommend` | Có, query riêng | Có/shortcut | Có |
| `flash_sale_lookup` | `db` | Có | Có | Không |
| `coupon_lookup` | `db` | Có | Có | Không |
| `order_lookup` | `db` hoặc `non_db` nếu guest | Có | Có | Không |
| `article_lookup` | `db` | Có | Có | Có |
| `support_handoff` | `non_db` | Không | Có | Không |

`getResponseMode()` quy ước:

- `recommend_products`, `complementary_recommendation` -> `recommend`
- `non_db` -> `non_db`
- Các intent còn lại -> `db`

## 7. Chi tiết từng intent

### 7.1. `non_db` - Trả lời ngoài dữ liệu shop

Chức năng:

- Trả lời chào hỏi.
- Trả lời câu hỏi không cần dữ liệu shop.
- Hướng user về các khả năng chính của chatbot.

Ví dụ:

- "Xin chào"
- "Bạn là ai?"
- "Shop có thể hỗ trợ gì?"

Luật hoạt động:

- Nếu prompt normalize trùng nhóm chào hỏi ngắn, chatbot trả lời trực tiếp bằng `MESSAGES.GREETING(shopName)`, không gọi LLM.
- Nếu planner trả `non_db`, chatbot có thể gọi AI reply tự nhiên bằng `buildSystemPrompt(userPrompt)`.
- Prompt system cho non-db có thông tin shop, route frontend ngắn và system rules trong config.
- Nếu user hỏi về link/đường dẫn/route/login/đơn hàng/voucher/hỗ trợ/blog, prompt system sẽ thêm catalog route frontend đầy đủ.
- Nếu gọi AI lỗi, chatbot fallback về `message` từ planner hoặc `MESSAGES.DEFAULT_HINT(shopName)`.

Không query DB.

### 7.2. `product_search` - Tìm kiếm/lọc danh sách sản phẩm

Chức năng:

- Tìm danh sách sản phẩm theo tên, danh mục, thương hiệu, giá, rating, từ khóa.
- Trả nhiều sản phẩm, giới hạn bởi `runtime.maxProducts`.

Ví dụ:

- "Laptop dưới 20 triệu"
- "Điện thoại Samsung giá dưới 10 triệu"
- "Tai nghe bluetooth giá tốt"
- "Sản phẩm Apple còn hàng"

Dữ liệu dùng:

- `Product` -> `v_chatbot_products`
- Có thể join `Category`, `Brand`

Planner thường tạo:

- `resource: "Product"`
- `joins` nếu có category/brand.
- `where` theo `Category.name`, `Brand.name`, `min_price`, `max_price`, `status`.
- `sort` theo giá/rating/review/created_at tùy câu hỏi.

Format answer:

- Nếu nhiều row, dùng `formatProductLine()`.
- Mỗi dòng có tên, giá, tồn kho, brand, category, rating nếu có.
- Nếu chỉ có 1 row và resource là `Product`, controller format như product detail.

Giới hạn:

- Không tự query biến thể nếu planner chọn `Product`.
- Không bịa thông số nếu `spec_summary` không được select hoặc không có dữ liệu.

### 7.3. `product_detail` - Xem chi tiết một sản phẩm

Chức năng:

- Trả chi tiết một sản phẩm cụ thể.
- Ưu tiên match sản phẩm tốt nhất thay vì chỉ chạy plan thô.

Ví dụ:

- "iPhone 17 Pro Max giá bao nhiêu?"
- "MacBook Pro 14 M5 có gì?"
- "Thông tin Samsung Galaxy S25 Ultra"

Dữ liệu dùng:

- `v_chatbot_products`

Luồng riêng:

1. Lấy term từ `entities` và từ where field `name`.
2. `findBestMatchingProduct()` tìm trong `name`, `spec_summary`, `description`.
3. Sort ưu tiên:
   - Tên match chính xác.
   - Tên match prefix.
   - Nhiều review.
   - Rating cao.
   - Tồn kho cao.
   - Mới hơn.
4. Trả product đầu tiên hoặc `MESSAGES.NO_DATA(prompt)`.

Format answer:

- Tên sản phẩm.
- Brand.
- Category.
- Giá thấp nhất và giá gốc nếu khác.
- Tồn kho.
- Đánh giá.
- Mô tả đã strip HTML và cắt khoảng 220 ký tự.
- Tối đa 8 dòng thông số từ `spec_summary`.

Nếu bật synthesis:

- Safe context có description tối đa khoảng 420 ký tự.
- Spec highlights tối đa 6 dòng.
- AI chỉ được viết lại dựa trên safe context.

### 7.4. `variant_search` - Tìm biến thể/cấu hình/SKU

Chức năng:

- Tìm biến thể theo RAM, storage, màu, SKU, cấu hình, giá variant, tồn kho variant.

Ví dụ:

- "iPhone màu titan còn hàng không?"
- "MacBook bản 16GB RAM"
- "SKU này còn không?"
- "Laptop nào đang giảm giá?"

Dữ liệu dùng:

- `ProductVariant` -> `v_chatbot_product_variants`
- Có thể join `Product`

Guard quan trọng:

- Nếu entities có category hint như điện thoại, laptop, tablet, màn hình, tai nghe, chuột, bàn phím, đồng hồ, loa, controller thêm join `Product` và filter `Product.category_name`.
- Guard này tránh lỗi planner chỉ tìm variant giảm giá chung nhưng bỏ qua loại sản phẩm user hỏi.

Format answer:

- Tên product.
- Tên hiển thị variant.
- `attribute_summary`.
- Giá.
- Tồn kho variant.

### 7.5. `compare_products` - So sánh sản phẩm

Chức năng:

- So sánh hai sản phẩm.
- Chỉ ra ưu điểm tương đối và điểm cần cân nhắc dựa trên dữ liệu có thật.

Ví dụ:

- "So sánh iPhone 17 Pro Max với Samsung Galaxy S25 Ultra"
- "MacBook Pro 14 M5 hay MSI Prestige đáng mua hơn?"
- "iPhone 17 Pro Max có thông số gì vượt trội so với S26 Ultra?"

Luồng riêng:

1. Lấy 2 term từ planner `entities`.
2. Nếu planner không đủ, dùng regex fallback tách theo:
   - `so với`
   - `vs`
   - `với`
   - `hay`
   - `và`
3. Cleanup term để bỏ cụm như "so sánh", "nên mua", "cái nào", "ưu nhược điểm".
4. `resolveComparisonProducts()` tìm từng sản phẩm bằng `findBestMatchingProduct()`.
5. Nếu tìm đủ 2 sản phẩm khác nhau, build comparison.
6. Nếu thiếu, trả fallback yêu cầu user ghi rõ tên.

Dữ liệu so sánh:

- Giá thấp nhất.
- Rating.
- Review count.
- Tồn kho.
- Spec highlights.

Không so sánh ngoài dữ liệu:

- Nếu thiếu thông số CPU/màn hình/pin trong `spec_summary`, chatbot không tự suy luận.
- Nếu chỉ tìm được một sản phẩm, chatbot nói rõ chưa đủ dữ liệu.

### 7.6. `recommend_products` - Gợi ý sản phẩm chung

Chức năng:

- Gợi ý sản phẩm nổi bật, sản phẩm đánh giá cao và flash sale.
- Với user đăng nhập, ưu tiên gợi ý cá nhân hóa theo lịch sử mua.

Ví dụ:

- "Gợi ý sản phẩm hot"
- "Có gì đáng mua không?"
- "Sản phẩm nào đang nổi bật?"
- "Gợi ý điện thoại cho mình"

Guest dùng `getRecommendations()` với 3 query song song:

- `featured`: sản phẩm `ACTIVE`, `is_featured = true`, sort `created_at DESC`.
- `topRated`: sản phẩm `ACTIVE`, `review_count >= 1`, sort `avg_rating DESC`, `review_count DESC`, `created_at DESC`.
- `flashSales`: flash sale `ACTIVE`, đang trong thời gian hiện tại, sort `flash_price ASC`.

User đăng nhập dùng `getPersonalizedRecommendations(userId)`:

- `sameCategory`: sản phẩm cùng danh mục user đã mua, loại trừ sản phẩm đã mua.
- `sameBrand`: sản phẩm cùng brand user đã mua, loại trừ sản phẩm đã mua.
- `coPurchase`: sản phẩm khách khác thường mua cùng trong các đơn liên quan.
- `budgetMatch`: sản phẩm có giá trong khoảng 60%-140% giá trung bình user từng mua.
- Thêm `flashSales` nếu có.

Nếu không có dữ liệu cá nhân hóa, fallback về gợi ý chung.

Trạng thái order được coi là purchase event:

- `order_status`: `CONFIRMED`, `PROCESSING`, `SHIPPING`, `SHIPPED`
- hoặc `payment_status`: `COMPLETED`

### 7.7. `complementary_recommendation` - Gợi ý mua tiếp/mua kèm

Chức năng:

- Gợi ý sản phẩm bổ trợ sau khi user đã mua, đang mua hoặc đang hỏi về một sản phẩm/category.
- Áp dụng cho cả guest và user đăng nhập.

Ví dụ:

- "Mua laptop rồi nên mua gì tiếp?"
- "Có iPhone rồi mua thêm gì?"
- "Mua MacBook Pro 14 M5 thì nên mua gì kèm?"
- "Mua điện thoại cần phụ kiện gì?"
- "Gợi ý mua tiếp cho tôi"

Luật kích hoạt:

- Shortcut bắt trước planner nếu prompt có cụm mua tiếp/mua kèm/mua thêm/sắm tiếp/đi kèm/phụ kiện/combo.
- Planner cũng được hướng dẫn chọn intent này cho các câu cross-sell.
- Nếu câu hỏi có product/category rõ ràng, ưu tiên context trong câu hỏi.
- Nếu câu hỏi chung và user đăng nhập, dùng lịch sử mua gần đây.
- Nếu không resolve được nguồn hoặc không có sản phẩm bổ trợ, fallback về `recommend_products`.

Resolve source theo thứ tự:

1. Tách lookup terms từ prompt/entities.
2. Match category trực tiếp theo slug/name/keyword.
3. Tìm product tốt nhất trong `v_chatbot_products`, lấy category của product.
4. Match category bằng fallback alias như `iphone`, `galaxy`, `macbook`, `ipad`, `airpods`.
5. Nếu user đăng nhập, lấy tối đa 3 category mua gần đây từ `v_chatbot_user_purchase_events`.
6. Nếu vẫn không có source, fallback gợi ý chung.

Mapping bổ trợ hiện tại:

| Nguồn | Gợi ý bổ trợ |
| --- | --- |
| `laptop` | `man-hinh`, `chuot`, `ban-phim`, `tai-nghe` |
| `dien-thoai` | `tai-nghe`, `may-tinh-bang`, `loa` |
| `may-tinh-bang` | `ban-phim`, `tai-nghe`, `loa` |
| `man-hinh` | `chuot`, `ban-phim`, `loa` |
| `ban-phim` | `chuot`, `man-hinh`, `tai-nghe` |
| `chuot` | `ban-phim`, `man-hinh`, `tai-nghe` |
| `tai-nghe` | `dien-thoai`, `laptop`, `loa` |
| `loa` | `tai-nghe`, `dien-thoai`, `laptop` |

Các section trả về:

- `accessories`: "Phụ kiện nên mua kèm"
- `experience`: "Thiết bị nâng cấp trải nghiệm"
- `flashSales`: "Đang có ưu đãi"

Cách query sản phẩm bổ trợ:

- Product phải `ACTIVE`.
- Category nằm trong target categories.
- Loại trừ product gốc nếu source là product.
- Nếu user đăng nhập, loại trừ sản phẩm user đã mua.
- Sort:
  - Còn hàng trước.
  - Featured trước.
  - Rating cao.
  - Review nhiều.
  - Mới hơn.

Cách query flash sale bổ trợ:

- Flash sale `ACTIVE`.
- `start_time <= NOW()`.
- `end_time >= NOW()`.
- Category thuộc target categories.
- Loại trừ product gốc và product user đã mua nếu có.
- Sort `flash_price ASC`.

### 7.8. `flash_sale_lookup` - Tra flash sale

Chức năng:

- Liệt kê sản phẩm đang flash sale.

Ví dụ:

- "Đang có flash sale gì?"
- "Sản phẩm nào đang sale?"
- "Flash sale hôm nay có gì?"

Dữ liệu dùng:

- `FlashSaleItem` -> `v_chatbot_flash_sale_items`

Guard bắt buộc:

- `sale_status = 'ACTIVE'`
- `start_time <= now`
- `end_time >= now`
- Select thêm các field hiển thị cần thiết:
  - `sale_name`
  - `product_name`
  - `display_name`
  - `attribute_summary`
  - `flash_price`
  - `flash_stock`
  - `sold_count`
  - `end_time`
- Sort mặc định `end_time ASC`.
- Limit không vượt quá 10 trước khi qua cap runtime.

Format answer:

- Tên sản phẩm.
- Variant/attribute nếu có.
- Giá flash.
- Tồn flash.
- Đã bán nếu > 0.
- Tên chương trình.
- Thời gian hết hạn.

### 7.9. `coupon_lookup` - Tra voucher/mã giảm giá

Chức năng:

- Tìm voucher public còn hiệu lực và còn lượt dùng.

Ví dụ:

- "Có mã giảm giá không?"
- "Voucher nào còn dùng được?"
- "Có freeship không?"

Dữ liệu dùng:

- `Coupon` -> `coupons`

Guard bắt buộc trước query:

- `status = 'ACTIVE'`
- `is_public = true`
- Select thêm:
  - `code`
  - `discount_type`
  - `discount_value`
  - `min_order_value`
  - `max_discount_amount`
  - `usage_limit`
  - `used_count`
  - `start_date`
  - `end_date`
  - `status`
  - `is_public`
- Sort `end_date ASC`.
- Desired limit đủ lớn để post-process còn dữ liệu sau khi lọc.

Post-process sau query:

- Loại coupon không public.
- Loại coupon không `ACTIVE`.
- Loại coupon chưa tới `start_date`.
- Loại coupon đã qua `end_date`.
- Loại coupon đã hết lượt `used_count >= usage_limit`.
- Slice theo `maxProducts`.

Format discount:

- `PERCENTAGE`: `Giảm X%`
- `FIXED_AMOUNT`: `Giảm Xđ`
- `FREE_SHIP`: miễn phí vận chuyển, có thể kèm mức tối đa.
- Khác: `Ưu đãi đặc biệt`

Không bao giờ bịa mã ngoài DB.

### 7.10. `order_lookup` - Tra cứu đơn hàng

Chức năng:

- Tra trạng thái đơn hàng của user đã đăng nhập.
- Hỗ trợ hỏi theo mã `ORD-...` hoặc hỏi danh sách đơn gần đây.

Ví dụ:

- "Đơn ORD-20260502-98BC27 của tôi sao rồi?"
- "Đơn hàng gần đây của tôi"
- "Đơn của tôi đang giao chưa?"

Dữ liệu dùng:

- `Order` -> `v_chatbot_orders`

Luật bảo vệ:

- Nếu chưa đăng nhập, chatbot không query DB và trả `MESSAGES.ORDER_LOGIN_REQUIRED`.
- Nếu đã đăng nhập, plan bị thay bằng `buildOrderLookupPlan(...)`.
- Base where luôn có user scope:
  - Nếu principal giống UUID: `user_id = authPrincipal`
  - Nếu không: `user_email = authPrincipal`
- Nếu có mã `ORD-...`, thêm filter chính xác `order_number = ...`, limit 1.
- Nếu không có mã đơn, cho phép giữ lại một số filter an toàn từ planner:
  - `order_status`
  - `payment_status`
- Sort mặc định `created_at DESC`.
- Limit khi không có mã đơn nằm trong khoảng 3-10 tùy `maxProducts`.

Format status:

- `PENDING` -> chờ xác nhận
- `CONFIRMED` -> đã xác nhận
- `PROCESSING` -> đang xử lý
- `SHIPPING` -> đang giao
- `SHIPPED` -> đã giao
- `CANCELLED` -> đã hủy
- `RETURNED` -> đã hoàn trả

Payment status:

- `PENDING` -> chờ thanh toán
- `COMPLETED` -> đã thanh toán
- `FAILED` -> thanh toán thất bại
- `REFUNDED` -> đã hoàn tiền

Format answer gồm:

- Mã đơn.
- Trạng thái đơn.
- Trạng thái thanh toán.
- Tổng tiền nếu có.
- Thời gian đặt.
- Số dòng sản phẩm.
- Tóm tắt sản phẩm.
- Mã vận đơn nếu có.

### 7.11. `article_lookup` - Tìm bài viết/blog

Chức năng:

- Tìm bài viết tư vấn, hướng dẫn, tin tức công nghệ.

Ví dụ:

- "Có bài hướng dẫn chọn laptop không?"
- "Tin tức về iPhone mới"
- "Bài blog mới nhất"

Dữ liệu dùng:

- `Article` -> `articles`

Planner nên chọn intent này khi user nhắc:

- bài viết
- tin tức
- blog
- hướng dẫn
- tin công nghệ

Luật:

- Không chuyển sang `product_search` nếu user rõ ràng đang hỏi bài viết.
- Thường filter `is_published = true`.
- Sort mới trước nếu user hỏi bài mới.

Synthesis được hỗ trợ nếu bật.

### 7.12. `support_handoff` - Chuyển hỗ trợ

Chức năng:

- Hướng user sang support/admin khi yêu cầu cần người xử lý.

Ví dụ:

- "Tôi muốn gặp nhân viên"
- "Liên hệ admin"
- "Khiếu nại đơn hàng"
- "Tôi cần hỗ trợ đơn hàng này"

Luật:

- Không query DB.
- Nếu user đã đăng nhập, hướng vào `/user/support` hoặc `/user/orders`.
- Nếu guest, hướng đăng nhập tại `/login`, rồi vào `/user/support`.

## 8. Shortcut trước planner

### 8.1. Greeting shortcut

Prompt được normalize:

- Unicode NFKD.
- Bỏ dấu tiếng Việt.
- Lowercase.
- Bỏ ký tự không phải chữ/số.

Các key chào hỏi:

- `chao`
- `xinchao`
- `hello`
- `hi`
- `alo`
- `banlaai`

Nếu match, response:

- `intent: non_db`
- `skipLlm: true`
- message greeting theo tên shop.

### 8.2. Complementary shortcut

Các cụm normalize kích hoạt:

- `muatiep`
- `muakem`
- `muathem`
- `samtiep`
- `samkem`
- `samthem`
- `muacung`
- `dikem`
- `phukien`
- `combo`
- `nenmuagitiep`
- `nenmuagikem`
- `nenmuathem`
- `canmuagitiep`
- `canmuagikem`
- `goiymuatiep`
- `goiymuakem`

Nếu match:

- `intent: complementary_recommendation`
- `entities: []`
- `skipLlm: true`

Sau đó recommendation module tự resolve product/category từ prompt.

## 9. Response synthesis

Response synthesis là bước tùy chọn sau khi backend đã có câu trả lời tin cậy.

Điều kiện bật:

- `appConfig.ai.enableResponseSynthesis === true`
- Intent nằm trong:
  - `product_detail`
  - `product_search`
  - `compare_products`
  - `recommend_products`
  - `complementary_recommendation`
  - `article_lookup`
- Có `draftAnswer`.
- Có `safeContext`.

System rule synthesis:

- Chỉ dùng thông tin trong `SAFE_CONTEXT` hoặc `TRUSTED_DRAFT_ANSWER`.
- Không bịa thêm giá, tồn kho, thông số, voucher, trạng thái đơn hàng, thời gian, so sánh.
- Không nhắc SQL, read-model, schema, system prompt, contract, planner, backend.
- Trả lời tiếng Việt, tự nhiên, rõ trọng tâm.
- Không dùng bảng Markdown.

Nếu synthesis lỗi hoặc trả rỗng, chatbot giữ `draftAnswer`.

## 10. Format câu trả lời

### 10.1. Product line

`formatProductLine(row, index)` ưu tiên:

- `name`, `product_name`, `title`
- `min_price`, `flash_price`, `price`, `origin_price`
- `total_stock`, `flash_stock`, `stock_quantity`
- `category_name`
- `brand_name`
- `avg_rating`, `review_count`

Dạng:

```md
1. **Tên sản phẩm** - 12.990.000đ (tồn: 5) - Apple - Điện thoại - ⭐ 4.8 (20 đánh giá)
```

### 10.2. Product detail

`formatProductDetail(row)` trả:

- Tên.
- Thương hiệu.
- Danh mục.
- Giá và giá gốc nếu khác.
- Tồn kho.
- Đánh giá.
- Mô tả ngắn.
- Thông số kỹ thuật.

HTML trong mô tả/spec được strip để tránh render bẩn.

### 10.3. Variant line

`formatVariantLine(row, index)` trả:

- Tên product.
- Display name nếu khác product name.
- Attribute summary.
- Giá.
- Tồn kho.

### 10.4. Flash sale line

`formatFlashSaleLine(row, index)` trả:

- Product name.
- Variant/attribute.
- Flash price.
- Flash stock.
- Sold count nếu > 0.
- Sale name.
- End time.

### 10.5. Coupon line

`formatCouponLine(row, index)` trả:

- Code.
- Nhãn giảm giá.
- Đơn tối thiểu.
- Giảm tối đa.
- Hết hạn.

### 10.6. Order line

`formatOrderLine(row, index)` trả:

- Order number.
- Order status label.
- Payment status label.
- Total amount.
- Created time.
- Item count.
- Item summary.
- Tracking code.

## 11. Guardrail và an toàn dữ liệu

### 11.1. Không ghi DB

Mọi query qua `runReadOnly()`:

- Mở transaction `BEGIN READ ONLY`.
- Set `statement_timeout`.
- Chạy SQL.
- Commit nếu thành công.
- Rollback nếu lỗi.

Timeout query được clamp:

- Mặc định: 6000ms.
- Tối thiểu: 1000ms.
- Tối đa: 30000ms.

### 11.2. Không query ngoài contract

Planner chỉ tạo plan. SQL chỉ được tạo sau khi:

- Resource hợp lệ.
- Join hợp lệ.
- Field select/where/sort hợp lệ.
- Limit hợp lệ.

Nếu planner cố dùng field lạ, field đó bị loại. Nếu select rỗng, request chuyển sang fallback unclear.

### 11.3. Bảo vệ đơn hàng

Order lookup không tin vào filter user do planner sinh ra. Controller tự dựng lại plan:

- Luôn ép `user_id` hoặc `user_email` theo `req.userId`.
- Không cho query order nếu guest.
- Chỉ cho thêm filter `order_status`, `payment_status` ngoài user scope.
- Mã đơn `ORD-...` được extract và upper-case.

### 11.4. Bảo vệ voucher

Coupon được guard hai lớp:

- Trước query: `status = ACTIVE`, `is_public = true`.
- Sau query: check public, active, start/end date, usage limit.

### 11.5. Bảo vệ flash sale

Flash sale luôn được ép active và nằm trong thời gian hiện tại:

- `sale_status = ACTIVE`
- `start_time <= now`
- `end_time >= now`

### 11.6. Không lộ SQL mặc định

SQL/params chỉ nằm trong server log. Response mặc định không trả SQL. Chỉ khi `CHATBOT_DEBUG_RESPONSE=true`, response mới có debug object.

## 12. Error handling

Các lỗi được xử lý riêng:

- Thiếu read-model view (`42P01` với `v_chatbot_*`): trả `MESSAGES.ERR_DB_SYNCING`, mode `non_db`.
- `RESOURCE_NOT_ALLOWED`, `SELECT_EMPTY`, `PLAN_RESOURCE_MISSING`: trả `MESSAGES.ERR_UNCLEAR`, mode `non_db`.
- Timeout/unavailable AI:
  - `GEMINI_TIMEOUT`
  - `GEMINI_UNAVAILABLE`
  - `OPENAI_TIMEOUT`
  - `OPENAI_UNAVAILABLE`
  - `OPENAI_API_KEY_MISSING`
  -> trả `MESSAGES.ERR_AI_BUSY`, mode `non_db`.
- Lỗi khác: trả HTTP `500` với `{ error: error.message }`.

## 13. Ví dụ phân loại nhanh

| Câu hỏi | Intent kỳ vọng | Ghi chú |
| --- | --- | --- |
| "Xin chào" | `non_db` | Shortcut, không gọi LLM |
| "Điện thoại Samsung giá dưới 10 triệu" | `product_search` | Join Category + Brand |
| "Chi tiết iPhone 15" | `product_detail` | Resolve product tốt nhất |
| "Laptop nào đang giảm giá?" | `variant_search` | Variant + Product category guard |
| "So sánh iPhone 17 Pro Max với S26 Ultra" | `compare_products` | Cần 2 product khác nhau |
| "Gợi ý sản phẩm cho mình" | `recommend_products` | Guest global, user personalized |
| "Mua laptop rồi nên mua gì tiếp?" | `complementary_recommendation` | Shortcut mua kèm |
| "Flash sale hôm nay có gì?" | `flash_sale_lookup` | Active + time guard |
| "Có mã giảm giá nào dùng được không?" | `coupon_lookup` | Public + active + date + usage |
| "Tra cứu đơn ORD-20260502-98BC27" | `order_lookup` | Bắt buộc login |
| "Có bài viết mới nào không?" | `article_lookup` | Article published |
| "Tôi cần hỗ trợ đơn hàng này" | `support_handoff` | Hướng sang support |

## 14. Checklist test thủ công

Khi sửa chatbot, nên kiểm thử các nhóm sau:

- Guest chào hỏi: "Xin chào".
- Guest tìm sản phẩm: "Điện thoại Samsung giá dưới 10 triệu".
- Guest xem chi tiết: "Chi tiết iPhone 15".
- Guest hỏi variant: "MacBook bản 16GB RAM còn không?".
- Guest so sánh: "So sánh iPhone 17 Pro Max với S26 Ultra".
- Guest gợi ý chung: "Gợi ý sản phẩm hot".
- Guest mua kèm: "Mua laptop rồi nên mua gì tiếp?".
- Guest flash sale: "Flash sale hôm nay có gì?".
- Guest voucher: "Có mã giảm giá nào không?".
- Guest tra đơn: phải được yêu cầu đăng nhập.
- User đăng nhập tra đơn bằng `ORD-...`: chỉ thấy đơn của chính user.
- User đăng nhập hỏi "Gợi ý mua tiếp cho tôi": dùng lịch sử mua nếu có.
- Câu support: "Tôi muốn gặp nhân viên".
- Câu blog: "Có bài hướng dẫn chọn laptop không?".
- Tắt chatbot bằng config: mọi câu trả disabled message.
- Bật `CHATBOT_DEBUG_RESPONSE=true`: kiểm tra debug chỉ xuất hiện môi trường cần thiết.

## 15. Checklist khi thêm chức năng mới

Nếu thêm intent/resource mới:

1. Thêm resource/field/relation vào `chatbot/db/contract.js`.
2. Thêm intent vào `SUPPORTED_INTENTS`.
3. Nếu intent query DB, thêm mapping vào `INTENT_RESOURCE_MAP`.
4. Cập nhật `MESSAGES.PLAN_SYSTEM`.
5. Thêm few-shot vào `chatbot/prompt/few-shot.js`.
6. Thêm guard intent nếu cần bảo vệ dữ liệu.
7. Thêm formatter hoặc mở rộng `buildDbAnswer()`.
8. Cân nhắc có cho synthesis hay không trong `RESPONSE_SYNTHESIS_INTENTS`.
9. Thêm test/manual case vào tài liệu này.
10. Nếu cần read-model mới, cập nhật SQL init read-model và contract.

## 16. Kết luận vận hành

Chatbot hiện được thiết kế theo nguyên tắc: AI chỉ lập kế hoạch và diễn đạt, backend mới là nơi quyết định dữ liệu nào được đọc. DB contract, plan validation, read-only executor và guard theo intent là các lớp chính để tránh truy vấn sai schema, lộ dữ liệu nhạy cảm hoặc bịa fact.

Điểm khác biệt quan trọng nhất giữa hai nhóm gợi ý:

- `recommend_products`: gợi ý global hoặc cá nhân hóa theo lịch sử mua.
- `complementary_recommendation`: gợi ý mua kèm/mua tiếp dựa trên product/category nguồn hoặc lịch sử mua gần đây.

Vì vậy câu như "mua laptop rồi nên mua gì tiếp" phải vào `complementary_recommendation`, không phải `recommend_products`.
