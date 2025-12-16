cd ../..
pmd
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.TSPTWDdoMain" \
  -Dexec.args="bench/tsptw/$1 $2" \
  | grep '^%%'

cd bench/tsptw