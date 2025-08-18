import React, { useState, useEffect } from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Box, TextField, Button, Typography, Snackbar, Alert } from '@mui/material';
import { saveUserProfile } from '../../service/UserService';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import { setUser } from '../../redux/slice/authSlice';

const ContactTab = () => {
    const user = useSelector((state) => state.auth.user);
    const token = useSelector((state) => state.auth.token);
    const dispatch = useDispatch();
    const [snackbarOpen, setSnackbarOpen] = useState(false);
    const [snackbarMessage, setSnackbarMessage] = useState('');
    const [snackbarSeverity, setSnackbarSeverity] = useState('success');

    const [email, setEmail] = useState('');
    const [phoneNumber, setPhoneNumber] = useState('');
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [street, setStreet] = useState('');
    const [city, setCity] = useState('');
    const [state, setState] = useState('');
    const [postalCode, setPostalCode] = useState('');
    const [country, setCountry] = useState('');

    useEffect(() => {
        if (user) {
            setEmail(user.email || '');
            setPhoneNumber(user.phoneNumber || '');
            setFirstName(user.firstName || '');
            setLastName(user.lastName || '');
            if (user.address) {
                setStreet(user.address.street || '');
                setCity(user.address.city || '');
                setState(user.address.state || '');
                setPostalCode(user.address.postalCode || '');
                setCountry(user.address.country || '');
            }
        }
    }, [user]);

    const handleSaveChanges = async (event) => {
        event.preventDefault();
        try {
            const newAddress = {
                street, city, state, postalCode, country
            };
            const newUser = {
                id: user.id,
                username: user.username,
                email: email,
                firstName: firstName,
                lastName: lastName,
                phoneNumber: phoneNumber,
                address: newAddress,
                profilePictureUrl: user.profilePictureUrl
            }

            const updatedUser = await saveUserProfile({user: newUser});
            dispatch(showSnackbar({ open: true, message: 'Contact information updated successfully!', severity: 'success' }));
        } catch (error) {
            console.error('Failed to update contact information:', error);
            const errorMessage = error.response?.data || 'contact information.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        }
    };

    const handleSnackbarClose = () => {
        setSnackbarOpen(false);
    };

    return (
        <Box mt={2}>
            <Typography variant="h6" mb={2}>Contact Information</Typography>
            <Box display="flex" gap={4}>
                <Box flex={1}>
                    <TextField
                        label="Email Address"
                        fullWidth
                        margin="normal"
                        name="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                    />
                    <TextField
                        label="First Name"
                        fullWidth
                        margin="normal"
                        name="firstName"
                        value={firstName}
                        onChange={(e) => setFirstName(e.target.value)}
                    />
                    <TextField
                        label="Last Name"
                        fullWidth
                        margin="normal"
                        name="lastName"
                        value={lastName}
                        onChange={(e) => setLastName(e.target.value)}
                    />
                    <TextField
                        label="Phone Number"
                        fullWidth
                        margin="normal"
                        name="phoneNumber"
                        value={phoneNumber}
                        onChange={(e) => setPhoneNumber(e.target.value)}
                    />
                </Box>
                <Box flex={1}>
                    <TextField
                        label="Street"
                        fullWidth
                        margin="normal"
                        name="street"
                        value={street}
                        onChange={(e) => setStreet(e.target.value)}
                    />
                    <TextField
                        label="City"
                        fullWidth
                        margin="normal"
                        name="city"
                        value={city}
                        onChange={(e) => setCity(e.target.value)}
                    />
                    <TextField
                        label="State"
                        fullWidth
                        margin="normal"
                        name="state"
                        value={state}
                        onChange={(e) => setState(e.target.value)}
                    />
                    <TextField
                        label="Postal Code"
                        fullWidth
                        margin="normal"
                        name="postalCode"
                        value={postalCode}
                        onChange={(e) => setPostalCode(e.target.value)}
                    />
                    <TextField
                        label="Country"
                        fullWidth
                        margin="normal"
                        name="country"
                        value={country}
                        onChange={(e) => setCountry(e.target.value)}
                    />
                </Box>
            </Box>
            <Button
                variant="contained"
                color="primary"
                sx={{ mt: 2, marginTop: '30px', float: 'right' }}
                onClick={handleSaveChanges}
            >
                Save Changes
            </Button>
            <Snackbar
                open={snackbarOpen}
                autoHideDuration={6000}
                onClose={handleSnackbarClose}
                anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
            >
                <Alert onClose={handleSnackbarClose} severity={snackbarSeverity} sx={{ width: '100%' }}>
                    {snackbarMessage}
                </Alert>
            </Snackbar>
        </Box>
    );
};

export default ContactTab;
