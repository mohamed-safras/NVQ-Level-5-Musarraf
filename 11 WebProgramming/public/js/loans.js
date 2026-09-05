// js/loans.js
import { api, escapeHtml } from "./api.js";
import { toast } from "./ui.js";
import { loadBooks } from "./catalog.js";
import { loadOverdue } from "./overdue.js";

export function loanRow(l) {
  const overdue = l.status === "OVERDUE";
  return `
    <div class="index-row ${overdue ? "overdue" : ""}">
      <div class="index-row-main">
        <strong>${escapeHtml(l.memberName)}</strong> — ${escapeHtml(l.bookTitle)}
        <div class="index-row-sub">Loan #${l.loanId} · borrowed ${l.loanDate} · due ${l.dueDate}${l.returnDate ? " · returned " + l.returnDate : ""}</div>
      </div>
      <div class="row-actions">
        <span class="status-tag ${overdue ? "status-overdue" : l.status === "RETURNED" ? "status-returned" : "status-active"}">${l.status}</span>
        ${l.status !== "RETURNED" ? `<button class="btn-small" data-return-loan="${l.loanId}">Return</button>` : ""}
      </div>
    </div>
  `;
}

export async function loadLoans() {
  const list = document.getElementById("loanList");
  list.innerHTML = `<div class="empty-state">Loading…</div>`;
  try {
    const loans = await api("/loans");
    if (loans.length === 0) {
      list.innerHTML = `<div class="empty-state">No loans have been issued yet.</div>`;
      return;
    }
    list.innerHTML = loans.slice().reverse().map(loanRow).join("");
  } catch (e) {
    list.innerHTML = `<div class="empty-state">Could not load loans: ${escapeHtml(e.message)}</div>`;
  }
}

export function initLoans() {
  document.getElementById("issueLoanForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = e.target;
    const payload = { memberId: form.memberId.value, bookId: form.bookId.value };
    try {
      await api("/loans", { method: "POST", body: JSON.stringify(payload) });
      toast(`Loan issued.`);
      form.reset();
      form.classList.add("hidden");
      loadLoans();
      loadBooks();
    } catch (err) {
      toast(err.message, true);
    }
  });

  document.getElementById("loanList").addEventListener("click", async (e) => {
    const btn = e.target.closest("[data-return-loan]");
    if (!btn) return;
    const id = btn.dataset.returnLoan;
    try {
      const result = await api(`/loans/${id}/return`, { method: "POST" });
      toast(result.fine > 0 ? `Returned. Fine due: Rs. ${result.fine.toFixed(2)}` : "Returned. No fine due.");
      loadLoans();
      loadBooks();
      loadOverdue();
    } catch (err) {
      toast(err.message, true);
    }
  });
}
