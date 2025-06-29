package jpabook.jpashop.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED) //패키지 외에서 기본생성자 생성 못하도록 제어
public class Order {

    @Id @GeneratedValue
    @Column(name = "order_id")
    private  Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="member_id")
    private Member member; //왜래키 주인

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL) //cascade는 라이프사이클이 완벽하게 같을때 사용(주의)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id")
    private Delivery delivery;//배송정보

    private LocalDateTime orderDate;//wnanstlrks

    @Enumerated(EnumType.STRING)
    private OrderStatus status;//주문상태 [ORDER, CANCEL]

    //연관과계 메서드
    public void setMember(Member member){
        this.member = member;
        member.getOrders().add(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }
    //연관과계 메서드 end
    
    //생성 메서드
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems ){
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem: orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    //비즈니스 로직
    /**
     * 주문취소
     */
    public void cancel(){
        if(delivery.getDeliveryStatus() == DeliveryStatus.COMP){ // 이미 배송완료
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }
        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    //조회 로직
    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }
}
