

// TransGrid.java
//
// Transmission grid setup and formulations of rudimentary matrics in preparing
//  for DCOPFJ.java.  The matrix notations and Eq numebering follow DC-OPF paper.
//
// DC-OPF paper: Junjie Sun and Leigh Tesfatsion, (2006) "DC OPF Formulation
//       and Solution Using QuadProgJ", ISU Econ Working Paper Series #06014

package amesmarket;

import uchicago.src.sim.space.Object2DGrid;
import cern.colt.matrix.*;
import cern.colt.matrix.impl.*;

/** Example of what contains in nodeData and branchData

NodeData
//NN	penaltyCoeff
  3	0.05


BranchData
//From	To	MaxCap	Reactance
  1	2	100	0.08
  1	3	150	0.12
  2	3	120	0.05

*/


public class TransGrid {


  private static final int ID = 0;
  private static final int NN = 0;  //NN= number of buses
  private static final int PENALTY_COEFF = 1;
  private static final int FROM = 0;
  private static final int TO = 1;
  private static final int LINE_CAP = 2;
  private static final int REACTANCE = 3;


  private int numNodes;                        // K   (1x1)
  private double penaltyCoeff;                 // pi  (1x1)
  private double[][] vadWeight;                // W   (KxK)  ~ Eq(42)
    // vad = voltage angle difference
  private double[][] reducedVADWeight;         // Wrr (K-1)x(K-1) ~ Eq(45)
  private int numBranches;                     // N   (1x1)
  private double[][] branchIndex;              // BI  (Nx2)
  private double[] MaxCap;                    // T   (Nx1)
  private double[][] negativeSusceptance;      // B   (KxK)   ~ Eq(16)
  private double[][] busAdmittance;            // B'  (KxK)   ~ Eq(50)
  private double[][] reducedBusAdmittance;     // Br' (K-1)xK ~ Eq(51)
  private double[][] diagonalAdmittance;       // D   (NxN)   ~ Eq(55)
  private double[][] adjacency;                // A   (NxK)   ~ Eq(52)
  private double[][] reducedAdjacency;         // Ar  Nx(K-1) ~ Eq(53)
  private double[] reactance; //Nx1

  private DoubleMatrix2D ndata;  // to hold nodeData
  private DoubleMatrix2D bdata;  // to hold branchData
  private DoubleFactory2D fac2d = DoubleFactory2D.dense;
    // for using Colt's methods e.g. diagonal(), identity(), etc.
  private cern.jet.math.Functions F = cern.jet.math.Functions.functions;
    // F: Naming shortcut to save some keystrokes for calling Colt's functions

  private Object2DGrid powerSpace;
  private Object2DGrid genSpace;
  private Object2DGrid lseSpace;


  // Empty constructor for OPF to call
  public TransGrid(){

  }

  // Constructor
  public TransGrid(double[][] nodeData, double[][] branchData,
                   int xSize, int ySize){
//    for(int n=0; n<branchData.length; n++){
//      // Convert MaxCap from SI to PU
//      branchData[n][2] = branchData[n][2]/INIT.getBaseS();
//      // Convert reactance from SI to PU, x(pu) = x/Zo = x/(Vo^2/So) = (x*So)/Vo^2
//      branchData[n][3]
//          = (branchData[n][3]*INIT.getBaseS())/(INIT.getBaseV()*INIT.getBaseV());
//    }
    branchData = Support.correctRoundingError(branchData);

    numNodes = (int) nodeData[0][NN];
    penaltyCoeff = nodeData[0][PENALTY_COEFF];
    vadWeight = new double[numNodes][numNodes];
    reducedVADWeight = new double[numNodes-1][numNodes-1];
    numBranches = branchData.length;
    branchIndex = new double[numBranches][2];  //e.g., {{1,2},{1,4},{2,3},...}
    MaxCap = new double[numBranches];
    negativeSusceptance = new double[numNodes][numNodes];
    busAdmittance = new double[numNodes][numNodes];
    reducedBusAdmittance = new double[numNodes-1][numNodes];
    diagonalAdmittance = new double[numBranches][numBranches];
    adjacency = new double[numBranches][numNodes];
    reducedAdjacency = new double[numBranches][numNodes-1];
    reactance = new double[numBranches];

    ndata = new DenseDoubleMatrix2D(nodeData);
    bdata = new DenseDoubleMatrix2D(branchData);

    formMatrices();

    powerSpace = new Object2DGrid(xSize,ySize);
    genSpace   = new Object2DGrid(xSize, ySize);
    lseSpace   = new Object2DGrid(xSize, ySize);

    //Initialize the space objects by filling ech cell with an Integer Object.
    //The value of the Integer object will be the amount of power.
    for(int i = 0; i < xSize; i++){
      for(int j = 0; j <ySize; j++){
        powerSpace.putObjectAt(i,j,new Integer(0));
      }
    }
  }

  private void formMatrices(){
    setBranchIndex();
//    //setVADWeight();
//    setReducedVADWeight();
//    setLineCap();
//    //setNegativeSusceptance();
//    setBusAdmittance();
//    setReducedBusAdmittance();
//    setDiagonalAdmittance();
//    setAdjacency();
//    setReducedAdjacency();
    setReactance();
  }

  private void setBranchIndex(){
    branchIndex = bdata.viewPart(0,FROM,numBranches,2).toArray();
  }

  private void setVADWeight(){

    for(int n=0; n<numBranches; n++){
      vadWeight[(int)branchIndex[n][0]-1][(int)branchIndex[n][1]-1]
          = -2*penaltyCoeff;  //NOTE: there should be a factor 2 in front of penaltyCoeff
      vadWeight[(int)branchIndex[n][1]-1][(int)branchIndex[n][0]-1]
          = -2*penaltyCoeff;
    }
    for(int i=0; i<numNodes; i++){
      for(int j=0; j<numNodes; j++){
        if(j==i){
          for(int k=0; k<numNodes; k++){
            if(k!=i){
              vadWeight[i][j] = vadWeight[i][j] - vadWeight[i][k];
            }
          }
        }
      }
    }
    vadWeight = Support.correctRoundingError(vadWeight);
  }

  private void setReducedVADWeight(){
    for(int i=0; i<numNodes-1; i++){
      for(int j=0; j<numNodes-1; j++){
        reducedVADWeight[i][j] = vadWeight[i+1][j+1];
      }
    }

  }

  private void setLineCap(){
    MaxCap = bdata.viewColumn(LINE_CAP).toArray();
  }
  private void setNegativeSusceptance(){
    for(int n=0; n<numBranches; n++){
      negativeSusceptance[(int)branchIndex[n][0]-1][(int)branchIndex[n][1]-1]
          = 1/bdata.viewColumn(REACTANCE).toArray()[n];
      negativeSusceptance[(int)branchIndex[n][1]-1][(int)branchIndex[n][0]-1]
          = 1/bdata.viewColumn(REACTANCE).toArray()[n];

    }
  }

  private void setBusAdmittance(){
    for(int i=0; i<numNodes; i++){
      for(int j=0; j<numNodes; j++){
        if(j==i){
          for(int k=0; k<numNodes; k++){
            if(k!=i){
              busAdmittance[i][j] = busAdmittance[i][j] + negativeSusceptance[i][k];
            }
          }
        }
        else
          busAdmittance[i][j] = - negativeSusceptance[i][j];
      }
    }
  }
  private void setReducedBusAdmittance(){
    DoubleMatrix2D na = new DenseDoubleMatrix2D(busAdmittance);
    reducedBusAdmittance = na.viewPart(1,0,numNodes-1,numNodes).toArray();
  }
  private void setDiagonalAdmittance(){
    diagonalAdmittance = fac2d.diagonal(bdata.copy().viewColumn(REACTANCE)
                                      .assign(F.inv)).toArray();
    //NOTE: Have to keep .copy(), otherwise reactance will be 1/reactance
  }
  private void setAdjacency(){
    for(int n=0; n<numBranches; n++){
      for(int k=0; k<numNodes; k++){
        if(k == branchIndex[n][0]-1){
          adjacency[n][k] = 1;
        }
        else if (k == branchIndex[n][1]-1){
          adjacency[n][k] = -1;
        }
        else{
          adjacency[n][k] = 0;
        }
      }
    }
  }
  private void setReducedAdjacency(){
    DoubleMatrix2D adj = new DenseDoubleMatrix2D(adjacency);
    reducedAdjacency = adj.viewPart(0,1,numBranches,numNodes-1).toArray();
  }
  private void setReactance(){
    reactance = bdata.viewColumn(REACTANCE).toArray();
  }

  public int getNumNodes(){ return numNodes; }
  public double getPenaltyCoeff(){ return penaltyCoeff; }
  public double[][] getVADWeight(){return vadWeight;}
  public double[][] getReducedVADWeight(){return reducedVADWeight;}
  public int getNumBranches(){ return numBranches; }
  public double[][] getBranchIndex(){ return branchIndex; }
  public double[] getLineCap(){ return MaxCap; }
  public double[][] getNegativeSusceptance(){ return negativeSusceptance; }
  public double[][] getBusAdmittance(){ return busAdmittance; }
  public double[][] getReducedBusAdmittance(){ return reducedBusAdmittance;}
  public double[][] getDiagonalAdmittance(){ return diagonalAdmittance; }
  public double[][] getAdjacency(){ return adjacency; }
  public double[][] getReducedAdjacency(){ return reducedAdjacency; }
  public double[] getReactance(){ return reactance;}


  public Object2DGrid getCurrentPowerSpace(){
    return powerSpace;
  }
  public Object2DGrid getCurrentGenSpace(){
    return genSpace;
  }
  public Object2DGrid getCurrentLSESpace(){
    return lseSpace;
  }

  // This method is currently hard-coded to be 3-bus case, needs to be modified
  public void addGenAgentAtNodeK(GenAgent gen, int k){
    if(k==1){
      int x = (int) (0.5*genSpace.getSizeX());
      int y = (int) (0.2*genSpace.getSizeY());
      genSpace.putObjectAt(x, y, gen);
      gen.setXY(x, y);
    }
    else if(k==2){
      int x = (int) (0.2*genSpace.getSizeX());
      int y = (int) (0.8*genSpace.getSizeY());
      genSpace.putObjectAt(x,y,gen);
      gen.setXY(x,y);
    }
    else if(k==3){
      int x = (int) (0.8*genSpace.getSizeX());
      int y = (int) (0.8*genSpace.getSizeY());
      genSpace.putObjectAt(x,y,gen);
      gen.setXY(x,y);
    }
    else{
      System.out.print("");
    }
  }

  // LSEs are located right next to GENs
  // This method is currently hard-coded to be 3-bus case, needs to be modified
  public void addLSEAgentAtNodeK(LSEAgent lse, int k){
    if(k==1){
      int x = (int) (0.5*lseSpace.getSizeX()+1);
      int y = (int) (0.2*lseSpace.getSizeY());
      genSpace.putObjectAt(x,y,lse);
      lse.setXY(x,y);
    }
    else if(k==2){
      int x = (int) (0.2*lseSpace.getSizeX()+1);
      int y = (int) (0.8*lseSpace.getSizeY());
      genSpace.putObjectAt(x,y,lse);
      lse.setXY(x,y);
    }
    else if(k==3){
      int x = (int) (0.8*lseSpace.getSizeX()+1);
      int y = (int) (0.8*lseSpace.getSizeY());
      genSpace.putObjectAt(x,y,lse);
      lse.setXY(x,y);
    }
    else{
    }
  }




}
