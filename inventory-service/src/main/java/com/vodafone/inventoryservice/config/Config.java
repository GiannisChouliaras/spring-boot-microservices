package com.vodafone.inventoryservice.config;

import lombok.Getter;

@Getter
public class Config {

    public static final String FILE ="/home/ioannisch/Documents/Microservices/microservices/inventory-service/src/main/resources/data/products.json";

    private Config() {
        throw new IllegalStateException("Config is a utility class");
    }

}
