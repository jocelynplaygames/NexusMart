import React from 'react';
import { Box, Typography } from '@mui/material';
import ShoppingItemCard from './ShoppingItemCard';

const HomePageCarousal = ({ title, items }) => {
  return (
    <Box sx={{ width: '100%', marginTop: '50px', width: '100%'}}>
      <Typography variant="h4" component="div" sx={{ marginBottom: '20px', fontWeight: 'bold' }}>
        {title}
      </Typography>
      <Box sx={{ display: 'flex', gap: '30px', overflowX: 'auto', padding: '10px', padding: '40px 40px', backgroundColor: '#fafafa', borderRadius: '25px', flexWrap:'wrap'  }}>
        {items.map((item, index) => (
          <ShoppingItemCard key={index} item={item} />
        ))}
      </Box>
    </Box>
  );
};

export default HomePageCarousal;
