package com.library.api;

import com.library.exception.RecordNotFoundException;
import com.library.model.Loan;
import com.library.service.LoanService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;

/**
 * Routes:
 *   GET  /api/loans                 -> list all loans
 *   GET  /api/loans?overdue=true    -> list only overdue loans
 *   POST /api/loans                 -> issue a loan   {memberId,bookId}
 *   POST /api/loans/{id}/return     -> return a loan  -> {fine: <amount>}
 */
public class LoansHandler implements HttpHandler {
    private final LoanService service;

    public LoansHandler(LoanService service) {
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

            if (segments.length == 3) { // /api/loans
                switch (method) {
                    case "GET" -> {
                        Map<String, String> params = ApiServer.parseQuery(query);
                        var loans = "true".equals(params.get("overdue"))
                                ? service.listOverdueLoans()
                                : service.listAllLoans();
                        ApiServer.sendJson(exchange, 200, JsonUtil.toJsonArray(loans, JsonUtil::toJson));
                    }
                    case "POST" -> {
                        Map<String, String> body = JsonUtil.parseFlatObject(ApiServer.readBody(exchange));
                        int memberId = Integer.parseInt(body.get("memberId"));
                        int bookId = Integer.parseInt(body.get("bookId"));
                        Loan loan = service.issueLoan(memberId, bookId);
                        ApiServer.sendJson(exchange, 201, JsonUtil.toJson(loan));
                    }
                    default -> ApiServer.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
                }
                return;
            }

            if (segments.length == 5 && segments[4].equals("return") && method.equals("POST")) {
                int loanId = Integer.parseInt(segments[3]);
                double fine = service.returnLoan(loanId);
                ApiServer.sendJson(exchange, 200, String.format("{\"loanId\":%d,\"fine\":%.2f}", loanId, fine));
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
