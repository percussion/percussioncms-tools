/*[ PSLocationConfigTabPanel.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSStaticItemExtractorDef;

import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.Border;


/**
 * This is the location tab-panel used in defining the static extractor 
 * definition.
 */
public class PSLocationConfigTabPanel extends PSAbstractExtractorConfigTabPanel
{
   public PSLocationConfigTabPanel()
   {
      init();
   }

   /**
    * Initializes the gui components for this panel
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      // Create borders
      Border border = BorderFactory.createEmptyBorder(10, 10, 10, 10 );

      PSPropertyPanel bodyPanel = new PSPropertyPanel();
      bodyPanel.setAlignmentX(RIGHT_ALIGNMENT);
      bodyPanel.setLabelAlignment(LEFT_ALIGNMENT);
      bodyPanel.setBorder(border);

      // Name field
      m_nameTextField = new UTFixedHeightTextField();
      JComponent[] j1 = {m_nameTextField};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.name"), j1);

      // Context variable
      m_ctxVariable = new JComboBox();
      JComponent[] j2 = {m_ctxVariable};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.ctxvariable"), j2);
      
      // Source field
      m_sourceTextField = new UTFixedHeightTextField();
      JComponent[] j3 = {m_sourceTextField};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.source"), j3);

      add(bodyPanel);
   }

   // implements IPSExtractorConfigTabPanel interface method
   public boolean validateContent()
   {
      String name = m_nameTextField.getText();
      if (name == null || name.trim().length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.name"),
         getResourceString("error.title.name"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
         
      String sourceLocation = m_sourceTextField.getText();
      if (sourceLocation == null || sourceLocation.trim().length() == 0)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.sourcenotfound"),
         getResourceString("error.title.sourcenotfound"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
      
      if (m_ctxVariable.getItemCount() == 0 ||
          m_ctxVariable.getSelectedItem() == null)
      {
         ErrorDialogs.showErrorDialog(this,
         getResourceString("error.msg.ctxvariable"),
         getResourceString("error.title.ctxvariable"),
         JOptionPane.ERROR_MESSAGE);
         return false;
      }
         
      return true;
   }
   
   // implements IPSExtractorConfigTabPanel interface method
   public void load(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      m_config = config;
      PSStaticItemExtractorDef def = 
         (PSStaticItemExtractorDef) m_config.getExtractorDef();
      Iterator varEntries = null;
      String sourceLocation = "";
      sourceLocation = def.getSourceLocation();
      varEntries = m_config.getContextVariables();
      
      // Load name
      m_nameTextField.setText(def.getName());
      
      // Load source location
      m_sourceTextField.setText(sourceLocation);
      
      // Load context variable combo box
      m_ctxVariable.removeAllItems();
      while(varEntries.hasNext())
         m_ctxVariable.addItem(varEntries.next());
         
      PSEntry ctxVariable = m_config.getContextVariable();
      if(ctxVariable == null)
      {
         // If no selection then select first item in the list
         if (m_ctxVariable.getItemCount() > 0)
         {
            ctxVariable = (PSEntry)m_ctxVariable.getItemAt(0);
            m_config.setContextVariable(ctxVariable);
         }
      }
      if (ctxVariable != null)
         m_ctxVariable.setSelectedItem(ctxVariable);
   }

   // implements IPSExtractorConfigTabPanel interface method
   public void save(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");
            
      PSStaticItemExtractorDef def = 
         (PSStaticItemExtractorDef) m_config.getExtractorDef();

      // Update name
      def.setName(getName());
      def.setSourceLocation(m_sourceTextField.getText());
      
      PSEntry ctxVariable = (PSEntry) m_ctxVariable.getSelectedItem();
      def.setTargetLocation(ctxVariable.getValue());
      def.setContextVariableName(ctxVariable.getLabel().getText());
   }

   // implements IPSExtractorConfigTabPanel interface method
   public String getName()
   {
      return m_nameTextField.getText();
   }

   /**
    * Gets the resource mapping for the supplied key.
    *
    * @param key, may not be <code>null</code>
    * @return mapping corresponding to the key, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   private String getResourceString(String key)
   {
      return PSContentLoaderResources.getResourceString(ms_res, key);
   }

   /**
    * This extractor's configuration context. Initialized in ctor,
    * never <code>null</code> after that.
    */
   private PSExtractorConfigContext m_config;

   /**
    * Extractor name field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private UTFixedHeightTextField m_nameTextField;

   /**
    * The combo box for the context variable, Initialized in {@link #init()}, 
    * never <code>null</code> after that.
    */
   private JComboBox m_ctxVariable;
   
   /**
    * Source location field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private UTFixedHeightTextField m_sourceTextField;


   /**
    * Resource bundle for this class. Initialized once in {@link #init()},
    * never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;

}