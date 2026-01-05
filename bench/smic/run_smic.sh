#!/usr/bin/env bash
set -e

INST_DIR="../../data/SMIC"
TIME_LIMIT=4000
OUT_DIR="results"

mkdir -p "$OUT_DIR"

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

for inst in "${INSTANCES[@]}"; do
  echo "## Running $inst" >&2

  OUT_FILE_DDO="$OUT_DIR/ddo_${inst}.txt"
  ./ddo_smic.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_DDO"

  OUT_FILE_ASTAR="$OUT_DIR/astar_${inst}.txt"
  ./astar_smic.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ASTAR"

  OUT_FILE_ACS="$OUT_DIR/acs_${inst}.txt"
  ./acs_smic.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ACS"

done