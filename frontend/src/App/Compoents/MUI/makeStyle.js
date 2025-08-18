// File: frontend/src/App/Compoents/MUI/makeStyle.js
import { makeStyles } from '@mui/styles';

export default function makeComponentStyle(Component, style) {
    if (!style) {
        return Component;
    }

    // const filteredStyle = Object.keys(style).reduce((acc, key) => {
    //     if (style[key] !== undefined) {
    //         acc[key] = style[key];
    //     }
    //     return acc;
    // }, {});

    const StyledComponent = styled(Component)({
        ...style,
    });

    return StyledComponent;
}


