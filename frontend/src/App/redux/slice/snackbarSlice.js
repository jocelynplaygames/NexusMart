import { createSlice } from '@reduxjs/toolkit';

export const snackbarSlice = createSlice({
  name: 'snackbar',
  initialState: {
    open: false,
    message: '',
    severity: 'info', 
    vertical: 'bottom',
    horizontal: 'center',
    autoHideDuration: 6000,
  },
  reducers: {
    showSnackbar: (state, action) => {
      state.open = action.payload.open;
      state.message = action.payload.message;
      state.severity = action.payload.severity;
      state.vertical = action.payload.vertical || state.vertical;
      state.horizontal = action.payload.horizontal || state.horizontal;
      state.autoHideDuration = action.payload.autoHideDuration || 6000;
    },
    closeSnackbar: (state) => {
      state.open = false;
    },
  },
});

export const { showSnackbar, closeSnackbar } = snackbarSlice.actions;

export default snackbarSlice.reducer;
