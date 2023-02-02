package com.vodafone.inventoryservice.service;

import com.vodafone.inventoryservice.inventoryDTO.InventoryResponseDTO;
import com.vodafone.inventoryservice.model.Inventory;
import com.vodafone.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    /**
     * Do not check only if its empty. Need to pass extra argument in the request (not only skuCodes).
     * @param skuCode (List of skuCodes in the Order)
     * @return List of information for each skuCode in stock
     */
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> isInStock(List<String> skuCode, List<Integer> quantity) {
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponseDTO.builder()
                            .skuCode(inventory.getSkuCode())
                            .quantity(inventory.getQuantity())
                            .isInStock(checkQuantity(inventory, skuCode, quantity))
                            .build())
                .toList();
    }

    private Boolean checkQuantity(Inventory inventory, List<String> skuCode, List<Integer> quantity) {
        // TODO: write better code.
        for (int index = 0; index < skuCode.size(); index++)
            if (inventory.getSkuCode().equals(skuCode.get(index)) && inventory.getQuantity() >= quantity.get(index))
                return true;
        return false;
    }
}
