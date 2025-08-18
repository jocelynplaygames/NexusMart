import React from 'react';
import { Box, Typography, Container, Grid } from '@mui/material';
import NoBorderBtn from '../Buttons/NoBorder/NoBorderBtn';
import { useNavigate } from 'react-router-dom';

export default function Footer() {
    return (
        <Box component="footer" sx={{
            width: '100%',
            height: '80px',
            bgcolor: '#3c73d6',
            color: 'white',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            padding: '0 50px', 
            boxSizing: 'border-box',
            marginTop: 'auto',
        }}>
            <Box sx={{ display: 'flex', gap: 3 }}>
                <NoBorderBtn text={"Who We Are"} style={{color: 'white'}}/>
                <NoBorderBtn text={"Contact"} style={{color: 'white'}}/>
                <NoBorderBtn text={"Terms Of Use"} style={{color: 'white'}} link={{url:'/terms'}}/>
                <NoBorderBtn text={"Privacy"} style={{color: 'white'}}/>
            </Box>
            <Typography variant="body2" sx={{ textAlign: 'right' }}>
                Copyright © 2024 NexusMart Ecommerce.
            </Typography>
        </Box>
    );
};

