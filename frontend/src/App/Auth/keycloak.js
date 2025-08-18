import Keycloak from 'keycloak-js';

// Keycloak server configuration
// KEYCLOAK_URL: Keycloak server address
// KEYCLOAK_REALM: Keycloak realm
// KEYCLOAK_CLIENT_ID: Keycloak client ID
// KEYCLOAK_CLIENT_SECRET: Keycloak client secret
export const KEYCLOAK_URL = 'http://keycloak.nexusmart-ecommerce.com';
export const KEYCLOAK_REALM = 'NexusMart';
export const KEYCLOAK_CLIENT_ID = 'nexusmart-ecommerce';
export const KEYCLOAK_CLIENT_SECRET = 'g5HFcWL6MHBFnrAe9FO0HhlMipOHzFNZ';

let initOptions = {
    url: KEYCLOAK_URL,
    realm: KEYCLOAK_REALM,
    clientId: KEYCLOAK_CLIENT_ID,
    onLoad: 'check-sso', // Check SSO on page load
    KeycloakResponseType: 'code' // Authorization code flow
}

export const keycloakInstance = new Keycloak(initOptions);
// Keycloak instance for authentication operations
// Available methods:
// keycloakInstance.login() - Redirect to login page
// keycloakInstance.logout() - Logout user
// keycloakInstance.token - Get JWT token for API calls