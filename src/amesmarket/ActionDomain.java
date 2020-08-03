


package amesmarket;
// Action domain used in constructing action domain for agent learning

/** Action Domain Implementation (as an ArrayList)
 * Initialize the action domain for the generators (GenAgents)
 * Every element of AD consists of a triplet (lowerRI, upperRI, upperRCap)
 * where lowerRI   = lower Range Index, [0,RIMaxL]
 *       upperRI   = upper Range Index, [0,RIMaxU]
 *       upperRCap = upper relative capacity, [RIMinC,1]

 * AD = {triplet_1, triplet_2, ..., triplet_M}
 * The cardinality of AD is M1 * M2 * M3 = M
 *
 * Reference: DynTestAMES Working Paper Appendix A.3 Action Domain Construction
 * Available online at http://www.econ.iastate.edu/tesfatsi/DynTestAMES.JSLT.pdf
 */

import java.util.ArrayList;

public class ActionDomain {


  private ArrayList actionDomain;
  public static final int ADC = 3; // Action domain column = 3 for (lowerRI, upperRI, RCap)
  private double[] triplet;  // alpha's as in Eq(36) of DynTestAMES working paper
  public static final double SOME_LARGE_NUMBER = 10.0;

  // constructor
  public ActionDomain(int M1, int M2, int M3, 
      double RIMaxL, double RIMaxU, double RIMinC) {

    actionDomain = new ArrayList();
    
    if (M1>=1 && M2>=1 && M3>=1 && RIMaxL<1 && RIMaxL>=0 
        && RIMaxU<1 && RIMaxU>=0 && RIMinC<=1 && RIMinC>0){
      
      double inc1 = 0;
      double inc2 = 0;
      double inc3 = 0;  
      double someLargeNumber = SOME_LARGE_NUMBER;  
      // NOTE: someLargeNumber is used to make inc1-inn3 so large that the 
      //       following 3-layer for loops will stop with only the first 
      //       iteration, which is desirable for the case where M1-M3 are set to 1
      
      if(M1==1){
        inc1 = someLargeNumber; // special case for only one choice of lower RI 
      }
      else{
        inc1 = RIMaxL/(M1-1);  // incremental step for lower RI
      } 
      if(M2==1){
        inc2 = someLargeNumber; // special case for only one choice of upper RI
      }
      else{
        inc2 = RIMaxU/(M2-1);  // incremental step for upper RI
      }
      if(M3==1){
        inc3 = someLargeNumber; // special case for only one choice of upper Cap
      }
      else{
        inc3 = (1-RIMinC)/(M3-1); // incremental step for upper Cap
      }
      
      for(double i=0; i<=RIMaxL; i=i+inc1){
        for(double j=0; j<=RIMaxU; j=j+inc2){
          for(double k=1-RIMinC; k>=0; k=k-inc3){
            triplet = new double[ADC]; // initialize triplet to zeros (IMPORTANT!)
            triplet[0] = i; 
            triplet[1] = j; 
            triplet[2] = k + RIMinC; 
            actionDomain.add(triplet); // NOTE: triplet is passed by reference
          }
        }
      }
      //NOTE: the above loop takes care of the case where M1, M2,or M3 are set to 1.
    }
    else{
      System.out.println("INCORRECT PARAMETER RANGES: M1, M2, M3, RIMaxL, RIMaxU, RIMinC.");
    }
  }

  public ArrayList getActionDomain(){
    return actionDomain;
  }


}
