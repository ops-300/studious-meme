package supportnote;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** URLごとの受付担当。業務ルールはService、HTML生成はViewsに任せる。 */
public class WebController implements HttpHandler {
    private final TicketService service;
    private final String csrf = UUID.randomUUID().toString();
    private final byte[] css;

    public WebController(TicketService service, Path cssFile) throws IOException {
        this.service = service;
        this.css = Files.readAllBytes(cssFile);
    }

    @Override public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            if (method.equals("GET")) {
                Map<String, String> query = parse(exchange.getRequestURI().getRawQuery());
                switch (path) {
                    case "/" -> send(exchange, 200, HtmlViews.list(
                        service.search(query.getOrDefault("q", ""), query.getOrDefault("status", "")),
                        service.search("", ""), query.getOrDefault("q", ""), query.getOrDefault("status", ""),
                        "1".equals(query.get("saved"))));
                    case "/new" -> send(exchange, 200, HtmlViews.create(Map.of(), "", csrf));
                    case "/ticket" -> send(exchange, 200, HtmlViews.detail(service.find(id(query)), Map.of(), "", csrf));
                    case "/style.css" -> sendBytes(exchange, 200, "text/css; charset=UTF-8", css);
                    default -> send(exchange, 404, HtmlViews.error("ページが見つかりません。"));
                }
            } else if (method.equals("POST")) {
                if (!path.equals("/create") && !path.equals("/update")) {
                    send(exchange, 404, HtmlViews.error("ページが見つかりません。"));
                    return;
                }
                String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("application/x-www-form-urlencoded")) {
                    send(exchange, 415, HtmlViews.error("フォームから送信してください。"));
                    return;
                }
                // 意図: 想定を超える巨大な入力を丸ごとメモリに読み込まない。
                byte[] bytes = exchange.getRequestBody().readNBytes(32769);
                if (bytes.length > 32768) {
                    send(exchange, 413, HtmlViews.error("入力が大きすぎます。文字数を減らしてください。"));
                    return;
                }
                Map<String, String> form = parse(new String(bytes, StandardCharsets.UTF_8));
                // 意図: 別サイトから勝手に送られた更新を防ぐ。画面に埋めた確認用の値を照合する（CSRF対策）。
                if (!csrf.equals(form.get("token"))) {
                    send(exchange, 403, HtmlViews.error("画面を開き直してから操作してください。"));
                    return;
                }
                try {
                    if (path.equals("/create")) {
                        service.create(form.getOrDefault("title", ""), form.getOrDefault("category", ""), form.getOrDefault("description", ""), form.getOrDefault("priority", "NORMAL"));
                    } else {
                        service.update(id(form), form.getOrDefault("status", ""), form.getOrDefault("resolution", ""));
                    }
                    // 意図: 保存後にGETへ移動し、ページ更新で同じPOSTが再送されるのを防ぐ。
                    exchange.getResponseHeaders().set("Location", "/?saved=1");
                    exchange.sendResponseHeaders(303, -1);
                } catch (IllegalArgumentException e) {
                    renderFormError(exchange, path, form, e.getMessage(), 400);
                } catch (IOException e) {
                    System.err.println("保存失敗: " + e);
                    // 意図: 保存失敗を成功扱いにせず、入力内容を残して再試行できるようにする。
                    renderFormError(exchange, path, form, "保存できませんでした。保存先の空き容量・権限を確認してください。入力内容はこの画面に残しています。", 500);
                }
            } else {
                exchange.getResponseHeaders().set("Allow", "GET, POST");
                send(exchange, 405, HtmlViews.error("この操作は利用できません。"));
            }
        } catch (NoSuchElementException e) {
            send(exchange, 404, HtmlViews.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            send(exchange, 400, HtmlViews.error(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            send(exchange, 500, HtmlViews.error("処理中に問題が発生しました。"));
        } finally {
            exchange.close(); // 意図: 通信の後片付けを、正常時も例外時も必ず行う。
        }
    }

    private void renderFormError(HttpExchange e, String path, Map<String, String> form, String error, int code) throws IOException {
        send(e, code, path.equals("/create") ? HtmlViews.create(form, error, csrf)
            : HtmlViews.detail(service.find(id(form)), form, error, csrf));
    }

    private static long id(Map<String, String> values) {
        try {
            long id = Long.parseLong(values.getOrDefault("id", ""));
            if (id <= 0) throw new NumberFormatException();
            return id;
        } catch (NumberFormatException e) { throw new IllegalArgumentException("問い合わせ番号が不正です。"); }
    }

    static Map<String, String> parse(String raw) {
        Map<String, String> result = new HashMap<>();
        if (raw == null || raw.isEmpty()) return result;
        for (String pair : raw.split("&")) {
            String[] parts = pair.split("=", 2);
            // 意図: URLの日本語や改行は符号化されているため、UTF-8で元の文字に戻す。
            result.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8),
                parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "");
        }
        return result;
    }

    private static void send(HttpExchange exchange, int code, String html) throws IOException {
        sendBytes(exchange, code, "text/html; charset=UTF-8", html.getBytes(StandardCharsets.UTF_8));
    }

    private static void sendBytes(HttpExchange exchange, int code, String type, byte[] bytes) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.getResponseHeaders().set("Content-Security-Policy", "default-src 'none'; style-src 'self'; form-action 'self'; base-uri 'none'; frame-ancestors 'none'");
        exchange.sendResponseHeaders(code, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
