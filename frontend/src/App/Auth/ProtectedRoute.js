import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from './AuthContext';
import { useSelector } from 'react-redux';
import { isAuthenticated } from './JwtUtils';

const ProtectedRoute = ({ children }) =>  {
    const token = useSelector(state => state.auth.token) || localStorage.getItem('token');

    if (!isAuthenticated(token)) {
        return <Navigate to="/" />;
    }

    return children ? children : <Outlet />;
};

export default ProtectedRoute;


