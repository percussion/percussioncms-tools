/******************************************************************************
 *
 * [ PSDeploymentIDTypesDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDeployableElement;
import com.percussion.deployer.objectstore.PSExportDescriptor;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSListPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The dialog used to set type for literal ids of the local dependencies of the
 * deployable elements/packages in the export descriptor.
 */
public class PSDeploymentIDTypesDialog  extends PSDeploymentWizardDialog
{   
   /**
    * Constructs this dialog calling {@link 
    * PSDeploymentWizardDialog#PSDeploymentWizardDialog(Frame, 
    * PSDeploymentServer, int, int) super(parent, server, step, sequence)}.
    * Additional parameters are described below.
    * 
    * @param isTypical if <code>true</code> it displays the dependency objects 
    * that have unidentified literal ids with their types, otherwise it displays
    * all dependency objects that have literal ids.
    * @param elementIDTypes <code>List</code> of <code>Map.Entry</code>s <code>
    * PSDeployableElement</code> as the key and the <code>Iterator</code> of 
    * <code>PSApplicationIDTypes</code> for its local dependencies as the value,
    * may not be <code>null</code> or empty. 
    * @param locator Will be used to reload the descriptor after new id types 
    * are saved if not <code>null</code>, in which case 
    * {@link PSDeploymentWizardDialog#getDescriptor() getDescriptor()} should
    * be called after this dialog returns.
    * 
    * @throws PSDeployException if there are any errors. 
    */
   public PSDeploymentIDTypesDialog(Frame parent, 
      PSDeploymentServer deploymentServer, int step, int sequence,
      boolean isTypical, List elementIDTypes, PSDescriptorLocator locator) 
      throws PSDeployException
   {
      super(parent, deploymentServer, step, sequence);
      
      if(elementIDTypes == null || elementIDTypes.isEmpty())
         throw new IllegalArgumentException(
            "elementIDTypes may not be null or empty.");         
      
      m_isTypical = isTypical;
      
      //Update the map to have List object as value rather than Iterator.
      Iterator entries = elementIDTypes.iterator();
      while(entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         List idTypes = new ArrayList();
         Iterator values = (Iterator)entry.getValue();
         while(values.hasNext())
            idTypes.add(values.next());
         entry.setValue(idTypes);
      }
      m_elIDTypes = elementIDTypes;
      m_descriptorLocator = locator;
      
      initDialog();
   }
   
   /**
    * Creates the dialog framework with border layout keeping the description 
    * panel on north, controls panel on center and command panel on south.
    * 
    * @throws PSDeployException If there are any errors.
    */
   private void initDialog() throws PSDeployException
   {
      JPanel panel = new JPanel();
      getContentPane().add(panel);
      panel.setLayout(new BorderLayout(10, 20));
      EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
      panel.setBorder(emptyBorder);
      
      setTitle(getResourceString("title"));
      int steps = 5;
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

      Iterator entries = m_elIDTypes.iterator();
      while(entries.hasNext())
      {
         Map.Entry entry = (Map.Entry)entries.next();
         PSDeployableElement element = (PSDeployableElement)entry.getKey();
         String title = element.getDisplayIdentifier();
         JPanel tabPanel;
         Iterator idTypes = ((List)entry.getValue()).iterator();
         if(idTypes.hasNext())
         {
            tabPanel= new PSIDTypesPanel(idTypes, 
               m_deploymentServer.getLiteralIDTypes(), m_deploymentServer, 
               m_isTypical);
            ((PSIDTypesPanel)tabPanel).addChangeListener(new ChangeListener() {

               public void stateChanged(ChangeEvent e)
               {
                  // see if incomplete or not and update list icon
                  Object src = e.getSource();
                  if (src instanceof PSIDTypesPanel)
                  {
                     PSIDTypesPanel typePanel = (PSIDTypesPanel)src;
                     updatePanelIcon(typePanel);                     
                  }
               }});
         }
         else
         {
            tabPanel = new JPanel();
            tabPanel.setLayout(new BorderLayout());
            tabPanel.add( new JLabel(getResourceString("noIDTypes"), 
               SwingConstants.CENTER), BorderLayout.CENTER );
         }
         m_listPanel.addPanel(title, tabPanel);
         updatePanelIcon(tabPanel);
      }
      
      // add guess all button
      JButton guessButton = new JButton(getResourceString("guessAll"));
      guessButton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e)
         {
            onGuessAll();
            
         }});
      panel.add(createCommandPanel(true, new JComponent[] {guessButton}), 
         BorderLayout.SOUTH);

      pack();
      center();    
      setResizable(true);
   }
   
   /**
    * Attempts to guess all undefined ID types in each panel's model
    */
   protected void onGuessAll()
   {
      try
      {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
         
         // walk all id types
         PSIDTypesHandler handler = new PSIDTypesHandler(m_deploymentServer);
         Iterator iter = m_elIDTypes.iterator();
         while(iter.hasNext())
         {
            Map.Entry entry = (Map.Entry)iter.next();
            PSDeployableElement element = (PSDeployableElement)entry.getKey();
            String title = element.getDisplayIdentifier();         
            List entryIdTypes = (List)entry.getValue();
            
            // guess
            handler.guessIdTypes(entryIdTypes.iterator());
            
            // Update the panel
            JPanel panel = m_listPanel.getPanel(title);
            if (panel instanceof PSIDTypesPanel)
            {
               ((PSIDTypesPanel)panel).mappingsChanged();
               updatePanelIcon(panel);
            }
         }
      }
      catch (PSDeployException e)
      {
         ErrorDialogs.showErrorMessage(this, e.getLocalizedMessage(),
            getResourceString("error"));
      }
      finally
      {
         setCursor(Cursor.getDefaultCursor());
      }
   }

   /**
    * Updates the title of the supplied panel in the list panel with the correct
    * decoration to indicate if it has incomplete mappings.
    * 
    * @param panel The panel to update, may not be <code>null</code>. 
    */
   protected void updatePanelIcon(JPanel panel)
   {
      if (panel == null)
         throw new IllegalArgumentException("panel may not be null");
      
      String icon;
      if (!(panel instanceof PSIDTypesPanel))
         icon = "gif_transparent";
      else
      {
         PSIDTypesPanel typePanel = (PSIDTypesPanel) panel;
         if (typePanel.validateMappings())
            icon = "gif_transparent";
         else
            icon = "gif_exclamation";         
      }
      
      int index = m_listPanel.getIndex(panel);
      if (index != -1)
      {
         m_listPanel.setTitleImage(index, 
            PSDeploymentClient.getImageLoader().getImage(
               PSDeploymentClient.getResources().getString(icon)));         
      }
   }

   //nothing to implement
   protected void init()
   {      
   }

   /**
    * Validates and saves the ID Types for each element on the server. Displays 
    * error message if the validation fails. Calls super's <code>onNext()</code> 
    * to make the dialog invisible.
    */
   public void onNext()
   {
      if(validateData() && saveIDTypes())
      {
         super.onNext();         
      }
   }

   // see base class
   public Object getDataToSave()
   {
      return null;
   }

   // see base class
   public void onBack()
   {      
      setShouldUpdateUserSettings(true);
      
      int panels = m_listPanel.getPanelCount();
      for (int i = 0; i < panels; i++) 
      {
         JPanel panel = m_listPanel.getPanel(i);
         if(panel instanceof PSIDTypesPanel)
         {
            PSIDTypesPanel idTypesPanel = (PSIDTypesPanel)panel;
            idTypesPanel.stopEditing();
         }
      }
      
      if (saveIDTypes())      
         super.onBack();
   }
   
   /**
    * Validates that the all literal ids in all tabs of the tabbed pane are 
    * identified. If the validation fails for any tab, displays an error 
    * message and makes that tab as the current selected tab.
    * 
    * @return <code>true</code> if the validation succeeds, otherwise <code>
    * false</code>
    */
   protected boolean validateData()
   {
      int panels = m_listPanel.getPanelCount();
      for (int i = 0; i < panels; i++) 
      {
         JPanel panel = m_listPanel.getPanel(i);
         if(panel instanceof PSIDTypesPanel)
         {
            PSIDTypesPanel idTypesPanel = (PSIDTypesPanel)panel;
            idTypesPanel.stopEditing();
            if(!idTypesPanel.validateMappings())
            {
               String title = m_listPanel.getTitle(i);
               String msg = MessageFormat.format(
                  getResourceString("unIdentifiedIds"), new String[]{ title });
               ErrorDialogs.showErrorMessage(this, msg,
                  getResourceString("error"));               
               m_listPanel.setSelectedPanel(idTypesPanel);
               return false;
            }
         }
      }
      return true;
   }
   
   /**
    * Saves current id type defintions to the server, and reloads the 
    * descriptor to update it with any changes that result from defined ID 
    * types.  Does not validate that all mappings are completed (nor is this
    * required).  Use {@link #validateData()} if such validation is desired
    * before saving.
    * 
    * @return <code>true</code> if the operation completes successfully, 
    * <code>false</code> if there were errors.  Any handled exceptions are
    * presented to the user in error dialogs.
    */
   private boolean saveIDTypes()
   {
      boolean didSave = false;
      List idTypes = new ArrayList();
      Iterator iter = m_elIDTypes.iterator();
      while(iter.hasNext())
      {
         Map.Entry entry = (Map.Entry)iter.next();
         List entryIdTypes = (List)entry.getValue();
         Iterator elementIDTypes = entryIdTypes.iterator();
         while(elementIDTypes.hasNext())
            idTypes.add(elementIDTypes.next());
      }
      
      try 
      {
         m_deploymentServer.getDeploymentManager().saveIdTypes(
            idTypes.iterator());
         
         // now reload the descriptor if we have a locator
         if (m_descriptorLocator != null)
         {
            PSExportDescriptor curDesc = (PSExportDescriptor)m_descriptor;
            PSExportDescriptor fixedDesc = m_descriptorLocator.load(
               m_deploymentServer);
            // remove any packages that may have been "deselected"
            List pkgList = new ArrayList();
            Iterator pkgs = curDesc.getPackages();
            while (pkgs.hasNext())
            {
               PSDeployableElement pkg = (PSDeployableElement)pkgs.next();
               PSDeployableElement newPkg = fixedDesc.getPackage(
                  pkg.getKey());
               if (newPkg != null)
                  pkgList.add(newPkg); 
               else
                  pkgList.add(pkg); 
            }
            
            curDesc.setPackages(pkgList.iterator());               
         }
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
    * The list panel to hold IDType panels for each element in the <code>
    * m_elIDTypes</code>, initialized in the <code>initDialog()</code> and never
    * <code>null</code> or modified after that.
    */
   private PSListPanel m_listPanel;

   /**
    * The mode setting for the dialog, initialized in the constructor and never
    * modified after that. <code>true</code> indicates to display the dependency
    * objects that have literal ids unidentified with their types, <code>false
    * </code> indicates to display all dependency objects that have literal ids.
    */
   private boolean m_isTypical;
   
   /**
    * A List of <code>Map.Entry</code> objects with a 
    * <code>PSDeployableElement</code> as the key and a <code>List</code> of 
    * <code>PSApplicationIDTypes</code> for its local dependencies as the value. 
    * Each entry in the list represents a tab in the dialog. If the 
    * <code>List</code> of id types is empty, displays a message 
    * in the tab panel that this does not have any dependency objects with 
    * literal ids to identify. Initialized in the constructor and never <code>
    * null</code> or modified after that. The <code>PSApplicationIDTypes</code> 
    * will be modified as user updates the type for each literal id.
    */
   private List m_elIDTypes;

   /**
    * Locator used to re-load a saved descriptor after saving id types.
    */
   private PSDescriptorLocator m_descriptorLocator;
}
