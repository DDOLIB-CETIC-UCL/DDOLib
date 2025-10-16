package org.ddolib.examples.msct;


import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MSCTProblem implements Problem<MSCTState> {

    final int[] release; // release date of each job
    final int[] processing; // processing time of each job
    final Optional<Double> optimal;

    public MSCTProblem(final int[] release, final int[] processing, Optional<Double> optimal) {
        this.release = release;
        this.processing = processing;
        this.optimal = optimal;
    }
    public MSCTProblem(final int[] release, final int[] processing) {
        this.release = release;
        this.processing = processing;
        this.optimal = Optional.empty();
    }

    public MSCTProblem(final String file) throws IOException {
        boolean isFirst = true;
        String line;
        int count = 0;
        int nVar = 0;
        int[] releas = new int[0];
        int[] proces = new int[0];
        Optional<Double> optimal = Optional.empty();
        try (final BufferedReader bf = new BufferedReader(new FileReader(file))) {
            while ((line = bf.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    String[] tokens = line.split("\\s+");
                    nVar = Integer.parseInt(tokens[0]);
                    if (tokens.length == 2) {
                        optimal = Optional.of(Double.parseDouble(tokens[1]));
                    }
                    releas = new int[nVar];
                    proces = new int[nVar];
                } else {
                    if (count < nVar) {
                        String[] tokens = line.split("\\s+");
                        releas[count] = Integer.parseInt(tokens[0]);
                        proces[count] = Integer.parseInt(tokens[1]);
                        count++;
                    }
                }
            }
        }
        this.release = releas;
        this.processing = proces;
        this.optimal = optimal;
    }

//    public MSCTProblem(final String filename) throws IOException {
//        int nVar = 0;
//        int[] releas = new int[0];
//        int[] proces = new int[0];
//        final Scanner s = new Scanner(new File(filename)).useDelimiter("\\s+");
//        while (!s.hasNextInt())
//            s.nextLine();
//        nVar = s.nextInt();
//        releas = new int[nVar];
//        proces = new int[nVar];
//        for (int i = 0; i < nVar; i++) {
//            releas[i] = s.nextInt();
//            proces[i] = s.nextInt();
//        }
//        s.close();
//        this.release = releas;
//        this.processing = proces;
//        this.optimal = Optional.empty();
//    }

    public MSCTProblem(final int n) {
        int[] release = new int[n];
        int[] processing = new int[n];
        Random rand = new Random(100);
        for (int i = 0; i < n; i++) {
            release[i] = rand.nextInt(10);
            processing[i] = 1 + rand.nextInt(10);
        }
        this.release = release;
        this.processing = processing;
        this.optimal = Optional.empty();

    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal.map(x -> -x);
    }

    @Override
    public String toString() {
        return String.format("release: %s - processing: %s", Arrays.toString(release), Arrays.toString(processing));
    }

    @Override
    public int nbVars() {
        return release.length;
    }

    @Override
    public MSCTState initialState() {
        Set<Integer> jobs = new HashSet<>();
        for (int i = 0; i < nbVars(); i++) {
            jobs.add(i);
        }
        return new MSCTState(jobs, 0);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(MSCTState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (Integer job : state.remainingJobs()) {
            domain.add(job);
        }
        return domain.iterator();
    }

    @Override
    public MSCTState transition(MSCTState state, Decision decision) {
        Set<Integer> remaining = new HashSet<>(state.remainingJobs());
        remaining.remove(decision.val());
        int currentTime = Math.max(state.currentTime(), release[decision.val()]) + processing[decision.val()];
        return new MSCTState(remaining, currentTime);
    }

    @Override
    public double transitionCost(MSCTState state, Decision decision) {
        return Math.max(state.currentTime(), release[decision.val()]) + processing[decision.val()];
    }
}
