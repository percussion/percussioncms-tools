/*[ PSFileSelectorEditorPanel.java ]*******************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.UTComponents.UTFixedButton;
import com.percussion.UTComponents.UTFixedHeightTextField;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.PSDialog;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSFileSearchRoot;
import com.percussion.loader.objectstore.PSFileSelectorDef;
import com.percussion.loader.objectstore.PSFilter;
import com.percussion.loader.util.PSMapPair;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.w3c.dom.Element;


/**
 * Used for scanning file contents. Represents file content selector editor
 * panel comprising  a tabbed pane, a description panel and 'Add' and 'Remove'
 * buttons to add or remove a tabbed pane. Tabbed pane contains {@link
 * TablePanel} as search tabs.
 */
public class PSFileSelectorEditorPanel extends PSConfigPanel
{
   /**
    * Creates PSFileSelectorEditorPanel.
    */
   public PSFileSelectorEditorPanel()
   {
      init();
   }

   /**
    * Initializes the file selector editor panel with a 'Default' tab.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(
               getClass().getName() + "Resources", Locale.getDefault() );

      setLayout(new BorderLayout());
      m_tabPane = new JTabbedPane();
      m_tabPane.setPreferredSize(new Dimension(200, 300));
      final String defaultName = PSContentLoaderResources.getResourceString(
         ms_res, "tab.default");
      m_tabPane.addTab(defaultName, new TabPanel());
      m_tabPane.setSelectedIndex(0);
      add(m_tabPane, BorderLayout.CENTER);

      String desc = PSContentLoaderResources.getResourceString(ms_res,
         "description" );
      JTextArea txtArea = new JTextArea();
      JPanel descPanel =
            PSContentDescriptorDialog.createDescriptionPanel(desc, txtArea);
      JPanel dPanel = new JPanel();
      Border b1 = BorderFactory.createEmptyBorder( 5, 5, 5, 5 );

      Border b3 = BorderFactory.createEtchedBorder();
      Border b2 = BorderFactory.createCompoundBorder(b3, b1);
      dPanel.setBorder(b2);

      dPanel.setLayout(new BorderLayout());
      dPanel.add(descPanel, BorderLayout.CENTER);

      JPanel brPane = new JPanel();
      brPane.setLayout(new BoxLayout(brPane, BoxLayout.X_AXIS));
      brPane.setBorder(BorderFactory.createEmptyBorder( 10, 0, 0, 0));

      m_addBtn = new UTFixedButton(PSContentLoaderResources.getResourceString(
            ms_res, "button.add"));
      m_addBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            if (!validateContent())
               return;
            if (m_tabEntryDialog == null)
            {
               m_tabEntryDialog = new PopUp();
               m_tabEntryDialog.setVisible(true);
            }
            else
               m_tabEntryDialog.setVisible(true);

         }
      });
      m_removeBtn = new UTFixedButton(
         PSContentLoaderResources.getResourceString(ms_res, "button.remove"));
      m_removeBtn.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            int index = m_tabPane.getSelectedIndex();
            String tabName = m_tabPane.getTitleAt(index);
            if (index != -1 && !tabName.equals(defaultName))
            {
               if (m_newTabList.contains(tabName))
                  m_newTabList.remove(tabName);
               m_tabPane.remove(index);

            }
         }
      });
      brPane.add(Box.createHorizontalGlue());
      brPane.add(m_addBtn);
      brPane.add(Box.createRigidArea(new Dimension(5, 0)));
      brPane.add(m_removeBtn);
      dPanel.add(brPane, BorderLayout.SOUTH);
      add(dPanel, BorderLayout.SOUTH);
   }

   /**
    * Gets the resource mapping for the supplied key.
    *
    * @param key, may not be <code>null</code>
    * @return mapping corresponding to the key, never <code>null</code>.
    *
    * @throws IllegalArgumentException if the argument is invalid.
    */
   public String getResourceString(String key)
   {
      return PSContentLoaderResources.getResourceString(ms_res, key);
   }

   /**
    * Popup dialog for entering search name. It adds a unique search tab to
    * <code>m_tabPane</code>.
    */
   private class PopUp extends PSDialog
   {
      /**
       * Creates a popup dialog.
       */
      public PopUp()
      {
         init();
      }

      /**
       * Initializes the dialog.
       */
      private void init()
      {
         if (null == ms_res)
            ms_res = ResourceBundle.getBundle(
                  "PopUp" + "Resources", Locale.getDefault() );

         JPanel pane = new JPanel();
         pane.setBorder(BorderFactory.createEmptyBorder( 10, 10, 10, 10 ));
         pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));

         JLabel tabName = new JLabel(PSContentLoaderResources.getResourceString(
            ms_res, "textfield.label.tabname"));
         tabName.setAlignmentX(SwingConstants.LEFT);
         m_tabField = new UTFixedHeightTextField();
         m_tabField = new UTFixedHeightTextField();
         pane.add(tabName);
         pane.add(Box.createRigidArea(new Dimension(0, 10)));
         pane.add(m_tabField);
         pane.add(Box.createRigidArea(new Dimension(0, 10)));
         pane.add(createCommandPanel(SwingConstants.HORIZONTAL, false));
         getContentPane().add(pane);
         pane.setPreferredSize(new Dimension(270, 120));
         setResizable(true);
         center();
         setModal(true);
         pack();
      }

      /**
       * Listens to 'OK' button in the dialog and adds a unique search tab to
       * the <code>m_tabPane</code>. If the search name is not unique, an error
       * dialog is shown.
       */
      public void onOk()
      {
         String s = m_tabField.getText();
         if (!PSContentLoaderResources.nullOrEmpty(s) && isUnique(s))
         {
            m_tabPane.addTab(s, new TabPanel());
            int index = m_tabPane.indexOfTab(s);
            m_tabPane.setSelectedIndex(index);
            m_tabEntryDialog.setVisible(false);
            m_newTabList.add(s);
         }
         else
         {
            ErrorDialogs.showErrorDialog(m_tabEntryDialog,
                  PSContentLoaderResources.getResourceString(
            ms_res, "error.msg.searchrootadderror"),
            PSContentLoaderResources.getResourceString(
            ms_res, "error.title.nonuniquesearchroot"),
            JOptionPane.ERROR_MESSAGE);
         }
         m_tabField.setText("");
         super.onOk();
      }

      /**
       * Responds to 'Cancel' button in this dialog. Nothing is applied to the
       * <code>m_tabPane</code>.
       */
      public void onCancel()
      {
         m_tabField.setText("");
         super.onCancel();
      }

      /**
       * Text field for entering the search tab name. Initialised in {@link
       * #popUp()}, never <code>null</code> or modified after that.
       */
      private  UTFixedHeightTextField m_tabField;
   }

   /**
    * Tests if the the supplied argument is unique.
    *
    * @param root, name of the search, assumed to be not <code>null</code> and
    * nonempty.
    * @return, if <code>true</code> then the supplied string is unique i.e no
    * such tab in <code>m_tabPane</code> exists by that title.
    */
   private boolean isUnique(String root)
   {
      int tabCount = m_tabPane.getTabCount();
      int err;
      for (int k = 0; k < tabCount; k++)
      {
         if (m_tabPane.getTitleAt(k).equalsIgnoreCase(root))
            return false;
      }
      return true;
   }

   // Implements the IPSConfigPanel interface.
   public void load(Element configXml)
   {
      if (configXml == null)
         throw new IllegalArgumentException(
            "list selector objectstore object cannot tbe null");
      try
      {
         m_fileSelDef = new PSFileSelectorDef(configXml);
         m_searchRootList = m_fileSelDef.getSearchRootList();
         int len = m_searchRootList.size();
         for(int k = 0; k < len; k++)
         {
            PSFileSearchRoot root = (PSFileSearchRoot)m_searchRootList.get(k);
            TabPanel tab = null;
            if (k == 0)
               tab = (TabPanel)m_tabPane.getComponentAt(k);
            else
            {
               tab = new TabPanel();
               m_tabPane.addTab(root.getName(), tab);
            }
            tab.load(root);
            m_tabPane.setSelectedIndex(k);
         }
      }
      catch(PSLoaderException e)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, e.getMessage()),
            PSContentLoaderResources.getResourceString(
            ms_res, "err.title.loaderexception"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
      catch(PSUnknownNodeTypeException f)
      {
         ErrorDialogs.showErrorDialog(this,
            PSContentLoaderResources.getResourceString(
            ms_res, f.getMessage()),
            PSContentLoaderResources.getResourceString(
            ms_res, "error.title.unknownnode"),
            JOptionPane.ERROR_MESSAGE);
         return;
      }
   }

   // Implements the IPSConfigPanel interface.
   public void reset()
   {
      int len = m_newTabList.size();
      for(int z = 0; z < len; z++)
      {
          int index = m_tabPane.indexOfTab((String)m_newTabList.get(z));
          m_tabPane.remove(index);
      }
      m_newTabList.clear();
      int tabCount = m_tabPane.getTabCount();
      for (int k = 0; k < tabCount; k++)
      {
         ((TabPanel)m_tabPane.getComponentAt(k)).reset();
      }
   }

   // Implements the IPSConfigPanel interface.
   public Element save()
   {
      if(!validateContent())
         return null;
      int tabCount = m_tabPane.getTabCount();
      for (int k = 0; k < tabCount; k++)
      {
         ((TabPanel)m_tabPane.getComponentAt(k)).save();
      }
      return m_fileSelDef.toXml(PSXmlDocumentBuilder.createXmlDocument());
   }

   // Implements the IPSConfigPanel interface.
   public boolean validateContent()
   {
      int tabCount = m_tabPane.getTabCount();
      for (int k = 0; k < tabCount; k++)
      {
         if (!((TabPanel)m_tabPane.getComponentAt(k)).validateContent())
         {
            ErrorDialogs.showErrorDialog(m_tabEntryDialog,
            getResourceString("error.msg.missingsearchroot"),
            getResourceString("error.title.missingsearchroot"),
            JOptionPane.ERROR_MESSAGE);
            return false;
         }
      }
      return true;
   }

   /**
    * A List of zero or more <code>PSSearchRoot</code> objects, Initialized in
    * {@link @load(Element)}, never <code>null</code>, but may be empty.
    */
   List m_searchRootList;

   /**
    * Encapsulates a file selector definition, {@link
    * com.percussion.loader.objectstore.PSFileSelectorDef}. Initialized in
    * {@link @load(Element)}, never <code>null</code> or modified after that.
    */
   private PSFileSelectorDef m_fileSelDef;

   /**
    * Adds a search tab to this panel. Brings up <code>m_tabEntryDialog</code>
    * for entering the tab name. Initialized in {@link #init()}, never <code>
    * null</code> or modified after that.
    */
   private UTFixedButton m_addBtn;

   /**
    * Dialog for adding a search tab to this panel. See {@link #popUp()} for
    * details. Initialized in {@link #popUp()}, never <code>null</code> or
    * modified after that.
    */
   private PopUp m_tabEntryDialog;

   /**
    * Tabbed pane holding {@link TabPanel}'s  based on different searches.
    * See {@link TabPanel} for details. Initialized in <code>init()</code>,
    * never <code>null</code> or modified after that.
    */
   private JTabbedPane m_tabPane;

   /**
    * Resource bundle for this class. Initialised in the constructor.
    * It's not modified after that. Never <code>null</code>.
    */
   private static ResourceBundle ms_res;

   /**
    * Removes search tab from this panel, except the default tab. Initialized in
    * {@link #init()}, never <code>null</code> or modified after that.
    */
   UTFixedButton m_removeBtn;


   /**
    * List of String objects whic are tab names added through ui. Tab names are
    * added to it in {@link PopUP#onOK()} where tab name is entered  and tab
    * names are removed from it in the actionListener for <code> m_removeBtn
    * </code>.
    */
   List m_newTabList = new ArrayList();

   /**
    * Panel added to {@link PSFileSelectorEditorPanel#m_tabPane} as result of
    * 'Add' button click in {@link PSFileSelectorEditorPanel}. This panel
    * comprises a text field for specifying search root, a two column editable
    * table for specifying filters and two checkboxes - 'Follow Links' and
    * 'Recurse' for following all the links automatically and recursively.
    */
   private class TabPanel extends JPanel
   {
      /**
       * Creates TabPanel.
       */
      public TabPanel()
      {
         init();
      }

      /**
       * Initialises the TabPanel.
       */
      private void init()
      {
         setLayout(new BorderLayout());

         JPanel NVPane = new JPanel();
         NVPane.setLayout(new BoxLayout(NVPane, BoxLayout.Y_AXIS));
         JPanel urlPane = new JPanel();
         urlPane.setLayout(new BoxLayout(urlPane, BoxLayout.X_AXIS));
         JLabel root = new JLabel(
            PSContentLoaderResources.getResourceString(ms_res,
            "textfield.label.searchroot"));
         m_browse = new PSBrowsePanel(this, JFileChooser.DIRECTORIES_ONLY);
         root.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
         m_browse.setAlignmentY(JComponent.BOTTOM_ALIGNMENT);
         JComponent[] comp = new JComponent[]{m_browse};

         urlPane.add(root);
         urlPane.add(Box.createRigidArea(new Dimension(15, 0)));
         urlPane.add(m_browse);
         urlPane.add(Box.createHorizontalGlue());

         NVPane.add(urlPane);
         NVPane.add(Box.createRigidArea(new Dimension(0, 10)));

         m_filterM = new PSTwoColumnModel(PSTwoColumnModel.FILTER);
         m_table = new JTable(m_filterM);
         JScrollPane jsp = new JScrollPane(m_table);

         JPanel tablePanel = new JPanel();
         tablePanel.setLayout(new BorderLayout());
         Border b1 = BorderFactory.createEmptyBorder( 5, 10, 10, 10 );

         Border b3 = BorderFactory.createEtchedBorder();
         Border b2 = BorderFactory.createCompoundBorder(b3, b1);

         Border b = BorderFactory.createTitledBorder(b2,
               PSContentLoaderResources.getResourceString(ms_res,
               "border.filtertable"));
         tablePanel.setBorder(b);
         tablePanel.add(jsp, BorderLayout.CENTER);

        add(NVPane, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);

         JPanel linkPane = new JPanel();
         linkPane.setLayout(new BoxLayout(linkPane, BoxLayout.Y_AXIS));
         m_recurse = new JCheckBox(
            PSContentLoaderResources.getResourceString(ms_res,
            "checkbox.label.recurse"));
         linkPane.add(m_recurse);
         m_recurse.setAlignmentY(JPanel.LEFT_ALIGNMENT);
         m_recurse.setSelected(true);
         add(linkPane, BorderLayout.SOUTH);
         setBorder(BorderFactory.createEmptyBorder( 10, 10, 10, 10 ));
      }

      /**
       * Loads the data for this panel.
       *
       * @param searchRoot, search root definition, may be <code>null</code>.
       * @todo put the dtd for the loaded data
       */
      public void load(PSFileSearchRoot searchRoot)
      {
         if (searchRoot != null)
         {
            m_fileSrcRoot = searchRoot;
            m_recurse.setSelected(searchRoot.doRecurse());
            m_browse.setPath(searchRoot.getSearchRoot());
            Iterator filters = searchRoot.getFilters();
            int k = 0;
            List list = m_filterM.getList();
            while (filters.hasNext())
            {
               PSFilter filter = (PSFilter)filters.next();
               PSMapPair pair = (PSMapPair)list.get(k);
               pair.setKey(filter.getName());
               pair.setValue(filter.getValue());
               k++;
            }
            m_filterM.setData(list);
         }
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
       * Validates and saves all the data in the panel.
       */
      public void save()
      {
         validateContent();
         if (m_table.isEditing())
            stopExtTableEditing();
         String srchRt = getSearchRoot();
         if (m_fileSrcRoot == null)
         {
            int index = m_tabPane.indexOfComponent(this);
            m_fileSrcRoot = new PSFileSearchRoot(m_tabPane.getTitleAt(index),
              srchRt, isRecurse());
            m_fileSelDef.addSearchRoot(m_fileSrcRoot);
         }
         else
         {
            m_fileSrcRoot.setDoRecurse(isRecurse());
            m_fileSrcRoot.setSearchRoot(srchRt);
         }
         m_fileSrcRoot.clearFilters();
         List list = getFilters();
         Iterator itr = list.iterator();
         while(itr.hasNext())
         {
            PSMapPair pair = (PSMapPair)itr.next();
            String name = (String)pair.getKey();
            String value = (String)pair.getValue();
            if ( name.length() == 0 && value.length() == 0)
               continue;
            m_fileSrcRoot.addFilters(name, value);
         }
      }

      /**
       * Resets the panel back to the data it started with.
       */
      public void reset()
      {
         if (m_table.isEditing())
            stopExtTableEditing();
         if (m_fileSrcRoot != null)
            load(m_fileSrcRoot);
         else
         {
            setSearchRoot("");
            setRecurse(true);
            setFilters(null);
         }
      }

      /**
       * @return <code>true<code> if all the mandatory fields have been supplied
       * in the correct form.
       */
      public boolean validateContent()
      {
         String srchRt = getSearchRoot();
         return !(srchRt == null || srchRt.length() == 0);
      }

      /**
       * Search root where the scanner should start the search for content.
       * Format is file protocol.
       *
       * @return search root, never <code>null</code>.
       */
      public String getSearchRoot()
      {
         return m_browse.getPath();
      }

      /**
       * Sets the search root name.
       *
       * @param srch, search root name, assumed to be not <code>null</code>.
       */
      public void setSearchRoot(String srch)
      {
         m_browse.setPath(srch);
      }

      /**
       * Setter for a flag used to indicate whether to process directories
       * found under any directory specified in the Search Roots.
       *
       * @param recurse, <code>true</code>, if search is to be recursive.
       */
      public void setRecurse(boolean recurse)
      {
         m_recurse.setSelected(recurse);
      }

      /**
       * Sets the filter data for the 'Filters' table.
       *
       * @param list, containing {@link com.percussion.loader.util.PSMapPair}
       * objects, may be <code>null</code> or empty.
       */
      public void setFilters(List list)
      {
         m_filterM.setData(list);
      }

      /**
       * A flag used to indicate whether to process directories found under any
       * directory specified in the 'Search Root'.
       *
       * @return <code>true</code> if recursion was specified or else <code>
       * false</code>. Default is <code>false</code>.
       */
      public boolean isRecurse()
      {
         return m_recurse.isSelected();
      }

      /**
       * List of filters applied when scanning content. Any file that matches a
       * filter will be added to the tree, but marked as excluded. Filters are
       * applied in the order defined. Gets the list of {@link PSNVPair} objects.
       *
       * @return may be empty, never <code>null</code>.
       */
      public List getFilters()
      {
         return m_filterM.getList();
      }

      /**
       * Table containing filters {@link PSTwoColumnModel}. Initialized in
       * {@link #init()}, never <code>null</code> or modified after that.
       */
      private JTable m_table;

      /**
       * Encapsulates a file search root definition. Initialized in {@link
       * #load(PSFileSearchRoot)}, may be <code>null</code>.
       */
      private PSFileSearchRoot m_fileSrcRoot;

      /**
       * Text field for specifying the search root. Initialized in the {@link #
       * init()}, never <code>null</code> or modified after that.
       */
    //  private UTFixedHeightTextField m_rootField;
      private PSBrowsePanel m_browse;

      /**
       * Two column editable table for specifying filters. Initialized in the
       * {@link #init()}, never <code>null</code> or modified after that.
       */
      private PSTwoColumnModel m_filterM;

      /**
       * Specifies if the recursion is allowed. Initialized in the {@link #
       * init()}, never <code>null</code> or modified after that.
       */
      private JCheckBox m_recurse;
   }
}
