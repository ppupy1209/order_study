# Coupon Order Assignment

선착순 쿠폰 발급과 쿠폰 기반 주문을 처리하는 Spring Boot API 서버입니다. 쿠폰 발급 수량, 상품 재고, 동일 쿠폰 중복 사용은 DB 조건부 UPDATE와 UNIQUE 제약조건으로 동시성 정합성을 보장합니다.

## 사용 기술

- Java 21
- Spring Boot 3.5.6
- Spring Web, Spring Data JPA, Bean Validation
- PostgreSQL
- Gradle Wrapper
- Docker, Docker Compose
- JUnit 5, Testcontainers

## 실행 방법

```bash
docker-compose up --build
```

애플리케이션은 `http://localhost:8080`에서 실행됩니다. DB 스키마는 과제 범위에 맞춰 JPA `ddl-auto=update`로 초기화합니다.

## 테스트 실행

```bash
./gradlew test
```

테스트는 로컬 실행 편의를 위해 H2 PostgreSQL 모드로 실행합니다. 실제 애플리케이션 실행은 Docker Compose의 PostgreSQL을 사용합니다.

## API 명세

### 사용자 생성

`POST /api/users`

```json
{
  "name": "userA"
}
```

### 상품 등록

`POST /api/products`

```json
{
  "name": "기계식 키보드",
  "price": 100000,
  "stockQuantity": 100
}
```

### 쿠폰 정책 생성

`POST /api/coupon-policies`

```json
{
  "name": "선착순 5000원 할인 쿠폰",
  "discountType": "FIXED_AMOUNT",
  "discountValue": 5000,
  "totalQuantity": 100,
  "startedAt": "2026-05-01T10:00:00",
  "endedAt": "2026-05-31T23:59:59"
}
```

### 쿠폰 발급

`POST /api/coupons/issue`

```json
{
  "userId": 1,
  "couponPolicyId": 1
}
```

### 내 쿠폰 목록 조회

`GET /api/users/{userId}/coupons`

### 주문 생성

`POST /api/orders`

```json
{
  "userId": 1,
  "productId": 1,
  "quantity": 2,
  "userCouponId": 1
}
```

쿠폰 없이 주문할 때는 `userCouponId`를 `null`로 전달합니다.

### 주문 단건 조회

`GET /api/orders/{orderId}`

### 주문 취소

`PATCH /api/orders/{orderId}/cancel`

## 테이블 구조

| 테이블 | 주요 컬럼 |
| --- | --- |
| users | id, name, created_at |
| product | id, name, price, stock_quantity, created_at, updated_at |
| coupon_policy | id, name, discount_type, discount_value, total_quantity, issued_quantity, started_at, ended_at, created_at, updated_at |
| user_coupon | id, user_id, coupon_policy_id, status, issued_at, used_at, expired_at |
| orders | id, user_id, product_id, user_coupon_id, quantity, original_price, discount_amount, final_price, status, created_at, canceled_at |

주요 제약조건:

- `user_coupon(user_id, coupon_policy_id)` UNIQUE
- `product.stock_quantity >= 0` check
- `coupon_policy.issued_quantity <= coupon_policy.total_quantity` check

## 주요 비즈니스 규칙

- 쿠폰은 발급 기간 안에서만 발급할 수 있습니다.
- 한 사용자는 같은 쿠폰 정책의 쿠폰을 한 번만 발급받을 수 있습니다.
- 쿠폰 발급 수량은 `totalQuantity`를 초과할 수 없습니다.
- 주문 수량은 1 이상이어야 합니다.
- 주문 생성 시 상품 재고 차감, 쿠폰 사용 처리, 주문 저장은 하나의 트랜잭션으로 처리합니다.
- 주문 취소 시 재고와 쿠폰 상태를 같은 트랜잭션에서 복구합니다.

## 동시성 제어 방식

Q1. 쿠폰 발급 수량은 다음 조건부 UPDATE로 보장합니다.

```sql
UPDATE coupon_policy
SET issued_quantity = issued_quantity + 1
WHERE id = ?
  AND issued_quantity < total_quantity
  AND started_at <= ?
  AND ended_at >= ?;
```

영향받은 row가 0이면 `COUPON_SOLD_OUT` 또는 발급 기간 오류로 처리합니다.

Q2. 동일 사용자 중복 발급은 `user_coupon(user_id, coupon_policy_id)` UNIQUE 제약조건으로 최종 방어합니다. 애플리케이션에서도 사전 조회를 하지만, 동시 요청의 마지막 보장은 DB 제약조건입니다.

Q3. 상품 재고는 다음 조건부 UPDATE로 차감합니다.

```sql
UPDATE product
SET stock_quantity = stock_quantity - ?
WHERE id = ?
  AND stock_quantity >= ?;
```

Q4. 동일 쿠폰 중복 사용은 `status = ISSUED` 조건이 포함된 UPDATE로 막습니다.

```sql
UPDATE user_coupon
SET status = 'USED', used_at = ?
WHERE id = ?
  AND user_id = ?
  AND status = 'ISSUED'
  AND expired_at >= ?;
```

Q5. 주문 생성 중 재고 차감, 쿠폰 사용, 주문 저장 중 하나라도 실패하면 전체가 롤백되어야 합니다. 그래서 하나의 `@Transactional` 경계 안에서 처리합니다.

Q6. 조건부 UPDATE는 짧은 row lock만 사용해 처리량이 좋고 구현이 단순합니다. 대신 실패 원인을 세밀하게 구분하려면 사전 조회와 예외 매핑이 추가로 필요합니다.

Q7. 비관적 락은 대기 시간이 길어질 수 있고, 낙관적 락은 재시도 설계가 필요합니다. Redis Lock은 운영 구성 요소가 늘어납니다. 이 과제의 핵심 정합성은 단일 RDBMS UPDATE 조건으로 충분히 보장되므로 조건부 UPDATE를 선택했습니다.

Q8. Docker Compose는 애플리케이션과 PostgreSQL을 같은 명령으로 재현 가능하게 실행하기 위해 사용했습니다.

Q9. 시간이 더 있다면 Flyway 기반 스키마 관리, Swagger 문서화, 쿠폰 만료 배치, 주문 목록 조회, 재고/쿠폰 이력 테이블을 추가하겠습니다.

## 트랜잭션 설계

- 쿠폰 발급: 사용자/정책 검증, 발급 수량 증가, 사용자 쿠폰 저장을 하나의 트랜잭션으로 처리합니다.
- 주문 생성: 사용자/상품 검증, 재고 차감, 쿠폰 사용 처리, 주문 저장을 하나의 트랜잭션으로 처리합니다.
- 주문 취소: 주문 상태 변경, 재고 복구, 쿠폰 복구를 하나의 트랜잭션으로 처리합니다.

## 예외 응답

```json
{
  "code": "OUT_OF_STOCK",
  "message": "상품 재고가 부족합니다."
}
```

주요 코드: `USER_NOT_FOUND`, `PRODUCT_NOT_FOUND`, `ORDER_NOT_FOUND`, `COUPON_POLICY_NOT_FOUND`, `USER_COUPON_NOT_FOUND`, `COUPON_POLICY_NOT_ACTIVE`, `COUPON_SOLD_OUT`, `DUPLICATED_COUPON_ISSUE`, `COUPON_OWNER_MISMATCH`, `COUPON_NOT_AVAILABLE`, `COUPON_EXPIRED`, `OUT_OF_STOCK`, `INVALID_ORDER_QUANTITY`, `ALREADY_CANCELED_ORDER`.

## 개선 가능 사항

- Flyway로 명시적인 DDL과 인덱스 관리
- Swagger UI 또는 Spring REST Docs
- 쿠폰 만료 스케줄러
- 사용자별 주문 목록 페이징
- 재고 변경 이력과 쿠폰 발급 이력 저장
