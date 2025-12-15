cd ..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.KSAstarMain" \
  -Dexec.args="bench/$1 $2" \
  | grep '^%%'

cd bench