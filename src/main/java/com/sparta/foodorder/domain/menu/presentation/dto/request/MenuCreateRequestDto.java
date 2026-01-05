package com.sparta.foodorder.domain.menu.presentation.dto.request;

import com.sparta.foodorder.domain.menu.domain.Menu;
import com.sparta.foodorder.domain.menu.domain.Option;
import com.sparta.foodorder.domain.store.domain.Store;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class MenuCreateRequestDto {

    private UUID storeId;


    @NotBlank
    private String name;

    private String description;

    @NotNull
    @PositiveOrZero
    private Integer price;

    private boolean hidden = false;

    private boolean active = true;

    @Valid
    List<OptionCreateRequestDto> options;

    public Menu toEntity(Store store) {

           Menu menu = Menu.create(
                this.name,
                this.description,
                this.price,
                store,
                this.hidden,
                this.active,
                new ArrayList<>()
        );

           for(OptionCreateRequestDto dto : options) {
               Option option = dto.toEntity();
               menu.addOption(option);
           }
        return menu;

    }
}
