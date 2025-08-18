import { createSlice, createAction } from '@reduxjs/toolkit';

const setIsWide = createAction('window/setIsWide');

const initialState = {
    isWide: window.innerWidth > 768
};

const windowSizeSlice = createSlice({
    name: 'window',
    initialState,
    reducers: {
        setIsWide: (state, action) => {
            state.isWide = action.payload;
        }
    }
});

const startListeningToResize = () => dispatch => {
    const handleResize = () => {
        dispatch(setIsWide(window.innerWidth > 768));
    };

    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
};

export const windowSizeActions = {
    setIsWide,
    startListeningToResize
};

export default windowSizeSlice.reducer;
