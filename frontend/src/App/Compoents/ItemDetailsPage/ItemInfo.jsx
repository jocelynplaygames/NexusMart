import React from 'react';
import { Box, Typography } from '@mui/material';

const ItemInfo = ({ title, description, price }) => {
    return (
        <Box>
            <Typography variant="h4" fontWeight="bold">{title}</Typography>
            <Typography variant="body1" mt={2}>{description}</Typography>
            <Typography variant="h5" color="primary" mt={2}>${price}</Typography>
        </Box>
    );
};

export default ItemInfo;
