import axiosInstance from "./AxiosConfig";
import store from "../redux/store/store";


export const createOrder = async (cartId, shippingMethodId, cardId) => {
    console.log("Create Order " + cartId + " " + shippingMethodId + " " + cardId);
    try {
        const response = await axiosInstance.post('/api/orders', {
            cartId,
            shippingMethodId,
            cardId
        });

        return response.data; // This should return the order ID or other relevant data
    } catch (error) {
        console.error('Error creating order:', error);
        throw error;
    }
};

export const fetchUserOrders = async (userId) => {
    try {
        const response = await axiosInstance.get(`/api/orders/user/${userId}`);
        return response.data;
    } catch (error) {
        console.error('Error fetching user orders:', error);
        throw error;
    }
};

export const fetchOrder = async (orderId) => {
    const state = store.getState();
    const token = state.auth.token;

    try {
        const response = await axiosInstance.get(`/api/orders/${orderId}`, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        return response.data;
    } catch (error) {
        console.error('Failed to fetch order:', error);
        throw error;
    }
};