// ISO.java
// Independent system operator
package amesmarket;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.extern.psst.PSSTDAMOpt;
import java.util.ArrayList;
import java.sql.*;
import java.text.DecimalFormat;
import fncs.JNIfncs;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ISO {

    // ISO's data;
    private double[][] supplyOfferByGen;
    public double[][] GenProfileByNDG, RTGenProfileforNDGs;
    public double[][] loadProfileByLSE, RTloadProfileforLSEs; // If ISO doesn't receive real-time load forecast, it uses previously obtained loadProfileByLSE that is copied into loadProfileRT
    private double[][][] PSLdemandBidByLSE, RTPSLdemandBidByLSE;
    private int[][] demandHybridByLSE;
    private double[][] supplyOfferByGenRealTime;
    private double[][] committedLoadByLSERealTime;
    private double[][][] demandBidByLSERealTime;
    private double[][] dailyPriceSensitiveDispatch, dailyPriceSensitiveDispatchRT;
    private double[][] GenDAMDispatch, supplyOfferRT;
    private double[][] DAMLMP;
    private double[][] RTMLMP;
    private double[][] dailyBranchFlow;
    private ArrayList commitmentListByDay; // hourly commitments list for each agent by day (d)
    private ArrayList lmpListByDay;        // hourly LMPs list for each bus by day (d)

    private double[][] RTMDispatch, RTbranchflow;
    private double[][] dailyRTProductionCost;
    private double[][] dailyRTStartupCost;
    private double[][] dailyRTShutdownCost;


    private List<CommitmentDecision> genDAMCommitment, genRTCommitmentVector;

    private static final int A_INDEX = 0;
    private static final int B_INDEX = 1;
    private static final int CAP_LOWER = 2;
    private static final int CAP_UPPER = 3;

    private AMESMarket ames;
    public DAMarket dam;
    private RTMarket rtm;
    private RTMOptimization RTMOpt;
    private int H, I, J, K, L, M;

    private String SolutionStatus;

    // constructor
    public ISO(AMESMarket model) {
        //System.out.println("Creating the ISO object: iso \n");
        commitmentListByDay = new ArrayList();
        lmpListByDay = new ArrayList();
        ames = model;
        H = ames.NUM_HOURS_PER_DAY;
        I = ames.getNumGenAgents();
        J = ames.getNumLSEAgents();
        K = ames.getNumNodes();
        L = ames.getNumNDGAgents();
        M = ames.M;
        supplyOfferRT = new double[I][5];
        dailyPriceSensitiveDispatchRT = new double[H][J];

        dam = new DAMarket(ames);
        rtm = new RTMarket(this, ames);

    }

    
    public void marketOperation(int m, int interval, int h, int day, boolean FNCSActive) {

        if (day > 1 && m % this.ames.M == 0) {
            rtm.RTMCollectBidsAndOffers(h, day-1, FNCSActive);
            
            this.RTPSLdemandBidByLSE = this.rtm.getRTPSLDemandBidByLSE();
            double[][] realtimeload = this.getRealTimeLoadForecast(h - 1, day-1, FNCSActive); // fncs.get_events() is called to receive RT load forecast
            double[][] realtimeNDG = this.getRealTimeNDGForecast(h - 1, day-1, FNCSActive); // fncs.get_events() is called to receive RT NDG forecast

            System.out.println("\n\nRTM Optimization executing on Day " + (day-1) + " at Hour " + h + "- Interval " + (interval+1) + "\n");
            this.rtm.RTMarketOptimization(this.genRTCommitmentVector,
                    realtimeload, realtimeNDG, RTPSLdemandBidByLSE, m, interval, h, day);

            SolutionStatus = this.rtm.getSolutionStatus();

            if (!SolutionStatus.equals("infeasible")) {
                this.RTMLMP = this.rtm.getRtLMPs();
                this.RTMDispatch = this.rtm.getRtDispatches();

                this.ames.addRealTimeLMPByInterval(this.RTMLMP);
                this.ames.addGenAgentRealTimeDispatchByInterval(this.RTMDispatch);
                
                this.postRealTimeSolutions(h, day-1);  
            }
        }

        if (h == 6 && m == 0) {
            
            dam.DAMCollectBidsAndOffers(h, day, FNCSActive);  // fncs.get_events() is called to receive DAM forecast

            this.supplyOfferByGen = this.dam.getSupplyOfferByGen();
            this.loadProfileByLSE = this.dam.getLoadProfileByLSE();
            this.GenProfileByNDG = this.dam.getGenProfileByNDG();

            this.PSLdemandBidByLSE = dam.getDemandBidByLSE();

        }

        if (h == 10 && m == 0) {
            System.out.println("\n\nDAM Optimization for DAY " + day + " is executing at hour: " + h + "\n");

            this.dam.DAMarketOptimization(day, loadProfileByLSE, GenProfileByNDG, PSLdemandBidByLSE, supplyOfferByGen);
            SolutionStatus = this.dam.getSolutionStatus();

            if (!SolutionStatus.equals("infeasible")) {
                this.genDAMCommitment = this.dam.getDAMCommitment();
                this.DAMLMP = this.dam.getDAMLMPSolution();
                this.GenDAMDispatch = this.dam.getDAMDispatchSolution();

                ames.addGenAgentDispatchByDay(GenDAMDispatch); 
                ames.addLMPByDay(DAMLMP);
                // ames.addLSEAgenPriceSensitiveDemandByDay(dailyPriceSensitiveDispatch);
            }
        }

        if ((h == 24) && (day < this.ames.DAY_MAX) && m == 0) {
            
            ArrayList<GenAgent> genAgentList = this.ames.getGenAgentList();
            int[] tempVector = new int[this.ames.NUM_HOURS_PER_DAY];
            for (int j = 0; j < this.ames.getNumGenAgents(); j++) {
                GenAgent gc = genAgentList.get(j);
                for (int hour = 0; hour < this.ames.NUM_HOURS_PER_DAY; hour++) {
                    tempVector[hour] = this.dam.getGenDAMCommitmentStatusNextDay()[hour][j];
                }
                //System.out.println("");
                gc.setCommitmentStatus(tempVector); // Gen status is needed for RTM 
            }

            this.endOfDayCleanup();
            this.postScheduleToGenCos(day, this.genDAMCommitment);
        }

    }

    private double[][] getRealTimeLoadForecast(int h, int d, boolean FNCSActive) {

        int ColSize = (int) this.ames.NIRTM;

        double[][] LoadProfileByLSE = new double[J][ColSize];

        //receiving load forecast for real time operation
        if (FNCSActive) {
            String[] events = JNIfncs.get_events();
            for (int i = 0; i < events.length; ++i) {
                String[] values = JNIfncs.get_values(events[i]);

                for (int j = 0; j < J; j++) {
                    if (events[i].equals("loadforecastRTM_" + String.valueOf(j + 1))) {
                        System.out.println("\n Receiving RTM loadforecast: " + values[0]);

                        //Hourly forecast is converted uniformly into per interval forecast
                        for (int k = 0; k < (ColSize); k++) {
                            LoadProfileByLSE[j][k] = Double.parseDouble(values[0]);
                        }
                    }
                }
            }
        } else {
            for (int j = 0; j < J; j++) {
                for (int k = 0; k < (ColSize); k++) {
                    //Hourly forecast is converted uniformly into per interval forecast
                    LoadProfileByLSE[j][k] = this.RTloadProfileforLSEs[j][h];
                }
            }
        }
        return LoadProfileByLSE;
    }

    private double[][] getRealTimeNDGForecast(int h, int d, boolean FNCSActive) {

        int ColSize = (int) this.ames.NIRTM;

        double[][] hourlyNDGProfileByBus = new double[L][ColSize];

        //receiving NDG forecast for real time operation
        if (FNCSActive) {
            String[] events = JNIfncs.get_events();
            for (int i = 0; i < events.length; ++i) {
                String[] values = JNIfncs.get_values(events[i]);

                for (int j = 0; j < L; j++) {
                    if (events[i].equals("ndgforecastRTM_" + String.valueOf(j + 1))) {
                        System.out.println("receiving NDG forecast: " + values[0]);

                        //Hourly forecast is converted uniformly into per interval forecast
                        for (int k = 0; k < (ColSize); k++) {
                            hourlyNDGProfileByBus[j][k] = Double.parseDouble(values[0]);
                        }
                    }
                }
            }
        } else {

            for (int j = 0; j < L; j++) {
                for (int k = 0; k < (ColSize); k++) {
                    hourlyNDGProfileByBus[j][k] = this.dam.getGenProfileByNDG()[j][h]; 
                }
            }
        }

        return hourlyNDGProfileByBus;
    }

    public void endOfDayCleanup() {
        RTloadProfileforLSEs = new double[loadProfileByLSE.length][H];
        RTGenProfileforNDGs = new double[GenProfileByNDG.length][H];
        for (int i = 0; i < loadProfileByLSE.length; i++) {
            System.arraycopy(loadProfileByLSE[i], 0, RTloadProfileforLSEs[i], 0, loadProfileByLSE[i].length);
        }
        for (int i = 0; i < GenProfileByNDG.length; i++) {
            System.arraycopy(GenProfileByNDG[i], 0, RTGenProfileforNDGs[i], 0, GenProfileByNDG[i].length);
        }
        for (int i = 0; i < this.I; i++) {
            for (int j = 0; j < 4; j++) {
                this.supplyOfferRT[i][j] = this.supplyOfferByGen[i][j];
            }
        }
        //Copy the commitment schedule over to the RT datastructures.
        for (int i = 0; i < this.I; i++) {
            this.genRTCommitmentVector = new ArrayList<>();
            for (CommitmentDecision cd : this.genDAMCommitment) {
                // clone to keep from overwriting the DA data.
                CommitmentDecision rtcd = new CommitmentDecision(cd);

                GenAgent ga = this.ames.getGenAgentByName(cd.generatorName);

                // Always commit the generator in the realtime market if
                // it is a canary. Work around to ensure enough capacity for any
                // load when a reserve market doesn't exist in the program.
                if (ga.isCanary()) {
                    System.out.println("Canary: " + ga.getID() + ga.isCanary() + ga.getGenID());
                    for (int h = 0; h < rtcd.commitmentDecisions.length; h++) {
                        rtcd.commitmentDecisions[h] = 1;
                    }
                }

                this.genRTCommitmentVector.add(rtcd);

            }
        }
    }

    /**
     * Send the commitment decisions to the correct GenAgents.
     *
     * @param day
     * @param commDecision
     */
    private void postScheduleToGenCos(int day, List<CommitmentDecision> commDecision) {
        if (commDecision == null) {
            return; 
        }

        for (CommitmentDecision cd : commDecision) {
            int[] result = new int[this.ames.NUM_HOURS_PER_DAY];
            result = Arrays.copyOf(cd.commitmentDecisions, cd.commitmentDecisions.length);
            this.ames.getGenAgentByName(cd.generatorName).addCommitmentForDay(day, result); 
        }
    }

    /**
     * Copy the solutions to the interested parties.
     *
     * @param day
     */
    public void postRealTimeSolutions(int hour, int day) {
        
        this.postRTDispatchesToGenAgents(day, this.RTMDispatch);
    }

    public static String getStrings(double[][] a, int index) {
        //String[][] output = new String[a.length][];
        //int i = 0;
        String output = "";
        DecimalFormat LMPFormat = new DecimalFormat("###.####");
        for (int i = 0; i < a.length; i++) {
            //System.out.print( a[i][index]);
            String temp1 = LMPFormat.format(a[i][index]);
            output = output + temp1 + ",";
            //System.out.println("temp.."+temp);
            //output[i++] = Arrays.toString(d).replace("[", "").replace("]", "").split(",");
        }

        //System.out.println();
        return output;
    }


    public void produceCommitmentSchedule(int h, int d) {
        System.out.println("Hour " + h + " Day " + d
                + ": produce commitment schedule.");
    }



    /**
     * Post/store the commitment for each hour/genagent.
     *
     * @param dispatches hour X genco grid.
     */
    private void postRTDispatchesToGenAgents(int day, double[][] dispatches) {
        final ArrayList<GenAgent> genCos = this.ames.getGenAgentList();
        for (int gc = 0; gc < this.I; gc++) {
            final double[] genDispatches = new double[this.ames.NIRTM];
            for (int h = 0; h < this.ames.NIRTM; h++) {
                genDispatches[h] = dispatches[h][gc];
            }
            genCos.get(gc).addActualDispatch(day, genDispatches);
        }
    }

    // Get and set methods
    public double[][] getSupplyOfferByGen() {
        return supplyOfferByGen;
    }

    public double[][] getGenProfileByNDG() {
        return GenProfileByNDG;
    }

    public double[][] getLoadProfileByLSE() {
        return loadProfileByLSE;
    }


    public double[][][] getPSLDemandBidByLSE() {
        return PSLdemandBidByLSE;
    }

    public double[][][] getRTPSLDemandBidByLSE() {
        return RTPSLdemandBidByLSE;
    }


    public double[][] getSupplyOfferByGenRT() {
        return supplyOfferRT;
    }

    public double[][] getPriceSensitiveDispatchRT() {
        return dailyPriceSensitiveDispatch;
    }


    public DAMarket getDAMarket() {
        return dam;
    }

    public RTMarket getRTMarket() {
        return rtm;
    }

    public double[][] getDAMLMP() {
        return this.DAMLMP;
    }

    public double[][] getDailyRealTimeLMP() {
        return this.RTMLMP;
    }

    public double[][] getDAMktUnitPower() {
        return this.GenDAMDispatch;
    }

    public int[][] getDAMktUnitSchedule() {
        return this.dam.getGenDAMCommitmentStatusNextDay();
    }

    public String getSolutionStatus() {
        return this.SolutionStatus;
    }
    
    
    public int[][] getDemandHybridByLSE() {
        return demandHybridByLSE;
    }
}
