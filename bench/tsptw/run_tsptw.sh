#!/usr/bin/env bash
set -e

INST_DIR="../../data/TSPTW/AFG"
TIME_LIMIT=4000
OUT_DIR="results"
LOG_DIR="logs"

mkdir -p "$OUT_DIR"
mkdir -p "$LOG_DIR"

INSTANCES=(
  rbg010a.tw
  rbg016a.tw
  rbg016b.tw
  rbg017.2.tw
  rbg017.tw
  rbg035a.2.tw
  rbg035a.tw
  rbg038a.tw
  rbg040a.tw
  rbg041a.tw
  rbg193.2.tw
  rbg193.tw
  rbg201a.tw
  rbg233.2.tw
  rbg233.tw
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
  run_solver "ddo_tsptw.sh" "ddo" "$inst"

  # Run A*
  run_solver "astar_tsptw.sh" "astar" "$inst"

  # Run ACS
  run_solver "acs_tsptw.sh" "acs" "$inst"

  echo ""
done

#Remove the log file
rm -rf "$LOG_DIR"