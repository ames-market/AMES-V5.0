

 /*
 * PowerGridConfigure4.java
 *
 * Created on June 5, 2007, 9:56 PM
 */
package AMESGUIFrame;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class PowerGridConfigure4 extends javax.swing.JFrame {

    /**
     * Creates new form PowerGridConfigure4
     *
     * @param frame
     */
    public PowerGridConfigure4(AMESFrame frame) {
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

        setTitle("Step 3: Input GenCo parameters");

        DefaultTableModel dataModel = new DefaultTableModel(data, names);
        // Create the table
        jTable1 = new JTable(dataModel);

        TableColumn column = null;
        jTable1.setAutoscrolls(true);
        jScrollPane1.setViewportView(jTable1);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);

        jTable1.setDefaultRenderer(Object.class, renderer);
        jTable1.setToolTipText("GenCo Parameters Table");

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
                    setIcon((ImageIcon) value);
                } else {
                    setText((value == null) ? "" : value.toString());
                }
                setBorder(UIManager.getBorder("TableHeader.cellBorder"));
                setHorizontalAlignment(JLabel.CENTER);
                return this;
            }
        };

        column = jTable1.getColumnModel().getColumn(CONSTC_COLUMN_INDEX);
        column.setHeaderRenderer(iconHeaderRender);
        constbIcon = new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constb.gif"));
        column.setHeaderValue(constbIcon);

        column = jTable1.getColumnModel().getColumn(CAPU_COLUMN_INDEX);
        column.setHeaderRenderer(iconHeaderRender);
        capuIcon = new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capu.gif"));
        column.setHeaderValue(capuIcon);

        column = jTable1.getColumnModel().getColumn(CAPI_COLUMN_INDEX);
        column.setHeaderRenderer(iconHeaderRender);
        caplIcon = new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/capl.gif"));
        column.setHeaderValue(caplIcon);

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

        mainFrame.addGenNumber();
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
        mainFrame.deleteGenNumber();
    }

    public void loadBlankData() {
        DefaultTableModel blankDataModel = new DefaultTableModel(blankData, names);

        jTable1.setModel(blankDataModel);
    }

    public void loadData(Object[][] loadData) {
        DefaultTableModel loadDataModel = new DefaultTableModel(loadData, names);

        jTable1.setModel(loadDataModel);

        TableColumn column = null;
        jTable1.setAutoscrolls(true);
        jScrollPane1.setViewportView(jTable1);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);

        jTable1.setDefaultRenderer(Object.class, renderer);
        jTable1.setToolTipText("GenCo Parameters Table");

        JTableHeader header = jTable1.getTableHeader();

        column = jTable1.getColumnModel().getColumn(CONSTC_COLUMN_INDEX);
        column.setHeaderRenderer(iconHeaderRender);
        column.setHeaderValue(constbIcon);

        column = jTable1.getColumnModel().getColumn(CAPI_COLUMN_INDEX);
        column.setHeaderRenderer(iconHeaderRender);
        column.setHeaderValue(caplIcon);

        column = jTable1.getColumnModel().getColumn(CAPU_COLUMN_INDEX);
        column.setHeaderRenderer(iconHeaderRender);
        column.setHeaderValue(capuIcon);

        jTable1.setAutoscrolls(true);
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < names.length; i++) {
            column = jTable1.getColumnModel().getColumn(i);

            column.setPreferredWidth(120);
        }
    }

    public void addRowsBlankData(int iRow) {
        DefaultTableModel tableModel = new DefaultTableModel(blankData, names);

        for (int i = 1; i < iRow; i++) {
            tableModel.insertRow(i - 1, blankRowData);
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

        mainFrame.setdGenNumber(iRowCount);
        return returnData;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        CancelButton = new javax.swing.JButton();
        PrevButton = new javax.swing.JButton();
        NextButton = new javax.swing.JButton();
        DataVerifyButton = new javax.swing.JButton();

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 641, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(38, 38, 38)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        PrevButton.setFont(new java.awt.Font("Arial", 0, 12));
        PrevButton.setText("<< Prev");
        PrevButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        PrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevButtonActionPerformed(evt);
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
                .addGap(74, 74, 74)
                .addComponent(DataVerifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 265, Short.MAX_VALUE)
                .addComponent(CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PrevButton, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(PrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CancelButton)
                    .addComponent(DataVerifyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }//GEN-END:initComponents
    
    public String DataVerify() {
        String strMessage = "";

        // Verify each parameter is greater than 0
        // Step4: GenCo parameters
        //  "GenCo Name", "ID", "atBus", "SCost ($/H)", "a ($/H)", "b ($/MWh)", "c ($/MW2h)", "PMin (MW)", "PMax (MW)", "NS", "InitMoney ($)"
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
                strMessage += "The " + (i+1) + "th generator in ID column is not bigger than 0\n";
            }

            if (Integer.parseInt(verifyData[i][2].toString()) < 0) {
                strMessage += "The " + (i+1) + "th generator in atBus column is not bigger than 0\n";
            }

            if (Double.parseDouble(verifyData[i][3].toString()) < 0.0) {
                strMessage += "The " + (i+1) + "th generator in SCost column is not bigger than 0.0\n";
            }

            if (Double.parseDouble(verifyData[i][4].toString()) < 0.0) {
                strMessage += "The " + (i+1) + "th generator in a column is not >= 0.0\n";
            }

            if (Double.parseDouble(verifyData[i][5].toString()) < 0.0) {
                strMessage += "The " + (i+1) + "th generator in b column is not >= 0.0\n";
            }

            if (Double.parseDouble(verifyData[i][6].toString()) <= 0.0) {
                strMessage += "The " + (i+1) + "th generator in c column is not > 0.0\n";
            }
            
            if (Double.parseDouble(verifyData[i][7].toString()) < 1) {
                strMessage += "The " + (i+1) + "th generator in NS column is not >= 1\n";
            }

            if (Double.parseDouble(verifyData[i][8].toString()) < 0.0) {
                strMessage += "The " + (i+1) + "th generator in PMin column is not >= 0.0\n";
            }
            
            if (Double.parseDouble(verifyData[i][9].toString()) < 0.0) {
                strMessage += "The " + (i+1) + "th generator in PMax column is not >= 0.0\n";
            }
            
            if (Double.parseDouble(verifyData[i][9].toString()) < Double.parseDouble(verifyData[i][8].toString())) {
                strMessage += "The " + (i+1) + "th generator in PMax column is not >= PMin\n";
                
            }


            if (Double.parseDouble(verifyData[i][10].toString()) < 0.0) {
                strMessage += "The " + (i+1) + "th generator in InitMoney column is not bigger than 0.0\n";
            }

            double db = Double.parseDouble(verifyData[i][5].toString());
            double dc = Double.parseDouble(verifyData[i][6].toString());
            double dPMin = Double.parseDouble(verifyData[i][7].toString());

            if ((db + 2.0 * dc * dPMin) <= 0.0) {
                strMessage += "The " + (i+1) + "th generator b+2*c*PMin is not bigger than 0.0\n";
            }

        }

        return strMessage;
    }

    private void DataVerifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DataVerifyButtonActionPerformed
        String strErrorMessage = DataVerify();
        if (!strErrorMessage.isEmpty()) {
            JOptionPane.showMessageDialog(this, strErrorMessage, "Case Data Verification Message", JOptionPane.ERROR_MESSAGE);
        } else {
            String strMessage = "Case data verify ok!";
            JOptionPane.showMessageDialog(this, strMessage, "Case Data Verification Message", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_DataVerifyButtonActionPerformed
    
    private void NextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextButtonActionPerformed
        this.setVisible(false);
        mainFrame.activeLearnOption1();
    }//GEN-LAST:event_NextButtonActionPerformed
    
    private void PrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevButtonActionPerformed
        this.setVisible(false);
        mainFrame.activeConfig2();
    }//GEN-LAST:event_PrevButtonActionPerformed
    
    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_CancelButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton DataVerifyButton;
    private javax.swing.JButton NextButton;
    private javax.swing.JButton PrevButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

    private TableCellRenderer iconHeaderRender;
    private ImageIcon caplIcon;
    private ImageIcon capuIcon;
    private ImageIcon constbIcon;
    private final int CONSTC_COLUMN_INDEX = 6;
    private final int CAPI_COLUMN_INDEX = 8;
    private final int CAPU_COLUMN_INDEX = 9;

    private Vector copyRowVector;
    private javax.swing.JPopupMenu popupMenu = new JPopupMenu();
    private JMenuItem addRowItem, copyRowItem, pasteRowItem, deleteRowItem;

    private AMESFrame mainFrame;

    Object[][] data = {
        {"Gen1", 1, 1, 1600, 0, 14, 0.005, 10, 0, 110, 1000000, false},
        {"Gen2", 2, 1, 1200, 0, 15, 0.006, 10, 0, 100, 1000000, false},
        {"Gen3", 3, 3, 8500, 0, 25, 0.010, 10, 0, 520, 1000000, false},
        {"Gen4", 4, 4, 1000, 0, 30, 0.012, 10, 0, 200, 1000000, false},
        {"Gen5", 5, 5, 5400, 0, 10, 0.007, 10, 0, 600, 1000000, false}
    };
    final Object[] blankRowData = {"GenCo Name", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false};

    final Object[][] blankData = {{"GenCo Name", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, false}};

    String[] names = {
        "GenCo Name", "ID", "atBus", "SCost ($/H)", "a ($/h)", "b ($/MWh)", "c ($/MW2h)", "NS", "PMin (MW)", "PMax (MW)", "InitMoney ($)", "Alert Generator(?)"
    };
}
