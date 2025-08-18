import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, Typography, Button, Grid, Divider } from '@mui/material';
import { useParams, useNavigate } from 'react-router-dom';
import FeedbackDialog from '../Feedback/FeedbackDialog';
import { fetchItemFeedbacks, fetchUserFeedbacks } from '../../service/FeedbackService';

const OrderReceipt = ({ order }) => {
    const navigate = useNavigate();
    const itemTotal = order.orderItems.reduce((total, item) => total + item.quantity * item.price, 0);
    const tax = (itemTotal + order.shippingCost) * 0.06;
    const orderTotal = itemTotal + order.shippingCost + tax;

    const [feedbackDialogOpen, setFeedbackDialogOpen] = useState(false);
    const [selectedItem, setSelectedItem] = useState(null);
    const [userFeedbacks, setUserFeedbacks] = useState([]);

    useEffect(() => {
        const fetchFeedbacks = async () => {
            try {
                const feedbacks = await fetchUserFeedbacks(order.userId); // Correctly fetch user feedbacks
                setUserFeedbacks(feedbacks);
            } catch (error) {
                console.error('Error fetching user feedbacks:', error);
            }
        };

        fetchFeedbacks();
    }, [order.userId]);

    const handleLeaveFeedback = (item) => {
        setSelectedItem(item);
        setFeedbackDialogOpen(true);
    };

    const hasFeedback = (itemId) => {
        return userFeedbacks.some(feedback => feedback.itemId === itemId);
    };

    return (
        <Box p={3}>
            <Typography variant="h4" gutterBottom>Order Receipt</Typography>
            <Card>
                <CardContent>
                    <Typography variant="h6" gutterBottom>Order Date: {new Date(order.createdAt).toLocaleDateString()}</Typography>
                    <Divider />
                    <Box mt={2}>
                        <Typography variant="subtitle1" gutterBottom><strong>Order Items</strong></Typography>
                        <Grid container spacing={2}>
                            {order.orderItems.map((item) => (
                                <Grid item xs={12} key={item.id}>
                                    <div>
                                        <Typography variant="subtitle1" gutterBottom>{item.itemName}</Typography>
                                        <Typography variant="body2" color="textSecondary">Quantity: {item.quantity}</Typography>
                                        <Typography variant="body2" color="textSecondary">Price: ${item.price.toFixed(2)}</Typography>
                                        <Typography variant="body2" color="textSecondary">Subtotal: ${(item.quantity * item.price).toFixed(2)}</Typography>
                                        <div style={{ display: 'flex', alignItems: 'center', marginTop: '20px', marginBottom: '20px' }}>
                                            <Button variant="contained" color="primary" onClick={() => navigate(`/item/${item.itemId}/0`)} >View Details</Button>
                                            {hasFeedback(item.itemId) ? (
                                                <Button
                                                    variant="contained"
                                                    color="primary"
                                                    sx={{
                                                        backgroundColor: '#B947FF',
                                                        marginLeft: '20px',
                                                        '&:hover': {
                                                            backgroundColor: '#A511FF'
                                                        }
                                                    }}
                                                    onClick={() => navigate(`/item/${item.itemId}/1`)}
                                                >
                                                    View Feedback
                                                </Button>
                                            ) : (
                                                <Button
                                                    variant="contained"
                                                    color="primary"
                                                    sx={{
                                                        backgroundColor: '#B947FF',
                                                        marginLeft: '20px',
                                                        '&:hover': {
                                                            backgroundColor: '#A511FF'
                                                        }
                                                    }}
                                                    onClick={() => handleLeaveFeedback(item)}
                                                >
                                                    Leave Feedback
                                                </Button>
                                            )}
                                        </div>
                                        <hr />
                                    </div>
                                </Grid>
                            ))}
                        </Grid>
                    </Box>
                    <Box mt={4}>
                        <Typography variant="subtitle1" gutterBottom><strong>Payment Information</strong></Typography>
                        <Typography variant="body2" color="textSecondary">Card Used: {order.cardType} ending in {order.lastFourDigit}</Typography>
                    </Box>
                    <Box mt={4}>
                        <Typography variant="subtitle1" gutterBottom><strong>Shipping Information</strong></Typography>
                        <Typography variant="body2" color="textSecondary">Shipping Method: {order.shippingMethodName}</Typography>
                        <Typography variant="body2" color="textSecondary">Shipping Cost: ${order.shippingCost.toFixed(2)}</Typography>
                    </Box>
                    <Divider sx={{ my: 2 }} />
                    <Box mt={4}>
                        <Typography variant="subtitle1" gutterBottom><strong>Order Summary</strong></Typography>
                        <Typography variant="body2" color="textSecondary">Item Total: ${itemTotal.toFixed(2)}</Typography>
                        <Typography variant="body2" color="textSecondary">Shipping: ${order.shippingCost.toFixed(2)}</Typography>
                        <Typography variant="body2" color="textSecondary">Tax: ${tax.toFixed(2)}</Typography>
                        <Typography variant="h6">Order Total: ${orderTotal.toFixed(2)}</Typography>
                    </Box>
                    <Box mt={4} display="flex" justifyContent="flex-end">
                        <Button variant="contained" color="primary" onClick={() => navigate('/browse')}>Continue Shopping</Button>
                    </Box>
                </CardContent>
            </Card>

            {selectedItem && (
                <FeedbackDialog
                    open={feedbackDialogOpen}
                    onClose={() => setFeedbackDialogOpen(false)}
                    itemName={selectedItem.itemName}
                    itemId={selectedItem.itemId}
                    userId={order.userId}
                />
            )}
        </Box>
    );
};

export default OrderReceipt;