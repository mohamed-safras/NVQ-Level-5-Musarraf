package com.library.api;

import com.library.exception.RecordNotFoundException;
import com.library.model.Book;
import com.library.service.BookService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Routes:
 *   GET    /api/books              -> list all books
 *   GET    /api/books?search=kw    -> search by title/author
 *   GET    /api/books/{id}         -> get one book
 *   POST   /api/books              -> create a book  {isbn,title,category,author,copies}
 *   PUT    /api/books/{id}         -> update details  {title,category,author}
 *   DELETE /api/books/{id}         -> delete a book
 */
public class BooksHandler implements HttpHandler {
    private final BookService service;

    public BooksHandler(BookService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ApiServer.withCors(exchange);
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String[] segments = path.split("/");

        try {
            if (method.equals("OPTIONS")) {
                ApiServer.sendJson(exchange, 204, "");
                return;
            }

            if (segments.length == 3) { // /api/books
                switch (method) {
                    case "GET" -> {
                        Map<String, String> params = ApiServer.parseQuery(query);
                        List<Book> books = params.containsKey("search")
                                ? service.searchBooks(params.get("search"))
                                : service.listBooks();
                        ApiServer.sendJson(exchange, 200, JsonUtil.toJsonArray(books, JsonUtil::toJson));
                    }
                    case "POST" -> {
                        Map<String, String> body = JsonUtil.parseFlatObject(ApiServer.readBody(exchange));
                        int copies = Integer.parseInt(body.getOrDefault("copies", "1"));
                        Book b = service.addBook(
                                body.getOrDefault("isbn", ""),
                                body.getOrDefault("title", ""),
                                body.getOrDefault("category", ""),
                                body.getOrDefault("author", ""),
                                copies);
                        ApiServer.sendJson(exchange, 201, JsonUtil.toJson(b));
                    }
                    default -> ApiServer.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
                }
                return;
            }

            if (segments.length == 4) { // /api/books/{id}
                int id = Integer.parseInt(segments[3]);
                switch (method) {
                    case "GET" -> ApiServer.sendJson(exchange, 200, JsonUtil.toJson(service.findBook(id)));
                    case "PUT" -> {
                        Map<String, String> body = JsonUtil.parseFlatObject(ApiServer.readBody(exchange));
                        service.updateBookDetails(id,
                                body.getOrDefault("title", service.findBook(id).getTitle()),
                                body.getOrDefault("category", service.findBook(id).getCategory()),
                                body.getOrDefault("author", service.findBook(id).getAuthor()));
                        ApiServer.sendJson(exchange, 200, JsonUtil.toJson(service.findBook(id)));
                    }
                    case "DELETE" -> {
                        service.deleteBook(id);
                        ApiServer.sendJson(exchange, 200, "{\"deleted\":true}");
                    }
                    default -> ApiServer.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
                }
                return;
            }

            ApiServer.sendJson(exchange, 404, JsonUtil.error("Not found"));
        } catch (RecordNotFoundException e) {
            ApiServer.sendJson(exchange, 404, JsonUtil.error(e.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            ApiServer.sendJson(exchange, 400, JsonUtil.error(e.getMessage()));
        } catch (Exception e) {
            ApiServer.sendJson(exchange, 500, JsonUtil.error("Internal error: " + e.getMessage()));
        }
    }
}
