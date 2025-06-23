package com.sanjeev.projects.airBnbApp.strategy;

import com.sanjeev.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy{
    private final PricingStrategy wrapped;
    @Override
    public BigDecimal calculateInventory(Inventory inventory) {
        BigDecimal price = wrapped.calculateInventory(inventory);
        return price.multiply(inventory.getSurgeFactor());
    }
}
