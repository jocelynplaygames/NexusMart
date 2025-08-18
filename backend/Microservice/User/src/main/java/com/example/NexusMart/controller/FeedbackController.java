package com.example.NexusMart.controller;

// 导入必要的依赖包
import com.example.NexusMart.dto.FeedbackDTO;       // 反馈数据传输对象
import com.example.NexusMart.jwt.JwtTokenProvider;  
import com.example.NexusMart.service.FeedbackService; // 反馈业务服务层
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;          
import org.springframework.http.ResponseEntity;     
import org.springframework.web.bind.annotation.*;   
import java.util.List;

/**
 * 反馈评价管理控制器
 * 负责处理用户反馈和评价相关的HTTP请求，包括反馈的CRUD操作
 * 
 * 重要功能：
 * 1. 根据商品ID获取反馈列表
 * 2. 根据用户ID获取反馈列表
 * 3. 根据卖家ID获取反馈列表
 * 4. 创建新的反馈评价
 * 5. 更新反馈评价
 * 6. 删除反馈评价
 * 
 * 业务逻辑：
 * - 用户可以对购买的商品进行评价
 * - 评价包含评分和评论内容
 * - 支持反馈的修改和删除
 * - 通过JWT令牌验证用户身份
 * 
 * @RestController: 标识这是一个REST API控制器
 * @RequestMapping: 定义API的基础路径
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    // 依赖注入：反馈业务服务层
    private final FeedbackService feedbackService;
    
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 构造函数依赖注入
     * @param feedbackService 反馈业务服务
     * @param jwtTokenProvider JWT令牌提供者
     */
    @Autowired
    public FeedbackController(FeedbackService feedbackService, JwtTokenProvider jwtTokenProvider) {
        this.feedbackService = feedbackService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 根据商品ID获取反馈列表
     * HTTP GET /api/feedback/item/{itemId}
     * 
     * 功能说明：
     * - 获取指定商品的所有用户反馈
     * - 用于商品详情页面显示评价信息
     * - 帮助其他用户了解商品质量
     * 
     * @param itemId 商品ID
     * @return 该商品的反馈列表
     */
    @GetMapping("/item/{itemId}")
    public List<FeedbackDTO> getFeedbacksByItem(@PathVariable Long itemId) {
        return feedbackService.getFeedbacksByItemId(itemId);
    }

    /**
     * 根据用户ID获取反馈列表
     * HTTP GET /api/feedback/user/{userId}
     * 
     * 功能说明：
     * - 获取指定用户发布的所有反馈
     * - 用于用户个人中心显示评价历史
     * - 帮助用户管理自己的评价记录
     * 
     * @param userId 用户ID
     * @return 该用户的反馈列表
     */
    @GetMapping("/user/{userId}")
    public List<FeedbackDTO> getFeedbacksByUser(@PathVariable String userId) {
        return feedbackService.getFeedbacksByUserId(userId);
    }

    /**
     * 根据卖家ID获取反馈列表
     * HTTP GET /api/feedback/seller/{sellerId}
     * 
     * 功能说明：
     * - 获取指定卖家收到的所有反馈
     * - 用于卖家个人中心显示收到的评价
     * - 帮助卖家了解自己的服务质量和用户满意度
     * 
     * @param sellerId 卖家ID
     * @return 该卖家收到的反馈列表
     */
    @GetMapping("/seller/{sellerId}")
    public List<FeedbackDTO> getFeedbacksBySeller(@PathVariable String sellerId) {
        return feedbackService.getFeedbacksBySellerId(sellerId);
    }

    /**
     * 为商品创建新的反馈评价
     * HTTP POST /api/feedback/item/{itemId}
     * 
     * 重要步骤：
     * 1. 从JWT令牌中提取用户ID（验证用户身份）
     * 2. 调用业务服务创建反馈
     * 3. 返回创建的反馈信息
     * 
     * 业务规则：
     * - 只有已购买商品的用户才能评价
     * - 评价包含评分（1-5星）和评论内容
     * - 评价会影响卖家的整体评分
     * 
     * @param itemId 商品ID
     * @param feedbackDTO 反馈数据传输对象（包含评分和评论）
     * @param token JWT授权令牌
     * @return 创建的反馈信息
     */
    @PostMapping("/item/{itemId}")
    public ResponseEntity<FeedbackDTO> postFeedback(@PathVariable Long itemId, @RequestBody FeedbackDTO feedbackDTO, @RequestHeader("Authorization") String token) {
        
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        
        // 调用业务服务创建反馈评价
        FeedbackDTO createdFeedback = feedbackService.createFeedback(itemId, feedbackDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFeedback);
    }

    /**
     * 更新反馈评价
     * HTTP PUT /api/feedback/{feedbackId}
     * 
     * 重要步骤：
     * 1. 从JWT令牌中提取用户ID
     * 2. 验证用户是否有权限修改该反馈
     * 3. 调用业务服务更新反馈
     * 4. 返回更新后的反馈信息
     * 
     * 权限控制：
     * - 只有反馈的创建者才能修改
     * - 系统会验证用户身份和权限
     * 
     * @param feedbackId 反馈ID
     * @param feedbackDTO 更新的反馈信息
     * @param token JWT授权令牌
     * @return 更新后的反馈信息
     */
    @PutMapping("/{feedbackId}")
    public ResponseEntity<FeedbackDTO> updateFeedback(@PathVariable Long feedbackId, @RequestBody FeedbackDTO feedbackDTO, @RequestHeader("Authorization") String token) {
        
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        
        // 调用业务服务更新反馈（业务层会验证用户权限）
        FeedbackDTO updatedFeedback = feedbackService.updateFeedback(feedbackId, feedbackDTO, userId);
        return ResponseEntity.ok(updatedFeedback);
    }

    /**
     * 删除反馈评价
     * HTTP DELETE /api/feedback/{feedbackId}
     * 
     * 重要步骤：
     * 1. 从JWT令牌中提取用户ID
     * 2. 验证用户是否有权限删除该反馈
     * 3. 调用业务服务删除反馈
     * 4. 返回删除成功响应
     * 
     * 权限控制：
     * - 只有反馈的创建者才能删除
     * - 系统会验证用户身份和权限
     * - 删除操作不可逆，需要谨慎处理
     * 
     * @param feedbackId 反馈ID
     * @param token JWT授权令牌
     * @return 删除结果（无内容响应）
     */
    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long feedbackId, @RequestHeader("Authorization") String token) {
        
        String userId = jwtTokenProvider.extractUserIdFromToken(token.replace("Bearer ", ""));
        
        // 调用业务服务删除反馈（业务层会验证用户权限）
        feedbackService.deleteFeedback(feedbackId, userId);
        return ResponseEntity.noContent().build();
    }
}
