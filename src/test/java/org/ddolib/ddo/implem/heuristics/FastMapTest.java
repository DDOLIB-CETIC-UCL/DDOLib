package org.ddolib.ddo.implem.heuristics;

import org.ddolib.ddo.examples.setcover.elementlayer.SetCoverDistance;
import org.ddolib.ddo.examples.setcover.elementlayer.SetCoverState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;

public class FastMapTest {

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

    // TODO add tests not linked to any particular pb

}
