import axiosInstance from "./AxiosConfig";
import { decodeToken } from "../Auth/JwtUtils";
import store from '../redux/store/store';
import { setUser } from "../redux/slice/authSlice";

export const fetchUser = async () => {
    const state = store.getState();
    const token = state.auth.token;
    if (!token) {
        return;
    }

    const decoded = decodeToken(token);
    const userId = decoded.sub;

    try {
        const response = await axiosInstance.get(`/api/user/${userId}`, {
            headers: {
                Authorization: ``
            }
        });

        const newUser = response.data.user;
        store.dispatch(setUser({ user: newUser }));
        return newUser;
    } 
    catch (error) {
        console.error('Error fetching user data:', error);
        throw error;
    }
};

export const fetchSeller = async (id) => {
    try {
        const response = await axiosInstance.get(`/api/user/${id}`, {});
        return response.data.user;
    } catch (error) {
        console.error('Error fetching user data:', error);
        throw error;
    }
};

export const saveUserProfile = async ({ user, file }) => {
    const formData = new FormData();
    formData.append('user', new Blob([JSON.stringify({
        id: user.id,
        username: user.username,
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        phoneNumber: user.phoneNumber,
        address: user.address,
        profilePictureUrl: user.profilePictureUrl
    })], {
        type: "application/json"
    }));

    if (file) {
        formData.append('profilePicture', file);
    }

    const state = store.getState();
    const token = state.auth.token;

    try {
        const response = await axiosInstance.put(`/api/user/${user.id}`, formData, {
            headers: {
                Authorization: `Bearer ${token}`,
                'Content-Type': 'multipart/form-data',
            },
        });
        console.log(response);
        const updatedUser = response.data.user;
        store.dispatch(setUser({ user: updatedUser }));
        return updatedUser;
    } catch (error) {
        console.error('Failed to update user profile:', error);
        throw error;
    }
};

export const fetchUserCards = async (userId) => {
    const state = store.getState();
    const token = state.auth.token;

    try {
        const response = await axiosInstance.get(`/api/user/${userId}/card`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        return response.data;
    } catch (error) {
        console.error('Failed to fetch user cards:', error);
        throw error;
    }
};

export const createUserCard = async (userId, cardData) => {
    const state = store.getState();
    const token = state.auth.token;
    console.log("card data " + cardData);
    try {
        const response = await axiosInstance.post(`/api/user/${userId}/card`, cardData, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Failed to create user card:', error);
        throw error;
    }
};

export const updateUserCard = async (userId, cardId, cardData) => {
    const state = store.getState();
    const token = state.auth.token;
    console.log("updating card!");
    try {
        const response = await axiosInstance.put(`/api/user/${userId}/card/${cardId}`, cardData, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Failed to update user card:', error);
        throw error;
    }
};

export const deleteUserCard = async (userId, cardId) => {
    const state = store.getState();
    const token = state.auth.token;

    try {
        await axiosInstance.delete(`/api/user/${userId}/card/${cardId}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
    } catch (error) {
        console.error('Failed to delete user card:', error);
        throw error;
    }
};