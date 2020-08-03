
package amesmarket.extern.psst;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import amesmarket.AMESMarket;
import amesmarket.AMESMarketException;
import amesmarket.CaseFileData;
import amesmarket.CaseFileData.StorageInputData;
import amesmarket.GenAgent;
import amesmarket.ISO;
import amesmarket.LSEAgent;
import amesmarket.NDGenAgent;
import amesmarket.StorageAgent;
import amesmarket.extern.common.CommitmentDecision;
import java.text.DecimalFormat;
import java.util.Arrays;

/**
 *
 * Write the data files used to transfer information over to the various coopr
 * programs.
 *
 *
 */
public class DataFileWriter {



    /**
     *
     * @param fileObj
     * @param ames
     * @param day
     * @param LoadProfileLSE
     * @param GenProfileNDG
     * @param numTimeSteps
     * @throws AMESMarketException
     */
    public void writeScucScenDatFile(File fileObj, AMESMarket ames, int day, double[][] LoadProfileLSE, double[][] GenProfileNDG, double[][][] PSLDemandBidLSE, double[][] supplyOfferByGen, int numTimeSteps) throws AMESMarketException {
        //set up all the elements we need.
        //final int numHoursPerDay = ames.NUM_HOURS_PER_DAY;
//        final int numIntervalsInSim = ames.NUM_HOURS_PER_DAY_UC;
        final ISO iso = ames.getISO();
        final int numNodes = ames.getNumNodes();
        final double baseS = ames.getBaseS();
        final double baseV = ames.getBaseV();
        final int numGenAgents = ames.getNumGenAgents();
        final int numLSEAgents = ames.getNumLSEAgents();
        final int numNDGAgents = ames.getNumNDGAgents();

        final double[][] branchIndex;
        final double[][] numBranchData;
        final double DownReservePercent;
        final double UpReservePercent;
        final double hasStorage;
        final double hasNDG;

        if (!ensureFileParentExists(fileObj)) {
            throw new AMESMarketException("Could not create the directory for " + fileObj.getPath());
        }

        numBranchData = ames.getBranchData();
        DownReservePercent = ames.getDARRD();
        UpReservePercent = ames.getDARRU();
        branchIndex = ames.getTransGrid().getBranchIndex();
        hasStorage = ames.gethasStorage();
        hasNDG = 0; //ames.gethasNDG();


        //Now that we have all the parameters. Write it out.
        try {

            BufferedWriter refBufferWriter = new BufferedWriter(new FileWriter(fileObj));

            refBufferWriter.write("# Written by AMES per unit ");
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MM/dd/yyyy HH:mm:ss\n\n");
            Date date = new Date();
            refBufferWriter.write(dateFormat.format(date));

            refBufferWriter.write("set Buses := ");

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("Bus" + (i + 1) + " ");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set TransmissionLines :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write("Bus" + (int) branchIndex[i][0] + " Bus"
                        + (int) branchIndex[i][1] + "\n");
            }

            refBufferWriter.write(";\n\n");

            refBufferWriter.write("param NumTransmissionLines := "
                    + branchIndex.length + " ;\n\n");

            refBufferWriter
                    .write("param: BusFrom BusTo ThermalLimit Reactance :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write((i + 1) + " Bus"
                        + (int) numBranchData[i][0] + " Bus"
                        + (int) numBranchData[i][1] + " " + numBranchData[i][2]/ baseS
                        + " " + (numBranchData[i][3] * baseS/ (baseV * baseV)) + "\n");
            }

            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set ThermalGenerators := ");

            for (GenAgent gc : ames.getGenAgentList()) {
                refBufferWriter.write(gc.getID() + " ");
            }

            refBufferWriter.write(";\n\n");

            GenAgent[][] genNodeBusTable = new GenAgent[numNodes][numGenAgents];
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numGenAgents; j++) {
                    GenAgent gen = (GenAgent) ames.getGenAgentList().get(j);
                    //System.out.println(gen.getAtNode());

                    if (gen.getAtNode() - 1 == i) {
                        genNodeBusTable[i][j] = gen;
                    } else {
                        genNodeBusTable[i][j] = null;
                    }
                }
            }

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("set ThermalGeneratorsAtBus[Bus"
                        + (i + 1) + "] := ");

                for (int j = 0; j < numGenAgents; j++) {
                    if (genNodeBusTable[i][j] != null) {
                        refBufferWriter.write(genNodeBusTable[i][j].getID() + " ");
                    }

                }

                refBufferWriter.write(" ;\n");
            }

            int BalPenPos = ames.getTestCaseConfig().BalPenPos ;
            int BalPenNeg = ames.getTestCaseConfig().BalPenNeg ;

            refBufferWriter.write("\nparam BalPenPos := " + BalPenPos
                    + " ;\n\n");

            refBufferWriter.write("\nparam BalPenNeg := " + BalPenNeg
                    + " ;\n\n");

            // int TimePeriodLength = 1;
            double TimePeriodLength = ames.getTestCaseConfig().DATDur;
            refBufferWriter.write("\nparam TimePeriodLength := " + TimePeriodLength
                    + " ;\n\n");

            refBufferWriter.write("\nparam NumTimePeriods := " + numTimeSteps
                    + " ;\n\n");


            refBufferWriter
                    .write("param: PowerGeneratedT0 UnitOnT0State InitialTimeON InitialTimeOFF MinimumPowerOutput MaximumPowerOutput ScaledMinimumUpTime ScaledMinimumDownTime ScaledRampUpLimit ScaledRampDownLimit ScaledStartupRampLimit ScaledShutdownRampLimit ScaledColdStartTime ColdStartCost HotStartCost ShutdownCost :=\n");

            for (GenAgent ga : ames.getGenAgentList()) {
                refBufferWriter.write(genAgentToSCUCDesc(ga, day, ames.getTestCaseConfig().DATDur, numTimeSteps, baseS, true));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");

            refBufferWriter
                    .write("param: ID atBus EndPointSoc MaximumEnergy ScaledRampDownInput ScaledRampUpInput ScaledRampDownOutput ScaledRampUpOutput MaximumPowerInput MinimumPowerInput MaximumPowerOutput MinimumPowerOutput MinimumSoc EfficiencyEnergy :=\n");

            for (StorageAgent su : ames.getStorageAgentList()) {
                refBufferWriter.write(StorageAgentScucToFile(ames.getTestCaseConfig().DATDur, su, day, baseS));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");
            
            refBufferWriter.write("param PriceSenLoadFlag := "
                + ames.getPriceSensitiveDemandFlag() + " ;\n\n");

            refBufferWriter.write("param StorageFlag := "
                    + hasStorage + " ;\n\n");

            refBufferWriter.write("param DownReservePercent := "
                    + DownReservePercent + " ;\n\n");

            refBufferWriter.write("param UpReservePercent := "
                    + UpReservePercent + " ;\n\n");

            boolean HasZonalReserves = false;
            if(ames.getTestCaseConfig().NumberOfReserveZones > 1){
                HasZonalReserves = true;
            }            
            
            if(HasZonalReserves) {
            refBufferWriter.write("param HasZonalReserves := "
                    + HasZonalReserves + " ;\n\n");
            
            refBufferWriter.write("param NumberOfZones := "
                    + ames.getTestCaseConfig().NumberOfReserveZones + " ;\n\n");


            refBufferWriter.write("set Zones := ");

            for (int i = 0; i < ames.getTestCaseConfig().NumberOfReserveZones; i++) {
                refBufferWriter.write("Zone" + (i + 1) + " ");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter
                    .write("param: Buses ZonalDownReservePercent ZonalUpReservePercent :=\n");

            for (String zName : ames.getTestCaseConfig().getZonalData().keySet()) {
                CaseFileData.ZonalData ZoneData = ames.getTestCaseConfig().getZonalData().get(zName);
                int[] buses = ZoneData.getBuses();
                String stemp = "";
                for (int i = 0; i < buses.length; i++) {
                    stemp = stemp + "Bus" + buses[i] + ",";
                }
                refBufferWriter.write(zName + " " + stemp + " " + ZoneData.getZonalDARRD() + " " + ZoneData.getZonalDARRU());
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(";\n\n");

            }

            refBufferWriter.write("param: NetFixedLoadForecast :=\n");

            //System.out.println("LoadProfileLSE length: " + LoadProfileLSE[0].length);
            double[][] NetFixedLoadForecast = new double[numNodes][numTimeSteps];
            for (int n = 0; n < numNodes; n++) {
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseNode = lse.getAtNode();
                    if ((n+1) == lseNode) {
                        for (int h = 0; h < numTimeSteps; h++) {
                            NetFixedLoadForecast[n][h] = NetFixedLoadForecast[n][h] + LoadProfileLSE[i][h];
                        }
                    }
                }
                
                for (int i = 0; i < numNDGAgents; i++) {
                    NDGenAgent ndg = ames.getNDGenAgentList().get(i);
                    int ndgNode = ndg.getAtNode();
                    if ((n+1) == ndgNode) {
                        for (int h = 0; h < numTimeSteps; h++) {
//                            NetDemand[n][h] = NetDemand[n][h]; // - GenProfileNDG[i][h];
                            NetFixedLoadForecast[n][h] = NetFixedLoadForecast[n][h] - GenProfileNDG[i][h];
                        }
                    }
                }

                for (int h = 0; h < numTimeSteps; h++) {
                    refBufferWriter.write("Bus" + (n+1) + " " + (h + 1) + " "
                            + (NetFixedLoadForecast[n][h] / baseS) + "\n");
                }
                refBufferWriter.write("\n");
            }
            
            refBufferWriter.write("; \n\n");
            

            if (ames.getPriceSensitiveDemandFlag() > 0) { 
                refBufferWriter.write("set PricesSensitiveLoadNames :=");
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseNode = lse.getAtNode();
                    if (i > 0)
                      refBufferWriter.write(", ");
                    refBufferWriter.write("LSE" + lseNode);
                }
                refBufferWriter.write(";\n");

                refBufferWriter.write("param: Name ID atBus hourIndex d e f SLMax NS :=\n");
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseID = lse.getID();

                    double[][] bid = PSLDemandBidLSE[i];
                    for (int h = 0; h < bid.length; h++) {
                        //System.out.print(" h: "+ h + " LoadProfileLSE[i][h]: " + LoadProfileLSE[i][h]);
                        refBufferWriter.write("LSE" + lseID + " " + lseID + " Bus" + lse.getAtNode() + " " + (h + 1)  + " "
                            + bid[h][0] + " "
                            + bid[h][1] * baseS + " "
                            + bid[h][2] * baseS * baseS + " "
                            + bid[h][3]/baseS + " "
                            + (int) bid[h][4] + "\n");
                    }
                    refBufferWriter.write("\n");
                }
                refBufferWriter.write(";\n\n");
            }

            
            refBufferWriter.write("param: a b c NS :=\n");

            final ArrayList<GenAgent> genagents = ames.getGenAgentList();
            for (int i = 0; i < numGenAgents; i++) {
                GenAgent ga = genagents.get(i);
                refBufferWriter.write(
                        ga.getID() + " "
                        + supplyOfferByGen[i][0] + " "
                        + (supplyOfferByGen[i][1] * baseS) + " "
                        + (supplyOfferByGen[i][2] * baseS * baseS) + " "
                        + (int) supplyOfferByGen[i][3] + " " + "\n");
            }

            refBufferWriter.write("; \n");

            refBufferWriter.close();
        } catch (IOException e) {
            throw new AMESMarketException("Unable to write the reference model.", e);
        }
    }

    /**
     *
     * @param fileObj
     * @param ames
     * @param min
     * @param hour
     * @param day
     * @param LoadProfileLSE
     * @param GenProfileNDG
     * @param numTimeSteps
     * @throws AMESMarketException
     */
    public void writeScedScenDatFile(File fileObj, AMESMarket ames, int min, int hour, int day, double[][] LoadProfileLSE, double[][] GenProfileNDG, double[][][] PSLDemandBidLSE, int numTimeSteps) throws AMESMarketException {
        //set up all the elements we need.
        // Important: Resolve the minimum up time and minimum down time values of the generators

        //final int numHoursPerDay = ames.NUM_HOURS_PER_DAY;
//        final int numIntervalsInSim = ames.NUM_HOURS_PER_DAY_UC;
        final ISO iso = ames.getISO();
        final int numNodes = ames.getNumNodes();
        final double baseS = ames.getBaseS();
        final double baseV = ames.getBaseV();
        final int numGenAgents = ames.getNumGenAgents();
        final int numLSEAgents = ames.getNumLSEAgents();
        final int numNDGAgents = ames.getNumNDGAgents();

        final double[][] branchIndex;
        final double[][] numBranchData;
        final double DownReservePercent;
        final double UpReservePercent;
        final double hasStorage;
        final double hasNDG;

        if (!ensureFileParentExists(fileObj)) {
            throw new AMESMarketException("Could not create the directory for " + fileObj.getPath());
        }

        numBranchData = ames.getBranchData();
        DownReservePercent = ames.getRTRRD(); 
        UpReservePercent = ames.getRTRRU(); 
        branchIndex = ames.getTransGrid().getBranchIndex();
        hasStorage = ames.gethasStorage();
        hasNDG = 0; //ames.gethasNDG();


        //Now that we have all the parameters. Write it out.
        try {

            BufferedWriter refBufferWriter = new BufferedWriter(new FileWriter(fileObj));

            refBufferWriter.write("# Written by AMES per unit ");
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "MM/dd/yyyy HH:mm:ss\n\n");
            Date date = new Date();
            refBufferWriter.write(dateFormat.format(date));

            refBufferWriter.write("set Buses := ");

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("Bus" + (i + 1) + " ");
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set TransmissionLines :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write("Bus" + (int) branchIndex[i][0] + " Bus"
                        + (int) branchIndex[i][1] + "\n");
            }

            refBufferWriter.write(";\n\n");

            refBufferWriter.write("param NumTransmissionLines := "
                    + branchIndex.length + " ;\n\n");

            refBufferWriter
                    .write("param: BusFrom BusTo ThermalLimit Reactance :=\n");

            for (int i = 0; i < branchIndex.length; i++) {
                refBufferWriter.write((i + 1) + " Bus"
                        + (int) numBranchData[i][0] + " Bus"
                        + (int) numBranchData[i][1] + " " + numBranchData[i][2]/ baseS
                        + " " + (numBranchData[i][3] * baseS/ (baseV * baseV)) + "\n"); //(branchData[n][3]*INIT.getBaseS())/(INIT.getBaseV()*INIT.getBaseV())
            }
            refBufferWriter.write(";\n\n");

            refBufferWriter.write("set ThermalGenerators := ");

            for (GenAgent gc : ames.getGenAgentList()) {
                refBufferWriter.write(gc.getID() + " ");
            }

            refBufferWriter.write(";\n\n");

            GenAgent[][] genNodeBusTable = new GenAgent[numNodes][numGenAgents];
            for (int i = 0; i < numNodes; i++) {
                for (int j = 0; j < numGenAgents; j++) {
                    GenAgent gen = (GenAgent) ames.getGenAgentList().get(j);
                    //System.out.println(gen.getAtNode());

                    if (gen.getAtNode() - 1 == i) {
                        genNodeBusTable[i][j] = gen;
                    } else {
                        genNodeBusTable[i][j] = null;
                    }
                }
            }

            for (int i = 0; i < numNodes; i++) {
                refBufferWriter.write("set ThermalGeneratorsAtBus[Bus"
                        + (i + 1) + "] := ");

                for (int j = 0; j < numGenAgents; j++) {
                    if (genNodeBusTable[i][j] != null) {
                        refBufferWriter.write(genNodeBusTable[i][j].getID() + " ");
                    }

                }

                refBufferWriter.write(" ;\n");
            }

            double TimePeriodLength = (double) ames.getTestCaseConfig().RTKDur / 60;

            int BalPenPos = ames.getTestCaseConfig().BalPenPos;
            int BalPenNeg = ames.getTestCaseConfig().BalPenNeg;

            refBufferWriter.write("\nparam BalPenPos := " + BalPenPos
                    + " ;\n\n");

            refBufferWriter.write("\nparam BalPenNeg := " + BalPenNeg
                    + " ;\n\n");

            refBufferWriter.write("\nparam TimePeriodLength := " + TimePeriodLength
                    + " ;\n\n");

            refBufferWriter.write("\nparam NumTimePeriods := " + (numTimeSteps)
                    + " ;\n\n");
//           refBufferWriter.write("\nparam Interval := " + interval
//                    + " ;\n\n");

            refBufferWriter
                    .write("param: PowerGeneratedT0 UnitOnT0State InitialTimeON InitialTimeOFF MinimumPowerOutput MaximumPowerOutput ScaledMinimumUpTime ScaledMinimumDownTime ScaledRampUpLimit ScaledRampDownLimit ScaledStartupRampLimit ScaledShutdownRampLimit ScaledColdStartTime ColdStartCost HotStartCost ShutdownCost :=\n");

            for (GenAgent ga : ames.getGenAgentList()) {
                refBufferWriter.write(genAgentToSCEDDesc(ga, hour, ames.getTestCaseConfig().RTKDur, numTimeSteps, baseS, false));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");

            refBufferWriter
                    .write("param: ID atBus EndPointSoc MaximumEnergy ScaledRampDownInput ScaledRampUpInput ScaledRampDownOutput ScaledRampUpOutput MaximumPowerInput MinimumPowerInput MaximumPowerOutput MinimumPowerOutput MinimumSoc EfficiencyEnergy :=\n");

            for (StorageAgent su : ames.getStorageAgentList()) {
                refBufferWriter.write(StorageAgentScedToFile(ames.getTestCaseConfig().RTKDur, su, day, baseS));
                refBufferWriter.write("\n");
            }

            refBufferWriter.write(" ;\n");
            
            refBufferWriter.write("param PriceSenLoadFlag := "
                + ames.getPriceSensitiveDemandFlag() + " ;\n\n");

            refBufferWriter.write("param StorageFlag := "
                    + hasStorage + " ;\n\n");

            refBufferWriter.write("param DownReservePercent := "
                    + DownReservePercent + " ;\n\n");

            refBufferWriter.write("param UpReservePercent := "
                    + UpReservePercent + " ;\n\n");

            boolean HasZonalReserves = false;
            if(ames.getTestCaseConfig().NumberOfReserveZones > 1){
                HasZonalReserves = true;
            }
            
            if(HasZonalReserves){
                refBufferWriter.write("param HasZonalReserves := "
                        + HasZonalReserves + " ;\n\n");
                refBufferWriter.write("param NumberOfZones := "
                        + ames.getTestCaseConfig().NumberOfReserveZones + " ;\n\n");


                refBufferWriter.write("set Zones := ");

                for (int i = 0; i < ames.getTestCaseConfig().NumberOfReserveZones; i++) {
                    refBufferWriter.write("Zone" + (i + 1) + " ");
                }
                refBufferWriter.write(";\n\n");

                refBufferWriter
                        .write("param: Buses ZonalDownReservePercent ZonalUpReservePercent :=\n");

                for (String zName : ames.getTestCaseConfig().getZonalData().keySet()) {
                    CaseFileData.ZonalData ZoneData = ames.getTestCaseConfig().getZonalData().get(zName);
                    int[] buses = ZoneData.getBuses();
                    String stemp = "";
                    for (int i = 0; i < buses.length; i++) {
                        stemp = stemp + "Bus" + buses[i] + ",";
                    }
                    refBufferWriter.write(zName + " " + stemp + " " + ZoneData.getZonalRTRRD() + " " + ZoneData.getZonalRTRRU());
                    refBufferWriter.write("\n");
                }

                refBufferWriter.write(";\n\n");

            }

            refBufferWriter.write("param: NetFixedLoadForecast :=\n");

            double[][] NetFixedLoadForecast = new double[numNodes][numTimeSteps];
            for (int n = 0; n < numNodes; n++) {
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseNode = lse.getAtNode();
                    if ((n+1) == lseNode) {
                        for (int h = 0; h < numTimeSteps; h++) {
//                            NetDemand[n][h] = NetDemand[n][h]; // + LoadProfileLSE[i][min+h];
                            NetFixedLoadForecast[n][h] = NetFixedLoadForecast[n][h] + LoadProfileLSE[i][h];
                        }
                    }
                }
                
                for (int i = 0; i < numNDGAgents; i++) {
                    NDGenAgent ndg = ames.getNDGenAgentList().get(i);
                    int ndgNode = ndg.getAtNode();
                    if ((n+1) == ndgNode) {
                        for (int h = 0; h < numTimeSteps; h++) {
//                            NetDemand[n][h] = NetDemand[n][h]; // - GenProfileNDG[i][min+h];
                            NetFixedLoadForecast[n][h] = NetFixedLoadForecast[n][h] - GenProfileNDG[i][h];
                        }
                    }
                }

                for (int h = 0; h < numTimeSteps; h++) {
                    refBufferWriter.write("Bus" + (n+1) + " " + (min+h + 1) + " "
                            + (NetFixedLoadForecast[n][h] / baseS) + "\n");
                }
                refBufferWriter.write("\n");
            }
            
            refBufferWriter.write("; \n\n");
            
//            refBufferWriter.write("param: Demand :=\n");
//
//            for (int i = 0; i < numLSEAgents; i++) {
//                LSEAgent lse = ames.getLSEAgentList().get(i);
//                int lseNode = lse.getAtNode();
//
//                for (int h = 0; h < numIntervalsInSim; h++) {
//                    //System.out.println("min+h: "+ (min+h));
//                    refBufferWriter.write("Bus" + lseNode + " " + (min + h + 1) + " "
//                            + LoadProfileLSE[i][min + h] / baseS + "\n");
//                }
//
//                refBufferWriter.write("\n");
//            }
//
//            refBufferWriter.write("; \n");
//            if (hasNDG > 0) {
//                refBufferWriter.write("param: NDG :=\n");
//
//                for (int i = 0; i < numNDGAgents; i++) {
//                    NDGenAgent ndg = ames.getNDGenAgentList().get(i);
//                    int ndgNode = ndg.getAtNode();
//
//                    for (int h = 0; h < numIntervalsInSim; h++) {
//                        refBufferWriter.write("Bus" + ndgNode + " " + (min + h + 1) + " "
//                                + GenProfileNDG[i][min + h] / baseS + "\n");
//                    }
//
//                    refBufferWriter.write("\n");
//                }
//
//                refBufferWriter.write("; \n");
//
//            }

            if (ames.getPriceSensitiveDemandFlag() > 0) {
                refBufferWriter.write("set PricesSensitiveLoadNames :=");
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseNode = lse.getAtNode();
                    if (i > 0)
                      refBufferWriter.write(", ");
                    refBufferWriter.write("LSE" + lseNode);
                }
                refBufferWriter.write(";\n");

                 
                refBufferWriter.write("param: Name ID atBus hourIndex d e f SLMax NS :=\n");
                for (int i = 0; i < numLSEAgents; i++) {
                    LSEAgent lse = ames.getLSEAgentList().get(i);
                    int lseID = lse.getID();
                    int TAU = (int) (ames.M/ames.getTestCaseConfig().RTKDur); 
                    double[][] bid = PSLDemandBidLSE[i];
                    // System.out.println("hour: "+hour);
                    for (int k = 0; k < TAU; k++) {
                        // System.out.print(" k: "+ k);
                        refBufferWriter.write("LSE" + lseID + " " + lseID + " Bus" + lse.getAtNode() + " " + (k + 1)  + " "
                            + bid[k][0] + " "
                            + bid[k][1] * baseS + " "
                            + bid[k][2] * baseS * baseS + " "
                            + bid[k][3]/baseS + " "
                            + (int) bid[k][4] + "\n");
                    }
                    refBufferWriter.write("\n");
                }
                refBufferWriter.write(";\n\n");
            }

            refBufferWriter
                    .write("param: a b c NS :=\n");

            double[][] supplyOfferByGen = iso.getSupplyOfferByGenRT();

            final ArrayList<GenAgent> genagents = ames.getGenAgentList();
            for (int i = 0; i < numGenAgents; i++) {
                GenAgent ga = genagents.get(i);
                refBufferWriter.write(
                          ga.getID() + " "
                        + supplyOfferByGen[i][0] + " "
                        + (supplyOfferByGen[i][1] * baseS) + " "
                        + (supplyOfferByGen[i][2] * baseS * baseS) + " " 
                        + (int) supplyOfferByGen[i][3]  + " " + "\n");
            }

            refBufferWriter.write("; \n");

            refBufferWriter.close();
        } catch (IOException e) {
            throw new AMESMarketException("Unable to write the reference model.", e);
        }
    }

    /**
     * Get a string to write into the SCUC input file describing the genco.
     *
     * @param ga
     * @param baseS
     * @return a string with all of the parameters, or an empty string if the ga
     * parameter is null.
     */
    private String genAgentToSCUCDesc(GenAgent ga, int day, double DeltaT, int numTimeSteps, double baseS, boolean scuctype) {
        if (ga == null) {
            return "";
        }

        int UnitONT0State = ga.getUnitOnT0State(day); 
        int SUnitONT0State = 1;
        
        if (Math.abs(UnitONT0State) >= 1){
           SUnitONT0State = (int) Math.round(UnitONT0State/DeltaT);
        } else if (0<UnitONT0State && UnitONT0State<1) {
            SUnitONT0State = 1;
        } else if (-1<UnitONT0State && UnitONT0State<0) {
            SUnitONT0State = -1;
        } 
        
        //do all the conversions
        double powerT0 = ga.getPowerT0NextDay() / baseS;
        double capMin = ga.getCapacityMin() / baseS;
        double capMax = ga.getCapacityMax() / baseS;
        double ScaledRampUpLimit;
        double ScaledRampDownLimit;
        double ScaledStartUpRampLimit;
        double ScaledShutDownRampLimit;
        
        int ScaledMinUpTime;
        int ScaledMinDownTime;
        int ScaledColdStartTime;
        
        int InitialTimeON;
        int InitialTimeOFF;
        
        double DeltaK = DeltaT * 60; // DeltaK (min) = DeltaT (h) * 60 (min/h)
        // DAM SCUC : Units MW/min
        ScaledRampUpLimit = ( ga.getNominalRampUpLim() * DeltaK ) / ( baseS );
        ScaledRampDownLimit = ( ga.getNominalRampDownLim() * DeltaK ) / ( baseS );
        ScaledStartUpRampLimit = ( ga.getStartupRampLim() * DeltaK ) / ( baseS );
        ScaledShutDownRampLimit = ( ga.getShutdownRampLim() * DeltaK ) / ( baseS );
        
        ScaledMinUpTime = (int) Math.round( ga.getMinUpTime() / DeltaT );
        ScaledMinDownTime = (int) Math.round( ga.getMinDownTime() / DeltaT );
        ScaledColdStartTime = (int) Math.round( ga.getColdStartUpTime() / DeltaT);
        
        if(UnitONT0State > 0){
            InitialTimeON = Math.min( numTimeSteps , Math.max( 0 , (int) Math.round( (ga.getMinUpTime() - UnitONT0State)/DeltaT ) ) );    
        }
        else{
            InitialTimeON = 0;
        }
        
        
        if(UnitONT0State < 0){
            InitialTimeOFF = Math.min( numTimeSteps , Math.max( 0 , (int) Math.round( (ga.getMinDownTime() + UnitONT0State)/DeltaT ) ) );    
        }
        else{
            InitialTimeOFF = 0;
        }
        
        double coldstartupcost = ga.getColdStartUpCost();
        double hotstartupcost = ga.getHotStartUpCost();
        double shutdowncost = ga.getShutDownCost();

        //some rounding checks
        if (powerT0 < capMin && ga.getUnitOnStateT0NextDay() > 0) {
            System.err.println("SCUC Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " is less than capMin of " + capMin
                    + ". Adjusting to " + capMin
            );
            powerT0 = capMin;
        } else if (powerT0 > capMax) {
            System.err.println("Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " excedes the capMax value of " + capMax
                    + ". Adjusting to " + capMax
            );
            powerT0 = capMax;
        }
        // System.out.print(ga.getMinUpTime());
        return String.format(// 1     2          3       4       5       6       7       8               9               10          11
                //Name, powerTO, On/OffT0, InitialTimeON, InitialTimeOFF, MinPow, MaxPow, ScaledMinUp, ScaledMinDown, ScaledRampUp, ScaledRampDown, ScaledStartupLim, ScaledShutdownLim, ScaledColdStartTime, ColdStartupCost, HotStartupCost, ShutDownCost
                "%1$s %2$.15f %3$d %4$d %5$d %6$.15f %7$.15f %8$d %9$d %10$.15f %11$.15f %12$.15f %13$.15f %14$d %15$.15f %16$.15f %17$.15f" 
                ,
                 ga.getID() //1
                ,
                 powerT0 //2
                ,
                 SUnitONT0State//3
                ,
                 InitialTimeON
                ,
                 InitialTimeOFF
                ,
                 capMin //4
                ,
                 capMax//5
                ,
                 ScaledMinUpTime //6
                ,
                 ScaledMinDownTime //7
                ,
                 ScaledRampUpLimit //8
                ,
                 ScaledRampDownLimit//9
                ,
                 ScaledStartUpRampLimit//10
                ,
                 ScaledShutDownRampLimit//11
                ,
                 ScaledColdStartTime //12
                ,
                 coldstartupcost //13
                ,
                 hotstartupcost //14
                ,
                 shutdowncost //15
        );
    }

    private String genAgentToSCEDDesc(GenAgent ga, int hour, int DeltaK, int numTimeSteps,double baseS, boolean scuctype) {
        if (ga == null) {
            return "";
        }


        int UnitONT0State = 0; 
        int SUnitONT0State = 1;
        double DeltaT = DeltaK/60;
        
        if (Math.abs(UnitONT0State) >= 1){
           SUnitONT0State = (int) Math.round(UnitONT0State/DeltaT);
        } else if (0<UnitONT0State && UnitONT0State<1) {
            SUnitONT0State = 1;
        } else if (-1<UnitONT0State && UnitONT0State<0) {
            SUnitONT0State = -1;
        }

        //do all the conversions
        double powerT0 = ga.getPowerPrevInterval() / baseS;
        double capMin = ga.getCapacityMin() / baseS;
        double capMax = ga.getCapacityMax() / baseS;
        double ScaledRampUpLimit;
        double ScaledRampDownLimit;
        double ScaledStartUpRampLimit;
        double ScaledShutDownRampLimit;

        // RTM SCED : scaled ramp up and down limits (MW)
        ScaledRampUpLimit = (ga.getNominalRampUpLim() * DeltaK) / (baseS);  // 
        ScaledRampDownLimit = (ga.getNominalRampDownLim()* DeltaK) / (baseS);
        ScaledStartUpRampLimit = (ga.getStartupRampLim()* DeltaK) / (baseS);
        ScaledShutDownRampLimit = (ga.getShutdownRampLim()* DeltaK) / (baseS);
        
        int ScaledMinUpTime;
        int ScaledMinDownTime;
        int ScaledColdStartTime;
        int InitialTimeON;
        int InitialTimeOFF;
        
        ScaledMinUpTime = (int) Math.round( ga.getMinUpTime() * 60 / DeltaK);
        ScaledMinDownTime = (int) Math.round( ga.getMinDownTime() * 60 / DeltaK);
        ScaledColdStartTime = (int) Math.round( ga.getColdStartUpTime() * 60 / DeltaK);
        
        if(UnitONT0State > 0){
            InitialTimeON = Math.min( numTimeSteps , Math.max( 0 , (int) Math.round( (ga.getMinUpTime() - UnitONT0State)/DeltaT ) ) );    
        }
        else{
            InitialTimeON = 0;
        }
        
        
        if(UnitONT0State < 0){
            InitialTimeOFF = Math.min( numTimeSteps , Math.max( 0 , (int) Math.round( (ga.getMinDownTime() + UnitONT0State)/DeltaT ) ) );    
        }
        else{
            InitialTimeOFF = 0;
        }
        
        double coldstartupcost = ga.getColdStartUpCost();
        double hotstartupcost = ga.getHotStartUpCost();
        double shutdowncost = ga.getShutDownCost();

        //some rounding checks
        if (powerT0 < capMin && ga.getCommitmentStatus(hour - 1) > 0) {
            System.err.println("SCED Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " is less than capMin of " + capMin
                    + ". Adjusting to " + capMin
            );
            powerT0 = capMin;
        } else if (powerT0 > capMax) {
            System.err.println("Warning: " + ga.getID() + " PowerT0 value of "
                    + powerT0 + " excedes the capMax value of " + capMax
                    + ". Adjusting to " + capMax
            );
            powerT0 = capMax;
        }
        //System.out.println("");
        return String.format(// 1     2          3       4       5       6       7       8               9               10          11
                //Name, powerTO, On/OffT0, InitialTimeON, InitialTimeOFF, MinPow, MaxPow, ScaledMinUp, ScaledMinDown, ScaledRampUp, ScaledRampDown, ScaledStartupLim, ScaledShutdownLim, ScaledColdStartTime, ColdStartupCost, HotStartupCost, ShutDownCost
                "%1$s %2$.15f %3$d %4$d %5$d %6$.15f %7$.15f %8$d %9$d %10$.15f %11$.15f %12$.15f %13$.15f %14$d %15$.15f %16$.15f %17$.15f" 
                ,
                 ga.getID() //1
                ,
                 powerT0 //2
                ,
                 SUnitONT0State//3
                ,
                 InitialTimeON
                ,
                 InitialTimeOFF
                ,
                 capMin //4
                ,
                 capMax//5
                ,
                 ScaledMinUpTime//6
                ,
                 ScaledMinDownTime//7
                ,
                 ScaledRampUpLimit//8
                ,
                 ScaledRampDownLimit//9
                ,
                 ScaledStartUpRampLimit//10
                ,
                 ScaledShutDownRampLimit//11
                ,
                 ScaledColdStartTime//12
                ,
                 coldstartupcost //13
                ,
                 hotstartupcost //14
                ,
                 shutdowncost //15
        );
    }

    
    /**
     * Get a string to write into the Storage input file describing the Storage
     * Unit.
     *
     * @param su
     * @param baseS
     * @return a string with all of the parameters, or an empty string if the ga
     * parameter is null.
     */
    private String StorageAgentScucToFile(double DeltaT, StorageAgent sa, int day, double baseS) {
        if (sa == null) {
            return "";
        }

        StorageInputData Data = sa.getData();

        //do all the conversions
        double DeltaK = DeltaT * 60; // DeltaK (min) = DeltaT (h) * 60 (min/h)
        double ID = Data.getID();
        double atBus = Data.getatBus();
        double EndPointSoc = Data.getEndPointSoc();
        double MaxEnergy = Data.getMaximumEnergy();
        double ScaledRampDownInput = Data.getNominalRampDownInput() * DeltaK;
        double ScaledRampUpInput = Data.getNominalRampDownInput() * DeltaK;
        double ScaledRampDownOutput = Data.getNominalRampDownOutput() * DeltaK;
        double ScaledRampUpOutput = Data.getNominalRampUpOutput() * DeltaK;
        double MaxPowInput = Data.getMaximumPowerInput();
        double MinPowInput = Data.getMinimumPowerInput();
        double MaxPowOutput = Data.getMaximumPowerOutput();
        double MinPowOutput = Data.getMinimumPowerOutput();
        double MinimumSoc = Data.getMinimumSoc();
        double EfficiencyEnergy = Data.getEfficiencyEnergy();

        // System.out.print(ga.getMinUpTime());
        return String.format(// 1     2          3       4       5       6       7       8               9               10          11
                // ID, atBus EndPointSoc MaxEnergy ScaledRampDownInput ScaledRampUpInput ScaledRampDownOutput ScaledRampUpOutput MaxPowInput MinPowInput MaxPowOutput  MinPowOutput MinimumSoc EfficiencyEnergy
                "%1$s %2$.15f %3$.15f %4$.15f %5$.15f %6$.15f %7$.15f %8$.15f %9$.15f %10$.15f %11$.15f %12$.15f %13$.15f %14$.15f "
                ,
                 ID //1
                ,
                 atBus //2
                ,
                 EndPointSoc //3
                ,
                 MaxEnergy //4
                ,
                 ScaledRampDownInput//5
                ,
                 ScaledRampUpInput//6
                ,
                 ScaledRampDownOutput//7
                ,
                 ScaledRampUpOutput//8
                ,
                 MaxPowInput//9
                ,
                 MinPowInput //10
                ,
                 MaxPowOutput //11
                ,
                 MinPowOutput //12
                ,
                 MinimumSoc //13
                ,
                 EfficiencyEnergy //14
        );
    }
    
    /**
     * Get a string to write into the Storage input file describing the Storage
     * Unit.
     *
     * @param su
     * @param baseS
     * @return a string with all of the parameters, or an empty string if the ga
     * parameter is null.
     */
    private String StorageAgentScedToFile(int DeltaK, StorageAgent sa, int day, double baseS) {
        if (sa == null) {
            return "";
        }

        StorageInputData Data = sa.getData();

        //do all the conversions
        double ID = Data.getID();
        double atBus = Data.getatBus();
        double EndPointSoc = Data.getEndPointSoc();
        double MaxEnergy = Data.getMaximumEnergy();
        double ScaledRampDownInput = Data.getNominalRampDownInput() * DeltaK;
        double ScaledRampUpInput = Data.getNominalRampDownInput() * DeltaK;
        double ScaledRampDownOutput = Data.getNominalRampDownOutput() * DeltaK;
        double ScaledRampUpOutput = Data.getNominalRampUpOutput() * DeltaK;
        double MaxPowInput = Data.getMaximumPowerInput();
        double MinPowInput = Data.getMinimumPowerInput();
        double MaxPowOutput = Data.getMaximumPowerOutput();
        double MinPowOutput = Data.getMinimumPowerOutput();
        double MinimumSoc = Data.getMinimumSoc();
        double EfficiencyEnergy = Data.getEfficiencyEnergy();

        // System.out.print(ga.getMinUpTime());
        return String.format(// 1     2          3       4       5       6       7       8               9               10          11
                // ID, atBus EndPointSoc MaxEnergy ScaledRampDownInput ScaledRampUpInput ScaledRampDownOutput ScaledRampUpOutput MaxPowInput MinPowInput MaxPowOutput  MinPowOutput MinimumSoc EfficiencyEnergy
                "%1$s %2$.15f %3$.15f %4$.15f %5$.15f %6$.15f %7$.15f %8$.15f %9$.15f %10$.15f %11$.15f %12$.15f %13$.15f %14$.15f " 
                ,
                 ID //1
                ,
                 atBus //2
                ,
                 EndPointSoc //3
                ,
                 MaxEnergy //4
                ,
                 ScaledRampDownInput//5
                ,
                 ScaledRampUpInput//6
                ,
                 ScaledRampDownOutput//7
                ,
                 ScaledRampUpOutput//8
                ,
                 MaxPowInput//9
                ,
                 MinPowInput //10
                ,
                 MaxPowOutput //11
                ,
                 MinPowOutput //12
                ,
                 MinimumSoc //13
                ,
                 EfficiencyEnergy //14
        );
    }

    /**
     * Write out the generator commitments for the sced.
     *
     * @param ames
     * @param M length of RTM interval to write the commitments
     * @param m beginning of the minute interval
     * @param h corresponding hour of the interval
     * @param gencoCommitments not null
     * @param ucVectorFile file to write the information into.
     * @throws AMESMarketException
     * @throws IllegalArgumentException if gencoCommitments is null.
     */
    public void writeGenCommitments(AMESMarket ames, int M, int m, int h, List<CommitmentDecision> gencoCommitments, File ucVectorFile) throws AMESMarketException {

        int TAU = (int) (M/ames.getTestCaseConfig().RTKDur);      // Tau - number of sub-intervals of each RTM interval 
        if (gencoCommitments == null) {
            throw new IllegalArgumentException();
        }

        if (!ensureFileParentExists(ucVectorFile)) {
            throw new AMESMarketException("Could not create the directory for " + ucVectorFile.getPath());
        }

        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(ucVectorFile));

            final String eol = System.getProperty("line.separator");
            final String indent = "\t";
            //strings for whether or not the unit is committed.
            final String ucOn = "1";
            final String ucOff = "0";

            for (CommitmentDecision cd : gencoCommitments) {
                out.println(cd.generatorName); 
                StringBuilder sb = new StringBuilder();

                int[] commitmentVector = new int[TAU];
                for (int k = 0; k < TAU; k++) {
                    commitmentVector[k] = cd.commitmentDecisions[h - 1];
                }
                //Boolean[] commitmentVector = gencoCommitments.get(g);
                if (commitmentVector == null) { //yes, I'm being very cautious.
                    System.err.println("[Warning External SCED] No commit vector for " + cd.generatorName);
                    continue;
                }

                for (int b : commitmentVector) {
                    sb.append(indent);
                    sb.append((b == 1 ? ucOn : ucOff)); //convert boolean to the format the external SCED expects.
                    sb.append(eol);
                }
                out.print(sb.toString());
            }

            out.close();
        } catch (IOException e) {
            throw new AMESMarketException("Unable to write the generator commitment schedule.", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private boolean ensureFileParentExists(File f) {
        File parent = f.getParentFile();
        if (parent == null) {
            return true; //no parent file. nothing to be done
        } else if (parent.exists()) {
            return true; //nothing to be done.
        } else {
            parent.mkdirs(); //try and make it. return whether or not it exists.
            return parent.exists();
        }
    }
}
