// File: frontend/src/App/Pages/CheckoutPage/CheckoutPage.jsx
import React, { useEffect, useState, useRef } from 'react';
import { Box, Grid, Typography, Button } from '@mui/material';
import SummaryCard from '../../Compoents/CheckoutPage/SummaryCard';
import AddressBox from '../../Compoents/CheckoutPage/AddressBox';
import PaymentSelection from '../../Compoents/CheckoutPage/PaymentSelection';
import { useDispatch, useSelector } from 'react-redux';
import { loadCartItems } from '../../redux/slice/cartSlice';
import { fetchUserCards } from '../../service/UserService';
import ShippingMethodSelection from '../../Compoents/CheckoutPage/ShippingMethodSelection';
import { fetchShippingMethods } from '../../service/CartService';
import { createOrder } from '../../service/OrderSerivce';
import { useNavigate } from 'react-router-dom';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import SockJS from 'sockjs-client';

const CheckoutPage = () => {
    const user = useSelector((state) => state.auth.user);
    const cart = useSelector((state) => state.cart);
    const dispatch = useDispatch();
    const navigate = useNavigate();

    const [cards, setCards] = useState([]);
    const [shippingMethods, setShippingMethods] = useState([]);
    const [selectedShippingCost, setSelectedShippingCost] = useState(0);
    const [selectedShippingMethod, setSelectedShippingMethod] = useState(null);
    const [selectedPaymentMethod, setSelectedPaymentMethod] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const socketRef = useRef(null);

    useEffect(() => {
        if (user?.id) {
            dispatch(loadCartItems(user.id));

            const getUserCards = async () => {
                try {
                    const fetchedCards = await fetchUserCards(user.id);
                    setCards(fetchedCards);
                    if (fetchedCards.length > 0) {
                        setSelectedPaymentMethod(fetchedCards[0].id);
                    }
                } catch (error) {
                    console.error('Error fetching user cards:', error);
                }
            };

            const getShippingMethods = async () => {
                try {
                    const fetchedMethods = await fetchShippingMethods();
                    setShippingMethods(fetchedMethods);
                    if (fetchedMethods.length > 0) {
                        setSelectedShippingMethod(fetchedMethods[0].id);
                        setSelectedShippingCost(fetchedMethods[0].price);
                    }
                } catch (error) {
                    console.error('Error fetching shipping methods:', error);
                }
            };

            getUserCards();
            getShippingMethods();
        }
    }, [user, dispatch]);

    const closeWebSocket = () => {
        if (socketRef.current) {
            socketRef.current.close();
            socketRef.current = null;
            console.log('WebSocket connection closed manually');
        }
    };

    const connectWebSocket = () => {
        return new Promise((resolve, reject) => {
            socketRef.current = new SockJS('http://www.nexusmart-ecommerce.com/api/orders/ws');
            socketRef.current.onopen = () => {
                console.log('Connected to WebSocket');
                resolve();
            };

            socketRef.current.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    console.log('Received WebSocket message:', message);
                    handleOrderStatusUpdate(message.status, message.orderId);
                } catch (error) {
                    console.error('Error parsing WebSocket message:', error);
                }
            };

            socketRef.current.onerror = (error) => {
                console.error('WebSocket error:', error);
                reject(error);
            };

            socketRef.current.onclose = () => {
                console.log('WebSocket connection closed');
                socketRef.current = null;
            };
        });
    };

    const handleOrderStatusUpdate = (status, orderId) => {
        console.log(`Received WebSocket update for order ID: ${orderId}, status: ${status}`);
        if (status === 'COMPLETED') {
            dispatch(showSnackbar({
                open: true,
                message: 'Payment successful! Redirecting to receipt...',
                severity: 'success'
            }));
            setTimeout(() => {
                navigate(`/receipt/${orderId}`);
            }, 500);
        } else if (status === 'FAILED') {
            dispatch(showSnackbar({
                open: true,
                message: 'Payment failed. Please try again.',
                severity: 'error'
            }));
            setIsSubmitting(false);
            closeWebSocket();
        }
    };

    const calculateTotalPrice = () => {
        return cart.items.reduce((total, item) => total + item.price * item.quantity, 0);
    };

    const handleShippingMethodChange = (price, id) => {
        setSelectedShippingCost(price);
        setSelectedShippingMethod(id);
    };

    const handlePaymentChange = (id) => {
        setSelectedPaymentMethod(id);
    };

    const handleConfirmAndPay = async () => {
        if (isSubmitting) {
            return;
        }
        setIsSubmitting(true);
        try {
            dispatch(showSnackbar({
                open: true,
                message: 'Order submitting...',
                severity: 'info',
                autoHideDuration: 5000
            }));

            await connectWebSocket();
            await createOrder(cart.cartId, selectedShippingMethod, selectedPaymentMethod);
        } 
        catch (error) {
            console.error('Error initializing WebSocket connection or creating order:', error);
            dispatch(showSnackbar({
                open: true,
                message: 'Failed to create order. Please try again.',
                severity: 'error'
            }));
            setIsSubmitting(false);
            closeWebSocket();
        }
    };

    if (cart.status === 'loading') {
        return <div>Loading...</div>;
    }

    if (cart.status === 'failed') {
        return <div>Error loading cart</div>;
    }

    return (
        <div className='app-content' style={{ backgroundColor: '#fafafa', width: '100vw' }}>
            <div style={{ padding: '50px', width: '100%', boxSizing: 'border-box' }}>
                <Typography variant="h3" fontWeight='bold' textAlign='center'>Checkout</Typography>
                <Box display="flex" justifyContent="center" padding={4} mt={3}>
                    <Grid container spacing={4} maxWidth="1200px">
                        <Grid item xs={12} md={8}>
                            <PaymentSelection cards={cards} onPaymentMethodChange={handlePaymentChange} />
                            <ShippingMethodSelection
                                shippingMethods={shippingMethods}
                                onShippingMethodChange={(price, id) => handleShippingMethodChange(price, id)}
                            />
                            <hr />
                            <AddressBox address={user?.address} />
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <SummaryCard
                                items={cart.items}
                                total={calculateTotalPrice()}
                                cards={cards}
                                address={user?.address}
                                shippingCost={selectedShippingCost}
                                onConfirmAndPay={handleConfirmAndPay}
                                isSubmitting={isSubmitting}
                            />
                        </Grid>
                    </Grid>
                </Box>
            </div>
        </div>
    );
};

export default CheckoutPage;