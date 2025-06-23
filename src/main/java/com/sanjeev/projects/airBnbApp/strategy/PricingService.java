package com.sanjeev.projects.airBnbApp.strategy;


import com.sanjeev.projects.airBnbApp.entity.Inventory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {

    public BigDecimal dynamicPricingStrategy(Inventory inventory){

        PricingStrategy pricingStrategy = new BasePricingStrategy();

        // apply the additional strategy
        pricingStrategy = new SurgePricingStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPricingStrategy(pricingStrategy);
        pricingStrategy = new HolidayPricingStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPricingStrategy(pricingStrategy);

        return pricingStrategy.calculateInventory(inventory);

    }

    public BigDecimal calculateTotalPrice(List<Inventory> inventoryList){

        return inventoryList.stream()
                .map(this::dynamicPricingStrategy)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }
}
