import { configureStore } from '@reduxjs/toolkit';
import snackbarReducer from '../slice/snackbarSlice';
import windowSizeReducer, { windowSizeActions } from '../slice/windowSizeSlice';
import authReducer from '../slice/authSlice';
import cartReducer from '../slice/cartSlice';

const store = configureStore({
    reducer: {
        snackbar: snackbarReducer,
        windowSize: windowSizeReducer,
        auth: authReducer,
        cart: cartReducer
    },
});

store.dispatch(windowSizeActions.startListeningToResize());
export default store;
