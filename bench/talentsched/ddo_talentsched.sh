#!/usr/bin/env bash

set -e

cd ../..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.TSDdoMain" \
  -Dexec.args="bench/talentsched/$1 $2"

cd bench/talentsched