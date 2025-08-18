import React, { useState } from 'react';
import { Box, Typography, IconButton, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button, TextField, MenuItem, Select, FormControl, InputLabel } from '@mui/material';
import { Formik, Form, Field, ErrorMessage } from 'formik';
import * as Yup from 'yup';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';

const validationSchema = Yup.object({
    cardholderName: Yup.string().required('Cardholder Name is required'),
    cardNumber: Yup.string().required('Card Number is required').matches(/^[0-9]+$/, 'Card Number must be digits').min(16, 'Card Number must be 16 digits').max(16, 'Card Number must be 16 digits'),
    expirationDate: Yup.string().required('Expiration Date is required').matches(/^(0[1-9]|1[0-2])\/\d{2}$/, 'Expiration Date must be in MM/YY format'),
    billingAddress: Yup.string().required('Billing Address is required'),
    type: Yup.string().required('Card Type is required'),
});

const cardTypes = [
    { value: 'Visa', label: 'Visa' },
    { value: 'MasterCard', label: 'MasterCard' },
    { value: 'AmericanExpress', label: 'American Express' },
];

const CardInfoCard = ({ card, onEdit, onAdd, onDelete }) => {
    const [editOpen, setEditOpen] = useState(false);

    const handleEditOpen = () => {
        setEditOpen(true);
    };

    const handleEditClose = () => {
        setEditOpen(false);
    };

    const handleSave = (values) => {
        if(card.id){
            handleEditSave(values);
        }
        else{
            handleAddSave(values);
        }
        
    }

    const handleAddSave = (values) => {
        const [month, year] = values.expirationDate.split('/');
        const expirationDate = `${month}/${year}`;
        onAdd({ ...card, ...values, expirationDate });
        handleEditClose();
    };


    const handleEditSave = (values) => {
        const [month, year] = values.expirationDate.split('/');
        const expirationDate = `${month}/${year}`;
        onEdit({ ...card, ...values, expirationDate });
        handleEditClose();
    };

    const renderError = (message) => (
        <span style={{ fontSize: '12px', color: 'red', height: '1.5em', userSelect: 'none' }}>
            {message || ' '}
        </span>
    );

    return (
        <Box sx={{ boxShadow: 2, border: '1px solid #E2E2E2' }} borderRadius={4} p={2} mb={2}>
            <Typography variant="subtitle1">
                {card.cardholderName ?
                    `${card.cardholderName}'s card ending ${card.cardNumber.slice(-4)}`
                    :
                    'New Card'
                }
            </Typography>
            <Box display="flex" justifyContent="space-between" mt={1}>
                <IconButton color="primary" onClick={handleEditOpen}>
                    <EditIcon />
                </IconButton>
                <IconButton color="secondary" onClick={() => onDelete(card.id, card.tempId)}>
                    <DeleteIcon />
                </IconButton>
            </Box>

            <Dialog open={editOpen} onClose={handleEditClose}>
                <DialogTitle>Edit Card Info</DialogTitle>
                <DialogContent>
                    <DialogContentText>
                        Please enter your card information and billing address.
                    </DialogContentText>
                    <Formik
                        initialValues={{
                            cardholderName: card.cardholderName || '',
                            cardNumber: card.cardNumber || '',
                            expirationDate: card.expirationDate ? card.expirationDate.substring(5, 7) + '/' + card.expirationDate.substring(2, 4) : '',
                            billingAddress: card.billingAddress || '',
                            type: card.type || '',
                        }}
                        validationSchema={validationSchema}
                        onSubmit={handleSave}
                    >
                        {({ isSubmitting, setFieldValue, values }) => (
                            <Form>
                                <Field
                                    as={TextField}
                                    name="cardholderName"
                                    label="Cardholder Name"
                                    fullWidth
                                    margin="normal"
                                    helperText={<ErrorMessage name="cardholderName" render={renderError} />}
                                    error={Boolean(ErrorMessage.name === "cardholderName")}
                                />
                                <Field
                                    as={TextField}
                                    name="cardNumber"
                                    label="Card Number"
                                    fullWidth
                                    margin="normal"
                                    type="text"
                                    helperText={<ErrorMessage name="cardNumber" render={renderError} />}
                                    error={Boolean(ErrorMessage.name === "cardNumber")}
                                />
                                <Field
                                    as={TextField}
                                    name="expirationDate"
                                    label="Expiration Date"
                                    fullWidth
                                    margin="normal"
                                    placeholder="MM/YY"
                                    helperText={<ErrorMessage name="expirationDate" render={renderError} />}
                                    error={Boolean(ErrorMessage.name === "expirationDate")}
                                />
                                <Field
                                    as={TextField}
                                    name="billingAddress"
                                    label="Billing Address"
                                    fullWidth
                                    margin="normal"
                                    helperText={<ErrorMessage name="billingAddress" render={renderError} />}
                                    error={Boolean(ErrorMessage.name === "billingAddress")}
                                />
                                <FormControl fullWidth margin="normal">
                                    <InputLabel id="type-label">Card Type</InputLabel>
                                    <Select
                                        labelId="type-label"
                                        id="type"
                                        name="type"
                                        value={values.type}
                                        onChange={(e) => setFieldValue('type', e.target.value)}
                                        label="Card Type"
                                    >
                                        {cardTypes.map((type) => (
                                            <MenuItem key={type.value} value={type.value}>
                                                {type.label}
                                            </MenuItem>
                                        ))}
                                    </Select>
                                    <span style={{marginLeft: '12px'}}>
                                        <ErrorMessage name="type" render={renderError} />
                                    </span>
                                </FormControl>
                                <DialogActions>
                                    <Button onClick={handleEditClose} color="primary">
                                        Cancel
                                    </Button>
                                    <Button type="submit" color="primary" disabled={isSubmitting}>
                                        Save
                                    </Button>
                                </DialogActions>
                            </Form>
                        )}
                    </Formik>
                </DialogContent>
            </Dialog>
        </Box>
    );
};

export default CardInfoCard;