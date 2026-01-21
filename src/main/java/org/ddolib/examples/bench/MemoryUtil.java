package org.ddolib.examples.bench;

public class MemoryUtil {


    public static long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    public static String printMemoryConsumption(long bytes) {
        double megabytes = (double) bytes / (1024 * 1024);
        return "memory:" + megabytes;
    }

}
