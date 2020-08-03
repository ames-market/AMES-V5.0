
// RTMarket.java
// Real-time market
package amesmarket;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.extern.psst.DataFileWriter;
import amesmarket.extern.psst.PSSTRTMOpt;
import amesmarket.filereaders.BadDataFileFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Real-time market.
 */
public class RTMarket {

    //Real time market's data
    private AMESMarket ames;
    private ISO iso;

    /*
     * The values of these arrays come from
     * the SCED. The PSSTSced allocates fresh arrays
     * each time it is called. Which means we do not
     * need to worry about aliasing issues with these
     * arrays and can just return the references.
     */
    private double[][] rtDispatches;
    private double[] intervalRtDispatches;
    //private double[][] rtBranchFlow;
    private double[][] rtLMPs;
    private double[] intervalRTLMPs;
    private double[][] rtProductionCost;
    private double[][] rtStartupCost;
    private double[][] rtShutdownCost;
    
    private double[] rtPrintGenCoDispatch;

    private double[][] supplyOfferByGen;
    private double[][] priceSensitiveDispatch;

    private ArrayList lseAgentList;
    private double[][] RTPSLdemandBid;
    private double[][][] RTPSLdemandBidByLSE;
    
    private int numGenAgents;
    private int numLSEAgents;
    private int numSupplyOfferParams;
    private int numHoursPerDay;
    
    private String SolutionStatus;
            
    private int numIntervalsInSim;

    private final PSSTRTMOpt sced;

    private final File RTUnitCommitmentsFile = new File("DataFiles/RTUnitCommitments.dat");
    private final File RTMReferenceModelFile = new File("DataFiles/RTMReferenceModel.dat");// new File("SCUCresources/ScenarioData/RTRefernceModel.dat");
    private final File RTMResultsFile = new File("DataFiles/RTMResults.dat");

    // constructor
    public RTMarket(ISO iso, AMESMarket model) {

        //System.out.println("Created a RTMarket objecct");
        ames = model;
        this.iso = iso;
        
        lseAgentList = ames.getLSEAgentList();
        
        numGenAgents = ames.getNumGenAgents();
        numLSEAgents = ames.getNumLSEAgents();
        numHoursPerDay = ames.getNumHoursPerDay();
        numSupplyOfferParams = 5;
        
        
        RTPSLdemandBidByLSE = new double[ames.NIRTM][numLSEAgents][4];

        supplyOfferByGen = new double[numGenAgents][numSupplyOfferParams];

        priceSensitiveDispatch = new double[numHoursPerDay][numLSEAgents];

        sced = new PSSTRTMOpt(model, model.getBaseS(),
                RTUnitCommitmentsFile,
                RTMReferenceModelFile,
                RTMResultsFile);
    }

    public void RTMCollectBidsAndOffers(int h, int d, boolean FNCSActive) {
        //System.out.println("Hour " + h + " Day " + d + ": Real Time Market operation.");
        supplyOfferByGen = iso.getSupplyOfferByGenRT();
        
        for (int j = 0; j < numLSEAgents; j++) {
            LSEAgent lse = (LSEAgent) lseAgentList.get(j);

            //Receive price sensitive demand bid into demandBid and then into demandBidbyLSE for each LSE
            RTPSLdemandBid = lse.submitRTMPriceSensitiveDemandBid(h-1, d, j, ames.getPriceSensitiveDemandFlag(), ames.NIRTM, FNCSActive);
            RTPSLdemandBidByLSE[j] = RTPSLdemandBid;

            //String lseName = String.format("%1$10d", lse.getID());
            //strTemp += String.format("\t%1$15s", lseName);
        }
    }

    /**
     *
     * @param genCoCommitments
     * @param rtDemand
     * @param rtNDG
     * @param h
     * @param d
     * @param m
     * @param interval
     * @throws AMESMarketException
     */
    public void RTMarketOptimization(
            List<CommitmentDecision> genCoCommitments,
            //double[][] supplyOfferRT, double[] dailyPriceSensitiveDispatchRT,
            double[][] rtDemand, double[][] rtNDG, double[][][] RTPSLdemandBidByLSE, int m, int interval,
            int h, int d) {

        
        //double[][][] PSLDemandBidLSE= this.iso.getRTPSLDemandBidByLSE();
        
        DataFileWriter dfw = new DataFileWriter();

        try {
            dfw.writeGenCommitments(ames, ames.M, m, h, genCoCommitments, RTUnitCommitmentsFile);
        } catch (AMESMarketException ex) {
            Logger.getLogger(RTMarket.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            dfw.writeScedScenDatFile(RTMReferenceModelFile, ames, m, h, d, rtDemand, rtNDG, RTPSLdemandBidByLSE, ames.NIRTM);
        } catch (AMESMarketException ex) {
            Logger.getLogger(RTMarket.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            //
            sced.solveRTMOptimization(interval);
            SolutionStatus = sced.getSolutionStatus();
        } catch (AMESMarketException ex) {
            Logger.getLogger(RTMarket.class.getName()).log(Level.SEVERE, null, ex);
        }

        //pull the data back out
        //power
        rtDispatches = sced.getRTMDispatchSolution();
        rtLMPs = sced.getRTMLMPSolution();
        intervalRTLMPs = sced.getIntervalLMP();
        intervalRtDispatches = sced.getIntervalGenDispatches();
        //sced.getDailyPriceSensitiveDemand();
        rtProductionCost = sced.getProductionCost();
        rtStartupCost = sced.getStartupCost();
        rtShutdownCost = sced.getShutdownCost();
        rtPrintGenCoDispatch = sced.getPrintGenCoDispatch();
    }

//    public double[][] getSupplyOfferByGen() {
//        return supplyOfferByGen;
//    }
//
//    public double[][] getPriceSensitiveDispatch() {
//        return priceSensitiveDispatch;
//    }


    /**
     * @return the rtDispatches
     */
    public double[][] getRtDispatches() {
        return rtDispatches;
    }
    
    public double[] getIntervalRtDispatches() {
        return intervalRtDispatches;
    }

//    /**
//     * @return the rtBranchFlow
//     */
//    public double[][] getRtBranchFlow() {
//        return rtBranchFlow;
//    }

    /**
     * @return the rtLMPs
     */
    public double[][] getRtLMPs() {
        return rtLMPs;
    }
    public double[] getIntervalRtLMPs() {
        return intervalRTLMPs;
    }

    /**
     * @return the rtProductionCost
     */
    public double[][] getRtProductionCost() {
        return rtProductionCost;
    }

    /**
     * @return the rtStartupCost
     */
    public double[][] getRtStartupCost() {
        return rtStartupCost;
    }

    /**
     * @return the rtShutdownCost
     */
    public double[][] getRtShutdownCost() {
        return rtShutdownCost;
    }
    
    /**
     * @return the rtShutdownCost
     */
    public double[] getRtPrintGenCoDispatch() {
        return rtPrintGenCoDispatch;
    }

    /**
     * @return the RTMReferenceModelFile
     */
    public File getRtRefModelFile() {
        return RTMReferenceModelFile;
    }
    
        
    public String getSolutionStatus() {
        return SolutionStatus;
    }
    
    
    public double[][][] getRTPSLDemandBidByLSE() {
        return RTPSLdemandBidByLSE;
    }

}
