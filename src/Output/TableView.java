

/*
 * TableView.java
 *
 * Created on 2007 3 11, 7:27
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Output;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.lang.Math;

import AMESGUIFrame.*;
import java.awt.Component;


public class TableView extends JPanel {
  private boolean DEBUG = false;

public TableView(AMESFrame frame) {
    super(new GridLayout(1, 0));
    amesFrame=frame;

    iconHeaderRender = new DefaultTableCellRenderer() {
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            // Inherit the colors and font from the header component
            if (table != null) {
                JTableHeader header = table.getTableHeader();
                if (header != null) {
                    setForeground(header.getForeground());
                    setBackground(header.getBackground());
                    setFont(header.getFont());
                }
            }

            if (value instanceof ImageIcon) {
                setIcon((ImageIcon)value);
            } else {
                setText((value == null) ? "" : value.toString());
            }
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(JLabel.CENTER);
            return this;
        }
    };
        
    table = new JTable( );
    table.setPreferredScrollableViewportSize(new Dimension(300, 70));

    //Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(table);

    //Add the scroll pane to this panel.
    add(scrollPane);
    
  }


  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  private static void createAndShowGUI() {
    //Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);

    //Create and set up the window.
    JFrame frame = new JFrame("TableView");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    AMESFrame amesFrame = new AMESFrame( );
    TableView newContentPane = new TableView(amesFrame);
    newContentPane.setOpaque(true); //content panes must be opaque
    frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public  void createView() {
    //Make sure we have nice window decorations.
    JFrame.setDefaultLookAndFeelDecorated(true);

    //Create and set up the window.
    JFrame frame = new JFrame("TableView");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    //Create and set up the content pane.
    TableView newContentPane = new TableView(amesFrame);
    newContentPane.setOpaque(true); //content panes must be opaque
    frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
  }

  public void displayGridLinesData(int [] selectIndex) {
      String [] names =  {
                "Line name",  "From", "To", "MaxCap (MW)", "Reactance (ohm)"
            };
      int [] columnWidth={100, 80, 80, 120, 150};
        
      Object [][] branchData=amesFrame.getBranchData( );
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
            DefaultTableModel loadDataModel = new DefaultTableModel(branchData,  names);

            table.setModel(loadDataModel);
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
          Object [][] data=new Object[iDataNumber][iField];
          
          for(int i=0; i<iDataNumber; i++)
              for(int j=0; j<iField; j++){
                  if(j<3){
                    data[i][j]=branchData[selectIndex[i]-1][j];
                  }
                  else{
                      double dTemp=Double.parseDouble(branchData[selectIndex[i]-1][j].toString());
                      data[i][j]=String.format("%1$15.4f", dTemp);
                  }
              }
          
            DefaultTableModel loadDataModel = new DefaultTableModel(data,  names);

            table.setModel(loadDataModel);
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
       for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidth[i]);
        }

        DefaultTableCellRenderer   render   =   new   DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class,   render);   

        table.repaint();
 }
  
  public void displayGeneratorData(int [] selectIndex) {
      String [] names =  {
                "GenCo Name", "ID", "atBus", "SCost ($/H)", "a ($/MWh)", "b ($/MW2h)", "CapL (MW)", "CapU (MW)", "NS", "InitMoney ($)"
           };
        
      Object [][] genData=amesFrame.getGeneratorData( );
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
            DefaultTableModel loadDataModel = new DefaultTableModel(genData,  names);

            table.setModel(loadDataModel);
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
          Object [][] data=new Object[iDataNumber][iField];
          
          for(int i=0; i<iDataNumber; i++)
              for(int j=0; j<iField; j++)
                  data[i][j]=genData[selectIndex[i]-1][j];
          
            DefaultTableModel loadDataModel = new DefaultTableModel(data,  names);

            table.setModel(loadDataModel);
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
       column=table.getColumnModel().getColumn(5);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon constbIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constb.gif"));
       column.setHeaderValue(constbIcon);
       
       column=table.getColumnModel().getColumn(6);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon caplIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capl.gif"));
       column.setHeaderValue(caplIcon);
        
       column=table.getColumnModel().getColumn(7);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon capuIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capu.gif"));
       column.setHeaderValue(capuIcon);

       for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            if((i == 2)||(i == 1)){
                column.setPreferredWidth(60);
             }
            else {
               column.setPreferredWidth(120);
             }
        }

        DefaultTableCellRenderer   render   =   new   DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class,   render);   
     
        table.repaint();
 }
  
  public void displayLSEFixedDemandData(int [] selectIndex) {
      String [] names =  {
            "LSE Name", "ID", "atBus", "H-00 (MW)", "H-01 (MW)", "H-02 (MW)", "H-03 (MW)", "H-04 (MW)", "H-05 (MW)",
            "H-06 (MW)", "H-07 (MW)", "H-08 (MW)", "H-09 (MW)", "H-10 (MW)", "H-11 (MW)",
            "H-12 (MW)", "H-13 (MW)", "H-14 (MW)", "H-15 (MW)", "H-16 (MW)", "H-17 (MW)",
            "H-18 (MW)", "H-19 (MW)", "H-20 (MW)", "H-21 (MW)", "H-22 (MW)", "H-23 (MW)"
            };
        
      Object [][] lseData=amesFrame.getLSEData( );
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
            DefaultTableModel loadDataModel = new DefaultTableModel(lseData,  names);

            table.setModel(loadDataModel);
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
          Object [][] data=new Object[iDataNumber][iField];
          
          for(int i=0; i<iDataNumber; i++)
              for(int j=0; j<iField; j++)
                  data[i][j]=lseData[selectIndex[i]-1][j];
          
            DefaultTableModel loadDataModel = new DefaultTableModel(data,  names);

            table.setModel(loadDataModel);
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            if( i == 0 ){
                column.setPreferredWidth(100);
             }
            else{
                column.setPreferredWidth(80);
             }
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
        
        table.repaint();
 }
  
  public void displayLSEPSDemandData(int [] selectIndex) {
      String [] names =  {
            "LSE Name", "ID", "atBus", "hourIndex", "c ($/MWh)", "d ($/MW2h)", "SLMax (MW)"
            };
      
      int [] iColumnWidth={90, 70, 70, 90, 90, 90, 90};
        
      Object [][][] lseData=amesFrame.getLSEPriceSensitiveDemandData();
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          Object [][] data=new Object[lseData.length*24][7];
          
          for(int i=0; i<lseData.length; i++) {
             for(int h=0; h<24; h++){
                  for(int j=0; j<7; j++) {
                          data[i*24+h][j]=lseData[i][h][j];
                  }
             }
          }
          
            DefaultTableModel loadDataModel = new DefaultTableModel(data,  names);

            table.setModel(loadDataModel);
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
          Object [][] data=new Object[iDataNumber*24][iField];
          
          for(int i=0; i<iDataNumber; i++)
             for(int h=0; h<24; h++){
                  for(int j=0; j<7; j++) {
                          data[i*24+h][j]=lseData[selectIndex[i]-1][h][j];
                  }
             }
          
            DefaultTableModel loadDataModel = new DefaultTableModel(data,  names);

            table.setModel(loadDataModel);
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
       column=table.getColumnModel().getColumn(5);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon constdIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constd.gif"));
       column.setHeaderValue(constdIcon);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
        
        table.repaint();
 }
  
  public void displayInitialRandomSeedData(int [] selectIndex) {
      String [] names =  {
            "Initial Random Seed"
            };
        
      Object [][] randomData= new Object[1][1];
      randomData[0][0]=amesFrame.GetRandomSeed();

      DefaultTableModel loadDataModel = new DefaultTableModel(randomData,  names);

      table.setModel(loadDataModel);
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            if( i == 0 ){
                column.setPreferredWidth(200);
             }
            else{
                column.setPreferredWidth(80);
             }
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
        
        table.repaint();
 }

  public void displayStoppingRuleData(int [] selectIndex) {
      String [] names =  {
            "No.", "Explanation"
            };
      int [] iColumnWidth={40, 800};
        
      Object [][] stoppingRuleData= new Object[5][2];
      
      if(amesFrame.bMaximumDay)
        stoppingRuleData[0][0]="X (1)";
      else
        stoppingRuleData[0][0]="  (1)";
          
      stoppingRuleData[0][1]="Maximum Day Check. The user-specified maximum day:"+amesFrame.getMaxDay();
      
      if(amesFrame.bThreshold)
        stoppingRuleData[1][0]="X (2)";
      else
        stoppingRuleData[1][0]="  (2)";
          
      stoppingRuleData[1][1]="Threshold Probability Check. The user-specified threshold probability:"+amesFrame.getThresholdProbability();
      
      if(amesFrame.bActionProbabilityCheck)
        stoppingRuleData[2][0]="X (3)";
      else
        stoppingRuleData[2][0]="  (3)";
          
      stoppingRuleData[2][1]="GenCo Action Probability Check. The user-specified start day:"+ amesFrame.iStartDay+"\t Consecutive day length:"+amesFrame.iCheckDayLength+"\t Probability difference:" + amesFrame.dActionProbability;
      
      if(amesFrame.bLearningCheck)
        stoppingRuleData[3][0]="X (4)";
      else
        stoppingRuleData[3][0]="  (4)";
          
      stoppingRuleData[3][1]="GenCo Action Stability Check. The user-specified start day:"+ amesFrame.iLearningCheckStartDay+"\t Consecutive day length:"+amesFrame.iLearningCheckDayLength+"\t Difference:" + amesFrame.dLearningCheckDifference;
      
      if(amesFrame.bDailyNetEarningThreshold)
        stoppingRuleData[4][0]="X (5)";
      else
        stoppingRuleData[4][0]="  (5)";
          
      stoppingRuleData[4][1]="Daily Net Earning Threshold Check. The user-specified start day:"+ amesFrame.iDailyNetEarningStartDay+"\t Consecutive day length:"+amesFrame.iDailyNetEarningDayLength+"\t Threshold:" + amesFrame.dDailyNetEarningThreshold;

      DefaultTableModel loadDataModel = new DefaultTableModel(stoppingRuleData,  names);

      table.setModel(loadDataModel);
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            column.setPreferredWidth(iColumnWidth[i]);
         }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.LEFT);   
        table.setDefaultRenderer(Object.class, render);   
        
        table.repaint();
 }

  public void displayGeneratorProfitWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 " GenCo Name", "  Hour",  "Profit ($/H)", "Net Earnings ($/H)", "Revenues ($/H)"};
      int [] iColumnWidth={90, 90, 120, 120, 120};
      
      ArrayList genAgentProfitWithTrueCost=amesFrame.getAMESMarket().getGenAgentProfitAndNetGainWithTrueCost();
      
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          displayData=new Object [iGenNumber*(24+1)][5];

          double [][] genProfittWithTrueCost=(double [][])genAgentProfitWithTrueCost.get(0);
          for(int i=0; i<24+1; i++) {
              for(int j=0; j<iGenNumber; j++){
                  int iStartIndex=i*iGenNumber;

                displayData[iStartIndex+j][0]=genData[j][0];
                
                if(i==0){
                    displayData[iGenNumber*24+j][1]="Total";
                    displayData[iGenNumber*24+j][2]=ChangeToCurrency(genProfittWithTrueCost[j][i]);
                    displayData[iGenNumber*24+j][3]=ChangeToCurrency(genProfittWithTrueCost[j][i+1]);
                    displayData[iGenNumber*24+j][4]=ChangeToCurrency(genProfittWithTrueCost[j][i+2]);
                }
                else{
                    iStartIndex=(i-1)*iGenNumber;
                    displayData[iStartIndex+j][1]=hoursName[i-1];
                    displayData[iStartIndex+j][2]=ChangeToCurrency(genProfittWithTrueCost[j][i+2]);
                    displayData[iStartIndex+j][3]=ChangeToCurrency(genProfittWithTrueCost[j][i+2+24]);
                    displayData[iStartIndex+j][4]=ChangeToCurrency(genProfittWithTrueCost[j][i+2+24+24]);
                }
                
               }
          }
      }
      else{
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          displayData=new Object [iDataNumber*(24+1)][5];

          double [][] genProfittWithTrueCost=(double [][])genAgentProfitWithTrueCost.get(0);
          for(int i=0; i<24+1; i++) {
              for(int j=0; j<iDataNumber; j++){
                  int iStartIndex=i*iDataNumber;

                displayData[iStartIndex+j][0]=genData[selectIndex[j]-1][0];
                
                if(i==0){
                    displayData[iDataNumber*24+j][1]="Total";

                    displayData[iDataNumber*24+j][2]=ChangeToCurrency(genProfittWithTrueCost[selectIndex[j]-1][i]);
                    displayData[iDataNumber*24+j][3]=ChangeToCurrency(genProfittWithTrueCost[selectIndex[j]-1][i+1]);
                    displayData[iDataNumber*24+j][4]=ChangeToCurrency(genProfittWithTrueCost[selectIndex[j]-1][i+2]);
                }
                else{
                    iStartIndex=(i-1)*iDataNumber;
                    displayData[iStartIndex+j][1]=hoursName[i-1];

                    displayData[iStartIndex+j][2]=ChangeToCurrency(genProfittWithTrueCost[selectIndex[j]-1][i+2]);
                    displayData[iStartIndex+j][3]=ChangeToCurrency(genProfittWithTrueCost[selectIndex[j]-1][i+2+24]);
                    displayData[iStartIndex+j][4]=ChangeToCurrency(genProfittWithTrueCost[selectIndex[j]-1][i+2+24+24]);
                }
                
               }
          }
      }
 
      DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
      table.setModel(loadDataModel);

      TableColumn column = null;
      table.setAutoscrolls(true);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
      for (int i = 0; i < names.length; i++) {
          column =table.getColumnModel().getColumn(i);
            
          column.setPreferredWidth(iColumnWidth[i]);
      }

      DefaultTableCellRenderer   render   =   new   DefaultTableCellRenderer();   
      render.setHorizontalAlignment(JLabel.RIGHT);   
      table.setDefaultRenderer(Object.class,   render);   
     
      table.repaint();
 }

  public void displayGeneratorCommitmentWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 " GenCo Name", "  Hour",  " Power (MW)", "CapL (MW)", "CapU (MW)" 
           };
      int [] iColumnWidth={90, 90, 90, 90, 90};
      
      ArrayList genAgentCommitmentWithTrueCost=amesFrame.getAMESMarket().getGenAgentDispatchWithTrueCost();
      
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          displayData=new Object [iGenNumber*24][5];

          double [][] genCommitmentWithTrueCost=(double [][])genAgentCommitmentWithTrueCost.get(0);
          for(int i=0; i<24; i++) {
              for(int j=0; j<iGenNumber; j++){
                  int iStartIndex=i*iGenNumber;

                displayData[iStartIndex+j][0]=genData[j][0];
                displayData[iStartIndex+j][1]=hoursName[i];
                displayData[iStartIndex+j][2]=String.format("%1$15.2f", genCommitmentWithTrueCost[i][j]);
                displayData[iStartIndex+j][3]=genData[j][6];
                displayData[iStartIndex+j][4]=genData[j][7];
               }
          }
      }
      else{
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          displayData=new Object [iDataNumber*24][5];

          double [][] genCommitmentWithTrueCost=(double [][])genAgentCommitmentWithTrueCost.get(0);
          for(int i=0; i<24; i++) {
              for(int j=0; j<iDataNumber; j++){
                  int iStartIndex=i*iDataNumber;

                displayData[iStartIndex+j][0]=genData[selectIndex[j]-1][0];
                displayData[iStartIndex+j][1]=hoursName[i];
                displayData[iStartIndex+j][2]=String.format("%1$15.2f", genCommitmentWithTrueCost[i][selectIndex[j]-1]);
                displayData[iStartIndex+j][3]=genData[selectIndex[j]-1][6];
                displayData[iStartIndex+j][4]=genData[selectIndex[j]-1][7];
               }
          }
      }
 
      DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
      table.setModel(loadDataModel);

      TableColumn column = null;
      table.setAutoscrolls(true);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
      column=table.getColumnModel().getColumn(3);
      column.setHeaderRenderer(iconHeaderRender);
      ImageIcon caplIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capl.gif"));
      column.setHeaderValue(caplIcon);
        
      column=table.getColumnModel().getColumn(4);
      column.setHeaderRenderer(iconHeaderRender);
      ImageIcon capuIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capu.gif"));
      column.setHeaderValue(capuIcon);
 
      for (int i = 0; i < names.length; i++) {
          column =table.getColumnModel().getColumn(i);
            
          column.setPreferredWidth(iColumnWidth[i]);
      }

      DefaultTableCellRenderer   render   =   new   DefaultTableCellRenderer();   
      render.setHorizontalAlignment(JLabel.RIGHT);   
      table.setDefaultRenderer(Object.class,   render);   
     
      table.repaint();
 }

  public void displayPSDemandDispatchWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "LSE Name", "Hour",  "c ($/MWh)", "d ($/MW2h)", "SLMax (MW)", "Price-Sensitive Demand (MW)"
           };
      int [] iColumnWidth={90, 90, 90, 90, 90, 200};
      
      ArrayList LSEAgentPriceSensitiveDemandWithTrueCost=amesFrame.getAMESMarket().getLSEAgentPriceSensitiveDemandWithTrueCost();
      Object [][][] lsePriceSensitiveData=amesFrame.getLSEPriceSensitiveDemandData();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lseHybridData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          displayData=new Object [iLSENumber*24][6];

          double [][] priceSensitive=(double [][])LSEAgentPriceSensitiveDemandWithTrueCost.get(0);
          
          for(int i=0; i<24; i++) {
              int iStartIndex=i*iLSENumber;
              int psLoadIndex=0;
              
              for(int j=0; j<iLSENumber; j++){
                  int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[j][i+3].toString());

                  displayData[iStartIndex+j][0]=lsePriceSensitiveData[j][i][0];
                  displayData[iStartIndex+j][1]=hoursName[i];
                  displayData[iStartIndex+j][2]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][4]);
                  displayData[iStartIndex+j][3]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][5]);
                  displayData[iStartIndex+j][4]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][6]);

                  if((hourlyLoadHybridFlagByLSE&2)==2)
                      displayData[iStartIndex+j][5]=String.format("%1$15.2f", Math.abs(priceSensitive[i][psLoadIndex++]));
                  else
                      displayData[iStartIndex+j][5]=String.format("%1$15.2f", 0.0);
               }
          }
      }
      else{
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          displayData=new Object [iDataNumber*24][6];

          double [][] priceSensitive=(double [][])LSEAgentPriceSensitiveDemandWithTrueCost.get(0);
          for(int i=0; i<24; i++) {
              for(int j=0; j<iDataNumber; j++){
                  int psLoadIndex=0;
                  // Get the psLoadIndex
                  for(int k=0; k<selectIndex[j]-1; k++){
                    int hourlyFlag=Integer.parseInt(lseHybridData[k][i+3].toString());
                    if((hourlyFlag&2)==2)
                        psLoadIndex++;
                  }
                  int iStartIndex=i*iDataNumber;
                  int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[selectIndex[j]-1][i+3].toString());

                  displayData[iStartIndex+j][0]=lsePriceSensitiveData[selectIndex[j]-1][i][0];
                  displayData[iStartIndex+j][1]=hoursName[i];
                  displayData[iStartIndex+j][2]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][4]);
                  displayData[iStartIndex+j][3]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][5]);
                  displayData[iStartIndex+j][4]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][6]);
                  if((hourlyLoadHybridFlagByLSE&2)==2)
                      displayData[iStartIndex+j][5]=String.format("%1$15.2f", Math.abs(priceSensitive[i][psLoadIndex++]));
                  else
                      displayData[iStartIndex+j][5]=String.format("%1$15.2f", 0.0);
               }
          }
      }
 
      DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
      table.setModel(loadDataModel);

      TableColumn column = null;
      table.setAutoscrolls(true);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
       column=table.getColumnModel().getColumn(3);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon constdIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constd.gif"));
       column.setHeaderValue(constdIcon);
 
      for (int i = 0; i < names.length; i++) {
          column =table.getColumnModel().getColumn(i);
            
          column.setPreferredWidth(iColumnWidth[i]);
      }

      DefaultTableCellRenderer   render   =   new   DefaultTableCellRenderer();   
      render.setHorizontalAlignment(JLabel.RIGHT);   
      table.setDefaultRenderer(Object.class,   render);   
     
      table.repaint();
 }

  public void displayLSESurplusWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "LSE Name", "Hour",  "P-S Net Earnings ($/H)"
           };
      int [] iColumnWidth={90, 150, 150};
      
      ArrayList LSEAgentSurplusWithTrueCost=amesFrame.getAMESMarket().getLSEAgentSurplusWithTrueCost();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lseHybridData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          displayData=new Object [iLSENumber*(24+1)][3];

          double [][] surplus=(double [][])LSEAgentSurplusWithTrueCost.get(0);
          
          for(int i=0; i<24+1; i++) {
              
              for(int j=0; j<iLSENumber; j++){
                  int iStartIndex=i*iLSENumber;
                  displayData[iStartIndex+j][0]=lseHybridData[j][0];
                  
                  if(i==0){
                      displayData[iLSENumber*24+j][1]="Total P-S Net Earnings";
                      displayData[iLSENumber*24+j][2]=ChangeToCurrency(surplus[j][i]);
                  }
                  else{
                      iStartIndex=(i-1)*iLSENumber;
                      displayData[iStartIndex+j][1]=hoursName[i-1];
                      displayData[iStartIndex+j][2]=ChangeToCurrency(surplus[j][i]);
                  }
                  
               }
          }
      }
      else{
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          displayData=new Object [iDataNumber*(24+1)][3];

          double [][] surplus=(double [][])LSEAgentSurplusWithTrueCost.get(0);
          for(int i=0; i<24+1; i++) {
              for(int j=0; j<iDataNumber; j++){
                  int iStartIndex=i*iDataNumber;

                  displayData[iStartIndex+j][0]=lseHybridData[selectIndex[j]-1][0];
                 
                  if(i==0){
                      displayData[iDataNumber*24+j][1]="Total P-S Net Earnings";
                      displayData[iDataNumber*24+j][2]=ChangeToCurrency(surplus[selectIndex[j]-1][i]);
                  }
                  else{
                      iStartIndex=(i-1)*iDataNumber;
                      displayData[iStartIndex+j][1]=hoursName[i-1];
                      displayData[iStartIndex+j][2]=ChangeToCurrency(surplus[selectIndex[j]-1][i]);
                  }
                  
               }
          }
      }
 
      DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
      table.setModel(loadDataModel);

      TableColumn column = null;
      table.setAutoscrolls(true);
      table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

      for (int i = 0; i < names.length; i++) {
          column =table.getColumnModel().getColumn(i);
            
          column.setPreferredWidth(iColumnWidth[i]);
      }

      DefaultTableCellRenderer   render   =   new   DefaultTableCellRenderer();   
      render.setHorizontalAlignment(JLabel.RIGHT);   
      table.setDefaultRenderer(Object.class,   render);   
     
      table.repaint();
 }

  public void displayNodeLMPWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "Bus Name", "   Hour", "LMP ($/MWh)"
           };
       int [] iColumnWidth={90, 90, 90};
     
      ArrayList LMPWithTrueCost=amesFrame.getAMESMarket().getLMPWithTrueCost();
      
      String [] nodeName=amesFrame.getNodeNameData( );
      int iNodeNumber=nodeName.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          displayData=new Object [iNodeNumber*24][3];

          double [][] lmp=(double [][])LMPWithTrueCost.get(0);
          for(int i=0; i<24; i++) {
              for(int j=0; j<iNodeNumber; j++){
                  int iStartIndex=i*iNodeNumber;

                displayData[iStartIndex+j][0]=nodeName[j];
                displayData[iStartIndex+j][1]=hoursName[i];
                displayData[iStartIndex+j][2]=ChangeToCurrency(lmp[i][j]);
               }
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          displayData=new Object [iDataNumber*24][3];

          double [][] lmp=(double [][])LMPWithTrueCost.get(0);
          for(int i=0; i<24; i++) {
              for(int j=0; j<iDataNumber; j++){
                  int iStartIndex=i*iDataNumber;

                displayData[iStartIndex+j][1]=hoursName[i];;
                displayData[iStartIndex+j][0]=nodeName[selectIndex[j]-1];
                displayData[iStartIndex+j][2]=ChangeToCurrency(lmp[i][selectIndex[j]-1]);
               }
          }
                  
     }

      DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
      table.setModel(loadDataModel);
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer   render   =   new   DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class,   render);   
     
        table.repaint();
 }

  public void displayGeneratorSupplyOfferData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int [] selectIndex) {
      String [] names =  {
                 "GenCo Name", "Day Index", "aR ($/MWh)", "bR ($/MW2h)", "CapRL (MW)", "CapRU (MW)",
           };
      
      ArrayList genAgentSupplyOfferByDay=amesFrame.getAMESMarket().getGenAgentSupplyOfferByDay();
      
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")) {
                int iDayNumber=genAgentSupplyOfferByDay.size();
                displayData=new Object [iGenNumber*iDayNumber][6];
              
              double [][] genOffer;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iStartIndex=iDay*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;

                      for(int j=2; j<6; j++){
                          displayData[iStartIndex+i][j]=String.format("%1$15.4f", genOffer[i][j-2]);
                      }
                  }
              }
                
              DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
              table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iGenNumber*iDayNumber][6];
              
              double [][] genOffer;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;

                      for(int j=2; j<6; j++){
                          displayData[iStartIndex+i][j]=String.format("%1$15.4f", genOffer[i][j-2]);
                      }
                  }
              }
                
              DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
              table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")) {
                int iDayNumber=genAgentSupplyOfferByDay.size();
                displayData=new Object [iGenNumber*iDayNumber][6];
              
              double [][] genOffer;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iStartIndex=iDay*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;

                      for(int j=2; j<6; j++){
                          displayData[iStartIndex+i][j]=String.format("%1$15.4f", genOffer[i][j-2]);
                      }
                  }
              }
                
              DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
              table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iGenNumber*iDayNumber][6];
              
              double [][] genOffer;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;

                      for(int j=2; j<6; j++){
                          displayData[iStartIndex+i][j]=String.format("%1$15.4f", genOffer[i][j-2]);
                      }
                  }
              }
                
              DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
              table.setModel(loadDataModel);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")) {
                    int iDayNumber=genAgentSupplyOfferByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber][6];

                  double [][] genOffer;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                     int iStartIndex=iDay*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=genData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+1;

                          for(int j=2; j<6; j++){
                              displayData[iStartIndex+i][j]=String.format("%1$15.4f", genOffer[selectIndex[i]-1][j-2]);
                          }
                      }
                 }
                  
                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
            }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber][6];

                  double [][] genOffer;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                     int iStartIndex=(iDay-(iStartTime-1))*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=genData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+1;

                          for(int j=2; j<6; j++){
                              displayData[iStartIndex+i][j]=String.format("%1$15.4f", genOffer[selectIndex[i]-1][j-2]);
                          }
                      }
                 }
                  
                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
            }
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
       column=table.getColumnModel().getColumn(2);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon reportaIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/reporta.gif"));
       column.setHeaderValue(reportaIcon);
       
       column=table.getColumnModel().getColumn(3);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon reportbIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/reportb.gif"));
       column.setHeaderValue(reportbIcon);
       
       column=table.getColumnModel().getColumn(4);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon caprlIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/caprl.gif"));
       column.setHeaderValue(caprlIcon);
        
       column=table.getColumnModel().getColumn(5);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon capruIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capru.gif"));
       column.setHeaderValue(capruIcon);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            if( i == 0 ){
                column.setPreferredWidth(100);
             }
            else {
               column.setPreferredWidth(90);
             }
        }

        DefaultTableCellRenderer   render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class,   render);   
     
        table.repaint();
 }

  public void displayGeneratorCommitmentData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 " GenCo Name", "Day Index","  Hour", " Power (MW)", "CapRL (MW)", "CapRU (MW)" 
           };
      int [] iColumnWidth={90, 70, 90, 90, 90, 90};
      
      iStartTime=iStartTime-1;
      iEndTime=iEndTime-1;
      
      ArrayList genAgentCommitmentByDay=amesFrame.getAMESMarket().getGenAgentDispatchByDay();
      ArrayList genAgentSupplyOfferByDay=amesFrame.getAMESMarket().getGenAgentSupplyOfferByDay();
      
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      Object [][] displayData;
      double [][] genOffer;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                int iDayNumber=genAgentCommitmentByDay.size();
                displayData=new Object [iGenNumber*iDayNumber][8];
              
              double [][] genCommitment;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 
                 genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iStartIndex=iDay*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                      displayData[iStartIndex+i][3]=String.format("%1$15.2f", genCommitment[iDayHour][i]);
                      displayData[iStartIndex+i][3]=String.format("%1$15.2f", Math.abs(genCommitment[iDayHour][i]));
                      
                      displayData[iStartIndex+i][4]=String.format("%1$15.2f", genOffer[i][2]);
                      displayData[iStartIndex+i][5]=String.format("%1$15.2f", genOffer[i][3]);
                 }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iGenNumber*iDayNumber][8];
              
              double [][] genCommitment;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                     
                      displayData[iStartIndex+i][3]=String.format("%1$15.2f", Math.abs(genCommitment[iDayHour][i]));
                      
                      displayData[iStartIndex+i][4]=String.format("%1$15.2f", genOffer[i][2]);
                      displayData[iStartIndex+i][5]=String.format("%1$15.2f", genOffer[i][3]);
                 }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                int iDayNumber=genAgentCommitmentByDay.size();
                displayData=new Object [iGenNumber*iDayNumber*24][8];
              
              double [][] genCommitment;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iDayStartIndex=iDay*iGenNumber*24;
                 
                  for(int i=0; i<24; i++) {
                      for(int j=0; j<iGenNumber; j++){
                          int iStartIndex=iDayStartIndex+i*iGenNumber;

                        displayData[iStartIndex+j][0]=genData[j][0];
                        displayData[iStartIndex+j][1]=iDay+2;
                        displayData[iStartIndex+j][2]=hoursName[i];
                          
                        displayData[iStartIndex+j][3]=String.format("%1$15.2f", Math.abs(genCommitment[i][j]));
                        
                        displayData[iStartIndex+j][4]=String.format("%1$15.2f", genOffer[j][2]);
                        displayData[iStartIndex+j][5]=String.format("%1$15.2f", genOffer[j][3]);
                       }
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iGenNumber*iDayNumber*24][8];
              
              double [][] genCommitment;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                 genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                 int iDayStartIndex=(iDay-(iStartTime-1))*iGenNumber*24;
                 
                  for(int i=0; i<24; i++) {
                      for(int j=0; j<iGenNumber; j++){
                          int iStartIndex=iDayStartIndex+i*iGenNumber;

                        displayData[iStartIndex+j][0]=genData[j][0];
                        displayData[iStartIndex+j][1]=iDay+2;
                        displayData[iStartIndex+j][2]=hoursName[i];
                          
                        displayData[iStartIndex+j][3]=String.format("%1$15.2f", Math.abs(genCommitment[i][j]));
                        
                        displayData[iStartIndex+j][4]=String.format("%1$15.2f", genOffer[j][2]);
                        displayData[iStartIndex+j][5]=String.format("%1$15.2f", genOffer[j][3]);
                       }
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                    int iDayNumber=genAgentCommitmentByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber][8];

                  double [][] genCommitment;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                     genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                     int iStartIndex=iDay*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=genData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                        
                          displayData[iStartIndex+i][3]=String.format("%1$15.2f", Math.abs(genCommitment[iDayHour][selectIndex[i]-1]));
                          
                          displayData[iStartIndex+i][4]=String.format("%1$15.2f", genOffer[selectIndex[i]-1][2]);
                          displayData[iStartIndex+i][5]=String.format("%1$15.2f", genOffer[selectIndex[i]-1][3]);
                     }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber][8];

                  double [][] genCommitment;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                     genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                     int iStartIndex=(iDay-(iStartTime-1))*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=genData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                        
                          displayData[iStartIndex+i][3]=String.format("%1$15.2f", Math.abs(genCommitment[iDayHour][selectIndex[i]-1]));
                          
                          displayData[iStartIndex+i][4]=String.format("%1$15.2f", genOffer[selectIndex[i]-1][2]);
                          displayData[iStartIndex+i][5]=String.format("%1$15.2f", genOffer[selectIndex[i]-1][3]);
                     }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber*24][8];

                  double [][] genCommitment;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                     genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                     int iDayStartIndex=(iDay-(iStartTime-1))*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                          for(int j=0; j<iDataNumber; j++){
                              int iStartIndex=iDayStartIndex+i*iDataNumber;

                            displayData[iStartIndex+j][0]=genData[selectIndex[j]-1][0];
                            displayData[iStartIndex+j][1]=iDay+2;
                            displayData[iStartIndex+j][2]=hoursName[i];
                        
                              displayData[iStartIndex+j][3]=String.format("%1$15.2f", Math.abs(genCommitment[i][selectIndex[j]-1]));
                              
                        
                            displayData[iStartIndex+j][4]=String.format("%1$15.2f", genOffer[selectIndex[j]-1][2]);
                            displayData[iStartIndex+j][5]=String.format("%1$15.2f", genOffer[selectIndex[j]-1][3]);
                            }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
            }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                    int iDayNumber=genAgentCommitmentByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber*24][8];

                  double [][] genCommitment;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                     genOffer=(double [][])genAgentSupplyOfferByDay.get(iDay);
                     int iDayStartIndex=iDay*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                          for(int j=0; j<iDataNumber; j++){
                              int iStartIndex=iDayStartIndex+i*iDataNumber;

                            displayData[iStartIndex+j][0]=genData[selectIndex[j]-1][0];
                            displayData[iStartIndex+j][1]=iDay+2;
                            displayData[iStartIndex+j][2]=hoursName[i];
                        
                            displayData[iStartIndex+j][3]=String.format("%1$15.2f", Math.abs(genCommitment[i][selectIndex[j]-1]));
                              
                            displayData[iStartIndex+j][4]=String.format("%1$15.2f", genOffer[selectIndex[j]-1][2]);
                            displayData[iStartIndex+j][5]=String.format("%1$15.2f", genOffer[selectIndex[j]-1][3]);
                            }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
         }
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
       column=table.getColumnModel().getColumn(4);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon caprlIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/caprl.gif"));
       column.setHeaderValue(caprlIcon);
        
       column=table.getColumnModel().getColumn(5);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon capruIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capru.gif"));
       column.setHeaderValue(capruIcon);

       for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
     
        table.repaint();
 }
  
  public String ChangeToCurrency(double origin){
      double dValue=(double)(Math.round(origin*100))/100;
      String sTemp=Double.toString(dValue);
      
      int index=sTemp.indexOf(".");
      int iLength=sTemp.length();
      String temp="";
      
      if(index<0){ // no period
          int iLoop;
          sTemp+=".00";
          iLength+=3;
          
          if(dValue>0)
              iLoop=(iLength-1)/3;
          else
              iLoop=(iLength-2)/3;
          
          for(int i=0; i<iLoop; i++){
              temp=sTemp.substring(iLength-(i+1)*3, iLength-i*3)+temp;
              temp=","+temp;
          }
               
          temp=sTemp.substring(0, iLength-iLoop*3)+temp;
      }
      else{ // has period
          if(iLength-index==2){
              sTemp+="0";
              iLength+=1;
          }
          
          temp=sTemp.substring(index, iLength);
          int iLoop;
          if(dValue>0)
              iLoop=(index-1)/3;
          else
              iLoop=(index-2)/3;
          
          for(int i=0; i<iLoop; i++){
              temp=sTemp.substring(index-(i+1)*3, index-i*3)+temp;
              temp=","+temp;
          }
               
          temp=sTemp.substring(0, index-iLoop*3)+temp;
     }
      
      return temp;
  }

   public void displayGeneratorProfitAndNetEarningsData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int [] selectIndex) {
      String [] names =  {
                 "GenCo Name", "Day Index", "Profit ($/D)", "Net Earnings ($/D)", "Revenues ($/D)"
           };
      
      int [] iColumnWidth={90, 90, 90, 120, 120};

      ArrayList genAgentProfitAndNetGainByDay=amesFrame.getAMESMarket().getGenAgentProfitAndNetGainByDay();
      
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
                int iDayNumber=genAgentProfitAndNetGainByDay.size();
                displayData=new Object [iGenNumber*iDayNumber][5];
              
              double [][] genProfit;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);
                 int iStartIndex=iDay*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;
                      displayData[iStartIndex+i][2]=ChangeToCurrency(genProfit[i][0]);
                      displayData[iStartIndex+i][3]=ChangeToCurrency(genProfit[i][1]);
                      displayData[iStartIndex+i][4]=ChangeToCurrency(genProfit[i][2]);
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iGenNumber*iDayNumber][5];
              
              double [][] genProfit;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iGenNumber;
                 
                  for(int i=0; i<iGenNumber; i++) {
                      displayData[iStartIndex+i][0]=genData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;
                      displayData[iStartIndex+i][2]=ChangeToCurrency(genProfit[i][0]);
                      displayData[iStartIndex+i][3]=ChangeToCurrency(genProfit[i][1]);
                      displayData[iStartIndex+i][4]=ChangeToCurrency(genProfit[i][2]);
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
                    int iDayNumber=genAgentProfitAndNetGainByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber][5];

                  double [][] genProfit;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);
                     int iStartIndex=iDay*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=genData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+1;
                          displayData[iStartIndex+i][2]=ChangeToCurrency(genProfit[selectIndex[i]-1][0]);
                          displayData[iStartIndex+i][3]=ChangeToCurrency(genProfit[selectIndex[i]-1][1]);
                          displayData[iStartIndex+i][4]=ChangeToCurrency(genProfit[selectIndex[i]-1][2]);
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber][5];

                  double [][] genProfit;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);
                     int iStartIndex=(iDay-(iStartTime-1))*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=genData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+1;
                          displayData[iStartIndex+i][2]=ChangeToCurrency(genProfit[selectIndex[i]-1][0]);
                          displayData[iStartIndex+i][3]=ChangeToCurrency(genProfit[selectIndex[i]-1][1]);
                          displayData[iStartIndex+i][4]=ChangeToCurrency(genProfit[selectIndex[i]-1][2]);
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
                column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
     
        table.repaint();
 }

   public void displayLSESurplusData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int [] selectIndex) {
      String [] names =  {
                 "LSE Name", "Day Index", "Net Earnings ($/D)"
           };
      
      int [] iColumnWidth={70, 90, 120};

      ArrayList lseAgentSurplusByDay=amesFrame.getAMESMarket().getLSEAgentSurplusByDay();
      
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lseHybridData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
                int iDayNumber=lseAgentSurplusByDay.size();
                displayData=new Object [iLSENumber*iDayNumber][3];
              
              double [][] lseSurplus;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);
                 int iStartIndex=iDay*iLSENumber;
                 
                  for(int i=0; i<iLSENumber; i++) {
                      displayData[iStartIndex+i][0]=lseHybridData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;
                      displayData[iStartIndex+i][2]=ChangeToCurrency(lseSurplus[i][0]);
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iLSENumber*iDayNumber][3];
              
              double [][] lseSurplus;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iLSENumber;
                 
                  for(int i=0; i<iLSENumber; i++) {
                      displayData[iStartIndex+i][0]=lseHybridData[i][0];
                      displayData[iStartIndex+i][1]=iDay+1;
                      displayData[iStartIndex+i][2]=ChangeToCurrency(lseSurplus[i][0]);
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
                    int iDayNumber=lseAgentSurplusByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber][4];

                  double [][] lseSurplus;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);
                     int iStartIndex=iDay*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=lseHybridData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+1;
                          displayData[iStartIndex+i][2]=ChangeToCurrency(lseSurplus[selectIndex[i]-1][0]);
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber][4];

                  double [][] lseSurplus;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);
                     int iStartIndex=(iDay-(iStartTime-1))*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=lseHybridData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+1;
                          displayData[iStartIndex+i][2]=ChangeToCurrency(lseSurplus[selectIndex[i]-1][0]);
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
                column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
     
        table.repaint();
 }


   public void displayLSEPriceSensitiveDemandData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "LSE Name", "Day Index",  "Hour",  "c ($/MWh)", "d ($/MW2h)", "SLMax (MW)", "Price-Sensitive Demand (MW)"
           };
      int [] iColumnWidth={90, 70, 90, 90, 90, 90, 200};
      
      Object [][][] lsePriceSensitiveData=amesFrame.getLSEPriceSensitiveDemandData();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lsePriceSensitiveData.length;
      
      iStartTime=iStartTime-1;
      iEndTime=iEndTime-1;
      
      ArrayList priceSensitiveByDay=amesFrame.getAMESMarket().getLSEAgenPriceSensitiveDemandByDay();
                 
      int[] hasSolutions;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
              int iDayNumber=priceSensitiveByDay.size();
              displayData=new Object [iLSENumber*iDayNumber][7];
              
              double [][] priceSensitive;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                 int iStartIndex=iDay*iLSENumber;
                 int psLoadIndex=0;
                 
                  for(int i=0; i<iLSENumber; i++) {
                      int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[i][iDayHour+3].toString());
                      
                      displayData[iStartIndex+i][0]=lsePriceSensitiveData[i][iDayHour][0];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                      displayData[iStartIndex+i][3]=String.format("%1$15.2f", lsePriceSensitiveData[i][iDayHour][4]);
                      displayData[iStartIndex+i][4]=String.format("%1$15.2f", lsePriceSensitiveData[i][iDayHour][5]);
                      displayData[iStartIndex+i][5]=String.format("%1$15.2f", lsePriceSensitiveData[i][iDayHour][6]);

                          if((hourlyLoadHybridFlagByLSE&2)==2)
                              displayData[iStartIndex+i][6]=String.format("%1$15.2f", Math.abs(priceSensitive[iDayHour][psLoadIndex++]));
                          else
                              displayData[iStartIndex+i][6]=String.format("%1$15.2f", 0.0);
                 }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iLSENumber*iDayNumber][7];
              
              double [][] priceSensitive;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iLSENumber;
                 int psLoadIndex=0;
                 
                  for(int i=0; i<iLSENumber; i++) {
                      int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[i][iDayHour+3].toString());
                      
                      displayData[iStartIndex+i][0]=lsePriceSensitiveData[i][iDayHour][0];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      displayData[iStartIndex+i][3]=String.format("%1$15.2f", lsePriceSensitiveData[i][iDayHour][4]);
                      displayData[iStartIndex+i][4]=String.format("%1$15.2f", lsePriceSensitiveData[i][iDayHour][5]);
                      displayData[iStartIndex+i][5]=String.format("%1$15.2f", lsePriceSensitiveData[i][iDayHour][6]);

                      if ((hourlyLoadHybridFlagByLSE & 2) == 2) {
                          displayData[iStartIndex + i][6] = String.format("%1$15.2f", Math.abs(priceSensitive[iDayHour][psLoadIndex++]));
                      } else {
                          displayData[iStartIndex + i][6] = String.format("%1$15.2f", 0.0);
                      }

                 }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                int iDayNumber=priceSensitiveByDay.size();
                displayData=new Object [iLSENumber*iDayNumber*24][7];
              
              double [][] priceSensitive;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                 int iDayStartIndex=iDay*iLSENumber*24;
                 
                  for(int i=0; i<24; i++) {
                      int psLoadIndex=0;
                      for(int j=0; j<iLSENumber; j++){
                          int iStartIndex=iDayStartIndex+i*iLSENumber;
                          int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[j][i+3].toString());
                      
                          displayData[iStartIndex+j][0]=lsePriceSensitiveData[j][i][0];
                          displayData[iStartIndex+j][1]=iDay+2;
                          displayData[iStartIndex+j][2]=hoursName[i];
                          displayData[iStartIndex+j][3]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][4]);
                          displayData[iStartIndex+j][4]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][5]);
                          displayData[iStartIndex+j][5]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][6]);

                          if ((hourlyLoadHybridFlagByLSE & 2) == 2) {
                              displayData[iStartIndex + j][6] = String.format("%1$15.2f", Math.abs(priceSensitive[i][psLoadIndex++]));
                          } else {
                              displayData[iStartIndex + j][6] = String.format("%1$15.2f", 0.0);
                          }

                       }
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iLSENumber*iDayNumber*24][7];
              
              double [][] priceSensitive;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                 int iDayStartIndex=(iDay-(iStartTime-1))*iLSENumber*24;
                 
                  for(int i=0; i<24; i++) {
                      int psLoadIndex=0;
                      for(int j=0; j<iLSENumber; j++){
                          int iStartIndex=iDayStartIndex+i*iLSENumber;
                          int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[j][i+3].toString());
                      
                          displayData[iStartIndex+j][0]=lsePriceSensitiveData[j][i][0];
                          displayData[iStartIndex+j][1]=iDay+2;
                          displayData[iStartIndex+j][2]=hoursName[i];
                          displayData[iStartIndex+j][3]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][4]);
                          displayData[iStartIndex+j][4]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][5]);
                          displayData[iStartIndex+j][5]=String.format("%1$15.2f", lsePriceSensitiveData[j][i][6]);

                              if((hourlyLoadHybridFlagByLSE&2)==2)
                                  displayData[iStartIndex+j][6]=String.format("%1$15.2f", Math.abs(priceSensitive[i][psLoadIndex++]));
                              else
                                  displayData[iStartIndex+j][6]=String.format("%1$15.2f", 0.0);
                        
                       }
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                  int iDayNumber=priceSensitiveByDay.size();
                  displayData=new Object [iDataNumber*iDayNumber][7];

                  double [][] priceSensitive;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                     int iStartIndex=iDay*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          int psLoadIndex=0;
                          // Get the psLoadIndex
                          for(int k=0; k<selectIndex[i]-1; k++){
                            int hourlyFlag=Integer.parseInt(lseHybridData[k][iDayHour+3].toString());
                            if((hourlyFlag&2)==2)
                                psLoadIndex++;
                          }
                          
                          int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[selectIndex[i]-1][iDayHour+3].toString());
                          displayData[iStartIndex+i][0]=lsePriceSensitiveData[selectIndex[i]-1][iDayHour][0];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                          displayData[iStartIndex+i][3]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[i]-1][iDayHour][4]);
                          displayData[iStartIndex+i][4]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[i]-1][iDayHour][5]);
                          displayData[iStartIndex+i][5]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[i]-1][iDayHour][6]);

                              if((hourlyLoadHybridFlagByLSE&2)==2)
                                  displayData[iStartIndex+i][6]=String.format("%1$15.2f", Math.abs(priceSensitive[iDayHour][psLoadIndex]));
                              else
                                  displayData[iStartIndex+i][6]=String.format("%1$15.2f", 0.0);
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber][7];

                  double [][] priceSensitive;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                     int iStartIndex=(iDay-(iStartTime-1))*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          int psLoadIndex=0;
                          // Get the psLoadIndex
                          for(int k=0; k<selectIndex[i]-1; k++){
                            int hourlyFlag=Integer.parseInt(lseHybridData[k][iDayHour+3].toString());
                            if((hourlyFlag&2)==2)
                                psLoadIndex++;
                          }
                          int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[selectIndex[i]-1][iDayHour+3].toString());
                          displayData[iStartIndex+i][0]=lsePriceSensitiveData[selectIndex[i]-1][iDayHour][0];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                          displayData[iStartIndex+i][3]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[i]-1][iDayHour][4]);
                          displayData[iStartIndex+i][4]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[i]-1][iDayHour][5]);
                          displayData[iStartIndex+i][5]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[i]-1][iDayHour][6]);

                              if((hourlyLoadHybridFlagByLSE&2)==2)
                                  displayData[iStartIndex+i][6]=String.format("%1$15.2f", Math.abs(priceSensitive[iDayHour][psLoadIndex]));
                              else
                                  displayData[iStartIndex+i][6]=String.format("%1$15.2f", 0.0);
                    }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                    int iDayNumber=priceSensitiveByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber*24][7];

                  double [][] priceSensitive;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                     int iDayStartIndex=iDay*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                           for(int j=0; j<iDataNumber; j++){
                              int psLoadIndex=0;
                              // Get the psLoadIndex
                              for(int k=0; k<selectIndex[j]-1; k++){
                                int hourlyFlag=Integer.parseInt(lseHybridData[k][i+3].toString());
                                if((hourlyFlag&2)==2)
                                    psLoadIndex++;
                              }
                              
                              int iStartIndex=iDayStartIndex+i*iDataNumber;
                              int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[selectIndex[j]-1][i+3].toString());
                              
                              displayData[iStartIndex+j][0]=lsePriceSensitiveData[selectIndex[j]-1][i][0];
                              displayData[iStartIndex+j][1]=iDay+2;
                              displayData[iStartIndex+j][2]=hoursName[i];
                              displayData[iStartIndex+j][3]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][4]);
                              displayData[iStartIndex+j][4]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][5]);
                              displayData[iStartIndex+j][5]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][6]);

                                  if((hourlyLoadHybridFlagByLSE&2)==2)
                                      displayData[iStartIndex+j][6]=String.format("%1$15.2f", Math.abs(priceSensitive[i][psLoadIndex]));
                                  else
                                      displayData[iStartIndex+j][6]=String.format("%1$15.2f", 0.0);
                          }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
         }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber*24][7];

                  double [][] priceSensitive;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     priceSensitive=(double [][])priceSensitiveByDay.get(iDay);
                     int iDayStartIndex=(iDay-(iStartTime-1))*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                          for(int j=0; j<iDataNumber; j++){
                              int psLoadIndex=0;
                              // Get the psLoadIndex
                              for(int k=0; k<selectIndex[j]-1; k++){
                                int hourlyFlag=Integer.parseInt(lseHybridData[k][i+3].toString());
                                if((hourlyFlag&2)==2)
                                    psLoadIndex++;
                              }
                              int iStartIndex=iDayStartIndex+i*iDataNumber;
                              int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[selectIndex[j]-1][i+3].toString());

                              displayData[iStartIndex+j][0]=lsePriceSensitiveData[selectIndex[j]-1][i][0];
                              displayData[iStartIndex+j][1]=iDay+2;
                              displayData[iStartIndex+j][2]=hoursName[i];
                              displayData[iStartIndex+j][3]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][4]);
                              displayData[iStartIndex+j][4]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][5]);
                              displayData[iStartIndex+j][5]=String.format("%1$15.2f", lsePriceSensitiveData[selectIndex[j]-1][i][6]);

                                  if((hourlyLoadHybridFlagByLSE&2)==2)
                                      displayData[iStartIndex+j][6]=String.format("%1$15.2f", Math.abs(priceSensitive[i][psLoadIndex++]));
                                  else
                                      displayData[iStartIndex+j][6]=String.format("%1$15.2f", 0.0);
                           }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
         }
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
       column=table.getColumnModel().getColumn(4);
       column.setHeaderRenderer(iconHeaderRender);
       ImageIcon constdIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constd.gif"));
       column.setHeaderValue(constdIcon);

       for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
     
        table.repaint();
 }

   public void displayBranchPowerFlowData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "Branch Name", "Day Index",  "Hour",  "Power (MW)", "MaxCap (MW)"
           };
      int [] iColumnWidth={90, 70, 90, 90, 90};
      
      iStartTime=iStartTime-1;
      iEndTime=iEndTime-1;
      
      ArrayList branchFlowByDay=amesFrame.getAMESMarket().getBranchFlowByDay();
                 
      int[] hasSolutions;
      
      Object [][] branchData=amesFrame.getBranchData( );
      int iBranchNumber=branchData.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
              int iDayNumber=branchFlowByDay.size();
              displayData=new Object [iBranchNumber*iDayNumber][5];
              
              double [][] branchFlow;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 branchFlow=(double [][])branchFlowByDay.get(iDay);
                 int iStartIndex=iDay*iBranchNumber;
                 
                  for(int i=0; i<iBranchNumber; i++) {
                      displayData[iStartIndex+i][0]=branchData[i][0];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                        displayData[iStartIndex+i][3]=String.format("%1$15.2f", branchFlow[iDayHour][i]);
                      
                      displayData[iStartIndex+i][4]=String.format("%1$15.2f", Double.parseDouble(branchData[i][3].toString()));
                 }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iBranchNumber*iDayNumber][5];
              
              double [][] branchFlow;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 branchFlow=(double [][])branchFlowByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iBranchNumber;
                 
                  for(int i=0; i<iBranchNumber; i++) {
                      displayData[iStartIndex+i][0]=branchData[i][0];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                        displayData[iStartIndex+i][3]=String.format("%1$15.2f", branchFlow[iDayHour][i]);
                      
                      displayData[iStartIndex+i][4]=String.format("%1$15.2f", Double.parseDouble(branchData[i][3].toString()));
                 }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                int iDayNumber=branchFlowByDay.size();
                displayData=new Object [iBranchNumber*iDayNumber*24][5];
              
              double [][] branchFlow;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 branchFlow=(double [][])branchFlowByDay.get(iDay);
                 int iDayStartIndex=iDay*iBranchNumber*24;
                 
                  for(int i=0; i<24; i++) {
                      for(int j=0; j<iBranchNumber; j++){
                          int iStartIndex=iDayStartIndex+i*iBranchNumber;

                        displayData[iStartIndex+j][0]=branchData[j][0];
                        displayData[iStartIndex+j][1]=iDay+2;
                        displayData[iStartIndex+j][2]=hoursName[i];
                          
                            displayData[iStartIndex+j][3]=String.format("%1$15.2f", branchFlow[i][j]);

                        displayData[iStartIndex+j][4]=String.format("%1$15.2f", Double.parseDouble(branchData[j][3].toString()));
                       }
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iBranchNumber*iDayNumber*24][5];
              
              double [][] branchFlow;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 branchFlow=(double [][])branchFlowByDay.get(iDay);
                 int iDayStartIndex=(iDay-(iStartTime-1))*iBranchNumber*24;
                 
                  for(int i=0; i<24; i++) {
                      for(int j=0; j<iBranchNumber; j++){
                          int iStartIndex=iDayStartIndex+i*iBranchNumber;

                        displayData[iStartIndex+j][0]=branchData[j][0];
                        displayData[iStartIndex+j][1]=iDay+2;
                        displayData[iStartIndex+j][2]=hoursName[i];
                          
                            displayData[iStartIndex+j][3]=String.format("%1$15.2f", branchFlow[i][j]);

                        displayData[iStartIndex+j][4]=String.format("%1$15.2f", Double.parseDouble(branchData[j][3].toString()));
                       }
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                  int iDayNumber=branchFlowByDay.size();
                  displayData=new Object [iDataNumber*iDayNumber][5];

                  double [][] branchFlow;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     int iStartIndex=iDay*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=branchData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                            displayData[iStartIndex+i][3]=String.format("%1$15.2f", branchFlow[iDayHour][selectIndex[i]-1]);

                          displayData[iStartIndex+i][4]=String.format("%1$15.2f", Double.parseDouble(branchData[selectIndex[i]-1][3].toString()));
                     }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber][5];

                  double [][] branchFlow;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     int iStartIndex=(iDay-(iStartTime-1))*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=branchData[selectIndex[i]-1][0];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                            displayData[iStartIndex+i][3]=String.format("%1$15.2f", branchFlow[iDayHour][selectIndex[i]-1]);

                          displayData[iStartIndex+i][4]=String.format("%1$15.2f", Double.parseDouble(branchData[selectIndex[i]-1][3].toString()));
                     }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                    int iDayNumber=branchFlowByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber*24][5];

                  double [][] branchFlow;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     int iDayStartIndex=iDay*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                          for(int j=0; j<iDataNumber; j++){
                              int iStartIndex=iDayStartIndex+i*iDataNumber;

                            displayData[iStartIndex+j][0]=branchData[selectIndex[j]-1][0];
                            displayData[iStartIndex+j][1]=iDay+2;
                            displayData[iStartIndex+j][2]=hoursName[i];
                            
                                displayData[iStartIndex+j][3]=String.format("%1$15.2f", branchFlow[i][selectIndex[j]-1]);
                            
                            displayData[iStartIndex+j][4]=String.format("%1$15.2f", Double.parseDouble(branchData[selectIndex[j]-1][3].toString()));
                           }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
         }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber*24][5];

                  double [][] branchFlow;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     int iDayStartIndex=(iDay-(iStartTime-1))*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                          for(int j=0; j<iDataNumber; j++){
                              int iStartIndex=iDayStartIndex+i*iDataNumber;

                            displayData[iStartIndex+j][0]=branchData[selectIndex[j]-1][0];
                            displayData[iStartIndex+j][1]=iDay+2;
                            displayData[iStartIndex+j][2]=hoursName[i];
                            
                                displayData[iStartIndex+j][3]=String.format("%1$15.2f", branchFlow[i][selectIndex[j]-1]);
                            displayData[iStartIndex+j][4]=String.format("%1$15.2f", Double.parseDouble(branchData[selectIndex[j]-1][3].toString()));
                           }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
         }
      }
      
       TableColumn column = null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
     
        table.repaint();
 }

   public void displayNodeLMPData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "Bus Name", "Day Index", "   Hour",  "LMP ($/MWh)"
           };
       int [] iColumnWidth={70, 60, 90, 90};
     
      iStartTime=iStartTime-1;
      iEndTime=iEndTime-1;
      
      ArrayList LMPByDay=amesFrame.getAMESMarket().getLMPByDay();
                 
      int[] hasSolutions;
      
      String [] nodeName=amesFrame.getNodeNameData( );
      int iNodeNumber=nodeName.length;
      
      Object [][] displayData;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
              int iDayNumber=LMPByDay.size();
              displayData=new Object [iNodeNumber*iDayNumber][4];
              
              double [][] lmp;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 lmp=(double [][])LMPByDay.get(iDay);
                 int iStartIndex=iDay*iNodeNumber;
                 
                  for(int i=0; i<iNodeNumber; i++) {
                      displayData[iStartIndex+i][0]=nodeName[i];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                          displayData[iStartIndex+i][3]=ChangeToCurrency(lmp[iDayHour][i]);
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iNodeNumber*iDayNumber][4];
              
              double [][] lmp;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 lmp=(double [][])LMPByDay.get(iDay);
                 int iStartIndex=(iDay-(iStartTime-1))*iNodeNumber;
                 
                  for(int i=0; i<iNodeNumber; i++) {
                      displayData[iStartIndex+i][0]=nodeName[i];
                      displayData[iStartIndex+i][1]=iDay+2;
                      displayData[iStartIndex+i][2]=hoursName[iDayHour];
                      
                          displayData[iStartIndex+i][3]=ChangeToCurrency(lmp[iDayHour][i]);
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                int iDayNumber=iEndTime-iStartTime+1;
                displayData=new Object [iNodeNumber*iDayNumber*24][4];
              
              double [][] lmp;
              
              for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                 lmp=(double [][])LMPByDay.get(iDay);
                 int iDayStartIndex=(iDay-(iStartTime-1))*iNodeNumber*24;
                 
                  for(int i=0; i<24; i++) {
                      for(int j=0; j<iNodeNumber; j++){
                          int iStartIndex=iDayStartIndex+i*iNodeNumber;

                        displayData[iStartIndex+j][0]=nodeName[j];
                        displayData[iStartIndex+j][1]=iDay+2;
                        displayData[iStartIndex+j][2]=hoursName[i];
                        
                            displayData[iStartIndex+j][3]=ChangeToCurrency(lmp[i][j]);
                       }
                  }
              }
           
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                int iDayNumber=LMPByDay.size();
                displayData=new Object [iNodeNumber*iDayNumber*24][4];
              
              double [][] lmp;
              
              for(int iDay=0; iDay<iDayNumber; iDay++) {
                 lmp=(double [][])LMPByDay.get(iDay);
                 int iDayStartIndex=iDay*iNodeNumber*24;
                 
                  for(int i=0; i<24; i++) {
                      for(int j=0; j<iNodeNumber; j++){
                          int iStartIndex=iDayStartIndex+i*iNodeNumber;

                        displayData[iStartIndex+j][0]=nodeName[j];
                        displayData[iStartIndex+j][1]=iDay+2;
                        displayData[iStartIndex+j][2]=hoursName[i];
                        
                            displayData[iStartIndex+j][3]=ChangeToCurrency(lmp[i][j]);
                       }
                  }
              }
              
            DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
            table.setModel(loadDataModel);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                    int iDayNumber=LMPByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber][4];

                  double [][] lmp;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     lmp=(double [][])LMPByDay.get(iDay);
                     int iStartIndex=iDay*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=nodeName[selectIndex[i]-1];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                          
                              displayData[iStartIndex+i][3]=ChangeToCurrency(lmp[iDayHour][selectIndex[i]-1]);
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber][4];

                  double [][] lmp;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     lmp=(double [][])LMPByDay.get(iDay);
                     int iStartIndex=(iDay-(iStartTime-1))*iDataNumber;

                      for(int i=0; i<iDataNumber; i++) {
                          displayData[iStartIndex+i][0]=nodeName[selectIndex[i]-1];
                          displayData[iStartIndex+i][1]=iDay+2;
                          displayData[iStartIndex+i][2]=hoursName[iDayHour];
                          
                              displayData[iStartIndex+i][3]=ChangeToCurrency(lmp[iDayHour][selectIndex[i]-1]);
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                    int iDayNumber=LMPByDay.size();
                    displayData=new Object [iDataNumber*iDayNumber*24][4];

                  double [][] lmp;

                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     lmp=(double [][])LMPByDay.get(iDay);
                     int iDayStartIndex=iDay*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                          for(int j=0; j<iDataNumber; j++){
                              int iStartIndex=iDayStartIndex+i*iDataNumber;

                            displayData[iStartIndex+j][0]=nodeName[selectIndex[j]-1];
                            displayData[iStartIndex+j][1]=iDay+2;
                            displayData[iStartIndex+j][2]=hoursName[i];
                            
                                displayData[iStartIndex+j][3]=ChangeToCurrency(lmp[i][selectIndex[j]-1]);
                           }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
         }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                    int iDayNumber=iEndTime-iStartTime+1;
                    displayData=new Object [iDataNumber*iDayNumber*24][4];

                  double [][] lmp;

                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                      lmp=(double [][])LMPByDay.get(iDay);
                      int iDayStartIndex=(iDay-(iStartTime-1))*iDataNumber*24;

                      for(int i=0; i<24; i++) {
                          for(int j=0; j<iDataNumber; j++){
                              int iStartIndex=iDayStartIndex+i*iDataNumber;

                            displayData[iStartIndex+j][0]=nodeName[selectIndex[j]-1];
                            displayData[iStartIndex+j][1]=iDay+2;
                            displayData[iStartIndex+j][2]=hoursName[i];
                            
                                displayData[iStartIndex+j][3]=ChangeToCurrency(lmp[i][selectIndex[j]-1]);
                           }
                      }
                 }

                DefaultTableModel loadDataModel = new DefaultTableModel(displayData,  names);
                table.setModel(loadDataModel);
         }
      }
      
       TableColumn column=null;
       table.setAutoscrolls(true);
       table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
        for (int i = 0; i < names.length; i++) {
            column =table.getColumnModel().getColumn(i);
            
            column.setPreferredWidth(iColumnWidth[i]);
        }

        DefaultTableCellRenderer render=new DefaultTableCellRenderer();   
        render.setHorizontalAlignment(JLabel.RIGHT);   
        table.setDefaultRenderer(Object.class, render);   
     
        table.repaint();
 }
   
      private String [] hoursName={
                 "0:00", "1:00", "2:00", "3:00", "4:00", "5:00",
                 "6:00", "7:00", "8:00", "9:00", "10:00", "11:00",
                 "12:00", "13:00", "14:00", "15:00", "16:00", "17:00",
                 "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"
           };
      private TableCellRenderer iconHeaderRender;
      private AMESFrame amesFrame;
      private JTable table;

}
