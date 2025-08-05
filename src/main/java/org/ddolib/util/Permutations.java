package org.ddolib.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Collections of methods that can apply function on permutations
 */
public class Permutations {

    /**
     * Generate all permutations and apply the given action to each of them.
     *
     * @param startInclusive The smallest number present in the permutation.
     * @param endExclusive   The biggest number
     * @param action         The action to apply to each generated permutation.
     */
    public static void generateAllPermutations(int startInclusive, int endExclusive, Consumer<List<Integer>> action) {
        List<Integer> initialNumbers = IntStream.range(startInclusive, endExclusive)
                .boxed().collect(Collectors.toCollection(ArrayList::new));

        generateAllPermutations(0, initialNumbers, action);
    }

    /**
     * Internal method to generate permutations.
     *
     * @param index   The fixe index that will be swapped with all other indices.
     * @param numbers The numbers currently present in the permutations
     * @param action  The action to apply to each generated permutation.
     */
    private static void generateAllPermutations(int index, List<Integer> numbers, Consumer<List<Integer>> action) {
        if (index > numbers.size() - 1) {
            action.accept(numbers);
        } else {
            for (int i = index; i < numbers.size(); i++) {
                Collections.swap(numbers, index, i);
                generateAllPermutations(index + 1, numbers, action);
                Collections.swap(numbers, index, i);
            }
        }
    }

    /**
     * Generate permutations without symmetry and apply the given action to each of them.
     *
     * @param startInclusive The smallest number present in the permutation.
     * @param endExclusive   The biggest number
     * @param action         The action to apply to each generated permutation.
     */
    public static void generateUniquePermutations(int startInclusive, int endExclusive, Consumer<List<Integer>> action) {
        List<Integer> allNumbers = IntStream.range(startInclusive, endExclusive)
                .boxed()
                .toList();
        int n = allNumbers.size();

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {

                int first = allNumbers.get(i);
                int last = allNumbers.get(j);

                List<Integer> remainingNumbers = new ArrayList<>();
                for (int k = 0; k < n; k++) {
                    if (k != i && k != j) {
                        remainingNumbers.add(allNumbers.get(k));
                    }
                }

                generateAllPermutations(0, remainingNumbers, (middlePermutation) -> {
                    List<Integer> finalPermutation = new ArrayList<>(n);
                    finalPermutation.add(first);
                    finalPermutation.addAll(middlePermutation);
                    finalPermutation.add(last);
                    action.accept(finalPermutation);
                });
            }
        }
    }


}
