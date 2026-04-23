# quanligara

Ứng dụng quản lý gara (Jakarta EE Web) dùng **Servlet** + **JPA (Hibernate)**, build bằng **Maven** và đóng gói dạng **WAR**.

## Yêu cầu

- Java 8
- Maven (hoặc dùng `./mvnw`)
- MySQL 8.x (hoặc MariaDB tương thích)
- 1 servlet container để chạy WAR (ví dụ: Apache Tomcat)

## Cấu hình MySQL connection

Dự án đang khởi tạo JPA bằng persistence unit `default` (xem `src/main/java/com/quanli/quanligara/dao/UserDAO.java`), vì vậy bạn cấu hình DB ở:

- `src/main/resources/META-INF/persistence.xml`

Thêm các property JDBC vào `<properties>` (chỉnh `host`, `port`, `database`, `user`, `password` theo máy bạn):

```xml
<properties>
  <!-- Existing properties -->
  <property name="jakarta.persistence.schema-generation.database.action" value="create"/>
  <property name="hibernate.show_sql" value="true"/>
  <property name="hibernate.format_sql" value="true"/>

  <!-- MySQL connection -->
  <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>
  <property name="jakarta.persistence.jdbc.url" value="jdbc:mysql://localhost:3306/quanligara?useSSL=false&amp;serverTimezone=UTC"/>
  <property name="jakarta.persistence.jdbc.user" value="root"/>
  <property name="jakarta.persistence.jdbc.password" value="your_password"/>

  <!-- Hibernate dialect (tham khảo, có thể cần chỉnh theo MySQL version) -->
  <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
</properties>
```

Lưu ý:

- Nếu bạn muốn **không** để user/password trong source, có thể chuyển sang cấu hình **JNDI Datasource** ở Tomcat/app server và trỏ persistence unit tới datasource (cách này cần cấu hình phía server).
- `jakarta.persistence.schema-generation.database.action` đang để `create` nghĩa là mỗi lần chạy có thể tạo lại schema. Khi chạy thật bạn nên đổi sang `none` hoặc chiến lược phù hợp.

## Thêm MySQL JDBC driver

Hiện `pom.xml` chưa có dependency driver MySQL. Bạn cần thêm một trong các driver sau:

- MySQL: `com.mysql:mysql-connector-j`
- MariaDB: `org.mariadb.jdbc:mariadb-java-client`

Sau khi thêm dependency, chạy build lại.

## Build

```bash
./mvnw clean package
```

Kết quả WAR nằm ở:

- `target/quanligara-1.0-SNAPSHOT.war`

## Run / Deploy

Triển khai file WAR lên Tomcat:

- Copy WAR vào thư mục `webapps/` của Tomcat, hoặc deploy qua IDE.

Sau khi Tomcat chạy, mở:

- `http://localhost:8080/<context-path>/`

`context-path` thường là tên WAR (ví dụ `quanligara-1.0-SNAPSHOT`) nếu bạn không cấu hình lại.

## Cấu trúc thư mục chính

- `src/main/java/`: Servlet / filter / service / DAO / model
- `src/main/resources/META-INF/persistence.xml`: cấu hình JPA persistence unit
- `src/main/webapp/WEB-INF/web.xml`: cấu hình web app (hiện đang tối giản)

