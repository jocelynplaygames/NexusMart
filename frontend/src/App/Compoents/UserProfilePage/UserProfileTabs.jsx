import React, { useState, useEffect } from 'react';
import { Box, Tabs, Tab } from '@mui/material';
import ContactTab from './ContactTab';
import FeedbackTab from './FeedbackTab';
import PaymentTab from './PaymentTab';
import OrderHistory from './OrderHistory';
import ItemsOnSale from './ItemsOnSale';
import { useNavigate, useLocation } from 'react-router-dom';
import { useSelector } from 'react-redux';

const UserProfileTabs = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const query = new URLSearchParams(location.search);
    const initialTabIndex = parseInt(query.get('tab'), 10) || 0;
    const user = useSelector((state) => state.auth.user);
    const userId = user?.id;
    const [tabIndex, setTabIndex] = useState(initialTabIndex);

    useEffect(() => {
        setTabIndex(initialTabIndex);
    }, [initialTabIndex]);

    const handleTabChange = (event, newValue) => {
        setTabIndex(newValue);
        navigate(`/profile?tab=${newValue}`);
    };

    return (
        <Box>
            <Tabs value={tabIndex} onChange={handleTabChange}>
                <Tab label="Contact" />
                <Tab label="Payment" />
                <Tab label="Items On Sale" />
                <Tab label="Order History" />
                <Tab label="Feedback From Buyers" />
                <Tab label="Feedback To Sellers" />
            </Tabs>
            {tabIndex === 0 && <ContactTab />}
            {tabIndex === 1 && <PaymentTab />}
            {tabIndex === 2 && <ItemsOnSale />}
            {tabIndex === 3 && <OrderHistory />}
            {tabIndex === 4 && <FeedbackTab seller={true} userId={userId}/>}
            {tabIndex === 5 && <FeedbackTab seller={false} userId={userId}/>}
        </Box>
    );
};

export default UserProfileTabs;