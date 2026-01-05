package com.sparta.foodorder.domain.menu.presentation.dto.response;

import com.sparta.foodorder.domain.menu.domain.Menu;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Getter;

@Getter
public class MenuResponseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String name;
    private String description;
    private Integer price; //가격은 int로 ?
    private boolean hidden;
    private boolean active;
    private UUID storeId;
    private final List<OptionResponseDto> options;

    private MenuResponseDto(UUID id, String name, String description, Integer price, boolean hidden, boolean active, UUID storeId, List<OptionResponseDto> options) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.hidden = hidden;
        this.active = active;
        this.storeId = storeId;
        this.options = options;

    }

    public static MenuResponseDto from(Menu menu) {
        return new MenuResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice(),
                menu.isHidden(),
                menu.isActive(),
                menu.getStore().getId(),
                OptionResponseDto.findAllOptions(menu.getOptions())

        );
    }
}
