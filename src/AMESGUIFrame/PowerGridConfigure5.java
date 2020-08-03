/*
 * PowerGridConfigure5.java
 *
 * Created on May 28, 2007, 10:39 PM
 */
package AMESGUIFrame;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;

public class PowerGridConfigure5 extends javax.swing.JFrame {

    /**
     * Creates new form PowerGridConfigure5
     *
     * @param frame
     */
    public PowerGridConfigure5(AMESFrame frame) {
        mainFrame = frame;
        initComponents();

        CheckAllFixedLoadItem = popupMenuFlagTable.add("Check all fixed load");
        CheckAllFixedLoadItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckAllFixedLoaItemActionPerformed(evt);
            }
        });

        popupMenuFlagTable.addSeparator();

        unCheckAllFixedLoadItem = popupMenuFlagTable.add("UnCheck all fixed load");
        unCheckAllFixedLoadItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unCheckAllFixedLoaItemActionPerformed(evt);
            }
        });

        popupMenuFlagTable.addSeparator();

        CheckAllPriceSensitiveItem = popupMenuFlagTable.add("Check all price-sensitive demand");
        CheckAllPriceSensitiveItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CheckAllPriceSensitiveItemActionPerformed(evt);
            }
        });

        popupMenuFlagTable.addSeparator();

        unCheckAllPriceSensitiveItem = popupMenuFlagTable.add("UnCheck all price-sensitive demand");
        unCheckAllPriceSensitiveItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unCheckAllPriceSensitiveItemActionPerformed(evt);
            }
        });

        popupMenuFlagTable.addSeparator();

        copyRowFixedLoadItem = popupMenuFixedLoadTable.add("Copy A Row");
        copyRowFixedLoadItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyRowItemFixedLoadActionPerformed(evt);
            }
        });

        popupMenuFixedLoadTable.addSeparator();

        pasteRowFixedLoadItem = popupMenuFixedLoadTable.add("Paste A Row");
        pasteRowFixedLoadItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteRowItemFixedLoadActionPerformed(evt);
            }
        });

        popupMenuFixedLoadTable.addSeparator();

        copyRowPriceSensitiveItem = popupMenuPriceSensitiveTable.add("Copy A Row");
        copyRowPriceSensitiveItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyRowItemPriceSensitiveActionPerformed(evt);
            }
        });

        popupMenuPriceSensitiveTable.addSeparator();

        pasteRowPriceSensitiveItem = popupMenuPriceSensitiveTable.add("Paste A Row");
        pasteRowPriceSensitiveItem.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pasteRowItemPriceSensitiveActionPerformed(evt);
            }
        });

        popupMenuPriceSensitiveTable.addSeparator();

        setTitle("Step 5: Input LSE parameters");

        DefaultTableModel dataModel = new DefaultTableModel(blankData, names);
        // Create the table
        FixedLoadTable = new JTable(dataModel);

        TableColumn column = null;
        FixedLoadTable.setAutoscrolls(true);
        FixedLoadTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.CENTER);
        FixedLoadTable.setDefaultRenderer(Object.class, renderer);

        FixedLoadTable.setToolTipText("LSE Parameters Table");
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane1.setViewportView(FixedLoadTable);

        dataModel = new DefaultTableModel(blankPriceSensitiveDataBenefit, priceSensitiveNamesBenefit);

        // Create the table
        PriceSensitiveTable = new JTable(dataModel);

        PriceSensitiveTable.setAutoscrolls(true);
        PriceSensitiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        PriceSensitiveTable.setDefaultRenderer(Object.class, renderer);

        PriceSensitiveTable.setToolTipText("LSE Parameters Table");
        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jScrollPane2.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        jScrollPane2.setViewportView(PriceSensitiveTable);

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

//        column=PriceSensitiveTable.getColumnModel().getColumn(CONSTD_COLUMN_INDEX);
//        column.setHeaderRenderer(iconHeaderRender);
//        constdIcon=new javax.swing.ImageIcon(AMESFrame.class.getResource("/resources/constd.gif"));
//        column.setHeaderValue(constdIcon);
        FlagTable.setToolTipText("First row is for fixed demand and second row is for price-sensitive demand.");
        // Set the component to show the popup menu
        FlagTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenuFlagTable.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenuFlagTable.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        // Set the component to show the popup menu
        PriceSensitiveTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenuPriceSensitiveTable.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenuPriceSensitiveTable.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        FixedLoadTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenuFixedLoadTable.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }

            public void mouseReleased(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    popupMenuFixedLoadTable.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        iCurrentLSEIndex = 0;
    }

    private void CheckAllFixedLoaItemActionPerformed(java.awt.event.ActionEvent evt) {
        for (int j = 0; j < 24; j++) {
            lseDemandFlag[0][j] = true;
        }

        FlagTable.repaint();
    }

    private void unCheckAllFixedLoaItemActionPerformed(java.awt.event.ActionEvent evt) {
        for (int j = 0; j < 24; j++) {
            lseDemandFlag[0][j] = false;
        }

        FlagTable.repaint();
    }

    private void CheckAllPriceSensitiveItemActionPerformed(java.awt.event.ActionEvent evt) {
        for (int j = 0; j < 24; j++) {
            lseDemandFlag[1][j] = true;
        }

        FlagTable.repaint();
    }

    private void unCheckAllPriceSensitiveItemActionPerformed(java.awt.event.ActionEvent evt) {
        for (int j = 0; j < 24; j++) {
            lseDemandFlag[1][j] = false;
        }

        FlagTable.repaint();
    }

    private void copyRowItemPriceSensitiveActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) PriceSensitiveTable.getModel();
        Vector dataConstantLoad = tableModel.getDataVector();

        int iSelectRow = PriceSensitiveTable.getSelectedRow();

        Vector row = (Vector) dataConstantLoad.elementAt(iSelectRow);
        copyRowVector = (Vector) row.clone();

        PriceSensitiveTable.repaint();
    }

    private void pasteRowItemPriceSensitiveActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) PriceSensitiveTable.getModel();
        Vector dataConstantLoad = tableModel.getDataVector();
        int iSelectRow = PriceSensitiveTable.getSelectedRow();

        Vector selectRow = (Vector) dataConstantLoad.elementAt(iSelectRow);
        for (int i = 0; i < selectRow.size(); i++) {
            selectRow.set(i, copyRowVector.get(i));
        }

        PriceSensitiveTable.repaint();
    }

    private void copyRowItemFixedLoadActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) FixedLoadTable.getModel();
        Vector dataConstantLoad = tableModel.getDataVector();

        int iSelectRow = FixedLoadTable.getSelectedRow();

        Vector row = (Vector) dataConstantLoad.elementAt(iSelectRow);
        copyRowVector = (Vector) row.clone();

        FixedLoadTable.repaint();
    }

    private void pasteRowItemFixedLoadActionPerformed(java.awt.event.ActionEvent evt) {
        DefaultTableModel tableModel = (DefaultTableModel) FixedLoadTable.getModel();
        Vector dataConstantLoad = tableModel.getDataVector();
        int iSelectRow = FixedLoadTable.getSelectedRow();

        Vector selectRow = (Vector) dataConstantLoad.elementAt(iSelectRow);
        for (int i = 0; i < selectRow.size(); i++) {
            selectRow.set(i, copyRowVector.get(i));
        }

        FixedLoadTable.repaint();
    }

    public void loadBlankData() {
    }

    class HybridTableModel extends AbstractTableModel {

        public int getColumnCount() {
            return priceHybridNames.length;
        }

        public int getRowCount() {
            return lseDemandFlag.length;
        }

        public String getColumnName(int col) {
            return priceHybridNames[col];
        }

        public Object getValueAt(int row, int col) {
            return lseDemandFlag[row][col];
        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        public void setValueAt(Object value, int row, int col) {
            lseDemandFlag[row][col] = value;
            fireTableCellUpdated(row, col);
        }

    };

    public void loadData(Object[][] lseFixedData, Object[][][] lsePriceSensitiveData, Object[][] lseHybridData) {

        bDataLoad = false;

        LSENameComboBox.removeAllItems();
        lseFixedDeamndList.clear();
        lsePriceSensitiveDemandList.clear();
        lseHybridDemandList.clear();

        int iRow = lseFixedData.length;
        int iCol = lseFixedData[0].length;

        for (int i = 0; i < iRow; i++) {
            Object[] FixedDeamndArray = new Object[iCol];

            for (int j = 0; j < iCol; j++) {
                FixedDeamndArray[j] = lseFixedData[i][j];
            }

            lseFixedDeamndList.add(FixedDeamndArray);
        }

        if (lsePriceSensitiveData != null) {
            iRow = lsePriceSensitiveData.length;
            iCol = lsePriceSensitiveData[0][0].length;
            for (int i = 0; i < iRow; i++) {
                Object[][] PriceDeamndArray = new Object[24][iCol];

                for (int h = 0; h < 24; h++) {
                    for (int j = 0; j < iCol; j++) {
                        PriceDeamndArray[h][j] = lsePriceSensitiveData[i][h][j];
                    }
                }

                lsePriceSensitiveDemandList.add(PriceDeamndArray);
            }
        }

        if (lseHybridData != null) {
            iRow = lseHybridData.length;
            iCol = lseHybridData[0].length;
            for (int i = 0; i < iRow; i++) {
                Object[] HybridDeamndArray = new Object[iCol];

                for (int j = 0; j < iCol; j++) {
                    HybridDeamndArray[j] = lseHybridData[i][j];
                }
                lseHybridDemandList.add(HybridDeamndArray);
                LSENameComboBox.addItem(lseHybridData[i][0].toString());
            }
        }

        if (LSENameComboBox.getItemCount() < 1) {
            return;
        }
        LSENameComboBox.setSelectedIndex(0);
        lseName = (String) LSENameComboBox.getItemAt(0);
        SelectLSE(lseName);

        bDataLoad = true;
    }

    public void addRowsBlankData(int iRow) {
    }

    public void saveTableDataToList() {
        String selectedName = (String) LSENameComboBox.getSelectedItem();

        for (int i = 0; i < lseHybridDemandList.size(); i++) {
            Object[] temp = (Object[]) lseHybridDemandList.get(i);
            if (selectedName.equalsIgnoreCase(temp[0].toString())) {
                String strTemp = IDTextField.getText();
                iID = Integer.parseInt(strTemp);
                temp[1] = iID;
                strTemp = AtNodeTextField.getText();
                iAtNode = Integer.parseInt(strTemp);
                temp[2] = iAtNode;

                for (int j = 0; j < 24; j++) {
                    int Flag = 0;
                    if (Boolean.parseBoolean(lseDemandFlag[0][j].toString())) {
                        Flag += 1;
                    }
                    if (Boolean.parseBoolean(lseDemandFlag[1][j].toString())) {
                        Flag += 2;
                    }

                    temp[j + 3] = Flag;
                }

                for (int j = 0; j < lseFixedDeamndList.size(); j++) {
                    Object[] tempFixed = (Object[]) lseFixedDeamndList.get(j);
                    if (selectedName.equalsIgnoreCase(tempFixed[0].toString())) {
                        tempFixed[1] = iID;
                        tempFixed[2] = iAtNode;
                        DefaultTableModel tableModelFixedLoad = (DefaultTableModel) FixedLoadTable.getModel();
                        for (int k = 0; k < 24; k++) {
                            tempFixed[k + 3] = tableModelFixedLoad.getValueAt(0, k);
                        }
                    }
                }

                for (int j = 0; j < lsePriceSensitiveDemandList.size(); j++) {
                    Object[][] tempPrice = (Object[][]) lsePriceSensitiveDemandList.get(j);
                    if (selectedName.equalsIgnoreCase(tempPrice[0][0].toString())) {
                        DefaultTableModel tableModelPriceDemand = (DefaultTableModel) PriceSensitiveTable.getModel();

                        for (int h = 0; h < 24; h++) {
                            tempPrice[h][1] = iID;
                            tempPrice[h][2] = iAtNode;
                            for (int k = 0; k < 4; k++) {
                                tempPrice[h][k + 3] = tableModelPriceDemand.getValueAt(h, k);
                            }
                        }
                    }
                }
            }
        }
    }

    public void saveData() {
        saveTableDataToList();

        mainFrame.lseData = TransListToArray(lseFixedDeamndList);
        mainFrame.lsePriceSensitiveDemand = TransPriceSensitiveListToArray(lsePriceSensitiveDemandList);
        mainFrame.lseHybridDemand = TransListToArray(lseHybridDemandList);
        mainFrame.setLSENumber(lseHybridDemandList.size());

    }

    private Object[][] TransListToArray(ArrayList list) {
        int iRow = list.size();
        Object[][] obj;
        int iCol = 0;

        if (iRow < 1) {
            obj = null;
        } else {
            iCol = ((Object[]) list.get(0)).length;
            obj = new Object[iRow][iCol];
        }

        for (int i = 0; i < iRow; i++) {
            Object[] temp = (Object[]) list.get(i);

            for (int j = 0; j < iCol; j++) {
                obj[i][j] = temp[j];
            }
        }

        return obj;
    }

    private Object[][][] TransPriceSensitiveListToArray(ArrayList list) {
        int iRow = list.size();
        Object[][][] obj;
        int iCol = 0;

        if (iRow < 1) {
            obj = null;
        } else {
            iCol = ((Object[][]) list.get(0))[0].length;
            obj = new Object[iRow][24][iCol];
        }

        for (int i = 0; i < iRow; i++) {
            Object[][] temp = (Object[][]) list.get(i);

            for (int h = 0; h < 24; h++) {
                for (int j = 0; j < iCol; j++) {
                    obj[i][h][j] = temp[h][j];
                }
            }
        }

        return obj;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        LSENameComboBox = new javax.swing.JComboBox();
        AddlButton = new javax.swing.JButton();
        DeletelButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        IDTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        AtNodeTextField = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        FixedLoadTable = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        FlagTable = new javax.swing.JTable();
        jLabel6 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        PriceSensitiveTable = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        Prev = new javax.swing.JButton();
        DataVerifyButton = new javax.swing.JButton();
        NextButton = new javax.swing.JButton();
        CancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setFont(new java.awt.Font("Arial", 1, 12));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("LSE name:");
        jLabel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        LSENameComboBox.setEditable(true);
        LSENameComboBox.setFont(new java.awt.Font("Arial", 0, 12));
        LSENameComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LSENameComboBoxActionPerformed(evt);
            }
        });

        AddlButton.setFont(new java.awt.Font("Arial", 0, 12));
        AddlButton.setText("Add");
        AddlButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        AddlButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddlButtonActionPerformed(evt);
            }
        });

        DeletelButton.setFont(new java.awt.Font("Arial", 0, 12));
        DeletelButton.setText("Delete");
        DeletelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        DeletelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                DeletelButtonActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Arial", 1, 12));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("ID No:");
        jLabel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        IDTextField.setFont(new java.awt.Font("Tahoma", 0, 12));
        IDTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        IDTextField.setText("0");

        jLabel3.setFont(new java.awt.Font("Arial", 1, 12));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Bus No:");
        jLabel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        AtNodeTextField.setFont(new java.awt.Font("Tahoma", 0, 12));
        AtNodeTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        AtNodeTextField.setText("0");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        FixedLoadTable.setFont(new java.awt.Font("Arial", 0, 12));
        FixedLoadTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(FixedLoadTable);

        jLabel4.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel4.setText("Fixed Demand Values by Hour");
        jLabel4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .addComponent(jLabel4))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        FlagTable.setFont(new java.awt.Font("Arial", 0, 12));
        FlagTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(FlagTable);

        jLabel6.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel6.setText("Flag Selection for Existence of Fixed and/or Price-Sensitive Demand by Hour");
        jLabel6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel6))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, 21, Short.MAX_VALUE)
                .addGap(11, 11, 11)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED)));

        PriceSensitiveTable.setFont(new java.awt.Font("Arial", 0, 12));
        PriceSensitiveTable.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(PriceSensitiveTable);

        jLabel5.setFont(new java.awt.Font("Arial", 1, 12)); // NOI18N
        jLabel5.setText("Price-Sensitive Demand Function Parameters by Hour");
        jLabel5.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 507, Short.MAX_VALUE)
                    .addComponent(jLabel5))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 101, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(26, 26, 26)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(LSENameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(57, 57, 57)
                                .addComponent(AddlButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(DeletelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(IDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(26, 26, 26)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 101, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AtNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(LSENameComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AddlButton)
                    .addComponent(DeletelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(IDTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AtNodeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        Prev.setFont(new java.awt.Font("Arial", 0, 12));
        Prev.setText("<< Prev");
        Prev.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Prev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PrevActionPerformed(evt);
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

        NextButton.setFont(new java.awt.Font("Arial", 0, 12));
        NextButton.setText("Next >>");
        NextButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        NextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextButtonActionPerformed(evt);
            }
        });

        CancelButton.setFont(new java.awt.Font("Arial", 0, 12));
        CancelButton.setText("Cancel");
        CancelButton.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(DataVerifyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(191, 191, 191)
                .addComponent(CancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(Prev, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15)
                .addComponent(NextButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(38, 38, 38))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(DataVerifyButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(NextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(Prev)
                    .addComponent(CancelButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void LSENameComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LSENameComboBoxActionPerformed
        
        if (!bDataLoad) {
            return;
        }
        int iSelectedIndex = LSENameComboBox.getSelectedIndex();
        // nothing left in ComboBox
        if (LSENameComboBox.getSelectedItem() == null) {
            return;
        }

        String selectedName = (String) LSENameComboBox.getSelectedItem();

        if (lseName.equalsIgnoreCase(selectedName)) {
            return;
        }

        //System.out.println("lseName="+lseName+" selectedName="+selectedName+" iCurrentLSEIndex="+iCurrentLSEIndex+" iSelectedIndex="+iSelectedIndex);
        if (iSelectedIndex < 0) {
            LSENameComboBox.removeItem(lseName);
            LSENameComboBox.insertItemAt(selectedName, iCurrentLSEIndex);
            lseName = selectedName;

            Object[] temp = (Object[]) lseHybridDemandList.get(iCurrentLSEIndex);
            temp[0] = selectedName;
            Object[] tempFixed = (Object[]) lseFixedDeamndList.get(iCurrentLSEFixedDemandIndex);
            tempFixed[0] = selectedName;
            Object[][] tempPrice = (Object[][]) lsePriceSensitiveDemandList.get(iCurrentLSEPriceDemandIndex);

            for (int h = 0; h < 24; h++) {
                tempPrice[h][0] = selectedName;
            }

            return;
        } else {
            iCurrentLSEIndex = iSelectedIndex;
        }

        for (int i = 0; i < lseHybridDemandList.size(); i++) {
            Object[] temp = (Object[]) lseHybridDemandList.get(i);
            if (lseName.equalsIgnoreCase(temp[0].toString())) {
                String strTemp = IDTextField.getText();
                iID = Integer.parseInt(strTemp);
                temp[1] = iID;
                strTemp = AtNodeTextField.getText();
                iAtNode = Integer.parseInt(strTemp);
                temp[2] = iAtNode;

                for (int j = 0; j < 24; j++) {
                    int Flag = 0;
                    if (Boolean.parseBoolean(lseDemandFlag[0][j].toString())) {
                        Flag += 1;
                    }
                    if (Boolean.parseBoolean(lseDemandFlag[1][j].toString())) {
                        Flag += 2;
                    }

                    temp[j + 3] = Flag;
                }

                for (int j = 0; j < lseFixedDeamndList.size(); j++) {
                    Object[] tempFixed = (Object[]) lseFixedDeamndList.get(j);
                    if (lseName.equalsIgnoreCase(tempFixed[0].toString())) {
                        tempFixed[1] = iID;
                        tempFixed[2] = iAtNode;
                        DefaultTableModel tableModelFixedLoad = (DefaultTableModel) FixedLoadTable.getModel();
                        for (int k = 0; k < 24; k++) {
                            tempFixed[k + 3] = tableModelFixedLoad.getValueAt(0, k);
                        }
                    }
                }

                for (int j = 0; j < lsePriceSensitiveDemandList.size(); j++) {
                    Object[][] tempPrice = (Object[][]) lsePriceSensitiveDemandList.get(j);
                    if (lseName.equalsIgnoreCase(tempPrice[0][0].toString())) {
                        for (int h = 0; h < 24; h++) {
                            tempPrice[h][1] = iID;
                            tempPrice[h][2] = iAtNode;
                            DefaultTableModel tableModelPriceDemand = (DefaultTableModel) PriceSensitiveTable.getModel();
                            for (int k = 0; k < 5; k++) {
                                tempPrice[h][k + 3] = tableModelPriceDemand.getValueAt(h, k);
                            }
                        }
                    }
                }
            }
        }

        lseName = selectedName;

        SelectLSE(lseName);
}//GEN-LAST:event_LSENameComboBoxActionPerformed

private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
    setVisible(false);
}//GEN-LAST:event_CancelButtonActionPerformed

private void NextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextButtonActionPerformed
    this.setVisible(false);
    mainFrame.activeSimulationControl();
}//GEN-LAST:event_NextButtonActionPerformed

    public String DataVerify() {
        String strMessage = "";

        saveTableDataToList();

        String name;
        for (int i = 0; i < lseHybridDemandList.size(); i++) {
            Object[] temp = (Object[]) lseHybridDemandList.get(i);
            name = temp[0].toString();

            int ID = Integer.parseInt(temp[1].toString());
            int AtNode = Integer.parseInt(temp[2].toString());

            if (ID < 0) {
                strMessage += name + "'s ID number is not bigger than 0\n";
            }

            if (AtNode < 0) {
                strMessage += name + "'s atBus number is not bigger than 0\n";
            }

            boolean bFixedDemandFound = false;
            Object[] tempFixed = null;
            for (int k = 0; k < lseFixedDeamndList.size(); k++) {
                tempFixed = (Object[]) lseFixedDeamndList.get(k);
                if (name.equalsIgnoreCase(tempFixed[0].toString())) {
                    bFixedDemandFound = true;
                    break;
                }
            }

            boolean bPriceSensitiveDemandFound = false;
            Object[][] tempPrice = null;
            for (int k = 0; k < lsePriceSensitiveDemandList.size(); k++) {
                tempPrice = (Object[][]) lsePriceSensitiveDemandList.get(k);
                if (name.equalsIgnoreCase(tempPrice[0][0].toString())) {
                    bPriceSensitiveDemandFound = true;
                    break;
                }
            }

            boolean bUsePriceSensitiveDemand = false;
            for (int j = 0; j < 24; j++) {
                int Flag = Integer.parseInt(temp[j + 3].toString());

                if ((Flag & 1) == 1) {
                    if (!bFixedDemandFound) {
                        strMessage += name + "'s fixed demand is not found!\n";
                    }
                    //else{
                    //     if(Double.parseDouble(tempFixed[j+3].toString())<0.0)
                    //        strMessage+=name+"'s in "+names[j+3]+" column is not bigger than 0.0\n";
                    //}
                }
                // Check price sensitive demand once if needed
                if (((Flag & 2) == 2) && !bUsePriceSensitiveDemand) {
                    if (!bPriceSensitiveDemandFound) {
                        strMessage += name + "'s price sensitive demand is not found!\n";
                    } else {
                    }

                    bUsePriceSensitiveDemand = true;
                }
            }
        }

        for (int k = 0; k < lsePriceSensitiveDemandList.size(); k++) {
            Object[][] tempPrice = (Object[][]) lsePriceSensitiveDemandList.get(k);
            name = tempPrice[0][0].toString();
            for (int h = 0; h < 24; h++) {
                double d = Double.parseDouble(tempPrice[h][4].toString());
                double e = Double.parseDouble(tempPrice[h][5].toString());
                double f = Double.parseDouble(tempPrice[h][6].toString());
                double slMax = Double.parseDouble(tempPrice[h][7].toString());
                if (mainFrame.getPriceSensitiveDemandFlag() > 0) {
                    if (e <= 0.0) {
                        strMessage += name + "'s at hour " + h + " in e column is not bigger than 0.0\n";
                    }

                    if (f <= 0.0) {
                        strMessage += name + "'s at hour " + h + " in f column is not bigger than 0.0\n";
                    }

                    if (slMax < 0.0) {
                        strMessage += name + "'s at hour " + h + " in SLMax column is not >= 0.0\n";
                    }

                    if (e - f * 2.0 * slMax < 0.0) {
                        strMessage += name + "'s at hour " + h + " e-f*2.0*SLMax is not >= 0.0\n";
                    }
                }

            }
        }

        return strMessage;
    }


private void DataVerifyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DataVerifyButtonActionPerformed
    String strErrorMessage = DataVerify();
    if (!strErrorMessage.isEmpty())
        JOptionPane.showMessageDialog(this, strErrorMessage, "Case Data Verification Message", JOptionPane.ERROR_MESSAGE);
    else{//GEN-LAST:event_DataVerifyButtonActionPerformed
            String strMessage = "Case data verify ok!";
            JOptionPane.showMessageDialog(this, strMessage, "Case Data Verification Message", JOptionPane.INFORMATION_MESSAGE);
        }

    }

    private void PrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PrevActionPerformed
        this.setVisible(false);
        mainFrame.activeLearnOption1();

    }//GEN-LAST:event_PrevActionPerformed

    private void AddlButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddlButtonActionPerformed
        String lseNewName = "LSE00";

        for (int i = 0; i < lseHybridDemandList.size(); i++) {
            Object[] temp = (Object[]) lseHybridDemandList.get(i);
            if (lseNewName.equalsIgnoreCase(temp[0].toString())) {
                SelectLSE("LSE00");
                return;
            }
        }

        Object[] FixedDeamndArray = new Object[27];
        FixedDeamndArray[0] = "LSE00";
        for (int i = 1; i < 27; i++) {
            FixedDeamndArray[i] = 0;
        }

        lseFixedDeamndList.add(FixedDeamndArray);

        Object[][] PriceDeamndArray = new Object[24][7];
        for (int h = 0; h < 24; h++) {
            PriceDeamndArray[h][0] = "LSE00";
            for (int i = 1; i < 7; i++) {
                PriceDeamndArray[h][i] = 0;
            }
        }

        lsePriceSensitiveDemandList.add(PriceDeamndArray);

        Object[] HybridDeamndArray = new Object[27];
        HybridDeamndArray[0] = "LSE00";
        for (int i = 1; i < 27; i++) {
            HybridDeamndArray[i] = 0;
        }

        lseHybridDemandList.add(HybridDeamndArray);

        LSENameComboBox.addItem("LSE00");
        SelectLSE("LSE00");

}//GEN-LAST:event_AddlButtonActionPerformed

    private void DeletelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_DeletelButtonActionPerformed
        int iSelectedIndex = LSENameComboBox.getSelectedIndex();
        String selectedName = (String) LSENameComboBox.getSelectedItem();
        if (selectedName.isEmpty()) {
            return;
        }

        LSENameComboBox.removeItem(selectedName);

        for (int i = 0; i < lseHybridDemandList.size(); i++) {
            Object[] temp = (Object[]) lseHybridDemandList.get(i);
            if (selectedName.equalsIgnoreCase(temp[0].toString())) {
                lseHybridDemandList.remove(i);
                break;
            }
        }

        for (int j = 0; j < lseFixedDeamndList.size(); j++) {
            Object[] tempFixed = (Object[]) lseFixedDeamndList.get(j);
            if (selectedName.equalsIgnoreCase(tempFixed[0].toString())) {
                lseFixedDeamndList.remove(j);
                break;
            }
        }

        for (int j = 0; j < lsePriceSensitiveDemandList.size(); j++) {
            Object[][] tempPrice = (Object[][]) lsePriceSensitiveDemandList.get(j);
            if (selectedName.equalsIgnoreCase(tempPrice[0][0].toString())) {
                lsePriceSensitiveDemandList.remove(j);
                break;
            }
        }

        if (lseHybridDemandList.size() < 1)
            AddlButtonActionPerformed(evt);
        else {
            if (iSelectedIndex < 1) {
                iSelectedIndex = 0;
            } else {
                iSelectedIndex--;
            }

            Object[] temp = (Object[]) lseHybridDemandList.get(iSelectedIndex);
            lseName = temp[0].toString();
            SelectLSE(lseName);
        }
}//GEN-LAST:event_DeletelButtonActionPerformed

    private void SelectLSE(String name) {
        LSENameComboBox.setSelectedItem(name);

        for (int i = 0; i < lseHybridDemandList.size(); i++) {
            Object[] temp = (Object[]) lseHybridDemandList.get(i);
            if (name.equalsIgnoreCase(temp[0].toString())) {
                iID = Integer.parseInt(temp[1].toString());
                iAtNode = Integer.parseInt(temp[2].toString());
                IDTextField.setText(String.valueOf(iID));
                AtNodeTextField.setText(String.valueOf(iAtNode));

                for (int j = 0; j < 24; j++) {
                    int Flag = Integer.parseInt(temp[j + 3].toString());
                    lseDemandFlag[0][j] = false;
                    lseDemandFlag[1][j] = false;

                    if ((Flag & 1) == 1) {
                        lseDemandFlag[0][j] = true;
                    }
                    if ((Flag & 2) == 2) {
                        lseDemandFlag[1][j] = true;
                    }
                }

                HybridTableModel hybridModel = new HybridTableModel();
                FlagTable.setModel(hybridModel);

                TableColumn column = null;
                FlagTable.setAutoscrolls(true);
                FlagTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                for (int j = 0; j < priceHybridNames.length; j++) {
                    column = FlagTable.getColumnModel().getColumn(j);

                    column.setPreferredWidth(60);
                }

                for (int j = 0; j < lseFixedDeamndList.size(); j++) {
                    Object[] tempFixed = (Object[]) lseFixedDeamndList.get(j);
                    if (name.equalsIgnoreCase(tempFixed[0].toString())) {
                        iCurrentLSEFixedDemandIndex = j;
                        Object[][] fixedDemand = new Object[1][24];
                        for (int k = 0; k < 24; k++) {
                            fixedDemand[0][k] = tempFixed[k + 3];
                        }

                        DefaultTableModel loadDataModel = new DefaultTableModel(fixedDemand, names);
                        FixedLoadTable.setModel(loadDataModel);

                        FixedLoadTable.setAutoscrolls(true);
                        FixedLoadTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                        for (int k = 0; k < names.length; k++) {
                            column = FixedLoadTable.getColumnModel().getColumn(k);

                            column.setPreferredWidth(120);
                        }
                    }
                }

                for (int j = 0; j < lsePriceSensitiveDemandList.size(); j++) {
                    Object[][] tempPrice = (Object[][]) lsePriceSensitiveDemandList.get(j);
                    if (name.equalsIgnoreCase(tempPrice[0][0].toString())) {
                        iCurrentLSEPriceDemandIndex = j;
                        Object[][] priceDemand = new Object[24][5];
                        for (int h = 0; h < 24; h++) {
                            for (int k = 0; k < 5; k++) {
                                priceDemand[h][k] = tempPrice[h][k + 3];
                            }
                        }

                        DefaultTableModel loadDataModel;
                        loadDataModel = new DefaultTableModel(priceDemand, priceSensitiveNamesBenefit);

                        PriceSensitiveTable.setModel(loadDataModel);

                        PriceSensitiveTable.setAutoscrolls(true);
                        PriceSensitiveTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

//                    column=PriceSensitiveTable.getColumnModel().getColumn(CONSTD_COLUMN_INDEX);
//                    column.setHeaderRenderer(iconHeaderRender);
//                    column.setHeaderValue(constdIcon);
                        for (int k = 0; k < priceSensitiveNamesBenefit.length; k++) {
                            column = PriceSensitiveTable.getColumnModel().getColumn(k);

                            column.setPreferredWidth(120);
                        }
                    }
                }
            }
        }

        FlagTable.repaint();
        FixedLoadTable.repaint();
        PriceSensitiveTable.repaint();

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddlButton;
    private javax.swing.JTextField AtNodeTextField;
    private javax.swing.JButton CancelButton;
    private javax.swing.JButton DataVerifyButton;
    private javax.swing.JButton DeletelButton;
    private javax.swing.JTable FixedLoadTable;
    private javax.swing.JTable FlagTable;
    private javax.swing.JTextField IDTextField;
    private javax.swing.JComboBox LSENameComboBox;
    private javax.swing.JButton NextButton;
    private javax.swing.JButton Prev;
    private javax.swing.JTable PriceSensitiveTable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables
    private Vector copyRowVector;
    private javax.swing.JPopupMenu popupMenuFixedLoadTable = new JPopupMenu();
    private javax.swing.JPopupMenu popupMenuPriceSensitiveTable = new JPopupMenu();
    private javax.swing.JPopupMenu popupMenuFlagTable = new JPopupMenu();
    private JMenuItem CheckAllFixedLoadItem, unCheckAllFixedLoadItem, CheckAllPriceSensitiveItem, unCheckAllPriceSensitiveItem;
    private JMenuItem copyRowFixedLoadItem, pasteRowFixedLoadItem;
    private JMenuItem copyRowPriceSensitiveItem, pasteRowPriceSensitiveItem;
    private boolean bDataLoad = false;

    private TableCellRenderer iconHeaderRender;
    private ImageIcon constdIcon;
    private final int CONSTD_COLUMN_INDEX = 2;

    private ArrayList lseFixedDeamndList = new ArrayList(); // LSEName, H-00, ... H-23
    private ArrayList lsePriceSensitiveDemandList = new ArrayList();    // LSEName, d, e, f, max
    private ArrayList lseHybridDemandList = new ArrayList();    // LSEName, ID, AtNode, H-00 Flag, ... H-23 Flag
    // 1 = bFixedDemand, 
    // 2 = bPriceSensitiveDemand
    // 3 = bFixedDemand & bPriceSensitiveDemand

    //private Object[][] lseFixedDeamnd; // LSEName, H-00, ... H-23
    //private Object[][] lsePriceSensitiveDemand; // LSEName, c, d, min, max
    //private Object[][] lseHybridDemand;  // LSEName, ID, AtNode, H-00 Flag, ... H-23 Flag
    // 1 = bFixedDemand, 
    // 2 = bPriceSensitiveDemand
    // 3 = bFixedDemand & bPriceSensitiveDemand
    private Object[][] lseDemandFlag = new Object[2][24];

    private int iCurrentLSEIndex;
    private int iCurrentLSEFixedDemandIndex;
    private int iCurrentLSEPriceDemandIndex;
    private int iID;
    private int iAtNode;
    private String lseName;

    private AMESFrame mainFrame;

    Object[][] dataLoad;
    Object[][] data = {
        {"LSE1", 1, 2, 350, 322.93, 305.04, 296.02, 287.16, 291.59, 296.02, 314.07,
            358.86, 394.80, 403.82, 408.25, 403.82, 394.80, 390.37, 390.37,
            408.25, 448.62, 430.73, 426.14, 421.71, 412.69, 390.37, 363.46},
        {"LSE2", 2, 3, 300, 276.80, 261.47, 253.73, 246.13, 249.93, 253.73, 269.20,
            307.60, 338.40, 346.13, 349.93, 346.13, 338.40, 334.60, 334.60,
            349.93, 384.53, 369.20, 365.26, 361.47, 353.73, 334.60, 311.53},
        {"LSE3", 3, 4, 250, 230.66, 217.89, 211.44, 205.11, 208.28, 211.44, 224.33,
            256.33, 282.00, 288.44, 291.61, 288.44, 282.00, 278.83, 278.83,
            291.61, 320.44, 307.67, 304.39, 301.22, 294.78, 278.83, 259.61}
    };

    final Object[] blankRowData = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    final Object[][] blankData = {{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}};

    final Object[][] blankPriceSensitiveDataBenefit = {{0, 0, 0, 0}};

    public String[] names = {
        "H-00 (MW)", "H-01 (MW)", "H-02 (MW)", "H-03 (MW)", "H-04 (MW)", "H-05 (MW)",
        "H-06 (MW)", "H-07 (MW)", "H-08 (MW)", "H-09 (MW)", "H-10 (MW)", "H-11 (MW)",
        "H-12 (MW)", "H-13 (MW)", "H-14 (MW)", "H-15 (MW)", "H-16 (MW)", "H-17 (MW)",
        "H-18 (MW)", "H-19 (MW)", "H-20 (MW)", "H-21 (MW)", "H-22 (MW)", "H-23 (MW)"
    };

    public String[] priceSensitiveNamesBenefit = {
        "Hour Index", "d ($/h)", "e ($/MWh)", "f ($/(MW)^2h)", "SLMax (MW)"
    };

    public String[] priceHybridNames = {
        "FLAG-00", "FLAG-01", "FLAG-02", "FLAG-03", "FLAG-04", "FLAG-05",
        "FLAG-06", "FLAG-07", "FLAG-08", "FLAG-09", "FLAG-10", "FLAG-11",
        "FLAG-12", "FLAG-13", "FLAG-14", "FLAG-15", "FLAG-16", "FLAG-17",
        "FLAG-18", "FLAG-19", "FLAG-20", "FLAG-21", "FLAG-22", "FLAG-23"
    };

}
