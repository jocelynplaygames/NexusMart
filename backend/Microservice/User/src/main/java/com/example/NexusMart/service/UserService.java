package com.example.NexusMart.service;

// 导入必要的依赖包
import com.example.NexusMart.config.DataSourceType;                    // 数据源类型枚举（主库/从库）
import com.example.NexusMart.config.ReplicationRoutingDataSourceContext; // 数据源路由上下文
import com.example.NexusMart.dto.UserDTO;                             // 用户数据传输对象
import com.example.NexusMart.dto.CardDTO;                             // 支付卡数据传输对象
import com.example.NexusMart.mapper.CardMapper;                       // 支付卡数据映射器
import com.example.NexusMart.exception.ResourceNotFoundException;     // 资源未找到异常
import com.example.NexusMart.exception.UserAlreadyExistsException;    // 用户已存在异常
import com.example.NexusMart.mapper.UserMapper;                       // 用户数据映射器
import com.example.NexusMart.model.*;                                 // 实体模型类
import com.example.NexusMart.repository.AddressRepository;           // 地址数据访问层
import com.example.NexusMart.repository.CardRepository;              // 支付卡数据访问层
import com.example.NexusMart.repository.RatingRepository;            // 评分数据访问层
import com.example.NexusMart.repository.UserRepository;              // 用户数据访问层
import com.example.NexusMart.jwt.JwtTokenProvider;                   // JWT令牌提供者
import jakarta.persistence.OptimisticLockException;                  // 乐观锁异常
import lombok.extern.slf4j.Slf4j;                                    // Lombok日志注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;             // 配置属性注入
import org.springframework.stereotype.Service;                        // 服务层注解
import java.util.*;
import java.util.stream.Collectors;                                   // 流式处理工具

import org.springframework.transaction.annotation.Transactional;      // 事务注解
import org.springframework.web.multipart.MultipartFile;               // 文件上传
import java.io.IOException;
import java.util.Optional;

/**
 * 用户业务服务层
 * 负责处理用户相关的业务逻辑，包括用户管理、支付卡管理、文件上传等功能
 * 
 * 重要功能：
 * 1. 用户CRUD操作（创建、查询、更新、删除）
 * 2. 支付卡管理（增删改查）
 * 3. 头像文件上传到云存储
 * 4. Keycloak身份认证集成
 * 5. 数据库主从复制路由
 * 6. 乐观锁并发控制
 * 
 * 技术特点：
 * - 使用主从数据库分离读写操作
 * - 集成Keycloak进行身份认证
 * - 使用Cloudinary进行文件存储
 * - 实现乐观锁防止并发冲突
 * - 事务管理确保数据一致性
 * 
 * @Service: 标识这是一个Spring服务层组件
 * @Slf4j: Lombok提供的日志注解
 */
@Service
@Slf4j
public class UserService {//控制器（UserController）收到用户请求后，所有具体的 “办事逻辑”（比如注册时检查邮箱是否重复、更新时上传头像、管理银行卡）都在这里处理。

    // ==================== 依赖注入 ====================
    
    // 数据访问层依赖
    private final UserRepository userRepository;        // 用户数据访问层
    private final AddressRepository addressRepository;  // 地址数据访问层
    private final CardRepository cardRepository;        // 支付卡数据访问层
    private final RatingRepository ratingRepository;    // 评分数据访问层
    
    // 外部服务依赖
    private final KeycloakService keycloakService;      // Keycloak身份认证服务
    private final CloudinaryService cloudinaryService;  // Cloudinary云存储服务
    private final JwtTokenProvider jwtTokenProvider;    // JWT令牌提供者

    // 配置属性
    @Value("${cloudinary.avatar-upload-folder}")
    private String imageFolder;                         // Cloudinary头像上传文件夹

    /**
     * 构造函数依赖注入
     * Spring会自动注入所有需要的依赖组件
     * 
     * @param userRepository 用户数据访问层
     * @param addressRepository 地址数据访问层
     * @param keycloakService Keycloak身份认证服务
     * @param cloudinaryService Cloudinary云存储服务
     * @param jwtTokenProvider JWT令牌提供者
     * @param cardRepository 支付卡数据访问层
     * @param ratingRepository 评分数据访问层
     */
    @Autowired
    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       KeycloakService keycloakService,
                       CloudinaryService cloudinaryService,
                       JwtTokenProvider jwtTokenProvider,
                       CardRepository cardRepository,
                       RatingRepository ratingRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.keycloakService = keycloakService;
        this.cloudinaryService = cloudinaryService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.cardRepository = cardRepository;
        this.ratingRepository = ratingRepository;
    }

    // ==================== 用户管理方法 ====================

    /**
     * 创建新用户
     * 
     * 重要步骤：
     * 1. 设置数据源为主库（写操作）
     * 2. 检查邮箱是否已存在
     * 3. 在Keycloak中注册用户
     * 4. 获取JWT令牌
     * 5. 创建用户评分记录
     * 6. 保存用户信息到数据库
     * 
     * 业务逻辑：
     * - 用户注册时需要同时创建Keycloak账户和数据库记录
     * - 为新用户创建空的评分记录，用于后续评价系统
     * - 使用Keycloak生成的用户ID作为数据库主键
     * 
     * @param userDTO 用户数据传输对象
     * @return JWT令牌字符串
     * @throws UserAlreadyExistsException 当邮箱已存在时抛出
     */
    //用户填了申请表（userDTO）→ 工作人员检查是否重复（optionalUser）→ 在身份系统登记（keycloakService）→ 发通行证（token）→ 建档存档（user和rating）→ 把通行证给用户（return token）。
    public String createUser(UserDTO userDTO) {//从UserController传来（前端→控制器→这里）//这个userDTO是从UserController（前端对接的 “窗口”）传过来的 —— 用户在 APP 上填的注册信息，先被前端打包成UserDTO，
        // 设置数据源为主库，用于写操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);//“系统设置”：告诉系统 “接下来要写数据，用主数据库（MASTER）”。
        try {
            // 将DTO转换为实体对象
            User user = UserMapper.toEntity(userDTO);//UserMapper是一个 “格式转换器”（提前定义好的工具类），它会把userDTO（申请表）里的信息（用户名、邮箱等）复制到User对象（档案）里。
            //这里通过 UserMapper（数据转换工具），把前端传来的 UserDTO（用户提交的注册信息）转换成 User 实体对象。
            //User 是和数据库表对应的结构完全对应的 “数据载体”，里面包含了数据库需要的所有字段（比如用户名、邮箱、创建时间等），而 UserDTO 只包含前端需要传递的部分信息。
            
            // 检查邮箱是否已存在
            Optional<User> optionalUser = userRepository.findByEmail(userDTO.getEmail());//userRepository是 “数据库操作工具”（提前通过依赖注入进来的，相当于档案管理员），调用它的findByEmail方法，传入userDTO里的邮箱（userDTO.getEmail()），查是否有重复邮箱的用户。查询结果存在optionalUser里
            if (optionalUser.isPresent()) {
                log.info("User already registered with given email!");
                throw new UserAlreadyExistsException("User already registered with given email: " + userDTO.getEmail());
            }

            // 在Keycloak中注册用户
            String adminToken = keycloakService.getAdminToken();//keycloakService是 “身份认证对接工具”（提前依赖注入的，相当于对接公安局身份系统的接口），调用它的方法获取管理员令牌（没有这个令牌，没法在身份系统里创建用户）。
//keycloakService生成的令牌→存在adminToken里。
            keycloakService.createUserInKeycloak(adminToken, userDTO);//用上面拿到的adminToken（授权证明）和userDTO（用户信息），通过keycloakService在 Keycloak（身份认证系统）里创建用户账号（相当于在公安局系统里登记这个人的身份）。
            
            // 获取用户JWT令牌
            String token = keycloakService.getUserToken(userDTO.getEmail(), userDTO.getPassword());//用userDTO里的邮箱和密码，从 Keycloak 获取用户专属的令牌（以后用户登录不用输账号密码，带这个令牌就行）。

            // 从JWT令牌中提取用户ID
            String keycloakUserId = jwtTokenProvider.extractUserIdFromToken(token);//jwtTokenProvider是 “令牌解析工具”（提前依赖注入的），从上面拿到的token（通行证）里解析出用户 ID。存进keycloakUserId：是 “用户在身份系统中的唯一编号”里
            log.info("Extracted Keycloak user ID: {}", keycloakUserId);
            user.setId(keycloakUserId); // 确保ID设置正确//存入user对象的id属性中
            //keycloakUserId：
            //是 “身份认证系统（Keycloak）” 给用户的唯一编号，相当于公安局给你的身份证号。
            //来源：由 Keycloak 自动生成（就像身份证号由公安系统生成，全国唯一）。
            //作用：在整个身份认证过程中标识用户（比如登录、权
            // userId：
            //是 “你的业务系统（数据库）” 中用户的唯一编号，相当于户籍系统里的户籍编号。
            //来源：在代码中，userId被设置为和keycloakUserId完全一样（user.setId(keycloakUserId)）。
            //为什么要这样？：为了让 “身份系统” 和 “业务系统” 中的用户能一一对应（就像你的身份证号和户籍编号通常是绑定的，查户籍时用身份证号就能找到）。
            
//前端收集用户信息（姓名、邮箱、密码），打包成UserDTO（户籍证明）传给后端。
//后端用UserMapper把UserDTO转成User（户口本原件），准备存入数据库。
//同时，后端让 Keycloak（公安局）创建用户，Keycloak 生成keycloakUserId（身份证号）。
//后端把keycloakUserId设置为User的id（户口本编号 = 身份证号）。
//最后，User（带身份证号的户口本）被存入数据库，UserDTO则不会被存，只是传递信息的 “临时载体”。

//身份系统（Keycloak）和业务系统（你的数据库）的用户一一对应。
//前端调用 user/{id} 接口时，无需关心背后的身份系统，直接用同一个 ID 就能操作用户。

            // 创建用户评分记录（初始为空）
            Rating rating = new Rating();
            rating.setEntityId(keycloakUserId);//关联用户 ID（信用记录属于这个用户）。
            rating.setEntityType(Rating.EntityType.SELLER);//信用记录类型（SELLER：卖家）。
            rating.setTotalRating(0);//初始信用值为0。
            rating.setNumRatings(0);//初始评价次数为0。
            ratingRepository.save(rating);//保存信用记录到数据库。
            log.info("Created empty rating for user with ID: {}", user.getId());//记录创建成功。

            // 保存用户信息到数据库
            userRepository.save(user);//保存用户信息到数据库。
            log.info("Successfully created a user with ID: {}", user.getId());
            return token;
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();//不管注册成功还是失败，最后都要清理之前的 “主库设置”（相当于办完业务后，工作人员说 “主档案库用完了，恢复默认设置”）。
        }
    }

    /**
     * 根据用户ID获取用户信息
     * 
     * 重要步骤：
     * 1. 设置数据源为从库（读操作）
     * 2. 查询用户信息
     * 3. 转换为DTO返回
     * 
     * 技术特点：
     * - 使用@Transactional(readOnly = true)优化读操作性能
     * - 从从库读取数据，减轻主库压力
     * 
     * @param id 用户ID
     * @return 用户数据传输对象
     * @throws ResourceNotFoundException 当用户不存在时抛出
     */
    @Transactional(readOnly = true)
    public UserDTO getUserById(String id) {
        // 设置数据源为从库，用于读操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            User user = userRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("User", "id", id)
            );
            return UserMapper.toDTO(user);
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    /**
     * 更新用户信息（支持头像上传）
     * 
     * 重要步骤：
     * 1. 设置数据源为主库（写操作）
     * 2. 验证用户是否存在
     * 3. 检查邮箱唯一性
     * 4. 更新用户基本信息
     * 5. 处理地址信息更新
     * 6. 处理头像文件上传
     * 7. 同步更新Keycloak信息
     * 
     * 业务逻辑：
     * - 支持部分字段更新
     * - 头像上传会删除旧头像并上传新头像
     * - 邮箱或用户名变更时会同步更新Keycloak
     * - 使用乐观锁防止并发更新冲突
     * 
     * 专业术语：
     * - 乐观锁：通过版本号控制并发，避免数据覆盖
     * - 主从复制：读写分离，提高系统性能
     * 
     * @param id 用户ID
     * @param userDTO 更新的用户信息
     * @param profilePicture 头像文件（可选）
     * @return 更新是否成功
     * @throws IOException 文件处理异常
     * @throws OptimisticLockException 乐观锁冲突异常
     */
    public boolean updateUser(String id, UserDTO userDTO, MultipartFile profilePicture) throws IOException {
        // 设置数据源为主库，用于写操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            // 查找现有用户
            User existingUser = userRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("User", "id", id)
            );

            // 验证邮箱有效性
            if (Objects.equals(userDTO.getEmail(), "")) {
                throw new RuntimeException("Email is not valid.");
            }

            log.info("Updating user with ID: {}", id);
            log.info("Existing user: {}", existingUser);
            log.info("New user's ProfilePictureUrl", userDTO.getProfilePictureUrl());

            // 检查邮箱和用户名是否发生变化
            boolean emailChanged = !existingUser.getEmail().equals(userDTO.getEmail());
            boolean usernameChanged = !existingUser.getUsername().equals(userDTO.getUsername());

            // 如果邮箱发生变化，检查新邮箱是否已被其他用户使用
            if (emailChanged) {
                Optional<User> userWithEmail = userRepository.findByEmail(userDTO.getEmail());
                if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                    throw new UserAlreadyExistsException("Email is already in use by another user.");
                }
            }

            // 更新用户基本信息
            UserMapper.updateEntityFromDTO(userDTO, existingUser);
            log.info("Updated user entity: {}", existingUser.getProfilePictureUrl());

            // 处理地址信息更新
            if (userDTO.getAddress() != null) {
                Address address = existingUser.getAddress() != null ? existingUser.getAddress() : new Address();
                address.setUser(existingUser);
                address.setStreet(userDTO.getAddress().getStreet());
                address.setCity(userDTO.getAddress().getCity());
                address.setState(userDTO.getAddress().getState());
                address.setPostalCode(userDTO.getAddress().getPostalCode());
                address.setCountry(userDTO.getAddress().getCountry());
                
                // 设置乐观锁版本号
                if (address.getVersion() == null) {
                    address.setVersion(0);
                }
                log.info("Saving address: {}", address);
                address = addressRepository.save(address);
                existingUser.setAddress(address);
            }

            // 处理头像文件上传
            if (profilePicture != null && !profilePicture.isEmpty()) {
                // 删除旧头像
                if (existingUser.getProfilePictureUrl() != null && !existingUser.getProfilePictureUrl().isEmpty()) {
                    log.info("Current profile picture URL: {}", existingUser.getProfilePictureUrl());
                    String publicId = cloudinaryService.extractPublicIdFromUrl(existingUser.getProfilePictureUrl());
                    log.info("Deleting old avatar with public ID: {}", publicId);
                    cloudinaryService.deleteFile(publicId, imageFolder);
                }
                
                // 上传新头像
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(profilePicture.getBytes(), imageFolder);
                existingUser.setProfilePictureUrl((String) uploadResult.get("url"));
            }

            // 保存更新后的用户信息
            log.info("Saving user: {}", existingUser);
            userRepository.save(existingUser);

            // 如果邮箱或用户名发生变化，同步更新Keycloak
            if (emailChanged || usernameChanged) {
                String adminToken = keycloakService.getAdminToken();
                keycloakService.updateUserInKeycloak(adminToken, id, userDTO);
            }

            return true;
        } catch (OptimisticLockException ex) {
            // 处理乐观锁冲突
            log.error("Optimistic locking failure while updating user with ID: {}", id, ex);
            throw ex;
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    /**
     * 删除用户
     * 
     * 重要步骤：
     * 1. 设置数据源为主库（写操作）
     * 2. 验证用户是否存在
     * 3. 删除用户记录
     * 
     * @param id 用户ID
     * @return 删除是否成功
     * @throws ResourceNotFoundException 当用户不存在时抛出
     */
    public boolean deleteUserById(String id) {
        // 设置数据源为主库，用于写操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            User user = userRepository.findById(id).orElseThrow(
                    () -> new ResourceNotFoundException("User", "id", id)
            );
            userRepository.deleteById(user.getId());
            return true;
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    // ==================== 支付卡管理方法 ====================

    /**
     * 根据支付卡ID获取支付卡信息
     * 
     * @param cardId 支付卡ID
     * @return 支付卡数据传输对象
     * @throws ResourceNotFoundException 当支付卡不存在时抛出
     */
    @Transactional(readOnly = true)//这是 “事务注解”，标记这个方法是 “只读操作”。
    public CardDTO getCardById(Long cardId) {//CardDTO：方法的返回类型，是 “支付卡信息的对外传输格式”
        // 指定 “用从库（SLAVE）查询数据”。背景：数据库通常有 “主库（MASTER）” 和 “从库（SLAVE）”，主库负责写操作（新增 / 修改 / 删除），从库负责读操作（查询），这样能减轻主库压力。
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            Card card = cardRepository.findById(cardId)//cardRepository：是 “支付卡数据访问工具”（提前通过依赖注入进来的，相当于银行的 “银行卡档案库管理员”）。存储查到的 “支付卡实体”（相当于从档案库中找到的原始银行卡档案，包含数据库需要的所有信息，比如卡类型、卡号、过期时间、关联的用户 ID 等）。
                    .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId.toString()));
            return CardMapper.toDTO(card);//CardMapper是 “格式转换器”（提前定义的工具类），把数据库用的Card实体（原始档案）转换成对外传输的CardDTO（详情单）。转换目的：Card实体可能包含敏感信息或数据库内部字段（比如卡的加密信息、版本号），CardDTO只保留需要展示给前端的信息（比如卡类型、后四位卡号、过期时间），更安全简洁。
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();//查完卡后，告诉系统 “我用完从库了，恢复默认设置”
        }
    }

    /**
     * 根据用户ID获取所有支付卡
     * 
     * 技术特点：
     * - 使用Stream API进行数据转换
     * - 从从库读取数据，提高性能
     * 
     * @param userId 用户ID
     * @return 支付卡列表
     */
    @Transactional(readOnly = true)
    public List<CardDTO> getCardsByUserId(String userId) {
        // 设置数据源为从库，用于读操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.SLAVE);
        try {
            List<Card> cards = cardRepository.findByUserId(userId);
            // 使用Stream API将实体列表转换为DTO列表
            return cards.stream().map(CardMapper::toDTO).collect(Collectors.toList());
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    /**
     * 为用户创建新的支付卡
     * 
     * 重要步骤：
     * 1. 设置数据源为主库（写操作）
     * 2. 验证用户是否存在
     * 3. 创建支付卡实体
     * 4. 建立用户与支付卡的关联关系
     * 5. 保存支付卡信息
     * 
     * @param userId 用户ID
     * @param cardDTO 支付卡信息
     * @return 创建的支付卡数据传输对象
     * @throws ResourceNotFoundException 当用户不存在时抛出
     */
    public CardDTO createCard(String userId, CardDTO cardDTO) {
        // 设置数据源为主库，用于写操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            log.info("creating card! User id: " + userId);
            
            // 验证用户是否存在
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            // 创建支付卡实体
            Card card = CardMapper.toEntity(cardDTO);
            card.setUser(user); // 建立关联关系
            
            log.info("saving card!");
            Card savedCard = cardRepository.save(card);
            return CardMapper.toDTO(savedCard);
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    /**
     * 更新支付卡信息
     * 
     * 重要步骤：
     * 1. 设置数据源为主库（写操作）
     * 2. 查找现有支付卡
     * 3. 更新支付卡信息
     * 4. 保存更新后的支付卡
     * 
     * @param cardId 支付卡ID
     * @param cardDTO 更新的支付卡信息
     * @return 更新后的支付卡数据传输对象
     * @throws ResourceNotFoundException 当支付卡不存在时抛出
     */
    public CardDTO updateCard(Long cardId, CardDTO cardDTO) {
        // 设置数据源为主库，用于写操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            // 查找现有支付卡
            Card existingCard = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId.toString()));
            
            // 更新支付卡信息
            existingCard.setType(cardDTO.getType());
            existingCard.setCardNumber(cardDTO.getCardNumber());
            existingCard.setExpirationDate(cardDTO.getExpirationDate());
            
            Card updatedCard = cardRepository.save(existingCard);
            return CardMapper.toDTO(updatedCard);
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }

    /**
     * 删除支付卡
     * 
     * 重要步骤：
     * 1. 设置数据源为主库（写操作）
     * 2. 验证支付卡是否存在
     * 3. 删除支付卡记录
     * 
     * @param cardId 支付卡ID
     * @throws ResourceNotFoundException 当支付卡不存在时抛出
     */
    public void deleteCard(Long cardId) {
        // 设置数据源为主库，用于写操作
        ReplicationRoutingDataSourceContext.setDataSourceType(DataSourceType.MASTER);
        try {
            // 验证支付卡是否存在
            Card existingCard = cardRepository.findById(cardId).orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId.toString()));
            cardRepository.delete(existingCard);
        } finally {
            // 清理数据源上下文
            ReplicationRoutingDataSourceContext.clearDataSourceType();
        }
    }
}



