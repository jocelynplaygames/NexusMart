import React from 'react';
import { Box, Button, TextField } from '@mui/material';

const AddToCart = () => {
    return (
        <Box mt={4} display="flex" alignItems="center">
            <TextField
                type="number"
                defaultValue={1}
                InputProps={{ inputProps: { min: 1 } }}
                sx={{ width: '80px', marginRight: '16px' }}
            />
            <Button variant="contained" color="primary" size="large">
                Add to Cart
            </Button>
        </Box>
    );
};

export default AddToCart;
