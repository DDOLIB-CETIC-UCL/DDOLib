package org.ddolib.astar.examples.JobShop;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class JSInstance {
    private int nJobs;
    private int nMachines;

    /**
     * <p> duration for a task per job </p>
     * <p> duration[job][jobOrder] </p>
     */
    private int[][] duration;

    /**
     * <p> number of the machine used for a task per job </p>
     * <p> machine[job][jobOrder] </p>
     */
    private int[][] machine;

    private int[][] tasks;

    private int horizon;

    /**
     * Read the job-shop instance from the specified file
     *
     * @param file
     */
    public JSInstance(String file, boolean openshop) {
        try {
            FileInputStream istream = new FileInputStream(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(istream));
            StringTokenizer tokenizer = new StringTokenizer(in.readLine());
            nJobs = Integer.parseInt(tokenizer.nextToken());
            nMachines = Integer.parseInt(tokenizer.nextToken());
            duration = new int[nJobs][nMachines];
            machine = new int[nJobs][nMachines];
            tasks = new int[nMachines][nJobs];
            horizon = 0;
            for (int i = 0; i < nJobs; i++) {
                tokenizer = new StringTokenizer(in.readLine());
                for (int j = 0; j < nMachines; j++) {
                    if (openshop ){
                        machine[i][j] = j;
                    }else {
                        machine[i][j] = Integer.parseInt(tokenizer.nextToken());
                    }
                    duration[i][j] = Integer.parseInt(tokenizer.nextToken());
                    horizon += duration[i][j];
                    tasks[machine[i][j]][i] = i * nMachines + j;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // getters
    public int getnJobs() {
        return nJobs;
    }

    public int getnMachines() {
        return nMachines;
    }

    /**
     * @return {@link JSInstance#duration}
     */
    public int[][] getDuration() {
        return duration;
    }

    /**
     * @return {@link JSInstance#machine}
     */
    public int[][] getMachine() {
        return machine;
    }

    public int getHorizon() {
        return horizon;
    }

    public int[][] getTasks() {
        return tasks;
    }
}
