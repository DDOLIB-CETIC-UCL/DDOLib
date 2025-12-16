#!/usr/bin/env bash
set -e

INST_DIR="../../data/TSPTW/AFG"
TIME_LIMIT=4000
OUT_DIR="results/AFG"

mkdir -p "$OUT_DIR"

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

for inst in "${INSTANCES[@]}"; do
  echo "## Running $inst" >&2

  OUT_FILE_DDO="$OUT_DIR/ddo_${inst}.txt"
  ./ddo_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_DDO"

  OUT_FILE_ASTAR="$OUT_DIR/astar_${inst}.txt"
  ./astar_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ASTAR"

  OUT_FILE_ACS="$OUT_DIR/acs_${inst}.txt"
  ./acs_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ACS"

done

INST_DIR="../../data/TSPTW/Dumas"
OUT_DIR="results/Dumas"

mkdir -p "$OUT_DIR"

INSTANCES=(
  n60w20.001.txt
  n60w20.002.txt
  n60w20.003.txt
  n60w20.004.txt
  n60w20.005.txt
  n100w20.001.txt
  n100w20.002.txt
  n100w20.003.txt
  n100w20.004.txt
  n100w20.005.txt
  n200w40.001.txt
  n200w40.002.txt
  n200w40.003.txt
  n200w40.004.txt
  n200w40.005.txt
)

for inst in "${INSTANCES[@]}"; do
  echo "## Running $inst" >&2

  OUT_FILE_DDO="$OUT_DIR/ddo_${inst}.txt"
  ./ddo_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_DDO"

  OUT_FILE_ASTAR="$OUT_DIR/astar_${inst}.txt"
  ./astar_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ASTAR"

  OUT_FILE_ACS="$OUT_DIR/acs_${inst}.txt"
  ./acs_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ACS"

done


INST_DIR="../../data/TSPTW/OhlmannThomas"
OUT_DIR="results/OhlmannThomas"

mkdir -p "$OUT_DIR"

INSTANCES=(
  n150w120.001.txt
  n150w120.002.txt
  n150w120.003.txt
  n150w120.004.txt
  n150w120.005.txt
  n200w120.001.txt
  n200w120.002.txt
  n200w120.003.txt
  n200w120.004.txt
  n200w120.005.txt
)

for inst in "${INSTANCES[@]}"; do
  echo "## Running $inst" >&2

  OUT_FILE_DDO="$OUT_DIR/ddo_${inst}.txt"
  ./ddo_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_DDO"

  OUT_FILE_ASTAR="$OUT_DIR/astar_${inst}.txt"
  ./astar_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ASTAR"

  OUT_FILE_ACS="$OUT_DIR/acs_${inst}.txt"
  ./acs_tsptw.sh "$INST_DIR/$inst" "$TIME_LIMIT" \
    2>&1 | grep '^%%' > "$OUT_FILE_ACS"

done

