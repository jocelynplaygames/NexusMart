import React, {useState} from 'react';
import { Box, Grid, CardMedia, Typography } from '@mui/material';

const ItemImageGallery = ({ images }) => {
    const [mainImage, setMainImage] = useState(images[0]);

    return (
        <Box>
            <CardMedia
                component="img"
                image={mainImage}
                alt="Main item image"
                sx={{ objectFit: 'cover', maxHeight: '600px' }}
            />
            <Grid container spacing={1} mt={2}>
                {images.map((image, index) => (
                    <Grid item xs={2} key={index}>
                        <CardMedia
                            component="img"
                            image={image}
                            alt={`Thumbnail ${index + 1}`}
                            sx={{ width: '100%', height: '150px', objectFit: 'cover', cursor: 'pointer' }}
                            onClick={() => setMainImage(image)}
                        />
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
};

export default ItemImageGallery;
