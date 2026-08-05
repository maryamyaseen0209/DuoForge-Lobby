#!/usr/bin/env sh
set -eu
ROOT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
BUILD_DIR="$ROOT_DIR/build"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/classes"
find "$ROOT_DIR/src/main/java" -name '*.java' | sort > "$BUILD_DIR/sources.txt"
javac --release 21 -d "$BUILD_DIR/classes" @"$BUILD_DIR/sources.txt"
cp -R "$ROOT_DIR/src/main/resources/." "$BUILD_DIR/classes/"
jar --create --file "$BUILD_DIR/duoforge-lobby.jar" --main-class com.duoforge.lobby.DuoForgeApplication -C "$BUILD_DIR/classes" .
echo "Built $BUILD_DIR/duoforge-lobby.jar"
