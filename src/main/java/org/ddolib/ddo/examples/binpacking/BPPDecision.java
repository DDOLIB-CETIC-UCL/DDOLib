package org.ddolib.ddo.examples.binpacking;

import java.util.Objects; /**
 * Decision of affecting an item to a defined bin.
 */
public class BPPDecision {
    public int item;
    public int bin;

    public BPPDecision(int item, int bin) {
        this.item = item;
        this.bin = bin;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BPPDecision that = (BPPDecision) o;
        return item == that.item && bin == that.bin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, bin);
    }
}
