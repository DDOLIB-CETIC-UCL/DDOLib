#!/usr/bin/env bash
set -e

INST_DIR="../../data/TalentScheduling"
TIME_LIMIT=4000
OUT_DIR="results"

mkdir -p "$OUT_DIR"

INSTANCES=(
  film103.dat
  film105.dat
  film116.dat
  film119.dat
  film118.dat
  film117.dat
  film114.dat
)

for inst in "${INSTANCES[@]}"; do
  echo "## Running $inst" >&2

  OUT_FILE_DDO="$OUT_DIR/ddo_${inst}.txt"
  ./ddo_talentscheduling.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_DDO"

  OUT_FILE_ASTAR="$OUT_DIR/astar_${inst}.txt"
  ./astar_talentscheduling.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ASTAR"

  OUT_FILE_ACS="$OUT_DIR/acs_${inst}.txt"
  ./acs_talentscheduling.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ACS"

done