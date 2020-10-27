/*[ PSTransformationEditorPanel ]***********************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *****************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSFieldTransformationDef;
import com.percussion.loader.objectstore.PSParamDef;
import com.percussion.loader.objectstore.PSTransformationDef;
import com.percussion.loader.util.PSMapPair;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.w3c.dom.Element;

/**
 * Panel for showing field transformation editor. Besides showing the target
 * field in the 'Target Field' combo box, it shows all the parameters for the
 * transformation in the 'Parameters' table. On a row selection in the table,
 * the parameter description is displayed in the 'Description' text box.
 *
 */
public class PSTransformationEditorPanel extends PSConfigPanel
{

   /**
    * Creates PSFieldTransformationEditorPanel.
    */
   public PSTransformationEditorPanel(boolean isItem)
   {
      m_isItem = isItem;
      init();
   }

   /**
    * Initializes the transformation editor panel.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
            getClass().getName() + "Resources", Locale.getDefault());

      setLayout(new BorderLayout());
      setBorder(BorderFactory.createEmptyBorder( 10, 10, 10, 10 ));
      JPanel targetPane = null;
      if (!m_isItem)
      {
         targetPane = new JPanel();
         targetPane.setLayout(new BoxLayout(targetPane, BoxLayout.X_AXIS));
         JLabel label = new JLabel(
               PSContentLoaderResources.getResourceString(ms_res,
               "textfield.label.target"));
         m_targetCombo = new JComboBox();
         m_targetCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
               JComboBox cb = (JComboBox)e.getSource();
               m_target = (String)cb.getSelectedItem();
            }
         });
         m_targetCombo.setEditable(true);
         targetPane.add(Box.createHorizontalGlue());
         targetPane.add(label);
         targetPane.add(Box.createRigidArea(new Dimension(15, 0)));
         targetPane.add(m_targetCombo);
         targetPane.setBorder(BorderFactory.createEmptyBorder( 10, 10, 10, 2 ));
         add(targetPane, BorderLayout.NORTH);
      }
      m_defaultModel = new PSTwoColumnModel();
      m_table = new JTable(m_defaultModel);
      m_table.getSelectionModel().addListSelectionListener(
         new DecriptionListener());
      m_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane jsp = new JScrollPane(m_table);
      JPanel tablePanel = new JPanel();
      tablePanel.setLayout(new BorderLayout());

      Border b1 = BorderFactory.createEmptyBorder( 5, 10, 20, 10 );
      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      Border b = BorderFactory.createTitledBorder(b2,
            PSContentLoaderResources.getResourceString(ms_res,
            "border.label.paramTable"));
      tablePanel.setBorder(b);
      tablePanel.add(jsp, BorderLayout.CENTER);
      tablePanel.setPreferredSize(new Dimension(100, 200));
      add(tablePanel, BorderLayout.CENTER);

      String desc = null;
      m_textArea = new JTextArea();
      if (!m_isItem)
         desc = PSContentLoaderResources.getResourceString(ms_res,
         "field.description");
      else
         desc = PSContentLoaderResources.getResourceString(ms_res,
         "item.description");

      JPanel descPanel =
            PSContentDescriptorDialog.createDescriptionPanel(desc, m_textArea);
      JPanel dPanel = new JPanel();
      b = BorderFactory.createTitledBorder(b2,
         PSContentLoaderResources.getResourceString(ms_res,
         "border.label.description"));
      dPanel.setBorder(b);

      dPanel.setLayout(new BorderLayout());
      dPanel.add(descPanel, BorderLayout.CENTER);
      add(dPanel, BorderLayout.SOUTH);
   }

   /**
    * Gets the target field for the transformer.
    *
    * @return target field, may be <code>null</code>.
    */
   public String getTarget()
   {
      return m_target;
   }

   /**
    * Setter for the target field.
    *
    * @param target, target field, assumed to be not <code>null</code> or empty.
    */
   public void setTarget(String target)
   {
      m_target = target;
      m_targetCombo.setSelectedItem(target);
   }

   /**
    * Based on the row selection in 'Parameters' table 'Description' panel
    * should display the description corresponding to the parameter in that row.
    */
   private class DecriptionListener implements ListSelectionListener
   {
      public void valueChanged(ListSelectionEvent e)
      {
         ListSelectionModel model = (ListSelectionModel)e.getSource();
         int index = model.getMinSelectionIndex();
         PSParamDef def = null;
         if (index != -1 && index < m_paramList.size())
            def = (PSParamDef)m_paramList.get(index);
         if (def != null)
            m_textArea.setText(def.getDescription());
         else
            m_textArea.setText("");
      }
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void load(Element configXml)
   {
      try
      {
         //load target field later on
         if (!m_isItem)
         {
            m_transDef = new PSFieldTransformationDef(configXml);
            m_target = ((PSFieldTransformationDef)m_transDef).getTargetField();
         }
         else
            m_transDef = new PSTransformationDef(configXml);

         m_paramList = m_transDef.getParameterList();
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
    * Loads the 'Parameters' table from <code>m_paramList</code>.
    */
   private void load()
   {
      populateParamTable(m_paramList);
   }

   /**
    * Tell the editor to stop editing and accept any partially edited value
    * as the value of the editor.
    */
   private void stopExtTableEditing()
   {
      if(m_table.isEditing())
      {
         m_table.getCellEditor(m_table.getEditingRow(),
            m_table.getEditingColumn()).stopCellEditing();
      }
   }

   /**
    * Populates 'Parameters' table in item and field transformers panel.
    *
    * @param list, list of {@link com.percussion.objectstore.PSParamDef},
    * assumed to be not <code>null</code>.
    */
   private void populateParamTable(List list)
   {
      PSMapPair pair = null;
      PSParamDef def = null;
      List tableList = m_defaultModel.getList();
      int len = list.size();
      for (int k = 0; k < len; k++)
      {
         def = (PSParamDef)list.get(k);
         pair = (PSMapPair)tableList.get(k);
         pair.setKey(def.getName());
         pair.setValue(def.getValue());
      }
      m_defaultModel.setData(tableList);
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public Element save()
   {
      if (m_table.isEditing())
         stopExtTableEditing();
      if(!validateContent())
         return null;
      List tableList = m_defaultModel.getList();
      int len = m_paramList.size();
      for (int z = 0; z < len; z++)
      {
         PSMapPair pair = (PSMapPair)tableList.get(z);
         PSParamDef def = (PSParamDef)m_paramList.get(z);
         def.setValue((String)pair.getValue());
      }
      if (!m_isItem)
      {
         ((PSFieldTransformationDef)m_transDef).setTargetField(
            m_target);
      }
      //check if it's an item or field and update the fields accordingly
      return m_transDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public boolean validateContent()
   {
      if (!m_isItem)
      {
         if (m_target == null || m_target.length() == 0)
         {
            ErrorDialogs.showErrorDialog(this,
               PSContentLoaderResources.getResourceString(
               ms_res, "err.msg.targetnotspecified"),
               PSContentLoaderResources.getResourceString(
               ms_res, "err.title.targetnotspecified"),
               JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }
      return true;
   }

   /**
    * Implements the {@link IPSConfigPanel} interface.
    */
   public void reset()
   {
      if (m_table.isEditing())
         stopExtTableEditing();
      m_defaultModel.setData(null);
      if (!m_paramList.isEmpty())
         load();
   }

   /**
    * Set the target list.
    * 
    * @param list the new target list to set, may be <code>null</code> or empty.
    */
   public void setTargetList(List list)
   {
      m_targetCombo.removeAllItems();
      if (list != null && !list.isEmpty())
      {
         Iterator itor = list.listIterator();
         while (itor.hasNext())
         {
            m_targetCombo.addItem(itor.next());
         }
         m_targetCombo.setSelectedItem(m_target);
      }
   }

   /**
    * List of {@link PSMapPair} objects.
    *
    * @return may be empty, never <code>null</code>.
    */
   public List getParameters()
   {
      return m_defaultModel.getList();
   }

   /**
    * Drop down for holding the target fields. Initialized in {@link #init()},
    * never <code>null</code>.
    */
   private JComboBox m_targetCombo;

   /**
    * Encapsulates a transformation definition. Initialized in  {@link #load(
    * Element)}, never <code>null</code> or modified after that.
    */
   private PSTransformationDef m_transDef;

   /**
    * Holds the target field got from <code>m_transDef</code>.
    * Initialised in the ActionListener for the combo box.
    *
    * May not be <code>null</code>.
    */
   private String m_target;

   /**
    * 'Parameters' table holding parameter name and value. Initialised in the
    * {@link #init()}, never <code>null</code> or modified after that.
    */
   private JTable m_table;

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;

   /**
    * Two column editable table for specifying parameters name and value.
    * Initialised in the {@link #init()}, never <code>null</code> or modified
    * after that.
    */
   private PSTwoColumnModel m_defaultModel;

   /**
    * List containing {@link PSParamDef} objects. Initialized in  {@link #load(
    * Element)}, never <code>null</code> or modified after that.
    */
   private List m_paramList;

   /**
    * Displays parameter description based on the parameter selected from the
    * 'Parameters' table. Initialised in the {@link #init()}, never <code>null
    * </code> or modified after that.
    */
   private JTextArea m_textArea;

   /**
    * If <code>true</code> then it's an editor for item transformation or else
    * it's a field transformation editor. Initialized in the ctor.
    */
   private boolean m_isItem;
}