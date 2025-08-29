'use client'

import { useAppDispatch, useAppSelector } from '@/lib/store'
import { setUser, logout, setLoading } from '@/lib/features/auth/authSlice'

export default function AuthExample() {
  const dispatch = useAppDispatch()
  const { user, isAuthenticated, isLoading } = useAppSelector((state) => state.auth)

  const handleLogin = () => {
    dispatch(setLoading(true))
    
    // Simulate API call
    setTimeout(() => {
      dispatch(setUser({
        id: '1',
        email: 'user@example.com',
        name: 'John Doe',
        role: 'admin'
      }))
    }, 1000)
  }

  const handleLogout = () => {
    dispatch(logout())
  }

  return (
    <div className="p-6 border rounded-lg">
      <h2 className="text-xl font-bold mb-4">Redux Auth Example</h2>
      
      {isLoading && <p className="text-blue-600">Loading...</p>}
      
      {isAuthenticated ? (
        <div className="space-y-2">
          <p><strong>Welcome:</strong> {user?.name}</p>
          <p><strong>Email:</strong> {user?.email}</p>
          <p><strong>Role:</strong> {user?.role}</p>
          <button 
            onClick={handleLogout}
            className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
          >
            Logout
          </button>
        </div>
      ) : (
        <div>
          <p className="mb-2">You are not logged in</p>
          <button 
            onClick={handleLogin}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            disabled={isLoading}
          >
            Login
          </button>
        </div>
      )}
    </div>
  )
}
