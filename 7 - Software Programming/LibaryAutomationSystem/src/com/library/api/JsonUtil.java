package com.library.api;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JsonUtil {

    private JsonUtil() {}

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toJson(Member m) {
        return "{"
                + "\"memberId\":" + m.getMemberId() + ","
                + "\"firstName\":\"" + escape(m.getFirstName()) + "\","
                + "\"lastName\":\"" + escape(m.getLastName()) + "\","
                + "\"fullName\":\"" + escape(m.getFullName()) + "\","
                + "\"email\":\"" + escape(m.getEmail()) + "\","
                + "\"phone\":\"" + escape(m.getPhone()) + "\","
                + "\"membershipDate\":\"" + m.getMembershipDate() + "\","
                + "\"status\":\"" + m.getStatus() + "\""
                + "}";
    }

    public static String toJson(Book b) {
        return "{"
                + "\"bookId\":" + b.getBookId() + ","
                + "\"isbn\":\"" + escape(b.getIsbn()) + "\","
                + "\"title\":\"" + escape(b.getTitle()) + "\","
                + "\"category\":\"" + escape(b.getCategory()) + "\","
                + "\"author\":\"" + escape(b.getAuthor()) + "\","
                + "\"totalCopies\":" + b.getTotalCopies() + ","
                + "\"availableCopies\":" + b.getAvailableCopies()
                + "}";
    }

    public static String toJson(Loan l) {
        return "{"
                + "\"loanId\":" + l.getLoanId() + ","
                + "\"memberId\":" + l.getMember().getMemberId() + ","
                + "\"memberName\":\"" + escape(l.getMember().getFullName()) + "\","
                + "\"bookId\":" + l.getBook().getBookId() + ","
                + "\"bookTitle\":\"" + escape(l.getBook().getTitle()) + "\","
                + "\"loanDate\":\"" + l.getLoanDate() + "\","
                + "\"dueDate\":\"" + l.getDueDate() + "\","
                + "\"returnDate\":" + (l.getReturnDate() == null ? "null" : "\"" + l.getReturnDate() + "\"") + ","
                + "\"status\":\"" + l.getStatus() + "\""
                + "}";
    }

    public static <T> String toJsonArray(List<T> items, java.util.function.Function<T, String> mapper) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(mapper.apply(items.get(i)));
        }
        return sb.append("]").toString();
    }

    public static String error(String message) {
        return "{\"error\":\"" + escape(message) + "\"}";
    }

    /**
     * Parses a flat JSON object (string/number/boolean values only, no
     * nesting/arrays) into a String-keyed map. Sufficient for the request
     * bodies this API accepts (member/book create-and-update forms).
     */
    public static Map<String, String> parseFlatObject(String json) {
        Map<String, String> result = new LinkedHashMap<>();
        if (json == null) return result;
        String s = json.trim();
        if (s.startsWith("{")) s = s.substring(1);
        if (s.endsWith("}")) s = s.substring(0, s.length() - 1);

        int i = 0, n = s.length();
        while (i < n) {
            while (i < n && (s.charAt(i) == ',' || Character.isWhitespace(s.charAt(i)))) i++;
            if (i >= n) break;
            if (s.charAt(i) != '"') break;
            int keyStart = ++i;
            while (i < n && s.charAt(i) != '"') i++;
            String key = s.substring(keyStart, i);
            i++; // closing quote
            while (i < n && (s.charAt(i) == ':' || Character.isWhitespace(s.charAt(i)))) i++;

            String value;
            if (i < n && s.charAt(i) == '"') {
                int valStart = ++i;
                StringBuilder val = new StringBuilder();
                while (i < n && s.charAt(i) != '"') {
                    if (s.charAt(i) == '\\' && i + 1 < n) {
                        i++;
                        val.append(switch (s.charAt(i)) {
                            case 'n' -> '\n';
                            case 't' -> '\t';
                            default -> s.charAt(i);
                        });
                    } else {
                        val.append(s.charAt(i));
                    }
                    i++;
                }
                value = val.toString();
                i++; // closing quote
            } else {
                int valStart = i;
                while (i < n && s.charAt(i) != ',' ) i++;
                value = s.substring(valStart, i).trim();
            }
            result.put(key, value);
        }
        return result;
    }
}
