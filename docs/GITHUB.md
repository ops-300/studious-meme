# GitHubに載せる手順

このZIPはリポジトリに置ける構成です。今回の納品時点ではGitHubへのアップロードはしていません。

## 公開前の確認

1. アプリを起動して登録・更新・再起動を試す。
2. test.batまたはbash test.shでテストを実行する。
3. MY_CHANGESに実際に理解・変更した内容を残す。
4. 個人名・社員名・実際の社内情報を入れない。サンプルは架空データだけにする。
5. data、outをアップロードしない。.gitignoreにはこの2つを記載済み。

## Gitを使う場合

GitHubで空のリポジトリ `support-note-java` を作成する。Gitはコードの変更履歴を管理するツール、リポジトリはその保存場所。

プロジェクトのルートで次を実行する。`YOUR_NAME`は自分のGitHub名に置き換える。

```bash
git init
git add .
git commit -m "Add Java support ticket app with design notes and tests"
git branch -M main
git remote add origin https://github.com/YOUR_NAME/support-note-java.git
git push -u origin main
```

GitHubで先にREADMEを作ると、手元の履歴との調整が必要になる。初回は空のリポジトリにする。認証はGitHubが案内する方法で行う。

## ブラウザからアップロードする場合

- 展開したプロジェクトの中身をアップロードする。ZIPそのものだけではコードが読みにくい。
- src、test、web、docs、README、起動・テストスクリプトを載せる。
- `.github/workflows`と`.gitignore`は隠しファイルとして見えないことがある。自動テストまで有効にするならGitを使う方法が確実。

## 公開後の確認

- トップにREADMEとスクリーンショットが表示される。
- コード内の日本語の意図メモが見える。
- Actions画面でテスト結果を確認する。制作時点ではGitHub上での実行は未確認。
- リポジトリURLを応募資料に載せる。

## 説明欄の案

Java基礎学習用の問い合わせ管理アプリ。登録・対応状況更新・対応メモ検索・ファイル保存を実装。日本語の設計メモと自動テスト付き。初期版はAI支援で作成。

修正したら「分類絞り込みを自分で追加」など、実際の変更を追記する。未使用のSpring・SQLを技術欄に書かない。
