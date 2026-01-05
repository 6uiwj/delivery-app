package com.sparta.foodorder.domain.menu.presentation.dto.request;

import com.sparta.foodorder.domain.menu.domain.Option;
import com.sparta.foodorder.domain.menu.domain.OptionValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionCreateRequestDto {

    @NotBlank
    private String optionName;

    @Valid
    List<OptionValueCreateRequestDto> optionValues;

    public Option toEntity() {

        List<OptionValue> optionValueList = optionValues.stream()
            .map(dto -> OptionValue.create(
                dto.getValue(), dto.getDescription(), dto.getAddPrice()))
            .toList();

        return Option.create(
                this.optionName,
                optionValueList
        );
    }

}
