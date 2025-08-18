package com.example.NexusMart.service;

import com.example.NexusMart.config.DataSourceType;
import com.example.NexusMart.config.ReplicationRoutingDataSourceContext;
import com.example.NexusMart.dto.ItemDTO;
import com.example.NexusMart.dto.RatingDTO;
import com.example.NexusMart.exception.ResourceNotFoundException;
import com.example.NexusMart.mapper.ItemMapper;
import com.example.NexusMart.model.Item;
import com.example.NexusMart.model.MainCategory;
import com.example.NexusMart.model.SubCategory;
import com.example.NexusMart.pojoClass.User;
import com.example.NexusMart.pojoClass.UserResponse;
import com.example.NexusMart.repository.ItemRepository;
import com.example.NexusMart.repository.MainCategoryRepository;
import com.example.NexusMart.repository.SubCategoryRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final MainCategoryRepository mainCategoryRepository;
    private final SubCategoryRepository subCategoryRepository;
    private final CloudinaryService cloudinaryService;
    private final RestTemplate restTemplate;

    @Value("${cloudinary.item-upload-folder}")
    private String imageFolder;

    @Value("${userservice.url}")
    private String userServiceUrl;

    @Value("${ratingservice.url}")
    private String ratingServiceUrl;

    private final CacheManager cacheManager;

    @Autowired
    public ItemService(ItemRepository itemRepository,
                       MainCategoryRepository mainCategoryRepository,
                       SubCategoryRepository subCategoryRepository,
                       CloudinaryService cloudinaryService,
                       RestTemplate restTemplate,
                       CacheManager cacheManager) {
        this.itemRepository = itemRepository;
        this.mainCategoryRepository = mainCategoryRepository;
        this.subCategoryRepository = subCategoryRepository;
        this.cloudinaryService = cloudinaryService;
        this.restTemplate = restTemplate;
        this.cacheManager = cacheManager;
    }

    @Transactional(readOnly = true)
    public List<ItemDTO> getItemsByUserId(String userId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            User user = getUserById(userId);
            List<Item> items = itemRepository.findBySellerIdAndDeletedFalse(userId);
            return items.stream().map(ItemMapper::toDTO).collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional(readOnly = true)
    public List<ItemDTO> getRandomItems(int count) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            List<Item> items = itemRepository.findRandomItems(count);
            return items.stream().map(ItemMapper::toDTO).collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public ItemDTO createItem(@Valid ItemDTO itemDTO, MultipartFile imageFile, List<MultipartFile> subImageFiles) throws IOException {
        log.info("Saving new listing!");
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            MainCategory mainCategory = mainCategoryRepository.findById(itemDTO.getMainCategoryId()).orElseThrow(
                    () -> new ResourceNotFoundException("MainCategory", "id", itemDTO.getMainCategoryId().toString())
            );
            SubCategory subCategory = subCategoryRepository.findById(itemDTO.getSubCategoryId()).orElseThrow(
                    () -> new ResourceNotFoundException("SubCategory", "id", itemDTO.getSubCategoryId().toString())
            );

            if (itemDTO.getQuantity() <= 0) {
                throw new RuntimeException("Quantity should be greater than 0!");
            }
            if (itemDTO.getPrice() <= 0) {
                throw new RuntimeException("Price should be greater than 0!");
            }

            Item item = ItemMapper.toEntity(itemDTO, mainCategory, subCategory);

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
            log.info("Listing saved");

            // Create an empty rating for the new product
            RatingDTO productRating = new RatingDTO();
            productRating.setEntityId(savedItem.getId().toString());
            productRating.setEntityType("PRODUCT");
            productRating.setTotalRating(0);
            productRating.setNumRatings(0);
            RatingDTO savedRating = createRating(productRating);

            // Update the item with the rating ID
            savedItem.setRatingId(savedRating.getId());
            itemRepository.save(savedItem);
            return ItemMapper.toDTO(savedItem);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    public void updateItemQuantity(ItemDTO itemDTO) {
        Item item = itemRepository.findById(itemDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", "id", itemDTO.getId().toString()));
        item.setQuantity(itemDTO.getQuantity());
        itemRepository.save(item);
    }

    @Transactional
    public ItemDTO updateItem(Long itemId, ItemDTO itemDTO, MultipartFile imageFile, List<MultipartFile> subImageFiles, List<String> subImageFileURLs) throws IOException {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            Item existingItem = itemRepository.findById(itemId).orElseThrow(
                    () -> new ResourceNotFoundException("Item", "id", itemId.toString())
            );
            MainCategory mainCategory = mainCategoryRepository.findById(itemDTO.getMainCategoryId()).orElseThrow(
                    () -> new ResourceNotFoundException("MainCategory", "id", itemDTO.getMainCategoryId().toString())
            );
            SubCategory subCategory = subCategoryRepository.findById(itemDTO.getSubCategoryId()).orElseThrow(
                    () -> new ResourceNotFoundException("SubCategory", "id", itemDTO.getSubCategoryId().toString())
            );

            ItemMapper.updateEntityFromDTO(itemDTO, existingItem, mainCategory, subCategory);

            if (imageFile != null && !imageFile.isEmpty()) {
                if (existingItem.getImageUrl() != null && !existingItem.getImageUrl().isEmpty()) {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existingItem.getImageUrl());
                    cloudinaryService.deleteFile(publicId, imageFolder);
                }
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(imageFile.getBytes(), imageFolder);
                existingItem.setImageUrl((String) uploadResult.get("url"));
            } else {
                existingItem.setImageUrl(itemDTO.getImageUrl());
            }

            uploadSubImages(existingItem, subImageFiles, subImageFileURLs);
            Item updatedItem = itemRepository.save(existingItem);

            invalidateSearchItemsCache();
            return ItemMapper.toDTO(updatedItem);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional
    public void deleteItem(Long itemId) throws IOException {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
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

            invalidateSearchItemsCache();
            itemRepository.save(item);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional(readOnly = true)
    public ItemDTO getItem(Long itemId) {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            Item item = itemRepository.findById(itemId).orElseThrow(
                    () -> new ResourceNotFoundException("Item", "id", itemId.toString())
            );

            if (item.isDeleted()) {
                throw new ResourceNotFoundException("Item", "id", itemId.toString());
            }

            return ItemMapper.toDTO(item);
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional(readOnly = true)
    public List<ItemDTO> getAllItems() {
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            List<Item> items = itemRepository.findAllByDeletedFalse();
            return items.stream().map(ItemMapper::toDTO).collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "searchItemsCache", key = "'-' + #query + '-' + #country + '-' + #state + '-' + #minPrice + '-' + #maxPrice + '-' + #mainCategoryId + '-' + #subCategoryId")
    public List<ItemDTO> searchItems(String query, String country, String state, Double minPrice, Double maxPrice, Long mainCategoryId, Long subCategoryId) {
        log.info("Fetching items from MySQL database...");
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            Specification<Item> spec = Specification.where((root, query1, criteriaBuilder) ->
                    criteriaBuilder.isFalse(root.get("deleted"))
            );
            boolean hasCriteria = false;

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
                    User seller = getUserById(item.getSellerId());
                    if (seller != null) {
                        log.info("seller: " + seller.toString());
                        log.info("Seller's username: " + seller.getUsername());
                        itemDTO.setUsername(seller.getUsername());
                    } else {
                        log.warn("Seller invalid!");
                    }
                    return itemDTO;
                }).collect(Collectors.toList());
            }

            log.info("debugging!!!");
            List<Item> items = itemRepository.findAll(spec);
            log.info("Found {} items matching criteria", items.size());

            return items.stream().map(item -> {
                ItemDTO itemDTO = ItemMapper.toDTO(item);
                User seller = getUserById(item.getSellerId());
                log.info("Seller: " + seller.toString());
                if (seller != null) {
                    itemDTO.setUsername(seller.getUsername());
                }
                return itemDTO;
            }).collect(Collectors.toList());
        } finally {
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    private User getUserById(String userId) {
        try {
            String url = String.format("%s/%s", userServiceUrl, userId);
            ResponseEntity<UserResponse> userResponse = restTemplate.getForEntity(url, UserResponse.class);
            UserResponse response = userResponse.getBody();
            if (response == null || response.getUser() == null) {
                throw new ResourceNotFoundException("User", "id", userId);
            }
            return response.getUser();
        } catch (HttpServerErrorException e) {
            log.error("Error fetching user with id {}: {}", userId, e.getMessage());
            throw new ResourceNotFoundException("User", "id", userId);
        }
    }

    private RatingDTO createRating(RatingDTO ratingDTO) {
        try {
            String url = String.format("%s", ratingServiceUrl);
            ResponseEntity<RatingDTO> ratingResponse = restTemplate.postForEntity(url, ratingDTO, RatingDTO.class);
            return ratingResponse.getBody();
        } catch (HttpServerErrorException e) {
            log.error("Error creating rating: {}", e.getMessage());
            throw new RuntimeException("Failed to create rating");
        }
    }

    private void uploadSubImages(Item existingItem, List<MultipartFile> subImageFiles, List<String> subImageFileURLs) throws IOException {
        existingItem.setSubImageUrl1(null);
        existingItem.setSubImageUrl2(null);
        existingItem.setSubImageUrl3(null);
        existingItem.setSubImageUrl4(null);
        int index = 0;
        for (String s : subImageFileURLs) {
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

        if (subImageFiles != null) {
            for (MultipartFile o : subImageFiles) {
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

    @Scheduled(fixedRateString = "${search.items.cache.refresh.rate}", initialDelayString = "${search.items.cache.initial.delay}")
    public void refreshSearchItemsCache() {
        log.info("Refreshing search items cache...");
        cacheManager.getCache("searchItemsCache").clear();
        log.info("Search items cache invalidated.");
    }

    private void invalidateSearchItemsCache() {
        cacheManager.getCache("searchItemsCache").clear();
        log.info("Search items cache invalidated.");
    }
}