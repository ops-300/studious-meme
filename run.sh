#!/usr/bin/env bash
# 意図: 必ずプロジェクトの場所に移動し、保存先とCSSの相対パスを安定させる。
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 --release 17 -d out src/supportnote/*.java
java -cp out supportnote.App "$@"
