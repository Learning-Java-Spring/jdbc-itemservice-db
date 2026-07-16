package hello.itemservice.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "item")
public class Item {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", length = 50)
    private String itemName;
    @Column(name = "price")
    private Integer price;
    @Column(name = "quantity")
    private Integer quantity;


    //JPA는 public 기본생성자가 꼭 있어야 한다.
    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
