package com.library.api;

import com.library.exception.RecordNotFoundException;
import com.library.model.Member;
import com.library.service.MemberService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Routes:
 *   GET    /api/members              -> list all members
 *   GET    /api/members?search=kw    -> search by name/email
 *   GET    /api/members/{id}         -> get one member
 *   POST   /api/members              -> create a member  {firstName,lastName,email,phone}
 *   PUT    /api/members/{id}         -> update contact    {phone,email}
 *   DELETE /api/members/{id}         -> delete a member
 */
public class MembersHandler implements HttpHandler {
    private final MemberService service;

    public MembersHandler(MemberService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ApiServer.withCors(exchange);
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        String[] segments = path.split("/");
        // segments: ["", "api", "members", "{id}"?]

        try {
            if (method.equals("OPTIONS")) {
                ApiServer.sendJson(exchange, 204, "");
                return;
            }

            if (segments.length == 3) { // /api/members
                switch (method) {
                    case "GET" -> {
                        Map<String, String> params = ApiServer.parseQuery(query);
                        List<Member> members = params.containsKey("search")
                                ? service.searchMembers(params.get("search"))
                                : service.listMembers();
                        ApiServer.sendJson(exchange, 200, JsonUtil.toJsonArray(members, JsonUtil::toJson));
                    }
                    case "POST" -> {
                        Map<String, String> body = JsonUtil.parseFlatObject(ApiServer.readBody(exchange));
                        Member m = service.registerMember(
                                body.getOrDefault("firstName", ""),
                                body.getOrDefault("lastName", ""),
                                body.getOrDefault("email", ""),
                                body.getOrDefault("phone", ""));
                        ApiServer.sendJson(exchange, 201, JsonUtil.toJson(m));
                    }
                    default -> ApiServer.sendJson(exchange, 405, JsonUtil.error("Method not allowed"));
                }
                return;
            }

            if (segments.length == 4) { // /api/members/{id}
                int id = Integer.parseInt(segments[3]);
                switch (method) {
                    case "GET" -> ApiServer.sendJson(exchange, 200, JsonUtil.toJson(service.findMember(id)));
                    case "PUT" -> {
                        Map<String, String> body = JsonUtil.parseFlatObject(ApiServer.readBody(exchange));
                        service.updateMemberContact(id, body.getOrDefault("phone", ""), body.getOrDefault("email", ""));
                        ApiServer.sendJson(exchange, 200, JsonUtil.toJson(service.findMember(id)));
                    }
                    case "DELETE" -> {
                        service.deleteMember(id);
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
