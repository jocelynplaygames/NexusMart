import axiosInstance from './AxiosConfig';
import store from '../redux/store/store';

export const createListing = async (itemData, coverImage, additionalImages) => {
    const formData = new FormData();
    formData.append('item', new Blob([JSON.stringify(itemData)], {
        type: 'application/json'
    }));

    
    if (coverImage) {
        formData.append('imageFile', coverImage);
    }

    additionalImages.forEach((file, index) => {
        formData.append('subImageFiles', file);
    });

    const state = store.getState();
    const token = state.auth.token;

    const response = await axiosInstance.post('/api/item', formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${token}`
        }
    });

    return response.data;
};

export const fetchItems = async (userId) => {
    const url = userId ? `/api/item?userId=${userId}` : '/api/item';
    const response = await axiosInstance.get(url);
    return response.data;
};

export const updateItem = async (itemId, itemData, coverImage, additionalImages) => {
    const formData = new FormData();
    if (coverImage) {
        formData.append('imageFile', coverImage);
    }

    const existingImageUrls = additionalImages.filter(img => typeof img === 'string');
    const additionalImageObjs = additionalImages.filter(img => img instanceof File);
    additionalImageObjs.forEach((file) => {
        formData.append('subImageFiles', file);
    });

    for (let i = 0; i < 4; i++) {
        itemData[`subImageUrl${i + 1}`] = null;
    }
    
    for (let i = 0; i < existingImageUrls.length; i++) {
        itemData[`subImageUrl${i + 1}`] = existingImageUrls[i];
    }
    console.log(itemData);
    formData.append('item', new Blob([JSON.stringify(itemData)], {
        type: 'application/json'
    }));
    const state = store.getState();
    const token = state.auth.token;

    const response = await axiosInstance.put(`/api/item/${itemId}`, formData, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${token}`
        }
    });

    return response.data;
};

export const deleteItem = async (itemId) => {
    const state = store.getState();
    const token = state.auth.token;

    const response = await axiosInstance.delete(`/api/item/${itemId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });

    return response.data;
};

export const fetchItem = async (itemId) => {
    try {
        const response = await axiosInstance.get(`/api/item/${itemId}`);
        console.log('fetching item. id: ' + itemId);
        return response.data;
    } catch (error) {
        console.error('Error fetching item:', error);
        throw error;
    }
};

