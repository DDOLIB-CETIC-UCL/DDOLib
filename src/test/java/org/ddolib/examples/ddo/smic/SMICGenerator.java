package org.ddolib.examples.ddo.smic;

import java.util.Arrays;
import java.util.Random;

/*public class SMICGenerator {
    public  String name;
    int nbJob;
    int capaInventory;
    int initInventory;
    int minProcessing;
    int maxHorizon;
    private int[] type;
    private int[] processing;
    private int[] weight;
    private int[] release;
    private int[] inventory;
    public SMICProblem SMICGenerator(int nbJob, int initInventory, int capaInventory, int minProcessing, int maxHorizon) {
        this.nbJob = nbJob;
        this.capaInventory = capaInventory;
        this.initInventory = initInventory;
        this.minProcessing = minProcessing;
        this.maxHorizon = maxHorizon;
        this.type = new int[nbJob];
        this.processing = new int[nbJob];
        this.weight = new int[nbJob];
        this.release = new int[nbJob];
        this.inventory = new int[nbJob];
        int currentRelease = 0;
        int inv = initInventory;
        int sumProcessing = 0;
        Random rand = new Random();
        for (int i = 0; i < nbJob; i++) {
            processing[i] = minProcessing + rand.nextInt(nbJob);
            weight[i] = 0;
            release[i] = rand.nextInt(maxHorizon-processing[i]);
            sumProcessing += processing[i];
            int currentInv = 1 + rand.nextInt(capaInventory);
            boolean makeProducer;
            if (currentInv + inv <= capaInventory) {
                makeProducer = true;
                type[i] = 1;
            } else if (inv == capaInventory) {
                makeProducer = false;
            } else {
                makeProducer = rand.nextBoolean();
            }

            if (makeProducer) {
                type[i] = 1; // producteur
                int prod = 1 + rand.nextInt(Math.max(1, capaInventory - inv));
                inventory[i] = prod;
                inv += prod;
            } else {
                type[i] = -1; // consommateur
                int cons = 1 + rand.nextInt(inv);
                inventory[i] = cons;
                inv -= cons;
            }
        }

        // équilibrage final : si inventaire > 0, ajouter un consommateur
        if (inv > 0) {
            // redimensionner les tableaux
            int[] type2 = Arrays.copyOf(type, nbJob + 1);
            int[] processing2 = Arrays.copyOf(processing, nbJob + 1);
            int[] weight2 = Arrays.copyOf(weight, nbJob + 1);
            int[] release2 = Arrays.copyOf(release, nbJob + 1);
            int[] inventory2 = Arrays.copyOf(inventory, nbJob + 1);

            type2[nbJob] = -1;
            processing2[nbJob] = 1;
            weight2[nbJob] = 1;
            release2[nbJob] = currentRelease;
            inventory2[nbJob] = inv;

            nbJob += 1;
            inv = 0;

            return new SMICProblem(name, nbJob, initInventory, capaInventory,
                    type2, processing2, weight2, release2, inventory2);
        }
        return new SMICProblem(name, nbJob, initInventory, capaInventory, type, processing, weight, release, inventory);
    }

    public static void main(String[] args) {
        SMICGenerator data = new SMICGenerator();
    }
}*/
