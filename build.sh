#!/bin/bash
set -e

cd "$(dirname "$0")"

STARSECTOR_CORE="../.."
MODS_DIR=".."

CLASSPATH=""
for jar in "$STARSECTOR_CORE"/*.jar; do
    [ -f "$jar" ] && CLASSPATH="${CLASSPATH:+$CLASSPATH:}$jar"
done
for jar in \
    "$MODS_DIR"/LazyLib*/jars/*.jar \
    "$MODS_DIR"/LazyLib*/jars/internal/*.jar \
    "$MODS_DIR"/MagicLib*/jars/*.jar \
    "$MODS_DIR"/*GraphicsLib*/jars/*.jar \
    "$MODS_DIR"/Nexerelin*/jars/*.jar \
    "$MODS_DIR"/LunaLib*/jars/*.jar; do
    [ -f "$jar" ] && CLASSPATH="$CLASSPATH:$jar"
done

rm -rf bin build build_classes sources.txt
mkdir -p bin jar jars
find src -name "*.java" | sort > sources.txt

javac -encoding UTF-8 -Xlint:-options --release 17 -sourcepath "" -implicit:none -cp "$CLASSPATH" -d bin @sources.txt
jar cf jar/SEEKER.jar -C bin .
cp jar/SEEKER.jar jars/SEEKER.jar
rm -rf bin sources.txt

echo "Jar built successfully: jar/SEEKER.jar"
