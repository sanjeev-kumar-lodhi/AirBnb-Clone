package com.sanjeev.projects.airBnbApp.cotroller;

import com.sanjeev.projects.airBnbApp.dto.InventoryDto;
import com.sanjeev.projects.airBnbApp.dto.UpdateInventoryRequestDto;
import com.sanjeev.projects.airBnbApp.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<InventoryDto>> getAllInventoryByRoom(@PathVariable Long roomId){
        return ResponseEntity.ok(inventoryService.getAllInventoryByRoom(roomId));
    }

    @PutMapping("/rooom/{roomId}")
    public ResponseEntity<Void> updateInventory(@PathVariable Long roomId,
                                                @RequestBody UpdateInventoryRequestDto updateInventoryRequestDto){
             inventoryService.updateInventory(roomId,updateInventoryRequestDto);
             return ResponseEntity.noContent().build();
    }
}
