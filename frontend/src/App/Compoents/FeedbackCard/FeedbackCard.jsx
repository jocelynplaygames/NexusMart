import React, {useState} from 'react';
import { Box, Typography, Rating, TextField, Button } from '@mui/material';
import { deleteFeedback, updateFeedback } from '../../service/FeedbackService';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';
import { showSnackbar } from '../../redux/slice/snackbarSlice';

const FeedbackCard = ({ feedback, onDelete, onUpdate }) => {
    const [isEditing, setIsEditing] = useState(false);
    const [updatedFeedback, setUpdatedFeedback] = useState(feedback);
    const userId = useSelector((state) => state.auth.user?.id);
    const dispatch = useDispatch();

    const handleEditClick = () => {
        setIsEditing(true);
    };

    const handleCancelClick = () => {
        setIsEditing(false);
    };

    const handleUpdateClick = async () => {
        try {
            const data = await updateFeedback(feedback.id, updatedFeedback);
            onUpdate(feedback.id, data);
            setIsEditing(false);
            dispatch(showSnackbar({
                open: true,
                message: 'Feedback updated successfully!',
                severity: 'success'
            }));
        } catch (error) {
            console.error('Error updating feedback:', error);
            dispatch(showSnackbar({
                open: true,
                message: 'Failed to update feedback!',
                severity: 'error'
            }));
        }
    };

    const handleDeleteClick = async () => {
        try {
            await deleteFeedback(feedback.id);
            onDelete(feedback.id);
            dispatch(showSnackbar({
                open: true,
                message: 'Feedback deleted successfully!',
                severity: 'success'
            }));
        } catch (error) {
            console.error('Error deleting feedback:', error);
            dispatch(showSnackbar({
                open: true,
                message: 'Failed to delete feedback!',
                severity: 'error'
            }));
        }
    };

    return (
        <Box border={1} borderColor="grey.300" borderRadius={2} p={2} mb={2} sx={{backgroundColor: 'rgb(225, 222, 237)'}}>
            <Box display="flex" alignItems="center" mb={1}>
                <Typography variant="h6" mr={2}>
                    {feedback.userName}
                </Typography>
                <Rating value={feedback.rating} readOnly />
            </Box>
            {isEditing ? (
                <Box>
                    <Rating
                        value={updatedFeedback.rating}
                        onChange={(event, newValue) => setUpdatedFeedback({ ...updatedFeedback, rating: newValue })}
                    />
                    <TextField
                        value={updatedFeedback.comment}
                        onChange={(e) => setUpdatedFeedback({ ...updatedFeedback, comment: e.target.value })}
                        multiline
                        rows={4}
                        fullWidth
                        variant="outlined"
                    />
                    <Box display="flex" justifyContent="flex-end" mt={2}>
                        <Button onClick={handleCancelClick} color="primary">
                            Cancel
                        </Button>
                        <Button onClick={handleUpdateClick} color="primary" variant="contained" sx={{ ml: 2 }}>
                            Update
                        </Button>
                    </Box>
                </Box>
            ) : (
                <Box>
                    <Typography variant="body1">{feedback.comment}</Typography>
                    {feedback.userId === userId && (
                        <Box display="flex" justifyContent="flex-end" mt={2}>
                            <Button onClick={handleEditClick} color="primary">
                                Edit
                            </Button>
                            <Button onClick={handleDeleteClick} color="secondary" sx={{ ml: 2 }}>
                                Delete
                            </Button>
                        </Box>
                    )}
                </Box>
            )}
        </Box>
    );
};

export default FeedbackCard;