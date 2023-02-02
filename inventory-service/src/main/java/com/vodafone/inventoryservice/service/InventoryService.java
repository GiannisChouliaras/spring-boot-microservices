package com.vodafone.inventoryservice.service;

import com.vodafone.inventoryservice.inventoryDTO.InventoryResponseDTO;
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
     * TODO: Add Business logic to check if the desired amount of products exists in inventory.
     * Do not check only if its empty. Need to pass extra argument in the request (not only skuCodes).
     * @param skuCode (List of skuCodes in the Order)
     * @return List of informations for each skuCode in stock
     */
    @Transactional(readOnly = true)
    public List<InventoryResponseDTO> isInStock(List<String> skuCode) {
        return inventoryRepository.findBySkuCodeIn(skuCode).stream()
                .map(inventory ->
                    InventoryResponseDTO.builder()
                            .skuCode(inventory.getSkuCode())
                            .isInStock(inventory.getQuantity() > 0)
                            .build()).toList();
    }
}
