cd ../..
pmd
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.SMICAstarMain" \
  -Dexec.args="bench/smic/$1 $2" \
  | grep '^%%'

cd bench/smic