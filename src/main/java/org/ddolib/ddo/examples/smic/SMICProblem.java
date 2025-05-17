package org.ddolib.ddo.examples.smic;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Problem;
import java.util.*;

public class SMICProblem  implements Problem<SMICState> {
    final String name;
    final int nbJob;
    final int initInventory;
    final int capaInventory;
    final int[] type;
    final int[] processing;
    final int[] weight;
    final int[] release;
    final int[] inventory;

    public SMICProblem(String name, int nbJob, int initInventory, int capaInventory, int[] type, int[] processing, int[] weight, int[] release, int[] inventory) {
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
    public int nbVars() {
        return nbJob;
    }

    @Override
    public SMICState initialState() {
        Set<Integer> jobs = new HashSet<>();
        for (int i = 0; i < nbVars(); i++) {
            jobs.add(i);
        }
        return new SMICState(jobs,0, initInventory);
    }

    @Override
    public int initialValue() {
        return 0;
    }

    @Override
    public Iterator<Integer> domain(SMICState state, int var) {
        ArrayList<Integer> domain = new ArrayList<>();
        for (Integer job : state.getRemainingJobs()) {
            int currentInventory = (type[job] <= 0) ? (state.getCurrentInventory() - inventory[job]) : (state.getCurrentInventory() + inventory[job]);
            if (currentInventory >= 0 && currentInventory <= capaInventory) {
                domain.add(job);
            }
        }
        return domain.iterator();
    }

    @Override
    public SMICState transition(SMICState state, Decision decision) {
        Set<Integer> remaining = new HashSet<>(state.getRemainingJobs());
        if (decision.val() == 1) {
            remaining.remove(decision.val());
            int currentTime = Math.max(state.getCurrentTime(), release[decision.val()]) + processing[decision.val()];
            return new SMICState(remaining, currentTime, state.getCurrentInventory()-inventory[decision.val()]);
        }
        else {
            return state;
        }
    }

    @Override
    public int transitionCost(SMICState state, Decision decision) {
        int currentTime = Math.max(state.getCurrentTime(), release[decision.val()]) + processing[decision.val()];
        return -currentTime;
    }
}
