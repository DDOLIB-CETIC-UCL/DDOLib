#!/usr/bin/env bash
set -e

INST_DIR="../../data/Knapsack"
TIME_LIMIT=4000
OUT_DIR="results"

mkdir -p "$OUT_DIR"

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

for inst in "${INSTANCES[@]}"; do
  echo "## Running $inst" >&2

  OUT_FILE_DDO="$OUT_DIR/ddo_${inst}.txt"
  ./ddo_knapsack.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_DDO"

  OUT_FILE_ASTAR="$OUT_DIR/astar_${inst}.txt"
  ./astar_knapsack.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ASTAR"

  OUT_FILE_ACS="$OUT_DIR/acs_${inst}.txt"
  ./acs_knapsack.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ACS"

done