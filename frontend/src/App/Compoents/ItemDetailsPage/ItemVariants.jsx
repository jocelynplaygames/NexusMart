import React from 'react';
import { Box, Grid, Card, CardContent, Typography } from '@mui/material';

const ItemVariants = ({ variants }) => {
    return (
        <Box mt={4}>
            <Typography variant="h6" mb={2}>Variants</Typography>
            <Grid container spacing={2}>
                {variants.map((variant, index) => (
                    <Grid item xs={3} key={index}>
                        <Card>
                            <CardContent>
                                <Typography variant="body2">{variant}</Typography>
                            </CardContent>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
};

export default ItemVariants;
