

package amesmarket;

// INIT.java
// Initialization of input data

import java.io.*;
import java.util.ArrayList;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;
import java.util.HashMap;
import java.util.Map;


public class INIT {

  // Tolerance value (used in correctRoundingError())
  private static final double TOL = 1.0E-9;

  // Default values
  public static final int BASE_S = 100;
  public static final int BASE_V = 10;

  // Number of parameters (number of columns in the data file)
  public static final int NUM_NODE_PARAMS = 2;
    // numNodes, softPenaltyCoeff
  public static final int NUM_BRANCH_PARAMS = 4;
    // FromNode, ToNode, reactance, MaxCap (Thermal Limits)
  public static final int NUM_GEN_PARAMS = 10;
    // genID, atWhichNode, SCost, a, b, c, NS, PMin, PMax, InitMoney 
  public static final int NUM_LSE_3SEC_PARAMS = 2+8;
    // lseID, atWhichNode, H-00,...,H-07
  public static final int NUM_LSE_PARAMS = 2+24;
    // lseID, atWhichNode, H-00,...,H-23
  public int NUM_LSE_PARAMS_SLOAD = 8;
    // lseID, atWhichNode, hourIndex, d, e, f, PMax, NS
  public static final int NUM_LSE_PARAMS_HYBRID_LOAD = 2+24;
    // lseID, atWhichNode, H-00,...,H-23

  private String[][] paramNames = {
   {"NN","penaltyCoeff"},
   {"From","To","MaxCap","Reactance"},
   {"ID","atBus","SCost","a","b", "c", "NS", "PMin","PMax","InitMoney"},
   {"ID","atBus","d","e","f","PMax"},
   {"ID","atBus","H-00","H-01","H-02","H-03","H-04","H-05","H-06","H-07"},
   {"ID","atBus","H-08","H-09","H-10","H-11","H-12","H-13","H-14","H-15"},
   {"ID","atBus","H-16","H-17","H-18","H-19","H-20","H-21","H-22","H-23"}};

  
  private double[][] nodeData;
  private double[][] branchData;
  private double[][] genData;
  /**
  * Marker of generators that are alert/canary generators.
  */
  private boolean[] alertGenIDs;
  private double[][] lse3SecData;  // 3-sectional 8-hour LSE data
  private double[][] lseSec1Data;  // First 8-hour LSE data
  private double[][] lseSec2Data;  // Second 8-hour LSE data
  private double[][] lseSec3Data;  // Third 8-hour LSE data
  private double[][] lseDataFixedDemand;  // Combine 3-sectional to 24-hour LSE data
  private double[][][] lseDataPriceSensitiveDemand; 
  private int   [][] lseDataHybridDemand; 
  private boolean isPU;
  private static double baseS;
  private static double baseV;
  //private double reserveRequirements;

  private double[][] NDGData;  // Combine 3-sectional to 24-hour NDG data
  private double[][] NDGSec1Data;  // First 8-hour NDG data
  private double[][] NDGSec2Data;  // Second 8-hour NDG data
  private double[][] NDGSec3Data;  // Third 8-hour NDG data
  
    /**
   * slist: the master string list to store tokenized strings in data file
   */
  private ArrayList slist = new ArrayList();

  private HashMap<String, CaseFileData.SCUCInputData> extraGenCoParams;
  private HashMap<String, CaseFileData.StorageInputData> StorageParams;
  private double hasStorage;
  private double hasNDG;
  
  // Constructor
  public INIT() {
        baseS = BASE_S;
        baseV = BASE_V;
  }
  
  public String[][] getParamNames(){
    return paramNames;
  }
  public static double getBaseS(){
    return baseS;
  }
  public static double getBaseV(){
     return baseV;
   }
  
  public static void setBaseS(double s){
    baseS=s;
  }
  public static void setBaseV(double v){
     baseV=v;
   }

  public void sethasStorage(double s){
      hasStorage = s;
  }

  public void sethasNDG(double s){
      hasNDG = s;
  }
  
  public double gethasStorage(){
      return hasStorage;
  }
  
  public double gethasNDG(){
      return hasNDG;
  }
  
  public double[][] getNodeData(){
    return nodeData;
  }

  public double[][] getBranchData(){
    return branchData;
  }
  public double[][] getGenData(){
    return genData;
  }
  
    public boolean[] getAlertGenMarker() {
      return alertGenIDs;
  }

  
  public int[][] getLSEDataHybridDemand(){
    return lseDataHybridDemand;
  }
  
  public double[][][] getLSEDataPriceSensitiveDemand(){
    return lseDataPriceSensitiveDemand;
  }
  
  public double[][] getLSEDataFixedDemand(){
    return lseDataFixedDemand;
  }

  public double[][] getNDGData(){
    return NDGData;
  }  
  
  public double[][] getLSESec1Data(){
    return lseSec1Data;
  }
  public double[][] getLSESec2Data(){
    return lseSec2Data;
  }
  public double[][] getLSESec3Data(){
    return lseSec3Data;
  }

  public static void printArray(double [][]a){
    for(int i = 0; i<a.length; i++){
      for(int j = 0 ; j<a[0].length; j++){
        System.out.print("\t" + a[i][j]);
        }
        System.out.println();
      }
      System.out.println();
  }

  public static void printArray(String[] s){
    for(int i=0; i<s.length; i++){
      System.out.print("\t" + s[i]);
    }
    System.out.println();
  }

  public void setNodeDataFromGUI(double [][] object) {
      int iNumberRow=object.length;
      int iNumberCol=object[0].length;

      nodeData = new double[iNumberRow][NUM_NODE_PARAMS];
      
      for(int i=0;i<iNumberRow;i++)
          for(int j=0; j<NUM_NODE_PARAMS; j++)
              nodeData[i][j]=object[i][j];
  }
  
 public void setBranchDataFromGUI(double [][] object) {
      int iNumberRow=object.length;
      int iNumberCol=object[0].length;

      branchData = new double[iNumberRow][NUM_BRANCH_PARAMS];
      
      //System.out.println("Branch Data");
      for(int i=0;i<iNumberRow;i++)
          for(int j=0; j<NUM_BRANCH_PARAMS; j++){
              branchData[i][j]=object[i][j];
              //System.out.println("i= "+i+" j= "+j+" branchData[i][j]="+branchData[i][j]);
          }
  }
  
  public void setGenDataFromGUI(double [][] object) {
      int iNumberRow=object.length;
      int iNumberCol=object[0].length;

      genData = new double[iNumberRow][NUM_GEN_PARAMS];
      // System.out.println("iNumberCol: " + iNumberCol + " NUM_GEN_PARAMS: " + NUM_GEN_PARAMS);
      
      for(int i=0;i<iNumberRow;i++)
          for(int j=0; j<NUM_GEN_PARAMS; j++){
              genData[i][j]=object[i][j];
//              System.out.println("i= "+i+" j= "+j+" genData[i][j]="+genData[i][j]);
          }
  }

    /**
     * Set list of ids that for gencos that are alert gencos.
     *
     * This list should be by the 'actual' index of the generators.
     * e.g. if GenCo1 in the data file, which is described by genData[0]
     * is an Alert/canary generator, then the value of one of the elements
     * of the alertGenIDs array should be zero. No order is assumed. The type
     * could just as well be a Set, but plain arrays fit with the existing style.

     * @param alertGenIDs
     */
    public void setAlertGenIdsFromGUI(boolean[] alertGenIDs) {
        if(alertGenIDs == null) return;

        this.alertGenIDs = new boolean[alertGenIDs.length];
        System.arraycopy(alertGenIDs, 0,
                this.alertGenIDs, 0, alertGenIDs.length);
    }  
  
  public void setLSEPriceDataFromGUI(double [][][] object) {
      int iNumberRow=object.length;
      int iNumberCol=object[0][0].length;

      lseDataPriceSensitiveDemand = new double[iNumberRow][24][NUM_LSE_PARAMS_SLOAD];
      
      //System.out.println("LSEPriceSensitive Data");
      for(int i=0;i<iNumberRow;i++)
          for(int h=0; h<24; h++)
              for(int j=0; j<NUM_LSE_PARAMS_SLOAD; j++){
                  lseDataPriceSensitiveDemand[i][h][j]=object[i][h][j];
                  //System.out.println("i= "+i+" h="+h+ " j= "+j+" lseDataPriceSensitiveDemand[i][h][j]="+lseDataPriceSensitiveDemand[i][h][j]);
              }
  }

  public void setLSEHybridDataFromGUI(int [][] object) {
      int iNumberRow=object.length;
      int iNumberCol=object[0].length;

      lseDataHybridDemand = new int[iNumberRow][NUM_LSE_PARAMS_HYBRID_LOAD];
      
      for(int i=0;i<iNumberRow;i++)
          for(int j=0; j<NUM_LSE_PARAMS_HYBRID_LOAD; j++)
              lseDataHybridDemand[i][j]=object[i][j];
  }

  public void setLSEDataFromGUI(double [][] object) {
      int iNumberRow=object.length;
      int iNumberCol=object[0].length;

      lseDataFixedDemand = new double[iNumberRow][NUM_LSE_PARAMS];
      lseSec1Data = new double[iNumberRow][NUM_LSE_3SEC_PARAMS];  // lesSec1 as double[3][10]
      lseSec2Data = new double[iNumberRow][NUM_LSE_3SEC_PARAMS];  // lesSec2 as double[3][10]
      lseSec3Data = new double[iNumberRow][NUM_LSE_3SEC_PARAMS];  // lesSec3 as double[3][10]
      
      for(int i=0;i<iNumberRow;i++)
          for(int j=0; j<NUM_LSE_PARAMS; j++) {
              lseDataFixedDemand[i][j]=object[i][j];
              
              if(j<2) {
                  lseSec1Data[i][j]=object[i][j];
                  lseSec2Data[i][j]=object[i][j];
                  lseSec3Data[i][j]=object[i][j];
              }
              
              if((j>=2)&&(j<10))
                  lseSec1Data[i][j]=object[i][j];
              
              if((j>=10)&&(j<18))
                  lseSec2Data[i][j-8]=object[i][j];
               
              if(j>=18)
                  lseSec3Data[i][j-16]=object[i][j];
         }
  }


  public void setNDGDataFromGUI(double [][] object) {
      int iNumberRow=object.length;
      int iNumberCol=object[0].length;

      NDGData = new double[iNumberRow][NUM_LSE_PARAMS];
      NDGSec1Data = new double[iNumberRow][NUM_LSE_3SEC_PARAMS];  // lesSec1 as double[3][10]
      NDGSec2Data = new double[iNumberRow][NUM_LSE_3SEC_PARAMS];  // lesSec2 as double[3][10]
      NDGSec3Data = new double[iNumberRow][NUM_LSE_3SEC_PARAMS];  // lesSec3 as double[3][10]
      
      for(int i=0;i<iNumberRow;i++)
          for(int j=0; j<NUM_LSE_PARAMS; j++) {
              NDGData[i][j]=object[i][j];
              
              if(j<2) {
                  NDGSec1Data[i][j]=object[i][j];
                  NDGSec2Data[i][j]=object[i][j];
                  NDGSec3Data[i][j]=object[i][j];
              }
              
              if((j>=2)&&(j<10))
                  NDGSec1Data[i][j]=object[i][j];
              
              if((j>=10)&&(j<18))
                  NDGSec2Data[i][j-8]=object[i][j];
               
              if(j>=18)
                  NDGSec3Data[i][j-16]=object[i][j];
         }
  }

  
  
  /**
     * Get a reference to the extra genco parameter map.
     * @return
     */
    public Map<String, CaseFileData.SCUCInputData> getExtraGenCoParams(){
        return this.extraGenCoParams;
    }

    /**
     * Set the extra genco params.
     *
     * Creates a shallow copy of the parameter.
     * @param extraGenCoParams
     */
    public void setExtraGenCoParams(Map<String, CaseFileData.SCUCInputData> extraGenCoParams) {
        this.extraGenCoParams = new HashMap<String, CaseFileData.SCUCInputData>(extraGenCoParams);
    }
  
  /**
     * Get a reference to the extra storage parameter map.
     * @return
     */
    public Map<String, CaseFileData.StorageInputData> getStorageParams(){
        return this.StorageParams;
    }
    
    /**
     * Set the extra storage params.
     *
     * Creates a shallow copy of the parameter.
     * @param storageParams
     */
    public void setStorageParams(Map<String, CaseFileData.StorageInputData> storageParams) {
        this.StorageParams = new HashMap<String, CaseFileData.StorageInputData>(storageParams);
    }

}
