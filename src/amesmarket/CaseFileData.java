
package amesmarket;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;


import java.util.Arrays;

/**
 * Configuration parameters.
 *
 * Parameters are public to make adapting the existing code quicker.
 *
 * Adapted from {@link AMESGUIFrame.AMESFrame}
 *
 */
public class CaseFileData {

    /**
     * Where the LSE data comes from. Either comes from the load case, or
     * directly specified in the TestCase file. One of
     * {@link #LSE_DEMAND_TEST_CASE} or {@link #LSE_DEMAND_LOAD_CASE}.
     */
    private int lseDemandSource;

    public Object[][] nodeData;
    public Object[][] branchData;
    public GenData[] genData;
    public Object[][] lse3SecData;  // 3-sectional 8-hour LSE data
    public Object[][] lseData;      // Combine 3-sectional to 24-hour LSE data
    public Object[][] lseSec1Data;  // First 8-hour LSE data
    public Object[][] lseSec2Data;  // Second 8-hour LSE data
    public Object[][] lseSec3Data;  // Third 8-hour LSE data
    public Object[][][] lsePriceSensitiveDemand;
    public Object[][] lseHybridDemand;

    public int iNodeData;
    public int iBranchData;
    public int iGenData;
    public int iLSEData;
    public int iNDGData;
    public boolean bPriceSensitiveDemand;
    public boolean bHybridDemand;
    public boolean isPU;
    public double baseS;
    public double baseV;
    public double DARRD;
    public double DARRU;
    public double RTRRD;
    public double RTRRU;
    public int NumberOfReserveZones;

    public Object[][] NDGData;      // Combine 3-sectional to 24-hour NDG data
    public Object[][] NDGSec1Data;  // First 8-hour NDG data
    public Object[][] NDGSec2Data;  // Second 8-hour NDG data
    public Object[][] NDGSec3Data;  // Third 8-hour NDG data

    // Learning and action domain parameters
    public double Default_Cooling;
    public double Default_Experimentation;
    public double Default_InitPropensity;
    public int Default_M1;
    public int Default_M2;
    public int Default_M3;
    public double Default_RI_MAX_Lower;
    public double Default_RI_MAX_Upper;
    public double Default_RI_MIN_C;
    public double Default_Recency;
    public double Default_SlopeStart;
    public int Default_iSCostExcludedFlag;
    public double Cooling;
    public double Experimentation;
    public double InitPropensity;
    public int M1;
    public int M2;
    public int M3;
    public double RI_MAX_Lower;
    public double RI_MAX_Upper;
    public double RI_MIN_C;
    public double Recency;
    public double SlopeStart;
    public int iSCostExcludedFlag;
    public double[][] genLearningData;
    public boolean bGenLearningDataSet = false;

    public long RandomSeed;
    public boolean FNCSActive;
    public String Solver;
    public double DATDur;
    public int RTOPDur;
    public int RTKDur;
    public int BalPenPos;
    public int BalPenNeg;
    public int iMaxDay;
    public double dThresholdProbability;
    public double hasStorage;
    public double hasNDG;
    public int priceSensitiveDemandFlag;
    
    public double dDailyNetEarningThreshold;
    public double dGenPriceCap;
    public double dLSEPriceCap;
    public int iStartDay;
    public int iCheckDayLength;
    public double dActionProbability;
    public int iLearningCheckStartDay;
    public int iLearningCheckDayLength;
    public double dLearningCheckDifference;
    public int iDailyNetEarningStartDay;
    public int iDailyNetEarningDayLength;
    public boolean bMaximumDay = true;
    public boolean bThreshold = true;
    public boolean bDailyNetEarningThreshold = false;
    public boolean bActionProbabilityCheck = false;
    public boolean bLearningCheck = false;
    private boolean hasGenLearningData = false;
    private boolean hasReserveZoneData = false;

    /**
     * Name of the file that describes the load scenarios. Must be specified by
     * the TestCase file.
     */
    public String loadCaseControlFile;
    /**
     * The capacity reserve margin. Used to scale 'real' input data to
     * generation capacity of the grid. Stored a decimal [-1, 1] not as a
     * percentage, [-100, 100]. See
     * {@link DefaultLPCProvider#scaleLoadProfileCollections(double, double)}.
     * Defaults to 10, unless set in the TestCase file.
     */
    public double capacityMargin;
    private ArrayList<String> canaryGenCos;

    private final Map<String, SCUCInputData> scucData;
    private final Map<String, ZonalData> ZoneData;
    private final Map<String, StorageInputData> storageData;

    /**
     * Store an association between zone names and the array index used to model
     * that zone in the bulk of the program.
     */


    private final Map<String, Integer> coldStartTime;
    private final Map<String, Double> coldStartUpCosts;
    private final Map<String, Double> hotStartUpCosts;
    private final Map<String, Double> ShutDownCosts;

    private final Map<String, String> fuelType;


    public CaseFileData() {
        baseS = 100;
        baseV = 10;
        RandomSeed = 695672061;
        Solver = "cplex";
        DATDur = 1; 
        RTOPDur = 60;
        RTKDur = 5;
        BalPenPos = 100000;
        BalPenNeg = 100000;
        iMaxDay = 5;
        dThresholdProbability = 0.999;
        dDailyNetEarningThreshold = 10.0;
        dGenPriceCap = 1000.0;
        dLSEPriceCap = 0.0;
        iStartDay = 1;
        iCheckDayLength = 5;
        dActionProbability = 0.001;
        iLearningCheckStartDay = 1;
        iLearningCheckDayLength = 5;
        dLearningCheckDifference = 0.001;
        iDailyNetEarningStartDay = 1;
        iDailyNetEarningDayLength = 5;
        DARRD = 0.05;
        DARRU = 0.05;
        RTRRD = 0.01;
        RTRRU = 0.01; 
        NumberOfReserveZones = 1;
        hasStorage = 0;
        hasNDG = 0;
        FNCSActive = false;

        lseDemandSource = LSE_DEMAND_TEST_CASE;
        loadCaseControlFile = "DATA/ControlFile.dat";



        capacityMargin = .10;
        canaryGenCos = new ArrayList<String>();

        scucData = new HashMap<String, CaseFileData.SCUCInputData>();
        ZoneData = new HashMap<>();
        storageData = new HashMap<String, CaseFileData.StorageInputData>();

        coldStartTime = new HashMap<String, Integer>();
        ShutDownCosts = new HashMap<String, Double>();
        coldStartUpCosts = new HashMap<String, Double>();
        hotStartUpCosts = new HashMap<String, Double>();

        fuelType = new HashMap<String, String>();
    }

    /**
     * @param should be one of the LSE_DEMAND_* constants.
     */
    public void setLSEDemandSource(int lseDemandSource) {
        this.lseDemandSource = lseDemandSource;
    }

    /**
     * @return one of {@link #LSE_DEMAND_TEST_CASE} or
     * {@link #LSE_DEMAND_LOAD_CASE}
     */
    public int getLSEDemandSource() {
        return lseDemandSource;
    }

    public void setCanaryGenCo(ArrayList<String> alertGenCos) {
        this.canaryGenCos.addAll(alertGenCos);
    }


    public void markCanaryGenCos() {
        if (canaryGenCos.size() == 0) {
            return;
        }

        //quick-to-implement algorithm. Iterate over the entire
        //list until we find the genco with the same name.
        //If this turns into a bottle neck, sort the genco list
        //by name into a new list and search for the names.
        //Should probably leave the original list in order.
        //Not sure if there is an implied order between the
        //the list of genco's and how the rest of the network
        //gets connected up.
        for (String genName : canaryGenCos) {
            boolean foundCanary = false;
            for (int g = 0; g < genData.length && !foundCanary; g++) {
                if (genData[g].name.equals(genName)) {
                    genData[g].isCanary = true;
                    foundCanary = true;
                }
            }
            if (!foundCanary) {
                System.err.println(String.format(
                        "[WARNING] AlertGen %s Not Found", genName));
            }
        }
    }

    /**
     * Check to see if any of the gencos is missing SCUC data. If so, generate a
     * default set of parameters.
     *
     * @param testConf
     */
    public void ensureSCUCData() {
        if (genData == null) {
            return;
        }
        for (GenData gd : genData) {
            if (scucData.get(gd.name) == null) {
                scucData.put(gd.name, new SCUCInputData(gd.PMax, 1, 0, 0, 999999, 999999, 999999, 999999)); //, 1, 1)); 
            }
        }
    }

    

    /**
     * Helper method to prevent NPEs where the TestCase uses a LoadCase for the
     * demand source and there is no fixed demand in the TestCase file.
     */
    public void ensureLSEData() {
        //don't modify the data if the LSE demand is supposed
        //to be in the TestCase.
        if (lseDemandSource == LSE_DEMAND_TEST_CASE || lseData != null) {
            return;
        }

        iLSEData = iNodeData;
        //System.out.println("iLSEData: " + iLSEData);
        lseSec1Data = new Object[iLSEData][11];
        lseSec2Data = new Object[iLSEData][11];
        lseSec3Data = new Object[iLSEData][11];
        lseData = new Object[iLSEData][27];

        for (int z = 0; z < lseData.length; z++) {
            lseData[z][0] = "LSE" + (z + 1); //NAME
            lseData[z][1] = (z + 1);         //ID
            lseData[z][2] = (z + 1);         //At bus
            //hourly demand
            for (int h = 3; h < lseData[z].length; h++) {
                lseData[z][h] = 0; //dummy no demand data
            }
        }

    }

    /**
     * Generate some dummy price sensitive demand date, if not in the test case.
     * Make to call after {@link #ensureLSEData()}, or may crash.
     */
    public void ensureLSEPriceSenstiveDemandData() {
        //don't modify the data if the LSE demand is supposed
        //to be in the TestCase.
//        if (lseDemandSource == LSE_DEMAND_TEST_CASE
//                || lsePriceSensitiveDemand != null) {    
        if (lsePriceSensitiveDemand != null) {
            return;
        }

        final int numHours = 24;
        final int numCols = 9;

        //iLSEData = ; //zoneIndexMap.getNumZones();
        //System.out.println("iLSEData: "+ iLSEData);
        lsePriceSensitiveDemand = new Object[iLSEData][numHours][numCols];

        for (int l = 0; l < iLSEData; l++) {
            for (int h = 0; h < 24; h++) {
                lsePriceSensitiveDemand[l][h][0] = lseData[l][0]; //NAME
                lsePriceSensitiveDemand[l][h][1] = lseData[l][1]; //ID
                lsePriceSensitiveDemand[l][h][2] = lseData[l][2]; //BUS
                lsePriceSensitiveDemand[l][h][3] = h;             //HOUR
                lsePriceSensitiveDemand[l][h][4] = 0;             //d
                lsePriceSensitiveDemand[l][h][5] = 0;             //e
                lsePriceSensitiveDemand[l][h][6] = 0;             //f
                lsePriceSensitiveDemand[l][h][7] = 0;             //SLMax
                lsePriceSensitiveDemand[l][h][8] = 0;             //NS
            }
        }
        //System.out.println("lsePriceSensitiveDemand: "+ (lsePriceSensitiveDemand[1][3][6]));
    }


    public void ensureNDGData() {
        
        //don't modify the data if the LSE demand is supposed
        //to be in the TestCase.

        if (NDGData != null) {
            return;
        }

        final int numHours = 24;
        final int numCols = 7;

        //iLSEData = ; //zoneIndexMap.getNumZones();
        //System.out.println("iLSEData: "+ iLSEData);
        NDGData = new Object[iLSEData][27];

        for (int l = 0; l < iLSEData; l++) {
            NDGData[l][0] = lseData[l][0]; //NAME
            NDGData[l][1] = lseData[l][1]; //ID
            NDGData[l][2] = lseData[l][2]; //BUS
            for (int h = 0; h < 24; h++) {
                NDGData[l][h + 3] = 0; //flag - default value is kept 1 i.e. fixed demand
            }
        }
    }

        public void ensureLSEHybridDemandData() {
        
        //don't modify the data if the LSE demand is supposed
        //to be in the TestCase.

        if (lseHybridDemand != null) {
            return;
        }

        final int numHours = 24;
        final int numCols = 7;

        //iLSEData = ; //zoneIndexMap.getNumZones();
        //System.out.println("iLSEData: "+ iLSEData);
        lseHybridDemand = new Object[iLSEData][27];

        for (int l = 0; l < iLSEData; l++) {
            lseHybridDemand[l][0] = lseData[l][0]; //NAME
            lseHybridDemand[l][1] = lseData[l][1]; //ID
            lseHybridDemand[l][2] = lseData[l][2]; //BUS
            for (int h = 0; h < 24; h++) {
                lseHybridDemand[l][h + 3] = 1; //flag - default value is kept 1 i.e. fixed demand
            }
        }
    }
        
    ///////////////////////CONSTANTS////////////////////////////////////////////
    /**
     * Use the LSE demand data in the TestCase file.
     */
    public static final int LSE_DEMAND_TEST_CASE = 1;
    /**
     * Use the LoadCase as the source of LSE demand data.
     */
    public static final int LSE_DEMAND_LOAD_CASE = 2;
    ///////////////////////END CONSTANTS///////////////////////////////////////

    /**
     * Model the GenData section of the input file.

     */
    public static class GenData {

        /**
         * @param name
         * @param id
         * @param atBus
         * @param sCost
         * @param a
         * @param b
         * @param c
         * @param capL
         * @param capU
         * @param NS
         * @param initMoney
         */
        public GenData(String name, int id, int atBus, double sCost, double a, double b,
                double c, int NS, double PMin, double PMax, double initMoney) {
            this.name = name;
            this.id = id;
            this.atBus = atBus;
            this.sCost = sCost;
            this.a = a;
            this.b = b;
            this.c = c;
            this.PMin = PMin;
            this.PMax = PMax;
            this.NS = NS;
            this.initMoney = initMoney;
            //Assume all GenCos are 'standard' generators
            //until one is marked explicitly as a canary.
            this.isCanary = false;
        }

        /**
         * Creates a copy of the parameters to a double array.
         *
         * Utility method to make loading data into the AMESMarket easy.
         *
         * @return
         */
        public Object[] asArray() {
            return new Object[]{
                name, id, atBus, sCost, a, b, c, NS, PMin, PMax, initMoney, isCanary
            };
        }

        public final String name;
        public final int id;
        public final int atBus;
        public final double sCost;
        public final double a;
        public final double b;
        public final double c;
        public final int NS;
        public final double PMin;
        public final double PMax;
        public final double initMoney;
        /**
         * Whether or not the generator is a 'canary' generator. Like the canary
         * in a coal mine, canary generators indicate a problem with the grid.
         * Canary generators provide a means to ensure a load profile can be
         * met, since AMES does not yet handle reserves/reserve markets.
         */
        //not final because the list of 'canaries' get 'matched up'
        //with generators until after the entire file is read.
        public boolean isCanary;
    }

    /**
     * Adjusts the control file path as relative to the TestCase file, if
     * needed.
     *
     * The path is adjusted if these conditions hold:
     * <ol>
     * <li>{@link #testCaseFile} is not null</li>
     * <li>The path for the control file is not null</li>
     * </ol>
     *
     * @param root root file location. Does nothing if the root is null.
     */
    public void adjustLoadControlFilePath(File root) {
        if (root == null) {
            return; //ignore a null root.
        }
        File loadCaseControl = new File(loadCaseControlFile);
        if (!loadCaseControl.isAbsolute()) {
            File rootDir = root.getParentFile();
            if (rootDir != null) {
                loadCaseControlFile = new File(rootDir, loadCaseControlFile).getPath();
            }
        }
    }

    /**
     * Put the scuc data for a single genco.
     *
     * @param gencoName
     * @param powerT0
     * @param unitOnT0State
     * @param minUpTime
     * @param minDownTime
     * @param NominalRampUp
     * @param nominalRampDown
     * @param StartupRampLim
     * @param ShutdownRampLim
     * @param schedule1
     * @param schedule2
     */
    public void putScucData(String gencoName, double powerT0, int unitOnT0State,
            int minUpTime, int minDownTime, double nominalRampUp, double nominalRampDown,
            double startupRampLim, double shutdownRampLim) { //, int schedule1, int schedule2) {
        SCUCInputData sid = new SCUCInputData(powerT0, unitOnT0State, minUpTime,
                minDownTime, nominalRampUp, nominalRampDown, startupRampLim,
                shutdownRampLim); //, schedule1, schedule2);

        scucData.put(gencoName, sid);
    }

    public void putZoneData(String ZoneName, int[] Buses, double ZonalDARRD,
            double ZonalDARRU, double ZonalRTRRD, double ZonalRTRRU) {
        ZonalData z = new ZonalData(Buses, ZonalDARRD, ZonalDARRU, ZonalRTRRD, ZonalRTRRU);

        ZoneData.put(ZoneName, z);
    }
    /**
     * Put the Storage data for a single Storage Unit.
     *
     * @param ID
     * @param atBus
     * @param EndPointSoc
     * @param MaximumEnergy
     * @param NominalRampDownInput
     * @param NominalRampUpInput
     * @param NominalRampDownOutput
     * @param NominalRampUpOutput
     * @param MaximumPowerInput
     * @param MinimumPowerInput
     * @param MaximumPowerOutput
     * @param MinimumPowerOutput
     * @param MinimumSoc
     * @param EfficiencyEnergy
     */
    public void putStorageData(int ID, int atBus, double EndPointSoc, double MaximumEnergy, double NominalRampDownInput,
            double NominalRampUpInput, double NominalRampDownOutput, double NominalRampUpOutput,
            double MaximumPowerInput, double MinimumPowerInput, double MaximumPowerOutput,
            double MinimumPowerOutput, double MinimumSoc, double EfficiencyEnergy) {
        StorageInputData sid = new StorageInputData(ID, atBus, EndPointSoc, MaximumEnergy, NominalRampDownInput,
                NominalRampUpInput, NominalRampDownOutput, NominalRampUpOutput,
                MaximumPowerInput, MinimumPowerInput, MaximumPowerOutput,
                MinimumPowerOutput, MinimumSoc, EfficiencyEnergy);
        storageData.put(Integer.toString(ID), sid);
    }



    public void addFuelType(String genCo, String fuel) {
        fuelType.put(genCo, fuel);
    }


    public void addColdStartTime(String genCo, int hours) {
        coldStartTime.put(genCo, hours);
    }

    public void addColdStartUpCost(String genCo, double cost) {
        coldStartUpCosts.put(genCo, cost);
    }

    public void addHotStartUpCost(String genCo, double cost) {
        hotStartUpCosts.put(genCo, cost);
    }

    public void addShutDownCost(String genCo, double cost) {
        ShutDownCosts.put(genCo, cost);
    }

    public boolean hasFuelType(String genCo) {
        return fuelType.containsKey(genCo);
    }


    public boolean hasColdStartTime(String genCo) {
        return coldStartTime.containsKey(genCo);
    }

    public boolean hasColdStartUpCost(String genCo) {
        return coldStartUpCosts.containsKey(genCo);
    }

    public boolean hasHotStartUpCost(String genCo) {
        return hotStartUpCosts.containsKey(genCo);
    }

    public boolean hasShutDownCost(String genCo) {
        return ShutDownCosts.containsKey(genCo);
    }

    public double getShutDownCostForGen(String genCo) {
        Double d = ShutDownCosts.get(genCo);
        if (d == null) {
            throw new IllegalArgumentException("Unknown key " + genCo);
        } else {
            return d.doubleValue();
        }
    }

    public int getColdStartTimeForGen(String genCo) {
        Integer d = coldStartTime.get(genCo);
        if (d == null) {
            throw new IllegalArgumentException("Unknown key " + genCo);
        } else {
            return d;
        }
    }

    public double getColdStartUpCostForGen(String genCo) {
        Double d = coldStartUpCosts.get(genCo);
        if (d == null) {
            throw new IllegalArgumentException("Unknown key " + genCo);
        } else {
            return d.doubleValue();
        }
    }

    public double getHotStartUpCostForGen(String genCo) {
        Double d = hotStartUpCosts.get(genCo);
        if (d == null) {
            throw new IllegalArgumentException("Unknown key " + genCo);
        } else {
            return d.doubleValue();
        }
    }

    public String getFuelTypeForGen(String genCo) {
        String d = fuelType.get(genCo);
        if (d == null) {
            throw new IllegalArgumentException("Unknown key " + genCo);
        } else {
            return d;
        }
    }



    /**
     * Get a reference to the map storing the input data.
     *
     * This is useful to move the entire set of parameters around at once.
     *
     * @return
     */
    public Map<String, SCUCInputData> getSCUCInputData() {
        return scucData;
    }
    
    public Map<String, ZonalData> getZonalData() {
        return ZoneData;
    }
    /**
     * Get a reference to the map storing the input data.
     *
     * This is useful to move the entire set of parameters around at once.
     *
     * @return
     */
    public Map<String, StorageInputData> getStorageInputData() {
        return storageData;
    }

    /**
     * Mark if the TestCase included learning data for the gencos.
     *
     * @param b
     */
    public void setHasGenLearningData(boolean b) {
        this.hasGenLearningData = b;
    }

    public void setHasReserveZoneData(boolean b) {
        this.hasReserveZoneData = b;
    }

        public static class ZonalData {

        private int[] Buses;
        private double ZonalDARRD;
        private double ZonalDARRU;
        private double ZonalRTRRD;
        private double ZonalRTRRU;
        
        public ZonalData(int[] Buses, double ZonalDARRD, double ZonalDARRU, double ZonalRTRRD, double ZonalRTRRU) {
            this.Buses = Buses;
            this.ZonalDARRD = ZonalDARRD;
            this.ZonalDARRU = ZonalDARRU;
            this.ZonalRTRRD = ZonalRTRRD;
            this.ZonalRTRRU = ZonalRTRRU;
        }
        
        public int[] getBuses() {
            return Buses;
        }
        
        public double getZonalDARRD() {
            return ZonalDARRD;
        }
        
        public double getZonalDARRU() {
            return ZonalDARRU;
        }
        
        public double getZonalRTRRD() {
            return ZonalRTRRD;
        }
        
        public double getZonalRTRRU() {
            return ZonalRTRRU;
        }
        
        }
    /**
     * {@link #hasGenLearningData()}.
     */
    public boolean hasGenLearningData() {
        return hasGenLearningData;
    }

    public boolean hasReserveZoneData() {
        return hasReserveZoneData;
    }
    /**
     * Struct to model the scuc input data for a single GenCo.
     *
     *
     */
    public static class SCUCInputData {

        private double PowerT0;
        private int UnitOnT0State;
        private int MinUpTime;
        private int MinDownTime;
        private double NominalRampUp;
        private double NominalRampDown;
        private double StartupRampLim;
        private double ShutdownRampLim;
        //private int schedule1;
        //private int schedule2;

        /**
         * @param powerT0
         * @param unitOnT0State
         * @param MinUpTime
         * @param MinDownTime
         * @param nominalRampUp
         * @param nominalRampDown
         * @param startupRampLim
         * @param shutdownRampLim
         * @param schedule1
         * @param schedule2
         */
        public SCUCInputData(double powerT0, int unitOnT0State, int minUpTime,
                int minDownTime, double nominalRampUp, double nominalRampDown,
                double startupRampLim, double shutdownRampLim) { //, int schedule1, int schedule2) {
            this.PowerT0 = powerT0;
            this.UnitOnT0State = unitOnT0State;
            this.MinUpTime = minUpTime;
            this.MinDownTime = minDownTime;
            this.NominalRampUp = nominalRampUp;
            this.NominalRampDown = nominalRampDown;
            this.StartupRampLim = startupRampLim;
            this.ShutdownRampLim = shutdownRampLim;
            //this.schedule1 = schedule1;
            //this.schedule2 = schedule2;
        }

        /**
         * @return the powerT0
         */
        public double getPowerT0() {
            return PowerT0;
        }

        /**
         * @param powerT0 the powerT0 to set
         */
        public void setPowerT0(double powerT0) {
            this.PowerT0 = powerT0;
        }

        /**
         * @return the unitOnT0
         */
        public int getUnitOnT0State() {
            return UnitOnT0State;
        }

        /**
         * @param unitOnT0State the unitOnT0State to set
         */
        public void setUnitOnT0State(int unitOnT0State) {
            this.UnitOnT0State = unitOnT0State;
        }

        /**
         * @return the minUpTime
         */
        public int getMinUpTime() {
            return MinUpTime;
        }

        /**
         * @param minUpTime the minUpTime to set
         */
        public void setMinUpTime(int minUpTime) {
            this.MinUpTime = minUpTime;
        }

        /**
         * @return the minDownTime
         */
        public int getMinDownTime() {
            return MinDownTime;
        }

        /**
         * @param minDownTime the minDownTime to set
         */
        public void setMinDownTime(int minDownTime) {
            this.MinDownTime = minDownTime;
        }

        /**
         * @return the nominalRampUp
         */
        public double getNominalRampUp() {
            return NominalRampUp;
        }

        /**
         * @param nominalRampUp the nominalRampUp to set
         */
        public void setNominalRampUp(double nominalRampUp) {
            this.NominalRampUp = nominalRampUp;
        }

        /**
         * @return the nominalRampDown
         */
        public double getNominalRampDown() {
            return NominalRampDown;
        }

        /**
         * @param nominalRampDown the nominalRampDown to set
         */
        public void setNominalRampDown(double nominalRampDown) {
            this.NominalRampDown = nominalRampDown;
        }

        /**
         * @return the startupRampLim
         */
        public double getStartupRampLim() {
            return StartupRampLim;
        }

        /**
         * @param startupRampLim the startupRampLim to set
         */
        public void setStartupRampLim(double startupRampLim) {
            this.StartupRampLim = startupRampLim;
        }

        /**
         * @return the shutdownRampLim
         */
        public double getShutdownRampLim() {
            return ShutdownRampLim;
        }

        /**
         * @param shutdownRampLim the shutdownRampLim to set
         */
        public void setShutdownRampLim(double shutdownRampLim) {
            this.ShutdownRampLim = shutdownRampLim;
        }


    }

    /**
     * Struct to model the Storage input data for a single GenCo.
     *
     */
    public static class StorageInputData {

        private int ID;
        private int atBus;
        private double EndPointSoc;
        private double MaximumEnergy;
        private double NominalRampDownInput;
        private double NominalRampUpInput;
        private double NominalRampDownOutput;
        private double NominalRampUpOutput;
        private double MaximumPowerInput;
        private double MinimumPowerInput;
        private double MaximumPowerOutput;
        private double MinimumPowerOutput;
        private double MinimumSoc;
        private double EfficiencyEnergy;

        /**
         * @param ID
         * @param atBus
         * @param EndPointSoc
         * @param MaximumEnergy
         * @param NominalRampDownInput
         * @param NominalRampUpInput
         * @param NominalRampDownOutput
         * @param NominalRampUpOutput
         * @param MaximumPowerInput
         * @param MinimumPowerInput
         * @param MaximumPowerOutput
         * @param MinimumPowerOutput
         * @param MinimumSoc
         * @param EfficiencyEnergy
         */
        public StorageInputData(int ID, int atBus, double EndPointSoc, double MaximumEnergy, double NominalRampDownInput,
                double NominalRampUpInput, double NominalRampDownOutput, double NominalRampUpOutput,
                double MaximumPowerInput, double MinimumPowerInput, double MaximumPowerOutput,
                double MinimumPowerOutput, double MinimumSoc, double EfficiencyEnergy) {
            this.ID = ID;
            this.atBus = atBus;
            this.EndPointSoc = EndPointSoc;
            this.MaximumEnergy = MaximumEnergy;
            this.NominalRampDownInput = NominalRampDownInput;
            this.NominalRampUpInput = NominalRampUpInput;
            this.NominalRampDownOutput = NominalRampDownOutput;
            this.NominalRampUpOutput = NominalRampUpOutput;
            this.MaximumPowerInput = MaximumPowerInput;
            this.MinimumPowerInput = MinimumPowerInput;
            this.MaximumPowerOutput = MaximumPowerOutput;
            this.MinimumPowerOutput = MinimumPowerOutput;
            this.MinimumSoc = MinimumSoc;
            this.EfficiencyEnergy = EfficiencyEnergy;
        }

        /**
         * @return the ID
         */
        public int getID() {
            return this.ID;
        }

        /**
         * @param ID the ID to set
         */
        public void setID(int ID) {
            this.ID = ID;
        }

        /**
         * @return the atBus
         */
        public int getatBus() {
            return this.atBus;
        }

        /**
         * @param atBus the atBus to set
         */
        public void setatBus(int atBus) {
            this.atBus = atBus;
        }

        /**
         * @return the EndPointSoc
         */
        public double getEndPointSoc() {
            return this.EndPointSoc;
        }

        /**
         * @param EndPointSoc the EndPointSoc to set
         */
        public void setEndPointSoc(double EndPointSoc) {
            this.EndPointSoc = EndPointSoc;
        }

        /**
         * @return the MaximumEnergy
         */
        public double getMaximumEnergy() {
            return this.MaximumEnergy;
        }

        /**
         * @param MaximumEnergy the MaximumEnergy to set
         */
        public void setMaximumEnergy(double MaximumEnergy) {
            this.MaximumEnergy = MaximumEnergy;
        }

        /**
         * @return the NominalRampDownInput
         */
        public double getNominalRampDownInput() {
            return this.NominalRampDownInput;
        }

        /**
         * @param NominalRampDownInput the NominalRampDownInput to set
         */
        public void setNominalRampDownInput(double NominalRampDownInput) {
            this.NominalRampDownInput = NominalRampDownInput;
        }

        /**
         * @return the NominalRampUpInput
         */
        public double getNominalRampUpInput() {
            return this.NominalRampUpInput;
        }

        /**
         * @param NominalRampUpInput the NominalRampUpInput to set
         */
        public void setNominalRampUpInput(double NominalRampUpInput) {
            this.NominalRampUpInput = NominalRampUpInput;
        }

        /**
         * @return the NominalRampDownOutput
         */
        public double getNominalRampDownOutput() {
            return this.NominalRampDownOutput;
        }

        /**
         * @param NominalRampDownOutput the NominalRampDownOutput to set
         */
        public void setNominalRampDownOutput(double NominalRampDownOutput) {
            this.NominalRampDownOutput = NominalRampDownOutput;
        }

        /**
         * @return the NominalRampUpOutput
         */
        public double getNominalRampUpOutput() {
            return this.NominalRampUpOutput;
        }

        /**
         * @param NominalRampUpOutput the NominalRampUpOutput to set
         */
        public void setNominalRampUpOutput(double NominalRampUpOutput) {
            this.NominalRampUpOutput = NominalRampUpOutput;
        }

        /**
         * @return the MaximumPowerInput
         */
        public double getMaximumPowerInput() {
            return this.MaximumPowerInput;
        }

        /**
         * @param MaximumPowerInput the MaximumPowerInput to set
         */
        public void setMaximumPowerInput(double MaximumPowerInput) {
            this.MaximumPowerInput = MaximumPowerInput;
        }

        /**
         * @return the MinimumPowerInput
         */
        public double getMinimumPowerInput() {
            return this.MinimumPowerInput;
        }

        /**
         * @param MinimumPowerInput the MinimumPowerInput to set
         */
        public void setMinimumPowerInput(double MinimumPowerInput) {
            this.MinimumPowerInput = MinimumPowerInput;
        }

        /**
         * @return the MaximumPowerOutput
         */
        public double getMaximumPowerOutput() {
            return this.MaximumPowerOutput;
        }

        /**
         * @param MaximumPowerOutput the MaximumPowerOutput to set
         */
        public void setMaximumPowerOutput(double MaximumPowerOutput) {
            this.MaximumPowerOutput = MaximumPowerOutput;
        }

        /**
         * @return the MinimumPowerOutput
         */
        public double getMinimumPowerOutput() {
            return this.MinimumPowerOutput;
        }

        /**
         * @param MinimumPowerOutput the MinimumPowerOutput to set
         */
        public void setMinimumPowerOutput(double MinimumPowerOutput) {
            this.MinimumPowerOutput = MinimumPowerOutput;
        }

        /**
         * @return the MinimumSoc
         */
        public double getMinimumSoc() {
            return this.MinimumSoc;
        }

        /**
         * @param MinimumSoc the MinimumSoc to set
         */
        public void setMinimumSoc(double MinimumSoc) {
            this.MinimumSoc = MinimumSoc;
        }

        /**
         * @return the EfficiencyEnergy
         */
        public double getEfficiencyEnergy() {
            return this.EfficiencyEnergy;
        }

        /**
         * @param EfficiencyEnergy the EfficiencyEnergy to set
         */
        public void setEfficiencyEnergy(double EfficiencyEnergy) {
            this.EfficiencyEnergy = EfficiencyEnergy;
        }
    }
}
