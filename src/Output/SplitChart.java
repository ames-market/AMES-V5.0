/* 
 * JFreeChart is a free chart library for the Java(tm) platform
 * JFreeChart Project Info:  http://www.jfree.org/jfreechart/index.html
 */

/*
 * SplitChart.java
 *
 * Created on 2007 3 12 ,  10:52
 *
 * to change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package Output;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.text.DecimalFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.labels.CustomXYToolTipGenerator;

import AMESGUIFrame.*;

public class SplitChart  extends JFrame    {
    
    /** Creates a new instance of SplitChart */
    public SplitChart() {
        super("Output Chart View");
     }
    
    public  void createAndShowGUI() {
        //Create and set up the window.
        chartPanel = new ChartPanel(chart);
        int [] indexNull=new int[0];
        drawGeneratorCommitmentWithTrueCostData("", 0, 0, 0, indexNull);
 
        SelectPanel selectPanel = new SelectPanel(amesFrame,false,null,this);
        
        //Provide minimum sizes for the two components in the split pane
        selectPanel.setMinimumSize(new Dimension(100, 50));
        chartPanel.setMinimumSize(new Dimension(100, 30));
        
        JSplitPane splitPane = new JSplitPane();
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(selectPanel);
        splitPane.setRightComponent(chartPanel);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(430);

        //Add the split pane to this frame
        getContentPane().add(splitPane);

        //Display the window.
        pack();
        setVisible(true);
    }

public void drawGridLinesData(int [] selectIndex) {
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();
      Object [][] branchData=amesFrame.getBranchData( );
      
      int iBranchNumber=branchData.length;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          for(int i=0; i<iBranchNumber; i++) {
              String branchName=(String)branchData[i][0];

              dataset3D.addValue((double)(Math.round(Double.parseDouble(branchData[i][3].toString())*1000))/1000.0, "Capacities (MWs)", branchName);   
           }
      }
      else {
          int iDataNumber=selectIndex.length;
          
          for(int i=0; i<iDataNumber; i++) {
              String branchName=(String)branchData[selectIndex[i]-1][0];

              dataset3D.addValue((double)(Math.round(Double.parseDouble(branchData[selectIndex[i]-1][3].toString())*1000))/1000.0, "Capacities (MWs)", branchName);   
          }
      }
              

        chart=ChartFactory.createBarChart3D(
           "Branch Max Capacities",      // chart title
            "",               // domain axis label
            "Capacities (MWs)",                  // range axis label
            dataset3D,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        final CategoryPlot plot=chart.getCategoryPlot();
        final CategoryAxis axis=plot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/8.0));

        final CategoryItemRenderer renderer=plot.getRenderer();
        renderer.setItemLabelsVisible(true);
        final BarRenderer r=(BarRenderer) renderer;
        r.setMaximumBarWidth(0.05);

        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }
   
public void drawGeneratorData(int [] selectIndex) {
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();
      String xLabel="Power (MWs)";
      dataset=new XYSeriesCollection();
      boolean bDraw3D=true;

      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          for(int i=0; i<iGenNumber; i++) {
              String genName=(String)genData[i][0];

              dataset3D.addValue((double)(Math.round(Double.parseDouble(genData[i][6].toString())*1000))/1000.0, "capL (MW)", genName);   
              dataset3D.addValue((double)(Math.round(Double.parseDouble(genData[i][7].toString())*1000))/1000.0, "capU (MW)", genName);   
           }
      }
      else {
          int iDataNumber=selectIndex.length;
          
          if(iDataNumber>1){
              for(int i=0; i<iDataNumber; i++) {
                  String genName=(String)genData[selectIndex[i]-1][0];

                  dataset3D.addValue((double)(Math.round(Double.parseDouble(genData[selectIndex[i]-1][6].toString())*1000))/1000.0, "capL (MW)", genName);   
                  dataset3D.addValue((double)(Math.round(Double.parseDouble(genData[selectIndex[i]-1][7].toString())*1000))/1000.0, "capU (MW)", genName);   
              }
          }
          else{// only select one GenCo
              XYSeries series = new XYSeries("True");
              int genIndex=selectIndex[0]-1;
              chartTitle=(String)genData[genIndex][0]+"'s True Marginal Cost Function";

              double da=Double.parseDouble(genData[genIndex][4].toString());
              double db=Double.parseDouble(genData[genIndex][5].toString());
              double dMinCap=Double.parseDouble(genData[genIndex][6].toString());
              double dMaxCap=Double.parseDouble(genData[genIndex][7].toString());

              double dStart=da+2.0*db*dMinCap;
              double dEnd=da+2.0*db*dMaxCap;
              series.add(dMinCap, dStart);
              series.add(dMaxCap, dEnd);
      
              dataset.addSeries(series);
              
              bDraw3D=false;
          }
      }
     
      if(bDraw3D){
        chart=ChartFactory.createBarChart3D(
           "GenCo Lower and Upper Operating Limits",      // chart title
            "",               // domain axis label
            "Capacities (MWs)",                  // range axis label
            dataset3D,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        final CategoryPlot plot=chart.getCategoryPlot();
        final CategoryAxis axis=plot.getDomainAxis();
        axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/8.0));

        final CategoryItemRenderer renderer=plot.getRenderer();
        renderer.setItemLabelsVisible(true);
        final BarRenderer r=(BarRenderer) renderer;
        r.setMaximumBarWidth(0.05);
      }
      else{
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            xLabel,                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

         chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);     
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
      }
      
       chart.getTitle().setFont(font);
       chartPanel.setChart(chart);
 }
      
public void drawLSEFixedDemandData(int [] selectIndex) {
     chartTitle="24 Hour LSE Fixed Demand (Load Profile)";
     Object [][] lseData=amesFrame.getLSEData( );

     dataset=new XYSeriesCollection();
     DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();

      if((selectIndex.length<1)||(selectIndex[0]==0)) {
            for(int i=0; i<lseData.length; i++) {
                XYSeries series = new XYSeries((String)lseData[i][0]);
                
                for(int j=0; j<24; j++)
                    series.add(j, Double.parseDouble(lseData[i][j+3].toString()));
                
                dataset.addSeries(series);
            }
      }
      else {
          int iDataNumber=selectIndex.length;
          
          for(int i=0; i<iDataNumber; i++) {
               XYSeries series = new XYSeries((String)lseData[selectIndex[i]-1][0]);
          
              for(int j=0; j<24; j++) {
                    if(iDataNumber==1) {
                        String temp=" "+j;
                        dataset3D.addValue(Double.parseDouble(lseData[selectIndex[i]-1][j+3].toString()), lseData[selectIndex[i]-1][0].toString(), temp); 
                    }
                    else
                        series.add(j, Double.parseDouble(lseData[selectIndex[i]-1][j+3].toString()));
                        
              }
                
               if(iDataNumber!=1)
                   dataset.addSeries(series);
          }
      }
     
      if((selectIndex.length==1)&&(selectIndex[0]!=0)) {
           chartTitle=(String)(lseData[selectIndex[0]-1][0]);
           chartTitle=chartTitle+" 24 Hour Fixed Demand (Load Profile)";

            chart = ChartFactory.createBarChart3D(
               chartTitle,      // chart title
                "Hour",               // domain axis label
                "Power (MWs)",                  // range axis label
                dataset3D,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
            );

            final CategoryPlot plot=chart.getCategoryPlot();
            final CategoryAxis axis=plot.getDomainAxis();
            axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/8.0));

            final CategoryItemRenderer renderer=plot.getRenderer();
            renderer.setItemLabelsVisible(true);
            final BarRenderer r=(BarRenderer) renderer;
            r.setMaximumBarWidth(0.05);
     }
     else {
            // create the chart...
            chart=ChartFactory.createXYLineChart(
                chartTitle,      // chart title
                "Hour",                      // x axis label
                "Power (MWs)",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
            );

            chart.setBackgroundPaint(Color.white);

            // get a reference to the plot for further customisation...
            final XYPlot plot=chart.getXYPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setDomainGridlinePaint(Color.blue);
            plot.setRangeGridlinePaint(Color.blue);

            final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
            XYToolTipGenerator generator=new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
            renderer.setToolTipGenerator(generator);       
            plot.setRenderer(renderer);
        
            NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
            xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            xAxis.setRange(0, 23.5);
        
     }
         
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }
  
public void drawGeneratorCommitmentWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names={
                 "GenCo Name",  "Hour", "Power  (MW)"
           };
      chartTitle="GenCo Commitments (Benchmark)";
      
      ArrayList genAgentCommitmentWithTrueCost=amesFrame.getAMESMarket().getGenAgentDispatchWithTrueCost();
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();

      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
             
          double [][] genCommitmentWithTrueCost=(double [][])genAgentCommitmentWithTrueCost.get(0);
          for(int j=0; j<iGenNumber; j++){
              XYSeries series = new XYSeries((String)genData[j][0]);

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(genCommitmentWithTrueCost[i][j]*1000))/1000.0);
              }

              dataset.addSeries(series);
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          double [][] genCommitmentWithTrueCost=(double [][])genAgentCommitmentWithTrueCost.get(0);
          for(int j=0; j<iDataNumber; j++){
              XYSeries series = new XYSeries((String)genData[selectIndex[j]-1][0]);

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(genCommitmentWithTrueCost[i][selectIndex[j]-1]*1000))/1000.0);
              }

              dataset.addSeries(series);
          }
      }
       
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            "Hour",                      // x axis label
            "Power (MWs)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setRange(0, 23.5);
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }
     
public void drawGeneratorProfitWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex, boolean bProfit) {
    if(bProfit)  
        chartTitle="GenCo Profits (Benchmark)";
    else
        chartTitle="GenCo Net Earnings (Benchmark)";
     
      ArrayList genAgentProfitWithTrueCost=amesFrame.getAMESMarket().getGenAgentProfitAndNetGainWithTrueCost();
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();

      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
             
          double [][] genProfitWithTrueCost=(double [][])genAgentProfitWithTrueCost.get(0);
          for(int j=0; j<iGenNumber; j++){
              XYSeries series;
/*              
              = new XYSeries((String)genData[j][0]+" Hourly Profits");

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(genProfitWithTrueCost[j][i+3]*1000))/1000.0);
              }

              dataset.addSeries(series);
              
              series = new XYSeries((String)genData[j][0]+" Daily Profits");

              double sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=genProfitWithTrueCost[j][i+3];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
  */          
              series = new XYSeries((String)genData[j][0]);

              for(int i=0; i<24; i++) {
                  if(bProfit)
                    series.add(i, (double)(Math.round(genProfitWithTrueCost[j][i+3]*1000))/1000.0);
                  else
                    series.add(i, (double)(Math.round(genProfitWithTrueCost[j][i+27]*1000))/1000.0);
              }

              dataset.addSeries(series);
/*              
              series = new XYSeries((String)genData[j][0]+" Daily Net Earnings");

              double sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=genProfitWithTrueCost[j][i+27];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
              
              series = new XYSeries((String)genData[j][0]+" Hourly Revenue");

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(genProfitWithTrueCost[j][i+51]*1000))/1000.0);
              }

              dataset.addSeries(series);
              
              series = new XYSeries((String)genData[j][0]+" Daily Revenue");

              sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=genProfitWithTrueCost[j][i+51];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
*/          }
      }
      else {
          int iDataNumber=selectIndex.length;
            
           double [][] genProfitWithTrueCost=(double [][])genAgentProfitWithTrueCost.get(0);
          for(int j=0; j<iDataNumber; j++){
              XYSeries series;
/*              
              = new XYSeries((String)genData[selectIndex[j]-1][0]+" Houely Profits");

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(genProfitWithTrueCost[selectIndex[j]-1][i+3]*1000))/1000.0);
              }

              dataset.addSeries(series);
              
              series = new XYSeries((String)genData[selectIndex[j]-1][0]+" Daily Profits");

              double sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=genProfitWithTrueCost[selectIndex[j]-1][i+3];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
*/            series = new XYSeries((String)genData[selectIndex[j]-1][0]);
              
              for(int i=0; i<24; i++) {
                  if(bProfit)
                    series.add(i, (double)(Math.round(genProfitWithTrueCost[selectIndex[j]-1][i+3]*1000))/1000.0);
                  else
                    series.add(i, (double)(Math.round(genProfitWithTrueCost[selectIndex[j]-1][i+27]*1000))/1000.0);
              }

              dataset.addSeries(series);
/*              
              series = new XYSeries((String)genData[selectIndex[j]-1][0]+" Daily Net Earnings");

              sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=genProfitWithTrueCost[selectIndex[j]-1][i+27];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
              
              series = new XYSeries((String)genData[selectIndex[j]-1][0]+" Hourly Revenue");

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(genProfitWithTrueCost[selectIndex[j]-1][i+51]*1000))/1000.0);
              }

              dataset.addSeries(series);
              
              series = new XYSeries((String)genData[selectIndex[j]-1][0]+" Daily Revenue");

              sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=genProfitWithTrueCost[selectIndex[j]-1][i+51];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
*/          }
      }
       
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            "Hour",                      // x axis label
            "Money ($/H)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setRange(0, 23.5);
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }
     
public void drawPSDemandDispatchWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "LSE Name", "Hour",  "c ($/MWh)", "d ($/MW2h)", "SLMax (MW)", "Price-Sensitive Demand (MW)"
           };
      
      Object [][][] lsePriceSensitiveData=amesFrame.getLSEPriceSensitiveDemandData();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lsePriceSensitiveData.length;
      
      ArrayList LMPWithTrueCost=amesFrame.getAMESMarket().getLMPWithTrueCost();
      double [][] lmp=(double [][])LMPWithTrueCost.get(0);
      
      ArrayList priceSensitiveWithTrueCost=amesFrame.getAMESMarket().getLSEAgentPriceSensitiveDemandWithTrueCost();
      
      chartTitle="LSE Price-Sensitive Demand Function \nand Cleared Point (Benchmark)";
      String xLabel="Power (MWs)";
      
      dataset=new XYSeriesCollection();
 
      if(selectIndex.length<1){
          JOptionPane.showMessageDialog(this, "No LSE is selected!", "Error Message", JOptionPane.ERROR_MESSAGE);
          return;
      }
      
      double [][] lsePS=(double [][])priceSensitiveWithTrueCost.get(0);

      int lseIndex=selectIndex[0];
      int lseAtBus=Integer.parseInt(lseHybridData[lseIndex][2].toString());
      double busLMP=lmp[iDayHour][lseAtBus-1];

      chartTitle=chartTitle+"\n "+lsePriceSensitiveData[lseIndex][0][0]+" Hour "+iDayHour;
      XYSeries series = new XYSeries("Cleared Point");
      
      int psLoadIndex=0;
      // Get the psLoadIndex
      for(int k=0; k<selectIndex[0]; k++){
        int hourlyFlag=Integer.parseInt(lseHybridData[k][iDayHour+3].toString());
        if((hourlyFlag&2)==2)
            psLoadIndex++;
      }
   
      double dStart=0;
      double dEnd=0;
      int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[selectIndex[0]][iDayHour+3].toString());
      if((hourlyLoadHybridFlagByLSE&2)==2)
          dStart=lsePS[iDayHour][psLoadIndex];
      
      double c=Double.parseDouble(lsePriceSensitiveData[lseIndex][iDayHour][4].toString());
      double d=Double.parseDouble(lsePriceSensitiveData[lseIndex][iDayHour][5].toString());
      double slMax=Double.parseDouble(lsePriceSensitiveData[lseIndex][iDayHour][6].toString());
      
      dEnd=c-2.0*d*dStart;
  
      series.add(dStart, busLMP);

      dataset.addSeries(series);

      series = new XYSeries("Price-Sensitive Demand Function");

      dEnd=c-2.0*d*slMax;
      series.add(0, c);
      series.add(slMax, dEnd);

      dataset.addSeries(series);
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            xLabel,                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

         chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }
     
public void drawLSESurplusWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      chartTitle="LSE Net Earnings (Benchmark)";
      
      ArrayList LSEAgentSurplusWithTrueCost=amesFrame.getAMESMarket().getLSEAgentSurplusWithTrueCost();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lseHybridData.length;
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();

      if((selectIndex.length<1)||(selectIndex[0]==0)) {
             
         double [][] surplus=(double [][])LSEAgentSurplusWithTrueCost.get(0);
         for(int j=0; j<iLSENumber; j++){
              XYSeries series = new XYSeries((String)lseHybridData[j][0]);

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(surplus[j][i+1]*1000))/1000.0);
              }

              dataset.addSeries(series);

 /*             series = new XYSeries((String)lseHybridData[j][0]+" Total");

              double sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=surplus[j][i+1];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
   */       }
      }
      else {
          int iDataNumber=selectIndex.length;
            
         double [][] surplus=(double [][])LSEAgentSurplusWithTrueCost.get(0);
          for(int j=0; j<iDataNumber; j++){
              XYSeries series = new XYSeries((String)lseHybridData[selectIndex[j]-1][0]);

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(surplus[selectIndex[j]-1][i+1]*1000))/1000.0);
              }

              dataset.addSeries(series);
/*              
              series = new XYSeries((String)lseHybridData[selectIndex[j]-1][0]+" Total");

              double sum=0.0;
              for(int i=0; i<24; i++) {
                  sum+=surplus[selectIndex[j]-1][i+1];
                series.add(i, (double)(Math.round(sum*1000))/1000.0);
              }

              dataset.addSeries(series);
  */        }
      }
       
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            "Hour",                      // x axis label
            "Net Earnings ($)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setRange(0, 23.5);
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }
     
     public void drawNodeLMPWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "Bus Name",  "   Hour", "    LMP"
           };
      chartTitle="Locational Marginal Prices (Benchmark)";
      
      ArrayList LMPWithTrueCost=amesFrame.getAMESMarket().getLMPWithTrueCost();
      
      String [] nodeName=amesFrame.getNodeNameData( );
      int iNodeNumber=nodeName.length;
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
               
          double [][] lmp=(double [][])LMPWithTrueCost.get(0);
          for(int j=0; j<iNodeNumber; j++){
              XYSeries series = new XYSeries(nodeName[j]);

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(lmp[i][j]*1000))/1000.0);
              }

              dataset.addSeries(series);
          }
              
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          double [][] lmp=(double [][])LMPWithTrueCost.get(0);
            for(int j=0; j<iDataNumber; j++){
              XYSeries series = new XYSeries(nodeName[selectIndex[j]-1]);

              for(int i=0; i<24; i++) {
                series.add(i, (double)(Math.round(lmp[i][selectIndex[j]-1]*1000))/1000.0);
              }

              dataset.addSeries(series);
            }
      }
      
          // create the chart...
        chart=ChartFactory.createXYLineChart(
           chartTitle,      // chart title
            "Hour",                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMIZATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        xAxis.setRange(0, 23.5);
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }


     public void drawAggreagtedSupplyAndDemandWithTrueCostData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      ArrayList genAgentSupplyOfferByDay=amesFrame.getAMESMarket().getGenAgentSupplyOfferByDay();
      
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      double [][] genOfferData=new double [iGenNumber][8]; // a, b, minCap, maxCap, 1/b, a/b, currentPower, currentPrice
      double [][] offerPrices=new double [iGenNumber*2][2];  // price, index
      
      for(int i=0; i<iGenNumber; i++){
          genOfferData[i][0]=Double.parseDouble(genData[i][4].toString());
          genOfferData[i][1]=Double.parseDouble(genData[i][5].toString());
          genOfferData[i][2]=Double.parseDouble(genData[i][6].toString());
          genOfferData[i][3]=Double.parseDouble(genData[i][7].toString());
          genOfferData[i][4]=1.0/genOfferData[i][1];
          genOfferData[i][5]=genOfferData[i][0]/genOfferData[i][1];
          
          offerPrices[2*i][0]=genOfferData[i][0]+2*genOfferData[i][1]*genOfferData[i][2];
          offerPrices[2*i][1]=2*i;
          offerPrices[2*i+1][0]=genOfferData[i][0]+2*genOfferData[i][1]*genOfferData[i][3];
          offerPrices[2*i+1][1]=2*i+1;
          
          genOfferData[i][6]=genOfferData[i][2];
          genOfferData[i][7]=offerPrices[2*i][0];
      }
      
      // sort the price of each generator
      double dTemp=0;
      double iTemp=0;
      for(int i=0;i<2*iGenNumber; i++){
          for(int j=i+1;j<2*iGenNumber; j++){
              if(offerPrices[i][0]>offerPrices[j][0]){
                  dTemp=offerPrices[i][0];
                  iTemp=offerPrices[i][1];
                  offerPrices[i][0]=offerPrices[j][0];
                  offerPrices[i][1]=offerPrices[j][1];
                  offerPrices[j][0]=dTemp;
                  offerPrices[j][1]=iTemp;
              }
          }
      }
      
      double [][] genAggregateOfferDataPoints=new double[iGenNumber*2-1][4]; // leftPoint(power, price), rightPoint(power, price)
      int [][] genAggregateOfferCommit=new int[iGenNumber*2-1][iGenNumber*2]; 
      // in the first iGenNumber of each row is the index of gen whose output is fixed
      // in the second iGenNumber of each row is the index of gen whose output is variable
      // otherwise -1
      
      for(int i=0;i<2*iGenNumber-1; i++){
          for(int j=0;j<2*iGenNumber; j++){
              genAggregateOfferCommit[i][j]=-1;
          }
      }
          
      double leftPrice=offerPrices[0][0];
      for(int i=0; i<iGenNumber; i++){
          if(leftPrice>=genOfferData[i][7]){
              genAggregateOfferDataPoints[0][0]+=genOfferData[i][6];
          }
      }
      
      for(int i=0;i<2*iGenNumber-1; i++){
          genAggregateOfferDataPoints[i][1]=offerPrices[i][0];
          genAggregateOfferDataPoints[i][3]=offerPrices[i+1][0];
          
          if(i>0)
            genAggregateOfferDataPoints[i][0]=genAggregateOfferDataPoints[i-1][2];

          genAggregateOfferDataPoints[i][2]=genAggregateOfferDataPoints[i][0];
          
          int iIndexFixed=0;
          int iIndexVariable=0;
          
          double rightPrice=offerPrices[i+1][0];
          for(int j=0;j<iGenNumber; j++){
              if(rightPrice>genOfferData[j][7]){
                  if(Math.abs(genOfferData[j][6]-genOfferData[j][3])<0.000001){// already at maxCap
                      genAggregateOfferCommit[i][iIndexFixed++]=j;
                  }
                  else{
                      double power=(rightPrice-genOfferData[j][0])/(2*genOfferData[j][1]);
                      genAggregateOfferDataPoints[i][2]+=power-genOfferData[j][6];
                      genAggregateOfferCommit[i][iGenNumber+iIndexVariable]=j;
                      iIndexVariable++;
                     
                      genOfferData[j][7]=rightPrice;
                      genOfferData[j][6]=power;
                      
                  }
                
              }
          }
      }
      
      double HighestGenOfferPrice=genAggregateOfferDataPoints[iGenNumber*2-2][3];
      double HighestGenOfferPower=genAggregateOfferDataPoints[iGenNumber*2-2][2];
      
      // Demand
      Object [][][] lsePriceSensitiveData=amesFrame.getLSEPriceSensitiveDemandData();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      Object [][] lseData=amesFrame.getLSEData( );
      int iLSENumber=lsePriceSensitiveData.length;
      double [][] lseDemandData=new double [iLSENumber][6]; // c, d, slMax, fixed demand, currentPower, currentPrice
      double [][] lsePrices=new double [iLSENumber*2][2];  // price, index
      int hourlyLoadHybridFlagByLSE=0;
      double priceCap=1000.0;
      
      for(int i=0; i<iLSENumber; i++){
          hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[i][iDayHour+3].toString());
          
          if((hourlyLoadHybridFlagByLSE&2)==2){
              double c=Double.parseDouble(lsePriceSensitiveData[i][iDayHour][4].toString());
              double d=Double.parseDouble(lsePriceSensitiveData[i][iDayHour][5].toString());
              double slMax=Double.parseDouble(lsePriceSensitiveData[i][iDayHour][6].toString());
      
              lseDemandData[i][0]=c;
              lseDemandData[i][1]=d;
              lseDemandData[i][2]=slMax;
              lseDemandData[i][4]=0.0;
              lseDemandData[i][5]=c;
              
              lsePrices[2*i][0]=c;
              lsePrices[2*i][1]=2*i;
              lsePrices[2*i+1][0]=c-2*d*slMax;
              lsePrices[2*i+1][1]=2*i+1;
          }
          else{
              lseDemandData[i][0]=0.0;
              lseDemandData[i][1]=0.0;
              lseDemandData[i][2]=0.0;
              lseDemandData[i][4]=0.0;
              lseDemandData[i][5]=priceCap;
              
              lsePrices[2*i][0]=priceCap;
              lsePrices[2*i][1]=2*i;
              lsePrices[2*i+1][0]=priceCap;
              lsePrices[2*i+1][1]=2*i+1;
          }
          
          if((hourlyLoadHybridFlagByLSE&1)==1){ // fixed demand
              lseDemandData[i][3]=Double.parseDouble(lseData[i][iDayHour+3].toString());
          }
      }

      // sort the price of each lse
      for(int i=0;i<2*iLSENumber; i++){
          for(int j=i+1;j<2*iLSENumber; j++){
              if(lsePrices[i][0]<lsePrices[j][0]){
                  dTemp=lsePrices[i][0];
                  iTemp=lsePrices[i][1];
                  lsePrices[i][0]=lsePrices[j][0];
                  lsePrices[i][1]=lsePrices[j][1];
                  lsePrices[j][0]=dTemp;
                  lsePrices[j][1]=iTemp;
              }
          }
      }
      
      double [][] lseAggregateDemandDataPoints=new double[iLSENumber*2-1][4]; // leftPoint(power, price), rightPoint(power, price)
      int [][] lseAggregateDemandCommit=new int[iLSENumber*2-1][iLSENumber*2]; 
      // in the first iLSENumber of each row is the index of LSE whose commit is fixed
      // in the second iLSENumber of each row is the index of LSE whose commit is variable
      // otherwise -1
      
      for(int i=0;i<2*iLSENumber-1; i++){
          for(int j=0;j<2*iLSENumber; j++){
              lseAggregateDemandCommit[i][j]=-1;
          }
      }
          
      leftPrice=lsePrices[0][0];
      for(int i=0; i<iLSENumber; i++){
          lseAggregateDemandDataPoints[0][0]+=lseDemandData[i][3];
          
          if(leftPrice<=lseDemandData[i][5]){
              lseAggregateDemandDataPoints[0][0]+=lseDemandData[i][4];
          }
      }
      
      for(int i=0;i<2*iLSENumber-1; i++){
          lseAggregateDemandDataPoints[i][1]=lsePrices[i][0];
          lseAggregateDemandDataPoints[i][3]=lsePrices[i+1][0];
          
          if(i>0)
            lseAggregateDemandDataPoints[i][0]=lseAggregateDemandDataPoints[i-1][2];

          lseAggregateDemandDataPoints[i][2]=lseAggregateDemandDataPoints[i][0];
          
          int iIndexFixed=0;
          int iIndexVariable=0;
          
          double rightPrice=lsePrices[i+1][0];
          for(int j=0;j<iLSENumber; j++){
              if(rightPrice<lseDemandData[j][5]){
                  if(Math.abs(lseDemandData[j][4]-lseDemandData[j][2])<0.000001){// already at slMax
                      lseAggregateDemandCommit[i][iIndexFixed++]=j;
                  }
                  else{
                      double power=(lseDemandData[j][0]-rightPrice)/(2*lseDemandData[j][1]);
                      lseAggregateDemandDataPoints[i][2]+=power-lseDemandData[j][4];
                      lseAggregateDemandCommit[i][iLSENumber+iIndexVariable]=j;
                      iIndexVariable++;
                     
                      lseDemandData[j][5]=rightPrice;
                      lseDemandData[j][4]=power;
                      
                  }
                
              }
          }
      }
      
      double HighestLSEDemandPrice=lseAggregateDemandDataPoints[0][1];
      double HighestLSEDemandPower=lseAggregateDemandDataPoints[0][0];
      
      double highestPrice=(HighestGenOfferPrice>HighestLSEDemandPrice)? HighestGenOfferPrice: HighestLSEDemandPrice;
      highestPrice+=50;
      
      
      chartTitle="True Total Supply and Demand Curves at Hour "+iDayHour;
      String xLabel="Power (MWs)";
      
      dataset=new XYSeriesCollection();
 
      XYSeries series = new XYSeries("Supply");
        
      ArrayList genTipList=new ArrayList();
      String tipString="";
      
      leftPrice=genAggregateOfferDataPoints[0][1];
      double leftPower=genAggregateOfferDataPoints[0][0];
      series.add(leftPower, leftPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", leftPower, leftPrice);
      genTipList.add(new String(tipString));
      double rightPrice=0;
      double rightPower=0;
      
      for(int i=0;i<2*iGenNumber-1; i++){
          rightPrice=genAggregateOfferDataPoints[i][3];
          rightPower=genAggregateOfferDataPoints[i][2];
          
          series.add(rightPower, rightPrice);
          
          // For tip display
          tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, rightPrice);
          
          String tempFixed=" FixedGen: ";
          String tempVariable=" Marginal GenCos: ";
          String temp;
          boolean bFixed=false;
          boolean bVariable=false;
          
          for(int j=0; j<2*iGenNumber; j++){
              if((j<iGenNumber)&&(genAggregateOfferCommit[i][j]!=-1)){
                  bFixed=true;
                  tempFixed+=genData[genAggregateOfferCommit[i][j]][0]+" ";
              }
              
              if((j>=iGenNumber)&&(genAggregateOfferCommit[i][j]!=-1)){
                  bVariable=true;
                  tempVariable+=genData[genAggregateOfferCommit[i][j]][0]+" ";
              }
          }
          
          if(bFixed)
            tipString+=tempFixed;
          
          if(bVariable)
            tipString+=tempVariable;
      
          genTipList.add(new String(tipString));

      }
      // last infinity part
      series.add(rightPower, highestPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, highestPrice);
      genTipList.add(new String(tipString));
      
      dataset.addSeries(series);

      series = new XYSeries("Demand");

      ArrayList LSETipList=new ArrayList();
      
      //first infinity part
      series.add(HighestLSEDemandPower, highestPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", HighestLSEDemandPower, highestPrice);
      LSETipList.add(new String(tipString));
      
      //second point
      leftPrice=lseAggregateDemandDataPoints[0][1];
      leftPower=lseAggregateDemandDataPoints[0][0];
      series.add(leftPower, leftPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", leftPower, leftPrice);
      LSETipList.add(new String(tipString));
      
      for(int i=0;i<2*iLSENumber-1; i++){
          rightPrice=lseAggregateDemandDataPoints[i][3];
          rightPower=lseAggregateDemandDataPoints[i][2];
          
          series.add(rightPower, rightPrice);
          
          // For tip display
          tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, rightPrice);
          
          String tempFixed=" MaxPS LSE: ";
          String tempVariable=" VariablePS LSE: ";
          String temp;
          boolean bFixed=false;
          boolean bVariable=false;
          
          for(int j=0; j<2*iLSENumber; j++){
              if((j<iLSENumber)&&(lseAggregateDemandCommit[i][j]!=-1)){
                  bFixed=true;
                  tempFixed+=lseData[lseAggregateDemandCommit[i][j]][0]+" ";
              }
              
              if((j>=iLSENumber)&&(lseAggregateDemandCommit[i][j]!=-1)){
                  bVariable=true;
                  tempVariable+=lseData[lseAggregateDemandCommit[i][j]][0]+" ";
              }
          }
          
          if(bFixed)
            tipString+=tempFixed;
          
          if(bVariable)
            tipString+=tempVariable;
      
          LSETipList.add(new String(tipString));

      }

      // last infinity part
      series.add(rightPower, 0.0);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, 0.0);
      LSETipList.add(new String(tipString));

      dataset.addSeries(series);
  
      // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            xLabel,                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

         chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        CustomXYToolTipGenerator customTip=new CustomXYToolTipGenerator();
        customTip.addToolTipSeries(genTipList);
        customTip.addToolTipSeries(LSETipList);
        
        renderer.setToolTipGenerator(customTip);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }

     public void drawAggreagtedSupplyAndDemandData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      ArrayList genAgentSupplyOfferByDay=amesFrame.getAMESMarket().getGenAgentSupplyOfferByDay();
      double [][] genOffer=(double [][])genAgentSupplyOfferByDay.get(iStartTime-2);

      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      double [][] genOfferData=new double [iGenNumber][8]; // a, b, minCap, maxCap, 1/b, a/b, currentPower, currentPrice
      double [][] offerPrices=new double [iGenNumber*2][2];  // price, index
      
      for(int i=0; i<iGenNumber; i++){
          genOfferData[i][0]=genOffer[i][0];
          genOfferData[i][1]=genOffer[i][1];
          genOfferData[i][2]=genOffer[i][2];
          genOfferData[i][3]=genOffer[i][3];
          genOfferData[i][4]=1.0/genOfferData[i][1];
          genOfferData[i][5]=genOfferData[i][0]/genOfferData[i][1];
          
          offerPrices[2*i][0]=genOfferData[i][0]+2*genOfferData[i][1]*genOfferData[i][2];
          offerPrices[2*i][1]=2*i;
          offerPrices[2*i+1][0]=genOfferData[i][0]+2*genOfferData[i][1]*genOfferData[i][3];
          offerPrices[2*i+1][1]=2*i+1;
          
          genOfferData[i][6]=genOfferData[i][2];
          genOfferData[i][7]=offerPrices[2*i][0];
      }
      
      // sort the price of each generator
      double dTemp=0;
      double iTemp=0;
      for(int i=0;i<2*iGenNumber; i++){
          for(int j=i+1;j<2*iGenNumber; j++){
              if(offerPrices[i][0]>offerPrices[j][0]){
                  dTemp=offerPrices[i][0];
                  iTemp=offerPrices[i][1];
                  offerPrices[i][0]=offerPrices[j][0];
                  offerPrices[i][1]=offerPrices[j][1];
                  offerPrices[j][0]=dTemp;
                  offerPrices[j][1]=iTemp;
              }
          }
      }
      
      double [][] genAggregateOfferDataPoints=new double[iGenNumber*2-1][4]; // leftPoint(power, price), rightPoint(power, price)
      int [][] genAggregateOfferCommit=new int[iGenNumber*2-1][iGenNumber*2]; 
      // in the first iGenNumber of each row is the index of gen whose output is fixed
      // in the second iGenNumber of each row is the index of gen whose output is variable
      // otherwise -1
      
      for(int i=0;i<2*iGenNumber-1; i++){
          for(int j=0;j<2*iGenNumber; j++){
              genAggregateOfferCommit[i][j]=-1;
          }
      }
          
      double leftPrice=offerPrices[0][0];
      for(int i=0; i<iGenNumber; i++){
          if(leftPrice>=genOfferData[i][7]){
              genAggregateOfferDataPoints[0][0]+=genOfferData[i][6];
          }
      }
      
      for(int i=0;i<2*iGenNumber-1; i++){
          genAggregateOfferDataPoints[i][1]=offerPrices[i][0];
          genAggregateOfferDataPoints[i][3]=offerPrices[i+1][0];
          
          if(i>0)
            genAggregateOfferDataPoints[i][0]=genAggregateOfferDataPoints[i-1][2];

          genAggregateOfferDataPoints[i][2]=genAggregateOfferDataPoints[i][0];
          
          int iIndexFixed=0;
          int iIndexVariable=0;
          
          double rightPrice=offerPrices[i+1][0];
          for(int j=0;j<iGenNumber; j++){
              if(rightPrice>genOfferData[j][7]){
                  if(Math.abs(genOfferData[j][6]-genOfferData[j][3])<0.000001){// already at maxCap
                      genAggregateOfferCommit[i][iIndexFixed++]=j;
                  }
                  else{
                      double power=(rightPrice-genOfferData[j][0])/(2*genOfferData[j][1]);
                      genAggregateOfferDataPoints[i][2]+=power-genOfferData[j][6];
                      genAggregateOfferCommit[i][iGenNumber+iIndexVariable]=j;
                      iIndexVariable++;
                     
                      genOfferData[j][7]=rightPrice;
                      genOfferData[j][6]=power;
                      
                  }
                
              }
          }
      }
      
      double HighestGenOfferPrice=genAggregateOfferDataPoints[iGenNumber*2-2][3];
      double HighestGenOfferPower=genAggregateOfferDataPoints[iGenNumber*2-2][2];
      
      // Demand
      Object [][][] lsePriceSensitiveData=amesFrame.getLSEPriceSensitiveDemandData();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      Object [][] lseData=amesFrame.getLSEData( );
      int iLSENumber=lsePriceSensitiveData.length;
      double [][] lseDemandData=new double [iLSENumber][6]; // c, d, slMax, fixed demand, currentPower, currentPrice
      double [][] lsePrices=new double [iLSENumber*2][2];  // price, index
      int hourlyLoadHybridFlagByLSE=0;
      double priceCap=1000.0;
      
      for(int i=0; i<iLSENumber; i++){
          hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[i][iDayHour+3].toString());
          
          if((hourlyLoadHybridFlagByLSE&2)==2){
              double c=Double.parseDouble(lsePriceSensitiveData[i][iDayHour][4].toString());
              double d=Double.parseDouble(lsePriceSensitiveData[i][iDayHour][5].toString());
              double slMax=Double.parseDouble(lsePriceSensitiveData[i][iDayHour][6].toString());
      
              lseDemandData[i][0]=c;
              lseDemandData[i][1]=d;
              lseDemandData[i][2]=slMax;
              lseDemandData[i][4]=0.0;
              lseDemandData[i][5]=c;
              
              lsePrices[2*i][0]=c;
              lsePrices[2*i][1]=2*i;
              lsePrices[2*i+1][0]=c-2*d*slMax;
              lsePrices[2*i+1][1]=2*i+1;
          }
          else{
              lseDemandData[i][0]=0.0;
              lseDemandData[i][1]=0.0;
              lseDemandData[i][2]=0.0;
              lseDemandData[i][4]=0.0;
              lseDemandData[i][5]=priceCap;
              
              lsePrices[2*i][0]=priceCap;
              lsePrices[2*i][1]=2*i;
              lsePrices[2*i+1][0]=priceCap;
              lsePrices[2*i+1][1]=2*i+1;
          }
          
          if((hourlyLoadHybridFlagByLSE&1)==1){ // fixed demand
              lseDemandData[i][3]=Double.parseDouble(lseData[i][iDayHour+3].toString());
          }
      }

      // sort the price of each lse
      for(int i=0;i<2*iLSENumber; i++){
          for(int j=i+1;j<2*iLSENumber; j++){
              if(lsePrices[i][0]<lsePrices[j][0]){
                  dTemp=lsePrices[i][0];
                  iTemp=lsePrices[i][1];
                  lsePrices[i][0]=lsePrices[j][0];
                  lsePrices[i][1]=lsePrices[j][1];
                  lsePrices[j][0]=dTemp;
                  lsePrices[j][1]=iTemp;
              }
          }
      }
      
      double [][] lseAggregateDemandDataPoints=new double[iLSENumber*2-1][4]; // leftPoint(power, price), rightPoint(power, price)
      int [][] lseAggregateDemandCommit=new int[iLSENumber*2-1][iLSENumber*2]; 
      // in the first iLSENumber of each row is the index of LSE whose commit is fixed
      // in the second iLSENumber of each row is the index of LSE whose commit is variable
      // otherwise -1
      
      for(int i=0;i<2*iLSENumber-1; i++){
          for(int j=0;j<2*iLSENumber; j++){
              lseAggregateDemandCommit[i][j]=-1;
          }
      }
          
      leftPrice=lsePrices[0][0];
      for(int i=0; i<iLSENumber; i++){
          lseAggregateDemandDataPoints[0][0]+=lseDemandData[i][3];
          
          if(leftPrice<=lseDemandData[i][5]){
              lseAggregateDemandDataPoints[0][0]+=lseDemandData[i][4];
          }
      }
      
      for(int i=0;i<2*iLSENumber-1; i++){
          lseAggregateDemandDataPoints[i][1]=lsePrices[i][0];
          lseAggregateDemandDataPoints[i][3]=lsePrices[i+1][0];
          
          if(i>0)
            lseAggregateDemandDataPoints[i][0]=lseAggregateDemandDataPoints[i-1][2];

          lseAggregateDemandDataPoints[i][2]=lseAggregateDemandDataPoints[i][0];
          
          int iIndexFixed=0;
          int iIndexVariable=0;
          
          double rightPrice=lsePrices[i+1][0];
          for(int j=0;j<iLSENumber; j++){
              if(rightPrice<lseDemandData[j][5]){
                  if(Math.abs(lseDemandData[j][4]-lseDemandData[j][2])<0.000001){// already at slMax
                      lseAggregateDemandCommit[i][iIndexFixed++]=j;
                  }
                  else{
                      double power=(lseDemandData[j][0]-rightPrice)/(2*lseDemandData[j][1]);
                      lseAggregateDemandDataPoints[i][2]+=power-lseDemandData[j][4];
                      lseAggregateDemandCommit[i][iLSENumber+iIndexVariable]=j;
                      iIndexVariable++;
                     
                      lseDemandData[j][5]=rightPrice;
                      lseDemandData[j][4]=power;
                      
                  }
                
              }
          }
      }
      
      double HighestLSEDemandPrice=lseAggregateDemandDataPoints[0][1];
      double HighestLSEDemandPower=lseAggregateDemandDataPoints[0][0];
      
      double highestPrice=(HighestGenOfferPrice>HighestLSEDemandPrice)? HighestGenOfferPrice: HighestLSEDemandPrice;
      highestPrice+=50;
      
      chartTitle="Reported Total Supply and Demand Curves at Day "+iStartTime+" Hour "+iDayHour;
      String xLabel="Power (MWs)";
      
      dataset=new XYSeriesCollection();
 
      XYSeries series = new XYSeries("Supply");
        
      ArrayList genTipList=new ArrayList();
      String tipString="";
      
      leftPrice=genAggregateOfferDataPoints[0][1];
      double leftPower=genAggregateOfferDataPoints[0][0];
      series.add(leftPower, leftPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", leftPower, leftPrice);
      genTipList.add(new String(tipString));
      
      double rightPrice=0;
      double rightPower=0;
      
      for(int i=0;i<2*iGenNumber-1; i++){
          rightPrice=genAggregateOfferDataPoints[i][3];
          rightPower=genAggregateOfferDataPoints[i][2];
          
          series.add(rightPower, rightPrice);
          
          // For tip display
          tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, rightPrice);
          
          String tempFixed=" FixedGen: ";
          String tempVariable=" Marginal GenCos: ";
          String temp;
          boolean bFixed=false;
          boolean bVariable=false;
          
          for(int j=0; j<2*iGenNumber; j++){
              if((j<iGenNumber)&&(genAggregateOfferCommit[i][j]!=-1)){
                  bFixed=true;
                  tempFixed+=genData[genAggregateOfferCommit[i][j]][0]+" ";
              }
              
              if((j>=iGenNumber)&&(genAggregateOfferCommit[i][j]!=-1)){
                  bVariable=true;
                  tempVariable+=genData[genAggregateOfferCommit[i][j]][0]+" ";
              }
          }
          
          if(bFixed)
            tipString+=tempFixed;
          
          if(bVariable)
            tipString+=tempVariable;
      
          genTipList.add(new String(tipString));

      }
      
      // last infinity part
      series.add(rightPower, highestPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, highestPrice);
      genTipList.add(new String(tipString));
      
      dataset.addSeries(series);

      series = new XYSeries("Demand");

      ArrayList LSETipList=new ArrayList();
      
      //first infinity part
      series.add(HighestLSEDemandPower, highestPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", HighestLSEDemandPower, highestPrice);
      LSETipList.add(new String(tipString));
      
      //second point
      leftPrice=lseAggregateDemandDataPoints[0][1];
      leftPower=lseAggregateDemandDataPoints[0][0];
      series.add(leftPower, leftPrice);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", leftPower, leftPrice);
      LSETipList.add(new String(tipString));
      
      for(int i=0;i<2*iLSENumber-1; i++){
          rightPrice=lseAggregateDemandDataPoints[i][3];
          rightPower=lseAggregateDemandDataPoints[i][2];
          
          series.add(rightPower, rightPrice);
          
          // For tip display
          tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, rightPrice);
          
          String tempFixed=" MaxPS LSE: ";
          String tempVariable=" VariablePS LSE: ";
          String temp;
          boolean bFixed=false;
          boolean bVariable=false;
          
          for(int j=0; j<2*iLSENumber; j++){
              if((j<iLSENumber)&&(lseAggregateDemandCommit[i][j]!=-1)){
                  bFixed=true;
                  tempFixed+=lseData[lseAggregateDemandCommit[i][j]][0]+" ";
              }
              
              if((j>=iLSENumber)&&(lseAggregateDemandCommit[i][j]!=-1)){
                  bVariable=true;
                  tempVariable+=lseData[lseAggregateDemandCommit[i][j]][0]+" ";
              }
          }
          
          if(bFixed)
            tipString+=tempFixed;
          
          if(bVariable)
            tipString+=tempVariable;
      
          LSETipList.add(new String(tipString));

      }

      // last infinity part
      series.add(rightPower, 0.0);
      tipString=String.format("Power=%1$.2f Price=%2$.2f", rightPower, 0.0);
      LSETipList.add(new String(tipString));

      dataset.addSeries(series);
  
      // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            xLabel,                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

         chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);
        

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        CustomXYToolTipGenerator customTip=new CustomXYToolTipGenerator();
        customTip.addToolTipSeries(genTipList);
        customTip.addToolTipSeries(LSETipList);
        
        renderer.setToolTipGenerator(customTip);     
        //renderer.setSeriesStroke(0, new BasicStroke(3.0f));
        //renderer.setSeriesStroke(1, new BasicStroke(3.0f));
        
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        //xAxis.setLabelFont(font);
        //xAxis.setTickLabelFont(font);
        
        //NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        //yAxis.setLabelFont(font);
        //yAxis.setTickLabelFont(font);
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }

     public void drawGeneratorSupplyOfferData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int [] selectIndex) {
      String [] names =  {
                 "GenCo Name", "Day Index", "aR ($/MWh)", "bR ($/MW2h)", "CapRL (MW)", "CapRU (MW)",
           };
      
      ArrayList genAgentSupplyOfferByDay=amesFrame.getAMESMarket().getGenAgentSupplyOfferByDay();
      
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      String xLabel="Power (MWs)";
      
      dataset=new XYSeriesCollection();
 
      if(selectIndex.length<1){
          JOptionPane.showMessageDialog(this, "No GenCo is selected!", "Error Message", JOptionPane.ERROR_MESSAGE);
          return;
      }
      
      double [][] genOffer=(double [][])genAgentSupplyOfferByDay.get(iStartTime-1);

      int genIndex=selectIndex[0];

      chartTitle=genData[genIndex][0]+" Supply Offer for Day "+iStartTime;
      XYSeries series = new XYSeries("Reported");

      double dStart=genOffer[genIndex][0]+2.0*genOffer[genIndex][1]*genOffer[genIndex][2];
      double dEnd=genOffer[genIndex][0]+2.0*genOffer[genIndex][1]*genOffer[genIndex][3];
      series.add(genOffer[genIndex][2], dStart);
      series.add(genOffer[genIndex][3], dEnd);

      dataset.addSeries(series);

      series = new XYSeries("True");

      double da=Double.parseDouble(genData[genIndex][4].toString());
      double db=Double.parseDouble(genData[genIndex][5].toString());
      double dMinCap=Double.parseDouble(genData[genIndex][6].toString());
      double dMaxCap=Double.parseDouble(genData[genIndex][7].toString());
      
      dStart=da+2.0*db*dMinCap;
      dEnd=da+2.0*db*dMaxCap;
      series.add(dMinCap, dStart);
      series.add(dMaxCap, dEnd);

      dataset.addSeries(series);
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            xLabel,                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

         chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }

     public void drawGeneratorCommitmentData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                " GenCo Name", "Day Index","  Hour", " Power (MW)", "CapRL (MW)", "CapRU (MW)" 
            };
      chartTitle="GenCo Commitments";
      String xLabel="";
      
      iStartTime=iStartTime-1;
      iEndTime=iEndTime-1;
      
      ArrayList genAgentCommitmentByDay=amesFrame.getAMESMarket().getGenAgentDispatchByDay();
                 
      int[] hasSolutions;
      
      dataset=new XYSeriesCollection();
 
      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
              int iDayNumber=genAgentCommitmentByDay.size();
              double [][] genCommitment;

              chartTitle=chartTitle+"\n "+"for Entire Run (At Hour "+iDayHour+":00)";
              xLabel="Day";            
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              for(int j=0; j<iGenNumber; j++) {
                  XYSeries series = new XYSeries((String)genData[j][0]);
                 
                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                      
                     series.add(iDay+2, (double)(Math.round(genCommitment[iDayHour][j]*1000))/1000.0);
                    
                }

                dataset.addSeries(series);
              }
            
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
              int iDayNumber=iEndTime-iStartTime+1;
              double [][] genCommitment;

              chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (At Hour "+iDayHour+":00)";
              xLabel="Day";            
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              for(int j=0; j<iGenNumber; j++) {
                  XYSeries series = new XYSeries((String)genData[j][0]);
                 
                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                     
                     series.add(iDay+2, (double)(Math.round(genCommitment[iDayHour][j]*1000))/1000.0);
                    
                }

              dataset.addSeries(series);
              }
            
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
              int iDayNumber=genAgentCommitmentByDay.size();
              double [][] genCommitment;

              chartTitle=chartTitle+"\n "+"for Entire Run";
              xLabel="Day";            
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              for(int j=0; j<iGenNumber; j++) {
                  XYSeries series = new XYSeries((String)genData[j][0]);
                 
                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                     
                     for(int i=0; i<24; i++) {
                        series.add(iDay+1+i/24.0, (double)(Math.round(genCommitment[i][j]*1000))/1000.0);
                        
                      }
                 }

                 dataset.addSeries(series);
             }
            
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
              int iDayNumber=iEndTime-iStartTime+1;
              double [][] genCommitment;

              chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (All Hours)";
              xLabel="Day";            
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              for(int j=0; j<iGenNumber; j++) {
                  XYSeries series = new XYSeries((String)genData[j][0]);
                 
                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);
                     
                     for(int i=0; i<24; i++) {
                        series.add(iDay+1+i/24.0, (double)(Math.round(genCommitment[i][j]*1000))/1000.0);
                        
                        
                      }
                 }

                 dataset.addSeries(series);
                 
                 if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
             }
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                  int iDayNumber=genAgentCommitmentByDay.size();
                  double [][] genCommitment;
                  chartTitle=chartTitle+"\n "+"for Entire Run (At Hour "+iDayHour+":00)";
                  xLabel="Day";            

                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
              
                  for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)genData[selectIndex[j]-1][0]);

                      for(int iDay=0; iDay<iDayNumber; iDay++) {
                            genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);

                            series.add(iDay+1, (double)(Math.round(genCommitment[iDayHour][selectIndex[j]-1]*1000))/1000.0);
                            
                            
                        }

                  dataset.addSeries(series);
                  }
                 
                 if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
              }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                  int iDayNumber=iEndTime-iStartTime+1;
                  double [][] genCommitment;
                  chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (At Hour "+iDayHour+":00)";
                  xLabel="Day";            

                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
              
                  for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)genData[selectIndex[j]-1][0]);

                      for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                            genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);

                            series.add(iDay+2, (double)(Math.round(genCommitment[iDayHour][selectIndex[j]-1]*1000))/1000.0);
                            
                        }

                  dataset.addSeries(series);
                  }
                 
                 if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                  int iDayNumber=genAgentCommitmentByDay.size();
                  double [][] genCommitment;
                  chartTitle=chartTitle+"\n "+"for Entire Run";
                  xLabel="Day";            

                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
              
                  for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)genData[selectIndex[j]-1][0]);

                      for(int iDay=0; iDay<iDayNumber; iDay++) {
                         genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);

                         for(int i=0; i<24; i++) {
                            series.add(iDay+1+i/24.0, (double)(Math.round(genCommitment[i][selectIndex[j]-1]*1000))/1000.0);
                            
                          }
                     }

                     dataset.addSeries(series);
                  }
                 
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                  int iDayNumber=iEndTime-iStartTime+1;
                  double [][] genCommitment;
                  chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (All Hours)";
                  xLabel="Day";            

                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
              
                  for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)genData[selectIndex[j]-1][0]);

                      for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                         genCommitment=(double [][])genAgentCommitmentByDay.get(iDay);

                         for(int i=0; i<24; i++) {
                            series.add(iDay+1+i/24.0, (double)(Math.round(genCommitment[i][selectIndex[j]-1]*1000))/1000.0);
                            
                            
                          }
                     }

                     dataset.addSeries(series);
                  }
                  
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
            }
      }
       
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            xLabel,                      // x axis label
            "Power (MWs)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

         chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }

     public void drawGeneratorProfitAndNetEarningsData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int [] selectIndex, boolean bProfit) {
      
      if(bProfit)
        chartTitle="GenCo Daily Profits";
      else
        chartTitle="GenCo Daily Net Earnings";
          
      ArrayList genAgentProfitAndNetGainByDay=amesFrame.getAMESMarket().getGenAgentProfitAndNetGainByDay();
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D = new DefaultCategoryDataset();

      Object [][] genData=amesFrame.getGeneratorData( );
      int iGenNumber=genData.length;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
         if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
              int iDayNumber=genAgentProfitAndNetGainByDay.size();
              double [][] genProfit;
 
              chartTitle=chartTitle+"\n "+"for Entire Run";
             
              for(int i=0; i<iGenNumber; i++) {
                String genName=(String)genData[i][0];
                         
                XYSeries series1;
                
                series1= new XYSeries(genName);
               
                for(int iDay=0; iDay<iDayNumber; iDay++) {
                      genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);
                 
                      if(bProfit)
                          series1.add(iDay+1, (double)(Math.round(genProfit[i][0]*1000))/1000.0);
                      else
                          series1.add(iDay+1, (double)(Math.round(genProfit[i][1]*1000))/1000.0);
                      }

                   dataset.addSeries(series1);
              }
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
              int iDayNumber=iEndTime-iStartTime+1;
              double [][] genProfit;
 
              chartTitle=chartTitle+"\n "+"From Day "+iStartTime+" to Day "+iEndTime;
             
              for(int i=0; i<iGenNumber; i++) {
                String genName=(String)genData[i][0];
                         
                XYSeries series1;
                
                series1= new XYSeries(genName);

                for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                      genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);
                 
                      if(bProfit)
                          series1.add(iDay+1, (double)(Math.round(genProfit[i][0]*1000))/1000.0);
                      else
                          series1.add(iDay+1, (double)(Math.round(genProfit[i][1]*1000))/1000.0);
                  }

               dataset.addSeries(series1);
              }
          }
      }
      else {
          int iDataNumber=selectIndex.length;
            
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
                  int iDayNumber=genAgentProfitAndNetGainByDay.size();
                  double [][] genProfit;

                  chartTitle=chartTitle+"\n "+"for Entire Run";

                  for(int i=0; i<iDataNumber; i++) {
                         String genName=(String)genData[selectIndex[i]-1][0];
                         
                        XYSeries series1;

                        series1= new XYSeries(genName);
                         
                          for(int iDay=0; iDay<iDayNumber; iDay++) {
                                genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);

                                 if(bProfit)
                                    series1.add(iDay+1, (double)(Math.round(genProfit[selectIndex[i]-1][0]*1000))/1000.0);
                                 else
                                    series1.add(iDay+1, (double)(Math.round(genProfit[selectIndex[i]-1][1]*1000))/1000.0);
                              }

                           dataset.addSeries(series1);
                       }
              }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
                  int iDayNumber=iEndTime-iStartTime+1;
                  double [][] genProfit;

                  chartTitle=chartTitle+"\n "+"From Day "+iStartTime+" to Day "+iEndTime;

                  for(int i=0; i<iDataNumber; i++) {
                         String genName=(String)genData[selectIndex[i]-1][0];
                         
                            XYSeries series1;

                          series1= new XYSeries(genName);
                         
                          for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                                genProfit=(double [][])genAgentProfitAndNetGainByDay.get(iDay);

                                if(bProfit)
                                    series1.add(iDay+1, (double)(Math.round(genProfit[selectIndex[i]-1][0]*1000))/1000.0);
                                else
                                    series1.add(iDay+1, (double)(Math.round(genProfit[selectIndex[i]-1][1]*1000))/1000.0);
                              }

                           dataset.addSeries(series1);
                       }
              }
      }
      
      if(draw3DChart) {

            chart=ChartFactory.createBarChart3D(
               chartTitle,      // chart title
                "Value Category",               // domain axis label
                "Value ($/D)",                  // range axis label
                dataset3D,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
            );

            final CategoryPlot plot=chart.getCategoryPlot();
            final CategoryAxis axis=plot.getDomainAxis();
            axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/8.0));

            final CategoryItemRenderer renderer=plot.getRenderer();
            renderer.setItemLabelsVisible(true);
            final BarRenderer r=(BarRenderer) renderer;
            r.setMaximumBarWidth(0.05);
     }
     else {
            // create the chart...
            chart=ChartFactory.createXYLineChart(
               chartTitle,      // chart title
                "Day",                      // x axis label
                "Money ($/D)",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
            );

            chart.setBackgroundPaint(Color.white);

            // get a reference to the plot for further customisation...
            final XYPlot plot=chart.getXYPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setDomainGridlinePaint(Color.blue);
            plot.setRangeGridlinePaint(Color.blue);

            final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
            XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
            renderer.setToolTipGenerator(generator);       
            plot.setRenderer(renderer);
        
            NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
            xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
     }
         
       chart.getTitle().setFont(font);
       chartPanel.setChart(chart);
 }

     public void drawLSESurplusData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int [] selectIndex) {
      String [] names =  {
                 "LSE Name", "Day Index", "Surplus ($/H)"
           };
      
      chartTitle="LSE Daily Net Earnings";
      ArrayList lseAgentSurplusByDay=amesFrame.getAMESMarket().getLSEAgentSurplusByDay();
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D = new DefaultCategoryDataset();

      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lseHybridData.length;
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
         if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
              int iDayNumber=lseAgentSurplusByDay.size();
              double [][] lseSurplus;
 
              chartTitle=chartTitle+"\n "+"for Entire Run";
             
              for(int i=0; i<iLSENumber; i++) {
                String lseName=(String)lseHybridData[i][0];
                         
                XYSeries series1 = new XYSeries(lseName+" Surplus");
               
                for(int iDay=0; iDay<iDayNumber; iDay++) {
                      lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);
                 
                      series1.add(iDay+1, (double)(Math.round(lseSurplus[i][0]*1000))/1000.0);
                      }

                   dataset.addSeries(series1);
              }
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
              int iDayNumber=iEndTime-iStartTime+1;
              double [][] lseSurplus;
 
              chartTitle=chartTitle+"\n "+"From Day "+iStartTime+" to Day "+iEndTime;
             
              for(int i=0; i<iLSENumber; i++) {
                String lseName=(String)lseHybridData[i][0];
                         
                XYSeries series1 = new XYSeries(lseName+" Surplus");
               
                for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                      lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);
                 
                      series1.add(iDay+1, (double)(Math.round(lseSurplus[i][0]*1000))/1000.0);
                      }

                   dataset.addSeries(series1);
              }
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          int iField=names.length;
            
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run")){
                  int iDayNumber=lseAgentSurplusByDay.size();
                  double [][] lseSurplus;

                  chartTitle=chartTitle+"\n "+"for Entire Run";

                  for(int i=0; i<iDataNumber; i++) {
                         String lseName=(String)lseHybridData[selectIndex[i]-1][0];
                         
                         XYSeries series1 = new XYSeries(lseName+" Surplus");
                         
                          for(int iDay=0; iDay<iDayNumber; iDay++) {
                                lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);

                              series1.add(iDay+1, (double)(Math.round(lseSurplus[selectIndex[i]-1][0]*1000))/1000.0);
                              }

                           dataset.addSeries(series1);
                       }
              }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day")){
                  int iDayNumber=iEndTime-iStartTime+1;
                  double [][] lseSurplus;

                  chartTitle=chartTitle+"\n "+"From Day "+iStartTime+" to Day "+iEndTime;

                  for(int i=0; i<iDataNumber; i++) {
                         String lseName=(String)lseHybridData[selectIndex[i]-1][0];
                         
                         XYSeries series1 = new XYSeries(lseName+" Surplus");
                         
                          for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                                lseSurplus=(double [][])lseAgentSurplusByDay.get(iDay);

                              series1.add(iDay+1, (double)(Math.round(lseSurplus[selectIndex[i]-1][0]*1000))/1000.0);
                              }

                           dataset.addSeries(series1);
                       }
              }
      }
      
      if(draw3DChart) {

            chart=ChartFactory.createBarChart3D(
               chartTitle,      // chart title
                "Value Category",               // domain axis label
                "Value",                  // range axis label
                dataset3D,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
            );

            final CategoryPlot plot=chart.getCategoryPlot();
            final CategoryAxis axis=plot.getDomainAxis();
            axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI/8.0));

            final CategoryItemRenderer renderer=plot.getRenderer();
            renderer.setItemLabelsVisible(true);
            final BarRenderer r=(BarRenderer) renderer;
            r.setMaximumBarWidth(0.05);
     }
     else {
            // create the chart...
            chart=ChartFactory.createXYLineChart(
               chartTitle,      // chart title
                "Day",                      // x axis label
                "Value",                      // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
            );

            chart.setBackgroundPaint(Color.white);

            // get a reference to the plot for further customisation...
            final XYPlot plot=chart.getXYPlot();
            plot.setBackgroundPaint(Color.white);
            plot.setDomainGridlinePaint(Color.blue);
            plot.setRangeGridlinePaint(Color.blue);

            final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
            XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
            renderer.setToolTipGenerator(generator);       
            plot.setRenderer(renderer);
        
            NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
            xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
            
        
     }
         
       chart.getTitle().setFont(font);
       chartPanel.setChart(chart);
 }

     public void drawLSEPriceSensitiveDemandData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "LSE Name", "Day Index",  "Hour",  "c ($/MWh)", "d ($/MW2h)", "SLMax (MW)", "Price-Sensitive Demand (MW)"
           };
      
      Object [][][] lsePriceSensitiveData=amesFrame.getLSEPriceSensitiveDemandData();
      Object [][] lseHybridData=amesFrame.getLSEHybridDemandData();
      int iLSENumber=lsePriceSensitiveData.length;
      
      ArrayList LMPByDay=amesFrame.getAMESMarket().getLMPByDay();
      
      ArrayList priceSensitiveByDay=amesFrame.getAMESMarket().getLSEAgenPriceSensitiveDemandByDay();
      
      chartTitle="LSE Price-Sensitive Demand Function and Cleared Point";
      String xLabel="Power (MWs)";
      
      dataset=new XYSeriesCollection();
 
      if(selectIndex.length<1){
          JOptionPane.showMessageDialog(this, "No LSE is selected!", "Error Message", JOptionPane.ERROR_MESSAGE);
          return;
      }
      
      double [][] lsePS=(double [][])priceSensitiveByDay.get(iStartTime-2);
      double [][] lmp=(double [][])LMPByDay.get(iStartTime-2);

      int lseIndex=selectIndex[0];
      int lseAtBus=Integer.parseInt(lseHybridData[lseIndex][2].toString());
      double busLMP=lmp[iDayHour][lseAtBus-1];
      
      chartTitle=chartTitle+"\n "+lsePriceSensitiveData[lseIndex][0][0]+" for Day "+iStartTime+" ( At Hour "+iDayHour+":00)";
      XYSeries series = new XYSeries("Cleared Point");
      
      int psLoadIndex=0;
      // Get the psLoadIndex
      for(int k=0; k<selectIndex[0]; k++){
        int hourlyFlag=Integer.parseInt(lseHybridData[k][iDayHour+3].toString());
        if((hourlyFlag&2)==2)
            psLoadIndex++;
      }
   
      double dStart=0;
      double dEnd=0;
      int hourlyLoadHybridFlagByLSE=Integer.parseInt(lseHybridData[selectIndex[0]][iDayHour+3].toString());
      if((hourlyLoadHybridFlagByLSE&2)==2)
          dStart=lsePS[iDayHour][psLoadIndex];
      
      double c=Double.parseDouble(lsePriceSensitiveData[lseIndex][iDayHour][4].toString());
      double d=Double.parseDouble(lsePriceSensitiveData[lseIndex][iDayHour][5].toString());
      double slMax=Double.parseDouble(lsePriceSensitiveData[lseIndex][iDayHour][6].toString());
      
      //dEnd=c-2.0*d*dStart;
  
      series.add(dStart, busLMP);

      dataset.addSeries(series);

      series = new XYSeries("Price-Sensitive Demand Function");

      dEnd=c-2.0*d*slMax;
      series.add(0, c);
      series.add(slMax, dEnd);

      dataset.addSeries(series);
          // create the chart...
        chart=ChartFactory.createXYLineChart(
            chartTitle,      // chart title
            xLabel,                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

         chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }

     public void drawBranchPowerFlowData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "Branch Name", "Day Index",  "Hour",  "Power (MW)", "MaxCap (MW)"
           };
      chartTitle="Branch Power Flows";
      boolean bSubtitle=false;
      String subtitles="";
      String xLabel="";
      
      iStartTime=iStartTime-1;
      iEndTime=iEndTime-1;
      
      ArrayList branchFlowByDay=amesFrame.getAMESMarket().getBranchFlowByDay();
                 
      int[] hasSolutions;
      
      Object [][] branchData=amesFrame.getBranchData( );
      int iBranchNumber=branchData.length;
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
              chartTitle=chartTitle+"\n "+"For Entire Run (At Hour "+iDayHour+":00)\n";
              xLabel="Day";
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              int iDayNumber=branchFlowByDay.size();
              
              double [][] branchFlow;
              
               for(int j=0; j<iBranchNumber; j++) {
                  XYSeries series = new XYSeries((String)branchData[j][0]);
                  
                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     
                     series.add(iDay+2, (double)(Math.round(branchFlow[iDayHour][j]*1000))/1000.0);
                    
                    
                }

                dataset.addSeries(series);
              }
              
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                int iDayNumber=iEndTime-iStartTime+1;
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              double [][] branchFlow;
              
               for(int j=0; j<iBranchNumber; j++) {
                  XYSeries series = new XYSeries((String)branchData[j][0]);
                 
                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     
                     series.add(iDay+2, (double)(Math.round(branchFlow[iDayHour][j]*1000))/1000.0);
                    
                    
                  }

                 dataset.addSeries(series);
              }
              
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);

              chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (At Hour "+iDayHour+":00)";
              xLabel="Day";
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
              int iDayNumber=branchFlowByDay.size();
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              double [][] branchFlow;
              
              for(int j=0; j<iBranchNumber; j++) {
                  XYSeries series = new XYSeries((String)branchData[j][0]);
                
                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     
                     for(int i=0; i<24; i++) {
                         series.add(iDay+1+i/24.0, (double)(Math.round(branchFlow[i][j]*1000))/1000.0);
                    
                        
                      }
                 }

                 dataset.addSeries(series);
             }
              
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);

              chartTitle=chartTitle+"\n "+"For Entire Run (All Hours)";
              xLabel="Day";
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
              int iDayNumber=iEndTime-iStartTime+1;
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              double [][] branchFlow;
              
              for(int j=0; j<iBranchNumber; j++) {
                  XYSeries series = new XYSeries((String)branchData[j][0]);
                 
                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                     branchFlow=(double [][])branchFlowByDay.get(iDay);
                     
                     for(int i=0; i<24; i++) {
                        series.add(iDay+1+i/24.0, (double)(Math.round(branchFlow[i][j]*1000))/1000.0);
                    
                        
                      }
                 }

                 dataset.addSeries(series);
             }
              
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);

              chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (All Hours)";
              xLabel="Day";
          }
      }
      else {
          int iDataNumber=selectIndex.length;
          if(iDataNumber==1)
              bSubtitle=true;
          
          int iField=names.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
                  int iDayNumber=branchFlowByDay.size();

                  double [][] branchFlow;
                  for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)branchData[selectIndex[j]-1][0]);
                      subtitles+=branchData[selectIndex[j]-1][0]+" Thermal Limit: "+branchData[selectIndex[j]-1][3].toString()+"\n";                 

                      for(int iDay=0; iDay<iDayNumber; iDay++) {
                            branchFlow=(double [][])branchFlowByDay.get(iDay);

                            series.add(iDay+2, (double)(Math.round(branchFlow[iDayHour][selectIndex[j]-1]*1000))/1000.0);
                             
                            
                      }

                    dataset.addSeries(series);
                  }
              
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);

                  chartTitle=chartTitle+"\n "+"For Entire Run (At Hour "+iDayHour+":00)";
                  xLabel="Day";
              }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
                  int iDayNumber=iEndTime-iStartTime+1;

                  double [][] branchFlow;
                  for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)branchData[selectIndex[j]-1][0]);
                      subtitles+=branchData[selectIndex[j]-1][0]+" Thermal Limit: "+branchData[selectIndex[j]-1][3].toString()+"\n";                 

                      for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                            branchFlow=(double [][])branchFlowByDay.get(iDay);

                            series.add(iDay+2, (double)(Math.round(branchFlow[iDayHour][selectIndex[j]-1]*1000))/1000.0);
                            
                      }

                      dataset.addSeries(series);
                  }
              
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);

                  chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (At Hour "+iDayHour+":00)";
                  xLabel="Day";
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
                  int iDayNumber=branchFlowByDay.size();
 
                  double [][] branchFlow;

                   for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)branchData[selectIndex[j]-1][0]);
                      subtitles+=branchData[selectIndex[j]-1][0]+" Thermal Limit: "+branchData[selectIndex[j]-1][3].toString()+"\n";                 

                      for(int iDay=0; iDay<iDayNumber; iDay++) {
                          branchFlow=(double [][])branchFlowByDay.get(iDay);

                         for(int i=0; i<24; i++) {
                            series.add(iDay+1+i/24.0, (double)(Math.round(branchFlow[i][selectIndex[j]-1]*1000))/1000.0);
                            
                          }
                     }

                     dataset.addSeries(series);
                }
                  
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
      
                  chartTitle=chartTitle+"\n "+"For Entire Run (All Hours)";
                  xLabel="Day";
            }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;
                  int iDayNumber=iEndTime-iStartTime+1;
 
                  double [][] branchFlow;

                   for(int j=0; j<iDataNumber; j++) {
                      XYSeries series = new XYSeries((String)branchData[selectIndex[j]-1][0]);
                      subtitles+=branchData[selectIndex[j]-1][0]+" Thermal Limit: "+branchData[selectIndex[j]-1][3].toString()+"\n";                 

                      for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                          branchFlow=(double [][])branchFlowByDay.get(iDay);

                         for(int i=0; i<24; i++) {
                            series.add(iDay+1+i/24.0, (double)(Math.round(branchFlow[i][selectIndex[j]-1]*1000))/1000.0);
                            
                          }
                     }

                     dataset.addSeries(series);
                  }
                  
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
      
                  chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (All Hours)";
                  xLabel="Day";
            }
      }
      
          // create the chart...
        chart=ChartFactory.createXYLineChart(
           chartTitle,      // chart title
            xLabel,                      // x axis label
            "Power (MWs)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        
        if(bSubtitle){
            TextTitle subTitle=new TextTitle();
            subTitle.setText(subtitles);

            chart.addSubtitle(subTitle);
        }

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }
   
   public void drawNodeLMPData(String outputTimeTypeSelect, int iStartTime, int iEndTime, int iDayHour, int [] selectIndex) {
      String [] names =  {
                 "Bus Name", "Day Index", "   Hour",  "LMP ($/MWh)"
           };
      chartTitle="Locational Marginal Price";
      String xLabel="";
      
      iStartTime=iStartTime-1;
      iEndTime=iEndTime-1;
      
      ArrayList LMPByDay=amesFrame.getAMESMarket().getLMPByDay();
                 
      int[] hasSolutions;
      
      String [] nodeName=amesFrame.getNodeNameData( );
      int iNodeNumber=nodeName.length;
      
      boolean draw3DChart=false;
      dataset=new XYSeriesCollection();
      DefaultCategoryDataset dataset3D=new DefaultCategoryDataset();
      
      if((selectIndex.length<1)||(selectIndex[0]==0)) {
          if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
              int iDayNumber=LMPByDay.size();
              xLabel="Day";
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              double [][] lmp;
              
               for(int j=0; j<iNodeNumber; j++) {
                 XYSeries series = new XYSeries(nodeName[j]);
                 
                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                        lmp=(double [][])LMPByDay.get(iDay);
                     
                        series.add(iDay+2, (double)(Math.round(lmp[iDayHour][j]*1000))/1000.0);
                        
                }

                dataset.addSeries(series);
              }
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);

              chartTitle=chartTitle+"\n "+"for Entire Run (At Hour "+ iDayHour+":00)";
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
              xLabel="Day";
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              double [][] lmp;
              
               for(int j=0; j<iNodeNumber; j++) {
                 XYSeries series = new XYSeries(nodeName[j]);
                 
                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                        lmp=(double [][])LMPByDay.get(iDay);
                     
                        series.add(iDay+2, (double)(Math.round(lmp[iDayHour][j]*1000))/1000.0);
                        
                }

                dataset.addSeries(series);
              }
              if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);

              chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (At Hour "+ iDayHour+":00)";
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
              int iDayNumber=LMPByDay.size();
              xLabel="Day";
              
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              double [][] lmp;
              
              for(int j=0; j<iNodeNumber; j++) {
                 XYSeries series = new XYSeries(nodeName[j]);
                 
                  for(int iDay=0; iDay<iDayNumber; iDay++) {
                    lmp=(double [][])LMPByDay.get(iDay);
                     
                     for(int i=0; i<24; i++) {
                        series.add(iDay+1+i/24.0, (double)(Math.round(lmp[i][j]*1000))/1000.0);
                        
                      }
                 }

                 dataset.addSeries(series);
             }
             if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);

              chartTitle=chartTitle+"\n "+"for Entire Run (All Hours)";
          }
          else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
              xLabel="Day";
             
              XYSeries noSolutionSeries = new XYSeries("No solution");
              dataset.addSeries(noSolutionSeries);
              boolean bNoSolution=false;
              
              double [][] lmp;
              
              for(int j=0; j<iNodeNumber; j++) {
                 XYSeries series = new XYSeries(nodeName[j]);
                 
                  for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                    lmp=(double [][])LMPByDay.get(iDay);
                     
                     for(int i=0; i<24; i++) {
                        series.add(iDay+1+i/24.0, (double)(Math.round(lmp[i][j]*1000))/1000.0);
                        
                      }
                 }

                 dataset.addSeries(series);
             }
             if(!bNoSolution)
                dataset.removeSeries(noSolutionSeries);

              chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (All Hours)";
          }
      }
      else {
          int iDataNumber=selectIndex.length;
            
              if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (Selected Hour)")) {
                  int iDayNumber=LMPByDay.size();
                  xLabel="Day";
             
                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;

                  double [][] lmp;

                  for(int j=0; j<iDataNumber; j++) {
                       XYSeries series = new XYSeries(nodeName[selectIndex[j]-1]);

                      for(int iDay=0; iDay<iDayNumber; iDay++) {
                          lmp=(double [][])LMPByDay.get(iDay);

                          series.add(iDay+2, (double)(Math.round(lmp[iDayHour][selectIndex[j]-1]*1000))/1000.0);

                      }

                  dataset.addSeries(series);
                  }
                 
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);

                  chartTitle=chartTitle+"\n "+"for Entire Run (At Hour "+iDayHour+":00)";
              }
              else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (Selected Hour)")) {
                  xLabel="Day";

                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;

                  double [][] lmp;

                  for(int j=0; j<iDataNumber; j++) {
                       XYSeries series = new XYSeries(nodeName[selectIndex[j]-1]);

                      for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                          lmp=(double [][])LMPByDay.get(iDay);

                          series.add(iDay+2, (double)(Math.round(lmp[iDayHour][selectIndex[j]-1]*1000))/1000.0);

                      }

                  dataset.addSeries(series);
                  }
                 
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);

                  chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (At Hour "+iDayHour+":00)";
              }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Entire Run (All Hours)")) {
                  int iDayNumber=LMPByDay.size();
                  xLabel="Day";

                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;

                  double [][] lmp;

                   for(int j=0; j<iDataNumber; j++) {
                       XYSeries series = new XYSeries(nodeName[selectIndex[j]-1]);

                      for(int iDay=0; iDay<iDayNumber; iDay++) {
                          lmp=(double [][])LMPByDay.get(iDay);

                         for(int i=0; i<24; i++) {
                            series.add(iDay+1+i/24.0, (double)(Math.round(lmp[i][selectIndex[j]-1]*1000))/1000.0);
                            
                          }
                     }

                     dataset.addSeries(series);
                   }
                 
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
      
                  chartTitle=chartTitle+"\n "+"for Entire Run (All Hours)";
             }
               else if(outputTimeTypeSelect.equalsIgnoreCase("Start to End Day (All Hours)")) {
                  xLabel="Day";

                  XYSeries noSolutionSeries = new XYSeries("No solution");
                  dataset.addSeries(noSolutionSeries);
                  boolean bNoSolution=false;

                  double [][] lmp;

                   for(int j=0; j<iDataNumber; j++) {
                       XYSeries series = new XYSeries(nodeName[selectIndex[j]-1]);

                      for(int iDay=iStartTime-1; iDay<iEndTime; iDay++) {
                          lmp=(double [][])LMPByDay.get(iDay);

                         for(int i=0; i<24; i++) {
                            series.add(iDay+1+i/24.0, (double)(Math.round(lmp[i][selectIndex[j]-1]*1000))/1000.0);
                            
                          }
                     }

                     dataset.addSeries(series);
                   }
                 
                  if(!bNoSolution)
                    dataset.removeSeries(noSolutionSeries);
      
                  chartTitle=chartTitle+"\n "+"From Day "+(iStartTime+1)+" to Day "+(iEndTime+1) +" (All Hours)";
             }
          }
      
          // create the chart...
        chart = ChartFactory.createXYLineChart(
           chartTitle,      // chart title
            xLabel,                      // x axis label
            "Price ($/MWh)",                      // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final XYPlot plot=chart.getXYPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.blue);
        plot.setRangeGridlinePaint(Color.blue);

        final XYLineAndShapeRenderer renderer=new XYLineAndShapeRenderer();
        XYToolTipGenerator generator = new StandardXYToolTipGenerator("{2}", new DecimalFormat("0.00"), new DecimalFormat("0.00"));
        renderer.setToolTipGenerator(generator);       
        plot.setRenderer(renderer);
         
        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        chart.getTitle().setFont(font);
        chartPanel.setChart(chart);
 }

     public void setAMESFrame(AMESFrame frame) {
        amesFrame=frame;
    }
    
     public void setChartTitle(String title) {
        chartTitle = title;
    }

    public void setXLabel(String label) {
        xLabel = label;
    }

    public void setYLabel(String label) {
        xLabel = label;
    }
    
    public void setbackgroundColor(Color newColor) {
        Plot plot=chart.getPlot();
        plot.setBackgroundPaint(newColor);
     }
    
   public void setGridLineColor(Color newColor) {
        Plot plot=chart.getPlot();
        
        if(plot instanceof XYPlot) {
            XYPlot xyPlot =(XYPlot)plot;
            xyPlot.setDomainGridlinePaint(newColor);
            xyPlot.setRangeGridlinePaint(newColor);
        }
     }
    
     private String chartTitle;
     private String xLabel;
     private String yLabel;
     private XYSeriesCollection dataset;
     private JFreeChart chart;
     private ChartPanel chartPanel;
     private Font font = new Font("Times New Roman", Font.BOLD, 20);
     private AMESFrame amesFrame;
  
}
