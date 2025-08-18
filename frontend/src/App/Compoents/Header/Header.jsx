import React, { useState } from 'react';
import { AppBar, Toolbar, Typography, Button, IconButton, ButtonBase, Avatar  } from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import LoginDialog from '../LoginDialog/LoginDialog';

// Header component - parent component for LoginDialog
// User clicks "Login" button in Header → parent component handleLoginOpen triggers → loginOpen = true → child component LoginDialog shows due to open={true}
// User completes action in LoginDialog (login success, click "Cancel") → child component calls onClose (parent's handleLoginClose) → parent loginOpen = false → child hides due to open={false}
const Header = () => {
    const sampleAvatarUrl = "https://via.placeholder.com/150"; // Sample avatar URL for users without profile picture
    const [loginOpen, setLoginOpen] = useState(false); // React useState hook creates state and state update function
    const { isAuthenticated, user } = useSelector(state => state.auth); // Get user login status and user info from Redux
    // useSelector is a Redux hook for getting data from global state
    // Display different content based on authentication status (show "Login" button when not authenticated, show avatar/cart when authenticated)
    const dispatch = useDispatch();

    const handleLoginOpen = () => { // Custom function to open login dialog when called
        setLoginOpen(true);
    };

    const handleLoginClose = () => { // handleLoginClose sets loginOpen to false
        setLoginOpen(false);
    };

    const navigate = useNavigate();

    return (
        <AppBar sx={{ backgroundColor: '#5696fc', width: '100vw', padding: '0px' }}>
            <Toolbar sx={{ display: 'flex', justifyContent: 'space-between' }}>
                <ButtonBase onClick={() => navigate('/')} sx={{ flexGrow: 1, textAlign: 'left', maxWidth: '250px' }}>
                    <Typography variant="h6">
                        NexusMart Ecommerce Site
                    </Typography>
                </ButtonBase>
                <div>
                    {!isAuthenticated && ( // If user not authenticated, show "Login" button
                        <Button color="inherit" onClick={handleLoginOpen}>
                            Login
                        </Button>
                    )}
                    {isAuthenticated && ( // If user authenticated, show "Sell" button, cart icon, user avatar
                        <>
                            <Button color="inherit" onClick={() => navigate('/sell')}>
                                Sell
                            </Button>
                            <IconButton color="inherit" onClick={() => navigate('/cart')}>
                                <ShoppingCartIcon />
                            </IconButton>
                            <IconButton color="inherit" onClick={() => navigate('/profile')}>
                                <Avatar src={user?.profilePictureUrl || sampleAvatarUrl} alt="User Avatar" />
                            </IconButton>
                        </>
                    )}
                </div>
            </Toolbar>
            <LoginDialog open={loginOpen} onClose={handleLoginClose} />
        </AppBar>
    );
};

export default Header;