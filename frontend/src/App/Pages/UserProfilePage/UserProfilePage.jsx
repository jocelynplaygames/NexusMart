import React, {useEffect} from 'react';
import { Container, Box } from '@mui/material';
import UserProfileHeader from '../../Compoents/UserProfilePage/UserProfileHeader';
import UserProfileTabs from '../../Compoents/UserProfilePage/UserProfileTabs';


const UserProfilePage = () => {

    return (
        <div className='app-content' style={{ paddingTop: '20px', padding: '50px', width: '100%', boxSizing: 'border-box' }}>
            <div style={{padding: '50px', boxSizing: 'border-box'}}>
                <UserProfileHeader />
                < hr style={{marginBottom: '30px'}} />
                <UserProfileTabs />
            </div>
        </div>
    );
};

export default UserProfilePage;
