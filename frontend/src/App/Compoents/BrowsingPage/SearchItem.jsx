import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Grid, Card, CardMedia, CardContent, CardActions, Typography, Button, Box } from '@mui/material';
import StarIcon from '@mui/icons-material/Star';
import StarBorderIcon from '@mui/icons-material/StarBorder';
import StarHalfIcon from '@mui/icons-material/StarHalf';
import { decodeToken } from '../../Auth/JwtUtils';
import { useSelector } from 'react-redux';
import { fetchItemRating } from '../../service/RatingSerivce';
import { fetchSeller } from '../../service/UserService';
import { fetchUserRating } from '../../service/RatingSerivce';

const SearchItem = ({ result }) => {
    const navigate = useNavigate();
    const token = useSelector((state) => state.auth.token);
    const decodedToken = decodeToken(token);
    const currentUserId = decodedToken?.sub;

    const [rating, setRating] = useState(null);
    const [numRatings, setNumRatings] = useState(0);
    const [seller, setSeller] = useState(null);
    const [sellerRating, setSellerRating] = useState(null);
    const [numRatingsSeller, setNumRatingsSeller] = useState(0);

    useEffect(() => {
        const getItemRating = async () => {
            try {
                const ratingData = await fetchItemRating(result.id);
                setRating(ratingData.totalRating / ratingData.numRatings);
                setNumRatings(ratingData.numRatings);
            } catch (error) {
                console.error('Error fetching item rating:', error);
            }
        };

        const getSellerData = async () => {
            try {
                const sellerData = await fetchSeller(result.sellerId);
                setSeller(sellerData);
                const sellerRatingData = await fetchUserRating(sellerData.id);
                setSellerRating(sellerRatingData.totalRating / sellerRatingData.numRatings);
                setNumRatingsSeller(sellerRatingData.numRatings);
            } catch (error) {
                console.error('Error fetching seller data:', error);
            }
        };

        getItemRating();
        getSellerData();
    }, [result.id, result.sellerId]);

    const handleSellerClick = () => {
        if (result.sellerId === currentUserId) {
            navigate('/profile');
        } else {
            navigate(`/seller-profile/${result.sellerId}`);
        }
    };

    const handleItemClick = () => {
        navigate(`/item/${result.id}/0`);
    };

    const renderStars = (averageRating) => {
        if (!averageRating) return (
            <div style={{transform:'translateY(-2.5px)'}}>
                No ratings yet
            </div>
        );
        const stars = [];
        for (let i = 1; i <= 5; i++) {
            if (i <= averageRating) {
                stars.push(<StarIcon key={i} sx={{ color: '#FFD700', fontSize: '1rem' }} />);
            } else if (i - 1 < averageRating && averageRating < i) {
                stars.push(<StarHalfIcon key={i} sx={{ color: '#FFD700', fontSize: '1rem' }} />);
            } else {
                stars.push(<StarBorderIcon key={i} sx={{ color: '#FFD700', fontSize: '1rem' }} />);
            }
        }
        return stars;
    };

    return (
        <Grid item xs={12}>
            <Card>
                <Grid container>
                    <Grid item>
                        <CardMedia
                            component="img"
                            alt={result.title}
                            image={result.imageUrl}
                            sx={{
                                width: '250px',
                                height: '250px',
                                objectFit: 'cover',
                            }}
                        />
                    </Grid>
                    <Grid item xs container direction="column" justifyContent="space-between" paddingBottom={1}>
                        <CardContent sx={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between', flex: '1', paddingBottom: '5px' }}>
                            <div>
                                <Typography variant="h5" sx={{ fontWeight: 'bold' }}>{result.title}</Typography>
                                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                    {renderStars(rating)}
                                    {numRatings > 0 && (
                                        <Typography variant="body2" color="textSecondary" sx={{ marginLeft: 1 }}>
                                            ({rating.toFixed(1)} / {numRatings} ratings)
                                        </Typography>
                                    )}
                                </Box>
                            </div>
                            <div>
                                <Typography sx={{ fontSize: '22px' }} color="primary" >${result.price}</Typography>
                                <Typography variant="body2" color="textSecondary">{result.city}, {result.state}, {result.country}</Typography>
                                <div style={{ display: 'flex', alignItems: 'center', minHeight: '30px' }} >
                                    <Button
                                        size="small"
                                        color="primary"
                                        onClick={handleSellerClick}
                                        sx={{ transform: 'translateY(3px)', marginRight: '10px', paddingLeft: '0px' }}
                                    >
                                        {result.username}
                                    </Button>
                                    <Box sx={{ display: 'flex', transform: 'translateY(3.5px)' }}>
                                        {renderStars(sellerRating)}
                                        {numRatingsSeller > 0 && (
                                            <Typography variant="body2" color="textSecondary" sx={{ marginLeft: 1 }}>
                                                ({sellerRating.toFixed(1)} / {numRatingsSeller} ratings)
                                            </Typography>
                                        )}
                                    </Box>
                                </div>
                            </div>
                        </CardContent>
                        <CardActions>
                            <Button size="small" color="primary" variant='contained' sx={{ marginLeft: '8px' }} onClick={handleItemClick}>View Details</Button>
                        </CardActions>
                    </Grid>
                </Grid>
            </Card>
        </Grid>
    );
};

export default SearchItem;