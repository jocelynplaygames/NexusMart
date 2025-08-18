import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import { fetchCartItems, updateCart } from '../../service/CartService';

const initialState = {
    cartId: null, // Add cartId to the initial state
    items: [],
    status: 'idle',
    error: null
};

export const loadCartItems = createAsyncThunk(
    'cart/loadCartItems',
    async (userId, { rejectWithValue }) => {
        try {
            const cart = await fetchCartItems(userId);
            return cart; // Return the full cart object
        } catch (error) {
            return rejectWithValue(error.response.data);
        }
    }
);

export const updateCartState = createAsyncThunk(
    'cart/updateCartState',
    async (cart, { rejectWithValue }) => {
        try {
            const updatedCart = await updateCart(cart.userId, cart.cartItems);
            return updatedCart; // Ensure the full cart data is returned
        } catch (error) {
            return rejectWithValue(error.response.data);
        }
    }
);

const cartSlice = createSlice({
    name: 'cart',
    initialState,
    reducers: {
        setCartItems(state, action) {
            state.items = action.payload.cartItems;
            state.cartId = action.payload.id; // Set cartId from the action payload
        },
        clearCart(state) { // Add clearCart action
            state.items = [];
            state.cartId = null;
            state.status = 'idle';
            state.error = null;
        }
    },
    extraReducers: (builder) => {
        builder
            .addCase(loadCartItems.pending, (state) => {
                state.status = 'loading';
            })
            .addCase(loadCartItems.fulfilled, (state, action) => {
                state.status = 'succeeded';
                state.items = action.payload.cartItemsOutput;
                state.cartId = action.payload.id; // Set cartId from the action payload
            })
            .addCase(loadCartItems.rejected, (state, action) => {
                state.status = 'failed';
                state.error = action.payload;
            })
            .addCase(updateCartState.fulfilled, (state, action) => {
                state.items = action.payload.cartItemsOutput;
                state.cartId = action.payload.id; // Set cartId from the action payload
            });
    }
});

export const { setCartItems, clearCart } = cartSlice.actions;

export default cartSlice.reducer;