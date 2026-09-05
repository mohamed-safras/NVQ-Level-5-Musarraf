package com.library.api;

import com.library.policy.DailyRateFineCalculator;
import com.library.policy.StandardEligibilityPolicy;
import com.library.policy.StandardLoanPolicy;
import com.library.repository.memory.InMemoryBookRepository;
import com.library.repository.memory.InMemoryLoanRepository;
import com.library.repository.memory.InMemoryMemberRepository;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import com.library.service.impl.BookServiceImpl;
import com.library.service.impl.LoanServiceImpl;
import com.library.service.impl.MemberServiceImpl;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Library Automation System — REST API backend.
 * NVQ Level 5 — Software Programming unit (backend) / Web Programming unit (API consumed by the site).
 *
 * This class is the application's "composition root": the one place that
 * is allowed to know about concrete classes (InMemoryMemberRepository,
 * MemberServiceImpl, StandardLoanPolicy, ...). Every other class in the
 * codebase — services, handlers — depends only on interfaces and receives
 * its collaborators through the constructor. That is what actually makes
 * the SOLID split useful: swapping an in-memory repository for a JDBC one,
 * or the daily fine rate for a tiered one, is a one-line change here and
 * nowhere else.
 *
 * Built entirely on the JDK's built-in com.sun.net.httpserver.HttpServer so
 * the project needs no external framework or Maven/internet access to
 * compile and run — only `javac`/`java`.
 */
public class ApiServer {
    private static final int PORT = 8088;

    public static void main(String[] args) throws IOException {
        // ---- Composition root: wire concrete implementations behind interfaces ----
        InMemoryMemberRepository memberRepository = new InMemoryMemberRepository();
        InMemoryBookRepository bookRepository = new InMemoryBookRepository();
        InMemoryLoanRepository loanRepository = new InMemoryLoanRepository();

        MemberService memberService = new MemberServiceImpl(memberRepository, loanRepository);
        BookService bookService = new BookServiceImpl(bookRepository);
        LoanService loanService = new LoanServiceImpl(
                loanRepository,
                memberService,
                bookService,
                new StandardLoanPolicy(),          // 14-day loans — swap for a different LoanPolicy to change this
                new StandardEligibilityPolicy(),   // active member + available copy — swap for a stricter rule
                new DailyRateFineCalculator());     // Rs. 30/day — swap for a different FineCalculator

        seedDemoData(memberService, bookService, loanService);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/members", new MembersHandler(memberService));
        server.createContext("/api/books", new BooksHandler(bookService));
        server.createContext("/api/loans", new LoansHandler(loanService));

        // Serves the Web Programming unit's front-end (public/index.html, css, js)
        // from the same origin as the API, so no separate web server is needed
        // for the demo and no CORS configuration is required in the browser.
        String webRoot = args.length > 0 ? args[0] : "public";
        server.createContext("/", new StaticFileHandler(webRoot));

        server.setExecutor(null);
        server.start();
        System.out.println("Library Automation System API running on http://localhost:" + PORT);
        System.out.println("Serving front-end static files from: " + new File(webRoot).getAbsolutePath());
    }

    // ---------------- Shared helpers used by all handlers ----------------

    public static void withCors(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    public static void sendJson(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length == 0 ? -1 : bytes.length);
        if (bytes.length > 0) {
            try (var os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }

    public static String readBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public static Map<String, String> parseQuery(String query) {
        Map<String, String> params = new LinkedHashMap<>();
        if (query == null || query.isBlank()) return params;
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq < 0) continue;
            String key = URLDecoder.decode(pair.substring(0, eq), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            params.put(key, value);
        }
        return params;
    }

    private static void seedDemoData(MemberService memberService, BookService bookService, LoanService loanService) {
        var m1 = memberService.registerMember("Nadeesha", "Perera", "nadeesha@mail.com", "0771234561");
        var m2 = memberService.registerMember("Kasun", "Fernando", "kasun@mail.com", "0771234562");
        memberService.registerMember("Ishara", "Silva", "ishara@mail.com", "0771234563");

        bookService.addBook("978-0132350884", "Clean Code", "Computer Science", "Robert Martin", 2);
        bookService.addBook("978-0451524935", "1984", "Fiction", "George Orwell", 1);
        bookService.addBook("978-0062316097", "Sapiens", "Science", "Yuval Noah Harari", 1);
        bookService.addBook("978-0596007126", "Head First Java", "Computer Science", "Kathy Sierra", 2);

        loanService.issueLoan(m1.getMemberId(), 2); // Nadeesha borrows 1984
        loanService.issueLoan(m2.getMemberId(), 3); // Kasun borrows Sapiens
    }

    /** Minimal static file server for the front-end (index.html, style.css, app.js). */
    static class StaticFileHandler implements com.sun.net.httpserver.HttpHandler {
        private final Path root;

        StaticFileHandler(String rootDir) {
            this.root = Path.of(rootDir).toAbsolutePath().normalize();
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            Path file = root.resolve(path.substring(1)).normalize();

            if (!file.startsWith(root) || !Files.exists(file) || Files.isDirectory(file)) {
                byte[] notFound = "404 Not Found".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(404, notFound.length);
                try (var os = exchange.getResponseBody()) { os.write(notFound); }
                return;
            }

            String contentType = guessContentType(file.toString());
            exchange.getResponseHeaders().set("Content-Type", contentType);
            byte[] bytes = Files.readAllBytes(file);
            exchange.sendResponseHeaders(200, bytes.length);
            try (var os = exchange.getResponseBody()) { os.write(bytes); }
        }

        private String guessContentType(String filename) {
            if (filename.endsWith(".html")) return "text/html; charset=utf-8";
            if (filename.endsWith(".css")) return "text/css; charset=utf-8";
            if (filename.endsWith(".js")) return "application/javascript; charset=utf-8";
            if (filename.endsWith(".json")) return "application/json; charset=utf-8";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            return "application/octet-stream";
        }
    }
}
