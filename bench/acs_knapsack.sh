cd ..
mvn exec:java \
  -Dexec.mainClass="org.ddolib.examples.bench.KSAcsMain" \
  -Dexec.args="bench/$1 $2" \
  | grep '^%%'

cd bench