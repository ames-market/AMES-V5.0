package amesmarket.filereaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import amesmarket.CaseFileData;
import amesmarket.DefaultSimulationParameters;
import amesmarket.CaseFileData.GenData;
import amesmarket.NumberRecognizer;
import amesmarket.DAMOptimization;
import amesmarket.Support;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parse a case file.
 *
 *
 */
public class CaseFileReader {

    //TOKENS
    private static final String BASE_S = "BASE_S";
    private static final String BASE_V = "BASE_V";
    private static final String FNCS_ACTIVE = "FNCSActive";
    private static final String SOLVER = "solver";
    private static final String DATDur = "DATDur";
    private static final String RTOPDur = "RTOPDur";
    private static final String RTKDur = "RTKDur";
    private static final String BAL_PEN_POS = "BalPenPos";
    private static final String BAL_PEN_NEG = "BalPenNeg";
    private static final String MAX_DAY = "MaxDay";
    private static final String RANDOM_SEED = "RandomSeed";
    private static final String CAP_MARGIN = "Capacity_Margin";
    private static final String LOAD_CASE_CONTROL = "Load_Case_Control_File";
    private static final String THRESH_PROB = "ThresholdProbability";
    private static final String LSE_DATA_SOURCE = "LSEDemandSource";
    //private static final String RESERVE_REQUIREMENTS = "Reserve_Requirement";
    private static final String PRICE_SENSITIVE_DEMAND_FLAG = "PriceSensitiveDemandFlag";
//    private static final String DOWN_RESERVE_PERCENT = "DARRD";
//    private static final String UP_RESERVE_PERCENT = "DARRU";
//    private static final String RTRRD = "RTRRD";
//    private static final String RTRRU = "RTRRU";
    private static final String RESERVE_PERCENT_START = "#ReservePercentStart";
    private static final String RESERVE_PERCENT_END = "#ReservePercentEnd";
    private static final String LSE_DATA_TESTCASE = "TestCase";
    private static final String LSE_DATA_LOADCASE = "LoadCase";
    private static final String GEN_FUELTYPE_START = "#GenFuelTypeStart";
    private static final String GEN_FUELTYPE_END = "#GenFuelTypeEnd";
    private static final String BUS_DATA_START = "#BusDataStart";
    private static final String BUS_DATA_END = "#BusDataEnd";
    private static final String NUMBER_OF_BUSES = "NumberOfBuses";
    private static final String LINE_DATA_START = "#LineDataStart";
    private static final String LINE_DATA_END = "#LineDataEnd";
    private static final String GEN_DATA_START = "#GenDataStart";
    private static final String GEN_DATA_END = "#GenDataEnd";
    private static final String LSE_DATA_FIXED_DEM_START = "#LSEDataFixedDemandStart";
    private static final String LSE_DATA_FIXED_DEM_END = "#LSEDataFixedDemandEnd";
    private static final String NDG_DATA_START = "#NDGDataStart";
    private static final String NDG_DATA_END = "#NDGDataEnd";
    private static final String LSE_DATA_PRICE_SENS_DEM_START = "#LSEDataPriceSensitiveDemandStart";
    private static final String LSE_DATA_PRICE_SENS_DEM_END = "#LSEDataPriceSensitiveDemandEnd";
    private static final String GEN_LEARNING_DATA_START = "#GenLearningDataStart";
    private static final String GEN_LEARNING_DATA_END = "#GenLearningDataEnd";
    private static final String ALERT_GEN_START = "#AlertGenCoStart";
    private static final String ALERT_GEN_END = "#AlertGenCoEnd";
    private static final String SCUC_INPUT_DATA_START = "#ScucInputDataStart";
    private static final String SCUC_INPUT_DATA_END = "#ScucInputDataEnd";
    private static final String NUMBER_RESERVE_ZONES = "NumberOfReserveZones";
    private static final String ZONE_DATA_START = "#ZoneDataStart";
    private static final String ZONE_DATA_END = "#ZoneDataEnd";
    private static final String HAS_STORAGE = "StorageFlag";
    private static final String HAS_NDG = "NDGFlag";
    private static final String STORAGE_INPUT_DATA_START = "#StorageInputDataStart";
    private static final String STORAGE_INPUT_DATA_END = "#StorageInputDataEnd";
//    private static final String ZONE_NAMES_START = "#ZoneNamesStart";
//    private static final String ZONE_NAMES_END = "#ZoneNamesEnd";
    private static final String GEN_COST_START = "#GenCostStart";
    private static final String GEN_COST_END = "#GenCostEnd";

    private static final String WS_REG_EX = "\\s+";

    private static final int HOURS_PER_DAY = 24;
    private static final int PSL_PARAM_LEN = 9;

    private SimpleLineReader inputReader;
    /**
     * If opening from a file (instead of a reader) name of file. Non absolute
     * paths in the test case are relative to this file if not null.
     */
    private File testCaseFile = null;
    private String currentLine = null;

    /**
     * Use this to convert int/doubles strings. It will handle floating points
     * where integers are expected.
     */
    private final NumberRecognizer numRecog = new NumberRecognizer();

    /**
     * Read in/load the test file configuration.
     *
     * @param testCaseFile
     * @throws BadDataFileFormatException
     */
    public CaseFileData loadCaseFileData(final File testCaseFile) {
        inputReader = new SimpleLineReader(testCaseFile);
        this.testCaseFile = testCaseFile;
        return loadCaseFileData(true);
    }

    /**
     * Read in/load the test file configuration.
     *
     * @param testCaseInput
     * @throws BadDataFileFormatException
     */
    public CaseFileData loadCaseFileData(final Reader testCaseInput) {
        return loadCaseFileData(testCaseInput, true);
    }

    /**
     * Read in/load the test file configuration.
     *
     * This method is protected to prevent general access to the method. There
     * are only a few situation when this is needed and those are mostly related
     * to testing. The method can be exposed by extending the class if the
     * method is really needed.
     *
     * @param testCaseInput
     * @param doFinishActions whether or not to run the finish actions.
     * @throws BadDataFileFormatException
     */
    protected CaseFileData loadCaseFileData(final Reader testCaseInput, boolean doFinishActions) {
        inputReader = new SimpleLineReader(testCaseInput);
        return loadCaseFileData(doFinishActions);
    }

    /**
     * Load the case file data.
     *
     * Assumes the internal inputReader has been initialized.
     *
     * @param doFinishActions whether or not to run the
     * {@link #finish(CaseFileData)} method. Should not be turned off for
     * production. Useful for testing.
     * @throws BadDataFileFormatException
     */
    private CaseFileData loadCaseFileData(boolean doFinishActions) {
        CaseFileData testConf = new CaseFileData();

        try {
            parseDataFile(testConf, doFinishActions);
        } catch (BadDataFileFormatException ex) {
            Logger.getLogger(CaseFileReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        inputReader.close();
        return testConf;
    }

    private boolean move() {
        currentLine = inputReader.nextLine();
        return currentLine != null;
    }

    private void match(String expected) throws BadDataFileFormatException {
        if (!expected.equals(currentLine)) {
            throw new BadDataFileFormatException(
                    inputReader.sourceFile, inputReader.lineNum,
                    "Expected " + expected + ". Found" + currentLine);
        }
    }

    private void parseDataFile(CaseFileData testConf, boolean doFinishActions) throws BadDataFileFormatException {

        while (inputReader.hasNext() && move()) {
            if (currentLine.startsWith(BASE_S)) {
                parseBASE_S(testConf);
            } else if (currentLine.startsWith(BASE_V)) {
                parseBASE_V(testConf);
            } else if (currentLine.startsWith(SOLVER)) {
                parseSolver(testConf);
            } else if (currentLine.startsWith(DATDur)) {
                parseDATDur(testConf);
            } else if (currentLine.startsWith(RTOPDur)) {
                parseRTOPDur(testConf);
            } else if (currentLine.startsWith(RTKDur)) {
                parseRTKDur(testConf);
            } else if (currentLine.startsWith(MAX_DAY)) {
                parseMaxDay(testConf);
            } else if (currentLine.startsWith(BAL_PEN_POS)) {
                parseBalPenPos(testConf);
            } else if (currentLine.startsWith(BAL_PEN_NEG)) {
                parseBalPenNeg(testConf);
            } else if (currentLine.startsWith(NUMBER_OF_BUSES)) {
                parseNBuses(testConf);
            } else if (currentLine.startsWith(RANDOM_SEED)) {
                parseRandomSeed(testConf);
            } else if (currentLine.startsWith(CAP_MARGIN)) {
                parseReserveMargin(testConf);
            } else if (currentLine.startsWith(LSE_DATA_SOURCE)) {
                parseLSEDataSource(testConf);
            } else if (currentLine.startsWith(LOAD_CASE_CONTROL)) {
                parseLoadCaseControlFile(testConf);
            } else if (currentLine.startsWith(THRESH_PROB)) {
                parseThreshholdProbability(testConf);
            } else if (currentLine.startsWith(FNCS_ACTIVE)) {
                parseFNCSFlag(testConf);
            } else if (currentLine.startsWith(HAS_STORAGE)) {
                parseHasStorage(testConf);
            } else if (currentLine.startsWith(HAS_NDG)) {
                parseHasNDG(testConf);
            } else if (currentLine.startsWith(PRICE_SENSITIVE_DEMAND_FLAG)) {
                parsePriceSensitiveLSE(testConf);
//            } else if(currentLine.equals(BUS_DATA_START)) {
//                parseBusData(testConf);
            } else if (currentLine.equals(LINE_DATA_START)) {
                parseLineData(testConf);
            } else if (currentLine.equals(GEN_DATA_START)) {
                parseGenData(testConf);
            } else if (currentLine.equals(ALERT_GEN_START)) {
                parseAlertGenCos(testConf);
            } else if (currentLine.equals(SCUC_INPUT_DATA_START)) {
                parseScucInputData(testConf);
            } else if (currentLine.equals(ZONE_DATA_START)) {
                testConf.setHasReserveZoneData(true);
                parseZoneData(testConf);
            } else if (currentLine.equals(STORAGE_INPUT_DATA_START)) {
                parseStorageInputData(testConf);
            } else if (currentLine.equals(LSE_DATA_FIXED_DEM_START)) {
                parseLSEFixedDemand(testConf);
            } else if (currentLine.equals(NDG_DATA_START)) {
                parseNDGData(testConf);
            } else if (currentLine.equals(LSE_DATA_PRICE_SENS_DEM_START)) {
                parseLSEPSensDemand(testConf);
            } else if (currentLine.equals(GEN_LEARNING_DATA_START)) {
                testConf.setHasGenLearningData(true);
                parseGenLearningData(testConf);
//            } else if(currentLine.equals(ZONE_NAMES_START)){
//                parseZoneNames(testConf);
            } else if (currentLine.equals(GEN_COST_START)) {
                parseGenCoCosts(testConf);
            } else if (currentLine.startsWith(RESERVE_PERCENT_START)) {
                parseSystemReserveData(testConf);
//            } else if(currentLine.startsWith(DOWN_RESERVE_PERCENT)) {
//                parseDownReservePercent(testConf);
//            } else if(currentLine.startsWith(UP_RESERVE_PERCENT)) {
//                parseUpReservePercent(testConf);
            } else if (currentLine.startsWith(NUMBER_RESERVE_ZONES)) {
                parseNumberOfReserveZones(testConf);
            } else if (currentLine.equals(GEN_FUELTYPE_START)) {
                parseGenCoFuelType(testConf);
            } else {
                System.err.println("Unknown Line " + currentLine);
            }
        }

        if (doFinishActions) {
            finish(testConf);
        }
    }

    /**
     * Helper method to collect all of the actions that performed after the
     * TestCase file is read.
     *
     * @throws BadDataFileFormatException
     */
    private void finish(CaseFileData testConf) throws BadDataFileFormatException {

        if (!testConf.hasGenLearningData()) {
            loadDefaultGenLearningData(testConf);
        }

        if (!testConf.hasReserveZoneData()) {
            if (testConf.NumberOfReserveZones > 1) {
                System.out.println("Data for Reserve Zones is not provided, setting number of reserve zones to 1");
                testConf.NumberOfReserveZones = 1;
            }
            //setDefaultReserveZoneData(testConf);
        }

        /*
         * don't mark the canary/alert gencos until after we've read the entire file.
         * There is no reason to expect the AlertGenCo section will come
         * after GenData section.
         */
        testConf.markCanaryGenCos();

        testConf.ensureSCUCData();

        testConf.ensureLSEData();
        testConf.ensureLSEPriceSenstiveDemandData();
        testConf.ensureNDGData();

        testConf.ensureLSEHybridDemandData();

    }

    /**
     * Parse a key/value pair line. Assume the line starts with the expected key
     * and contains white space followed by the value.
     *
     * <p>
     * For example, argument "BASE_S 100" return "100".</p>
     *
     * @param line
     * @param key
     * @return the value for the pair
     * @throws BadDataFileFormatException
     */
    private String splitValueFromKey(String line, String key) throws BadDataFileFormatException {
        //Split the string on white space
        line = line.trim();
        String[] splits = line.split(WS_REG_EX);
        if (splits.length != 2) {
            throw new BadDataFileFormatException(inputReader.sourceFile,
                    inputReader.lineNum, "Expected key/value pair in " + line + ". Expected 2 items, found " + splits.length);
        } else if (!splits[0].equals(key)) {
            throw new BadDataFileFormatException(inputReader.sourceFile,
                    inputReader.lineNum, "Expected key " + key + " in line "
                    + line + "Found key " + splits[0]);
        }
        return splits[1];
    }

//    private void parseDownReservePercent(CaseFileData testConf) throws BadDataFileFormatException {
//        testConf.DownReservePercent = Support.parseDouble(
//                             splitValueFromKey(currentLine, DOWN_RESERVE_PERCENT));
//    }
//    
//    private void parseUpReservePercent(CaseFileData testConf) throws BadDataFileFormatException {
//        testConf.UpReservePercent = Support.parseDouble(
//                             splitValueFromKey(currentLine, UP_RESERVE_PERCENT));
//    }
    private void parseNumberOfReserveZones(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.NumberOfReserveZones = Integer.parseInt(
                splitValueFromKey(currentLine, NUMBER_RESERVE_ZONES));
    }

    private void parseBASE_S(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.baseS = Support.parseDouble(
                splitValueFromKey(currentLine, BASE_S));
    }

    private void parseBASE_V(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.baseV = Support.parseDouble(
                splitValueFromKey(currentLine, BASE_V));
    }

    // MaxDay
    private void parseMaxDay(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.iMaxDay = Integer.parseInt(
                splitValueFromKey(currentLine, MAX_DAY));
    }

    //parseNBuses
    private void parseNBuses(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.nodeData = new Object[1][2];

        testConf.iNodeData = Integer.parseInt(
                splitValueFromKey(currentLine, NUMBER_OF_BUSES));

        testConf.nodeData[0][0] = testConf.iNodeData;
        testConf.nodeData[0][1] = 0;

    }

    // BalPenPos
    private void parseBalPenPos(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.BalPenPos = Integer.parseInt(
                splitValueFromKey(currentLine, BAL_PEN_POS));
    }

    // BalPenPos
    private void parseBalPenNeg(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.BalPenNeg = Integer.parseInt(
                splitValueFromKey(currentLine, BAL_PEN_NEG));
    }

    // DATDur
    private void parseDATDur(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.DATDur = Support.parseDouble(splitValueFromKey(currentLine, DATDur));
    }

    // RTOPDur
    private void parseRTOPDur(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.RTOPDur = Integer.parseInt(
                splitValueFromKey(currentLine, RTOPDur));
    }

    // RTKDur
    private void parseRTKDur(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.RTKDur = Integer.parseInt(splitValueFromKey(currentLine, RTKDur));
    }

    // Solver
    private void parseSolver(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.Solver = (splitValueFromKey(currentLine, SOLVER));
    }

    /**
     * Parse the random seed field
     *
     * @param testConf
     * @throws BadDataFileFormatException
     * @throws NumberFormatException
     */
    private void parseRandomSeed(CaseFileData testConf) throws NumberFormatException, BadDataFileFormatException {
        testConf.RandomSeed = Long.parseLong(
                splitValueFromKey(currentLine, RANDOM_SEED));
    }

    /**
     * Parse the required reserve margin.
     *
     * @param testConf
     * @throws BadDataFileFormatException
     * @throws NumberFormatException
     */
    private void parseReserveMargin(CaseFileData testConf) throws NumberFormatException, BadDataFileFormatException {
        testConf.capacityMargin = Support.parseDouble(splitValueFromKey(currentLine, CAP_MARGIN));
        testConf.capacityMargin /= 100; //Convert the percentage in the file to a decimal.
    }

    private void parseLSEDataSource(CaseFileData testConf) throws BadDataFileFormatException {
        String lseData = splitValueFromKey(currentLine, LSE_DATA_SOURCE);

        if (lseData == null || "".equals(lseData)) {
            throw new BadDataFileFormatException(inputReader.sourceFile, inputReader.lineNum,
                    "No LSE demand data source found in " + currentLine);
        }

        if (LSE_DATA_TESTCASE.equals(lseData)) {
            testConf.setLSEDemandSource(CaseFileData.LSE_DEMAND_TEST_CASE);
        } else if (LSE_DATA_LOADCASE.equals(lseData)) {
            testConf.setLSEDemandSource(CaseFileData.LSE_DEMAND_LOAD_CASE);
        } else {
            throw new BadDataFileFormatException(inputReader.sourceFile, inputReader.lineNum,
                    "Unknown LSE demand data source " + lseData);
        }
    }

    /**
     * Parse the name of the LoadCase Control File.
     *
     * @param testConf
     * @throws BadDataFileFormatException
     */
    private void parseLoadCaseControlFile(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.loadCaseControlFile = splitValueFromKey(currentLine, LOAD_CASE_CONTROL);
        testConf.adjustLoadControlFilePath(testCaseFile);
    }

    /**
     * Parse the Threshold Probability field
     *
     * @param testConf
     * @throws BadDataFileFormatException
     * @throws NumberFormatException
     */
    private void parseThreshholdProbability(CaseFileData testConf) throws NumberFormatException, BadDataFileFormatException {
        testConf.dThresholdProbability = Support.parseDouble(
                splitValueFromKey(currentLine, THRESH_PROB));
    }

    private void parseFNCSFlag(CaseFileData testConf) throws NumberFormatException, BadDataFileFormatException {
        testConf.FNCSActive = Support.parseBoolean(
                splitValueFromKey(currentLine, FNCS_ACTIVE));
    }

    /**
     * Parse the Has Storage field
     *
     * @param testConf
     * @throws BadDataFileFormatException
     * @throws NumberFormatException
     */
    private void parseHasStorage(CaseFileData testConf) throws NumberFormatException, BadDataFileFormatException {
        testConf.hasStorage = Support.parseDouble(
                splitValueFromKey(currentLine, HAS_STORAGE));
    }

    /**
     * Parse the Has NDG field
     *
     * @param testConf
     * @throws BadDataFileFormatException
     * @throws NumberFormatException
     */
    private void parseHasNDG(CaseFileData testConf) throws NumberFormatException, BadDataFileFormatException {
        testConf.hasNDG = Support.parseDouble(
                splitValueFromKey(currentLine, HAS_NDG));
    }

    private void parsePriceSensitiveLSE(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.priceSensitiveDemandFlag = Integer.parseInt(
                splitValueFromKey(currentLine, PRICE_SENSITIVE_DEMAND_FLAG));
    }

    private void parseBusData(CaseFileData testConf) throws BadDataFileFormatException {
        testConf.nodeData = new Object[1][2];
        move();

        String[] splits = currentLine.split(WS_REG_EX);

        if (splits.length != 2) {
            throw new BadDataFileFormatException(inputReader.sourceFile, inputReader.lineNum, currentLine);
        }

        int numNodes = Integer.parseInt(splits[0]);
        testConf.iNodeData = numNodes;

        testConf.nodeData[0][0] = numNodes;
        testConf.nodeData[0][1] = Support.parseDouble(splits[1]);

        move();
        match(BUS_DATA_END);
    }

    private void parseLineData(CaseFileData testConf) throws BadDataFileFormatException {
        move();

//        IZoneIndexProvider zoneIdxs = testConf.getZoneNames();
        ArrayList<String> branchDataList = collectLines(LINE_DATA_END);

        int iBranchNumber = branchDataList.size();
        testConf.branchData = new Object[iBranchNumber][5];
        testConf.iBranchData = iBranchNumber;

        for (int i = 0; i < iBranchNumber; i++) {
            String strBranch = (String) branchDataList.get(i);
            int iBranchFields = 4;

            //The columns are:
            //Name  From    To  MaxCap  Reactance
            //Idxs 0, 1, 2 are strings. Idxs 3 and 4 are floating point.
            while (iBranchFields > 0) {
                int iIndex = strBranch.lastIndexOf("\t");
                if (iIndex < 0) {
                    iIndex = strBranch.lastIndexOf(" ");
                }

                String strData = strBranch.substring(iIndex + 1);
                strData = strData.trim();
                //System.out.println("strBranch: " + strBranch + " iIndex: "+ iIndex + " strData: "+strData);
                if (iBranchFields > 2) {
                    double dTemp = Support.parseDouble(strData);
                    testConf.branchData[i][iBranchFields] = String.format("%1$15.4f", dTemp);
                } else {
// Commenting zone for now
//                    if(!zoneIdxs.hasIndexForName(strData)) {
//                        unknownZoneName(strData);
//                    }
//
//                    int zidx = zoneIdxs.get(strData);      
//                    System.out.println("zidx: " + zidx);
                    testConf.branchData[i][iBranchFields] = Integer.parseInt(strData); //zidx
                }

                iBranchFields--;

                if (iIndex > 0) {
                    strBranch = strBranch.substring(0, iIndex).trim();
                }

            }

            if (strBranch.length() > 0) {
                testConf.branchData[i][0] = strBranch;
            }
        }
    }

    /**
     *
     * Collect all of the lines in the GenData section and create GenData object
     * from the fields.
     *
     * @param testConf
     * @throws BadDataFileFormatException
     */
    private void parseGenData(CaseFileData testConf) throws BadDataFileFormatException {
        move();

//        IZoneIndexProvider zoneNames = testConf.getZoneNames();
        ArrayList<String> genDataList = collectLines(GEN_DATA_END);

        int iGenNumber = genDataList.size();
        testConf.genData = new GenData[iGenNumber];
        testConf.iGenData = iGenNumber;

        for (int i = 0; i < iGenNumber; i++) {
            String strGen = (String) genDataList.get(i);

            //Split the line at white space.
            String[] lineElems = strGen.split(WS_REG_EX);
            if (lineElems.length != 11) {
                throw new BadDataFileFormatException(inputReader.sourceFile, inputReader.lineNum, strGen);
            }

            Integer atBusIdx = Integer.parseInt(lineElems[2]); //zoneNames.get(lineElems[2]);
            if (atBusIdx == null) {
                throw new BadDataFileFormatException(
                        inputReader.sourceFile,
                        inputReader.lineNum,
                        String.format("Unknown zone name %s.", lineElems[2])
                );
            }

            try {
                testConf.genData[i] = new GenData(
                        lineElems[0],//name
                        Integer.parseInt(lineElems[1]),//id
                        atBusIdx.intValue(),//atBus
                        Support.parseDouble(lineElems[3]),//sCost
                        Support.parseDouble(lineElems[4]),//a
                        Support.parseDouble(lineElems[5]),//b
                        Support.parseDouble(lineElems[6]),//c
                        Support.parseInt(lineElems[7]),//NS
                        Support.parseDouble(lineElems[8]),//PMin
                        Support.parseDouble(lineElems[9]),//PMax
                        Support.parseDouble(lineElems[10])//initMoney
                );
            } catch (NumberFormatException nfe) {
                throw new BadDataFileFormatException(inputReader.sourceFile,
                        inputReader.lineNum, nfe);
            }
        }
    }

    private void parseAlertGenCos(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        ArrayList<String> alertGenCos = collectLines(ALERT_GEN_END);
        testConf.setCanaryGenCo(alertGenCos);
    }

    /**
     * Parse/read the data for the scuc input. Assume a single data line looks
     * like: Name PowerT0 UnitOnT0State MinUpTime MinDownTime NominalRampUp
     * NominalRampDown StartupRampLim ShutdownRampLim
     *
     * @param testConf
     * @throws BadDataFileFormatException
     */
    private void parseScucInputData(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        ArrayList<String> scucData = collectLines(SCUC_INPUT_DATA_END);

        for (String s : scucData) {
            try {
                //Split the line at white space.
                String[] lineElems = s.split(WS_REG_EX);
                testConf.putScucData(lineElems[0],
                        numRecog.stod(lineElems[1]),//PowerT0
                        numRecog.stoi(lineElems[2]),//UnitOnT0State
                        numRecog.stoi(lineElems[3]),//MinUp
                        numRecog.stoi(lineElems[4]),//MinDown
                        numRecog.stod(lineElems[5]),//NominalRampUp
                        numRecog.stod(lineElems[6]),//NominalRampDown
                        numRecog.stod(lineElems[7]),//StartupRampLim
                        numRecog.stod(lineElems[8])//ShutdownRampLim
                //                        numRecog.stoi(lineElems[9]),//Schedule
                //                        numRecog.stoi(lineElems[10])//Schedule2
                );
            } catch (NumberFormatException nfe) {
                throw new BadDataFileFormatException(inputReader.sourceFile,
                        inputReader.lineNum,
                        "Problem in ScucInputData section. " + nfe.getMessage());
            }
        }
    }

    private void parseSystemReserveData(CaseFileData testConf) throws BadDataFileFormatException {

        move();
        String ReserveData = inputReader.nextLine();

        //Split the line at white space.
        String[] lineElems = ReserveData.split(WS_REG_EX);

        testConf.DARRD = Support.parseDouble(lineElems[0]);  //DownReservePercent
        testConf.DARRU = Support.parseDouble(lineElems[1]);    //UpReservePercent
        testConf.RTRRD = Support.parseDouble(lineElems[2]); // Real Time Down Reserve Percent  
        testConf.RTRRU = Support.parseDouble(lineElems[3]); // Real Time Up Reserve Percent 

        move();
    }

    private void parseZoneData(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        ArrayList<String> ZoneData = collectLines(ZONE_DATA_END);

        for (String s : ZoneData) {
            try {
                //Split the line at white space.
                String[] lineElems = s.split(WS_REG_EX);
                //System.out.println(""+lineElems[1]); 
                String[] sTemp = lineElems[1].split(",");
                int[] tempArray = new int[sTemp.length];
                //System.out.println(""+Arrays.toString(sTemp));
                for (int i = 0; i < sTemp.length; i++) {
                    //System.out.println("sTemp[i]:"+sTemp[i]);
                    tempArray[i] = Integer.parseInt(sTemp[i]);
                    //System.out.println("tempArray[i]:"+tempArray[i]);
                }
                System.out.println("");
                testConf.putZoneData(lineElems[0],
                        tempArray,//Buses
                        numRecog.stod(lineElems[2]),//Zonal DAM DownReservePercent
                        numRecog.stod(lineElems[3]),//Zonal DAM  UpReservePercent
                        numRecog.stod(lineElems[4]),//Zonal RTM DownReservePercent
                        numRecog.stod(lineElems[5])//Zonal RTM UpReservePercent
                );
            } catch (NumberFormatException nfe) {
                throw new BadDataFileFormatException(inputReader.sourceFile,
                        inputReader.lineNum,
                        "Problem in ZoneData section. " + nfe.getMessage());
            }
        }
    }

    /**
     * Parse/read the data for the Storage input. Assume a single data line
     * looks like: Name PowerT0 UnitOnT0State MinUpTime MinDownTime
     * NominalRampUp NominalRampDown StartupRampLim ShutdownRampLim
     *
     * @param testConf
     * @throws BadDataFileFormatException
     */
    private void parseStorageInputData(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        ArrayList<String> storageData = collectLines(STORAGE_INPUT_DATA_END);

        for (String s : storageData) {
            try {
                //Split the line at white space.
                String[] lineElems = s.split(WS_REG_EX);
                testConf.putStorageData(numRecog.stoi(lineElems[1]),//ID
                        numRecog.stoi(lineElems[2]),//atBus
                        numRecog.stod(lineElems[3]),//EndPointSoc
                        numRecog.stod(lineElems[4]),//MaximumEnergy
                        numRecog.stod(lineElems[5]),//NominalRampDownInput
                        numRecog.stod(lineElems[6]),//NominalRampUpInput
                        numRecog.stod(lineElems[7]),//NominalRampDownOutput
                        numRecog.stod(lineElems[8]),//NominalRampUpOutput
                        numRecog.stod(lineElems[9]),//MaximumPowerInput
                        numRecog.stod(lineElems[10]),//MinimumPowerInput
                        numRecog.stod(lineElems[11]),//MaximumPowerOutput
                        numRecog.stod(lineElems[12]),//MinimumPowerOutput
                        numRecog.stod(lineElems[13]),//MinimumSoc
                        numRecog.stod(lineElems[14])//EfficiencyEnergy
                );
            } catch (NumberFormatException nfe) {
                throw new BadDataFileFormatException(inputReader.sourceFile,
                        inputReader.lineNum,
                        "Problem in StorageInputData section. " + nfe.getMessage());
            }
        }
    }

    private void parseLSEFixedDemand(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        final ArrayList<String> LSEDataList = collectLines(LSE_DATA_FIXED_DEM_END);
//        final IZoneIndexProvider zip = testConf.getZoneNames();

        int iLSENumber = LSEDataList.size() / 3;
        testConf.lseSec1Data = new Object[iLSENumber][11];
        testConf.lseSec2Data = new Object[iLSENumber][11];
        testConf.lseSec3Data = new Object[iLSENumber][11];
        testConf.lseData = new Object[iLSENumber][27];
        testConf.iLSEData = iLSENumber;

        for (int i = 0; i < iLSENumber * 3; i++) {
            String strLSE = (String) LSEDataList.get(i);
            int iLSEFields = 10;
            int iLSEIndex = i % iLSENumber;

            while (iLSEFields > 0) {
                int iIndex = strLSE.lastIndexOf("\t");
                if (iIndex < 0) {
                    iIndex = strLSE.lastIndexOf(" ");
                }

                String strData = strLSE.substring(iIndex + 1);
                strData = strData.trim();

                if (i < iLSENumber) {
                    if (iLSEFields > 2) {
                        double dTemp = Support.parseDouble(strData);
                        testConf.lseSec1Data[iLSEIndex][iLSEFields] = String.format("%1$15.4f", dTemp);
                        testConf.lseData[iLSEIndex][iLSEFields] = String.format("%1$15.4f", dTemp);
                    } else {
                        //Special Cases:
                        //when ILSE fields is 2, we need to lookup an index from the name
                        if (iLSEFields == 2) {
                            testConf.lseSec1Data[iLSEIndex][iLSEFields] = Integer.parseInt(strData);
                            testConf.lseData[iLSEIndex][iLSEFields] = Integer.parseInt(strData);
                        } else { //otherwise, it is just an Int.
                            int id = Integer.parseInt(strData);
                            testConf.lseSec1Data[iLSEIndex][iLSEFields] = id;
                            testConf.lseData[iLSEIndex][iLSEFields] = id;
                        }
                    }
                } else if ((i >= iLSENumber) && (i < 2 * iLSENumber)) { //Process columns 3 - 8
                    if (iLSEFields > 2) {
                        double dTemp = Support.parseDouble(strData);
                        testConf.lseSec2Data[iLSEIndex][iLSEFields] = String.format("%1$15.4f", dTemp);
                        testConf.lseData[iLSEIndex][iLSEFields + 8] = String.format("%1$15.4f", dTemp);
                    } else {
                        //Special Cases:
                        //when ILSE fields is 2, we need to lookup an index from the name
                        if (iLSEFields == 2) {
//                            if(zip.hasIndexForName(strData)) {
//                                int zidx = zip.get(strData);
                            testConf.lseSec2Data[iLSEIndex][iLSEFields] = Integer.parseInt(strData); //zidx;
//                            } else {
//                                unknownZoneName(strData);
//                            }
                        } else {
                            testConf.lseSec2Data[iLSEIndex][iLSEFields] = Integer.parseInt(strData);
                        }
                    }

                } else if (i >= 2 * iLSENumber) {
                    if (iLSEFields > 2) {
                        double dTemp = Support.parseDouble(strData);
                        testConf.lseSec3Data[iLSEIndex][iLSEFields] = String.format("%1$15.4f", dTemp);
                        testConf.lseData[iLSEIndex][iLSEFields + 16] = String.format("%1$15.4f", dTemp);
                    } else {
                        //Special Cases:
                        //when ILSE fields is 2, we need to lookup an index from the name
                        if (iLSEFields == 2) {
//                            if(zip.hasIndexForName(strData)) {
//                                int zidx = zip.get(strData);
                            testConf.lseSec3Data[iLSEIndex][iLSEFields] = Integer.parseInt(strData); //zidx;
//                            } else {
//                                unknownZoneName(strData);
//                            }
                        } else {
                            testConf.lseSec3Data[iLSEIndex][iLSEFields] = Integer.parseInt(strData);
                        }
                    }
                }

                iLSEFields--;

                if (iIndex > 0) {
                    strLSE = strLSE.substring(0, iIndex).trim();
                }
            }

            if (i < iLSENumber) {
                if (strLSE.length() > 0) {
                    testConf.lseSec1Data[iLSEIndex][0] = strLSE;
                    testConf.lseData[iLSEIndex][0] = strLSE;
                }
            } else if ((i >= iLSENumber) && (i < 2 * iLSENumber)) {
                if (strLSE.length() > 0) {
                    testConf.lseSec2Data[iLSEIndex][0] = strLSE;
                }
            } else if (i >= 2 * iLSENumber) {
                if (strLSE.length() > 0) {
                    testConf.lseSec3Data[iLSEIndex][0] = strLSE;
                }
            }
        }
    }

    private void parseNDGData(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        final ArrayList<String> NDGDataList = collectLines(NDG_DATA_END);
//        final IZoneIndexProvider zip = testConf.getZoneNames();

        // System.out.println("NDGDataList: " + NDGDataList.toString());
        int iNDGNumber = NDGDataList.size() / 3;
        //System.out.println("iNDGNumber: " + iNDGNumber);
        testConf.NDGSec1Data = new Object[iNDGNumber][11];
        testConf.NDGSec2Data = new Object[iNDGNumber][11];
        testConf.NDGSec3Data = new Object[iNDGNumber][11];
        testConf.NDGData = new Object[iNDGNumber][27];
        testConf.iNDGData = iNDGNumber;

        for (int i = 0; i < iNDGNumber * 3; i++) {
            String strNDG = (String) NDGDataList.get(i);
            int iNDGFields = 10;
            int iNDGIndex = i % iNDGNumber;

            while (iNDGFields > 0) {
                int iIndex = strNDG.lastIndexOf("\t");
                if (iIndex < 0) {
                    iIndex = strNDG.lastIndexOf(" ");
                }

                String strData = strNDG.substring(iIndex + 1);
                strData = strData.trim();

                if (i < iNDGNumber) {
                    if (iNDGFields > 2) {
                        double dTemp = Support.parseDouble(strData);
                        testConf.NDGSec1Data[iNDGIndex][iNDGFields] = String.format("%1$15.4f", dTemp);
                        testConf.NDGData[iNDGIndex][iNDGFields] = String.format("%1$15.4f", dTemp);
                    } else {
                        //Special Cases:
                        //when INDGfields is 2, we need to lookup an index from the name
                        if (iNDGFields == 2) {
//                            if(zip.hasIndexForName(strData)) {
//                                int zidx = zip.get(strData);
                            testConf.NDGSec1Data[iNDGIndex][iNDGFields] = Integer.parseInt(strData); //zidx;
                            testConf.NDGData[iNDGIndex][iNDGFields] = Integer.parseInt(strData); //zidx;
//                            } else {
//                                unknownZoneName(strData);
//                            }
                        } else { //otherwise, it is just an Int.
                            int id = Integer.parseInt(strData);
                            testConf.NDGSec1Data[iNDGIndex][iNDGFields] = id;
                            testConf.NDGData[iNDGIndex][iNDGFields] = id;
                        }
                    }
                } else if ((i >= iNDGNumber) && (i < 2 * iNDGNumber)) { //Process columns 3 - 8
                    if (iNDGFields > 2) {
                        double dTemp = Support.parseDouble(strData);
                        testConf.NDGSec2Data[iNDGIndex][iNDGFields] = String.format("%1$15.4f", dTemp);
                        testConf.NDGData[iNDGIndex][iNDGFields + 8] = String.format("%1$15.4f", dTemp);
                    } else {
                        //Special Cases:
                        //when INDGfields is 2, we need to lookup an index from the name
                        if (iNDGFields == 2) {
//                            if(zip.hasIndexForName(strData)) {
//                                int zidx = zip.get(strData);
                            testConf.NDGSec2Data[iNDGIndex][iNDGFields] = Integer.parseInt(strData); //zidx;
//                            } else {
//                                unknownZoneName(strData);
//                            }
                        } else {
                            testConf.NDGSec2Data[iNDGIndex][iNDGFields] = Integer.parseInt(strData);
                        }
                    }

                } else if (i >= 2 * iNDGNumber) {
                    if (iNDGFields > 2) {
                        double dTemp = Support.parseDouble(strData);
                        testConf.NDGSec3Data[iNDGIndex][iNDGFields] = String.format("%1$15.4f", dTemp);
                        testConf.NDGData[iNDGIndex][iNDGFields + 16] = String.format("%1$15.4f", dTemp);
                    } else {
                        //Special Cases:
                        //when INDG fields is 2, we need to lookup an index from the name
                        if (iNDGFields == 2) {
//                            if(zip.hasIndexForName(strData)) {
//                                int zidx = zip.get(strData);
                            testConf.NDGSec3Data[iNDGIndex][iNDGFields] = Integer.parseInt(strData); //zidx;
//                            } else {
//                                unknownZoneName(strData);
//                            }
                        } else {
                            testConf.NDGSec3Data[iNDGIndex][iNDGFields] = Integer.parseInt(strData);
                        }
                    }
                }

                iNDGFields--;

                if (iIndex > 0) {
                    strNDG = strNDG.substring(0, iIndex).trim();
                }
            }

            if (i < iNDGNumber) {
                if (strNDG.length() > 0) {
                    testConf.NDGSec1Data[iNDGIndex][0] = strNDG;
                    testConf.NDGData[iNDGIndex][0] = strNDG;
                }
            } else if ((i >= iNDGNumber) && (i < 2 * iNDGNumber)) {
                if (strNDG.length() > 0) {
                    testConf.NDGSec2Data[iNDGIndex][0] = strNDG;
                }
            } else if (i >= 2 * iNDGNumber) {
                if (strNDG.length() > 0) {
                    testConf.NDGSec3Data[iNDGIndex][0] = strNDG;
                }
            }
        }
    }

    private void parseLSEPSensDemand(CaseFileData testConf) throws BadDataFileFormatException {
        move();

        ArrayList<String> LSEPriceDemandDataList = collectLines(LSE_DATA_PRICE_SENS_DEM_END);

        int iLSEPSDemandNumber = LSEPriceDemandDataList.size() / HOURS_PER_DAY;
        testConf.lsePriceSensitiveDemand = new Object[testConf.iLSEData][HOURS_PER_DAY][PSL_PARAM_LEN];

        for (int i = 0; i < iLSEPSDemandNumber; i++) {
            for (int j = 0; j < HOURS_PER_DAY; j++) {
                String strLSEDemand = (String) LSEPriceDemandDataList.get(i * HOURS_PER_DAY + j);
                int NumParam = strLSEDemand.split(WS_REG_EX).length;
                if (NumParam <= (PSL_PARAM_LEN - 1)) {
                        testConf.lsePriceSensitiveDemand[i][j][PSL_PARAM_LEN - 1] = 5;
                    }
                int iDemandFields = NumParam - 1;
                while (iDemandFields > 0) {
                    int iIndex = strLSEDemand.lastIndexOf("\t");
                    if (iIndex < 0) {
                        iIndex = strLSEDemand.lastIndexOf(" ");
                    }

                    String strData = strLSEDemand.substring(iIndex + 1);
                    strData = strData.trim();

                    if (iDemandFields == (PSL_PARAM_LEN - 1)) {
                        testConf.lsePriceSensitiveDemand[i][j][iDemandFields] = Integer.parseInt(strData);
                    } else if (iDemandFields > 3) {
                        testConf.lsePriceSensitiveDemand[i][j][iDemandFields] = Support.parseDouble(strData);
                    } else {
                        if (NumParam == (PSL_PARAM_LEN - 2)) {
                            double f = (double) testConf.lsePriceSensitiveDemand[i][j][PSL_PARAM_LEN - 3];
                            double e = (double) testConf.lsePriceSensitiveDemand[i][j][PSL_PARAM_LEN - 4];
                            testConf.lsePriceSensitiveDemand[i][j][PSL_PARAM_LEN - 2] = e / (2 * f);
                        }
                        testConf.lsePriceSensitiveDemand[i][j][iDemandFields] = Integer.parseInt(strData);
                    }

                    iDemandFields--;

                    if (iIndex > 0) {
                        strLSEDemand = strLSEDemand.substring(0, iIndex).trim();
                    }
                }

                if (strLSEDemand.length() > 0) {
                    testConf.lsePriceSensitiveDemand[i][j][0] = strLSEDemand;
                }

            }
        }
        /*
        for (int i = 0; i < iLSEPSDemandNumber; i++) {
            for (int j = 0; j < HOURS_PER_DAY; j++) {
                for (int k = 0; k < PSL_PARAM_LEN; k++) {
                    System.out.print(" : " + testConf.lsePriceSensitiveDemand[i][j][k]);
                }
                System.out.println("");
            }
        } */

        if (iLSEPSDemandNumber < testConf.iLSEData) {
            for (int l = iLSEPSDemandNumber; l < testConf.iLSEData; l++) {
                for (int h = 0; h < HOURS_PER_DAY; h++) {
                    testConf.lsePriceSensitiveDemand[l][h][0] = testConf.lseData[l][0]; //NAME
                    testConf.lsePriceSensitiveDemand[l][h][1] = testConf.lseData[l][1]; //ID
                    testConf.lsePriceSensitiveDemand[l][h][2] = testConf.lseData[l][2]; //BUS
                    testConf.lsePriceSensitiveDemand[l][h][3] = h;             //HOUR
                    testConf.lsePriceSensitiveDemand[l][h][4] = 0;             //d
                    testConf.lsePriceSensitiveDemand[l][h][5] = 0;             //e
                    testConf.lsePriceSensitiveDemand[l][h][6] = 0;             //f
                    testConf.lsePriceSensitiveDemand[l][h][7] = 0;             //PMax
                    testConf.lsePriceSensitiveDemand[l][h][8] = 0;             //NS
                }
            }
        }
    }

    private void parseGenLearningData(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        ArrayList<String> genLearningDataList = collectLines(GEN_LEARNING_DATA_END);

        int iGenNumber = genLearningDataList.size();
        testConf.genLearningData = new double[iGenNumber][12];
        testConf.iGenData = iGenNumber;

        for (int i = 0; i < iGenNumber; i++) {
            String strGenLearning = (String) genLearningDataList.get(i);
            int iGenLearningFields = 11;

            while (iGenLearningFields >= 0) {
                int iIndex = strGenLearning.lastIndexOf("\t");
                if (iIndex < 0) {
                    iIndex = strGenLearning.lastIndexOf(" ");
                }

                String strData = strGenLearning.substring(iIndex + 1);
                strData = strData.trim();

                double dTemp = Support.parseDouble(strData);
                testConf.genLearningData[i][iGenLearningFields] = dTemp;

                iGenLearningFields--;

                if (iIndex >= 0) {
                    strGenLearning = strGenLearning.substring(0, iIndex).trim();
                }
            }

        }
    }

    private void loadDefaultGenLearningData(CaseFileData testConf) {
        DefaultSimulationParameters defSimParams = new DefaultSimulationParameters();
        testConf.genLearningData = new double[testConf.iGenData][12];

        for (int i = 0; i < testConf.iGenData; i++) {
            testConf.genLearningData[i][0] = defSimParams.Default_InitPropensity;
            testConf.genLearningData[i][1] = defSimParams.Default_Cooling;
            testConf.genLearningData[i][2] = defSimParams.Default_Recency;
            testConf.genLearningData[i][3] = defSimParams.Default_Experimentation;
            testConf.genLearningData[i][4] = defSimParams.Default_M1;
            testConf.genLearningData[i][5] = defSimParams.Default_M2;
            testConf.genLearningData[i][6] = defSimParams.Default_M3;
            testConf.genLearningData[i][7] = defSimParams.Default_RI_MAX_Lower;
            testConf.genLearningData[i][8] = defSimParams.Default_RI_MAX_Upper;
            testConf.genLearningData[i][9] = defSimParams.Default_RI_MIN_C;
            testConf.genLearningData[i][10] = defSimParams.Default_SlopeStart;
            testConf.genLearningData[i][11] = defSimParams.Default_iSCostExcludedFlag;
        }
    }

    /*
    private void setDefaultReserveZoneData(CaseFileData testConf) {

       int NBus = testConf.iNodeData;
       int[] BusArray = new int[NBus];
       for(int n=0; n<NBus; n++){
           BusArray[n] = n+1;
       }
       testConf.putZoneData("Zone1",
                        BusArray,//Buses
                        0.0,//ZonalDownReservePercent
                        0.0 //ZonalUpReservePercent
                );
    }
     */
//    private void parseZoneNames(CaseFileData testConf) throws BadDataFileFormatException {
//        move();
//        ArrayList<String> zoneNames = collectLines(ZONE_NAMES_END);
//        System.out.println("zoneNames: " + zoneNames.toString());
//        int idx = 1;
//        for(String zoneName : zoneNames) {
//            testConf.addZoneNameMapping(zoneName, idx);
//            idx++; //increment index for next zone.
//        }
//    }
    private void parseGenCoFuelType(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        ArrayList<String> fuelTypes = collectLines(GEN_FUELTYPE_END);

        for (String fuelType : fuelTypes) {
            String[] p = fuelType.split(WS_REG_EX);
            testConf.addFuelType(p[0], p[1]);
        }
    }

    private void parseGenCoCosts(CaseFileData testConf) throws BadDataFileFormatException {
        move();
        ArrayList<String> zoneNoLoads = collectLines(GEN_COST_END);

        for (String noLoad : zoneNoLoads) {
            String[] p = noLoad.split(WS_REG_EX);
            if (p.length != 5) {
                throw new BadDataFileFormatException(
                        "Expected 5 fields in '" + noLoad + "'. Found " + p.length + "."
                );
            }
            try {
                testConf.addColdStartTime(p[0], Support.parseInt(p[1]));
                testConf.addColdStartUpCost(p[0], Support.parseDouble(p[2]));
                testConf.addHotStartUpCost(p[0], Support.parseDouble(p[3]));
                testConf.addShutDownCost(p[0], Support.parseDouble(p[4]));
            } catch (Exception e) {
                throw new BadDataFileFormatException(inputReader.sourceFile,
                        inputReader.lineNum, currentLine, e);
            }
        }
    }

    /**
     * Handle finding an unknown zone name.
     *
     * @param zoneName
     * @throws BadDataFileFormatException
     */
    private void unknownZoneName(String zoneName) throws BadDataFileFormatException {
        throw new BadDataFileFormatException(
                inputReader.sourceFile,
                inputReader.lineNum,
                String.format("Unknown zone name %s.", zoneName)
        );
    }

    private ArrayList<String> collectLines(String endMarker) throws BadDataFileFormatException {
        ArrayList<String> lines = new ArrayList<String>();

        while (!endMarker.equals(currentLine)) {
            if (!inputReader.hasNext()) {
                throw new BadDataFileFormatException("Unexpected end of file. Is a data section end marker missing?");
            }
            lines.add(currentLine);
            move();
        }

        return lines;
    }

    /**
     * A simple line reader which skips any line which starts with a comment
     * delimiter.
     */
    private static class SimpleLineReader {

        Scanner scanner;
        int lineNum = 0;
        File sourceFile = null;

        public SimpleLineReader(File inputFile) {
            try {
                scanner = new Scanner(inputFile);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CaseFileReader.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.sourceFile = inputFile;
        }

        public SimpleLineReader(Reader inputReader) {
            scanner = new Scanner(inputReader);
        }

        String nextLine() {
            String line = "";
            boolean scanMore = false;
            do {
                ++lineNum;
                scanMore = false;
                if (!scanner.hasNext()) {
                    line = null;
                    break;
                }

                line = scanner.nextLine();

                line = line.trim();
                if (line.length() == 0) {
                    scanMore = true;
                }

                int iCommentIndex = line.indexOf("//");
                if (iCommentIndex == 0) {
                    scanMore = true;
                }
            } while (scanMore);

            return line;
        }

        boolean hasNext() {
            return scanner.hasNext();
        }

        void close() {
            scanner.close();
        }
    }
}
