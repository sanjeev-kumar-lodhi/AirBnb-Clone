package com.sanjeev.projects.airBnbApp.strategy;

import com.sanjeev.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@RequiredArgsConstructor
public class UrgencyPricingStrategy implements  PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculateInventory(Inventory inventory) {
        BigDecimal price = wrapped.calculateInventory(inventory);

        LocalDate today = LocalDate.now();
        if(!inventory.getDate().isBefore(today) && inventory.getDate().isBefore(today.plusDays(7))){
            price = price.multiply(BigDecimal.valueOf(1.15));
        }
        return price;
    }
}
