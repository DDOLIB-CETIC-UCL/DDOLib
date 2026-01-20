#!/usr/bin/env bash
set -e

################################################################################
# TALENT SCHEDULING BENCHMARK CONFIGURATION
#
# This script defines the specific configuration (instances, data paths, classes)
# for the Talent Scheduling problem.
#
# It relies on the shared 'benchmark_runner.sh' script to perform the actual
# execution logic (compilation, logging, error handling).
#
# To run: ./run_talentsched.sh
################################################################################

# Problem Configuration
PROBLEM_NAME="talentsched"
DATA_DIR="data/TalentScheduling"
TIME_LIMIT=4000

# Main Java Classes Definition
CLASS_DDO="org.ddolib.examples.bench.TSDdoMain"
CLASS_ASTAR="org.ddolib.examples.bench.TSAstarMain"
CLASS_ACS="org.ddolib.examples.bench.TSAcsMain"

# Instances List
INSTANCES=(
  film103.dat
  film105.dat
  film116.dat
  film119.dat
  film118.dat
  film117.dat
  film114.dat
  'film-12'
  MobStory
  Shaw2020
)

# Load and execute the runner
source ../benchmark_runner.sh