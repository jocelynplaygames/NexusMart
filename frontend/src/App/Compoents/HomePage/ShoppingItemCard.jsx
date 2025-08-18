import React from 'react';
import Card from '@mui/material/Card';
import CardActions from '@mui/material/CardActions';
import CardContent from '@mui/material/CardContent';
import CardMedia from '@mui/material/CardMedia';
import IconButton from '@mui/material/IconButton';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import FavoriteIcon from '@mui/icons-material/Favorite';
import { useSelector } from 'react-redux';
import { addItemToCart } from '../../service/CartService';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { showSnackbar } from '../../redux/slice/snackbarSlice';

const ShoppingItemCard = ({ item }) => {
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const handleAddToCart = async () => {
    try {
      console.log(item);
      const cartItem = {
        itemId: item.id,
        quantity: 1,
      };
      await addItemToCart(cartItem);
      dispatch(showSnackbar({ open: true, message: 'Item added to cart', severity: 'success' }));
    } 
    catch (error) {
      console.error('Error adding item to cart:', error);
      let message = 'Failed to add item to cart! ';
      if (error.response?.status === 401) {
        message = 'Please login and try again!';
      } else {
        message += error.response.data;
      }
      dispatch(showSnackbar({ open: true, message, severity: 'error' }));
    }
  };

  const handleCardClick = () => {
    navigate(`/item/${item.id}/0`);
  };

  return (
    <Card style={{ position: 'relative', width: '200px' }} onClick={handleCardClick}>
      <CardMedia
        component="img"
        alt={item.title}
        image={item.imageUrl}
        title={item.title}
        sx={{ width: '200px', height: '200px', objectFit: 'cover' }}
      />
      <CardContent>
        <Typography component="div" sx={{ fontSize: '16px' }}>
          {item.title}
        </Typography>
      </CardContent>
      <CardActions style={{ justifyContent: 'space-between', padding: '0 15px 10px 15px' }}>
        <Typography variant="body1" color="textSecondary">
          ${item.price}
        </Typography>
        {isAuthenticated && (
          <Button size="small" color="primary" onClick={(e) => { e.stopPropagation(); handleAddToCart(); }}>
            Add to Cart
          </Button>
        )}
      </CardActions>
    </Card>
  );
};

export default ShoppingItemCard;