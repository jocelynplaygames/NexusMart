import React, { useState, useEffect } from 'react';
import ImageUpload from '../../Compoents/SellPage/ImageUpload';
import ListingDetailsForm from '../../Compoents/SellPage/ListingDetailsForm';
import { fetchMainCategories, fetchSubCategories } from '../../service/CategoryService';
import { Box, Button, Grid, MenuItem, TextField, Typography } from '@mui/material';
import { createListing } from '../../service/ListingsService';
import { useNavigate } from 'react-router-dom';
import { useDispatch } from 'react-redux';
import { showSnackbar } from '../../redux/slice/snackbarSlice';

const SellPage = () => {
    const [mainCategories, setMainCategories] = useState([]);
    const [subCategories, setSubCategories] = useState([]);
    const [selectedMainCategory, setSelectedMainCategory] = useState('');
    const [coverImage, setCoverImage] = useState(null);
    const [additionalImages, setAdditionalImages] = useState([]);
    const navigate = useNavigate();
    const dispatch = useDispatch();

    useEffect(() => {
        const loadCategories = async () => {
            const categories = await fetchMainCategories();
            setMainCategories(categories);
            console.log(categories);
        };

        loadCategories();
    }, []);

    const handleMainCategoryChange = async (mainCategoryId) => {
        setSelectedMainCategory(mainCategoryId);
        console.log("main cat changed to: " + mainCategoryId);
        const subCategories = await fetchSubCategories(mainCategoryId);
        setSubCategories(subCategories);
    };

    const handleCoverImageUpload = (file) => {
        setCoverImage(file);
    };

    const handleAdditionalImagesUpload = (files) => {
        setAdditionalImages(files);
    };

    const handleSubmit = async (itemData) => {
        try {
            dispatch(showSnackbar({ open: true, message: 'Creating listing...', severity: 'info' }));
            const newListing = await createListing(itemData, coverImage, additionalImages);
            console.log('New listing created:', newListing);
            dispatch(showSnackbar({ open: true, message: 'Listing created successfully!', severity: 'success' }));
            navigate('/');
        } catch (error) {
            console.error('Failed to create listing:', error);
            const errorMessage = error.response?.data || 'Failed to create listing.';
            dispatch(showSnackbar({ open: true, message: errorMessage, severity: 'error' }));
        }
    };

    return (
        <Box className='app-content' sx={{ paddingTop: '50px', minHeight: 'calc(100vh - 150px)', width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', padding: '0px 80px' }}>
            <div style={{ width: '100%', padding: '60px 0px' }}>
                <Typography variant="h4" fontWeight="bold" mb={1} textAlign='center'>Create a New Listing</Typography>
                <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%', gap: 4 }}>
                    <Box sx={{ width: '15%', minWidth: '200px' }}>
                        <ImageUpload
                            initialCoverImage={coverImage}
                            initialAdditionalImages={additionalImages}
                            onCoverImageUpload={handleCoverImageUpload}
                            onAdditionalImagesUpload={handleAdditionalImagesUpload}
                        />
                    </Box>
                    <ListingDetailsForm
                        mainCategories={mainCategories}
                        subCategories={subCategories}
                        onMainCategoryChange={handleMainCategoryChange}
                        selectedMainCategory={selectedMainCategory}
                        onSubmit={handleSubmit}
                    />
                </Box>
            </div>
        </Box>
    );
};

export default SellPage;