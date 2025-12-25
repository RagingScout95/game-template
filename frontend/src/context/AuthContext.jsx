import { createContext, useContext, useState, useEffect } from 'react';
import { createGraphQLClient } from '../graphql/client';
import { LOGIN_MUTATION, REGISTER_PLAYER_MUTATION } from '../graphql/queries';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  
  useEffect(() => {
    // Check for stored token
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    
    setLoading(false);
  }, []);
  
  const login = async (username, password) => {
    try {
      const client = createGraphQLClient();
      const data = await client.request(LOGIN_MUTATION, {
        input: { username, password }
      });
      
      const { token: newToken, user: newUser } = data.login;
      
      setToken(newToken);
      setUser(newUser);
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(newUser));
      
      return { success: true };
    } catch (error) {
      console.error('Login error:', error);
      return { success: false, error: error.message };
    }
  };
  
  const register = async (username, password, name, empId) => {
    try {
      const client = createGraphQLClient();
      const data = await client.request(REGISTER_PLAYER_MUTATION, {
        input: { username, password, name, empId }
      });
      
      const { token: newToken, user: newUser } = data.registerPlayer;
      
      setToken(newToken);
      setUser(newUser);
      localStorage.setItem('token', newToken);
      localStorage.setItem('user', JSON.stringify(newUser));
      
      return { success: true };
    } catch (error) {
      console.error('Registration error:', error);
      return { success: false, error: error.message };
    }
  };
  
  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };
  
  const getAuthenticatedClient = () => {
    return createGraphQLClient(token);
  };
  
  return (
    <AuthContext.Provider value={{
      user,
      token,
      login,
      register,
      logout,
      loading,
      isAuthenticated: !!token,
      getAuthenticatedClient
    }}>
      {children}
    </AuthContext.Provider>
  );
};

