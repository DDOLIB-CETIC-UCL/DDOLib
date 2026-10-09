package org.ddolib.examples.layered.smic;

import java.util.Optional;
import java.util.Random;

/**
 * The {@code SMICGenrator} class is responsible for generating random instances of the Single
 * Machine with Inventory Constraint (SMIC) problem.
 *
 * <p>Each generated instance defines a set of production jobs characterized by: processing times,
 * release dates, inventory changes (positive or negative), and an initial inventory level that
 * satisfies the given capacity constraints.
 *
 * <p>The generator takes several tunable parameters that control the instance structure and
 * difficulty:
 *
 * <ul>
 *   <li>{@code n} – number of jobs,
 *   <li>{@code alpha} – upper bound on job processing times,
 *   <li>{@code tau} – scaling factor for release dates,
 *   <li>{@code eta} – inventory scaling factor used to derive capacity limits,
 *   <li>{@code seed} – random seed for reproducibility.
 * </ul>
 *
 * <p>The generator randomly samples job attributes and ensures feasibility by checking that the
 * resulting initial inventory and signed inventory variations do not violate capacity constraints.
 * If a valid instance cannot be constructed after {@value #MAX_ASSIGN_ATTEMPTS} trials, an
 * exception is thrown.
 *
 * <h2>Generation Process</h2>
 *
 * <ol>
 *   <li>Sample job processing times uniformly from {@code [1, α]};
 *   <li>Sample absolute inventory changes |Δᵢ| uniformly from {@code [1, 10]};
 *   <li>Randomly assign each job as a production (+1) or consumption (0) job;
 *   <li>Determine a feasible initial inventory {@code I₀} within capacity bounds;
 *   <li>Generate release dates uniformly from {@code [0, τ × pᵢ]};
 *   <li>Construct a {@link SMICProblem} instance encapsulating the generated data.
 * </ol>
 *
 * <h2>Example</h2>
 *
 * <pre>{@code
 * SMICGenrator generator = new SMICGenrator(10, 5, 0.5, 2, 12345L);
 * SMICProblem instance = generator.generate();
 * System.out.println(instance);
 * }</pre>
 *
 * @see SMICProblem
 * @see java.util.Random
 */
public class SMICGenrator {
    /** Maximum number of random assignments before aborting. */
    private static final int MAX_ASSIGN_ATTEMPTS = 10000;

    /** Number of jobs to generate. */
    private final int nbJobs;

    /** Upper bound on processing times. */
    private final int alpha;

    /** Scaling factor for release times. */
    private final double tau;

    /** Scaling parameter for inventory capacity. */
    private final int eta;

    /** Random seed for reproducibility. */
    private final long seed;

    /** Initial inventory level. */
    private int initInventory;

    /** Maximum inventory capacity. */
    private int capaInventory;

    /** Job processing times. */
    private int[] processing;

    /** Job release times. */
    private int[] release;

    /** Absolute inventory changes per job. */
    private int[] inventory;

    /** Job types: 1 for production, 0 for consumption. */
    private int[] type;

    /** Job weights (currently unused in generation). */
    private int[] weight;

    /**
     * Constructs a new SMIC instance generator with the given parameters.
     *
     * @param n the number of jobs to generate
     * @param alpha the upper bound for processing times
     * @param tau the release time scaling factor
     * @param eta the scaling factor for inventory capacity
     * @param seed the random seed for reproducibility
     */
    public SMICGenrator(int n, int alpha, double tau, int eta, long seed) {
        this.nbJobs = n;
        this.alpha = alpha;
        this.tau = tau;
        this.eta = eta;
        this.seed = seed;
    }

    /**
     * Generates a random feasible {@link SMICProblem} instance based on the parameters provided at
     * construction.
     *
     * <p>The generator repeatedly samples job attributes and type assignments until a feasible
     * configuration (respecting inventory capacity constraints) is found or until {@value
     * #MAX_ASSIGN_ATTEMPTS} iterations are exceeded.
     *
     * @return a feasible {@link SMICProblem} instance
     * @throws RuntimeException if no feasible configuration can be generated after {@value
     *     #MAX_ASSIGN_ATTEMPTS} attempts
     */
    public SMICProblem generate() {
        final Random rand = new Random(seed);
        processing = new int[nbJobs];
        release = new int[nbJobs];
        type = new int[nbJobs];
        weight = new int[nbJobs];
        inventory = new int[nbJobs];
        // step 1: sample processing[i]
        int sumProcessing = 0;
        for (int i = 0; i < nbJobs; i++) {
            processing[i] = 1 + rand.nextInt(alpha); // uniform in [1, alpha]
            sumProcessing += processing[i];
        }

        // step 2: sample |delta_i|
        for (int i = 0; i < nbJobs; i++) {
            inventory[i] = 1 + rand.nextInt(10); // uniform [1,10]
        }

        // compute the bound of the capacity of onventory
        int minInventoryCapacitory = 10 * eta;
        int maxInventoryCapacitory = 20 * eta;
        capaInventory =
                minInventoryCapacitory
                        + rand.nextInt(maxInventoryCapacitory - minInventoryCapacitory + 1);
        boolean feasible = false;
        for (int attempt = 0; attempt < MAX_ASSIGN_ATTEMPTS && !feasible; attempt++) {

            // assign signs randomly 50/50
            boolean[] inJplus = new boolean[nbJobs];
            for (int i = 0; i < nbJobs; i++) {
                inJplus[i] = rand.nextBoolean();
            }

            // build signed delta according to assignment
            for (int i = 0; i < nbJobs; i++) {
                type[i] = inJplus[i] ? 1 : 0;
            }

            // check job-level constraints (no job exceeds IC in its signed delta sense)
            boolean violates = false;
            for (int i = 0; i < nbJobs; i++) {
                if (inJplus[i] && inventory[i] > capaInventory) {
                    violates = true;
                    break;
                }
                if (!inJplus[i] && inventory[i] < -capaInventory) {
                    violates = true;
                    break;
                }
            }
            if (violates) {
                continue; // reassign
            }

            // sum over J+ and J-
            int sumPos = 0;
            int sumNeg = 0;
            for (int i = 0; i < nbJobs; i++) {
                if (inJplus[i]) {
                    sumPos += inventory[i];
                } else {
                    sumNeg -= inventory[i];
                } // sumNeg <= 0
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
            for (int i = 0; i < nbJobs; i++) {
                int maxRi = (int) Math.floor(tau * processing[i]);
                if (maxRi < 0) {
                    maxRi = 0;
                }
                release[i] = maxRi == 0 ? 0 : rand.nextInt(maxRi + 1);
            }

            feasible = true;
        }
        if (!feasible) {
            // as fallback, try a weaker strategy: regenerate absDelta until feasible (rare)
            // but for safety, throw an exception to signal user to retry with different params
            throw new RuntimeException(
                    "Failed to generate feasible assignment after "
                            + MAX_ASSIGN_ATTEMPTS
                            + " attempts for seed "
                            + seed
                            + ". Try re-running with different random seed.");
        }

        return new SMICProblem(
                "smic_" + nbJobs + "_" + initInventory + "_" + capaInventory,
                nbJobs,
                initInventory,
                capaInventory,
                type,
                processing,
                weight,
                release,
                inventory,
                Optional.empty());
    }
}
