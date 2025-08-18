import { Box, Typography, RadioGroup, FormControlLabel, Radio, Button } from '@mui/material';
import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const PaymentSelection = ({ cards }) => {
    const [paymentMethod, setPaymentMethod] = React.useState(cards.length > 0 ? cards[0].id : '');
    
    useEffect(() => {
        if (cards.length > 0) {
            setPaymentMethod(cards[0].id);
        }
    }, [cards]);

    const handlePaymentChange = (event) => {
        setPaymentMethod(event.target.value);
    };

    const navigate = useNavigate();

    return (
        <Box marginBottom={3}>
            <Typography variant="h6" fontWeight='bold'>Pay with</Typography>
            <RadioGroup value={paymentMethod} onChange={handlePaymentChange}>
                {cards.map((card, index) => (
                    <FormControlLabel 
                        key={index} 
                        value={card.id} 
                        control={<Radio />} 
                        label={`${card.cardholderName}'s ${card.type} card ending in ${card.cardNumber.slice(-4)}`} 
                    />
                ))}
            </RadioGroup>
            <Button variant="outlined" onClick={() => navigate('/profile?tab=1')} sx={{ marginTop: '10px' }}>
                {cards.length > 0 ? 'Add new payment method' : 'No cards found, please add a card to checkout'}
            </Button>
        </Box>
    );
};

export default PaymentSelection;