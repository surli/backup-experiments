/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.ui;
//CHECKSTYLE:OFF
import com.google.common.collect.Multimap;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.joda.time.DateTime;
import org.spf4j.base.Pair;
import org.spf4j.tsdb2.Charts;
import org.spf4j.tsdb2.TSDBQuery;
import org.spf4j.tsdb2.TSDBQuery.TableDefEx;
import org.spf4j.tsdb2.avro.ColumnDef;
import org.spf4j.tsdb2.avro.TableDef;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings({"FCBL_FIELD_COULD_BE_LOCAL", "SE_BAD_FIELD"})
public class TSDB2ViewJInternalFrame extends javax.swing.JInternalFrame {
    private static final long serialVersionUID = 1L;

    private final File tsDb;

    /**
     * Creates new form TSDBViewJInternalFrame
     */
    public TSDB2ViewJInternalFrame(final File tsDb) throws IOException {
        super(tsDb.getPath());
        this.tsDb = tsDb;
        initComponents();
        Multimap<String, TableDefEx> columnsInfo = TSDBQuery.getAllTablesWithDataRanges(tsDb);
        Map<String, DefaultMutableTreeNode> gNodes = new HashMap<>();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(tsDb.getName());
        long startDateMillis = System.currentTimeMillis();
        for (Map.Entry<String, Collection<TableDefEx>> info : columnsInfo.asMap().entrySet()) {
            String groupName = info.getKey();
            final Collection<TableDefEx> defs = info.getValue();
            for (TableDefEx tde : defs) {
                long tableStart = tde.getStartTime();
                if (tableStart < startDateMillis) {
                    startDateMillis = tableStart;
                }
            }
            Pair<String, String> pair = Pair.from(groupName);
            if (pair == null) {
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(groupName);
                for (ColumnDef colDef : defs.iterator().next().getTableDef().getColumns()) {
                    child.add(new DefaultMutableTreeNode(colDef.getName()));
                }
                root.add(child);
            } else {
                groupName = pair.getFirst();
                DefaultMutableTreeNode gNode = gNodes.get(groupName);
                if (gNode == null) {
                    gNode = new DefaultMutableTreeNode(groupName);
                    gNodes.put(groupName, gNode);
                    root.add(gNode);
                }
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(pair.getSecond());
                for (ColumnDef colDef : defs.iterator().next().getTableDef().getColumns()) {
                    child.add(new DefaultMutableTreeNode(colDef.getName()));
                }
                gNode.add(child);
            }
        }
        measurementTree.setModel(new DefaultTreeModel(root));
        measurementTree.setVisible(true);
        this.startDate.setValue(new DateTime(startDateMillis).toDate());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rightPanel = new javax.swing.JPanel();
        mainSplitPannel = new javax.swing.JSplitPane();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        measurementTree = new javax.swing.JTree();
        chartPannel = new javax.swing.JScrollPane();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        startDate = new javax.swing.JSpinner();
        endDate = new javax.swing.JSpinner();

        org.jdesktop.layout.GroupLayout rightPanelLayout = new org.jdesktop.layout.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 448, Short.MAX_VALUE)
        );
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 306, Short.MAX_VALUE)
        );

        setClosable(true);
        setMaximizable(true);
        setResizable(true);

        mainSplitPannel.setDividerSize(5);
        mainSplitPannel.setPreferredSize(new java.awt.Dimension(600, 500));

        measurementTree.setAutoscrolls(true);
        jScrollPane1.setViewportView(measurementTree);

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 684, Short.MAX_VALUE)
        );

        mainSplitPannel.setLeftComponent(jPanel2);
        mainSplitPannel.setRightComponent(chartPannel);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        jButton1.setText("Plot");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        jButton2.setText("Export");
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton2);

        startDate.setModel(new javax.swing.SpinnerDateModel());
        startDate.setEditor(new javax.swing.JSpinner.DateEditor(startDate, "yyyy-MM-dd HH:mm:ss"));
        startDate.setMinimumSize(new java.awt.Dimension(200, 28));
        startDate.setName(""); // NOI18N
        jToolBar1.add(startDate);

        endDate.setModel(new javax.swing.SpinnerDateModel());
        endDate.setEditor(new javax.swing.JSpinner.DateEditor(endDate, "yyyy-MM-dd HH:mm:ss"));
        jToolBar1.add(endDate);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
                        .add(236, 236, 236))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                        .add(mainSplitPannel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainSplitPannel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 688, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    @SuppressFBWarnings("UP_UNUSED_PARAMETER")
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        TreePath[] selectionPaths = measurementTree.getSelectionPaths();
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        chartPannel.setViewportView(content);
        try {
            Set<String> selectedTables = getSelectedTables(selectionPaths);
            for(String tableName : selectedTables) {
                addChartToPanel(tableName, content);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        chartPannel.repaint();
    }//GEN-LAST:event_jButton1ActionPerformed

    @SuppressFBWarnings("UP_UNUSED_PARAMETER")
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        TreePath[] selectionPaths = measurementTree.getSelectionPaths();
        Set<String> selectedTables = getSelectedTables(selectionPaths);
        if (!selectedTables.isEmpty()) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            int returnVal = chooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    TSDBQuery.writeCsvTables(tsDb, selectedTables, file);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane chartPannel;
    private javax.swing.JSpinner endDate;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JSplitPane mainSplitPannel;
    private javax.swing.JTree measurementTree;
    private javax.swing.JPanel rightPanel;
    private javax.swing.JSpinner startDate;
    // End of variables declaration//GEN-END:variables

    @SuppressFBWarnings("CLI_CONSTANT_LIST_INDEX")
    private static Set<String> getSelectedTables(@Nullable final TreePath[] selectionPaths) {
        if (selectionPaths == null) {
            return Collections.EMPTY_SET;
        }
        Set<String> result = new HashSet<>();
        for (TreePath path : selectionPaths) {
            Object[] pathArr = path.getPath();
            if (pathArr.length < 2) {
                continue;
            }
            DefaultMutableTreeNode colNode = (DefaultMutableTreeNode) pathArr[1];
            int depth = colNode.getDepth();
            String tableName;
            if (depth == 1) {
                result.add((String) colNode.getUserObject());
            } else {
                Enumeration childEnum = colNode.children();
                while (childEnum.hasMoreElements()) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) childEnum.nextElement();
                    tableName =
                            Pair.of((String) colNode.getUserObject(), (String) child.getUserObject()).toString();
                    result.add(tableName);
                }
            }
        }
        return result;
    }

    private void addChartToPanel(final String tableName, final JPanel content) throws IOException {
        List<TableDef> info =  TSDBQuery.getTableDef(tsDb, tableName);
        long startTime = ((Date) startDate.getValue()).getTime();
        long endTime = ((Date) endDate.getValue()).getTime();
        if (Charts.canGenerateHeatChart(info.get(0))) {
            JFreeChart chart = Charts.createHeatJFreeChart(tsDb, info,
                    startTime, endTime);
            ChartPanel pannel = new ChartPanel(chart);
            pannel.setPreferredSize(new Dimension(600, 800));
            pannel.setDomainZoomable(false);
            pannel.setMouseZoomable(false);
            pannel.setRangeZoomable(false);
            pannel.setZoomAroundAnchor(false);
            pannel.setZoomInFactor(1);
            pannel.setZoomOutFactor(1);
            content.add(pannel);
        }
        if (Charts.canGenerateMinMaxAvgCount(info.get(0))) {
            JFreeChart chart = Charts.createMinMaxAvgJFreeChart(tsDb, info,
                    startTime, endTime);
            ChartPanel pannel = new ChartPanel(chart);
            pannel.setPreferredSize(new Dimension(600, 600));
            content.add(pannel);

        }
        if (Charts.canGenerateCount(info.get(0))) {
            JFreeChart chart = Charts.createCountJFreeChart(tsDb, info,
                    startTime, endTime);
            ChartPanel pannel = new ChartPanel(chart);
            pannel.setPreferredSize(new Dimension(600, 600));
            content.add(pannel);
        } else {
            List<JFreeChart> createJFreeCharts = Charts.createJFreeCharts(tsDb, info, startTime, endTime);
            for (JFreeChart chart : createJFreeCharts) {
                ChartPanel pannel = new ChartPanel(chart);
                pannel.setPreferredSize(new Dimension(600, 600));
                content.add(pannel);
            }
        }
    }
}
