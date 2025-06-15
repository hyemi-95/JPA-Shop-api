package jpabook.jpashop.api;


import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
//            order.getOrderItems().forEach(oi -> {oi.getItem().getName();});// LAZY 강제 초기화
        }
        return all;
    }

    //ctrl+alt+n -> 줄임
    //alt+enter -> static+import

    /**
     * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
     * - 단점: 지연로딩으로 쿼리 N번 호출
     */
    @GetMapping("/api/v2/simple-orders")//DTO사용(절대적)
    public List<SimpleOrderDto> ordersV2(){
        return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(toList());//map은 a를 b로 바꾼다는 것. 즉 order를 SimpleOrderDto로 바꿈. 그리고 collect로 해서 list로 반환
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

}
