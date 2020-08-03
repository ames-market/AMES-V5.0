/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package amesmarket.extern.psst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;

import amesmarket.AMESMarket;
import amesmarket.AMESMarketException;
import amesmarket.GenAgent;
import amesmarket.ISO;
//import amesmarket.LoadCaseControl;
//import amesmarket.LoadProfileCollection;
import amesmarket.DAMOptimization;
import amesmarket.Support;
import amesmarket.extern.common.CommitmentDecision;
import amesmarket.filereaders.BadDataFileFormatException;
import java.text.DecimalFormat;

/**
 *
 */
public class PSSTDAMOpt implements DAMOptimization {

    /**
     * Whether or not to delete the files created.
     */
    private final boolean deleteFiles;

    private final AMESMarket ames;
    private final ISO iso;
    private List<CommitmentDecision> genDAMCommitment;
    private double[][] DAMLMP;
    private double[][] GenDAMDispatch;
    private int[][] GenDAMCommitmentStatusPresentDay;
    private int[][] GenDAMCommitmentStatusNextDay;
    private String SolutionStatus;
    private final int numGenAgents, numLSEAgents, numHours, numIntervals; // numGenAgents;

    private final File MarketDir, TempDir, DAUnitCommitmentsFile, DAMResultsFile, DAMReferenceModelFile;

    /**
     * Configuration for external coopr call.
     */
    private final PSSTConfig PSSTExt;
    DecimalFormat Format = new DecimalFormat("###.####");

    public PSSTDAMOpt(ISO independentSystemOperator, AMESMarket model) {
        this.ames = model;
        this.iso = independentSystemOperator;
        this.numGenAgents = this.ames.getNumGenAgents();
        this.numLSEAgents = this.ames.getNumLSEAgents();
        this.numHours = this.ames.NUM_HOURS_PER_DAY;
        this.numIntervals = this.ames.NUM_HOURS_PER_DAY_UC;

        this.GenDAMCommitmentStatusNextDay = new int[this.numHours][this.numGenAgents];
        this.GenDAMCommitmentStatusPresentDay = new int[this.numHours][this.numGenAgents];
        //genSchedule=new int[numGenAgents][numHoursPerDay];
        this.MarketDir = new File("DataFiles");
        this.TempDir = new File("DataFiles/PyomoTempFiles");
        this.MarketDir.mkdirs();
        this.TempDir.mkdirs();
        this.DAUnitCommitmentsFile = new File("DataFiles/DAUnitCommitments.dat");
        this.DAMReferenceModelFile = new File("DataFiles/DAMReferenceModel.dat");
        this.DAMResultsFile = new File("DataFiles/DAMResults.dat");

        this.PSSTExt = PSSTConfig.createDeterministicPSST("scuc", this.DAUnitCommitmentsFile, this.DAMReferenceModelFile, this.DAMResultsFile, this.ames.getTestCaseConfig().Solver);

        this.deleteFiles = model.isDeleteIntermediateFiles();
    }

    /* (non-Javadoc)
	 * @see amesmarket.extern.coopr.SCUC#calcSchedule(int)
     */
    @Override
    public void solveDAMOptimization(int day) throws IOException, AMESMarketException, BadDataFileFormatException {

        String strTemp = "";

        this.syscall(this.PSSTExt);

        //Read the data file back in to get the GenCo commitments.
        if (!this.DAMResultsFile.exists()) {
            throw new BadDataFileFormatException(new FileNotFoundException(
                    this.DAMResultsFile.getPath()));
        }

        //System.out.println("Reading Solution Status from " + this.DAMResultsFile.getPath());
        java.util.Scanner raf = new Scanner(this.DAMResultsFile);

        //System.out.println("" + raf.nextLine());
        if (raf.nextLine().equals("SOLUTION_STATUS")) {
            String vs = raf.nextLine();
            if (vs.startsWith("optimal")) {
                SolutionStatus = "optimal";
            } else if (vs.startsWith("infeasible")) {
                SolutionStatus = "infeasible";
            }

            raf.nextLine();
        }

        if (SolutionStatus.equals("optimal")) {
            //System.out.println("Reading GenCo schedule from " + this.DAMResultsFile.getPath());
            this.genDAMCommitment = new ArrayList<CommitmentDecision>();
            this.GenDAMCommitmentStatusPresentDay = new int[this.numHours][this.numGenAgents];
            // System.out.println("len: " + this.GenDAMCommitmentStatusNextDay.length);
            for (int j = 0; j < this.numGenAgents; j++) {
                for (int hour = 0; hour < this.ames.NUM_HOURS_PER_DAY; hour++) {
                    GenDAMCommitmentStatusPresentDay[hour][j] = GenDAMCommitmentStatusNextDay[hour][j];
                }
            }
            this.GenDAMCommitmentStatusNextDay = new int[this.numHours][this.numGenAgents];
            this.GenDAMDispatch = new double[this.numHours][this.numGenAgents];
            for (int j = 0; j < this.numGenAgents; j++) {
                int[] commitmentschedule = new int[this.numHours];
                int i = 0;
                String genCoMarker = raf.nextLine().trim();
                GenAgent gc = this.ames.getGenAgentByName(genCoMarker);
                //System.out.println("GenCo" + gc.getGenID() + " ");
                if (gc == null) {
                    throw new BadDataFileFormatException("Unknown GenAgent, "
                            + genCoMarker + ", in SCUC results");
                }
                //System.out.println("Hour vg Pg");
                while (i < this.numHours) {
                    strTemp = raf.nextLine();
                    if (strTemp == null) {
                        throw new BadDataFileFormatException(
                                "No schedule for " + gc.getID() + " hour " + i);
                    }

                    int iIndex = strTemp.indexOf(" ");
                    String delims = "[ ]+";
                    String[] values = strTemp.split(delims);
                    commitmentschedule[i] = Integer.parseInt(strTemp.substring(iIndex + 1, iIndex + 2));
                    //System.out.print("" + (i+1));
                    //System.out.print(": " + commitmentschedule[i]);
                    double value = Double.parseDouble(values[2]) * ames.getBaseS();
                    //System.out.print(": " + Format.format(value));
                    GenDAMDispatch[i][gc.getGenID() - 1] = value;
                    GenDAMCommitmentStatusNextDay[i][gc.getGenID() - 1] = Integer.parseInt(values[1]);
                    i++;

                    if (i == this.numHours) {
                        if (day == 1) {
                            gc.setPowerPrevInterval(value);
                        }
                        gc.setPowerT0NextDay(value);
                    }
                    //System.out.println("");
                }
                //System.out.println("");
                while (i < this.ames.NUM_HOURS_PER_DAY_UC) 
                {
                    strTemp = raf.nextLine();
                    i++;
                }
                this.genDAMCommitment.add(new CommitmentDecision(gc.getID(), gc.getIndex(), commitmentschedule));
            }

            //System.out.println("");

            this.cleanup();
            
            this.DAMLMP = new double[this.numHours][ames.getNumNodes()];
            Boolean Flag = true;
            for (int j = 0; j < this.numHours; j++) {
                int i = 0;

                while (Flag) {
                    strTemp = raf.nextLine();
                    if (strTemp.equalsIgnoreCase("DAMLMP")) {
                        Flag = false;
                    }
                }
                while (i < ames.getNumNodes()) {
                    strTemp = raf.nextLine();
                    String[] data = strTemp.split(":");
                    double lmp = Double.parseDouble(data[2]) / ames.getBaseS();
                    this.DAMLMP[j][i] = lmp;
                    i++;
                }

            }
            DecimalFormat LMPFormat = new DecimalFormat("###.##");
            System.out.println("DAM LMP Outcomes:");
            System.out.print("Hour");

            for (int n = 0; n < ames.getNumNodes(); n++) {
                System.out.print("  Bus" + (n + 1));
            }
            System.out.println("");

            for (int h = 0; h < this.numHours; h++) {
                System.out.print("" + (h + 1));
                for (int n = 0; n < ames.getNumNodes(); n++) {
                    System.out.print("  " + LMPFormat.format(this.DAMLMP[h][n]));
                }
                System.out.println("");
            }
            System.out.println("");
            //Sort the collection by array index. Keeps the list
            //in the 'expected' order for ames. The output from the external
            //solver is sorted by name, which means GenCo10 comes after GenCo1
            Collections.sort(this.genDAMCommitment, new Comparator<CommitmentDecision>() {
                @Override
                public int compare(CommitmentDecision o1, CommitmentDecision o2) {
                    if (o1.generatorIdx < o2.generatorIdx) {
                        return -1;
                    }
                    if (o1.generatorIdx == o2.generatorIdx) {
                        return 0;
                    }
                    return 1;
                }
            });
        } else if (SolutionStatus.equals("infeasible")) {
            System.out.println("Solution is Infeasible because the reserve constraints are not satisfied.");
        }
        raf.close();

    }

    public void syscall(PSSTConfig runefConfig) throws IOException {

        System.out.println(" AMES V5.0 SCUC and SCED operations are performed using an external call to a Modified version of PSST. ");

        System.out.println(" PSST (Power System Simulation Toolbox) is a Python-based wrapper developed by Dheepak Krishnamurthy.  ");

        System.out.println("\n The following user-accessible data files function as input and output data files for PSST operations: \n");

        System.out.println(" # 'DAMReferenceModel.dat' is an input data file for PSST obtained from the DataFileWriter.java file; PSST uses this input data file to perform combined DAM SCUC/SCED operations.");
        System.out.println(" # 'DAUnitCommitments.dat' is an output data file by PSST that contains generator unit commitment data; it is used by PSST to perform DAM SCED operations. ");

        System.out.println(" # 'RTMReferenceModel.dat' is an input data file for PSST obtained from the DataFileWriter.java file; PSST uses this input data file to perform RTM SCED operations.");
        System.out.println(" # 'RTUnitCommitments.dat' is an input data file for PSST that contains generator unit commitment data; it is used by PSST to perform RTM SCED operations. ");

        System.out.println(" # 'DAMResults.dat' is an output data file generated by PSST after performing DAM operations; the contents of this output data file are read back into the .java files.");
        System.out.println(" # 'RTMResults.dat' is an output data file generated by PSST after performing RTM operations; the contents of this output data file are read back into the .java files. ");

        System.out.println("");

        Process p = runefConfig.createPSSTProcess(this.MarketDir);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        // read the output from the command
        String s = null;
        System.out.println("Here is the standard output of the command with SCUC:\n");
        while ((s = stdInput.readLine()) != null) {
            System.out.println("SCUC output: " + s);
        }

        // read any errors from the attempted command
        System.err.println("Here is the standard error of the command with SCUC (if any):\n");
        while ((s = stdError.readLine()) != null) {
            System.out.println("There is error with SCUC: ");
            System.out.println(s);
        }
    }

    /**
     * @return The file where the reference model for pyomo was written.
     */
    public File getReferenceModelFile() {
        return this.DAMReferenceModelFile;
    }

    /* (non-Javadoc)
	 * @see amesmarket.extern.coopr.SCUC#getSchedule()
     */
    @Override
    public List<CommitmentDecision> getDAMCommitment() {

        return this.genDAMCommitment;
    }

    @Override
    public double[][] getDAMLMPSolution() {
        return this.DAMLMP;
    }

    @Override
    public double[][] getDAMDispatchSolution() {
        return this.GenDAMDispatch;
    }

    public int[][] getGenDAMCommitmentStatusNextDay() {
        return this.GenDAMCommitmentStatusNextDay;
    }

    public String getSolutionStatus() {
        return this.SolutionStatus;
    }

    private void cleanup() {
        if (this.deleteFiles) {
            Support.deleteFiles(Arrays.asList(this.DAMResultsFile));
        }
    }
}
