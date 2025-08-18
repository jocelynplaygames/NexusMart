import React, { useState } from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, ButtonBase, Avatar  } from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import LoginDialog from '../LoginDialog/LoginDialog';
// 是LoginDialog的父组件
// 用户点击 Header 中的 “Login” 按钮 → 父组件 handleLoginOpen 触发 → loginOpen = true → 子组件 LoginDialog 因 open={true} 显示。
// 用户在 LoginDialog 中完成操作（如登录成功、点击 “Cancel”）→ 子组件调用 onClose（即父组件的 handleLoginClose）→ 父组件 loginOpen = false → 子组件因 open={false} 隐藏。
const Header = () => {
    const sampleAvatarUrl = "https://via.placeholder.com/150";//一个示例头像图片地址，当用户未上传头像时，会用这个占位图显示
    const [loginOpen, setLoginOpen] = useState(false);//通过 React 的 useState 钩子，创建了两个关键东西，一个状态、一个更新状态的函数
    const { isAuthenticated, user } = useSelector(state => state.auth); //从 Redux 获取用户登录状态（isAuthenticated）和用户信息（user），用于动态显示导航栏内容
    //useSelector是 Redux 提供的一个钩子（Hook），专门用于从 Redux 的全局状态（state）中获取组件需要的数据。
    // 根据用户是否登录（isAuthenticated），在导航栏显示不同内容（比如未登录显示 “Login” 按钮，已登录显示头像、购物车等）
    // Assuming auth state contains isAuthenticated and user info
    const dispatch = useDispatch();
// 传递给子组件时用open：open={open}
// loginOpen 相当于 “灯的状态”（亮 / 灭）。
// setLoginOpen 相当于 “灯的开关按钮”（按一下开灯，再按一下关灯）。
// handleLoginOpen 相当于 “专门用来开灯的快捷方式”（比如床头的开灯按钮，只负责开灯）。
    const handleLoginOpen = () => {//自定义函数，作用很简单：当被调用时，就打开登录弹窗。它被绑定到 “Login” 按钮的点击事件上。
    // 用户点击 “Login” 按钮 → 触发 handleLoginOpen → 调用 setLoginOpen(true) → loginOpen 变成 true → 弹窗显示。
        setLoginOpen(true);
    };

    const handleLoginClose = () => {// handleLoginClose 方法通过 setLoginOpen(false) 将 loginOpen 设为 false
        setLoginOpen(false);
    };

    const navigate = useNavigate();

    return (
        <AppBar sx={{ backgroundColor: '#5696fc', width: '100vw', padding: '0px' }}>
            <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <ButtonBase onClick={() => navigate('/')} sx={{ flexGrow: 1, textAlign: 'left', maxWidth: '250px' }}>
                    <Typography variant="h6">
                        NexusMart Ecommerce Site
                    </Typography>
                </ButtonBase>
                <div>
                    {!isAuthenticated && (// 如果用户未登录（isAuthenticated为false）,显示“登录”按钮
                        <Button color="inherit" onClick={handleLoginOpen}>
                            Login
                        </Button>
                    )}
                    {isAuthenticated && (// 如果用户已登录（isAuthenticated为true）,显示“Sell”按钮、购物车图标、用户头像
                        <>
                            <Button color="inherit" onClick={() => navigate('/sell')}>
                                Sell
                            </Button>
                            <IconButton color="inherit" onClick={() => navigate('/cart')}>
                                <ShoppingCartIcon />
                            </IconButton>
                            <IconButton color="inherit" onClick={() => navigate('/profile')}>
                                <Avatar src={user?.profilePictureUrl || sampleAvatarUrl} alt="User Avatar" />
                            </IconButton>
                        </>
                    )}
{/*本父组件在渲染 LoginDialog 时，将 loginOpen 作为 open 属性传入，直接控制子组件LoginDialog的显示状态
在导航栏（AppBar）中嵌入登录弹窗（LoginDialog）
将父组件的 loginOpen 状态传递给子组件的 open 属性
将父组件的 handleLoginClose 方法传递给子组件的 onClose 属性,让子组件在需要关闭时（比如点击 “Cancel”）能通知父组件更新状态。
*/}
                </div>
            </Toolbar>
            <LoginDialog open={loginOpen} onClose=
             />
        </AppBar>
    );
};

export default Header;

// 父组件：Header（页面顶部的导航栏，包含 “Login” 按钮）。
// 子组件：LoginDialog（登录弹窗，被Header组件使用）。

// 1.一开始：爸爸loginOpen=false → 传给儿子open=false → 儿子隐藏（不显示）。
// 2.用户点击爸爸的 “Login” 按钮：爸爸调用setLoginOpen(true) → loginOpen=true → 传给儿子open=true → 儿子显示（弹窗打开）。
// 3.用户在儿子那里点击 “Cancel”：儿子调用onClose（即爸爸的handleLoginClose）→ 爸爸setLoginOpen(false) → 传给儿子open=false → 儿子隐藏（弹窗关闭）。

// 1. 一开始：爸爸loginOpen=false → 传给儿子open=false → 儿子隐藏
// // 爸爸（Header组件）中：初始状态定义
// const [loginOpen, setLoginOpen] = useState(false); // ① 爸爸的初始状态是false

// // 爸爸调用儿子时，把loginOpen传给儿子的open属性
// <LoginDialog open={loginOpen} onClose={handleLoginClose} /> // ② 传给儿子open=false

// // 儿子（LoginDialog组件）中：Dialog组件的open属性控制显示
// <Dialog
//   open={open} // ③ 儿子接收的open是false → 弹窗隐藏



// 2. 用户点击爸爸的 “Login” 按钮 → 弹窗打开
// // 爸爸（Header组件）中：“Login”按钮绑定点击事件
// <Button color="inherit" onClick={handleLoginOpen}> // ① 用户点击按钮
//   Login
// </Button>

// // 爸爸中定义的handleLoginOpen方法
// const handleLoginOpen = () => {
//   setLoginOpen(true); // ② 爸爸调用setLoginOpen，把loginOpen改成true
// };

// // 爸爸传给儿子的open属性此时变成true
// <LoginDialog open={loginOpen} ... /> // ③ 此时loginOpen是true → 传给儿子open=true

// // 儿子（LoginDialog组件）中：Dialog接收open=true
// <Dialog
//   open={open} // ④ 儿子的open是true → 弹窗显示




// 3. 用户点击儿子的 “Cancel” → 弹窗关闭
// // 儿子（LoginDialog组件）中：“Cancel”按钮绑定点击事件
// <Button onClick={onClose} color="primary"> // ① 用户点击Cancel按钮
//   Cancel
// </Button>

// // 儿子调用的onClose，是爸爸传过来的handleLoginClose方法
// // 爸爸（Header组件）中定义的handleLoginClose
// const handleLoginClose = () => {
//   setLoginOpen(false); // ② 爸爸调用setLoginOpen，把loginOpen改成false
// };

// // 爸爸传给儿子的open属性此时变回false
// <LoginDialog open={loginOpen} ... /> // ③ 此时loginOpen是false → 传给儿子open=false

// // 儿子（LoginDialog组件）中：Dialog接收open=false
// <Dialog
//   open={open} // ④ 儿子的open是false → 弹窗隐藏
//   ...
// ></Dialog>