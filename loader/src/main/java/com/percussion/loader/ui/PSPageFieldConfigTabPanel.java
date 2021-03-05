/*[ PSPageFieldConfigTabPanel.java ]******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSEntry;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSPageExtractorDef;
import com.percussion.loader.objectstore.PSProperty;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

public class PSPageFieldConfigTabPanel extends PSAbstractExtractorConfigTabPanel
{


   /**
    * Constructs a new <code>PSPageFieldConfigTabPanel</code> object
    *
    */
   public PSPageFieldConfigTabPanel()
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
      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
            ms_res.getString("field.label.valueMap"));

      PSPropertyPanel bodyPanel = new PSPropertyPanel();
      bodyPanel.setAlignmentX(RIGHT_ALIGNMENT);
      bodyPanel.setLabelAlignment(LEFT_ALIGNMENT);
      bodyPanel.setBorder(b1);

      // Encoding field
      m_encodingComboField = new JComboBox();
      m_encodingComboField.setEditable(true);
      JComponent[] j1 = {m_encodingComboField};
      bodyPanel.addPropertyRow(ms_res.getString("field.label.encoding"), j1);

      // field-value map panel
      JPanel tablePanel = new JPanel(new BorderLayout());
      tablePanel.setBorder(b);
      tablePanel.setPreferredSize(new Dimension(100, 100));
      m_valueMapTable = PSFieldValueMapTable.newInstance(false);
      JScrollPane scrollpane = new JScrollPane(m_valueMapTable);
      tablePanel.add(scrollpane, BorderLayout.CENTER);
      add(bodyPanel);
      add(tablePanel);


   }

   // implements IPSExtractorConfigTabPanel interface method
   public void load(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      PSExtractorDef def = config.getExtractorDef();
      String defaultEncoding = System.getProperty(PROPNAME_FILE_ENCODING);
      PSProperty prop = PSLoaderUtils.getOptionalProperty(
         PSPageExtractorDef.ENCODING,
         def.getProperties());

      String selectedEncoding = null;
      m_encodingComboField.addItem(defaultEncoding);
      if(null == prop)
      {
         selectedEncoding = defaultEncoding;
      }
      else
      {
         selectedEncoding = prop.getValue();
         m_encodingComboField.addItem(selectedEncoding);
      }
      m_encodingComboField.setSelectedItem(selectedEncoding);


      // Load field value map table
      PSFieldValueMapTableModel model =
         (PSFieldValueMapTableModel)m_valueMapTable.getModel();
      PSEntry contentType = getContentType(config);
      Iterator fields = new ArrayList().iterator();
      if(null != contentType)
      {
         fields = config.getAvailableFields(contentType, false);
      }
      model.load(fields);
      model.setData(config.getDefinedFields(false));

   }

   // implements IPSExtractorConfigTabPanel interface method
   public void save(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

      if(!validateContent())
         return;

      // Update encoding
      PSExtractorDef def = config.getExtractorDef();
      PSProperty prop = PSLoaderUtils.getOptionalProperty(
         PSPageExtractorDef.ENCODING,
         def.getProperties());
      String encoding = (String)m_encodingComboField.getSelectedItem();
      if(null == prop)
      {
         def.addProperty(
            new PSProperty(PSPageExtractorDef.ENCODING, encoding));
      }
      else
      {
         prop.setValue(encoding);
      }

      // Update fields
      PSFieldValueMapTableModel model =
         (PSFieldValueMapTableModel)m_valueMapTable.getModel();
      config.setDefinedFields(model.getDataList(), false);
   }

   // implements IPSExtractorConfigTabPanel interface method
   public boolean validateContent()
   {
      return ((PSFieldValueMapTableModel)m_valueMapTable.getModel()).validate();
   }

   /**
    * Encoding combo box field. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JComboBox m_encodingComboField;

   /**
    * Field-value map table. Initialized in {@link #init()}, never
    * <code>null</code> after that.
    */
   private JTable m_valueMapTable;

   /**
    * This extractor's configuration context. Initialized in ctor,
    * never <code>null</code> after that.
    */
   private PSExtractorConfigContext m_config;

   /**
    * Resource bundle for this class. Initialized once in {@link #init()},
    * never <code>null</code> after that.
    */
   protected static ResourceBundle ms_res;

   /**
    * System file encoding property name
    */
   protected static final String PROPNAME_FILE_ENCODING = "file.encoding";

}