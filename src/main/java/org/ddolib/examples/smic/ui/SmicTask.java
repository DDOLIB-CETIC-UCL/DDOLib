package org.ddolib.examples.smic.ui;

public record SmicTask(int start, int duration, int inventoryAtEnd, int id){
    public int end(){
        return start + duration;
    }

}