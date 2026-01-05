package com.sparta.foodorder.domain.menu.infrastructure;

import com.sparta.foodorder.domain.menu.domain.Menu;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuJpaRepository extends JpaRepository<Menu, UUID> {
    List<Menu> findByStoreId(UUID storeId);
  
    List<Menu> findByStoreIdAndDeletedAtIsNull(UUID storeId);
  
    List<Menu> findByStoreIdAndActiveTrueAndHiddenFalse(UUID storeId);


    @Query("SELECT DISTINCT m FROM Menu m " +
        "LEFT JOIN FETCH m.options o " +
        "LEFT JOIN FETCH o.optionValues " +
        "WHERE m.store.id = :storeId " +
        "AND m.active = true " +
        "AND m.hidden = false " +
        "AND m.deletedAt IS NULL")
    List<Menu> findByStoreIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(@Param("storeId")UUID storeId);
  
    Optional<Menu> findByIdAndDeletedAtIsNull(UUID menuId);
  
    Page<Menu> findByNameContaining(String searchString, Pageable pageable);

    Optional<Menu> findByIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(UUID menuId);
}
