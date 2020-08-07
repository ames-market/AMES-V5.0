// AMESMarket.java - main class for AMESMarket project
//
// Reference: DynTestAMES Working Paper
// Available online at http://www.econ.iastate.edu/tesfatsi/DynTestAMES.JSLT.pdf
package amesmarket;

import amesmarket.CaseFileData.SCUCInputData;
import amesmarket.CaseFileData.StorageInputData;

// Repast
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.util.Random;
import uchicago.src.sim.engine.Controller;

// JReLM
import edu.iastate.jrelm.rl.rotherev.variant.VREParameters;
import edu.iastate.jrelm.core.BasicLearnerManager;
import edu.iastate.jrelm.gui.BasicSettingsEditor;

// Java
import java.util.ArrayList;
import java.util.*;
import fncs.JNIfncs;
import java.text.DecimalFormat;

public class AMESMarket extends SimModelImpl {

    public static final double VERSION = 5.0;
    public static final String DATE = "1/1/2019";

    public static final int GRID_X_SIZE = 40;
    public static final int GRID_Y_SIZE = 40;
    public static final int NUM_HOURS_PER_DAY = 24;
    public static final int NUM_HOURS_PER_DAY_UC = 24; // Number of hours for UC

    //public static final int M = 5; // Interval length => should divide 60
    //public static final int INTERVAL_SIZE = 1; // represents interval size in minutes - M/INTERVAL_SIZE gives number of time slots in every interval
    //public static final int NUM_INTERVALS_PER_HOUR = 60/M; // Interval length
    //public static final int NUM_INTERVALS_PER_DAY = NUM_HOURS_PER_DAY * NUM_INTERVALS_PER_HOUR;
    public boolean FNCSActive;
    public int M;    // RTOPDur
    public int NIRTM; // No of intervals in RTOPDur
    public int RTMFrequencyPerHour; //NUM_INTERVALS_PER_HOUR;

    public double COOLING;
    public double EXPERIMENTATION;
    public double INIT_PROPENSITY;
    public double RECENCY;

    public int M1;
    public int M2;
    public int M3;
    public double RIMAX_L;
    public double RIMAX_U;
    public double RIMIN_C;
    public double SLOPE_START;

    public double[][] genLearningParameters;

    public int DAY_MAX; // max days of model simulation
    public boolean bMaximumDay;
    public boolean bThreshold;

    public int RANDOM_SEED;

    public int stopCode; // foe stop number

    private int gridXSize = GRID_X_SIZE;
    private int gridYSize = GRID_Y_SIZE;
    private int numHoursPerDay = NUM_HOURS_PER_DAY;
    private int numNodes;      //K
    private int numBranches;   //N
    private int numGenAgents;  //I
    private int numLSEAgents;  //J
    private int numNDGenAgents;
    private int numStorageAgents;
    private double DARRD;
    private double DARRU;
    private double RTRRD;
    private double RTRRU;
    private int NumberOfReserveZones;
    private double[][] nodeData;
    private double[][] branchData;
    private double[][] genData;
    private boolean[] alertGenMarkers;
    private Map<String, SCUCInputData> extraGenCoParams;
    private Map<String, StorageInputData> StorageParams;
    private double[][] lseData;
    private double[][] NDGData;
    private double[][][] lsePriceSensitiveDemand;
    private int[][] lseHybridDemand;
    private double baseS;
    private double baseV;
    private double hasStorage;
    private double hasNDG;
    private INIT init;

// Model time
    private int min;
    public int interval;
    private int hour;
    private int day;
    private boolean isConverged; // used to determine if the model needs to be stopped
    private boolean isGenActionProbabilityConverged; // used to determine if the model needs to be stopped
    private boolean isGenLearningResultConverged; // another stopping rule, check is gen's learning results are stable
    private boolean isGenDailyNetEarningConverged;
    private int dayMax;
    private double dThresholdProbability;
    private double dGenPriceCap;
    private double dLSEPriceCap;
    private int priceSensitiveDemandFlag;
    private int iStartDay;
    private int iCheckDayLength;
    private double dActionProbability;
    private boolean bActionProbabilityCheck;
    private int iLearningCheckStartDay;
    private int iLearningCheckDayLength;
    private double dLearningCheckDifference;
    private boolean bLearningCheck;
    private double dDailyNetEarningThreshold;
    private boolean bDailyNetEarningThreshold;
    private int iDailyNetEarningStartDay;
    private int iDailyNetEarningDayLength;

/// Repast required variables
    private Schedule schedule;
    private DisplaySurface displaySurf;

// Model variables
    private TransGrid transGrid;
    private ISO iso;
    private ArrayList<GenAgent> genAgentList;
    private Map<String, GenAgent> genAgentsByName;
    private ArrayList<LSEAgent> lseAgentList;
    private ArrayList<NDGenAgent> NDGAgentList;
    private ArrayList<StorageAgent> StorageAgentList;

//  Learning variables
    private ActionDomain ad;
    int numLowerRIs;
    int numUpperRIs;
    int numUpperCaps;
    double RIMaxL;
    double RIMaxU;
    double RIMinC;
    double slopeStart;

    // Learning parameters
    private double[] coolingOfGen;
    private double[] initPropensityOfGen;
    private double[] experimentationOfGen;
    private double[] recencyOfGen;
    private int[] learningRandomSeedsOfGen;

    private ArrayList genAgentSupplyOfferByDay;
    private ArrayList lseAgentPriceSensitiveDemandByDay;
    private ArrayList lseAgentSurplusByDay;
    private ArrayList lseAgentSurplusWithTrueCost;
    private ArrayList lseAgentPriceSensitiveDemandWithTrueCost;
    private ArrayList genAgentProfitAndNetGainByDay;
    private ArrayList genAgentActionPropensityAndProbilityByDay;
    private ArrayList genAgentProfitAndNetGainWithTrueCost;
    private ArrayList genAgentDispatchWithTrueCost;
    private ArrayList genAgentDispatchByDay, genAgentRealTimeDispatchByInterval;

    private ArrayList branchFlowByDay;
    private ArrayList LMPByDay, realTimeLMPByInterval, realTimeBranchFlowByDay;
    private ArrayList LMPWithTrueCost;
    private ArrayList<double[][]> productionCostsByDay, startupCostsByDay, shutdownCostsByDay;

    private ArrayList lastDayGenActions;

    private CaseFileData testcaseConfig;

    private static Controller ModelController = null;
    private boolean bCalculationEnd = false;

    private String SolutionStatus = "optimal";

    private final boolean deleteIntermediateFiles;

    private long time_granted = 0;
    private long time_next = 0;

// RePast required methods
    public String getName() {
        return "AMES Market";
    }

    public String[] getInitParam() {
        String[] initParams = {"NumNodes", "NumBranches", "NumGenAgents", "NumLSEAgents"};
        return initParams;
    }

    // The first method SimModelImpl calls
    public void setup() {
        //System.out.println("Running setup");
        //System.out.println("\n setup method of AMESMarket.java is called");
        // Tear down any objects created over the course of the run to null.
        transGrid = null;
        iso = null;
        genAgentList = new ArrayList<GenAgent>();
        genAgentsByName = new HashMap<String, GenAgent>();
        lseAgentList = new ArrayList<LSEAgent>();
        NDGAgentList = new ArrayList<NDGenAgent>();
        StorageAgentList = new ArrayList<StorageAgent>();
        schedule = new Schedule(1);
        setInitialStructuralParameters(init);
        setInitialLearningParameters();
        setInitialActionDomainParameters();
        //System.out.println(" AMESMarket object parameters are set (some of them from INIT object). \n");
    }

//maybe this is related with input output method
    private void setInitialStructuralParameters(INIT init) {

        setNumNodes((int) init.getNodeData()[0][0]);
        setNumBranches(init.getBranchData().length);
        setNumGenAgents(init.getGenData().length);
        setNumLSEAgents(init.getLSEDataFixedDemand().length);
        setNumNDGenAgents(init.getNDGData().length);
        setNumStorageAgents(init.getStorageParams().size());
        sethasStorage(init.gethasStorage());
        sethasNDG(init.gethasNDG());

        setBaseS(init.getBaseS());
        setBaseV(init.getBaseV());
        //setReserveRequirements(init.getReserveRequirements());

        setNodeData(init.getNodeData());
        setBranchData(init.getBranchData());
        setGenData(init.getGenData());
        setAlertGenMarkers(init.getAlertGenMarker());
        setExtraGenData(init.getExtraGenCoParams());
        setStorageData(init.getStorageParams());
        setLSEData(init.getLSEDataFixedDemand());
        setNDGData(init.getNDGData());
        setLSEPriceSensitiveData(init.getLSEDataPriceSensitiveDemand());
        setLSEHybridData(init.getLSEDataHybridDemand());
    }

    private void setInitialLearningParameters() {
        coolingOfGen = new double[numGenAgents];
        experimentationOfGen = new double[numGenAgents];
        initPropensityOfGen = new double[numGenAgents];
        recencyOfGen = new double[numGenAgents];
        for (int i = 0; i < numGenAgents; i++) {
            coolingOfGen[i] = genLearningParameters[i][1];
            experimentationOfGen[i] = genLearningParameters[i][3];
            initPropensityOfGen[i] = genLearningParameters[i][0];
            recencyOfGen[i] = genLearningParameters[i][2];
        }
        learningRandomSeedsOfGen = new int[numGenAgents];
    }

    private void setInitialActionDomainParameters() {
        numLowerRIs = M1;
        numUpperRIs = M2;
        numUpperCaps = M3;
        RIMaxL = RIMAX_L;
        RIMaxU = RIMAX_U;
        RIMinC = RIMIN_C;
        slopeStart = SLOPE_START;
    }

    public void begin() {
        buildModel();
        buildSchedule();
        Random.createUniform();  // Create random uniform
    }

    public void buildModel() {

        System.out.println("\nAMES V5.0 models the ISO and each DAM/RTM market participant (LSE, GenCo) by means of a separate Java class.");
        Date sysDate = new Date();
        System.out.println("\nSimulation Start time: " + sysDate.toString() + "\n");
        //System.out.println("Print the user-specified random seed: " + RANDOM_SEED + "\n");

        //System.out.println("Print structural parameters\n");
        //printStructuralParameters();
        setRngSeed(RANDOM_SEED);

        min = 0;
        interval = 0;
        hour = 1;
        day = 1;
        isConverged = false;
        dayMax = DAY_MAX;

        NIRTM = M / getTestCaseConfig().RTKDur;
        RTMFrequencyPerHour = 60 / M;

        transGrid = new TransGrid(nodeData, branchData, gridXSize, gridYSize);

        java.util.Random randomSeed = new java.util.Random((int) this.getRngSeed());
        addGenAgents(randomSeed);
        addLSEAgents();
        addNDGenAgents();
        addStorageAgents();
        iso = new ISO(this);

        String strTemp = String.format("%1$15.2f", dGenPriceCap);
        //System.out.println("\n Supply-Offer Price Cap:  " + strTemp + " ($/MWh) \n");

        strTemp = String.format("%1$15.2f", dLSEPriceCap);
        //System.out.println("Demand-Bid Price Floor:  " + strTemp + " ($/MWh) \n");

        //System.out.println("\nPrice Sensitive LSE: " + priceSensitiveLSE + "\n");
        //System.out.println("Print the Simulation Controls: \n");
        //System.out.println("Print the derived learning random seed for each GenAgent:" + "\n");
        //printLearningRandomSeeds();
        //System.out.println("M: "+ M);
        System.out.println("Print the Stopping Rule:");
        if (bMaximumDay) {
            System.out.println("\t   (1) Maximum Day Check. The user-specified maximum day: " + DAY_MAX);
        }
    }

    private void printStructuralParameters() {
        System.out.println("Penalty Weight in DC-OPF Objective Function: " + nodeData[0][1]);
        System.out.println("\nGridData: ");
        int iNode = (int) nodeData[0][0];
        System.out.println("Number of Buses: " + iNode);
        System.out.println("Base Apparent Power: " + init.getBaseS());
        System.out.println("Base Voltage: " + init.getBaseV());

        System.out.println("\nNumber of Branches: " + numBranches);
        System.out.println("BranchData: ");
        String strTemp = String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s",
                "ID", init.getParamNames()[1][0], init.getParamNames()[1][1],
                init.getParamNames()[1][2], init.getParamNames()[1][3]);
        System.out.println(strTemp);
        for (int i = 0; i < branchData.length; i++) {
            strTemp = String.format("\t%1$15d\t%2$15d\t%3$15d\t%4$15.4f\t%5$15.4f",
                    i + 1, (int) branchData[i][0], (int) branchData[i][1], branchData[i][2], branchData[i][3]);
            System.out.println(strTemp);
        }

        System.out.println("Number of GenCo Agents: " + numGenAgents);
        System.out.println("GenCo Data: ");
        strTemp = String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s",
                init.getParamNames()[2][0], init.getParamNames()[2][1],
                init.getParamNames()[2][2], init.getParamNames()[2][3],
                init.getParamNames()[2][4], init.getParamNames()[2][5],
                init.getParamNames()[2][6], init.getParamNames()[2][7]);
        System.out.println(strTemp);
        //Support.printArray(init.getParamNames()[2]);
        for (int i = 0; i < genData.length; i++) {
            strTemp = String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f",
                    (int) genData[i][0], (int) genData[i][1], genData[i][2], genData[i][3],
                    genData[i][4], genData[i][5], genData[i][6], genData[i][7], genData[i][8], (int) genData[i][9]);
            System.out.println(strTemp);
        }

        System.out.println("\nGenCos' Action Domain Parameters \n");
        printActionDomainParameters();

        System.out.println("GenCos' Learning Parameters \n");
        printLearningParameters();

        System.out.println("Number of LSE Agents: " + numLSEAgents);
        System.out.println("LSE Fixed Demand by Hour: ");
        strTemp = String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s\t%9$15s\t%10$15s",
                init.getParamNames()[4][0], init.getParamNames()[4][1],
                init.getParamNames()[4][2], init.getParamNames()[4][3],
                init.getParamNames()[4][4], init.getParamNames()[4][5],
                init.getParamNames()[4][6], init.getParamNames()[4][7],
                init.getParamNames()[4][8], init.getParamNames()[4][9]);
        System.out.println(strTemp);

        for (int i = 0; i < lseData.length; i++) {
            strTemp = String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f\t%9$15.4f\t%10$15.4f",
                    (int) init.getLSESec1Data()[i][0], (int) init.getLSESec1Data()[i][1],
                    init.getLSESec1Data()[i][2], init.getLSESec1Data()[i][3],
                    init.getLSESec1Data()[i][4], init.getLSESec1Data()[i][5],
                    init.getLSESec1Data()[i][6], init.getLSESec1Data()[i][7],
                    init.getLSESec1Data()[i][8], init.getLSESec1Data()[i][9]);
            System.out.println(strTemp);
        }
        strTemp = String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s\t%9$15s\t%10$15s",
                init.getParamNames()[5][0], init.getParamNames()[5][1],
                init.getParamNames()[5][2], init.getParamNames()[5][3],
                init.getParamNames()[5][4], init.getParamNames()[5][5],
                init.getParamNames()[5][6], init.getParamNames()[5][7],
                init.getParamNames()[5][8], init.getParamNames()[5][9]);
        System.out.println(strTemp);

        for (int i = 0; i < lseData.length; i++) {
            strTemp = String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f\t%9$15.4f\t%10$15.4f",
                    (int) init.getLSESec2Data()[i][0], (int) init.getLSESec2Data()[i][1],
                    init.getLSESec2Data()[i][2], init.getLSESec2Data()[i][3],
                    init.getLSESec2Data()[i][4], init.getLSESec2Data()[i][5],
                    init.getLSESec2Data()[i][6], init.getLSESec2Data()[i][7],
                    init.getLSESec2Data()[i][8], init.getLSESec2Data()[i][9]);
            System.out.println(strTemp);
        }
        strTemp = String.format("\t%1$15s\t%2$15s\t%3$15s\t%4$15s\t%5$15s\t%6$15s\t%7$15s\t%8$15s\t%9$15s\t%10$15s",
                init.getParamNames()[6][0], init.getParamNames()[6][1],
                init.getParamNames()[6][2], init.getParamNames()[6][3],
                init.getParamNames()[6][4], init.getParamNames()[6][5],
                init.getParamNames()[6][6], init.getParamNames()[6][7],
                init.getParamNames()[6][8], init.getParamNames()[6][9]);
        System.out.println(strTemp);

        for (int i = 0; i < lseData.length; i++) {
            strTemp = String.format("\t%1$15d\t%2$15d\t%3$15.4f\t%4$15.4f\t%5$15.4f\t%6$15.4f\t%7$15.4f\t%8$15.4f\t%9$15.4f\t%10$15.4f",
                    (int) init.getLSESec3Data()[i][0], (int) init.getLSESec3Data()[i][1],
                    init.getLSESec3Data()[i][2], init.getLSESec3Data()[i][3],
                    init.getLSESec3Data()[i][4], init.getLSESec3Data()[i][5],
                    init.getLSESec3Data()[i][6], init.getLSESec3Data()[i][7],
                    init.getLSESec3Data()[i][8], init.getLSESec3Data()[i][9]);
            System.out.println(strTemp);
        }

        System.out.println("\nLSE Price-Sensitive Demand Function Parameters by Hour: ");
        strTemp = String.format("\t%1$10s\t%2$10s\t%3$10s\t%4$15s\t%5$15s\t%6$15s\t%7$15s",
                "ID", "atBus", "hourIndex", "d", "e", "f", "PMax");
        System.out.println(strTemp);

        for (int i = 0; i < lseData.length; i++) {
            for (int h = 0; h < 24; h++) {
                strTemp = "";

                for (int j = 0; j < 8; j++) {
                    if (j < 3) {
                        int iTemp = (int) lsePriceSensitiveDemand[i][h][j];
                        strTemp = strTemp + "\t" + String.format("%1$10d", iTemp);
                    } else {
                        double dTemp = lsePriceSensitiveDemand[i][h][j];
                        strTemp = strTemp + "\t" + String.format("%1$15.4f", dTemp);
                    }

                }

                System.out.println(strTemp);
            }
        }

    }

    private void printActionDomainParameters() {
        String temp = String.format("\t%1$10s\t%2$10s\t%3$10s\t%4$10s\t%5$10s\t%6$10S\t%7$10s\t%8$10s",
                "ID", "M1", "M2", "M3", "RIMaxL", "RIMaxU", "RIMinC", "SS");
        System.out.println(temp);

        for (int i = 0; i < numGenAgents; i++) {
            temp = String.format("\t%1$10d\t%2$10d\t%3$10d\t%4$10d\t%5$10.4f\t%6$10.4f\t%7$10.4f\t%8$10.4f",
                    (i + 1), (int) genLearningParameters[i][4],
                    (int) genLearningParameters[i][5],
                    (int) genLearningParameters[i][6],
                    genLearningParameters[i][7],
                    genLearningParameters[i][8],
                    genLearningParameters[i][9],
                    genLearningParameters[i][10]);
            System.out.println(temp);
        }

        System.out.println();

    }

    private void printLearningParameters() {
        String temp = String.format("\t%1$10s\t%2$10s\t%3$10s\t%4$10s\t%5$10s",
                "ID", "q(1)", "T", "r", "e");
        System.out.println(temp);
        for (int i = 0; i < numGenAgents; i++) {
            temp = String.format("\t%1$10d\t%2$10.4f\t%3$10.4f\t%4$10.4f\t%5$10.4f",
                    (i + 1), initPropensityOfGen[i], coolingOfGen[i], recencyOfGen[i], experimentationOfGen[i]);
            System.out.println(temp);
        }
        System.out.println();
    }

    private void printLearningRandomSeeds() {
        String temp = String.format("\t%1$10s\t%2$15s", "ID", "randomSeed");
        System.out.println(temp);
        for (int i = 0; i < numGenAgents; i++) {
            temp = String.format("\t%1$10d\t%2$15d", (i + 1), learningRandomSeedsOfGen[i]);
            System.out.println(temp);
        }
        System.out.println();
    }

    private void addGenAgents(java.util.Random rn) {

        for (int i = 0; i < numGenAgents; i++) {
            int randomNumber = rn.nextInt();
            learningRandomSeedsOfGen[i] = randomNumber;
            VREParameters learningParams = new VREParameters(genLearningParameters[i][1],
                    genLearningParameters[i][3], genLearningParameters[i][0], genLearningParameters[i][2], randomNumber);

            ad = new ActionDomain((int) genLearningParameters[i][4],
                    (int) genLearningParameters[i][5],
                    (int) genLearningParameters[i][6],
                    genLearningParameters[i][7],
                    genLearningParameters[i][8],
                    genLearningParameters[i][9]);

            GenAgent gen = new GenAgent(genData[i], learningParams, ad.getActionDomain(),
                    genLearningParameters[i][10], dGenPriceCap, randomNumber, iStartDay, iCheckDayLength, dActionProbability, bActionProbabilityCheck,
                    iLearningCheckStartDay, iLearningCheckDayLength, dLearningCheckDifference, bLearningCheck, dDailyNetEarningThreshold,
                    bDailyNetEarningThreshold, iDailyNetEarningStartDay, iDailyNetEarningDayLength, (int) genLearningParameters[i][11],
                    alertGenMarkers[i],
                    i/*assume the order we create the agents in is the index order.*/
            );

            //update the name to one in the case files.
            gen.setID(testcaseConfig.genData[i].name);

            gen.setNS(testcaseConfig.genData[i].NS);

            final String gcName = gen.getID();
            SCUCInputData sid = extraGenCoParams.get(gcName);
            if (sid != null) {
                gen.addExtraData(sid);
            } else {
                System.err.println("Could not find the extra GenCo data for " + gcName);
            }

            if (testcaseConfig.hasFuelType(gcName)) {
                gen.setFuelType(testcaseConfig.getFuelTypeForGen(gcName));
            }

            if (testcaseConfig.hasColdStartTime(gcName)) {
                gen.setColdStartTime(testcaseConfig.getColdStartTimeForGen(gen.getID()));
            }
            if (testcaseConfig.hasColdStartUpCost(gcName)) {
                gen.setColdStartUpCost(testcaseConfig.getColdStartUpCostForGen(gen.getID()));
            }
            if (testcaseConfig.hasHotStartUpCost(gcName)) {
                gen.setHotStartUpCost(testcaseConfig.getHotStartUpCostForGen(gen.getID()));
            }
            if (testcaseConfig.hasShutDownCost(gcName)) {
                gen.setShutDownCost(testcaseConfig.getShutDownCostForGen(gen.getID()));
            }

            transGrid.addGenAgentAtNodeK(gen, (i + 1));
            genAgentList.add(gen);
        }

        rebuildGenAgentNameCache();
    }

    private void addStorageAgents() {
        //System.out.println("numStorageAgents : " + numStorageAgents + "\n");
        for (int j = 0; j < numStorageAgents; j++) {
            String sName = Integer.toString(j + 1);
            StorageInputData sd = StorageParams.get(sName);
            StorageAgent se = new StorageAgent(sd);
            StorageAgentList.add(se);
        }
    }

    /**
     * Iterate over the list of genagents and build a map which associates agent
     * names with the object representing the genagent.
     *
     * Should be called anytime a new agent is added to the list.
     *
     * The objects stored in the map are aliases to the objects in
     * {@link #genAgentList}.
     */
    private void rebuildGenAgentNameCache() {
        genAgentsByName.clear(); //dump the list, just in case.

        for (GenAgent ga : genAgentList) {
            genAgentsByName.put(ga.getID(), ga);
        }
    }

    private void addLSEAgents() {
        for (int j = 0; j < numLSEAgents; j++) {
            LSEAgent lse = new LSEAgent(lseData[j], lsePriceSensitiveDemand[j], lseHybridDemand[j]);
            transGrid.addLSEAgentAtNodeK(lse, (j + 1));
            lseAgentList.add(lse);
        }
    }

    private void addNDGenAgents() {
        for (int j = 0; j < numNDGenAgents; j++) {
            NDGenAgent ndg = new NDGenAgent(NDGData[j]);
            //transGrid.addLSEAgentAtNodeK(ndg, (j + 1));
            NDGAgentList.add(ndg);
        }
    }

    //custom function to get strings
    public static String getStrings(double[][] a, int index) {
        //String[][] output = new String[a.length][];
        //int i = 0;
        String output = "";
        DecimalFormat DAMLMPFormat = new DecimalFormat("###.##");
        for (int i = 0; i < a.length; i++) {
            String temp1 = DAMLMPFormat.format(a[i][index]);
            output = output + temp1 + ", ";
            //System.out.println("temp.."+temp1);
            //output[i++] = Arrays.toString(d).replace("[", "").replace("]", "").split(",");
        }

        return output;
    }

    double[][] RTLMP = new double[this.numHoursPerDay][8];

    public void buildSchedule() {
        //System.out.println("\n\n\t*******************************************************************************");
        //System.out.println("\t********* Wholesale power market with learning traders is now running *********");
        //System.out.println("\t*******************************************************************************\n\n");

        System.out.println("\n\n\t********* Wholesale power market is now running *********\n\n");

        for (int h = 0; h < this.numHoursPerDay; h++) {
            for (int n = 0; n < 8; n++) {
                RTLMP[h][n] = 0;
            }
        }

        class WPMarket extends BasicAction {

            public void execute() {
                DecimalFormat RTMLMPFormat = new DecimalFormat("###.##");
                int day_len = 86400; //in sec
                long hour_len = 3600; // in sec
                int min_len = 60; // in sec

                long t = (min_len * M) / 2;
                int NIH = (int) hour_len / (min_len * M); // number of intervals in an hour
                interval = (hour - 1) * NIH + (int) min / M;

                //System.out.println("\nMarket Operation for Day: " + day + " Hour: " + hour + " Interval: " + (interval));

                if (day > 1) {
                    if (min % M == 0) {
                        time_next = (day - 1) * (day_len) + (hour - 1) * (hour_len) + min * (min_len) - M * min_len;
                    }
                }
                //System.out.print("time requested: " + time_next);

                if (FNCSActive) {
                    while (time_granted < time_next) {
                        time_granted = JNIfncs.time_request(time_next);
                    }
                }

                //System.out.println("   time granted: " + time_granted);
                iso.marketOperation(min, interval, hour, day, FNCSActive);

                if (hour == 10 || day > 1) {
                    SolutionStatus = iso.getSolutionStatus();
                    if (SolutionStatus.equals("infeasible")) {
                        stop();
                        System.out.println("Simulation is stopped.");
                        bCalculationEnd = true;
                    }
                }

                if (!SolutionStatus.equals("infeasible")) {
                    //requests time_request 
                    if (day > 1) {
                        if (min % M == 0) {
                            time_next = (day - 1) * (day_len) + (hour - 1) * (hour_len) + min * (min_len) - M * min_len + 1 * min_len;
                        }
                    } else {
                        if (min % M == 0) {
                            time_next = (day - 1) * (day_len) + (hour - 1) * (hour_len) + min * min_len;
                        }
                    }

                    //System.out.print(" time_next: " + time_next);
                    if (FNCSActive) {
                        time_granted = JNIfncs.time_request(time_next);
                        while (time_granted < time_next) {
                            time_granted = JNIfncs.time_request(time_next);
                        }
                    }

                    //System.out.println(" time_granted: " + time_granted);
                    int NumNodes = (int) nodeData[0][0];
                    if (hour == 13 && min == 0) {
                        System.out.println("");
                        for (int n = 0; n < NumNodes; n++) {
                            if (FNCSActive) {
                                JNIfncs.publish("DailyLMP", getStrings(iso.getDAMLMP(), n));
                            }
                            System.out.println("Publishing DAM LMP values of Bus" + (n+1) + " : " +getStrings(iso.getDAMLMP(), n));
                        }
                    }

                    if (day > 1 && min % M == 0) {

                        if (FNCSActive) {
                            JNIfncs.publish("RTLMP", String.valueOf(iso.getDailyRealTimeLMP()));
                        }
                        System.out.println("");
                        for (int i = 0; i < getNumNodes(); i++) {
                            System.out.print("RTLMP LMP for Bus " + (i + 1));
                            double AvgLMP = 0, sum = 0;
                            for (int m = 0; m < NIRTM; m++) {
                                sum = sum + iso.getDailyRealTimeLMP()[m][i];
                            }
                            AvgLMP = sum / NIRTM;
                            System.out.println(": " + RTMLMPFormat.format(AvgLMP));
                            RTLMP[hour - 1][i] = AvgLMP;
                        }
                    }


                    stopCode = 0;

                    if (bMaximumDay) {

                        if ((hour == 24) && (day == dayMax)) {// Only dayMax has been reached
                            //System.out.println("in STOPCODE: 1");
                            stop();
                            stopCode = stopCode | 0x1;   // first bit
                        }

                    }

                    if (stopCode > 0) {

                        bCalculationEnd = true;

                        Date sysDate = new Date();
                        System.out.println("\nSimulation End time: " + sysDate.toString() + "\n");

                        String stopStr = "";
                        int iStopNumber = 0;
                        int iFirstIndex = -1;
                        int iLastIndex = 0;
                        int iTemp = stopCode;
                        for (int i = 0; i < 5; i++) {
                            if ((stopCode & 0x1) == 0x1) {
                                if (iFirstIndex == -1) {
                                    iFirstIndex = i;
                                }

                                iStopNumber++;
                                iLastIndex = i;
                            }

                            stopCode /= 2;
                        }

                        stopCode = iTemp;
                        for (int i = 0; i < 5; i++) {
                            if ((stopCode & 0x1) == 0x1) {

                                if (iFirstIndex == i) {
                                    stopStr += (i + 1);
                                } else if ((iLastIndex == i) && (iLastIndex != iFirstIndex)) {
                                    stopStr += ", and " + (i + 1);
                                } else {
                                    stopStr += ", " + (i + 1);
                                }
                            }

                            stopCode /= 2;
                        }

                        System.out.println("\nThe current simulation run is concluded on Day " + day + "\n"); // + " in response to the activation of the following \nstopping rule:(" + stopStr + ")\n\n");

                    }

                    min = min + M;
                    if (min % min_len == 0) {
                        hour++;
                        min = 0;
                        if (hour == 25) {
                            min = 0;
                            hour = 1;
                            day++;
                        }
                    }
                }
            }
        }

        schedule.scheduleActionBeginning(0, new WPMarket());
    }

    public void buildDisplay() {
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public ArrayList<GenAgent> getGenAgentList() {
        return genAgentList;
    }

    public ArrayList<StorageAgent> getStorageAgentList() {
        return StorageAgentList;
    }

    /**
     * Find a {@link GenAgent} by name.
     *
     * @param name
     * @return the genagent named name or null, if the name is unknown.
     */
    public GenAgent getGenAgentByName(String name) {
        return genAgentsByName.get(name);
    }

    public ArrayList<LSEAgent> getLSEAgentList() {
        return lseAgentList;
    }

    public ArrayList<NDGenAgent> getNDGenAgentList() {
        return NDGAgentList;
    }

    public TransGrid getTransGrid() {
        return transGrid;
    }

    public ISO getISO() {
        return iso;
    }

    public int getHour() {
        return hour;
    }

    public int getDay() {
        return day;
    }

    public void setIsConverged(boolean ic) {
        isConverged = ic;
    }

    public void setIsGenActionProbabilityConverged(boolean ic) {
        isGenActionProbabilityConverged = ic;
    }

    public void setIsGenLearningResultConverged(boolean ic) {
        isGenLearningResultConverged = ic;
    }

    public void setIsGenDailyNetEarningConverged(boolean ic) {
        isGenDailyNetEarningConverged = ic;
    }

    // Get and set methods for user-setting parameters
    public int getNumNodes() {
        return numNodes;
    }

    public void setNumNodes(int nn) {
        numNodes = nn;
    }

    public int getNumBranches() {
        return numBranches;
    }

    public void setNumBranches(int nb) {
        numBranches = nb;
    }

    public int getNumGenAgents() {
        return numGenAgents;
    }

    public void setNumGenAgents(int ngen) {
        numGenAgents = ngen;
    }

    public int getNumLSEAgents() {
        return numLSEAgents;
    }

    public void setNumLSEAgents(int nlse) {
        numLSEAgents = nlse;
    }

    public int getNumNDGAgents() {
        return numNDGenAgents;
    }

    public void setNumNDGenAgents(int nndg) {
        numNDGenAgents = nndg;
    }

    public void setNumStorageAgents(int ns) {
        numStorageAgents = ns;
    }

    public double[][] getNodeData() {
        return nodeData;
    }

    public void setNodeData(double[][] nd) {
        nodeData = nd;
    }

    public double getDARRD() {
        return DARRD;
    }

    public double getDARRU() {
        return DARRU;
    }

    public void setDARRD(double rR) {
        DARRD = rR;
    }

    public void setDARRU(double rR) {
        DARRU = rR;
    }

    public double getRTRRD() {
        return RTRRD;
    }

    public double getRTRRU() {
        return RTRRU;
    }

    public void setRTRRD(double rR) {
        RTRRD = rR;
    }

    public void setRTRRU(double rR) {
        RTRRU = rR;
    }

    public CaseFileData getTestCaseConfig() {
        return testcaseConfig;
    }

    public double[][] getBranchData() {
        return branchData;
    }

    public void setBranchData(double[][] bd) {
        branchData = bd;
    }

    public double[][] getGenData() {
        return genData;
    }

    public void setGenData(double[][] gd) {
        genData = gd;
    }

    /**
     * Get the list of flags marking each generator as an alert gen or not.
     *
     * @return
     */
    public boolean[] getAlertGenMarkers() {
        return alertGenMarkers;
    }

    public void setAlertGenMarkers(boolean[] alertGenMarkers) {
        this.alertGenMarkers = alertGenMarkers;
    }

    public void setExtraGenData(Map<String, SCUCInputData> extraGenCoParams) {
        this.extraGenCoParams = extraGenCoParams;
    }

    public void setStorageData(Map<String, StorageInputData> StorageParams) {
        this.StorageParams = StorageParams;
    }

    public double[][] getLSEData() {
        return lseData;
    }

    public void setLSEData(double[][] ld) {
        lseData = ld;
    }

    public void setNDGData(double[][] ndg) {
        NDGData = ndg;
    }

    public double[][][] getLSEPriceSensitiveData() {
        return lsePriceSensitiveDemand;
    }

    public void setLSEPriceSensitiveData(double[][][] ld) {
        lsePriceSensitiveDemand = ld;
    }

    public int[][] getLSEHybridData() {
        return lseHybridDemand;
    }

    public void setLSEHybridData(int[][] ld) {
        lseHybridDemand = ld;
    }

    public int getNumHoursPerDay() {
        return numHoursPerDay;
    }

    public void setNumHoursPerDay(int nhpd) {
        numHoursPerDay = nhpd;
    }

    public double getBaseS() {
        return baseS;
    }

    public void setBaseS(double bs) {
        baseS = bs;
    }

    public void sethasStorage(double s) {
        hasStorage = s;
    }

    public void sethasNDG(double s) {
        hasNDG = s;
    }

    public void setPriceSensitiveDemandFlag(int s) {
        priceSensitiveDemandFlag = s;
    }

    public int getPriceSensitiveDemandFlag() {
        return priceSensitiveDemandFlag;
    }

    public double gethasStorage() {
        return hasStorage;
    }

    public double gethasNDG() {
        return hasNDG;
    }

    public double getBaseV() {
        return baseV;
    }

    public void setBaseV(double bv) {
        baseV = bv;
    }

    public int getWorldXSize() {
        return gridXSize;
    }

    public void setWorldXSize(int wxs) {
        gridXSize = wxs;
    }

    public int getWorldYSize() {
        return gridYSize;
    }

    public void setWorldYSize(int wys) {
        gridYSize = wys;
    }

    public double getThresholdProbability() {
        return dThresholdProbability;
    }

    public void AMESMarketSetupFromGUI(double baseS, double baseV, double[][] nodeData, double[][] branchDataData, double[][] genData, double[][] lseData, double[][] NDGData, double[][][] lsePriceSenData, int[][] lseHybridData, boolean[] gencoAlertMarkers,
            Map<String, SCUCInputData> extraGenCoParams, Map<String, StorageInputData> StorageParams, double hasStorage, double hasNDG) {

        //System.out.println("Setting up AMESMarket");
        InitDataFromGUI(baseS, baseV, nodeData, branchDataData, genData, lseData, NDGData, lsePriceSenData, lseHybridData, gencoAlertMarkers,
                extraGenCoParams, StorageParams, hasStorage, hasNDG);

        ModelController = new Controller();

        this.setController(ModelController);

        ModelController.setModel(this);

        this.addSimEventListener(ModelController);

        ModelController.setConsoleOut(false);

        ModelController.setConsoleErr(false);

    }

    public void Setup() {
        System.out.println("ModelController Setup Called?");
        ModelController.setup();
    }

    public void Start() {
        stopCode = -1;
        bCalculationEnd = false;
        ModelController.startSim();
    }

    public void Step() {
        System.out.println("\nAMESMarket.java ModelController Step \n");
        ModelController.stepSim();
    }

    public void Stop() {
        System.out.println("\nAMESMArket.java ModelController Stop: Simulation is stopped");
        ModelController.stopSim();
    }

    public void Pause() {
        System.out.println("\nAMESMarket.java ModelController Pause: Simulation is paused\n");
        ModelController.pauseSim();
    }

    public void Initialize() {
        System.out.println("\nAMESMarket.java ModelController Initialize \n");
        ModelController.beginModel();
    }

    public void ViewSettings() {
        System.out.println("\nAMESMarket.java ModelController ViewSettings \n");
        ModelController.showSettings();
    }

    public void InitDataFromGUI(double s, double v, double[][] nodeData, double[][] branchDataData, double[][] genData, double[][] lseData, double[][] NDGData, double[][][] lsePriceSenData, int[][] lseHybridData, boolean[] gencoAlertMarkers,
            Map<String, SCUCInputData> extraGenCoParams, Map<String, StorageInputData> storageParams, double hasStorage, double hasNDG) {

        //System.out.println("Initializing data... ");
        init = new INIT();

        init.setBaseS(s);
        init.setBaseV(v);
        init.setNodeDataFromGUI(nodeData);
        init.setBranchDataFromGUI(branchDataData);
        init.setGenDataFromGUI(genData);
        init.setAlertGenIdsFromGUI(gencoAlertMarkers);
        init.setLSEDataFromGUI(lseData);
        init.setNDGDataFromGUI(NDGData);
        init.setLSEPriceDataFromGUI(lsePriceSenData);
        init.setLSEHybridDataFromGUI(lseHybridData);
        init.setExtraGenCoParams(extraGenCoParams);
        //init.setReserveRequirements(rR);
        init.setStorageParams(storageParams);
        init.sethasStorage(hasStorage);
        init.sethasNDG(hasNDG);
    }

    public void InitLearningParameters(double[][] learningData) {
        int iGen = learningData.length;
        genLearningParameters = new double[iGen][12];

        for (int i = 0; i < iGen; i++) {
            for (int j = 0; j < 12; j++) {
                genLearningParameters[i][j] = learningData[i][j];
            }
        }
    }

    public void InitSimulationParameters(boolean FNCSActiveVal, int RTOPDur, int iMax, double DARD, double DARU, double RTRD, double RTRU, boolean bMax, double dThreshold, boolean bThresh, double dEarningThreshold, boolean bEarningThresh, int iEarningStart, int iEarningLength, int iStart, int iLength, double dCheck, boolean bCheck, int iLearnStart, int iLearnLength, double dLearnCheck, boolean bLearnCheck, double dGCap, double dLseCap, long lRandom, int PSDemandFlag, CaseFileData cfd) {
        //System.out.println("Initializing Simulation Parameters");
        FNCSActive = FNCSActiveVal;
        DAY_MAX = iMax;
        M = RTOPDur;
        bMaximumDay = bMax;
        dThresholdProbability = dThreshold;
        bThreshold = bThresh;
        dDailyNetEarningThreshold = dEarningThreshold;
        bDailyNetEarningThreshold = bEarningThresh;
        iDailyNetEarningStartDay = iEarningStart;
        iDailyNetEarningDayLength = iEarningLength;
        iStartDay = iStart;
        iCheckDayLength = iLength;
        dActionProbability = dCheck;
        bActionProbabilityCheck = bCheck;
        iLearningCheckStartDay = iLearnStart;
        iLearningCheckDayLength = iLearnLength;
        dLearningCheckDifference = dLearnCheck;
        bLearningCheck = bLearnCheck;
        dGenPriceCap = dGCap;
        dLSEPriceCap = dLseCap;
        priceSensitiveDemandFlag = PSDemandFlag;
        DARRD = DARD;
        DARRU = DARU;
        RTRRD = RTRD;
        RTRRU = RTRU;
        RANDOM_SEED = (int) lRandom;
        testcaseConfig = cfd;
        NumberOfReserveZones = cfd.NumberOfReserveZones;
    }

    public void SetRandomSeed(long lSeed) {
        RANDOM_SEED = (int) lSeed;
    }

    public void addGenAgentSupplyOfferByDay(double[][] supplyOffer) {
        int iRow = supplyOffer.length;
        int iCol = supplyOffer[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = supplyOffer[i][j];
            }
        }

        genAgentSupplyOfferByDay.add(newObject);
    }

    public ArrayList getGenAgentSupplyOfferByDay() {
        return genAgentSupplyOfferByDay;
    }

    public void addLSEAgenPriceSensitiveDemandByDay(double[][] LSEPDemand) {
        int iRow = LSEPDemand.length;
        int iCol = LSEPDemand[0].length;
        for (int i = 0; i < iRow; i++) {
            if (iCol > LSEPDemand[i].length) {
                iCol = LSEPDemand[i].length;
            }
        }

        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = LSEPDemand[i][j];
            }
        }

        lseAgentPriceSensitiveDemandByDay.add(newObject);
    }

    public ArrayList getLSEAgenPriceSensitiveDemandByDay() {
        return lseAgentPriceSensitiveDemandByDay;
    }

    public void addLSEAgentSurplusByDay(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        lseAgentSurplusByDay.add(newObject);
    }

    public ArrayList getLSEAgentSurplusByDay() {
        return lseAgentSurplusByDay;
    }

    public void addGenAgentProfitAndNetGainByDay(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        genAgentProfitAndNetGainByDay.add(newObject);
    }

    public ArrayList getGenAgentProfitAndNetGainByDay() {
        return genAgentProfitAndNetGainByDay;
    }

    public void addGenAgentActionPropensityAndProbilityByDay(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        genAgentActionPropensityAndProbilityByDay.add(newObject);
    }

    public ArrayList getGenAgentActionPropensityAndProbilityByDay() {
        return genAgentActionPropensityAndProbilityByDay;
    }

    public void addLSEAgentPriceSensitiveDemandWithTrueCost(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        lseAgentPriceSensitiveDemandWithTrueCost.add(newObject);
    }

    public ArrayList getLSEAgentPriceSensitiveDemandWithTrueCost() {
        return lseAgentPriceSensitiveDemandWithTrueCost;
    }

    public void addGenAgentDispatchWithTrueCost(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        genAgentDispatchWithTrueCost.add(newObject);
    }

    public ArrayList getGenAgentDispatchWithTrueCost() {
        return genAgentDispatchWithTrueCost;
    }

    public void addGenAgentProfitAndNetGainWithTrueCost(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        genAgentProfitAndNetGainWithTrueCost.add(newObject);
    }

    public ArrayList getGenAgentProfitAndNetGainWithTrueCost() {
        return genAgentProfitAndNetGainWithTrueCost;
    }

    public void addLSEAgentSurplusWithTrueCost(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        lseAgentSurplusWithTrueCost.add(newObject);
    }

    public ArrayList getLSEAgentSurplusWithTrueCost() {
        return lseAgentSurplusWithTrueCost;
    }

    public void addGenAgentDispatchByDay(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        genAgentDispatchByDay.add(newObject);
    }

    public ArrayList getGenAgentDispatchByDay() {
        return genAgentDispatchByDay;
    }

    public void addGenAgentRealTimeDispatchByInterval(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        genAgentRealTimeDispatchByInterval.add(newObject);
    }

    public ArrayList getGenAgentRealTimeDispatchByInterval() {
        return genAgentRealTimeDispatchByInterval;
    }

    public void addBranchFlowByDay(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        branchFlowByDay.add(newObject);
    }

    public ArrayList getBranchFlowByDay() {
        return branchFlowByDay;
    }

    public void addLMPByDay(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        LMPByDay.add(newObject);
    }

    public ArrayList getLMPByDay() {
        return LMPByDay;
    }

    public void addActualStartupCostsByDay(double[][] costs) {
        if (costs == null) {
            throw new IllegalArgumentException();
        }
        double[][] costCopy = new double[costs.length][];
        for (int i = 0; i < costs.length; i++) {
            costCopy[i] = Arrays.copyOf(costs[i], costs[i].length);
        }

        startupCostsByDay.add(costCopy);
    }

    public void addActualProductionCostsByDay(double[][] costs) {
        if (costs == null) {
            throw new IllegalArgumentException();
        }
        double[][] costCopy = new double[costs.length][];
        for (int i = 0; i < costs.length; i++) {
            costCopy[i] = Arrays.copyOf(costs[i], costs[i].length);
        }

        productionCostsByDay.add(costCopy);
    }

    public void addActualShutdownCostsByDay(double[][] costs) {
        if (costs == null) {
            throw new IllegalArgumentException();
        }
        double[][] costCopy = new double[costs.length][];
        for (int i = 0; i < costs.length; i++) {
            costCopy[i] = Arrays.copyOf(costs[i], costs[i].length);
        }

        shutdownCostsByDay.add(costCopy);
    }

    public void addRealTimeLMPByInterval(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        realTimeLMPByInterval.add(newObject);
    }

//    public void addRealTimeLMPByDay(double[] object) {
//        int iRow = object.length;
//        //int iCol = object[0].length;
//        double[] newObject = new double[iRow];
//        for (int i = 0; i < iRow; i++) {
//                newObject[i] = object[i];
//        }
//
//        realTimeLMPByDay.add(newObject);
//    }
    public ArrayList getRealTimeLMPByInterval() {
        return realTimeLMPByInterval;
    }

    public void addRealTimeBranchFlowByDay(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        realTimeBranchFlowByDay.add(newObject);
    }

    public ArrayList getRealTimeBranchFlowByDay() {
        return realTimeBranchFlowByDay;
    }

    public void addLMPWithTrueCost(double[][] object) {
        int iRow = object.length;
        int iCol = object[0].length;
        double[][] newObject = new double[iRow][iCol];
        for (int i = 0; i < iRow; i++) {
            for (int j = 0; j < iCol; j++) {
                newObject[i][j] = object[i][j];
            }
        }

        LMPWithTrueCost.add(newObject);
    }

    public ArrayList getLMPWithTrueCost() {
        return LMPWithTrueCost;
    }

    public void addGenActions(double[][] object) {
        int iRow = object.length;

        double[][] newObject = new double[iRow][];
        for (int i = 0; i < iRow; i++) {
            int iCol = object[i].length;

            double[] newCol = new double[iCol];
            for (int j = 0; j < iCol; j++) {
                newCol[j] = object[i][j];
            }

            newObject[i] = newCol;
        }

        lastDayGenActions.add(newObject);
    }

    public ArrayList getGenActions() {
        return lastDayGenActions;
    }

    public boolean isDeleteIntermediateFiles() {
        return deleteIntermediateFiles;
    }

    public boolean IfCalculationEnd() {
        return bCalculationEnd;
    }

    public String SolutionStatus() {
        return SolutionStatus;
    }

    public int getStopCode() {
        return stopCode;
    }

    public long getTimeGranted() {
        return time_granted;
    }

    public ArrayList<double[][]> getActualStartupCostsByDay() {
        return startupCostsByDay;
    }

    public ArrayList<double[][]> getActualShutdownCostsByDay() {
        return shutdownCostsByDay;
    }

    public ArrayList<double[][]> getActualProductionCostsByDay() {
        return productionCostsByDay;
    }

    //constructor
    public AMESMarket(boolean deleteIntermediateFiles) {
        genAgentSupplyOfferByDay = new ArrayList();
        lseAgentPriceSensitiveDemandByDay = new ArrayList();
        lseAgentPriceSensitiveDemandWithTrueCost = new ArrayList();
        lseAgentSurplusByDay = new ArrayList();
        lseAgentSurplusWithTrueCost = new ArrayList();
        genAgentProfitAndNetGainByDay = new ArrayList();
        genAgentActionPropensityAndProbilityByDay = new ArrayList();
        genAgentProfitAndNetGainWithTrueCost = new ArrayList();
        genAgentDispatchWithTrueCost = new ArrayList();
        genAgentDispatchByDay = new ArrayList();
        genAgentRealTimeDispatchByInterval = new ArrayList();
        branchFlowByDay = new ArrayList();
        LMPByDay = new ArrayList();
        realTimeLMPByInterval = new ArrayList();
        realTimeBranchFlowByDay = new ArrayList();
        LMPWithTrueCost = new ArrayList();
        lastDayGenActions = new ArrayList();
        this.deleteIntermediateFiles = deleteIntermediateFiles;
    }

}
