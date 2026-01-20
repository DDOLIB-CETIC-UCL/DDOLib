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
  data20_1.txt
  data20_2.txt
  data20_3.txt
  data30_1.txt
  data30_2.txt
  data30_3.txt
  data40_1.txt
  data40_2.txt
  data40_3.txt
  data50_1.txt
  data50_2.txt
  data50_3.txt
  data60_1.txt
  data60_2.txt
  data60_3.txt
  data70_1.txt
  data70_2.txt
  data70_3.txt
  data80_1.txt
  data80_2.txt
  data80_3.txt
  data90_1.txt
  data90_2.txt
  data90_3.txt
  data100_1.txt
  data100_2.txt
  data100_3.txt
)

# Load and execute the runner
source ../benchmark_runner.sh