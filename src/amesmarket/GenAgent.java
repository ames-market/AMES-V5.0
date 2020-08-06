
// GenAgent.java
// GenCo (wholesale power producer)
package amesmarket;

import java.awt.*;

import uchicago.src.sim.gui.*;
import java.util.ArrayList;
import java.math.*;
import uchicago.src.sim.util.SimUtilities;
import edu.iastate.jrelm.core.JReLMAgent;
import edu.iastate.jrelm.core.SimpleAction;
import edu.iastate.jrelm.rl.ReinforcementLearner;
import edu.iastate.jrelm.rl.SimpleStatelessLearner;
import edu.iastate.jrelm.rl.rotherev.variant.VREParameters;
import edu.iastate.jrelm.rl.rotherev.REPolicy;

import java.util.Arrays;
import java.util.HashMap;
import amesmarket.CaseFileData.SCUCInputData;
import java.util.Map;

/**
 * Example showing what genData[i] contains
 *
 * GenData //ID	atBus	SCost	A	B	C   NS    PMin PMax  initMoney 
 *
 */


public class GenAgent implements Drawable, JReLMAgent {

    private static final int ID = 0;
    private static final int AT_NODE = 1;
    private static final int S_COST = 2;
    private static final int A = 3;
    private static final int B = 4;
    private static final int C = 5;
    private static final int NSeg = 6;
    private static final int P_MIN = 7;
    private static final int P_MAX = 8;
    private static final int INIT_MONEY = 9;
    private static final int HOURS_PER_DAY = 24;

    // GenCo's data
    private int xCoord;      // Coordinate x on trans grid
    private int yCoord;      // Coordinate y on trans grid

    private int id;            // GenCo's ID
    private final int index;         // index for the genco when it is in an array.
    private int atBus;        // GenCo's location (at which bus)
    private double fixedCost;  // GenCo's fixed cost
    private double a;          // GenCo's (true) cost attribute
    private double b;          // GenCo's (true) cost attribute
    private double c;          // GenCo's (true) cost attribute
    private int NS;          //
    private double PMin;     // GenCo's (true) minimum production capacity limit
    private double PMax;     // GenCo's (true) maximum production capacity limit
    private double[] trueSupplyOffer; // (a,b,c, NS, PMin, PMax), true

    private int ColdStartTime = 0;
    private double ColdStartUpCost = 0;
    private double HotStartUpCost = 0;
    private double ShutDownCost = 0;

    private double aReported;      // GenCo's reported cost attribute
    private double bReported;      // GenCo's reported cost attribute
    private double cReported;      // GenCo's reported cost attribute
    private double PMaxReported; // GenCo's reported maximum production capacity limit
    private double[] reportedSupplyOffer;   // (a, bReported,cReported,capMin,capMaxReported), strategic

    // GenCo's records by hours (within a day)
    private double[] commitment;  // Day-Ahead hourly power commitment quantity
    private double[] dispatch;    // Real-Time hourly power dispatch quantity
    private int[] commitmentStatus;
    private double[] dayAheadLMP; // Day-Ahead hourly locational marginal price
    private double[] realTimeLMP; // Real-Time hourly locational marginal price
    private double[] totalVariableCost;  // totalVariableCost = A*power + B*power^2
    private double[] hourlyTotalCost;          // hourlyTotalCost = totalVariableCost + SCost

    private double[] hourlyVariableCost;// hourlyVariableCost[h] = b*power + c*power^2
    private double[] hourlyNetEarning;// hourlyNetEarning[h] = dispatch[h]*lmp[h]
    private double[] hourlyProfit;// hourlyProfit[h] = dispatch[h]*lmp[h] - hourlyTotalCost[h]
    private double dailyNetEarnings;   // dailyNetEarnings = sum of hourlyNetEarning over 24 hours
    private double dailyProfit;   // dailyProfit = sum of hourlyProfit over 24 hours
    private double money;    // GenCo's accumulative money holding,
    // money (new) = money(previous) + dailyProfit(new)

    private double[] hourlyRevenue;
    private double dailyRevenue;  // 
    private double dailyRevenueCE; // dailyRevenue under CE (Competitive Equil) case

    private double choiceProbability; // gen's learning choice probability
    private double choicePropensity;  // gen's learning choice Propensity
    private int choiceID;  // gen's learning choice ID

    //GenCo's records by day
    //private ArrayList commitmentByDay;
    //private ArrayList dispatchByDay;
    //private ArrayList dayAheadLMPByDay;
    //private ArrayList realTimeLMPByDay;
    //GenCo's records by day
    /**
     * Commit vector for each day d. Now modified to matrix
     */
    private final HashMap<Integer, int[]> commitmentByDay;
    /**
     * Dispatch levels for each hour hour of each day.
     */
    private final HashMap<Integer, double[]> dispatchByDay;
    /**
     * Costs for each hour of a day.
     */
    private final HashMap<Integer, double[]> productionCostsByDay;
    //NOT USED. Stored in the AMESMARKET.
    private final HashMap<Integer, double[]> startupCostsByDay;
    private final HashMap<Integer, double[]> shutdownCostsByDay;

    private final ArrayList<double[][]> dayAheadLMPByDay;
    private final ArrayList<double[][]> realTimeLMPByDay;

    // JReLM component
    private SimpleStatelessLearner learner;
    private int randomSeed;

    // Learning variables
    private double lowerRI; // lower Range Index
    private double upperRI; // upper Range Index
    private double upperRCap; // upper relative capacity
    private double slopeStart;

    private double priceCap;  // max price for LMP
    private int iSCostExcludedFlag; // 0->profit, 1->net earnings

    // Check if Action Probability Stable
    private int iStartDay;
    private int iCheckDayLength;
    private double dActionProbability;
    private boolean bActionProbabilityCheck;

    private boolean bActionProbabilityConverge;
    private int iCheckDayLengthCount;
    private int iDayCount;
    private double[] oldActionProbability;
    private double[] newActionProbability;

    // Check if Learning Result Stable
    private int iLearningCheckStartDay;
    private int iLearningCheckDayLength;
    private double dLearningCheckDifference;
    private boolean bLearningCheck;

    private boolean bLearningCheckConverge;
    private int iLearningCheckDayLengthCount;
    private int iLearningCheckDayCount;
    private double[] oldLearningResult;

    private boolean bDailyNetEarningConverge;
    private int iDailyNetEarningDayLengthCount;
    private int iDailyNetEarningDayCount;
    private boolean bDailyNetEarningThreshold;
    private double dDailyNetEarningThreshold;
    private int iDailyNetEarningStartDay;
    private int iDailyNetEarningDayLength;
    private double[] oldDailyNetEarningResult;

    private int iActinDomain;
    ArrayList newActionDomain;

    private String genID;

    /**
     * Value for PowerGeneratedT0 in SCUC input file. This is the default t0
     * power only. Once the simulation starts running this will be ignored.
     */
    private double defaultT0pwer = 0;
    private double T0NextDayPower = 0;
    private double PrevIntervalPower = 0;

    private int HourUnitON = 0;
    private int T0NextDayUnitOnState = 0;

    private int UnitOnT0State = -2;
    private int MinUpTime = 0;
    private int MinDownTime = 0;
    private double nominalRampUpLim = 999999;
    private double nominalRampDownLim = 999999;
    private double startupRampLim = 999999;
    private double shutdownRampLim = 999999;

    private String fuelType = "Unknown";

    // Constructor
    /**
     * Flag for whether or not the generator represents an alert or canary
     * generator. Certain calculations (e.g. gen capacity) need to exclude the
     * canary generators.
     */
    private final boolean isCanary;

    /**
     * @param genData
     * @param learningParams
     * @param actionDomain
     * @param ss
     * @param dCap
     * @param random
     * @param iStart
     * @param iLength
     * @param dCheck
     * @param bCheck
     * @param iLearnStart
     * @param iLearnLength
     * @param dLearnCheck
     * @param bLearnCheck
     * @param dEarningThreshold
     * @param bEarningThresh
     * @param iEarningStart
     * @param iEarningLength
     * @param iSCostExcluded
     * @param isCanary
     */
    public GenAgent(double[] genData, VREParameters learningParams,
            ArrayList actionDomain, double ss, double dCap, int random, int iStart, int iLength, double dCheck, boolean bCheck,
            int iLearnStart, int iLearnLength, double dLearnCheck, boolean bLearnCheck, double dEarningThreshold, boolean bEarningThresh,
            int iEarningStart, int iEarningLength, int iSCostExcluded, boolean isCanary, int index) {

        xCoord = -1;
        yCoord = -1;

        // Parse genData
        id = (int) genData[ID];
        atBus = (int) genData[AT_NODE];
        fixedCost = genData[S_COST];
        a = genData[A];
        b = genData[B];
        c = genData[C];
        NS = (int) genData[NSeg];
        PMin = genData[P_MIN];
        PMax = genData[P_MAX];
        money = genData[INIT_MONEY];

        // trueSupplyOffer = (a, b, c, NS, PMin, PMax)
        trueSupplyOffer = new double[P_MAX - A + 1];
        for (int i = 0; i < P_MAX - A + 1; i++) {
            trueSupplyOffer[i] = genData[i + A];
        }

        // Initialize reportedSupplyOffer to all zeros
        // reportedSupplyOffer = (aReported, bReported, cReported, NS, PMin, PMaxReported)
        reportedSupplyOffer = new double[trueSupplyOffer.length];

        commitment = new double[HOURS_PER_DAY];
        dispatch = new double[HOURS_PER_DAY];
        dayAheadLMP = new double[HOURS_PER_DAY];
        realTimeLMP = new double[HOURS_PER_DAY];
        totalVariableCost = new double[HOURS_PER_DAY];

        commitmentStatus = new int[HOURS_PER_DAY];

        hourlyTotalCost = new double[HOURS_PER_DAY];
        hourlyProfit = new double[HOURS_PER_DAY];
        hourlyNetEarning = new double[HOURS_PER_DAY];
        hourlyVariableCost = new double[HOURS_PER_DAY];
        hourlyRevenue = new double[HOURS_PER_DAY];

        dailyRevenue = 0;
        dailyRevenueCE = 0;

        // Create historical data records (initialized all to zeros)
        commitmentByDay = new HashMap<Integer, int[]>();
        dispatchByDay = new HashMap<Integer, double[]>();
        dayAheadLMPByDay = new ArrayList<double[][]>();
        realTimeLMPByDay = new ArrayList<double[][]>();
        productionCostsByDay = new HashMap<Integer, double[]>();
        startupCostsByDay = new HashMap<Integer, double[]>();
        shutdownCostsByDay = new HashMap<Integer, double[]>();

        randomSeed = random;
        slopeStart = ss;
        priceCap = dCap;
        iStartDay = iStart;
        iCheckDayLength = iLength;
        dActionProbability = dCheck;
        bActionProbabilityCheck = bCheck;
        iCheckDayLengthCount = 0;
        iDayCount = 1;
        bActionProbabilityConverge = false;

        iLearningCheckStartDay = iLearnStart;
        iLearningCheckDayLength = iLearnLength;
        dLearningCheckDifference = dLearnCheck;
        bLearningCheck = bLearnCheck;
        bLearningCheckConverge = false;
        iLearningCheckDayLengthCount = 0;
        iLearningCheckDayCount = 1;
        oldLearningResult = new double[3];

        bDailyNetEarningConverge = false;
        iDailyNetEarningDayLengthCount = 0;
        iDailyNetEarningDayCount = 1;
        dDailyNetEarningThreshold = dEarningThreshold;
        bDailyNetEarningThreshold = bEarningThresh;
        iDailyNetEarningStartDay = iEarningStart;
        iDailyNetEarningDayLength = iEarningLength;
        oldDailyNetEarningResult = new double[iDailyNetEarningDayLength];

        iSCostExcludedFlag = iSCostExcluded;

        
        //System.out.println("GenCo ID="+id+" maxmum profit="+getMaxPotentialProfit());
        this.isCanary = isCanary;
        this.index = index;
    }




    public double[] submitSupplyOffer() {
        return trueSupplyOffer;
    }




    public boolean isSolvent() {
        boolean solvency = true;
        if (money < 0) {
            solvency = false;
            System.out.println("GenCo " + getGenID() + " is out of market.");
        }
        return solvency;
    }


    /**
     * Whether or not the generator is a 'canary' that is used to indicate the
     * load was too much for the standard generators. This is a simple way to
     * deal with the lack of reserve problem.
     */
    public boolean isCanary() {
        return isCanary;
    }

    /**
     * Get the dispatch values for day d.
     *
     * @param day
     * @return array of dispatches, or null if no data for the day.
     */
    public double[] getDispatchesForDay(int day) {
        return dispatchByDay.get(day);
    }

    public int getStartDay() {
        return iStartDay;
    }

    public int getCheckDayLength() {
        return iCheckDayLength;
    }

    // GenCo's get and set methods
    public void setXY(int newX, int newY) {
        xCoord = newX;
        yCoord = newY;
    }

    public int getGenID() { // This method name cannot be changed to "int getID"
        return id;           // because it'll conflict with JReLM interface method "String getID"
    }

    /**
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    public Map<Integer, int[]> getCommitmentDecisions() {
        return commitmentByDay;
    }


    public int getAtNode() {
        return atBus;
    }


    public double[] getCommitment() {
        return commitment;
    }

    public void setCommitment(double[] comm) {
        commitment = comm;
    }

    public void setCommitmentStatus(int[] status) {
        System.arraycopy(status, 0, commitmentStatus, 0, HOURS_PER_DAY);
    }

//    public void setCommitmentStatus(int hour, int status){
//        commitmentStatus[hour] = status;
//    }
    public int getCommitmentStatus(int h) {
        return commitmentStatus[h];
    }

    public double[] getDayAheadLMP() {
        return dayAheadLMP;
    }

    public void setDayAheadLMP(double[] lmprice) {
        dayAheadLMP = lmprice;
    }

    /*    public  getCommitmentByDay() {
        return commitmentByDay;
    }
     */
    public ArrayList getDayAheadLMPByDay() {
        return dayAheadLMPByDay;
    }

    public int getChoiceID() {
        return choiceID;
    }

    public void report() {
        System.out.println(getID() + " at (" + xCoord + "," + yCoord + ") has "
                + "current money holding " + money);
    }

    // Implemented methods for JReLMAgent interface
    public String getID() {
        //return "GenCo" + id;
        return genID;
    }

    public void setID(String gencoName) {
        this.genID = gencoName;
    }

    public ReinforcementLearner getLearner() {
        return learner;
    }

    // Implemented methods for Drawable interface
    public int getX() {
        return xCoord;
    }

    public int getY() {
        return yCoord;
    }

    public void draw(SimGraphics sg) {
        sg.drawFastRoundRect(Color.blue);         //GEN is blue colored
    }

    public void setNS(int d) {
        this.NS = d;
    }

    public void setColdStartTime(int n) {
        this.ColdStartTime = n;
    }

    public void setColdStartUpCost(double d) {
        this.ColdStartUpCost = d;
    }

    public void setHotStartUpCost(double d) {
        this.HotStartUpCost = d;
    }

    public void setShutDownCost(double d) {
        this.ShutDownCost = d;
    }

    public int getNS() {
        return this.NS;
    }


    public int getColdStartUpTime() {
        return this.ColdStartTime;
    }

    public double getColdStartUpCost() {
        return this.ColdStartUpCost;
    }

    public double getHotStartUpCost() {
        return this.HotStartUpCost;
    }

    public double getShutDownCost() {
        return this.ShutDownCost;
    }

    /**
     * Get the commitment decision for day d.
     *
     * @param d
     * @return
     */
    public int[] getCommitmentsForDay(int d) {
        return commitmentByDay.get(d);
    }

    /**
     * @return the Minimum generation capacity.
     */
    public double getCapacityMin() {
        return PMin;
    }

    /**
     * @return the Maximum generation capacity.
     */
    public double getCapacityMax() {
        return PMax;
    }

    /**
     * @return the Maximum generation capacity the genco reports.
     */
    public double getReportedCapacityMax() {
        return PMaxReported;
    }

    public void CalUnitONT0State(int day){

        final int[] commitmentsForDay = getCommitmentsForDay(day);
        
        final int lastHourState = commitmentsForDay[commitmentsForDay.length - 1];
        int numHourSame = 0;

        //count how many hours were the same generator state (on or off)
        for (int i=commitmentsForDay.length-1; i>=0; i--) {
            if (commitmentsForDay[i] == lastHourState) {
                numHourSame++;
            } else { //state changed, done counting.
                break;
            }
        }

        if (lastHourState == 1) { //Generator has been on.
            if (numHourSame == 24){
                setUnitOnT0State(numHourSame+UnitOnT0State);
            }
            else{
                setUnitOnT0State(numHourSame);
            }
        } else { //invert the sign. Generator has been off.
            if (numHourSame == 24){
                setUnitOnT0State(-numHourSame+UnitOnT0State);
            }
            else{
                setUnitOnT0State(-numHourSame);
            }
        }
        
    }
    
    public int getUnitOnT0State() {
        return UnitOnT0State;
    }
    
    /**
     * On/off Status for T0, day d. From the pyomo model:
     * <blockquote>
     * if positive, the number of hours prior to (and including) t=0 that the
     * unit has been on. if negative, the number of hours prior to (and
     * including) t=0 that the unit has been off. the value cannot be 0, by
     * definition.
     *
     * </blockquote>
     *
     * @param day
     * @return The on/off is whether or not the unit was on/off on the last hour
     * of day-1, or, 1, if the generator does not have a record for day d-1.
     *
     */
//    public int getUnitOnT0State(int day) {
//        if(day ==1){
//            return UnitOnT0State;
//        }
//        final int[] commitmentsForDay = getCommitmentsForDay(day-1);
//
//        if (commitmentsForDay == null) {
//            return 1;
//        }
//        
//        final int lastHourState = commitmentsForDay[commitmentsForDay.length - 1];
//        int numHourSame = 0;
//
//        //count how many hours were the same generator state (on or off)
//        for (int i=commitmentsForDay.length-1; i>=0; i--) {
//            if (commitmentsForDay[i] == lastHourState) {
//                numHourSame++;
//            } else { //state changed, done counting.
//                break;
//            }
//        }
//
//        if (lastHourState == 1) { //Generator has been on.
//            if (numHourSame == 24){
//                setUnitOnT0State(numHourSame+UnitOnT0State);
//                return UnitOnT0State;
//            }
//            else{
//                setUnitOnT0State(numHourSame);
//                return numHourSame;
//            }
//        } else { //invert the sign. Generator has been off.
//            if (numHourSame == 24){
//                setUnitOnT0State(-numHourSame+UnitOnT0State);
//                return UnitOnT0State;
//            }
//            else{
//                setUnitOnT0State(-numHourSame);
//                return -numHourSame;
//            }
//        }
//        
//    }

    public int getUnitOnHour() {
        return HourUnitON; //
    }

    public void setUnitOnHour(int i) {
        HourUnitON = i;
    }

    public double getPowerT0(int day) {
        double[] powerGen = dispatchByDay.get(day);
        if (powerGen != null) {
            return powerGen[powerGen.length - 1]; //last hour of the day.
        } else {
            return getCapacityMax();
        }
    }

    public void setPowerT0(double p) {
        defaultT0pwer = p;
        T0NextDayPower = p;
    }

    public double getPowerT0NextDay() {
        return T0NextDayPower; //last hour of the day.
    }

    public void setPowerT0NextDay(double p) {
        T0NextDayPower = p;
    }

    public int getUnitOnStateT0NextDay() {
        return T0NextDayUnitOnState; //24th hour of the day.
    }

    public void setUnitOnStateT0NextDay(int i) {
        T0NextDayUnitOnState = i;
    }

    public double getPowerPrevInterval() {
        return PrevIntervalPower; //
    }

    public void setPowerPrevInterval(double p) {
        PrevIntervalPower = p;
    }
    
    public void setUnitOnT0State(int n){
       UnitOnT0State = n;
    }

    public int getMinUpTime() {
        return MinUpTime;
    }

    public void setMinUpTime(int t) {
        MinUpTime = t;
    }

    public int getMinDownTime() {
        return MinDownTime;
    }

    public void setMinDownTime(int t) {
        MinDownTime = t;
    }

    public double getNominalRampUpLim() {
        return nominalRampUpLim;
    }

    /**
     * @param nominalRampUpLim the nominalRampUpLim to set
     */
    public void setNominalRampUpLim(double nominalRampUpLim) {
        this.nominalRampUpLim = nominalRampUpLim;
    }

    public double getNominalRampDownLim() {
        return nominalRampDownLim;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    /**
     * @param nominalRampDownLim the nominalRampDownLim to set
     */
    public void setNominalRampDownLim(double nominalRampDownLim) {
        this.nominalRampDownLim = nominalRampDownLim;
    }

    public double getStartupRampLim() {
        return startupRampLim;
    }

    /**
     * @param startupRampLim the startupRampLim to set
     */
    public void setStartupRampLim(double startupRampLim) {
        this.startupRampLim = startupRampLim;
    }

    public double getShutdownRampLim() {
        return shutdownRampLim;
    }

    /**
     * @param shutdownRampLim the shutdownRampLim to set
     */
    public void setShutdownRampLim(double shutdownRampLim) {
        this.shutdownRampLim = shutdownRampLim;
    }

    public void addExtraData(SCUCInputData sid) {
        setPowerT0(sid.getPowerT0());
        setUnitOnT0State(sid.getUnitOnT0State());
        setMinUpTime(sid.getMinUpTime());
        setMinDownTime(sid.getMinDownTime());
        setNominalRampUpLim(sid.getNominalRampUp());
        setNominalRampDownLim(sid.getNominalRampDown());
        setStartupRampLim(sid.getStartupRampLim());
        setShutdownRampLim(sid.getShutdownRampLim());

    }

    /**
     *
     * Put the 'actual', that is realtime computed dispatch for the GenCo.
     *
     * @param day actual day number
     * @param dispatchLevels dispatch for each hour, 0 to 23.
     */
    public void addActualDispatch(int day, double[] dispatchLevels) {
        dispatchByDay.put(day, dispatchLevels);
        //setCommitment(dispatchLevels);
    }


    /**
     * Put the commitment for day d. This stores the commitment by day d, but
     * the values are generated by the SCUC/DAM on day d-1. In otherwords, if we
     * are running the DAM on day d, add commitment for d+1.
     *
     * @param d
     * @param commitment
     */
    public void addCommitmentForDay(int d, int[] commitment) {
        commitmentByDay.put(d, commitment);
    }

}
