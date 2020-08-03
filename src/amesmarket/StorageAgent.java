
// LSEAgent.java
// Storage Unit Agent (wholesale power buyer)
package amesmarket;

import fncs.JNIfncs;
import java.awt.Color;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import java.sql.*;

import amesmarket.CaseFileData.StorageInputData;
/**
 * Example showing what lseData[i] contains LSEData //ID	atBus	block	LP	block	LP
 * block	LP	block	LP 1	1	6	22	6	30	6	34	6	27
 *
 * // block: load block for the next how many hours // LP: load profile (fixed
 * demand)
 */
public class StorageAgent {

    private StorageInputData data;
    private int atBus;
    private int ID;
    
    // Constructor
    public StorageAgent(StorageInputData inputdata) {
        this.data = inputdata;
        this.ID = inputdata.getID();
        this.atBus = inputdata.getatBus();
    }

    public StorageInputData getData(){
        return this.data;
    }
    
    public void setData(StorageInputData inputdata){
        this.data = inputdata;
    }
    
}
