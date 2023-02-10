package com.vodafone.inventoryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vodafone.inventoryservice.config.Config;
import com.vodafone.inventoryservice.model.Inventory;
import com.vodafone.inventoryservice.model.Product;
import com.vodafone.inventoryservice.model.ProductsDTO;
import com.vodafone.inventoryservice.repository.InventoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.io.File;
import java.io.IOException;

import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(InventoryRepository inventoryRepository) {
		return args -> {
			ObjectMapper mapper = new ObjectMapper();
			ProductsDTO products = getProductsFromJsonFile(mapper);
			saveToInventory(inventoryRepository, products);
		};
	}

	private void saveToInventory(InventoryRepository inventoryRepository, ProductsDTO products) {
		products.products().stream()
				.map(this::createNewInventory)
				.forEach(inventoryRepository::save);
	}

	private Inventory createNewInventory(Product product) {
		Inventory inventory = new Inventory();
		inventory.setSkuCode(product.skuCode());
		inventory.setQuantity(product.quantity());
		return inventory;
	}

	private ProductsDTO getProductsFromJsonFile(ObjectMapper mapper) {
		ProductsDTO products = null;
		try {products = mapper.readValue(new File(Config.FILE), ProductsDTO.class);}
		catch (IOException e) {e.printStackTrace();}
		return products;
	}
}
