
// DAMarket.java
// Day-ahead market
package amesmarket;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.extern.psst.DataFileWriter;
import amesmarket.extern.psst.PSSTDAMOpt;
import java.util.ArrayList;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DAMarket {

    private double CONVERGED_PROBABILITY;

    // Day-ahead market's data
    private AMESMarket ames;
    private ISO iso;
    
    
    private List<CommitmentDecision> genDAMCommitment;
    private double[][] DAMLMP;
    private double[][] GenDAMDispatch;
    
    private int[][] GenDAMCommitmentStatusNextDay;
    
    private ArrayList genAgentList;
    private ArrayList lseAgentList;
    private ArrayList NDGenAgentList;
    private double[] supplyOffer;
    private double[] loadProfile;
    private double[] NDGProfile;
    private double[][] demandBid;
    private int[] demandHybrid;
    private double[] trueSupplyOffer;
    private double[][] trueDemandBid;
    private double[][] supplyOfferByGen, supplyOffertemp;
    private double[][] GenProfileByNDG;
    private double[][] loadProfileByLSE;
    private double[][][] demandBidByLSE;
    private int[][] demandHybridByLSE;
    private double[][] trueSupplyOfferByGen;
    private double[][][] trueDemandBidByLSE;
    private double[] choiceProbability; // store each gen's learning choice probability

    private int numGenAgents;
    private int numLSEAgents;
    private int numNDGenAgents;
    private int numSupplyOfferParams;
    private int numHoursPerDay;
    
    private String SolutionStatus;
    
    private final PSSTDAMOpt scuc;
    private final File DAMReferenceModelFile = new File("DataFiles/DAMReferenceModel.dat");

    // constructor
    public DAMarket(AMESMarket model) {
        ames = model;
        this.scuc = new PSSTDAMOpt(this.iso, this.ames);
        
        CONVERGED_PROBABILITY = ames.getThresholdProbability();
        genAgentList = ames.getGenAgentList();
        lseAgentList = ames.getLSEAgentList();
        NDGenAgentList = ames.getNDGenAgentList();

        numGenAgents = ames.getNumGenAgents();
        numLSEAgents = ames.getNumLSEAgents();
        numNDGenAgents = ames.getNumNDGAgents();
        numHoursPerDay = ames.getNumHoursPerDay();

        numSupplyOfferParams = 5;

        supplyOfferByGen = new double[numGenAgents][numSupplyOfferParams];
        supplyOffertemp = new double[numGenAgents][numSupplyOfferParams];
        demandBidByLSE = new double[numLSEAgents][24][5];
        trueDemandBidByLSE = new double[numLSEAgents][24][5];
        GenProfileByNDG = new double[numNDGenAgents][numHoursPerDay];
        loadProfileByLSE = new double[numLSEAgents][numHoursPerDay];
        demandHybridByLSE = new int[numLSEAgents][numHoursPerDay];
        trueSupplyOfferByGen = new double[numGenAgents][numSupplyOfferParams];
        choiceProbability = new double[numGenAgents];
    }

    public void DAMCollectBidsAndOffers(int h, int d, boolean FNCSActive) {
        
        System.out.println("\nCollecting Bids and Offers at Hour " + h + " for Day " + (d) + " DAM operation");

        for (int i = 0; i < numGenAgents; i++) {
            GenAgent gen = (GenAgent) genAgentList.get(i);
            supplyOffer = gen.submitSupplyOffer();
            supplyOfferByGen[i] = supplyOffer; // Add supply offer from GEN_i to supplyOfferByGen in row i
        }
        
        String strTemp = String.format("%1$10s\t%2$15s\t%3$15s\t%4$15s\t%5$15s", "GenCo", "aReported", "bReported", "CapMin", "CapMaxReported");

        ames.addGenAgentSupplyOfferByDay(supplyOfferByGen);

        strTemp = String.format("%1$15s", "Hour");
        for (int j = 0; j < numLSEAgents; j++) {
            LSEAgent lse = (LSEAgent) lseAgentList.get(j);
            //Receive fixed demand bid into loadProfile
            loadProfile = lse.submitDAMFixedDemandBid(d, j, FNCSActive);
            loadProfileByLSE[j] = loadProfile;  // Add each load profile from LSE_j to loadProfileByLSE in row j

            //Receive price sensitive demand bid into demandBid and then into demandBidbyLSE for each LSE
            demandBid = lse.submitDAMPriceSensitiveDemandBid(d, j, ames.getPriceSensitiveDemandFlag(), FNCSActive);
            demandBidByLSE[j] = demandBid;

            String lseName = String.format("%1$10d", lse.getID());
            strTemp += String.format("\t%1$15s", lseName);
        }

        //System.out.println("numNDGenAgents:"+numNDGenAgents);
        for (int j = 0; j < numNDGenAgents; j++) {

            NDGenAgent ndg = (NDGenAgent) NDGenAgentList.get(j);
            NDGProfile = ndg.submitDAMNDGBid(d, j, FNCSActive);
            GenProfileByNDG[j] = NDGProfile;

        }

    }
    
    public void DAMarketOptimization(int day, double[][] loadProfileByLSE, double[][] GenProfileByNDG, double[][][] PSLdemandBidByLSE, double[][] supplyOfferByGen){
        
        int numTimeSteps = (int) (this.ames.NUM_HOURS_PER_DAY_UC / this.ames.getTestCaseConfig().DATDur);
        
        DataFileWriter dfw = new DataFileWriter();
        try {
            dfw.writeScucScenDatFile(this.DAMReferenceModelFile, this.ames, day, loadProfileByLSE, GenProfileByNDG, PSLdemandBidByLSE, supplyOfferByGen, numTimeSteps);
        } catch (AMESMarketException ex) {
            Logger.getLogger(DAMarket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        try {
            this.scuc.solveDAMOptimization(day);
            SolutionStatus = scuc.getSolutionStatus();
        } catch (IOException ex) {
            Logger.getLogger(DAMarket.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AMESMarketException ex) {
            Logger.getLogger(DAMarket.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //pull the data back out
        this.genDAMCommitment = this.scuc.getDAMCommitment();
        this.DAMLMP = this.scuc.getDAMLMPSolution();
        this.GenDAMDispatch = this.scuc.getDAMDispatchSolution();
        this.GenDAMCommitmentStatusNextDay = this.scuc.getGenDAMCommitmentStatusNextDay();
        
    }


    public double[][] getTrueSupplyOfferByGen() {
        return trueSupplyOfferByGen;
    }

    public double[][] getSupplyOfferByGen() {
        return supplyOfferByGen;
    }

    public double[][] getGenProfileByNDG() {
        return GenProfileByNDG;
    }

    public double[][] getLoadProfileByLSE() {
        return loadProfileByLSE;
    }

    public double[][][] getTrueDemandBidByLSE() {
        return trueDemandBidByLSE;
    }

    public double[][][] getDemandBidByLSE() {
        return demandBidByLSE;
    }

            
    public String getSolutionStatus() {
        return SolutionStatus;
    }
    
    public List<CommitmentDecision> getDAMCommitment() {

        return this.genDAMCommitment;
    }

    public double[][] getDAMLMPSolution() {
        return this.DAMLMP;
    }

    public double[][] getDAMDispatchSolution() {
        return this.GenDAMDispatch;
    }
    
    public int[][] getGenDAMCommitmentStatusNextDay() {
        return this.GenDAMCommitmentStatusNextDay;
    }
}
