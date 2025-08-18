import React, { useEffect, useState } from 'react';
import { Container, Grid, Button, Typography, Box, Divider, TextField } from '@mui/material';
import ItemImageGallery from '../../Compoents/ItemDetailsPage/ItemImageGallery';
import ItemDetails from '../../Compoents/ItemDetailsPage/ItemDetails';
import { fetchItem } from '../../service/ListingsService';
import { useParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import Rating from '@mui/material/Rating';
import { fetchSeller } from '../../service/UserService';
import { addItemToCart } from '../../service/CartService';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import { useDispatch } from 'react-redux';
import { fetchUserRating, fetchItemRating } from '../../service/RatingSerivce';

const ItemDetailsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [item, setItem] = useState(null);
    const [seller, setSeller] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const currentUserId = useSelector((state) => state.auth.user?.id);
    const [quantity, setQuantity] = useState(1);
    const dispatch = useDispatch();
    const [ratingSeller, setRatingSeller] = useState(0);
    const [numRatingsSeller, setNumRatingsSeller] = useState(0);
    const [ratingItem, setRatingItem] = useState(0);
    const [numRatingsItem, setNumRatingsItem] = useState(0);

    useEffect(() => {
        const getItem = async () => {
            try {
                const data = await fetchItem(id);
                setItem(data);

                const sellerData = await fetchSeller(data.sellerId);
                setSeller(sellerData);

                const itemRating = await fetchItemRating(id);
                const sellerRating = await fetchUserRating(data.sellerId);

                setRatingSeller(sellerRating.totalRating / sellerRating.numRatings);
                setNumRatingsSeller(sellerRating.numRatings);
                setRatingItem(itemRating.totalRating / itemRating.numRatings);
                setNumRatingsItem(itemRating.numRatings);

                setItem(prevItem => ({
                    ...prevItem,
                    totalRating: itemRating.totalRating,
                    numRatings: itemRating.numRatings,
                }));

                setSeller(prevSeller => ({
                    ...prevSeller,
                    totalRating: sellerRating.totalRating,
                    numRatings: sellerRating.numRatings,
                }));

                setLoading(false);
            } catch (error) {
                setError('Error fetching item or seller details');
                setLoading(false);
            }
        };

        getItem();
    }, [id]);

    if (loading) {
        return <div>Loading...</div>;
    }

    if (error) {
        return <div>{error}</div>;
    }

    const images = [
        item.imageUrl,
        item.subImageUrl1,
        item.subImageUrl2,
        item.subImageUrl3,
        item.subImageUrl4
    ].filter(Boolean); // Remove any undefined or null values

    const handleSellerClick = () => {
        if (item.sellerId === currentUserId) {
            navigate('/profile');
        } else {
            navigate(`/seller-profile/${item.sellerId}`);
        }
    };


    const handleQuantityChange = (event) => {
        const value = Math.min(event.target.value, item.quantity);
        setQuantity(value);
    };

    const handleAddToCart = async () => {
        try {
            const cartItem = {
                itemId: item.id,
                quantity: parseInt(quantity, 10),
            };
            await addItemToCart(cartItem);
            dispatch(showSnackbar({ open: true, message: 'Item added to cart', severity: 'success' }));
        }
        catch (error) {
            console.error('Error adding item to cart:', error);
            let message = 'Failed to add item to cart! ';
            if (error.response && error.response.status === 401) {
                message = 'Please login and try again!';
            } else {
                message += error.response ? error.response.data : error.message;
            }
            dispatch(showSnackbar({ open: true, message, severity: 'error' }));
        }
    };

    return (
        <Container className='app-content' style={{ minWidth: '100%' }}>
            <Box sx={{ padding: '40px 60px', width: '100%', boxSizing: 'border-box' }}>
                <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 6 }}>
                    <Box sx={{ flex: 1 }}>
                        {images.length > 0 ? (
                            <ItemImageGallery images={images} />
                        ) : (
                            <div>No images available</div>
                        )}
                    </Box>
                    <Box sx={{ flex: 1 }}>
                        <Box>
                            <Typography variant="h3" fontWeight="bold">{item.title}</Typography>
                        </Box>
                        <Box display="flex" alignItems="center" mb={2} >
                            <Button onClick={handleSellerClick} sx={{ padding: '10px 0px' }}>
                                {seller.username}
                            </Button>
                            <Box display="flex" alignItems="center" sx={{ marginLeft: '13px' }}>
                                <Rating value={ratingSeller} readOnly precision={0.1} />
                                {numRatingsSeller > 0 && (
                                    <Typography variant="body2" color="textSecondary" sx={{ ml: 1 }}>
                                        ({ratingSeller.toFixed(1)} / {numRatingsSeller} ratings)
                                    </Typography>
                                )}
                            </Box>
                        </Box>
                        <Box display="flex" alignItems="center" mb={2} sx={{ paddingTop: '10px' }}>
                            <Typography variant="body2" mr={1}>Item Rating</Typography>
                            <Rating value={ratingItem} readOnly precision={0.1} />
                            {numRatingsItem > 0 && (
                                <Typography variant="body2" color="textSecondary" sx={{ ml: 1 }}>
                                    ({ratingItem.toFixed(1)} / {numRatingsItem} ratings)
                                </Typography>
                            )}
                        </Box>
                        <Divider sx={{ my: 2 }} />
                        <Typography variant="h4" color="primary" mt={2}>${item.price}</Typography>
                        <Typography variant="body2" sx={{ paddingTop: '30px' }}>Quantity left: {item.quantity}</Typography>
                        <Box display="flex" alignItems="center" mt={2} mb={2} gap={2}>
                            <TextField
                                type="number"
                                label="Quantity"
                                value={quantity}
                                onChange={handleQuantityChange}
                                inputProps={{ min: 1, max: item.quantity }}
                                sx={{ width: '100px' }}
                                disabled={item.quantity <= 0 || !currentUserId}
                            />
                            <Button
                                variant="contained"
                                color="primary"
                                sx={{ height: '55px' }}
                                onClick={handleAddToCart}
                                disabled={item.quantity <= 0 || !currentUserId}
                            >
                                {item.quantity > 0 ? 'Add to Cart' : 'Out of stock'}
                            </Button>
                        </Box>
                        <Divider sx={{ my: 2 }} />
                        <Typography sx={{ fontWeight: 'bold', fontSize: '20px' }} mt={2}>Description</Typography>
                        <Typography variant="body1" mt={2}>{item.shortDescription}</Typography>
                    </Box>
                </Box>
                <Box mt={4}>
                    <ItemDetails item={item} />
                </Box>
            </Box>
        </Container>
    );
};

export default ItemDetailsPage;