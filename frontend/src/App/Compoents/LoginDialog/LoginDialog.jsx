import React, { useState } from 'react';
// Material-UI component library imports
import { Dialog, DialogActions, DialogContent, DialogTitle, Button, TextField, Tabs, Tab, Box } from '@mui/material';
// Redux state management
import { useDispatch } from 'react-redux';
// Form handling library
import { useFormik } from 'formik';
// Form validation library
import * as Yup from 'yup';
// Custom authentication context
import { useAuth } from '../../Auth/AuthContext';
// Redux notification component
import { showSnackbar } from '../../redux/slice/snackbarSlice';
// HTTP client configuration
import axiosInstance from '../../service/AxiosConfig';

/**
 * Login/Register Dialog Component
 * 
 * Functionality:
 * - Provides unified interface for user login and registration
 * - Uses tabs to switch between login and registration forms
 * - Integrates form validation and error handling
 * - Supports automatic login after user registration
 * 
 * Technical Features:
 * - Uses Material-UI for modern UI construction
 * - Uses Formik for form state management
 * - Uses Yup for form validation
 * - Integrates Redux for state management
 * - Uses custom authentication context
 * 
 * @param {boolean} open - Whether dialog is open
 * @param {function} onClose - Callback function to close dialog
 */
// React login/register dialog component that provides user login and registration interface
// Works with backend authentication logic to complete user registration and login flow
const LoginDialog = ({ open, onClose }) => {
    // open: controls dialog display (true=show, false=hide, like a switch for opening/closing login window)
    // onClose: callback function to close dialog (called when clicking "Cancel" or after successful login)

    // Current selected tab index (0=login, 1=register)
    const [tabIndex, setTabIndex] = useState(0);
    // tabIndex: stores current selected tab index (0=login, 1=register)
    // When user clicks "Sign Up" tab, handleTabChange is triggered, setTabIndex(1) changes state to 1, switching to registration form
    // useState(0): React state hook for defining "changeable state" in function components, initial value 0 means default to first tab (login)

    // Get login method from authentication context
    // useAuth(): custom authentication context hook that encapsulates app login logic
    // login method receives user email and password, initiates login request and handles result
    const { login } = useAuth();

    // Redux dispatch function
    // useDispatch(): Redux hook for getting "state update function" (dispatch)
    // dispatch: sends actions to Redux reducer to update global state
    const dispatch = useDispatch();

    /**
     * Handle tab switching
     * @param {object} event - Event object
     * @param {number} newValue - New tab index
     */
    // User clicks "Login" or "Sign Up" tab to switch between forms
    const handleTabChange = (event, newValue) => {
        setTabIndex(newValue); // Update tabIndex to newValue
    };

    /**
     * Handle user login
     * 
     * Important steps:
     * 1. Call authentication context's login method
     * 2. Show success notification
     * 3. Close dialog
     * 4. Error handling and user feedback
     * 
     * @param {object} values - Form values (email, password)
     */
    const handleLogin = async (values) => { // Async function for backend API calls
        // values: Formik passes user input form data (email and password)
        try {
            // Call login method (from useAuth), pass email and password
            // login method internally calls backend login interface through Keycloak
            await login(values.email, values.password);
            
            // Show success notification
            dispatch(showSnackbar({
                message: 'Login successful!',
                severity: 'success'
            }));
            
            // Close dialog
            onClose();
        } catch (error) {
            // Show error notification
            dispatch(showSnackbar({
                message: error.response?.data?.message || 'Login failed. Please try again.',
                severity: 'error'
            }));
        }
    };

    /**
     * Handle user registration
     * 
     * Registration flow:
     * 1. Send registration request to backend
     * 2. Show success notification
     * 3. Automatically login after successful registration
     * 4. Close dialog
     * 
     * @param {object} values - Form values (email, password, confirmPassword)
     */
    const handleRegister = async (values) => {
        try {
            // Send registration request to backend
            await axiosInstance.post('/user', {
                email: values.email,
                password: values.password,
                firstName: values.firstName,
                lastName: values.lastName
            });
            
            // Show success notification
            dispatch(showSnackbar({
                message: 'Registration successful! Please login.',
                severity: 'success'
            }));
            
            // Switch to login tab
            setTabIndex(0);
        } catch (error) {
            // Show error notification
            dispatch(showSnackbar({
                message: error.response?.data?.message || 'Registration failed. Please try again.',
                severity: 'error'
            }));
        }
    };

    // Form validation schemas
    const loginValidationSchema = Yup.object({
        email: Yup.string().email('Invalid email format').required('Email is required'),
        password: Yup.string().required('Password is required')
    });

    const registerValidationSchema = Yup.object({
        email: Yup.string().email('Invalid email format').required('Email is required'),
        password: Yup.string().min(6, 'Password must be at least 6 characters').required('Password is required'),
        confirmPassword: Yup.string().oneOf([Yup.ref('password'), null], 'Passwords must match').required('Confirm password is required'),
        firstName: Yup.string().required('First name is required'),
        lastName: Yup.string().required('Last name is required')
    });

    // Formik form configurations
    const formikLogin = useFormik({
        initialValues: {
            email: '',
            password: ''
        },
        validationSchema: loginValidationSchema,
        onSubmit: handleLogin
    });

    const formikRegister = useFormik({
        initialValues: {
            email: '',
            password: '',
            confirmPassword: '',
            firstName: '',
            lastName: ''
        },
        validationSchema: registerValidationSchema,
        onSubmit: handleRegister
    });

    return (
        <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
            <DialogTitle>Login / Register</DialogTitle>
            <DialogContent>
                <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 2 }}>
                    <Tabs value={tabIndex} onChange={handleTabChange}>
                        <Tab label="Login" />
                        <Tab label="Sign Up" />
                    </Tabs>
                </Box>

                {/* Login Form */}
                {tabIndex === 0 && (
                    <form onSubmit={formikLogin.handleSubmit}>
                        <TextField
                            fullWidth
                            margin="normal"
                            name="email"
                            label="Email"
                            value={formikLogin.values.email}
                            onChange={formikLogin.handleChange}
                            error={formikLogin.touched.email && Boolean(formikLogin.errors.email)}
                            helperText={formikLogin.touched.email && formikLogin.errors.email}
                        />
                        <TextField
                            fullWidth
                            margin="normal"
                            name="password"
                            label="Password"
                            type="password"
                            value={formikLogin.values.password}
                            onChange={formikLogin.handleChange}
                            error={formikLogin.touched.password && Boolean(formikLogin.errors.password)}
                            helperText={formikLogin.touched.password && formikLogin.errors.password}
                        />
                        <DialogActions>
                            <Button onClick={onClose}>Cancel</Button>
                            <Button type="submit" variant="contained" color="primary">
                                Login
                            </Button>
                        </DialogActions>
                    </form>
                )}

                {/* Register Form */}
                {tabIndex === 1 && (
                    <form onSubmit={formikRegister.handleSubmit}>
                        <TextField
                            fullWidth
                            margin="normal"
                            name="firstName"
                            label="First Name"
                            value={formikRegister.values.firstName}
                            onChange={formikRegister.handleChange}
                            error={formikRegister.touched.firstName && Boolean(formikRegister.errors.firstName)}
                            helperText={formikRegister.touched.firstName && formikRegister.errors.firstName}
                        />
                        <TextField
                            fullWidth
                            margin="normal"
                            name="lastName"
                            label="Last Name"
                            value={formikRegister.values.lastName}
                            onChange={formikRegister.handleChange}
                            error={formikRegister.touched.lastName && Boolean(formikRegister.errors.lastName)}
                            helperText={formikRegister.touched.lastName && formikRegister.errors.lastName}
                        />
                        <TextField
                            fullWidth
                            margin="normal"
                            name="email"
                            label="Email"
                            value={formikRegister.values.email}
                            onChange={formikRegister.handleChange}
                            error={formikRegister.touched.email && Boolean(formikRegister.errors.email)}
                            helperText={formikRegister.touched.email && formikRegister.errors.email}
                        />
                        <TextField
                            fullWidth
                            margin="normal"
                            name="password"
                            label="Password"
                            type="password"
                            value={formikRegister.values.password}
                            onChange={formikRegister.handleChange}
                            error={formikRegister.touched.password && Boolean(formikRegister.errors.password)}
                            helperText={formikRegister.touched.password && formikRegister.errors.password}
                        />
                        <TextField
                            fullWidth
                            margin="normal"
                            name="confirmPassword"
                            label="Confirm Password"
                            type="password"
                            value={formikRegister.values.confirmPassword}
                            onChange={formikRegister.handleChange}
                            error={formikRegister.touched.confirmPassword && Boolean(formikRegister.errors.confirmPassword)}
                            helperText={formikRegister.touched.confirmPassword && formikRegister.errors.confirmPassword}
                        />
                        <DialogActions>
                            <Button onClick={onClose}>Cancel</Button>
                            <Button type="submit" variant="contained" color="primary">
                                Register
                            </Button>
                        </DialogActions>
                    </form>
                )}
            </DialogContent>
        </Dialog>
    );
};

export default LoginDialog;