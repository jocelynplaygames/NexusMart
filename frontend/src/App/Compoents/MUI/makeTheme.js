import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  typography: {
    // Define global text settings
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    fontSize: 14,
    h1: {
      fontSize: '2.5rem', // Example for h1 size
    },
    body1: {
      fontSize: '1rem', // Example for body text size
    },
    // Add other global styles for different text variants if needed
  },
});

export default theme;

