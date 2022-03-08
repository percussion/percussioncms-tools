/******************************************************************************
 *
 * [ PSTransformsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.utils.collections.PSIteratorUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * The dialog used to map dbms credentials and the elements from a source server
 * to a target server.
 */
public class PSTransformsDialog extends PSDialog
{
   /**
    * Constructs this dialog.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    * @param source the source server for which the dbms credentials and
    * elements in the repository need to be mapped, may not be <code>null</code>
    * and must be connected.
    * @param targetServers the list of target servers to which source server
    * elements need to be mapped, may not be <code>null</code> and must not be
    * empty.
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSTransformsDialog(Frame parent, PSDeploymentServer source,
      List targetServers)
   {
      super(parent);

      if(source == null)
         throw new IllegalArgumentException("source may not be null.");
      if(!source.isConnected())
         throw new IllegalArgumentException("source must be connected.");

      if(targetServers == null || targetServers.isEmpty())
         throw new IllegalArgumentException(
            "list of target servers may not be null or empty.");

      try {
         ((List)targetServers).toArray(new PSDeploymentServer[0]);
      }
      catch(ArrayStoreException e)
      {
         throw new IllegalArgumentException(
            "The list of target servers must be instances of PSDeploymentServer");
      }

      m_sourceServer = source;

      initDialog(targetServers);
   }

   
   /**
    * Private helper method
    * @param resId the tab's label
    * @param tabIx the tab's index
    */
   private void setMnemonicsForTabbedPane(String resId, int tabIx)
   {
      String label = getResourceString(resId);
      char mn = getResourceString(resId + ".mn").charAt(0);
       
      m_tabbedPane.setMnemonicAt(tabIx, (int)mn);      
      m_tabbedPane.setDisplayedMnemonicIndexAt(0, label.indexOf(mn));
   }

   
   /**
    * Creates the dialog framework with top panel representing the list of
    * target servers, center panel representing the tabbed pane with tabs for
    * dbms credentials and elements mappings and bottom panel with command
    * buttons.
    */
   private void initDialog(List targetServers)
   {
      JPanel panel = new JPanel(new BorderLayout());
      getContentPane().add(panel);
      
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);

      setTitle(MessageFormat.format(getResourceString("title"),
         new String[] {m_sourceServer.getServerName()}));

      JPanel topPanel = new JPanel();
      topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
      String labelStr = getResourceString("targetServer");
      char mn = getResourceString("targetServer.mn").charAt(0);
      JLabel lbl = new JLabel(labelStr, SwingConstants.RIGHT);
      lbl.setDisplayedMnemonic(mn);
      lbl.setDisplayedMnemonicIndex(labelStr.indexOf(mn));
      topPanel.add(lbl);
      
      topPanel.add(Box.createHorizontalStrut(20));
      m_targetCombo = new JComboBox(
         new TargetServerComboBoxModel(targetServers));
      lbl.setLabelFor(m_targetCombo);
      m_targetCombo.setRenderer(new ServerComboCellRenderer());
      topPanel.add(m_targetCombo);
      topPanel.add(Box.createHorizontalStrut(10));
      topPanel.add(Box.createHorizontalGlue());
      topPanel.add(new JLabel(getResourceString("updateMsg")));
      topPanel.add(Box.createHorizontalGlue());
      topPanel.setAlignmentX(LEFT_ALIGNMENT);
      panel.add(topPanel, BorderLayout.NORTH);

      //controls panel in the center of the dialog.
      m_tabbedPane = new JTabbedPane();
      JPanel centerPanel = new JPanel();
      centerPanel.setBorder(emptyBorder);
      centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
      centerPanel.setAlignmentX(LEFT_ALIGNMENT);
      centerPanel.add(m_tabbedPane);
      panel.add(centerPanel, BorderLayout.CENTER);

      try {
         m_dbmsMapsPanel = new PSDBMSMapsPanel(PSIteratorUtils.emptyIterator(),
            PSDBMSMapsPanel.EDIT_TYPE_ALL);
         List sourceDataSrcs = new ArrayList();
         Iterator dataSrcs = m_sourceServer.getDataSources().getResults();
         while(dataSrcs.hasNext())
            sourceDataSrcs.add( ((PSCatalogResult)dataSrcs.next()).getID() );
         m_dbmsMapsPanel.setSourceDatasources(sourceDataSrcs);
         m_dbmsMapsPanel.setPanelState(false);
         m_tabbedPane.add(getResourceString("dbmsMaps"), m_dbmsMapsPanel);
         setMnemonicsForTabbedPane("dbmsMaps", 0);

         m_idMapsPanel = new PSIDMapsPanel(PSIteratorUtils.emptyIterator(),
            true, true, m_sourceServer, m_sourceServer.getLiteralIDTypes(),
            true);
         m_idMapsPanel.setPanelState(false);
         m_tabbedPane.add(getResourceString("elementMaps"), m_idMapsPanel);
         setMnemonicsForTabbedPane("elementMaps", 1);
      }
      catch(PSDeployException ex)
      {
         ErrorDialogs.showErrorMessage(getOwner(), ex.getLocalizedMessage(),
            getResourceString("error"));
      }

      UTFixedButton updateButton = new UTFixedButton(getResourceString("update"));
      updateButton.setMnemonic(getResourceString("update.mn").charAt(0));
      updateButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            updateMappings();
         }
      });

      UTFixedButton closeButton = new UTFixedButton(getResourceString("close"));
      closeButton.setMnemonic(getResourceString("close.mn").charAt(0));
      closeButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onCancel();
         }
      });

      UTFixedButton helpButton = new UTFixedButton(getResourceString("help"));
      helpButton.setMnemonic(getResourceString("help.mn").charAt(0));
      helpButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onHelp();
         }
      });

      JPanel commandPanel = new JPanel();
      commandPanel.setBorder(emptyBorder);
      commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));
      commandPanel.setAlignmentX(LEFT_ALIGNMENT);
      commandPanel.add(Box.createHorizontalGlue());
      commandPanel.add(updateButton);
      commandPanel.add(Box.createHorizontalStrut(5));
      commandPanel.add(closeButton);
      commandPanel.add(Box.createHorizontalStrut(5));
      commandPanel.add(helpButton);
      panel.add(commandPanel, BorderLayout.SOUTH);

      pack();
      center();
      setResizable(true);
   }

   /**
    * Action method for 'Update' button. Updates the mappings on selected target
    * server for this source and updates the panels that their data is not
    * modified to indicate that update is done. Displays an error message if an
    * exception happens saving the mappings.
    */
   private void updateMappings()
   {
      try {
         if(m_currentTransformsHandler != null)
         {
            m_dbmsMapsPanel.stopEditing();
            m_currentTransformsHandler.saveDbmsMappings();
            m_dbmsMapsPanel.dataNotModified();

            if(m_currentTransformsHandler.needToMapIds())
            {
               m_idMapsPanel.stopEditing();
               if(m_idMapsPanel.validateMappings())
               {
                  m_currentTransformsHandler.saveIdMappings();
                  m_idMapsPanel.dataNotModified();
               }
            }
         }
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
            getResourceString("error"));
      }
   }

   /**
    * The combo-box model used for the target servers combo-box, implemented to
    * handle the selected item change events. This handles the following.
    * <ol>
    * <li>If a server is deselected, displays a confirmation message to user
    * to update or loose the mappings edited. If user chooses to loose, it
    * continues with the selection change, otherwise simply returns to allow the
    * user to update.</li>
    * <li>If a server is selected, displays an error message that server must be
    * connected to update the maps.</li>
    * </ol>
    */
   private class TargetServerComboBoxModel extends AbstractListModel
      implements ComboBoxModel
   {
      /**
       * Constructs the combo-box model with the supplied list of servers.
       *
       * @param servers the list of <code>PSDeploymentServer</code> objects, may
       * not be <code>null</code> or empty.
       *
       * @throws IllegalArgumentException if servers is <code>null</code> or
       * empty.
       */
      public TargetServerComboBoxModel(List servers)
      {
         if(servers == null || servers.isEmpty())
            throw new IllegalArgumentException(
               "servers may not be null or empty.");
         m_servers = servers;
      }

      //implements ListModel interface method
      public int getSize()
      {
         return m_servers.size();
      }

      //implements ListModel interface method
      public Object getElementAt(int index)
      {
         if(index < 0 || index >= getSize())
            throw new IllegalArgumentException("invalid index");

         return m_servers.get(index);
      }

      //implements ComboBoxModel interface method
      public Object getSelectedItem()
      {
         return m_selectedItem;
      }

      /**
       * Handles the following for selection change events.
       * <ol>
       * <li>For deselecting server, displays a confirmation message to user
       * to update or lose the mappings edited if it has not yet been updated.
       * If user chooses to lose, it continues with the selection change,
       * otherwise updates the mappings.</li>
       * <li>For selecting server, displays an error message that server must be
       * connected to update the maps. This server should be licensed for
       * MSM, else an error message is displayed.</li>
       * </ol>
       */
      public void setSelectedItem(Object selItem)
      {
         Object curSelItem = getSelectedItem();
         if ( curSelItem != null &&
            curSelItem instanceof PSDeploymentServer)
         {
            PSDeploymentServer target = (PSDeploymentServer)curSelItem;
            boolean isCurrentServerUpdated = true;
            if(m_dbmsMapsPanel.isDataModified() ||
               m_idMapsPanel.isDataModified() )
            {
               isCurrentServerUpdated = false;
            }
            if(target.isConnected() && !isCurrentServerUpdated)
            {
               int option = JOptionPane.showConfirmDialog(
                  PSTransformsDialog.this, ErrorDialogs.cropErrorMessage(
                  getResourceString("mustUpdate")), MessageFormat.format(
                  getResourceString("mustUpdateTitle"),
                  new String[] {target.getServerName()}),
                  JOptionPane.YES_NO_CANCEL_OPTION);

               if(option == JOptionPane.YES_OPTION)
                  updateMappings();
               else if(option == JOptionPane.CANCEL_OPTION)
                  return;
            }
         }

         if ( selItem instanceof PSDeploymentServer )
         {
            PSDeploymentServer target = (PSDeploymentServer)selItem;
            if(!PSDeploymentClient.getConnectionHandler().connectToServer(
               target))
            {
               ErrorDialogs.showErrorMessage(PSTransformsDialog.this,
                  getResourceString("mustConnected"),
                  getResourceString("error") );
               return;
            }
            else if (!target.isServerLicensed())
            {
               ErrorDialogs.showErrorMessage(PSTransformsDialog.this,
                  getResourceString("notLicensedServer"),
                  getResourceString("error") );
               return;
            }
            else
            {
               try {
                  m_currentTransformsHandler = new PSTransformsHandler(target,
                     m_sourceServer.getServerName(),
                     m_sourceServer.getRepositoryInfo());
                  m_dbmsMapsPanel.setPanelState(true);

                  List<String> targetDataSrcs = new ArrayList<String>();
                  Iterator dataSrcs = target.getDataSources().getResults();
                  while(dataSrcs.hasNext())
                  {
                     targetDataSrcs.add(
                        ((PSCatalogResult)dataSrcs.next()).getID() );
                  }
                  m_dbmsMapsPanel.setTargetDataSources(targetDataSrcs);
                  m_dbmsMapsPanel.setData(
                     m_currentTransformsHandler.getDbmsMap().getMappings(),
                     m_currentTransformsHandler);

                  if(m_currentTransformsHandler.needToMapIds())
                  {
                     Iterator idMappings =
                        m_currentTransformsHandler.getIdMap().getMappings();
                     m_idMapsPanel.setPanelState(true);
                     m_idMapsPanel.setData(idMappings,
                        m_currentTransformsHandler);
                     m_tabbedPane.setComponentAt(ELEMENT_TAB_INDEX,
                        m_idMapsPanel);
                  }
                  else
                  {
                     if(m_noIdMapsPanel.getComponentCount() == 0)
                     {
                        m_noIdMapsPanel.setBorder(
                           BorderFactory.createEmptyBorder(10,10,10,10));
                        m_noIdMapsPanel.setLayout(
                           new BoxLayout(m_noIdMapsPanel, BoxLayout.Y_AXIS));
                        m_noIdMapsPanel.add(Box.createVerticalGlue());
                        m_noIdMapsPanel.add(new JLabel(
                           getResourceString("sameRepository")));
                        m_noIdMapsPanel.add(new JLabel(
                           getResourceString("noMapsRequired")));
                        m_noIdMapsPanel.add(Box.createVerticalGlue());
                     }
                     m_tabbedPane.setComponentAt(
                        ELEMENT_TAB_INDEX, m_noIdMapsPanel);
                  }
               }
               catch(PSDeployException e)
               {
                  ErrorDialogs.showErrorMessage(PSTransformsDialog.this,
                     e.getLocalizedMessage(), getResourceString("error"));
               }
            }
         }
         if(curSelItem != selItem)
         {
            m_selectedItem = selItem;
            fireContentsChanged(this, -1, -1);
         }
      }

      /**
       * The list of servers to be shown in the combo-box, when this model is
       * used, initialized in the constructor and never <code>null</code> or
       * modified after that.
       */
      private List m_servers;

      /**
       * Represents the current selected item, initialized to <code>null</code>
       * and may be modified by call to <code>setSelectedItem(Object)</code>.
       */
      private Object m_selectedItem = null;
   }

   /**
    * The cell renderer that is used with 'Target Server' combo box to show the
    * not connected servers as disabled.
    */
   private class ServerComboCellRenderer extends DefaultListCellRenderer
   {
      public Component getListCellRendererComponent(JList list, Object value,
         int index, boolean isSelected, boolean cellHasFocus)
      {
         super.getListCellRendererComponent(list, value, index, isSelected,
            cellHasFocus);
         if(value != null)
         {
            PSDeploymentServer server = (PSDeploymentServer)value;
            if(!server.isConnected())
            {
               setEnabled(false);
            }
         }
         return this;
      }
   }

   /**
    * Gets the right help page based on the tab selection.
    *
    * @param helpId name of the class, may not be <code>null</code> or
    * empty.
    *
    * @return help id corresponding to the tab selected. Never <code>null</code>
    * or empty.
    *
    * @throws IllegalArgumentException if any helpId is <code>null</code> or
    * empty.
    */
   protected String subclassHelpId( String helpId )
   {
      if(helpId == null || helpId.trim().length() == 0)
         throw new IllegalArgumentException("helpId may not be null or empty.");

      int k = m_tabbedPane.getSelectedIndex();
      if (k != -1)
      {
         if (k == 0)
            helpId = helpId +"_DbmsMap";
         else
            helpId = helpId +"_IdMap";
      }
      return helpId;
   }

   /**
    * The panel to represent id maps, initialized in <code>initDialog(List)
    * </code> and never <code>null</code> after that. Represents element
    * mappings of the source server on selected target server.
    */
   private PSIDMapsPanel m_idMapsPanel = null;

   /**
    * The panel to represent dbms maps, initialized in <code>initDialog(List)
    * </code> and never <code>null</code> after that. Represents dbms mappings
    * of the source server on selected target server.
    */
   private PSDBMSMapsPanel m_dbmsMapsPanel = null;

   /**
    * The tabbed panel to contain the 'DBMS Maps" and 'Element Maps' tabs.
    * Initialized in <code>initDialog(List)</code> and never <code>null</code>
    * or modified after that.
    */
   private JTabbedPane m_tabbedPane;

   /**
    * The panel that describes that element maps are not required because both
    * (source and target) share the same repository, initialized to an empty
    * panel and set with the description labels and shown when the source and
    * the selected target share same repository.
    */
   private JPanel m_noIdMapsPanel = new JPanel();

   /**
    * The combo-box used to show the list of target servers, initialized in
    * <code>initDialog()</code> and never <code>null</code> or modified after
    * that.
    */
   private JComboBox m_targetCombo;

   /**
    * The handler used to get and save dbms and id mappings for the source
    * server on the selected target server, initialized to <code>null</code> and
    * set to the appropriate handler as target server is changed.
    */
   private PSTransformsHandler m_currentTransformsHandler = null;

   /**
    * The source server whose external dbms credentials and element ids need to
    * be mapped to a set of target servers, initialized in the constructor and
    * never <code>null</code> or modified after that.
    */
   private PSDeploymentServer m_sourceServer;

   /**
    * The index of element maps tab.
    */
   private static final int ELEMENT_TAB_INDEX = 1;
}
