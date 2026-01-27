#!/usr/bin/env bash
set -e


################################################################################
# KNAPSACK BENCHMARK CONFIGURATION
#
# This script defines the specific configuration (instances, data paths, classes)
# for the Knapsack problem.
#
# It relies on the shared 'benchmark_runner.sh' script to perform the actual
# execution logic (compilation, logging, error handling).
#
# To run: ./run_knapsack.sh
################################################################################

# Problem Configuration
PROBLEM_NAME="knapsack"
DATA_DIR="data/Knapsack"
TIME_LIMIT=10000

# Main Java Classes Definition
CLASS_DDO="org.ddolib.examples.bench.KSDdoMain"
CLASS_ASTAR="org.ddolib.examples.bench.KSAstarMain"
CLASS_ACS="org.ddolib.examples.bench.KSAcsMain"

# Instances List
INSTANCES=(
  instance_n100_c500_10_5_10_5_0
  instance_n100_c500_10_5_10_5_1
  instance_n100_c500_10_5_10_5_2
  instance_n100_c500_10_5_10_5_3
  instance_n100_c500_10_5_10_5_4
  instance_n100_c500_10_5_10_5_5
  instance_n100_c500_10_5_10_5_6
  instance_n100_c500_10_5_10_5_7
  instance_n100_c500_10_5_10_5_8
  instance_n100_c500_10_5_10_5_9
  instance_n1000_c1000_10_5_10_5_0
  instance_n1000_c1000_10_5_10_5_1
  instance_n1000_c1000_10_5_10_5_2
  instance_n1000_c1000_10_5_10_5_3
  instance_n1000_c1000_10_5_10_5_4
  instance_n1000_c1000_10_5_10_5_5
  instance_n1000_c1000_10_5_10_5_6
  instance_n1000_c1000_10_5_10_5_7
  instance_n1000_c1000_10_5_10_5_8
  instance_n1000_c1000_10_5_10_5_9
)

# Load and execute the runner
source ../benchmark_runner.sh