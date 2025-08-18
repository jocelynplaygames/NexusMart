import React from 'react';
import { Card, CardMedia, CardContent, Typography } from '@mui/material';

const CategoryItemCard = ({ item, onClick }) => {
    return (
        <Card
            sx={{ maxWidth: '150px', height: '200px', margin: '20px', display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' }}
            onClick={onClick}
        >
            <CardMedia
                component="img"
                alt={item.title}
                image={item.image}
                title={item.title}
                sx={{
                    width: '150px',
                    height: '150px',
                    objectFit: 'cover'
                }}
            />
            <CardContent sx={{ textAlign: 'center', flex: '1', padding: '6px' }}>
                <Typography variant="h7">{item.title}</Typography>
                <Typography variant="body2" color="textSecondary">
                    {item.subtitle}
                </Typography>
            </CardContent>
        </Card>
    );
};

export default CategoryItemCard;

