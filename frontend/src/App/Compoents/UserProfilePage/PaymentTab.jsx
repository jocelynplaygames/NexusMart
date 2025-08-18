import React, { useState, useEffect } from 'react';
import { Box, Button, Typography, Grid } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import CardInfoCard from './CardInfoCard';
import { fetchUserCards, createUserCard, updateUserCard, deleteUserCard } from '../../service/UserService';
import { decodeToken } from '../../Auth/JwtUtils';
import { v4 as uuidv4 } from 'uuid';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import { useDispatch } from 'react-redux';
import { useSelector } from 'react-redux';

const PaymentTab = () => {
    const [cards, setCards] = useState([]);
    const token = useSelector((state) => state.auth.token);
    const dispatch = useDispatch();

    useEffect(() => {
        const loadCards = async () => {
            try {
                let userCards = await fetchUserCards(decodeToken(token).sub);
                userCards = userCards.map((userCard) => ({
                    ...userCard,
                    tempId: uuidv4(),
                }));

                setCards(userCards);
            } catch (error) {
                console.error('Failed to load card:', error);
                const errorMessage = error.response?.data || 'load card.';
                dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
            }
        };

        if(token){
            loadCards();
        }
    }, [token]);

    const handleAddNewCardComponent = () => {
        const newCard = { tempId: uuidv4(), cardholderName: '', cardNumber: '', expirationDate: '', billingAddress: '', type: '' };
        setCards([...cards, newCard]);
    };

    const handleAddCard = async (newCard) => {
        try {
            await createUserCard(decodeToken(token).sub, newCard);
            setCards(cards.map(card => (card.tempId === newCard.tempId ? newCard : card)));
            dispatch(showSnackbar({ open: true, message: 'Card added successfully.', severity: 'success' }));
        } catch (error) {
            console.error('Failed to add card:', error);
            const errorMessage = error.response?.data || 'Failed to add card.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        }
    };

    const handleEditCard = async (updatedCard) => {
        try {
            await updateUserCard(decodeToken(token).sub, updatedCard.id, updatedCard);
            setCards(cards.map(card => (card.tempId === updatedCard.tempId ? updatedCard : card)));
            dispatch(showSnackbar({ open: true, message: 'Card updated successfully.', severity: 'success' }));
        } catch (error) {
            console.error('Failed to update card:', error);
            const errorMessage = error.response?.data || 'Failed to update card.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        }
    };

    const handleRemoveCard = async (id, tempId) => {
        try {
            if(id){
                await deleteUserCard(decodeToken(token).sub, id);
            }
            setCards(cards.filter(card => card.tempId !== tempId));
            if(id){
                dispatch(showSnackbar({ open: true, message: 'Card deleted successfully.', severity: 'success' }));
            }
        } catch (error) {
            console.error('Failed to delete card:', error);
            const errorMessage = error.response?.data || 'Failed to delete card.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        }
    };

    return (
        <Box component="form" mt={2}>
            {cards.map((card) => (
                <CardInfoCard
                    key={card.tempId}
                    card={card}
                    onEdit={handleEditCard}
                    onAdd={handleAddCard}
                    onDelete={handleRemoveCard}
                />
            ))}
            <Button variant="contained" color="primary" onClick={handleAddNewCardComponent} startIcon={<AddIcon />} sx={{ mb: 2, float: 'right', marginTop: '10px' }}>
                Add A New Card
            </Button>
        </Box>
    );
};

export default PaymentTab;