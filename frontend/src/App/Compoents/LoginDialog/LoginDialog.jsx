// File: frontend/src/App/Compoents/LoginDialog/LoginDialog.jsx
import React, { useState } from 'react';
// Material-UI组件库导入
import { Dialog, DialogActions, DialogContent, DialogTitle, Button, TextField, Tabs, Tab, Box } from '@mui/material';
// Redux状态管理
import { useDispatch } from 'react-redux';
// 表单处理库
import { useFormik } from 'formik';
// 表单验证库
import * as Yup from 'yup';
// 自定义认证上下文
import { useAuth } from '../../Auth/AuthContext';
// Redux通知组件
import { showSnackbar } from '../../redux/slice/snackbarSlice';
// HTTP客户端配置
import axiosInstance from '../../service/AxiosConfig';

/**
 * 登录/注册对话框组件
 * 
 * 功能说明：
 * - 提供用户登录和注册的统一界面
 * - 使用标签页切换登录和注册表单
 * - 集成表单验证和错误处理
 * - 支持用户注册后自动登录
 * 
 * 技术特点：
 * - 使用Material-UI构建现代化UI
 * - 使用Formik进行表单状态管理
 * - 使用Yup进行表单验证
 * - 集成Redux进行状态管理
 * - 使用自定义认证上下文
 * 
 * @param {boolean} open - 对话框是否打开
 * @param {function} onClose - 关闭对话框的回调函数
 */
//一个React 登录 / 注册对话框组件，作用是在前端提供用户登录和注册的交互界面，与后端的用户认证逻辑（比如之前讲的UserService的createUser方法）配合，完成用户注册和登录流程
const LoginDialog = ({ open, onClose }) => {
    //open：控制对话框是否显示（true显示，false隐藏，相当于 “打开 / 关闭登录窗口” 的开关）。父组件通过修改 open 的值来控制对话框的显示 / 隐藏
    //onClose：关闭对话框的回调函数（点击 “取消” 或成功登录后，调用这个函数关闭窗口）。

//通过 React 和 Redux 的基础工具（useState、useAuth、useDispatch）

    // 当前选中的标签页索引（0=登录，1=注册）
    const [tabIndex, setTabIndex] = useState(0);
    //tabIndex：存储当前选中的标签页索引（0代表 “登录”，1代表 “注册”）。
    //当用户点击 “Sign Up” 标签时，会触发handleTabChange方法，通过setTabIndex(1)将状态改为 1，从而切换到注册表单：
    //useState(0)：这是 React 的状态钩子，用于在函数组件中定义一个 “可变化的状态”。初始值0表示默认选中第一个标签页（登录标签）。


// 新建了一个 React 状态变量 tabIndex，用于记录当前选中的标签页（0 代表 “登录”，1 代表 “注册”）。
// 同时新建了一个状态更新函数 setTabIndex，用于修改 tabIndex 的值。
// 使用了 React 的内置钩子 useState，这是 React 用于在函数组件中定义 “可变化状态” 的基础方法。
// useState(0) 中的 0 是初始值，表示默认选中第一个标签页（登录）。

    // 从认证上下文获取登录方法
    //useAuth()：这是一个自定义的 “认证上下文钩子”，封装了整个应用的登录逻辑（与后端 Keycloak 认证、JWT 令牌存储等相关）。
    //login方法接收用户输入的邮箱和密码，发起登录请求并处理结果（比如存储令牌、更新登录状态）。
    const { login } = useAuth();

    // Redux dispatch函数
    //useDispatch()：这是 Redux 的钩子，用于获取 “状态更新函数”（dispatch），用于触发 Redux 中的 “动作”（Action）。结合导入的showSnackbar（Redux 动作创建器），dispatch用于触发 “显示通知” 的状态更新。
    //dispatch：具体作用是将动作（比如 “显示登录成功提示”）发送给 Redux 的 reducer，从而更新全局状态。在登录 / 注册成功或失败时，通过dispatch显示对应的通知
    const dispatch = useDispatch();
// 新建了 dispatch 函数，用于触发 Redux 的状态更新（向 Redux store 发送 “动作”）。
// 使用了 Redux 的内置钩子 useDispatch()，这是 Redux 提供的用于在组件中获取 “状态更新器” 的方法。
// 后续通过 dispatch(showSnackbar(...)) 调用，用于触发全局通知（如登录成功提示、注册失败警告）


    /**
     * 处理标签页切换
     * @param {object} event - 事件对象
     * @param {number} newValue - 新的标签页索引
     */
    //用户点击 “Login” 或 “Sign Up” 标签时，切换显示对应的表单（就像切换手机 APP 的登录 / 注册页面）。
    const handleTabChange = (event, newValue) => {
// event：事件对象（自动传递，包含点击事件的详细信息，比如点击的元素、时间等，这里暂时直接使用但需要接收）。
// newValue：新的标签页索引（由 Material-UI 的Tabs组件自动传递，0代表 “登录” 标签，1代表 “注册” 标签）。
        setTabIndex(newValue);// 调用setTabIndex（由useState生成的状态更新函数,上文有定义），将tabIndex的值更新为newValue（新标签页的索引）。
    };

    /**
     * 处理用户登录
     * 
     * 重要步骤：
     * 1. 调用认证上下文的login方法
     * 2. 显示成功通知
     * 3. 关闭对话框
     * 4. 错误处理和用户提示
     * 
     * @param {object} values - 表单值（email, password）
     */
    const handleLogin = async (values) => {//标记这是一个异步函数（因为登录需要调用后端接口，等待响应
// 函数内部的代码不会阻塞整个程序的运行（比如等待登录请求时，浏览器可以继续做其他事）。
// 可以使用 await 关键字暂停函数执行，等待异步操作完成后再继续。
        //values：参数是 Formik 传递的 “用户输入的表单数据”（来自formikLogin的values，包含email和password，比如{ email: "user@test.com", password: "123456" }）。
        try {
            // 调用login方法（来自useAuth），传入邮箱和密码，底层会调用后端登录接口（比如通过 Keycloak 获取 JWT 令牌）。
            //login方法内部会做这些事（结合之前的 Keycloak 配置）：
            //调用 Keycloak 的登录接口（比如keycloakInstance.login()），传递邮箱和密码。
            //后端 Keycloak 服务验证通过后，返回 JWT 令牌（token）。
            //前端存储令牌（比如存在localStorage或内存中），用于后续 API 请求的身份验证。
            await login(values.email, values.password);//等待登录请求完成（成功或失败），再执行后续代码。
// 如果去掉 async/await，代码会变成：登录请求发送后，不等结果就执行下一步，显示Login successful!
// async 本身不直接指定顺序，但它允许使用 await，从而强制 **“异步操作必须完成后，才执行后续代码”**
            // 显示登录成功通知
            dispatch(showSnackbar({ open: true, message: 'Login successful!', severity: 'success' }));//open: true：告诉通知组件 “需要显示出来”（相当于 “打开通知” 的开关）。
            
            onClose();// 调用父组件传递的onClose方法，关闭登录对话框（用户登录成功后，通常会跳转到首页或个人中心，所以关闭登录窗口）
        } catch (error) {
            // 显示登录失败通知
            dispatch(showSnackbar({ open: true, message: 'Login failed. Please check your credentials.', severity: 'error' }));
        }
    };

    /**
     * 处理用户注册
     * 
     * 重要步骤：
     * 1. 验证密码确认
     * 2. 发送注册请求到后端API
     * 3. 注册成功后自动登录
     * 4. 错误处理和用户提示
     * 
     * 业务逻辑：
     * - 用户注册成功后自动登录，提供无缝体验
     * - 密码确认验证确保用户输入正确
     * - 使用UserDTO格式发送数据到后端
     * 
     * @param {object} values - 表单值（username, email, password, confirmPassword）
     */
// 给所有后端接口加 /api 前缀，相当于给它们贴了一个 “我是后端服务” 的标签：
// 前端页面路由：/login、/home、/user（没有 /api，表示 “这是给用户看的页面”）。
// 后端接口路由：/api/user、/api/order、/api/goods（有 /api，表示 “这是给前端代码调用的服务”）。

// 写 <Link to="/user"> → 跳转到前端用户页面。
// 写 axios.get('/api/user') → 调用后端用户接口。

// 比如你在开发一个电商网站：
// 用户点击 “我的订单”，前端路由跳转到 /orders（页面路由，显示订单列表页面）。
// 这个页面需要加载订单数据，前端代码会调用 axios.get('/api/orders')（接口路由，向后端请求订单数据）。
// 如果没有 /api 前缀，两者都叫 /orders，系统就会 confusion：“用户点的是要页面还是要数据？”

    const handleSignUp = async (values) => {// values来自formikSignUp的表单输入，已通过 Yup 验证（比如邮箱格式、密码非空）
        // 字段来自用户在注册表单的输入，由 Formik 统一管理存储。
        // formikSignUp 是通过 useFormik 创建的表单管理器，它内部维护了一个 values 对象，专门存储表单字段的值
        if (values.password !== values.confirmPassword) {// 在发送请求到后端前，在前端校验，先检查用户输入的 “密码” 和 “确认密码” 是否一致。
            dispatch(showSnackbar({ open: true, message: "Passwords do not match!", severity: 'error' }));
            return;
        }

        try {
            // 发送注册请求到后端API
            // 按照UserDTO格式组织数据：{email, password, username}
            const response = await axiosInstance.post(`/api/user`, {// 通过封装的 HTTP 工具（axiosInstance）向后端发送 POST 请求。
    // 请求发送到/api/user接口，后端UserService的createUser方法接收UserDTO，完成用户创建
    // URL：/api/user  是前后端约定的注册接口地址，前端通过它向后端发送注册请求，后端则通过 UserController 的 createUser 方法时@RequestMapping("/api/user")接收并处理请求。
    // 前端 POST /api/user 请求，会被后端 @PostMapping 注解的 createUser 方法接收。
    // 前端发送的 JSON 数据（{email, password, username}）会被自动转换为后端的 UserDTO 对象，作为 createUser 方法的参数。

// URL：/api/user（后端接收注册请求的接口地址，对应后端UserController的createUser方法）。
// 请求体：按UserDTO格式组织数据（email、password、username），与后端接收的参数对应。
// 请求头：Content-Type: application/json（告诉后端 “请求体是 JSON 格式”）
                email: values.email,
                password: values.password,
                username: values.username
            }, {
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            // 注册成功处理
            if (response.status === 201) {
                dispatch(showSnackbar({ open: true, message: 'Sign up successful! Logging you in...', severity: 'success' }));
                // 注册成功后自动登录
                await login(values.email, values.password);//注册成功后调用login方法（和登录逻辑共用），用刚注册的邮箱和密码登录
                onClose(); 
            } else {
                dispatch(showSnackbar({ open: true, message: `Sign up failed: ${response.data.message}`, severity: 'error' }));
            }
        } catch (error) {
            // 错误处理：显示后端返回的错误信息或默认错误信息
            dispatch(showSnackbar({ 
                open: true, 
                message: `Sign up failed: ${error.response ? error.response.data.message : 'Please try again.'}`, 
                severity: 'error' 
            }));
        }
    };

    /**
     * 登录表单配置
     * 
     * 使用Formik进行表单状态管理：
     * - initialValues: 表单初始值
     * - validationSchema: 使用Yup定义验证规则
     * - onSubmit: 表单提交处理函数
     */
// 自动洗衣机：只需放入衣服（配置 initialValues）、设置模式（配置 validationSchema）、按下按钮（onSubmit），洗衣机就会自动完成所有工作。
// 表单管理器：只需配置 initialValues、validationSchema、onSubmit，Formik 就会自动管理表单状态（比如输入值、错误提示、提交状态）。
// 用户点击 “Login” 按钮 → Formik 触发表单提交。
// Formik 自动用 validationSchema 验证 initialValues 中的字段。
// 验证通过 → 调用 onSubmit 指向的 handleLogin 函数，并把当前表单值（formikLogin.values）作为参数传入。
// 验证失败 → 不执行 handleLogin，而是显示 errors 中的错误提示（比如邮箱格式错误时，输入框下方显示 “Invalid email address”）。
    const formikLogin = useFormik({//useFormik 是 Formik 库的核心钩子（Hook），接收一个配置对象，返回一个包含表单所有状态和方法的对象（这里赋值给 formikLogin）
        // 定义表单字段的初始值（相当于一张空白的登录表，email 和 password 一开始都是空字符串）
        initialValues: {
            email: '',
            password: ''
        },
        // 表单验证规则
        validationSchema: Yup.object({
            email: Yup.string().email('Invalid email address').required('Required'),
            password: Yup.string().required('Required')
        }),
        // 表单提交时，执行 handleLogin 函数
        onSubmit: handleLogin
        
    });
// “自动同步”“验证时机” 的具体代码确实没有直接写在你的组件里 
// 当验证失败时，Formik 会自动把错误信息存到formikLogin.errors
// 因为它们是Formik 库内部封装好的逻辑，你只需要通过简单的配置（比如useFormik的参数、getFieldProps的绑定）来 “启用” 这些功能，
    /**
     * 注册表单配置
     * 
     * 验证规则说明：
     * - username: 必填
     * - email: 必填且必须是有效邮箱格式
     * - password: 必填
     * - confirmPassword: 必填且必须与password匹配
     */
    const formikSignUp = useFormik({
        // 表单初始值
        initialValues: {
            username: '',
            email: '',
            password: '',
            confirmPassword: ''
        },
        // 表单验证规则
        validationSchema: Yup.object({
            username: Yup.string().required('Required'),
            email: Yup.string().email('Invalid email address').required('Required'),
            password: Yup.string().required('Required'),
            confirmPassword: Yup.string()
                .oneOf([Yup.ref('password'), null], 'Passwords must match') // 密码确认验证
                .required('Required')
        }),
        // 表单提交处理
        onSubmit: handleSignUp
    });

    /**
     * 处理对话框关闭
     * 
     * 防止用户点击背景时意外关闭对话框
     * @param {object} event - 事件对象
     * @param {string} reason - 关闭原因
     */
    const handleDialogClose = (event, reason) => {//这个函数会被绑定到 Dialog 组件的 onClose 事件上，下文onClose={handleDialogClose}
        if (reason !== "backdropClick") {//只有当关闭原因不是 “点击背景层” 时，才执行关闭操作。
            //当 reason 是 "escapeKeyDown"（按 Esc 键）：满足 reason !== "backdropClick"，执行 onClose()，对话框关闭。
            onClose();
        }
    };



//open状态被直接绑定到 open 属性
//来自父组件Header: <LoginDialog open={loginOpen} onClose={handleLoginClose} />
// 父组件（Header）通过 open={loginOpen} 告诉子组件（LoginDialog）：“现在要显示（true）还是隐藏（false）”。
// 子组件通过 onClose={handleDialogClose} 控制：“什么操作可以触发关闭”，最终通过 onClose() 通知父组件 “该关闭了”。
// 父组件收到通知后，将 loginOpen 设为 false，子组件因 open={false} 而隐藏。
//不是初始化。是告诉组件：“你要根据 open 的值显示 / 隐藏，关闭时要执行 handleDialogClose 这个函数”。
    return (
        <Dialog
            open={open}
            onClose={handleDialogClose}
            aria-labelledby="form-dialog-title"
            maxWidth="sm"
            fullWidth
            disableEscapeKeyDown
        >
            {/* 对话框标题 */}
            <DialogTitle id="form-dialog-title" sx={{ textAlign: 'center', fontWeight: 'bold', fontSize: '36px' }}>
                Welcome
            </DialogTitle>
            
            {/* 标签页导航，作用是响应用户点击 “登录” 或 “注册” 标签时，切换显示对应的表单，
            value={tabIndex}：绑定当前选中的标签索引（由tabIndex状态控制）：
                当tabIndex=0时，选中第一个标签（Login）；
                当tabIndex=1时，选中第二个标签（Sign Up）
            onChange={handleTabChange}：绑定标签切换事件处理函数：
                当用户点击不同标签时，Tabs组件会自动传递新的索引（newValue）给handleTabChange，进而更新tabIndex状态
            */}
            <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                <Tabs value={tabIndex} onChange={handleTabChange} aria-label="login and sign up tabs">
                    <Tab label="Login" />
                    <Tab label="Sign Up" />
                </Tabs>
            </Box>
            
            {/* 对话框内容区域 
             React 的 “条件渲染” 语法 {条件 && 内容}，意思是 “当条件为真时，才显示后面的内容
            {tabIndex === 0 && (...)}：当tabIndex为 0（用户选中 “Login” 标签），显示登录表单。
            {tabIndex === 1 && (...)}：当tabIndex为 1（用户选中 “Sign Up” 标签），显示注册表单。
            */}
            <DialogContent sx={{ minWidth: 400 }}>
                {/* 登录表单
                onSubmit当用户点击 “Login” 按钮提交表单时，会触发 Formik 提供的handleSubmit方法，进行验证 */}
                {tabIndex === 0 && (
                    <Box component="form" onSubmit={formikLogin.handleSubmit}>
                        {/* 邮箱输入框 
                        formikLogin.getFieldProps('email') 是 Formik 提供的一个方法，当你传入字段名'email'时，它会返回一个包含 3 个关键属性的对象
{...formikLogin.getFieldProps('email')} 是 Formik 提供的 “快捷绑定语法”，通过展开运算符，自动给输入框添加value（显示值）、onChange（同步输入）、onBlur（标记触碰状态）三个属性，让输入框和 Formik 的状态 “全自动绑定”，不用你手动写一行同步代码。
  value: formikLogin.values.email,  // 当前邮箱输入框的值，当用户输入后，formikLogin.values.email会被onChange更新，输入框也会实时显示最新值
  onChange: (e) => formikLogin.handleChange(e),  // 输入变化时的处理函数，当用户在输入框中打字（输入变化）时，会触发这个函数
  onBlur: (e) => formikLogin.handleBlur(e)  // 失焦时的处理函数，当用户点击输入框（获取焦点）后，再点击其他地方（失去焦点，即 “失焦”）时，触发这个函数：
它会把 formikLogin.touched.email 设为 true（标记 “用户已经碰过这个输入框”）。

{} 是 “插入 JavaScript 表达式” 的标记。意思：这里面写的不是普通文本，而是需要执行的 JavaScript 代码，执行结果会作为属性传递给组件。
...是 JavaScript 的 “展开运算符”（spread operator）。把一个对象的所有属性 “拆开”，逐个传递给组件。
formikLogin这是之前通过 useFormik 创建的 “登录表单管理器” 对象（变量名）。里面包含了表单的所有状态（如 values、errors、touched）和方法（如 handleChange、handleBlur）。
要访问 formikLogin 对象中的 getFieldProps 方法。这是 Formik 库给 formikLogin 对象添加的一个内置方法（专门用于表单字段绑定）。根据传入的字段名，生成一个包含该字段所需属性的对象（value、onChange、onBlur）
                        */}
                        <TextField
                            autoFocus
                            margin="dense"
                            id="login-email"
                            label="Email Address"
                            type="email"
                            fullWidth
                            {...formikLogin.getFieldProps('email')} // 非常非常非常重要！！！Formik字段绑定，自动给邮箱输入框绑定了 “显示值”“输入同步”“失焦标记” 三个核心功能，让输入框和 Formik 的 “email 字段状态” 全自动关联。
                            error={formikLogin.touched.email && Boolean(formikLogin.errors.email)} // 错误状态
                            helperText={formikLogin.touched.email && formikLogin.errors.email} // 错误提示
                        />
                        {/* 密码输入框  type="password" // 输入类型为密码，输入内容会被隐藏为圆点/星号*/}
                        <TextField
                            margin="dense"
                            id="login-password"
                            label="Password"
                            type="password"
                            fullWidth
                            {...formikLogin.getFieldProps('password')}
                            error={formikLogin.touched.password && Boolean(formikLogin.errors.password)}
                            helperText={formikLogin.touched.password && formikLogin.errors.password}
                        />
                    </Box>
                )}
                
                {/* 注册表单 */}
                {tabIndex === 1 && (
                    <Box component="form" onSubmit={formikSignUp.handleSubmit}>
                        {/* 用户名输入框 */}
                        <TextField
                            autoFocus
                            margin="dense"
                            id="signup-username"
                            label="Username"
                            type="text"
                            fullWidth
                            {...formikSignUp.getFieldProps('username')}
                            error={formikSignUp.touched.username && Boolean(formikSignUp.errors.username)}
                            helperText={formikSignUp.touched.username && formikSignUp.errors.username}
                        />
                        {/* 邮箱输入框 */}
                        <TextField
                            margin="dense"
                            id="signup-email"
                            label="Email Address"
                            type="email"
                            fullWidth
                            {...formikSignUp.getFieldProps('email')}
                            error={formikSignUp.touched.email && Boolean(formikSignUp.errors.email)}
                            helperText={formikSignUp.touched.email && formikSignUp.errors.email}
                        />
                        {/* 密码输入框 */}
                        <TextField
                            margin="dense"
                            id="signup-password"
                            label="Password"
                            type="password"
                            fullWidth
                            {...formikSignUp.getFieldProps('password')}
                            error={formikSignUp.touched.password && Boolean(formikSignUp.errors.password)}
                            helperText={formikSignUp.touched.password && formikSignUp.errors.password}
                        />
                        {/* 密码确认输入框 */}
                        <TextField
                            margin="dense"
                            id="signup-confirm-password"
                            label="Confirm Password"
                            type="password"
                            fullWidth
                            {...formikSignUp.getFieldProps('confirmPassword')}
                            error={formikSignUp.touched.confirmPassword && Boolean(formikSignUp.errors.confirmPassword)}
                            helperText={formikSignUp.touched.confirmPassword && formikSignUp.errors.confirmPassword}
                        />
                    </Box>
                )}
            </DialogContent>
            
            {/* 对话框操作按钮 */}
            <DialogActions sx={{ paddingRight: '25px', paddingBottom: '15px' }}>
                {/* 取消按钮 */}
                <Button onClick={onClose} color="primary">
                    Cancel
                </Button>
                {/* 登录/注册按钮 */}
                <Button onClick={tabIndex === 0 ? formikLogin.handleSubmit : formikSignUp.handleSubmit} color="primary">
                    {tabIndex === 0 ? 'Login' : 'Sign Up'}
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default LoginDialog;