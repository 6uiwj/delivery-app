package com.sparta.foodorder.domain.menu.presentation.dto.response;

import com.sparta.foodorder.domain.menu.domain.Option;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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


    public static OptionResponseDto fromAll(Option option) {

        return new OptionResponseDto(
                option.getId(),
                option.getName(),
                option.getMenu().getId(),
                option.getOptionValues().stream()
                    .map(OptionValueResponseDto::from)
                    .toList());
    }

    public static OptionResponseDto fromActive(Option option) {

        return new OptionResponseDto(
            option.getId(),
            option.getName(),
            option.getMenu().getId(),
            option.getActiveOptionValues().stream()
                .map(OptionValueResponseDto::from)
                .toList());
    }
    public static OptionResponseDto from(Option option) {
        return fromAll(option);
    }

    public static List<OptionResponseDto> findAllOptions(Set<Option> options, boolean showAll) {
        return options.stream()
            .filter(Objects::nonNull)
            .map(option -> showAll
                ? OptionResponseDto.fromAll(option)
                : OptionResponseDto.fromActive(option))
            .toList();
    }

    public static List<OptionResponseDto> findAllOptions(Set<Option> options) {
        return findAllOptions(options, false);
    }
}
