#!/usr/bin/env sh
set -eu
ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
TEST_DIR="$ROOT_DIR/build/test-classes"
rm -rf "$TEST_DIR"
mkdir -p "$TEST_DIR"
find "$ROOT_DIR/src/main/java" "$ROOT_DIR/src/test/java" -name '*.java' | sort > "$ROOT_DIR/build/test-sources.txt"
javac --release 21 -d "$TEST_DIR" @"$ROOT_DIR/build/test-sources.txt"
java -cp "$TEST_DIR" com.duoforge.lobby.LobbyEngineTest
