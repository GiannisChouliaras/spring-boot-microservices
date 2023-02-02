package com.vodafone.orderservice.service;

import com.vodafone.orderservice.model.Order;
import com.vodafone.orderservice.model.OrderLineItems;
import com.vodafone.orderservice.orderDTO.InventoryResponseDTO;
import com.vodafone.orderservice.orderDTO.OrderLineItemsDTO;
import com.vodafone.orderservice.orderDTO.OrderRequest;
import com.vodafone.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private static final String INVENTORY_GET_REQUEST = "http://localhost:3002/api/inventory";
    private final OrderRepository orderRepository;
    private final WebClient webClient;


    /**
     * Synchronous request using WebClient. (bodyToMono and .block())
     * bodyToMono(): Decode the body to the given target type.
     * block(): make it Synchronous.
     *
     * [!!!!] Order could contain a bunch of products. So we do not call multiple requests per product,
     * we just pass a list
     *
     * @param orderRequest
     *
     */
    public void placeOrder(OrderRequest orderRequest) {
        Order order = constructOrder(orderRequest);
        List<String> skuCodes = getSkuCodesFromOrder(order);
        InventoryResponseDTO[] response = getResponsesArrayFromInventory(skuCodes);
        assert response != null && response.length == 0 : "response cannot be null or empty";
        if (Boolean.TRUE.equals(productsDontMatch(response))) throw new IllegalArgumentException("Not in Stock.");
        orderRepository.save(order);
    }

    private Boolean productsDontMatch(InventoryResponseDTO[] response) {
        return !Arrays.stream(response).allMatch(InventoryResponseDTO::isInStock);
    }

    private static List<String> getSkuCodesFromOrder(Order order) {
        return order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
    }

    private InventoryResponseDTO[] getResponsesArrayFromInventory(List<String> skuCodes) {
        return webClient.get()
                .uri(INVENTORY_GET_REQUEST,
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponseDTO[].class)
                .block();
    }
    private Order constructOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDTOList()
                 .stream()
                 .map(this::mapToDTO)
                 .toList();
        order.setOrderLineItemsList(orderLineItems);
        return order;
    }

    private OrderLineItems mapToDTO(OrderLineItemsDTO orderLineItemsDTO) {
       OrderLineItems orderLineItems = new OrderLineItems();
       orderLineItems.setPrice(orderLineItemsDTO.getPrice());
       orderLineItems.setQuantity(orderLineItemsDTO.getQuantity());
       orderLineItems.setSkuCode(orderLineItemsDTO.getSkuCode());
       return orderLineItems;
    }
}
