package supportnote;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.file.Path;

/** 起動準備を担当。処理本体をここに詰め込まず、各クラスの組み合わせだけを書いている。 */
public class App {
    public static void main(String[] args) throws Exception {
        // 意図: テストや複数のデモで保存先を分けられるよう、設定を外から指定できるようにする。
        int port = Integer.parseInt(System.getProperty("support.port", "8080"));
        Path data = Path.of(System.getProperty("support.data", "data/tickets.properties"));
        TicketRepository repository = new TicketRepository(data);
        TicketService service = new TicketService(repository);
        if (args.length > 0 && args[0].equals("--demo") && repository.findAll().isEmpty()) {
            // 意図: 空のときだけ架空データを作り、既存データを上書きしない。
            service.create("社内Wi-Fiに接続できない", "ネットワーク", "会議室でネットワークにつながりません。端末の再起動は実施済みです。");
            Ticket working = service.create("モニターに映像が表示されない", "PC・周辺機器", "外部モニターを接続しても画面が表示されません。ケーブルの接続を確認したいです。");
            service.update(working.id, "WORKING", "別のケーブルで切り分け中。端末本体の画面は正常です。");
            Ticket done = service.create("社内アカウントにログインできない", "アカウント", "パスワード変更後にログインできなくなりました。");
            service.update(done.id, "DONE", "保存済みの旧パスワードを削除し、新しいパスワードでのログインを確認しました。");
        }
        // 意図: 認証なしの学習用なので、接続はこのPC自身に限定する。
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", new WebController(service, Path.of("web/style.css")));
        // 意図: 単一の受付スレッドを使い、初学者向けの版では並列処理を増やさない。
        server.setExecutor(null);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));
        server.start();
        System.out.println("Support Note: http://127.0.0.1:" + port);
        System.out.println("保存先: " + data.toAbsolutePath());
        System.out.println("終了: Ctrl+C");
    }
}
