package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository { //순수 엔티티 조회하는데 쓰고 성능을 위한 패치조인 정도 (재사용도 가능)

    private final EntityManager em;

    public void save(Order order){
        em.persist(order);
    }

    public Order findOne(Long id){
        return em.find(Order.class,id);
    }

    //검색 => 동적쿼리를 해야함 JPQL 실무X
    public List<Order> findAllByString(OrderSearch orderSearch){
        String jpql = "select o from Order o join o.member m";
        boolean isFirstCondition =true;

        //주문 상태 검색
        if(orderSearch.getOrderStatus() != null){ // 상태가 null이 아닐때
            if(isFirstCondition){
                jpql +=" where";
                isFirstCondition =false;
            }else {
                jpql +=" and";
            }
            jpql +=" o.status = :status";
        }

        //회원이름검색
        if(StringUtils.hasText(orderSearch.getMemberName())){ //파라미터에서 name 에 값이 있을떄
            if(isFirstCondition){
                jpql +=" where";
                isFirstCondition =false;
            }else {
                jpql +=" and";
            }
            jpql +=" o.name like :name";
        }

        TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000);//최대 1000건

        //파라미터 set
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();

    }

    /**
     * JPA Creteria
     * JPA 표준스팩이지만 유지보수성이 너무 떨어짐, 실무X
     * */
    public List<Order> findAllByCriteria(OrderSearch orderSearch){

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Order> cq = cb.createQuery(Order.class);
        Root<Order> o = cq.from(Order.class);
        Join<Order, Member> m = o.join("member", JoinType.INNER); //회원과 조인
        List<Predicate> criteria = new ArrayList<>();
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            Predicate status = cb.equal(o.get("status"),
                    orderSearch.getOrderStatus());
            criteria.add(status);
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getMemberName())) {
            Predicate name =
                    cb.like(m.<String>get("name"), "%" + orderSearch.getMemberName() + "%");
            criteria.add(name);
        }
        cq.where(cb.and(criteria.toArray(new Predicate[criteria.size()])));
        TypedQuery<Order> query = em.createQuery(cq).setMaxResults(1000); //최대1000건
        return query.getResultList();
    }

    public List<Order> findAllWithMemberDelivery() {//fetch는 LAZY무시하고 모두 한번에 다 select해옴. 프록시가 아닌 진짜 객체의 값을 다 가져와서 채워줌 => 패치조인
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" , Order.class
        ).getResultList();
    }

    public List<Order> findAllWithItem() {//컬랙션은 패치조인 시 페이징 불가능
        return em.createQuery("select distinct o from Order o" +
                " join fetch o.member m" +
                " join fetch o.delivery d" +
                " join fetch o.orderItems oi" +
                " join fetch oi.item i", Order.class).getResultList();
    }

    public List<Order> findAllWithMemberDelivery2(int offset, int limit) {//페이징 => default_batch_fetch_size 설정을 하면 toOne도 fetch를 안해도 다 가져온다
        return em.createQuery(
                        "select o from Order o" +
                                " join fetch o.member m" +
                                " join fetch o.delivery d", Order.class
                ).setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
