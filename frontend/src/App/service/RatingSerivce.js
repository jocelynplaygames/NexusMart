import axiosInstance from './AxiosConfig';
import store from '../redux/store/store';

export const fetchUserRating = async (userId) => {
    try {
        const response = await axiosInstance.get(`/api/ratings/entity/${userId}/SELLER`);
        console.log('rating: ' + response.data);
        return response.data;
    } catch (error) {
        console.error('Error fetching user rating:', error);
        throw error;
    }
};

export const fetchItemRating = async (itemId) => {
    try {
        const response = await axiosInstance.get(`/api/ratings/entity/${itemId}/PRODUCT`);
        return response.data;
    } catch (error) {
        console.error('Error fetching item rating:', error);
        throw error;
    }
};


export const fetchRatingById = async (ratingId) => {
    try {
        const response = await axiosInstance.get(`/api/ratings/${ratingId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching rating:', error);
        throw error;
    }
};

export const createRating = async (rating) => {
    const state = store.getState();
    const token = state.auth.user.token;

    try {
        const response = await axiosInstance.post('/api/ratings', rating, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error creating rating:', error);
        throw error;
    }
};

export const updateRating = async (ratingId, updatedRating) => {
    const state = store.getState();
    const token = state.auth.user.token;

    try {
        const response = await axiosInstance.put(`/api/ratings/${ratingId}`, updatedRating, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Error updating rating:', error);
        throw error;
    }
};

export const deleteRating = async (ratingId) => {
    const state = store.getState();
    const token = state.auth.user.token;

    try {
        await axiosInstance.delete(`/api/ratings/${ratingId}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    } catch (error) {
        console.error('Error deleting rating:', error);
        throw error;
    }
};
