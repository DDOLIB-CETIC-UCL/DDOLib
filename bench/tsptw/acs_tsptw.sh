#!/usr/bin/env bash

set -e

cd ../..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.TSPTWAcsMain" \
  -Dexec.args="bench/tsptw/$1 $2"

cd bench/tsptw