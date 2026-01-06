package com.sparta.foodorder.domain.menu.presentation.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MenuUpdateRequestDto {
    private UUID storeId;
    private String name;
    private String description;

    private Integer price;

    private Boolean hidden;
    private Boolean active;
}
