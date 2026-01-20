#!/usr/bin/env bash
set -e

INST_DIR="../../data/SMIC"
TIME_LIMIT=4000
OUT_DIR="results"
LOG_DIR="logs"

mkdir -p "$OUT_DIR"
mkdir -p "$LOG_DIR"

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
  run_solver "ddo_smic.sh" "ddo" "$inst"

  # Run A*
  run_solver "astar_smic.sh" "astar" "$inst"

  # Run ACS
  run_solver "acs_smic.sh" "acs" "$inst"

  echo ""
done

#Remove the log file
rm -rf "$LOG_DIR"