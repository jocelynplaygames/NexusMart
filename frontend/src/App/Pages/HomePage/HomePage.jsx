import SearchBar from "../../Compoents/SearchBar/SearchBar";
import CategoryCardHomePage from '../../Compoents/HomePage/CategoryCardHomePage';
import '../../../App.css'
import React, { useState, useEffect } from 'react';
import { fetchMainCategories } from "../../service/CategoryService";
import HomePageCarousal from "../../Compoents/HomePage/HomePageCarsoual";
import { useLocation } from "react-router-dom";
import { fetchTrendingTodayItems, fetchItemsOnSale, fetchJustForYouItems } from "../../service/RecommendationService";

export default function HomePage() {
    const [mainCategories, setMainCategories] = useState([]);
    const [selectedCategory, setSelectedCategory] = useState(null);
    const [trendingItems, setTrendingItems] = useState([]);
    const [itemsOnSale, setItemsOnSale] = useState([]);
    const [justForYouItems, setJustForYouItems] = useState([]);

    const location = useLocation();
    const query = new URLSearchParams(location.search);

    useEffect(() => {
        const loadCategories = async () => {
            const categories = await fetchMainCategories();
            setMainCategories(categories);
            if (categories.length > 0) {
                setSelectedCategory(categories[0].id);
            }
        };

        const loadRecommendations = async () => {
            try {
                const [trending, sale, justForYou] = await Promise.all([
                    fetchTrendingTodayItems(),
                    fetchItemsOnSale(),
                    fetchJustForYouItems()
                ]);
                setTrendingItems(trending);
                setItemsOnSale(sale);
                setJustForYouItems(justForYou);
            } catch (error) {
                console.error("Error fetching recommendation items:", error);
            }
        };

        loadCategories();
        loadRecommendations();
    }, []);

    return (
        <div className="app-content" style={{ paddingTop: '30px', backgroundColor: 'rgba(0, 0, 0, 0)' }}>
            <div style={{ margin: '20px 0px' }}>
                <SearchBar 
                    params={query}
                    initialQuery={query.get('query') || ''} 
                    countryQuery={query.get('country') || ''}
                    stateQuery={query.get('state') || ''}
                    minPriceQuery={query.get('minPrice') || ''}
                    maxPriceQuery={query.get('maxPrice') || ''}
                    mainCategoryQuery={query.get('mainCategory') || 'all'}
                    subCategoryQuery={query.get('subCategory') || 'all'}
                />
            </div>
            <CategoryCardHomePage
                categories={mainCategories}
                onSelectCategory={setSelectedCategory}
                selectedCategory={selectedCategory}
            />
            <div style={{ display: 'flex', flexDirection: 'column', margin: '10px 0px 60px 0px' }}>
                <HomePageCarousal title="Trending Today" items={trendingItems} />
                <HomePageCarousal title="Items On Sale" items={itemsOnSale} />
                <HomePageCarousal title="Just For You" items={justForYouItems} />
            </div>
        </div>
    );
}