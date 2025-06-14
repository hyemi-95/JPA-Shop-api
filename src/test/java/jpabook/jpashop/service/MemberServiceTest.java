package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 해당 어노테이션은 test에 있으면 기본적으로 RollBack을 시킴 
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    
//    @Autowired EntityManager em;

    @Test
//    @Rollback(value = false) //롤백하지 않겠다
    public void 회원가입() throws Exception{

        //given
        Member member = new Member();
        member.setName("Kim");

        //when
        Long savedId = memberService.join(member);

        //then
//        em.flush(); //롤백은 할거지만 insert문을 확인하고 싶음
        assertEquals(member, memberRepository.findOne(savedId)); // member와 memberRepository.findOne(savedId) 결과가 똑같은지 체크 true 가 나오면 회원가입이 정상적으로 된 것
    }
   @Test
    public void 중복_회원_예외() throws Exception{
        //Given
       Member member1 = new Member();
       member1.setName("kim");
       Member member2 = new Member();
       member2.setName("kim");
        //When
       memberService.join(member1);
        //Then
        //IllegalStateException 예외가 발생하지 않으면 테스트 실패
       assertThrows(IllegalStateException.class, () ->
               memberService.join(member2));

    }

}