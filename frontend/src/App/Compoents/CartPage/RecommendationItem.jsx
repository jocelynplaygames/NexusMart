import React from 'react';
import { Card, CardMedia, CardContent, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';

const RecommendationItem = ({ item }) => {
    const navigate = useNavigate();

    const handleCardClick = () => {
        navigate(`/item/${item.id}/0`);
    };

    return (
        <Card sx={{maxWidth: '300px'}}>
            <CardMedia
                component="img"
                alt={item.title}
                height="250px"
                width='250px'
                image={item.imageUrl}
            />
            <CardContent>
                <Typography variant="h6">{item.title}</Typography>
                <Typography variant="body1" color="textSecondary">${item.price}</Typography>
                <Button variant="contained" color="primary" size="small" sx={{ mt: 2 }} onClick={handleCardClick} >View Details</Button>
            </CardContent>
        </Card>
    );
};

export default RecommendationItem;


