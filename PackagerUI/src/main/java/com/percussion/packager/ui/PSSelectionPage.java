/******************************************************************************
 *
 * [ PSSelectionPage.java ]
 * 
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.packager.ui;

import com.percussion.deployer.client.PSDeploymentManager;
import com.percussion.error.PSDeployException;
import com.percussion.packager.ui.data.PSElementFilter;
import com.percussion.packager.ui.data.PSElementNode;
import com.percussion.packager.ui.data.PSPackageDescriptorMetaInfo;
import com.percussion.packager.ui.managers.PSServerConnectionManager;
import com.percussion.packager.ui.model.PSPackagerClientModel;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

/**
 * @author erikserating
 *
 */
public class PSSelectionPage extends JPanel implements IPSPage,
      DocumentListener
{
   public PSSelectionPage()
   {
      init();
   }

   private void init()
   {
      MigLayout layout = new MigLayout("fill", "[]", "[]");
      setLayout(layout);
      add(getMainPanel(), "grow");
   }

   public JPanel getMainPanel()
   {
      MigLayout layout = new MigLayout("fill", "[][]", "[top][top][top]");

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);

      m_titleLabel = new JLabel(getResourceString("title"));
      mainPanel.add(m_titleLabel, "wrap 1, span 2, growprio 0, gapy 0px 20px");

      MigLayout leftLayout = new MigLayout("fill", "[]", "[top]0[top]");
      JPanel leftPanel = new JPanel();
      leftPanel.setLayout(leftLayout);
      leftPanel.add(getBottomLeftPanel(), "grow");
      mainPanel.add(leftPanel, "grow 50");

      MigLayout rightLayout = new MigLayout("fill", "[]", "[top]0[top]");
      JPanel rightPanel = new JPanel();
      rightPanel.setLayout(rightLayout);
      rightPanel.add(getTopRightPanel(), "grow, wrap 1");
      rightPanel.add(getBottomRightPanel());
      mainPanel.add(rightPanel, "grow 50, wrap 1");

      return mainPanel;
   }

   /**
    * Top Left Panel
    */
   public JPanel getTopLeftPanel()
   {
      MigLayout layout = new MigLayout("fill", "[][]", "[top][][]");

      JPanel mainPanel = new JPanel();
      mainPanel.setBorder(BorderFactory.createEtchedBorder());
      mainPanel.setLayout(layout);

      return mainPanel;
   }

   /**
    * Bottom Left Panel
    * 
    * @return the panel, never <code>null</code>
    */
   public JPanel getBottomLeftPanel()
   {
      MigLayout layout = new MigLayout("wrap 4", "[][][][]",
            "[top][top][top]5[top]20[top]");

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);

      JLabel filterLabel = new JLabel(getResourceString("label.designelem"));
      mainPanel.add(filterLabel, "wrap 1, span 4");

      m_filterTextField = new JTextField();
      m_filterTextField.getDocument().addDocumentListener(
            new DocumentListener()
      {
         public void changedUpdate(@SuppressWarnings("unused")
               DocumentEvent arg0)
         {

         }

         public void insertUpdate(@SuppressWarnings("unused")
               DocumentEvent arg0)
         {
            loadTree();
            m_clearButton.setEnabled(StringUtils
                  .isNotBlank(m_filterTextField.getText()));
            m_clearButton.setFocusable(true);
         }

         public void removeUpdate(@SuppressWarnings("unused")
               DocumentEvent arg0)
         {
            loadTree();
            m_clearButton.setEnabled(StringUtils
                  .isNotBlank(m_filterTextField.getText()));
            m_clearButton.setFocusable(true);
         }

      });

      //Define button dimension
      Dimension dim = new Dimension(50, 20);

      m_clearButton = new JButton(getResourceString("button.clearfilter"));
      m_clearButton.setMnemonic((int) getResourceString("button.clearfilter.m").charAt(0));
      m_clearButton.setEnabled(false);
      m_clearButton.setFocusable(false);
      m_clearButton.setMinimumSize(dim);
      m_clearButton.setMaximumSize(dim);
      m_clearButton.setPreferredSize(dim);

      m_clearButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            m_filterTextField.setText("");
            loadTree();
            m_clearButton.setEnabled(false);
            m_clearButton.setFocusable(false);
         }
      });

      //Add to Panel
      mainPanel.add(m_filterTextField, "growx, spanx 4, split 2");
      mainPanel.add(m_clearButton, "growx, wrap 1, right");

      m_root = new DefaultMutableTreeNode();

      m_treeModel = new DefaultTreeModel(m_root);
      m_tree = new JTree(m_treeModel);
      m_tree.setRootVisible(false);
      m_tree.setRowHeight(18);
      m_tree.setShowsRootHandles(true);

      PSCheckBoxNodeRenderer renderer = new PSCheckBoxNodeRenderer();
      m_tree.setCellRenderer(renderer);
      PSCheckBoxNodeEditor editor = new PSCheckBoxNodeEditor(m_tree);
      editor.addCellEditorListener(new CellEditorListener()
      {
         public void editingCanceled(@SuppressWarnings("unused")
               ChangeEvent arg0)
         {
            // No op
         }

         public void editingStopped(ChangeEvent event)
         {
            /*
             * This ugly code is needed to remove the checkbox when in 
             * selected only mode and a checkbox is unselected. 
             * We want to do this without reloading the tree so we
             * don't lose expansion and scroll state.
             */
            boolean selectedOnly = m_selectedOnlyCheckBox.isSelected();
            if (selectedOnly)
            {
               PSCheckBoxNodeEditor editor2 = (PSCheckBoxNodeEditor) event
                     .getSource();
               DefaultTreeModel treeModel = (DefaultTreeModel) m_tree
                     .getModel();
               TreeNode treeNode = editor2.getLastNode();
               TreeNode root = treeNode.getParent();

               if (root == null)
                  return;
               if (root.getChildCount() == 1)
                  treeNode = root;

               try
               {
                  treeModel.removeNodeFromParent((MutableTreeNode) treeNode);
               }
               catch (Exception ignore)
               {
               }

            }

         }

      });
      m_tree.setCellEditor(editor);
      m_tree.setEditable(true);
      m_tree.addKeyListener(new KeyAdapter()
      {

         /*
          * (non-Javadoc)
          * 
          * @see java.awt.event.KeyAdapter#keyPressed(java.awt.event.KeyEvent)
          */
         @Override
         public void keyPressed(KeyEvent e)
         {
            JTree source = (JTree) e.getSource();
            int code = e.getKeyCode();
            if (code == SPACE_KEY_CODE)
            {
               JCheckBox checkbox = null;
               PSCheckBoxNode cnode = null;
               TreePath path = source.getSelectionPath();

               if (path != null)
               {
                  Object node = path.getLastPathComponent();
                  if ((node != null)
                           && (node instanceof DefaultMutableTreeNode))
                  {
                     DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) node;
                     cnode = (PSCheckBoxNode) treeNode.getUserObject();

                  }
                  checkbox = ((PSCheckBoxNodeRenderer) source.getCellRenderer())
                           .getLeafRenderer();

                  if (checkbox != null)
                  {
                     if (checkbox.isEnabled())
                     {
                        cnode.setSelected(!cnode.isSelected());
                        repaint();
                     }
                  }
               }
            }

         }

      });

      JScrollPane scrollPane = new JScrollPane(m_tree,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      Dimension buttonSize = new Dimension(100, 20);

      //Select All Button
      JButton designElemSlctAll = new JButton(
            getResourceString("button.selectall"));
      designElemSlctAll.setMnemonic((int) getResourceString("button.selectall.m").charAt(0));
      designElemSlctAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            selectAll(m_tree, true);
         }
      });
      designElemSlctAll.setPreferredSize(buttonSize);

      //Clear All Button
      JButton designElemClrAll = new JButton(
            getResourceString("button.clearall"));
      designElemClrAll.setMnemonic(
            (int) getResourceString("button.clearall.m").charAt(0));
      designElemClrAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            selectAll(m_tree, false);
         }
      });
      designElemClrAll.setPreferredSize(buttonSize);

      //Expand All Button
      JButton designElemExpAll = new JButton(
            getResourceString("button.expandall"));
      designElemExpAll.setMnemonic(
            (int) getResourceString("button.expandall.m").charAt(0));
      designElemExpAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            expandAll(m_tree, true);
         }
      });
      designElemExpAll.setPreferredSize(buttonSize);

      //Collapse All Button
      JButton designElemAll = new JButton(
            getResourceString("button.collapseall"));
      designElemAll.setMnemonic(
            (int) getResourceString("button.collapseall.m").charAt(0));
      designElemAll.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            expandAll(m_tree, false);
         }
      });
      designElemAll.setPreferredSize(buttonSize);

      //Add to Panel    
      mainPanel.add(scrollPane, "gapy 5px 0px, span 4, grow");
      mainPanel.add(designElemSlctAll, "sg 1");
      mainPanel.add(designElemClrAll, "sg 1");
      mainPanel.add(designElemExpAll, "sg 1");
      mainPanel.add(designElemAll, "sg 1");
      
      m_selectedOnlyCheckBox = new JCheckBox(
            getResourceString("label.selected"));
      m_selectedOnlyCheckBox.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent event)
         {
            loadTree();
         }

      });

      mainPanel.add(m_selectedOnlyCheckBox,
            "span 4, left, gapx 10px 0px, gapy 0px 0px");

      return mainPanel;
   }

   /**
    * Top Right Panel
    * 
    * @return the panel, never <code>null</code>
    */
   public JPanel getTopRightPanel()
   {
      MigLayout layout = new MigLayout("fill", "[][]", "[top][top]5[top]");

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);

      //Add resource button
      JLabel fileResLabel = new JLabel(getResourceString("label.fileres"));

      JButton fileResAddButton = new JButton(getResourceString("button.add"));
      fileResAddButton.setMnemonic(
            (int) getResourceString("button.add.m").charAt(0));
      fileResAddButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            PSFileResourcesDialog dlg = new PSFileResourcesDialog();
            dlg.setModal(true);
            dlg.setVisible(true);
            loadResList();
         }
      });

      m_resList.setModel(m_defModel);
      // show list
      JScrollPane fileResList = new JScrollPane(m_resList,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      //Remove resource button
      JButton fileResRemoveButton = new JButton(
            getResourceString("button.remove"));
      fileResRemoveButton.setMnemonic(
            (int) getResourceString("button.remove.m").charAt(0));
      fileResRemoveButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            PSPackagerClientModel model = PSPackagerClient.getFrame()
                  .getModel();

            ArrayList<String> userDeps = (ArrayList<String>) model
                  .getFileResources();
            Object selList[] = m_resList.getSelectedValues();

            for (Object selItem : selList)
            {
               userDeps.remove(selItem.toString());
            }
            model.setAsDirty();
            loadResList();
         }
      });

      mainPanel.add(fileResLabel, "wrap 1, span 2");
      mainPanel.add(fileResList, "grow, wrap 1, span 2");
      mainPanel.add(fileResAddButton, "sg 1, right, gapy 5px");
      mainPanel.add(fileResRemoveButton, "sg 1, right, gapy 5px");

      return mainPanel;
   }

   private void loadResList()
   {
      PSPackagerClientModel model = PSPackagerClient.getFrame().getModel();
      ArrayList<String> paths = (ArrayList<String>) model.getFileResources();
      if (paths == null)
      {
         paths = new ArrayList<String>();
      }
      m_defModel.clear();
      Collections.sort(paths, ms_sortComp);
      for (String path : paths)
      {
         m_defModel.addElement(path);
      }
      m_resList.setModel(m_defModel);
   }

   /**
    * Bottom Right Panel
    * 
    * @return the panel, never <code>null</code>
    */
   public JPanel getBottomRightPanel()
   {
      MigLayout layout = new MigLayout("wrap 2", "[]", "[][][][][]");

      JPanel mainPanel = new JPanel();
      mainPanel.setLayout(layout);
      
      m_fc = new JFileChooser();
      m_fc.setApproveButtonText(getResourceString("button.fcSelect"));
      m_fc.setDialogTitle(getResourceString("title.fc"));
      disableNewFolderButton(m_fc);

      //Define browse button dimension
      Dimension dim = new Dimension(25, 20);

      // Config Def
      JLabel configDefLabel = new JLabel(getResourceString("label.configdef"));
      m_configDefField = new JTextField(50);

      JButton configDefButton = new JButton(
            getResourceString("button.getfile"));

      configDefButton.setMaximumSize(dim);
      configDefButton.setPreferredSize(dim);
      configDefButton.setMinimumSize(dim);

      configDefButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            File f = new File("");
            File[] files = {f};
            m_fc.setSelectedFile(f); 
            m_fc.setSelectedFiles(files);
            m_fc.setFileFilter(new FileFilter()
            {
               @Override
               public boolean accept(File f)
               {
                  PSPackagerClientModel model = PSPackagerClient.getFrame().getModel();
                  PSPackageDescriptorMetaInfo info = model.getDescriptorMetaInfo();
                  return f.isDirectory() ||
                     f.getName().equalsIgnoreCase(info.getName() + "_configDef.xml");
               }

               @Override
               public String getDescription()
               {               
                  return "Config Def File";
               }
            
            });
            int returnVal = m_fc.showOpenDialog(PSSelectionPage.this);            
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
               File file = m_fc.getSelectedFile();
               m_configDefField.setText(file.getAbsolutePath());
            }
         }
      });

      //Default Config
      JLabel localConfigLabel = new JLabel(
            getResourceString("label.defconfig"));
      m_localConfigField = new JTextField(50);

      JButton localConfigButton = new JButton(
            getResourceString("button.getfile"));
      localConfigButton.setMaximumSize(dim);
      localConfigButton.setPreferredSize(dim);
      localConfigButton.setMinimumSize(dim);
      localConfigButton.addActionListener(new ActionListener()
      {
         public void actionPerformed(@SuppressWarnings("unused")
         ActionEvent e)
         {
            File f = new File("");
            File[] files = {f};
            m_fc.setSelectedFile(f); 
            m_fc.setSelectedFiles(files);  
            m_fc.setFileFilter(new FileFilter()
            {
               @Override
               public boolean accept(File f)
               {
                  PSPackagerClientModel model = PSPackagerClient.getFrame().getModel();
                  PSPackageDescriptorMetaInfo info = model.getDescriptorMetaInfo();
                  return f.isDirectory() ||
                     f.getName().equalsIgnoreCase(info.getName() + "_defaultConfig.xml");
               }

               @Override
               public String getDescription()
               {               
                  return "Default Config File";
               }
            
            });
            int returnVal = m_fc.showSaveDialog(PSSelectionPage.this);
            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
               File file = m_fc.getSelectedFile();
               m_localConfigField.setText(file.getAbsolutePath());
            }
         }
      });

      mainPanel.add(configDefLabel, "wrap");
      mainPanel.add(m_configDefField);
      mainPanel.add(configDefButton);
      mainPanel.add(localConfigLabel, "wrap");
      mainPanel.add(m_localConfigField);
      mainPanel.add(localConfigButton);
      return mainPanel;
   }

   /**
    * Gets all Categories and Elements from the model.  If a filter is set in the 
    * Model it will use that to filter results
    *
    * @return root to tree
    */
   private DefaultMutableTreeNode getFilteredSelectionTreeNodes()
   {
      final PSPackagerClientModel model = PSPackagerClient.getFrame()
            .getModel();
      List<PSElementNode> nodes = model.getFilteredElementNodes();
      DefaultMutableTreeNode root = new DefaultMutableTreeNode();
      for (PSElementNode node : nodes)
      {
         DefaultMutableTreeNode cat = new DefaultMutableTreeNode(node
               .getName(), true);
         Iterator<PSElementNode> children = node.getChildren();
         while (children.hasNext())
         {
            PSElementNode child = children.next();
            PSCheckBoxNode checkbox = new PSCheckBoxNode(child);
            checkbox.addChangeListener(new ChangeListener()
            {
               public void stateChanged(@SuppressWarnings("unused")
                     ChangeEvent e)
               {
                  model.elementSelectionChange();
                  model.setAsDirty();
               }
            });
            cat.add(new DefaultMutableTreeNode(checkbox));
         }
         root.add(cat);
      }
      return root;
   }

   /**
    * If selected is true, expands all nodes in the tree.
    * Otherwise, collapses all nodes in the tree.
    *
    * @param tree traverse nodes
    * @param selected set to all nodes
    */
   @SuppressWarnings("unchecked") //Enumeration
   public void selectAll(JTree tree, boolean selected)
   {

      DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel()
            .getRoot();
      TreePath tpRoot = new TreePath(root);
      for (Enumeration e = root.children(); e.hasMoreElements();)
      {
         DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
         TreePath path = tpRoot.pathByAddingChild(n);
         selectAll(tree, path, selected);
      }
      if(!selected)
         loadTree();
   }

   /**
    * If selected is true, expands all nodes in the tree.
    * Otherwise, collapses all nodes in the tree.
    *
    * @param tree traverse nodes
    * @param parent path to parent
    * @param selected set to all nodes
    */
   @SuppressWarnings("unchecked") //Enumeration
   private void selectAll(JTree tree, TreePath parent, boolean selected)
   {
      // Traverse children
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent
            .getLastPathComponent();
      if (node.getChildCount() >= 0)
      {
         for (Enumeration e = node.children(); e.hasMoreElements();)
         {
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) e
                  .nextElement();
            PSCheckBoxNode child = (PSCheckBoxNode) n.getUserObject();
            child.setSelected(selected);
            ((DefaultTreeModel) tree.getModel()).nodeChanged(n);
         }
      }
   }

   /**
    * If expand is true, expands all nodes in the tree.
    * Otherwise, collapses all nodes in the tree.
    *
    * @param tree traverse nodes
    * @param expand set to all nodes
    */
   @SuppressWarnings("unchecked") //Enumeration
   public void expandAll(JTree tree, boolean expand)
   {

      TreeNode root = (TreeNode) tree.getModel().getRoot();
      TreePath tpRoot = new TreePath(root);
      for (Enumeration e = root.children(); e.hasMoreElements();)
      {
         TreeNode n = (TreeNode) e.nextElement();
         TreePath path = tpRoot.pathByAddingChild(n);
         expandAll(tree, path, expand);
      }
   }

   /**
    * If expand is true, expands all nodes in the tree.
    * Otherwise, collapses all nodes in the tree.
    *
    * @param tree traverse nodes
    * @param parent path to parent
    * @param expand set to all nodes
    */
   @SuppressWarnings("unchecked") //Enumeration
   private void expandAll(JTree tree, TreePath parent, boolean expand)
   {
      // Traverse children
      TreeNode node = (TreeNode) parent.getLastPathComponent();
      if (node.getChildCount() >= 0)
      {
         for (Enumeration e = node.children(); e.hasMoreElements();)
         {
            TreeNode n = (TreeNode) e.nextElement();
            TreePath path = parent.pathByAddingChild(n);
            expandAll(tree, path, expand);
         }
      }

      // Expansion or collapse must be done bottom-up
      if (expand)
      {
         tree.expandPath(parent);
      }
      else
      {
         tree.collapsePath(parent);
      }
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#load(java.lang.Object)
    */
   public void load(PSPackagerClientModel model)
   {
      loadTree();
      loadResList();

      //load config files
      turnOnListener(false);
      PSPackageDescriptorMetaInfo info = model.getDescriptorMetaInfo();
      m_titleLabel.setText(getResourceString("title") + " [" + info.getName()
            + "]");
      String localConfig = info.getLocalConfigPath();
      String configDef = info.getConfigDefPath();

      if (StringUtils.isEmpty(localConfig))
      {
         m_localConfigField.setText("");
      }
      else
      {
         m_localConfigField.setText(localConfig);
      }

      if (StringUtils.isEmpty(configDef))
      {
         m_configDefField.setText("");
      }
      else
      {
         m_configDefField.setText(configDef);
      }
      turnOnListener(true);
   }

   /**
    * Loads Tree to show all Elements on Server 
    */
   private void loadTree()
   {
      boolean selectedOnly = m_selectedOnlyCheckBox.isSelected();
      String expr = m_filterTextField.getText();
      PSElementFilter filter = null;
      if (StringUtils.isNotBlank(expr) || selectedOnly)
      {
         String lastChar = StringUtils.isBlank(expr) ? "" : expr
               .substring(expr.length() - 1);
         String modExpr = lastChar.equals("*") || lastChar.equals("?") ? expr
               : expr + "*";
         filter = new PSElementFilter(modExpr, false, selectedOnly, false);
      }
      PSPackagerClientModel model = PSPackagerClient.getFrame().getModel();
      model.setElementFilter(filter);
      m_root = getFilteredSelectionTreeNodes();
      m_treeModel.setRoot(m_root);
      m_treeModel.reload();
      if (filter != null)
      {
         expandAll(m_tree, true);
      }
   }

   /**
    * Clear the expansion state of the selection tree.
    */
   public void clearTreeExpansionState()
   {
      expandAll(m_tree, false);
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#update(java.lang.Object)
    */
   public void update(PSPackagerClientModel model)
   {

      PSPackageDescriptorMetaInfo info = model.getDescriptorMetaInfo();
      if (info != null)
      {
         // Config Files
         info.setConfigDefPath(m_configDefField.getText());
         info.setLocalConfigPath(m_localConfigField.getText());
      }

   }
   
   /**
    * Set the state of the selected only checkbox.
    * @param selectedOnly
    */
   public void setSelectedOnly(boolean selectedOnly)
   {
      m_selectedOnlyCheckBox.setSelected(selectedOnly);
   }
   
   /**
    * Clear the filter text field.
    */
   public void clearFilterField()
   {
      m_filterTextField.setText("");
   }

   /* (non-Javadoc)
    * @see com.percussion.packager.ui.IPSPage#validateData()
    */
   public List<String> validateData()
   {
      List<String> errorList = new ArrayList<String>();

      try
      {
         errorList = validateConfigs();
      }
      catch (PSDeployException e)
      {
         errorList.add(e.getLocalizedMessage());
      }

      return errorList;
   }

   /**
    * Validate the configuration files specified.
    * We validate that either both configs or no configs
    * have been specified. We validate the local config against
    * the localConfig.xsd.
    * @return <code>true</code> if validation passes.

    */
   private List<String> validateConfigs() throws PSDeployException
   {
      ArrayList<String> errorList = new ArrayList<String>();
      //Both configs must be specified or none
      boolean configDefSpecified = StringUtils.isNotBlank(m_configDefField
            .getText());
      boolean localDefSpecified = StringUtils.isNotBlank(m_localConfigField
            .getText());
      if (configDefSpecified ^ localDefSpecified)
      {
         Object[] args = {
               StringUtils.chomp(getResourceString("label.defconfig"), ":"),
               StringUtils.chomp(getResourceString("label.configdef"), ":") };
         if (configDefSpecified)
         {
            ArrayUtils.reverse(args);
         }

         errorList.add(MessageFormat.format(
               getResourceString("error.bothConfigsMustBeSpecified"), args));

         return errorList;
      }

      //Validate default config against schema
      if (StringUtils.isNotBlank(m_localConfigField.getText()))
      {
         File localConfigFile = new File(m_localConfigField.getText());
         PSServerConnectionManager cm = PSServerConnectionManager
               .getInstance();
         PSDeploymentManager deploymentMgr = cm.getDeploymentManager();
         List<String> errors = deploymentMgr
               .validateLocalConfigFile(localConfigFile);
         if (errors != null && !errors.isEmpty())
         {
            StringBuilder sb = new StringBuilder();
            for (String error : errors)
            {
               sb.append("\n");
               sb.append(error);
            }

            Object[] args = {
                  StringUtils.chomp(getResourceString("label.defconfig"), ":"),
                  localConfigFile.getName(),
                  sb.toString() };
            errorList.add(MessageFormat.format(
                  getResourceString("error.invalidConfigFile"), args));
         }
      }
      return errorList;
   }
   
   /**
    * Clear the the selected only checkbox.
    */        
   void clearSelectedOnlyCheckBox()
   {
      m_selectedOnlyCheckBox.setSelected(false);
   }

   /**
    * Get resource text
    * 
    * @param key
    * @return text
    */
   private String getResourceString(String key)
   {
      return PSResourceUtils.getResourceString(this.getClass(), key);
   }

   /**
    * Removes the new folder button on JFileChooser
    */
   public void disableNewFolderButton(Container c)
   {
      int len = c.getComponentCount();
      for (int i = 0; i < len; i++)
      {
         Component comp = c.getComponent(i);
         if (comp instanceof JButton)
         {
            JButton b = (JButton) comp;
            Icon icon = b.getIcon();
            if (icon != null
                  && icon == UIManager.getIcon("FileChooser.newFolderIcon"))
               b.setVisible(false);
         }
         else if (comp instanceof Container)
         {
            disableNewFolderButton((Container) comp);
         }
      }
   }

   /**
    * Set model to a dirty state.
    */
   private void dirtyModel()
   {
      PSPackagerClientModel model = PSPackagerClient.getFrame().getModel();
      model.setAsDirty();
   }

   public void changedUpdate(@SuppressWarnings("unused")
   DocumentEvent e)
   {
      dirtyModel();
   }

   public void insertUpdate(@SuppressWarnings("unused")
   DocumentEvent e)
   {
      dirtyModel();
   }

   public void removeUpdate(@SuppressWarnings("unused")
   DocumentEvent e)
   {
      dirtyModel();
   }

   /**
    * Turn on Listener
    */
   private void turnOnListener(boolean on)
   {

      if (on)
      {
         m_localConfigField.getDocument().addDocumentListener(this);
         m_configDefField.getDocument().addDocumentListener(this);
      }
      else
      {
         m_localConfigField.getDocument().removeDocumentListener(this);
         m_configDefField.getDocument().removeDocumentListener(this);
      }

   }

   /**
    * Sets focus on Filter Text Field
    */
   public void focusAndSelectFilterTextField()
   {
      m_filterTextField.selectAll();
      m_filterTextField.requestFocus();
   }
   
   /**
    * Title label.
    */
   private JLabel m_titleLabel;

   /**
    * Tree for Elements to select
    */
   private JTree m_tree;

   /**
    * Tree Model
    */
   private DefaultTreeModel m_treeModel;

   /**
    * Tree Root node
    */
   private DefaultMutableTreeNode m_root;

   /**
    * Check box for search on selected only
    */
   private JCheckBox m_selectedOnlyCheckBox;

   /**
    * Config Def
    */
   private JTextField m_configDefField;

   /**
    * Default Config
    */
   private JTextField m_localConfigField;

   /**
    * Filter
    */
   private JTextField m_filterTextField;

   /**
    * File chooser
    */
   private JFileChooser m_fc;

   /**
    * Comparator for PSCatalogResult.
    * Compares based on DisplayText and ignores case.
    */
   private static final Comparator<String> ms_sortComp = new Comparator<String>()
   {
      public int compare(String s1, String s2)
      {
         return (s1.compareToIgnoreCase(s2));
      }
   };

   /**
    * Default list model
    */
   DefaultListModel m_defModel = new DefaultListModel();

   /**
    * Resource List
    */
   JList m_resList = new JList();

   private JButton m_clearButton;
   
   /**
    * Constant for the space key code.
    */
   private static int SPACE_KEY_CODE = 32;

}
