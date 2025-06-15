package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

/**
 * XToOne관계(ManyToOne, OneToOne)
 * Order
 * Order -> Member
 * Order -> Delivery
 * */
@RestController // 일반 컨트롤러로 하면 retrun을 무조건 뷰로 인식함
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimpleQueryRepository orderSimpleQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5JakartaModule 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());//이 한줄만 쓰면 무한루프(엔티티와 컬렉션의 무한루프..)=> 양방향 걸리는건 모두 JsonIgnore 처리 해줘야함
        for (Order order : all) {//원하는 것만 가능
            order.getMember().getName();// LAZY 강제 초기화
            order.getDelivery().getAddress();// LAZY 강제 초기화
//            List<OrderItem> orderItems = order.getOrderItems();
//            orderItems.stream().forEach(o ->o.getItem().getName());//LAZY초기화
        }
        return all;
    }

    //ctrl+alt+n -> 줄임
    //alt+enter -> static+import

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X) , .DTO변환사용은 @JsonIgnore 필요없음
     * - 단점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")//DTO사용(절대적)
    public List<SimpleOrderDto> ordersV2(){
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(toList());//map은 a를 b로 바꾼다는 것. 즉 order를 SimpleOrderDto로 바꿈. 그리고 collect로 해서 list로 반환
    }

    /**
     * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
     * - fetch join으로 쿼리 1번 호출
     * 참고: fetch join에 대한 자세한 내용은 JPA 기본편 참고(정말 중요함)
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();//패치조인호출
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(toList());
        return result;
    }

    @Data
    private class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStaus;
        private Address address;
        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getName();//LAZY초기화
            this.orderDate = order.getOrderDate();
            this.orderStaus = order.getStatus();
            this.address = order.getDelivery().getAddress();//LAZY초기화
        }
    }

    /**
     * V4. JPA에서 DTO로 바로 조회
     * - 쿼리 1번 호출
     * - select 절에서 원하는 데이터만 선택해서 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderSimpleQueryRepository.findOrderDtos();
    }


// v2 : 총 5번 (1+N)
//    select o1_0.order_id,o1_0.delivery_id,o1_0.member_id,o1_0.order_date,o1_0.status from orders o1_0 join member m1_0 on m1_0.member_id=o1_0.member_id fetch first 1000 rows only
//    select m1_0.member_id,m1_0.city,m1_0.street,m1_0.zipcode,m1_0.name from member m1_0 where m1_0.member_id=1
//    select d1_0.delivery_id,d1_0.city,d1_0.street,d1_0.zipcode,d1_0.delivery_status from delivery d1_0 where d1_0.delivery_id=1
//    select m1_0.member_id,m1_0.city,m1_0.street,m1_0.zipcode,m1_0.name from member m1_0 where m1_0.member_id=2
//    select d1_0.delivery_id,d1_0.city,d1_0.street,d1_0.zipcode,d1_0.delivery_status from delivery d1_0 where d1_0.delivery_id=2
//
// v3 : 총 1번 => 모든 컬럼
//    select o1_0.order_id,d1_0.delivery_id,d1_0.city,d1_0.street,d1_0.zipcode,d1_0.delivery_status,m1_0.member_id,m1_0.city,m1_0.street,m1_0.zipcode,m1_0.name,o1_0.order_date,o1_0.status from orders o1_0 join member m1_0 on m1_0.member_id=o1_0.member_id join delivery d1_0 on d1_0.delivery_id=o1_0.delivery_id
//
// v4 : 총 1번 => 내가 원하는 컬럼만 가능
//select o1_0.order_id,m1_0.name,o1_0.order_date,o1_0.status,d1_0.city,d1_0.street,d1_0.zipcode from orders o1_0 join member m1_0 on m1_0.member_id=o1_0.member_id join delivery d1_0 on d1_0.delivery_id=o1_0.delivery_id
}
