cd ../..
pmd
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.SMICDdoMain" \
  -Dexec.args="bench/smic/$1 $2" \
  | grep '^%%'

cd bench/smic