package org.ddolib.ddo.examples.alp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Instance of an ALP.
 */
public class ALPInstance {
    public int nbClasses;
    public int nbAircraft;
    public int nbRunways;
    public int[] aircraftClass;
    public int[] aircraftTarget;
    public int[] aircraftDeadline;
    public int[][] classTransitionCost;
    // Optimal solution
    public Optional<Integer> optimal;

    /**
     * Extracts an ALP from a formatted file.
     *
     * @param fName The name of the file.
     * @throws IOException Exceptions reading the file.
     */
    public ALPInstance(String fName) throws IOException {
        final File f = new File(fName);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            int lineCounter = 0;
            List<String> linesList = bf.lines().toList();
            linesList = linesList.stream().filter(line -> !line.isBlank()).toList();
            for (String line : linesList) {
                String[] splitLine = line.split(" ");
                if (lineCounter == 0) {
                    nbAircraft = Integer.parseInt(splitLine[0]);
                    nbClasses = Integer.parseInt(splitLine[1]);
                    nbRunways = Integer.parseInt(splitLine[2]);
                    if (splitLine.length == 4) {
                        optimal = Optional.of(Integer.parseInt(splitLine[3]));
                    } else {
                        optimal = Optional.empty();
                    }
                    aircraftClass = new int[nbAircraft];
                    aircraftDeadline = new int[nbAircraft];
                    aircraftTarget = new int[nbAircraft];
                    classTransitionCost = new int[nbClasses][nbClasses];
                } else if (lineCounter < nbAircraft + 1) {
                    aircraftTarget[lineCounter - 1] = Integer.parseInt(splitLine[0]);
                    aircraftDeadline[lineCounter - 1] = Integer.parseInt(splitLine[1]);
                    aircraftClass[lineCounter - 1] = Integer.parseInt(splitLine[2]);
                } else {
                    int cnt = 0;
                    for (String s : splitLine) {
                        classTransitionCost[lineCounter - nbAircraft - 1][cnt] = Integer.parseInt(s);
                        cnt++;
                    }
                }
                lineCounter++;
            }
        }
    }
}
