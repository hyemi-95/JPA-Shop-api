package jpabook.jpashop.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberApiController {

    private final MemberService memberService;

    //회원등록
    //json으로  받음 (postman활용)
    @PostMapping("/api/v1/members")
    public CreateMemberResponse saveMemberV1(@RequestBody @Valid Member member) {//엔티티 ->어떤 값이 넘어올지모름(엔티티를 외부로 노출하지 말길..)
        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    //회원등록
    @PostMapping("/api/v2/members")
    public CreateMemberResponse saveMemberV2(@RequestBody @Valid CreateMemberRequest request) {//DTO

        Member member = new Member();
        member.setName(request.getName());

        Long id = memberService.join(member);
        return new CreateMemberResponse(id);
    }

    @Data
    static class CreateMemberResponse {
        private Long id;

        public CreateMemberResponse(Long id) {
            this.id = id;
        }
    }

    @Data
    static class CreateMemberRequest {//api스펙을 한눈에 알수잇음 어떤 필드가 넘언오는지
        @NotEmpty
        private String name;

        public CreateMemberRequest(String name) {
            this.name = name;
        }
    }

    //회원수정
    @PutMapping("api/v2/members/{id}")
    public UpdateMemberPosponse updateMemberV2(@PathVariable("id") Long id, @RequestBody @Valid UpdateMemberRequest request) {

        memberService.update(id, request.getName());
        Member findMember = memberService.findOne(id);

        return new UpdateMemberPosponse(findMember.getId(), findMember.getName());
    }

    @Data
    @AllArgsConstructor //모든 필드를 받는 생성자
    static class UpdateMemberPosponse {

        private Long id;
        private String name;
    }

    @Data
    static class UpdateMemberRequest {

        private String name;
    }

    //회원 조회
    @GetMapping("api/v1/members")
    public List<Member> membersV1() {
        return memberService.findMembers();
    }

    @GetMapping("api/v2/members")
    public Result membersV2() {
        List<Member> findMembers = memberService.findMembers();
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getName()))
                .collect(Collectors.toList());

        return new Result(collect,collect.size(),"OK"); //감싸서 반환
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
        private int count;
        private String status;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
    }
}
