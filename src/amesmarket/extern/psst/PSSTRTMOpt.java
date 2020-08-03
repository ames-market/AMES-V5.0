package amesmarket.extern.psst;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import amesmarket.AMESMarket;
import amesmarket.AMESMarketException;
import amesmarket.GenAgent;
import amesmarket.INIT;
import amesmarket.RTMOptimization;
import amesmarket.Support;
import amesmarket.TransGrid;
import amesmarket.filereaders.AbstractConfigFileReader;
import amesmarket.filereaders.BadDataFileFormatException;
import java.text.DecimalFormat;

/**
 * Setup and run the SCED using the PSST program
 *
 *
 */
public class PSSTRTMOpt implements RTMOptimization {

    private final File MarketDir = new File("DataFiles");
    private final File RTUnitCommitmentsFile;
    private final File RTMReferenceModelFile;
    private final File RTMResultsFile;

    private final AMESMarket ames;

    /**
     * Commitments by hour, for each genco.
     */
    private double[][] RTMDispatch;
    private double[] intervalGenDispatch;
    private double[][] shutdownCost;
    private double[][] startupCost;
    private double[][] productionCost;

    private double[] PrintGenCoDispatch;

    private int[][] GenUnitON;

    // previous initialization
    private double[][] RTMLMP;
    private double[] intervalLMP;
    private double[][] voltageAngles;
    //private double[][] branchFlow;
    private double[][] dailyPriceSensitiveDemand;
    private String SolutionStatus;

    //If future modifaction needs more than just baseS from from the INIT
    //class, we could change the code to keep an instance to the INIT instance.
    private final double baseS;
    private final TransGrid grid;

    private final int hoursPerDay;
    private final int numBranches;

    private final int K;
    private final int N;
    private final int I;
    private final int J;
    private final int H;
    private final int NIRTM; // Number of intervals in RTOPDur
    private final boolean deleteFiles;
    //private final int interval;
    private final PSSTConfig PSSTExt;

    /**
     * @param ames market instance begin used.
     * @param init init instance -- used to get the BaseS for PU/SI conversions.
     * @param ucVectorFile File the SCUC writes with the unit commitment
     * information
     * @param refModelFile ReferenceModel file
     * @param outFile output file for the SCED
     */
    public PSSTRTMOpt(AMESMarket ames, INIT init, File ucVectorFile, File refModelFile, File outFile) {
        this(ames, init.getBaseS(), ucVectorFile, refModelFile, outFile);
    }

    /**
     * @param ames market instance begin used.
     * @param baseS value for PU/SI conversions.
     * @param RTUnitCommitmentsFile File the SCUC writes with the unit
     * commitment information
     * @param RTMReferenceModelFile ReferenceModel file
     * @param RTMResultsFile output file for the SCED
     */
    public PSSTRTMOpt(AMESMarket ames, double baseS, File RTUnitCommitmentsFile, File RTMReferenceModelFile, File RTMResultsFile) {
        this.RTUnitCommitmentsFile = RTUnitCommitmentsFile;
        this.RTMReferenceModelFile = RTMReferenceModelFile;
        this.RTMResultsFile = RTMResultsFile;
        this.baseS = baseS;
        this.grid = ames.getTransGrid();
        this.ames = ames;

        this.hoursPerDay = ames.NUM_HOURS_PER_DAY;
        //this.interval = ames.interval;
        this.numBranches = ames.getNumBranches();

        this.K = ames.getNumNodes();
        this.N = this.numBranches;
        this.I = ames.getNumGenAgents();
        this.J = ames.getNumLSEAgents();
        this.H = this.hoursPerDay; //shorter name for local refs.
        this.NIRTM = ames.M / ames.getTestCaseConfig().RTKDur;
        this.deleteFiles = ames.isDeleteIntermediateFiles();
        this.PSSTExt = PSSTConfig.createDeterministicPSST("sced", this.RTUnitCommitmentsFile, this.RTMReferenceModelFile, this.RTMResultsFile, this.ames.getTestCaseConfig().Solver);

    }

    /**
     * Allocate new space for each a solution. Must be called every time a new
     * solution is created to prevent aliasing problems.
     */
    private void createSpaceForSols() {
        this.RTMDispatch = new double[NIRTM][this.I];
        this.intervalGenDispatch = new double[this.I];
        this.shutdownCost = new double[NIRTM][this.I];
        this.startupCost = new double[NIRTM][this.I];
        this.productionCost = new double[NIRTM][this.I];
        // previously used
        // this.dailyLMP        = new double[this.H][this.K];
        //this.dailyLMP = new double[this.K];
        this.RTMLMP = new double[NIRTM][this.K];
        this.intervalLMP = new double[this.K];
        this.voltageAngles = new double[NIRTM][this.K];  // N is changed to K 
        //this.branchFlow = new double[NIRTM][this.N];
        this.dailyPriceSensitiveDemand = new double[NIRTM][this.J];

        this.PrintGenCoDispatch = new double[this.I];
    }

    /**
     * Convert the PU back to SI.
     */
    private void convertToSI() {

        //helper method since we need to do this several times.
        //grumble...lambda functions...grumble
        class Converter {

            /**
             * Multiply or divide each element in vs by baseS
             *
             * @param vs values to covert
             * @param baseS
             * @param mult if true, multiply. if false divide.
             */
            final void convert(double[][] vs, double baseS, boolean mult) {
                for (int i = 0; i < vs.length; i++) {
                    for (int k = 0; k < vs[i].length; k++) {
                        if (mult) {
                            vs[i][k] *= baseS;
                        } else {
                            vs[i][k] /= baseS;
                        }
                    }
                }
            }

            final void convert(double[] vs, double baseS, boolean mult) {
                for (int i = 0; i < vs.length; i++) {
                    if (mult) {
                        vs[i] *= baseS;
                    } else {
                        vs[i] /= baseS;
                    }

                }
            }

            /**
             * Like {@link #convert}, but with default of multiplication set to
             * true.
             *
             * @param vs
             * @param baseS
             */
            final void convertM(double[][] vs, double baseS) {
                this.convert(vs, baseS, true);
            }

            /**
             * Like {@link #convert}, but with default of multiplication set to
             * true.
             *
             * @param vs
             * @param baseS
             */
            final void convertM(double[] vs, double baseS) {
                this.convert(vs, baseS, true);
            }

            /**
             * Like {@link #convert}, but with default of multiplication set to
             * false.
             *
             * @param vs
             * @param baseS
             */
            final void convertD(double[][] vs, double baseS) {
                this.convert(vs, baseS, false);
            }

            final void convertD(double[] vs, double baseS) {
                this.convert(vs, baseS, false);

            }
        }

        Converter c = new Converter();

        c.convertM(this.RTMDispatch, this.baseS);
        c.convertM(this.intervalGenDispatch, this.baseS);
        //c.convertM(this.branchFlow, this.baseS);
        c.convertD(this.RTMLMP, this.baseS);
        c.convertD(this.intervalLMP, this.baseS);
        c.convertM(this.dailyPriceSensitiveDemand, this.baseS);
    }

    /* (non-Javadoc)
	 * @see amesmarket.SCED#solveOPF()
     */
    @Override
    /**
     * Assumes all of the necessary data files have been written prior to
     * solving.
     */
    public void solveRTMOptimization(int interval) throws AMESMarketException {
        //allocate new arrays for this solution.
        this.createSpaceForSols();

        try {
            int resCode = this.syscall(interval);
            //System.out.println("SCED Result code: " + resCode +"\n");
            if (resCode != 0) {
                throw new RuntimeException(
                        "External SCEC exited with non-zero result code "
                        + resCode);
            }
        } catch (IOException | InterruptedException e1) {
            throw new AMESMarketException(e1);
        }

        //read result file
        this.readResults(this.RTMResultsFile, interval);
        this.convertToSI();

        this.cleanup();
    }

    private int syscall(int interval) throws IOException, InterruptedException {

        Process p = this.PSSTExt.createPSSTProcess(this.MarketDir);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // read the output from the command
        String s = null;
        System.out.println("\nHere is the standard output of the command with SCED:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println("SCED: " + s);
        }

        // read any errors from the attempted command
        System.err.println("\nHere is the standard error of the command with SCED (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println("There is error with SCED: ");
            System.out.println(s);
        }

        int resCode = p.waitFor();

        return resCode;
    }

    private void cleanup() {
        if (this.deleteFiles) {
            List<File> filesToRm = Arrays.asList(this.RTUnitCommitmentsFile,
                    this.RTMReferenceModelFile,
                    this.RTMResultsFile
            );
            Support.deleteFiles(filesToRm);
        }
    }

    private void readResults(File in, int interval) throws BadDataFileFormatException {
        SCEDReader scedr = new SCEDReader();
        scedr.read(in, interval);
    }

    /**
     * @return the RTUnitCommitmentsFile
     */
    public File getUcVectorFile() {
        return this.RTUnitCommitmentsFile;
    }

    /**
     * @return the RTMReferenceModelFile
     */
    public File getRefModelFile() {
        return this.RTMReferenceModelFile;
    }

    /**
     * @return the RTMResultsFile
     */
    public File getScedFile() {
        return this.RTMResultsFile;
    }

    ////////////////////BEGIN SCED///////////////////////////
    /* (non-Javadoc)
	 * @see amesmarket.SCED#getDailyCommitment()
     */
    @Override
    public double[][] getRTMDispatchSolution() {
        return this.RTMDispatch;
    }

    public double[] getIntervalGenDispatches() {
        return this.intervalGenDispatch;
    }


    /* (non-Javadoc)
	 * @see amesmarket.SCED#getDailyLMP()
     */
    @Override
    public double[][] getRTMLMPSolution() {
        return this.RTMLMP;
    }
//

    public double[] getIntervalLMP() {
        return this.intervalLMP;
    }

    /* (non-Javadoc)
	 * @see amesmarket.SCED#getDailyBranchFlow()
     */
//    @Override
//    public double[][] getRTMBranchFlowSolution() {
//        return this.branchFlow;
//    }

    /* (non-Javadoc)
	 * @see amesmarket.SCED#getDailyPriceSensitiveDemand()
     */
    @Override
    public double[][] getRTMPriceSensitiveDemandSolution() {
        return this.dailyPriceSensitiveDemand;
    }

    /**
     * @return the shutdownCost
     */
    public double[][] getShutdownCost() {
        return this.shutdownCost;
    }

    /**
     * @return the startupCost
     */
    public double[][] getStartupCost() {
        return this.startupCost;
    }

    /**
     * @return the productionCost
     */
    public double[][] getProductionCost() {
        return this.productionCost;
    }

    /**
     * @return the productionCost
     */
    public double[] getPrintGenCoDispatch() {
        return this.PrintGenCoDispatch;
    }

    public String getSolutionStatus() {
        return this.SolutionStatus;
    }

    ////////////////////END SCED///////////////////////////
    /**
     * Helper class to read the data file. It modifies the fields of the its
     * parent class as it reads.
     *
     */
    private class SCEDReader extends AbstractConfigFileReader<Void> {

        /**
         * Read the LMPs for each zone. Will need to divide by the baseS
         * parameter to get these values to sensible $/MWh.
         */
        private void readLMP() throws BadDataFileFormatException {
            double[][] temp = new double[NIRTM][K];
            do {
                this.move(true);
                if (this.endOfSection(LMP)) {
                    break;
                }

                String[] p = this.currentLine.split(DELIM);
                Support.trimAllStrings(p);

                int h = this.stoi(p[1]) - 1;
                int b = this.stoi(p[0]) - 1;
                temp[h][b] = this.stod(p[2]);

            } while (true);

            for (int b = 0; b < K; b++) {
                double Temp = 0;
                for (int i = 0; i < NIRTM; i++) {
                    PSSTRTMOpt.this.RTMLMP[i][b] = temp[i][b] * 60 / ames.getTestCaseConfig().RTKDur;
                    Temp = Temp + temp[i][b];
                }

                PSSTRTMOpt.this.intervalLMP[b] = Temp / NIRTM * 60 / ames.getTestCaseConfig().RTKDur;
                //System.out.println("b:" + b + "LMP:" +(Temp/NIRTM)* 60 /ames.getTestCaseConfig().RTDeltaT );
            }
        }

        /**
         * Read the dispatch level, in PU from PSST.
         */
        private void readGenCoResults(int interval) throws BadDataFileFormatException {
            double[][] dispatch = PSSTRTMOpt.this.RTMDispatch; //don't lookup the parent reference all the time.
            double[][] shutdownCost = PSSTRTMOpt.this.shutdownCost;
            double[][] startupCost = PSSTRTMOpt.this.startupCost;
            double[][] productionCost = PSSTRTMOpt.this.productionCost;

            //key/value indexes for the data in this section.
            final int KIDX = 0;
            final int VIDX = 1;
            int curGenCoIdx = 0;
            int curInterval = 0;

            GenAgent ga = null;
            do {
                this.move(true);
                if (this.endOfSection(GENCO_RESULTS)) {
                    break;
                }

                //first check for a 'new' genco line
                //Assume the list of commitments for each GenCo is
                // GenCoX where x is the index in the array/genco number
                // followed by a list of genco elements.
                if (this.currentLine.startsWith(GEN_CO_LABEL)) {
                    //lookup the label's idx
                    //System.out.println("");
                    ga = PSSTRTMOpt.this.ames.getGenAgentByName(this.currentLine);

                    if (ga == null) {
                        throw new BadDataFileFormatException(
                                "Unknown GenCo " + this.currentLine + " in " + this.sourceFile.getPath());
                    }

                    curGenCoIdx = ga.getIndex();
                    //System.out.print(ga.getID());
                } else {
                    //should find either Interval, PowerGenerated, ProductionCost, StartupCost, or ShutdownCost.
                    //these are all label ':' value
                    String[] keyAndValue = this.splitKeyValue(DELIM, this.currentLine, true);
                    try {
                        if (INTERVAL.equals(keyAndValue[KIDX])) {
                            curInterval = Integer.parseInt(keyAndValue[VIDX]);
                            if ((curInterval > 0)) { //if( (curHour > 0) && (curHour <= PSSTRTMOpt.this.hoursPerDay) )
                                curInterval = curInterval - 1; //adjust for array index repr.
                            } else {
                                throw new BadDataFileFormatException(this.lineNum,
                                        "Invalid minute for GenCo" + curGenCoIdx
                                        + " Encountered minute " + curInterval);
                            }
                        } else if (POWER_GEN.equals(keyAndValue[KIDX])) {  //PowerGenerated
                            dispatch[curInterval][curGenCoIdx] = Support.parseDouble(keyAndValue[VIDX]);
                            PSSTRTMOpt.this.intervalGenDispatch[curGenCoIdx] = dispatch[curInterval][curGenCoIdx];
                            PrintGenCoDispatch[curGenCoIdx] = (baseS * dispatch[curInterval][curGenCoIdx]);
                            //System.out.print(" : "+(baseS* dispatch[curInterval][curGenCoIdx]));
                            ga.setPowerPrevInterval(dispatch[curInterval][curGenCoIdx] * baseS);
                        } else {
                            throw new BadDataFileFormatException(this.lineNum, "Unknown label " + keyAndValue[KIDX]);
                        }
                    } catch (NumberFormatException nfe) {
                        //the only thing that throws a NumberFormatExecption is parsing the value of the key/value pairs.
                        throw new BadDataFileFormatException(this.lineNum,
                                "Invalid value " + keyAndValue[VIDX]);
                    }
                }

            } while (true);
        }

        /**
         * Read the voltage angle at each Bus, for each Hour, in radians.
         */
        private void readVoltageAngles() throws BadDataFileFormatException {
            final int BUS_LEN = 3; //length of the word Bus

            do {
                int busNum = -1;
                int min = -1;
                double voltageAngle = 0;

                this.move(true);
                if (this.endOfSection(VOLTAGE_ANGLES)) {
                    break;
                }

                String[] p = this.currentLine.split(":");
                Support.trimAllStrings(p);

                if (p.length != 2) {
                    throw new BadDataFileFormatException("Expected BusX <min> : <angle>. Found " + this.currentLine);
                }

                String[] busAndHour = p[0].split(WS_REG_EX);
                Support.trimAllStrings(busAndHour);
                if (busAndHour.length != 2) {
                    throw new BadDataFileFormatException("Could not find and bus and min in" + p[0] + ".");
                }

                //assume that each bus starts with the word Bus
                try {
                    busNum = Integer.parseInt(
                            busAndHour[0].substring(BUS_LEN)
                    );
                } catch (NumberFormatException nfe) {
                    throw new BadDataFileFormatException(nfe);
                }

                //parse the min
                try {
                    min = Integer.parseInt(busAndHour[1]);
                    min = min - 1;
                } catch (NumberFormatException nfe) {
                    throw new BadDataFileFormatException(nfe);
                }

                //parse the actual angle.
                try {
                    voltageAngle = Support.parseDouble(p[1]);
                } catch (NumberFormatException nfe) {
                    throw new BadDataFileFormatException(nfe);
                }
                // System.out.println("N: "+ N + " busNum: "+ busNum + " min:" + min);
                PSSTRTMOpt.this.voltageAngles[min][busNum - 1] = voltageAngle; // changed busNum to (busNum-1)
            } while (true);
        }

        private void readBranchLMP() throws BadDataFileFormatException {
            do {
                this.move(true);
                //deal with the current line to find the lmp.
                //sced.lmp[branch][hour] ?or the other way around = value
            } while (!this.endOfSection(BRANCH_LMP));
        }

        private void readPriceSensitiveDemand() throws BadDataFileFormatException {
            do {
                this.move(true);
            } while (!this.endOfSection(PRICE_SENSITIVE_DEMAND));
        }

        private void readSolutionStatus() throws BadDataFileFormatException {
            this.move(true);
            String vs = this.currentLine;
            if (vs.startsWith("optimal")) {
                SolutionStatus = "optimal";
            } else if (vs.startsWith("infeasible")) {
                SolutionStatus = "infeasible";
            }

            //Look for the section end marker
            this.move(true);
            if (!this.endOfSection(SOLUTION_STATUS)) {
                throw new BadDataFileFormatException(this.lineNum,
                        "Expected END" + SOLUTION_STATUS + ". Found " + this.currentLine + "."
                );
            }
        }

        private boolean endOfSection(String secName) {
            return (END + secName).equals(this.currentLine);
        }

        @Override
        protected Void read(int interval) throws BadDataFileFormatException {

            while (true) {
                this.move(false);
                if (this.currentLine == null) {
                    break;
                }

                if (SOLUTION_STATUS.equals(this.currentLine)) {
                    this.readSolutionStatus();
                    this.move(false);
                }
                if (SolutionStatus.equals("optimal")) {
                    if (LMP.equals(this.currentLine)) {
                        this.readLMP();
                    } else if (GENCO_RESULTS.equals(this.currentLine)) {
                        this.readGenCoResults(interval);
                    } else if (VOLTAGE_ANGLES.equals(this.currentLine)) {
                        this.readVoltageAngles();
                    } else if (BRANCH_LMP.equals(this.currentLine)) {
                        this.readBranchLMP();
                    } else if (PRICE_SENSITIVE_DEMAND.equals(this.currentLine)) {
                        this.readPriceSensitiveDemand();
                    } else {
                        System.out.println("" + this.currentLine);
                        throw new BadDataFileFormatException(this.lineNum, this.currentLine);
                    }
                } else if (SolutionStatus.equals("infeasible")) {
                    System.out.println("Solution is Infeasible because the reserve constraints are not satisfied.");
                }
            }

            return null;
        }

        private static final String LMP = "LMP";
        private static final String GENCO_RESULTS = "GenCoResults";
        private static final String VOLTAGE_ANGLES = "VOLTAGE_ANGLES";
        private static final String BRANCH_LMP = "DAILY_BRANCH_LMP";
        private static final String PRICE_SENSITIVE_DEMAND = "PSLResults";
        private static final String HAS_SOLUTION = "HAS_SOLUTION";
        private static final String SOLUTION_STATUS = "SOLUTION_STATUS";
        private static final String INTERVAL = "Interval";
        private static final String DELIM = ":";
        //GenCo data labels/tokens
        private static final String GEN_CO_LABEL = "GenCo";
        private static final String POWER_GEN = "PowerGenerated";
        private static final String PRODUCTION_COST = "ProductionCost";
        private static final String STARTUP_COST = "StartupCost";
        private static final String SHUTDOWN_COST = "ShutdownCost";
        //End GenCo data labels.
        private static final String END = "END_"; //section end marker

    }
}
