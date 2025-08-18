import React, { useEffect, useState } from 'react';
import { Box, Typography } from '@mui/material';
import OrderReceipt from '../../Compoents/ReceiptPage/OrderReceipt';
import { useSelector } from 'react-redux';
import { useParams } from 'react-router-dom';
import { fetchOrder } from '../../service/OrderSerivce';

const UserReceiptPage = () => {
    const [order, setOrder] = useState(null);
    const user = useSelector((state) => state.auth.user);
    const { id } = useParams();

    useEffect(() => {
        if (user?.id && id) {
            const getOrder = async () => {
                try {
                    const fetchedOrder = await fetchOrder(id);
                    setOrder(fetchedOrder);
                } catch (error) {
                    console.error('Error fetching order:', error);
                }
            };

            getOrder();
        }
    }, [user, id]);

    return (
        <div className='app-content' style={{ backgroundColor: '#fafafa', width: '100vw' }}>
            {order ? (
                <Box p={3} sx={{ borderRadius: '8px' }}>
                    <OrderReceipt order={order} />
                </Box>
            ) : (
                <Typography>Loading...</Typography>
            )}
        </div>
    );
};

export default UserReceiptPage;
