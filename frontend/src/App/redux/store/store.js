// File: frontend/src/App/redux/store/store.js
import { configureStore } from '@reduxjs/toolkit';
//各个 reducer：对应不同功能的 “状态处理器”
//（如 authReducer 处理用户认证状态，cartReducer 处理购物车状态）
import snackbarReducer from '../slice/snackbarSlice';
import windowSizeReducer, { windowSizeActions } from '../slice/windowSizeSlice';
import authReducer from '../slice/authSlice';
import cartReducer from '../slice/cartSlice';
//
// store.js 文件是 Redux 的全局状态仓库配置文件
// 用于创建和导出整个应用的 Redux 存储（store），集中管理应用的所有全局状态
const store = configureStore({
    reducer: {// reducer 字段用于注册应用的所有状态切片，形成一个 “状态树”
        snackbar: snackbarReducer,
        windowSize: windowSizeReducer,
        auth: authReducer,//管理用户认证状态（如 isAuthenticated、user 信息）的状态
        cart: cartReducer
    },
});

store.dispatch(windowSizeActions.startListeningToResize());
//初始化窗口大小监听,初始化时就获取当前窗口大小并同步到 Redux 状态
// 后续窗口 resize 时自动更新状态
export default store;
//将创建好的 store 导出，供整个应用使用。
//通常会在应用的入口文件（如 index.js）中通过 Provider 组件将 store 注入应用

//使用：
// store.getState() 是 Redux 提供的方法，用于获取当前 Redux 中存储的所有全局状态
// 返回一个包含所有切片（slice）状态的对象。
// const token = state.auth.token;：提取认证令牌，从 auth 切片中提取 token 字段
// 这个 token 通常是用户登录成功后，后端返回的身份认证令牌（如 JWT）

// store 是容器
// 存储整个应用的全局状态（state）。
// 提供方法操作状态：比如 dispatch（触发 action 来修改状态）、getState（获取当前状态）、subscribe（监听状态变化）等。
// state 是容器里的内容，反映了应用在某一时刻的状态

// state 的结构（对应store中注册的4个切片）
// {
//     snackbar: { open: false, message: '', severity: 'info' }, // 通知状态
//     windowSize: { width: 1920, height: 1080 }, // 窗口尺寸状态
//     auth: { 
//       isAuthenticated: true, 
//       user: { email: 'user@example.com' }, 
//       token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' // 用户令牌
//     }, // 认证状态
//     cart: { items: [], total: 0 } // 购物车状态
//   }