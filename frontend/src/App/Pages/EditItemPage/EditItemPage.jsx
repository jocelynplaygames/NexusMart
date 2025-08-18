import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Grid, Button, Box } from '@mui/material';
import ImageUpload from '../../Compoents/SellPage/ImageUpload';
import ListingDetailsForm from '../../Compoents/SellPage/ListingDetailsForm';
import ConfirmDeletion from '../../Compoents/ConfirmDialog/ConfirmDeletion';
import { fetchItem, updateItem, deleteItem } from '../../service/ListingsService';
import { fetchMainCategories, fetchSubCategories } from '../../service/CategoryService';
import { showSnackbar } from '../../redux/slice/snackbarSlice';
import { useDispatch } from 'react-redux';

const EditItemPage = () => {
    const { itemId } = useParams();
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const [mainCategories, setMainCategories] = useState([]);
    const [subCategories, setSubCategories] = useState([]);
    const [selectedMainCategory, setSelectedMainCategory] = useState('');
    const [coverImage, setCoverImage] = useState(null);
    const [additionalImages, setAdditionalImages] = useState([]);
    const [itemData, setItemData] = useState(null);
    const [openDialog, setOpenDialog] = useState(false);

    useEffect(() => {
        const loadCategories = async () => {
            const categories = await fetchMainCategories();
            setMainCategories(categories);
        };

        const loadItem = async () => {
            try {
                const item = await fetchItem(itemId); // Ensure itemId is correctly passed as a Long
                setItemData(item);
                setSelectedMainCategory(item.mainCategoryId);
                const subCategories = await fetchSubCategories(item.mainCategoryId);
                setSubCategories(subCategories);
                setCoverImage(item.imageUrl);
                setAdditionalImages([item.subImageUrl1, item.subImageUrl2, item.subImageUrl3, item.subImageUrl4].filter(Boolean));
            } catch (error) {
                console.error('Failed to fetch item:', error);
            }
        };

        loadCategories();
        loadItem();
    }, [itemId]);

    const handleMainCategoryChange = async (mainCategoryId) => {
        setSelectedMainCategory(mainCategoryId);
        const subCategories = await fetchSubCategories(mainCategoryId);
        setSubCategories(subCategories);
    };

    const handleCoverImageUpload = (file) => {
        setCoverImage(file);
    };

    const handleAdditionalImagesUpload = (files) => {
        setAdditionalImages(files);
    };

    const handleSubmit = async (updatedItemData) => {
        try {
            dispatch(showSnackbar({ open: true, message: 'Updating listing...', severity: 'info' }));
            console.log('Additional images updated:', additionalImages);
            updatedItemData.imageUrl = itemData.imageUrl;
            const updatedListing = await updateItem(itemId, updatedItemData, coverImage, additionalImages);
            console.log('Listing updated:', updatedListing);
            dispatch(showSnackbar({ open: true, message: 'Listing updated successfully!', severity: 'success' }));
            navigate('/');
        }
        catch (error) {
            console.error('Failed to update listing:', error);
            const errorMessage = error.response?.data || 'Failed to update listing.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        }
    };

    const handleDelete = async () => {
        try {
            await deleteItem(itemId);
            dispatch(showSnackbar({ open: true, message: 'Listing deleted successfully', severity: 'success' }));
            navigate('/user/listings');
        } catch (error) {
            console.error('Failed to delete listing:', error);
            const errorMessage = error.response?.data || 'Failed to delete listing.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        } finally {
            setOpenDialog(false);
        }
    };

    const handleOpenDialog = () => {
        setOpenDialog(true);
    };

    const handleCloseDialog = () => {
        setOpenDialog(false);
    };

    if (!itemData) {
        return <Typography>Loading...</Typography>;
    }

    return (
        <Box className='app-content' sx={{ paddingTop: '50px', minHeight: 'calc(100vh - 150px)', width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '0px 80px' }}>
            <div style={{ width: '100%', padding: '60px 0px 0px 0px' }}>
                <Typography variant="h4" fontWeight="bold" mb={1} textAlign='center'>Edit Listing</Typography>
                <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', gap: 4 }}>
                    <Box sx={{ width: '15%', minWidth: '200px', marginRight: '20px' }}>
                        <ImageUpload
                            initialCoverImage={coverImage}
                            initialAdditionalImages={additionalImages}
                            onCoverImageUpload={handleCoverImageUpload}
                            onAdditionalImagesUpload={handleAdditionalImagesUpload}
                        />
                    </Box>
                    <div style={{ flex: '1' }}>
                        <ListingDetailsForm
                            mainCategories={mainCategories}
                            subCategories={subCategories}
                            onMainCategoryChange={handleMainCategoryChange}
                            selectedMainCategory={selectedMainCategory}
                            onSubmit={handleSubmit}
                            initialData={itemData}
                        />
                        <Box display="flex" justifyContent="flex-end" mt={2}>
                            <Button variant="contained" color="error" onClick={handleOpenDialog} sx={{ marginRight: '158px', height: '42.25px', transform: 'translateY(-58px)' }}>
                                Delete Listing
                            </Button>
                        </Box>
                    </div>
                </Box>
            </div>
            <ConfirmDeletion
                open={openDialog}
                onClose={handleCloseDialog}
                onConfirm={handleDelete}
                title="Confirm Deletion"
                message="Are you sure you want to delete this item? This action cannot be undone."
            />
        </Box>
    );
};

export default EditItemPage;