package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    @PersistenceContext
    private final EntityManager em;

    public void save(Member member) { //저장
        em.persist(member);
    }

    public Member findOne(Long id) {//단건 조회
        return em.find(Member.class, id);
    }

    public List<Member> findAll() { //리스트 조회 JPQL은 테이블이 아닌 객체를 조회해옴
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findByName(String name) { //파라미터 바인딩 후 조회
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name) //파라미터 set
                .getResultList();
    }
}
