// File: frontend/src/App/Auth/AuthContext.js
import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';
import { keycloakInstance, KEYCLOAK_URL, KEYCLOAK_REALM, KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET } from './keycloak';
import { useDispatch } from 'react-redux';
import { setAuthenticated, logout as reduxLogout } from '../redux/slice/authSlice';
import { decodeToken } from './JwtUtils';

// 创建认证上下文，用于在组件树中共享认证状态
const AuthContext = createContext();

/**
 * 认证提供者组件 (Authentication Provider Component)
 * 
 * 作用：为整个应用提供认证相关的状态和方法
 * 包装：通常包装在应用的根组件外层
 * 
 * @param {React.ReactNode} children - 子组件
 */
export const AuthProvider = ({ children }) => {
    // 认证状态管理
    const [isAuthenticated, setIsAuthenticated] = useState(false); // 用户是否已认证
    const [token, setToken] = useState(localStorage.getItem('token')); // JWT访问令牌
    const [refreshToken, setRefreshToken] = useState(localStorage.getItem('refreshToken')); // 刷新令牌
    const [loading, setLoading] = useState(true); // 初始化加载状态
    
    const dispatch = useDispatch(); // Redux状态分发器

    /**
     * 令牌刷新函数 (Token Refresh Function)
     * 
     * 功能：使用刷新令牌获取新的访问令牌
     * 触发时机：访问令牌即将过期时自动调用
     * 安全机制：防止用户会话意外中断
     * 
     * 执行流程：
     * 1. 检查是否有可用的刷新令牌
     * 2. 向Keycloak发送刷新请求
     * 3. 更新本地存储和状态
     * 4. 设置下一次刷新定时器
     */
    const refresh = async () => {
        const currentRefreshToken = localStorage.getItem('refreshToken');
        if (!currentRefreshToken) {
            console.error('No refresh token available');
            return;
        }

        console.log('token refreshed!');
        try {
            // 向Keycloak发送令牌刷新请求
            const response = await axios.post(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, new URLSearchParams({
                client_id: KEYCLOAK_CLIENT_ID,
                client_secret: KEYCLOAK_CLIENT_SECRET,
                grant_type: 'refresh_token', // 指定使用刷新令牌模式
                refresh_token: currentRefreshToken
            }), {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });

            if (response.status === 200) {
                // 刷新成功，更新令牌信息
                const newToken = response.data.access_token;
                const newRefreshToken = response.data.refresh_token;
                
                // 更新状态和本地存储
                setToken(newToken);
                setRefreshToken(newRefreshToken);
                localStorage.setItem('token', newToken);
                localStorage.setItem('refreshToken', newRefreshToken);
                dispatch(setAuthenticated({ isAuthenticated: true, token: newToken }));

                // 解析令牌并设置下次刷新定时器
                const tokenParsed = decodeToken(newToken);
                if (tokenParsed) {
                    // 在令牌过期前1分钟刷新
                    const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                    setTimeout(refresh, timeout);
                }
            } else {
                console.error('Token refresh failed', response.data);
                handleLogout(false); // 刷新失败时自动登出
            }
        } catch (error) {
            console.error('Error refreshing token', error);
            handleLogout(false); // 异常时自动登出
        }
    };

    /**
     * 初始化Keycloak认证系统
     * 
     * 执行时机：组件挂载时自动执行
     * 主要任务：
     * 1. 检查本地存储的令牌是否有效
     * 2. 初始化Keycloak实例
     * 3. 设置令牌自动刷新机制
     * 4. 同步认证状态到Redux
     */
    useEffect(() => {
        const initializeKeycloak = async () => {
            // 如果本地已有令牌，直接使用
            if (token && refreshToken) {
                keycloakInstance.token = token;
                keycloakInstance.refreshToken = refreshToken;
                setIsAuthenticated(true);
                setLoading(false);
                dispatch(setAuthenticated({ isAuthenticated: true, token }));

                // 设置令牌刷新定时器
                const tokenParsed = decodeToken(token);
                if (tokenParsed) {
                    const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                    setTimeout(refresh, timeout);
                }
            } else {
                // 本地无令牌，尝试SSO登录检查
                try {
                    const authenticated = await keycloakInstance.init({ onLoad: 'check-sso' });
                    if (authenticated) {
                        // SSO登录成功，获取新令牌
                        const newToken = keycloakInstance.token;
                        const newRefreshToken = keycloakInstance.refreshToken;
                        setToken(newToken);
                        setRefreshToken(newRefreshToken);
                        localStorage.setItem('token', newToken);
                        localStorage.setItem('refreshToken', newRefreshToken);
                        setIsAuthenticated(true);
                        dispatch(setAuthenticated({ isAuthenticated: true, token: newToken }));

                    } else {
                        // SSO检查失败，清除认证状态
                        handleLogout(false);
                    }
                    setLoading(false);
                    console.log('Keycloak initialized!');
                } catch (error) {
                    console.error('Error initializing Keycloak', error);
                    setLoading(false);
                }
            }
            
            // 设置令牌刷新定时器
            const tokenParsed = decodeToken(token);
            if (tokenParsed) {
                const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                setTimeout(refresh, timeout);
            }
        };

        initializeKeycloak();
    }, [dispatch]);

    /**
     * 处理用户登出逻辑 (Handle Logout Logic)
     * 
     * 功能：清除所有认证相关的状态和数据
     * 清理内容：
     * - 本地状态（认证状态、令牌）
     * - 本地存储（localStorage）
     * - Redux状态
     * 
     * @param {boolean} shouldReload - 是否刷新页面
     */
    const handleLogout = (shouldReload) => {
        setIsAuthenticated(false);
        setToken(null);
        setRefreshToken(null);
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        dispatch(reduxLogout());
        if (shouldReload) {
            window.location.reload();
        }
    };

    /**
     * 用户登录方法 (User Login Method)
     * 
     * 认证流程：
     * 1. 向Keycloak发送用户名密码
     * 2. 获取访问令牌和刷新令牌
     * 3. 保存令牌到本地存储
     * 4. 更新认证状态
     * 5. 设置令牌自动刷新
     * 
     * 安全特性：
     * - 使用OAuth2密码模式
     * - 令牌自动刷新机制
     * - 本地状态与Redux同步
     * 
     * @param {string} username - 用户名/邮箱
     * @param {string} password - 密码
     * @returns {Promise<void>}
     */
    const login = async (username, password) => {
        // 向Keycloak发送登录请求
        const response = await axios.post(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, new URLSearchParams({
            client_id: KEYCLOAK_CLIENT_ID,
            client_secret: KEYCLOAK_CLIENT_SECRET,
            username,
            password,
            grant_type: 'password' // OAuth2密码模式
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        });

        if (response.status === 200) {
            // 登录成功，处理令牌
            const newToken = response.data.access_token;
            const newRefreshToken = response.data.refresh_token;
            
            // 更新状态和存储
            setToken(newToken);
            setRefreshToken(newRefreshToken);
            localStorage.setItem('token', newToken);
            localStorage.setItem('refreshToken', newRefreshToken);
            setIsAuthenticated(true);
            dispatch(setAuthenticated({ isAuthenticated: true, token: newToken }));
            
            // 设置令牌自动刷新
            const tokenParsed = decodeToken(newToken);
            if (tokenParsed) {
                const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                setTimeout(refresh, timeout);
            }
        } else {
            console.error('Login failed', response.data);
        }
    };

    /**
     * 用户登出方法 (User Logout Method)
     * 
     * 功能：主动登出用户
     * 执行步骤：
     * 1. 向Keycloak发送登出请求
     * 2. 清除本地认证状态
     * 3. 刷新页面确保状态一致
     * 
     * @returns {Promise<void>}
     */
    const logout = async () => {
        // 向Keycloak发送登出请求
        await axios.post(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/logout`, new URLSearchParams({
            client_id: KEYCLOAK_CLIENT_ID,
            client_secret: KEYCLOAK_CLIENT_SECRET,
            refresh_token: refreshToken
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });

        // 清除本地状态并刷新页面
        handleLogout(true);
    };

    // 提供认证上下文值，包含状态和方法
    return (
        <AuthContext.Provider value={{ 
            isAuthenticated,  // 认证状态
            token,            // 访问令牌
            login,            // 登录方法
            logout,           // 登出方法
            loading,          // 加载状态
            refresh,          // 令牌刷新方法
            handleLogout      // 内部登出处理方法
        }}>
            {children}
        </AuthContext.Provider>
    );
};

/**
 * 认证上下文钩子 (Authentication Context Hook)
 * 
 * 使用方式：const { login, logout, isAuthenticated } = useAuth();
 * 返回值：包含所有认证相关状态和方法的对象
 * 
 * @returns {Object} 认证上下文值
 */
export const useAuth = () => useContext(AuthContext);