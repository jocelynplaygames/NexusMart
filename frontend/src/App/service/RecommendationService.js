import axiosInstance from "./AxiosConfig";

export const fetchTrendingTodayItems = async () => {
    const response = await axiosInstance.get('/api/item/trending-today');
    return response.data;
};

export const fetchItemsOnSale = async () => {
    const response = await axiosInstance.get('/api/item/items-on-sale');
    return response.data;
};

export const fetchJustForYouItems = async () => {
    const response = await axiosInstance.get('/api/item/just-for-you');
    return response.data;
};