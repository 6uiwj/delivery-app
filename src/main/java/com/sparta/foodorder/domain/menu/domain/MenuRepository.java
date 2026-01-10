package com.sparta.foodorder.domain.menu.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface MenuRepository {

    List<Menu> findByStoreId(UUID storeId);

    Optional<Menu> findById(UUID id);

    Menu save(Menu menu);

    void saveAndFlush(Menu menu);

    void delete(Menu menu);

    //메뉴 전체 조회(일반 유저용)
    List<Menu> findByStoreIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(@Param("storeId")UUID storeId);

    //메뉴 전체 조회(가게 주인용)
    List<Menu> findByStoreIdAndDeletedAtIsNull(UUID storeId);

    List<Menu> findAllById(List<UUID> menuIds);

    Page<Menu> findByNameContaining(String searchString, Pageable pageable);

    Optional<Menu> findByIdAndDeletedAtIsNull(UUID menuId);

    Optional<Menu> findByIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(UUID menuId);

    Optional<Menu> findByIdWithOptions(UUID menuId);

}
