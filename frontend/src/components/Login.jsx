import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';

export default function Login() {
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    name: '',
    empId: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const { login, register } = useAuth();
  const navigate = useNavigate();
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      let result;
      if (isLogin) {
        result = await login(formData.username, formData.password);
      } else{
        result = await register(
          formData.username,
          formData.password,
          formData.name,
          formData.empId
        );
      }
      
      if (result.success) {
        navigate('/games');
      } else {
        setError(result.error || 'An error occurred');
      }
    } catch (err) {
      setError('An unexpected error occurred');
    } finally {
      setLoading(false);
    }
  };
  
  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };
  
  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="bg-[#181818] border-2 border-gray-600 p-8 w-full max-w-md">
        <h1 className="text-3xl font-bold mb-6 text-center">
          {isLogin ? 'Login' : 'Register'}
        </h1>
        
        {error && (
          <div className="bg-red-900/50 border border-red-500 text-red-200 p-3 mb-4">
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-2">Username</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 bg-[#212121] border-2 border-gray-600 
                       focus:outline-none focus:border-gray-400"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium mb-2">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              className="w-full px-4 py-2 bg-[#212121] border-2 border-gray-600 
                       focus:outline-none focus:border-gray-400"
            />
          </div>
          
          {!isLogin && (
            <>
              <div>
                <label className="block text-sm font-medium mb-2">Full Name</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 bg-dark-primary border-2 border-gray-600 
                           focus:outline-none focus:border-gray-400"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Employee ID</label>
                <input
                  type="text"
                  name="empId"
                  value={formData.empId}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-2 bg-dark-primary border-2 border-gray-600 
                           focus:outline-none focus:border-gray-400"
                />
              </div>
            </>
          )}
          
          <button
            type="submit"
            disabled={loading}
            className="w-full btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {loading ? 'Loading...' : (isLogin ? 'Login' : 'Register')}
          </button>
        </form>
        
        <div className="mt-6 text-center">
          <button
            onClick={() => {
              setIsLogin(!isLogin);
              setError('');
            }}
            className="text-gray-400 hover:text-white underline"
          >
            {isLogin ? 'Need an account? Register' : 'Already have an account? Login'}
          </button>
        </div>
        
        {/* Demo credentials */}
        <div className="mt-6 p-4 bg-gray-800 border border-gray-600 text-sm">
          <p className="font-semibold mb-2">Demo Credentials:</p>
          <p>Player: player1 / player123</p>
          <p>Admin: admin / admin123</p>
        </div>
      </div>
    </div>
  );
}

