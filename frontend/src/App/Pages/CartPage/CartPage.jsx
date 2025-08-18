import React, { useState, useEffect } from 'react';
import { Box, Typography, Divider, Grid, TextField, Link } from '@mui/material';
import CartItem from '../../Compoents/CartPage/CartItem';
import CartTotal from '../../Compoents/CartPage/CartTotal';
import RecommendationItem from '../../Compoents/CartPage/RecommendationItem';
import { useSelector } from 'react-redux';
import { useDispatch } from 'react-redux';
import { loadCartItems, updateCartState } from '../../redux/slice/cartSlice';
import { fetchTrendingTodayItems } from '../../service/RecommendationService';

const sampleRecommendations = [
    {
        id: 1,
        title: 'Recommended Item 1',
        image: 'https://via.placeholder.com/150',
        price: '$350.00',
    },
    {
        id: 2,
        title: 'Recommended Item 2',
        image: 'https://via.placeholder.com/150',
        price: '$450.00',
    },
];


const Cart = () => {
    const dispatch = useDispatch();
    const cartItems = useSelector((state) => state.cart?.items) || [];
    const cartId = useSelector((state) => state.cart.cartId); // Select cartId from the state
    const currentUserId = useSelector((state) => state.auth.user?.id);
    const [sampleRecommendations, setSampleRecommendations] = useState([]);

    useEffect(() => {
        if (currentUserId) {
            dispatch(loadCartItems(currentUserId));
        }
    }, [currentUserId, dispatch]);

    useEffect(() => {
        const fetchRecommendations = async () => {
            try {
                const recommendations = await fetchTrendingTodayItems();
                const firstFiveRecommendations = recommendations.slice(0, 5);
                setSampleRecommendations(firstFiveRecommendations);
            } catch (error) {
                console.error('Error fetching recommendations:', error);
            }
        };

        fetchRecommendations();
    }, []);

    const handleQuantityChange = (id, quantity) => {
        const updatedItems = cartItems.map(item =>
            item.id === id ? { ...item, quantity: Math.max(1, quantity) } : item
        );
        dispatch(updateCartState({ userId: currentUserId, cartItems: updatedItems }));
    };

    const handleRemove = (id) => {
        const updatedItems = cartItems.filter(item => item.id !== id);
        dispatch(updateCartState({ userId: currentUserId, cartItems: updatedItems }));
    };

    const calculateTotal = () => {
        return cartItems.reduce((total, item) => total + item.quantity * item.price, 0);
    };

    return (
        <div className='app-content'>
            <Typography variant="h3" mb={4} sx={{ paddingTop: '50px', boxSizing: 'border-box', fontWeight: 'bold', textAlign: 'center' }}>
                Shopping Cart
            </Typography>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                <div style={{ backgroundColor: '#fafafa', borderRadius: '25px', flex: '1', marginRight: '30px' }}>
                    {cartItems && cartItems.length > 0 ?
                        cartItems.map(item => (
                            <CartItem
                                key={item.id}
                                item={item}
                                onQuantityChange={handleQuantityChange}
                                onRemove={handleRemove}
                            />
                        ))
                        :
                        <p style={{ width: '100%', textAlign: 'center', fontSize: '20px' }}>Your cart is empty.</p>
                    }
                </div>
                <CartTotal total={calculateTotal()} />
            </div>
            <Divider sx={{ my: 4 }} />
            <Typography variant="h5" mb={2}>Recommended for you</Typography>
            <Box display="flex" flexWrap="wrap" justifyContent="space-between" mb={5}>
                {sampleRecommendations.map(item => (
                    <Box key={item.id}  m={1} width="250px">
                        <RecommendationItem item={item} />
                    </Box>
                ))}
            </Box>
        </div>
    );
};

export default Cart;