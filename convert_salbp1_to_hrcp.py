#!/usr/bin/env python3
"""
Convert SALBP1 CSV instances (generated_SALBP1/) to HRCP format (HRCP/).

SALBP1 CSV format:
    Header: task,th,tr,tc,successor
    Rows:   1-indexed task id, human dur, robot dur, collab dur, successor list

HRCP format:
    # comment lines
    n
    humanDur robotDur collabDur numPreds [pred0 pred1 ...]   (0-indexed)

The script preserves the directory structure:
    generated_SALBP1/<subdir>/<file>.csv  ->  HRCP/<subdir>/<stem>
"""

import csv
import os
import sys
import re


def parse_successor_field(field: str) -> list[int]:
    """Parse a successor field like '[]', '[6]', or '[14, 15]' into a list of 0-indexed task ids."""
    field = field.strip().strip('"')
    if field == "[]":
        return []
    inner = field.strip("[]")
    return [int(x.strip()) - 1 for x in inner.split(",") if x.strip()]


def convert_csv_to_hrcp(csv_path: str, hrcp_path: str) -> None:
    """Convert a single SALBP1 CSV file to HRCP format."""
    # Read CSV
    tasks = []  # list of (task_id_1indexed, th, tr, tc, successors_0indexed)
    with open(csv_path, newline="") as f:
        reader = csv.reader(f)
        header = next(reader)  # skip header
        for row in reader:
            if len(row) < 5 or not row[0].strip():
                continue
            task_id = int(row[0].strip())
            th = int(row[1].strip())
            tr = int(row[2].strip())
            tc = int(row[3].strip())
            # The successor field may span columns if it was quoted with commas
            succ_str = ",".join(row[4:]).strip()
            successors = parse_successor_field(succ_str)
            tasks.append((task_id, th, tr, tc, successors))

    n = len(tasks)

    # Sort by task_id (1-indexed) to ensure order
    tasks.sort(key=lambda t: t[0])

    # Build predecessors from successors (both 0-indexed)
    predecessors = [[] for _ in range(n)]
    for task_id_1, _th, _tr, _tc, successors in tasks:
        src = task_id_1 - 1  # convert to 0-indexed
        for dst in successors:
            predecessors[dst].append(src)

    # Sort predecessor lists for determinism
    for preds in predecessors:
        preds.sort()

    # Write HRCP file
    os.makedirs(os.path.dirname(hrcp_path), exist_ok=True)
    with open(hrcp_path, "w") as f:
        f.write(f"# Converted from {os.path.basename(csv_path)}\n")
        f.write(f"{n}\n")
        for i in range(n):
            _, th, tr, tc, _ = tasks[i]
            preds = predecessors[i]
            parts = [str(th), str(tr), str(tc), str(len(preds))]
            parts.extend(str(p) for p in preds)
            f.write(" ".join(parts) + "\n")


def main():
    base = os.path.dirname(os.path.abspath(__file__))
    src_root = os.path.join(base, "data", "generated_SALBP1")
    dst_root = os.path.join(base, "data", "HRCP")

    if not os.path.isdir(src_root):
        print(f"Source directory not found: {src_root}", file=sys.stderr)
        sys.exit(1)

    converted = 0
    for subdir in sorted(os.listdir(src_root)):
        subdir_path = os.path.join(src_root, subdir)
        if not os.path.isdir(subdir_path):
            continue
        for fname in sorted(os.listdir(subdir_path)):
            if not fname.endswith(".csv"):
                continue
            csv_path = os.path.join(subdir_path, fname)
            stem = os.path.splitext(fname)[0]
            hrcp_path = os.path.join(dst_root, subdir, stem)
            convert_csv_to_hrcp(csv_path, hrcp_path)
            converted += 1

    print(f"Converted {converted} instances into {dst_root}")


if __name__ == "__main__":
    main()

