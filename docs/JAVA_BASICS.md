# このコードで使うJavaの基本

## 用語を短く説明

| 書き方 | 意味 | この作品での例 |
|---|---|---|
| class | データや処理をまとめた設計図 | TicketService |
| new | 設計図から実物を作る | new TicketService(repository) |
| コンストラクタ | newした直後の準備処理 | TicketRepository(Path file) |
| String | 文字列 | 件名、本文、メモ |
| long / int | 整数。longの方が大きい値を扱える | ID、件数 |
| boolean | true / falseの2値 | 必須かどうか |
| List<Ticket> | Ticketを複数並べた一覧 | 問い合わせ全件 |
| Map<String, String> | 名前と値を対応させる入れ物 | title → 入力した件名 |
| enum | 選択肢を固定する型 | OPEN / WORKING / DONE |
| final | 代入し直せない指定 | 問い合わせの各項目 |
| if | 条件が成立したときだけ処理する | 必須なのに空なら拒否 |
| for | 同じ処理を繰り返す | 一覧から1件ずつ探す |
| return | 呼び出し元へ結果を返して終了する | 検索結果を返す |
| throw | 問題を例外として伝える | 件名が空のとき |
| try / catch | 失敗する可能性のある処理と対処 | 保存失敗を知らせる |
| throws IOException | ファイル・通信の失敗を呼び出し元に伝える宣言 | saveAll |
| synchronized | 同じ対象の処理を順番に実行する | 登録・更新 |
| static | インスタンスを作らずクラスに属する | main、escape |
| package / import | コードの所属名と、使うクラスの指定 | package supportnote |

## 登録を1件ずつ追う

操作：「件名＝印刷できない、分類＝PC・周辺機器、内容＝印刷待ちで止まる」を登録。

1. HTMLフォームの `name="title"` が、送信する項目名になる。
2. WebControllerの `parse` が、受け取った値をMapに入れる。
3. `form.getOrDefault("title", "")` で件名を取り出す。項目がなければ空文字にする。
4. `service.create(...)` を呼ぶ。
5. `checkedText` が空白と文字数を確認する。
6. 既存データから新しいIDを決める。
7. `new Ticket(...)` で1件のデータを作る。
8. `next.add(ticket)` で保存予定の一覧に追加する。
9. `repository.saveAll(next)` がファイルに保存する。
10. 保存成功後、WebControllerが一覧ページへの移動を返す。

「どのデータが、どの変数に入ったか」をノートに書くと追いやすくなります。

## 分からなくてよいけれど、後で読む場所

- `Comparator.comparingLong((Ticket t) -> t.id).reversed()`：IDが大きいものから並べる。`->` はラムダ式＝短く書いた処理。
- `"...".formatted(...)`：文字列の `%s` や `%d` に値を入れる。
- `"""`：複数行の文字列を読みやすく書くtext block。
- `switch` の `case ... ->`：URLに応じて処理を振り分ける。
- テストの `@FunctionalInterface`：処理を引数として渡すための型。

最初はTicketとServiceを優先してください。HTTP通信やHTML生成を一度に暗記する必要はありません。

## 自分の理解を確認する問題

1. Ticketにはどんな情報が入っている？
2. 入力された件名が空白だけだと、どこで止まる？
3. 登録直後の状態はどこで決める？
4. ファイル保存に失敗したとき、一覧は更新される？
5. 「完了にはメモが必要」の条件を、どこで判断する？
6. 検索対象を増やすなら、どのメソッドを変更する？

答えはコードを見ながらで構いません。説明できない場所はMY_CHANGESの未確認欄に残してください。
