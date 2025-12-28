package com.sparta.foodorder.domain.menu.application;

import com.sparta.foodorder.domain.auth.infrastructure.CustomUserDetails;
import com.sparta.foodorder.domain.menu.domain.Menu;
import com.sparta.foodorder.domain.menu.domain.MenuRepository;
import com.sparta.foodorder.domain.menu.domain.Option;
import com.sparta.foodorder.domain.menu.domain.OptionRepository;
import com.sparta.foodorder.domain.menu.domain.OptionValue;
import com.sparta.foodorder.domain.menu.domain.OptionValueRepository;
import com.sparta.foodorder.domain.menu.presentation.dto.MenuCreateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.MenuResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.MenuSearchResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.MenuUpdateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.OptionCreateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.OptionResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.OptionUpdateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.OptionValueCreateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.OptionValueResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.OptionValueUpdateRequestDto;
import com.sparta.foodorder.domain.store.domain.Store;
import com.sparta.foodorder.domain.store.domain.StoreRepository;
import com.sparta.foodorder.domain.user.domain.UserRole;
import com.sparta.foodorder.global.dto.PagedResponse;
import com.sparta.foodorder.global.exception.BusinessException;
import com.sparta.foodorder.global.exception.ErrorCode;
import jakarta.validation.Valid;
import java.util.ArrayList;
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
    private final OptionRepository optionRepository;
    private final OptionValueRepository optionValueRepository;
    private final StoreRepository storeRepository;

    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public MenuResponseDto insertMenu(MenuCreateRequestDto requestDto, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        boolean isOwner = storeRepository.findByOwnerId(userId).isPresent();
        boolean isMasterOrManager = checkAdminAuthorization(userDetails);

        //#1 . 생성 권한이 있나 확인
        if(!isOwner && ! isMasterOrManager) {
            log.info("생성권한 없음(가게오너, MANAGER, MASTER가 아님");
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        Store store;

        if(isOwner) {
            log.info("가게 사장의 메뉴 생성 요청");
            //#2. 오너면 가게 주인이 맞나 확인
            store = storeRepository.findByOwnerId(userId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_PERMISSION_DENIED));

            if (store.isDeleted()) {
                log.info("삭제된 가게에 대한 요청");
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }
            Menu menu = requestDto.toEntity(store);
            Menu savedMenu = menuRepository.save(menu);
            return MenuResponseDto.from(savedMenu);
        }
        log.info("관리자의 메뉴 생성 요청");
        //#3. 관리자면 storeId를 가져왔나 확인
        if (requestDto.getStoreId() == null) {
            log.info("요청시 storeId 데이터 누락");
            throw new BusinessException(ErrorCode.MISSING_INPUT_VALUE);
        }
        //#4. store가 존재하나 확인
        store = storeRepository.findById(requestDto.getStoreId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if (store.isDeleted()) {
            log.info("삭제된 가게");
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        // 4️⃣ 메뉴 생성
        Menu menu = requestDto.toEntity(store);
        Menu savedMenu = menuRepository.save(menu);
        return MenuResponseDto.from(savedMenu);

    }


    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public MenuResponseDto createOption(OptionCreateRequestDto requestDto, UUID menuId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        boolean isOwner = storeRepository.findByOwnerId(userId).isPresent();
        boolean isMasterOrManager = checkAdminAuthorization(userDetails);

        //#1 . 생성 권한이 있나 확인
        if(!isOwner && ! isMasterOrManager) {
            log.info("옵션 생성 권한 없음(가게 OWNER, MANAGER, MASTER가 아님");
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        Store store;
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        if(isOwner) {
            log.info("가게 OWNER의 옵션 생성 요청");
            //#2. 오너면 가게 주인이 맞나 확인
            store = storeRepository.findByOwnerId(userId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_PERMISSION_DENIED));

            if(store.isDeleted()) {
                log.info("삭제된 가게에 대한 요청");
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }


            if(!menu.getStore().getId().equals(store.getId())) {
                log.info("해당 가게에 해당 메뉴가 존재하지 않음 ");
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }

            return saveOption(menu, requestDto);
        }

        log.info("관리자의 옵셔 생성");
        UUID storeId = menu.getStore().getId();
        store =  storeRepository.findById(storeId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        if(store.isDeleted()) {
            log.info("삭제된 가게");
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        return saveOption(menu, requestDto);
    }

    @CacheEvict(value = "menus", allEntries = true)
    public OptionValueResponseDto createOptionValue(OptionValueCreateRequestDto optionValueCreateRequestDto,UUID menuId, UUID optionId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        boolean isOwner = storeRepository.findByOwnerId(userId).isPresent();
        boolean isMasterOrManager = checkAdminAuthorization(userDetails);

        //#1 . 생성 권한이 있나 확인
        if(!isOwner && ! isMasterOrManager) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        Store store;
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        Option option = optionRepository.findById(optionId).orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));

        if(isOwner) {
            //#2. 오너면 가게 주인이 맞나 확인
            store = storeRepository.findByOwnerId(userId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

            if (store.isDeleted()) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }
            if (!menu.getStore().getId().equals(store.getId())) {
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
            OptionValue optionValue = optionValueCreateRequestDto.toEntity();

            return OptionValueResponseDto.from(optionValueRepository.save(optionValue));
        }
        UUID storeId = menu.getStore().getId();
        store =  storeRepository.findById(storeId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        if(store.isDeleted()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        OptionValue optionValue = optionValueCreateRequestDto.toEntity();

        return OptionValueResponseDto.from(optionValueRepository.save(optionValue));


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
        Store store = storeRepository.findById(storeId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

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

        Store store =   storeRepository.findById(storeId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        if (!store.getIsActive() || store.isDeleted()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));

        //해당 가게의 메뉴가 맞는지 확인
        if (!menu.getStore().getId().equals(storeId)) {
            log.info("해당 메뉴가 가게에 존재하지 않음");
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
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
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        if (store.isDeleted()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }


        if (!menu.getStore().getId().equals(storeId)) {
            log.info("해당 메뉴가 가게에 존재하지 않음 ");
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        boolean isOwner = store.getOwnerId().equals(userId);
        // ADMIN/Manager 여부 확인
        boolean isAdminOrManager = checkAdminAuthorization(userDetails);

        if(!isOwner && !isAdminOrManager) {
            log.info("가게주인이나 관리자가 아닙니다.");
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if(isOwner && menu.isDeleted()) {
            log.info("삭제된 메뉴");
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        return MenuResponseDto.from(menu);


    }


    public List<OptionResponseDto> getOptions(UUID menuId, CustomUserDetails userDetails) {


        //메뉴 존재 검증
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        //가게 활성화 여부
        Store checkStore = menu.getStore();
        if (!checkStore.getIsActive() || checkStore.isDeleted()) {
            log.info("삭제되었거나 활성화되지 않은 가게");
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        // 2. 권한 확인
        Long userId = userDetails.getUserId();
        boolean isOwner = storeRepository.findByOwnerId(userId)
                .map(store -> store.getId().equals(menu.getStore().getId()))
                .orElse(false);
        boolean isMasterOrManager = checkAdminAuthorization(userDetails);

        // 4. 일반 사용자 및 오너 검증
        if (!isMasterOrManager) {
            log.info("USER/OWNER의 조회요청");
            // 메뉴 삭제된 경우 접근 불가
            if (menu.isDeleted()) {
                log.info("삭제된 메뉴의 옵션에 접근 시도함");
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
            // 일반 사용자ㄷ 또는 다른가게 사장은 활성화/숨김 상태도 체크
            if (!isOwner && (!menu.isActive() || menu.isHidden())) {
                log.info("일반사용자혹은 다른가게 주인이 활성화되지 않은 메뉴에 접근시도함");
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
        }
        List<Option> optionList = isMasterOrManager
                ? optionRepository.findAllByMenuId(menuId)
                : optionRepository.findAllByMenuIdAndDeletedAtIsNull(menuId);

        return optionList.stream().map(OptionResponseDto::from).toList();
    }


    public List<OptionValueResponseDto> getOptionValues(UUID menuId, UUID optionId, CustomUserDetails userDetails) {
        // 1. 메뉴 존재 여부
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));


        Store checkStore = menu.getStore();
        if (!checkStore.getIsActive() || checkStore.isDeleted()) {
            log.info("가게가 삭제되거나 비활성화상태");
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }

        // 2. 권한 확인
        Long userId = userDetails.getUserId();
        boolean isOwner = storeRepository.findByOwnerId(userId)
                .map(store -> store.getId().equals(menu.getStore().getId()))
                .orElse(false);
        boolean isMasterOrManager = checkAdminAuthorization(userDetails);


        // 4. 일반 사용자 및 오너 검증
        if (!isMasterOrManager) {
            // 메뉴 삭제된 경우 접근 불가
            if (menu.isDeleted()) {
                log.info("삭제된 메뉴");
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
            // 일반 사용자는 활성화/숨김 상태도 체크
            if (!isOwner && (!menu.isActive() || menu.isHidden())) {
                log.info("일반 사용자나 다른가게 OWNER는 비활성화 메뉴에 접근 못함");
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }
        }

        // 5. 옵션 조회 (OWNER와 일반 사용자는 deleted 제외, MASTER/Manager는 모두 조회)
        List<OptionValue> optionValueList;
        if (isMasterOrManager) {
            log.info("관리자의 옵션 조회 - 모든 옵션");
            optionValueList = optionValueRepository.findAllByOptionId(optionId);
        } else {
            log.info("가게 사장의 옵션조회 - 삭제되지 않은 옵션만");
            optionValueList = optionValueRepository.findAllByOptionIdAndDeletedAtIsNull(optionId);
        }
        return optionValueList.stream().map(OptionValueResponseDto::from).toList();

    }

    @CacheEvict(value = "menus", key = "#storeId + '*'")
    public MenuResponseDto updateMenu(UUID menuId, @Valid MenuUpdateRequestDto requestDto, CustomUserDetails userDetails) {

        Long userId = userDetails.getUserId();
        boolean isOwner = storeRepository.findByOwnerId(userId).isPresent();
        boolean isMasterOrManager = checkAdminAuthorization(userDetails);

        //메뉴 존재 검증
        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        if(menu.isDeleted()) throw new BusinessException(ErrorCode.MENU_NOT_FOUND);

        if(!isOwner && !isMasterOrManager) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Store store;

        if(isOwner) {
            if (menu.isDeleted()) throw new BusinessException(ErrorCode.MENU_NOT_FOUND);

            //가게 존재 및 소유자 검증
            store = storeRepository.findByOwnerId(userId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

            if (store.isDeleted()) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }

            //메뉴가 해당 가게 아래에 있는 메뉴가 맞는지 검증
            if (menu.getStore().getId().equals(store.getId())) {
                log.info("해당 가게에 존재하는 메뉴인지 ");
                menu.changeMenu(
                        requestDto.getName(),
                        requestDto.getDescription(),
                        requestDto.getPrice(),
                        requestDto.isHidden(),
                        requestDto.isActive()
                );
                Menu savedMenu = menuRepository.save(menu); //엔티티변경시 더티체킹이 일어나서 save필요없음 (명시적으로 보여줄거면 해도된다)

                MenuResponseDto responseDto = MenuResponseDto.from(savedMenu);


                return responseDto;
            } else throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }
        if(requestDto.getStoreId() == null) {
            throw new BusinessException(ErrorCode.MISSING_INPUT_VALUE);
        }

        store = storeRepository.findById(requestDto.getStoreId()).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        if(store.isDeleted()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        if (menu.getStore().getId().equals(store.getId())) {
            log.info("해당 가게에 존재하는 메뉴인지 ");
            menu.changeMenu(
                    requestDto.getName(),
                    requestDto.getDescription(),
                    requestDto.getPrice(),
                    requestDto.isHidden(),
                    requestDto.isActive()
            );
            Menu savedMenu = menuRepository.save(menu);
            MenuResponseDto responseDto = MenuResponseDto.from(savedMenu);
            return responseDto;

        } else throw new BusinessException(ErrorCode.MENU_NOT_FOUND);

    }

    @CacheEvict(value = "menus", allEntries = true)
    public void deleteMenu(UUID menuId, CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();
        boolean isOwner = storeRepository.findByOwnerId(userId).isPresent();
        boolean isMasterOrManager = checkAdminAuthorization(userDetails);

        if(!isOwner && ! isMasterOrManager) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        Menu menu = menuRepository.findById(menuId).orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
        if(menu.isDeleted()) throw new BusinessException(ErrorCode.MENU_NOT_FOUND);


        String username = userDetails.getUsername();
        if(isOwner) {
            //가게 주인이 맞는지 확인
            Store store = storeRepository.findByOwnerId(userId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
            if(store.isDeleted()) {
                throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
            }
            if(!menu.getStore().getId().equals(store.getId())) {
                throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
            }

            //menuId로 메뉴에 대한 옵션들을 검색하고, 그 옵션들에 대한옵션값들을 검색해서 논리삭제후, 옵션들 논리삭제 -> 메뉴 논리삭제
            //옵션 id 가져오기
            List<UUID> optionIdList = optionRepository.findAllByMenuId(menuId).stream().map(Option::getId)
                    .toList();

            //옵션 값 논리삭제
            List<OptionValue> optionValues = new ArrayList<>();

            for(UUID optionId : optionIdList) {
                List<OptionValue> values = optionValueRepository.findAllByOptionId(optionId);
                optionValues.addAll(values);
            }
            optionValues.forEach(optionValue -> optionValue.delete(username));
            optionValueRepository.saveAll(optionValues);

            //옵션 논리 삭제
            List<Option> optionList = optionRepository.findAllByMenuId(menuId);
            optionList.forEach(option -> option.delete(username));
            optionRepository.saveAll(optionList);

            //메뉴 논리 삭제
            menu.deleteMenu(username);
            menuRepository.save(menu);
        }

        UUID storeId = menu.getStore().getId();
        Store store =  storeRepository.findById(storeId).orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
        //해당 가게의 메뉴자 맞는지 확인
        if( !menu.getStore().getId().equals(storeId)) {
            throw new BusinessException(ErrorCode.MENU_NOT_FOUND);
        }

        if(store.isDeleted()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
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

    public List<Menu> findAllById(List<UUID> menuIds) {
        return menuRepository.findAllById(menuIds);
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
        Menu menu = getValidMenu(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);

        Option option = getValidOptionOfMenu(optionId, menuId);
        option.updateOption(requestDto.getOptionName());

        return OptionResponseDto.from(option);
    }


    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public void deleteOption(UUID menuId, UUID optionId, Long userId, String email) {
        Menu menu = getValidMenu(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);

        Option option = getValidOptionOfMenu(optionId, menuId);
        option.delete(email);
    }

    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public OptionValueResponseDto updateOptionValue(
            OptionValueUpdateRequestDto requestDto,
            UUID menuId, UUID optionId, UUID optionValueId, Long userId
    ) {
        Menu menu = getValidMenu(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);
        validateOptionInMenu(optionId, menuId);

        OptionValue optionValue = getValidOptionValueOfOption(optionValueId, optionId);
        optionValue.updateOptionValue(
                requestDto.getValue(), requestDto.getDescription(), requestDto.getAddPrice());

        return OptionValueResponseDto.from(optionValue);
    }

    @CacheEvict(value = "menus", allEntries = true)
    @Transactional
    public void deleteOptionValue(
            UUID menuId, UUID optionId, UUID optionValueId, Long userId,
            String email
    ) {
        Menu menu = getValidMenu(menuId);
        Store store = getValidStore(menu);
        validateOwner(store, userId);
        validateOptionInMenu(optionId, menuId);

        OptionValue optionValue = getValidOptionValueOfOption(optionValueId, optionId);
        optionValue.delete(email);
    }

    private Menu getValidMenu(UUID menuId) {
        return menuRepository.findByIdAndDeletedAtIsNull(menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_NOT_FOUND));
    }

    private Store getValidStore(Menu menu) {
        Store store = menu.getStore();
        if (store.isDeleted()) {
            throw new BusinessException(ErrorCode.STORE_NOT_FOUND);
        }
        return store;
    }

    private Option getValidOptionOfMenu(UUID optionId, UUID menuId) {
        return optionRepository.findByIdAndMenuIdAndDeletedAtIsNull(optionId, menuId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_NOT_FOUND));
    }

    private OptionValue getValidOptionValueOfOption(UUID optionValueId, UUID optionId) {
        return optionValueRepository.findByIdAndOptionIdAndDeletedAtIsNull(optionValueId, optionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.OPTION_VALUE_NOT_FOUND));
    }

    private void validateOptionInMenu(UUID optionId, UUID menuId) {
        if (!optionRepository.existsByIdAndMenuIdAndDeletedAtIsNull(optionId, menuId)) {
            throw new BusinessException(ErrorCode.OPTION_NOT_FOUND);
        }
    }

    private void validateOwner(Store store, Long userId) {
        if (!Objects.equals(store.getOwnerId(), userId)) {
            throw new BusinessException(ErrorCode.STORE_PERMISSION_DENIED);
        }
    }

    public boolean checkAdminAuthorization(CustomUserDetails userDetails) {
        UserRole userRole = userDetails.getRole();
        return userRole == UserRole.MASTER || userRole == UserRole.MANAGER;
    }

    private MenuResponseDto saveOption(Menu menu, OptionCreateRequestDto requestDto) {
        Option option = requestDto.toEntity();
        menu.getOptions().add(option);
        menuRepository.saveAndFlush(menu);
        return MenuResponseDto.from(menu);
    }

}
