package org.ddolib.examples.ddo.smic;

import java.util.Random;

/*public class TestDominance {
    public static void main(String[] args) {
        SMICProblem data = generate(3, 3, 10,1,5);
        System.out.println(data);

    }
    private static SMICProblem generate(int producers, int consumers, int capacity, int minP, int maxP) {
        Random rand = new Random();
        int inv = 0;
        int currentRelease = 0;
        int n = producers + consumers;
        int[] type = new int[n];
        int[] processing = new int[n];
        int[] weight = new int[n];
        int[] release = new int[n];
        int[] inventory = new int[n];

        // Générer les producteurs en premier
        for (int i=0; i<producers; i++) {
            int p = rand.nextInt(maxP - minP + 1) + minP;
            // production limitée pour ne pas dépasser capacity
            int prod = 1 + rand.nextInt(Math.max(1, capacity - inv));
            inv += prod;
            currentRelease += p; // pour éviter idle
            type[i] = 1;
            processing[i] = p;
            weight[i] = 0;
            release[i] = currentRelease;
            inventory[i] = prod;
        }

        // Générer les consommateurs
        for (int i=producers; i<consumers+producers; i++) {
            int p = rand.nextInt(maxP - minP + 1) + minP;
            // consommation ≤ inventaire disponible
            int cons = 1 + rand.nextInt(inv);
            inv -= cons;
            currentRelease += p;
            type[i] = 0;
            processing[i] = p;
            weight[i] = 0;
            release[i] = currentRelease;
            inventory[i] = cons;
        }

        return new SMICProblem("SMIC", n, 0, capacity, type, processing, weight, release, inventory);
    }
}*/
