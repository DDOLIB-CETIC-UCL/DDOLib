package org.ddolib.examples.ddo.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.modeling.Problem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SMICProblem implements Problem<SMICState> {
    public final String name;
    final int nbJob;
    final int initInventory;
    final int capaInventory;
    final int[] type;
    final int[] processing;
    final int[] weight;
    final int[] release;
    final int[] inventory;

    private Optional<Double> optimal = Optional.empty();

    public SMICProblem(String name,
                       int nbJob,
                       int initInventory,
                       int capaInventory,
                       int[] type,
                       int[] processing,
                       int[] weight,
                       int[] release,
                       int[] inventory,
                       double optimal) {
        this.name = name;
        this.nbJob = nbJob;
        this.initInventory = initInventory;
        this.capaInventory = capaInventory;
        this.type = type;
        this.processing = processing;
        this.weight = weight;
        this.release = release;
        this.inventory = inventory;
        this.optimal = Optional.of(optimal);
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
        for (Integer job : state.getRemainingJobs()) {
            int minCurrentInventory = (type[job] == 0) ? (state.getMinCurrentInventory() - inventory[job]) : (state.getMinCurrentInventory() + inventory[job]);
            int maxCurrentInventory = (type[job] == 0) ? (state.getMaxCurrentInventory() - inventory[job]) : (state.getMaxCurrentInventory() + inventory[job]);
            if (minCurrentInventory >= 0 && maxCurrentInventory <= capaInventory) {
                domain.add(job);
            }
        }
        return domain.iterator();
    }

    @Override
    public SMICState transition(SMICState state, Decision decision) {
        Set<Integer> remaining = new HashSet<>(state.getRemainingJobs());
        remaining.remove(decision.val());
        int currentTime = Math.max(state.getCurrentTime(), release[decision.val()]) + processing[decision.val()];
        int minCurrentInventory = (type[decision.val()] == 0) ? (state.getMinCurrentInventory() - inventory[decision.val()]) : (state.getMinCurrentInventory() + inventory[decision.val()]);
        int maxCurrentInventory = (type[decision.val()] == 0) ? (state.getMaxCurrentInventory() - inventory[decision.val()]) : (state.getMaxCurrentInventory() + inventory[decision.val()]);
        return new SMICState(remaining, currentTime, minCurrentInventory, maxCurrentInventory);
    }

    @Override
    public double transitionCost(SMICState state, Decision decision) {
        int currentTime = Math.max(release[decision.val()] - state.getCurrentTime(), 0) + processing[decision.val()];
        return -currentTime;
    }
}
