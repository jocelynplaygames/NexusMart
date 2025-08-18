import React from 'react';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import FavoriteIcon from '@mui/icons-material/Favorite';
import StarIcon from '@mui/icons-material/Star';
import Box from '@mui/material/Box';
import Grid from '@mui/material/Grid';

const ItemCard = ({ image, title, ratings, price, shippingInfo, sellerInfo }) => {
  return (
    <Card style={{ display: 'flex', padding: 16, position: 'relative' }}>
      <CardMedia
        component="img"
        alt={title}
        image={image}
        title={title}
        style={{ width: 160, height: 160, objectFit: 'cover' }}
      />
      <Box style={{ position: 'absolute', top: 10, right: 10 }}>
        <IconButton>
          <FavoriteIcon />
        </IconButton>
      </Box>
      <CardContent style={{ flex: 1 }}>
        <Typography variant="h6" component="div">
          {title}
        </Typography>
        <Box display="flex" alignItems="center">
          <StarIcon color="primary" />
          <Typography variant="body2" color="textSecondary">
            {ratings}
          </Typography>
        </Box>
        <Typography variant="h5" component="div" style={{ marginTop: 8 }}>
          ${price}
        </Typography>
        <Typography variant="body2" color="textSecondary">
          {shippingInfo}
        </Typography>
        <Typography variant="body2" color="textSecondary">
          {sellerInfo}
        </Typography>
        <Button variant="contained" color="primary" style={{ marginTop: 16 }}>
          Buy Now
        </Button>
      </CardContent>
    </Card>
  );
};

export default ItemCard;
