import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { GET_ALL_GAMES_QUERY } from '../graphql/queries';

export default function GameList() {
  const [games, setGames] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  const { getAuthenticatedClient, logout, user } = useAuth();
  const navigate = useNavigate();
  
  useEffect(() => {
    loadGames();
  }, []);
  
  const loadGames = async () => {
    try {
      const client = getAuthenticatedClient();
      const data = await client.request(GET_ALL_GAMES_QUERY);
      setGames(data.getAllGames.filter(g => g.active));
    } catch (err) {
      console.error('Error loading games:', err);
      setError('Failed to load games');
    } finally {
      setLoading(false);
    }
  };
  
  const handleGameSelect = (gameId) => {
    navigate(`/game/${gameId}`);
  };
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-2xl">Loading games...</div>
      </div>
    );
  }
  
  return (
    <div className="min-h-screen p-8">
      <div className="max-w-6xl mx-auto">
        <div className="flex justify-between items-center mb-8">
          <div>
            <h1 className="text-4xl font-bold mb-2">Select a Game</h1>
            <p className="text-gray-400">Welcome, {user?.name}!</p>
          </div>
          <button
            onClick={logout}
            className="btn-secondary"
          >
            Logout
          </button>
        </div>
        
        {error && (
          <div className="bg-red-900/50 border border-red-500 text-red-200 p-4 mb-6">
            {error}
          </div>
        )}
        
        {games.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-xl text-gray-400">No games available</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {games.map((game) => (
              <div
                key={game.id}
                onClick={() => handleGameSelect(game.id)}
                className="bg-[#181818] border-2 border-gray-600 p-6 
                         hover:border-gray-400 transition-colors cursor-pointer"
              >
                <h2 className="text-2xl font-bold mb-3">{game.name}</h2>
                <p className="text-gray-400 mb-4">{game.description}</p>
                <div className="btn-primary text-center">
                  Play Game
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

