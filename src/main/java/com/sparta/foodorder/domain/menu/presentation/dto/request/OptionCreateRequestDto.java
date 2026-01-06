package com.sparta.foodorder.domain.menu.presentation.dto.request;

import com.sparta.foodorder.domain.menu.domain.Option;
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

        Option option = Option.create(this.optionName);

        for (OptionValueCreateRequestDto dto : optionValues) {
            option.addOptionValue(
                dto.getValue(),
                dto.getAddPrice(),
                dto.getDescription()
            );
        }

        return option;
    }
}
