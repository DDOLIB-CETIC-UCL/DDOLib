package org.ddolib.ddo.examples.alp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ALPInstance{
    public int nbClasses;
    public int nbAircraft;
    public int nbRunways;
    public int[] classes;
    public int[] target;
    public int[] latest;
    public int[][] separation;

    public ALPInstance(String fName) throws IOException {
        final File f = new File(fName);
        try (final BufferedReader bf = new BufferedReader(new FileReader(f))) {
            int lineCounter = 0;
            List<String> linesList = bf.lines().toList();
            for(String line: linesList){
                String[] splitLine = line.split(" ");
                if(lineCounter == 0){
                    nbAircraft = Integer.parseInt(splitLine[0]);
                    nbClasses = Integer.parseInt(splitLine[1]);
                    nbRunways = Integer.parseInt(splitLine[2]);
                    classes = new int[nbAircraft];
                    latest = new int[nbAircraft];
                    target = new int[nbAircraft];
                    separation = new int[nbClasses][nbClasses];
                } else if(lineCounter < nbAircraft+1) {
                    target[lineCounter-1] = Integer.parseInt(splitLine[0]);
                    latest[lineCounter-1] = Integer.parseInt(splitLine[1]);
                    classes[lineCounter-1] = Integer.parseInt(splitLine[2]);
                } else {
                    int cnt = 0;
                    for(String s: splitLine){
                        separation[lineCounter-nbAircraft-1][cnt] = Integer.parseInt(s);
                        cnt ++;
                    }
                }
                lineCounter ++;
            }
        }
    }
}
