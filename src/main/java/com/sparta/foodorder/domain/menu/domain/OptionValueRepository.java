package com.sparta.foodorder.domain.menu.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OptionValueRepository {

    Optional<OptionValue> findByIdAndOptionIdAndDeletedAtIsNull(UUID optionValueId, UUID optionId);

    OptionValue save(OptionValue optionValue);

    List<OptionValue> saveAll(List<OptionValue> optionValues);

    List<OptionValue> findAllByOptionId(UUID optionId);

    List<OptionValue> findAllByOptionIdAndDeletedAtIsNull(UUID optionId);

    Optional<OptionValue> findById(UUID valueId);

}
