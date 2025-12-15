cd ../..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.TSDdoMain" \
  -Dexec.args="bench/talentscheduling/$1 $2" \
  | grep '^%%'

cd bench/talentscheduling