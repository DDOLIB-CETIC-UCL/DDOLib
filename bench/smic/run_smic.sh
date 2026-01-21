#!/usr/bin/env bash
set -e

INST_DIR="../../data/SMIC"
TIME_LIMIT=600000
OUT_DIR="results"

mkdir -p "$OUT_DIR"

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

for inst in "${INSTANCES[@]}"; do
  echo "## Running $inst" >&2

  OUT_FILE_DDO="$OUT_DIR/ddo_${inst}.txt"
  ./ddo_smic.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_DDO"

  OUT_FILE_ASTAR="$OUT_DIR/astar_${inst}.txt"
  ./astar_smic.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ASTAR"
#  OUT_FILE_ACS="$OUT_DIR/acs_${inst}.txt"
#  ./acs_smic.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
#    2>&1 | grep '^%%' > "$OUT_FILE_ACS"
done