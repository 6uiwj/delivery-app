package com.sparta.foodorder.domain.menu.infrastructure;

import com.sparta.foodorder.domain.menu.domain.Menu;
import com.sparta.foodorder.domain.menu.domain.MenuRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@RequiredArgsConstructor
@Repository
public class MenuRepositoryImpl implements MenuRepository {

    private final MenuJpaRepository menuJpaRepository;

    @Override
    public Set<Menu> findByStoreId(UUID storeId) {
        return menuJpaRepository.findByStoreId(storeId);
    }

    @Override
    public Optional<Menu> findById(UUID id) {
        return menuJpaRepository.findById(id);
    }

    @Override
    public Menu save(Menu menu) {
        return menuJpaRepository.save(menu);
    }

    @Override
    public void saveAndFlush(Menu menu) {
        menuJpaRepository.saveAndFlush(menu);
    }

    @Override
    public void delete(Menu menu) {
        menuJpaRepository.delete(menu);
    }

    @Override
    public Set<Menu> findByStoreIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(@Param("storeId")UUID storeId) {
        return menuJpaRepository.findByStoreIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(
                storeId);
    }

    @Override
    public Set<Menu> findByStoreIdAndDeletedAtIsNull(UUID storeId) {
        return menuJpaRepository.findByStoreIdAndDeletedAtIsNull(storeId);
    }


    @Override
    public Set<Menu> findAllById(List<UUID> menuIds) {
        return menuJpaRepository.findAllByIdIn(menuIds);
    }

    @Override
    public Page<Menu> findByNameContaining(String searchString, Pageable pageable) {
        return menuJpaRepository.findByNameContaining(searchString, pageable);
    }

    public Optional<Menu> findByIdAndDeletedAtIsNull(UUID menuId) {
        return menuJpaRepository.findByIdAndDeletedAtIsNull(menuId);
    }

    @Override
    public Optional<Menu> findByIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(UUID menuId) {
        return menuJpaRepository.findByIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(menuId);
    }

    @Override
    public Optional<Menu> findByIdWithOptions(UUID menuId) {
        return menuJpaRepository.findByIdWithOptions(menuId);
    }


}
