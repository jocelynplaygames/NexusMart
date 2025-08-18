import React, { useState, useEffect } from 'react';
import { Box } from '@mui/material';
import { fetchSubCategories } from '../../service/CategoryService';
import CategoryItemCard from './CategoryItemCard';
import { useNavigate } from 'react-router-dom';

const BrowseCategory = ({ mainCategoryId }) => {
    const [subCategories, setSubCategories] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        if (mainCategoryId) {
            const loadSubCategories = async () => {
                const subCats = await fetchSubCategories(mainCategoryId);
                setSubCategories(subCats);
            };

            loadSubCategories();
        }
    }, [mainCategoryId]);

    if (!mainCategoryId) {
        return null;
    }

    const handleCategoryClick = (subCategoryId) => {
        navigate(`/browse?mainCategory=${mainCategoryId}&subCategory=${subCategoryId}`);
    };

    return (
        <Box
            sx={{
                borderRadius: '25px',
                width: '100%',
                boxSizing: 'border-box',
                display: 'flex',
                flexWrap: 'wrap',
                gap: '16px',
                padding: '16px'
            }}
        >
            {subCategories.map((subCategory) => (
                <Box key={subCategory.id} >
                    <CategoryItemCard
                        item={{ image: subCategory.imageUrl, title: subCategory.name, subtitle: '' }}
                        onClick={() => handleCategoryClick(subCategory.id)}
                    />
                </Box>
            ))}
        </Box>
    );
};

export default BrowseCategory;
