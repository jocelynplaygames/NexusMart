import axiosInstance from "./AxiosConfig";
import store from "../redux/store/store";
import { decodeToken } from "../Auth/JwtUtils";


axiosInstance.interceptors.request.use(
    (config) => {
        const state = store.getState();
        const token = state.auth.token;
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

export const fetchCartItems = async (userId) => {
    try {
        const response = await axiosInstance.get(`/api/cart/${userId}`);
        return response.data; // Return the full cart data including cartId
    } catch (error) {
        console.error('Error fetching cart items:', error);
        throw error;
    }
};

export const updateCart = async (userId, cartItems) => {
    try {
        console.log('Updating cart: ' + userId);
        const state = store.getState();
        const cartId = state.cart.cartId; // Get cartId from Redux state

        const cartDTO = {
            id: cartId, // Set the cartId
            userId: userId,
            cartItemsInput: cartItems // Ensure this is an array
        };

        const response = await axiosInstance.put(`/api/cart/${userId}`, cartDTO);
        return response.data; // Return the full cart data
    } catch (error) {
        console.error('Error updating cart state:', error);
        throw error;
    }
};

export const addItemToCart = async (cartItem) => {
    try {
        const response = await axiosInstance.post('/api/cart/items', cartItem);
        return response.data;
    } catch (error) {
        console.error('Error adding item to cart:', error);
        throw error;
    }
};

export const fetchShippingMethods = async () => {
    try {
        const response = await axiosInstance.get('/api/orders/shipping-methods');
        return response.data;
    } catch (error) {
        console.error('Error fetching shipping methods:', error);
        throw error;
    }
};