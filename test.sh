#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p out
javac -encoding UTF-8 --release 17 -d out src/supportnote/*.java test/supportnote/*.java
java -cp out supportnote.SupportNoteTest
