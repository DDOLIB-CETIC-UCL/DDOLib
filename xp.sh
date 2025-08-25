
INSTANCE_DIR=("data/JobShop/Lawrence1984" "data/JobShop/FisherThompson1963" "data/JobShop/ApplegateCook1991")
SEARCH=("ACS" "Astar")
inputFile="config"
SOLVER_JAR="target/ddolib-0.0.1-jar-with-dependencies.jar"
launch_solver="java -Xmx12000m -jar  $SOLVER_JAR"
 for DIR in "${INSTANCE_DIR[@]}" ; do
     echo $DIR
     for INSTANCE_FILE in "$DIR"/*; do
         # Vérifie que c'est bien un fichier
         if [ -f "$INSTANCE_FILE" ]; then
             for S in "${SEARCH[@]}" ; do
                   echo "$INSTANCE_FILE,$S" >> "${inputFile}.txt"
             done
         fi
     done
 done

 cat "${inputFile}.txt" | parallel -j 1 --colsep ',' $launch_solver -i {1}  -s {2} -t 120
