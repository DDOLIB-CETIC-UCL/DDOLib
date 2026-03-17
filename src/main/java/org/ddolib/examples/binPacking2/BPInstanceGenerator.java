package org.ddolib.examples.binPacking2;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.*;

public class BPInstanceGenerator {

    public static void main(String[] args) throws IOException {
        int[][] params = {{10, 5}/*,{10, 10}, {20, 10}, {10, 15}, {20, 15}*/};
        //String dir = "data/BPP/test";
        String dir = "src/test/resources/BinPacking";
        for (int[] p : params) {
            int capa = p[0], nbBins = p[1];
            for (int i = 0; i < 10; i++) {
                String path = dir + "/bp_" + capa + "_" + nbBins + "_" + i + ".txt";
                randomInstance(capa, nbBins, i).save(path);
            }
        }
    }

    record BPInstance(List<Integer> items, int capa, int nbBins) {
        public void save(String path) {
            // format:
            // nbItems nbBins
            // capa
            // weightItem1
            // weightItem2
            // ...
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
     * Generate a random instance of the bin packing problem
     * This instance has the specificity to be feasible and
     * require a complete fill of all the bins
     * @param capa
     * @param nbBins
     * @return BPInstance
     * @throws IOException
     */
    public static BPInstance randomInstance(int capa, int nbBins, int seed) throws IOException {
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

        int nbItems = itemWeightsList.size();
        // Sort heaviest-first, consistent with readInstance
        itemWeightsList.sort(Comparator.reverseOrder());
        int[] itemWeights = itemWeightsList.stream().mapToInt(i -> i).toArray();

        Optional<Double> optimal = Optional.of((double) nbBins);
        return new BPInstance(itemWeightsList, capa, nbBins);
    }
}
