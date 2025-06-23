package com.sanjeev.projects.airBnbApp.service;

import com.sanjeev.projects.airBnbApp.entity.Hotel;
import com.sanjeev.projects.airBnbApp.entity.HotelMinPrice;
import com.sanjeev.projects.airBnbApp.entity.Inventory;
import com.sanjeev.projects.airBnbApp.repository.HotelMinPriceRepository;
import com.sanjeev.projects.airBnbApp.repository.HotelRepository;
import com.sanjeev.projects.airBnbApp.repository.InventoryRepository;
import com.sanjeev.projects.airBnbApp.strategy.PricingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PricingUpdateService {

    // scheduler update the inventory and hotelMinPrice tables every hour
    private final HotelRepository hotelRepository;
    private final InventoryRepository inventoryRepository;
    private final HotelMinPriceRepository hotelMinPriceRepository;
    private final PricingService pricingService;


//    @Scheduled(cron = "*/5 * * * * *  ")
@Scheduled(cron = "0 0 * * * *  ")
    public void updatePrices(){
        int page=0;
        int batchSize=100;
        while(true){
            Page<Hotel> hotelPage = hotelRepository.findAll(PageRequest.of(page,batchSize));
            if(hotelPage.isEmpty()){
                break;
            }
            hotelPage.getContent().forEach(this::updateHotelPrice);
            page++;
        }
    }

    private void updateHotelPrice(Hotel hotel){

        log.info("Updating hotel prices for hotel Id : {}",hotel.getId());
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = LocalDate.now().plusYears(1);
        List<Inventory> inventoryList = inventoryRepository.findByHotelAndDateBetween(hotel,startDate,endDate);
        updateInventoryPrices(inventoryList);
        updateHotelMinPrice(hotel,inventoryList,startDate,endDate);
    }

    private void updateHotelMinPrice(Hotel hotel, List<Inventory> inventoryList,LocalDate startDate, LocalDate endDate) {

        // compute minimum price per day for the hotel
        Map<LocalDate,BigDecimal> dailyMinPrice = inventoryList.stream()
                .collect(Collectors.groupingBy(
                        Inventory::getDate,
                        Collectors.mapping(Inventory::getPrice,
                                Collectors.minBy(Comparator.naturalOrder()))
                ))
                .entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e-> e.getValue().orElse(BigDecimal.ZERO)));
        // prepare hotel price entities in bulk
        List<HotelMinPrice> hotelPrices = new ArrayList<>();
        dailyMinPrice.forEach((date,price)->{
                    HotelMinPrice hotelPrice = hotelMinPriceRepository.findByHotelAndDate(hotel,date)
                            .orElse(new HotelMinPrice(hotel,date));
            hotelPrice.setPrice(price);
            hotelPrices.add(hotelPrice);
                }
        );
        hotelMinPriceRepository.saveAll(hotelPrices);
    }

    private void updateInventoryPrices(List<Inventory> inventoryList){
            inventoryList.forEach(inventory ->{
                BigDecimal dynamicPrice = pricingService.dynamicPricingStrategy(inventory);
                inventory.setPrice(dynamicPrice);
            });
            inventoryRepository.saveAll(inventoryList);
    }
}
