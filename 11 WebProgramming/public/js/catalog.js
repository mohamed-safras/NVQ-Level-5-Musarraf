// js/catalog.js
import { api, escapeHtml } from "./api.js";
import { toast } from "./ui.js";

export async function loadBooks(keyword = "") {
  const grid = document.getElementById("bookGrid");
  grid.innerHTML = `<div class="empty-state">Loading…</div>`;
  try {
    const books = await api(keyword ? `/books?search=${encodeURIComponent(keyword)}` : "/books");
    if (books.length === 0) {
      grid.innerHTML = `<div class="empty-state">No books match your search.</div>`;
      return;
    }
    grid.innerHTML = books.map(b => `
      <div class="book-card">
        <span class="pill ${b.availableCopies > 0 ? "" : "out"}">${b.availableCopies}/${b.totalCopies} available</span>
        <div class="book-title">${escapeHtml(b.title)}</div>
        <div class="book-author">${escapeHtml(b.author)} · ${escapeHtml(b.category)}</div>
        <div class="book-meta">
          <span>ID ${b.bookId}</span>
          <span>${escapeHtml(b.isbn)}</span>
        </div>
      </div>
    `).join("");
  } catch (e) {
    grid.innerHTML = `<div class="empty-state">Could not load the catalog: ${escapeHtml(e.message)}</div>`;
  }
}

export function initCatalog() {
  document.getElementById("addBookForm").addEventListener("submit", async (e) => {
    e.preventDefault();
    const form = e.target;
    const payload = {
      title: form.title.value, author: form.author.value,
      category: form.category.value, isbn: form.isbn.value,
      copies: form.copies.value,
    };
    try {
      await api("/books", { method: "POST", body: JSON.stringify(payload) });
      toast(`Added "${payload.title}" to the catalog.`);
      form.reset();
      form.classList.add("hidden");
      loadBooks();
    } catch (err) {
      toast(err.message, true);
    }
  });

  let searchTimer;
  document.getElementById("bookSearch").addEventListener("input", (e) => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => loadBooks(e.target.value), 250);
  });
}
