#!/usr/bin/env bash
set -e

INST_DIR="../../data/Knapsack"
TIME_LIMIT=4000
OUT_DIR="results"
LOG_DIR="logs"

mkdir -p "$OUT_DIR"
mkdir -p "$LOG_DIR"

INSTANCES=(
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


run_solver(){
  local script_name=$1
  local output_prefix=$2
  local instance=$3


  local log_file="$LOG_DIR/${output_prefix}_${instance}.log"
  local res_file="$OUT_DIR/${output_prefix}_${instance}.txt"

  echo "  > Running $output_prefix on $instance"

  set +e
  bash "$script_name" "$INST_DIR/$instance" "$TIME_LIMIT" > "$log_file" 2>&1
  local exit_code=$?
  set -e

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


for inst in "${INSTANCES[@]}"; do
  echo "## Processing $inst" >&2

  # Run DDO
  run_solver "ddo_knapsack.sh" "ddo" "$inst"

  # Run A*
  run_solver "astar_knapsack.sh" "astar" "$inst"

  # Run ACS
  run_solver "acs_knapsack.sh" "acs" "$inst"

  echo ""
done

#Remove the log file
rm -rf "$LOG_DIR"