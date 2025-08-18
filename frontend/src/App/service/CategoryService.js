import axiosInstance from './AxiosConfig';

export const fetchMainCategories = async () => {
    const response = await axiosInstance.get('/api/categories/main-categories');
    return response.data;
};

export const fetchSubCategories = async (mainCategoryId) => {
    const response = await axiosInstance.get(`/api/categories/main-categories/${mainCategoryId}/sub-categories`);
    return response.data;
};
