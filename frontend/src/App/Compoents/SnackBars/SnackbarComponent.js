import React from 'react';
import { useSelector, useDispatch } from 'react-redux';
import { Snackbar, Alert } from '@mui/material';
import { closeSnackbar } from '../../redux/slice/snackbarSlice';

const SnackbarComponent = () => {
    const dispatch = useDispatch();
    const { open, message, severity, vertical, horizontal, autoHideDuration } = useSelector((state) => state.snackbar);

    const handleClose = () => {
        dispatch(closeSnackbar());
    };

    return (
        <Snackbar
            open={open}
            autoHideDuration={autoHideDuration}
            onClose={handleClose}
            anchorOrigin={{ vertical, horizontal }}
        >
            <Alert onClose={handleClose} severity={severity} sx={{ width: '100%' }}>
                {message}
            </Alert>
        </Snackbar>
    );
};

export default SnackbarComponent;
