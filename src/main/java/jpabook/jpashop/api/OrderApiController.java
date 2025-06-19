package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepositoy;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {//컬렉션 조회

    private final OrderRepository orderRepository;
    private final OrderQueryRepositoy orderQueryRepositoy;

    /**
     * V1. 엔티티 직접 노출
     * - Hibernate5Module 모듈 등록, LAZY=null 처리
     * - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> orderV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();//open-in-view: false 를 하면 지연로딩(초기화)불가 - 모든 지연로딩을 트랜잭션 안으로 넣거나 패치조인 해야서 List<Order> all 에 넘겨야 함
            order.getDelivery().getAddress();
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName());//LAZY초기화를 해준 이유 : Hibernate5JakartaModule 를 하면 프록시인 애는 데이타를 안뿌림
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDTO> orderV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDTO> collect = orders.stream()
                .map(o -> new OrderDTO(o))
                .collect(toList());
        return collect;
    }

    @GetMapping("/api/v3/orders")//컬렉션 페치 조인을 사용하면 페이징이 불가능함
    public List<OrderDTO> orderV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        for (Order order : orders) {
            System.out.println("order ref ="+ order + " id=" + order.getId());
        }

        List<OrderDTO> collect = orders.stream()
                .map(o -> new OrderDTO(o))
                .collect(toList());
        return collect;
    }

    /**
     * ToOne 관계는 페치 조인해도 페이징에 영향을 주지 않는다. 따라서 ToOne 관계는 페치조인으로 쿼리 수를 줄이고 해결하고,
     * 나머지는 `hibernate.default_batch_fetch_size` 로 최적화 하자.
     * */
    @GetMapping("/api/v3.1/orders")//페이징 한계 돌파
    public List<OrderDTO> orderV3_page(@RequestParam(value = "offset",defaultValue = "0") int offset,
                                       @RequestParam(value = "limit",defaultValue = "100") int limit)
    {
        List<Order> orders = orderRepository.findAllWithMemberDelivery2(offset,limit);
        List<OrderDTO> collect = orders.stream()
                .map(o -> new OrderDTO(o))
                .collect(toList());
        return collect;
    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> orderV4() {
        return orderQueryRepositoy.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders") //엔티티방식으로 하면 `hibernate.default_batch_fetch_size` 쓰는 V3.1과 같은 성능
    public List<OrderQueryDto> orderV5() {
        return orderQueryRepositoy.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    public List<OrderQueryDto> orderV6() {
        List<OrderFlatDto> flats = orderQueryRepositoy.findAllByDto_flat(); //Order기준으로 페이징이 불가능. 하려면 아래  처럼

        //루프돌리기 OrderFlatDto->OrderQueryDto
        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue())).collect(toList());
    }

    @Data
    private class OrderDTO {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
//        private List<OrderItem> orderItems;//DTO에 감싸긴했지만 엔티티가 모두 노출이 됨 -> 이 또한 DTO필요
        private List<OrderItemDTO> orderItems;
        public OrderDTO(Order order) {
              orderId = order.getId();
              name = order.getMember().getName();
              orderDate = order.getOrderDate();
              orderStatus = order.getStatus();
              address = order.getDelivery().getAddress();
//              order.getOrderItems().stream().forEach(o -> o.getItem().getName());// 초기화, 초기화 안하면 NULL 엔티티이기 떄문에
//              orderItems = order.getOrderItems();
              orderItems = order.getOrderItems().stream().map(orderItem -> new OrderItemDTO(orderItem)).collect(toList());
        }
    }

    @Getter // 또는 Data
    private class OrderItemDTO {

        private String itemName;//상품명
        private  int orderPrice;//상품가격
        private  int count;//주문 수량
        public OrderItemDTO(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }
}
/*
* 
forEach(...)는 읽기만, map(...).collect(...)는 변형해서 저장까지.
실무에서는 map()을 주로 DTO로 변환할 때 쓰고, forEach()는 단순 접근하거나 Lazy 강제 초기화 용도로 사용
*
* */