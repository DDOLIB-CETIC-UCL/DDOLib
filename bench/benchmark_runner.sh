#!/usr/bin/env bash
set -e


################################################################################
# GLOBAL BENCHMARK RUNNER
#
# Architecture:
#   This script acts as the centralized engine for the benchmarking process.
#   It isolates the logic for:
#     - Executing Maven with specific Java classes
#     - Handling directory navigation (pushd/popd)
#     - Managing logs and error detection
#     - Parsing results
#
#   It is designed to be "sourced" by problem-specific scripts, NOT run directly.
#
# Usage:
#   1. Create a specific script in a subfolder (e.g., bench/knapsack/run_knapsack.sh).
#   2. Define the required configuration variables:
#       - PROBLEM_NAME: Name of the problem (used for log folders).
#       - DATA_DIR: Path to data files relative to the PROJECT ROOT.
#       - CLASS_*: Java classes to execute (e.g., CLASS_DDO, CLASS_ASTAR).
#       - INSTANCES: Array of instance filenames.
#   3. Source this script at the end of your file:
#       source ../benchmark_runner.sh
#
################################################################################


# --- Default Configuration ---

TIME_LIMIT=${TIME_LIMIT:-4000}
OUT_DIR="results"
LOG_DIR="logs"

# Check if required variables are set
if [ -z "$PROBLEM_NAME" ]; then
    echo "Error: PROBLEM_NAME is not defined."
    exit 1
fi

if [ ${#INSTANCES[@]} -eq 0 ]; then
    echo "Error: INSTANCES array is empty."
    exit 1
fi


# --- Setup Directories ---
mkdir -p "$OUT_DIR"
mkdir -p "$LOG_DIR"


# --- The Logic Function ---
run_solver(){
  local script_name=$1
  local java_class=$2
  local instance=$3

  # If class is not defined, skip it
  if [ -z "$java_class" ]; then return; fi

  local log_file="$LOG_DIR/${script_name}_${instance}.log"
  local res_file="$OUT_DIR/${script_name}_${instance}.txt"

  echo "  > Running $script_name on $instance"

  pushd ../.. > /dev/null

  # Move to Project Root to run Maven
  set +e

  mvn -q exec:java \
      -Dexec.mainClass="$java_class" \
      -Dexec.args="$DATA_DIR/$instance $TIME_LIMIT" \
      > "bench/$PROBLEM_NAME/$log_file" 2>&1

  local exit_code=$?
  set -e

  # Return to bench/xxx
  popd > /dev/null

  # Handle Results / Errors
  if [ $exit_code -ne 0 ]; then
    echo "    [ERROR] Script $script_name failed for $instance"
    echo " --- Error Log Preview (last 20 lines) ---"
    tail -n 20 "$log_file"
    echo " --- End of Log"
    exit 1
  else
    grep '^%%' "$log_file" > "$res_file"
    echo "    [SUCCESS] Results saved to $res_file"
  fi
}

# --- Main Execution Loop ---

echo "========================================"
echo "Starting Benchmark for: $PROBLEM_NAME"
echo "Instances count: ${#INSTANCES[@]}"
echo "========================================"

for inst in "${INSTANCES[@]}"; do
  echo "## Processing $inst" >&2

  # Dynamic script names based on problem name
  # Example: ddo_knapsack.sh, astar_smic.sh

  run_solver "ddo"   "$CLASS_DDO"   "$inst"
  run_solver "astar" "$CLASS_ASTAR" "$inst"
  run_solver "acs"   "$CLASS_ACS"   "$inst"
  echo ""
done

# Cleanup logs only if successful
rm -rf "$LOG_DIR"

echo "All benchmarks completed successfully."