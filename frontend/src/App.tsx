import { useState, useEffect } from 'react'
import './App.css'

interface AgentHealth {
  status: string;
  agentsLoaded: number;
  instanceCount: number;
  availableModels: string[];
  degradedMode: boolean;
}

function App() {
  const [health, setHealth] = useState<AgentHealth | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetch('/api/agents/health')
      .then(res => res.json())
      .then(data => {
        setHealth(data)
        setLoading(false)
      })
      .catch(err => {
        console.error('Failed to fetch health:', err)
        setLoading(false)
      })
  }, [])

  return (
    <div className="App">
      <h1>Spring Boot AgentService Template</h1>
      <div className="card">
        <h2>Agent Service Status</h2>
        {loading ? (
          <p>Loading...</p>
        ) : health ? (
          <div>
            <p>Status: <strong>{health.status}</strong></p>
            <p>Agents Loaded: <strong>{health.agentsLoaded}</strong></p>
            <p>Instances: <strong>{health.instanceCount}</strong></p>
            <p>Available Models: <strong>{health.availableModels?.join(', ') || 'None'}</strong></p>
            <p>Degraded Mode: <strong>{health.degradedMode ? 'Yes' : 'No'}</strong></p>
          </div>
        ) : (
          <p>Failed to load health status. Make sure the backend is running on port 8080.</p>
        )}
      </div>
      <p className="read-the-docs">
        Edit <code>src/App.tsx</code> to get started
      </p>
    </div>
  )
}

export default App
