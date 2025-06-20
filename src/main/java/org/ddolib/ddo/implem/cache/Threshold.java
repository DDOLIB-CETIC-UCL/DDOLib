package org.ddolib.ddo.implem.cache;

public class Threshold implements Comparable<Threshold> {
    private  int value;
    private  boolean explored;

    public Threshold(int value, boolean explored) {
        this.value = value;
        this.explored = explored;
    }

    public int getValue() {
        return value;
    }

    public boolean getExplored() {return this.explored;}

    public boolean isExplored() {
        return explored;
    }

    public void setValue(int val) {this.value = val;}

    public void setExplored(boolean expl) {this.explored = expl;}

    @Override
    public int compareTo(Threshold other) {
        if (this.value != other.value) {
            return Integer.compare(this.value, other.value);
        } else {
            return Boolean.compare(this.explored, other.explored);
        }
    }
    @Override
    public String toString() {return "Threshold [value=" + value + ", explored=" + explored + "]";}
}
