import React from 'react';
import SearchItem from './SearchItem'; // Update the path to your SearchItem component
import { Grid } from '@mui/material';

const SearchResults = ({ results }) => {
    return (
        <div style={{ flex: '1', padding: '0px 0px 0px 20px', boxSizing: 'border-box' }}>
            <Grid container spacing={2}>
                {results.map((result, index) => (
                    <SearchItem key={index} result={result} />
                ))}
            </Grid>
        </div>
    );
};

export default SearchResults;
