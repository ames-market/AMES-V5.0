

 /*
 * PowerGridConfigure2.java
 *
 * Created on June 5, 2007, 9:25 PM
 */
package AMESGUIFrame;

import java.awt.event.*;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class PowerGridConfigure2 extends javax.swing.JFrame {

    /**
     * Creates new form PowerGridConfigure2
     *
     * @param frame
     */
    public PowerGridConfigure2(AMESFrame frame) {
        mainFrame = frame;
        initComponents();

        addRowItem = popupMenu.add("Add A Row");
        addRowItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowItemActionPerformed(evt);
            }
        });

        popupMenu.addSeparator();

        copyRowItem = popupMenu.add("Copy A Row");
        copyRowItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyRowItemActionPerformed(evt);
            }
        });

        popupMenu.addSeparator();

        pasteRowItem = popupMenu.add("Paste A Row");
        pasteRowItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteRowItemActionPerformed(evt);
            }
        });

        popupMenu.addSeparator();

        deleteRowItem = popupMenu.add("Delete A Row");
        deleteRowItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteRowItemActionPerformed(evt);
            }
        });

        setTitle("Step 2: Input grid branch parameters");

        DefaultTableModel dataModel = new DefaultTableModel(data, names);
        // Create the table
        jTable1 = new JTable(dataModel);

        TableColumn column = null;
        jTable1.setAutoscrolls(true);
        jScrollPane1.setViewportView(jTable1);

        DefaultTableCellRenderer render = new DefaultTableCellRenderer();
        render.setHorizontalAlignment(JLabel.CENTER);

        jTable1.setDefaultRenderer(Object.class, render);
        jTable1.setToolTipText("Power Grid Line Parameters Table");

        // Set the component to show the popup menu
        jTable1.addMouseListener(new MouseAdapter() {
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
    }

    private void addRowItemActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        int iSelectRow = jTable1.getSelectedRow();
        tableModel.insertRow(iSelectRow, blankRowData);

        jTable1.repaint();

        mainFrame.addBranchNumber();
    }

    private void copyRowItemActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        Vector data = tableModel.getDataVector();

        int iSelectRow = jTable1.getSelectedRow();

        Vector row = (Vector) data.elementAt(iSelectRow);
        copyRowVector = (Vector) row.clone();

        jTable1.repaint();
    }

    private void pasteRowItemActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        Vector data = tableModel.getDataVector();
        int iSelectRow = jTable1.getSelectedRow();

        Vector selectRow = (Vector) data.elementAt(iSelectRow);
        for (int i = 0; i < selectRow.size(); i++) {
            selectRow.set(i, copyRowVector.get(i));
        }

        jTable1.repaint();
    }

    private void deleteRowItemActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        int iSelectRow = jTable1.getSelectedRow();
        tableModel.removeRow(iSelectRow);

        jTable1.repaint();
        mainFrame.deleteBranchNumber();
    }

    public void loadBlankData() {
        DefaultTableModel blankDataModel = new DefaultTableModel(blankData, names);

        jTable1.setModel(blankDataModel);
    }

    public void loadData(Object[][] loadData) {
        DefaultTableModel loadDataModel = new DefaultTableModel(loadData, names);

        jTable1.setModel(loadDataModel);
        jTable1.setAutoscrolls(true);

        TableColumn column = null;
        jTable1.setAutoscrolls(true);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < names.length; i++) {
            column = jTable1.getColumnModel().getColumn(i);

            column.setPreferredWidth(120);
        }
        jScrollPane1.setViewportView(jTable1);
    }

    public void addRowsBlankData(int iRow) {
        DefaultTableModel tableModel = new DefaultTableModel(blankData, names);

        for (int i = 1; i < iRow; i++) {
            tableModel.insertRow(1, blankRowData);
        }

        jTable1.setModel(tableModel);
        jTable1.repaint();

    }

    public Object[][] saveData() {
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        int iRowCount = tableModel.getRowCount();
        int iColCount = tableModel.getColumnCount();

        Object[][] returnData = new Object[iRowCount][iColCount];

        for (int i = 0; i < iRowCount; i++) {
            for (int j = 0; j < iColCount; j++) {
                returnData[i][j] = tableModel.getValueAt(i, j);
            }
        }

        mainFrame.setdBranchNumber(iRowCount);
        return returnData;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        CancelButton = new javax.swing.JButton();
        NextButton = new javax.swing.JButton();
        PrevButton = new javax.swing.JButton();
        DataVerifyButton = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setFont(new java.awt.Font("Arial", 0, 12));

        jScrollPane1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jTable1.setFont(new java.awt.Font("Arial", 0, 12));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 529, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 182, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        CancelButton.setFont(new java.awt.Font("Arial", 0, 12));
        CancelButton.setText("Cancel");
        CancelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        NextButton.setFont(new java.awt.Font("Arial", 0, 12));
        NextButton.setText("Next >>");
        NextButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        NextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextButtonActionPerformed(evt);
            }
        });

        PrevButton.setFont(new java.awt.Font("Arial", 0, 12));
        PrevButton.setText("<< Prev");
        PrevButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        PrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevButtonActionPerformed(evt);
            }
        });

        DataVerifyButton.setFont(new java.awt.Font("Arial", 0, 12));
        DataVerifyButton.setText("Data Verification");
        DataVerifyButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DataVerifyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DataVerifyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(66, 66, 66)
                .addComponent(DataVerifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(117, 117, 117)
                .addComponent(CancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                .addGap(27, 27, 27)
                .addComponent(PrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                .addGap(26, 26, 26)
                .addComponent(NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)
                .addGap(19, 19, 19))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CancelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(DataVerifyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(13, 13, 13))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public String DataVerify() {
        String strMessage = "";

        // Verify each parameter is greater than 0
        // Step2: Branch parameters
        // "Line name",  "From", "To", "LineCap (MW)", "Reactance (ohm)"
        DefaultTableModel tableModel = (DefaultTableModel) jTable1.getModel();
        int iRowCount = tableModel.getRowCount();
        int iColCount = tableModel.getColumnCount();

        Object[][] verifyData = new Object[iRowCount][iColCount];

        for (int i = 0; i < iRowCount; i++) {
            for (int j = 0; j < iColCount; j++) {
                verifyData[i][j] = tableModel.getValueAt(i, j);
            }
        }

        for (int i = 0; i < verifyData.length; i++) {
            if (Integer.parseInt(verifyData[i][1].toString()) < 0) {
                strMessage += "The " + i + "th branch in From column is not bigger than 0\n";
            }

            if (Integer.parseInt(verifyData[i][2].toString()) < 0) {
                strMessage += "The " + i + "th branch in To column is not bigger than 0\n";
            }

            if (Double.parseDouble(verifyData[i][3].toString()) < 0.0) {
                strMessage += "The " + i + "th branch in LineCap column is not bigger than 0.0\n";
            }

            if (Double.parseDouble(verifyData[i][4].toString()) < 0.0) {
                strMessage += "The " + i + "th branch in Reactance column is not bigger than 0.0\n";
            }
        }

        //Check if all the branches are in one area
        int iBranch = verifyData.length;
        int[][] iBranchNode = new int[iBranch][3]; //0->left bus, 1->right bus, 2->area no 
        int[][] iNode = new int[2 * iBranch][2];  //0->bus no, 1->area no
        for (int i = 0; i < iBranch * 2; i++) {
            iNode[i][0] = -1;
            iNode[i][1] = -1;
        }

        // Count total bus number
        int iNodeCount = 0;
        for (int i = 0; i < iBranch; i++) {
            iBranchNode[i][0] = Integer.parseInt(verifyData[i][1].toString());
            iBranchNode[i][1] = Integer.parseInt(verifyData[i][2].toString());
            iBranchNode[i][2] = -1;

            boolean bFound = false;
            if (iNodeCount > 0) {
                for (int j = 0; j < iNodeCount; j++) {
                    if (iNode[j][0] == iBranchNode[i][0]) {
                        bFound = true;
                        break;
                    }
                }
            }

            if (!bFound) {
                iNode[iNodeCount][0] = iBranchNode[i][0];
                iNodeCount++;
            }

            bFound = false;
            if (iNodeCount > 0) {
                for (int j = 0; j < iNodeCount; j++) {
                    if (iNode[j][0] == iBranchNode[i][1]) {
                        bFound = true;
                        break;
                    }
                }
            }

            if (!bFound) {
                iNode[iNodeCount][0] = iBranchNode[i][1];
                iNodeCount++;
            }
        }

        // Check if two nodes of one branch is the same
        for (int i = 0; i < iBranch; i++) {
            if (iBranchNode[i][0] == iBranchNode[i][1]) {
                strMessage += "The" + i + "th branch has two same nodes!\n";
            }
        }

        // Check if two or more branches between two nodes 
        for (int i = 0; i < iBranch; i++) {
            int iSmallNodeNo = 0;
            int iBigNodeNo = 0;

            if (iBranchNode[i][0] < iBranchNode[i][1]) {
                iSmallNodeNo = iBranchNode[i][0];
                iBigNodeNo = iBranchNode[i][1];
            }

            for (int j = i + 1; j < iBranch; j++) {
                int iSmallNodeNo2 = 0;
                int iBigNodeNo2 = 0;

                if (iBranchNode[j][0] < iBranchNode[j][1]) {
                    iSmallNodeNo2 = iBranchNode[j][0];
                    iBigNodeNo2 = iBranchNode[j][1];
                }

                if ((iSmallNodeNo == iSmallNodeNo2) && (iBigNodeNo == iBigNodeNo2)) {
                    // error when the strMessage to big for the dialog
                    // so we cut if off to 2048
                    if (strMessage.length() < 2048) {
                        strMessage += "More than one branch between bus " + iSmallNodeNo + " and "
                                + iBigNodeNo + ": " + i + "th and " + j + "th branch!\n";
                    }
                }
            }
        }

//        System.out.println("iNode:"+Arrays.deepToString(iNode));
//        System.out.println("iBranchNode:"+Arrays.deepToString(iBranchNode));
        // Get bus connect branchs
        int[][] NodeConnection = new int[iNodeCount][];
        int[] iTempBranchArray = new int[iBranch];
        for (int i = 0; i < iNodeCount; i++) { 
            int iNodeNo = iNode[i][0];
            int iNodeConnectBranchNumber = 0;

            for (int j = 0; j < iBranch; j++) {
                if ((iNodeNo == iBranchNode[j][0]) || (iNodeNo == iBranchNode[j][1])) {
                    iTempBranchArray[iNodeConnectBranchNumber] = j;
                    iNodeConnectBranchNumber++;
                }
            }

            if (iNodeConnectBranchNumber > 0) {
                int[] iTemp = new int[iNodeConnectBranchNumber];
                for (int j = 0; j < iNodeConnectBranchNumber; j++) {
                    iTemp[j] = iTempBranchArray[j];
                }
                NodeConnection[i] = iTemp;
            }
        }

//        System.out.println("NodeConnection:"+Arrays.deepToString(NodeConnection));
        
        int iAreaNumber = 0;
        int[] iTempNodeNo = new int[iNodeCount];

        for (int i = 0; i < iNodeCount; i++) {
            int iNodeNo = iNode[i][0];
            int iTempNodeNumber = 0;

            if (iNode[i][1] == -1) {// bus not searched
                iAreaNumber++;
                iNode[i][1] = iAreaNumber;

                int iNodeConnectBranchNumber = NodeConnection[i].length;
                for (int j = 0; j < iNodeConnectBranchNumber; j++) {
                    int iBranchIndex = NodeConnection[i][j];

                    if (iBranchNode[iBranchIndex][2] == -1) { // branch not searched
                        iBranchNode[iBranchIndex][2] = iAreaNumber;

                        int newNode;
                        if (iNodeNo != iBranchNode[iBranchIndex][0]) {
                            newNode = iBranchNode[iBranchIndex][0];
                        } else {
                            newNode = iBranchNode[iBranchIndex][1];
                        }

                        boolean bInTempNodeNoArray = false;
                        for (int iTemp = 0; iTemp < iTempNodeNumber; iTemp++) {
                            if (iTempNodeNo[iTemp] == newNode) {
                                bInTempNodeNoArray = true;
                                break;
                            }
                        }

                        if (!bInTempNodeNoArray) {
                            for (int ii = 0; ii < iNodeCount; ii++) {
                                if ((iNode[ii][0] == newNode) && (iNode[ii][1] == -1)) { // add new connect bus to search
                                    iTempNodeNo[iTempNodeNumber] = newNode;
                                    iTempNodeNumber++;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (iTempNodeNumber > 0) { // search all connect nodes, branches
                    int iStartIndex = 0;

                    while (iStartIndex < iTempNodeNumber) {
                        int NodeNo = iTempNodeNo[iStartIndex];
                        iStartIndex++;

                        for (int ii = 0; ii < iNodeCount; ii++) {
                            if (iNode[ii][0] == NodeNo) {
                                iNode[ii][1] = iAreaNumber;
                                int iNodeConnectBranchNumber2 = NodeConnection[ii].length;
                                for (int j = 0; j < iNodeConnectBranchNumber2; j++) {
                                    int iBranchIndex = NodeConnection[ii][j];

                                    if (iBranchNode[iBranchIndex][2] == -1) { // branch not searched
                                        iBranchNode[iBranchIndex][2] = iAreaNumber;

                                        int newNode;
                                        if (NodeNo != iBranchNode[iBranchIndex][0]) {
                                            newNode = iBranchNode[iBranchIndex][0];
                                        } else {
                                            newNode = iBranchNode[iBranchIndex][1];
                                        }

                                        boolean bInTempNodeNoArray = false;
                                        for (int iTemp = 0; iTemp < iTempNodeNumber; iTemp++) {
                                            if (iTempNodeNo[iTemp] == newNode) {
                                                bInTempNodeNoArray = true;
                                                break;
                                            }
                                        }

                                        if (!bInTempNodeNoArray) {
                                            for (int ii2 = 0; ii2 < iNodeCount; ii2++) {
                                                if ((iNode[ii2][0] == newNode) && (iNode[ii2][1] == -1)) { // add new connect bus to search
                                                    iTempNodeNo[iTempNodeNumber] = newNode;
                                                    iTempNodeNumber++;
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Output other area's nodes, branches for error message
        if (iAreaNumber > 1) {
            for (int i = 2; i <= iAreaNumber; i++) {
                String strNode = "";
                String strBranch = "";

                for (int iNodeIndex = 0; iNodeIndex < iNodeCount; iNodeIndex++) {
                    if (iNode[iNodeIndex][1] == i) {
                        strNode += "Node " + iNode[iNodeIndex][0] + ", ";
                    }
                }

                for (int iBranchIndex = 0; iBranchIndex < iBranch; iBranchIndex++) {
                    if (iBranchNode[iBranchIndex][2] == i) {
                        strBranch += "Branch " + iBranchIndex + ", ";
                    }
                }

                strMessage += strNode;
                strMessage += strBranch;
                strMessage += "are in area " + i + "!\n";
            }
        }
        return strMessage;
    }

    private void DataVerifyButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String strErrorMessage = DataVerify();
        if (!strErrorMessage.isEmpty()) {
            JOptionPane.showMessageDialog(this, strErrorMessage, "Case Data Verification Message", JOptionPane.ERROR_MESSAGE);
        } else {
            String strMessage = "Case data verify ok!";
            JOptionPane.showMessageDialog(this, strMessage, "Case Data Verification Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        setVisible(false);
    }
    
    private void PrevButtonActionPerformed(java.awt.event.ActionEvent evt) {
            this.setVisible(false);
            mainFrame.activeConfig1();
    }
    
    private void NextButtonActionPerformed(java.awt.event.ActionEvent evt) {
        this.setVisible(false);
        mainFrame.activeConfig4();
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton DataVerifyButton;
    private javax.swing.JButton NextButton;
    private javax.swing.JButton PrevButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    private Vector copyRowVector;
    private javax.swing.JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem addRowItem, copyRowItem, pasteRowItem, deleteRowItem;

    private AMESFrame mainFrame;

    final Object[] blankRowData = {"Branch Name", 0, 0, 0, 0};

    final Object[][] blankData = {{"Branch Name", 0, 0, 0, 0}};

    Object[][] data = {
        {"Branch1", 1, 2, 250, 0.0281},
        {"Branch2", 1, 4, 150, 0.0304},
        {"Branch3", 1, 5, 400, 0.0064},
        {"Branch4", 2, 3, 350, 0.0108},
        {"Branch5", 3, 4, 240, 0.0297},
        {"Branch6", 4, 5, 240, 0.0297}
    };

    String[] names = {
        "Branch Name", "From", "To", "MaxCap (MWs)", "Reactance (ohms)"
    };
}
