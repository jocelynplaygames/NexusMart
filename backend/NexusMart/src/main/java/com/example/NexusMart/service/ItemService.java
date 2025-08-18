// File: backend/NexusMart/src/main/java/com/example/NexusMart/service/ItemService.java
package com.example.NexusMart.service;

import com.example.NexusMart.dto.ItemDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.ItemMapper;
import com.example.NexusMart.model.*;
import com.example.NexusMart.repository.*;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final RatingRepository ratingRepository;
    private final CloudinaryService cloudinaryService;

    private final OrderItemRepository orderItemRepository;
    private final FeedbackRepository feedbackRepository;

    @Value("${cloudinary.item-upload-folder}")
    private String imageFolder;

    @Autowired
    public ItemService(ItemRepository itemRepository,
                       UserRepository userRepository,
                       MainCategoryRepository mainCategoryRepository,
                       SubCategoryRepository subCategoryRepository,
                       RatingRepository ratingRepository,
                       CloudinaryService cloudinaryService,
                       OrderItemRepository orderItemRepository,
                       FeedbackRepository feedbackRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.mainCategoryRepository = mainCategoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.ratingRepository = ratingRepository;
        this.cloudinaryService = cloudinaryService;
        this.orderItemRepository = orderItemRepository;
        this.feedbackRepository = feedbackRepository;
    }

    public List<ItemDTO> getItemsByUserId(String userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new RuntimeException("The item can't be found!")
        );
        List<Item> items = itemRepository.findBySellerIdAndDeletedFalse(user.getId());
        return items.stream().map(ItemMapper::toDTO).collect(Collectors.toList());
    }

    public List<ItemDTO> getRandomItems(int count) {
        List<Item> items = itemRepository.findRandomItems(count);
        return items.stream().map(ItemMapper::toDTO).collect(Collectors.toList());
    }

    private void uploadSubImages(Item existingItem, List<MultipartFile> subImageFiles, List<String> subImageFileURLs) throws IOException {
        // Clear existing sub-image URLs
        existingItem.setSubImageUrl1(null);
        existingItem.setSubImageUrl2(null);
        existingItem.setSubImageUrl3(null);
        existingItem.setSubImageUrl4(null);
        int index = 0;
        for(String s: subImageFileURLs){
            switch (index) {
                case 0:
                    existingItem.setSubImageUrl1(s);
                    break;
                case 1:
                    existingItem.setSubImageUrl2(s);
                    break;
                case 2:
                    existingItem.setSubImageUrl3(s);
                    break;
                case 3:
                    existingItem.setSubImageUrl4(s);
                    break;
            }
            index++;
        }

        if(subImageFiles != null){
            for(MultipartFile o: subImageFiles){
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(o.getBytes(), imageFolder);
                String s = (String) uploadResult.get("url");
                switch (index) {
                    case 0:
                        existingItem.setSubImageUrl1(s);
                        break;
                    case 1:
                        existingItem.setSubImageUrl2(s);
                        break;
                    case 2:
                        existingItem.setSubImageUrl3(s);
                        break;
                    case 3:
                        existingItem.setSubImageUrl4(s);
                        break;
                }
                index++;
            }
        }
    }

    public ItemDTO createItem(@Valid ItemDTO itemDTO, MultipartFile imageFile, List<MultipartFile> subImageFiles) throws IOException {
        log.info("saving new listing!");
        User seller = userRepository.findById(itemDTO.getSellerId()).orElseThrow(
                () -> new ResourceNotFoundException("User", "id", itemDTO.getSellerId())
        );
        MainCategory mainCategory = mainCategoryRepository.findById(itemDTO.getMainCategoryId()).orElseThrow(
                () -> new ResourceNotFoundException("MainCategory", "id", itemDTO.getMainCategoryId().toString())
        );
        SubCategory subCategory = subCategoryRepository.findById(itemDTO.getSubCategoryId()).orElseThrow(
                () -> new ResourceNotFoundException("SubCategory", "id", itemDTO.getSubCategoryId().toString())
        );
        Rating rating = itemDTO.getRatingId() != null ?
                ratingRepository.findById(itemDTO.getRatingId()).orElse(null) : null;

        if(itemDTO.getQuantity() <= 0){
            throw new RuntimeException("Quantity should be greater than 0!");
        }
        if(itemDTO.getPrice() <= 0){
            throw new RuntimeException("Price should be greater than 0!");
        }

        Item item = ItemMapper.toEntity(itemDTO, seller, mainCategory, subCategory, rating);

        if (imageFile != null && !imageFile.isEmpty()) {
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(imageFile.getBytes(), imageFolder);
            item.setImageUrl((String) uploadResult.get("url"));
        }

        if (subImageFiles != null && !subImageFiles.isEmpty()) {
            if (subImageFiles.size() > 0) {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(subImageFiles.get(0).getBytes(), imageFolder);
                item.setSubImageUrl1((String) uploadResult.get("url"));
            }
            if (subImageFiles.size() > 1) {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(subImageFiles.get(1).getBytes(), imageFolder);
                item.setSubImageUrl2((String) uploadResult.get("url"));
            }
            if (subImageFiles.size() > 2) {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(subImageFiles.get(2).getBytes(), imageFolder);
                item.setSubImageUrl3((String) uploadResult.get("url"));
            }
            if (subImageFiles.size() > 3) {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(subImageFiles.get(3).getBytes(), imageFolder);
                item.setSubImageUrl4((String) uploadResult.get("url"));
            }
        }

        Item savedItem = itemRepository.save(item);
        log.info("listing saved");
        // Create an empty rating for the new product
        Rating productRating = new Rating();
        productRating.setEntityId(savedItem.getId().toString());
        productRating.setEntityType(Rating.EntityType.PRODUCT);
        productRating.setTotalRating(0);
        productRating.setNumRatings(0);
        ratingRepository.save(productRating);

        return ItemMapper.toDTO(savedItem);
    }

    public ItemDTO updateItem(Long itemId, ItemDTO itemDTO, MultipartFile imageFile, List<MultipartFile> subImageFiles, List<String> subImageFileURLs) throws IOException {
        Item existingItem = itemRepository.findById(itemId).orElseThrow(
                () -> new ResourceNotFoundException("Item", "id", itemId.toString())
        );
        MainCategory mainCategory = mainCategoryRepository.findById(itemDTO.getMainCategoryId()).orElseThrow(
                () -> new ResourceNotFoundException("MainCategory", "id", itemDTO.getMainCategoryId().toString())
        );
        SubCategory subCategory = subCategoryRepository.findById(itemDTO.getSubCategoryId()).orElseThrow(
                () -> new ResourceNotFoundException("SubCategory", "id", itemDTO.getSubCategoryId().toString())
        );
        Rating rating = itemDTO.getRatingId() != null ?
                ratingRepository.findById(itemDTO.getRatingId()).orElse(null) : null;

        ItemMapper.updateEntityFromDTO(itemDTO, existingItem, mainCategory, subCategory, rating);

        if (imageFile != null && !imageFile.isEmpty()) {
            if (existingItem.getImageUrl() != null && !existingItem.getImageUrl().isEmpty()) {
                String publicId = cloudinaryService.extractPublicIdFromUrl(existingItem.getImageUrl());
                cloudinaryService.deleteFile(publicId, imageFolder);
            }
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(imageFile.getBytes(), imageFolder);
            existingItem.setImageUrl((String) uploadResult.get("url"));
        } else if (existingItem.getImageUrl() == null || existingItem.getImageUrl().isEmpty()) {
            throw new IllegalArgumentException("Cover image is required.");
        }

        uploadSubImages(existingItem, subImageFiles, subImageFileURLs);
        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toDTO(updatedItem);
    }

    public void deleteItem(Long itemId) throws IOException {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ResourceNotFoundException("Item", "id", itemId.toString())
        );

        // Soft delete by setting the deleted flag to true
        item.setDeleted(true);
        itemRepository.save(item);

        // Delete the cover image from Cloudinary
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(item.getImageUrl());
            cloudinaryService.deleteFile(publicId, imageFolder);
        }

        // Delete the additional images from Cloudinary
        if (item.getSubImageUrl1() != null && !item.getSubImageUrl1().isEmpty()) {
            String publicId1 = cloudinaryService.extractPublicIdFromUrl(item.getSubImageUrl1());
            cloudinaryService.deleteFile(publicId1, imageFolder);
        }
        if (item.getSubImageUrl2() != null && !item.getSubImageUrl2().isEmpty()) {
            String publicId2 = cloudinaryService.extractPublicIdFromUrl(item.getSubImageUrl2());
            cloudinaryService.deleteFile(publicId2, imageFolder);
        }
        if (item.getSubImageUrl3() != null && !item.getSubImageUrl3().isEmpty()) {
            String publicId3 = cloudinaryService.extractPublicIdFromUrl(item.getSubImageUrl3());
            cloudinaryService.deleteFile(publicId3, imageFolder);
        }
        if (item.getSubImageUrl4() != null && !item.getSubImageUrl4().isEmpty()) {
            String publicId4 = cloudinaryService.extractPublicIdFromUrl(item.getSubImageUrl4());
            cloudinaryService.deleteFile(publicId4, imageFolder);
        }

        // Save the item to persist the deleted flag
        itemRepository.save(item);
    }

    public ItemDTO getItem(Long itemId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ResourceNotFoundException("Item", "id", itemId.toString())
        );

        if (item.isDeleted()) {
            throw new ResourceNotFoundException("Item", "id", itemId.toString());
        }

        return ItemMapper.toDTO(item);
    }

    public List<ItemDTO> getAllItems() {
        List<Item> items = itemRepository.findAllByDeletedFalse();
        return items.stream().map(ItemMapper::toDTO).collect(Collectors.toList());
    }


    public List<ItemDTO> searchItems(String query, String country, String state, Double minPrice, Double maxPrice, Long mainCategoryId, Long subCategoryId) {
        Specification<Item> spec = Specification.where((root, query1, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted"))
        );
        boolean hasCriteria = false;

        // Normalize and validate inputs
        final String finalQuery = query != null ? query.trim() : "";
        final String finalCountry = country != null ? country.trim() : "";
        final String finalState = state != null ? state.trim() : "";
        final Double finalMinPrice = (minPrice != null && minPrice >= 0) ? minPrice : null;
        final Double finalMaxPrice = (maxPrice != null && maxPrice >= 0) ? maxPrice : null;

        log.info("Starting search with parameters: query={}, country={}, state={}, minPrice={}, maxPrice={}, mainCategoryId={}, subCategoryId={}",
                finalQuery, finalCountry, finalState, finalMinPrice, finalMaxPrice, mainCategoryId, subCategoryId);

        if (!finalQuery.isEmpty() && !"all".equalsIgnoreCase(finalQuery)) {
            hasCriteria = true;
            spec = spec.and((root, criteriaQuery, criteriaBuilder) -> {
                String pattern = "%" + finalQuery.toLowerCase() + "%";
                return criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("mainCategory").get("name")), pattern),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("subCategory").get("name")), pattern)
                );
            });
            log.info("Added query criteria");
        }
        if (!finalCountry.isEmpty() && !"all".equalsIgnoreCase(finalCountry)) {
            hasCriteria = true;
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("country"), finalCountry));
            log.info("Added country criteria");
        }
        if (!finalState.isEmpty() && !"all".equalsIgnoreCase(finalState)) {
            hasCriteria = true;
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("state"), finalState));
            log.info("Added state criteria");
        }
        if (finalMinPrice != null) {
            hasCriteria = true;
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.greaterThanOrEqualTo(root.get("price"), finalMinPrice));
            log.info("Added minPrice criteria");
        }
        if (finalMaxPrice != null) {
            hasCriteria = true;
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.lessThanOrEqualTo(root.get("price"), finalMaxPrice));
            log.info("Added maxPrice criteria");
        }
        if (mainCategoryId != null) {
            hasCriteria = true;
            List<Long> subCategoryIds = subCategoryRepository.findByMainCategoryId(mainCategoryId).stream()
                    .map(SubCategory::getId)
                    .collect(Collectors.toList());
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.equal(root.get("mainCategory").get("id"), mainCategoryId),
                            root.get("subCategory").get("id").in(subCategoryIds)
                    ));
            log.info("Added mainCategoryId criteria with subcategories: {}", subCategoryIds);
        }
        if (subCategoryId != null) {
            hasCriteria = true;
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("subCategory").get("id"), subCategoryId));
            log.info("Added subCategoryId criteria");
        }

        if (!hasCriteria) {
            log.info("No search criteria provided, fetching all items");
            return itemRepository.findAllByDeletedFalse().stream().map(item -> {
                ItemDTO itemDTO = ItemMapper.toDTO(item);
                User seller = userRepository.findById(item.getSeller().getId()).orElse(null);
                if (seller != null) {
                    itemDTO.setUsername(seller.getUsername());
                    Rating rating = ratingRepository.findByEntityIdAndEntityType(seller.getId(), Rating.EntityType.SELLER)
                            .orElse(null);
                }
                return itemDTO;
            }).collect(Collectors.toList());
        }

        List<Item> items = itemRepository.findAll(spec);
        log.info("Found {} items matching criteria", items.size());

        return items.stream().map(item -> {
            ItemDTO itemDTO = ItemMapper.toDTO(item);
            User seller = userRepository.findById(item.getSeller().getId()).orElse(null);
            if (seller != null) {
                itemDTO.setUsername(seller.getUsername());
                Rating rating = ratingRepository.findByEntityIdAndEntityType(seller.getId(), Rating.EntityType.SELLER)
                        .orElse(null);
            }
            return itemDTO;
        }).collect(Collectors.toList());
    }
}

