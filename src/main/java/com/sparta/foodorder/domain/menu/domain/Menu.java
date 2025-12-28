package com.sparta.foodorder.domain.menu.domain;

import com.sparta.foodorder.domain.store.domain.Store;
import com.sparta.foodorder.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_menu")
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "price")
    private Integer price; //가격은 int로 ?

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "is_hidden")
    private boolean hidden = false;

    @Column(name = "is_active")
    private boolean active = true;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL)
    private List<Option> options = new ArrayList<>();

    private Menu(String name, String description, Integer price, Store store,
                 boolean hidden, boolean active) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.store = store;
        this.hidden = hidden;
        this.active = active;

    }

    public static Menu create(String name, String description, Integer price,
                              Store store, boolean hidden, boolean active,
                              List<Option> options) {
        Menu menu = new Menu(name, description, price, store, hidden, active);
        for(Option option : options) {
            menu.addOption(option);
        }

        return menu;
    }

    public void addOption(Option option) {
        if(options == null) {
            options = new ArrayList<>();
        }
        options.add(option);
        option.setMenu(this);
    }

    public void changeMenu(String name, String description, Integer price, boolean hidden, boolean active) {
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        this.hidden = hidden;
        this.active = active;
    }


    public void deleteMenu (String deletedBy) {
        deleteCascade(deletedBy);
        super.softDelete(deletedBy);
    }

    private void deleteCascade(String deletedBy) {
        for(Option option : this.options) {
            option.delete(deletedBy);
        }
    }

}
