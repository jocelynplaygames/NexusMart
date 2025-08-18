import React, { useState, useEffect } from 'react';
import { Box, Typography, Button, Modal, TextField, Rating  } from '@mui/material';
import { useAuth } from '../../Auth/AuthContext';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import { useDispatch } from 'react-redux';
import { saveUserProfile } from '../../service/UserService';
import { useSelector } from 'react-redux';
import { setUser } from '../../redux/slice/authSlice';
import { fetchUserRating } from '../../service/RatingSerivce';

const UserProfileHeader = () => {
    const defaultProfileImageURL = "https://via.placeholder.com/100";

    const { logout } = useAuth();
    const dispatch = useDispatch();
    const user = useSelector((state) => state.auth.user);
    const [isEditing, setIsEditing] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [username, setUsername] = useState(user?.username || '');
    const [profilePicture, setProfilePicture] = useState(user?.profilePictureUrl || defaultProfileImageURL);
    const [file, setFile] = useState(null);
    const [rating, setRating] = useState(0);
    const [numRatings, setNumRatings] = useState(0);

    const handleLogout = () => {
        logout();
    };

    const handleEditProfile = () => {
        setIsEditing(true);
    };

    const handleSaveProfile = async (event) => {
        event.preventDefault();
        setIsUploading(true);
        dispatch(showSnackbar({ open: true, message: 'Uploading...', severity: 'info' }));
        try {
            const newUser = {
                id: user.id,
                username: username,
                email: user.email,
                firstName: user.firstName,
                lastName: user.lastName,
                phoneNumber: user.phoneNumber,
                address: user.address,
                profilePictureUrl: user.profilePictureUrl
            }
            const updatedUser = await saveUserProfile({ user: newUser, file });
            dispatch(setUser({ user: updatedUser }));
            setProfilePicture(updatedUser.profilePictureUrl || defaultProfileImageURL);
            setIsEditing(false);
            dispatch(showSnackbar({ open: true, message: 'Profile updated successfully!', severity: 'success' }));
        } catch (error) {
            console.error('Failed to update profile:', error);
            const errorMessage = error.response?.data || 'Failed to update profile.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        } finally {
            setIsUploading(false);
        }
    };

    const handleFileChange = (e) => {
        setFile(e.target.files[0]);
    };

    useEffect(() => {
        if (user) {
            setUsername(user.username || '');
            setProfilePicture(user.profilePictureUrl || defaultProfileImageURL);

            // Fetch user rating
            const fetchRating = async () => {
                try {
                    const userRating = await fetchUserRating(user.id);
                    setNumRatings(userRating.numRatings);
                    setRating(userRating.numRatings? (userRating.totalRating / userRating.numRatings): 0);
                } catch (error) {
                    console.error('Failed to fetch user rating:', error);
                }
            };

            fetchRating();
        }
    }, [user]);

    return (
        <>
            {
                user ?
                    <Box display="flex" alignItems="center" mb={4}>
                        <Box
                            component="img"
                            src={profilePicture}
                            alt="Profile"
                            sx={{ width: 100, height: 100, borderRadius: '50%', mr: 2 }}
                        />
                        <Box>
                            <Typography  variant="h5" fontWeight="bold">{username}</Typography>
                            <Box display="flex" alignItems="center">
                                <Rating value={rating} readOnly precision={0.1}/>
                                <Typography variant="body2" color="textSecondary" ml={1}>
                                    ({rating.toFixed(1)} / {numRatings} ratings)
                                </Typography>
                            </Box>
                        </Box>
                        <Box ml="auto">
                            <Button variant="outlined" onClick={handleEditProfile}>Edit Profile</Button>
                            <Button
                                variant="contained"
                                sx={{
                                    backgroundColor: '#e81010',
                                    marginLeft: '10px',
                                    '&:hover': {
                                        backgroundColor: '#c30d0d'
                                    }
                                }}
                                onClick={handleLogout}
                            >
                                Log Out
                            </Button>
                        </Box>

                        <Modal
                            open={isEditing}
                            onClose={() => setIsEditing(false)}
                            aria-labelledby="edit-profile-modal-title"
                            aria-describedby="edit-profile-modal-description"
                        >
                            <Box
                                sx={{
                                    position: 'absolute',
                                    top: '50%',
                                    left: '50%',
                                    transform: 'translate(-50%, -50%)',
                                    width: 400,
                                    bgcolor: 'background.paper',
                                    boxShadow: 24,
                                    p: 4
                                }}
                            >
                                <Typography id="edit-profile-modal-title" variant="h6" component="h2">
                                    Edit Profile
                                </Typography>
                                <Box component="form" noValidate autoComplete="off">
                                    <TextField
                                        fullWidth
                                        margin="normal"
                                        id="username"
                                        label="Username"
                                        value={username}
                                        onChange={(e) => setUsername(e.target.value)}
                                    />
                                    <input
                                        accept="image/*"
                                        style={{ display: 'none' }}
                                        id="profile-picture"
                                        type="file"
                                        onChange={handleFileChange}
                                    />
                                    <label htmlFor="profile-picture">
                                        <Button variant="contained" color="primary" component="span" disabled={isUploading}>
                                            Upload Profile Picture
                                        </Button>
                                    </label>
                                    <Box mt={2} display="flex" justifyContent="flex-end">
                                        <Button variant="contained" color="primary" onClick={handleSaveProfile} disabled={isUploading}>
                                            Save
                                        </Button>
                                    </Box>
                                </Box>
                            </Box>
                        </Modal>
                    </Box>
                    :
                    <div>
                        Loading...
                    </div>
            }
        </>
    );
};

export default UserProfileHeader;