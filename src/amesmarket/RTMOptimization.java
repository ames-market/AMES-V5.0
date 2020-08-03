
package amesmarket;

/**
 * Defines the methods for running a SCED computation
 * and getting the computed data back to the AMES simulation.
 *
 *
 */
public interface RTMOptimization {
    
    /**
     * Run the solver.
     * @param interval
     */
    public void solveRTMOptimization(int interval) throws AMESMarketException;
    
    /**
     * 
     * @return
     */
    public double[][] getRTMDispatchSolution();
    
    /**
     * 
     * @return
     */
    public double[][] getRTMLMPSolution();
    
    /**
     * 
     * @return
     */
    //public double[] getDailyLMP();
    
    /**
     * 
     * @return
     */
//    public double[][] getRTMBranchFlowSolution();
    
    /**
     * 
     * @return
     */
    public double[][] getRTMPriceSensitiveDemandSolution();
    
}
