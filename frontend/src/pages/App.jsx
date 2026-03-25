import React, { useCallback, useEffect, useMemo, useState } from "react";
import EmailList from "../components/EmailList";
import DraftPanel from "../components/DraftPanel";
import { approveDraft, fetchDrafts, fetchEmails, rejectDraft, updateDraft } from "../services/api";

export default function App() {
  const [emails, setEmails] = useState([]);
  const [drafts, setDrafts] = useState([]);
  const [selectedEmailId, setSelectedEmailId] = useState(null);

  const selectedEmail = useMemo(() => {
    if (selectedEmailId == null) return null;
    return emails.find((email) => email.id === selectedEmailId) || null;
  }, [emails, selectedEmailId]);

  const selectedDraft = useMemo(() => {
    if (selectedEmail == null) return null;
    return drafts.find((d) => d.emailId === selectedEmail.id) || null;
  }, [drafts, selectedEmail]);

  const stats = useMemo(() => {
    const total = drafts.length;
    const ready = drafts.filter((d) => d.status === "DRAFT_READY").length;
    const sent = drafts.filter((d) => d.status === "SENT").length;
    return { total, ready, sent };
  }, [drafts]);

  const load = useCallback(async () => {
    const [emailData, draftData] = await Promise.all([fetchEmails(), fetchDrafts()]);
    setEmails(emailData);
    setDrafts(draftData);

    setSelectedEmailId((prevId) => {
      if (emailData.length === 0) return null;
      if (prevId != null && emailData.some((email) => email.id === prevId)) {
        return prevId;
      }
      return emailData[0].id;
    });
  }, []);

  useEffect(() => {
    load();
    const interval = setInterval(load, 8000);
    return () => clearInterval(interval);
  }, [load]);

  useEffect(() => {
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";
    const eventSource = new EventSource(`${apiBaseUrl}/events/drafts`);

    eventSource.onmessage = () => {
      load();
    };

    return () => {
      eventSource.close();
    };
  }, [load]);

  const handleApprove = async (id) => {
    await approveDraft(id);
    await load();
  };

  const handleReject = async (id) => {
    await rejectDraft(id);
    await load();
  };

  const handleSave = async (id, content) => {
    await updateDraft(id, content);
    await load();
  };

  return (
    <div className="app-shell">
      <div className="backdrop-orb orb-one" />
      <div className="backdrop-orb orb-two" />

      <main className="app">
        <header className="hero">
          <div>
            <p className="eyebrow">Customer Support Console</p>
            <h1>AI-Powered Gmail Agent</h1>
            <p className="hero-subtitle">Review, edit, and approve smart drafts in real time.</p>
          </div>
          <div className="stats-grid">
            <div className="stat-card">
              <span>Total Drafts</span>
              <strong>{stats.total}</strong>
            </div>
            <div className="stat-card">
              <span>Ready</span>
              <strong>{stats.ready}</strong>
            </div>
            <div className="stat-card">
              <span>Sent</span>
              <strong>{stats.sent}</strong>
            </div>
          </div>
        </header>

        <section className="layout">
          <EmailList emails={emails} selectedEmailId={selectedEmailId} onSelect={(email) => setSelectedEmailId(email.id)} />
          <DraftPanel
            email={selectedEmail}
            draft={selectedDraft}
            onApprove={handleApprove}
            onReject={handleReject}
            onSave={handleSave}
          />
        </section>
      </main>
    </div>
  );
}
