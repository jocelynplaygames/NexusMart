import React, { useEffect, useState } from 'react';
import { Box, TextField, Button, Typography, MenuItem } from '@mui/material';

const ListingDetailsForm = ({ mainCategories, subCategories, onMainCategoryChange, selectedMainCategory, onSubmit, initialData }) => {
    const [title, setTitle] = useState(initialData?.title || '');
    const [shortDescription, setShortDescription] = useState(initialData?.shortDescription || '');
    const [longDescription, setLongDescription] = useState(initialData?.longDescription || '');
    const [price, setPrice] = useState(initialData?.price || '');
    const [city, setCity] = useState(initialData?.city || '');
    const [state, setState] = useState(initialData?.state || '');
    const [country, setCountry] = useState(initialData?.country || '');
    const [quantity, setQuantity] = useState(initialData?.quantity || '');
    const [selectedSubCategory, setSelectedSubCategory] = useState(initialData?.subCategoryId || '');
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (initialData) {
            setTitle(initialData.title);
            setShortDescription(initialData.shortDescription);
            setLongDescription(initialData.longDescription);
            setPrice(initialData.price);
            setCity(initialData.city);
            setState(initialData.state);
            setCountry(initialData.country);
            setQuantity(initialData.quantity);
            setSelectedSubCategory(initialData.subCategoryId);
        }
    }, [initialData]);

    useEffect(() => {
        if (!initialData || selectedMainCategory !== initialData.mainCategoryId) {
            setSelectedSubCategory('');
        }
    }, [selectedMainCategory, initialData]);

    const handleSubmit = async (event) => {
        event.preventDefault();
        setIsSubmitting(true);
        const itemData = {
            title,
            shortDescription,
            longDescription,
            price,
            city,
            state,
            country,
            quantity,
            mainCategoryId: selectedMainCategory,
            subCategoryId: selectedSubCategory,
        };
        await onSubmit(itemData);
        setIsSubmitting(false);
    };

    return (
        <Box component="form" onSubmit={handleSubmit} sx={{ display: 'flex', flexDirection: 'column', gap: 2, flex: '1' }}>
            <div style={{marginTop: '20px'}}>
                <Typography variant="h6" fontWeight='bold' >Title</Typography>
                <TextField
                    label="Listing Title"
                    fullWidth
                    margin="normal"
                    value={title}
                    multiline
                    rows={2}
                    onChange={(e) => setTitle(e.target.value)}
                />
            </div>
            <Box sx={{ display: 'flex', gap: 4, width: '100%' }}>
                <div style={{ flexDirection: 'column', flex: '1' }}>
                    <Typography variant="h6" fontWeight='bold'>Description</Typography>
                    <Box sx={{ display: 'flex', flex: 1, flexDirection: 'column' }}>
                        <TextField
                            label="Brief Description"
                            fullWidth
                            margin="normal"
                            value={shortDescription}
                            multiline
                            rows={5}
                            onChange={(e) => setShortDescription(e.target.value)}
                        />
                        <TextField
                            label="Long Description"
                            fullWidth
                            margin="normal"
                            multiline
                            rows={20}
                            value={longDescription}
                            onChange={(e) => setLongDescription(e.target.value)}
                        />
                    </Box>
                </div>
                <div style={{ flexDirection: 'column', minWidth: '300px', justifyContent: 'space-between' }}>
                    <div>
                        <Typography variant="h6" fontWeight='bold'>Price & Quantity</Typography>
                        <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <TextField
                                label="Price"
                                type="number"
                                fullWidth
                                margin="normal"
                                value={price}
                                onChange={(e) => setPrice(e.target.value)}
                            />
                            <TextField
                                label="Quantity"
                                type="number"
                                fullWidth
                                margin="normal"
                                value={quantity}
                                onChange={(e) => setQuantity(e.target.value)}
                            />
                        </div>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', marginTop: '33px' }}>
                        <Typography variant="h6" fontWeight='bold'>Item Location</Typography>
                        <div style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                            <TextField
                                label="City"
                                fullWidth
                                margin="normal"
                                value={city}
                                onChange={(e) => setCity(e.target.value)}
                            />
                            <TextField
                                label="State"
                                fullWidth
                                margin="normal"
                                value={state}
                                onChange={(e) => setState(e.target.value)}
                            />
                            <TextField
                                label="Country"
                                fullWidth
                                margin="normal"
                                value={country}
                                sx={{ flex: '1'}}
                                onChange={(e) => setCountry(e.target.value)}
                            />
                        </div>
                    </div>

                    <div style={{ marginTop: '32px' }}>
                        <Typography variant="h6" fontWeight='bold'>Category</Typography>
                        <div style={{ display: 'flex', flexDirection: 'column' }}>
                            <TextField
                                label="Main Category"
                                select
                                fullWidth
                                margin="normal"
                                value={selectedMainCategory}
                                onChange={(e) => onMainCategoryChange(e.target.value)}
                            >
                                {mainCategories.map((category) => (
                                    <MenuItem key={category.id} value={category.id}>
                                        {category.name}
                                    </MenuItem>
                                ))}
                            </TextField>
                            <TextField
                                label="Sub Category"
                                select
                                fullWidth
                                margin="normal"
                                value={selectedSubCategory}
                                onChange={(e) => setSelectedSubCategory(e.target.value)}
                                disabled={!selectedMainCategory}
                            >
                                {Array.isArray(subCategories) ? subCategories.map((subCategory) => (
                                    <MenuItem key={subCategory.id} value={subCategory.id}>
                                        {subCategory.name}
                                    </MenuItem>
                                )) : null}
                            </TextField>
                        </div>
                    </div>

                </div>
            </Box>
            <Button 
                variant="contained" 
                color="primary" 
                size="large" 
                sx={{ mt: 2, alignSelf: 'flex-end' }} 
                type="submit"
                disabled={isSubmitting}
            >
                {isSubmitting ? 'Submitting...' : 'Submit'}
            </Button>
        </Box>
    );
};

export default ListingDetailsForm;