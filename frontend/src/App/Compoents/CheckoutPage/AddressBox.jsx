import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Box, Typography, Button } from '@mui/material';

const AddressBox = ({ address }) => {
    const navigate = useNavigate();
    console.log(address);
    const isAddressPresent = address? true: false;
    const isAddressValid = isAddressPresent && Object.values(address).every(field => field !== '');

    return (
        <Box marginBottom={3}>
            <Typography variant="h6" fontWeight='bold'>Ship to</Typography>
            <Typography variant="body1">{address?.firstName} {address?.lastName}</Typography>
            <Typography variant="body1">{address?.street}</Typography>
            <Typography variant="body1">
                {address?.city}
                {address?.city && address?.state ? ', ' : null}
                {address?.state} {address?.postalCode}
            </Typography>
            <Typography variant="body1">{address?.country}</Typography>
            <Typography variant="body1">{address?.phoneNumber}</Typography>
            <Button variant="outlined" onClick={() => navigate('/profile')} sx={{ marginTop: '10px' }}>
                {!isAddressPresent
                    ? 'No address, add an address to proceed.'
                    : (!isAddressValid
                        ? 'Invalid Address, please fix'
                        : 'Change Address'
                    )
                }
            </Button>
        </Box>
    );
}

export default AddressBox;