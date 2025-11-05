import React, { useMemo, useState } from "react";

const DEFAULT_ORG = import.meta.env.VITE_DEFAULT_ORG || "vercel";

export default function App() {
  const apiBase = (import.meta.env.VITE_API_BASE_URL || "").replace(/\/+$/, "");
  const baseUrl = apiBase || window.location.origin;

  const [org, setOrg] = useState(DEFAULT_ORG);
  const [sort, setSort] = useState("stars");
  const [limit, setLimit] = useState(5);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [repos, setRepos] = useState([]);

  const url = useMemo(() => {
    const l = Math.min(20, Math.max(1, Number(limit) || 5));
    const s = sort === "updated" ? "updated" : "stars";
    return `${baseUrl}/api/org/${encodeURIComponent(org)}/repos?limit=${l}&sort=${s}`;
  }, [baseUrl, org, sort, limit]);

  async function load() {
    setLoading(true);
    setError("");
    setRepos([]);
    try {
      const res = await fetch(url);
      if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `HTTP ${res.status}`);
      }
      const data = await res.json();
      setRepos(Array.isArray(data) ? data : []);
    } catch (e) {
      setError(e.message || "Request failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="container">
      <h1>GitHub Org Snapshot</h1>

      <div className="controls">
        <label>
          Organization
          <input
            type="text"
            value={org}
            onChange={(e) => setOrg(e.target.value.trim())}
            placeholder="vercel or spring-projects"
          />
        </label>

        <label>
          Sort by
          <select value={sort} onChange={(e) => setSort(e.target.value)}>
            <option value="stars">Stars</option>
            <option value="updated">Last Updated</option>
          </select>
        </label>

        <label>
          Limit
          <input
            type="number"
            min={1}
            max={20}
            value={limit}
            onChange={(e) => setLimit(e.target.value)}
          />
        </label>

        <button onClick={load} disabled={loading || !org}>
          {loading ? "Loading..." : "Load"}
        </button>
      </div>

      {error && <div className="state error">Error: {error}</div>}
      {!error && !loading && repos.length === 0 && (
        <div className="state empty">No data. Choose an org and click Load.</div>
      )}

      <div className="grid">
        {repos.map((r) => (
          <article key={r.htmlUrl} className="card">
            <header className="card-header">
              <a href={r.htmlUrl} target="_blank" rel="noreferrer">
                {r.name}
              </a>
            </header>
            <div className="metrics">
              <span>★ {r.stargazersCount ?? 0}</span>
              <span>⑂ {r.forksCount ?? 0}</span>
              <span>{r.language || "—"}</span>
            </div>
            <div className="updated">
              Updated: {formatDate(r.updatedAt)}
            </div>
            {r.description && <p className="desc">{r.description}</p>}
          </article>
        ))}
      </div>

      <footer className="footer">
        <small>
          Backend: <code>{baseUrl}</code>
        </small>
      </footer>
    </div>
  );
}

function formatDate(iso) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso || "—";
  }
}
