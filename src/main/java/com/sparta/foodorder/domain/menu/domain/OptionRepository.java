package com.sparta.foodorder.domain.menu.domain;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptionRepository {

    Option findByMenuId(UUID menuId);

    List<Option> findAllByMenuId(UUID menuId);

    Option save(Option option);

    Optional<Option> findById(UUID id);

    Optional<Option> findByIdAndMenuIdAndDeletedAtIsNull(UUID optionId, UUID menuId);

    boolean existsByIdAndMenuIdAndDeletedAtIsNull(UUID optionId, UUID menuId);

    List<Option> findAllByMenuIdAndDeletedAtIsNull(UUID menuId);

    List<Option> saveAll(List<Option> optionList);
}
