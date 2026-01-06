package com.sparta.foodorder.domain.menu.domain;

import com.sparta.foodorder.domain.store.domain.Store;
import com.sparta.foodorder.global.common.BaseEntity;
import com.sparta.foodorder.global.exception.BusinessException;
import com.sparta.foodorder.global.exception.ErrorCode;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
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

    public Option addOption(Option option) {
        options.add(option);
        option.setMenu(this);
        return option;
    }

    public OptionValue addOptionValue(UUID optionId, String value, Integer price, String description) {
        Option option = getOption(optionId);
        return option.addOptionValue(value, price, description);

    }

    public Option getOption(UUID optionId) {
        return this.options.stream()
            .filter(option -> option.getId().equals(optionId) && !option.isDeleted())
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));
    }

    public List<Option> getActiveOptions() {
        return this.options.stream()
            .filter(option -> !option.isDeleted())
            .collect(Collectors.toList());
    }



    public void changeMenu(String name, String description, Integer price, Boolean hidden, Boolean active) {
        if (name != null && !name.isBlank()) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (hidden != null) this.hidden = hidden;
        if (active != null) this.active = active;
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

    public void validateAccess(Long userId, boolean isAdmin) {
        Store store = getStore();

        if (store.isDeleted() || !store.getIsActive()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        if(!isAdmin && !store.getOwnerId().equals(userId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

}
