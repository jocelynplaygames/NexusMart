import React, { useState, useEffect } from 'react';
import { Box, Typography, IconButton } from '@mui/material';
import UploadIcon from '@mui/icons-material/Upload';
import DeleteIcon from '@mui/icons-material/Delete';

const MAX_IMAGES = 4;

const ImageUpload = ({ initialCoverImage, initialAdditionalImages, onCoverImageUpload, onAdditionalImagesUpload }) => {
    const [coverImage, setCoverImage] = useState(initialCoverImage || null);
    const [additionalImages, setAdditionalImages] = useState(initialAdditionalImages || []);

    useEffect(() => {
        setCoverImage(initialCoverImage);
    }, [initialCoverImage]);

    useEffect(() => {
        setAdditionalImages(initialAdditionalImages);
    }, [initialAdditionalImages]);

    const handleCoverImageChange = (event) => {
        const file = event.target.files[0];
        if (file) {
            setCoverImage(file);
            onCoverImageUpload(file);
        }
    };

    const handleAdditionalImagesChange = (event) => {
        const files = Array.from(event.target.files);
        const newImages = files.slice(0, MAX_IMAGES - additionalImages.length - (coverImage ? 1 : 0));
        setAdditionalImages(prevImages => [...prevImages, ...newImages]);
        onAdditionalImagesUpload([...additionalImages, ...newImages]);
    };

    const handleDeleteCoverImage = () => {
        setCoverImage(null);
        onCoverImageUpload(null);
    };

    const handleDeleteAdditionalImage = (index) => {
        const newImages = additionalImages.filter((_, i) => i !== index);
        setAdditionalImages(newImages);
        onAdditionalImagesUpload(newImages);
    };

    return (
        <Box sx={{paddingTop: '16px'}}>
            <Box mb={2}>
                <Typography variant="h5" mb={1} sx={{ fontWeight: "bold" }}>Cover Image</Typography>
                <Box
                    component="label"
                    display="flex"
                    alignItems="center"
                    justifyContent="center"
                    width="150px"
                    height="150px"
                    border="2px dashed #ddd"
                    borderRadius="4px"
                    position="relative"
                    sx={{ cursor: 'pointer' }}
                >
                    {coverImage ? (
                        <>
                            <img
                                src={typeof coverImage === 'string' ? coverImage : URL.createObjectURL(coverImage)}
                                alt="cover"
                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            />
                            <IconButton
                                onClick={handleDeleteCoverImage}
                                color="secondary"
                                style={{ position: 'absolute', top: 0, right: 0 }}
                            >
                                <DeleteIcon />
                            </IconButton>
                        </>
                    ) : (
                        <>
                            <UploadIcon />
                            <input
                                type="file"
                                hidden
                                accept="image/*"
                                onChange={handleCoverImageChange}
                            />
                        </>
                    )}
                </Box>
            </Box>
            <Box mb={2} sx={{marginTop: '40px'}}>
                <Typography variant="h5" mb={1} sx={{ fontWeight: "bold" }}>Additional Images</Typography>
                <Box display="flex" flexWrap="wrap" gap="30px">
                    {additionalImages.map((image, index) => (
                        <Box
                            key={index}
                            position="relative"
                            width="150px"
                            height="150px"
                            border="2px dashed #ddd"
                            borderRadius="4px"
                            sx={{ cursor: 'pointer', marginRight: index < additionalImages.length - 1 ? '0' : '0' }}
                        >
                            <img
                                src={typeof image === 'string' ? image : URL.createObjectURL(image)}
                                alt={`upload ${index}`}
                                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                            />
                            <IconButton
                                onClick={() => handleDeleteAdditionalImage(index)}
                                color="secondary"
                                style={{ position: 'absolute', top: 0, right: 0 }}
                            >
                                <DeleteIcon />
                            </IconButton>
                        </Box>
                    ))}
                    {additionalImages.length < (MAX_IMAGES - (coverImage ? 1 : 0)) && (
                        <Box
                            component="label"
                            display="flex"
                            alignItems="center"
                            justifyContent="center"
                            width="150px"
                            height="150px"
                            border="2px dashed #ddd"
                            borderRadius="4px"
                            sx={{ cursor: 'pointer' }}
                        >
                            <UploadIcon />
                            <input
                                type="file"
                                hidden
                                accept="image/*"
                                multiple
                                onChange={handleAdditionalImagesChange}
                            />
                        </Box>
                    )}
                </Box>
            </Box>
        </Box>
    );
};

export default ImageUpload;