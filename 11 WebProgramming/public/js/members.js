// js/members.js
import { api, escapeHtml } from "./api.js";
import { toast } from "./ui.js";

function statusClass(status) {
  return { ACTIVE: "status-active", SUSPENDED: "status-suspended", EXPIRED: "status-expired" }[status] || "";
}

export async function loadMembers(keyword = "") {
  const list = document.getElementById("memberList");
  list.innerHTML = `<div class="empty-state">Loading…</div>`;
  try {
    const members = await api(keyword ? `/members?search=${encodeURIComponent(keyword)}` : "/members");
    if (members.length === 0) {
      list.innerHTML = `<div class="empty-state">No members match your search.</div>`;
      return;
    }
    list.innerHTML = members.map(m => `
      <div class="index-row">
        <div class="index-row-main">
          <strong>${escapeHtml(m.fullName)}</strong>
          <div class="index-row-sub">ID ${m.memberId} · ${escapeHtml(m.email)} · ${escapeHtml(m.phone)}</div>
        </div>
        <div class="row-actions">
          <span class="status-tag ${statusClass(m.status)}">${m.status}</span>
          <button class="btn-small danger" data-delete-member="${m.memberId}">Delete</button>
        </div>
      </div>
    `).join("");
  } catch (e) {
    list.innerHTML = `<div class="empty-state">Could not load members: ${escapeHtml(e.message)}</div>`;
  }
}

export function initMembers() {
  document.getElementById("addMemberForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = e.target;
    const payload = {
      firstName: form.firstName.value, lastName: form.lastName.value,
      email: form.email.value, phone: form.phone.value,
    };
    try {
      await api("/members", { method: "POST", body: JSON.stringify(payload) });
      toast(`Registered ${payload.firstName} ${payload.lastName}.`);
      form.reset();
      form.classList.add("hidden");
      loadMembers();
    } catch (err) {
      toast(err.message, true);
    }
  });

  document.getElementById("memberList").addEventListener("click", async (e) => {
    const btn = e.target.closest("[data-delete-member]");
    if (!btn) return;
    const id = btn.dataset.deleteMember;
    try {
      await api(`/members/${id}`, { method: "DELETE" });
      toast(`Member ${id} deleted.`);
      loadMembers();
    } catch (err) {
      toast(err.message, true);
    }
  });

  let searchTimer;
  document.getElementById("memberSearch").addEventListener("input", (e) => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadMembers(e.target.value), 250);
  });
}
