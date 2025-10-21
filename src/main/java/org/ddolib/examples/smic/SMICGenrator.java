package org.ddolib.examples.smic;

import java.util.Random;

public class SMICGenrator {
    private int n;
    private int  alpha;
    private  double tau;
    private int eta;
    private long seed;
    private int initInventory;
    private int capaInventory;
    private int[] processing;
    private int[] release;
    private int[]  inventory; // absolu value of inventory
    private int[] type; // +1 or 0
    private int[] weight;
    private static final int MAX_ASSIGN_ATTEMPTS = 10000;
    public SMICGenrator(int n, int alpha, double tau, int eta, long seed) {
        this.n = n;
        this.alpha = alpha;
        this.tau = tau;
        this.eta = eta;
        this.seed = seed;

    }
    public SMICProblem generate() {
        Random rand = new Random(seed);
        processing = new int[n];
        release = new int[n];
        type = new int[n];
        weight = new int[n];
        inventory = new int[n];
        // step 1: sample processing[i]
        int sumProcessing = 0;
        for (int i = 0; i < n; i++) {
            processing[i] = 1 + rand.nextInt(alpha); // uniform in [1, alpha]
            sumProcessing += processing[i];
        }

        // step 2: sample |delta_i|
        for (int i = 0; i < n; i++) {
            inventory[i] = 1 + rand.nextInt(10); // uniform [1,10]
        }

        // compute the bound of the capacity of onventory
        int minInventoryCapacitory = 10 * eta;
        int maxInventoryCapacitory = 20 * eta;
        capaInventory = minInventoryCapacitory + rand.nextInt(maxInventoryCapacitory - minInventoryCapacitory + 1);
        boolean feasible = false;
        for (int attempt = 0; attempt < MAX_ASSIGN_ATTEMPTS && !feasible; attempt++) {

            // assign signs randomly 50/50
            boolean[] inJplus = new boolean[n];
            for (int i = 0; i < n; i++) {
                inJplus[i] = rand.nextBoolean();
            }

            // build signed delta according to assignment
            for (int i = 0; i < n; i++) {
                type[i] = inJplus[i] ? 1 : 0;
            }

            // check job-level constraints (no job exceeds IC in its signed delta sense)
            boolean violates = false;
            for (int i = 0; i < n; i++) {
                if (inJplus[i] && inventory[i] > capaInventory) {
                    violates = true; break;
                }
                if (!inJplus[i] && inventory[i] < -capaInventory) {
                    violates = true; break;
                }
            }
            if (violates) {
                continue; // reassign
            }

            // sum over J+ and J-
            int sumPos = 0;
            int sumNeg = 0;
            for (int i = 0; i < n; i++) {
                if (inJplus[i]) sumPos += inventory[i];
                else sumNeg -= inventory[i]; // sumNeg <= 0
            }

            // compute capacity of the inventory:
            int lower = Math.min(capaInventory, Math.max(0, sumNeg));
            int upper = Math.max(0, Math.min(capaInventory, capaInventory - sumPos));

            if (lower > upper) {
                // this assignment can't yield a valid I0; retry
                continue;
            }

            // draw I0 uniformly in [lower, upper]
            initInventory = lower + rand.nextInt(upper - lower + 1);

            // sample r_i with ri in [0, floor(tau * p_i)]
            for (int i = 0; i < n; i++) {
                int maxRi = (int) Math.floor(tau * processing[i]);
                if (maxRi < 0) maxRi = 0;
                release[i] = maxRi == 0 ? 0 : rand.nextInt(maxRi + 1);
            }

            feasible = true;
        }
        if (!feasible) {
            // as fallback, try a weaker strategy: regenerate absDelta until feasible (rare)
            // but for safety, throw an exception to signal user to retry with different params
            throw new RuntimeException("Failed to generate feasible assignment after " + MAX_ASSIGN_ATTEMPTS +
                    " attempts for seed " + seed + ". Try re-running with different random seed.");
        }

        return new SMICProblem("smic_"+n+"_"+initInventory+"_"+capaInventory,n, initInventory, capaInventory, type, processing, weight, release, inventory);
    }
}
