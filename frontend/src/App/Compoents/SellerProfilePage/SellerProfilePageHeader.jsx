import React, { useEffect, useState } from 'react';
import { Box, Avatar, Typography, Rating } from '@mui/material';
import { fetchUserRating } from '../../service/RatingSerivce';

const SellerProfileHeader = ({ seller }) => {
    const [rating, setRating] = useState(0);
    const [numRatings, setNumRatings] = useState(0);

    useEffect(() => {
        const getUserRating = async () => {
            try {
                const ratingData = await fetchUserRating(seller.id);
                setRating(ratingData.totalRating / ratingData.numRatings);
                setNumRatings(ratingData.numRatings);
            } catch (error) {
                console.error('Error fetching user rating:', error);
            }
        };

        getUserRating();
    }, [seller.id]);

    return (
        <Box display="flex" alignItems="center" mb={4}>
            <Avatar src={seller.profilePictureUrl} alt={seller.profilePictureUrl} sx={{ width: 100, height: 100, mr: 2 }} />
            <Box>
                <Typography variant="h5" fontWeight="bold">{seller.username}</Typography>
                <Box display="flex" alignItems="center">
                    <Rating value={rating} readOnly precision={0.1} />
                    {numRatings > 0 && (
                        <Typography variant="body2" color="textSecondary" sx={{ ml: 1 }}>
                            ({rating.toFixed(1)} / {numRatings} ratings)
                        </Typography>
                    )}
                </Box>
                {
                    (seller.address?.city && seller.address?.state && seller.address?.country) ?
                        <Typography variant="body1">{seller.address?.city}, {seller.address?.state}, {seller.address?.country}</Typography>
                        :
                        <div></div>
                }

            </Box>
        </Box>
    );
};

export default SellerProfileHeader;
