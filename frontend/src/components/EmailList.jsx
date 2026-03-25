import React, { useEffect, useMemo, useState } from "react";

const PAGE_SIZE = 5;

export default function EmailList({ emails, selectedEmailId, onSelect }) {
  const [page, setPage] = useState(1);
  const totalPages = Math.max(1, Math.ceil(emails.length / PAGE_SIZE));

  useEffect(() => {
    setPage((prev) => Math.min(prev, totalPages));
  }, [totalPages]);

  useEffect(() => {
    if (selectedEmailId == null) return;
    const selectedIndex = emails.findIndex((email) => email.id === selectedEmailId);
    if (selectedIndex === -1) return;
    const selectedPage = Math.floor(selectedIndex / PAGE_SIZE) + 1;
    setPage((prev) => (prev === selectedPage ? prev : selectedPage));
  }, [emails, selectedEmailId]);

  const pagedEmails = useMemo(() => {
    const start = (page - 1) * PAGE_SIZE;
    return emails.slice(start, start + PAGE_SIZE);
  }, [emails, page]);

  return (
    <div className="panel email-panel">
      <div className="panel-head">
        <h3>Incoming Emails</h3>
        <span className="pill">{emails.length}</span>
      </div>

      {emails.length === 0 && <p className="empty-state">No emails yet. New emails appear automatically.</p>}

      {pagedEmails.map((email) => {
        const active = selectedEmailId === email.id;
        return (
          <article key={email.id} className={`email-item ${active ? "active" : ""}`} onClick={() => onSelect(email)}>
            <div className="email-top-row">
              <strong>{email.subject || "(No subject)"}</strong>
              <span className={`status-tag status-${String(email.status || "").toLowerCase()}`}>{email.status}</span>
            </div>
            <p className="email-sender">{email.sender}</p>
            <p className="email-preview">{email.body || "No content"}</p>
          </article>
        );
      })}

      {emails.length > PAGE_SIZE && (
        <div className="pagination">
          <button className="pager-btn" onClick={() => setPage((prev) => Math.max(1, prev - 1))} disabled={page === 1}>
            Previous
          </button>
          <span className="pager-info">
            Page {page} of {totalPages}
          </span>
          <button className="pager-btn" onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))} disabled={page === totalPages}>
            Next
          </button>
        </div>
      )}
    </div>
  );
}
