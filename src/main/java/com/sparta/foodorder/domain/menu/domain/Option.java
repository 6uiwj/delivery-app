package com.sparta.foodorder.domain.menu.domain;

import com.sparta.foodorder.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_option")
public class Option extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @OneToMany(fetch = FetchType.LAZY)
    private List<OptionValue> optionValues;


    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    public Option(Menu menu, String name,  List<OptionValue> optionValues) {
        this.menu = menu;
        this.name = name;
        this.optionValues = optionValues != null ? optionValues : new ArrayList<>();

    }

    public static Option create(Menu menu, String name, List<OptionValue> optionValues) {
        return new Option(menu, name, optionValues);
    }

    public void updateOption(String name) {
        this.name = name;
    }

    public void delete(String deletedBy) {
        deleteCascade(deletedBy);
        this.softDelete(deletedBy);
    }

    private void deleteCascade(String deletedBy) {
        for(OptionValue optionValue : this.optionValues) {
            optionValue.softDelete(deletedBy);
        }
    }
}
