cd ../..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.TSPTWAcsMain" \
  -Dexec.args="bench/tsptw/$1 $2" \
  | grep '^%%'

cd bench/tsptw