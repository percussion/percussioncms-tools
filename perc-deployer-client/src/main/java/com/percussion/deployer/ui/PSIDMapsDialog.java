/******************************************************************************
 *
 * [ PSIDMapsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.objectstore.PSDbmsMap;
import com.percussion.deployer.objectstore.PSIdMap;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.utils.collections.PSIteratorUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Dialog for advanced editing all dbms and idmappings between two servers
 * during installation.
 */
public class PSIDMapsDialog extends PSDialog
{
   /**
    * Construct the dialog.
    * 
    * @param parent The parent dialog, may be <code>null</code>.
    * @param handler The transforms handler to use, contains the dbms and data
    * mappings to modify, may not be <code>null</code>.
    */
   PSIDMapsDialog(Dialog parent, PSTransformsHandler handler)
   {
      super(parent);
      
      if (handler == null)
         throw new IllegalArgumentException("handler may not be null");
      m_sourceTransformsHandler = handler;
      m_transformsHandler = new PSTransformsHandler(handler);
      
      initDialog();
   }
   
   /**
    * Initializes the dialog ui and data.
    */   
   private void initDialog()
   {
      setTitle(getResourceString("title"));
      JPanel mainpanel = new JPanel(new BorderLayout());
      mainpanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
      mainpanel.add(createTransformsPanel(), BorderLayout.CENTER);
      mainpanel.add(createCommandPanel(), BorderLayout.SOUTH);
      
      getContentPane().add(mainpanel);
      
      pack();      
      center();
      setResizable(true);
      
   }

   /**
    * Creates the panel with the command buttons.
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createCommandPanel()
   {
      UTFixedButton updateButton = new UTFixedButton(getResourceString(
         "update"));
      updateButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onUpdate();
         }
      });

      UTFixedButton cancelButton = new UTFixedButton(getResourceString(
         "cancel"));
      cancelButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onCancel();
         }
      });

      UTFixedButton helpButton = new UTFixedButton(getResourceString("help"));
      helpButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused") ActionEvent e)
         {
            onHelp();
         }
      });

      JPanel commandPanel = new JPanel();
      commandPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));
      commandPanel.setLayout(new BoxLayout(commandPanel, BoxLayout.X_AXIS));
      commandPanel.setAlignmentX(LEFT_ALIGNMENT);
      commandPanel.add(Box.createHorizontalGlue());
      commandPanel.add(updateButton);
      commandPanel.add(Box.createHorizontalStrut(5));
      commandPanel.add(cancelButton);
      commandPanel.add(Box.createHorizontalStrut(5));
      commandPanel.add(helpButton);      
      
      return commandPanel;
   }


   /**
    * Creates the panel containing the tabs for the dbms and ID mapping panels.
    * 
    * @return The panel, never <code>null</code>.
    */
   private JPanel createTransformsPanel()
   {
      JPanel panel = new JPanel(new BorderLayout());
      m_tabbedPane = new JTabbedPane();
      panel.add(m_tabbedPane, BorderLayout.CENTER);
      
      try 
      {
         PSDeploymentServer target = m_transformsHandler.getTarget();
         Iterator mappings = null; 
         PSDbmsMap dbmsMap = m_transformsHandler.getDbmsMap();
         if (dbmsMap != null)
            mappings = dbmsMap.getMappings();
         if (mappings != null && mappings.hasNext())
         {
            m_dbmsMapsPanel = new PSDBMSMapsPanel(
               PSIteratorUtils.emptyIterator(), PSDBMSMapsPanel.EDIT_TYPE_DEL);
            List<String> targetDrivers = new ArrayList<String>();
            Iterator drivers = target.getDataSources().getResults();
            while(drivers.hasNext())
               targetDrivers.add(((PSCatalogResult)drivers.next()).getID());
            m_dbmsMapsPanel.setTargetDataSources(targetDrivers);
            m_dbmsMapsPanel.setPanelState(true);
            
            m_dbmsMapsPanel.setData(
               m_transformsHandler.getDbmsMap().getMappings(), 
               m_transformsHandler);
            m_dbmsMapsPanel.addChangeListener(new ChangeListener() {
               public void stateChanged(
                  @SuppressWarnings("unused") ChangeEvent e)
               {
                  m_isDbmsDataModified = true;
               }});
            m_tabbedPane.add(getResourceString("dbmsMaps"), m_dbmsMapsPanel);            
         }


         // create a copy of the id mappings so we can allow user to cancel
         mappings = null;
         PSIdMap idMap = m_transformsHandler.getIdMap();
         if (idMap != null)
            mappings = idMap.getMappings();
         if (mappings != null && mappings.hasNext())
         {
            m_idMapsPanel = new PSIDMapsPanel(PSIteratorUtils.emptyIterator(),
               true, true, null, target.getLiteralIDTypes(),
               false);
            m_idMapsPanel.setPanelState(true);
            m_idMapsPanel.setData(m_transformsHandler.getIdMap().getMappings(), 
               m_transformsHandler);
            m_idMapsPanel.addChangeListener(new ChangeListener() {
               public void stateChanged(
                  @SuppressWarnings("unused") ChangeEvent e)
               {
                  m_isIdDataModified = true;
               }});
            m_tabbedPane.add(getResourceString("elementMaps"), m_idMapsPanel);
         }

      }
      catch(PSDeployException ex)
      {
         ErrorDialogs.showErrorMessage(getOwner(), ex.getLocalizedMessage(),
            getResourceString("error"));
      }      
      
      return panel;
   }
   

   /**
    * Handles the click of the update button, saves any changes and then 
    * delegates to {@link PSDialog#onOk()}.
    */
   protected void onUpdate()
   {
      try 
      {
         if (m_dbmsMapsPanel != null)
            m_dbmsMapsPanel.stopEditing();
         if (m_idMapsPanel != null)
            m_idMapsPanel.stopEditing();
         if (m_isDbmsDataModified || m_isIdDataModified)
         {
            if (m_isDbmsDataModified)
            {
               m_sourceTransformsHandler.getDbmsMap().copyFrom(
                  m_transformsHandler.getDbmsMap());
               m_sourceTransformsHandler.saveDbmsMappings();
            }
            if (m_isIdDataModified)    
            {
               m_sourceTransformsHandler.getIdMap().copyFrom(
                  m_transformsHandler.getIdMap());
               m_sourceTransformsHandler.saveIdMappings();
            }

            m_mappingsChanged = true;
         }
         onOk();
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
               getResourceString("error"));      
      }      
   }
   
   /**
    * Handles the cancel button click. If there are any changes, prompts the
    * user to discard them.  If user agrees, backs out the changes and then
    * deletegates to {@link PSDialog#onCancel()}.
    */
   public void onCancel()
   {
      if (m_isDbmsDataModified || m_isIdDataModified)
      {
         // prompt if changes will be lost
         int result = JOptionPane.showConfirmDialog(this, 
            getResourceString("cancel.msg"), getResourceString("cancel.title"),
            JOptionPane.OK_CANCEL_OPTION);
         
         if (result == JOptionPane.CANCEL_OPTION)
            return;
      }
            
      super.onCancel();
   }
   
   /**
    * Determine if any mappings have been modified.
    * 
    * @return <code>true</code> if mappings have been modified, 
    * <code>false</code> if not.
    */
   public boolean didMappingsChange()
   {
      return isOk() && m_mappingsChanged;
   }
   
   /**
    * The tabbed panel to contain the 'DBMS Maps" and 'Element Maps' tabs.
    * Initialized in during construction and never <code>null</code> or modified 
    * after that.
    */
   private JTabbedPane m_tabbedPane;   

   /**
    * The panel to display id mappings, initialized during construction and 
    * never <code>null</code> after that. 
    */
   private PSIDMapsPanel m_idMapsPanel;

   /**
    * The panel to display dbms mappings, initialized during construction and 
    * never <code>null</code> after that. 
    */
   private PSDBMSMapsPanel m_dbmsMapsPanel;
   
   /**
    * The handler used to get and save dbms and id mappings for each import 
    * package, a copy of the one supplied during construction and never 
    * <code>null</code> or modified after that.
    */
   private PSTransformsHandler m_transformsHandler;   
   
   /**
    * The handler supplied during construction, never <code>null</code> or 
    * modified after that.  Mappings are modified in {@link #onUpdate()} 
    */
   private PSTransformsHandler m_sourceTransformsHandler; 
   
   /**
    * Determines if the dbms mappings have been modified.  Initially 
    * <code>false</code>, changed to <code>true</code> as the dbms mapping panel 
    * contents are modified.
    */
   private boolean m_isDbmsDataModified = false;
   /**
    * Determines if the id mappings have been modified.  Initially 
    * <code>false</code>, changed to <code>true</code> as the id mapping panel 
    * contents are modified.
    */
   private boolean m_isIdDataModified = false;

   /**
    * Determines if any of the underlying data has been modified, either by 
    * updating the model with changes, or if "rolling back" changes if the user
    * cancels and changes have been made.  Initially <code>false</code>, changed
    * to <code>true</code> as the data is modified.
    */
   private boolean m_mappingsChanged = false;
}

