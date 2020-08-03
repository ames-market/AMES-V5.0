package amesmarket.extern.psst;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Configuration information for using a PSST program to run some
 * optimization problem.
 *
 * This class is generating commands used to run the SCUC/SCED
 * optimizations.
 *
 */
public class PSSTConfig {


	private final String[] args;
	/**
	 * Create a psst program.
	 * @param psstProg
	 * @param solutionWriter
	 */
	private PSSTConfig(String ... psstProgArgs) {
		this.args = psstProgArgs;
	}

	/**
	 *
         * @param MarketOp
         * @param UCFile
         * @param ReferenceModelFile
         * @param ResultsFile
         * @param solver
	 * @return
	 */
	public static PSSTConfig createDeterministicPSST(String MarketOp, File UCFile, File ReferenceModelFile, File ResultsFile, String solver) {
		return new PSSTConfig("psst", MarketOp,
                                "--uc", UCFile.getAbsolutePath(),
				"--data", ReferenceModelFile.getAbsolutePath(),
				"--output", ResultsFile.getAbsolutePath(),
                                "--solver", solver);
	}

	/**
	 * Get the arguments that will on invoked to start the process.
	 * @return
	 */
	public String[] getExecCmd() {
		return this.args;
	}

	/**
	 * Start a new process that runs a PSST program.
        * @param dir
	 * @return
	 * @throws IOException
	 */
	public Process createPSSTProcess(File dir) throws IOException{
		ProcessBuilder pb = new ProcessBuilder(this.getExecCmd());
		pb.directory(dir);
		return pb.start();
	}

}

