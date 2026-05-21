package org.ddolib.examples.binpacking;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.*;

public class BPInstanceGenerator {

    /**
     * Generates instances of Bin Packing Problem based on passed arguments.
     *
     * <p>
     * The problems are generated building fully filled bins. Therefore, we know the optimal solution.
     * </p>
     *
     * @param args Integers ==> `capa` `nbBins` `nbProblems`
     */
    public static void main(String[] args) throws IOException {
        int capa = Integer.parseInt(args[0]);
        int nbBins = Integer.parseInt(args[1]);
        int nbProblems = Integer.parseInt(args[2]);
        String dir = "src/test/resources/BinPacking";
        for (int i = 0; i < nbProblems; i++) {
            String path = dir + "/bpx_" + capa + "_" + nbBins + "_" + i + ".txt";
            randomInstance(capa, nbBins, i).save(path);
        }
    }

    record BPInstance(List<Integer> items, int capa, int nbBins) {
        public void save(String path) {
            File file = new File(path);
            if (file.getParentFile() != null) file.getParentFile().mkdirs();
            try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
                pw.println(items.size() + " " + nbBins);
                pw.println(capa);
                for (int w : items) {
                    pw.println(w);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to save instance to " + path, e);
            }
        }
    }

    /**
     * Generates a random instance of the bin packing problem
     * This instance has the specificity to be feasible and
     * requires a complete fill of all the bins.
     *
     * @param capa   The capacity of each bin.
     * @param nbBins The number of bins to fill.
     * @return A BPInstance
     */
    private static BPInstance randomInstance(int capa, int nbBins, int seed) throws IOException {
        Random rand = new Random(seed);
        List<Integer> itemWeightsList = new ArrayList<>();

        for (int bin = 0; bin < nbBins; bin++) {
            int remaining = capa;
            // First item is biased to be >= capa/2
            int minFirst = capa / 2;
            int w = minFirst + rand.nextInt(remaining - minFirst + 1);
            itemWeightsList.add(w);
            remaining -= w;
            // Remaining items are purely random
            while (remaining > 0) {
                w = rand.nextInt(remaining) + 1;
                itemWeightsList.add(w);
                remaining -= w;
            }
        }

        // Sort heaviest-first, consistent with readInstance
        itemWeightsList.sort(Comparator.reverseOrder());

        return new BPInstance(itemWeightsList, capa, nbBins);
    }
}
