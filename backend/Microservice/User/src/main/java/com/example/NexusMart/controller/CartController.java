package com.example.NexusMart.controller;

// 导入必要的依赖包
import com.example.NexusMart.dto.CartDTO;           // 购物车数据传输对象
import com.example.NexusMart.dto.CartItemInputDTO;  // 购物车商品输入数据传输对象
import com.example.NexusMart.dto.CartItemOutputDTO; // 购物车商品输出数据传输对象
import com.example.NexusMart.exception.ResourceNotFoundException;  // 资源未找到异常
import com.example.NexusMart.jwt.JwtTokenProvider;  
import com.example.NexusMart.pojoClass.Item;        // 商品实体类
import com.example.NexusMart.service.CartService;   // 购物车业务服务层
import com.example.NexusMart.service.UserService;   // 用户业务服务层
import lombok.extern.slf4j.Slf4j;                   
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;  // 配置属性注入注解
import org.springframework.dao.InvalidDataAccessApiUsageException;  
import org.springframework.http.HttpStatus;          
import org.springframework.http.ResponseEntity;     
import org.springframework.web.bind.annotation.*;   
import org.springframework.web.client.RestTemplate; 

/**
 * 购物车管理控制器
 * 负责处理购物车相关的HTTP请求，包括购物车CRUD操作、商品添加等功能
 * 
 * 重要功能：
 * 1. 购物车创建、查询、更新、删除
 * 2. 商品添加到购物车（包含业务逻辑验证）
 * 3. 购物车清空
 * 4. JWT令牌验证和用户权限控制
 * 
 * @RestController: 标识这是一个REST API控制器
 * @Slf4j: Lombok提供的日志注解
 * @RequestMapping: 定义API的基础路径
 */
@RestController
@Slf4j
@RequestMapping("/api/cart")
public class CartController {
    
    // 依赖注入：购物车业务服务层
    private final CartService cartService;
    
    private final JwtTokenProvider jwtTokenProvider;
    // 依赖注入：用户业务服务层
    private final UserService userService;
    
    private final RestTemplate restTemplate;

    
    @Value("${itemservice.url}")
    private String itemServiceUrl;

    /**
     * 构造函数依赖注入
     * @param cartService 购物车业务服务
     * @param jwtTokenProvider JWT令牌提供者
     * @param userService 用户业务服务
     * @param restTemplate REST客户端模板
     */
    @Autowired
    public CartController(CartService cartService, JwtTokenProvider jwtTokenProvider, UserService userService, RestTemplate restTemplate) {
        this.cartService = cartService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    /**
     * 创建购物车
     * HTTP POST /api/cart
     * 
     * 重要步骤：
     * 1. 从JWT令牌中提取用户ID
     * 2. 设置购物车的用户ID
     * 3. 调用业务服务创建购物车
     * 
     * @param cartDTO 购物车数据传输对象
     * @param token JWT授权令牌
     * @return 创建的购物车信息
     */
    @PostMapping
    public ResponseEntity<CartDTO> createCart(@RequestBody CartDTO cartDTO, @RequestHeader("Authorization") String token) {
        
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        cartDTO.setUserId(userId);
        CartDTO createdCart = cartService.createCart(cartDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCart);
    }

    /**
     * 根据用户ID获取购物车
     * HTTP GET /api/cart/{userId}
     * 
     * 重要步骤：
     * 1. 验证JWT令牌中的用户ID与请求路径中的用户ID是否一致
     * 2. 确保购物车存在（如果不存在则创建）
     * 3. 返回购物车信息
     * 
     * @param userId 用户ID
     * @param token JWT授权令牌
     * @return 购物车信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable String userId, @RequestHeader("Authorization") String token) {
        
        String tokenUserId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        
        // 权限验证：确保用户只能访问自己的购物车
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        // 确保购物车存在，如果不存在则创建新的购物车
        CartDTO cart = cartService.ensureCartExists(userId);
        return ResponseEntity.ok(cart);
    }

    /**
     * 根据购物车ID获取购物车
     * HTTP GET /api/cart/cartId/{id}
     * 
     * @param id 购物车ID
     * @return 购物车信息
     */
    @GetMapping("/cartId/{id}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable Long id) {
        CartDTO cart = cartService.getCart(id);
        return ResponseEntity.ok(cart);
    }

    /**
     * 更新购物车
     * HTTP PUT /api/cart/{userId}
     * 
     * 重要步骤：
     * 1. 验证用户权限
     * 2. 更新购物车信息
     * 
     * @param userId 用户ID
     * @param cartDTO 购物车数据传输对象
     * @param token JWT授权令牌
     * @return 更新后的购物车信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<CartDTO> updateCart(@PathVariable String userId, @RequestBody CartDTO cartDTO, @RequestHeader("Authorization") String token) {
        // 权限验证：确保用户只能更新自己的购物车
        String tokenUserId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        log.info("cartDTO id: " + cartDTO.getId());
        CartDTO updatedCart = cartService.updateCart(cartDTO);
        return ResponseEntity.ok(updatedCart);
    }

    /**
     * 删除购物车
     * HTTP DELETE /api/cart/{userId}
     * 
     * @param userId 用户ID
     * @param token JWT授权令牌
     * @return 删除结果（无内容响应）
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteCart(@PathVariable String userId, @RequestHeader("Authorization") String token) {
        // 权限验证：确保用户只能删除自己的购物车
        String tokenUserId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        if (!tokenUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        cartService.deleteCartByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 添加商品到购物车
     * HTTP POST /api/cart/items
     * 
     * 重要步骤：
     * 1. 从JWT令牌中提取用户ID
     * 2. 调用商品服务获取商品信息（微服务间通信）
     * 3. 业务逻辑验证：用户不能购买自己的商品
     * 4. 确保购物车存在
     * 5. 添加商品到购物车
     * 
     * 专业术语：
     * - 微服务间通信：使用RestTemplate调用其他微服务
     * - 业务逻辑验证：防止用户购买自己的商品
     * - 事务一致性：确保购物车和商品数据的一致性
     * 
     * @param cartItemDTO 购物车商品输入数据传输对象
     * @param token JWT授权令牌
     * @return 添加的购物车商品信息
     */
    @PostMapping("/items")
    public ResponseEntity<CartItemOutputDTO> addItemToCart(@RequestBody CartItemInputDTO cartItemDTO, @RequestHeader("Authorization") String token) {
        
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));

        // 微服务间通信：调用商品服务获取商品信息
        
        String url = String.format("%s/%s", itemServiceUrl, cartItemDTO.getItemId());
        ResponseEntity<Item> itemResponse = restTemplate.getForEntity(url, Item.class);
        Item item = itemResponse.getBody();

        // 验证商品是否存在
        if (item == null) {
            throw new ResourceNotFoundException("Item", "id", cartItemDTO.getItemId().toString());
        }

        
        String sellerId = item.getSellerId();

        // 业务逻辑验证：用户不能购买自己的商品
        if (userId.equals(sellerId)) {
            throw new InvalidDataAccessApiUsageException("You cannot add your own product to your cart");
        }

        // 确保购物车存在，如果不存在则创建新的购物车
        CartDTO cartDTO = cartService.ensureCartExists(userId);
        cartItemDTO.setCartId(cartDTO.getId());

        
        if (cartItemDTO.getCartId() == null) {
            throw new InvalidDataAccessApiUsageException("Cart id must not be null");
        }

        // 添加商品到购物车
        CartItemOutputDTO createdCartItem = cartService.addItemToCart(cartItemDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCartItem);
    }

    /**
     * 清空购物车
     * HTTP POST /api/cart/clear/{cartId}
     * 
     * @param cartId 购物车ID
     * @param token JWT授权令牌
     * @return 清空结果（无内容响应）
     */
    @PostMapping("/clear/{cartId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long cartId, @RequestHeader("Authorization") String token) {
        cartService.clearCart(cartId);
        return ResponseEntity.noContent().build();
    }
}




