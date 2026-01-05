package com.sparta.foodorder.domain.menu.domain;

import com.sparta.foodorder.global.common.BaseEntity;
import com.sparta.foodorder.global.exception.BusinessException;
import com.sparta.foodorder.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.stream.Collectors;
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

    @OneToMany(mappedBy = "option", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionValue> optionValues;


    @Column(name = "name", nullable = false)
    private String name;

    @Builder
    public Option(String name,  List<OptionValue> optionValues) {
        this.name = name;
        this.optionValues = optionValues != null ? optionValues : new ArrayList<>();

    }

    public static Option create(@NotBlank String optionName, List<OptionValue> optionValueList) {
        return Option.builder()
            .name(optionName)
            .optionValues(optionValueList)
            .build();
    }

    public OptionValue addOptionValue(String value, Integer price, String description) {
        if (optionValues == null) {
            optionValues = new ArrayList<>();
        }

        if (this.isDeleted()) {
            throw new BusinessException(ErrorCode.OPTION_NOT_FOUND);
        }
        OptionValue optionValue = OptionValue.create(
            value,
            description,
            price
        );

        this.optionValues.add(optionValue);
        return optionValue;
    }

    public OptionValue getOptionValue(UUID optionValueId) {
        return this.optionValues.stream()
            .filter(v -> v.getId().equals(optionValueId) && !v.isDeleted())
            .findFirst()
            .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_VALUE_NOT_FOUND));
    }

    public List<OptionValue> getActiveOptionValues() {
        return this.optionValues.stream()
            .filter(v -> !v.isDeleted())
            .collect(Collectors.toList());
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
