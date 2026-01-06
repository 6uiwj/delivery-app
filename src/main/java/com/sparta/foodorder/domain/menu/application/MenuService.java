package com.sparta.foodorder.domain.menu.application;

import com.sparta.foodorder.domain.auth.infrastructure.CustomUserDetails;
import com.sparta.foodorder.domain.menu.domain.Menu;
import com.sparta.foodorder.domain.menu.domain.MenuRepository;
import com.sparta.foodorder.domain.menu.domain.Option;
import com.sparta.foodorder.domain.menu.domain.OptionValue;
import com.sparta.foodorder.domain.menu.presentation.dto.request.MenuCreateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.request.MenuUpdateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.request.OptionCreateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.request.OptionUpdateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.request.OptionValueCreateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.request.OptionValueUpdateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.response.MenuResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.response.MenuSearchResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.response.OptionResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.response.OptionValueResponseDto;
import com.sparta.foodorder.domain.store.domain.Store;
import com.sparta.foodorder.domain.store.domain.StoreService;
import com.sparta.foodorder.domain.user.domain.UserRole;
import com.sparta.foodorder.global.dto.PagedResponse;
import com.sparta.foodorder.global.exception.BusinessException;
import com.sparta.foodorder.global.exception.ErrorCode;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final StoreService storeService;

    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public MenuResponseDto insertMenu(MenuCreateRequestDto requestDto, CustomUserDetails userDetails, UUID storeId) {
        Long userId = userDetails.getUserId();

        //manager,master, 해당 가게owner일 경우만 생성
        boolean isAdmin = isAdmin(userDetails); //관리자인지 검증
        Store store = storeService.findByUUID(storeId); //가게 존재여부 검증

        //생성 권한 확인
        if(!isAdmin && !isOwner(store, userId)) {
            log.info("생성권한 없음(가게오너, MANAGER, MASTER가 아님)");
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 메뉴 생성
        Menu menu = requestDto.toEntity(store);
        Menu savedMenu = menuRepository.save(menu);
        return MenuResponseDto.from(savedMenu);

    }

    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public MenuResponseDto createOption(OptionCreateRequestDto requestDto, UUID menuId, CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        Store store = menu.getStore();

        //manager,master, 해당 가게owner일 경우만 생성
        boolean isAdmin = isAdmin(userDetails); //관리자인지 검증

        //생성 권한 확인
        if(!isAdmin && !isOwner(store, userId)) {
            log.info("생성권한 없음(가게오너, MANAGER, MASTER가 아님)");
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        getValidStore(menu);
        return saveOption(menu, requestDto);
    }

    @CacheEvict(value = "menus", allEntries = true)
    public OptionValueResponseDto createOptionValue(OptionValueCreateRequestDto optionValueCreateRequestDto,UUID menuId, UUID optionId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        Store store = menu.getStore();

        //manager,master, 해당 가게owner일 경우만 생성
        boolean isAdmin = isAdmin(userDetails); //관리자인지 검증

        //생성 권한 확인
        if(!isAdmin && !isOwner(store, userId)) {
            log.info("생성권한 없음(가게오너, MANAGER, MASTER가 아님");
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

            OptionValue optionValue = menu.addOptionValue(
                optionId,
                optionValueCreateRequestDto.getValue(),
                optionValueCreateRequestDto.getAddPrice(),
                optionValueCreateRequestDto.getDescription()
            );

            menuRepository.save(menu);

            return OptionValueResponseDto.from(optionValue);
    }


    @Cacheable(
        value = "menus",
        key = "#storeId + ':' + #userDetails.role + ':' + #userDetails.userId",
        unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public List<MenuResponseDto> getMenus(CustomUserDetails userDetails, UUID storeId) {
        Long userId = userDetails.getUserId();
        UserRole userRole = userDetails.getRole();
        Store store = storeService.findByUUID(storeId);
        List<Menu> menu;

        if(userRole == UserRole.MANAGER||userRole == UserRole.MASTER) {
            log.info("관리자 메뉴조회");
            menu = menuRepository.findByStoreId(storeId);

        } else if(userRole == UserRole.USER || !store.getOwnerId().equals(userId)) {
            log.info("User와 다른가게 사장 조회");
            if (!store.getIsActive() || store.isDeleted()) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }
            menu = menuRepository.findByStoreIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(storeId);
        } else {
            log.info("Owner 조회 ");
            if (store.isDeleted()) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }
            menu = menuRepository.findByStoreIdAndDeletedAtIsNull(storeId);
        }

        if(menu.isEmpty()) {
            log.info("조회 데이터가 없음");
            return Collections.emptyList();
        }

        return menu.stream().map(MenuResponseDto::from).toList();
    }


    @Transactional(readOnly = true)
    public MenuResponseDto getMenuForUser(UUID menuId, UUID storeId) {
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        getValidStore(menu);

        //메뉴가 활성화 상태인지 확인
        if (!menu.isActive() || menu.isHidden() || menu.isDeleted()) {
            log.info("메뉴가 삭제되거나 비활성상태임");
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }

        return MenuResponseDto.from(menu);
    }


    @Transactional(readOnly = true)
    public MenuResponseDto getMenuForOwner(UUID menuId, CustomUserDetails userDetails, UUID storeId) {
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        Long userId = userDetails.getUserId();
        Store store = menu.getStore();

        boolean isAdmin = isAdmin(userDetails);

        if (store.isDeleted()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        if(!isAdmin && !isOwner(store, userId)) {
            log.info("가게오너, MANAGER, MASTER가 아님");
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        return MenuResponseDto.from(menu);


    }


    public List<OptionResponseDto> getOptions(UUID menuId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        //메뉴 존재 검증
        Menu menu = validateMenu(menuId);
        Store checkStore = menu.getStore();

        boolean admin = isAdmin(userDetails);
        boolean owner = isOwner(checkStore, userId);

        List<Option> optionList = admin||owner
                ? menu.getOptions()
                : menu.getActiveOptions();

        return optionList.stream().map(OptionResponseDto::from).toList();
    }


    public List<OptionValueResponseDto> getOptionValues(UUID menuId, UUID optionId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        // 1. 메뉴 존재 여부
        Menu menu = validateMenu(menuId);
        Option option = menu.getOption(optionId);

        Store checkStore = menu.getStore();
        getValidStore(menu);

        // 2. 권한 확인
        boolean owner = isOwner(checkStore, userId);
        boolean admin = isAdmin(userDetails);

        List<OptionValue> optionValueList = owner||admin ?
            option.getOptionValues() : option.getActiveOptionValues();

        return optionValueList.stream().map(OptionValueResponseDto::from).toList();

    }

    @CacheEvict(value = "menus", key = "#storeId + '*'")
    public MenuResponseDto updateMenu(UUID menuId, @Valid MenuUpdateRequestDto requestDto, CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        Menu menu = validateMenu(menuId);
        Store checkStore = menu.getStore();
        boolean owner = isOwner(checkStore, userId);
        boolean admin = isAdmin(userDetails);

        if(!owner && !admin) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if(admin && requestDto.getStoreId() == null) {
            throw new BusinessException(ErrorCode.MISSING_INPUT_VALUE);
        }

            menu.changeMenu(
                    requestDto.getName(),
                    requestDto.getDescription(),
                    requestDto.getPrice(),
                    requestDto.getHidden(),
                    requestDto.getActive()
            );
            Menu savedMenu = menuRepository.save(menu);
            return MenuResponseDto.from(savedMenu);
    }

    @CacheEvict(value = "menus", allEntries = true)
    public void deleteMenu(UUID menuId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        Menu menu = getMenuForAdmin(menuId);
        Store checkStore = menu.getStore();
        boolean owner = isOwner(checkStore, userId);
        boolean master = isAdmin(userDetails);

        if(!owner && !master) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        String username = userDetails.getUsername();


        menu.deleteMenu(username);
        menuRepository.save(menu);
    }

    //메뉴가 주문 가능한 상태인지(활성화되어있는지 체크)
    @Transactional(readOnly = true)
    public Menu OrderableMenu(UUID menuId) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        if (!menu.isActive() || menu.isHidden() || menu.isDeleted()) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        return menu;
    }


    public PagedResponse<MenuSearchResponseDto> searchMenus(String searchString, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Menu> menuPage = menuRepository.findByNameContaining(searchString, pageable);
        log.info("menuPage 데이터 존재? : {} ", menuPage.toString());
        List<MenuSearchResponseDto> menuSearchResponseDtoList = menuPage.getContent().stream()
                .filter(menu -> !menu.isDeleted())
                .filter(menu -> menu.isActive())
                .filter(menu -> !menu.isHidden())
                .map(MenuSearchResponseDto::fromEntity)
                .toList();

        boolean hasNext = menuPage.hasNext();
        return PagedResponse.success(menuSearchResponseDtoList, page, size, hasNext);
    }


    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public OptionResponseDto updateOption(
            OptionUpdateRequestDto requestDto,
            UUID menuId, UUID optionId, Long userId
    ) {
        Menu menu = getMenuForAdmin(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);

        Option option = menu.getOption(optionId);
        option.updateOption(requestDto.getOptionName());

        menuRepository.save(menu);
        return OptionResponseDto.from(option);
    }


    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public void deleteOption(UUID menuId, UUID optionId, Long userId, String email) {
        Menu menu = getMenuForAdmin(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);

        Option option = menu.getOption(optionId);
        option.delete(email);

    }

    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public OptionValueResponseDto updateOptionValue(
            OptionValueUpdateRequestDto requestDto,
            UUID menuId, UUID optionId, UUID optionValueId, Long userId
    ) {
        Menu menu = getMenuForAdmin(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);

        Option option = menu.getOption(optionId);
        OptionValue optionValue = option.getOptionValue(optionValueId);
        optionValue.updateOptionValue(
                requestDto.getValue(),
            requestDto.getDescription(),
            requestDto.getAddPrice());

        menuRepository.save(menu);
        return OptionValueResponseDto.from(optionValue);
    }

    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public void deleteOptionValue(
            UUID menuId, UUID optionId, UUID optionValueId, Long userId,
            String email
    ) {
        Menu menu = getMenuForAdmin(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);
        Option option = menu.getOption(optionId);
        OptionValue optionValue = option.getOptionValue(optionValueId);
        optionValue.delete(email);

        menuRepository.save(menu);

    }

    //=============================================================================
    // Util method

    public List<Menu> findAllById(List<UUID> menuIds) {
        return menuRepository.findAllById(menuIds);
    }

    private Menu getMenuForAdmin(UUID menuId) {
        return menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private Menu getMenuForUser(UUID menuId) {
        return menuRepository.findByIdAndActiveTrueAndHiddenFalseAndDeletedAtIsNull(menuId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private Menu validateMenu(UUID menuId) {
        return menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private Store getValidStore(Menu menu) {
        Store store = menu.getStore();
        if (store.isDeleted() || !store.getIsActive()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        return store;
    }


    private void validateOwner(Store store, Long userId) {
        if (!Objects.equals(store.getOwnerId(), userId)) {
            throw new BusinessException(ErrorCode.STORE_PERMISSION_DENIED);
        }
    }

    private boolean isOwner(Store store, Long userId) {
        return Objects.equals(store.getOwnerId(), userId);
    }


    public boolean isAdmin(CustomUserDetails userDetails) {
        UserRole userRole = userDetails.getRole();
        return userRole == UserRole.MASTER || userRole == UserRole.MANAGER;
    }

    private MenuResponseDto saveOption(Menu menu, OptionCreateRequestDto requestDto) {
        Option option = requestDto.toEntity();
        menu.addOption(option);
        menuRepository.saveAndFlush(menu);
        return MenuResponseDto.from(menu);
    }

}
