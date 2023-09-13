/******************************************************************************
 *
 * [ PSDeploymentTransformsDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.catalog.PSCatalogResultSet;
import com.percussion.deployer.objectstore.PSArchiveDetail;
import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSImportDescriptor;
import com.percussion.deployer.objectstore.PSImportPackage;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSListPanel;
import com.percussion.utils.collections.PSIteratorUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The dialog used to map dbms credentials and the elements in the import 
 * packages of the import descriptor that needs to be transformed (who have ids
 * in the repository).
 */
public class PSDeploymentTransformsDialog  extends PSDeploymentWizardDialog
{   
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, int, int) 
    * super(parent, step, sequence)}. Additional parameters are described below. 
    * 
    * @param isTypical if <code>true</code> it displays the id/dbms mappings
    * that are not mapped with their target, otherwise it displays all mappings.
    * @param transformsHandler the handler that is used to get dbms and id 
    * mappings in each mode (typical/custom) and to save the transforms on the
    * target server.
    * @param detail the archive detail which has information about the external
    * dbms credentials that are required to be transformed with this archive, 
    * may not be <code>null</code>
    * 
    * @throws PSDeployException if an exception happens extracting data from 
    * supplied parameters.
    */
   public PSDeploymentTransformsDialog(Frame parent, 
      int step, int sequence, boolean isTypical, 
      PSTransformsHandler transformsHandler, PSArchiveDetail detail) 
         throws PSDeployException
   {
      super(parent, step, sequence);
      
      if(transformsHandler == null)
         throw new IllegalArgumentException(
            "transformsHandler may not be null.");
            
      if(detail == null)
         throw new IllegalArgumentException("detail may not be null.");
            
      m_isTypical = isTypical;
      m_transformsHandler = transformsHandler;

      PSDeploymentServer tgt = m_transformsHandler.getTarget();
      Iterator drivers = null;
      try
      {
         PSCatalogResultSet tgtCatalogResultSet = tgt.getDataSources();
         drivers = tgtCatalogResultSet.getResults();
      }
      catch (PSDeployException e)
      {
         e.getLocalizedMessage();
         return;
      }
      catch (Exception e)
      {
         e.getLocalizedMessage();
         return;
      }
      m_targetDatasources = new ArrayList();
      while(drivers.hasNext())
         m_targetDatasources.add(((PSCatalogResult)drivers.next()).getID());               
      
      m_archiveDetail = detail;
      
      initDialog();
   }
   
   /**
    * Creates the dialog framework with border layout keeping the description 
    * panel on north, controls panel on center and command panel on south.
    */
   protected void initDialog()
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BorderLayout(10, 20));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle(getResourceString("title"));
      int steps = 2;
      try 
      {
         steps = Integer.parseInt(getResourceString("descStepCount"));
      }
      catch (NumberFormatException ex) 
      {
         //uses the default  
      }
      String[] description = new String[steps];
      for (int i = 1; i <= steps; i++) 
      {
         description[i-1] = getResourceString("descStep" + i);
      }
      
      JPanel descPanel = createDescriptionPanel(
         getResourceString("descTitle"), description);
      panel.add(descPanel, BorderLayout.NORTH);
      
      //controls panel in the center of the dialog.
      m_listPanel = new PSListPanel(getResourceString("packagesLabel"), 
         PSListPanel.ALIGN_RIGHT, false);  
      panel.add(m_listPanel, BorderLayout.CENTER);      
      
      JButton guessButton = new JButton(getResourceString("guessAll"));
      guessButton.setMnemonic(getResourceString("guessAll.mn").charAt(0));
      guessButton.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e)
         {
            onGuessAll();
         }});
      
      JButton advancedButton = new JButton(getResourceString("advanced"));
      advancedButton.setMnemonic(getResourceString("advanced.mn").charAt(0));
      advancedButton.addActionListener(new ActionListener() {

         public void actionPerformed(ActionEvent e)
         {
            onAdvanced();
         }});
      
      panel.add(createCommandPanel(true, new JComponent[] {guessButton, 
         advancedButton}), BorderLayout.SOUTH);
   }
   
   /**
    * Adds the tabs for each import package in the import descriptor. See <code>
    * super.init()</code> for more description.
    */
   protected void init()
   {      
      m_listPanel.removeAll();
      m_importDescriptor = (PSImportDescriptor)getDescriptor();
      List packages = m_importDescriptor.getImportPackageList();
      Collection skippedPkgs = (Collection)getData();
      try {
         for (int i = 0; i < packages.size(); i++) 
         {
            PSDeployableElement element = 
               ((PSImportPackage)packages.get(i)).getPackage();
            String title = element.getDisplayIdentifier();
            
            Iterator dbmsMappings = m_transformsHandler.getDBMSMappings(
               m_isTypical, element, m_archiveDetail);
            Iterator idMappings = PSIteratorUtils.emptyIterator();
            if(m_transformsHandler.needToMapIds())
            {
               idMappings = m_transformsHandler.getIDMappings(
                  m_isTypical, element);
            }
            
            if (!(dbmsMappings.hasNext() || idMappings.hasNext()))
               continue;
              
            // restore any previously saved skip settings
            boolean shouldSkip = skippedPkgs != null && skippedPkgs.contains(
               element.getKey());
            PSPackageMapsPanel panel = new PSPackageMapsPanel(element, 
               dbmsMappings, idMappings, shouldSkip);
            m_listPanel.addPanel(title,  panel);
            updatePanelIcon(panel);
         }
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(getOwner(), e.getLocalizedMessage(), 
            getResourceString("error"));
      }
      pack();      
      center();    
      setResizable(true);
   }

   // see base class
   public Object getDataToSave()
   {
      // save list of skipped packages
      return getSkippedList();
   }

   // see base class
   public void onBack()
   {
      Iterator panels = m_listPanel.getPanels();
      while (panels.hasNext())
      {
         PSPackageMapsPanel mapsPanel = 
            (PSPackageMapsPanel) panels.next();
         mapsPanel.stopEditing();
      }
      
      if (saveTransforms())
      {
         setShouldUpdateUserSettings(true);
         super.onBack();         
      }
   }
   
   /**
    * Validates and saves the dbms and id maps for each package on the target 
    * server. Removes the package from the descriptor if it is skipped by user
    * in this dialog. Displays error message if the validation fails. Calls 
    * super's <code>onNext()</code> to make the dialog invisible.
    */
   public void onNext()
   {
      List impPackList = 
         ((PSImportDescriptor)m_importDescriptor).getImportPackageList();
         
      if(validateData())
      {
         //if all packages are skipped, give an error message and return
         Collection skippedList = getSkippedList();
         if(skippedList.size() == impPackList.size())
         {
            ErrorDialogs.showErrorMessage(this, 
               getResourceString("noInstallPackages"), 
               getResourceString("error"));
            return;
         }        
         
         //save maps on the target server.
         if (saveTransforms())
         {
            //remove the skipped packages from the list.
            Iterator pkgs = impPackList.iterator();
            while (pkgs.hasNext())
            {
               PSImportPackage impPkg = (PSImportPackage) pkgs.next();
               if (skippedList.contains(impPkg.getPackage().getKey()))
                  pkgs.remove();
            }

            super.onNext();
         }
      }
   }

   /**
    * Handles the 'Guess All' action.
    */
   protected void onGuessAll()
   {
      try
      {
         setCursor(Cursor.getPredefinedCursor(
            Cursor.WAIT_CURSOR));
         
         // walk all packages and build a full list of mappings
         List allMappings = new ArrayList();
         m_importDescriptor = (PSImportDescriptor)getDescriptor();
         List packages = m_importDescriptor.getImportPackageList();
         for (int i = 0; i < packages.size(); i++) 
         {
            PSDeployableElement element = 
               ((PSImportPackage)packages.get(i)).getPackage();
               
            Iterator idMappings = m_transformsHandler.getIDMappings(
               m_isTypical, element);
            while (idMappings.hasNext())
               allMappings.add(idMappings.next());
         }
         
         // now guess them all at once
         List unmapped = m_transformsHandler.guessTarget
            (allMappings.iterator());
         
         // now re-guess any unmapped children
         m_transformsHandler.guessTarget(unmapped.iterator());
         
         // now update all panel icons
         updatePanelIcons();
      }
      catch (PSDeployException ex)
      {
         ErrorDialogs.showErrorMessage(this, ex.getLocalizedMessage(), 
            getResourceString("error"));
      }
      finally
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }      
   } 
    
   /**
    * Updates the title of each of the panels with the correct decoration to 
    * indicate if it has incomplete mappings.
    */
   private void updatePanelIcons()
   {
      Iterator panels = m_listPanel.getPanels();
      while (panels.hasNext())
      {
         PSPackageMapsPanel mapsPanel = 
            (PSPackageMapsPanel) panels.next();
         updatePanelIcon(mapsPanel);
      }
   }

   /**
    * Displays the advanced mappings dialog and update this dialog's data 
    * appropriately.
    */
   protected void onAdvanced()
   {
      PSIDMapsDialog advancedDlg = new PSIDMapsDialog(this, 
         m_transformsHandler);
      advancedDlg.setVisible(true);
      
      // reload with changes
      if (advancedDlg.didMappingsChange())
      {
         // refresh UI completely based on current model state.  Need to save
         // skipped package state first.
         setData(getDataToSave());
         init();

         // if OK and in typical mode, check if all packages are complete
         if (m_listPanel.getPanelCount() == 0)
            super.onNext();
      }
   }
   
   /**
    * Validates that the all dbms and id mappings are mapped for all packages 
    * (tabs). If the validation fails for any tab, displays an error message and
    * makes that tab as the current selected tab.
    * 
    * @return <code>true</code> if the validation succeeds, otherwise <code>
    * false</code>
    */
   protected boolean validateData()
   {
      int panels = m_listPanel.getPanelCount();
      for (int i = 0; i < panels; i++) 
      {
         PSPackageMapsPanel mapsPanel = 
            (PSPackageMapsPanel)m_listPanel.getPanel(i);
         if(!mapsPanel.isSkipPackage() && !mapsPanel.validateMappings())
         {
            String title = m_listPanel.getTitle(i);
            String msg = MessageFormat.format(
               getResourceString("unMappedMappings"), new String[]{ title });
            ErrorDialogs.showErrorMessage(this, msg,
               getResourceString("error"));               
            m_listPanel.setSelectedIndex(i);
            return false;
         }
      }
      return true;
   }
   
   /**
    * Saves current transform defintions to the server.  Does not validate that 
    * all mappings are completed or if all packages are "skipped" (nor is this
    * required).  Use {@link #validateData()} if such validation is desired
    * before saving, and {@link #getSkippedList()} to get list of skipped
    * package panels.
    * 
    * @return <code>true</code> if the operation completes successfully, 
    * <code>false</code> if there were errors.  Any handled exceptions are
    * presented to the user in error dialogs.

    */
   private boolean saveTransforms()
   {
      boolean didSave = false;
      //save maps on the target server.
      try {
         if(m_transformsHandler.getDbmsMap().getMappings().hasNext())
            m_transformsHandler.saveDbmsMappings();
         if(m_transformsHandler.needToMapIds())
            m_transformsHandler.saveIdMappings();
         didSave = true;
      }
      catch(PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
               getResourceString("error"));      
      }

      return didSave;
   }
   
   /**
    * Get list of "skipped" packages.
    * 
    * @return A set of skipped package keys as <code>String</code> objects, 
    * never <code>null</code>, may be empty.
    */
   private Set getSkippedList()
   {
      Set skippedList = new HashSet();
      int panels = m_listPanel.getPanelCount();         
      for (int i = 0; i < panels; i++) 
      {
         PSPackageMapsPanel mapsPanel = 
            (PSPackageMapsPanel)m_listPanel.getPanel(i);
         if(mapsPanel.isSkipPackage())            
            skippedList.add(mapsPanel.getPackage().getKey());
      }
      
      return skippedList;
   }

   /**
    * Updates the title of the supplied panel in the list panel with the correct
    * decoration to indicate if it has incomplete mappings.  The skip package
    * checkbox state is adjusted based on whether mapppings are incomplete.
    * 
    * @param panel The panel to update, may not be <code>null</code>. 
    */
   protected void updatePanelIcon(PSPackageMapsPanel panel)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      String icon;

      if (panel.validateMappings())
      {
         icon = "gif_transparent";
         panel.setCanSkipPackage(false);
      }
      else
      {
         icon = "gif_exclamation";
         panel.setCanSkipPackage(true);
      }
      
      int index = m_listPanel.getIndex(panel);
      if (index != -1)
      {
         m_listPanel.setTitleImage(index, 
            PSDeploymentClient.getImageLoader().getImage(
               PSDeploymentClient.getResources().getString(icon)));         
      }
   }
   
   /**
    * Panel to represent dbms and id maps for an import package. Displays a 
    * tabbed panel with one tab for each kind of mapping. 
    */
   private class PSPackageMapsPanel extends JPanel implements ItemListener, 
      ChangeListener
   {
      /**
       * Constructs the panel with supplied mappings. Initializes the panel with
       * a tab for each kind of mapping. If the mappings are empty, 
       * corresponding tab won't be visible. If both mappings are empty, 
       * displays a description label that it does not have anything to map.
       * 
       * @param element The element represented by this panel, may not be 
       * <code>null</code>.
       * @param dbmsMappings the dbms mappings to map, may not be <code>null
       * </code>, can be empty.
       * @param idMappings the id mappings to map, may not be <code>null
       * </code>, can be empty.
       * @param shouldSkip <code>true</code> to mark this package as skipped
       * initially, <code>false</code> otherwise.
       * 
       * @throws PSDeployException if an error happens initializing the panel.
       */
      public PSPackageMapsPanel(PSDeployableElement element, 
         Iterator dbmsMappings, Iterator idMappings, boolean shouldSkip) 
            throws PSDeployException
      {
         if (element == null)
            throw new IllegalArgumentException("element may not be null");
         if (dbmsMappings == null)
            throw new IllegalArgumentException("dbmsMappings may not be null");
         if (idMappings == null)
            throw new IllegalArgumentException("idMappings may not be null");
         
         m_package = element;
         
         setLayout(new BorderLayout(20,20));
         setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
         
         if(dbmsMappings.hasNext() || idMappings.hasNext())
         {
            JTabbedPane tabbedPane = new JTabbedPane();
            if(dbmsMappings.hasNext())
            {
               m_dbmsMapsPanel = new PSDBMSMapsPanel(dbmsMappings, 
                                                PSDBMSMapsPanel.EDIT_TYPE_EDIT);
               m_dbmsMapsPanel.setTargetDataSources(m_targetDatasources);
               m_dbmsMapsPanel.addChangeListener(this);
               tabbedPane.add(getResourceString("dbmsMaps"), m_dbmsMapsPanel);
            }
            
            if(idMappings.hasNext())
            {
               m_idMapsPanel = new PSIDMapsPanel(
                  PSIteratorUtils.emptyIterator(), false, true, 
                  null, m_transformsHandler.getTarget().getLiteralIDTypes(), 
                  false);
               m_idMapsPanel.setData(idMappings, m_transformsHandler);
               m_idMapsPanel.addChangeListener(this);
               tabbedPane.add(getResourceString("elementMaps"), m_idMapsPanel);
            }
            add(tabbedPane, BorderLayout.CENTER);   
         }
         else
         {
            add(new JLabel(getResourceString("noMaps"), SwingConstants.CENTER),
               BorderLayout.CENTER);
         }
         m_skipPackageCheck = new JCheckBox(getResourceString("skipPackage"));
         m_skipPackageCheck.setSelected(shouldSkip);
         m_skipPackageCheck.addItemListener(this);         
         add(m_skipPackageCheck, BorderLayout.SOUTH);
      }
      
      
      /**
       * Get the package represented by this panel.
       * 
       * @return The package supplied during construction, never 
       * <code>null</code>.
       */
      public PSDeployableElement getPackage()
      {
         return m_package;
      }
      
      /**
       * Handles the item state change event for 'Skip this package' checkbox.
       * Displays an informational confirmation message if the package to skip 
       * is a package that some other packages depend on it. Skips the package 
       * if the user continues to skip, otherwise not.
       * 
       * @param e the item state change event, assumed not <code>null</code> as
       * this method will be called by Swing model when item state change event
       * occurs.
       */
      public void itemStateChanged(ItemEvent e)
      {
         if(e.getSource() == m_skipPackageCheck && 
            e.getStateChange() == ItemEvent.SELECTED)
         {
            Set parentPackages = new HashSet();
            int index = m_listPanel.getSelectedIndex();
            PSDeployableElement element = ((PSImportPackage)m_importDescriptor.
               getImportPackageList().get(index)).getPackage();
            if(PSDeploymentWizardDialog.isDependencyPackage(
               m_importDescriptor, element, parentPackages))
            {
               String[] args = new String[] {element.toString(), 
               parentPackages.toString()};            
               int option = JOptionPane.showConfirmDialog(this, 
                  ErrorDialogs.cropErrorMessage( MessageFormat.format(
                  getResourceString("packageSkipWarning"), args) ), 
                  getResourceString("warning"), 
                  JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
               
               if(option == JOptionPane.NO_OPTION)
                  m_skipPackageCheck.setSelected(false);
            }
         }
      }
      
      /**
       * Stops editing of both panels contained in this panel
       */
      public void stopEditing()
      {
         if (m_dbmsMapsPanel != null)
            m_dbmsMapsPanel.stopEditing();
         if (m_idMapsPanel != null)
            m_idMapsPanel.stopEditing();
      }
      
      /**
       * Get the dbms map panel
       * 
       * @return The panel, may be <code>null</code> if no dbms mappings were 
       * supplied.
       */
      public PSDBMSMapsPanel getDbmsMapsPanel()
      {
         return m_dbmsMapsPanel;
      }
      
      /**
       * Get the ID map panel
       * 
       * @return The panel, may be <code>null</code> if no ID mappings were 
       * supplied.
       */
      public PSIDMapsPanel getIdMapsPanel()
      {
         return m_idMapsPanel;
      }
      
      /**
       * Gets the list of dbms mappings represented by this panel.
       * 
       * @return the dbms mappings, never <code>null</code>, may be empty.
       */
      public List getDBMSMappings()
      {
         if(m_dbmsMapsPanel != null)
            return m_dbmsMapsPanel.getDBMSMappings();
         return new ArrayList();
      }

      
      /**
       * Gets the list of id mappings represented by this panel.
       * 
       * @return the id mappings, never <code>null</code>, may be empty.
       */
      public List getIdMappings()
      {
         if(m_idMapsPanel != null)
            return m_idMapsPanel.getIdMappings();
         return new ArrayList();
      }
      
      /**
       * Validates that all dbms and id mappings are mapped.
       * 
       * @return <code>true</code> if all mappings are mapped, otherwise <code>
       * false</code>
       */
      public boolean validateMappings()
      {
         boolean valid = true;
         if(m_dbmsMapsPanel != null)
            valid = m_dbmsMapsPanel.validateMappings();
         if(valid && m_idMapsPanel != null)
            valid = m_idMapsPanel.validateMappings();
            
         return valid;
      }
      
      /**
       * Gets whether the package represented by this panel should be skipped
       * from installation. Basically gets user selection for 'Skip Package' 
       * check-box in the panel.
       * 
       * @return <code>true</code> if user selected to skip the package, 
       * otherwise <code>false</code>.
       */
      public boolean isSkipPackage()
      {
         return m_skipPackageCheck.isSelected();
      }
      
      /**
       * Sets if the skip package checkbox should be selectable. 
       * 
       * @param canSkip <code>true</code> to enable the checkbox, 
       * <code>false</code> to disable it.  If <code>false</code> and the
       * checkbox is current selected, it will be deselected before it is
       * disabled.
       */
      public void setCanSkipPackage(boolean canSkip)
      {
         if (canSkip && !m_skipPackageCheck.isEnabled())
         {
            m_skipPackageCheck.setEnabled(true);
         }
         else if (!canSkip && m_skipPackageCheck.isEnabled())
         {
            m_skipPackageCheck.setSelected(false);
            m_skipPackageCheck.setEnabled(false);
         }
      }
      
      /**
       * The package represented by this panel, initialized during construction,
       * never <code>null</code> or modified after that.
       */
      private PSDeployableElement m_package;
      
      /**
       * The panel to represent id maps, initialized in <code>init()</code> and
       * never modified after that. May be <code>null</code> if there are no id
       * mappings to represent by a panel.
       */
      private PSIDMapsPanel m_idMapsPanel = null;
      
      /**
       * The panel to represent dbms maps, initialized in <code>init()</code> 
       * and never modified after that. May be <code>null</code> if there are no
       * dbms mappings to represent by a panel.
       */
      private PSDBMSMapsPanel m_dbmsMapsPanel = null;
      
      /**
       * The check-box to allow user to skip a package to install, initialized
       * in <code>init()</code> and never <code>null</code> or modified after 
       * that.
       */
      private JCheckBox m_skipPackageCheck;


      /**
       * Updates the decoration on the packages in the list
       */
      public void stateChanged(ChangeEvent e)
      {
         // update all panels in case there are mappings for dependencies that
         // exist in multiple packages - do this asynchronously so the event
         // can finish, avoiding cell editor rendering slowness
         SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
               updatePanelIcons();
            }});
      }
   }
   
   /**
    * The list panel to hold IDType panels for each element in the <code>
    * m_elIDTypes</code>, initialized in the <code>initDialog()</code> and never
    * <code>null</code> or modified after that.
    */
   private PSListPanel m_listPanel;

   /**
    * The mode setting for the dialog, initialized in the constructor and never
    * modified after that. <code>true</code> indicates to display unmapped 
    * mappings, <code>false</code> indicates to display all mappings.
    */
   private boolean m_isTypical;
   
   /**
    * The handler used to get and save dbms and id mappings for each import 
    * package, initialized in the constructor and never <code>null</code> or 
    * modified after that.
    */
   private PSTransformsHandler m_transformsHandler;
   
   /**
    * The list of target datasrcs (<code>String</code>) that should be used to 
    * map, initialized in the constructor and never <code>null</code> or 
    * modified after that.
    */
   private List m_targetDatasources;
   
   /**
    * The archive detail consisting external dbms info, initialized in the 
    * constructor and never <code>null</code> or modified after that.
    */
   private PSArchiveDetail m_archiveDetail;
   
   /**
    * The import descriptor passed into <code>onShow(PSDescriptor)</code>, 
    * casted for convenience in {@link #init()}. Never <code>null</code> after 
    * it is initialized and descriptor state will change in <code>onNext()
    * </code>
    */
   private PSImportDescriptor m_importDescriptor = null;

}
