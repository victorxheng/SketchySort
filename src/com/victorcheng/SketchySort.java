package com.victorcheng;

import java.io.*;
import java.util.Arrays;

import static com.victorcheng.SketchySort.*;

class SketchySort {
    //MODIFIABLE VARIABLES
    static int testCount = 5000;//number of times to run the sorting algorithm, takes best time
    static int threadCount = 5;//number of threads (can be any number now, seems that optimal is 6-7)

    static boolean starter = false;
    static boolean starter3 = false;

    static int random1Size = 10000000;
    //input array
    static int[] random1 = new int[random1Size];

    //combine all sketchyTables into 1 transposition table
    static byte[] table = new byte[100000 + 1];

    //all sorted values are stored in this array
    static int[] finalArray = new int[random1Size];
    static SketchyThreadToTable[] SketchyToTableThreads = new SketchyThreadToTable[threadCount];
    static SketchyThreadFromTable t1;
    static SketchyThreadFromTable t2;

    public static void main(String[] args) throws IOException, InterruptedException {
        long time1 = System.nanoTime();
        //loads in file onto array, random1
        BufferedReader f = new BufferedReader(new FileReader("random1.txt"));
        for (int i = 0; i < random1Size; i++) {
            random1[i] = Integer.parseInt(f.readLine());
        }

        //verifies that the sorting algorithm works
        //sketchySort1();
        //System.out.println(verifySorted(finalArray));

        //begins tests, finding shortest
        double shortestTime = 10000000;
        for (int i = 0; i < testCount; i++) {
            reset();
            //sketchy sort algorithm. time is taken right before and right after
            long time2 = System.nanoTime();
            sketchySort1();
            long time3 = System.nanoTime();
            double sortTime = (double) (time3 - time2) / (double) 1000000;
            if (sortTime < shortestTime) shortestTime = sortTime;
        }

        long time4 = System.nanoTime();
        print(shortestTime);
        print((double) (time4 - time1) / (double) 1000000 + shortestTime);

        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("random.out")));
        int[] outArray = finalArray;
        for (int i : outArray) {
            out.println(i);
        }
        out.close();
    }

    //resets all objects with a new instance
    private static void reset() {
        starter = false;
        starter3 = false;
        int approxCount = random1Size / threadCount;
        int start = 0;
        for (int i = 0; i < SketchyToTableThreads.length - 1; i++) {
            SketchyToTableThreads[i] = new SketchyThreadToTable(start, approxCount);
            start += approxCount;
        }
        SketchyToTableThreads[threadCount - 1] = new SketchyThreadToTable(start, random1Size - start);

        for (SketchyThreadToTable s : SketchyToTableThreads) {
            s.start();
        }

        t1 = new SketchyThreadFromTable(true);
        t2 = new SketchyThreadFromTable(false);
        t1.start();
        t2.start();
        finalArray = new int[random1Size];
        table = new byte[100000 + 1];
        Arrays.fill(table, (byte) -128);
    }

    private static void sketchySort1() {
        //SECTION 1A
        starter = true;//starts threads
        while (true) {//waits for threads to finish
            boolean b = false;
            for (SketchyThreadToTable s : SketchyToTableThreads) {
                if (s.isAlive()) {
                    b = true;
                    break;
                }
            }
            if (!b) break;
        }
        //SECTION 1A Time: Around 1.97

        //SECTION 2A
        for (SketchyThreadToTable sk : SketchyToTableThreads) {
            for (int i = 1; i < 100001; i++) {
                table[i] += sk.sketchyTable[i] + 128;
            }
        }
        //SECTION 2A Time: Around 0.018

        //SECTION 3A
        starter3 = true;
        while (t1.isAlive() || t2.isAlive()) {//waits for last threads to finish
        }
        //SECTION 3A Time: 2.28

    }

    private static boolean verifySorted(int[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1] > array[i]) return false;
        }
        return array[0] == 1 && array[array.length - 1] == 100000;
    }

    public static void print(Object o) {
        System.out.println("Time" + ": " + o.toString());
    }
}

class SketchyThreadToTable extends Thread {
    byte[] sketchyTable = new byte[100000 + 1];
    int start, count;

    public SketchyThreadToTable(int start, int count) {
        this.start = start;
        this.count = count;
        Arrays.fill(sketchyTable, (byte) -128);
    }

    public void run() {
        while (!starter) {
            Thread.yield();
        }
        for (int i = start; i < start + count; i++) sketchyTable[random1[i]]++;
    }
}

class SketchyThreadFromTable extends Thread {
    boolean s;

    public SketchyThreadFromTable(boolean s) {
        this.s = s;
    }

    public void run() {
        while (!starter3) {
            Thread.yield();
        }
        if (s) {
            int finalIndex = 0;
            for (int i = 1; i <= 50000; i++) {
                for (int j = finalIndex; j < table[i] + 128 + finalIndex; j++) {
                    finalArray[j] = i;
                }
                finalIndex += table[i] + 128;
            }
        } else {
            int finalIndex = 9999999;
            for (int i = 100000; i > 50000; i--) {
                for (int j = finalIndex; j > finalIndex - (table[i] + 128); j--) {
                    finalArray[j] = i;
                }
                finalIndex -= (table[i] + 128);
            }
        }
    }
}