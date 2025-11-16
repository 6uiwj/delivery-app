package com.sparta.foodorder.domain.menu.infrastructure;

import com.sparta.foodorder.domain.menu.domain.Option;
import com.sparta.foodorder.domain.menu.domain.OptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class OptionRepositoryImpl implements OptionRepository {
    private final OptionJpaRepository optionJpaRepository;

    @Override
    public Option findByMenuId(UUID menuId) {
        return null;
    }

    @Override
    public List<Option> findAllByMenuId(UUID menuId) {
        return optionJpaRepository.findAllByMenuId(menuId);
    }

    @Override
    public Option save(Option option) {
        return optionJpaRepository.save(option);
    }

    @Override
    public Optional<Option> findById(UUID optionId) {
        return optionJpaRepository.findById(optionId);
    }

    @Override
    public Optional<Option> findByIdAndMenuIdAndDeletedAtIsNull(UUID optionId, UUID menuId) {
        return optionJpaRepository.findByIdAndMenu_IdAndDeletedAtIsNull(optionId, menuId);
    }

    @Override
    public boolean existsByIdAndMenuIdAndDeletedAtIsNull(UUID optionId, UUID menuId) {
        return optionJpaRepository.existsByIdAndMenu_idAndDeletedAtIsNull(optionId, menuId);
    }

    @Override
    public List<Option> findAllByMenuIdAndDeletedAtIsNull(UUID menuId) {
        return optionJpaRepository.findAllByMenuIdAndDeletedAtIsNull(menuId);
    }

    @Override
    public List<Option> saveAll(List<Option> optionList) {
        return optionJpaRepository.saveAll(optionList);
    }

}
