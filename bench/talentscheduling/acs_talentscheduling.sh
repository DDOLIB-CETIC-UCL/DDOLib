cd ../..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.TSAcsMain" \
  -Dexec.args="bench/talentscheduling/$1 $2"

cd bench/talentscheduling