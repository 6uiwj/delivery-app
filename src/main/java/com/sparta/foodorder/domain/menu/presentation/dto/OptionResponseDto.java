package com.sparta.foodorder.domain.menu.presentation.dto;

import com.sparta.foodorder.domain.menu.domain.Option;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class OptionResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String optionName;
    private final UUID menuId;
    private final List<OptionValueResponseDto> optionValues;

    private OptionResponseDto(UUID id, String optionName, UUID menuId, List<OptionValueResponseDto> optionValues) {
        this.id = id;
        this.optionName = optionName;
        this.menuId = menuId;
        this.optionValues = optionValues;
    }


    public static OptionResponseDto from(Option option) {
        return new OptionResponseDto(
                option.getId(),
                option.getName(),
                option.getMenu().getId(),
                OptionValueResponseDto.findAllOptionValues(option.getOptionValues())
        );
    }

    public static List<OptionResponseDto> findAllOptions(List<Option> options) {
        return options.stream()
                .filter(Objects::nonNull)
                .map(OptionResponseDto::from)
                .toList();
    }
}
