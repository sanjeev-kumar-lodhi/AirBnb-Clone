package com.sanjeev.projects.airBnbApp.strategy;

import com.sanjeev.projects.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculateInventory(Inventory inventory);
}
