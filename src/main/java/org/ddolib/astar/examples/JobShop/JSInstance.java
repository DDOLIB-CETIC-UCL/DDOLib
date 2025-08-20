package org.ddolib.astar.examples.JobShop;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.BitSet;
import java.util.StringTokenizer;

import static java.lang.Math.max;
import static java.lang.Math.min;

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

    public JSInstance(int task1, int task2, JSInstance data, int start1, int start2) {
        nJobs = 2;
        nMachines = data.nMachines;
        duration = new int[nJobs][nMachines];
        machine = new int[nJobs][nMachines];
        tasks = new int[nMachines][nJobs];
        horizon = min(start1,start2);

        int job1 = task1/nMachines;
        int job2 = task2/nMachines;
        int op1 = task1%nMachines;
        int op2 = task2%nMachines;
        BitSet machineSet1 = new BitSet(nMachines);
        BitSet machineSet2 = new BitSet(nMachines);
        for(int i = op1; i < nMachines; i++){

            duration[0][i - op1] = data.getDuration()[job1][i];
            machine[0][i - op1] = data.getMachine()[job1][i];
            tasks[machine[0][i - op1]][0] =  (i-op1);
            horizon += data.getDuration()[job1][i];
            machineSet1.set(machine[0][i - op1]);
        }
        int idx = 0;
        for(int i = 0; i < nMachines; i++){
            if(!machineSet1.get(i)){
                machine[0][(nMachines - op1)+idx] = i;
                duration[0][(nMachines - op1)+idx] = 0;
                tasks[i][0] = (nMachines - op1)+idx;
                idx++;
            }
        }
        idx =0;
        for(int i = op2; i < nMachines; i++){

            duration[1][i - op2] = data.getDuration()[job2][i];
            machine[1][i - op2] = data.getMachine()[job2][i];
            tasks[machine[1][i - op2]][1] =  nMachines + (i-op2);
            horizon += data.getDuration()[job2][i];
            machineSet2.set(machine[1][i - op2]);
        }
        for(int i = 0; i < nMachines; i++){
            if(!machineSet2.get(i)){
                machine[1][(nMachines - op2)+idx] = i;
                duration[1][(nMachines - op2)+idx] = 0;
                tasks[i][1] = nMachines + (nMachines - op2)+idx;
                idx++;
            }
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

    public void setHorizon(int horizon) {
        this.horizon = max(this.horizon,horizon);
    }

    public int[][] getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return "nJobs: " + nJobs + ", nMachines: " + nMachines + "\t duration : "+ Arrays.deepToString(duration) + "\t machine: " + Arrays.deepToString(machine) + "\t tasks: " + Arrays.deepToString(tasks);
    }
}
