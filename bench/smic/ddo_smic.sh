#!/usr/bin/env bash

set -e

cd ../..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.SMICDdoMain" \
  -Dexec.args="bench/smic/$1 $2"

cd bench/smic