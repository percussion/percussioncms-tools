/*[ PSMimeTypeEditorPanel ]***********************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTCellEditor;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSExtensionDef;
import com.percussion.loader.objectstore.PSImageExtractorDef;
import com.percussion.loader.objectstore.PSMimeTypeDef;
import com.percussion.loader.util.PSMapPair;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;

import org.w3c.dom.Element;

/**
 * Represents editor used for extracting images not extracted by an Static
 * Item Extractor.
 */
public class PSMimeTypeEditorPanel extends PSConfigPanel
{
   /**
    * Creates image extraction editor.
    */
   public PSMimeTypeEditorPanel()
   {
      init();
   }

   /**
    * Initializes this panel with a table.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
            getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BorderLayout());
      /**
       * Either create a new one or read an existing one from the object
       * store object. For now a new model is created.
       */
      m_extensionModel = new PSTwoColumnModel(PSTwoColumnModel.EXTENSION);

      m_table = new JTable(m_extensionModel);
      JScrollPane jsp = new JScrollPane(m_table);
      m_table.getColumnModel().getColumn(1).setCellEditor(
         //new UTCellEditor(new Editor()));
            new UTCellEditor(new PSExtensionsColumnEditor(m_table)));
      JPanel tablePanel = new JPanel();
      tablePanel.setLayout(new BorderLayout());

      Border b1 = BorderFactory.createEmptyBorder(10, 10, 10, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
         PSContentLoaderResources.getResourceString(ms_res,
         "border.extensionsTable"));

      tablePanel.add(jsp, BorderLayout.CENTER);
      tablePanel.setPreferredSize(new Dimension(100, 200));
      tablePanel.setBorder(b);
      setBorder(b1);
      add(tablePanel, BorderLayout.NORTH);
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void load(Element configXml)
   {
      try
      {
         m_imageDef = new PSImageExtractorDef(configXml);
         load();
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "err.title.loaderexception"), JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(ms_res,
            "error.title.unknownnode"), JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   /**
    * Loads the data for this panel from <code>m_imageDef</code>, which is
    * initialized in {@link #load(Element)}.
    */
   private void load()
   {
      List mimeList = m_imageDef.getMimeTypeList();
      if (!mimeList.isEmpty())
         populateImageExtTable(mimeList);
   }

   /**
    * Notifies the <code>m_extTable</code> to stop editing.
    */
   private void stopExtTableEditing()
   {
      if (m_table.isEditing())
      {
         m_table.getCellEditor(m_table.getEditingRow(),
            m_table.getEditingColumn()).stopCellEditing();
      }
   }

   /**
    * Populates <code>m_extensionModel</code> with name of the extensions in and
    * a comma delimited string of extensions. <code>list</code> contains {@link
    * com.percusssion.loader.objectstore.PSMimeTypeDef} objects.
    *
    * @param list, may be empty, assumed to be not <code>null</code>.
    */
   private void populateImageExtTable(List list)
   {
      PSMapPair pair = null;
      PSMimeTypeDef def = null;
      List tableList = m_extensionModel.getList();
      int len = list.size();
      for (int k = 0; k < len; k++)
      {
         def = (PSMimeTypeDef)list.get(k);
         pair = (PSMapPair)tableList.get(k);
         pair.setKey(def.getName());
         pair.setValue(getExtensionString(def.getExtensions()));
      }
      m_extensionModel.setData(tableList);
   }

   /**
    * Prepares a comma delimited string of extensions.
    *
    * @param itr, an iteration over {@link com.percussion.loader.objectstore.
    * PSExtensionDef} objects. Assumed to be not <code>null</code>, may be empty.
    *
    * @return comma delimited string of mimetype extension, never <code>null
    * </code>, may be empty.
    */
   private String getExtensionString(Iterator itr)
   {
      StringBuffer sbf = new StringBuffer();
      PSExtensionDef extDef = null;
      while(itr.hasNext())
      {
         extDef = (PSExtensionDef)itr.next();
         sbf.append(extDef.getAttValue());
         if (itr.hasNext())
            sbf.append(",");
         else
            break;
      }
      return sbf.toString();
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public Element save()
   {
      if(!validateContent())
         return null;
      stopExtTableEditing();
      List mimeList = m_imageDef.getMimeTypeList();
      mimeList.clear();
      List list = m_extensionModel.getList();
      List extList = null;
      PSMimeTypeDef mimeDef = null;
      int len = list.size();
      for(int z = 0; z < len; z++)
      {
         PSMapPair pair = (PSMapPair)list.get(z);
         String name = (String)pair.getKey();
         String value = (String)pair.getValue();
         if ( name.length() == 0 && value.length() == 0)
            continue;
         extList = PSExtensionsColumnEditor.getExtList(value);
         mimeDef = new PSMimeTypeDef(name, extList);
         mimeList.add(mimeDef);
      }
      return m_imageDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public boolean validateContent()
   {
      return true;
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void reset()
   {
      stopExtTableEditing();
      m_extensionModel.setData(null);
      load();
   }

   /**
    * {@link com.percussion.loader.PSImageExtractorDef}, initialized in {@link
    * #load(Element)}, never <code>null</code> or modified after that.
    */
   private PSImageExtractorDef m_imageDef;

   /**
    * Table having 2 columns with mimetype name as the first column and mimetype
    * extension as the second column. Initialized in <code>init()</code> and
    * never <code>null</code> or modified after that.
    */
   private JTable m_table;

   /**
    * A generic table model for two column tables. Initialized in <code>init()
    * </code> and never <code>null</code> and never modified after that.
    */
   private PSTwoColumnModel m_extensionModel;

   /**
    * The resource bundle used to get the resource strings of this panel,
    * initialized in <code>init()</code>, may be <code>null</code> if it could
    * not load, never modified after that.
    */
   private static ResourceBundle ms_res;

   /**
     * Gets the value for a key in the resource bundle.
     *
     * @param name key for the string whoes value is required. May not to be
     * <code>null</code>.
     * @return value of the key supplied, never <code>null</code>, if the value
     * doesn't exist key itself is returned.
     *
     * @throws IllegalArgumentException if any param is invalid.
     */
    private String getResourceString(String name)
    {
       if(name == null || name.trim().length() == 0)
          throw new IllegalArgumentException("key may not be null or empty");
       return PSContentLoaderResources.getResourceString(ms_res, name);
    }
}