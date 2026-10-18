# Ngân hàng câu hỏi phỏng vấn Fullstack Intern cho dự án Htech

Tài liệu này dùng để ôn phỏng vấn vị trí Fullstack Intern/Junior dựa trên đồ án Htech/Hozitech: website thương mại điện tử sản phẩm công nghệ, frontend React/TypeScript, backend Spring Boot, PostgreSQL và service chatbot Node.js/Express tích hợp OpenAI/Gemini.

Khi trả lời phỏng vấn, nên đi theo công thức: **khái niệm -> áp dụng trong Htech -> trade-off/hạn chế**. Ví dụ: giải thích JWT là gì, Htech dùng JWT ở đâu, sau đó nói thêm về rủi ro token bị lộ và cách giảm thiểu. Không nên nói quá mức: phần kiểm thử trong đồ án nên gọi là functional/manual testing nếu chưa có automated test rõ ràng.

---

## 1. Java Core, OOP, Collections, Exception, Stream (Chuyên Sâu)

### Câu 01 - Java có những đặc điểm nào phù hợp để xây dựng backend enterprise như Htech?

**Câu hỏi:** Vì sao Java vẫn là lựa chọn hàng đầu cho các hệ thống backend doanh nghiệp lớn và phù hợp với đồ án thương mại điện tử Htech?

**Bản chất kỹ thuật & Cơ chế hoạt động:**
- **Static Typing & Type Safety:** Trình biên dịch `javac` phát hiện lỗi kiểu dữ liệu ngay tại thời điểm biên dịch (Compile-time) thay vì chờ đến lúc chạy (Runtime) như JavaScript hay Python. Trong các nghiệp vụ tài chính, tiền tệ và trừ tồn kho, tính chặt chẽ về kiểu (`BigDecimal`, `UUID`, `Enum`) giúp loại trừ hoàn toàn các lỗi ép kiểu ngầm định.
- **JVM & Quản trị tài nguyên:** JVM (Java Virtual Machine) tối ưu hóa mã byte-code thông qua JIT (Just-In-Time) Compiler thành mã máy nhị phân bản địa (Native Code) cho các đoạn code nóng (Hotspots). Các bộ Garbage Collector hiện đại (G1GC, ZGC) có khả năng quản lý heap từ vài GB tới TB với độ trễ micro-giây.
- **Hệ sinh thái Enterprise hoàn chỉnh:** Thư viện chuẩn mực, cơ chế quản lý đa luồng (Multi-threading, Concurrency Utilities `java.util.concurrent`), hỗ trợ JDBC, bảo mật mạnh mẽ và tiêu chuẩn JPA/Hibernate không đối thủ.

**Liên hệ với Htech:**
- Backend Htech sử dụng Java 25 kết hợp Spring Boot xử lý các luồng transaction phức tạp: Checkout đơn hàng, tính toán mã giảm giá theo điều kiện giỏ hàng, xác thực chữ ký số Webhook và quản lý hàng đợi xuất file dữ liệu.
- Việc sử dụng `BigDecimal` cho giá tiền, `UUID` cho định danh và các `Enum` có ràng buộc chặt chẽ (`OrderStatus`, `PaymentMethod`) giúp hệ thống tránh tuyệt đối các lỗi sai lệch số học trong giao dịch thanh toán.

**Cạm bẫy & Lưu ý khi trả lời:**
- *Cạm bẫy:* Chỉ trả lời sáo rỗng "Java dễ học, bảo mật, độc lập nền tảng Write Once Run Anywhere".
- *Cách trả lời ăn điểm:* Nhấn mạnh vào **Compile-time safety**, **Concurrency model**, **Hệ sinh thái Transaction/JPA** và khả năng scale lâu dài khi dự án phình to về logic nghiệp vụ.

---

### Câu 02 - Bốn tính chất OOP trong Java và cách áp dụng thực tế trong Htech

**Câu hỏi:** Hãy phân tích 4 tính chất OOP (Encapsulation, Abstraction, Inheritance, Polymorphism) qua các class cụ thể trong mã nguồn Htech? Tại sao lại ưu tiên Composition hơn Inheritance?

**Bản chất kỹ thuật & Phân tích thực tế:**
1. **Encapsulation (Đóng gói):**
   - *Bản chất:* Che giấu trạng thái nội tại của đối tượng bằng access modifier `private`, chỉ cho phép thay đổi qua các phương thức công khai có kiểm tra tính hợp lệ.
   - *Trong Htech:* Entity [Order.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/domain/entities/Order.java) có các thuộc tính `orderStatus`, `totalAmount` đều là `private`. Không cho phép controller từ bên ngoài tùy tiện gán giá trị sai logic, mà phải thông qua các business method có kiểm tra điều kiện chuyển đổi trạng thái hợp lệ.
2. **Abstraction (Trừu tượng hóa):**
   - *Bản chất:* Tập trung vào việc đối tượng "làm được gì" (What) thay vì "làm như thế nào" (How), che giấu sự phức tạp sau các Interface.
   - *Trong Htech:* [OrderService.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/application/services/order/OrderService.java) định nghĩa contract: `createOrder(...)`, `handlePaymentWebhook(...)`. Controller chỉ gọi contract này mà không cần quan tâm bên dưới dùng PostgreSQL, JPA hay gọi sang RabbitMQ.
3. **Inheritance (Kế thừa):**
   - *Bản chất:* Tái sử dụng thuộc tính và hành vi chung giữa các lớp có quan hệ "IS-A".
   - *Trong Htech:* [Order.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/domain/entities/Order.java) và [Product.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/domain/entities/Product.java) đều kế thừa từ [AbstractAuditingEntity.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/domain/entities/base/AbstractAuditingEntity.java) để tự động sở hữu các trường kiểm toán: `createdAt`, `updatedAt`, `createdBy`.
4. **Polymorphism (Đa hình):**
   - *Bản chất:* Cùng một thông điệp/interface nhưng các đối tượng khác nhau sẽ phản ứng theo những cách khác nhau (Method Overriding / Dynamic Method Dispatch).
   - *Trong Htech:* Tầng lưu trữ file [ExportFileStorage.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/application/services/export/ExportFileStorage.java). Ở môi trường Local, Spring inject [LocalExportFileStorage.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/application/services/export/LocalExportFileStorage.java) lưu vào ổ đĩa; ở môi trường Production, có thể hoán đổi sang `AwsS3ExportFileStorage` mà không cần sửa 1 dòng code nghiệp vụ nào trong `ExportJobWorker`.

**Composition over Inheritance (Ủy thác thay vì kế thừa):**
- Kế thừa tạo ra mối liên kết cực kỳ chặt (Tight Coupling) và phá vỡ tính đóng gói (lớp cha thay đổi có thể làm hỏng toàn bộ lớp con).
- Trong Htech: `OrderServiceImpl` không kế thừa từ `OrderCheckoutHelper` hay `NotificationService`, mà nó chứa (HAS-A) các đối tượng này thông qua Dependency Injection.

---

### Câu 03 - Interface vs Abstract Class: Sự khác biệt bản chất và cải tiến từ Java 8/9

**Câu hỏi:** Phân biệt Interface và Abstract Class? Khi nào nên dùng loại nào? Từ Java 8 và Java 9, Interface đã có thêm những tính năng gì?

**Bản chất kỹ thuật:**
- **Abstract Class:**
  - Đại diện cho quan hệ bản chất "IS-A" (Là một).
  - Có thể có Constructor, có biến trạng thái (State / Instance fields `private int count;`).
  - Một class con chỉ có thể kế thừa (extends) duy nhất 1 abstract class (Đơn kế thừa).
  - Thích hợp làm Base Class chia sẻ logic chung giữa các class có họ hàng gần gũi.
- **Interface:**
  - Đại diện cho quan hệ năng lực/hành vi "CAN-DO" (Có thể làm được).
  - Không có Constructor, không thể lưu trữ instance state (chỉ có hằng số `public static final`).
  - Một class có thể implement nhiều interface (Đa kế thừa hành vi).
  - Thích hợp để thiết kế Contract, phân tách ranh giới hệ thống (Loose Coupling).
- **Cải tiến hiện đại của Interface:**
  - **Java 8 - `default method`:** Cho phép viết body code mặc định trong interface. Mục đích lớn nhất là để mở rộng interface mà không làm gãy (break) hàng ngàn class cũ đang implement nó (ví dụ: thêm method `forEach()`, `stream()` vào `Iterable`/`Collection`).
  - **Java 8 - `static method`:** Cung cấp các hàm tiện ích gắn liền với interface (ví dụ: `Comparator.comparing()`, `List.of()`).
  - **Java 9 - `private method`:** Cho phép chia nhỏ logic dùng chung giữa các default method trong cùng interface, tăng tính đóng gói.

**Liên hệ với Htech:**
- `AbstractAuditingEntity`: Dùng **Abstract Class** vì nó chứa state (`createdAt`, `updatedAt`) và định nghĩa logic JPA `@MappedSuperclass`.
- `OrderService`, `PaymentWebhookSignatureVerifier`: Dùng **Interface** để làm contract giao tiếp, dễ dàng mock khi viết Unit Test.

---

### Câu 04 - Hợp đồng (Contract) giữa `equals()` và `hashCode()` & Cạm bẫy trong JPA Entity

**Câu hỏi:** Tại sao khi override `equals()` bắt buộc phải override cả `hashCode()`? Chuyện gì xảy ra nếu vi phạm? Cần lưu ý gì khi viết `equals/hashCode` cho JPA Entity?

**Bản chất kỹ thuật & Cơ chế băm:**
- **Nguyên lý Hash Bucket trong `HashMap` / `HashSet`:**
  - Khi bạn đưa một object vào `HashSet` hoặc làm Key trong `HashMap`, Java sẽ gọi `key.hashCode()` trước để tính toán vị trí Bucket (ô chứa trong mảng).
  - Nếu bucket đó đang có sẵn object, Java mới gọi `.equals()` để xác định xem object mới có trùng với object cũ hay không.
- **Quy tắc vàng (Contract):**
  1. Nếu `a.equals(b) == true` $\Rightarrow$ Bắt buộc `a.hashCode() == b.hashCode()`.
  2. Nếu `a.hashCode() == b.hashCode()` $\Rightarrow$ Chưa chắc `a.equals(b)` đã bằng true (đây gọi là hiện tượng Hash Collision - xung đột băm).
- **Hậu quả nếu vi phạm:**
  - Nếu override `equals` mà không override `hashCode`: Hai object có cùng giá trị nghiệp vụ (ví dụ 2 đối tượng `User` có cùng `id = 1`) sẽ có 2 giá trị `hashCode()` ngẫu nhiên khác nhau do `Object.hashCode()` mặc định dựa trên địa chỉ vùng nhớ.
  - Khi đưa vào `HashSet`, cả 2 object này sẽ lọt vào 2 bucket khác nhau $\Rightarrow$ `HashSet` bị trùng lặp dữ liệu, hàm `set.contains(user)` trả về `false` dù phần tử đã tồn tại!

**Cạm bẫy "chết người" với JPA Entity:**
- Nếu dùng Lombok `@Data` hoặc `@EqualsAndHashCode` mặc định trên Entity: Lombok sẽ dùng **tất cả các trường** để so sánh.
- Khi Entity vừa khởi tạo (`transient`), `id` bằng `null`. Sau khi gọi `repository.save(entity)`, database sinh ra ID tự động $\Rightarrow$ Giá trị `hashCode()` của entity bị thay đổi giữa trước và sau khi lưu!
- Nếu entity đã lọt vào `HashSet` trước đó, sau khi được gán ID, bạn sẽ **không bao giờ tìm thấy hoặc xóa được nó ra khỏi Set** vì nó nằm sai bucket ban đầu.
- *Quy chuẩn cho JPA Entity:* Chỉ so sánh dựa trên Business Key duy nhất (ví dụ: `orderNumber`, `email`, `slug`) hoặc kiểm tra `id != null && id.equals(other.id)`.

---

### Câu 05 - `ArrayList` vs `LinkedList`: Cấu trúc bộ nhớ, CPU Cache Locality và Hiệu năng

**Câu hỏi:** Phân tích sự khác nhau giữa `ArrayList` và `LinkedList`? Tại sao trong 99% trường hợp thực tế, kể cả khi cần chèn xóa nhiều, các kỹ sư backend vẫn ưu tiên dùng `ArrayList`?

**Bản chất kỹ thuật & Phân tích phần cứng:**
- **ArrayList:**
  - Cài đặt bằng một **Mảng động liên tục (Contiguous Dynamic Array)** trong Heap.
  - Khi mảng đầy, nó tự động mở rộng theo công thức: `newCapacity = oldCapacity + (oldCapacity >> 1)` (Tăng thêm 50% dung lượng) và gọi `System.arraycopy()` để sao chép dữ liệu.
  - *Truy cập ngẫu nhiên:* Cực nhanh $O(1)$ qua index nhờ công thức tính địa chỉ bộ nhớ trực tiếp: `Address = BaseAddress + index * elementSize`.
- **LinkedList:**
  - Cài đặt bằng **Danh sách liên kết đôi (Doubly-linked List)**. Mỗi phần tử là một `Node<E>` riêng lẻ, nằm rải rác trên khắp vùng nhớ Heap, gồm: con trỏ `prev`, con trỏ `next`, và con trỏ trỏ tới dữ liệu `item`.
  - *Tốn bộ nhớ (Memory Overhead):* Mỗi node tốn thêm ít nhất 24 byte chỉ để lưu trữ con trỏ liên kết.
  - *Truy cập ngẫu nhiên:* Chậm $O(N)$, phải duyệt tuần tự từ đầu hoặc từ cuối danh sách.
- **Bí mật CPU Cache Locality (Lý do ArrayList chiến thắng):**
  - CPU hiện đại đọc dữ liệu từ RAM lên **CPU L1/L2 Cache Line** theo từng khối liên tục (64 bytes).
  - Vì các phần tử của `ArrayList` nằm cạnh nhau trên RAM, khi CPU đọc phần tử thứ nhất, nó tự động kéo theo các phần tử tiếp theo vào CPU Cache (Spatial Locality) $\Rightarrow$ Tốc độ duyệt cực kỳ nhanh.
  - Ngược lại, các node của `LinkedList` phân tán rải rác trong Heap, mỗi lần nhảy sang node tiếp theo là một lần bị **CPU Cache Miss**, buộc CPU phải chờ nạp lại dữ liệu từ RAM vật lý (chậm hơn hàng chục lần).

**Liên hệ với Htech:**
- Danh sách sản phẩm, đơn hàng, giỏ hàng trả về cho REST API hoặc xử lý Stream DTO luôn sử dụng `ArrayList` để tận dụng tốc độ duyệt và tối ưu hóa bộ nhớ.

---

### Câu 06 - Checked Exception vs Unchecked Exception: Phân cấp ngoại lệ & Kiến trúc Error Handling

**Câu hỏi:** Phân biệt Checked Exception và Unchecked Exception trong Java? Trong kiến trúc Spring Boot REST API, tại sao xu hướng hiện đại lại ưu tiên dùng Unchecked Exception?

**Bản chất kỹ thuật & Cây phân cấp `Throwable`:**
```
                Throwable
               /                   Error           Exception
       (OOM, SOE)         /                      Checked Exceptions   RuntimeException (Unchecked)
               (IOException, SQL)   (NPE, IllegalArgumentException)
```
- **Error (Lỗi nghiêm trọng cấp hệ thống):** Kế thừa `Error` (`OutOfMemoryError`, `StackOverflowError`). Do môi trường JVM gặp sự cố phần cứng/bộ nhớ. Lập trình viên không nên catch lỗi này vì ứng dụng không thể tự phục hồi.
- **Checked Exception:** Kế thừa trực tiếp từ `Exception` (trừ `RuntimeException`). Trình biên dịch bắt buộc phải xử lý bằng khối `try-catch` hoặc khai báo `throws` ở signature của hàm (ví dụ: `IOException`, `SQLException`).
- **Unchecked Exception:** Kế thừa từ `RuntimeException`. Trình biên dịch không bắt buộc khai báo hay bắt lỗi (ví dụ: `NullPointerException`, `IndexOutOfBoundsException`).

**Tại sao Backend REST API ưu tiên Unchecked Exception:**
- Checked exception làm bẩn chữ ký method (Polluting method signatures). Nếu Repository throw `SQLException`, toàn bộ Service và Controller đều phải khai báo `throws SQLException`, phá vỡ nguyên tắc Abstraction.
- Trong Spring Boot, ngoại lệ nghiệp vụ (`BadRequestException`, `ResourceNotFoundException`, `UnauthorizedException`) được thiết kế kế thừa `RuntimeException`.
- Khi service ném ngoại lệ, transaction sẽ tự động được Spring kích hoạt **Rollback** (mặc định `@Transactional` chỉ tự động rollback với `RuntimeException` và `Error`).
- Ngoại lệ sau đó được bắt gọn tại [GlobalExceptionHandler.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/web/exceptions/GlobalExceptionHandler.java) bằng `@RestControllerAdvice` và chuyển thành format JSON chuẩn mực (`timestamp`, `status`, `message`, `path`).

---

### Câu 07 - Stream API trong Java: Cơ chế Hoạt Động (Lazy Evaluation) & Cạm Bẫy Lạm Dụng

**Câu hỏi:** Stream API hoạt động như thế nào? Phân biệt Intermediate Operation và Terminal Operation? Khi nào nên dùng Stream và khi nào loop `for` truyền thống vượt trội hơn?

**Bản chất kỹ thuật & Cơ chế Lazy Evaluation:**
- **Pipeline của Stream:**
  ```
  Source (Collection/Array) -> Intermediate Operations (0..N) -> Terminal Operation (1)
  ```
- **Intermediate Operations (Xử lý trung gian):** Các hàm như `.filter()`, `.map()`, `.sorted()`, `.flatMap()`. 
  - *Đặc điểm:* Có tính chất **Lazy (Lười biếng)**. Khi bạn gọi `filter()` hay `map()`, Java chưa hề thực thi một phép so sánh hay biến đổi nào cả. Nó chỉ tạo ra một pipeline mô tả logic.
- **Terminal Operations (Thao tác kết thúc):** Các hàm như `.collect()`, `.forEach()`, `.count()`, `.reduce()`, `.findFirst()`.
  - Khi gặp terminal operation, Stream mới bắt đầu kéo (pull) dữ liệu từ nguồn đi qua toàn bộ pipeline trong đúng **1 vòng duyệt duy nhất**.
  - **Short-circuiting:** Các hàm như `findFirst()`, `anyMatch()` có thể ngắt pipeline ngay khi tìm thấy phần tử thỏa mãn mà không cần duyệt hết mảng.
- **So sánh Hiệu năng & Khi nào dùng:**
  - *Dùng Stream khi:* Biến đổi dữ liệu thuần túy (Transform/Mapping DTO), gom nhóm dữ liệu (`Collectors.groupingBy`), tính toán tổng hợp hoặc lọc danh sách ngắn/vừa. Code mang tính khai báo (Declarative), rõ ràng và dễ đọc.
  - *Dùng Loop truyền thống khi:*
    1. Vòng lặp chứa logic nghiệp vụ phức tạp có nhiều nhánh rẽ, cần gọi database hoặc throw checked exception.
    2. Logic cần lệnh `break`, `continue` hoặc cần theo dõi chỉ mục `index`.
    3. Các vòng lặp xử lý số nguyên thủy (primitive types) hoặc dữ liệu siêu lớn cần tối ưu micro-seconds: Loop `for` cơ bản không tốn chi phí khởi tạo Pipeline object và con trỏ lambda, tốc độ thực thi nhanh hơn Stream.
  - *Cạm bẫy cấm kỵ:* Tuyệt đối không thay đổi trạng thái bên ngoài (Side effects) bên trong Stream (ví dụ: dùng `list.stream().forEach(e -> repository.save(e))` là phản mẫu - anti-pattern).

---

### Câu 08 - `Optional` trong Java: Giá Trị Cốt Lõi, Cách Dùng Chuẩn & Các Anti-Patterns

**Câu hỏi:** Lớp `Optional<T>` được sinh ra để giải quyết vấn đề gì? Hãy chỉ ra các lỗi dùng sai phổ biến nhất của lập trình viên và cách sử dụng đúng chuẩn trong Spring Boot?

**Bản chất kỹ thuật:**
- `Optional<T>` là một đối tượng bao bọc (Wrapper Object) có thể chứa một giá trị non-null hoặc rỗng (`Optional.empty()`).
- Mục đích thiết kế: Làm rõ trong Interface/Method Signature rằng phương thức này **có thể không tìm thấy dữ liệu**, ép buộc người gọi phải chủ động xử lý kịch bản rỗng, từ đó loại trừ lỗi ám ảnh `NullPointerException` (NPE).

**Các Anti-Pattern thường gặp của Intern/Fresher:**
1. *Gọi `.get()` trực tiếp không kiểm tra:*
   ```java
   // SAI: Vẫn văng NoSuchElementException nếu rỗng, tệ hơn cả NPE
   User user = userRepository.findByEmail(email).get(); 
   ```
2. *Dùng `if (optional.isPresent())` rồi `.get()`:*
   ```java
   // XẤU: Không khác gì kiểu code kiểm tra `if (user != null)` truyền thống
   Optional<User> opt = userRepository.findById(id);
   if (opt.isPresent()) {
       return opt.get().getName();
   }
   ```
3. *Dùng Optional làm tham số hàm hoặc trường trong Entity (Field/Param):*
   - `Optional` không implement `Serializable`, không thể dùng làm field trong Entity JPA (gây lỗi khi Hibernate serialize cache).
   - Truyền `Optional` vào tham số hàm làm phức tạp code và có nguy cơ caller truyền vào giá trị `null` cho chính biến Optional đó.

**Cách dùng chuẩn mực trong Backend Htech:**
```java
// 1. Chuyển đổi thành Business Exception rõ ràng
User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

// 2. Chuyển đổi dữ liệu chuỗi (Fluent API)
String username = userRepository.findById(id)
    .map(User::getFullName)
    .orElse("Khách vãng lai");

// 3. Thực thi hành động nếu có dữ liệu
userRepository.findByEmail(email).ifPresent(u -> notificationService.sendWelcome(u));
```

---

### Câu 09 - Tính Bất Biến (Immutability), Record Class (Java 14+) vs Mutable JPA Entity

**Câu hỏi:** Tại sao tính bất biến (Immutability) lại quan trọng trong kiến trúc backend? Tại sao JPA Entity lại phải là Mutable, trong khi DTO nên hướng tới Immutable/Record?

**Bản chất kỹ thuật:**
- **Immutable Object:** Là đối tượng mà trạng thái nội tại không thể bị thay đổi sau khi được khởi tạo thành công (tất cả các trường đều là `private final`, không có setter, class là `final`).
- **Lợi ích to lớn:**
  1. **Thread-Safe tuyệt đối:** Nhiều luồng có thể đọc đồng thời mà không bao giờ cần dùng lock hay lo ngại xung đột dữ liệu (Race Condition).
  2. **Tránh Side Effect:** Khi truyền object qua 10 lớp service khác nhau, bạn hoàn toàn yên tâm không có lớp nào vô tình sửa đổi dữ liệu bên trong.
- **Java 14+ Record Classes (Cú pháp hiện đại cho Immutable DTO):**
  ```java
  public record OrderSummaryResponse(UUID id, String orderNumber, BigDecimal totalAmount) {}
  ```
  - Java compiler tự động sinh ra: các trường `private final`, Constructor chuẩn, Getter (dạng `id()`), phương thức `equals()`, `hashCode()` và `toString()` chuẩn xác.
- **Nghịch lý với JPA Entity (Tại sao Entity bắt buộc phải Mutable):**
  - Hibernate/JPA quản lý vòng đời entity theo mô hình **Persistence Context** và cơ chế **Dirty Checking**.
  - Khi transaction kết thúc, Hibernate tự động so sánh trạng thái hiện tại của Entity với snapshot ban đầu. Nếu phát hiện trường nào thay đổi qua setter, nó tự động sinh câu lệnh SQL `UPDATE`.
  - Ngoài ra Hibernate cần gọi Reflection không tham số (`default constructor`) và can thiệp proxy (CGLIB) để nạp dữ liệu từ DB lên entity, do đó Entity không thể thiết kế dạng `Record` hay `immutable`.

---

### Câu 10 - Lombok: Lợi Ích, Cơ Chế Biên Dịch (AST) & Cạm Bẫy Trong JPA / Debug

**Câu hỏi:** Thư viện Lombok hoạt động như thế nào ở tầng biên dịch? Những annotation nào của Lombok bị coi là "nguy hiểm" khi dùng trên JPA Entity và cách phòng tránh?

**Bản chất kỹ thuật:**
- Lombok hoạt động dựa trên cơ chế **Annotation Processing API (JSR 269)**.
- Khi biên dịch bằng `javac`, Lombok hook vào quá trình phân tích cú pháp, can thiệp trực tiếp và sửa đổi cây cú pháp trừu tượng (**AST - Abstract Syntax Tree**) để chèn thêm mã byte-code của Getter, Setter, Builder, Constructor trước khi sinh ra file `.class` cuối cùng. Khi runtime, ứng dụng chạy mã byte-code thật mà không tốn chi phí reflection.
- **Các cạm bẫy nguy hiểm với JPA Entity:**
  1. **Cạm bẫy `@Data`:**
     - `@Data` tự động bao gồm `@ToString` và `@EqualsAndHashCode`.
     - Nếu 2 Entity có quan hệ hai chiều `@OneToMany` và `@ManyToOne` (ví dụ: `Order` chứa list `OrderItem`, và `OrderItem` chứa `Order`), phương thức `toString()` do Lombok sinh ra sẽ gọi đệ quy lẫn nhau $\Rightarrow$ Gây lỗi tràn bộ nhớ stack: `java.lang.StackOverflowError`.
     - *Khắc phục:* Thay `@Data` bằng `@Getter`, `@Setter` riêng biệt. Luôn thêm `@ToString.Exclude` trên các trường liên kết quan hệ.
  2. **Cạm bẫy `@Builder` làm mất Default Constructor:**
     - Khi dùng `@Builder`, Lombok mặc định không sinh Constructor không tham số (`no-arg constructor`).
     - Hibernate bắt buộc phải có no-arg constructor để khởi tạo entity từ CSDL. Nếu thiếu, app sẽ văng lỗi `InstantiationException`.
     - *Khắc phục:* Luôn đi kèm bộ 3 annotation: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.

---

### Câu 10.1 - Java Memory Model: Stack vs Heap vs Metaspace & Bản Chất Pass-By-Value

**Câu hỏi:** Java là Pass-by-Value hay Pass-by-Reference? Hãy giải thích cơ chế cấp phát bộ nhớ Stack và Heap khi truyền một đối tượng vào một method?

**Bản chất kỹ thuật:**
- **Khẳng định chắc chắn:** Trong Java, **100% MỌI THỨ ĐỀU LÀ PASS-BY-VALUE (Truyền theo giá trị)**. Không hề có pass-by-reference.
- **Cơ chế phân bổ bộ nhớ:**
  + Khi bạn khởi tạo: `User user = new User("Hội");`
  + Một đối tượng `User` được tạo ra trên vùng nhớ **Heap** (giả sử tại địa chỉ ô nhớ `0x1234`).
  + Biến con trỏ `user` nằm trên **Stack** của luồng hiện tại, lưu giá trị là con số địa chỉ `0x1234`.
- **Chuyện gì xảy ra khi gọi hàm:**
  ```java
  public void modify(User u) {
      u.setName("Nam");          // Thay đổi được thuộc tính bên trong
      u = new User("Khác");     // KHÔNG làm thay đổi đối tượng của hàm gọi ngoài!
  }
  ```
  - Khi gọi `modify(user)`, Java tạo ra một bản sao (copy giá trị) của biến con trỏ trên Stack: biến `u` nhận giá trị copy là địa chỉ `0x1234`.
  - Khi gọi `u.setName("Nam")`: Biến `u` lần theo địa chỉ `0x1234` trên Heap để sửa thuộc tính $\Rightarrow$ Đối tượng thật bị thay đổi.
  - Khi gọi `u = new User("Khác")`: Biến `u` được gán trỏ sang địa chỉ mới `0x5678`. Bản thân biến `user` ở method cha bên ngoài vẫn trỏ vào địa chỉ `0x1234` ban đầu! $\Rightarrow$ Chứng minh Java hoàn toàn truyền theo bản sao giá trị của tham chiếu (Copy of reference value).

---

### Câu 10.2 - String Constant Pool, Immutability & So Sánh `String` vs `StringBuilder` vs `StringBuffer`

**Câu hỏi:** Tại sao lớp `String` trong Java lại là immutable? String Constant Pool là gì? So sánh hiệu năng và độ an toàn đa luồng giữa String, StringBuilder và StringBuffer?

**Bản chất kỹ thuật & String Constant Pool:**
- **Tính Immutability của String:**
  - Lớp `String` được đánh dấu là `final`, mảng byte lưu trữ nội dung `private final byte[] value` không thể bị gán lại sau khi tạo.
  - *Lợi ích:*
    1. **Bảo mật (Security):** String thường dùng lưu trữ username, password, connection URL, tham số mạng. Nếu mutable, một luồng khác có thể can thiệp sửa đổi nội dung giữa chừng gây lỗ hổng bảo mật.
    2. **Caching & Tối ưu RAM:** Nhờ immutable, Java mới có thể xây dựng vùng nhớ **String Constant Pool** trong Heap.
- **String Constant Pool hoạt động ra sao:**
  - `String s1 = "hello"; String s2 = "hello";`
  - Java kiểm tra trong Pool xem đã có chuỗi `"hello"` chưa. Nếu có rồi, nó cho `s2` trỏ chung vào ô nhớ của `s1`. Lúc này `s1 == s2` trả về `true`.
  - `String s3 = new String("hello");` $\Rightarrow$ Bắt buộc cấp phát một ô nhớ mới ngoài Heap. Lúc này `s1 == s3` trả về `false`, nhưng `s1.equals(s3)` trả về `true`.
- **So sánh Bộ 3 Xử Lý Chuỗi:**
  - `String`: Dùng toán tử cộng chuỗi `s += "a"` trong vòng lặp sẽ liên tục tạo ra hàng ngàn object rác trên Heap, gây chậm hệ thống và bắt GC dọn dẹp liên tục.
  - `StringBuilder`: Sử dụng mảng ký tự động có thể thay đổi kích thước (Mutable), không tạo object mới khi nối chuỗi (`append`). **Tốc độ nhanh nhất, nhưng KHÔNG an toàn trong môi trường đa luồng (Non-thread-safe)**. Phù hợp xử lý chuỗi cục bộ trong method.
  - `StringBuffer`: Giống hệt `StringBuilder` nhưng các phương thức đều có từ khóa `synchronized`. **An toàn đa luồng (Thread-safe), nhưng tốc độ chậm hơn StringBuilder** do chi phí khóa/mở lock.

---

### Câu 10.3 - `HashMap` Under The Hood: Thuật Toán Băm, Bucket Indexing & Cơ Chế Treeify

**Câu hỏi:** Hãy trình bày chi tiết từng bước khi thực thi hàm `map.put(key, value)` trong Java 8+? Thuật toán tính index, chỉ số loadFactor và điều kiện chuyển đổi thành Red-Black Tree là gì?

**Bản chất kỹ thuật & Mã nguồn Java `HashMap`:**
1. **Bước 1: Tính toán Hash Code:**
   - Tránh việc phân bố hash không đều:
     `int hash = (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);`
   - Dịch bit phải 16 bit và thực hiện phép XOR để các bit cao tác động xuống bit thấp, giảm tối đa tỷ lệ trùng index khi mảng có kích thước nhỏ.
2. **Bước 2: Xác định vị trí Bucket trong mảng:**
   - `index = (n - 1) & hash` (với `n` là độ dài mảng `Node<K,V>[] table`, luôn là lũy thừa của 2 như 16, 32, 64...).
   - Phép toán bitwise AND `(n - 1) & hash` tương đương với phép chia lấy dư `hash % n` nhưng tốc độ tính toán của CPU nhanh hơn gấp nhiều lần.
3. **Bước 3: Xử lý ghi dữ liệu:**
   - Nếu bucket tại `index` đang trống: Tạo `Node<K,V>` mới và đặt vào mảng.
   - Nếu bucket đã có phần tử (Collision):
     + Duyệt qua danh sách: Nếu tìm thấy key có cùng hash và `key.equals(existingKey) == true` $\Rightarrow$ Ghi đè giá trị cũ bằng giá trị mới.
     + Nếu không trùng key: Chèn node mới vào cuối danh sách liên kết (Tail insertion - Java 8, thay vì chèn đầu như Java 7 để tránh Deadlock khi resize).
4. **Bước 4: Cơ chế Treeify (Chuyển thành Cây Đỏ - Đen):**
   - Khi độ dài LinkedList trong 1 bucket đạt tới ngưỡng **`TREEIFY_THRESHOLD = 8`** VÀ tổng dung lượng mảng `capacity >= 64`:
   - LinkedList tự động được chuyển hóa thành **Red-Black Tree (TreeNode)** $\Rightarrow$ Kéo giảm thời gian tìm kiếm từ $O(N)$ xuống $O(\log N)$.
   - Nếu sau này các phần tử bị xóa bớt chỉ còn **`UNTREEIFY_THRESHOLD = 6`**, cây sẽ được hoàn nguyên trở lại thành LinkedList để tiết kiệm bộ nhớ.
5. **Bước 5: Mở rộng mảng (Resizing):**
   - Mặc định dung lượng khởi tạo `DEFAULT_INITIAL_CAPACITY = 16`, hệ số tải `DEFAULT_LOAD_FACTOR = 0.75`.
   - Ngưỡng mở rộng (Threshold) = $16 	imes 0.75 = 12$.
   - Khi số lượng phần tử vượt quá 12, mảng tự động nhân đôi kích thước lên 32 và thực hiện rehash lại các phần tử.

---

### Câu 10.4 - `ConcurrentHashMap` vs `Collections.synchronizedMap` vs `Hashtable`

**Câu hỏi:** Khi cần lưu trữ dữ liệu dạng Key-Value trong môi trường đa luồng (Multi-threading), tại sao không nên dùng `Hashtable` hay `Collections.synchronizedMap` mà bắt buộc phải dùng `ConcurrentHashMap`?

**Bản chất kỹ thuật:**
- **Hashtable & `Collections.synchronizedMap` (Khóa toàn bảng - Coarse-Grained Lock):**
  - Cả 2 thư viện này đều sử dụng cơ chế khóa toàn cục: Tất cả các method `get()`, `put()`, `remove()` đều được bọc bởi từ khóa `synchronized` trên toàn bộ đối tượng Map.
  - *Hậu quả:* Tại một thời điểm, chỉ có **duy nhất 1 luồng** được phép đọc hoặc ghi. Nếu có 100 luồng cùng truy cập, 99 luồng kia bị treo chờ (Blocked). Throughput của hệ thống tụt dốc thảm hại.
- **ConcurrentHashMap trong Java 8+ (Khóa phân đoạn cực mịn - Fine-Grained Lock):**
  - **Thao tác Đọc (`get`):** Hoàn toàn **KHÔNG DÙNG LOCK**. Nó sử dụng biến `volatile Node.val` và `volatile Node.next` đảm bảo tính hiển thị tức thời (Visibility) giữa các luồng. Hàng ngàn luồng có thể đọc đồng thời với tốc độ $O(1)$.
  - **Thao tác Ghi (`put`):**
    + Nếu bucket tại vị trí index đang rỗng: Sử dụng thuật toán **CAS (Compare-And-Swap)** không khóa của CPU để chèn node mới.
    + Nếu bucket đã có phần tử: Chỉ khóa (lock) **duy nhất node đầu tiên của bucket đó** (`synchronized (node)`), các bucket khác trong mảng vẫn cho phép các luồng khác ghi bình thường.
  - Nhờ cơ chế này, `ConcurrentHashMap` đạt hiệu năng xử lý đồng thời gấp hàng trăm lần so với `Hashtable`.

---

### Câu 10.5 - `try-with-resources` & Giao Diện `AutoCloseable`

**Câu hỏi:** `try-with-resources` trong Java 7+ giải quyết vấn đề gì so với khối `try-catch-finally` truyền thống? Làm thế nào để một class tự định nghĩa có thể dùng được trong cú pháp này?

**Bản chất kỹ thuật & Phân tích Bytecode:**
- **Vấn đề của `finally` truyền thống:**
  ```java
  // Rườm rà và dễ rò rỉ tài nguyên (Resource Leak)
  FileInputStream fis = null;
  try {
      fis = new FileInputStream("file.txt");
  } finally {
      if (fis != null) {
          try { fis.close(); } catch (IOException e) { /* Nuốt lỗi */ }
      }
  }
  ```
  Nếu trong khối `try` ném exception A, và trong khối `finally` lệnh `close()` lại ném tiếp exception B, thì exception B sẽ **nuốt chửng** exception A ban đầu, khiến việc debug nguyên nhân gốc rễ trở nên bất khả thi.
- **Cơ chế `try-with-resources`:**
  ```java
  try (FileInputStream fis = new FileInputStream("file.txt")) {
      // Đọc file
  } // Tự động gọi fis.close() tại đây bất kể có lỗi hay không
  ```
  - Bất kỳ class nào implement interface **`java.lang.AutoCloseable`** (chỉ có duy nhất 1 hàm `void close() throws Exception`) đều có thể đặt vào dấu ngoặc đơn của `try (...)`.
  - **Suppressed Exceptions:** Nếu cả khối try và hàm `close()` đều ném ngoại lệ, Java sẽ giữ nguyên ngoại lệ chính ban đầu và đính kèm ngoại lệ của `close()` vào danh sách `Throwable.getSuppressed()`, bảo toàn 100% vết lỗi để điều tra.

---

### Câu 10.6 - Java Generics: Bản Chất Type Erasure & Cú Pháp Bounded Wildcards (`? extends` vs `? super`)

**Câu hỏi:** Cơ chế Type Erasure (Xóa kiểu) trong Java Generics là gì? Nguyên tắc PECS ("Producer Extends, Consumer Super") được hiểu như thế nào?

**Bản chất kỹ thuật:**
- **Type Erasure (Xóa kiểu thời điểm biên dịch):**
  - Để đảm bảo tương thích ngược (Backward Compatibility) với các phiên bản Java 1.4 trở về trước, Generics trong Java chỉ tồn tại ở thời điểm **Biên dịch (Compile-time)** để kiểm tra an toàn kiểu dữ liệu.
  - Khi trình biên dịch chuyển code thành Byte-code `.class`, toàn bộ thông tin kiểu generic như `<String>`, `<User>` đều bị **xóa bỏ (erased)** và thay thế bằng kiểu thô `Object` (hoặc upper bound class), đồng thời tự động chèn các lệnh ép kiểu ngầm định.
  - *Hệ quả:* Không thể làm các thao tác như `new T()`, `new T[10]`, hoặc `instanceof List<String>` tại runtime.
- **Nguyên tắc PECS (Producer Extends, Consumer Super):**
  - **`? extends T` (Upper Bounded Wildcard - Producer):**
    + Dùng khi bạn chỉ muốn **LẤY DỮ LIỆU RA** từ collection (Collection đóng vai trò là nhà sản xuất - Producer).
    + `List<? extends Number> list`: Bạn có thể đọc các phần tử ra dưới dạng kiểu `Number`, nhưng **KHÔNG THỂ THÊM** bất kỳ phần tử nào vào list (trừ `null`) vì compiler không biết chính xác kiểu cụ thể bên trong là `Integer`, `Double` hay `Long`.
  - **`? super T` (Lower Bounded Wildcard - Consumer):**
    + Dùng khi bạn muốn **ĐƯA DỮ LIỆU VÀO** collection (Collection đóng vai trò là nơi tiêu thụ - Consumer).
    + `List<? super Integer> list`: Bạn có thể an toàn thêm một số `Integer` vào list, vì danh sách này chắc chắn chấp nhận kiểu `Integer` hoặc cha của nó (`Number`, `Object`).

---

### Câu 10.7 - Đa Luồng Cơ Bản: Vòng Đời Thread, `volatile`, `synchronized` và `AtomicInteger`

**Câu hỏi:** Hãy phân biệt cơ chế đồng bộ hóa giữa từ khóa `volatile`, khối `synchronized` và các lớp nguyên tử `AtomicInteger`? Khi nào dùng loại nào?

**Bản chất kỹ thuật & Tầng phần cứng CPU Cache:**
- **Hiện tượng CPU Cache Visibility Problem:**
  - Mỗi lõi CPU có các bộ nhớ đệm riêng siêu nhanh (L1, L2 Cache). Khi một luồng đọc biến `flag`, nó copy giá trị vào L1 Cache của lõi đó.
  - Nếu Luồng 1 ở Core 1 sửa `flag = true` nhưng chỉ ghi vào L1 Cache của Core 1 mà chưa flush về RAM, Luồng 2 ở Core 2 sẽ mãi mãi nhìn thấy `flag = false` (Treo vòng lặp vô tận).
- **1. Từ khóa `volatile`:**
  - *Cơ chế:* Ép buộc CPU phải đọc và ghi trực tiếp giá trị của biến từ **Bộ nhớ chính (RAM)**, vô hiệu hóa việc lưu trên CPU Cache riêng. Đồng thời tạo ra **Memory Barrier** ngăn chặn CPU tự ý đảo thứ tự lệnh (Instruction Reordering).
  - *Giới hạn:* Chỉ đảm bảo tính nhìn thấy (Visibility), **KHÔNG đảm bảo tính nguyên tử (Atomicity)**. Thao tác `count++` gồm 3 bước (Đọc -> Tăng -> Ghi), nếu dùng `volatile` vẫn bị Race Condition như thường.
- **2. Từ khóa `synchronized` (Pessimistic Locking):**
  - Đảm bảo cả 2 tính chất: **Visibility** và **Atomicity**.
  - Luồng muốn thực thi phải chiếm được khóa màn hình (Monitor Lock) trên đối tượng. Các luồng khác phải chuyển sang trạng thái `BLOCKED` và ngủ chờ. Chi phí context switch của OS tương đối cao.
- **3. Các lớp nguyên tử `AtomicInteger`, `AtomicLong` (Lock-Free with CAS):**
  - Sử dụng lệnh phần cứng của CPU mang tên **CAS (Compare-And-Swap)** thông qua lớp `Unsafe`.
  - Cơ chế: "Tôi hy vọng giá trị hiện tại là A, nếu đúng là A thì hãy đổi thành B. Nếu không phải A (do luồng khác đã nhanh tay sửa), tôi sẽ không bị khóa (non-blocking) mà tự động lặp lại vòng thử (retry loop) cho đến khi thành công".
  - Đạt hiệu năng cực cao trong các bài toán đếm request, sequence id mà không tốn chi phí khóa luồng.

---

### Câu 10.8 - `ThreadLocal` Trong Java: Cơ Chế, Ứng Dụng Trong Spring & Nguy Cơ Rò Rỉ Bộ Nhớ

**Câu hỏi:** `ThreadLocal` hoạt động như thế nào? Spring Security sử dụng nó ở đâu? Tại sao việc sử dụng `ThreadLocal` kết hợp với Thread Pool lại có nguy cơ gây Memory Leak nghiêm trọng?

**Bản chất kỹ thuật:**
- **Cơ chế:** `ThreadLocal<T>` cung cấp các biến có phạm vi cục bộ riêng cho từng luồng (Thread-confined). Mỗi thread khi truy cập vào biến `ThreadLocal` sẽ có một bản sao độc lập của riêng mình, không luồng nào nhìn thấy hay can thiệp vào dữ liệu của luồng khác.
- **Cấu trúc lưu trữ bên dưới:**
  - Bản thân đối tượng `Thread` có một trường nội tại: `ThreadLocal.ThreadLocalMap threadLocals`.
  - Dữ liệu thực chất được lưu bên trong chính đối tượng `Thread` hiện tại, với Key là con trỏ yếu (WeakReference) trỏ tới đối tượng `ThreadLocal`, còn Value là object dữ liệu thật của bạn.
- **Ứng dụng trong Spring Boot:**
  - [SecurityContextHolder.java](file:///Users/hoinguyen/Documents/SpringFW/DATN_HN/server/src/main/java/com/hoz/hozitech/security/JwtAuthenticationFilter.java): Lưu trữ thông tin đăng nhập và quyền hạn của User đang gọi request (`Authentication`).
  - `@Transactional`: Lưu trữ kết nối CSDL hiện tại (`Connection`) để đảm bảo tất cả repository trong cùng một request đều dùng chung 1 database transaction duy nhất.
- **Nguy cơ Memory Leak với Thread Pool:**
  - Trong môi trường Web (Tomcat/AsyncPool), các Thread **không bị tiêu hủy** sau khi kết thúc request mà được đưa trở lại Pool để tái sử dụng cho request sau.
  - Key của `ThreadLocalMap` là `WeakReference`, có thể bị GC dọn dẹp khi hết tham chiếu mạnh, nhưng **Value lại là Strong Reference**.
  - Nếu kết thúc request mà lập trình viên quên gọi **`threadLocal.remove()`**, đối tượng Value sẽ bị kẹt mãi mãi trong bộ nhớ của Thread đó trong Pool $\Rightarrow$ Gây ra rò rỉ bộ nhớ (Memory Leak) và nguy hiểm hơn là **Request của Người dùng B có thể đọc nhầm dữ liệu Security của Người dùng A!**

---

### Câu 10.9 - `Comparable` vs `Comparator`: Khác Biệt & Cách Dùng Lambda Expression

**Câu hỏi:** Phân biệt `Comparable` và `Comparator` trong Java? Khi nào dùng cái nào? Viết code sắp xếp danh sách sản phẩm theo giá tăng dần, nếu bằng giá thì sắp theo tên?

**Bản chất kỹ thuật:**
- **`Comparable<T>` (Sắp xếp tự nhiên - Natural Ordering):**
  - Nằm trong package `java.lang`.
  - Phải được implement trực tiếp bên trong class của Entity/Model thông qua phương thức `int compareTo(T o)`.
  - Định nghĩa quy tắc sắp xếp mặc định vĩnh viễn cho class đó.
- **`Comparator<T>` (Sắp xếp tùy biến - Custom Ordering):**
  - Nằm trong package `java.util`.
  - Tách rời hoàn toàn khỏi class của Entity. Định nghĩa chiến lược sắp xếp độc lập qua phương thức `int compare(T o1, T o2)`.
  - Cho phép tạo ra vô số quy tắc sắp xếp khác nhau mà không cần sửa đổi class gốc (tuân thủ Open/Closed Principle).
- **Ví dụ sắp xếp đa tiêu chí chuẩn Java 8+:**
  ```java
  // Sắp xếp danh sách Product: Giá tăng dần, nếu trùng giá sắp theo Tên A-Z
  List<Product> products = getProducts();
  products.sort(
      Comparator.comparing(Product::getPrice)
                .thenComparing(Product::getName)
  );
  ```

---
## 2. Spring Boot, REST API, Layered Architecture

### Câu 11 - Spring Boot giúp gì so với Spring truyền thống?

**Câu hỏi:** Vì sao Spring Boot phù hợp cho backend Htech?

**Đáp án mẫu:** Spring Boot cung cấp auto-configuration, embedded server, starter dependencies và convention-over-configuration, giúp khởi tạo ứng dụng nhanh hơn Spring truyền thống. Nó vẫn giữ được sức mạnh của Spring ecosystem như DI, MVC, Security, Data JPA, Validation và Actuator. Với Htech, Spring Boot giúp tổ chức backend REST API và các module nghiệp vụ một cách rõ ràng.

**Liên hệ với Htech:** Backend có controller, service, repository, config, security, WebSocket và i18n theo cấu trúc Spring Boot.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu giá trị thực tế của framework, không chỉ biết tạo project.

### Câu 12 - Dependency Injection là gì?

**Câu hỏi:** Dependency Injection trong Spring hoạt động như thế nào?

**Đáp án mẫu:** Dependency Injection là cách framework tạo và đưa dependency vào class thay vì class từ khởi tạo bằng `new`. Spring quản lý bean trong container, tiêm dependency qua constructor, setter hoặc field. Constructor injection thường được khuyến nghị vì rõ ràng, dễ test và đảm bảo dependency bắt buộc được cung cấp.

**Liên hệ với Htech:** Các service như `OrderServiceImpl` nhận repository, helper, notification service qua constructor, giúp tách trách nhiệm và để mock khi test.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu IoC/DI, một nền tảng quan trọng của Spring.

### Câu 13 - Controller, Service, Repository nên phần chia trách nhiệm thế nào?

**Câu hỏi:** Trong layered architecture, mỗi lớp nên làm gì?

**Đáp án mẫu:** Controller nhận request, validate đầu vào cơ bản, lấy principal/path/query và trả response. Service chứa business logic, transaction, rule nghiệp vụ và điều phối repository/helper. Repository truy cập database, thực hiện query và persistence. Cách chia này giúp code dễ đọc, dễ test và tránh controller bi đầy logic.

**Liên hệ với Htech:** Luồng checkout nằm trong `OrderServiceImpl` và helper liên quan, controller chỉ gọi service; các repository như `OrderRepository`, `CartRepository` phụ trách truy vấn.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng tổ chức code backend theo lớp.

### Câu 14 - REST API nên thiết kế endpoint như thế nào?

**Câu hỏi:** Một REST API tốt cần nhưng yêu to gì?

**Đáp án mẫu:** REST API nên dùng HTTP method đúng nghĩa: GET đọc, POST tạo/xử lý action, PUT cập nhật toàn phần, PATCH cập nhật một phần, DELETE xóa. Endpoint nên đặt theo resource, response có status code phù hợp, format lỗi thống nhất, pagination/filter/sort rõ ràng. Nên tránh endpoint quá mơ hồ hoặc trả về dữ liệu thừa.

**Liên hệ với Htech:** Htech có các endpoint `/api/v1/products`, `/api/v1/cart`, `/api/v1/orders`, `/admin/api/v1/**` tách public/user/admin rõ ràng.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức về API contract và khả năng làm việc với frontend.

### Câu 15 - DTO và Entity khác nhau thế nào?

**Câu hỏi:** Vì sao không nên trả trực tiếp entity JPA ra frontend?

**Đáp án mẫu:** Entity đại diện cho bảng/database và quan hệ persistence, còn DTO đại diện cho dữ liệu request/response cần trao đổi qua API. Tra trực tiếp entity có thể lộ trường nhạy cảm, gây vòng lặp JSON do quan hệ hai chiều, làm frontend phụ thuộc vào schema DB. DTO giúp kiểm soát contract, validate đầu vào và thay đổi database mà ít ảnh hưởng client.

**Liên hệ với Htech:** Backend có request/response DTO cho product, order, user, coupon, checkout; service map entity sang response phù hợp.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu boundary giữa persistence model và API model.

**Lỗi cần tránh:** Dùng entity JPA làm response trực tiếp và cho rằng như vậy "nhanh hơn" mà bỏ qua rủi ro lộ dữ liệu, vòng lặp JSON và coupling schema.

### Câu 16 - Validation nên đặt ở đâu?

**Câu hỏi:** Spring Boot hỗ trợ validate request như thế nào?

**Đáp án mẫu:** Spring Boot dùng Bean Validation/Jakarta Validation với annotation như `@NotNull`, `@Size`, `@Email`, `@Valid`. Validation format/cơ bản nên đặt ở DTO request, business validation nên đặt trong service vì cần truy cập DB và rule nghiệp vụ. Lỗi validation nên được global handler chuyển thành response rõ ràng cho frontend.

**Liên hệ với Htech:** Checkout cần validate address thuộc user, danh sách item không rỗng, payment method được bật, voucher hợp lệ; các rule này nằm ở service vì cần truy vấn dữ liệu.

**Điểm nhà tuyển dụng đang kiểm tra:** Phân biệt validation kỹ thuật và validation nghiệp vụ.

### Câu 17 - `@Transactional` dùng để làm gì?

**Câu hỏi:** Vì sao checkout cần transaction?

**Đáp án mẫu:** `@Transactional` đảm bảo các thao tác database trong một method thành một đơn vị atomic: hoặc tất cả thành công, hoặc rollback khi lỗi. Checkout cần tạo order, trừ tồn kho, áp voucher, xóa cart, tạo history/notification. Nếu một bước lỗi mà không có transaction, dữ liệu có thể bị lệch, ví dụ đã trừ tồn kho nhưng không tạo được order.

**Liên hệ với Htech:** `OrderServiceImpl.checkout` là luồng cần transaction vì cập nhật nhiều bảng và có nhiều rule nghiệp vụ.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu tính nhất quán dữ liệu trong backend.

**Lỗi cần tránh:** Nói transaction chỉ để "chạy nhanh hơn"; thực tế transaction chủ yếu đảm bảo tính atomic/consistent cho các thao tác ghi liên quan.

### Câu 18 - Global Exception Handler có vai trò gì?

**Câu hỏi:** Tại sao nên có global exception handler trong REST API?

**Đáp án mẫu:** Global exception handler gom logic xử lý lỗi về một nơi, giúp response lỗi thống nhất, đúng HTTP status và không lộ stack trace. Nó cùng giúp mapping business exception sang mã lỗi, message key, localized message. Nếu không có, mỗi controller phải try-catch lặp lại và dễ sai format.

**Liên hệ với Htech:** Htech có exception handler để xử lý `BusinessException`, not found, unauthorized, invalid param và lỗi validation.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng xây dựng API có trải nghiệm lỗi tốt cho frontend.

### Câu 19 - Pagination trong API cần lưu ý gì?

**Câu hỏi:** Vì sao danh sách sản phẩm/đơn hàng nên phân trang?

**Đáp án mẫu:** Pagination giúp giảm tải database, network và frontend rendering. API nên nhận page, size, sort/filter và trả về metadata như total, current page, last page. Cần giới hạn size tối đa để tránh client lấy quá nhiều dữ liệu trong một request.

**Liên hệ với Htech:** Các màn hình admin sản phẩm, đơn hàng, khách hàng, feedback và danh sách product public cần pagination để vận hành ổn với dữ liệu lớn hơn.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy performance cơ bản trong API.

### Câu 20 - Mapper trong backend có vai trò gì?

**Câu hỏi:** Bạn mapping entity sang DTO bằng cách nào?

**Đáp án mẫu:** Mapper chuyển đổi giữa entity và DTO, giúp controller/service không bị lặp code convert. Có thể dùng thư viện như ModelMapper/MapStruct hoặc viết manual mapping. Manual mapping rõ ràng hơn với logic phức tạp, còn auto mapper nhanh với object đơn giản. Quan trọng là không để mapping làm ẩn mất business rule.

**Liên hệ với Htech:** Backend có `OrderResponseMapper` và ModelMapper config; order response cần mapping chi tiết item, status, payment và snapshot địa chỉ.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu vì sao mapping là boundary quan trọng giữa domain và API.

### Câu 21 - CORS là gì và vì sao backend cần cấu hình?

**Câu hỏi:** Khi frontend React gọi Spring Boot API khác port/domain, vì sao cần CORS?

**Đáp án mẫu:** CORS là cơ chế browser kiểm soát request cross-origin. Nếu frontend chạy `localhost:3000` và backend chạy port khác, browser sẽ chặn request nếu server không cho phép origin đó. Backend cần cấu hình allowed origins, methods, headers và credentials nếu dùng auth header/cookie.

**Liên hệ với Htech:** Server đọc `ALLOWED_ORIGINS`/allowed origins để frontend có thể gọi API và WebSocket từ domain được phép.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu lỗi frontend-backend thực tế khi tích hợp.

### Câu 22 - i18n backend có lợi ích gì?

**Câu hỏi:** Vì sao API có thể cần localized response/message?

**Đáp án mẫu:** i18n giúp ứng dụng hỗ trợ nhiều ngôn ngữ cho message thành công/lỗi. Backend có thể trả message key hoặc message đã localize dựa trên locale request. Điều này hữu ích khi frontend phục vụ người dùng Việt/Anh và cần thông báo nhất quán.

**Liên hệ với Htech:** Htech có i18n tiếng Việt/tiếng Anh ở backend và frontend, giúp response lỗi/notification phù hợp ngôn ngữ.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức về trải nghiệm người dùng và API message design.

### Câu 23 - Spring Scheduler/Async dùng khi nào?

**Câu hỏi:** Khi nào nên chạy xử lý async thay vì xử lý ngay trong request?

**Đáp án mẫu:** Nên dùng async cho tác vụ tốn thời gian hoặc không cần client đợi ngay, như export file lớn, gửi email, cleanup job. Scheduler dùng cho tác vụ lặp lại theo thời gian, như đồng bộ trạng thái khuyến mãi hoặc hủy đơn pending quá hạn. Cần quản lý trạng thái job và log lỗi dễ vận hành.

**Liên hệ với Htech:** Htech có async export job, scheduler hủy stale pending order và đồng bộ promotion status.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu xử lý nền và tránh block request.

### Câu 24 - Actuator dùng để làm gì?

**Câu hỏi:** Spring Boot Actuator giúp gì trong vận hành?

**Đáp án mẫu:** Actuator cung cấp endpoint quan sát ứng dụng như health, metrics, info. Trong môi trường thật, nó giúp load balancer/monitoring biết service còn sống hay không. Tuy nhiên cần bảo mật endpoint actuator, không nên mở tất cả thông tin nhạy cảm ra public.

**Liên hệ với Htech:** Server có dependency actuator, có thể dùng health check khi triển khai backend.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy operability cơ bản.

### Câu 25 - Nếu controller quá đầy logic thì vấn đề là gì?

**Câu hỏi:** Dấu hiệu nào cho thấy controller đang làm quá nhiều?

**Đáp án mẫu:** Controller quá đầy logic khi nó truy cập nhiều repository, tính toán nghiệp vụ, xử lý transaction, gửi notification và mapping phức tạp. Điều này làm code khó test, khó tái sử dụng và dễ sai khi thêm endpoint khác. Nên đưa logic vào service/helper, controller chỉ là lớp giao tiếp HTTP.

**Liên hệ với Htech:** Các flow lớn như checkout, payment webhook, export, chatbot orchestration nên nằm trong service/controller riêng có boundary rõ.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng nhận diện code smell trong backend.

---

## 3. JPA, PostgreSQL, Transaction, Query

### Câu 26 - JPA/Hibernate giải quyết vấn đề gì?

**Câu hỏi:** Vì sao dùng JPA thay vì viết SQL thủ công cho mỗi thao tác?

**Đáp án mẫu:** JPA là ORM giúp map object Java với bảng database, quản lý entity lifecycle, relationship, dirty checking và transaction. Nó giảm boilerplate CRUD và giúp code làm việc ở mức domain hơn. Tuy nhiên với query phức tạp, báo cáo, dashboard, export, vẫn có lúc cần JPQL/native SQL hoặc custom repository để tối ưu.

**Liên hệ với Htech:** Htech dùng Spring Data JPA cho entity/repository, đồng thời có query riêng cho dashboard, export và product summary.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu lợi ích và giới hạn của ORM.

### Câu 27 - Entity relationship trong e-commerce thường có nhưng quan hệ nào?

**Câu hỏi:** Hãy mô tả quan hệ giữa User, Order, OrderItem, ProductVariant.

**Đáp án mẫu:** Một user có nhiều order. Một order có nhiều order item. Mỗi order item tham chiếu product variant đã mua và lưu snapshot giá/tên tại thời điểm đặt hàng. Product có nhiều variant, variant có stock, SKU và thuộc tính. Cần thiết kế để lịch sử đơn hàng không bị thay đổi khi giá sản phẩm sau này thay đổi.

**Liên hệ với Htech:** Htech có entity `Order`, `OrderItem`, `Product`, `ProductVariant`, `User`, giúp xử lý đơn hàng và biến thể sản phẩm công nghệ.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy database schema cho bài toán thương mại điện tử.

### Câu 28 - Lazy loading và eager loading khác nhau thế nào?

**Câu hỏi:** Lazy loading có thể gây lỗi gì trong REST API?

**Đáp án mẫu:** Lazy loading chỉ tải relationship khi truy cập, giúp giảm query ban đầu. Eager loading tại ngây cùng entity chính, để dùng nhưng có thể lấy dữ liệu thừa. Lazy loading có thể gây `LazyInitializationException` nếu truy cập sau khi session đã động, hoặc gây N+1 query nếu duyệt danh sách và truy cập relation từng dòng.

**Liên hệ với Htech:** Khi tra product/order response, backend nên fetch dùng relation cần thiết hoặc mapping trong transaction, không serialize entity trực tiếp.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu vấn đề thực tế khi dùng JPA.

### Câu 29 - N+1 query là gì?

**Câu hỏi:** Làm sao phát hiện và giảm N+1 query?

**Đáp án mẫu:** N+1 xảy ra khi lấy N entity rồi mỗi entity lại trigger thêm một query để lấy relation, tổng cộng 1 + N query. Có thể giảm bằng fetch join, entity graph, batch size, projection DTO hoặc query custom. Cần xem log SQL/metrics để phát hiện.

**Liên hệ với Htech:** Danh sách order/admin dashboard/product list nếu lấy kem item, image, variant không cẩn thận có thể bị N+1 và chậm khi dữ liệu lớn.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức performance database khi dùng ORM.

### Câu 30 - Transaction isolation là gì?

**Câu hỏi:** Tại sao checkout đồng thời cần quan tâm isolation/locking?

**Đáp án mẫu:** Transaction isolation quy định mức độ một transaction nhìn thấy thay đổi của transaction khác. Trong checkout, nếu nhiều người mua cùng một variant stock thấp, cần tránh bán vượt tồn kho. Có thể dùng lock row, optimistic/pessimistic locking hoặc advisory lock tùy bài toán.

**Liên hệ với Htech:** Checkout có cơ chế Idempotency và lock theo user/key; webhook dùng lock/order for update để tránh xử lý trung và cập nhật trạng thái sai.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu concurrency ở mức backend thực tế.

### Câu 31 - Idempotency trong database/API là gì?

**Câu hỏi:** Vì sao Idempotency quan trọng với checkout và webhook?

**Đáp án mẫu:** Idempotency đảm bảo cùng một request lặp lại không tạo nhiều side effect. Với checkout, nếu client retry do timeout, không nên tạo hai đơn hàng. Với webhook, provider có thể gửi lại event nhiều lần, backend phải nhận diện event đã xử lý. Cần lưu Idempotency key/event id và có unique/lock phù hợp.

**Liên hệ với Htech:** Htech dùng Idempotency key cho checkout và payment webhook event để trả về kết quả cũ thay vì xử lý lại.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy API an toàn trước retry/network failure.

**Lỗi cần tránh:** Nhầm Idempotency với validation thông thường; Idempotency tập trung vào việc request lặp lại không tạo side effect mới.

### Câu 32 - Index database dùng để làm gì?

**Câu hỏi:** Nên tạo index cho những trường nào trong Htech?

**Đáp án mẫu:** Index giúp tăng tốc truy vấn tìm kiếm/filter/sort nhưng làm tăng chi phí ghi và dung lượng lưu trữ. Nên index các trường hay dùng trong where/join/order by như email, order number, product slug, category/brand id, status, created_at. Không nên index tràn lan nếu không đo đạc.

**Liên hệ với Htech:** Tim sản phẩm theo slug, xem order theo order number, lọc đơn theo status/date và đăng nhập theo email/username là các vị trí cần index.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu performance database cân bằng đọc/ghi.

### Câu 33 - Pagination offset có hạn chế gì?

**Câu hỏi:** Offset pagination và cursor pagination khác nhau thế nào?

**Đáp án mẫu:** Offset pagination dùng page/size, dễ cài đặt và phù hợp với admin/list thông thường. Hạn che là page sau có thể chậm khi offset lớn và dữ liệu thay đổi có thể gây lặp/bỏ sót. Cursor pagination dùng moc như id/created_at, tốt hơn cho feed lớn hoặc infinite scroll, nhưng phức tạp hơn.

**Liên hệ với Htech:** Man hinh admin có thể dùng page/size; nếu sau này có feed sản phẩm lớn hoặc notification realtime nhiều, có thể cần cursor.

**Điểm nhà tuyển dụng đang kiểm tra:** Biết trade-off API pagination.

### Câu 34 - Projection DTO trong JPA có lợi ích gì?

**Câu hỏi:** Khi nào nên dùng projection thay vì lấy full entity?

**Đáp án mẫu:** Projection chỉ lấy các cột cần thiết, giảm dữ liệu đọc từ database và giảm mapping entity phức tạp. Nó phù hợp với list item, dashboard summary, report, autocomplete. Nếu cần update entity thì phải lấy entity thật.

**Liên hệ với Htech:** Danh sách product admin, product picker, dashboard top list và order list có thể dùng projection/query riêng để giảm tải.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy tối ưu query theo use case.

### Câu 35 - PostgreSQL có uu điểm gì cho dự án này?

**Câu hỏi:** Tại sao PostgreSQL phù hợp với e-commerce và chatbot read-model?

**Đáp án mẫu:** PostgreSQL mạnh về relational data, transaction, constraint, index, view và query tổng hợp. E-commerce có nhiều quan hệ chặt như user-order-product-voucher-payment. Chatbot read-model có thể dùng view/materialized view/query tổng hợp để cho AI đọc dữ liệu an toàn hơn.

**Liên hệ với Htech:** Htech dùng PostgreSQL cho cả backend chính và chatbot read-model để tra cứu sản phẩm, flash sale, voucher, order summary.

**Điểm nhà tuyển dụng đang kiểm tra:** Biết gắn DBMS với yêu cầu bài toán.

### Câu 36 - Database constraint và validation khác nhau thế nào?

**Câu hỏi:** Vì sao cần cả validation ở app và constraint ở DB?

**Đáp án mẫu:** Validation ở app giúp trả lời lỗi thân thiện và áp dụng rule nghiệp vụ trước khi ghi DB. Constraint ở DB là lớp bảo vệ cuối cùng, đảm bảo dữ liệu không bị sai ngây cả khi bug app hoặc có nhiều service cùng ghi. Nên dùng cả hai cho trường bắt buộc, unique, foreign key, check constraint.

**Liên hệ với Htech:** Email/order number/SKU/code coupon nên có ràng buộc unique; service vẫn validate để trả message dùng cho frontend.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức về data integrity.

### Câu 37 - Read-model là gì?

**Câu hỏi:** Read-model khác gì với schema giao dịch chính?

**Đáp án mẫu:** Read-model là mô hình dữ liệu tối ưu cho đọc/tra cứu, có thể gom sẵn dữ liệu từ nhiều bảng, chỉ chứa field cần thiết và không dùng để ghi nghiệp vụ. Schema giao dịch chính tối ưu cho tính nhất quán, update và transaction. Read-model giúp query đơn giản, an toàn và nhanh hơn cho use case đọc nhiều.

**Liên hệ với Htech:** Chatbot Htech dùng read-model/view riêng để AI chỉ nhìn thấy dữ liệu được phép, không truy cập toàn bộ schema sản xuất.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu CQRS/read model ở mức thực dụng.

---

## 4. Security, JWT, Spring Security, RBAC

### Câu 38 - JWT là gì?

**Câu hỏi:** JWT hoạt động như thế nào trong hệ thống login?

**Đáp án mẫu:** JWT là token chứa claims đã ký, server phát sau khi user đăng nhập thành công. Client gửi token trong `Authorization: Bearer ...` cho request sau. Backend verify chữ ký, hạn sử dụng và lấy thông tin user/role để authorize. JWT phù hợp cho stateless API, nhưng token bị lộ thì có rủi ro cho đến khi hết hạn nếu không có revoke/refresh strategy.

**Liên hệ với Htech:** Htech dùng JWT cho user/admin API, protected routes frontend và optional auth cho chatbot.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu authentication stateless và rủi ro token.

**Lỗi cần tránh:** Nói JWT được mã hóa nên không ai đọc được; thông thường JWT chỉ được ký, payload có thể decode, nên không đưa secret vào token.

### Câu 39 - Authentication và authorization khác nhau thế nào?

**Câu hỏi:** Đăng nhập thành công có đồng nghĩa được vào admin không?

**Đáp án mẫu:** Authentication xác định bạn là ai, còn authorization xác định bạn được làm gì. User đăng nhập thành công có thể truy cập trang user, nhưng chỉ user có role ADMIN mới được vào admin API/trang admin. Cần tách rõ hai bước này để tránh lỗi bảo mật.

**Liên hệ với Htech:** `ProtectedRoute` phía frontend kiểm tra login/role, Spring Security phía backend chặn `/admin/api/v1/**` nếu không có role admin.

**Điểm nhà tuyển dụng đang kiểm tra:** Phân biệt authn/authz, nền tảng quan trọng.

### Câu 40 - Spring Security filter chain xử lý request như thế nào?

**Câu hỏi:** JWT filter thường nằm ở đâu trong Spring Security?

**Đáp án mẫu:** Request đi qua security filter chain trước khi vào controller. JWT filter đọc Authorization header, verify token, tạo Authentication và đưa vào SecurityContext nếu hợp lệ. Filter này thường đặt trước `UsernamePasswordAuthenticationFilter` để request đã có context trước khi authorize.

**Liên hệ với Htech:** Backend có `JwtAuthenticationFilter` và `SecurityConfig` cấu hình whitelist public, admin role và stateless session.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu luồng request security thay vì chỉ biết annotation.

### Câu 41 - BCrypt dùng để làm gì?

**Câu hỏi:** Vì sao không lưu password plain text hoặc hash SHA-256 đơn giản?

**Đáp án mẫu:** BCrypt là password hashing algorithm có salt và cost factor, làm chậm brute force. Plain text là cực kỳ nguy hiểm vì database lộ là lộ mật khẩu thật. SHA-256 đơn giản quá nhanh, dễ brute force bằng GPU. BCrypt phù hợp để lưu password user.

**Liên hệ với Htech:** Spring Security cấu hình `BCryptPasswordEncoder` cho password của user.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức bảo mật cơ bản về password.

### Câu 42 - Whitelist endpoint cần lưu ý gì?

**Câu hỏi:** Endpoint nào nên public và endpoint nào cần auth?

**Đáp án mẫu:** Endpoint public nên là login/register, xem sản phẩm, danh mục, brand, CMS công khai, flash sale công khai. Endpoint liên quan user data như cart, order, profile, notification cần authenticated. Endpoint admin cần role admin. Whitelist quá rộng có thể lộ dữ liệu hoặc cho phép thao tác trái phép.

**Liên hệ với Htech:** `SecurityConfig` whitelist `/api/v1/products/**`, `/api/v1/auth/**`, public settings, CMS; `/admin/api/v1/**` cần role ADMIN.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng review endpoint security.

### Câu 43 - Refresh token dùng để làm gì?

**Câu hỏi:** Vì sao access token thường nên sống ngắn?

**Đáp án mẫu:** Access token sống ngắn giảm thời gian rủi ro nếu token bị lộ. Refresh token sống dài hơn dùng để lấy access token mới mà không cần login lại. Refresh token nên lưu/quản lý cẩn thận, có thể revoke khi logout hoặc nghĩ ngờ bị lộ.

**Liên hệ với Htech:** Backend có endpoint refresh-token, nên giải thích rằng đây là một phần cải thiện UX nhưng vẫn cần quản lý token an toàn.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu token lifecycle và trade-off bảo mật/trải nghiệm.

### Câu 44 - CORS có phải biện pháp bảo mật API tuyệt đối không?

**Câu hỏi:** Nếu API cấu hình CORS, có ngăn được Postman/curl không?

**Đáp án mẫu:** CORS là cơ chế của browser, không ngắn request từ Postman/curl/server khác. Nó giúp browser chặn website không được phép đọc response cross-origin. API vẫn phải có authentication, authorization, validation và rate limit nếu cần.

**Liên hệ với Htech:** CORS cho frontend Htech gọi API từ origin được phép, nhưng backend vẫn cần JWT và role check cho endpoint nhạy cảm.

**Điểm nhà tuyển dụng đang kiểm tra:** Tránh hiểu sai pho biến về CORS.

**Lỗi cần tránh:** Nói CORS bảo vệ API khỏi mọi client; CORS chủ yếu là cơ chế browser, không thay thế authentication/authorization.

### Câu 45 - XSS và CSRF là gì?

**Câu hỏi:** Trong SPA dùng JWT, cần quan tâm XSS/CSRF như thế nào?

**Đáp án mẫu:** XSS là lỗi cho phép chèn script độc hại vào trang, có thể lấy token nếu token lưu trong localStorage. CSRF là lỗi ép browser gửi request thấy user, thường nghiêm trọng khi auth đưa vào cookie tự động. Với JWT trong Authorization header, CSRF thấp hơn cookie auth, nhưng XSS vẫn cần quan tâm. Cần sanitize output, tránh render HTML nguy hiểm và bảo vệ token.

**Liên hệ với Htech:** Frontend React mặc định escape text, nhưng nếu render markdown/chatbot/content CMS cần cẩn thận với HTML/script.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức security frontend-backend.

### Câu 46 - Role-based access control là gì?

**Câu hỏi:** RBAC khác gì với check userId sở hữu tại nguyen?

**Đáp án mẫu:** RBAC cấp quyền theo role như USER, ADMIN. Nó trả lời câu hỏi user có loai quyền nào. Ownership check trả lời câu hỏi user có sở hữu tại nguyen cũ the không, vì đủ order này có thuộc user này không. Hai cơ chế bổ sung cho nhau, không thay thế nhau.

**Liên hệ với Htech:** Admin có role để vào admin API, còn user xem order phải check order.user.id trung với userId trong token.

**Điểm nhà tuyển dụng đang kiểm tra:** Biết authorization không chỉ là role.

**Lỗi cần tránh:** Chỉ check role USER rồi cho xem mọi order; phải check thêm order đó có thuộc user đang đăng nhập hay không.

### Câu 47 - Optional authentication trong chatbot là gì?

**Câu hỏi:** Tại sao chatbot cho phép guest vẫn chat được?

**Đáp án mẫu:** Optional auth nghĩa là request có token hợp lệ thì gắn userId, không có token thì vẫn xử lý như anonymous. Cách này giữ trải nghiệm public cho guest, nhưng vẫn cho phép tính năng cá nhân như tra cứu đơn hàng khi user đã login. Cần đảm bảo intent/order lookup chỉ chạy khi có userId và scope theo user.

**Liên hệ với Htech:** Chatbot Htech có middleware `optionalAuth`, token sai/hết hạn không làm endpoint lỗi 401 mà chỉ coi là guest.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy UX và security khi thiết kế AI service.

**Lỗi cần tránh:** Cho optional auth truy cập dữ liệu cá nhân khi token lỗi; token không hợp lệ chỉ nên được coi là anonymous.

---

## 5. Payment, Order Domain, Checkout, Webhook

### Câu 48 - Checkout flow trong Htech gồm những bước chính nào?

**Câu hỏi:** Hãy mô tả luồng checkout từ cart đến order.

**Đáp án mẫu:** User chọn item, địa chỉ, payment method và coupon nếu có. Backend validate user/address/item, kiểm tra stock, tính subtotal, shipping fee, discount, tax, tổng tiền, validate payment method, tạo order và order item, ghi history, xóa item khỏi cart, gửi email/notification. Các bước ghi dữ liệu cần trong transaction.

**Liên hệ với Htech:** `OrderServiceImpl.checkout` điều phối helper, coupon applier, tax calculator, notification và email sender.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng mô tả business flow phức tạp có tính nhất quán.

### Câu 49 - Vì sao checkout cần Idempotency key?

**Câu hỏi:** Nếu user bam nut đặt hàng hai lần hoặc request timeout rồi retry thì sao?

**Đáp án mẫu:** Nếu không có Idempotency, hệ thống có thể tạo hai đơn hàng và trừ tồn kho/voucher hai lần. Idempotency key giúp backend nhận diện request lặp lại của cùng user và trả về order đã tạo trước đó. Cần normalize key, lưu cùng order và lock để tránh race condition.

**Liên hệ với Htech:** Checkout yêu cầu Idempotency key, có lock theo user + key và tim order cũ trước khi tạo order mới.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy chống duplicate side effect.

**Lỗi cần tránh:** Chỉ disable nut đặt hàng ở frontend; backend vẫn phải chống retry/duplicate vì request có thể lặp lại ngoài UI.

### Câu 50 - Payment webhook là gì?

**Câu hỏi:** Vì sao hệ thống payment cần webhook/IPN?

**Đáp án mẫu:** Webhook/IPN là callback từ payment provider về server để thông báo kết quả thanh toán. Client return URL không đủ tin cậy vì user có thể tắt trình duyệt. Backend cần verify event, tim order, validate amount/currency/status, cập nhật payment status và ghi event đã xử lý.

**Liên hệ với Htech:** Htech có endpoint payment webhook, MoMo IPN/return và VNPay return/IPN, có handler xử lý Idempotency và status transition.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu payment flow server-side.

**Lỗi cần tránh:** Tin vào trang return URL trên frontend để cập nhật thanh toán; kết quả tin cậy phải đến từ server webhook/IPN đã verify.

### Câu 51 - Tại sao webhook cần validate amount và currency?

**Câu hỏi:** Nếu provider bảo thanh toán thành công nhưng amount không đúng thì sao?

**Đáp án mẫu:** Backend không nên chỉ tin status thành công. Cần so sánh amount/currency với order expected amount để tránh cập nhật đơn thành paid khi số tiền sai hoặc payload bị lỗi. Nếu mismatch, nên reject/ghi lỗi và không chuyển payment completed.

**Liên hệ với Htech:** `PaymentWebhookHandler` validate amount/currency với total amount của order cho completed/refunded event.

**Điểm nhà tuyển dụng đang kiểm tra:** Tính cẩn thận với security và accounting trong payment.

**Lỗi cần tránh:** Chỉ so sánh orderNumber và status; payment completed vẫn cần đối chiếu amount/currency/provider/event.

### Câu 52 - Order status và payment status khác nhau thế nào?

**Câu hỏi:** Vì sao không dùng một status duy nhất cho đơn hàng?

**Đáp án mẫu:** Order status mô tả tiến trình fulfilment như pending, confirmed, processing, shipping, shipped, cancelled, returned. Payment status mô tả tiến trình thanh toán như pending, completed, failed, refunded. Hai trạng thái liên quan nhưng không giống nhau: đơn có thể pending và payment pending, hoặc shipped và payment completed.

**Liên hệ với Htech:** Htech có enum `OrderStatus` và `PaymentStatus` riêng, webhook cập nhật payment status và có thể kéo theo order status trong một số trường hợp.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu domain modeling.

**Lỗi cần tránh:** Gom order status và payment status vào một trường duy nhất, làm workflow giao hàng và thanh toán bị lẫn nhau.

### Câu 53 - Khi thanh toán failed cần rollback gì?

**Câu hỏi:** Nếu thanh toán thất bại sau khi đã tạo order thì hệ thống cần làm gì?

**Đáp án mẫu:** Tuy policy, có thể hủy đơn, restore stock đã trừ, restore coupon usage, ghi status history, thông báo user/admin. Cần làm idempotent để webhook failed lặp lại không rollback nhiều lần. Nếu đơn đã shipped/returned thì không nên cập nhật tùy tiện.

**Liên hệ với Htech:** Webhook failed có logic rollback allocation, cancel order và tạo notification nếu trạng thái cho phép.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng xử lý failure mode trong transaction/payment.

### Câu 54 - State transition là gì trong order workflow?

**Câu hỏi:** Vì sao cần giới hạn trạng thái đơn hàng được chuyển?

**Đáp án mẫu:** State transition là tập các bước hợp lệ từ trạng thái này sang trạng thái khác. Giới hạn transition giúp tránh thao tác vô lý, ví dụ cancelled không được chuyển lại shipping, shipped không nên về pending. Backend nên validate transition thay vì chỉ tin frontend.

**Liên hệ với Htech:** `OrderServiceImpl` có map allowed status transitions cho các trạng thái PENDING, CONFIRMED, PROCESSING, SHIPPING, SHIPPED, CANCELLED, RETURNED.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu business rule cần enforce ở backend.

### Câu 55 - Coupon applier cần xử lý những rule nào?

**Câu hỏi:** Áp dụng voucher không chỉ là trừ tiền, đúng không?

**Đáp án mẫu:** Đúng. Coupon cần validate code tồn tại, active, thời gian, usage limit, user đã dùng chưa, điều kiện subtotal, sản phẩm/category được áp dụng, loại discount và max discount nếu có. Cần tính riêng product coupon và shipping coupon nếu hệ thống hỗ trợ.

**Liên hệ với Htech:** Checkout có `CouponApplier` áp product coupon và shipping coupon, đồng thời restore usage khi payment failed trong một số trường hợp.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu business rule có thể phức tạp hơn UI.

---

## 6. React, TypeScript, Frontend Architecture

### Câu 56 - React component là gì?

**Câu hỏi:** Component trong React giúp gì khi xây UI Htech?

**Đáp án mẫu:** Component là khối UI độc lập có props, state và render logic. Tach component giúp tái sử dụng, dễ đọc và dễ test. Trong ứng dụng e-commerce, các phần như product card, pagination, status badge, modal, layout, order card nên tách thành component.

**Liên hệ với Htech:** Frontend có `ProductCard`, `AdminTable`, `Pagination`, `StatusBadge`, `ProtectedRoute`, layout user/admin và các component checkout/order.

**Điểm nhà tuyển dụng đang kiểm tra:** Nền tảng React và khả năng tổ chức UI.

### Câu 57 - Props và state khác nhau thế nào?

**Câu hỏi:** Khi nào dùng props, khi nào dùng state?

**Đáp án mẫu:** Props là dữ liệu component nhận từ cha, nên cói là read-only. State là dữ liệu nội bộ component thay đổi theo tương tác hoặc request. Nếu dữ liệu cần chia sẻ nhiều component, có thể đưa lên parent, dùng context/store hoặc server-state library.

**Liên hệ với Htech:** `ProductCard` nhận product qua props, còn trang product list có state filter/search/page; auth/cart/wishlist có thể đặt trong Zustand store.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu data flow một chiều trong React.

### Câu 58 - Hook là gì?

**Câu hỏi:** Vì sao React dùng hook như `useState`, `useEffect`, custom hook?

**Đáp án mẫu:** Hook cho phép function component dùng state, lifecycle-like behavior và tái sử dụng logic. `useState` quản lý state local, `useEffect` xử lý side effect như fetch, subscribe, sync. Custom hook gom logic lặp lại như debounce, click outside, query param, async export job.

**Liên hệ với Htech:** Frontend có custom hooks `useDebounce`, `useAdminList`, `useAsyncExportJob`, `usePageQueryParam`.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu hook không chỉ là cũ pháp.

### Câu 59 - `useEffect` có những lỗi thường gap nào?

**Câu hỏi:** Khi fetch API trong `useEffect`, cần lưu ý gì?

**Đáp án mẫu:** Lỗi thường gặp là thiếu dependency, dependency sai gây loop, không cleanup subscription/timer, race condition khi request cũ về sau request mới. Nếu dùng React Query cho server state, nhiều logic fetch/cache/retry có thể chuyển sang query hook thay vì tự viết `useEffect`.

**Liên hệ với Htech:** Htech dùng React Query cho nhiều màn hình admin/client, giúp quản lý cache/loading/error tốt hơn fetch thủ công.

**Điểm nhà tuyển dụng đang kiểm tra:** Kinh nghiem xử lý side effect frontend.

### Câu 60 - TypeScript giúp gì cho frontend?

**Câu hỏi:** Vì sao Htech dùng React + TypeScript thay vì JavaScript thuan?

**Đáp án mẫu:** TypeScript giúp khai báo type cho props, API response, request payload, store state và utility function. Nó phát hiện lỗi sớm khi compile, giảm sai field khi backend thay đổi contract. Với dữ liệu phức tạp như product variant, order, coupon, chatbot config, TypeScript giúp code dễ bảo trì hơn.

**Liên hệ với Htech:** Frontend có thư mức `types/` cho product, order, cart, auth, chatbot, notification, dashboard.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu lợi ích static typing trên frontend.

### Câu 61 - React Router dùng để làm gì?

**Câu hỏi:** Htech tổ chức route frontend như thế nào?

**Đáp án mẫu:** React Router quản lý routing trong SPA, mapping path sang component và layout. Htech tách public routes, user routes và admin routes. Public có home/products/product detail/cart/checkout/login; user có profile/order/voucher/notification/support; admin có dashboard/products/orders/customers/coupons/chatbot/settings.

**Liên hệ với Htech:** File router tách `publicRoutes`, `userRoutes`, `adminRoutes`, và admin route boc `ProtectedRoute requiredRole="ADMIN"`.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng thiết kế navigation cho ứng dụng có nhiều khu vực.

### Câu 62 - ProtectedRoute có vai trò gì?

**Câu hỏi:** Protected route phía frontend có đủ bảo mật không?

**Đáp án mẫu:** ProtectedRoute giúp UX: chặn user chưa login vào trang cần auth, redirect sang login, ẩn trang admin với user thường. Tuy nhiên bảo mật thật phải nằm ở backend, vì user có thể gọi API trực tiếp bằng tool khác. Frontend guard chỉ là lớp bảo vệ trải nghiệm.

**Liên hệ với Htech:** Htech có `ProtectedRoute` cho wishlist/cart/checkout/user/admin, backend vẫn dùng Spring Security role check cho API.

**Điểm nhà tuyển dụng đang kiểm tra:** Không nham lan frontend guard với security server-side.

**Lỗi cần tránh:** Nói ProtectedRoute là đủ bảo mật; frontend guard có thể bị bypass, backend mỗi là nơi bắt buộc enforce quyền.

### Câu 63 - Zustand dùng cho loại state nào?

**Câu hỏi:** Khac gì React Query?

**Đáp án mẫu:** Zustand phù hợp cho client state: auth user, UI state, cart count, wishlist local, notification dropdown. React Query phù hợp cho server state: data fetch từ API, cache, refetch, loading/error, invalidation. Không nên đưa tất cả vào một store lớn nếu server state đã có React Query quản lý tốt hơn.

**Liên hệ với Htech:** Htech dùng Zustand stores `useAuthStore`, `useCartStore`, `useWishlistStore`, `useUIStore`, và React Query cho danh sách/query API.

**Điểm nhà tuyển dụng đang kiểm tra:** Phân biệt client state và server state.

**Lỗi cần tránh:** Đưa tất cả API data vào Zustand; server state nên có cache/invalidation/loading/error riêng, React Query phù hợp hơn.

### Câu 64 - Axios service layer có lợi ích gì?

**Câu hỏi:** Vì sao không gọi `axios.get` trực tiếp trong mỗi component?

**Đáp án mẫu:** Service layer gom base URL, interceptor, auth header, error handling và endpoint function vào một nơi. Component chỉ gọi function có ý nghĩa nghiệp vụ như `getProducts`, `checkout`, `getOrders`. Cách này giảm lặp code và dễ thay đổi API contract.

**Liên hệ với Htech:** Frontend có `apis/services/*Service.ts` cho auth, product, order, cart, payment, admin dashboard, chatbot.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy tách layer trong frontend.

### Câu 65 - React Query có lợi ích gì?

**Câu hỏi:** React Query giải quyết vấn đề nào khi fetch data?

**Đáp án mẫu:** React Query quản lý server state, cache, loading, error, retry, refetch và invalidation. Nó giúp component không phải tự viết nhiều state fetch lặp lại. Sau mutation như tạo sản phẩm/cập nhật order, có thể invalidate query để danh sách mới tự động lấy lại.

**Liên hệ với Htech:** Các màn hình admin danh sách sản phẩm, đơn hàng, dashboard, feedback có lợi khi dùng React Query để quản lý data fetch.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu modern data fetching trong React.

### Câu 66 - Form frontend cần validate như thế nào?

**Câu hỏi:** Validation frontend có thay thế validation backend không?

**Đáp án mẫu:** Frontend validation giúp UX tốt hơn, bắt lỗi nhanh như required, email format, số luồng > 0. Nó không thay thế backend validation vì user có thể bypass frontend. Backend vẫn phải validate toàn bộ request và business rule.

**Liên hệ với Htech:** Form đăng ký, login, product admin, checkout có validate phía frontend để hướng dẫn user, backend vẫn kiểm tra password, address ownership, stock, coupon.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu multi-layer validation.

**Lỗi cần tránh:** Tin frontend validation là đủ; request có thể được gửi trực tiếp toi API nên backend vẫn phải validate.

### Câu 67 - i18next trong frontend dùng để làm gì?

**Câu hỏi:** Vì sao nên tách text UI vào file locale?

**Đáp án mẫu:** i18next giúp hỗ trợ nhiều ngôn ngữ bằng key/value, tách text khỏi component. Khi cần sửa text hoặc thêm ngôn ngữ, không phải sửa logic component. Cần tránh hard-code literal nếu app có yêu cầu i18n.

**Liên hệ với Htech:** Frontend có locale `vì` và `en` cho admin, account, checkout, auth, catalog, home, settings.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng xây UI có khả năng mở rộng ngôn ngữ.

### Câu 68 - Code splitting/lazy route có lợi ích gì?

**Câu hỏi:** Vì sao routes dùng `lazy()`?

**Đáp án mẫu:** Lazy loading giúp chia bundle theo trang, chỉ tải component khi user truy cập route đó. Điều này giảm initial bundle và cải thiện thời gian load ban đầu. Cần có suspense/loading state phù hợp.

**Liên hệ với Htech:** Routes public/user/admin dùng `lazy()` cho các page như dashboard, product form, checkout, login, blog detail.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức performance frontend cơ bản.

### Câu 69 - Recharts dùng cho use case nào?

**Câu hỏi:** Dashboard admin cần hiển thị dữ liệu như thế nào?

**Đáp án mẫu:** Thư viện chart như Recharts giúp vẽ biểu đồ doanh thu, order, category, top list để admin nắm tình hình kinh doanh. Frontend cần transform API response sang data shape của chart và xử lý loading/empty/error. Chart không nên tự tính business logic phức tạp nếu backend đã có summary.

**Liên hệ với Htech:** Admin dashboard có revenue chart, order chart, category chart, stats và top lists.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng hiển thị dữ liệu vận hành trên frontend.

### Câu 70 - Chatbot widget frontend nên xử lý lỗi ra sao?

**Câu hỏi:** Nếu service chatbot lỗi, frontend có nên làm hỏng flow mua hàng không?

**Đáp án mẫu:** Không. Chatbot là tính năng tăng cường, nên có fallback message khi API lỗi, timeout hoặc bot disabled. Widget nên giới hạn history gửi lên, hiển thị loading/error rõ ràng và không chặn các luồng chính như xem sản phẩm, cart, checkout.

**Liên hệ với Htech:** Frontend có component chatbot/support chat và service `chatbotService`, đọc public widget config để hiển thị welcome/suggestions/isEnabled.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy degrade gracefully khi tích hợp service phụ.

---

## 7. Node.js, Express, Chatbot Service

### Câu 71 - Node.js phù hợp với chatbot service vì sao?

**Câu hỏi:** Tại sao chatbot Htech tách thành Node.js/Express thay vì gộp vào Spring Boot?

**Đáp án mẫu:** Node.js/Express gọn nhẹ, phù hợp cho API service xử lý request chatbot, gọi provider AI và truy vấn read-model. Tách service giúp ranh giới rõ: Spring Boot xử lý giao dịch chính, chatbot chỉ tư vấn/tra cứu read-only. Cũng dễ thay đổi prompt, provider AI, config runtime mà không ảnh hưởng lỗi nghiệp vụ đơn hàng.

**Liên hệ với Htech:** Thư mục `chatbot/` là service riêng, có controller, router, middleware, config, db executor và prompt.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng giải thích kiến trúc service tách biệt.

### Câu 72 - Express middleware là gì?

**Câu hỏi:** Middleware trong Express dùng để làm gì?

**Đáp án mẫu:** Middleware là function chạy giữa request và route handler, có thể xử lý auth, logging, parse body, CORS, error handling. Middleware nhận `req`, `res`, `next`; nếu hợp lệ thì gọi `next()`, nếu không thì trả response. Nó giúp tách logic dùng chung khỏi controller.

**Liên hệ với Htech:** Chatbot có `optionalAuth` middleware đọc JWT nếu có, gắn `req.userId` hoặc tiếp tục như guest.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu pipeline của Express.

### Câu 73 - Optional auth trong Express cần xử lý token lỗi thế nào?

**Câu hỏi:** Nếu token hết hạn khi guest chat, nên trả 401 hay tiếp tục?

**Đáp án mẫu:** Tùy mục tiêu endpoint. Với chatbot public, optional auth nên tiếp tục như anonymous nếu không có token hoặc token lỗi, để guest vẫn chat được. Nhưng các intent cần dữ liệu riêng như order lookup phải yêu cầu userId hợp lệ. Endpoint admin config thì không optional, phải yêu cầu JWT admin.

**Liên hệ với Htech:** `optionalAuth` không trả 401 cho token sai/hết hạn, còn admin router verify role admin.

**Điểm nhà tuyển dụng đang kiểm tra:** Phân biệt public UX và dữ liệu cá nhân.

### Câu 74 - Runtime config trong chatbot có lợi ích gì?

**Câu hỏi:** Vì sao chatbot có `defaults.json` và `settings.json`?

**Đáp án mẫu:** Runtime config cho phép đổi bot name, welcome message, suggestions, provider/model, temperature, enable/disable bot mà không cần sửa code frontend. Defaults là giá trị nền tảng, settings là cấu hình hiện tại có thể merge partial. Cần validate config nếu đưa vào production.

**Liên hệ với Htech:** Chatbot admin API cho đọc/cập nhật/reset config và public widget config cho frontend.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu configurability và admin operability.

### Câu 75 - Provider fallback OpenAI/Gemini là gì?

**Câu hỏi:** Vì sao chatbot nên có fallback provider/model?

**Đáp án mẫu:** Gọi AI provider có thể lỗi do timeout, quota, billing, rate limit hoặc thiếu key. Fallback sang provider/model khác giúp service có khả năng tiếp tục trả lời ở mức chấp nhận được. Tuy nhiên không nên che giấu tất cả lỗi; cần log, timeout và thông báo fallback hợp lý.

**Liên hệ với Htech:** Chatbot có OpenAI/Gemini, config provider/model và fallback model khi OpenAI lỗi trong một số trường hợp.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy resilience khi tích hợp API bên ngoài.

**Lỗi cần tránh:** Fallback mọi lỗi một cách im lặng; vẫn cần log/timeout và biết khi nào nên trả lời fallback thay vì che giấu sự cố.

### Câu 76 - `dotenv` dùng để làm gì?

**Câu hỏi:** Vì sao không hard-code API key và DB password trong source?

**Đáp án mẫu:** `dotenv` đọc biến môi trường từ file `.env` khi phát triển local. Secret như API key, DB password, JWT secret không nên hard-code hoặc commit lên git vì có thể bị lộ. Môi trường production nên dùng secret manager/env vars của platform.

**Liên hệ với Htech:** Chatbot cần `OPENAI_API_KEY` hoặc `GEMINI_API_KEY`, `JWT_SECRET_KEY`, thông tin DB; server cần DB/JWT/AWS/mail/payment env.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức bảo mật secret.

### Câu 77 - Read-only DB executor cần bảo vệ điều gì?

**Câu hỏi:** Tại sao chatbot chỉ nên query read-only?

**Đáp án mẫu:** AI có thể hiểu sai prompt hoặc user có thể prompt injection. Nếu chatbot có quyền ghi, rủi ro sửa/xóa đơn hàng, stock, user data. Read-only executor, transaction read-only, allowlist resource/field và SQL parameterization giúp giảm rủi ro. Chatbot chỉ nên tư vấn/tra cứu, không thực hiện hành động nghiệp vụ.

**Liên hệ với Htech:** Chatbot dùng read-model và `runReadOnly`, plan validate trước khi sinh SQL.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy an toàn khi tích hợp AI với database.

**Lỗi cần tránh:** Chỉ đưa rule vào prompt và tin AI sẽ tự tuân thủ; vẫn cần validate bằng code và quyền DB read-only.

### Câu 78 - Controller chatbot đang làm những bước nào?

**Câu hỏi:** Hãy mô tả pipeline xử lý một prompt trong chatbot.

**Đáp án mẫu:** Controller nhận prompt/history, đọc config, kiểm tra bot enabled, cắt ngắn history theo budget, gọi planner AI để tạo JSON plan, normalize intent, validate plan, chuyển plan thành SQL parameterized, query read-model, format/synthesis câu trả lời và trả response. Một số intent như recommendation/support/non-db có path riêng.

**Liên hệ với Htech:** File `chatbot/controller/chatbot.js` điều phối nhiều bước: intent, guardrail, read-only query, recommendation, provider fallback và response synthesis.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng đọc và giải thích flow code Node.js lớn.

### Câu 79 - Vì sao cần giới hạn history gửi lên AI?

**Câu hỏi:** Gửi toàn bộ lịch sử chat có vấn đề gì?

**Đáp án mẫu:** Gửi toàn bộ history làm prompt dài, tăng chi phí, tăng latency và có thể đưa thông tin không liên quan vào context. Nên cắt theo số message, số ký tự mỗi message và tổng ký tự. Vẫn giữ đủ ngữ cảnh gần nhất để hỏi tiếp nhưng tránh vượt token budget.

**Liên hệ với Htech:** Chatbot có `HISTORY_BUDGETS` riêng cho planner và reply.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu chi phí/latency/context trong ứng dụng AI.

### Câu 80 - API admin chatbot nên bảo mật ra sao?

**Câu hỏi:** AI được cập nhật config bot/model/widget?

**Đáp án mẫu:** Chỉ admin đã xác thực mới được đọc/cập nhật/reset config đầy đủ. Public chỉ nên đọc widget config an toàn như bot name, suggestions, isEnabled. Admin API cần verify JWT, role ADMIN và validate dữ liệu đầu vào để tránh cấu hình sai.

**Liên hệ với Htech:** Chatbot có router admin với config/defaults/models/widget-config, route widget-config public còn route config cần admin.

**Điểm nhà tuyển dụng đang kiểm tra:** Security boundary của service phụ.

### Câu 81 - SQL parameterization quan trọng thế nào trong Node.js?

**Câu hỏi:** Nếu user hỏi chatbot và input được đưa vào SQL thì rủi ro gì?

**Đáp án mẫu:** Nếu nối chuỗi input vào SQL trực tiếp sẽ có nguy cơ SQL injection. Parameterized query tách SQL template và value, database driver xử lý escape/bind value an toàn hơn. Ngoài ra chatbot còn cần validate field/operator/resource để AI không tạo truy vấn ngoài contract.

**Liên hệ với Htech:** `plan-to-sql` sinh SQL có params từ plan đã validate, executor chạy read-only.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức bảo mật database.

### Câu 82 - Khi nào chatbot nên tra non-db answer?

**Câu hỏi:** Không phải câu nào cũng cần query database, đúng không?

**Đáp án mẫu:** Dùng. Lời chào, hướng dẫn chung, chính sách của shop hoặc câu hỏi không cần dữ liệu sản phẩm cũ the có thể tra non-db. Điều này giảm query DB, giảm latency và tránh dùng dữ liệu sai mức dich. Tuy nhiên với câu hỏi về giá, stock, voucher, order, chatbot nên đưa vào dữ liệu hệ thống.

**Liên hệ với Htech:** Chatbot có intent `non_db` và shortcut cho greeting/support/recommendation path riêng.

**Điểm nhà tuyển dụng đang kiểm tra:** Biết khi nào cần dữ liệu thực, khi nào không.

---

## 8. AI Chatbot, Prompt, Guardrail, Hallucination Control

### Câu 83 - Intent planning trong chatbot là gì?

**Câu hỏi:** Vì sao chatbot không cho AI sinh câu trả lời trực tiếp ngay?

**Đáp án mẫu:** Intent planning là bước AI phân loại câu hỏi và tạo plan có cấu trúc: intent, resource, select, where, sort, limit. Cách này giúp hệ thống kiểm soát AI trước khi truy vấn DB. Nếu AI trả lời trực tiếp không dựa trên dữ liệu thật, để hallucinate giá, stock, voucher hoặc trạng thái đơn hàng.

**Liên hệ với Htech:** Chatbot planner tạo JSON plan, controller validate plan rồi mới sinh SQL/query read-model.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu AI orchestration thay vì chỉ gọi API LLM.

**Lỗi cần tránh:** Để model vừa tự nghĩ vừa tự trả lời về giá/stock mà không đưa vào dữ liệu DB hiện tại.

### Câu 84 - Guardrail là gì trong chatbot?

**Câu hỏi:** Guardrail nào cần có khi AI truy cập dữ liệu?

**Đáp án mẫu:** Guardrail là tập rule giới hạn hành vi AI: chỉ được dùng resource/field trong contract, chỉ query read-only, bắt buộc scope order theo user, coupon chỉ public/active, flash sale chỉ active, limit số dòng, parameterized SQL, không thêm fact ngoài context. Guardrail giúp giảm rủi ro sai thông tin và lộ dữ liệu.

**Liên hệ với Htech:** Chatbot có contract, `validatePlan`, guard theo intent và read-only executor.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức về AI safety trong app thực tế.

**Lỗi cần tránh:** Xem guardrail là một prompt duy nhất; guardrail tốt phải gom prompt, contract, validation, SQL params, auth scope và read-only permission.

### Câu 85 - Hallucination trong AI là gì?

**Câu hỏi:** Làm sao giảm việc chatbot bịa thông tin sản phẩm?

**Đáp án mẫu:** Hallucination là khi model tạo thông tin nghe có vẻ đúng nhưng không có cơ sở. Có thể giảm bằng cách đưa SAFE_CONTEXT từ DB, bắt model chỉ trả lời dựa trên context, nếu không có dữ liệu thì nói không tìm thấy, tách planner/query/synthesis, và không cho thêm fact mới khi viết lại câu trả lời.

**Liên hệ với Htech:** Response synthesis của chatbot chỉ nên viết lại dựa trên context an toàn từ DB, không tự bịa giá/stock/thông số.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu hạn chế của LLM và cách thiết kế giảm rủi ro.

**Lỗi cần tránh:** Nói chatbot "luôn đúng" vì dùng model mạnh; LLM vẫn có thể sai nếu context, prompt hoặc dữ liệu đầu vào sai.

### Câu 86 - Prompt engineering trong dự án này có vai trò gì?

**Câu hỏi:** Prompt có phải chỉ là viết câu lệnh cho AI không?

**Đáp án mẫu:** Prompt trong ứng dụng production cần có role, quy tắc, schema/contract, ví dụ few-shot, định dạng output và giới hạn hành vi. Prompt tốt giúp planner trả về JSON đúng, phân loại intent tốt và không sinh SQL/field ngoài contract. Prompt chỉ là một lớp, vẫn cần validate bằng code.

**Liên hệ với Htech:** Chatbot có `prompt-system`, `few-shot`, `contract-to-prompt`, planner rule và validation layer.

**Điểm nhà tuyển dụng đang kiểm tra:** Không coi prompt là biện pháp bảo mật duy nhất.

### Câu 87 - DB contract trong chatbot là gì?

**Câu hỏi:** Vì sao cần contract resource/field thay vì đưa schema thật cho AI?

**Đáp án mẫu:** DB contract là danh sách resource, field, relation và rule mà chatbot được phép dùng. Nó che bot schema thật, giảm phức tạp và ngăn AI truy cập bảng/trường nhạy cảm. Contract cũng giúp validate plan và render schema an toàn vào prompt.

**Liên hệ với Htech:** `chatbot/db/contract.js` định nghĩa các resource như Product, ProductVariant, FlashSaleItem, Order, Coupon, Article.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy schema exposure và least privilege.

### Câu 88 - AI chatbot có nên tự động tạo đơn hàng không?

**Câu hỏi:** Nếu user nói "đặt cho tôi sản phẩm này", chatbot nên làm gì?

**Đáp án mẫu:** Trong scope Htech hiện tại, chatbot không nên tự động tạo đơn. Nó nên hướng dẫn user xem sản phẩm, thêm vào giỏ hoặc đến checkout. Tạo đơn, trừ tồn kho, thanh toán là nghiệp vụ có side effect cao, phải qua backend Spring Boot và UI xác nhận rõ ràng. Nếu sau này muốn agentic action, cần auth, confirmation, audit log và permission rất chặt.

**Liên hệ với Htech:** Chatbot được thiết kế chỉ tư vấn/tra cứu, không ghi order, stock, payment hay user data.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng giới hạn scope AI hợp lý.

### Câu 89 - Personalized recommendation cần dữ liệu gì?

**Câu hỏi:** Gợi ý sản phẩm cá nhân hóa cần cẩn thận điều gì?

**Đáp án mẫu:** Cá nhân hóa có thể đưa vào lịch sử xem, đơn hàng, wishlist, category quan tâm, ngân sách và câu hỏi hiện tại. Cần tôn trọng privacy, chỉ dùng dữ liệu user được phép, có fallback cho guest và không đưa ra kết luận quá mức. Kết quả nên dựa trên product/variant thật và tình trạng active/stock.

**Liên hệ với Htech:** Chatbot có recommendation path, guest có thể gợi ý chung, user login có thể cá nhân hóa hơn nếu có dữ liệu hợp lệ.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy dữ liệu và privacy trong AI feature.

### Câu 90 - Eval chatbot nên làm như thế nào?

**Câu hỏi:** Nếu muốn kiểm thử chatbot, bạn test những gì?

**Đáp án mẫu:** Nên test intent classification, plan validation, SQL sinh ra, scope user cho order lookup, fallback khi DB/AI lỗi, hallucination khi không có dữ liệu, câu hỏi tiếng Việt có dấu/không dấu, câu hỏi so sánh/giá/stock/voucher. Có thể tạo bộ câu hỏi mẫu và expected behavior thay vì chỉ nhìn cảm tính câu trả lời.

**Liên hệ với Htech:** Các flow cần test kỹ là product search, compare, voucher public, flash sale active, order lookup có auth và non-db/support.

**Điểm nhà tuyển dụng đang kiểm tra:** Nhận thức kiểm thử AI khác kiểm thử API CRUD thông thường.

---

## 9. WebSocket, Realtime, Export, Background Job

### Câu 91 - WebSocket khác polling thế nào?

**Câu hỏi:** Khi nào dùng WebSocket thay vì client lặp lại request GET?

**Đáp án mẫu:** Polling là client định kỳ hỏi server có dữ liệu mới không, dễ cài đặt nhưng tốn request và có độ trễ. WebSocket tạo kết nối hai chiều dài hạn, server có thể push event ngây khi có thay đổi. Phù hợp với notification realtime, chat/support, trạng thái job cần update nhanh.

**Liên hệ với Htech:** Htech có WebSocket `/ws/support` và push notification/event cho user/admin.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu realtime communication pattern.

### Câu 92 - WebSocket authentication cần làm gì?

**Câu hỏi:** Nếu user kết nối WebSocket, server biết ai đang kết nối bằng cách nào?

**Đáp án mẫu:** WebSocket handshake cần kèm token hoặc credential, server verify token và gắn session với userId/role. Sau đó session registry lưu mapping user/admin -> WebSocket sessions để push đúng người. Cần xử lý disconnect và remove session.

**Liên hệ với Htech:** Backend có `JWTWebSocketAuthInterceptor`, `RealtimeSocketSessionRegistry` và `RealtimeEventPushService`.

**Điểm nhà tuyển dụng đang kiểm tra:** Bảo mật và quản lý session realtime.

### Câu 93 - Async export job nên thiết kế ra sao?

**Câu hỏi:** Vì sao export báo cáo lớn không nên xử lý trực tiếp trong HTTP request?

**Đáp án mẫu:** Export lớn có thể tốn thời gian, để timeout và làm user đợi lâu. Nên tạo export job, tra job id, worker xử lý async, cập nhật progress/status, user poll/download khi xong. Cần lưu lỗi, file path, content type và cleanup file cũ.

**Liên hệ với Htech:** Htech có `ExportJobWorker`, `ChunkedExcelExportGenerator`, status RUNNING/SUCCEEDED/FAILED và download endpoint.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy background processing.

### Câu 94 - PDF/Excel export cần lưu ý gì?

**Câu hỏi:** Tạo invoice/report có những rủi ro nào?

**Đáp án mẫu:** Cần đảm bảo dữ liệu đúng tại thời điểm export, format tiền/ngày tháng chuẩn, file không quá lớn, memory không bị tràn, tên file/content type đúng và endpoint download có auth. Với report lớn nên chunk data và stream/ghi file theo từng phần.

**Liên hệ với Htech:** Backend dùng Apache POI cho Excel export và OpenPDF cho invoice/report PDF.

**Điểm nhà tuyển dụng đang kiểm tra:** Kinh nghiem xử lý file/report trong backend.

### Câu 95 - Notification realtime và notification persisted khác nhau thế nào?

**Câu hỏi:** Nếu user offline thì notification WebSocket có mật không?

**Đáp án mẫu:** Realtime push chỉ đến user đang có session mở. Persisted notification lưu DB để user offline vẫn xem lại khi đăng nhập. Hệ thống tốt thường kết hợp cả hai: ghi notification vào DB, nếu user/admin online thì push realtime thêm.

**Liên hệ với Htech:** Htech có user/admin notification service và realtime push service, phù hợp với mô hình vừa lưu vừa đẩy event.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu reliability trong notification.

---

## 10. System Design, Testing, Performance, Deployment, Teamwork

### Câu 96 - Kiến trúc tổng thể Htech gồm những thành phần nào?

**Câu hỏi:** Hãy mô tả system design của Htech trong 1-2 phút.

**Đáp án mẫu:** Htech gồm React/TypeScript frontend cho public, user và admin; Spring Boot backend xử lý nghiệp vụ chính, auth, product, cart, order, payment, notification, export; PostgreSQL lưu dữ liệu quan hệ; Node.js/Express chatbot service tích hợp OpenAI/Gemini và chỉ đọc read-model; WebSocket dùng cho realtime. Các thành phần giao tiếp qua HTTP API, chatbot và backend cùng đọc PostgreSQL theo ranh giới quyền khác nhau.

**Liên hệ với Htech:** Day là câu mở đâu nên nói rõ ranh giới: Spring Boot ghi/nghiệp vụ, chatbot read-only/AI, frontend hiển thị và gọi API.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng tom tắt hệ thống phức tạp ngắn gọn.

### Câu 97 - Bạn đã test dự án như thế nào?

**Câu hỏi:** Nếu nhà tuyển dùng hỏi về testing, nên trả lời trung thực thế nào?

**Đáp án mẫu:** Em đã thực hiện functional/manual testing cho các flow chính như đăng ký, đăng nhập, tìm kiếm sản phẩm, cart, checkout, xem đơn, admin product/order, chatbot và notification. Em nhận thức automated test là phần cần bổ sung, đặc biệt cho checkout, webhook Idempotency, plan validation chatbot và frontend flow quan trọng.

**Liên hệ với Htech:** Không nên nói "100% automated test" nếu repo chưa có bằng chứng. Nên nói rõ test theo kịch bản chức năng và hướng cải thiện.

**Điểm nhà tuyển dụng đang kiểm tra:** Sự trung thực kỹ thuật và khả năng tự đánh giá hạn chế.

**Lỗi cần tránh:** Gọi bộ testcase manual là automated test; phỏng vấn kỹ thuật đánh giá cao sự rõ ràng và trung thực.

### Câu 98 - Nếu hệ thống chậm, bạn sẽ điều tra từ đâu?

**Câu hỏi:** Các điểm có thể là bottleneck trong Htech là gì?

**Đáp án mẫu:** Em sẽ đo đạc trước: API latency, SQL query time, frontend bundle/render, chatbot AI latency, WebSocket/session, export job. Các điểm cần xem là product search/filter, dashboard report, checkout transaction, webhook retry, AI provider timeout. Sau khi có dữ liệu mới thì tối ưu index/query/cache/lazy load/async.

**Liên hệ với Htech:** Product list, dashboard, chatbot và export là những luồng có khả năng tồn tại bottleneck khi dữ liệu tăng.

**Điểm nhà tuyển dụng đang kiểm tra:** Tư duy performance dựa trên đo đạc, không tối ưu cảm tính.

### Câu 99 - Nếu deploy Htech, cần cấu hình những gì?

**Câu hỏi:** Các biến môi trường quan trọng của hệ thống là gì?

**Đáp án mẫu:** Backend cần DB URL/username/password, JWT secret, allowed origins, frontend URL, mail, AWS S3, payment secret/config. Chatbot cần AI API key, JWT secret, DB config, provider/model, CORS origins. Frontend cần API base URL và chatbot service URL. Ngoài ra cần HTTPS, reverse proxy, migration/seed, logging và backup DB.

**Liên hệ với Htech:** README server/chatbot đã liệt kê các biến bắt buộc và tùy chọn cho local/deploy.

**Điểm nhà tuyển dụng đang kiểm tra:** Hiểu ứng dụng không chỉ chạy local mà cần cấu hình môi trường.

### Câu 100 - Nếu làm việc nhóm với dự án này, bạn quản lý Git và task ra sao?

**Câu hỏi:** Quy trình làm việc tốt cho team fullstack là gì?

**Đáp án mẫu:** Nên chia task theo module/feature, tạo branch riêng, commit nhỏ có message rõ, review code trước khi merge, tránh sửa cùng file lớn nếu không cần, cập nhật README/env example khi đổi cấu hình. API contract nên thống nhất trước giữa frontend/backend, có issue/checklist và test flow trước khi merge.

**Liên hệ với Htech:** Các module như product, order, chatbot, admin dashboard, payment có boundary rõ nên có thể chia task theo module.

**Điểm nhà tuyển dụng đang kiểm tra:** Khả năng làm việc trong quy trình thực tế, không chỉ code một mình.

---

## Mock interview nhanh trong 30 phút

Dùng 15 câu sau để tự luyện nói. Mỗi câu trả lời trong 1-2 phút, ưu tiên nói rõ "khái niệm -> Htech -> hạn chế/trade-off".

1. Hãy giới thiệu ngắn gọn dự án Htech và vai trò của bạn.
2. Kiến trúc tổng thể của hệ thống gồm những thành phần nào?
3. Vì sao backend chính dùng Spring Boot còn chatbot tách Node.js/Express?
4. JWT trong Htech được dùng như thế nào?
5. Phân biệt authentication, authorization và ownership check.
6. Checkout flow của Htech gồm những bước nào?
7. Idempotency trong checkout/webhook giải quyết vấn đề gì?
8. Vì sao chatbot chỉ được đọc read-model, không ghi DB?
9. React Router và ProtectedRoute trong frontend được tổ chức ra sao?
10. Zustand và React Query khác nhau thế nào trong Htech?
11. JPA có vấn đề N+1 query là gì và cách giảm?
12. Nếu API product list chậm, bạn điều tra và tối ưu như thế nào?
13. Nếu chatbot trả lời sai giá sản phẩm, bạn sẽ debug ở những bước nào?
14. Bạn đã test dự án như thế nào và automated test còn thiếu gì?
15. Nếu có thêm thời gian, bạn sẽ cải tiến Htech ở đâu?

---

## Checklist kiến thức cần ôn trước phỏng vấn

- Java core: OOP, collection, exception, stream, optional, immutable, equals/hashCode.
- Spring Boot: DI, controller/service/repository, REST API, DTO/entity, validation, global exception, transaction.
- JPA/PostgreSQL: relationship, lazy/eager, N+1, index, pagination, lock, Idempotency, read-model.
- Security: JWT, Spring Security filter, RBAC, ownership check, BCrypt, CORS, XSS/CSRF.
- Domain Htech: checkout, order status, payment status, webhook, coupon, stock rollback, notification.
- React/TypeScript: component, props/state, hook, routing, protected route, Zustand, React Query, Axios service, i18n.
- Node/Express: middleware, router/controller, dotenv, optional auth, runtime config, provider fallback.
- AI chatbot: intent, prompt, guardrail, plan validation, parameterized SQL, hallucination, read-only executor.
- Realtime/export: WebSocket session, notification persisted vs realtime, async export job, Excel/PDF.
- Soft skills: giải thích trade-off, nói trung thực về manual testing, nếu được hạn chế và hướng cải tiến.
