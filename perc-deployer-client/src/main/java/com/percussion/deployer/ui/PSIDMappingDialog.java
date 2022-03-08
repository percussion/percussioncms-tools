/******************************************************************************
 *
 * [ PSIDMappingDialog.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.deployer.ui;

import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.deployer.catalog.PSCatalogResult;
import com.percussion.deployer.objectstore.PSIdMapping;
import com.percussion.error.PSDeployException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The dialog for adding/editing id mapping.
 */
public class PSIDMappingDialog extends PSDialog implements ItemListener
{
   /**
    * Construct this class with all required parameters.
    *
    * @param parent the parent window of this dialog, may be <code>null</code>.
    *
    * @param type, represents the object type may not be code>null</code>.
    *
    * @param idMap mapping within a {@link 
    * com.percussion.deployer.objectstore.PSIdMap }. Supply <code>null</code> to
    * create a new mapping, otherwise the mapping to be edited.
    * 
    * @param idMapsHandler the handler that should be used to get source and 
    * target elements that have ids to map, may not be <code>null</code>
    * 
    * @param sourceServer the source server to get the source elements, may not
    * be <code>null</code> and must be connected to create a new mapping.
    *
    * @throws IllegalArgumentException if any of the required parameters
    * are invalid.
    */
   public PSIDMappingDialog(Dialog parent, PSCatalogResult type,
      PSIdMapping idMap, PSTransformsHandler idMapsHandler, 
      PSDeploymentServer sourceServer) throws PSDeployException
   {
      super(parent);

      if (type == null)
         throw new IllegalArgumentException("Element type cannot be null.");
         
      if(idMapsHandler == null)
         throw new IllegalArgumentException("idMapsHandler cannot be null.");

      if ( idMap == null && 
         (sourceServer == null || !sourceServer.isConnected()) )
      {
         throw new IllegalArgumentException("source server may not be null and"
            + "must be connected in case of creating a new mapping.");
      }

      m_idMapsHandler = idMapsHandler;
      m_sourceServer = sourceServer;
      m_type = type;
      m_idMap = idMap;      
      
      initDialog();
   }

   /**
    * Loads the combo box with data from supplied iterator. Sorts the displayed 
    * data lexicographically. If the supplied combo is a 'Target' combo-box it
    * adds an empty element to the list to give an option to the user to choose
    * to 'Add to server'.
    * 
    * @param elements the list of <code>PSMappingElement</code> objects that 
    * need to be added to the combo-box, assumed not to be <code>null</code>.
    * @param combo the combo-box that need to be populated, assumed not to be 
    * <code>null</code>.
    */
   private void populateCombo(List elements, JComboBox combo)
   {      
      Collections.sort(elements);

      combo.removeAllItems();    
      Iterator iter = elements.iterator();
      while(iter.hasNext())
         combo.addItem(iter.next());
      //If it is target we will add an empty string to choose to add to server.
      if(combo == m_targetCombo)
         combo.addItem(new EmptyElement());              
   }

   /**
    * Initializes the dialog framework.
    */
   protected void initDialog() throws PSDeployException
   {
      setTitle(getResourceString("title"));
      JPanel mainpanel = new JPanel();

      mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
      mainpanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

      PSPropertyPanel bodyPanel = new PSPropertyPanel();

      m_typeField = new UTFixedHeightTextField();
      m_typeField.setEnabled(false);
      JComponent[] type = {m_typeField};
      bodyPanel.add(Box.createRigidArea(new Dimension(0, 5)));
      bodyPanel.add(Box.createVerticalGlue());
      bodyPanel.addPropertyRow(getResourceString("type"), type);

      bodyPanel.add(Box.createRigidArea(new Dimension(0, 5)));
      bodyPanel.add(Box.createVerticalGlue());
      
      m_srcCombo = new JComboBox();
      m_srcCombo.addItemListener(this);
      JComponent[] src = {m_srcCombo};
      bodyPanel.addPropertyRow(getResourceString("src"), src);

      bodyPanel.add(Box.createRigidArea(new Dimension(0, 5)));
      bodyPanel.add(Box.createVerticalGlue());
            
      m_targetCombo = new JComboBox();
      m_targetCombo.addItemListener(this);
      JComponent[] target = {m_targetCombo};
      bodyPanel.addPropertyRow(getResourceString("target"), target);

      bodyPanel.add(Box.createRigidArea(new Dimension(0, 5)));
      bodyPanel.add(Box.createVerticalGlue());

      JPanel cbPanel = new JPanel();
      cbPanel.setLayout(new BoxLayout(cbPanel, BoxLayout.X_AXIS));
      m_addtoServer.setText(getResourceString("cblabel"));
      m_addtoServer.addItemListener(this);
      cbPanel.add(Box.createRigidArea(new Dimension(8, 10)));
      cbPanel.add(m_addtoServer);
      cbPanel.add(Box.createHorizontalGlue());

      JPanel cmdPanel = new JPanel();
      cmdPanel.setLayout(new BoxLayout(cmdPanel, BoxLayout.X_AXIS));
      cmdPanel.add(Box.createHorizontalGlue());
      cmdPanel.add(createCommandPanel(SwingConstants.HORIZONTAL, true));
      cmdPanel.add(Box.createVerticalGlue());
      mainpanel.add(bodyPanel);
      mainpanel.add(cbPanel);
      mainpanel.add(Box.createRigidArea(new Dimension(0,10)));
      mainpanel.add(cmdPanel);
      getContentPane().add(mainpanel);
      
      init();
      pack();      
      center();
      setResizable(true);
   }
   
   /**
    * Checks whether the dialog is invoked to create a new mapping.
    * 
    * @return <code>true</code> if it is in new mode, otherwise <code>false
    * </code>
    */
   private boolean isNewMapping()
   {
      return m_idMap == null;
   }
   
   /**
    * Handles the item state changed events for 'Source' and 'Target' 
    * combo-boxes. 
    * <ol>
    * <li>When the selected source element has parent, it updates the 'Target'
    * combo-box with the unmapped target elements for that source element</li>
    * <li>When the selected target element is empty target, it makes the 'Add to
    * Server' check-box as selected, otherwise as deselected.</li>
    * </ol>
    * 
    * @param e the item changed event, assumed not to be <code>null</code> as 
    * this method will be called by <code>Swing</code> model when an item 
    * selection changes in combo-box.
    */
   public void itemStateChanged(ItemEvent e)
   {
      if(e.getStateChange() == ItemEvent.SELECTED)
      {
         /*
          * Based on the selection in <code>m_sourceCombo</code> fill the target
          * elements. The target elements are different for each source element
          * if the source element supports parent, otherwise they will be same,
          * so in case of source element supports parent, it refreshes the 
          * target combo otherwise not.
          */
         if(e.getSource() == m_srcCombo)
         {         
            try {
               PSMappingElement source = (PSMappingElement) e.getItem();
               if(source.hasParent())
               {
                  if(!validateSource())
                     return;
                  List targetElements = 
                     m_idMapsHandler.getUnmappedTargetElements(source.getType(),
                     source.getParentType(), source.getParentId());
                  populateCombo(targetElements, m_targetCombo);
               }                    
               PSMappingElement targetEl = m_idMapsHandler.guessTarget(source);
               if(targetEl == null)
                  m_targetCombo.setSelectedItem(new EmptyElement());
               else
                  m_targetCombo.setSelectedItem(targetEl);
            }
            catch(PSDeployException ex)
            {
               ErrorDialogs.showErrorMessage(PSIDMappingDialog.this, 
                  ex.getLocalizedMessage(), getResourceString("error"));
            }           
         }
         /*
          * Based on the selection in <code>m_targetCombo</code> 'Add to server'
          * check box is selected or unselected. If a target element is chosen,
          * the check-box is unselected or if an empty string is chosen, the 
          * check-box is selected.
          */         
         else if(e.getSource() == m_targetCombo)
         {            
            if(e.getItem() instanceof EmptyElement)
               m_addtoServer.setSelected(true);
            else
               m_addtoServer.setSelected(false);    
         }
         else if (e.getSource() == m_addtoServer)
         {
            if ( m_addtoServer.isSelected() )
               m_targetCombo.setSelectedItem(new EmptyElement());
         }
      }
      else if ( e.getStateChange() == ItemEvent.DESELECTED)
      {
         if (e.getSource() == m_addtoServer)
         {
            m_targetCombo.setSelectedItem(m_srcCombo.getSelectedItem());
         }
      }
   }

   /**
    * Initializes the dialog fields ('Source', 'Target' combo-boxes and 
    * 'Add to server' check-box) with data appropriately. 
    * <li>Element Type field is set with type</li>
    * <li>In case of new mapping populates the 'Source' combo with unmapped 
    * source elements and 'Target' combo with unmapped target elements if the
    * element type has no parent type.</li>
    * <li>In case of editing the mapping, updates 'Source' combo with mapping's
    * source element and makes it disabled. Updates the 'Target' combo with
    * unmapped target elements and 'Add to server' check-box is selected if the
    * mapping is set with target as new object.</li>
    * 
    */
   private void init() throws PSDeployException
   {
      //update type
      m_typeField.setText(m_type.getDisplayText()); 
      
      if (isNewMapping()) //if new update source elements
      {
         /* Fill the target elements before source elements because populating
          * the source combo-box causes selecting a source element and
          * selecting an element in source causes target to be guessed and
          * so target should have the elements for selecting.
          */
               
         //If the element type does not support or have parent type, then we 
         //fill the target elements also because they are not going to be 
         //changed.
         if(!m_idMapsHandler.hasParentType(m_type.getID()))
         {
            populateCombo( m_idMapsHandler.getUnmappedTargetElements(
               m_type.getID(), null, null), m_targetCombo );
         }            
         
         //fill source elements.
         populateCombo( m_idMapsHandler.getUnmappedSourceElements(
            m_sourceServer, m_type.getID()), m_srcCombo );
      }
      else //editing update all fields from the mapping.
      {
         String id = m_idMap.getSourceId();
         String name = m_idMap.getSourceName();
         PSMappingElement sourceElement = 
            new PSMappingElement(m_type.getID(), id, name);
         if(m_idMap.getParentType() != null)
         {
            sourceElement.setParent(m_idMap.getParentType(), 
               m_idMap.getSourceParentId(), m_idMap.getSourceParentName());
         }
         m_srcCombo.addItem(sourceElement);
         m_srcCombo.setEnabled(false);
         
         if(m_idMap.isMapped())
         {
            List unmappedTargets = 
               m_idMapsHandler.getUnmappedTargetElements(
               m_type.getID(), sourceElement.getParentType(), 
               sourceElement.getParentId());   
            PSMappingElement targetElement = new EmptyElement();
            if (!m_idMap.isNewObject())
            {
               id = m_idMap.getTargetId();
               name = m_idMap.getTargetName();
               targetElement = 
                  new PSMappingElement(m_type.getID(), id, name);
               if(m_idMap.getParentType() != null)
               {
                  targetElement.setParent(m_idMap.getParentType(), 
                     m_idMap.getTargetParentId(), 
                     m_idMap.getTargetParentName());
               }            
               unmappedTargets.add(targetElement);               
            }            
            populateCombo( unmappedTargets, m_targetCombo );                                    
            m_targetCombo.setSelectedItem(targetElement);            
         }
      }
   }
   
   /**
    * Validates that the selected source element's parent is mapped if it has a
    * parent. Displays an error message if the parent is not mapped.
    * 
    * @return <code>true</code> if the source element does not have a parent or
    * its parent is mapped, otherwise <code>false</code>
    */
   private boolean validateSource()
   {
      PSMappingElement source = (PSMappingElement)m_srcCombo.getSelectedItem();
      if(source.hasParent())
      {
         if(!m_idMapsHandler.getIdMap().isMapped(source.getParentId(), 
            source.getParentType()))
         {
            if(isShowing())
            {
               try {
                  String[] args = { 
                     source.getParentName() + "(" + source.getParentId() + ")",
                     m_idMapsHandler.getTarget().getTypeDisplayName(
                     source.getParentType()), 
                     source.toString() };
                  ErrorDialogs.showErrorMessage(this, 
                     MessageFormat.format(getResourceString("mustMapParent"), 
                     args), getResourceString("error"));     
               }
               catch(PSDeployException ex)
               {
                  ErrorDialogs.showErrorMessage(this, 
                     ex.getLocalizedMessage(), getResourceString("error"));
               }
            }                                     
            return false;
         }
      }
      return true;
   }
   
   /**
    * Validates that source and target are chosen. Displays an error message if
    * any of the data is invalid.
    * 
    * @return <code>true</code> if the validation succeeds, otherwise <code>
    * false</code>
    */
   private boolean validateData()
   {
      boolean isValid = true;
      PSMappingElement source = (PSMappingElement)m_srcCombo.getSelectedItem();
      if(source == null)
      {
         ErrorDialogs.showErrorMessage(this, 
            MessageFormat.format(getResourceString("mustSelectSource"), 
            new String[] {source.toString()}), 
            getResourceString("error"));                                          
         isValid = false;
      }
      else if(!validateSource())
         isValid = false;
      else if(!m_addtoServer.isSelected())
      {
         PSMappingElement target = (PSMappingElement)m_targetCombo.getSelectedItem();
         if(target == null)
         {
            ErrorDialogs.showErrorMessage(this, 
               MessageFormat.format(getResourceString("mustSelectTarget"), 
               new String[] {source.toString()}), 
               getResourceString("error"));                                          
            isValid = false;
         }
      }
      
      return isValid;
   }

   /**
    * Validates that source and target are specified. If the validation succeeds
    * creates a new mapping if the dialog is invoked to created a new mapping,
    * otherwise updates the mapping supplied to the dialog. Calls super's 
    * <code>onOk()</code> to dispose off the dialog.
    */
   public void onOk()
   {            
      if(!validateData())
         return;
            
      PSMappingElement source = (PSMappingElement)m_srcCombo.getSelectedItem();;
      if (isNewMapping())
      {
         if(source.hasParent())
         {          
            m_idMap = new PSIdMapping(source.getId(), source.getName(), 
               m_type.getID(), source.getParentId(), source.getParentName(), 
               source.getParentType(), true);
         }
         else
         {
            m_idMap = new PSIdMapping(source.getId(), source.getName(), 
               m_type.getID(), true);
         }
      }

      if (m_addtoServer.isSelected())
      {
         m_idMap.setIsNewObject(true);
      }
      else
      {
         PSMappingElement element = 
            (PSMappingElement)m_targetCombo.getSelectedItem();
         m_idMap.setIsNewObject(false);
         m_idMap.setTarget(element.getId(), element.getName(), 
            element.getParentId(), element.getParentName());
      }
      super.onOk();
   }

   /**
    * Gets <code>PSIdMapping</code> object. This should be called only when
    * control to the caller is returned after the dialog is disposed by clicking
    *  OK which can be determined by {@link #isOk()}.
    * @return never <code>null</code>.
    */
   public PSIdMapping getIdMapping()
   {
      return m_idMap;
   }
   
   /**
    * Utility class to represent an empty element. 
    * Note:This class is implemented because <code>JComboBox</code> requires 
    * same instances of objects in the list to work properly with selection. 
    * Instead of this element if an empty <code>String</code> is added, then it
    * is displaying properly in the combo-box, but it is not firing the <code>
    * ItemChanged</code> event when that particular item is selected.
    */
   private class EmptyElement extends PSMappingElement
   {      
      /**
       * Constructs an empty element without data.
       */
      public EmptyElement()
      {
         super();
      }
      
      /**
       * Overrides equals method to return <code>true<code> only if the
       * instance of the element is also EmptyElement.
       */
      @Override
      public boolean equals(Object obj)
      {
         return obj instanceof EmptyElement;
      }
      
      /**
       * Returns constant for any EmptyElement.
       */
      @Override
      public int hashCode()
      {
         return 1;
      }

      /**
       * Gets the string representation of this element, an empty string.
       * 
       * @return an empty string, never <code>null</code>
       */
      public String toString()
      {
         return "";
      }
   }

   /**
    * The text field to show element type. Initialised in <code>initDialog()
    * </code> and never <code>null</code> or modified after that.
    */
   private UTFixedHeightTextField m_typeField;

   /**
    * Represents source elements, initialised in <code>initDialog()</code>, 
    * updated in <code>populateCombo(Iterator, JComboBox)</code>, where data is
    * added to it. If a mapping is being edited then it is disabled, displaying 
    * source element from the supplied mapping. Never <code>null</code> or 
    * modified after that.
    */
   private JComboBox m_srcCombo;

   /**
    * Represents target elements, initialised in <code>initDialog()</code>, 
    * modified in <code>populateCombo(Iterator, JComboBox)
    * </code>, where data is added to it. Never <code>null</code> after that.
    */
   private JComboBox m_targetCombo;

   /**
    * Represents the object type, initialised in ctor, never code>null</code> 
    * or modified after that.
    */
   private PSCatalogResult m_type;

   /**
    * Represents mapping within a {@link 
    * com.percussion.deployer.objectstore.PSIdMap }. Initialised in ctor. May be
    * <code>null</code> if the dialog is invoked to create a new mapping, but
    * will be set with the created mapping in <code>onOk()</code>.
    */
   private PSIdMapping m_idMap;
   
   /**
    * The handler to use to get the source and target elements to map, 
    * initialized in the constructor and never <code>null</code> or modified 
    * after that.
    */
   private PSTransformsHandler m_idMapsHandler;
   
   /**
    * The source deployment server to get the source elements, initialized in 
    * the constructor, may be <code>null</code> if the dialog is invoked to edit
    * the mapping rather than to create a new mapping. Never modified after it
    * is initialized.
    */
   private PSDeploymentServer m_sourceServer;

   /**
    * Represents 'Add to server' check box. Always disabled. Initialsed in ctor.
    * Never <code>null</code>. If target element exist for an
    * <code>PSIdMapping</code> it's unselected or else selected.
    */
   private final JCheckBox m_addtoServer = new JCheckBox();
}
