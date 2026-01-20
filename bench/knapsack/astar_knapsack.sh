#!/usr/bin/env bash

set -e

cd ../..

mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.KSAstarMain" \
  -Dexec.args="bench/knapsack/$1 $2"

cd bench/knapsack