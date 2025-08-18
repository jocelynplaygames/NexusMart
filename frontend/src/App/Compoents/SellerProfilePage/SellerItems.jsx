import React, { useEffect, useState } from 'react';
import { Box, Card, CardContent, CardMedia, Grid, Typography, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { fetchItems } from '../../service/ListingsService';

const sampleItems = [
    {
        id: 1,
        title: 'Item 1',
        description: 'Description for item 1',
        price: '$100',
        image: 'https://via.placeholder.com/250',
        dateListed: '2023-05-01',
    },
    {
        id: 2,
        title: 'Item 2',
        description: 'Description for item 2',
        price: '$200',
        image: 'https://via.placeholder.com/250',
        dateListed: '2023-04-20',
    },
    {
        id: 3,
        title: 'Item 3',
        description: 'Description for item 3',
        price: '$300',
        image: 'https://via.placeholder.com/250',
        dateListed: '2023-03-15',
    },
    {
        id: 4,
        title: 'Item 4',
        description: 'Description for item 4',
        price: '$400',
        image: 'https://via.placeholder.com/250',
        dateListed: '2023-02-01',
    },
];

const SellerItems = ({ seller }) => {
    const navigate = useNavigate();
    const [items, setItems] = useState([]);
    const userId = seller.seller.id;
    
    useEffect(() => {
        const getItems = async () => {
            try {
                if(!userId){
                    return;
                }
                const fetchedItems = await fetchItems(userId);
                setItems(fetchedItems);
                console.log(fetchedItems);
            } 
            catch (error) {
                console.error('Error fetching items:', error);
            }
        };

        getItems();
    }, [userId]);

    return (
        <Box>
            <Grid container spacing={2}>
                {items.map((item) => (
                    <Grid item xs={12} key={item.id}>
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
                                        <Typography variant="body2" color="textSecondary">{item.description}</Typography>
                                        <Typography variant="h6" color="primary">{item.price}</Typography>
                                        <Typography variant="body2" color="textSecondary">Listed on: {new Date(item.dateListed).toLocaleDateString()}</Typography>
                                    </CardContent>
                                    <Box padding={1} paddingLeft={2}>
                                        <Button variant="contained" color="primary" size="small" onClick={() => navigate(`/item/${item.id}/0`)}>View Details</Button>
                                    </Box>
                                </Grid>
                            </Grid>
                        </Card>
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
};

export default SellerItems;


