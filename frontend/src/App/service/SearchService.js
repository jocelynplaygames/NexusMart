import axiosInstance from "./AxiosConfig";

const SearchService = {
    searchItems: async (query, country, city, minPrice, maxPrice, mainCategoryId, subCategoryId) => {
        try {
            const response = await axiosInstance.get('/api/item/search', {
                params: {
                    query,
                    country,
                    city,
                    minPrice,
                    maxPrice,
                    mainCategoryId,
                    subCategoryId
                }
            });
            console.log(response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching search results:', error);
            throw error;
        }
    },
    fetchSuggestions: async (query) => {
        try {
            const response = await axiosInstance.get('/api/item/search', {
                params: { query }
            });
            return response.data;
        } catch (error) {
            console.error('Error fetching suggestions:', error);
            throw error;
        }
    }
};

export default SearchService;