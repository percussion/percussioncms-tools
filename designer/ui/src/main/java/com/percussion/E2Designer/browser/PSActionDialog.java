/*[ PSActionDialog ]*******************************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.guitools.PSDialog;
import com.percussion.guitools.UTStandardCommandPanel;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;


public class PSActionDialog extends PSDialog implements
   TreeSelectionListener
{
   /**
    * Constructs the display format dialog.
    */
   public PSActionDialog(Frame parent, List list,PSComponentProcessorProxy proxy)
   {
      super(parent);
      m_actionDbCollect = (PSDbComponentCollection)list.get(0);
      m_tabbedPane = new PSActionTabbedPanel(list);
      m_proxy = proxy;
      init();
   }

   /**
    * Initializes the dialog.
    */
   private void init()
   {
      if (null == ms_res)
         ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
            Locale.getDefault());
      setTitle(ms_res.getString("dlg.title"));
      JPanel mainPane = new JPanel();
      mainPane.setLayout(new BorderLayout());

      JPanel cmdPane = new JPanel();
      cmdPane.setLayout(new BoxLayout(cmdPane, BoxLayout.X_AXIS));

      UTStandardCommandPanel cmdPanel = new UTStandardCommandPanel(this,
         SwingConstants.HORIZONTAL, true, true);

      cmdPane.add(Box.createHorizontalGlue());
      JPanel pane = new JPanel();
      pane.setPreferredSize(new Dimension(450, 0));
      cmdPane.add(pane);
      cmdPane.add(cmdPanel);
      PSDbComponentCollection c = null;
      m_treePanel = new PSActionTreePanel(m_actionDbCollect);
      m_treePanel.addTreeSelectionListener(this);

      m_rootNode = (DefaultMutableTreeNode)m_treePanel.getModel().getRoot();
      PSTreeObject root = (PSTreeObject)m_rootNode.getUserObject();
      JPanel defaultPane = root.getUIObject();

      m_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      m_split.setLeftComponent(m_treePanel);
      m_split.setRightComponent(defaultPane);
      defaultPane.setPreferredSize(new Dimension(300, 400));
      m_treePanel.setPreferredSize(new Dimension(350, 400));
      m_split.setPreferredSize(new Dimension(700, 500));

      mainPane.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      cmdPanel.setBorder(BorderFactory.createEmptyBorder( 5, 0, 0, 0 ));
      mainPane.add(m_split, BorderLayout.CENTER);
      mainPane.add(cmdPane, BorderLayout.SOUTH);
      getContentPane().add(mainPane);
      pack();
      center();
      setResizable(true);
   }

   /**
    * Implements TreeSelectionListener interface.
    *
    * @param e never <code>null</code>, called by the swing event handling
    * mechanism.
    */
   public void valueChanged(TreeSelectionEvent e)
   {
      JTree tree = (JTree)e.getSource();
      DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode)
         tree.getLastSelectedPathComponent();

      m_lastSelectedNode = nextNode;

      PSAction prevAction = null;
      PSAction nextAction = null;

      if (nextNode == null)
         return;

      PSTreeObject nextNodeObj = (PSTreeObject)nextNode.getUserObject();
      TreePath prevPath = e.getOldLeadSelectionPath();
      DefaultMutableTreeNode prevNode = null;
      if (nextNode != m_rootNode)
         nextAction = (PSAction)nextNodeObj.getDataObject();

      if (prevPath != null)
      {
         prevNode = (DefaultMutableTreeNode)prevPath.getLastPathComponent();
         if (prevNode != m_rootNode)
         {
            PSTreeObject prevObj = (PSTreeObject)prevNode.getUserObject();
            prevAction = (PSAction)prevObj.getDataObject();
            if(ms_deadLockError == 0)
            {
               //save the data before loading
               if(!m_tabbedPane.update(prevAction, false))
               {
                  if (!prevAction.getName().equalsIgnoreCase(nextAction.getName()))
                  {
                     ms_deadLockError++;
                     tree.setSelectionPath(prevPath);
                  }
                  //load the data
                  m_tabbedPane.update(prevAction, true);
                  m_split.setRightComponent(m_tabbedPane);
                  return;
               }
               else
                  m_tabbedPane.update(prevAction, false);
            }
            else
               ms_deadLockError = 0;

            //don't reload if label didn't change
            if (!prevObj.toString().equalsIgnoreCase(prevAction.getLabel()))
            {
               prevObj.setName(prevAction.getLabel());
               ((DefaultTreeModel)tree.getModel()).reload();
               tree.setSelectionPath(e.getNewLeadSelectionPath());
            }
         }
      }
      if (nextNode == m_rootNode)
         m_split.setRightComponent(nextNodeObj.getUIObject());
      else
      {
         m_tabbedPane.update(nextAction, true);
         m_split.setRightComponent(m_tabbedPane);
      }
      addDefaultKeyListeners(m_split.getRightComponent());
   }

   /**
    * Saves the currently selected node and reloads the tree if necessary.
    *
    * @return <code>true</code> if there were no validation errors during the
    * save, <code>false</code> otherwise.
    */
   private boolean  save()
   {
      boolean saveOk = true;
      //save the last selected node when hitting OK or Apply btn
      JTree tree = m_treePanel.getTree();
      DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode)
         tree.getLastSelectedPathComponent();

      //on a refresh, the tree node might get unselected
      if (nextNode == null)
         nextNode = m_lastSelectedNode;

      if (nextNode != null && nextNode != m_rootNode)
      {
         PSTreeObject nodeObj = (PSTreeObject)nextNode.getUserObject();
         PSAction action = (PSAction)nodeObj.getDataObject();
         m_lastSavedAction = action;
         saveOk = m_tabbedPane.update(action, false);
         //don't reload if label didn't change
         if (!nodeObj.toString().equalsIgnoreCase(action.getLabel()))
         {
            nodeObj.setName(action.getLabel());
            ((DefaultTreeModel)tree.getModel()).reload();
         }
      }
      return saveOk;
   }
   
   /**
    * Selects a node in the tree based on it's internal action name, does 
    * nothing if either parameter is <code>null</code>.
    * @param tree the tree the node belongs to, may be <code>null</code>.
    * @param node the node to be selected, may be <code>null</code>.
    */
   private void selectTreeNode(JTree tree, DefaultMutableTreeNode node)
   {
      if(tree == null || node == null)
         return;
      PSTreeObject nodeObj = (PSTreeObject)node.getUserObject();
      PSAction action = (PSAction)nodeObj.getDataObject(); 
      String internalName = "";
      if(action != null)
      {
         internalName = action.getName() != null ? action.getName() : "";
      }
      else
      {
         return;
      }
      
      
      for(int i = 0; i < tree.getRowCount(); i++)
      {
         TreePath path = tree.getPathForRow(i);
         DefaultMutableTreeNode currentNode = 
           (DefaultMutableTreeNode)path.getLastPathComponent();
         nodeObj = (PSTreeObject)currentNode.getUserObject();
         action = (PSAction)nodeObj.getDataObject();
         if(action != null && internalName.equals(action.getName()))
         {
            tree.setSelectionRow(i);
            return;
         }
      }
   }

   /**
    * Overriding the same method in {@link PSDialog}.
    */
   public void onApply()
   {
      try
      {
         
         JTree tree = m_treePanel.getTree();
         DefaultMutableTreeNode thisNode = (DefaultMutableTreeNode)
            tree.getLastSelectedPathComponent();
            
         m_b = save();
         if (m_b)
         {
            m_proxy.save(new IPSDbComponent[] {m_actionDbCollect});
            m_treePanel.refreshTree();
            m_tabbedPane.refreshInternalName(m_lastSavedAction);
            selectTreeNode(tree, thisNode);

         }
      }
      catch(Exception e)
      {
         //todo: proper error msg
         e.printStackTrace();
      }
   }

   /**
    * Overriding the same method in {@link PSDialog}.
    */
   public void onOk()
   {
     onApply();
     if (m_b)
        super.onOk();
   }

   /**
    * Appends a string that corresponds to the active tab type.
    * @param the helpId that acts as the root of a unique help key. May be
    * <code>null</code>.
    * @return the unique help key string
    */
   protected String subclassHelpId( String helpId )
   {
      // NOTE: The order of strings in this array must match the actual tab order
      String[] tabs = {"General","Usage","Command","Visibility","Properties"};
      int selected = m_tabbedPane.getSelectedTabIndex();
      DefaultMutableTreeNode currentTreeNode = m_treePanel.getSelectedTreeNode();
      if(null != helpId && null != currentTreeNode && !currentTreeNode.isRoot())
         helpId += "_"+tabs[selected];

      return helpId ;

   }

   //test code
   public static void main(String[] arg)
   {
     // DisplayFormatDialog d = new DisplayFormatDialog();
     // d.setVisible(true);
   }

   private boolean m_b;

   private PSActionTabbedPanel m_tabbedPane;

   private PSComponentProcessorProxy m_proxy;

   private PSDbComponentCollection m_actionDbCollect;

   private JTabbedPane m_tabPane;

   //prevents deadlock situation if switched to and switched from node both have errors
   private static int ms_deadLockError = 0;

   /**
    * Panel containing display format tree. Initialized in the {@link #init()},
    * never <code>null</code> or modified after that.
    */
   private PSActionTreePanel m_treePanel;

   /**
    * Split pane containing the left and right panel. The left panel shows the
    * tree and the right panel displays node sensitive panel. Initialized in the
    * {@link #init()}, never <code>null</code> or modified after that.
    */
   private JSplitPane m_split;

   /**
    * The last saved action. Modified in {@link #save()}.May be <code>null</code>
    * after that.
    */
   private PSAction m_lastSavedAction;

   /**
    * In certain cases the tree node might get unselected on a tree refresh
    * we set this object on every tree change event, so that later on we
    * can get the last selected node even if the tree says that nothing is
    * selected. May be <code>null</code>. Modified in {@link #valueChanged()}.
   */
   private DefaultMutableTreeNode m_lastSelectedNode;

   /**
    * Root node of the tree. Initialized in the {@link #init()}, never <code>
    * null</code> or modified after that.
    */
   private DefaultMutableTreeNode m_rootNode;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
}