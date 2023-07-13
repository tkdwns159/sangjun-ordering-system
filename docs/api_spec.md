## [[return]](../README.md)

## POST /orders

- Request Body

``````
{
  "customerId": "49f72dbd-ae1d-4f6b-b17c-122d402e55c5",  // 고객 ID (UUID 형식)
  "restaurantId": "d215b5f8-0249-4dc5-89a3-51fd148cfb45",  // 음식점 ID (UUID 형식)
  "orderAddress": {
    "street": "street_1",  // 도로명 주소
    "postalCode": "1000AB",  // 우편번호
    "city": "Amsterdam"  // 도시명
  },
  "price": 200.00,  // 총 주문 가격
  "items": [  // 주문 항목 배열
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",  // 제품 ID (UUID 형식)
      "quantity": 1,  // 수량
      "price": 50.00,  // 개당 가격
      "subTotal": 50.00  // 합계
    },
    {
      "productId": "d215b5f8-0249-4dc5-89a3-51fd148cfb48",  // 제품 ID (UUID 형식)
      "quantity": 3,  // 수량
      "price": 50.00,  // 개당 가격
      "subTotal": 150.00  // 합계
    }
  ]
}
``````

- 성공 응답 (HTTP 상태 코드: 200 OK)

``````
{
    "orderTrackingId": "6688de1d-a5ba-4f7f-af17-4a16fd7f833b",
    "orderStatus": "PENDING",
    "message": "Order Created Successfully"
}
``````

- 실패 응답 (HTTP 상태 코드: 400 BAD REQUEST)

``````
{
    "code": "Bad Request",
    "message": {실패 사유}
}
``````

---

## GET /orders/{orderTrackingId}

- 요청 예시

``````
GET /orders/f410ab7d-8678-49b4-84d6-35c00933932e
``````

- 성공 응답 (HTTP 상태 코드: 200 OK)

``````
{
    "orderTrackingId": "f410ab7d-8678-49b4-84d6-35c00933932e",  // 주문 추적 ID (UUID 형식)
    "orderStatus": "APPROVED",  // 주문 상태 (예: APPROVED, PAID, APPROVED)
    "failureMessages": [  // 실패 메시지 배열 (주문이 성공적으로 처리되면 빈 배열)
        ""
    ]
}
``````

- 실패 응답 (HTTP 상태 코드: 400 BAD REQUEST)

``````
{
    "code": "Not Found",
    "message": "Could not find order with tracking idf430ab7d-8678-49b4-84d6-35c00933932e"
}
``````
