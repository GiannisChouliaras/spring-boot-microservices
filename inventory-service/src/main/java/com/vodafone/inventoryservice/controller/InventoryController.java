package com.vodafone.inventoryservice.controller;

import com.vodafone.inventoryservice.inventoryDTO.InventoryResponseDTO;
import com.vodafone.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponseDTO> isInStock(@RequestParam List<String> skuCode, @RequestParam List<Integer> quantity) {
        return inventoryService.isInStock(skuCode, quantity);
    }

}
