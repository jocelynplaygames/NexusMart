package com.example.NexusMart.controller;

import com.example.NexusMart.dto.ItemDTO;
import com.example.NexusMart.jwt.JwtTokenProvider;
import com.example.NexusMart.service.ItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping(path = "/api/item", produces = "application/json")
public class ItemController {

    private final ItemService itemService;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public ItemController(ItemService itemService, JwtTokenProvider jwtTokenProvider) {
        this.itemService = itemService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    // New endpoint to fetch items by seller ID
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<ItemDTO>> getItemsBySellerId(@PathVariable String sellerId) {
        List<ItemDTO> items = itemService.getItemsByUserId(sellerId);
        return ResponseEntity.ok(items);
    }

    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestPart("item") String itemJson,
                                              @RequestPart("imageFile") MultipartFile imageFile,
                                              @RequestPart(value = "subImageFiles", required = false) List<MultipartFile> subImageFiles,
                                              @RequestHeader("Authorization") String token) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ItemDTO itemDTO = objectMapper.readValue(itemJson, ItemDTO.class);

        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        itemDTO.setSellerId(userId);

        ItemDTO createdItem = itemService.createItem(itemDTO, imageFile, subImageFiles);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PutMapping(value = "/{id}/quantity", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateItemQuantity(@PathVariable Long id, @RequestBody ItemDTO itemDTO) {
        ItemDTO existingItem = itemService.getItem(id);
        existingItem.setQuantity(itemDTO.getQuantity());
        itemService.updateItemQuantity(existingItem);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ItemDTO> updateItem(@Valid @PathVariable Long id,
                                              @RequestPart("item") String itemJson,
                                              @RequestPart(value = "imageFile", required = false) MultipartFile imageFile,
                                              @RequestPart(value = "subImageFiles", required = false) List<MultipartFile> subImageFiles,
                                              @RequestHeader("Authorization") String token) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ItemDTO itemDTO = objectMapper.readValue(itemJson, ItemDTO.class);

        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        ItemDTO existingItem = itemService.getItem(id);

        if (!existingItem.getSellerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<String> subImageURLs = new ArrayList<>();
        if(itemDTO.getSubImageUrl1() != null && !itemDTO.getSubImageUrl1().isEmpty()){
            subImageURLs.add(itemDTO.getSubImageUrl1());
        }
        if(itemDTO.getSubImageUrl2() != null && !itemDTO.getSubImageUrl2().isEmpty()){
            subImageURLs.add(itemDTO.getSubImageUrl2());
        }
        if(itemDTO.getSubImageUrl3() != null && !itemDTO.getSubImageUrl3().isEmpty()){
            subImageURLs.add(itemDTO.getSubImageUrl3());
        }
        if(itemDTO.getSubImageUrl4() != null && !itemDTO.getSubImageUrl4().isEmpty()){
            subImageURLs.add(itemDTO.getSubImageUrl4());
        }

        log.info("ItemSubImagesDTO: " + subImageURLs.toString());
        ItemDTO updatedItem = itemService.updateItem(id, itemDTO, imageFile, subImageFiles, subImageURLs);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id, @RequestHeader("Authorization") String token) throws IOException {
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        ItemDTO existingItem = itemService.getItem(id);

        if (!existingItem.getSellerId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable Long id) {
        ItemDTO itemDTO = itemService.getItem(id);
        return ResponseEntity.ok(itemDTO);
    }

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems(
            @RequestParam(value = "userId", required = false) String userId) {
        List<ItemDTO> items;
        if (userId != null) {
            items = itemService.getItemsByUserId(userId);
        } else {
            items = itemService.getAllItems();
        }
        return ResponseEntity.ok(items);
    }

    @GetMapping("/trending-today")
    public ResponseEntity<List<ItemDTO>> getTrendingTodayItems() {
        List<ItemDTO> items = itemService.getRandomItems(8);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items-on-sale")
    public ResponseEntity<List<ItemDTO>> getItemsOnSale() {
        List<ItemDTO> items = itemService.getRandomItems(8);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/just-for-you")
    public ResponseEntity<List<ItemDTO>> getJustForYouItems() {
        List<ItemDTO> items = itemService.getRandomItems(8);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDTO>> searchItems(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "country", required = false) String country,
            @RequestParam(value = "city", required = false) String city,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "mainCategoryId", required = false) String mainCategoryId,
            @RequestParam(value = "subCategoryId", required = false) String subCategoryId) {

        log.info("Start querying!" + mainCategoryId + subCategoryId);
        mainCategoryId = mainCategoryId != null? ((mainCategoryId.equals("all")? null: mainCategoryId)) : null;
        subCategoryId = subCategoryId != null? ((subCategoryId.equals("all")? null: subCategoryId)) : null;
        long mainCategoryIdLong = -1;
        long subCategoryIdLong = -1;
        if(mainCategoryId != null){
            mainCategoryIdLong = Long.parseLong(mainCategoryId);
        }
        if(subCategoryId != null){
            subCategoryIdLong = Long.parseLong(subCategoryId);
        }

        List<ItemDTO> items = itemService.searchItems(query, country, city, minPrice, maxPrice, mainCategoryIdLong == -1? null: mainCategoryIdLong, subCategoryIdLong == -1? null: subCategoryIdLong);
        return ResponseEntity.ok(items);
    }
}




