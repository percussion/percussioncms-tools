/*[ StatusPropertyPanel.java ]*************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.E2Designer.UTReadOnlyTableCellEditor;
import com.percussion.UTComponents.UTFixedButton;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

/**
 * The applets main dialog is implemented as a tabbed dialog. One Tab of this
 * dialog is the "Status" Tab which displays server/application status
 * informations
 */
////////////////////////////////////////////////////////////////////////////////
/**
 * TODO: document
 */
public class StatusPropertyPanel extends JPanel implements ListSelectionListener
{
   /**
    * Construct server/application status panel.
    *
    * @param parent         the paren frame
    * @param anchor         anchor to which to attach sub dialogs
    * @param console         the server console used to access the data
    */
   /////////////////////////////////////////////////////////////////////////////
   public StatusPropertyPanel(Frame parent, Component anchor,
      ServerConsole console)
   {
      try
      {
         BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
         this.setLayout(layout);

         m_parent = parent;
         m_anchor = anchor;
         m_console = console;

         this.setLayout(layout);

         JScrollPane tablePanel = createTablePanel();

         if(m_tableView.getParent() != null)
            m_tableView.getParent().setBackground(Color.white);

         JPanel basePane = new JPanel(new BorderLayout());
         basePane.add(tablePanel,BorderLayout.CENTER );
         basePane.setBackground(Color.white);
         this.add("North", basePane); 

         JPanel commandPanel = createCommandPanel();
         this.add("South", commandPanel);

         updateCommandButtonStatus();
      }
      catch (Exception ex)
      {
         ex.printStackTrace();
      }
   }

   /**
    * Create the command panel and implement the action listeners
    *
    */
   /////////////////////////////////////////////////////////////////////////////
   private   JPanel createCommandPanel() throws Exception
   {
     JPanel panel = new JPanel();
      FlowLayout layout = new FlowLayout(FlowLayout.RIGHT);
      m_detailsButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onDetailsAction();
         }
      });

      m_startButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
            onStart();
         }
      });

      m_stopButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
           onStop();
         }
      });

      m_restartButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
           onRestart();
         }
      });

      m_refreshButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent event)
         {
           onRefresh();
         }
      });

      setMnemonicsInTheTab();
      panel.setLayout(layout);
      panel.add(m_detailsButton);
      panel.add(m_startButton);
      panel.add(m_stopButton);
      panel.add(m_restartButton);
      panel.add(m_refreshButton);

      return panel;
   }


   /**
    * Performs the action when the Details button is pressed.
    */
   private void onDetailsAction()
   {
      try
      {
         m_detailsButton.setCursor(m_parent.getCursor().getPredefinedCursor(
            Cursor.WAIT_CURSOR));
         m_detailsButton.setEnabled(false);

         int iSelected = m_tableView.getSelectedRow();
         if (iSelected >= 0)
         {
            String application = m_tableModel.getValueAt(iSelected,
               ProjectConstants.STATUS_TABLE_NAME_INDEX).toString();
            // create details for application
            StatusDetailsDialog details = new StatusDetailsDialog(m_parent,
               m_res.getString("detailsTitle") + application, m_console,
               application);
            details.setLocationRelativeTo(m_parent);
            details.setVisible(true);
         }
         else
         {
            // create details for server
            StatusDetailsDialog details = new StatusDetailsDialog(m_parent,
               m_res.getString("detailsTitle"), m_console);
            details.setLocationRelativeTo(m_parent);
             details.pack();
            details.setVisible(true);
         }

         m_detailsButton.setEnabled(true);
         m_detailsButton.setCursor(Cursor.getDefaultCursor());
      }
      catch (Exception e0)
      {
         e0.printStackTrace();
      }
   }

   /**
    * Performs the action when the Start button is pressed.
    */
   private void onStart()
   {
      int iSelected = m_tableView.getSelectedRow();
      if (iSelected >= 0)
      {
         String appName = m_tableModel.getValueAt(iSelected,
            ProjectConstants.STATUS_TABLE_NAME_INDEX).toString();
         m_console.startApplication(appName);
         updateApplicationStatus(iSelected);
         m_tableView.repaint();
      }
   }

/**
 * Performs the action when the Stop button is pressed.
 */
  private void onStop()
  {
      int iSelected = m_tableView.getSelectedRow();
      if (iSelected >= 0)
      {
         String appName = m_tableModel.getValueAt(iSelected,
            ProjectConstants.STATUS_TABLE_NAME_INDEX).toString();
         m_console.stopApplication(appName);
         updateApplicationStatus(iSelected);
         m_tableView.repaint();
      }
  }

   /**
    * Performs the action when the Restart button is pressed.
    */
   private void onRestart()
   {
      int iSelected = m_tableView.getSelectedRow();
      if (iSelected >= 0)
      {
         String appName = m_tableModel.getValueAt(iSelected,
            ProjectConstants.STATUS_TABLE_NAME_INDEX).toString();
         m_console.restartApplication(appName);
      }
   }

   /**
    * Performs the action when the Refresh button is pressed.
    */
   private void onRefresh()
   {
      // clear existing table model
      m_tableModel.clearTableEntries();

      // fill in table data
      m_tableModel.initTable(m_console);

      // inform the ui
      m_tableView.repaint();
      updateCommandButtonStatus();
   }


   /**
    *Updates the application status for the given row.
    *
    * @param row The row that the application resides.
    */
   private void updateApplicationStatus(int row)
   {
      String appName = m_tableModel.getValueAt(row,
                          ProjectConstants.STATUS_TABLE_NAME_INDEX).toString();
      String command = "show status application " + appName;

      try
      {
         org.w3c.dom.Document doc = m_console.execute(command);
         m_tableModel.setValueAt(m_res.getString("active"), row,
            ProjectConstants.STATUS_TABLE_STATUS_INDEX);
         m_startButton.setEnabled(false);
         m_stopButton.setEnabled(true);
         m_detailsButton.setEnabled(true);
         m_restartButton.setEnabled(true);
      }
      catch (Exception e)
      {
         // exception is okay, this means that the application is NOT active;
         m_tableModel.setValueAt(m_res.getString("inactive"), row,
            ProjectConstants.STATUS_TABLE_STATUS_INDEX);
         m_startButton.setEnabled(true);
         m_stopButton.setEnabled(false);
         m_detailsButton.setEnabled(false);
         m_restartButton.setEnabled(false);
      }
   }

   /**
    * Create the status table panel which contains 3 columns for Name, Type and
    * Status.
    *
    */
   /////////////////////////////////////////////////////////////////////////////
   public JScrollPane createTablePanel()
   {
      // define the cell editors
      m_tableView.getColumn(m_res.getString("tableName")).setCellEditor(
        new UTReadOnlyTableCellEditor());
      m_tableView.getColumn(m_res.getString("tableType")).setCellEditor(
        new UTReadOnlyTableCellEditor());
      m_tableView.getColumn(m_res.getString("tableStatus")).setCellEditor(
        new UTReadOnlyTableCellEditor());
      m_tableView.getColumn(m_res.getString("tableName")).setPreferredWidth(
         200);
      m_tableView.getColumn(m_res.getString("tableType")).setPreferredWidth(60);
      m_tableView.getColumn(m_res.getString("tableStatus")).setPreferredWidth(
         40);

      // do not allow column reordering
      m_tableView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      m_tableView.setRowSelectionAllowed(true);
      m_tableView.setColumnSelectionAllowed(false);
      m_tableView.setShowVerticalLines(true);
      m_tableView.setShowHorizontalLines(true);
      m_tableView.setRequestFocusEnabled(false);
      m_tableView.getTableHeader().setReorderingAllowed(false); 

      // add change list selection listener
      m_tableView.getSelectionModel().addListSelectionListener(this);

      // fill in table data
      m_tableModel.initTable(m_console);

      return new JScrollPane(m_tableView);
   }

   /**
    * Updates the enables states of the command buttons based on the currently
    * selected row.
    */
   private void updateCommandButtonStatus()
   {
      int iSelected = m_tableView.getSelectedRow();
      if (iSelected >= 0)
      {
         String appStatus = m_tableModel.getValueAt(iSelected,
            ProjectConstants.STATUS_TABLE_STATUS_INDEX).toString();
         setApplicationCommandEnabled(appStatus.equals(m_res.getString(
            "active")));
      }
   }

   /**
    * Update the command panels enabled buttons.
    *
    * @param bEnabled      application is active (true) or inactive (false)
    */
   /////////////////////////////////////////////////////////////////////////////
   private void setApplicationCommandEnabled(boolean bEnabled)
   {
      m_startButton.setEnabled(!bEnabled);
      m_stopButton.setEnabled(bEnabled);
      m_restartButton.setEnabled(bEnabled);
      m_detailsButton.setEnabled(bEnabled);
   }

   /////////////////////////////////////////////////////////////////////////////
   // implementation for list selection listener
   public void valueChanged(ListSelectionEvent e)
   {
      if (!e.getValueIsAdjusting())
       {
         int iSelected = m_tableView.getSelectedRow();
         if (iSelected >= 0)
         {
            String appStatus = m_tableModel.getValueAt(iSelected,
               ProjectConstants.STATUS_TABLE_STATUS_INDEX).toString();
            setApplicationCommandEnabled(appStatus.equals(m_res.getString(
               "active")));
         }
      }
   }

   /**
    * Private method that sets mnemonics for all the controls within the tab 
    *
    */
   private void setMnemonicsInTheTab()
   {
       m_detailsButton.setMnemonic(getMnemonicForControl("details"));
       m_startButton.setMnemonic(getMnemonicForControl("start"));
       m_stopButton.setMnemonic(getMnemonicForControl("stop"));
       m_restartButton.setMnemonic(getMnemonicForControl("restart"));
       m_refreshButton.setMnemonic(getMnemonicForControl("refresh"));
   }
   /**
    * @param resId the resource id from the bundle, prepend status and append mn
    * @return return the integer value of the char ( upper case )
    */
   private int getMnemonicForControl(String resId)
   {
       char mnemonic;
       String cmdName = m_res.getString(resId);
       mnemonic = m_res.getString("status."+resId+".mn").charAt(0);
       return (int)(""+mnemonic).toUpperCase().charAt(0);
   }
   //////////////////////////////////////////////////////////////////////////////
   public final String STATUS_TABLE_HEADER[] =
   {
      m_res.getString("tableName"),
      m_res.getString("tableType"),
      m_res.getString("tableStatus")
   };

   /**
    * the parent frame
    */
   private Frame m_parent = null;

   /**
    * the anchor for further dialogs
    */
   private Component m_anchor = null;

   /**
    * the server console used
    */
   private ServerConsole m_console = null;

   /**
    * the table data model
    */
   private StatusTable m_tableModel = new StatusTable();

   /**
    * the table view
    */
   private JTable m_tableView = new JTable(m_tableModel);
   
   /**
    * the details button
    */
   private JButton m_detailsButton = new UTFixedButton(m_res.getString(
      "details"), new Dimension(90, 24));
      
   /**
    * the start button
    */
   private JButton m_startButton = new UTFixedButton(m_res.getString("start"));
   
   /**
    * the stop button
    */
   private JButton m_stopButton = new UTFixedButton(m_res.getString("stop"));
   
   /**
    * the restart button
    */
   private JButton m_restartButton = new UTFixedButton(m_res.getString(
      "restart"));
   
   /**
    * the refresh button
    */
   private JButton m_refreshButton = new UTFixedButton(m_res.getString(
      "refresh"));

   /**
   * Resources
   */
   private static ResourceBundle m_res = PSServerAdminApplet.getResources();

}

