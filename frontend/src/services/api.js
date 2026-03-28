import axios from 'axios'

const client = axios.create({
  baseURL: '/api'
})

export const fetchSummary = async () => (await client.get('/dashboard/summary')).data
export const fetchAgents = async () => (await client.get('/agents')).data
export const createAgent = async (payload) => (await client.post('/agents', payload)).data

export const fetchTools = async () => (await client.get('/mcp-tools')).data
export const createTool = async (payload) => (await client.post('/mcp-tools', payload)).data

export const fetchSkills = async () => (await client.get('/skills')).data
export const createSkill = async (payload) => (await client.post('/skills', payload)).data

export const createReactAgent = async (payload) => (await client.post('/react-agents/create', payload)).data
