package org.ddolib.examples.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SMICProblem implements Problem<SMICState> {

    final String name;
    final int nbJob;
    final int initInventory;
    final int capaInventory;
    final int[] type;
    final int[] processing;
    final int[] weight;
    final int[] release;
    final int[] inventory;

    private Optional<Double> optimal;

    public SMICProblem(String name,
                       int nbJob,
                       int initInventory,
                       int capaInventory,
                       int[] type,
                       int[] processing,
                       int[] weight,
                       int[] release,
                       int[] inventory,
                       Optional<Double> optimal) {
        this.name = name;
        this.nbJob = nbJob;
        this.initInventory = initInventory;
        this.capaInventory = capaInventory;
        this.type = type;
        this.processing = processing;
        this.weight = weight;
        this.release = release;
        this.inventory = inventory;
        this.optimal = optimal;
    }

    public SMICProblem(String name,
                       int nbJob,
                       int initInventory,
                       int capaInventory,
                       int[] type,
                       int[] processing,
                       int[] weight,
                       int[] release,
                       int[] inventory) {
        this.name = name;
        this.nbJob = nbJob;
        this.initInventory = initInventory;
        this.capaInventory = capaInventory;
        this.type = type;
        this.processing = processing;
        this.weight = weight;
        this.release = release;
        this.inventory = inventory;
        this.optimal = Optional.empty();
    }

    public SMICProblem(String filename) throws IOException {
        Scanner s = new Scanner(new File(filename)).useDelimiter("\\s+");
        int nbJob = 0;
        int initInventory = 0;
        int capaInventory = 0;
        int[] type = new int[0];
        int[] processing = new int[0];
        int[] weight = new int[0];
        int[] release = new int[0];
        int[] inventory = new int[0];
        this.name = filename;
        while (!s.hasNextLine()) {
            s.nextLine();
        }
        nbJob = s.nextInt();
        initInventory = s.nextInt();
        capaInventory = s.nextInt();
        type = new int[nbJob];
        processing = new int[nbJob];
        weight = new int[nbJob];
        release = new int[nbJob];
        inventory = new int[nbJob];
        Optional<Double> opti = Optional.empty();
        for (int i = 0; i < nbJob; i++) {
            type[i] = s.nextInt();
            processing[i] = s.nextInt();
            weight[i] = s.nextInt();
            release[i] = s.nextInt();
            inventory[i] = s.nextInt();
        }
        if (s.hasNextInt()) {
            opti = Optional.of(s.nextDouble());
        }
        s.close();
        this.nbJob = nbJob;
        this.initInventory = initInventory;
        this.capaInventory = capaInventory;
        this.type = type;
        this.processing = processing;
        this.weight = weight;
        this.release = release;
        this.inventory = inventory;
        this.optimal = opti;
    }

    @Override
    public Optional<Double> optimalValue() {
        return optimal;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int nbVars() {
        return nbJob;
    }

    @Override
    public SMICState initialState() {
        Set<Integer> jobs = new HashSet<>();
        for (int i = 0; i < nbVars(); i++) {
            jobs.add(i);
        }
        return new SMICState(jobs, 0, initInventory, initInventory);
    }

    @Override
    public double initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(SMICState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (Integer job : state.remainingJobs()) {
            int deltaInventory = (type[job] == 0) ? - inventory[job] : + inventory[job];
            int minCurrentInventory = state.minCurrentInventory() + deltaInventory;
            int maxCurrentInventory = state.maxCurrentInventory() + deltaInventory;
            if (maxCurrentInventory >= 0 && minCurrentInventory <= capaInventory) {
                domain.add(job);
            }
        }
        return domain.iterator();
    }

    @Override
    public SMICState transition(SMICState state, Decision decision) {
        Set<Integer> remaining = new HashSet<>(state.remainingJobs());
        int job = decision.val();
        remaining.remove(job);
        int currentTime = Math.max(state.currentTime(), release[job]) + processing[job];
        int deltaInventory = (type[job] == 0) ? - inventory[job] : + inventory[job];
        int minCurrentInventory = state.minCurrentInventory() + deltaInventory;
        int maxCurrentInventory = state.maxCurrentInventory() + deltaInventory;
        return new SMICState(remaining, currentTime, minCurrentInventory, maxCurrentInventory);
    }

    @Override
    public double transitionCost(SMICState state, Decision decision) {
        return Math.max(release[decision.val()] - state.currentTime(), 0) + processing[decision.val()];
    }
}
