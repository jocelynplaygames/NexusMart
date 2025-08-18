import { createSlice } from '@reduxjs/toolkit';

const authSlice = createSlice({
  name: 'auth',
  initialState: {
    isAuthenticated: false,
    token: null,
    user: null, // Add user state
  },
  reducers: {
    setAuthenticated(state, action) {
      state.isAuthenticated = action.payload.isAuthenticated;
      state.token = action.payload.token;
    },
    setUser(state, action) {
      state.user = action.payload.user; // Add setUser action
    },
    logout(state) {
      state.isAuthenticated = false;
      state.token = null;
      state.user = null; // Clear user state on logout
    }
  }
});

export const { setAuthenticated, setUser, logout } = authSlice.actions;
export default authSlice.reducer;

