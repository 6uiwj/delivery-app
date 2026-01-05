package com.sparta.foodorder.domain.store.domain;

import com.sparta.foodorder.domain.store.application.dto.StoreCreateRequestDto;
import com.sparta.foodorder.domain.store.application.dto.StoreDetailResponseDto;
import com.sparta.foodorder.domain.store.application.dto.StoreResponseDto;
import com.sparta.foodorder.domain.store.application.dto.StoreUpdateRequestDto;
import com.sparta.foodorder.domain.user.domain.UserRole;
import com.sparta.foodorder.global.dto.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface StoreService {

    /**
     * 가게 생성 메서드
     */
    StoreResponseDto createStore(
        StoreCreateRequestDto storeCreateRequestDto,
        Long userId,
        UserRole role
    );

    /**
     * 가게 수정 메서드
     */
    StoreResponseDto updateStore(
        StoreUpdateRequestDto storeUpdateRequestDto,
        UUID storeId,
        String email,
        UserRole role
    );

    /**
     * 가게 삭제 메서드
     */
    void deleteStore(UUID storeId, String email, UserRole role);

    /**
     * 가게목록 조회 메서드 검색 키워드 있을 경우, 키워드 검색 결과 가게 목록 조회
     */
    PagedResponse<StoreResponseDto> getStores(String query, Pageable pageable, UserRole role);

    /**
     * 가게 상세조회 메서드
     */
    StoreDetailResponseDto getStore(UUID storeId, UserRole role);

    /**
     * 카테고리별 가게 조회 메서드
     */
    PagedResponse<StoreResponseDto> getStoresByCategory(UUID categoryId, Pageable pageable,
        UserRole role);

    Store findByUUID(UUID storeId);

    void validateExistenceById(UUID storeId);

    Store updateRating(UUID storeId);

    List<Store> findAllByIds(Set<UUID> storeIds);

    void validateOwner(Long userId);
}