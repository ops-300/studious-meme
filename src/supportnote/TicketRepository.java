package supportnote;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/** データの読み書きだけを担当。保存先を変えるときに、業務ルールへの影響を小さくする。 */
public class TicketRepository {
    private final Path file;
    private List<Ticket> tickets = new ArrayList<>();

    public TicketRepository(Path file) throws IOException {
        this.file = file.toAbsolutePath();
        load();
    }

    private void load() throws IOException {
        if (!Files.exists(file)) return; // 意図: 初回起動では空の一覧から開始する。
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        try {
            if (!"1".equals(required(p, "version"))) throw new IllegalArgumentException("未知の保存形式");
            int count = Integer.parseInt(required(p, "count"));
            if (count < 0) throw new IllegalArgumentException("件数が不正");
            Set<Long> ids = new HashSet<>();
            for (int i = 0; i < count; i++) {
                String key = "ticket." + i + ".";
                long id = Long.parseLong(required(p, key + "id"));
                if (id <= 0 || !ids.add(id)) throw new IllegalArgumentException("IDが不正");
                tickets.add(new Ticket(id, required(p, key + "title"), required(p, key + "category"),
                // 意図: 再起動後も優先度を復元できるよう、保存形式に追加する。
                    required(p, key + "description"), Ticket.Status.valueOf(required(p, key + "status")),
                    Ticket.Priority.valueOf(p.getProperty(key + "priority", "NORMAL")),
                    required(p, key + "resolution"), LocalDateTime.parse(required(p, key + "createdAt")),
                    LocalDateTime.parse(required(p, key + "updatedAt"))));
            }
        } catch (RuntimeException e) {
            // 意図: 壊れたデータを空扱いにして上書きすると復旧できなくなる。起動を止めて知らせる。
            throw new IOException("保存ファイルを読めません。元ファイルを保管して内容を確認してください: " + file, e);
        }
    }

    private static String required(Properties p, String key) {
        String value = p.getProperty(key);
        if (value == null) throw new IllegalArgumentException("項目がありません: " + key);
        return value;
    }

    public synchronized List<Ticket> findAll() {
        // 意図: 呼び出し側に内部の一覧を直接渡さず、勝手な追加・削除を防ぐ。
        return new ArrayList<>(tickets);
    }

    public synchronized void saveAll(List<Ticket> next) throws IOException {
        Properties p = new Properties();
        p.setProperty("version", "1");
        p.setProperty("count", String.valueOf(next.size()));
        for (int i = 0; i < next.size(); i++) {
            Ticket t = next.get(i);
            String key = "ticket." + i + ".";
            p.setProperty(key + "id", String.valueOf(t.id));
            p.setProperty(key + "title", t.title);
            p.setProperty(key + "category", t.category);
            p.setProperty(key + "description", t.description);
            p.setProperty(key + "status", t.status.name());
            p.setProperty(key + "priority", t.priority.name());
            p.setProperty(key + "resolution", t.resolution);
            p.setProperty(key + "createdAt", t.createdAt.toString());
            p.setProperty(key + "updatedAt", t.updatedAt.toString());
        }
        Files.createDirectories(file.getParent());
        Path temp = Files.createTempFile(file.getParent(), "tickets-", ".tmp");
        try {
            // 意図: 改行・日本語の変換を標準のPropertiesに任せ、独自の区切り形式による破損を避ける。
            try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                p.store(writer, "Support Note data - do not edit while running");
            }
            // 意図: 書き終わるまで旧ファイルを残す。同じディスク上の一時ファイルで置き換える。
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                // 一括置換に非対応の環境でも動かす。ただしこの場合、突然の停止への保証は弱くなる。
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
            tickets = new ArrayList<>(next); // 保存成功後にだけメモリを更新する。
        } finally {
            Files.deleteIfExists(temp);
        }
    }
}
