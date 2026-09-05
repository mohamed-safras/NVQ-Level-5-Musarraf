// js/api.js
// Thin fetch wrapper shared by every feature module. Centralising this means
// there is exactly one place that knows the API's base URL and one place
// that turns a failed HTTP response into a thrown Error with a readable
// message — every module below can just `await api(...)` and catch.

export const API_BASE = "/api";

export async function api(path, options = {}) {
  let res;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      headers: { "Content-Type": "application/json" },
      ...options,
    });
  } catch (networkErr) {
    // fetch() itself throws only on a network-level failure (server down,
    // DNS failure, CORS block) — distinguish that from an HTTP error status.
    throw new Error("Could not reach the API server. Is ApiServer running?");
  }

  const text = await res.text();
  const data = text ? JSON.parse(text) : null;

  if (!res.ok) {
    throw new Error((data && data.error) || `Request failed (${res.status})`);
  }
  return data;
}

export function escapeHtml(str) {
  return String(str).replace(/[&<>"']/g, (c) => ({
    "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
  }[c]));
}
