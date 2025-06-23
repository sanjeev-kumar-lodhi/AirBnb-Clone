package com.sanjeev.projects.airBnbApp.cotroller;

import com.sanjeev.projects.airBnbApp.dto.HotelInfoDto;
import com.sanjeev.projects.airBnbApp.dto.HotelPriceDto;
import com.sanjeev.projects.airBnbApp.dto.HotelSearchRequest;
import com.sanjeev.projects.airBnbApp.service.HotelService;
import com.sanjeev.projects.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/hotels")
public class HotelBrowseController {

    private final InventoryService inventoryService;
    private final HotelService hotelService;

    @GetMapping("/search")
    public ResponseEntity<Page<HotelPriceDto>> searchHotels(@RequestBody HotelSearchRequest hotelSearchRequest){
        var  page = inventoryService.searchHotels(hotelSearchRequest);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{hotelId}/info")
    public ResponseEntity<HotelInfoDto> getHotelInfo(@PathVariable Long hotelId){
        return ResponseEntity.ok(hotelService.getHotelInfoById(hotelId));
    }
}
