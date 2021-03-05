/*[ PSFieldConfigTabPanel.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSEntry;
import com.percussion.loader.PSLoaderException;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

/**
 * Implements the tab panel which contains one field table.
 */
public class PSFieldConfigTabPanel extends PSAbstractExtractorConfigTabPanel
{


   /**
    * Constructs a new <code>PSFieldConfigTabPanel</code> object
    *
    * @param allowsXPathType flag indicating that the XPath type
    *    should be allowed in the value type combo box for the value-map table.
    */
   public PSFieldConfigTabPanel(boolean allowsXPathType)
   {
     init(allowsXPathType);
   }


   /**
    * Initializes the gui components for this panel
    *
    * @param allowsXPathType flag indicating that the XPath type
    * should be allowed in the value type combo box for the value-map table
    */
   private void init(boolean allowsXPathType)
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

      // Create borders
      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);

      // Field table panel
      JPanel tablePanel = new JPanel(new BorderLayout());
      tablePanel.setBorder(BorderFactory.createTitledBorder(b2,
            ms_res.getString("field.label.valueMap")));
      tablePanel.setPreferredSize(new Dimension(100, 100));
      tablePanel.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
      m_valueMapTable = PSFieldValueMapTable.newInstance(allowsXPathType);
      JScrollPane scrollpane = new JScrollPane(m_valueMapTable);
      tablePanel.add(scrollpane, BorderLayout.CENTER);
      add(tablePanel);

   }

   // implements IPSExtractorConfigTabPanel interface method
   public void load(PSExtractorConfigContext config)
      throws PSLoaderException
   {
      if(null == config)
         throw new IllegalArgumentException(
            "Extractor Configuration context cannot be null");

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
    * Filters table. Initialized in {@link #init()}, never
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


}