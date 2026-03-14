package com.sparta.foodorder.domain.order.application;

import com.sparta.foodorder.domain.menu.application.MenuService;
import com.sparta.foodorder.domain.menu.domain.Menu;
import com.sparta.foodorder.domain.menu.domain.Option;
import com.sparta.foodorder.domain.menu.domain.OptionValue;
import com.sparta.foodorder.domain.order.domain.*;
import com.sparta.foodorder.domain.order.event.OrderEvent;
import com.sparta.foodorder.domain.order.presentation.dto.CreateOrderRequestDto;
import com.sparta.foodorder.domain.order.presentation.dto.GetOrderResponseDto;
import com.sparta.foodorder.domain.order.presentation.dto.GetStoreOrdersResponseDto;
import com.sparta.foodorder.domain.order.presentation.dto.GetUserOrdersResponseDto;
import com.sparta.foodorder.domain.payment.domain.Payment;
import com.sparta.foodorder.domain.payment.domain.PaymentService;
import com.sparta.foodorder.domain.store.domain.Store;
import com.sparta.foodorder.domain.store.domain.StoreService;
import com.sparta.foodorder.domain.user.domain.User;
import com.sparta.foodorder.domain.user.domain.UserService;
import com.sparta.foodorder.global.dto.PagedResponse;
import com.sparta.foodorder.global.exception.BusinessException;
import com.sparta.foodorder.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final PaymentService paymentService;
    private final StoreService storeService;
    private final MenuService menuService;
    private final UserService userService;
    private final ApplicationEventPublisher applicationEventPublisher;

    private Order getOrder(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));
    }

    @Transactional
    public UUID createOrder(CreateOrderRequestDto dto, Long userId) {
        storeService.validateExistenceById(dto.storeId());
        Set<Menu> menus = menuService.findAllById(dto.getMenuIds());
        Map<UUID, Menu> menuMap = menus.stream().collect(Collectors.toMap(Menu::getId, m -> m));

        // Order 저장
        Order order = new Order(userId, dto.storeId(), dto.addressLine(), dto.detailAddress(), 0, dto.memo());
        UUID orderId = orderRepository.save(order);

        int totalPrice = 0;

        // OrderMenu 생성
        for (CreateOrderRequestDto.MenuInfo menuInfo : dto.menus()) {
            Menu menu = menuMap.get(menuInfo.menuId());
            OrderMenu orderMenu = new OrderMenu(order, menu.getId(), menuInfo.quantity(), menu.getName(), 0);

            int menuTotalPrice = menu.getPrice() * menuInfo.quantity();

            // 옵션 처리
            for (CreateOrderRequestDto.OptionInfo optionInfo : menuInfo.options()) {
                Option option = menu.getOption(optionInfo.optionId());
                OrderMenuOption orderMenuOption = new OrderMenuOption(orderMenu, option.getName(), 0);

                int optionTotalPrice = 0;
                for (UUID valueId : optionInfo.optionValueIds()) {
                    OptionValue value = option.getOptionValue(valueId);
                    OrderMenuOptionValue orderMenuOptionValue = new OrderMenuOptionValue(orderMenuOption, value.getValue(), value.getAddPrice());
                    orderMenuOption.addValue(orderMenuOptionValue);
                    optionTotalPrice += value.getAddPrice();
                }

                orderMenuOption.setPrice(optionTotalPrice);
                orderMenu.addOption(orderMenuOption);
                menuTotalPrice += optionTotalPrice;
            }

            orderMenu.updateTotalPrice(menuTotalPrice);
            orderMenuRepository.save(orderMenu);
            totalPrice += menuTotalPrice;
        }

        order.updateTotalPrice(totalPrice);
        orderRepository.save(order);

        return orderId;
    }

    @Transactional
    public void cancelOrder(UUID orderId, Long userId) {
        Order order = getOrder(orderId);
        order.validateOrderWriter(userId);
        order.cancel();

        // 이벤트 전송 (orderId 받으면 환불 처리)
        Optional<Payment> optionalPayment = paymentService.findByOrderId(orderId);
        if (optionalPayment.isPresent()) {
            Payment payment = optionalPayment.get();
            OrderEvent.OrderCanceled refunded = new OrderEvent.OrderCanceled(
                    orderId,
                    payment.getId(),
                    "사용자 주문 취소"
            );
            applicationEventPublisher.publishEvent(refunded);
            log.info("주문 취소 이벤트 발행 - orderId : {}", orderId);
        }
    }

    @Transactional
    public void acceptOrder(UUID orderId, Long userId) {
        Order order = getOrder(orderId);
        Store store = storeService.findByUUID(order.getStoreId());
        store.validateOwner(userId);
        order.accept();
    }

    @Transactional
    public void rejectOrder(UUID orderId, Long userId) {
        Order order = getOrder(orderId);
        Store store = storeService.findByUUID(order.getStoreId());
        store.validateOwner(userId);
        order.reject();
    }

    @Transactional
    public void readyOrder(UUID orderId, Long userId) {
        Order order = getOrder(orderId);
        Store store = storeService.findByUUID(order.getStoreId());
        store.validateOwner(userId);
        order.ready();
    }

    @Transactional
    public void deliverOrder(UUID orderId, Long userId) {
        Order order = getOrder(orderId);
        Store store = storeService.findByUUID(order.getStoreId());
        store.validateOwner(userId);
        order.deliver();
    }

    @Transactional
    public void completeOrder(UUID orderId, Long userId) {
        Order order = getOrder(orderId);
        order.validateOrderWriter(userId);
        order.complete();
    }

    public PagedResponse<GetUserOrdersResponseDto> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = (Pageable) PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAllByUserId(userId, pageable);

        Set<UUID> storeIds = orderPage.getContent().stream()
                .map(Order::getStoreId)
                .collect(Collectors.toSet());
        List<Store> stores = storeService.findAllByIds(storeIds);
        Map<UUID, Store> storeMap = stores.stream()
                .collect(Collectors.toMap(Store::getId, s -> s));

        List<GetUserOrdersResponseDto> dtos = orderPage.getContent().stream()
                .map(order -> {
                    Store store = storeMap.get(order.getStoreId());
                    List<OrderMenu> orderMenus = orderMenuRepository.findAllByOrder(order);
                    return GetUserOrdersResponseDto.from(order, orderMenus, store);
                })
                .toList();
        return PagedResponse.success(dtos, orderPage.getNumber() + 1, orderPage.getSize(), orderPage.hasNext());
    }

    public PagedResponse<GetStoreOrdersResponseDto> getStoreOrders(UUID storeId, Long userId, int page, int size) {
        Store store = storeService.findByUUID(storeId);
        store.validateOwner(userId);
        Pageable pageable = (Pageable) PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findAllByStoreId(storeId, pageable);

        Set<Long> userIds = orderPage.getContent().stream()
                .map(Order::getUserId)
                .collect(Collectors.toSet());
        List<User> users = userService.findAllByIdIn(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<GetStoreOrdersResponseDto> dtos = orderPage.getContent().stream()
                .map(order -> {
                    User user = userMap.get(order.getUserId());
                    List<OrderMenu> orderMenus = orderMenuRepository.findAllByOrder(order);
                    return GetStoreOrdersResponseDto.from(order, orderMenus, user);
                })
                .toList();
        return PagedResponse.success(dtos, orderPage.getNumber() + 1, orderPage.getSize(), orderPage.hasNext());
    }

    public GetOrderResponseDto getOrder(UUID orderId, Long userId, String nickname) {
        Order order = getOrder(orderId);
        order.validateOrderWriter(userId);
        List<OrderMenu> orderMenus = orderMenuRepository.findAllByOrder(order);
        return GetOrderResponseDto.from(order, orderMenus, nickname);
    }
}
