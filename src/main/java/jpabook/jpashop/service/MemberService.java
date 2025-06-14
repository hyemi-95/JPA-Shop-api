package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor // final이 있는 필드만 생성자 생성
public class MemberService {

    private final MemberRepository memberRepository;

    /*
     * 회원가입
     */
    @Transactional
    public Long join(Member member){
        validateDuplidateMember(member); //중복회원이 유무 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplidateMember(Member member) {
        //EXCEPTION
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if(!findMembers.isEmpty()){
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    /*
     * 회원 전체 조회
     */
    public List<Member> findMembers(){
        return memberRepository.findAll();
    }

    /*
     * 회원 단건 조회
     */
    public Member findOne(Long memberId){
        return memberRepository.findOne(memberId);
    }

    @Transactional
    public void update(Long id, String name) { //커맨드와 쿼리는 분리를 하는게 좋음 (이건 커맨드)
        Member member = memberRepository.findOne(id);
        member.setName(name);
    }
}
