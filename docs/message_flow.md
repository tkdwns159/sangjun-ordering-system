## [[return]](../README.md)

## 메세지 플로우

<p>
    <img src="../img/saga-2.png"/>
</p>

### 정상 실행 흐름

1. Order Service에 주문이 요청되고, 주문 데이터를 생성합니다. 초기 주문 상태는 PENDING 입니다.
2. Order Service에서 해당 주문에 대한 결제 요청을 Payment Service로 보냅니다. (ORDER CREATED)
3. 결제가 완료되면, Payment Service에서 Order Service로 완료 메세지를 보냅니다. (PAYMENT COMPLETED)
4. 해당 메세지를 수신한 Order Service는 주문 상태를 PENDING에서 PAID 로 바꿉니다.
5. Order Service에서 해당 주문에 대한 식당의 승인을 받기위해 Restaurant Service에 승인 요청을 보냅니다. (ORDER PAID)
6. 승인이 완료되면, Restaurant Service에서 Order Service로 완료 메세지를 보냅니다.(ORDER APPROVED)
7. 해당 메세지를 수신한 Order Service는 주문 상태를 APPROVED로 바꿉니다.

<p>
    <img src="../img/saga(failure).png"/>
</p>

### 결제 실패시

결제 실패시, 주문 상태를 PENDING에서 CANCELLED로 바꾸고 실패처리합니다.

### 식당 승인 거부 및 실패시

식당 승인 요청을 하기 위해서는 결제가 성공적으로 완료되어야하기 때문에, 해당 주문의 현재 주문상태는 PAID 입니다.

1. 식당 승인이 실패하거나 거부되었을 경우, Restaurant Service에서 승인 실패 메세지를 보냅니다. (ORDER CANCELLED)
2. 해당 메세지를 수신한 Order Service는 주문 상태를 PAID 에서 CANCELLING 으로 바꿉니다.
3. Order Service는 결제를 철회하기 위해 Payment Service에 결제 철회 메세지를 보냅니다. (ORDER CANCELLED)
3. 결제가 성공적으로 철회되면, Payment Service는 Order Service에 완료 메세지를 보냅니다. (PAYMENT CANCELLED)
4. 해당 메세지를 수신한 Order Service는 주문 상태를 CANCELLED 로 바꾸고 실패처리합니다.
