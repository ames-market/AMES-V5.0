


/*
 * OutputPane.java
 *
 * Created on June 16, 2007, 4:36 PM
 */

package AMESGUIFrame;


import java.awt.*;
import java.awt.event.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JTextArea;
import amesmarket.AMESMarket;

public class OutputPane extends javax.swing.JPanel {
    
    /** Creates new form OutputPane */
    public OutputPane() {
        initComponents();
     
        PrintStream print=new PrintStream(new TextAreaOutputStream(OutputTextArea));

        System.setOut(print);
        System.setErr(print);
        
        
        System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>\n");
        System.out.println("                     Version 5.0 of the AMES Wholesale Power Market Test Bed (AMES) \n");
        System.out.println("                             developed by S. Battula and L. Tesfatsion \n");
        System.out.println("            with support from PNNL (Tom McDermott, Mitch Pelton, Qiuhua Huang, and Sarmad Hanif) \n");
        System.out.println("                       released as open-source software under the BSD 3-Clause License  \n");
        System.out.println("                             Copyright (c) 2020, Battelle Memorial Institute \n");
        System.out.println("     AMES Version Release History: http://www2.econ.iastate.edu/tesfatsi/AMESVersionReleaseHistory.htm \n");
        System.out.println("<><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><><>\n");
        
        Font font = new Font("Courier New", Font.PLAIN, 12);
        OutputTextArea.setFont(font);
        
        clearAllItem = popupMenu.add("Clear All");          
        clearAllItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  clearAllItemActionPerformed(evt);
                }
        });

        popupMenu.addSeparator();                      

        cutItem = popupMenu.add("Cut");          
        cutItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  cutItemActionPerformed(evt);
                }
        });

        copyItem = popupMenu.add("Copy");          
        copyItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  copyItemActionPerformed(evt);
                }
        });

        pasteItem = popupMenu.add("Paste");          
        pasteItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  pasteItemActionPerformed(evt);
                }
        });

        deleteItem = popupMenu.add("Delete");          
        deleteItem.addActionListener(new ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                  deleteItemActionPerformed(evt);
                }
        });
       
        // Set the component to show the popup menu
        OutputTextArea.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });
     
        CheckOutputPaneRunnable checkRunable=new CheckOutputPaneRunnable();
        checkRunable.setOutputPane(this);
        (new Thread(checkRunable)).start();
  }
    
  private void clearAllItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.selectAll();
    OutputTextArea.replaceSelection("");
  }
 
 private void cutItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.cut();
 }
 
 private void copyItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.copy();
 }
 
 private void pasteItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.paste();
 }
 
 private void deleteItemActionPerformed(java.awt.event.ActionEvent evt) {
    OutputTextArea.replaceSelection("");   
 }
public class CheckOutputPaneRunnable implements Runnable {

    private OutputPane outputPane;
    
    public void setOutputPane(OutputPane pane){
        outputPane=pane;
    }
    
    public void run() {
        while(true){
                try {
                    Thread.sleep(1000);
                    //int iCount=OutputTextArea.getLineCount();
                    //System.out.println("iCount="+iCount);

                    if (OutputTextArea.getLineCount() > 10000) {
                        OutputTextArea.select(0, 0);
                        OutputTextArea.selectAll();
                        OutputTextArea.replaceSelection("");
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(OutputPane.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
    }
}
 
   /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        OutputTextArea = new javax.swing.JTextArea();

        OutputTextArea.setColumns(20);
        OutputTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        OutputTextArea.setRows(5);
        jScrollPane1.setViewportView(OutputTextArea);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    
public class TextAreaOutputStream extends OutputStream {
    private JTextArea output;
    
    public TextAreaOutputStream(JTextArea area) {
        output=area;
    }
    
    public void write(int b) throws IOException {
         output.append(String.valueOf((char)b));
    }   
}
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea OutputTextArea;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
     
    private javax.swing.JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem clearAllItem, cutItem, copyItem, pasteItem, deleteItem;
   
}
