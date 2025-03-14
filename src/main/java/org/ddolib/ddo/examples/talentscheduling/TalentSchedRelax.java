package org.ddolib.ddo.examples.talentscheduling;

import org.ddolib.ddo.core.Decision;
import org.ddolib.ddo.core.Relaxation;

import java.util.Iterator;

public class TalentSchedRelax implements Relaxation<TalentSchedState> {
    @Override
    public TalentSchedState mergeStates(Iterator<TalentSchedState> states) {
        return null;
    }

    @Override
    public int relaxEdge(TalentSchedState from, TalentSchedState to, TalentSchedState merged, Decision d, int cost) {
        return 0;
    }
}
