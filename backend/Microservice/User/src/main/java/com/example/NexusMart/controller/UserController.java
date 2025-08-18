package com.example.NexusMart.controller;

// 导入必要的依赖包
import com.example.NexusMart.dto.CardDTO;           // 支付卡数据传输对象
import com.example.NexusMart.dto.ResponseDto;       // 统一响应数据传输对象
import com.example.NexusMart.dto.UserDTO;           // 用户数据传输对象
import com.example.NexusMart.exception.UserAlreadyExistsException;  // 用户已存在异常
import com.example.NexusMart.service.UserService;   // 用户业务服务层
import com.fasterxml.jackson.databind.ObjectMapper; // JSON序列化/反序列化工具
import lombok.extern.slf4j.Slf4j;                   // Lombok日志注解
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;      // Spring环境配置
import org.springframework.http.HttpStatus;          // HTTP状态码
import org.springframework.http.ResponseEntity;     // HTTP响应实体
import org.springframework.validation.annotation.Validated;  // 数据验证注解
import org.springframework.web.bind.annotation.*;   // REST API注解
import jakarta.validation.Valid;                   // Bean验证注解
import org.springframework.web.multipart.MultipartFile;  // 文件上传
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//这段代码叫 UserController（用户控制器），你可以把它理解成 **“用户服务中心的总机”**。
//用户在 APP / 网站上做的各种操作（比如注册账号、改头像、添加银行卡），都会变成 “信号”（专业叫 “HTTP 请求”）发到这里，这个 “总机” 会负责：
//接收这些信号（比如 “我要注册”“查我的信息”）
//告诉后面的 “办事员”（专业叫 “服务层”）该做什么
//把结果整理好回传给用户（比如 “注册成功”“你的信息是 XXX”）

/**
 * 用户管理控制器
 * 负责处理用户相关的HTTP请求，包括用户CRUD操作、支付卡管理等功能
 * 
 * @Slf4j: Lombok提供的日志注解，自动生成log变量
 * @RestController: 标识这是一个REST API控制器，返回JSON格式数据
 * @RequestMapping: 定义API的基础路径和响应格式
 * @Validated: 启用方法级别的参数验证
 * @RestController：告诉系统 “这是个处理网络请求的总机，回复的内容都是 JSON 格式（就像标准化的短信）”。
@RequestMapping("/api/user")：总机的 “分机号”。用户要找它，必须拨这个号（比如 APP 里点 “我的”，背后就会发请求到 /api/user 这个地址）。
@Slf4j：自带 “记事本”，可以记录工作过程（比如 “用户 XXX 注册成功”“用户 YYY 查了信息”），方便出问题时查原因。
 */
@Slf4j
@RestController
@RequestMapping(path="/api/user", produces = "application/json")//前端通过网络发送一个 POST 请求，这个请求包含两部分：地址：比如 http://你的服务器地址/api/user（就像快递的 “收件地址”）。正文（body）：上面打包好的 JSON 字符串（就像快递包裹里的 “物品”）。 
@Validated
public class UserController {

    // 依赖注入：用户业务服务层
    private final UserService userService;
    // 依赖注入：Spring环境配置，用于获取系统配置信息
    private final Environment environment;

    /**
     * 构造函数依赖注入
     * @param userService 用户业务服务
     * @param environment Spring环境配置
     * UserService 是你项目中自定义的业务逻辑层组件，通常用于处理用户相关的核心业务（比如用户注册、查询、修改信息等）。
     * Environment 是 Spring 框架提供的接口，属于 org.springframework.core.env 包，用于访问应用程序的环境配置信息。
     * 它的主要作用是：获取系统环境变量（如 JAVA_HOME）、JVM 系统属性（如 java.version）。
     * 读取配置文件中的属性（如 application.properties 或 application.yml 中的配置，比如 server.port）。
     * 
     * UserService userService：“用户业务办事员”。总机接了请求后，不用自己动手，喊这个办事员去做具体工作（比如查数据库、保存信息）。
     * Environment environment：“系统说明书”。总机如果需要系统配置（比如查当前用的 Java 版本），就翻这本说明书。
     *  @Autowired 构造函数，就像 “公司后勤” 直接把这两个工具送到总机，不用总机自己去借，省时省力。
     * 行政部门（这里指 Spring 框架的 “容器”）看到这个注解后，会主动：
     * 找一个现成的、能用的UserService助理（已经提前培训好的）
     * 找一本现成的Environment手册（已经整理好的）
     * 把这两个东西打包，在你入职时（创建 UserController 对象时）直接送到你桌上。
     */
    @Autowired
    public UserController(UserService userService, Environment environment) {
        this.userService = userService;
        this.environment = environment;
    }

    /**
     * 全局异常处理器：处理用户已存在异常
     * 当用户注册时邮箱已存在时触发此异常处理
     * 
     * @param ex 用户已存在异常对象
     * @return HTTP 409 Conflict响应，包含错误信息
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        Map<String, String> response = new HashMap<>(); //建一个 “回复模板”（Map<String, String>），里面写清楚错误原因（ex.getMessage()，
        response.put("message", ex.getMessage());
        log.info("User already registered with given email!");
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * 创建新用户
     * HTTP POST /api/user
     * 
     * @param userDTO 用户数据传输对象，包含用户注册信息
     * @return 创建成功响应，包含JWT令牌
     * 每个带 @PostMapping @GetMapping 等标签的方法，就是总机处理的一项具体业务。
     * 标签 @PostMapping：对应 “新增” 操作（就像用户填了注册表单点 “提交”）。
     * 接收的 UserDTO：你之前听过的 “快递箱”，里面装着用户填的注册信息（用户名、密码、邮箱等）。
     * 流程：总机拿到这个 “箱子”，喊 userService 办事员 “帮他注册”，办事员注册成功后会返回一个 “通行证”（JWT 令牌），总机再把 “注册成功” 和通行证回传给用户。
     */
    @PostMapping//这个方法专门处理POST类型的请求”。类比：就像快递柜上的 “寄件口” 标识，只接收 “寄件” 操作，不接收 “取件”。
    public ResponseEntity<Map<String, String>> createUser(@Valid @RequestBody UserDTO userDTO) {//就是说：“我（后端）只接收 UserDTO 格式的数据包，其他格式不收”。给userDTO加的 “安检标签”，要求系统检查盒子里的内容是否符合规则（比如邮箱格式对不对、密码长度够不够），不合格就打回去。
        // 调用业务服务创建用户并获取JWT令牌
        //当用户在 APP / 网页上填完注册信息（用户名、密码、邮箱），前端程序员会把这些信息按照 UserDTO 的格式
        //把 “申请表” 递给后台办事员（userService.createUser(userDTO)），让专业的人做专业的事。
        String token = userService.createUser(userDTO);//当后端收到 UserDTO 并确认注册成功后，userService.createUser(userDTO) 这个方法会生成一个 JWT 令牌（就像景区售票员给你打印的门票）。
        Map<String, String> response = new HashMap<>();//声明一个 “键值对容器”（就像一个带格子的托盘，每个格子有标签和内容）
        response.put("message", "User created successfully");//后端把 “注册成功” 的消息和 JWT 令牌打包成响应（{"message":"成功","token":"xxx"}），发给前端。
        response.put("token", token);//后端把 JWT 放在响应里（response.put("token", token)），发给前端。前端收到后会存起来（比如存在手机本地）。
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
        //ResponseEntity：HTTP 响应的 “外壳”，包含状态码和内容。
        //status(HttpStatus.CREATED)：设置响应的 “状态码” 为201（CREATED的意思是 “创建成功”），就像快递单上盖的 “已签收” 章，告诉前端 “事办成了”。
        //body(response)：把前面整理好的response（包含消息和令牌）放进响应的 “正文” 里。
        //return：把这个打包好的响应发回给前端
    }

    /**
     * 根据用户ID获取用户信息
     * HTTP GET /api/user/{id}
     * 
     * @param id 用户ID
     * @return 用户信息响应
     * 标签 @GetMapping("/{id}")：对应 “查询” 操作，{id} 是用户的唯一编号（比如 “10086”）。
     * 流程：用户问 “查一下 ID 是 10086 的用户信息”，总机让办事员 userService 去查，查到后整理成 “回复短信”（ResponseDto）发回去，里面包含用户的具体信息（用户名、头像等）。
     */
//告诉系统这个方法专门处理GET类型的请求（就像快递柜上的 “取件口”，只处理 “查询 / 获取” 操作）。
//"/{id}"：这是请求的 “具体地址”，其中{id}是一个 “占位符”，代表用户的唯一编号（比如1001、abc123）。
//举例：如果要查 ID 为1001的用户，前端就要发请求到/api/user/1001（结合类上的@RequestMapping("/api/user")，完整地址是/api/user/{id}）。
    @GetMapping("/{id}")
    public ResponseEntity<ResponseDto> fetchUserById(@PathVariable String id) {//这个方法返回一个 “HTTP 响应包”，包里的内容是ResponseDto类型（ResponseDto是你自己定义的 “统一响应模板”，类似快递单的标准格式）。
    //@PathVariable：告诉系统 “这个参数id要从请求地址的{id}占位符里取”。比如前端请求/api/user/1001，这个注解会自动把1001取出来，赋值给变量id。  
        UserDTO userDTO = userService.getUserById(id);//调用 “用户服务办事员”（userService）的getUserById方法，传入用户 ID（id），让办事员去查询这个用户的信息。
        log.info("fetched user's username: " + userDTO.getUsername());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDto("200", "User fetched successfully", null, userDTO));//响应的 “正文内容” 是一个ResponseDto对象（你自己定义的统一响应模板），里面包含 4 个信息
    }

    /**
     * 更新用户信息（支持头像上传）
     * HTTP PUT /api/user/{id}
     * 
     * 重要步骤：
     * 1. 解析JSON格式的用户数据
     * 2. 处理可选的头像文件上传
     * 3. 调用业务服务更新用户信息
     * 4. 返回更新后的用户信息
     * 
     * @param id 用户ID
     * @param userJson JSON格式的用户数据
     * @param profilePicture 可选的头像文件
     * @return 更新结果响应
     * 标签 @PutMapping("/{id}")：对应 “修改” 操作。
     * 特殊点：支持传 “文字信息”（比如改昵称）和 “图片文件”（头像）。总机会先把文字信息拆出来（UserDTO），再和头像一起交给办事员更新，最后返回更新后的信息。
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseDto> updateUser(@PathVariable String id,//从 URL 的{id}里取用户 ID（比如1001），确定 “要修改谁的信息”。
                                                  @RequestPart("user") String userJson,//这部分数据是 JSON 格式的字符串（比如{"username":"新名字","email":"new@xx.com"}），暂时以字符串形式接收。
                                                  @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture) {
                                                    //"profilePicture"：另一部分数据的标签，表示 “用户头像文件”（比如一张.jpg 图片）。
                                                    //required = false：表示 “这部分可选”—— 用户可以只改文字信息，不上传新头像。
                                                    //MultipartFile profilePicture：接收文件的变量类型（Spring 提供的文件处理工具，类似 “装照片的小袋子”，方便操作文件）。
        try {
            // 使用Jackson将JSON字符串反序列化为UserDTO对象
            //ObjectMapper：这是一个 “JSON 翻译官”（Jackson 库提供的工具），能把 JSON 字符串转换成 Java 对象。
            //因为前端传来的用户文字信息是 JSON 字符串（userJson），后端没法直接用，所以用readValue方法把它 “翻译” 成UserDTO对象（
            ObjectMapper objectMapper = new ObjectMapper();
            UserDTO userDTO = objectMapper.readValue(userJson, UserDTO.class);
            // 1. 创建一个 ObjectMapper 对象（objectMapper），它是一个 “JSON 翻译官”，能把 JSON 字符串转换成 Java 对象。
            // 2. 调用 objectMapper 的 readValue 方法，把 userJson 这个 JSON 字符串 “翻译” 成 UserDTO 对象。
            
            // 调用业务服务更新用户信息。办事员会做这些事：去数据库找到该用户，更新文字信息，同时把新头像存到服务器（比如存到图片文件夹），最后返回一个boolean值（true表示更新成功，false表示失败）。
            boolean isUpdated = userService.updateUser(id, userDTO, profilePicture);
            
            if (isUpdated) {
                // 更新成功，获取更新后的用户信息
                UserDTO updatedUser = userService.getUserById(id);//先查一次数据库，获取更新后的完整用户信息（updatedUser），确保返回给前端的是最新数据。
                log.info("Update successful!" + updatedUser);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(new ResponseDto("200", "User updated successfully", null, updatedUser));
            } else {
                // 更新失败
                return ResponseEntity
                        .status(HttpStatus.EXPECTATION_FAILED)//返回状态码417（EXPECTATION_FAILED，HTTP 规范的 “预期失败”），告诉前端 “修改没成功”（可能是数据库出问题，或者用户 ID 不存在）。
                        .body(new ResponseDto("417", "Failed to update user", null, null));
            }
        } catch (IOException e) {//IOException：可能发生的错误，比如：
            //userJson格式不对，ObjectMapper翻译失败；
            //头像文件太大 / 格式不对，上传出错。
            // 处理JSON解析或文件处理异常
            log.error("Error updating user profile picture", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto("500", "Error updating user profile picture", e.getMessage(), null));//这时返回状态码500（INTERNAL_SERVER_ERROR，HTTP 规范的 “服务器内部错误”）
        }
    }

    /**
     * 删除用户
     * HTTP DELETE /api/user/{id}
     * 
     * @param id 用户ID
     * @return 删除结果响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDto> deleteUser(@PathVariable String id) {
        boolean isDeleted = userService.deleteUserById(id);
        if (isDeleted) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto("200", "User deleted successfully", "", null));
        } else {
            return ResponseEntity
                    .status(HttpStatus.EXPECTATION_FAILED)
                    .body(new ResponseDto("417", "Failed to delete user", "", null));
        }
    }

    // ==================== 支付卡管理相关API ====================

    /**
     * 根据卡ID获取支付卡信息
     * HTTP GET /api/user/card/{cardId}
     * 
     * @param cardId 支付卡ID
     * @return 支付卡信息
     */
    @GetMapping("/card/{cardId}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable Long cardId) {
        CardDTO cardDTO = userService.getCardById(cardId);
        return ResponseEntity.ok(cardDTO);
    }

    /**
     * 获取用户的所有支付卡
     * HTTP GET /api/user/{id}/card
     * 
     * @param id 用户ID
     * @return 用户支付卡列表
     */
    @GetMapping("/{id}/card")
    public ResponseEntity<List<CardDTO>> getAllCards(@PathVariable String id) {
        List<CardDTO> cards = userService.getCardsByUserId(id);
        return ResponseEntity.status(HttpStatus.OK).body(cards);
    }

    /**
     * 为用户创建新的支付卡
     * HTTP POST /api/user/{id}/card
     * 
     * @param id 用户ID
     * @param cardDTO 支付卡信息
     * @return 创建的支付卡信息
     */
    @PostMapping("/{id}/card")
    public ResponseEntity<CardDTO> createCard(@PathVariable String id, @RequestBody CardDTO cardDTO) {
        CardDTO createdCard = userService.createCard(id, cardDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    /**
     * 更新支付卡信息
     * HTTP PUT /api/user/{id}/card/{cardId}
     * 
     * @param id 用户ID
     * @param cardId 支付卡ID
     * @param cardDTO 更新的支付卡信息
     * @return 更新后的支付卡信息
     */
    @PutMapping("/{id}/card/{cardId}")
    public ResponseEntity<CardDTO> updateCard(@PathVariable String id, @PathVariable Long cardId, @RequestBody CardDTO cardDTO) {
        try {
            CardDTO updatedCard = userService.updateCard(cardId, cardDTO);
            return ResponseEntity.status(HttpStatus.OK).body(updatedCard);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * 删除支付卡
     * HTTP DELETE /api/user/{id}/card/{cardId}
     * 
     * @param id 用户ID
     * @param cardId 支付卡ID
     * @return 删除结果（无内容响应）
     */
    @DeleteMapping("/{id}/card/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable String id, @PathVariable Long cardId) {
        try {
            userService.deleteCard(cardId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // ==================== 测试相关API ====================

    /**
     * 获取Java版本信息（用于测试）
     * HTTP GET /api/user/java-version
     * 
     * @return Java环境信息
     */
    @GetMapping("/java-version")
    public ResponseEntity<String> getJavaVersion() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(environment.getProperty("JAVA_HOME"));
    }
}

// 用户服务
// 1. 应用启动 → UserApplication.java
//     ↓
// 2. HTTP请求 → UserController.java
//     ↓
// 3. 业务处理 → UserService.java
//     ↓
// 4. 数据操作 → UserRepository.java
//     ↓
// 5. 实体映射 → User.java + BaseEntity.java
//     ↓
// 6. 身份认证 → KeycloakService.java
//     ↓
// 7. 令牌处理 → JwtTokenProvider.java
//     ↓
// 8. 文件存储 → CloudinaryService.java
//     ↓
// 9. 数据转换 → UserMapper.java
//     ↓
// 10. 异常处理 → UserAlreadyExistsException.java
//     ↓
// 11. 审计记录 → AuditAwareImpl.java
