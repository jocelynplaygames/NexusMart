// File: frontend/src/App/service/OrderService.js
import axiosInstance from "./AxiosConfig";
//axiosInstance：预先配置好的 axios 实例（可能包含基础 URL、默认请求头、拦截器等）
import store from "../redux/store/store";
//store：Redux 的状态管理容器，用于获取用户登录后的令牌（token），实现身份验证


// 前端API调用服务，与后端通信;HTTP请求封装、数据格式转换、错误处理
export const createOrder = async (cartId, shippingMethodId, cardId) => {
    console.log("Create Order " + cartId + " " + shippingMethodId + " " + cardId);
    try {
        const response = await axiosInstance.post('/api/orders', {
            //用axiosInstance.post发送 POST 请求到/api/orders接口
            cartId,
            shippingMethodId,
            cardId
        });
        

        return response.data; //返回订单列表数据（包含每个订单的 ID、金额、状态等）
    } catch (error) {
        console.error('Error creating order:', error);
        throw error;
    }
};
//前端将cartId、shippingMethodId、cardId以 JSON 格式放在请求体中，发送到后端/api/orders接口。
//后端的OrderController（控制器）会接收这个 POST 请求，然后调用OrderService的createOrder方法处理业务：


export const fetchUserOrders = async (userId) => {
    try {
        const response = await axiosInstance.get(`/api/orders/user/${userId}`);
        //用axiosInstance.get发送 GET 请求到/api/orders/user/${userId}接口
        return response.data;
    } catch (error) {
        console.error('Error fetching user orders:', error);
        throw error;
    }
};
//前端发送GET /api/orders/user/123（userId=123）。
//后端OrderService.getOrdersByUser("123")查询该用户的所有订单，转换为OrderDTO列表返回。
//前端接收订单列表数据，用于渲染 “我的订单” 页面。


export const fetchOrder = async (orderId) => {
    const state = store.getState(); // 获取Redux中的全局状态
    const token = state.auth.token; // 从状态中提取用户登录令牌（token）

    try {
        const response = await axiosInstance.get(`/api/orders/${orderId}`, {
            headers: {//在请求头中添加
                Authorization: `Bearer ${token}`
                // 格式为 "Bearer 令牌值"，是后端通常要求的认证格式
            }
        });
        return response.data;

    } catch (error) {
        console.error('Failed to fetch order:', error);
        throw error;
    }

//Spring Security OAuth2 Resource Server 机制， JWT token 的拦截、验证流程会由 Spring Security 自动完成，无需手动编写拦截器。
// 当前端发送带有Authorization: Bearer ${token}的请求（如GET /api/orders/789）时，Spring Security 会自动执行以下步骤：
// Spring Security 的JwtAuthenticationFilter（过滤器链中的一环）会自动拦截所有请求
// 检查请求头中是否包含Authorization: Bearer xxx。从请求头中提取 JWT token。
// 根据SecurityConfig中的配置（如jwkSetUri或signingKey）验证 token 的签名、 token 的过期时间（exp字段）、签发者（iss字段）
// 验证通过后，Spring Security 会解析 JWT 中的用户信息（如sub字段通常为用户 ID），创建JwtAuthenticationToken对象，存入SecurityContextHolder（安全上下文）。
// 接口 OrderService.java中OrderDTO.getOrder(orderId)可以通过SecurityContextHolder获取当前用户信息
// 前端接收数据，渲染 “订单详情页”。
};