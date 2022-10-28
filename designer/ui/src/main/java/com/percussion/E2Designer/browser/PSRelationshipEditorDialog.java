/******************************************************************************
 * [ PSRelationshipEditorDialog.java ]
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.JavaExitsPropertyDialog;
import com.percussion.E2Designer.OSExitCallSet;
import com.percussion.E2Designer.OSExtensionCall;
import com.percussion.E2Designer.PSCatalogTransitionActionTriggers;
import com.percussion.E2Designer.PSRuleEditorDialog;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.design.objectstore.PSCloneHandlerConfig;
import com.percussion.design.objectstore.PSCloneOverrideFieldList;
import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSConditionalExtension;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSEntry;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSProcessCheck;
import com.percussion.design.objectstore.PSProperty;
import com.percussion.design.objectstore.PSRelationshipConfig;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSRemoteRequester;
import com.percussion.UTComponents.UTBrowseButton;
import com.percussion.UTComponents.UTFixedButton;
import com.percussion.guitools.ErrorDialogs;
import com.percussion.guitools.IPSTableModel;
import com.percussion.guitools.PSCalendarField;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.PSPropertyPanel;
import com.percussion.guitools.PSTableModel;
import com.percussion.guitools.UTStandardCommandPanel;
import org.apache.commons.collections.IteratorUtils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;


/**
 * The dialog that will be used to edit the relationship set configuration.
 */
public class PSRelationshipEditorDialog extends PSDialog implements
   TreeSelectionListener
{

   /**
    * Constructs the dialog with the supplied parameters. Sorts the supplied
    * exits into 3 different categories (pre-exits, post-exits and effects) to
    * be available to the user appropriately.
    *
    * @param parent the parent frame of the dialog, may be <code>null</code>
    * @param relationshipConfigSet the locked configuration to edit, may not be
    * <code>null</code>. If not locked, exceptions will come upon saving the
    * relationships, when user clicks OK.
    * @param cloneHandlerConfig The system clone handler configuration that
    * defines the process checks that can be run with a relationship, may not be
    * <code>null</code>
    * @param exits the collection of extension definitions, may not be <code>
    * null</code>, can be empty. Any exits/extensions other than pre-exits,
    * post-exits and effects are ignored.
    */
   @SuppressWarnings("unchecked")
   public PSRelationshipEditorDialog(Frame parent,
      PSRelationshipConfigSet relationshipConfigSet,
      PSCloneHandlerConfig cloneHandlerConfig, Iterator exits)
      throws PSCmsException
   {
      super(parent);

      if(relationshipConfigSet == null)
         throw new IllegalArgumentException(
            "relationshipConfigSet may not be null.");

      if(cloneHandlerConfig == null)
         throw new IllegalArgumentException(
            "cloneHandlerConfig may not be null.");

      if(exits == null)
         throw new IllegalArgumentException("exits may not be null.");

      m_parentFrame = parent;
      m_rsConfigSet = relationshipConfigSet;
      m_chConfig = cloneHandlerConfig;

      while(exits.hasNext())
      {
         IPSExtensionDef exit = (IPSExtensionDef)exits.next();

         if(exit.implementsInterface(OSExitCallSet.EXT_TYPE_UDF))
            m_udfs.add(exit);
         if(exit.implementsInterface(OSExitCallSet.EXT_TYPE_REQUEST_PRE_PROC))
            m_preExtensions.add(exit);
         if(exit.implementsInterface(OSExitCallSet.EXT_TYPE_RESULT_DOC_PROC))
            m_postExtensions.add(exit);
         if(exit.implementsInterface(OSExitCallSet.EXT_TYPE_RS_EFFECT))
            m_effects.add(exit);
      }

      for (int i = 0; i < PSProperty.TYPE_ENUM.length; i++)
      {
         m_propertyTypes.put(i, new PropertyType( i,
            getResourceString(PSProperty.TYPE_ENUM[i]) ) );
      }


      //get CE system fields
   //TODO: currently this field list all search fields which need to be filtered to rstrict to writable fields.
      PSRemoteRequester appReq = new PSRemoteRequester(
            E2Designer.getLoginProperties());
      PSRemoteCataloger remCatlg = new PSRemoteCataloger(appReq);

      PSContentEditorFieldCataloger fieldCatlgObj =
         new PSContentEditorFieldCataloger(remCatlg, null,
            PSRemoteCataloger.FLAG_INCLUDE_RESULTONLY);

      m_ceSystemFields = new TreeSet(fieldCatlgObj.getSystemMap().keySet());

      initDialog();
   }

   /**
    * Initializes all table column lists, icons and layouts the dialog.
    */
   private void initDialog()
   {
      setTitle(getResourceString("title"));

      if(ms_propColumns.isEmpty())
      {
         ms_propColumns.add(getResourceString("name"));
         ms_propColumns.add(getResourceString("value"));
         ms_propColumns.add(getResourceString("locked"));
         ms_propColumns.add(getResourceString("type"));
      }

      if(ms_cloneColumns.isEmpty())
      {
         ms_cloneColumns.add(getResourceString("enable"));
         ms_cloneColumns.add(getResourceString("name"));
         ms_cloneColumns.add(getResourceString("conditionsColumn"));
      }

      if(ms_cloneFieldOverridesColumns.isEmpty())
      {
         ms_cloneFieldOverridesColumns.add(getResourceString("field"));
         ms_cloneFieldOverridesColumns.add(getResourceString("udf"));
         ms_cloneFieldOverridesColumns.add(getResourceString("conditionsColumn"));
      }

      if(ms_extColumns.isEmpty())
      {
         ms_extColumns.add(getResourceString("exit"));
         ms_extColumns.add(getResourceString("conditionsColumn"));
      }

      if(ms_effColumns.isEmpty())
      {
         ms_effColumns.add(getResourceString("direction"));
         ms_effColumns.add(getResourceString("effect"));
         ms_effColumns.add(getResourceString("conditionsColumn"));
      }

      if(ms_condIcon == null)
      {
         ms_condIcon = new ImageIcon(
            getClass().getResource(getResourceString("gif_cond")));
      }

      if(ms_noCondIcon == null)
      {
         ms_noCondIcon = new ImageIcon(
            getClass().getResource(getResourceString("gif_noCond")));
      }

      if(ms_treeLeafIcon == null)
      {
         ms_treeLeafIcon = new ImageIcon(
            getClass().getResource(getResourceString("gif_treeLeaf")));
      }

      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());

      JPanel cmdPane = new JPanel();
      cmdPane.setLayout(new BoxLayout(cmdPane, BoxLayout.X_AXIS));

      UTStandardCommandPanel commandPanel =
         new UTStandardCommandPanel(this, SwingConstants.HORIZONTAL, true, true);

      cmdPane.add(Box.createHorizontalGlue());
      JPanel pane = new JPanel();
      pane.setPreferredSize(new Dimension(450, 0));
      cmdPane.add(pane);
      cmdPane.add(commandPanel);

      m_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      JPanel leftPanel = createLeftPanel();
      m_split.setLeftComponent(leftPanel);

      JPanel viewPanel = new JPanel();

      m_split.setRightComponent(viewPanel);
      leftPanel.setPreferredSize(new Dimension(300, 400));
      viewPanel.setPreferredSize(new Dimension(350, 400));
      m_split.setPreferredSize(new Dimension(700, 500));

      mainPane.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      commandPanel.setBorder(BorderFactory.createEmptyBorder( 5, 0, 0, 0 ));
      mainPane.add(m_split, BorderLayout.CENTER);
      mainPane.add(cmdPane, BorderLayout.SOUTH);
      getContentPane().add(mainPane);
      pack();
      center();
      setResizable(true);


   }

   /**
    * Creates the left panel with tree on top and command buttons (Add, Delete)
    * at the bottom.
    *
    * @return the panel, never <code>null</code>
    */
   private JPanel createLeftPanel()
   {
      //create tree model
      DefaultMutableTreeNode root = new DefaultMutableTreeNode(
         getResourceString("rootNode"));
      m_rsConfigTreeModel = new DefaultTreeModel(root);

      DefaultMutableTreeNode systemNode = new DefaultMutableTreeNode(
         getResourceString("systemNode"))
      {
         /**
          * Display this node as folder always.
          */
         @Override
         public boolean isLeaf()
         {
            return false;
         }
      };
      DefaultMutableTreeNode userNode = new DefaultMutableTreeNode(
         getResourceString("userNode"))
      {
         /**
          * Display this node as folder always.
          */
         @Override
         public boolean isLeaf()
         {
            return false;
         }
      };
      root.add(systemNode);
      root.add(userNode);

      Iterator relationships = m_rsConfigSet.iterator();
      while(relationships.hasNext())
      {
         PSRelationshipConfig rsConfig =
            (PSRelationshipConfig)relationships.next();

         if(rsConfig.isSystem())
            addRelationshipNode(systemNode, rsConfig);
         else
            addRelationshipNode(userNode, rsConfig);
      }

      m_rsConfigTree = new JTree(m_rsConfigTreeModel);
      m_rsConfigTree.getSelectionModel().setSelectionMode(
         TreeSelectionModel.SINGLE_TREE_SELECTION);
      m_rsConfigTree.getSelectionModel().addTreeSelectionListener(this);
      DefaultTreeCellRenderer renderer =
         (DefaultTreeCellRenderer)m_rsConfigTree.getCellRenderer();
      renderer.setLeafIcon(ms_treeLeafIcon);

      JScrollPane pane = new JScrollPane(m_rsConfigTree);
      pane.setAlignmentX(CENTER_ALIGNMENT);

      JPanel panel = new JPanel(new BorderLayout());
      panel.add(pane, BorderLayout.CENTER);


      JPanel btnPane = new JPanel();
      btnPane.setLayout(new BoxLayout(btnPane, BoxLayout.X_AXIS));
      m_addButton = new UTFixedButton(getResourceString("new"));
      m_addButton.setMnemonic(getResourceString("new.mn").charAt(0));
      m_addButton.setEnabled(true);
      m_addButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onAdd();
         }
      });
      m_deleteButton = new UTFixedButton(getResourceString("delete"));
      m_deleteButton.setMnemonic(getResourceString("delete.mn").charAt(0));
      m_deleteButton.setEnabled(false);
      m_deleteButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(ActionEvent e)
         {
            onDelete();
         }
      });
      btnPane.add(Box.createHorizontalGlue());
      btnPane.add(m_addButton);
      btnPane.add(Box.createHorizontalStrut(20));
      btnPane.add(m_deleteButton);
      btnPane.add(Box.createHorizontalGlue());

      btnPane.setBorder(BorderFactory.createEmptyBorder(10, 0,0,0));

      panel.add(btnPane, BorderLayout.SOUTH);

      Border b = BorderFactory.createEmptyBorder(10, 10, 10, 10);
      panel.setBorder(
         BorderFactory.createCompoundBorder(
            BorderFactory.createEtchedBorder(), b));
      JPanel thePane = new JPanel();
      thePane.setLayout(new BorderLayout());
      thePane.setBorder(b);
      thePane.add(panel, BorderLayout.CENTER);

      //Expand User and System nodes all by default
      TreePath rootPath = new TreePath(root);
      m_rsConfigTree.expandPath(rootPath.pathByAddingChild(userNode));
      m_rsConfigTree.expandPath(rootPath.pathByAddingChild(systemNode));

      return thePane;
   }

   /**
    * A wrapper around a super class method so that the compiler
    * does not get confused when building the call from an inner class
    * @param s the key to get the string resource
    * @return the resource string specified by the key passed in
    */
   @Override
   public String getResourceString(String s)
   {
      return super.getResourceString(s);
   }

   /**
    * Edits the provided extension call.
    * @return the updated extension call or <code>null</code> if editing was
    * cancelled.
    */
   public static OSExtensionCall editExitCall(OSExtensionCall call)
   {
      final OSExitCallSet callSet = new OSExitCallSet();
      callSet.add(call,
         (String)call.getExtensionDef().getInterfaces().next());
      final JavaExitsPropertyDialog dlg =
            new JavaExitsPropertyDialog(m_parentFrame, callSet, false);
     dlg.setFocusable(true);
     dlg.setModal(true);
     dlg.center();

      dlg.addWindowFocusListener(new WindowFocusListener() {

         @Override
         public void windowLostFocus(WindowEvent arg0) {

         }
         @Override
         public void windowGainedFocus(WindowEvent arg0) {
            dlg.toFront();
         }
      });
      dlg.setVisible(true);
      return dlg.wasOkExit() ? (OSExtensionCall) callSet.get(0) : null;
   }

   /**
    * Create exit call.
    * @return the updated extension call or <code>null</code> if editing was
    * cancelled.
    */
   public static OSExtensionCall createExitCall(IPSExtensionDef def)
   {
      final OSExitCallSet callSet = new OSExitCallSet();
      callSet.setExtension(def, (String)def.getInterfaces().next());

      //show dialog to edit param values if it has any.
      if(def.getRuntimeParameterNames().hasNext())
      {
         final JavaExitsPropertyDialog dlg = new JavaExitsPropertyDialog(
            m_parentFrame, callSet, false);
         dlg.setVisible(true);
         return dlg.wasOkExit() ? (OSExtensionCall) callSet.get(0) : null;
      }

      return (OSExtensionCall)callSet.get(0);
   }

   /**
     * This panel represents a top level relationship config properties.
     */
    protected class CfgPanelRelationship extends PSCfgPanel
    {
       /**
        * Ctor. See base class for details.
        */
       public CfgPanelRelationship(String view, PSRelationshipEditorDialog owner)
       {
          super(view, owner);
       }

       /**
        *  See base class for details.
        */
       public void initPanel()
       {
          mi_generalPropertiesPanel = new PSPropertyPanel();
          mi_nameField = new JTextField();

          mi_generalPropertiesPanel.addPropertyRow(
                         getResourceString("internalName"),
                         new JComponent[] { mi_nameField }, mi_nameField,
                         getResourceString("internalName.mn").charAt(0), null);

          mi_labelField = new JTextField();
          mi_generalPropertiesPanel.addPropertyRow(getResourceString("label"),
                         new JComponent[] { mi_labelField }, mi_labelField,
                         getResourceString("label.mn").charAt(0), null);

          mi_categoryComboBox = new JComboBox();
          mi_categoryComboBox.setEditable(false);

          mi_generalPropertiesPanel.addPropertyRow(getResourceString("category"),
                         new JComponent[] { mi_categoryComboBox }, 
                         mi_categoryComboBox,
                         getResourceString("category.mn").charAt(0), null);


          mi_descTextArea = new JTextArea();
          mi_descTextArea.setLineWrap(true);
          mi_descTextArea.setWrapStyleWord(true);

          JScrollPane descPane = new JScrollPane(mi_descTextArea);
          mi_generalPropertiesPanel.addPropertyRow(
                            getResourceString("description"),
                            new JComponent[] {descPane}, mi_descTextArea,
                            getResourceString("description.mn").charAt(0),null);

          descPane.setPreferredSize(new Dimension(300, 75));

          //create props table with custom cell editor and cell rend.
          PSCfgTableCellRenderer cellRenderer =
                new PSCfgTableCellRenderer(PSRelationshipEditorDialog.this);
          CfgTableCellEditor   cellEditor = new CfgTableCellEditor();

          cellEditor.setPropertyTypes(m_propertyTypes.values().iterator());

          //properties table
          mi_systemPropsTable = new SpecialPropsTable(new DefaultTableModel(),
            cellEditor, cellRenderer);

          mi_systemPropsTable.getSelectionModel().addListSelectionListener(
             new CfgTableSelectionListener(mi_systemPropsTable,
               mi_sharedDescription));

          JScrollPane systemPropsPanel = new JScrollPane(mi_systemPropsTable);
          systemPropsPanel.setPreferredSize(new Dimension(300,300));
          systemPropsPanel.setVisible(true);


          //add multi-property panel first
          add(mi_generalPropertiesPanel);

          //add table
          add(systemPropsPanel);
       }

       //see base class for details.
       public void updateDataFromView()
       {
          if (mi_cfg == null)
            throw new IllegalStateException("mi_cfg may not be null!");

          //save data
          if(mi_cfg.isUser())
          {
             //note: User can not modify system config values.
             mi_cfg.setName(mi_nameField.getText());
             mi_cfg.setLabel(mi_labelField.getText());
             PSEntry entry = (PSEntry)mi_categoryComboBox.getSelectedItem();
             mi_cfg.setCategory(entry.getValue());
             mi_cfg.setDescription(mi_descTextArea.getText());
          }
       }

       //see base class for details.
       @Override
       public void updateViewFromData(PSRelationshipConfig cfg)
       {
          super.updateViewFromData(cfg);

          mi_nameField.setText(mi_cfg.getName());
          mi_labelField.setText(mi_cfg.getLabel());

          String category = mi_cfg.getCategory();

          if (category!=null && category.trim().length() > 1)
          {
            int itemCount = mi_categoryComboBox.getItemCount();
            for (int j = 0; j < itemCount; j++)
            {
               PSEntry entry = (PSEntry)mi_categoryComboBox.getItemAt(j);
               if (category.equals(entry.getValue()))
               {
                  mi_categoryComboBox.setSelectedIndex(j);
                  break;
               }
            }
          }
          else
          {
            mi_categoryComboBox.setSelectedIndex(0);
          }

          // Set text and replace tabs with a space
          String desc = (null == mi_cfg.getDescription()) ?
            "" : mi_cfg.getDescription().replace('\u0009', '\u0032');

          mi_descTextArea.setText(desc);
          TableColumn tc = null;
          
          //load cfg data and set table model for the props. table
          mi_systemPropsTable.setModel(
            new PropertiesTableModel(mi_cfg.getSysPropertiesFiltered(), false));
          
          tc = mi_systemPropsTable.getColumn(getResourceString("value"));
          tc.setPreferredWidth(134);
          tc.setMinWidth(134);
          
          boolean isUser = mi_cfg.isUser();
          Component[] comps = mi_generalPropertiesPanel.getComponents();
          for (int i = 0; i < comps.length; i++)
             comps[i].setEnabled(isUser);
       }

       //see base class
       public boolean validateViewData()
       {
          boolean valid = true;

          if (mi_cfg.isUser())
          {
            String name = mi_nameField.getText();
            if(name.trim().length() == 0)
            {
               ErrorDialogs.showErrorMessage(m_parentFrame,
                  getResourceString("mustEnterName"), getResourceString("error"));
               valid = false;
            }
            else
            {
               PSRelationshipConfig dupConfig = m_rsConfigSet.getConfig(name);
               if(dupConfig != null && dupConfig != mi_cfg)
               {

                  ErrorDialogs.showErrorMessage(
                     m_parentFrame, MessageFormat.format(
                     getResourceString("duplicateRelationship"), new Object[]{name} ),
                     getResourceString("error") );
                  valid = false;
               }
            }

            if(valid && mi_labelField.getText().trim().length() == 0)
            {
               ErrorDialogs.showErrorMessage(m_parentFrame,
                  getResourceString("mustEnterLabel"), getResourceString("error"));
               valid = false;
            }
          }

          return  valid;
       }


       //UI components of this panel
       private PSPropertyPanel mi_generalPropertiesPanel;

       private JTextField mi_nameField;
       private JTextField mi_labelField;
       private JComboBox  mi_categoryComboBox;
       private JTextArea  mi_descTextArea;

       /**
        * The table that shows system properties for the selected relationship.
        * Initialized in <code>initPanel()</code> and never <code>
        * null</code> after that.
        */
       private SpecialPropsTable mi_systemPropsTable;
   }

   /**
    * User Properties panel.
    */
   protected class CfgPanelUserProps extends PSCfgPanel
   {
      public CfgPanelUserProps(String view, PSRelationshipEditorDialog owner)
      {
         super(view, owner);
      }

      /* (non-Javadoc)
       * @see PSRelationshipEditorDialog.CfgPanel#initPanel()
       */
      public void initPanel()
      {
         //create props table with custom cell editor and cell rend.
         PSCfgTableCellRenderer cellRenderer =
               new PSCfgTableCellRenderer(PSRelationshipEditorDialog.this);
         CfgTableCellEditor   cellEditor = new CfgTableCellEditor();
         cellEditor.setPropertyTypes(m_propertyTypes.values().iterator());

         mi_userPropertiesTable = new SpecialPropsTable(new DefaultTableModel(),
            cellEditor, cellRenderer);

         mi_userPropertiesTable.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);

         KeyStroke ksEnterRelease =
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
         mi_userPropertiesTable.getInputMap().put(ksEnterRelease, "noAction");

         AbstractAction noAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
               //executes no action, implemented to avoid sending the event up
               //to its container heirarchy.
            }
         };

         mi_userPropertiesTable.getActionMap().put("noAction", noAction);

         mi_userPropertiesTable.addKeyListener( getIPSTableModelKeyAdapter());

         mi_userPropertiesTable.getSelectionModel().addListSelectionListener(
            new CfgTableSelectionListener(mi_userPropertiesTable,
               mi_sharedDescription));

         JScrollPane pane = new JScrollPane(mi_userPropertiesTable);

         //add table
         add(pane);
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#updateViewFromData(com.percussion.design.objectstore.PSRelationshipConfig)
       */
      @Override
      public void updateViewFromData(PSRelationshipConfig cfg)
      {
         super.updateViewFromData(cfg);
         
         TableColumn tc = null;
         mi_userPropertiesTable.setModel(
            new PropertiesTableModel(mi_cfg.getUserDefProperties(), true));
         
         tc = mi_userPropertiesTable.getColumn(getResourceString("value"));
         tc.setPreferredWidth(134);
         tc.setMinWidth(134);   
            
      }

      /* (non-Javadoc)
       * @see PSRelationshipEditorDialog.CfgPanel#validateViewData()
       */
      public boolean validateViewData()
      {
         boolean valid = true;

         PropertiesTableModel model =
            (PropertiesTableModel) mi_userPropertiesTable.getModel();

         final Set<String> dupUserProps = new HashSet<>();
         final Set<String> dupSysProps = new HashSet<>();
         Map sysProps = mi_cfg.getSystemProperties();
         
         boolean isActiveAssemblyCategory = mi_cfg.getCategory().equals(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         final Map<String, PSProperty> requiredUserProps =
               new HashMap<String, PSProperty>();
         if (isActiveAssemblyCategory)
            requiredUserProps.putAll(ms_requiredUserProps);

         Iterator properties = model.getData();
         List<String> userProps = new ArrayList<>();
         while (properties.hasNext())
         {
            PSProperty prop = (PSProperty) properties.next();
            String name = prop.getName();
            
            requiredUserProps.remove(name);

            if (userProps.contains(name))
               dupUserProps.add(name);
            if (sysProps.containsKey(name))
               dupSysProps.add(name);

            userProps.add(name);
         }

         if (!dupUserProps.isEmpty() || !dupSysProps.isEmpty())
         {
            valid = false;
            if (!dupSysProps.isEmpty())
            {
               ErrorDialogs.showErrorMessage(m_parentFrame,
                  MessageFormat.format(getResourceString("duplicateSysProps"),
                  new Object[] {dupSysProps.toString()}),
                  getResourceString("error"));
            }

            if (!dupUserProps.isEmpty())
            {
               ErrorDialogs.showErrorMessage(m_parentFrame,
                  MessageFormat.format(getResourceString("duplicateUserProps"),
                  new Object[] {dupUserProps.toString()}),
                  getResourceString("error"));
            }
         }
         
         if (!requiredUserProps.isEmpty())
         {
            displayUserInfo(mi_cfg.getName(), requiredUserProps);
            
            Vector data = model.getDataVector();
            for (final PSProperty property : requiredUserProps.values())
            {
               Vector<PSProperty> rowVector = new Vector<PSProperty>();
               rowVector.add(property);
               
               data.insertElementAt(rowVector, 0);
            }
         }

         return valid;
      }

      /* (non-Javadoc)
       * @see PSRelationshipEditorDialog.CfgPanel#updateDataFromView()
       */
      public void updateDataFromView()
      {
         PropertiesTableModel model =
            (PropertiesTableModel) mi_userPropertiesTable.getModel();
         
         mi_cfg.setUserDefProperties(model.getData());
      }

      //UI comps of this panel

      /**
       * The table that shows user props. Initialized in <code>initPanel()</code>
       * and never <code>null</code> after that.
       */
      private JTable mi_userPropertiesTable;
   }

   /**
    * Cloning config panel.
    */
   protected class CfgPanelCloning extends PSCfgPanel
   {
      /**
       * Ctor.
       * @param view view name, never <code>null</code>.
       */
      public CfgPanelCloning(String view, PSRelationshipEditorDialog owner)
      {
         super(view, owner);
      }

      /* (non-Javadoc)
       * @see PSRelationshipEditorDialog.CfgPanel#initPanel()
       */
      public void initPanel()
      {
         //relationship cloning panel
         JPanel topCloningPanel = new JPanel(new BorderLayout());
         topCloningPanel.setBorder(BorderFactory.createTitledBorder(
           getResourceString("cloningOptions")));

         topCloningPanel.setPreferredSize(new Dimension (300, 90));
         topCloningPanel.setMaximumSize(new Dimension (Integer.MAX_VALUE, 100));
         topCloningPanel.setVisible(true);

         mi_allowCloningCheckBox = new JCheckBox(
            getResourceString("rsAllowCloning"));
         mi_allowCloningCheckBox.setMnemonic(
                                 getResourceString("rsAllowCloning.mn").charAt(0));
         FocusAdapter cloningFocusAdapter = new FocusAdapter()
         {
           /*
            * Display the property description.
            */
            @Override
            public void focusGained(FocusEvent e)
            {
               String desc = "";

               if(mi_cfg != null)
               {
                  PSProperty prop = mi_cfg.getSysProperty(
                    PSRelationshipConfig.RS_ALLOWCLONING);

                  if(prop!=null)
                    desc = prop.getDescription();
               }

               mi_sharedDescription.setText(desc);
            }

            /*
             * Set the property description empty.
             */
            @Override
            public void focusLost(FocusEvent e)
            {
               mi_sharedDescription.setText("");
            }
         };

         mi_allowCloningCheckBox.addFocusListener(cloningFocusAdapter);

         mi_cloningLockedCheckBox = new JCheckBox(getResourceString("locked"));
         mi_cloningLockedCheckBox.setMnemonic(
                                    getResourceString("locked.mn").charAt(0));
         mi_cloningLockedCheckBox.addFocusListener(cloningFocusAdapter);

         JPanel cBoxPanel = new JPanel(new GridLayout(2,1));
         cBoxPanel.setMaximumSize(new Dimension(50,40));
         cBoxPanel.add(mi_allowCloningCheckBox);
         cBoxPanel.add(mi_cloningLockedCheckBox);
         topCloningPanel.add(cBoxPanel, BorderLayout.WEST);

         add(topCloningPanel);

         add(initCloningConfigTable());

         add(initCloneFieldOverridesTable());
      }

      private JPanel initCloningConfigTable()
      {
         AbstractAction noAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
               //executes no action, implemented to avoid sending the event up
               //to its container heirarchy.
            }
         };

         //create cone config table with custom cell editor and cell rend.
         PSCfgTableCellRenderer cellRenderer =
               new PSCfgTableCellRenderer(PSRelationshipEditorDialog.this);
         CfgTableCellEditor cellEditor = new CfgTableCellEditor();
         cellEditor.setConditionalEditorCommandName(
            COMMAND_NAME_CLONING_CONFIG_CONDITIONALS);

         PSCloningConfigModel model =
            new PSCloningConfigModel((new ArrayList<>()).iterator(), m_chConfig);

         mi_cloningConfigTable = new SpecialPropsTable(model,
            cellEditor, cellRenderer);

         mi_cloningConfigTable.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);

         mi_cloningConfigTable.setPreferredSize(new Dimension (300, 100));
         mi_cloningConfigTable.setMaximumSize(new Dimension (Integer.MAX_VALUE, 120));

         KeyStroke ksEnterRelease =
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
         mi_cloningConfigTable.getInputMap().put(ksEnterRelease, "noAction");

         mi_cloningConfigTable.getActionMap().put("noAction", noAction);

         mi_cloningConfigTable.addKeyListener( getIPSTableModelKeyAdapter() );

         mi_cloningConfigTable.getSelectionModel().addListSelectionListener(
            new CfgTableSelectionListener(mi_cloningConfigTable,
               mi_sharedDescription));

         JScrollPane cloningConfigTablePane = new JScrollPane(mi_cloningConfigTable);
         cloningConfigTablePane.setVerticalScrollBarPolicy(
            JScrollPane.VERTICAL_SCROLLBAR_NEVER);

         JPanel cloningTablePanel = new JPanel(new BorderLayout());
         cloningTablePanel.setBorder(BorderFactory.createTitledBorder(
           getResourceString("conditions")));

         cloningTablePanel.setMinimumSize(new Dimension (300, 90));
         cloningTablePanel.setPreferredSize(new Dimension (300, 100));
         cloningTablePanel.setMaximumSize(new Dimension (Integer.MAX_VALUE, 120));

         cloningTablePanel.add(cloningConfigTablePane, BorderLayout.CENTER);
         return cloningTablePanel;
      }

      /**
       * Initialize the cloen field overrides panel.
       * 
       * @return the newly created panel, never <code>null</code>.
       */
      private JPanel initCloneFieldOverridesTable()
      {
         AbstractAction noAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
               // executes no action, implemented to avoid sending the event up
               // to its container heirarchy.
            }
         };

         //create cone config table with custom cell editor and cell rend.
         PSCfgTableCellRenderer cellRenderer =
               new PSCfgTableCellRenderer(PSRelationshipEditorDialog.this);
         CfgTableCellEditor cellEditor = new CfgTableCellEditor();
         cellEditor.setExtensions(m_udfs.iterator());

         PSCloningFieldOverridesTableModel model =
            new PSCloningFieldOverridesTableModel((new ArrayList()).iterator());

         mi_cloneFieldOverridesTable = new SpecialPropsTable(model,
            cellEditor, cellRenderer);

         mi_cloneFieldOverridesTable.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);

         mi_cloneFieldOverridesTable.setPreferredSize(new Dimension (300, 200));

         KeyStroke ksEnterRelease = KeyStroke.getKeyStroke(
            KeyEvent.VK_ENTER, 0, true);
         mi_cloneFieldOverridesTable.getInputMap().put(
            ksEnterRelease, "noAction");

         mi_cloneFieldOverridesTable.getActionMap().put("noAction", noAction);

         mi_cloneFieldOverridesTable.addKeyListener(
            getIPSTableModelKeyAdapter());

         mi_cloneFieldOverridesTable.getSelectionModel().addListSelectionListener(
            new CfgTableSelectionListener(mi_cloneFieldOverridesTable,
               mi_sharedDescription));

         JScrollPane cloningFieldOverridesTablePane = new JScrollPane(
            mi_cloneFieldOverridesTable);

         JPanel cloningFieldOverridesTablePanel = new JPanel(
            new BorderLayout());
         cloningFieldOverridesTablePanel.setBorder(
            BorderFactory.createTitledBorder(getResourceString(
               "cloneFieldOverrides")));

         cloningFieldOverridesTablePanel.setMinimumSize(
            new Dimension (300, 100));
         cloningFieldOverridesTablePanel.setPreferredSize(
            new Dimension (300, 200));

         cloningFieldOverridesTablePanel.add(cloningFieldOverridesTablePane, 
            BorderLayout.CENTER);
            
         return cloningFieldOverridesTablePanel;
      }

      /* (non-Javadoc)
       * @see PSRelationshipEditorDialog.CfgPanel#updateViewFromData(
       *    PSRelationshipConfig)
       */
      @Override
      public void updateViewFromData(PSRelationshipConfig cfg)
      {
         super.updateViewFromData(cfg);

         TableModel model = new PSCloningConfigModel(cfg.getProcessChecks(), m_chConfig);
         mi_cloningConfigTable.setModel(model);

         List overrides = new ArrayList();

         if (cfg.getCloneOverrideFieldList() != null)
            overrides = cfg.getCloneOverrideFieldList();

         TableModel overridesModel = new PSCloningFieldOverridesTableModel(
            overrides.iterator());

         mi_cloneFieldOverridesTable.setModel(overridesModel);

         mi_allowCloningCheckBox.setSelected(cfg.isCloningAllowed());
         PSProperty prop = cfg.getSysProperty(
            PSRelationshipConfig.RS_ALLOWCLONING);

         mi_cloningLockedCheckBox.setSelected(prop.isLocked());

         TableColumn tc = mi_cloningConfigTable.getColumn(
            getResourceString("conditionsColumn"));

         tc.setPreferredWidth(20);
         tc.setMinWidth(20);
         tc.setMaxWidth(20);

         tc = mi_cloningConfigTable.getColumn(getResourceString("enable"));

         tc.setPreferredWidth(20);
         tc.setMinWidth(20);
         tc.setMaxWidth(20);

         tc = mi_cloneFieldOverridesTable.getColumn(
            getResourceString("conditionsColumn"));

         tc.setPreferredWidth(20);
         tc.setMinWidth(20);
         tc.setMaxWidth(20);

         tc = mi_cloneFieldOverridesTable.getColumn(getResourceString("field"));

         tc.setPreferredWidth(150);
         tc.setMinWidth(80);
         tc.setMaxWidth(200);
      }

      /* (non-Javadoc)
       * @see PSRelationshipEditorDialog.CfgPanel#validateViewData()
       */
      public boolean validateViewData()
      {
         return true;
      }

      /* (non-Javadoc)
       * @see PSRelationshipEditorDialog.CfgPanel#updateDataFromView()
       */
      public void updateDataFromView()
      {
         PSCloningConfigModel model =
            (PSCloningConfigModel) mi_cloningConfigTable.getModel();

         mi_cfg.setProcessChecks(cleanDisabledProcessChecks(model.getData()));

         PSCloningFieldOverridesTableModel overrideModel = 
            (PSCloningFieldOverridesTableModel) 
               mi_cloneFieldOverridesTable.getModel();

         PSCloneOverrideFieldList list = new PSCloneOverrideFieldList();

         Iterator iter = overrideModel.getData();

         while (iter.hasNext())
         {
            list.add(iter.next());
         }

         mi_cfg.setCloneOverrideFieldList(list);


         PSProperty prop = mi_cfg.getSysProperty(
            PSRelationshipConfig.RS_ALLOWCLONING);

         prop.setValue(mi_allowCloningCheckBox.isSelected());

         prop.setLock(mi_cloningLockedCheckBox.isSelected());
      }

      /**
       * Main cloning switch, if turned off - no cloning allowed.
       */
      private JCheckBox mi_allowCloningCheckBox;

      /**
       * Lock cloning check.
       */
      private JCheckBox mi_cloningLockedCheckBox;

      /**
       * The table that shows cloning config. Initialized in
       * <code>initPanel()</code> and never <code>null</code> after that.
       */
      private JTable mi_cloningConfigTable;

      /**
       * The table that shows clone field overrides. Initialized in
       * <code>initPanel()</code> and never <code>null</code> after that.
       */
      private JTable mi_cloneFieldOverridesTable;
   }

   /**
    * Extensions panel. Handles both PRE and POST exists.
    */
   protected class CfgPanelExtensions extends PSCfgPanel
   {
      /**
       * Ctor.
       * @param view view name, never <code>null</code>.
       * @param extType ext type id, must be either TYPE_PRE_EXTENSIONS
       * or TYPE_POST_EXTENSIONS.
       */
      public CfgPanelExtensions(String view, int extType, PSRelationshipEditorDialog owner)
      {
         super(view, owner);

         mi_extType = extType;
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#initPanel()
       */
      public void initPanel()
      {
         //create props table with custom cell editor and cell rend.
         PSCfgTableCellRenderer cellRenderer =
               new PSCfgTableCellRenderer(PSRelationshipEditorDialog.this);
         CfgTableCellEditor cellEditor = new CfgTableCellEditor();

         if (mi_extType == TYPE_PRE_EXTENSIONS)
            cellEditor.setExtensions(m_preExtensions.iterator());
         else
            cellEditor.setExtensions(m_postExtensions.iterator());

         ExtensionsTableModel model =
            new ExtensionsTableModel((new ArrayList()).iterator(), mi_extType);

         mi_extensionsTable = new SpecialPropsTable(model,
            cellEditor, cellRenderer);

         mi_extensionsTable.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);

         KeyStroke ksEnterRelease =
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
         mi_extensionsTable.getInputMap().put(ksEnterRelease, "noAction");

         AbstractAction noAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
               //executes no action, implemented to avoid sending the event up
               //to its container heirarchy.
            }
         };

         mi_extensionsTable.getActionMap().put("noAction", noAction);

         mi_extensionsTable.addKeyListener( getIPSTableModelKeyAdapter() );

         mi_extensionsTable.getSelectionModel().addListSelectionListener(
            new CfgTableSelectionListener(mi_extensionsTable,
               mi_sharedDescription));

         JScrollPane pane = new JScrollPane(mi_extensionsTable);

         //add table
         add(pane);
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#updateViewFromData(com.percussion.design.objectstore.PSRelationshipConfig)
       */
      public void updateViewFromData(PSRelationshipConfig cfg)
      {
         super.updateViewFromData(cfg);

         TableModel model =
            new ExtensionsTableModel(getExtensions(cfg, mi_extType), mi_extType);

         mi_extensionsTable.setModel(model);

         TableColumn tc = mi_extensionsTable.getColumn(getResourceString("conditionsColumn"));

         tc.setPreferredWidth(20);
         tc.setMinWidth(20);
         tc.setMaxWidth(20);
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#validateViewData()
       */
      public boolean validateViewData()
      {
         return true;
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#updateDataFromView()
       */
      public void updateDataFromView()
      {
         ExtensionsTableModel model =
            (ExtensionsTableModel)mi_extensionsTable.getModel();

         setExtensions(mi_cfg, model.getData(), mi_extType);
      }

      //UI comps of this panel

      /**
       * The table that shows user props. Initialized in <code>initPanel()</code>
       * and never <code>null</code> after that.
       */
      private JTable mi_extensionsTable;

      /**
       * Since pre and post exits panels are almost the same, this type
       * is used to load pre or post exits into this panel. Initialized by ctor.
       */
      private int mi_extType;
   }

   /**
    * Effects panel.
    */
   protected class CfgPanelEffects extends PSCfgPanel
   {
      public CfgPanelEffects(String view, PSRelationshipEditorDialog owner)
      {
         super(view, owner);
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#initPanel()
       */
      public void initPanel()
      {
         //create props table with custom cell editor and cell rend.
         PSCfgTableCellRenderer cellRenderer =
               new PSCfgTableCellRenderer(PSRelationshipEditorDialog.this);
         CfgTableCellEditor cellEditor = new CfgTableCellEditor();
         cellEditor.setExtensions(m_effects.iterator());

         PSEffectsTableModel model =
            new PSEffectsTableModel((new ArrayList()).iterator());

         mi_effectTable = new SpecialPropsTable(model,
            cellEditor, cellRenderer);

         mi_effectTable.setSelectionMode(
            ListSelectionModel.SINGLE_INTERVAL_SELECTION);

         KeyStroke ksEnterRelease =
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true);
         mi_effectTable.getInputMap().put(ksEnterRelease, "noAction");

         AbstractAction noAction = new AbstractAction()
         {
            public void actionPerformed(ActionEvent e)
            {
               //executes no action, implemented to avoid sending the event up
               //to its container heirarchy.
            }
         };

         mi_effectTable.getActionMap().put("noAction", noAction);

         mi_effectTable.addKeyListener( getIPSTableModelKeyAdapter() );

         mi_effectTable.getSelectionModel().addListSelectionListener(
            new CfgTableSelectionListener(mi_effectTable,
               mi_sharedDescription));

         JScrollPane pane = new JScrollPane(mi_effectTable);

         //add table
         add(pane);
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#updateViewFromData(com.percussion.design.objectstore.PSRelationshipConfig)
       */
      public void updateViewFromData(PSRelationshipConfig cfg)
      {
         super.updateViewFromData(cfg);

         TableModel model = new PSEffectsTableModel(mi_cfg.getEffects());
         mi_effectTable.setModel(model);

         TableColumn tc = mi_effectTable.getColumn(getResourceString("direction"));
         tc.setPreferredWidth(100);
         tc.setMinWidth(100);
         tc.setMaxWidth(100);

         tc = mi_effectTable.getColumn(getResourceString("conditionsColumn"));

         tc.setPreferredWidth(20);
         tc.setMinWidth(20);
         tc.setMaxWidth(20);
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#validateViewData()
       */
      public boolean validateViewData()
      {
         return true;
      }

      /* (non-Javadoc)
       * @see com.percussion.E2Designer.browser.PSRelationshipEditorDialog.CfgPanel#updateDataFromView()
       */
      public void updateDataFromView()
      {
         PSEffectsTableModel model =
            (PSEffectsTableModel)mi_effectTable.getModel();

         mi_cfg.setEffects(model.getData());
      }

      //UI comps of this panel

      /**
       * The table that shows user props. Initialized in <code>initPanel()</code>
       * and never <code>null</code> after that.
       */
      private JTable mi_effectTable;
   }

   /**
    * This is a right panel factory method that creates and then caches
    * concreate panel classes that are made specific to handle a given view.
    * This is basically home grown tab control manager, except that there
    * are no visible tabs. The logic used here can be easily replaced by a
    * real set of tabs.
    *
    * @param view never <code>null</code>.
    *
    * @return right panel with layout already created and that knows how to
    * load and save the data, never <code>null</code>.
    */
   private PSCfgPanel getRightPanel(String view)
   {
      if (view == null)
         throw new IllegalArgumentException("view may not be null");

      PSCfgPanel panel = (PSCfgPanel)m_rightPanels.get(view);

      //is it already in the map?
      if (panel!=null)
         return panel;

      /* Create an appropriate panel instance that is specific to a given view.
       */
      if(view.equals(RELATIONSHIP))
      {
         //top level relationship config
         panel = new CfgPanelRelationship(view, this);

      }
      else if(view.equals(getResourceString("userProps")))
      {
         //user props panel
         panel = new CfgPanelUserProps(view, this);
      }
      else if(view.equals(getResourceString("cloningProps")))
      {
         panel = new CfgPanelCloning(view, this);
      }
      else if(view.equals(getResourceString("preExits")))
      {
         //pre exits
         panel = new CfgPanelExtensions(view, TYPE_PRE_EXTENSIONS, this);
      }
      else if(view.equals(getResourceString("postExits")))
      {
         //post exits
         panel = new CfgPanelExtensions(view, TYPE_POST_EXTENSIONS, this);
      }
      else if( view.equals(getResourceString("effects")) )
      {
         //effects panel
         panel = new CfgPanelEffects(view, this);
      }
      else
      {
         //must be a top level tree element - return an empty/filler panel
         panel = new PSCfgPanelDummy(view, this);
      }

      //layout the panel
      panel.preInitPanel();
      panel.initPanel();
      panel.postInitPanel();

      //cache newly created panel
      m_rightPanels.put(view, panel);

      return panel;
   }



   /**
    * Adds a node for the specified relationship configuration at the end of
    * child list of supplied parent node.
    *
    * @param parentNode the parent node of the relationship node, assumed not
    * <code>null</code>
    * @param rsConfig the relationship configuration, assumed not <code>null
    * </code>
    *
    * @return the node added to the tree, never <code>null</code>
    */
   private DefaultMutableTreeNode addRelationshipNode(
      DefaultMutableTreeNode parentNode, PSRelationshipConfig rsConfig)
   {
      DefaultMutableTreeNode rsNode = new DefaultMutableTreeNode(rsConfig);

      rsNode.add(new DefaultMutableTreeNode(getResourceString("userProps")));
      rsNode.add(new DefaultMutableTreeNode(getResourceString("cloningProps")));
      rsNode.add(new DefaultMutableTreeNode(getResourceString("preExits")));
      rsNode.add(new DefaultMutableTreeNode(getResourceString("postExits")));
      rsNode.add(new DefaultMutableTreeNode(getResourceString("effects")));

      m_rsConfigTreeModel.insertNodeInto(rsNode, parentNode,
         parentNode.getChildCount());

      return rsNode;
   }

   /**
    * Validates current view and updates data from current view panel to the
    * configuration. Closes dialog. Calls {@link #save()}
    */
   @Override
   public void onOk()
   {
      if(!save())
         return;
      super.onOk();
   }

   /**
    * Validates current view and updates data from current view panel to the
    * configuration. Calls {@link #save()}
    */
   @Override
   public void onApply()
   {
      if(!save())
         return;
      super.onApply();
      refreshSelectedNode();

   }

   /**
    * Validates current view and updates data from current view panel to the
    * configuration. Saves the configuration to the server with the modified
    * configuration. Displays appropriate error messages if anything fails.
    * 
    * @return <code>true</code> if config validates and saves without error,
    *    else <code>false</code>.
    */
   private boolean save()
   {
      try 
      {
         // validate the current panel
         TreePath path = m_rsConfigTree.getSelectionPath();
         if (path != null)
         {
            Component comp = m_split.getRightComponent();

            if (comp instanceof PSCfgPanel)
            {
               PSCfgPanel cfgPanel = (PSCfgPanel) comp;

               if (!cfgPanel.validateViewData())
                  return false;

               cfgPanel.updateDataFromView();
            }
         }
         
         // validate user properties for Active Assembly category types
         Iterator configs = m_rsConfigSet.getConfigsByCategory(
            PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY);
         while (configs.hasNext())
         {
            PSRelationshipConfig config = (PSRelationshipConfig) configs.next();
            
            final Map<String, PSProperty> requiredUserProps =
                  new HashMap<>(ms_requiredUserProps);
            Iterator props = config.getUserDefProperties();
            while (props.hasNext())
            {
               PSProperty prop = (PSProperty) props.next();
               if (requiredUserProps.get(prop.getName()) != null)
                  requiredUserProps.remove(prop.getName());
            }
            
            if (!requiredUserProps.isEmpty())
            {
               displayUserInfo(config.getName(), requiredUserProps);
               
               Map userProps = config.getUserProperties();
               userProps.putAll(requiredUserProps);
               config.setUserDefProperties(userProps.values().iterator());
            }
         }
         
         E2Designer.getApp().getMainFrame().getObjectStore().
            saveRxConfiguration(PSConfigurationFactory.RELATIONSHIPS_CFG,
            m_rsConfigSet, false);
         
         JOptionPane.showMessageDialog(this, 
            getResourceString("needsServerRestart"));
      }
      catch (PSException ex)
      {
         ErrorDialogs.showErrorMessage(this, ex.getLocalizedMessage(),
            getResourceString("error"));

         return false;
      }

      return true;
   }
   
   /**
    * Displays the information of which required user properties were added
    * for the specified relationship because they were missing.
    * 
    * @param relationshipName the name of the relationship for which to
    *    display the message, assumed not <code>null</code> or empty.
    * @param missingUserProps a map with all missing user properties, assumed
    *    not <code>null</code> or empty. The map key is the property name
    *    as <code>String</code>, the map value is the property definition as
    *    <code>PSProperty</code> object.
    */
   private void displayUserInfo(String relationshipName, Map missingUserProps)
   {
      StringBuilder missing = new StringBuilder();
      Iterator names = missingUserProps.keySet().iterator();
      while (names.hasNext())
      {
         missing.append(names.next().toString());
         if (names.hasNext())
            missing.append(", ");
      }
      
      Object[] args =
      {
         relationshipName,
         PSRelationshipConfig.CATEGORY_ACTIVE_ASSEMBLY,
         missing.toString()
      };
      
      ErrorDialogs.showErrorDialog(m_parentFrame,
         getResourceString("missingRequiredUserProperties"), args,
         getResourceString("info"), JOptionPane.INFORMATION_MESSAGE);
   }

   /**
    * Appends a string that corresponds to the active panel view.
    * @param helpId the helpId that acts as the root of a unique help key. May be
    * <code>null</code>.
    * @return the unique help key string
    */
   @Override
   protected String subclassHelpId( String helpId )
   {

      Map<String, String> helpKeys = new HashMap<>();
      helpKeys.put(RELATIONSHIP,"Properties");
      helpKeys.put(getResourceString("userProps"),"UserProps");
      helpKeys.put(getResourceString("cloningProps"),"Cloning");
      helpKeys.put(getResourceString("preExits"),"PreExit");
      helpKeys.put(getResourceString("postExits"),"PostExit");
      helpKeys.put(getResourceString("effects"),"Effects");
      if(null != helpId &&
         null != m_currentView && helpKeys.containsKey(m_currentView))
         helpId += "_"+((String)helpKeys.get(m_currentView));

      return helpId ;

   }


   /**
    * Refreshes currently selected tree node
    */
   private void refreshSelectedNode()
   {
      TreePath selectedPath = m_rsConfigTree.getSelectionPath();
      if(selectedPath == null || selectedPath.getParentPath() == null)
         return;
      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode)selectedPath.getLastPathComponent();
      DefaultTreeModel model = (DefaultTreeModel)m_rsConfigTree.getModel();
      model.reload(node);
   }

   /**
    * Action method for Tree selection change event. When selection changes it
    * validates current view data, updates data to corresponding config object,
    * and updates the view(right) panel with data from new selection. Displays
    * an error message if the validation of current view data has any errors and
    * does not allow to change the view.
    *
    * @param e the selection change event, assumed not <code>null</code> as this
    * method is listener event method called by Swing.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      if(m_isErrorChangeView)
      {
         m_isErrorChangeView = false;
         return;
      }

      //Get the path that is getting deselected and update data from user
      //modified values. Removes the panels that are shown for this view.

      PSCfgPanel panel = null;

      TreePath oldPath = e.getOldLeadSelectionPath();
      if(oldPath != null)
      {
         PSRelationshipConfig config = getConfig(oldPath);

         //get an old one
         JPanel oldPanel = (JPanel)m_split.getRightComponent();

         if(config != null)
         {
            boolean isValid = false;

            if (oldPanel instanceof PSCfgPanel)
            {
               panel = (PSCfgPanel)oldPanel;
               isValid = panel.validateViewData();
            }

            if(!isValid)
            {
               ((TreeSelectionModel)e.getSource()).setSelectionPath(oldPath);
               return;
            }
            else
            {
               panel.updateDataFromView();
               panel.setVisible(false); //hide old one
            }
         }
      }

      //Update the view panel for new selection
      TreePath selPath = e.getNewLeadSelectionPath();
      if(selPath != null)
      {
         String view = getView(selPath);
         PSRelationshipConfig config = getConfig(selPath);

         //new one
         panel = getRightPanel(view);

         //default state to buttons
         m_deleteButton.setEnabled(false);

         if (config != null)
         {
            if (view.equals(RELATIONSHIP))
            {
               if (config.isUser())
               {
                  m_deleteButton.setEnabled(true);
                  if (panel instanceof CfgPanelRelationship)
                  {
                     JComboBox cbox =
                        ((CfgPanelRelationship) panel).mi_categoryComboBox;
                     cbox.removeAllItems();
                     for (int i = 0;
                        i < PSRelationshipConfig.CATEGORY_ENUM.length;
                        i++)
                     {
                        PSEntry entry = PSRelationshipConfig.CATEGORY_ENUM[i];
                        //We do not allow create folder category of 
                        //relationships
                        if (!entry
                           .getValue()
                           .equals(PSRelationshipConfig.CATEGORY_FOLDER))
                        {
                           cbox.addItem(PSRelationshipConfig.CATEGORY_ENUM[i]);
                        }
                     }
                  }
               }
               else
               {
                  //Keep all categories for system relationships 
                  if (panel instanceof CfgPanelRelationship)
                  {
                     JComboBox cbox =
                        ((CfgPanelRelationship) panel).mi_categoryComboBox;
                     cbox.removeAllItems();
                     for (int i = 0;
                        i < PSRelationshipConfig.CATEGORY_ENUM.length;
                        i++)
                     {
                        cbox.addItem(PSRelationshipConfig.CATEGORY_ENUM[i]);
                     }
                  }
               }
            }

            panel.updateViewFromData(config);

         }
      }

      panel.setVisible(true); //show new one
      m_split.setRightComponent(panel);
   }

   /**
    * Removes all conditional rules from a process check except
    * for the rule that indicates that the check is enabled
    * @param checks May be <code>null</code>.
    * @return Iterator, never <code>null</code>, may be empty.
    */
   public static Iterator cleanDisabledProcessChecks(Iterator checks)
   {
      List<PSProcessCheck> list = new ArrayList<>();

      if (checks != null)
      {
         while(checks.hasNext())
         {
            final PSProcessCheck check = (PSProcessCheck) checks.next();
            list.add(check);
            if(!isProcessCheckEnabled(check))
            {
               Iterator rules = check.getConditions();
               if(rules.hasNext())
               {
                  PSRule rule = (PSRule)rules.next();
                  List<PSRule> cleaned = new ArrayList<>(1);
                  cleaned.add(rule);
                  check.setConditions(cleaned.iterator());
               }
            }
         }
      }
      return list.iterator();
   }

   /**
    * Determines if a process check is enabled. A process check is
    * enabled if the first conditional of the first rule has an equals
    * operator, the variable and values are equal and both the variable and
    * value are instances of either <code>PSTextLiteral</code> or
    * <code>PSNumericLiteral</code>.
    * @param check the process check may be <code>null</code>.
    * @return <code>true<code> if the check is enabled, else <code>false</code>.
    */
   static boolean isProcessCheckEnabled(PSProcessCheck check)
   {
       if(null == check)
          return false;
       Iterator rules = check.getConditions();
       PSRule rule = null;
       if(rules.hasNext())
       {
          rule = (PSRule)rules.next();
          Iterator conds = rule.getConditionalRules();
          if(conds.hasNext())
          {
            PSConditional cond = (PSConditional)conds.next();
            String val = cond.getValue().getValueText();
            String var = cond.getVariable().getValueText();
            String op = cond.getOperator();
            if(op.equals("=") && var.equals(val)
               && (cond.getValue() instanceof PSTextLiteral
                  || cond.getValue() instanceof PSNumericLiteral)
               && (cond.getVariable() instanceof PSTextLiteral
                  || cond.getVariable() instanceof PSNumericLiteral))
            return true;
          }
       }

       return false;

    }


   /**
    * Gets the view represented by the given path.
    *
    * @param path the path to check, assumed not <code>null</code>
    *
    * @return <code>RELATIONSHIP</code> if the last node represents a
    * relationship configuration, otherwise its display name.
    */
   private String getView(TreePath path)
   {
      String view;

      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode)path.getLastPathComponent();
      if(node.getUserObject() instanceof PSRelationshipConfig)
         view = RELATIONSHIP;
      else
         view = node.getUserObject().toString();

      return view;
   }

   /**
    * Gets the configuration represented by the given path.
    *
    * @param path the path to check, assumed not <code>null</code>.
    *
    * @return the config, may be <code>null</code> if the last node of the path
    * does not represent a particular relationship or its child nodes.
    */
   private PSRelationshipConfig getConfig(TreePath path)
   {
      PSRelationshipConfig config = null;

      DefaultMutableTreeNode node =
         (DefaultMutableTreeNode)path.getLastPathComponent();
      if(node.getUserObject() instanceof PSRelationshipConfig)
         config = (PSRelationshipConfig)node.getUserObject();
      else if(path.getParentPath() != null)
         config = getConfig(path.getParentPath());

      return config;
   }

   /**
    * Prompts to enter a user relationship configuration name, validates for
    * uniqueness and adds to the configuration as user configuration. Refreshes
    * the tree also. Does not do anything if the current selected node is not
    * 'User' node.
    */
   private void onAdd()
   {
      TreePath path = getUserTreePath();
      if (path != null)
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode) path.getLastPathComponent();

         String selectedValue = JOptionPane.showInputDialog(this, "",
            getResourceString("addRelationshipTitle"),
            JOptionPane.INFORMATION_MESSAGE);

         if (selectedValue != null) //OK is clicked
         {
            if (selectedValue.trim().length() == 0)
            {
               ErrorDialogs.showErrorMessage(
                  this, getResourceString("mustEnterName"),
                  getResourceString("error") );
               return;
            }
            if (m_rsConfigSet.getConfig(selectedValue) == null)
            {
               PSRelationshipConfig config =
                  m_rsConfigSet.addConfig(selectedValue.toString(),
                  PSRelationshipConfig.RS_TYPE_USER);
                  
               DefaultMutableTreeNode configNode =
                  addRelationshipNode( node, config );
               m_rsConfigTree.setSelectionPath(
                  new TreePath(configNode.getPath()));
            }
            else
            {
               ErrorDialogs.showErrorMessage(
                  this, MessageFormat.format(
                  getResourceString("duplicateRelationship"),
                  new Object[] {selectedValue}),
                  getResourceString("error") );
            }
         }
      }
   }

   /**
    * Helper method to locate and get the TreePath for the User Relattionships. 
    * @return TreePath onject for the User relationships node. May be 
    * <code>null</code>.
    */
   private TreePath getUserTreePath()
   {
      TreeNode root = (TreeNode) m_rsConfigTree.getModel().getRoot();
      Enumeration e = root.children();
      TreePath path = null;
      TreePath rootPath = new TreePath(root);
      while (e.hasMoreElements())
      {
         TreeNode element = (TreeNode) e.nextElement();
         if(element.toString().equals("User"))
         {
            path = rootPath.pathByAddingChild(element);
            break;
         }
      }
      return path;
   }

   /**
    * Confirms and deletes the selected relationship node if that represents
    * a 'User' relationship configuration.
    */
   private void onDelete()
   {
      TreePath path = m_rsConfigTree.getSelectionPath();
      if(path != null)
      {
         DefaultMutableTreeNode node =
            (DefaultMutableTreeNode)path.getLastPathComponent();
         if( node.getUserObject() instanceof PSRelationshipConfig)
         {
            PSRelationshipConfig config =
               (PSRelationshipConfig)node.getUserObject();
            if(config.isUser())
            {
               int option = JOptionPane.showConfirmDialog(this,
                  MessageFormat.format( getResourceString("deleteRelationship"),
                  new Object[] {config.toString()}),
                  getResourceString("deleteRelationshipTitle"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.QUESTION_MESSAGE);

               if(option == JOptionPane.YES_OPTION)
               {
                  m_rsConfigSet.deleteConfig(config.toString());
                  m_rsConfigTreeModel.removeNodeFromParent(node);
                  m_rsConfigTree.setSelectionPath(path.getParentPath());
               }
            }
         }
      }
   }

   /**
    * Gets the list of extensions based on supplied type from the supplied
    * config.
    *
    * @param config the relationship config from which extensions need to get,
    * assumed not <code>null</code>
    * @param type the type of extensions, assumed to be one of the <code>
    * TYPE_PRE_EXTENSIONS</code>, <code>TYPE_POST_EXTENSIONS</code> values.
    *
    * @return the list of pre or post extensions, never <code>null</code>, may
    * be empty.
    */
   private Iterator getExtensions(PSRelationshipConfig config, int type)
   {
      List exts = new ArrayList();
      return exts.iterator();
   }

   /**
    * Sets the supplied extensions on the supplied configuration as pre or post
    * extensions based on supplied type. In case of pre-exits, gets all post
    * exits and combines with supplied pre-exits and sets the whole set of
    * extensions on the config. This is vice-versa for post-exits.
    *
    * @param config the configuration to set with extensions, assumed not <code>
    * null</code>
    * @param exts the extensions to set, assumed not <code>null</code>, may be
    * empty.
    * @param type the type of extensions, assumed to be one of the <code>
    * TYPE_PRE_EXTENSIONS</code>, <code>TYPE_POST_EXTENSIONS</code> values.
    */
   private void setExtensions(PSRelationshipConfig config, Iterator exts,
      int type)
   {
      Iterator prePostExts;

      if(type == TYPE_PRE_EXTENSIONS)
         prePostExts = getExtensions(config, TYPE_POST_EXTENSIONS);
      else
         prePostExts = getExtensions(config, TYPE_PRE_EXTENSIONS);
   }

   /**
    * The table model to support display and editing of 'System Properties'
    * and 'User Properties' views. In case of 'User' it supports adding new rows
    * and editing all columns and setting description, where as in case of
    * 'System' it allows editing of 'Value' and 'Locked' columns alone.
    */
   private class PropertiesTableModel extends PSTableModel
   {
      /**
       * Constructs this model with supplied parameters.
       *
       * @param properties the list of properties as data of the model, may not
       * be <code>null</code>, can be empty.
       * @param isUser supply <code>true</code> to represent 'User Properties'
       * or <code>false</code> to represent 'System Properties'.
       */
      public PropertiesTableModel(Iterator properties, boolean isUser)
      {
         if(properties == null)
            throw new IllegalArgumentException("properties may not be null.");

         Vector data = new Vector();
         while(properties.hasNext())
         {
            Vector rowVector = new Vector();
            rowVector.add(properties.next());
            data.add(rowVector);
         }

         setDataVector(data, ms_propColumns);

         if(data.size() < MIN_ROWS)
            setNumRows(MIN_ROWS);

         m_isUser = isUser;
      }

      /**
       * Gets the properties set on this table model.
       *
       * @return the list of properties, never <code>null</code>, may be empty.
       */
      public Iterator getData()
      {
         List<Object> props = new ArrayList<Object>();

         Iterator properties = getDataVector().iterator();
         while(properties.hasNext())
         {
            Object prop = ((Vector)properties.next()).get(0);
            if(prop instanceof PSProperty)
               props.add(prop);
         }

         return props.iterator();
      }

      /**
       * Checks whether the supplied cell is editable or not. If this model
       * object represents 'User Properties', any cell is editable if the
       * supplied row represents a <code>PSProperty</code> object (property is
       * defined), otherwise only 'Name' column is editable. If this model
       * object represents 'System Properties', the 'Value' and 'Locked' column
       * cells of the rows which displays properties alone are editable.
       *
       * @param row the row index of value to get, must be >= 0 and less than
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than
       * {@link #getColumnCount() columncount} of this model.
       *
       * @return <code>true</code> if the cell is editable, otherwise <code>
       * false</code>
       */
      @Override
      public boolean isCellEditable(int row, int col)
      {
         checkRow(row);
         checkColumn(col);

         boolean editable = false;

         PSProperty prop = getProperty(row);
         if(m_isUser)
         {
            if( col == COL_PROPS_NAME )
               editable = true;
            else if( prop != null)
                  editable = true;
         }
         else if(prop != null)
         {
            if( col == COL_PROPS_VALUE || col == COL_PROPS_LOCKED )
               editable = true;
         }

         return editable;
      }

      /**
       * Finds whether data rows can be removed.
       *
       * @return <code>true</code> if the model represents 'User Propertis',
       * otherwise <code>false</code>
       */
      @Override
      public boolean allowRemove()
      {
         return m_isUser;
      }

      /**
       * Gets the value at specified row and column.
       *
       * @param row the row index of value to get, must be >= 0 and less than
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than
       * {@link #getColumnCount() columncount} of this model.
       *
       * @return the value of cell, may be <code>null</code>
       */
      @Override
      public Object getValueAt(int row, int col)
      {
         checkRow(row);
         checkColumn(col);

         Object data = null;

         PSProperty prop = getProperty(row);
         if(prop != null)
         {
            if( col == COL_PROPS_NAME )
               data = prop.getName();
            else if( col == COL_PROPS_VALUE )
               data = prop.getValue();
            else if( col == COL_PROPS_LOCKED )
               data = prop.isLocked() ? Boolean.TRUE : Boolean.FALSE;
            else if( col == COL_PROPS_TYPE )
               data = m_propertyTypes.get(prop.getType());
         }

         return data;
      }

      /**
       * Sets the value at the specified cell. Updates only if the cell is
       * editable.
       *
       * @param value value to assign to cell, may not be <code>null</code>
       * @param row the row index of value to get, must be >= 0 and less than
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than
       * {@link #getColumnCount() columncount} of this model.
       */
      @Override
      public void setValueAt(Object value, int row, int col)
      {
         checkRow(row);
         checkColumn(col);

         if(value == null)
            throw new IllegalArgumentException("value may not be null.");

         if(!isCellEditable(row, col))
            throw new IllegalStateException(
               "Can not set a value on the cell that is not editable");

         PSProperty prop = getProperty(row);

         if(prop == null && m_isUser)
         {
            if(col == COL_PROPS_NAME)
            {
               Vector<PSProperty> data = new Vector<>();
               data.add(new PSProperty(value.toString()));
               getDataVector().setElementAt(data, row);
            }
            else
               throw new IllegalStateException(
                  "Name must be set before setting any other value");
         }
         else if(prop != null)
         {
            if( col == COL_PROPS_NAME )
               prop.setName(value.toString());
            else if( col == COL_PROPS_VALUE )
               prop.setValue(value);
            else if( col == COL_PROPS_LOCKED )
               prop.setLock((Boolean) value);
            else if( col == COL_PROPS_TYPE )
            {
               PropertyType type = (PropertyType)value;
               prop.setType(type.getType());
               if( type.getType() == PSProperty.TYPE_BOOLEAN &&
                  !(prop.getValue() instanceof Boolean) )
               {
                  if(prop.getValue() != null)
                     prop.setValue(Boolean.valueOf(prop.getValue().toString()));
                  else
                     prop.setValue(Boolean.FALSE);
               }
               else if(prop.getValue() instanceof Boolean &&
                  type.getType() != PSProperty.TYPE_BOOLEAN)
               {
                  prop.setValue(prop.getValue().toString());
               }
            }
         }
         fireTableRowsUpdated(row, row);
      }

      //implements IPSTableModel interface method.
      public String getDescription(int row)
      {
         checkRow(row);

         String description = "";
         PSProperty prop = getProperty(row);
         if(prop != null)
            description = prop.getDescription();

         return description;
      }

      /**
       * Finds whether description for any row can be modified.
       *
       * @return <code>true</code> if the model represents 'User Properties',
       * otherwise <code>false</code>
       */
      @Override
      public boolean allowChangeDescription()
      {
         return m_isUser;
      }

      //implements IPSTableModel interface method.
      @Override
      public void setDescription(int row, String desc)
      {
         checkRow(row);

         if(!allowChangeDescription())
            throw new UnsupportedOperationException(
               "description can not be modified");

         PSProperty prop = getProperty(row);
         if(prop != null)
            prop.setDescription(desc);

      }

      /**
       * Gets the property represented by this row.
       *
       * @param row the index, assumed to be less than row count of this model
       * and >= 0.
       *
       * @return the property, may be <code>null</code> if the row does not
       * represent a property.
       */
      private PSProperty getProperty(int row)
      {
         return (PSProperty)((Vector)getDataVector().get(row)).get(0);
      }

      /**
       * The flag that indicates whether this model represents 'User' or
       * 'System' properties. <code>true</code> represents 'User Properties' and
       * vice-versa. Initialized in the constructor and never changed after that.
       */
      private boolean m_isUser;
   }

   /**
    * The table model represents 'Pre Extensions', 'Post Extensions'
    * of the relationship configuration in the table.
    */
   private class ExtensionsTableModel extends PSTableModel
   {
      /**
       * Extension name column index.
       */
      public static final int COL_EXT_NAME = 0;

      /**
       * Extension conditional column index.
       */
      public static final int COL_EXT_COND = 1;

      /**
       * Constructs this model with supplied parameters.
       *
       * @param extensions the list of extensions to edit/view, may not be
       * <code>null</code>.
       * @param extType the type of extensions it is representing, must be one
       * of the <code>TYPE_PRE_EXTENSIONS</code>, <code>TYPE_POST_EXTENSIONS
       * </code> and <code>TYPE_EFFECTS</code> values.
       */
      public ExtensionsTableModel(Iterator extensions, int extType)
      {
         if(extensions == null)
            throw new IllegalArgumentException("extensions may not be null.");

         if(extType != TYPE_PRE_EXTENSIONS &&
            extType != TYPE_POST_EXTENSIONS)
         {
            throw new IllegalArgumentException("extType is not valid");
         }

         m_type = extType;

         Vector<Vector<Object>> data = new Vector<Vector<Object>>();
         while(extensions.hasNext())
         {
            Object obj = extensions.next();
            Vector<Object> element = new Vector<>(getColumnNames().size());
            data.add(element);

            if(!(obj instanceof PSConditionalExtension))
            {
               throw new IllegalArgumentException(
                  "all elements in extensions must be instances of" +
                  " PSConditionalExtension");
            }

            PSConditionalExtension ext = (PSConditionalExtension) obj;
            element.add( new OSExtensionCall(ext.getExtension()) );
            element.add(IteratorUtils.toList(ext.getConditions()));
         }

         setDataVector(data, getColumnNames());

         //manages to have minimum number of rows in model.
         if(data.size() < MIN_ROWS)
            setNumRows(MIN_ROWS);
      }

      /**
       * Gets the extensions or effecrs set on this table model.
       *
       * @return the list of extensions, never <code>null</code>, may be empty.
       */
      public Iterator getData()
      {
         List extensions = new ArrayList();

         for (int i = 0; i < getRowCount(); i++)
         {
            Object rowData;

            Object obj = getValueAt(i, COL_EXT_NAME);
            Object conds = getValueAt(i, COL_EXT_COND);

            if(obj instanceof PSExtensionCall)
            {
               rowData = new PSConditionalExtension((PSExtensionCall)obj);
               if(conds instanceof List)
               {
                  ((PSConditionalExtension)rowData).setConditions(
                     ((List)conds).iterator());
               }

               extensions.add(rowData);
            }
         }

         return extensions.iterator();
      }

      /**
       * Sets the value at the specified cell. Overriden to set empty list of
       * conditions in the column <code>COL_EXT_COND</code> for that row if it
       * is not already set, when an extension is chosen in the column <code>
       * COL_EXT_NAME</code>. Useful to render a 'conditions' button when an
       * extension chosen in a new row. See super's description for parameter
       * description.
       */
      @Override
      public void setValueAt(Object value, int row, int col)
      {
         if(value != null)
         {
            if(col == COL_EXT_NAME)
            {
               List conds = (List)getValueAt(row, COL_EXT_COND);
               if(conds == null)
               {
                  super.setValueAt(
                     new ArrayList(), row, COL_EXT_COND);
               }
            }
         }
         super.setValueAt(value, row, col);
      }

      /**
       * Checks whether the supplied cell is editable or not. Overridden to make
       * the <code>COL_EXT_COND</code> is editable only if the extension/effect
       * in that row is set.
       *
       * @param row the row index of value to get, must be >= 0 and less than
       * {@link #getRowCount() rowcount} of this model.
       * @param col the column index of value to get, must be >= 0 and less than
       * {@link #getColumnCount() columncount} of this model.
       *
       * @return <code>true</code> if the cell is editable, otherwise <code>
       * false</code>
       */
      @Override
      public boolean isCellEditable(int row, int col)
      {
         if(col == COL_EXT_COND)
         {
            if(getValueAt(row, COL_EXT_NAME) == null)
               return false;
         }

         return true;
      }

      /**
       * Gets the column names of the model based on type of extensions it is
       * representing.
       *
       * @return the list of column names, never <code>null</code> or empty.
       */
      private Vector getColumnNames()
      {
         return ms_extColumns;
      }

      //implements interface method.
      @Override
      public String getDescription(int row)
      {
         String description = "";
         Object obj = getValueAt(row, COL_EXT_NAME);
         if(obj instanceof OSExtensionCall)
         {
            OSExtensionCall call = (OSExtensionCall)obj;
            IPSExtensionDef def = call.getExtensionDef();
            description = def.getInitParameter(
               IPSExtensionDef.INIT_PARAM_DESCRIPTION);
         }

         return description;
      }

      //overridden to return true always.
      @Override
      public boolean allowRemove()
      {
         return true;
      }

      //overridden to return true always.
      @Override
      public boolean allowMove()
      {
         return true;
      }

      /**
       * Gets the type of extensions this table model representing.
       *
       * @return the type, one of the <code>TYPE_PRE_EXTENSIONS</code>,
       * <code>TYPE_POST_EXTENSIONS</code> and <code>TYPE_EFFECTS</code> values
       */
      public int getExtensionsType()
      {
         return m_type;
      }

      /**
       * The type of extensions this table model representing, will be one of
       * the <code>TYPE_PRE_EXTENSIONS</code>, <code>TYPE_POST_EXTENSIONS
       * </code> and <code>TYPE_EFFECTS</code> values, initialized in the
       * constructor and never modified after that.
       */
      private int m_type;
   }

   /**
    * The table cell editor that supports editing of all columns of the
    * relationship configuration table's different views. In general it provides
    * a text field editor for the data that represents a <code>String</code>,
    * check-box for a <code>Boolean</code> and provides different editors
    * (combo-box, panel with combo-box and button, button) as required by column
    * definition.
    */
   class CfgTableCellEditor extends AbstractCellEditor
      implements TableCellEditor, ItemListener, ActionListener
   {
      /**
       * Constructs this editor and initializes all its editor components.
       */
      public CfgTableCellEditor()
      {
         m_textFieldEditor = new JTextField();
         m_comboEditor = new JComboBox();
         m_dateEditor = new PSCalendarField();
         m_dateEditor.setHeightFixed(true);
         m_dateEditor.setDateTextFieldSize(10); 
         m_comboEditor.addItemListener(this);

         m_endpointComboEditor = new JComboBox();
         Iterator it = PSRelationshipConfig.getActivationEndPoints();

         while (it.hasNext())
         {
            String endpoint = (String)it.next();

            m_endpointComboEditor.addItem(
                  PSEffectsTableModel.translateEndpointName(endpoint, true));
         }

         m_overrideFieldComboEditor = new JComboBox();
         m_overrideFieldComboEditor.addItemListener(this);

         Iterator itSysFields = m_ceSystemFields.iterator();

         while (itSysFields.hasNext())
         {
            String element = (String) itSysFields.next();
            m_overrideFieldComboEditor.addItem(element);
         }

         m_ntransComboEditor = new JComboBox();
         m_ntransComboEditor.addItem("Default");
         Iterator triggers =
            PSCatalogTransitionActionTriggers.getCatalog(false).iterator();
         while(triggers.hasNext())
            m_ntransComboEditor.addItem(triggers.next());

         m_checkEditor = new JCheckBox();
         m_checkEditor.addActionListener(this);
         m_checkEditor.setHorizontalAlignment(SwingConstants.CENTER);
         m_checkEditor.setBackground(
            UIManager.getColor("Table.selectionBackground"));

         m_extComboEditor = new JComboBox();
         m_extComboEditor.addItemListener(this);
         AbstractAction exitAction = new AbstractAction()
         {
            //displays exit properties dialog to edit the parameter values of
            //the extension call represented by the cell.
            public void actionPerformed(ActionEvent e)
            {
               OSExtensionCall call = (OSExtensionCall)getValue(DATA_OBJECT);
               if (call != null)
               {
                  final OSExtensionCall result = editExitCall(call);
                  if (result != null)
                  {
                     putValue(DATA_OBJECT, result);
                  }
               }

               stopCellEditing();
            }
         };

         m_extEditButton = new UTBrowseButton();
         m_extEditButton.setAction(exitAction);

         m_extensionEditor = new JPanel();
         m_extensionEditor.setLayout(new BorderLayout());
         m_extensionEditor.add(m_extComboEditor, BorderLayout.CENTER);
         m_extensionEditor.add(m_extEditButton, BorderLayout.EAST);

         AbstractAction condAction = new AbstractAction()
         {
            //Displays a dialog to edit the conditions.
            public void actionPerformed(ActionEvent e)
            {
               List rules = (List) getValue(DATA_OBJECT);

               if (rules == null)
                  throw new IllegalStateException("no conditions found");

               // checking whether this is editCloningConditionals 
               boolean editCloningConditionals = false;
               String actionCommand = e.getActionCommand();
               if (actionCommand != null)
               {
                  editCloningConditionals = actionCommand.equals(
                        COMMAND_NAME_CLONING_CONFIG_CONDITIONALS);
               }

               // checking whether this is cloning view
               boolean isCloningView = getCurViewName().equals(
                     getResourceString("cloningProps"));

               editRules(rules, editCloningConditionals, isCloningView);

               putValue(DATA_OBJECT, rules);
               stopCellEditing();
            }

            private void editRules(List rules, boolean editCloningConditionals,
                  boolean isCloningView)
            {
               try
               {
                  /*
                   * Pull out the first rule if this is the cloning view
                   * as this is the rule that determines if this property
                   * is checked or not and should not be editable other then
                   * by checking or unchecking.
                   */
                  Object firstConditional = null;

                  if (isCloningView && editCloningConditionals && 
                     !rules.isEmpty())
                  {
                     firstConditional = rules.get(0);
                     rules.remove(0);
                  }
                  
                  PSRuleEditorDialog ruleDlg =
                     new PSRuleEditorDialog(PSRelationshipEditorDialog.this);
                  ruleDlg.center();
                  ruleDlg.onEdit(rules.iterator());
                  ruleDlg.setVisible(true);
                  Iterator iter = ruleDlg.getRulesIterator();
                  rules.clear();
                  // Put the removed conditional back to the top of
                  // the list if necessary
                  if (null != firstConditional)
                     rules.add(firstConditional);
                  while(iter.hasNext())
                     rules.add(iter.next());

               }
               catch (ClassNotFoundException cnfe)
               {
                  ErrorDialogs.showErrorMessage(
                     PSRelationshipEditorDialog.this,
                        cnfe.getMessage(), getResourceString("error"));
               }
            }
         };

         condAction.putValue(AbstractAction.SMALL_ICON, ms_condIcon);
         m_condEditor = new JButton();
         m_condEditor.setAction(condAction);
      }
      
      /**
       * Set the command name for the conditional edit button.
       * 
       * @param commandName the new command name, may be <code>null</code> or
       *    empty.
       */
      public void setConditionalEditorCommandName(String commandName)
      {
         m_condEditor.setActionCommand(commandName);
      }

      /**
       * Sets the list of extensions to show in the extension combo-box editor
       * for the Extensions view.
       *
       * @param exits the exits to show, may not be <code>null</code>, can be
       * empty.
       */
      public void setExtensions(Iterator exits)
      {
         if(exits == null)
            throw new IllegalArgumentException("exits may not be null.");

         m_extComboEditor.removeItemListener(this);
         m_extComboEditor.removeAllItems();
         while(exits.hasNext())
            m_extComboEditor.addItem(exits.next());
         m_extComboEditor.addItemListener(this);
      }

      /**
       * Sets the property data types to show in the combo-box editor for
       * 'Properties' view.
       *
       * @param types the types to show, may not be <code>null</code>, can be
       * empty.
       */
      public void setPropertyTypes(Iterator types)
      {
         if(types == null)
            throw new IllegalArgumentException("types may not be null.");

         m_comboEditor.removeAllItems();
         while(types.hasNext())
            m_comboEditor.addItem(types.next());
      }

      /**
       * Action method for item selection change event for combo-box editors. In
       * general it stops cell editing. In case of extensions editor, displays
       * a dialog to edit the extension param values it has any.
       *
       * @param e the selection change event, assumed not to be <code>null
       * </code> as this method is called by Swing.
       */
      public void itemStateChanged(ItemEvent e)
      {
         if(e.getSource() == m_extComboEditor)
         {
            if(e.getStateChange() == ItemEvent.SELECTED &&
               m_extComboEditor.isShowing())
            {
               IPSExtensionDef def = (IPSExtensionDef)e.getItem();
               OSExtensionCall call = createExitCall(def);
               if(call == null)
                  throw new IllegalStateException("extension got removed");

               m_extEditButton.getAction().putValue(DATA_OBJECT, call);

               stopCellEditing();
            }
         }
         else
            stopCellEditing();
      }

      /**
       * Action method to stop cell editing when the editors are done with their
       * actions.
       *
       * @param e the action event, assumed not to be <code>null</code> as this
       * method is called by Swing.
       */
      public void actionPerformed(ActionEvent e)
      {
         stopCellEditing();
      }

      /**
       * Checks whether a cell is editable or not. In general the cell is
       * editable if the event is a mouse double-click, but if the cell renderer
       * component is <code>JCheckBox</code> or <code>JButton</code>, it allows
       * editing for a single-click.
       *
       * @param event the event, assumed not to be <code>null</code> as this
       * method is called by Swing.
       *
       * @return <code>true</code> if the cell is editable, otherwise <code>
       * false</code>
       */
      @Override
      public boolean isCellEditable(EventObject event)
      {
         if (event instanceof MouseEvent)
         {
            MouseEvent e = (MouseEvent)event;
            int clickCount = 2;
            if(e.getSource() instanceof JTable)
            {
               JTable source = (JTable)e.getSource();
               int row = source.rowAtPoint(e.getPoint());
               int col = source.columnAtPoint(e.getPoint());
               TableCellRenderer renderer = source.getCellRenderer(row, col);
               Component comp = renderer.getTableCellRendererComponent(source,
                  source.getValueAt(row, col), source.isCellSelected(row, col),
                  source.hasFocus(), row, col);

               if( comp instanceof JCheckBox || comp instanceof JButton)
                  clickCount = 1;
            }
            return e.getClickCount() >= clickCount;
         }
         return false;
      }

      //implements interface method
      public Object getCellEditorValue()
      {
         Object data = null;

         if(m_curEditorComponent == m_endpointComboEditor)
         {
            data = m_endpointComboEditor.getSelectedItem();
         }
         if(m_curEditorComponent == m_overrideFieldComboEditor)
         {
            data = m_overrideFieldComboEditor.getSelectedItem();
         }
         else if(m_curEditorComponent == m_textFieldEditor)
         {
            data = m_textFieldEditor.getText();
         }
         else if(m_curEditorComponent == m_checkEditor)
         {
            data = m_checkEditor.isSelected() ? Boolean.TRUE : Boolean.FALSE;
         }
         else if(m_curEditorComponent == m_comboEditor)
         {
            data = m_comboEditor.getSelectedItem();
         }
         else if(m_curEditorComponent == m_condEditor)
         {
            data = m_condEditor.getAction().getValue(DATA_OBJECT);
         }
         else if(m_curEditorComponent == m_extensionEditor)
         {
            data = m_extEditButton.getAction().getValue(DATA_OBJECT);
         }
         else if(m_curEditorComponent == m_ntransComboEditor)
         {
            data = m_ntransComboEditor.getSelectedItem();
         }
         else if(m_curEditorComponent == m_dateEditor)
         {
            if(m_lastSelectedTable != null)
               m_lastSelectedTable.setRowHeight(m_lastSelectedRow, 16);
            data = m_dateEditor.getDateString();
         }

         return data;
      }

      /**
       * Implements interface method to show different editor components based
       * on cell and set its state from the cell value. See the interface for
       * description about parameters and return value. The following explains
       * the editors used for different table views.
       * <pre>
       *    PropertiesTable
       *       Name  - JTextField
       *       Value - JCheckBox if the 'Type' for that row is Boolean,
       *       else if this is rs_namedtransition them special comboBox
       *       otherwise JTextField
       *       Locked - JCheckBox
       *       Type - JComboBox with available property value types.
       *
       *    CloningPropertiesTable
       *       Enable - JCheckBox
       *       Conditions - JButton (clicking on that displays a dialog)
       *
       *    ExtensionsTableModel
       *       Exit or Effect - JPanel with JComboBox and JButton
       *       Conditions - JButton (clicking on that displays a dialog)
       * </pre>
       */
      public Component getTableCellEditorComponent(JTable table,
         Object value, boolean isSelected, int row, int column)
      {
         //erase any previous text
         m_textFieldEditor.setText("");
         m_comboEditor.setSelectedItem(null);
         m_endpointComboEditor.setSelectedItem(null);
         m_overrideFieldComboEditor.setSelectedItem(null);
         m_extComboEditor.setSelectedItem(null);
         m_ntransComboEditor.setSelectedItem(null);
         m_lastSelectedRow = row;
         m_lastSelectedTable = table;

         TableModel model = table.getModel();
         if(model instanceof PropertiesTableModel)
         {
            if(column == COL_PROPS_NAME)
            {
               m_curEditorComponent = m_textFieldEditor;
               if(value != null)
                  m_textFieldEditor.setText(value.toString());
            }
            else if(column == COL_PROPS_VALUE)
            {
               int type = PSProperty.TYPE_STRING;
               PropertyType propType =
                  (PropertyType)model.getValueAt(row, COL_PROPS_TYPE);
               if(propType != null)
                  type = propType.getType();

               if(type == PSProperty.TYPE_BOOLEAN)
               {
                  m_curEditorComponent = m_checkEditor;
                  if(value != null)
                     m_checkEditor.setSelected((Boolean) value);
               }
               else if(type == PSProperty.TYPE_DATE)
               {
                  table.setRowHeight(row, 30);
                  m_curEditorComponent = m_dateEditor;
                  if(value != null)
                     m_dateEditor.setDate((String)value, "yyyy-MM-dd");
               }
               else
               {
                  m_curEditorComponent = m_textFieldEditor;
                  if(value != null)
                     m_textFieldEditor.setText(value.toString());
               }
            }
            else if(column == COL_PROPS_LOCKED)
            {
               m_curEditorComponent = m_checkEditor;
               if(value != null)
                  m_checkEditor.setSelected((Boolean) value);
            }
            else if(column == COL_PROPS_TYPE)
            {
               m_curEditorComponent = m_comboEditor;

               if(value != null)
                  m_comboEditor.setSelectedItem( value );
            }
         }
         else if(model instanceof PSCloningConfigModel)
         {
            if(column == PSCloningConfigModel.COL_CLONE_ENABLE)
            {
               m_curEditorComponent = m_checkEditor;
               if(value != null)
                  m_checkEditor.setSelected((Boolean) value);
            }
            else if(column == PSCloningConfigModel.COL_CLONE_COND)
            {
               m_curEditorComponent = m_condEditor;
               if(value != null)
               {
                  m_condEditor.getAction().putValue(
                     DATA_OBJECT, value);
               }
            }
         }
         else if(model instanceof PSEffectsTableModel)
         {
            if(column == PSEffectsTableModel.COL_EXT_ENDPOINT)
            {
               m_curEditorComponent = m_endpointComboEditor;
               m_endpointComboEditor.setSelectedItem(value);
            }
            else if(column == PSEffectsTableModel.COL_EXT_NAME)
            {
               m_curEditorComponent = m_extensionEditor;
               List exitDefs = m_effects;

               m_extEditButton.getAction().putValue(DATA_OBJECT, value);
               if(value != null)
               {
                  for (Object exitDef : exitDefs) {
                     IPSExtensionDef def = (IPSExtensionDef) exitDef;
                     if (def.equals(((OSExtensionCall) value).getExtensionDef())) {
                        m_extComboEditor.setSelectedItem(def);
                        break;
                     }
                  }

               }

            }
            else if(column == PSEffectsTableModel.COL_EXT_COND)
            {
               m_curEditorComponent = m_condEditor;
               if(value != null)
               {
                  m_condEditor.getAction().putValue(
                     DATA_OBJECT, value);
               }
            }
         }
         else if (model instanceof PSCloningFieldOverridesTableModel)
         {
            if (column == PSCloningFieldOverridesTableModel.COL_FIELD)
            {
               m_curEditorComponent = m_overrideFieldComboEditor;
               m_overrideFieldComboEditor.setSelectedItem(value);
            }
            else if (column == PSCloningFieldOverridesTableModel.COL_UDF)
            {
               m_curEditorComponent = m_extensionEditor;
               List exitDefs = m_udfs;

               m_extEditButton.getAction().putValue(DATA_OBJECT, value);
               if (value != null)
               {
                  Iterator exits = exitDefs.iterator();
                  while (exits.hasNext())
                  {
                     IPSExtensionDef def = (IPSExtensionDef) exits.next();
                     if (def.equals(((OSExtensionCall) value).getExtensionDef()))
                     {
                        m_extComboEditor.setSelectedItem(def);
                        break;
                     }
                  }
               }
            }
            else if (column == PSCloningFieldOverridesTableModel.COL_COND)
            {
               m_curEditorComponent = m_condEditor;
               if (value != null)
               {
                  m_condEditor.getAction().putValue(DATA_OBJECT,  value);
               }
            }
         }
         else if (model instanceof ExtensionsTableModel)
         {
            ExtensionsTableModel extModel = (ExtensionsTableModel)model;

            if(column == ExtensionsTableModel.COL_EXT_NAME)
            {
               m_curEditorComponent = m_extensionEditor;
               List exitDefs;
               if(extModel.getExtensionsType() == TYPE_PRE_EXTENSIONS)
                  exitDefs = m_preExtensions;
               else
                  exitDefs = m_postExtensions;

               m_extEditButton.getAction().putValue(DATA_OBJECT, value);
               if(value != null)
               {
                  for (Object exitDef : exitDefs) {
                     IPSExtensionDef def = (IPSExtensionDef) exitDef;
                     if (def.equals(((OSExtensionCall) value).getExtensionDef())) {
                        m_extComboEditor.setSelectedItem(def);
                        break;
                     }
                  }

               }

            }
            else if(column == ExtensionsTableModel.COL_EXT_COND)
            {
               m_curEditorComponent = m_condEditor;
               if(value != null)
               {
                  m_condEditor.getAction().putValue(
                     DATA_OBJECT, value);
               }
            }
         }

         return m_curEditorComponent;
      }

      /**
       * The text field editor to edit string values, initialized in the
       * constructor and never <code>null</code> after that. Its display text
       * gets set as per the cell value that this is used for.
       */
      private JTextField m_textFieldEditor;

      /**
       * The combo-box editor to edit property types, initialized in the
       * constructor and never <code>null</code> after that.
       */
      private JComboBox m_comboEditor;

      /**
       * The combo-box editor to show extensions or effects, initialized in the
       * constructor and never <code>null</code> after that.
       */
      private JComboBox m_extComboEditor;

      /**
       * The combo-box editor to show transition action triggers for rs_namedtransition,
       * initialized in the constructor and never <code>null</code> after that.
       */
      private JComboBox m_ntransComboEditor;

      /**
       * The button to edit extensions or effects param values, initialized in
       * the constructor and never <code>null</code> after that. The extension
       * call that is being edited is stored in this button action and used as
       * value to edit or save.
       */
      private UTBrowseButton m_extEditButton;

      /**
       * The combo editor to edit endpoint values, initialized in the
       * constructor and never <code>null</code> after that.
       */
      private JComboBox m_endpointComboEditor;

      /**
       * The combo editor to edit override field names. Initialized in the
       * constructor and never <code>null</code> after that.
       */
      private JComboBox m_overrideFieldComboEditor;

      /**
       * The panel to edit extensions or effects, initialized in the constructor
       * and never <code>null</code> or modified after that.
       */
      private JPanel m_extensionEditor;

      /**
       * The editor to edit <code>Boolean</code> values, initialized in the
       * constructor and never <code>null</code> after that. State gets modified
       * as per cell value being edited.
       */
      private JCheckBox m_checkEditor;

      /**
       * The editor to use to edit the conditions,  initialized in the
       * constructor and never <code>null</code> after that. The conditions that
       * are being edited is stored in this button action and used as value to
       * edit or save.
       */
      JButton m_condEditor;

      /**
       * The component that is used to edit the current cell being edited is
       * stored in this and is used later to give back the editor value when the
       * editing is stopped.
       */
      private Component m_curEditorComponent = null;
      
      /**
       * The component used to edit date cells.
       */
      private PSCalendarField m_dateEditor;
      
      /**
       * The index of the last selected table row
       */
      private int m_lastSelectedRow;
      
      /**
       * The table where the last selection was made
       */
      private JTable m_lastSelectedTable;
   }

   /**
    *
    */
   String getCurViewName()
   {
      Component comp = m_split.getRightComponent();

      if (comp instanceof PSCfgPanel)
         return ((PSCfgPanel)comp).getView();
      else
         return "";
   }

   /**
    * Supports updating description text area as the row selection changes in
    * the table.
    */
   private class CfgTableSelectionListener implements ListSelectionListener
   {
      /**
       * Constructor that passes in the calling table reference
       * @param table reference to the calling table. Must not be <code>null</code>.
       * @param description descrption field to update with selected row desc.,
       * never <code>null</code>.
       */
      public CfgTableSelectionListener(JTable table, JTextArea description)
      {
         if(null == table)
            throw new IllegalArgumentException("table cannot be null.");
         if(null == description)
            throw new IllegalArgumentException("description cannot be null.");

         m_table = table;
         m_desc = description;
      }
      
      /**
       * Action method for table row selection change, displays the description
       * of the first selected row in the text area.
       *
       * @param event the selection change event, assumed not <code>null</code>
       * as this is called by Swing.
       */
      public void valueChanged(ListSelectionEvent event)
      {
         ListSelectionModel selModel = (ListSelectionModel)event.getSource();
         int row = selModel.getMinSelectionIndex();

         TableModel model = m_table.getModel();

         if(model instanceof IPSTableModel && row >= 0 &&
            row < model.getRowCount())
         {
            String desc = ((IPSTableModel)model).
               getDescription(row);

            m_desc.setText(desc);
         }
         else
         {
            m_desc.setText("");
         }

      }

      /**
       * Reference to the calling table. Initialized in the ctor. Must not
       * be <code>null</code>.
       */
      private JTable m_table;

      /**
       * Reference to the description text area to update with selected row
       * desc. if any.
       */
      private JTextArea m_desc;
   }

   /**
    * Class to define property types with an identifier and a display text.
    */
   private class PropertyType
   {
      /**
       * Constructs this object with supplied parameters.
       *
       * @param type the property data type, assumed to be one of the allowed
       * types of properties
       * @param typeString the display string of type, assumed not <code>null
       * </code> or empty.
       */
      private PropertyType(int type, String typeString)
      {
         m_type = type;
         m_typeString = typeString;
      }

      /**
       * Gets the type.
       *
       * @return the type.
       */
      private int getType()
      {
         return m_type;
      }

      /**
       * Gets display string of this property type.
       *
       * @return the propety type display string, never <code>null
       * </code> or empty.
       */
      public String toString()
      {
         return m_typeString;
      }

      /**
       * The display string of property type, initialized in the ctor, never
       * <code>null</code>, empty or modified after that.
       */
      private String m_typeString;

      /**
       * The type of the property, initialized in the ctor, never modified after
       * that.
       */
      private int m_type;
   }

   /**
    * Class to represent the special properties tables for sys properties
    * and built in effects.
    */
   private class SpecialPropsTable extends JTable
   {
      /**
       * Constructs a table with the Default table model
       *
       */
      public SpecialPropsTable(TableCellEditor cellEditor,
         TableCellRenderer cellRenderer)
      {
         this(null, cellEditor, cellRenderer);
      }

      /**
       * Constructs a table using the passed in table model
       * @param model the TableModel to be used for this table.
       * Should not be <code>null</code>.
       */
      public SpecialPropsTable(TableModel model,
         TableCellEditor cellEditor, TableCellRenderer cellRenderer)
      {
         super(model);

         if (cellEditor== null)
            throw new IllegalArgumentException("cellEditor may not be null");
            
         if (cellRenderer== null)
            throw new IllegalArgumentException("cellRenderer may not be null");

         mi_cellEditor = cellEditor;
         mi_cellRenderer = cellRenderer;
         
         getTableHeader().setReorderingAllowed(false);
      }
      
      /**
       * Returns the cell editor for the specified row and column
       * @param row the table cell's row index
       * @param column the table cell's column index
       * @return the cell editor. Never <code>null</code>.
       */
      public TableCellEditor getCellEditor(int row, int column)
      {
         return mi_cellEditor;
      }

      /**
       * Returns the cell renderer for the specified row and column
       * @param row the table renderer's row index
       * @param column the table renderer's column index
       * @return the cell renderer. Never <code>null</code>.
       */
      public TableCellRenderer getCellRenderer(int row, int column)
      {
         return mi_cellRenderer;
      }

      /**
       * displays an error message when user sets an empty name for the
       * property and does not allow completion of editing.
       * @param e the ChangeEvent captured. Never <code>null</code>.
       */
      public void editingStopped(ChangeEvent e)
      {
         TableCellEditor editor = getCellEditor();
         if (editor != null)
         {
            Object value = editor.getCellEditorValue();
            int editingCol = getEditingColumn();
            TableModel model = getModel();
            if(model instanceof PropertiesTableModel &&
               editingCol == COL_PROPS_NAME)
            {
               if(value == null || value.toString().trim().length() == 0)
               {
                  ErrorDialogs.showErrorMessage(
                     PSRelationshipEditorDialog.this,
                     getResourceString("emptyPropName"),
                     getResourceString("error"));
                     getEditorComponent().requestFocus();
                     return;
               }
            }
            super.editingStopped(e);
         }
     }

     private TableCellEditor   mi_cellEditor;
     private TableCellRenderer mi_cellRenderer;
   }

   /**
    * The button to add a new 'User' relationship,  initialized in
    * <code>createLeftPanel()</code> and never <code>null</code> after that.
    * Will be disabled if the selected tree node does not represent 'User' node.
    */
   private UTFixedButton m_addButton;

   /**
    * The button to delete 'User' relationship, initialized in
    * <code>createLeftPanel()</code> and never <code>null</code> after that.
    * Will be disabled if the selected tree node does not represent 'User'
    * relationship node.
    */
   private UTFixedButton m_deleteButton;

   /**
    * The tree model to represent relationship configuration, initialized in
    * <code>createLeftPanel()</code> and never <code>null</code> after that.
    * Gets modified as user creates/deletes 'User' relationships.
    */
   private DefaultTreeModel m_rsConfigTreeModel;

   /**
    * The tree to show relationship configuration, initialized in
    * <code>createLeftPanel()</code> and never <code>null</code> after that.
    * Gets modified as user creates/deletes 'User' relationships.
    */
   private JTree m_rsConfigTree;

   /**
    * A flag to indicate whether the current view data has an error or not.
    */
   private boolean m_isErrorChangeView = false;

   /**
    * The map of allowed property data types, with key being the type (<code>
    * Integer</code>) and value being <code>PropertyType</code> object that
    * encapsulates its display text. Initialized in the constructor and never
    * <code>null</code> or modified after that. Never empty.
    */
   private Map<Integer, PropertyType> m_propertyTypes =
         new HashMap<Integer, PropertyType>();

   /**
    * The list of effects (<code>IPSEffect</code>) available to associate with a
    * relationship, initialized in the constructor and never <code>null</code>
    * or modified after that. May be empty.
    */
   private List<IPSExtensionDef> m_effects = new ArrayList<IPSExtensionDef>();

   /**
    * The list of post-extensions (<code>IPSExtensionDef</code>) available to
    * associate with a relationship, initialized in the constructor and never
    * <code>null</code> or modified after that. May be empty.
    */
   private List<IPSExtensionDef> m_postExtensions =
         new ArrayList<IPSExtensionDef>();

   /**
    * The list of pre-extensions (<code>IPSExtensionDef</code>) available to
    * associate with a relationship, initialized in the constructor and never
    * <code>null</code> or modified after that. May be empty.
    */
   private List<IPSExtensionDef> m_preExtensions =
         new ArrayList<>();

   /**
    * The list of UDFs (<code>IPSUdfProcessor</code>) available to
    * associate with a relationship, initialized in the constructor and never
    * <code>null</code> or modified after that. May be empty.
    */
   private List<IPSExtensionDef> m_udfs = new ArrayList<>();

   /**
    * The system clone handler configuration that defines the process checks
    * that a relationship can have, initialized in the constructor and never
    * modified or <code>null</code> after that.
    */
   private PSCloneHandlerConfig m_chConfig;

   /**
    * The relationships configuration that needs to be edited in this dialog,
    * initialized in the constructor and modified as user changes. Never <code>
    * null</code> after initialization.
    */
   private PSRelationshipConfigSet m_rsConfigSet;

   /**
    * The current view for the right panel.
    * May be <code>null</code>.
    */
   private String m_currentView = null;

   /**
    * The constant to indicate that selected node or view represents a
    * 'Relationship'
    */
   static final String RELATIONSHIP = "relationship view";

   /**
    * The constant used as key to store the data object being edited in the
    * button editor action.
    */
   private static final String DATA_OBJECT = "dataObject";

   /**
    * The minimum number of rows to display for the table.
    */
   static final int MIN_ROWS = 20;

   /**
    * The constant to indicate 'Pre Extensions' view.
    */
   private static final int TYPE_PRE_EXTENSIONS = 0;

   /**
    * The constant to indicate 'Post Extensions' view.
    */
   private static final int TYPE_POST_EXTENSIONS = 1;


   /* The following represents indices of columns of properties table model */
   private static final int COL_PROPS_NAME = 0;
   private static final int COL_PROPS_VALUE = 1;
   private static final int COL_PROPS_LOCKED = 2;
   private static final int COL_PROPS_TYPE = 3;

   /**
    * The command name used for the conditionals button of the cloning table
    * in the cloning view. 
    */      
   static final String COMMAND_NAME_CLONING_CONFIG_CONDITIONALS = 
      "editCloningConfigConditionals";

   /**
    * The list of column names for properties table model in the order specified
    * by the column index constants. Initialized and set with data when this
    * class is loaded in <code>initDialog()</code> and never <code>null</code>
    * or modified after that.
    */
   private static Vector<String> ms_propColumns = new Vector<>();

   /**
    * The list of column names for cloning table model in the order specified
    * by the column index constants. Initialized and set with data when this
    * class is loaded in <code>initDialog()</code> and never <code>null</code>
    * or modified after that.
    */
   static Vector<String> ms_cloneColumns = new Vector<>();

   /**
    * The list of column names for cloning field override table
    * model in the order specified by the column index constants.
    * Initialized and set with data when this class is loaded
    * in <code>initDialog()</code> and never <code>null</code>
    * or modified after that.
    */
   static Vector<String> ms_cloneFieldOverridesColumns =
         new Vector<>();


   /**
    * The list of column names for extensions table model when it is used for
    * pre or post extensions in the order specified by the column index
    * constants. Initialized and set with data when this class is loaded in
    * <code>initDialog()</code> and never <code>null</code> or modified after
    * that.
    */
   private static Vector<String> ms_extColumns = new Vector<>();

   /**
    * The list of column names for extensions table model when it is used for
    * effects in the order specified by the column index constants. Initialized
    * and set with data when this class is loaded in <code>initDialog()</code>
    * and never <code>null</code> or modified after that.
    */
   static Vector<String> ms_effColumns = new Vector<>();

   /**
    * The icon used to represent that the cell has some conditions, initialized
    * in <code>initDialog()</code> when the class is loaded and never <code>
    * null</code> or modified after that.
    */
   static ImageIcon ms_condIcon;

   /**
    * The icon used to represent that the cell has no conditions, initialized
    * in <code>initDialog()</code> when the class is loaded and never <code>
    * null</code> or modified after that.
    */
   static ImageIcon ms_noCondIcon = null;

   /**
    * The icon used to represent the tree leaf, initialized
    * in <code>initDialog()</code> when the class is loaded and never <code>
    * null</code> or modified after that.
    */
   private static ImageIcon ms_treeLeafIcon = null;

   /**
    * Split pane, Initilized by the ctor and
    * never <code>null</code> after that.
    */
   private JSplitPane m_split;

   /**
    * Map that keeps all right panels. No order is preserved.
    */
   private Map<String, PSCfgPanel> m_rightPanels = new HashMap<>();

   /**
    * Parent frame that can be used for error dialogs,
    * may be <code>null</code>.
    */
   private static Frame m_parentFrame;

   /**
    * Contains an ordered set of CE system fields. Initialized by
    * the ctor and never <code>null</code> after that.
    */
   private Collection m_ceSystemFields;
   
   /**
    * A map with the default definitions of all required user properties. The
    * map key is the property value as <code>String</code>, the value is the
    * property definition as <code>PSProperty</code>.
    */
   private static final Map<String, PSProperty> ms_requiredUserProps =
         new HashMap<>();
   static
   {
      ms_requiredUserProps.put(IPSHtmlParameters.SYS_SLOTID,
         new PSProperty(IPSHtmlParameters.SYS_SLOTID,
            PSProperty.TYPE_STRING, null, false, "The slot used."));
      ms_requiredUserProps.put(IPSHtmlParameters.SYS_SORTRANK,
         new PSProperty(IPSHtmlParameters.SYS_SORTRANK, 
            PSProperty.TYPE_STRING, "1", false, "The sorting rank."));
      ms_requiredUserProps.put(IPSHtmlParameters.SYS_VARIANTID,
         new PSProperty(IPSHtmlParameters.SYS_VARIANTID,
            PSProperty.TYPE_STRING, null, false, "The variant used."));
   }
}
