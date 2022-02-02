#! /bin/bash

set -e
set -o pipefail

./gradlew -v
echo ""

./gradlew assemble --no-daemon

java -Dcom.sun.management.jmxremote -Xmx128m -jar ./build/libs/gitlabstatscrawler-*-all.jar
