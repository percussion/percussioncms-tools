/******************************************************************************
 *
 * [ CommunitiesPanel.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSMultiValuedProperty;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * The panel diaplays the communities that are allowed to use the display format
 * (representing one of the nodes in the tree). For each enabled community one
 * row will be added to the table with the actual community as value. If all
 * communities are allowed, the special value 'All' will be set.
 */
public class CommunitiesPanel extends ParentPanel
   implements ChangeListener
{
   /**
    * Constructs the communities panel.
    * 
    * @param parent reference to parent dialog, may be <code>null</code>. Not
    *           used currently.
    * @param communList list of communities. Must not be <code>null</code> may
    *           be empty (which is not a desired condtion but recoverable). Each
    *           object in the list must be {@link PSComparablePair} object.
    */
   public CommunitiesPanel(List communList)
   {
      m_communList = communList;
      init();
   }

   /**
    * See base class for more details. This class provides an error if no
    * communities are selected.
    *
    * @return <code>true</code> if the All button is selected or at least
    *    1 community is checked, <code>false</code> otherwise.
    */
   public boolean validateData()
   {
      boolean valid = true;
      if (!isAllSelected())
      {
         //must have at least 1 checked
         TableModel model = getTableModel();

         int rowCount = model.getRowCount();

         boolean found = false;
         for (int i=0; i<rowCount && !found; i++)
         {
            String strName = (String)
                  ((PSComparablePair) model.getValueAt(i, 0)).getValue();
            Object obj = model.getValueAt(i, 1);

            // Threshold - ignore case where no name exists.
            if (strName == null || strName.trim().length() == 0)
               continue;

            if (obj instanceof Boolean)
            {
               Boolean bObj = (Boolean) obj;
               if (bObj.booleanValue())
                  found = true;
            }
         }

         if (!found)
         {
            valid = false;
            if(!m_isValidationQuiet)
            {
               PSDlgUtil.showErrorDialog(ms_res.getString("error.msg.nocommselected"),
                     ms_res.getString("error.title.nocommselected"));
            }
            requestFocus();
         }
      }
      return valid;
   }

   /**
    * Initializes the panel.
    */
   private void init()
   {
      try
      {
         if (null == ms_res)
            ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
               Locale.getDefault());
      }
      catch (Exception e)
      {
         if (null == ms_res)
            ms_res = ResourceBundle.getBundle(
               CommunitiesPanel.class.getName() + "Resources",
               Locale.getDefault());
      }

      if (ms_res == null)
         throw new IllegalStateException(
            "Can't find bundle for " + getClass().getName());

      setLayout(new BorderLayout());
      EmptyBorder emptyBorder = new EmptyBorder(0, 10, 10, 10);
      setBorder(emptyBorder);
      JPanel radPanel = new JPanel();
      radPanel.setLayout(new BoxLayout(radPanel, BoxLayout.Y_AXIS));
      m_allRadio = new JRadioButton(ms_res.getString(
         "radiobtn.label.all"));
      m_allRadio.setMnemonic(
                           ms_res.getString("radiobtn.label.all.mn").charAt(0));
      m_selectedRadio  = new JRadioButton(ms_res.getString(
         "radiobtn.label.selected"));
      m_selectedRadio.setMnemonic(
                     ms_res.getString("radiobtn.label.selected.mn").charAt(0));
      m_selectedRadio.setSelected(true); // selected be default
      m_allRadio.addChangeListener(this);
      m_selectedRadio.addChangeListener(this);

      ButtonGroup bg = new ButtonGroup();
      bg.add(m_allRadio);
      bg.add(m_selectedRadio);
      radPanel.add(Box.createVerticalStrut(20));
      radPanel.add(m_allRadio);
      radPanel.add(m_selectedRadio);
      radPanel.add(Box.createVerticalStrut(20));
      JPanel contextPane = new JPanel();
      contextPane.setLayout(new BorderLayout());
      String allow = ms_res.getString("col.allow");

      m_tableModel = new DefaultTableModel(new String[]
            {
               ms_res.getString("col.community"),
               allow
            }, 0);
      setData();
      m_contextTab = new JTable(m_tableModel)
      {
         public boolean isCellEditable(int row, int col)
         {
            if (col == 0)
               return false;
            return true;
         }
      };
      m_contextTab.getColumn(allow).setCellRenderer(
         new ActionColumnRenderer());
      DefaultCellEditor editor = new DefaultCellEditor(new JCheckBox())
      {
         public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected, int row, int column)
         {
            super.getTableCellEditorComponent(table, value, isSelected, row,
               column);
            if (column == 1)
            {
               String str = (String) ((PSComparablePair)
                     m_tableModel.getValueAt(row, 0)).getValue();
               if (str == null || str.length() == 0)
                  return new JLabel("");
            }
            return editorComponent;
         }
      };
      m_contextTab.getColumn(allow).setCellEditor(editor);
      JScrollPane contextjsp = new JScrollPane(m_contextTab);
      JViewport prt = contextjsp.getViewport();
      prt.setBackground(m_contextTab.getBackground());
      contextPane.add(contextjsp);
      JPanel fill = new JPanel();
      fill.setPreferredSize(new Dimension(500, 200));
      radPanel.setAlignmentX(CENTER_ALIGNMENT);
      m_contextTab.setAlignmentX(CENTER_ALIGNMENT);
      add(radPanel, BorderLayout.NORTH);
      add(contextPane, BorderLayout.CENTER);
      add(fill, BorderLayout.SOUTH);
      setPreferredSize(new Dimension(500, 600));
   }

   /**
    * Set validation to quiet mode to suppress error messages.
    * <code>true</code> will suppress messages.
    * @param isQuiet
    */
   public void setValidationQuiet(boolean isQuiet)
   {
      m_isValidationQuiet = isQuiet;
   }

   // see interface for description
   public void stateChanged(ChangeEvent e)
   {
      if (e.getSource() == m_allRadio)
      {
         JRadioButton ch = (JRadioButton) e.getSource();

         if (ch.isSelected())
         {
            onSetAll();
         }
         else
         {
            m_contextTab.setEnabled(true);
         }
      }
      else if (e.getSource() == m_selectedRadio)
      {

         JRadioButton ch = (JRadioButton) e.getSource();

         if (ch.isSelected())
         {
            m_contextTab.setEnabled(true);
         }
      }
   }

   private void setData()
   {
      int sz = m_communList.size();
      for(int k = 0; k < sz; k++)
      {
         m_tableModel.addRow(new Object[]
         {
            m_communList.get(k),
            false
         });
      }
   }

   // see base class for description
   public boolean save()
   {
      return true;
   }

   /**
    * Loads the communities from the properties list of the supplied format.
    *
    * @param data Must be a valid Iterator, never <code>null</code>. Each
    *    entry in this iterator must be a PSMultiValuedProperty.
    *
    * @throws ClassCastException If data is not of type Iterator or any
    *    value obtained from the iterator is not of type PSMultiValuedProperty.
    */
   public void load(Object data)
   {
      if (data == null || !(data instanceof Iterator))
         throw new IllegalArgumentException("Must supply valid Iterator");

      Iterator itr = (Iterator) data;

      // intialized the ui comps
      defaultComponents();

      boolean isAll = false;

      String val = null;
      int sz = m_tableModel.getRowCount();
      String community = null;
      m_selectedRadio.setSelected(true);

      while (itr.hasNext())
      {
         m_communtiyMultiProp = (PSMultiValuedProperty)itr.next();
         String strName = m_communtiyMultiProp.getName();

         // Continue if not a community property
         if (!strName.equalsIgnoreCase(PROP_COMMUNITY))
            continue;

         Iterator valItr =m_communtiyMultiProp.iterator();
         while (valItr.hasNext())
         {
            val = (String) valItr.next();

            // check for all
            if (val.equalsIgnoreCase(PROP_COMMUNITY_ALL))
            {
               m_allRadio.setSelected(true);
               isAll = true;
                m_contextTab.setEnabled(false);
               break;
            }

            // let's loop through and enable the appropriate communities
            // leave the others disabled:
            for (int k = 0; k < sz; k++)
            {
               community = (String) ((PSComparablePair)
                     m_tableModel.getValueAt(k, 0)).getKey();

               if (val.equalsIgnoreCase(community))
                  m_tableModel.setValueAt(true, k, 1);
            }
         }

         if (isAll)
         {
            break;
         }
      }
   }

   /**
    * @return <code>true</code> if we have selected all communitied.
    *    Otherwise, <code>false</code>/
    */
   protected boolean isAllSelected()
   {
      return m_allRadio.isSelected();
   }

   private void defaultComponents()
   {
      int sz = m_tableModel.getRowCount();
      m_contextTab.setEnabled(true);

      for (int k = 0; k < sz; k++)
         m_tableModel.setValueAt(false, k, 1);
   }

   private void onSetAll()
   {
      int sz = m_tableModel.getRowCount();
      m_contextTab.setEnabled(false);

      for (int k = 0; k < sz; k++)
         m_tableModel.setValueAt(true, k, 1);
   }

   /**
    * Gets the table model for given derived classes to read and
    * perform additional loading if neccessary.
    *
    * @return table model Never <code>null</code>
    */
   protected TableModel getTableModel()
   {
      return m_tableModel;
   }

   /**
    * Implements the interface which defines the method required by any object
    * that would like to be a renderer for cells in a JTable.
    */
   private class ActionColumnRenderer implements TableCellRenderer
   {
      /**
       * Returns JCheckBox as the renderer if <code>value</code> is an instance
       * of Boolean or else a label displaying <code>value</code> as a String.
       * All the input params are called by swing model so they are valid.
       */
      public Component getTableCellRendererComponent(JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column)
      {
         if (value instanceof Boolean) //action column
         {
            if (column == 1)
            {
               String str = (String) ((PSComparablePair)
                     m_tableModel.getValueAt(row, 0)).getValue();
               if (str == null || str.length() == 0)
                  return new JLabel("");
            }
            JCheckBox chk = new JCheckBox();
            chk.setSelected(((Boolean)value).booleanValue());
            if(isSelected)
            {
               chk.setBackground(UIManager.getColor("Table.selectionBackground"));
               chk.setForeground(UIManager.getColor("Table.selectionForeground"));
            }
            else
            {
               chk.setBackground(UIManager.getColor("Table.textBackground"));
               chk.setForeground(UIManager.getColor("Table.textForeground"));
            }
            return chk;
         }
         else
         {
            String text = "";
            if(value != null)
               text = value.toString();
            return new JLabel(text);
         }
      }
   }

   // private defines
   private static final String PROP_COMMUNITY_ALL =
      PSDisplayFormat.PROP_COMMUNITY_ALL;
   private static final String PROP_COMMUNITY =
      PSDisplayFormat.PROP_COMMUNITY;



   protected PSMultiValuedProperty m_communtiyMultiProp;

   private JTable m_contextTab;

   /**
    *
    */
   private List m_communList;

   /**
    * It's selected by default allowing all communities to use this display
    * form. If selected, the 'Categories' table in {@link
    * DisplayFormatColumnPanel} is disabled. Initialized in the {@link #init()},
    * never <code>null</code> or modified after that.
    */
   private JRadioButton m_allRadio;

   /**
    * It's not selected by default hence the 'Categories' table in {@link
    * DisplayFormatColumnPanel} is enabled and the user can select the allowed
    * communities. Initialized in the {@link #init()}, never <code>null</code>
    * or modified after that.
    */
   private JRadioButton m_selectedRadio;

   /**
    * Table model representing two columns 'Community' - lists all available
    * communities and 'Allow' - provides a checkbox as an editor to allow /
    * disallow each community. Initialized in the {@link #init()}, never
    * <code>null</code> or modified after that.
    * <p>The first column contains PSComparablePair objects, the 2nd column
    * Boolean objects, indicating the state of the check box.
    */
   private DefaultTableModel m_tableModel;

   /**
    * Flag indicating that validation should suppress error messages
    * defaults to <code>false</code>.
    */
   private boolean m_isValidationQuiet = false;

   /**
    * Resource bundle for this class. Initialized in {@link #init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
}
