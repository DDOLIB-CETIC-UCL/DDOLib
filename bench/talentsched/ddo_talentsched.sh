cd ../..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.TSDdoMain" \
  -Dexec.args="bench/talentsched/$1 $2" \
  | grep '^%%'

cd bench/talentsched