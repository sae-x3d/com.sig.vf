#!/usr/bin/env bash
set -e
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
DIR=/home/sae-x3d/INFO/S4/SIG/app-v2
JAR="$DIR/target/app-0.0.1-SNAPSHOT.jar"
echo "=== Compilation ==="
cd "$DIR"
export JAVA_HOME
mvn package -DskipTests -q
echo ""
echo "=== Lancement ==="
echo "URL: http://localhost:8080/"
echo "Ctrl+C pour arrêter"
"$JAVA_HOME/bin/java" -jar "$JAR"
