import Button from '@mui/material/Button';
import makeComponentStyle from '../../MUI/makeStyle';
import { useLocation, useNavigate } from 'react-router-dom';
import { useCallback } from 'react';

export default function NoBorderBtn({ text, style, link, callBack }) {
    const NoBorderButton = makeComponentStyle(Button, style);
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
            <NoBorderButton variant="text" className='noBorderBtn' onClick={handleClick}>{text}</NoBorderButton>
        </>
    )
}