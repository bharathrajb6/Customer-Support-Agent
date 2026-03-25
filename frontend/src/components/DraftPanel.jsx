import React, { useEffect, useState } from "react";

export default function DraftPanel({ email, draft, onApprove, onReject, onSave }) {
  const [content, setContent] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const isSent = draft?.status === "SENT";
  const isRejected = draft?.status === "REJECTED";
  const isTerminal = isSent || isRejected;

  useEffect(() => {
    setContent(draft?.content || "");
    setIsSubmitting(false);
  }, [draft]);

  const runAction = async (action) => {
    if (isTerminal || isSubmitting) return;
    setIsSubmitting(true);
    try {
      await action();
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!draft) {
    return (
      <div className="panel">
        <div className="panel-head">
          <h3>Draft Workspace</h3>
        </div>
        {email ? (
          <div className="email-details">
            <div className="email-details-row">
              <span className="label">Subject</span>
              <strong>{email.subject || "(No subject)"}</strong>
            </div>
            <div className="email-details-row">
              <span className="label">From</span>
              <span>{email.sender || "Unknown"}</span>
            </div>
            <div className="email-details-row">
              <span className="label">Received</span>
              <span>{email.createdAt ? new Date(email.createdAt).toLocaleString() : "-"}</span>
            </div>
            <div className="email-details-body">{email.body || "No content"}</div>
          </div>
        ) : (
          <p className="empty-state">Select an email with a draft.</p>
        )}
      </div>
    );
  }

  return (
    <div className="panel">
      <div className="panel-head">
        <h3>Draft Workspace</h3>
        <span className={`status-tag status-${String(draft.status || "").toLowerCase()}`}>{draft.status}</span>
      </div>

      <div className="draft-meta">
        <p>
          Draft ID <strong>#{draft.id}</strong>
        </p>
        <p>
          Confidence <strong>{draft.confidence}</strong>
        </p>
      </div>

      {email && (
        <div className="email-details">
          <div className="email-details-row">
            <span className="label">Subject</span>
            <strong>{email.subject || "(No subject)"}</strong>
          </div>
          <div className="email-details-row">
            <span className="label">From</span>
            <span>{email.sender || "Unknown"}</span>
          </div>
          <div className="email-details-row">
            <span className="label">Received</span>
            <span>{email.createdAt ? new Date(email.createdAt).toLocaleString() : "-"}</span>
          </div>
          <div className="email-details-body">{email.body || "No content"}</div>
        </div>
      )}

      <textarea value={content} onChange={(e) => setContent(e.target.value)} disabled={isTerminal || isSubmitting} />

      <div className="draft-actions">
        <button className="btn btn-secondary" onClick={() => runAction(() => onSave(draft.id, content))} disabled={isTerminal || isSubmitting}>
          {isSubmitting ? "Working..." : "Save Edit"}
        </button>
        <button className="btn btn-success" onClick={() => runAction(() => onApprove(draft.id))} disabled={isTerminal || isSubmitting}>
          {isSent ? "Already Sent" : isSubmitting ? "Sending..." : "Approve & Send"}
        </button>
        <button className="btn btn-danger" onClick={() => runAction(() => onReject(draft.id))} disabled={isTerminal || isSubmitting}>
          Reject
        </button>
      </div>
    </div>
  );
}
