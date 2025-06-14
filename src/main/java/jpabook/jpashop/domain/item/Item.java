package jpabook.jpashop.domain.item;

import jakarta.persistence.*;
import jpabook.jpashop.domain.Category;
import jpabook.jpashop.exception.NotEnoughStockException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.EnableMBeanExport;

import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)//테이블전략
@DiscriminatorColumn(name="dtype")//군분잡기
@Getter @Setter
public abstract class Item {

    @Id @GeneratedValue
    @Column(name="item_id")
    private Long id;

    private String name;

    private int price;//가격

    private int stockQuantity;//재고

    @ManyToMany(mappedBy = "items")
    private List<Category> categories = new ArrayList<>();
    
    //비즈니스 로직 추가 - 엔티티에 있는게 응집력이 좋다.(도메인 모텔 패턴) / 서비스계층에서 비즈니스 로직을 처리는 (트랜잭션 스크립트 패턴)
    //또한 setter를 사용하기보다는 아래와 같이 비지니스 로직(필요 메서드)을 활용해서 값을 수정해야함 => 객체지향적으로
    
    /**
     * stock (재고)증가
     * */
    public void addStock(int quantity){
        this.stockQuantity +=quantity;
    }

    /**
     * stock (재고)감소
     * */
    public void removeStock(int quantity){
        int restStock = this.stockQuantity - quantity;
        if(restStock < 0){ //재고수량이 0보다 작다면
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }
}
