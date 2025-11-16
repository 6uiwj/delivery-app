package com.sparta.foodorder.domain.menu.infrastructure;

import com.sparta.foodorder.domain.menu.domain.OptionValue;
import com.sparta.foodorder.domain.menu.domain.OptionValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OptionValueRepositoryImpl implements OptionValueRepository {
    private final OptionValueJpaRepository optionValueJpaRepository;

    @Override
    public Optional<OptionValue> findByIdAndOptionIdAndDeletedAtIsNull(
            UUID optionValueId,
            UUID optionId
    ) {
        return optionValueJpaRepository.findByIdAndOption_IdAndDeletedAtIsNull(optionValueId, optionId);
    }

    @Override
    public OptionValue save(OptionValue optionValue) {
        return optionValueJpaRepository.save(optionValue);
    }

    @Override
    public List<OptionValue> saveAll(List<OptionValue> optionValues) {
        return optionValueJpaRepository.saveAll(optionValues);
    }

    @Override
    public List<OptionValue> findAllByOptionId(UUID optionId) {
        return optionValueJpaRepository.findAllByOptionId(optionId);
    }

    @Override
    public List<OptionValue> findAllByOptionIdAndDeletedAtIsNull(UUID optionId) {
        return optionValueJpaRepository.findAllByOptionIdAndDeletedAtIsNull(optionId);
    }

    @Override
    public Optional<OptionValue> findById(UUID valueId) {
        return optionValueJpaRepository.findById(valueId);
    }
}
