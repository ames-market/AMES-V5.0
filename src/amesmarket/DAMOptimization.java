package amesmarket;

import java.io.IOException;
import java.util.List;

import amesmarket.extern.common.CommitmentDecision;
import amesmarket.filereaders.BadDataFileFormatException;

public interface DAMOptimization {

	/**
	 *
	 * @param day
	 * @throws IOException
	 * @throws AMESMarketException
	 * @throws BadDataFileFormatException
	 */
	public abstract void solveDAMOptimization(int day) throws IOException,
	AMESMarketException, BadDataFileFormatException;

	public abstract List<CommitmentDecision> getDAMCommitment();
        
        /**
         * 
         * @return
         */
        public double[][] getDAMDispatchSolution();
            
        /**
         * 
         * @return
         */
        public double[][] getDAMLMPSolution();

}
