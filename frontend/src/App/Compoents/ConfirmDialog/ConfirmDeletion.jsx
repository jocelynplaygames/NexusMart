import React from 'react';
import { Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, Button } from '@mui/material';

const ConfirmDeletion = ({ open, onClose, onConfirm, title, message }) => {
    return (
        <Dialog open={open} onClose={onClose} >
            <div style={{padding: '10px'}}>
                <DialogTitle>{title}</DialogTitle>
                <DialogContent sx={{ paddingBottom: '5px' }}>
                    <DialogContentText>{message}</DialogContentText>
                </DialogContent>
                <DialogActions sx={{ paddingRight: '20px' }}>
                    <Button onClick={onClose} color="primary">
                        Cancel
                    </Button>
                    <Button onClick={onConfirm} color="secondary">
                        Delete
                    </Button>
                </DialogActions>
            </div>
        </Dialog>
    );
};

export default ConfirmDeletion;
