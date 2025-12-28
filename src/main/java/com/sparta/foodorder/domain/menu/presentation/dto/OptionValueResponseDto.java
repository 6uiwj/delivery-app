package com.sparta.foodorder.domain.menu.presentation.dto;

import com.sparta.foodorder.domain.menu.domain.OptionValue;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OptionValueResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String value;
    private final String description;
    private final Integer addPrice;

    private OptionValueResponseDto(UUID id, String value, String description, Integer addPrice) {
        this.id =  id;
        this.value = value;
        this.description = description;
        this.addPrice = addPrice;
    }

    public static OptionValueResponseDto from(OptionValue optionValue) {
        return new OptionValueResponseDto (
                optionValue.getId(),
                optionValue.getValue(),
                optionValue.getDescription(),
                optionValue.getAddPrice()
        );
    }

    public static List<OptionValueResponseDto> findAllOptionValues(List<OptionValue>  optionValues) {
        return optionValues.stream()
                .filter(optionValue -> optionValue.getDeletedAt() == null)
                .map(OptionValueResponseDto::from).toList();
    }
}
