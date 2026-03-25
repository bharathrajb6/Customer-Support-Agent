import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081'
});

export const fetchEmails = async () => (await api.get('/emails')).data;
export const fetchDrafts = async () => (await api.get('/drafts')).data;
export const approveDraft = async (id) => (await api.post(`/drafts/${id}/approve`)).data;
export const rejectDraft = async (id) => (await api.post(`/drafts/${id}/reject`)).data;
export const updateDraft = async (id, content) => (await api.put(`/drafts/${id}`, { content })).data;
