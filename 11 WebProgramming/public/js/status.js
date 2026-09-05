// js/status.js
import { api } from "./api.js";

export async function checkStatus() {
  const el = document.getElementById("apiStatus");
  try {
    await api("/books");
    el.className = "masthead-status ok";
    el.innerHTML = `<span class="dot"></span> API connected`;
  } catch (e) {
    el.className = "masthead-status err";
    el.innerHTML = `<span class="dot"></span> API unreachable — is ApiServer running on :8080?`;
  }
}
