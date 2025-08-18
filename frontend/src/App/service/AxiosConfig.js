// File: frontend/src/App/service/AxiosConfig.js
import axios from 'axios';
//创建并导出一个自定义配置的 Axios 实例
// 用于统一管理前端项目中所有的 HTTP 请求（比如调用后端 API 接口）
const axiosInstance = axios.create({
    baseURL: 'http://www.nexusmart-ecommerce.com', 
    //如果后端接口的域名发生变化只需修改这里的 baseURL无需修改项目中所有的请求路径
});
// baseURL 是 Axios 实例的核心配置项，用于设置所有请求的基础路径（根 URL）。 
// 是一个字符串，代表你所有后端 API 接口的 “公共前缀”。
// 之后发送请求时，只需写相对路径（如 /api/login）
// Axios 会自动拼接 baseURL如axiosInstance.post('/api/login')
// 等价于发送请求到：baseURL + '/api/login'

export default axiosInstance;
//将创建好的 axiosInstance 实例 “导出”
// 让项目中的其他文件可以通过 import 引入并使用这个实例发送请求