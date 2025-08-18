import React from 'react';
import { Box, List, ListItem, ListItemText, Typography, Rating } from '@mui/material';

const sampleFeedbacks = [
    {
        id: 1,
        username: 'user1',
        feedback: 'Great seller! Fast shipping.',
        rating: 5,
        date: '2024-01-01',
    },
    {
        id: 2,
        username: 'user2',
        feedback: 'Item as described. Good communication.',
        rating: 4,
        date: '2024-01-02',
    },
    // Add more feedbacks as needed
];

const SellerFeedback = () => {
    return (
        <Box>
            <Typography variant="h6" mb={2}>Recent Feedbacks</Typography>
            <List>
                {sampleFeedbacks.map((feedback) => (
                    <ListItem key={feedback.id} alignItems="flex-start">
                        <Box display="flex" flexDirection="column" width="100%">
                            <Box display="flex" justifyContent="space-between">
                                <Typography variant="subtitle1" fontWeight="bold">{feedback.username}</Typography>
                                <Typography variant="body2" color="textSecondary">{feedback.date}</Typography>
                            </Box>
                            <Rating value={feedback.rating} readOnly size="small" />
                            <Typography variant="body2">{feedback.feedback}</Typography>
                        </Box>
                    </ListItem>
                ))}
            </List>
        </Box>
    );
};

export default SellerFeedback;

