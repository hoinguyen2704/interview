Ứng viên Junior thường chỉ trả lời được từ Controller $\rightarrow$ Service $\rightarrow$ Repository $\rightarrow$ Database.
Một Backend Engineer xuất sắc sẽ nhìn thấy toàn bộ bức tranh: từ Mạng (Network) $\rightarrow$ Web Server (Tomcat) $\rightarrow$ Filter Chain & Security $\rightarrow$ DispatcherServlet $\rightarrow$ Spring AOP Proxy & Transaction $\rightarrow$ Connection Pool (HikariCP) $\rightarrow$ Hibernate Persistence Context $\rightarrow$ Database Engine $\rightarrow$ Reverse Serialization.

sequenceDiagram
    autonumber
    actor Client as Khách hàng (React/App)
    participant Nginx as Nginx / Gateway
    participant Tomcat as Tomcat Thread Pool
    participant Filter as Security Filter Chain (JWT)
    participant Dispatcher as DispatcherServlet
    participant Controller as OrderController
    participant Proxy as Spring AOP Transaction Proxy
    participant Service as OrderServiceImpl
    participant Hikari as HikariCP Connection Pool
    participant Hibernate as Hibernate (L1 Cache / SQL)
    participant DB as PostgreSQL Database

    Client->>Nginx: 1. POST /api/v1/orders/checkout (Header JWT + Body JSON)
    Nginx->>Tomcat: 2. Reverse Proxy chuyển tiếp qua port 8080
    Tomcat->>Filter: 3. Giao cho Worker Thread -> Chạy Filter Chain
    Note over Filter: Giải mã JWT, check Expiration & gán SecurityContext
    Filter->>Dispatcher: 4. Chuyển vào DispatcherServlet
    Note over Dispatcher: HandlerMapping tìm Controller & Jackson deserialize JSON + @Valid
    Dispatcher->>Controller: 5. Gọi checkout(@AuthenticationPrincipal, @Valid request)
    Controller->>Proxy: 6. orderService.checkout(...)
    Note over Proxy: Kích hoạt @Transactional -> Mượn Connection từ HikariCP (BEGIN)
    Proxy->>Service: 7. Thực thi nghiệp vụ checkout
    Note over Service: Advisory Lock -> Idempotency Check -> Trừ kho -> Áp Coupon
    Service->>Hibernate: 8. orderRepository.save(order)
    Hibernate->>DB: 9. Đẩy SQL INSERT/UPDATE qua JDBC Driver
    DB-->>Hibernate: 10. PostgreSQL commit vào đĩa cứng (WAL)
    Service-->>Proxy: 11. Hoàn tất method
    Note over Proxy: COMMIT Transaction -> Trả Connection về HikariCP Pool
    Proxy-->>Controller: 12. Trả về OrderResponse DTO
    Controller-->>Dispatcher: 13. ResponseEntity.ok(ApiResponse)
    Note over Dispatcher: Jackson serialize Java DTO -> Chuỗi JSON UTF-8
    Dispatcher-->>Tomcat: 14. Ghi vào HttpServletResponse
    Tomcat-->>Nginx: 15. Gửi TCP Packet HTTP/1.1 200 OK
    Nginx-->>Client: 16. Phản hồi JSON về Trình duyệt
