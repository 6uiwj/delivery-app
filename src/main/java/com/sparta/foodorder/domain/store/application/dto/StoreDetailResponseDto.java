package com.sparta.foodorder.domain.store.application.dto;

import com.sparta.foodorder.domain.menu.presentation.dto.response.MenuResponseDto;
import com.sparta.foodorder.domain.store.domain.Store;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record StoreDetailResponseDto(
    UUID storeId,
    Long ownerId,
    List<CategoryResponseDto> categories,
    List<MenuResponseDto> menus,
    String name,
    String description,
    String address,
    Double longitude,
    Double latitude,
    String phoneNumber,
    Boolean isActive,
    Long ratingCount,
    Double ratingAvg,
    Long deliveryFee,
    Long minOrderAmount,
    LocalTime opensAt,
    LocalTime closesAt,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static StoreDetailResponseDto from(
        Store store,
        List<CategoryResponseDto> categoryResponseDtoList,
        List<MenuResponseDto> menuResponseDtoList
    ) {
        return StoreDetailResponseDto.builder()
            .storeId(store.getId())
            .ownerId(store.getOwnerId())
            .categories(categoryResponseDtoList)
            .menus(menuResponseDtoList)
            .name(store.getName())
            .description(store.getDescription())
            .address(store.getAddress())
            .longitude(store.getLocation().getX())
            .latitude(store.getLocation().getY())
            .phoneNumber(store.getPhoneNumber())
            .isActive(store.getIsActive())
            .ratingCount(store.getRatingCount())
            .ratingAvg(store.getRatingAvg())
            .deliveryFee(store.getDeliveryFee())
            .minOrderAmount(store.getMinOrderAmount())
            .opensAt(store.getOpensAt())
            .closesAt(store.getClosesAt())
            .createdAt(store.getCreatedAt())
            .updatedAt(store.getUpdatedAt())
            .build();
    }
}
