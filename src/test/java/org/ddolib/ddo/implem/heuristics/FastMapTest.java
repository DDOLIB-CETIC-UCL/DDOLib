package org.ddolib.ddo.implem.heuristics;

import org.ddolib.ddo.examples.setcover.elementlayer.SetCoverDistance;
import org.ddolib.ddo.examples.setcover.elementlayer.SetCoverState;
import org.ddolib.ddo.heuristics.StateDistance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class FastMapTest {

    // TODO add tests not linked to any particular pb
    @Test
    public void smallTest1d() {
        SetCoverState a = new SetCoverState(Set.of(0, 1));
        SetCoverState b = new SetCoverState(Set.of(2, 3));
        SetCoverState c = new SetCoverState(Set.of(0));

        FastMap<SetCoverState> map = new FastMap<>(List.of(a,b,c), 1, new SetCoverDistance());
        // We can assert that the two pivot will be a and b as they are the farthest points
        // but we cannot know which one of them will be the point "0"
        Assertions.assertTrue(0.0 ==  map.getCoordinates(a)[0] || 4.0 ==  map.getCoordinates(a)[0]);
        Assertions.assertTrue(4.0 == map.getCoordinates(b)[0] || 0.0 ==  map.getCoordinates(b)[0]);
        Assertions.assertTrue(1.0 ==  map.getCoordinates(c)[0] || 3.0 == map.getCoordinates(c)[0]);
    }

    @Test
    public void testAllPointSuperposed() {
        SetCoverState a = new SetCoverState(Set.of(0, 1));
        SetCoverState b = new SetCoverState(Set.of(0, 1));
        SetCoverState c = new SetCoverState(Set.of(0, 1));

        FastMap<SetCoverState> map = new FastMap<>(List.of(a,b,c), 1, new SetCoverDistance());
        Assertions.assertEquals(0.0,  map.getCoordinates(a)[0]);
        Assertions.assertEquals(0.0,  map.getCoordinates(b)[0]);
        Assertions.assertEquals(0.0,  map.getCoordinates(c)[0]);
    }

    private static class TestDistance implements StateDistance<String> {
        Map<Set<String>, Double> distanceMap;

        public TestDistance() {
            distanceMap = new HashMap<>();
            distanceMap.put(Set.of("A", "B"), 10.0);
            distanceMap.put(Set.of("C", "D"), 8.0);
            distanceMap.put(Set.of("A", "C"), 6.4031242374328485);
            distanceMap.put(Set.of("A", "D"), 6.4031242374328485);
            distanceMap.put(Set.of("B", "C"), 6.4031242374328485);
            distanceMap.put(Set.of("B", "D"), 6.4031242374328485);

        }

        @Override
        public double distance(String a, String b) {
            if (Objects.equals(a, b)) return 0.0;
            return distanceMap.get(Set.of(a, b));
        }
    }

    @Test
    public void test2D() {
        FastMap<String> map = new FastMap<>(
                List.of("A", "B", "C", "D"),
                2,
                new TestDistance()
        );
        // Assertions on first dimension
        // A and B should be selected as the first pivots
        Assertions.assertTrue(map.getCoordinates("A")[0] == 0.0 || map.getCoordinates("A")[0] == 10.0);
        Assertions.assertTrue(map.getCoordinates("B")[0] == 0.0 || map.getCoordinates("B")[0] == 10.0);
        Assertions.assertTrue(map.getCoordinates("C")[0] == 5.0);
        Assertions.assertTrue(map.getCoordinates("D")[0] == 5.0);

        // Assertions on second dimension
        // C and D should be selected as the seconds pivots
        Assertions.assertTrue(map.getCoordinates("A")[1] == 4.0);
        Assertions.assertTrue(map.getCoordinates("B")[1] == 4.0);
        Assertions.assertTrue(map.getCoordinates("C")[1] == 0.0 || map.getCoordinates("C")[1] == 8.0);
        Assertions.assertTrue(map.getCoordinates("D")[1] == 0.0 || map.getCoordinates("D")[1] == 8.0);
    }

    @Disabled
    @Test
    public void test2DLarge() throws IOException {
        int elementNbr = 190;
        int setNumber = 10000;
        int setSize = 60;
        List<SetCoverState> states = generateSets(elementNbr, setNumber, setSize);

        FastMap<SetCoverState> map = new FastMap<>(states, 2, new SetCoverDistance());
        FileWriter fw = new FileWriter("tmp/FastMapDistribution.txt", false);

        for (SetCoverState state : states) {
            double[] coordinates =  map.getCoordinates(state);
            StringBuilder str = new StringBuilder();
            for (int i = 0; i < coordinates.length; i++) {
                str.append(coordinates[i]).append(" ");
            }
            str.append("\n");
            fw.write(str.toString());
        }
        fw.close();
    }

    private List<SetCoverState> generateSets(int elementNbr, int setNumber, int setSize) {
            List<SetCoverState> states = new ArrayList<>(setNumber);
            List<Integer> range = new ArrayList<>(elementNbr);
            for (int i = 0; i < elementNbr; i++) {
                range.add(i);
            }

            for (int i = 0; i < setNumber; i++) {
                Collections.shuffle(range);
                Set<Integer> set = new HashSet<>(range.subList(0, setSize));
                states.add(new SetCoverState(set));
            }

            return states;
    }

}
