'use client'

import { useState, useEffect } from 'react'

export default function Page() {
  const [count, setCount] = useState<number | null>(null)
  const [loading, setLoading] = useState(false)

  // Fetch initial count on mount
  useEffect(() => {
    const fetchInitialCount = async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/count`)
        const data = await res.json()
        setCount(data.count)
      } catch (error) {
        console.error('Failed to fetch count:', error)
        setCount(0)
      }
    }

    fetchInitialCount()
  }, [])

  // Handle click
  const handleClick = async () => {
    setLoading(true)
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/click`, { method: 'POST' })
      const data = await res.json()
      setCount(data.count)
    } catch (error) {
      console.error('Failed to increment count:', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 to-slate-800">
      <div className="text-center">
        <div className="mb-12">
          <div className="text-7xl font-bold text-white mb-4">
            {count !== null ? count : '—'}
          </div>
          <p className="text-slate-400 text-lg">Total Clicks</p>
        </div>

        <button
          onClick={handleClick}
          disabled={loading}
          className="px-12 py-6 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-800 text-white text-2xl font-bold rounded-lg transition-colors duration-200 shadow-lg hover:shadow-xl active:scale-95"
        >
          {loading ? 'Clicking...' : 'Click!'}
        </button>
      </div>
    </main>
  )
}
