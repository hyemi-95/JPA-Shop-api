package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty
    private String name;

    @Embedded
    private Address address;

//    @JsonIgnore //->Response에서 해당 필드는 무시하고 반환하기(password에서는 쓰겠지..?)
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

}
