package jpabook.jpashop.repository.order.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
    public class OrderSimpleQueryDto { //해당 Repository는 재사용성이 없음
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStaus;
        private Address address;
        public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStaus, Address address) {
            this.orderId = orderId;
            this.name = name;//LAZY초기화
            this.orderDate = orderDate;
            this.orderStaus = orderStaus;
            this.address = address;//LAZY초기화
        }
    }