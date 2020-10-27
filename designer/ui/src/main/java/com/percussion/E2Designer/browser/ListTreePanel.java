/*[ ListTreePanel.java ]***************************************************
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.objectstore.PSAction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ResourceBundle;

/**
 * NOTE: While reviewing please keep in mind that no data objects have been
 * used as of yet so ctor and other places may have to be modified but that
 * shouldn't affect the ui in general, hopefully.
 */

/**
 * Panel containing a tree panel and {@link PSSourceTragetListPanel}.
 */
public class ListTreePanel extends JPanel implements
    TreeSelectionListener
{
   /**
  * NOTE: While reviewing please keep in mind that no data objects have been
  * used as of yet so ctor and other places may have to be modified but that
  * shouldn't affect the ui in general, hopefully.
  */

    /**
     * Constructs the panel.
     */
    public ListTreePanel()
    {
       init();
    }

    /**
     * Initializes the panel.
     */
    private void init()
    {
     /**  if (null == ms_res)
          ms_res = ResourceBundle.getBundle(getClass().getName() + "Resources",
          Locale.getDefault());*/
       setLayout(new BorderLayout());
       setPreferredSize(new Dimension(500, 500));
       JPanel treePanel = new JPanel();
       treePanel.setLayout(new BorderLayout());

       EmptyBorder emptyBorder = new EmptyBorder(10, 10, 10, 10);
       treePanel.setBorder(emptyBorder);
       treePanel.setPreferredSize(new Dimension(400, 200));
       m_tree = new JTree();
       m_tree.addTreeSelectionListener(this);

       m_tree.getSelectionModel().setSelectionMode(
          TreeSelectionModel.SINGLE_TREE_SELECTION);
       m_tree.setEditable(false);
       JScrollPane scrollPane = new JScrollPane(m_tree);
       treePanel.add(scrollPane, BorderLayout.NORTH);
       add(treePanel, BorderLayout.NORTH);



       JPanel jp = new JPanel();
       jp.setPreferredSize(new Dimension(400, 100));
       add(jp, BorderLayout.SOUTH);
    }

    /**
     * Sets the tree model.
     *
     * @param mdl, may not be <code>null</code>
     */
    public void setTreeModel(TreeModel mdl)
    {
       if (mdl != null)
       {
          m_tree.setModel(mdl);
          m_tree.setSelectionRow(0);
       }
    }

    /**
     * Implements the interface. Based on the node selection the associated
     * ui is displayed.
     *
     * @param e, event generated on tree selection, never <code>null</code>,
     * called by the swing event handling mechanism.
     */
    public void valueChanged(TreeSelectionEvent e)
    {
       JTree tree = (JTree)e.getSource();
       DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)
          tree.getModel().getRoot();
       DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode)
          tree.getLastSelectedPathComponent();
       TreePath oldPath = e.getOldLeadSelectionPath();
       DefaultMutableTreeNode oldNode = null;
       JPanel oldPanel = null;
       PSTreeObject oldUsrObj = null;
       if (oldPath != null)
       {
          oldNode = (DefaultMutableTreeNode)
             oldPath.getLastPathComponent();
          oldUsrObj = (PSTreeObject)oldNode.getUserObject();
          oldPanel = oldUsrObj.getUIObject();
          if (oldNode != null && (oldNode != rootNode))
             update(oldUsrObj.getDataObject(), oldUsrObj.getUIObject(), false);
       }
       if (nextNode == null)
          return;
       PSTreeObject nextNodeInfo = (PSTreeObject)nextNode.getUserObject();
       JPanel pane = nextNodeInfo.getUIObject();
       if (pane != null)
       {
          if (oldPanel != null)
          {
             remove(oldPanel);
             revalidate();
          }
          add(pane, BorderLayout.CENTER);
          if (nextNode != rootNode)
             update(oldUsrObj.getDataObject(), oldUsrObj.getUIObject(), true);
          repaint();
       }
    }

    public boolean update(Object data, Object uiComp, boolean isLoad)
    {
       PSAction action = null;

       if (data instanceof PSAction)
       {

       }
       /**PSActionVisibilityContext ctxt = (PSActionVisibilityContext)data;
       PSSourceTargetListPanel list = (PSSourceTargetListPanel)uiComp;
       if (isLoad)
       {

          return isLoad;
       }
       else
       {

       }*/
       return true;
    }

    public boolean validateData()
  {
     return true;
   }


    //test code
 /**   public static void main(String[] arg)
    {
       JFrame f = new JFrame("BoxLayoutDemo");
       Container contentPane = f.getContentPane();
       ActionsUsagePanel ac = new ActionsUsagePanel();
       contentPane.add(ac, BorderLayout.CENTER);
       f.addWindowListener(new WindowAdapter() {
          public void windowClosing(WindowEvent e) {
             System.exit(0);
          }
       });
       f.pack();
       f.setVisible(true);
    }*/
    //end




    /**
     * A generic tree whoes model is specified by {@link#setModel(TreeModel)}.
     * Initialized in {@link#init()}, never <code>null</code> or modified after
     * that.
     */
    private JTree m_tree;

    /**
     * Resource bundle for this class. Initialized in {@link init()}.
     * It's not modified after that. Never <code>null</code>, equivalent to a
     * variable declared final.
     */
    private static ResourceBundle ms_res;
}