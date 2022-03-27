#! /bin/bash

set -e
set -o pipefail

echo ""

gradle assemble --no-daemon --build-cache

java -Dcom.sun.management.jmxremote -Xmx128m -jar ./build/libs/gitlabstatscrawler-*-all.jar
