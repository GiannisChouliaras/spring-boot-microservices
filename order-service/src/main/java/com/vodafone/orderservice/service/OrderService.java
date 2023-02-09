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

    private static final String INVENTORY_GET_REQUEST = "http://inventory-service/api/inventory";
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;


    /**
     * Synchronous request using WebClient. (bodyToMono and .block())
     * bodyToMono(): Decode the body to the given target type.
     * block(): make it Synchronous.
     *
     * [!!!!] Order could contain a bunch of products. So we do not call multiple requests per product,
     * we just pass a list
     */
    public void placeOrder(OrderRequest orderRequest) {
        Order order = constructOrder(orderRequest);
        List<String> skuCodes = getSkuCodesFromOrder(order);
        List<Integer> quantity = getQuantityFromOrder(order);
        InventoryResponseDTO[] response = getResponsesArrayFromInventory(skuCodes, quantity);

        if (Boolean.TRUE.equals(productsDontMatch(response))) throw new IllegalArgumentException("Not in Stock.");
        orderRepository.save(order);
    }

    private List<Integer> getQuantityFromOrder(Order order) {
        return order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getQuantity)
                .toList();
    }

    private Boolean productsDontMatch(InventoryResponseDTO[] response) {
        return !Arrays.stream(response).allMatch(InventoryResponseDTO::isInStock);
    }

    private List<String> getSkuCodesFromOrder(Order order) {
        return order.getOrderLineItemsList().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();
    }

    private InventoryResponseDTO[] getResponsesArrayFromInventory(List<String> skuCodes, List<Integer> quantity) {
        try {
            return webClientBuilder.build().get()
                    .uri(INVENTORY_GET_REQUEST,
                            uriBuilder -> uriBuilder
                                    .queryParam("skuCode", skuCodes)
                                    .queryParam("quantity", quantity)
                                    .build())
                    .retrieve()
                    .bodyToMono(InventoryResponseDTO[].class)
                    .block();
        } catch (NullPointerException e) {
            throw new NullPointerException("Response returned null");
        }
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
