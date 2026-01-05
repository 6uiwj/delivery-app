package com.sparta.foodorder.domain.store.application;

import com.sparta.foodorder.domain.menu.domain.Menu;
import com.sparta.foodorder.domain.menu.presentation.dto.response.MenuResponseDto;
import com.sparta.foodorder.domain.review.application.dto.RatingStats;
import com.sparta.foodorder.domain.review.domain.ReviewRepository;
import com.sparta.foodorder.domain.store.application.dto.*;
import com.sparta.foodorder.domain.store.domain.*;
import com.sparta.foodorder.domain.user.domain.UserRole;
import com.sparta.foodorder.global.dto.PagedResponse;
import com.sparta.foodorder.global.exception.BusinessException;
import com.sparta.foodorder.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final CategoryService categoryService;

    private static final Set<Integer> PERMITTED_PAGE_SIZES = Set.of(10, 30, 50);
    private static final int DEFAULT_PAGE_SIZE = 10;

    @Override
    @Transactional
    public StoreResponseDto createStore(
        StoreCreateRequestDto requestDto,
        Long userId,
        UserRole role
    ) {
        validateOwnerRole(role);

        if (storeRepository.existsByNameAndIsActiveTrue(requestDto.name())) {
            throw new BusinessException(ErrorCode.STORE_ALREADY_EXIST);
        }
        if (storeRepository.existsByOwnerIdAndIsActiveTrue(userId)) {
            throw new BusinessException(ErrorCode.OWNER_ALREADY_HAS_STORE);
        }
        if (storeRepository.existsByPhoneNumberAndIsActiveTrue(requestDto.phoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_ALREADY_EXIST);
        }

        Set<UUID> categoryIds = requestDto.categories();
        List<Category> categories = categoryRepository.findByIdIn(categoryIds);
        if (categoryIds.size() != categories.size()) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Store store = Store.createStore(
            userId, requestDto.name(), requestDto.description(), requestDto.address(),
            requestDto.longitude(), requestDto.latitude(), requestDto.phoneNumber(),
            requestDto.minOrderAmount(), requestDto.deliveryFee(),
            requestDto.opensAt(), requestDto.closesAt()
        );
        categories.forEach(store::addCategory);

        Store savedStore = storeRepository.save(store);

        return StoreResponseDto.from(savedStore);
    }

    @Override
    @Transactional
    public StoreResponseDto updateStore(
        StoreUpdateRequestDto requestDto,
        UUID storeId,
        String email,
        UserRole role
    ) {
        validateOwnerRole(role);
        Store store = getValidStore(storeId, role);
        if (!store.getCreatedBy().equals(email)) {
            throw new BusinessException(ErrorCode.STORE_PERMISSION_DENIED);
        }

        store.updateStore(requestDto.name(), requestDto.description(), requestDto.address(),
            requestDto.longitude(), requestDto.latitude(), requestDto.phoneNumber(),
            requestDto.isActive(), requestDto.minOrderAmount(), requestDto.deliveryFee(),
            requestDto.opensAt(), requestDto.closesAt());

        updateCategories(store, requestDto.categories());

        return StoreResponseDto.from(store);
    }

    @Override
    @Transactional
    public void deleteStore(UUID storeId, String email, UserRole role) {
        validateOwnerRole(role);
        Store store = getValidStore(storeId, role);
        if (!store.getCreatedBy().equals(email)) {
            throw new BusinessException(ErrorCode.STORE_PERMISSION_DENIED);
        }

        store.softDelete(email);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StoreResponseDto> getStores(
        String query, Pageable pageable, UserRole role
    ) {

        Pageable normalized = normalizedPageable(pageable);
        Page<Store> storePage = switch (role) {
            case USER -> getStoresForUser(query, normalized);
            case OWNER, MANAGER, MASTER -> getStoreForNonUser(query, normalized);
        };

        List<StoreResponseDto> data = storePage.getContent().stream()
            .map(StoreResponseDto::from)
            .toList();

        return PagedResponse.success(data, storePage.getNumber(), storePage.getSize(),
            storePage.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public StoreDetailResponseDto getStore(UUID storeId, UserRole role) {
        Store store = getValidStore(storeId, role);
        List<CategoryResponseDto> categoryResponseDtoList = categoryService.getCategoryDtoList(
            store);
        List<MenuResponseDto> menuResponseDtoList = getMenuDtoList(store);

        return StoreDetailResponseDto.from(store, categoryResponseDtoList, menuResponseDtoList);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<StoreResponseDto> getStoresByCategory(
        UUID categoryId, Pageable pageable, UserRole role
    ) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }

        Pageable normalized = normalizedPageable(pageable);
        Page<Store> storesByCategory = switch (role) {
            case USER -> storeRepository.findAllByIsActiveTrueAndStoreCategoriesCategoryId(
                categoryId, normalized);
            case OWNER, MANAGER, MASTER ->
                storeRepository.findAllByDeletedAtIsNullAndStoreCategoriesCategoryId(
                    categoryId, normalized);
        };

        List<StoreResponseDto> data = storesByCategory.getContent().stream()
            .map(StoreResponseDto::from).toList();

        return PagedResponse.success(data, storesByCategory.getNumber(), storesByCategory.getSize(),
            storesByCategory.hasNext());
    }

    @Override
    public Store findByUUID(UUID storeId) {
        return storeRepository.findById(storeId)
            .filter(s -> !s.isDeleted())
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    @Override
    public void validateExistenceById(UUID storeId) {
        if (!storeRepository.existsByIdAndIsActiveTrue(storeId)) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
    }


    @Override
    public void validateOwner(Long userId) {
        storeRepository.findByOwnerId(userId).orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }

    @Override
    @Transactional
    public Store updateRating(UUID storeId) {
        Store store = storeRepository.findByIdAndIsActiveTrue(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        RatingStats stats = reviewRepository.calculateRatingStats(storeId);
        store.updateRating(stats.count(), stats.average());
        return storeRepository.save(store);
    }

    public List<Store> findAllByIds(Set<UUID> storeIds) {
        return storeRepository.findAllByIdIn(storeIds);
    }

    private void updateCategories(Store store, Set<UUID> categories) {
        if (categories != null && !categories.isEmpty()) {
            store.getStoreCategories().clear();
            categories.forEach(categoryId -> {
                Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

                store.addCategory(category);
            });
        }
    }

    private Store getValidStore(UUID storeId, UserRole role) {
        if (role == UserRole.USER) {
            return storeRepository.findByIdAndIsActiveTrue(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        }

        return storeRepository.findByIdAndDeletedAtIsNull(storeId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private void validateOwnerRole(UserRole role) {
        if (role != UserRole.OWNER) {
            throw new BusinessException(ErrorCode.STORE_PERMISSION_DENIED);
        }
    }

    private List<MenuResponseDto> getMenuDtoList(Store store) {
        List<Menu> menuList = store.getMenus().stream().toList();
        List<MenuResponseDto> menuResponseDtoList = new ArrayList<>();
        for (Menu menu : menuList) {
            menuResponseDtoList.add(MenuResponseDto.from(menu));
        }

        return menuResponseDtoList;
    }

    private Page<Store> getStoresForUser(String query, Pageable pageable) {
        if (!StringUtils.hasText(query)) {
            return storeRepository.findAllByIsActiveTrue(pageable);
        }
        return storeRepository.findAllByNameContainingIgnoreCaseAndIsActiveTrue(query, pageable);
    }

    private Page<Store> getStoreForNonUser(String query, Pageable pageable) {
        if (!StringUtils.hasText(query)) {
            return storeRepository.findAllByDeletedAtIsNull(pageable);
        }
        return storeRepository.findAllByNameContainingIgnoreCaseAndDeletedAtIsNull(query, pageable);
    }

    private Pageable normalizedPageable(Pageable pageable) {
        int size = PERMITTED_PAGE_SIZES.contains(
            pageable.getPageSize()) ? pageable.getPageSize() : DEFAULT_PAGE_SIZE;

        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
