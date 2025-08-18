import Button from '@mui/material/Button';
import makeComponentStyle from '../../MUI/makeStyle';
import { useNavigate } from 'react-router-dom';
import { useCallback } from 'react';

export default function FilledBgBtn({text, style, link, callBack}){
    const FilledBgButton = makeComponentStyle(Button, style);
    const navigate = useNavigate();

    const handleClick = useCallback(() => {
        if (link) {
            if (link.absolute) {
                window.open(link.url, '_blank');
            } 
            else {
                navigate(link.url);
            }
        }
        if (callBack) {
            callBack();
        }
    }, [link, callBack, navigate]);

    return (
        <>
            <FilledBgButton variant="contained" onClick={handleClick}>{text}</FilledBgButton>
        </>   
    )
}