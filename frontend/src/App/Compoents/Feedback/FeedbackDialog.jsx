import React, { useState } from 'react';
import { Dialog, DialogActions, DialogContent, DialogTitle, Button, TextField, Rating, Box, Typography } from '@mui/material';
import { submitFeedback } from '../../service/FeedbackService';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import { useDispatch } from 'react-redux';

const FeedbackDialog = ({ open, onClose, itemName, itemId }) => {
    const [rating, setRating] = useState(0);
    const [comment, setComment] = useState('');
    const dispatch = useDispatch();

    const handleSubmit = async () => {
        const feedback = {
            rating,
            comment
        };
        try {
            await submitFeedback(itemId, feedback);
            dispatch(showSnackbar({
                open: true,
                message: 'Feedback submitted successfully!',
                severity: 'success'
            }));
            onClose();
        } catch (error) {
            console.error('Error submitting feedback:', error);
            dispatch(showSnackbar({
                open: true,
                message: 'Failed to submit feedback!',
                severity: 'error'
            }));
        }
    };

    return (
        <Dialog open={open} onClose={onClose}>
            <DialogContent sx={{ minWidth: '500px', padding: '30px', paddingBottom: '5px' }}>
                <DialogTitle sx={{ fontWeight: 'bold', fontSize: '24px', marginBottom: '10px', paddingLeft: '0px', paddingTop: '0px' }}>Leave Feedback</DialogTitle>
                <Typography variant="h6" gutterBottom>
                    {itemName}
                </Typography>
                <Rating
                    value={rating}
                    onChange={(event, newValue) => {
                        setRating(newValue);
                    }}
                    size="large"
                />
                <Box display="flex" flexDirection="column" alignItems="center">
                    <TextField
                        margin="dense"
                        label="Your feedback"
                        type="text"
                        fullWidth
                        variant="outlined"
                        value={comment}
                        onChange={(e) => setComment(e.target.value)}
                        multiline
                        rows={8}
                    />
                </Box>
            </DialogContent>
            <DialogActions sx={{ marginRight: '22px', marginBottom: '20px' }}>
                <Button onClick={onClose} color="primary">
                    Cancel
                </Button>
                <Button onClick={handleSubmit} color="primary" variant="contained">
                    Submit
                </Button>
            </DialogActions>
        </Dialog>
    );
};

export default FeedbackDialog;