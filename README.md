# Support Note — 問い合わせ管理アプリ

社内問い合わせの「何が未対応か分からない」「以前の対応方法を探しにくい」を解決する、Javaの学習用ポートフォリオです。

![一覧画面](docs/screenshot-desktop.png)

## できること

- 件名・分類・内容を入力して問い合わせを登録
- 優先度を「低・中・高」から選び、一覧表示・保存・再起動後の復元に対応
- 未対応／対応中／完了の3段階で状況を管理
- 対応メモを残し、件名・内容・メモからキーワード検索


## 技術と今回の選択

| 技術 | 役割 | 選んだ理由 |
|---|---|---|
| Java 17以降 | アプリの処理 | クラス、条件分岐、繰り返し、例外、Listを学ぶ |
| JDK標準のHttpServer | ブラウザからのリクエスト受付 | フレームワーク導入前に画面とJavaのつながりを追う |
| HTML / CSS | 画面と見た目 | JavaScriptなしでもフォームで動くようにする |
| Properties形式のファイル | 永続化＝終了後もデータを残す | 外部ライブラリなしで日本語・改行・記号を保存する |
| Javaによる自動テスト | 重要な処理の検証 | 入力ルールと保存失敗の挙動を確認する |
| GitHub Actions | 変更時の自動テスト | GitHubにpushするとテストが実行される構成 |

フレームワーク・データベース・ログイン機能は今回の範囲に含めません。**これはJava基礎を学ぶ第1段階の作品です。Spring BootやSQLの経験を示す作品ではありません。** 今後の発展案は `docs/LEARNING.md` に記載しています。

## 最初に読む順番

1. このREADME：目的と起動方法
2. `docs/START_HERE.md`：初回の操作を順番に実施
3. `docs/JAVA_BASICS.md` と `docs/CODE_NOTES.md`：基本用語と各処理の理由
4. `docs/LEARNING.md`：自分で変更する学習課題
5. `docs/INTERVIEW.md`：面接で説明できるかを確認
6. `docs/GITHUB.md`：公開手順と確認する内容

## 起動方法

必要なのは **JDK 17以降**です。JDKはJavaをコンパイル・実行するための開発ツール一式です。JRE（実行専用）だけでは足りません。ターミナルまたはコマンドプロンプトで、次を確認します。

```text
java -version
javac -version
```

ZIPを展開して `support-note` フォルダに移動します。

### Windows

```bat
run.bat --demo
```

### macOS / Linux

```bash
bash run.sh --demo
```

ブラウザで **http://127.0.0.1:8080** を開きます。初回の `--demo` は架空のサンプル3件を登録します。既存データがある場合は追加しません。通常起動は `run.bat` または `bash run.sh`。停止は起動した画面で `Ctrl+C`。

手動で起動する場合は、プロジェクトのルートで次を実行します。

```bash
javac -encoding UTF-8 --release 17 -d out src/supportnote/*.java
java -cp out supportnote.App
```

Windowsの手動コンパイルではパスを `src\supportnote\*.java` にします。

## 保存について

- 保存先：プロジェクト内の `data/tickets.properties`
- 保存した後にメモリ上の一覧を更新するため、保存失敗を成功扱いにしません。
- 一時ファイルへの書き込み完了後に置換します。一括置換に非対応の環境では通常置換を使います。
- ファイルが壊れている場合は起動を止めます。勝手に空扱いにして上書きしません。
- バックアップするときはアプリを止めて `data` フォルダをコピーします。
- **電源断などあらゆる状況での保存を保証するものではありません。** 複数のアプリを同じ保存先で同時起動する使い方も対象外です。
- 問い合わせの削除は、この版では対応記録を残す目的で実装していません。誤登録なら対応メモに「誤登録」と残します。

## テスト

```bash
bash test.sh
```

Windowsでは `test.bat`。32項目を確認し、成功時に `PASS: 32 checks` と表示します。テストは一時フォルダを使い、実際の問い合わせデータは変更しません。
優先度について、指定なしで「中」になること、低・高での登録、
不正な値の拒否、状態変更時の保持、保存ファイルからの復元を確認しています。

確認項目と、この制作時の実測結果は `docs/VERIFICATION.md` に記載しています。

## 設定の変更

ポート8080が使用中なら、手動コンパイル後に次を実行します。

```bash
java -Dsupport.port=8081 -cp out supportnote.App
```

保存先も指定できます。

```bash
java -Dsupport.data=data/demo.properties -cp out supportnote.App --demo
```

## 利用範囲

このPC自身からのみ接続できる、個人用・学習用のアプリです。インターネット公開用の構成ではありません。スマートフォン幅の表示には対応していますが、スマートフォンからPCへ接続する設定は含みません。スクリーンショットや架空データで動作を紹介してください。

入力したHTMLを文字として扱うXSS対策、別サイトからの更新を防ぐCSRF対策、送信サイズの制限を入れています。共同利用向けの認証・権限管理・変更履歴・競合制御は未実装です。

## 制作方法を正直に説明する

この初期版のコード・資料はAI支援で生成しています。まだ理解・修正していない処理を「自力で設計・実装した」とは記載しないでください。自分で変更した内容や、実際に起きた問題を `docs/MY_CHANGES.md` に記録すると、説明する材料になります。

## 参照した公式資料

初期版のコードと資料はAI支援で作成しました。
その後、AIの説明やコード例を参考に、自分でコードを編集して
優先度機能を追加しました。

画面から登録処理へ値を渡す処理、enumによる選択肢の固定、
ファイル保存と復元、入力エラー時の選択保持を変更しました。
また、優先度の自動テストを7項目追加し、合計32項目の成功を確認しました。

作業中の問題と変更内容は [変更記録](docs/MY_CHANGES.md) に記載しています。
- [Java 17 HttpServer](https://docs.oracle.com/en/java/javase/17/docs/api/jdk.httpserver/com/sun/net/httpserver/HttpServer.html)
- [Properties](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/util/Properties.html)
- [Files](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html)
- [GitHub Actions / Javaのビルドとテスト](https://docs.github.com/en/actions/use-cases-and-examples/building-and-testing/building-and-testing-java-with-maven)

※本作品はMavenを使いません。GitHubの資料はJDKのセットアップと自動テストの考え方の参考です。
