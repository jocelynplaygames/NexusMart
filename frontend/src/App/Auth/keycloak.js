import Keycloak from 'keycloak-js';//keycloak-js是 Keycloak 官方提供的前端库，封装了和 Keycloak 服务器通信的方法（比如登录、登出、获取用户信息）。

//对接 Keycloak 服务器的 “关键信息”，相当于登录第三方系统的 “地址和账号”：
//KEYCLOAK_URL：Keycloak 服务器的地址（相当于公安局的地址）。
//KEYCLOAK_REALM：Keycloak 中的 “区域”，相当于公安局的 “分局”。
//KEYCLOAK_CLIENT_ID：Keycloak 中的 “客户端”，相当于公安局的 “身份证办理窗口”。
//KEYCLOAK_CLIENT_SECRET：Keycloak 的 “客户端密钥”，相当于身份证办理窗口的 “钥匙”。
export const KEYCLOAK_URL = 'http://keycloak.nexusmart-ecommerce.com';
export const KEYCLOAK_REALM = 'NexusMart';
export const KEYCLOAK_CLIENT_ID = 'nexusmart-ecommerce';//Keycloak 中的 “客户端”，相当于公安局的 “身份证办理窗口”。
export const KEYCLOAK_CLIENT_SECRET = 'g5HFcWL6MHBFnrAe9FO0HhlMipOHzFNZ';

let initOptions = {
    url: KEYCLOAK_URL,
    realm: KEYCLOAK_REALM,
    clientId: KEYCLOAK_CLIENT_ID,//复用上面定义的服务器地址、领域、客户端 ID（必填项，否则找不到 Keycloak 服务器）。
    onLoad: 'check-sso',//页面加载时的行为 ——“自动检查用户是否已登录”（SSO 是单点登录的意思）。    比如用户之前在其他页面登录过，打开当前页面时，Keycloak 会自动识别 “已登录状态”，不用重复登录。
    KeycloakResponseType: 'code'//登录成功后，Keycloak 返回的 “授权类型” 为code（一种安全的授权码，前端用这个码去换用户令牌，避免直接传递令牌的风险）。
}


export const keycloakInstance = new Keycloak(initOptions);
//用上面的配置创建一个 Keycloak 实例（keycloakInstance），这个实例就像 “已经配置好的微信登录工具”，可以直接调用它的方法。
//后续前端可以用这个实例做这些事：
//keycloakInstance.login()：跳转到 Keycloak 的登录页面。
//keycloakInstance.logout()：退出登录。
//keycloakInstance.token：获取用户的 JWT 令牌（用来调用后端 API 时证明身份）。