package com.sanjeev.projects.airBnbApp.strategy;

import com.sanjeev.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy{

    private final PricingStrategy wrapped;

    @Override
    public BigDecimal calculateInventory(Inventory inventory) {

        BigDecimal price = wrapped.calculateInventory(inventory);

        double occupancyRate = (double) inventory.getBookedCount()/inventory.getTotalCount();
        if(occupancyRate>0.8){
            price = price.multiply(BigDecimal.valueOf(1.2));
        }
        return price;
    }
}
