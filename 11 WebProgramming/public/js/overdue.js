// js/overdue.js
import { api, escapeHtml } from "./api.js";

// loanRow lives in loans.js; imported lazily inside the function below to
// avoid a circular top-level import (loans.js also imports from this file).
export async function loadOverdue() {
  const { loanRow } = await import("./loans.js");
  const list = document.getElementById("overdueList");
  list.innerHTML = `<div class="empty-state">Loading…</div>`;
  try {
    const loans = await api("/loans?overdue=true");
    if (loans.length === 0) {
      list.innerHTML = `<div class="empty-state">Nothing is currently overdue.</div>`;
      return;
    }
    list.innerHTML = loans.map(loanRow).join("");
  } catch (e) {
    list.innerHTML = `<div class="empty-state">Could not load overdue loans: ${escapeHtml(e.message)}</div>`;
  }
}

export function initOverdue() {
  document.querySelector('[data-tab="overdue"]').addEventListener("click", loadOverdue);
}
