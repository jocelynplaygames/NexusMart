import axios from 'axios';

const axiosInstance = axios.create({
    baseURL: 'http://www.nexusmart-ecommerce.com', 
});

export default axiosInstance;