/*[ PSContentTypeConfigTabPanel.java ]******************************************
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
import com.percussion.loader.objectstore.PSFieldProperty;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;


/**
 * This class defines the name, community id, content type and all system
 * fields for an extractor. It is typically used as one of the sub
 * tab panel within the main configuration panel of an extractor.
 */
public class PSContentTypeConfigTabPanel
   extends PSAbstractExtractorConfigTabPanel
   implements ItemListener
{

   /**
    * Constructs a new <code>PSContentTypeConfigTabPanel</code> object
    * @param allowXPath if <code>true</code> then XPath expressions will
    * be allowed in the value map table.
    */
   public PSContentTypeConfigTabPanel(boolean allowXPath)
   {
      init(allowXPath);
   }

   /**
    * Constructs a new <code>PSContentTypeConfigTabPanel</code> object    
    */
  public PSContentTypeConfigTabPanel()
   {
      init(false);
   }

   /**
    * Initializes the gui components for this panel
    */
   private void init(boolean allowXPath)
   {

      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      // Create borders
      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
            ms_res.getString("field.label.valueMap"));

      PSPropertyPanel bodyPanel = new PSPropertyPanel();
      bodyPanel.setAlignmentX(RIGHT_ALIGNMENT);
      bodyPanel.setLabelAlignment(LEFT_ALIGNMENT);
      bodyPanel.setBorder(b1);

      // Name field
      m_nameTextField = new UTFixedHeightTextField();
      JComponent[] j1 = {m_nameTextField};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.name"), j1);

      // Community field
      m_commComboField = new JComboBox();
      m_commComboField.addItemListener(this);
      JComponent[] j2 = {m_commComboField};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.community"), j2);

      // Content type field
      m_contentComboField = new JComboBox();
      m_contentComboField.addItemListener(this);
      JComponent[] j3 = {m_contentComboField};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.contentType"), j3);

      // System field-value map panel
      JPanel tablePanel = new JPanel(new BorderLayout());
      tablePanel.setBorder(b);
      tablePanel.setPreferredSize(new Dimension(100, 100));
      tablePanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
      m_valueMapTable = PSFieldValueMapTable.newInstance(allowXPath);
      JScrollPane scrollpane = new JScrollPane(m_valueMapTable);
      tablePanel.add(scrollpane, BorderLayout.CENTER);
      add(bodyPanel);
      add(tablePanel);

      // Add change listener
      addChangeListener(new ExtractorConfigChangeListener());
   }


   // implements IPSExtractorConfigTabPanel interface method
   public void load(PSExtractorConfigContext configCtx)
      throws PSLoaderException
   {
      if(null == configCtx)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");
      m_configCtx = configCtx;
      m_isLoading = true;
      try
      {
         Iterator entries = null;
         // Load name
         m_nameTextField.setText(configCtx.getExtractorDef().getName());

         // Load community combo box
         entries = configCtx.getCommunities();
          m_commComboField.removeAllItems();
         while(entries.hasNext())
            m_commComboField.addItem(entries.next());
         if(null == configCtx.getCommunity())
         {
            // If no selection then select first item in the list
            if(m_commComboField.getItemCount() > 0)
               configCtx.setCommunity((PSEntry)m_commComboField.getItemAt(0));
         }
         m_commComboField.setSelectedItem(configCtx.getCommunity());


         // Load content type combo box
         entries = configCtx.getContentTypes();
         m_contentComboField.removeAllItems();
         while(entries.hasNext())
            m_contentComboField.addItem(entries.next());
         if(null == configCtx.getContentType())
         {
            // If no selection then select first item in the list
            if(m_contentComboField.getItemCount() > 0)
               configCtx.setContentType(
                   (PSEntry)m_contentComboField.getItemAt(0));
         }
         m_contentComboField.setSelectedItem(configCtx.getContentType());

         // Load system field value map table
         PSFieldValueMapTableModel model =
            (PSFieldValueMapTableModel)m_valueMapTable.getModel();
         model.load(configCtx.getAvailableFields(configCtx.getContentType(),
             true));
         model.setData(configCtx.getDefinedFields(true));

      }
      finally
      {
         m_isLoading = false;
      }

   }


   // implements IPSExtractorConfigTabPanel interface method
   public void save(PSExtractorConfigContext configCtx)
      throws PSLoaderException
   {
      if(null == configCtx)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      if(!validateContent())
         return;

      // Update name
      configCtx.getExtractorDef().setName(getName());

      // Update community
      if(null != m_commComboField.getSelectedItem())
         configCtx.setCommunity((PSEntry)m_commComboField.getSelectedItem());

      // Update content type
      if(null != m_contentComboField.getSelectedItem())
         configCtx.setContentType(
             (PSEntry)m_contentComboField.getSelectedItem());

      // Update system fields
      PSFieldValueMapTableModel model =
          (PSFieldValueMapTableModel)m_valueMapTable.getModel();
      configCtx.setDefinedFields(model.getDataList(), true);

   }

   // implements IPSExtractorConfigTabPanel interface method
   public boolean validateContent()
   {
      PSFieldValueMapTableModel model =
         (PSFieldValueMapTableModel)m_valueMapTable.getModel();


      // name is required
      if(getName() == null || getName().trim().length() == 0)
      {
          ErrorDialogs.showErrorDialog(PSContentTypeConfigTabPanel.this,
             ms_res.getString("error.name.required"),
             ms_res.getString("error.title.validationexception"),
             JOptionPane.ERROR_MESSAGE);
         return false;
      }

      // sys_title is required
      Iterator it = model.getData();
      boolean hasSysTitle = false;
      while(it.hasNext())
      {
         PSFieldProperty prop = (PSFieldProperty)it.next();
         if(prop.getName().equalsIgnoreCase("sys_title"))
         {
            hasSysTitle = true;
            break;
         }
      }

      if(!hasSysTitle)
      {
          ErrorDialogs.showErrorDialog(PSContentTypeConfigTabPanel.this,
             ms_res.getString("error.systitle.required"),
             ms_res.getString("error.title.validationexception"),
             JOptionPane.ERROR_MESSAGE);
         return false;
      }
      return model.validate();
   }

   // implements IPSExtractorConfigTabPanel interface method
   public String getName()
   {
      return m_nameTextField.getText();
   }

   // implements ItemListener interface method
   public void itemStateChanged(ItemEvent event)
   {
      Object source = event.getSource();

      int type = 0;
      // Fire change event on selection change for community
      // and content type combo boxes
      if(source == m_commComboField)
      {
         type = PSExtractorConfigChangeEvent.VALUE_TYPE_COMMUNITY;
      }
      if(source == m_contentComboField)
      {
         type = PSExtractorConfigChangeEvent.VALUE_TYPE_CONTENTTYPE;
      }
      fireConfigChange(type);
   }

   /**
    * Stop table editing
    */
   public void stopTableEditing()
   {
      m_valueMapTable.stopCellEditing();
   }

   /**
    * It handles the operations whenever a new community or content type has
    * been selected.
    */
   private class ExtractorConfigChangeListener
      implements IPSExtractorConfigChangeListener
   {
      // Implements IPSExtractorConfigChangeListener#configChanged()
      public void configChanged(PSExtractorConfigChangeEvent event)
      {
         if(m_isLoading)
            return;
         if(event.getType() ==
            PSExtractorConfigChangeEvent.VALUE_TYPE_COMMUNITY
               && m_commComboField.getSelectedItem() != null)
         {
            m_configCtx.getExtractorDef().setName(getName());
            m_configCtx.setCommunity(
                (PSEntry)m_commComboField.getSelectedItem());
            try
            {
               reset(m_configCtx);
            }
            catch(PSLoaderException e)
            {
               ErrorDialogs.showErrorDialog(PSContentTypeConfigTabPanel.this,
                  e.getMessage(),
                  ms_res.getString("error.title.remoteexception"),
                  JOptionPane.ERROR_MESSAGE);
            }
         }
         if(event.getType() ==
            PSExtractorConfigChangeEvent.VALUE_TYPE_CONTENTTYPE
               && m_contentComboField.getSelectedItem() != null)
         {

            m_configCtx.getExtractorDef().setName(getName());
            m_configCtx.setContentType(
               (PSEntry)m_contentComboField.getSelectedItem());
            try
            {
               reset(m_configCtx);
            }
            catch(PSLoaderException e)
            {
               ErrorDialogs.showErrorDialog(PSContentTypeConfigTabPanel.this,
                  e.getMessage(),
                  ms_res.getString("error.title.remoteexception"),
                  JOptionPane.ERROR_MESSAGE);
            }
         }
      }
   }

   /**
    * Extractor name field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private UTFixedHeightTextField m_nameTextField;

   /**
    * Community combo box field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JComboBox m_commComboField;

   /**
    * Content type combo box field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JComboBox m_contentComboField;

   /**
    * System field-value map table. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private PSFieldValueMapTable m_valueMapTable;

   /**
    * Flag indicating that the panel is currently loading.
    */
   private boolean m_isLoading = false;

   /**
    * This extractor's configuration context. Initialized in
    * {@link #load(PSExtractorConfigContext)}, never <code>null</code>
    *  after that.
    */
   private PSExtractorConfigContext m_configCtx;

   /**
    * Resource bundle for this class. Initialized once in {@link #init()},
    * never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;


}