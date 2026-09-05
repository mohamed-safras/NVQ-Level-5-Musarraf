// js/main.js
// Entry point. Loaded as <script type="module">, so this file (and every
// file it imports) only runs once, after the DOM is parsed, with no global
// variables leaking outside their own module.

import { initTabs, wireFormToggle, initCloseFormButtons } from "./ui.js";
import { checkStatus } from "./status.js";
import { loadBooks, initCatalog } from "./catalog.js";
import { loadMembers, initMembers } from "./members.js";
import { loadLoans, initLoans } from "./loans.js";
import { initOverdue } from "./overdue.js";

initTabs();
wireFormToggle("btnShowAddBook", "addBookForm");
wireFormToggle("btnShowAddMember", "addMemberForm");
wireFormToggle("btnShowIssueLoan", "issueLoanForm");
initCloseFormButtons();

initCatalog();
initMembers();
initLoans();
initOverdue();

checkStatus();
loadBooks();
loadMembers();
loadLoans();
