package org.ddolib.examples.binpacking;

import java.util.*;

public class BPPState {
    int[] binsUsedSpace;
    int binMaxSpace;

    BPPState(int[] binsUsedSpace, int binMaxSpace) {
        this.binsUsedSpace = binsUsedSpace;
        this.binMaxSpace = binMaxSpace;
    }

    public int usedBins() {
        return binsUsedSpace.length;
    }

    public BPPState packItem(int itemWeight, int bin) {
        int binNb = usedBins();
        int newBinNb = bin == binNb ? binNb + 1 : binNb;
        int[] newBinsUsedSpace = new int[newBinNb];
        System.arraycopy(binsUsedSpace, 0, newBinsUsedSpace, 0, binNb);
        if (newBinNb > binNb) {
            newBinsUsedSpace[bin] = itemWeight;
        } else {
            newBinsUsedSpace[bin] = newBinsUsedSpace[bin] + itemWeight;
        }
        return new BPPState(newBinsUsedSpace, binMaxSpace);
    }

    public Iterator<Integer> findFittingBins(int weight) {
        if(usedBins() == 0) { return List.of(0).iterator();}

        HashSet<Integer> openFittingBins = new HashSet<>();
        for(int i = 0; i < usedBins() + 1; i++) {
            if(i != usedBins() && binsUsedSpace[i] + weight == binMaxSpace) { return List.of(i).iterator();}
            else if(i == usedBins() || binsUsedSpace[i] + weight < binMaxSpace) { openFittingBins.add(i);};
        }
        return openFittingBins.iterator();
    }

    @Override
    public String toString() {
        String binsUsage = String.join(" - ", Arrays.stream(binsUsedSpace).
                mapToObj(us -> String.format("%d/%d", us, binMaxSpace)).toList());
        return String.format("\n\tBins : %d\n\t%s\n", binsUsedSpace.length, binsUsage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(binsUsedSpace));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() == this.getClass()) {
            BPPState other = (BPPState) obj;
            return Arrays.equals(
                    Arrays.stream(other.binsUsedSpace).sorted().toArray(),
                    Arrays.stream(this.binsUsedSpace).sorted().toArray()
            );
        }
        return false;
    }
}
