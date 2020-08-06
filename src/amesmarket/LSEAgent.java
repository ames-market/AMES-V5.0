// LSEAgent.java
// Load serving entity (wholesale power buyer)
package amesmarket;

import fncs.JNIfncs;
import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.sql.*;

/**
 * Example showing what lseData[i] contains LSEData //ID	atBus	block	LP	block	LP
 * block	LP	block	LP 1	1	6	22	6	30	6	34	6	27
 *
 * // block: load block for the next how many hours // LP: load profile (fixed
 * demand)
 */
public class LSEAgent implements Drawable {

    private static final int ID = 0;
    private static final int AT_NODE = 1;
    private static final int BLOCK1 = 2;
    private static final int LP1 = 3;
    private static final int BLOCK2 = 4;
    private static final int LP2 = 5;
    private static final int BLOCK3 = 6;
    private static final int LP3 = 7;
    private static final int BLOCK4 = 8;
    private static final int LP4 = 9;

    private static final int HOURS_PER_DAY = 24;
    private static final int PRICE_PARAM_LEN = 5;

    // LSE's data
    private int x;      // Coordinate x on trans grid
    private int y;      // Coordinate y on trans grid
    private double power;
    private double money;

    private int id;
    private int atBus;
    private double[] loadProfile;
    private double[] loadForecast;
    private double[] committedLoad;

    private double[][] priceSensitiveDemand; // 24 hour

//    private double d;                 // LSE's (true) cost attribute
//    private double e;                 // LSE's (true) cost attribute
//    private double f;                 // LSE's (true) cost attribute
//    private double PMax;         // LSE's (true) maximum demand limit
//    private double[] trueDemandBid;   // (d,e,f,PMax), true
    private int[] hybridFlag;         // use loadProfile or price sensitive demand
    private int[] realTimeHybridFlag;
    // LSE's records by hours (within a day)
    private double[] psDispatch;  // Day-Ahead hourly price-sensitive demand quantity

    // Constructor
    public LSEAgent(double[] lseData, double[][] lsePriceSenData, int[] lseHybridData) {

        x = -1;
        y = -1;
        power = 0;
        money = 0;

        // Parse lseData
        id = (int) lseData[ID];
        atBus = (int) lseData[AT_NODE];
        loadProfile = new double[HOURS_PER_DAY];
        loadForecast = new double[HOURS_PER_DAY];
        hybridFlag = new int[HOURS_PER_DAY];
        realTimeHybridFlag = new int[HOURS_PER_DAY];
        priceSensitiveDemand = new double[HOURS_PER_DAY][PRICE_PARAM_LEN];
        //the load indludes the load profile and price sensititve demand. There are three parts for the
        //price sensitive demand
        committedLoad = new double[HOURS_PER_DAY];

        for (int h = 0; h < HOURS_PER_DAY; h++) {
            realTimeHybridFlag[h] = 1;
        }
        //System.out.println("LSE ID="+id); it is flagged off
        for (int h = 2; h < lseData.length; h++) {
            loadProfile[h - 2] = lseData[h];
            hybridFlag[h - 2] = lseHybridData[h];

            //System.out.println("h="+(h-2)+"\tloadProfile="+loadProfile[h-2]+"\tFlag="+hybridFlag[h-2]);
        }

        for (int h = 0; h < HOURS_PER_DAY; h++) {
            //System.out.println();

            for (int j = 0; j < PRICE_PARAM_LEN; j++) {
                priceSensitiveDemand[h][j] = lsePriceSenData[h][j + 3];
                //System.out.print("h="+h+"\tj="+n+"\tpriceSensitiveDemand[h][n]="+priceSensitiveDemand[h][n]+"   \t" );
            }
        }
    }
//method

    public double[] submitDAMFixedDemandBid(int day, int lse, boolean FNCSActive) {

        double[] load = new double[HOURS_PER_DAY];
        // Receives load forecast through fncs
        if (day > 1) {
            if (FNCSActive) {
                String[] events = JNIfncs.get_events();
                for (int i = 0; i < events.length; ++i) {
                    String[] values = JNIfncs.get_values(events[i]);

                    for (int j = 0; j < HOURS_PER_DAY; j++) {
                        if (events[i].equals("loadforecastDAM_LSE" + String.valueOf(lse + 1) + "_H" + String.valueOf(j))) {
                            System.out.println("LSE" + (lse + 1) + " Received DAM loadforecast: " + values[0]);
                            load[j] = Double.parseDouble(values[0]);
                        }
                    }
                }
                for (int i = 0; i < HOURS_PER_DAY; i++) {
                    loadForecast[i] = load[i];
                }

            } else {
                loadForecast = loadProfile;
            }
            return loadForecast;
        } else {
            return loadProfile;
        }
    }

    public double[][] submitDAMPriceSensitiveDemandBid(int day, int lse, int PSLFlag, boolean FNCSActive) {
        double[][] reportDemandBid = new double[HOURS_PER_DAY][PRICE_PARAM_LEN];
        //double[][] temp = new double[HOURS_PER_DAY][3];

        if (day > 1 && PSLFlag == 1) {
            if (FNCSActive) {
                String[] events = JNIfncs.get_events();
                //System.out.println("DAM events.len: " + events.length);
                for (int i = 0; i < events.length; ++i) {
                    //String value = JNIfncs.get_value(events[i]);
                    String[] values = JNIfncs.get_values(events[i]);

                    for (int j = 0; j < HOURS_PER_DAY; j++) {
                        if (events[i].equals("PSLDemandBidDAM_LSE" + String.valueOf(lse + 1) + "_dVal_H" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS0_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid d: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][0] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidDAM_LSE" + String.valueOf(lse + 1) + "_eVal_H" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS1_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid e: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][1] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidDAM_LSE" + String.valueOf(lse + 1) + "_fVal_H" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS2_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid f: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][2] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidDAM_LSE" + String.valueOf(lse + 1) + "_SLMaxVal_H" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS2_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid SLMax: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][3] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidDAM_LSE" + String.valueOf(lse + 1) + "_NSVal_H" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS2_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid SLMax: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][4] = Integer.parseInt(values[0]);
                        }
                    }
                }

                for (int h = 0; h < HOURS_PER_DAY; h++) {
                    if (reportDemandBid[h][1] == 0 && reportDemandBid[h][2] == 0) {
                        for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                            reportDemandBid[h][i] = priceSensitiveDemand[h][i];
                        }
                    } else if (reportDemandBid[h][2] > 0){
                        if(reportDemandBid[h][3] == 0){
                             reportDemandBid[h][3] = reportDemandBid[h][1] / (2 * reportDemandBid[h][2]);
                        }
                        if(reportDemandBid[h][4] == 0){
                            reportDemandBid[h][3] = 5;
                        }
                    }

                    //for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                      //  System.out.print(": " + reportDemandBid[h][i]);
                    //}
                    //System.out.println("");
                }

            } else {
                for (int h = 0; h < HOURS_PER_DAY; h++) {
                    for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                        reportDemandBid[h][i] = priceSensitiveDemand[h][i];
                        //System.out.print(": " + reportDemandBid[h][i]);
                    }
                    //System.out.println("");
                }
            }
            return reportDemandBid;
        } else {
            for (int h = 0; h < HOURS_PER_DAY; h++) {
                for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                    reportDemandBid[h][i] = priceSensitiveDemand[h][i];
                    //System.out.print(": " + reportDemandBid[h][i]);
                }
                //System.out.println("");
            }
            return reportDemandBid;
        }

    }

    public double[][] submitRTMPriceSensitiveDemandBid(int h, int day, int lse, int PSLFlag, int NIRTM, boolean FNCSActive) {
        double[][] reportDemandBid = new double[NIRTM][PRICE_PARAM_LEN];
        //double[][] temp = new double[HOURS_PER_DAY][3];

        if (day > 0 && PSLFlag == 1) {
            if (FNCSActive) {
                String[] events = JNIfncs.get_events();
                //System.out.println("DAM events.len: " + events.length);
                for (int i = 0; i < events.length; ++i) {
                    //String value = JNIfncs.get_value(events[i]);
                    String[] values = JNIfncs.get_values(events[i]);

                    for (int j = 0; j < NIRTM; j++) {
                        if (events[i].equals("PSLDemandBidRTM_LSE" + String.valueOf(lse + 1) + "_dVal_I" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS0_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid d: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][0] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidRTM_LSE" + String.valueOf(lse + 1) + "_eVal_I" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS1_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid e: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][1] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidRTM_LSE" + String.valueOf(lse + 1) + "_fVal_I" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS2_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid f: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][2] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidRTM_LSE" + String.valueOf(lse + 1) + "_SLMaxVal_I" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS2_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid SLMax: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][3] = Double.parseDouble(values[0]);
                        }
                        if (events[i].equals("PSLDemandBidRTM_LSE" + String.valueOf(lse + 1) + "_NSVal_H" + String.valueOf(j))) {
                            //if (events[i].equals("loadforecastDAM_PS2_h" + String.valueOf(n))) {
                            //System.out.println("receiving DAM PSL bid SLMax: " + values[0]);
                            //System.out.println("i:"+i);
                            reportDemandBid[j][4] = Integer.parseInt(values[0]);
                        }
                    }
                }

                for (int n = 0; n < NIRTM; n++) {
                    if (reportDemandBid[n][1] == 0 && reportDemandBid[n][2] == 0) {
                        for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                            reportDemandBid[n][i] = priceSensitiveDemand[h-1][i];
                        }
                    }  else if (reportDemandBid[n][2] > 0){
                        if(reportDemandBid[n][3] == 0){
                             reportDemandBid[n][3] = reportDemandBid[n][1] / (2 * reportDemandBid[n][2]);
                        }
                        if(reportDemandBid[n][4] == 0){
                            reportDemandBid[n][3] = 5;
                        }
                    }

                    //for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                      //  System.out.print(": " + reportDemandBid[n][i]);
                    //}
                    //System.out.println("");
                }
            } else {

                for (int n = 0; n < NIRTM; n++) {
                    for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                        reportDemandBid[n][i] = priceSensitiveDemand[h-1][i];
                        //System.out.print(": " + reportDemandBid[n][i]);
                    }
                    //System.out.println("");
                }
            }
            return reportDemandBid;
        } else {
                for (int n = 0; n < NIRTM; n++) {
                    for (int i = 0; i < PRICE_PARAM_LEN; i++) {
                        reportDemandBid[n][i] = priceSensitiveDemand[h-1][i];
                        //System.out.print(": " + reportDemandBid[n][i]);
                    }
                    //System.out.println("");
                }
            return reportDemandBid;
        }

    }

    public int[] submitHybridFlag() {
        return hybridFlag;
    }

    public int[] submitRealTimeHybridFlag() {
        return realTimeHybridFlag;
    }

    // LSE's get and set methods
    public void setXY(int newX, int newY) {
        x = newX;
        y = newY;
    }

    public int getID() {
        return id;
    }

    public int getAtNode() {
        return atBus;
    }

    public double[] getLoadProfile() {
        return loadProfile;
    }

    public double getPower() {
        return power;
    }

    public void report() {
        System.out.println(getID() + "at" + x + "," + y + "demands" + getPower()
                + "MWhs of power.");
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void draw(SimGraphics sg) {
        sg.drawFastRoundRect(Color.yellow);  // LSE is yellow colored
    }

    public void setPSDispatch(double[] ps) {
        psDispatch = ps;
    }



}
