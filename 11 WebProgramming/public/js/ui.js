// js/ui.js
// Page chrome that isn't specific to any one feature: the toast, the tab
// switcher, and the generic "toggle a form open/closed" behaviour used by
// the add-book, add-member and issue-loan forms alike.

export function toast(message, isError = false) {
  const el = document.getElementById("toast");
  el.textContent = message;
  el.className = "toast show" + (isError ? " err" : "");
  clearTimeout(toast._t);
  toast._t = setTimeout(() => { el.className = "toast"; }, 3200);
}

export function initTabs() {
  document.getElementById("drawerTabs").addEventListener("click", (e) => {
    const btn = e.target.closest(".drawer-tab");
    if (!btn) return;
    document.querySelectorAll(".drawer-tab").forEach(b => b.classList.remove("active"));
    document.querySelectorAll(".panel").forEach(p => p.classList.remove("active"));
    btn.classList.add("active");
    document.getElementById(`panel-${btn.dataset.tab}`).classList.add("active");
  });
}

export function wireFormToggle(buttonId, formId) {
  document.getElementById(buttonId).addEventListener("click", () => {
    document.getElementById(formId).classList.toggle("hidden");
  });
}

export function initCloseFormButtons() {
  document.querySelectorAll("[data-close-form]").forEach(btn => {
    btn.addEventListener("click", () => btn.closest(".index-form").classList.add("hidden"));
  });
}
