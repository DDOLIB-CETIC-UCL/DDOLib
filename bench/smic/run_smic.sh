#!/usr/bin/env bash
set -e

################################################################################
# SMIC BENCHMARK CONFIGURATION
#
# This script defines the specific configuration (instances, data paths, classes)
# for the SMIC problem.
#
# It relies on the shared 'benchmark_runner.sh' script to perform the actual
# execution logic (compilation, logging, error handling).
#
# To run: ./run_smic.sh
################################################################################

# Problem Configuration
PROBLEM_NAME="smic"
DATA_DIR="data/SMIC"
TIME_LIMIT=4000

# Main Java Classes Definition
CLASS_DDO="org.ddolib.examples.bench.SMICDdoMain"
CLASS_ASTAR="org.ddolib.examples.bench.SMICAstarMain"
CLASS_ACS="org.ddolib.examples.bench.SMICAcsMain"

# Instances List
INSTANCES=(
  data10_1.txt
  data10_2.txt
  data10_3.txt
  data10_4.txt
  data10_5.txt
  data10_6.txt
  data10_7.txt
  data10_8.txt
  data10_9.txt
  data10_10.txt
  data20_1.txt
  data20_2.txt
  data20_3.txt
  data20_4.txt
  data20_5.txt
  data20_6.txt
  data20_7.txt
  data20_8.txt
  data20_9.txt
  data20_10.txt
)

# Load and execute the runner
source ../benchmark_runner.sh