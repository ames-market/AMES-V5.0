package amesmarket.extern.common;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

import amesmarket.AMESMarket;
//import amesmarket.AMESMarketException;
import amesmarket.GenAgent;
import amesmarket.ISO;
//import amesmarket.LoadCaseControl;
//import amesmarket.LoadProfileCollection;
//import amesmarket.SCUC;
import amesmarket.Support;
import amesmarket.filereaders.BadDataFileFormatException;

/**
 * Model the commitment decision for a genco.
 *
 * Bridge/adapter to sometimes get the information by actual genco
 * name, and also preserve the implicit/assumed order information that would
 * be lost if we just had a map from genco name to commitment.
 *
 * Several places in the program, especially the now legacy
 * {@link amesmarket.BUC} class assume the order of the gencos.
 *
 *
 */
public class CommitmentDecision {
    public final String generatorName;
    public final int generatorIdx;
    public final int[] commitmentDecisions;
    //public final int[] commitmentDecisionsbyTAU;

    /**
     * @param generatorName
     * @param generatorIdx
     * @param commitmentDecisions
     */
    public CommitmentDecision(String generatorName, int generatorIdx,
            int[] commitmentDecisions) {
        this.generatorName = generatorName;
        this.generatorIdx = generatorIdx;
        this.commitmentDecisions = commitmentDecisions;
    }

    /**
     * Copy constructor.
     * @param other
     */
    public CommitmentDecision(CommitmentDecision other) {
        generatorName = other.generatorName;
        generatorIdx = other.generatorIdx;
        commitmentDecisions = Arrays.copyOf(other.commitmentDecisions, other.commitmentDecisions.length);
    }

    
    // Should correct this function for compatibility with commitmentDecisions[][]
    public String toString() {
        StringBuilder sb =new StringBuilder();

        sb.append(generatorName);
        sb.append(": ");
        for(int i = 0;  i < commitmentDecisions.length; i++) {
            sb.append(commitmentDecisions[i]);
            if(i < commitmentDecisions.length -1)
                sb.append(", ");
        }

        return sb.toString();
    }
}

