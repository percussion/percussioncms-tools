/******************************************************************************
 *
 * [ PSArchiveSummaryDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.objectstore.PSArchiveInfo;
import com.percussion.deployer.objectstore.PSArchivePackage;
import com.percussion.deployer.objectstore.PSArchiveSummary;
import com.percussion.deployer.objectstore.PSLogSummary;
import com.percussion.deployer.objectstore.PSTransactionLogSummary;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.util.PSEntrySet;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *  The dialog to show installed archive summary.
 */
public class PSArchiveSummaryDialog extends PSDialog
{
   /**
    * Constructs this dialog.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    * @param server the deployment server  may not be <code>null
    * </code> and and must be connected.
    * @param arcSum archive summary of the installed package. May not
    * be <code>null</code>.
    * @throws IllegalArgumentException if any of the required parameters
    * are invalid or the server is not connected.
    */
   public PSArchiveSummaryDialog(Frame parent, PSDeploymentServer server,
      PSArchiveSummary arcSum)
   {
      super(parent);
      if (arcSum == null)
         throw new IllegalArgumentException("Archive summary cannot be null");
      if (server == null)
         throw new IllegalArgumentException("Deployment server cannot be null");
      if (!server.isConnected())
         throw new IllegalArgumentException("Server is not connected");
      
      m_archiveSummary = arcSum;
      m_server = server;
      initDialog();
   }

   /**
    * Private helper method
    * @param resId the tab's label
    * @param tabIx the tab's index
    */
   private void setMnemonicsForTabbedPane(JTabbedPane tab, String resId, int tabIx)
   {
      String label = getResourceString(resId);
      char mn = getResourceString(resId + ".mn").charAt(0);
       
      tab.setMnemonicAt(tabIx, (int)mn);      
      tab.setDisplayedMnemonicIndexAt(0, label.indexOf(mn));
   }

   /**
    * Creates the dialog framework.
    */
   private void initDialog()
   {
      JPanel mainPane = new JPanel();
      mainPane.setPreferredSize(new Dimension(600, 500));
      mainPane.setLayout(new BorderLayout());
      mainPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      JPanel northPane = new JPanel();
      northPane.setLayout(new BoxLayout(northPane, BoxLayout.X_AXIS));
      northPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      Date date = m_archiveSummary.getInstallDate();
      DateFormat dfInsDate = DateFormat.getDateTimeInstance();
      JLabel insdateLabel = new JLabel(getResourceString("insDate"));
      JLabel insdate = new JLabel(dfInsDate.format(date));

      PSArchiveInfo arcInfo = m_archiveSummary.getArchiveInfo();
      String verBuild = arcInfo.getServerVersion() + "/" +
         arcInfo.getServerBuildNumber();
      JLabel srcSrvLabel = new JLabel(getResourceString("srcServer"));
      JLabel srcSrv = new JLabel(arcInfo.getServerName());
      JLabel vrBldLabel = new JLabel(getResourceString("vrBld"));
      JLabel vrBld = new JLabel(verBuild);

      JPanel labelPanel = new JPanel();
      labelPanel.setLayout(new BoxLayout(labelPanel , BoxLayout.Y_AXIS));
      labelPanel.add(insdateLabel);
      labelPanel.add(Box.createRigidArea(new Dimension(0,10)));
      labelPanel.add(srcSrvLabel);
      labelPanel.add(Box.createRigidArea(new Dimension(0,10)));
      labelPanel.add(vrBldLabel);

      JPanel valuesPanel = new JPanel();
      valuesPanel.setLayout(new BoxLayout(valuesPanel, BoxLayout.Y_AXIS));
      valuesPanel.add(insdate);
      valuesPanel.add(Box.createRigidArea(new Dimension(0,10)));
      valuesPanel.add(srcSrv);
      valuesPanel.add(Box.createRigidArea(new Dimension(0,10)));
      valuesPanel.add(vrBld);

      northPane.add(labelPanel);
      northPane.add(Box.createRigidArea(new Dimension(10,0)));
      northPane.add(valuesPanel);

      JPanel cmdPanel = new JPanel(new BorderLayout());
      cmdPanel.setBorder(BorderFactory.createEmptyBorder(10,0,0,0));
      UTFixedButton close = new UTFixedButton(getResourceString("close"));
      close.setMnemonic(getResourceString("close.mn").charAt(0));
      close.setPreferredSize(new Dimension(100,24));
      close.addActionListener(new CloseListener());

      cmdPanel.add(close, BorderLayout.EAST);

      // create tabbed pane with package and txn log tabs
      final JTabbedPane tabbedPane = new JTabbedPane();
      
      tabbedPane.add(getResourceString("packageLogs"), createPackagePanel());
      setMnemonicsForTabbedPane(tabbedPane, "packageLogs", 0);
      tabbedPane.add(getResourceString("txnLog"), createTxnPanel());
      setMnemonicsForTabbedPane(tabbedPane, "txnLog", 1);
      tabbedPane.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e)
         {
            // suppress eclipse warning
            if (null == e);
            
            // lazily build txn table model if selecting that tab for first time
            if (tabbedPane.getSelectedIndex() == 1 && m_txnTableModel == null)
            {
               try
               {
                  getContentPane().setCursor(Cursor.getPredefinedCursor(
                     Cursor.WAIT_CURSOR));

                  
                  List logSumList = new ArrayList();
                  Iterator pkgs = m_archiveSummary.getPackageList();
                  while (pkgs.hasNext())
                  {
                     PSArchivePackage pack = (PSArchivePackage)pkgs.next();
                     int logId = pack.getLogId();
                     if (logId != -1)
                     {
                        PSLogSummary logSum = 
                           m_server.getDeploymentManager().getLogSummary(logId);
                        PSTransactionLogSummary txnLogSum = 
                           logSum.getLogDetail().getTransactionLog();
                        logSumList.add(new PSEntrySet(pack.getName(), 
                           txnLogSum));
                     }
                  }
                  m_txnTableModel = new PSTransLogModel(logSumList);
                  m_txnTable.setModel(m_txnTableModel);
                  pack();
               }
               catch(PSDeployException ex)
               {
                  ErrorDialogs.showErrorMessage(PSArchiveSummaryDialog.this,
                     ex.getLocalizedMessage(), getResourceString("error"));
               }
               finally
               {
                  getContentPane().setCursor(Cursor.getPredefinedCursor(
                        Cursor.DEFAULT_CURSOR));
               }
            }
         }});
      
      mainPane.add(northPane, BorderLayout.NORTH);
      mainPane.add(tabbedPane, BorderLayout.CENTER);
      mainPane.add(cmdPanel, BorderLayout.SOUTH);
      getContentPane().add(mainPane);
      setResizable(true);
      pack();
      center();
   }
   
   /**
    * Creates the panel with the transaction logs
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createTxnPanel()
   {
      JPanel txnPanel = new JPanel(new BorderLayout());
      txnPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      
      m_txnTable = new JTable();
      
      JScrollPane scrollPane = new JScrollPane(m_txnTable);
      txnPanel.add(scrollPane, BorderLayout.CENTER);
      
      return txnPanel;
   }

   /**
    * Creates the panel with the list of packages and the view log button.
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createPackagePanel()
   {
      JPanel pkgListPanel = new JPanel();
      JPanel buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

      //final because used in the inner class in table's list selection listener
      final UTFixedButton vLog = new UTFixedButton(getResourceString("vLog"));
      vLog.setMnemonic(getResourceString("vLog.mn").charAt(0));
      vLog.setPreferredSize(new Dimension(100,24));
      vLog.addActionListener(new ViewLogListener());
      buttonPanel.add(Box.createHorizontalGlue());
      buttonPanel.add(vLog);
      buttonPanel.add(Box.createHorizontalGlue());
      
      pkgListPanel.setLayout(new BoxLayout(pkgListPanel, BoxLayout.Y_AXIS));

      String title = MessageFormat.format(getResourceString("title"),
         new Object[]{m_archiveSummary.getArchiveInfo().getArchiveRef()});
      setTitle(title);
      pkgListPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

      m_pkgTable = new JTable(new ArchiveSummaryTableModel(m_archiveSummary));
      m_pkgTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);      
      ListSelectionModel lsm = m_pkgTable.getSelectionModel();
      lsm.addListSelectionListener(new ListSelectionListener(){
         public void valueChanged(ListSelectionEvent e)
         {
            int index = m_pkgTable.getSelectedRow();
            if (index != -1)
            {
               ArchiveSummaryTableModel model =
                     (ArchiveSummaryTableModel)m_pkgTable.getModel();
               List list = model.getPackageList();
               if (!list.isEmpty() && index < list.size())
               {
                  PSArchivePackage pack = (PSArchivePackage)list.get(index);
                  if (pack.hasLog())
                     vLog.setEnabled(true);
                  else
                     vLog.setEnabled(false);
               }
               else
                  vLog.setEnabled(false);
            }
         }
      });
      lsm.setSelectionInterval(0, 0);

      JScrollPane jp = new JScrollPane(m_pkgTable);
      pkgListPanel.add(jp);
      pkgListPanel.add(Box.createVerticalStrut(10));
      pkgListPanel.add(buttonPanel);
      return pkgListPanel;
   }

   /**
    * Table displaying list of packages. Initalized in
    * <code>initDialog()</code>, never <code>null</code> after that.
    * Never modified after initialization.
    */
   private JTable m_pkgTable;
   
   /**
    * Table displaying list of transactions. Initalized in
    * <code>initDialog()</code>, never <code>null</code> after that.
    * Lazily populated on first time the txn log tab is selected. 
    */
   private JTable m_txnTable;
   
   /**
    * Transaction table model, created lazily on first access of the txn log
    * tab, never <code>null</code> or modified after that.
    */
   private TableModel m_txnTableModel = null;


   /**
    * Displays selected deployed package log on pressing 'View Log' button.
    * The 'View Log' button is enabled only if the selected package is a
    * deployed package.
    */
   private class ViewLogListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         int index = m_pkgTable.getSelectedRow();
         if (index != -1)
         {
            ArchiveSummaryTableModel model =
                  (ArchiveSummaryTableModel)m_pkgTable.getModel();
            List list = model.getPackageList();
            int logId = 0;
            if (!list.isEmpty() && index < list.size())
            {
               PSArchivePackage pack = (PSArchivePackage)list.get(index);
               logId = pack.getLogId();
               try
               {
                  PSLogSummaryDialog logSum = new PSLogSummaryDialog(
                     PSArchiveSummaryDialog.this,
                     m_server.getDeploymentManager().getLogSummary(logId),
                     m_server.getLiteralIDTypes());
                  logSum.setVisible(true);
               }
               catch(PSDeployException ex)
               {
                  ErrorDialogs.showErrorMessage(PSArchiveSummaryDialog.this,
                     ex.getLocalizedMessage(), getResourceString("error"));
               }
            }
         }
      }
   }

   /**
    * Responds to 'Close' button. Closes the dialog box
    * <code>PSDeploymentArchiveSummaryDialog</code>.
    */
   private class CloseListener implements ActionListener
   {
      public void actionPerformed(ActionEvent e)
      {
         setVisible(false);
         dispose();
      }
   }

   /**
    * Table model for displaying archive summary for the list of installed
    * packages.
    */
   private class ArchiveSummaryTableModel extends AbstractTableModel
   {
      /**
       * Represents table model for archive summary.
       * Populates with <code>PSArchiveSummary</code> object.
       * @param obj Never <code>null</code>.
       */
      public ArchiveSummaryTableModel(PSArchiveSummary obj)
      {
         if (obj == null)
            throw new IllegalArgumentException(
            "Archive summary cannot be null.");
         loadColumnNames();
         m_archiveSummary = obj;
         Iterator itr = obj.getPackageList();
         if (itr.hasNext())
         {
            while (itr.hasNext())
               m_packList.add(itr.next());
         }
      }

      /**
       * Checks that the supplied row exists in this model.
       *
       * @param row the row index to check, must be >= 0 and less than
       * {@link #getRowCount() rowcount} of this model.
       *
       * @throws IllegalArgumentException if row index is invalid.
       */
      private void checkRow(int row)
      {
         if(row < 0 || row >= getRowCount())
            throw new IllegalArgumentException("row must be between 0 and " +
                  (getRowCount()-1) + "inclusive");
      }

      /**
       * Checks that the supplied column exists in this model.
       *
       * @param col the column index to check, must be >= 0 and less than
       * {@link #getRowCount() rowcount} of this model.
       *
       * @throws IllegalArgumentException if column index is invalid.
       */
      private void checkColumn(int col)
      {
         if(col < 0 || col >= getColumnCount())
            throw new IllegalArgumentException("col must be between 0 and " +
                  (getColumnCount()-1) + "inclusive");
      }

      /**
       * Loads column names for model.
       */
      private void loadColumnNames()
      {
         if (m_colNames.isEmpty())
         {
            int col = 0;
            try
            {
               col = Integer.parseInt(getResourceString("colCount"));
            }
            catch(NumberFormatException e)
            {
               //assuming a default value
               col = 3;
            }
            String colName = null;
            for(int k = 0; k < col; k++)
            {
               colName = getResourceString("col" + (k+1));
               m_colNames.add(colName);
            }
         }
      }

      /**
       * Gets number of columns in the model.
       * @return column count.
       */
      public int getColumnCount()
      {
         return m_colNames.size();
      }

      /**
       * Gets the number of rows in the table.
       * A minimum of <code>MIN_ROWS</code> are returned.
       * @return number of rows.
       */
      public int getRowCount()
      {
         if(m_packList.size() < MIN_ROWS)
            return MIN_ROWS;
         else
            return m_packList.size();
      }

      /**
       * Gets the name of the column specified by its column number.
       * @param col number of the column whoes name is being sought.
       * @return  name of the the column. Never <code>null</code> or empty.
       *
       * @throws IllegalArgumentException if column index is invalid.
       */
      public String getColumnName(int col)
      {
         checkColumn(col);
         return (String)m_colNames.get(col);
      }

      /**
       * Gets the data at the specified row and column number.
       * @param row row number, valid value ranges from
       * 0 to ((@link #getRowCount()) - 1)
       * @param col column number, valid value ranges from
       * 0 to ((@link #getColumnCount()) - 1)
       * @return data at <code>row</code> and <code>col</code>, may be
       * <code>null</code>.
       *
       * @throws IllegalArgumentException if column or row index is invalid.
       */
      public Object getValueAt(int row, int col) 
      {
         checkRow(row);
         checkColumn(col);
         try {
            if(row < m_packList.size())
            {
               PSArchivePackage pak = (PSArchivePackage)m_packList.get(row);
               switch(col)
               {
                  case 0:
                     return pak.getName();
   
                  case 1:
                     return m_server.getPackageTypeDisplayName(pak.getType());
   
                  case 2:
                     if (pak.isInstalled())
                        return getResourceString("yes");
                     else if (pak.isFailed())
                        return getResourceString("failed");
                     else
                        return getResourceString("no");
               }
            }
         }
         catch(PSDeployException ex)
         {
            ErrorDialogs.showErrorMessage(PSArchiveSummaryDialog.this,
               ex.getLocalizedMessage(), getResourceString("error"));            
         }
         return null;
      }

      /**
       * Gets a list of <code>PSArchivePackage</code> objects.
       * @return list of archive pacakges, never <code>null</code>, may be
       * empty.
       */
      public List getPackageList()
      {
         return m_packList;
      }

      /**
       * Decides if the cell is editable or not.
       * @param row
       * @param col
       * @return Since no cells are editable in this model,
       * it returns <code>false</code>.
       */
      public boolean isCellEditable(int row, int col)
      {
         return false;
      }

      /**
       * List of column names. Names are loaded in
       * <code>loadColumnNames()</code>, assuming  resource bundle exists
       * and there is no problem in loading it's not empty. Never
       * <code>null</code>
       */
       private List m_colNames = new ArrayList();

      /**
       * List of <code>PSArchivePackage</code> objects. Packages are addded in
       * the model's constructor and never modfied after that. May be empty,
       * never <code>null</code>.
       */
      List m_packList = new ArrayList();
   }

   /**
    * The deployment server. Never <code>null</code> or modified.
    */
   PSDeploymentServer m_server;

   /**
    * Archive summary object. Never <code>null</code> or modified.
    */
   PSArchiveSummary m_archiveSummary;

   /**
    * Minimum number of empty rows to be displayed if there is no data.
    */
   private static int MIN_ROWS = 20;
}
