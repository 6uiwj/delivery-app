package com.sparta.foodorder.domain.menu.presentation.dto.response;

import com.sparta.foodorder.domain.menu.domain.Menu;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuSearchResponseDto {
    private UUID id;
    private String name;
    private String description;
    private int price;

    public MenuSearchResponseDto(UUID id, String name, String description, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public static MenuSearchResponseDto fromEntity(Menu menu) {
        return new MenuSearchResponseDto(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice()
        );
    }
}