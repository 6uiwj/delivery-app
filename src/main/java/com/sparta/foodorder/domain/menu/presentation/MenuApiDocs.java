package com.sparta.foodorder.domain.menu.presentation;

import com.sparta.foodorder.domain.auth.infrastructure.CustomUserDetails;
import com.sparta.foodorder.domain.menu.presentation.dto.response.OptionResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.request.OptionUpdateRequestDto;
import com.sparta.foodorder.domain.menu.presentation.dto.response.OptionValueResponseDto;
import com.sparta.foodorder.domain.menu.presentation.dto.request.OptionValueUpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "메뉴", description = "메뉴 API")
public interface MenuApiDocs {




    @Operation(summary = "옵션 수정", description = "가게 주인이 특정 메뉴에 대한 옵션을 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                        {
                                            "id": "b62fb884-ce0a-49e3-ab98-c93726639506",
                                            "optionName": "매운육수",
                                            "menuId": "6a3d326e-21f2-4806-8f3e-64137dc1c79c"
                                            "optionValues": [
                                                {
                                                   "id": "83f943fb-61e2-4128-9dec-fc856bb9b15e",
                                                   "value": "1단계",
                                                   "description": "보통",
                                                   "addPrice": 0
                                                },
                                                {
                                                   "id": "aeb6c00e-2752-4569-bd06-05be2230b464",
                                                   "value": "2단계",
                                                   "description": "매움",
                                                   "addPrice": 0
                                                }
                                            ]
                                        }
                                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                                {
                                                    "status": 403,
                                                    "code": "S005",
                                                    "message": "가게에 대한 접근권한이 없습니다.",
                                                    "timestamp": "2025-10-16T18:01:06.5396248"
                                                }
                                            """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M001",
                                                "message": "존재하지 않는 메뉴입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "S001",
                                                "message": "가게를 찾을 수 없습니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M002",
                                                "message": "존재하지 않는 옵션입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        )
    })
    public ResponseEntity<OptionResponseDto> updateOption(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @RequestBody OptionUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    );





    @Operation(summary = "옵션 삭제", description = "가게 주인이 특정 메뉴에 대한 옵션을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200"
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                                {
                                                    "status": 403,
                                                    "code": "S005",
                                                    "message": "가게에 대한 접근권한이 없습니다.",
                                                    "timestamp": "2025-10-16T18:01:06.5396248"
                                                }
                                            """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M001",
                                                "message": "존재하지 않는 메뉴입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "S001",
                                                "message": "가게를 찾을 수 없습니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M002",
                                                "message": "존재하지 않는 옵션입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        )
    })
    public ResponseEntity<Void> deleteOption(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    );






    @Operation(summary = "옵션값 수정", description = "가게 주인이 특정 옵션에 대한 옵션값을 수정합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                        {
                                            "id": "83f943fb-61e2-4128-9dec-fc856bb9b15e",
                                            "value": "1단계",
                                            "description": "보통",
                                            "addPrice": 500
                                        }
                                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                                {
                                                    "status": 403,
                                                    "code": "S005",
                                                    "message": "가게에 대한 접근권한이 없습니다.",
                                                    "timestamp": "2025-10-16T18:01:06.5396248"
                                                }
                                            """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M001",
                                                "message": "존재하지 않는 메뉴입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "S001",
                                                "message": "가게를 찾을 수 없습니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M002",
                                                "message": "존재하지 않는 옵션입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M003",
                                                "message": "존재하지 않는 옵션값입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        )
    })
    public ResponseEntity<OptionValueResponseDto> updateOptionValue(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @PathVariable UUID valueId,
        @RequestBody OptionValueUpdateRequestDto requestDto,
        @AuthenticationPrincipal CustomUserDetails userDetails
    );






    @Operation(summary = "옵션값 삭제", description = "가게 주인이 특정 옵션에 대한 옵션값을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(
            responseCode = "200"
        ),
        @ApiResponse(
            responseCode = "403",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                                {
                                                    "status": 403,
                                                    "code": "S005",
                                                    "message": "가게에 대한 접근권한이 없습니다.",
                                                    "timestamp": "2025-10-16T18:01:06.5396248"
                                                }
                                            """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M001",
                                                "message": "존재하지 않는 메뉴입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "S001",
                                                "message": "가게를 찾을 수 없습니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M002",
                                                "message": "존재하지 않는 옵션입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                                            {
                                                "status": 404,
                                                "code": "M003",
                                                "message": "존재하지 않는 옵션값입니다.",
                                                "timestamp": "2025-10-16T18:01:06.5396248"
                                            }
                                        """)
            )
        )
    })
    public ResponseEntity<Void> deleteOptionValue(
        @PathVariable UUID menuId,
        @PathVariable UUID optionId,
        @PathVariable UUID valueId,
        @AuthenticationPrincipal CustomUserDetails userDetails
    );
}
