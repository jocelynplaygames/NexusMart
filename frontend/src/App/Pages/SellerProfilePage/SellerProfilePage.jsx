import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { Container } from '@mui/material';
import SellerProfileHeader from '../../Compoents/SellerProfilePage/SellerProfilePageHeader';
import SellerProfileTabs from '../../Compoents/SellerProfilePage/SellerProfileTabs';
import { fetchSeller } from '../../service/UserService';

const seller = {
    username: 'seller123',
    profilePicture: 'https://via.placeholder.com/100',
    rating: 4.5,
    location: 'New York, USA',
};

const SellerProfilePage = () => {
    const { id } = useParams(); // Extract the user ID from the URL parameters
    const [seller, setSeller] = useState(null);

    useEffect(() => {
        const getSeller = async () => {
            try {
                const fetchedSeller = await fetchSeller(id);
                setSeller(fetchedSeller);
                console.log(fetchedSeller);
            } catch (error) {
                console.error('Error fetching seller data:', error);
            }
        };

        getSeller();
    }, [id]);

    if (!seller) {
        return <div>Loading...</div>;
    }

    return (
        <div className='app-content' style={{ paddingTop: '20px', padding: '50px', width: '100%', boxSizing: 'border-box' }}>
            <div style={{ padding: '50px', boxSizing: 'border-box' }}>
                <SellerProfileHeader seller={seller} />
                <hr style={{ marginBottom: '30px' }} />
                <SellerProfileTabs seller={seller} />
            </div>
        </div>
    );
};

export default SellerProfilePage;
