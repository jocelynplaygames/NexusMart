import React, { useEffect, useState } from 'react';
import { Grid, Card, CardMedia, CardContent, Typography, CardActions, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle } from '@mui/material';
import { useNavigate, useParams } from 'react-router-dom';
import { fetchItems, deleteItem } from '../../service/ListingsService';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import { useDispatch } from 'react-redux';
import ConfirmDeletion from '../ConfirmDialog/ConfirmDeletion';
import emptyBox from '../../assets/images/profilePage/emptyBox.png'
import { useSelector } from 'react-redux';

function ItemsOnSale() {
    const [items, setItems] = useState([]);
    const [openDialog, setOpenDialog] = useState(false);
    const [itemToDelete, setItemToDelete] = useState(null);
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const userId = useSelector(state => state.auth.user?.id);

    useEffect(() => {
        const loadItems = async () => {
            try {
                const itemsData = await fetchItems(userId);
                setItems(itemsData);
            } catch (error) {
                console.error('Failed to fetch items:', error);
            }
        };

        loadItems();
    }, [userId]);

    const handleEdit = (itemId) => {
        navigate(`/item/${itemId}/edit`);
    };

    const handleDelete = async () => {
        try {
            await deleteItem(itemToDelete);
            setItems(items.filter(item => item.id !== itemToDelete));
            dispatch(showSnackbar({ open: true, message: 'Item deleted successfully', severity: 'success' }));
        } catch (error) {
            console.error('Failed to delete item:', error);
            dispatch(showSnackbar({ open: true, message: 'Failed to delete item', severity: 'error' }));
        } finally {
            setOpenDialog(false);
            setItemToDelete(null);
        }
    };

    const handleOpenDialog = (itemId) => {
        setItemToDelete(itemId);
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
        setItemToDelete(null);
    };

    return (
        <>
            {
                items.length > 0 ? <Grid container spacing={2} sx={{ marginTop: '5px' }}>
                    {items.map(item => (
                        <Grid item key={item.id} xs={12}>
                            <Card>
                                <Grid container>
                                    <Grid item>
                                        <CardMedia
                                            component="img"
                                            alt={item.title}
                                            image={item.imageUrl}
                                            sx={{
                                                width: '230px',
                                                height: '230px',
                                                objectFit: 'cover',
                                            }}
                                        />
                                    </Grid>
                                    <Grid item xs container direction="column" justifyContent="space-between" paddingBottom={1}>
                                        <CardContent>
                                            <Typography variant="h6">{item.title}</Typography>
                                            <Typography variant="body2" color="textSecondary">{item.shortDescription}</Typography>
                                            <Typography variant="h6" color="primary">${item.price}</Typography>
                                            <Typography variant="body2" color="textSecondary">Listed on: {new Date(item.dateListed).toLocaleDateString()}</Typography>
                                        </CardContent>
                                        <CardActions>
                                            <Button size="small" color="primary" onClick={() => navigate(`/item/${item.id}/0`)}>View Details</Button>
                                            <Button size="small" color="secondary" onClick={() => handleEdit(item.id)}>Edit</Button>
                                            <Button size="small" color="error" onClick={() => handleOpenDialog(item.id)}>Delete</Button>
                                        </CardActions>
                                    </Grid>
                                </Grid>
                            </Card>
                        </Grid>
                    ))}
                    <ConfirmDeletion
                        open={openDialog}
                        onClose={handleCloseDialog}
                        onConfirm={handleDelete}
                        title="Confirm Deletion"
                        message="Are you sure you want to delete this item? This action cannot be undone."
                    />
                </Grid>

                    :
                    <div style={{display: 'flex', flexDirection: 'column', width: '100%', alignItems: 'center', marginTop: '50px'}}>
                        <img src={emptyBox} alt="" style={{width: '300px'}} />
                        <Button variant="contained" sx={{marginTop: '20px'}} onClick={() => navigate('/sell')}>
                            Post A Listing
                        </Button>
                    </div>
            }
        </>
    );
}

export default ItemsOnSale;