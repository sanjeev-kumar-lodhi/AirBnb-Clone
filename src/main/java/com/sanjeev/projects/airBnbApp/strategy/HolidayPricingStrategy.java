package com.sanjeev.projects.airBnbApp.strategy;

import com.sanjeev.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
@RequiredArgsConstructor
public class HolidayPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculateInventory(Inventory inventory) {
        BigDecimal price = wrapped.calculateInventory(inventory);

        Boolean isTodayHoliday = true;

        if(isTodayHoliday){
            price = price.multiply(BigDecimal.valueOf(1.25));
        }

        return price;
    }
}
