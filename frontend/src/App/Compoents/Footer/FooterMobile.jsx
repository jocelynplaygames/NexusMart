import React from 'react';
import { Box, Typography, Container, Grid } from '@mui/material';
import NoBorderBtn from '../Buttons/NoBorder/NoBorderBtn';

export default function Footer() {
  const buttonStyle = {
    color: 'white', fontSize: '8px', padding: '5px', margin: '0px', minWidth: '0'
  }

  return (
    <Box component="footer" sx={{
      width: '100%',
      height: '65px',
      bgcolor: '#3c73d6',
      color: 'white',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      padding: '0 5px',
      boxSizing: 'border-box',
      marginTop: 'auto'
    }}>
      <div style={{ display: 'flex' }}>
        <NoBorderBtn text={"Profile"} style={buttonStyle} />
        <NoBorderBtn text={"Who We Are"} style={buttonStyle} />
        <NoBorderBtn text={"Contact"} style={buttonStyle} />
        <NoBorderBtn text={"Terms Of Use"} style={buttonStyle} link={{url:'/terms'}}/>
        <NoBorderBtn text={"Privacy"} style={buttonStyle} />
      </div>
      <p style={{ textAlign: 'right', fontSize: '9px' }}>
        Copyright © 2024 My Ecommerce.
      </p>
    </Box>
  );
};


