

/*
 * SplitTable.java
 *
 * Created on 2007 3 11, 7:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Output;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

import AMESGUIFrame.*;


public class SplitTable extends JFrame       {

    public SplitTable() {
        super("Output Table View");
    }

     /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    public  void createAndShowGUI() {
        //Create and set up the window.
        //JFrame frame = new SplitTable();
        
        TableView tableView = new TableView(amesFrame);
 
        SelectPanel selectPanel = new SelectPanel(amesFrame,true,tableView,null);
        
        //Provide minimum sizes for the two components in the split pane
        selectPanel.setMinimumSize(new Dimension(300, 50));
        tableView.setMinimumSize(new Dimension(50, 30));
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(selectPanel);
        splitPane.setRightComponent(tableView);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(430);


        //Add the split pane to this frame
        getContentPane().add(splitPane);

        //Display the window.
        pack();
        setVisible(true);
    }

    public void setAMESFrame(AMESFrame frame) {
        amesFrame=frame;
    }
    
    public void setIfTableView(boolean bView) {
        bTableView=bView;
    }
        private AMESFrame amesFrame;
        private boolean bTableView;

}
