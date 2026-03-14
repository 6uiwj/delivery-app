package com.sparta.foodorder.domain.menu.domain;

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

import java.util.*;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "p_option")
public class Option extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_id", nullable = false, columnDefinition = "BINARY(16)")
    private Menu menu;

    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private Set<OptionValue> optionValues = new HashSet<>();


    @Column(name = "name", nullable = false)
    private String name;

    private Option(String name) {
        this.name = name;
    }

    public static Option create(String optionName) {
        return new Option(optionName);
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public OptionValue addOptionValue(String value, Integer price, String description) {

        if (this.isDeleted()) {
            throw new BusinessException(ErrorCode.OPTION_NOT_FOUND);
        }
        OptionValue optionValue = OptionValue.create(
            value,
            description,
            price
        );

        this.optionValues.add(optionValue);
        optionValue.setOption(this);
        return optionValue;
    }

    public OptionValue getOptionValue(UUID optionValueId) {
        return this.optionValues.stream()
            .filter(v -> v.getId().equals(optionValueId) && !v.isDeleted())
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_VALUE_NOT_FOUND));
    }

    public Set<OptionValue> getActiveOptionValues() {
        return this.optionValues.stream()
            .filter(v -> !v.isDeleted())
            .collect(Collectors.toSet());
    }

    public void updateOption(String name) {
        if(this.isDeleted()) {
            throw new BusinessException(ErrorCode.OPTION_NOT_FOUND);
        }
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
