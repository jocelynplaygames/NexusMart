import { Box, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom'; // Ensure you are using react-router-dom for navigation

const CartTotal = ({ total }) => {
    const navigate = useNavigate();

    const handleCheckout = () => {
        if (total > 0) {
            navigate('/checkout');
        }
    };

    return (
        <Box borderColor="grey.300" borderRadius={4} p={2} width="300px" textAlign="center" mb={4} sx={{display: 'flex', flexDirection:'column', border: '2px #dedede solid', backgroundColor: '#dedede'}}>
            <Typography variant="h4">Item(s)</Typography>
            <Typography variant="h6" my={1}>Subtotal: ${total.toFixed(2)}</Typography>
            <Typography variant="h6" mb={2}>Total: ${total.toFixed(2)}</Typography>
            <Button 
                variant="contained" 
                color="primary" 
                size="large" 
                fullWidth 
                sx={{ marginTop:'auto', borderRadius: '15px' }}
                disabled={total === 0} // Disable the button if total is 0
                onClick={handleCheckout} // Handle button click
            >
                Go to checkout
            </Button>
        </Box>
    );
};

export default CartTotal;
