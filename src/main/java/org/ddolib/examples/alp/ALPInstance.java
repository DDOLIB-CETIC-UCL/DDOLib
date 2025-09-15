package org.ddolib.examples.alp;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public Optional<Double> optimal;

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
                        optimal = Optional.of(Double.parseDouble(splitLine[3]));
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

    /**
     * Generates an ALP instance following the given parameters.
     * The instance is generated such that the optimal solution is always 0 (meaning 0 delay).
     * <p>
     *
     * <ul> Steps :
     *     <li> We select the class of the aircraft and the runway where it will land.</li>
     *     <li> Based on the runway we get the transition cost and the last landing time. </li>
     *     <li> Finally we add a small gap for the target time and a bigger one for the deadline. </li>
     * </ul>
     *
     * @param outputFile The output file where the instance has to be written.
     * @param nbAircraft The number aircraft.
     * @param nbClasses  The number classes.
     * @param nbRunways  The number runways.
     */
    public static void writeNoDelayInstance(String outputFile, int nbAircraft, int nbClasses, int nbRunways) throws IOException {
        assert nbAircraft > 0 && nbClasses > 0 && nbRunways > 0 : "Input parameters have to be positive.";
        assert outputFile != null : "Output file is null.";
        assert nbAircraft >= nbClasses : "Number of aircraft must be greater than number of classes.";
        assert nbAircraft >= nbRunways : "Number of aircraft must be greater than number of runways.";

        int[][] classTransitionCost = new int[nbClasses][nbClasses];
        int[] lastLandingTime = new int[nbRunways];
        int[] lastLandingClass = new int[nbRunways];
        Arrays.fill(lastLandingClass, -1);

        for (int c1 = 0; c1 < nbClasses; c1++) {
            for (int c2 = 0; c2 < nbClasses; c2++) {
                if (c1 != c2) classTransitionCost[c1][c2] = (int) (Math.random() * 10) + 15;
                else classTransitionCost[c1][c2] = (int) (Math.random() * 10);
            }
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            bw.write(String.format("%d %d %d 0\n", nbAircraft, nbClasses, nbRunways));

            for (int a = 0; a < nbAircraft; a++) {
                int aClass = (int) (Math.random() * nbClasses);
                int aRunway = (int) (Math.random() * nbRunways);
                // Interval between target and deadline
                int landingInterval = (int) (Math.random() * 200) + 50;
                // Small gap before target
                int smallGap = (int) (Math.random() * 20);

                int lastLandingTimeAtRunway = lastLandingTime[aRunway];
                int lastClassAtRunway = lastLandingClass[aRunway];
                // if lastClassAtRunway == -1 ==> no aircraft landed on that runway yet.
                int transitionCost = lastClassAtRunway == -1 ? 0 : classTransitionCost[lastClassAtRunway][aClass];
                int target = lastLandingTimeAtRunway + transitionCost + smallGap;
                int deadline = target + landingInterval;
                lastLandingTime[aRunway] = target;
                lastLandingClass[aRunway] = aClass;
                bw.write(String.format("%d %d %d\n", target, deadline, aClass));
            }
            for (int c1 = 0; c1 < nbClasses; c1++) {
                bw.write(IntStream.of(classTransitionCost[c1]).mapToObj(Integer::toString).collect(Collectors.joining(" ")) + "\n");
            }
        }

    }
}
