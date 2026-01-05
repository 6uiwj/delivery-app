package com.sparta.foodorder.domain.menu.presentation;


import com.sparta.foodorder.domain.auth.infrastructure.CustomUserDetails;
import com.sparta.foodorder.domain.menu.application.MenuService;
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
import com.sparta.foodorder.domain.user.domain.UserRole;
import com.sparta.foodorder.global.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "메뉴", description = "메뉴 API")
@RequestMapping("/v1/menus")
@RestController
@RequiredArgsConstructor
@Slf4j
public class MenuController implements MenuApiDocs {

    private final MenuService menuService;

    //메뉴 생성시 관리자는 요청에 storeId를 명시해야함!!
    @Operation(summary = "메뉴 생성", description = "새로운 메뉴를 생성합니다.")
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<MenuResponseDto> createMenu(@RequestParam UUID storeId,
                                                      @RequestBody @Valid MenuCreateRequestDto requestDto,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {

        MenuResponseDto responseDto = menuService.insertMenu(requestDto, userDetails, storeId);

        return ResponseEntity.ok(responseDto);

    }

    @Operation(summary = "옵션 생성", description = "이미 존재하는 메뉴의 새로운 옵션을 생성합니다.")
    @PostMapping("/{menuId}/options")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    public ResponseEntity<MenuResponseDto> createOption(@PathVariable UUID menuId,
        @RequestBody @Valid OptionCreateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails) {

        MenuResponseDto menuResponseDto = menuService.createOption(requestDto, menuId, userDetails);
        return ResponseEntity.ok(menuResponseDto);

    }


    //옵션 value 생성
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    @PostMapping("/{menuId}/options/{optionId}/values")
    public ResponseEntity<OptionValueResponseDto> createOptionValue(@PathVariable UUID menuId,
                                                                    @PathVariable UUID optionId,
                                                                    @RequestBody OptionValueCreateRequestDto optionValueCreateRequestDto,
                                                                    @AuthenticationPrincipal CustomUserDetails userDetails) {
        OptionValueResponseDto optionValueResponseDto = menuService.createOptionValue(optionValueCreateRequestDto, menuId, optionId, userDetails);
        return ResponseEntity.ok(optionValueResponseDto);
    }


    @Operation(summary = "전체 메뉴 조회", description = "해당 가게의 모든 메뉴를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<MenuResponseDto>> getAllMenus(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                             @RequestParam UUID storeId) {
        List<MenuResponseDto> menuList = menuService.getMenus(userDetails, storeId);
        return ResponseEntity.ok(menuList);
    }



    @Operation(summary = "단일 메뉴 조회", description = "해당 가게의 특정 메뉴를 조회합니다.")
    @GetMapping("/{menuId}")
    public ResponseEntity<MenuResponseDto> getMenu(@PathVariable UUID menuId,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestParam UUID storeId) {
        UserRole userRole = userDetails.getRole();

        if(userRole == UserRole.USER) {
            log.info("메뉴 단일 조회 - 유저");
            MenuResponseDto responseDto = menuService.getMenuForUser(menuId,storeId);
            return ResponseEntity.ok(responseDto);

        } else {
            log.info("메뉴 단일 조회 - 사장");
            MenuResponseDto responseDto = menuService.getMenuForOwner(menuId, userDetails, storeId); //표현계층에 서비스가 의존. menuId만 반환후 조회해서 가져오는 것이 좋음
            return ResponseEntity.ok(responseDto);
        }
    }


    @GetMapping("/{menuId}/options")
    public ResponseEntity<List<OptionResponseDto>> getOptions(@PathVariable UUID menuId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info(" 컨트롤러 유입 {}", menuId);
        List<OptionResponseDto> optionResponseDtoList = menuService.getOptions(menuId, userDetails);

        return ResponseEntity.ok(optionResponseDtoList);
    }

    //TODO: 단일 option에 대한 value들 조회
    @GetMapping("/{menuId}/options/{optionId}")
    public ResponseEntity<List<OptionValueResponseDto>> getOptionValues(@PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<OptionValueResponseDto> optionValueResponseDtoList = menuService.getOptionValues(
            menuId, optionId, userDetails);
        return ResponseEntity.ok(optionValueResponseDtoList);
    }


    //메뉴 수정시 관리자는 요청에 storeId를 명시해야함!!
    @Operation(summary = "메뉴 수정", description = "해당 가게의 특정 메뉴를 수정합니다.")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    @PutMapping("/{menuId}")
    public ResponseEntity<MenuResponseDto> updateMenu(@PathVariable UUID menuId,
                                                      @RequestBody @Valid MenuUpdateRequestDto requestDto,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        MenuResponseDto responseDto = menuService.updateMenu(menuId, requestDto, userDetails);

        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "메뉴 삭제", description = "해당 가게의 특정 메뉴를 삭제합니다.")
    @PreAuthorize("hasAnyRole('OWNER','MANAGER','MASTER')")
    @DeleteMapping("/{menuId}")
    public ResponseEntity<Void>  deleteMenu(@PathVariable UUID menuId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        menuService.deleteMenu(menuId, userDetails);
        return ResponseEntity.ok().build();
    }


    @GetMapping("/search")
    public ResponseEntity<PagedResponse<MenuSearchResponseDto>> searchMenus(
            @RequestParam String searchString,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if( size != 10 && size != 30 && size != 50) {
            size = 10;
        }
        PagedResponse<MenuSearchResponseDto> response = menuService.searchMenus(searchString, page, size);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{menuId}/options/{optionId}")
    public ResponseEntity<OptionResponseDto> updateOption(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @RequestBody OptionUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        OptionResponseDto responseDto = menuService.updateOption(requestDto, menuId, optionId,
            userId);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{menuId}/options/{optionId}")
    public ResponseEntity<Void> deleteOption(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        String email = userDetails.getUserEmail();
        menuService.deleteOption(menuId, optionId, userId, email);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{menuId}/options/{optionId}/values/{valueId}")
    public ResponseEntity<OptionValueResponseDto> updateOptionValue(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @PathVariable UUID valueId,
        @RequestBody OptionValueUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        OptionValueResponseDto responseDto =
            menuService.updateOptionValue(requestDto, menuId, optionId, valueId, userId);

        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{menuId}/options/{optionId}/values/{valueId}")
    public ResponseEntity<Void> deleteOptionValue(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @PathVariable UUID valueId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = userDetails.getUserId();
        String email = userDetails.getUserEmail();
        menuService.deleteOptionValue(menuId, optionId, valueId, userId, email);

        return ResponseEntity.ok().build();
    }

}
