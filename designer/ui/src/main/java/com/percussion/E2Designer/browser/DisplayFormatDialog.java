/*[ DisplayFormatDialog ]*******************************************************
 * COPYRIGHT (c) 2002 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSDbComponent;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSDisplayFormatCollection;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
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


/**
 * Display Formats are used to define the presentation of Folders and Views in
 * the Content Explorer. Display Formats are created and maintained in the
 * workbench.
 *
 * Display format dialog displaying display format tree containing display
 * format node in the left panel and editor for the selected node in the right
 * panel.
 */
public class DisplayFormatDialog extends PSDialog implements
   TreeSelectionListener
{
   /**
    * Constructs the display format dialog.
    *
    * @param parent the parent frame of this dialog. Never <code>null</code>.
    *
    * @param communList a list of communities see
    * {@link CommunitiesCataloger#getAllCommunities()
    * CommunitiesCataloger.getAllCommunities()}, must not be <code>null</code>,
    * may be empty.
    *
    * @param ceCatlg must not be <code>null</code>.
    *
    * @param dbCollect must not be <code>null</code>.
    *
    * @param proxy must not be <code>null</code>.
    */
   public DisplayFormatDialog(Frame parent, List communList,
      PSContentEditorFieldCataloger ceCatlg,
      PSDisplayFormatCollection dbCollect, PSComponentProcessorProxy proxy)
   {
      super(parent);

      if(communList == null)
         throw new IllegalArgumentException("communList must not be null");

      if(ceCatlg == null)
         throw new IllegalArgumentException("ceCatlg must not be null");

      if(dbCollect == null)
         throw new IllegalArgumentException("dbCollect must not be null");

      if(proxy == null)
         throw new IllegalArgumentException("proxy must not be null");

      m_communList = communList;
      m_ceCatlg = ceCatlg;
      m_dbCollect = dbCollect;
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

      int height = 600;

      // split pane:
      m_split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      m_split.setPreferredSize(new Dimension(700, height));

      // tabbed panel:
      m_tabPanel = new DisplayFormatTabbedPanel(this);
      m_split.setRightComponent(m_tabPanel);
      m_tabPanel.setPreferredSize(new Dimension(300, height));

      // tree panel:
      m_treePanel = new TreePanel(m_dbCollect);
      m_treePanel.addTreeSelectionListener(this);
      m_rootNode = (DefaultMutableTreeNode)m_treePanel.getModel().getRoot();
      m_split.setLeftComponent(m_treePanel);
      m_treePanel.setPreferredSize(new Dimension(350, height));

      mainPane.setBorder(BorderFactory.createEmptyBorder( 5, 5, 5, 5 ));
      cmdPanel.setBorder(BorderFactory.createEmptyBorder( 5, 0, 0, 0 ));
      mainPane.add(m_split, BorderLayout.CENTER);
      mainPane.add(cmdPane, BorderLayout.SOUTH);
      getContentPane().add(mainPane);
      
      pack();
      center();
      setResizable(true);

      // set default selected:
      setTabbedData(m_treePanel.getSelectedNode());
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
      TreePath prevPath = e.getOldLeadSelectionPath();
      TreePath newPath = e.getNewLeadSelectionPath();
      PSDisplayFormat prevFormat = null;
      boolean mustRestore = false;

      try
      {
         if(m_restoringPreviousNode)
            return;

         // let's validate first:
         if(!save())
         {
            mustRestore = true;
            return;
         }

         DefaultMutableTreeNode nextNode =
            (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

         if (nextNode == null)
            return;

         // if selecting root node:
         if(newPath.getParentPath() == null)
         {
            PSTreeObject root = (PSTreeObject)m_rootNode.getUserObject();
            ParentPanel defaultPane = (ParentPanel)root.getUIObject();
            m_split.setRightComponent(defaultPane);
            reloadNode(prevPath);

            return;
         }

         // set tabbed data
         setTabbedData(nextNode);
         addDefaultKeyListeners(m_split.getRightComponent());

         // if not leaving root node:
         if(prevPath != null && prevPath.getParentPath() != null)
            // update previous node:
            reloadNode(prevPath);

      }
      finally
      {
         /*We put this in a finally block so that in case any exceptions
            occur while validating, we won't get left in an odd state, ie.
            the new node is selected but the old search object data is still
            in the panels. Specifically, this happened when we got a missing
            resource exception.*/
         if (mustRestore && prevPath != null)
         {
            try
            {
               m_restoringPreviousNode = true;
               ((JTree) e.getSource()).setSelectionPath(prevPath);
            }
            finally
            {
               m_restoringPreviousNode = false;
            }
         }
      }
   }

   /**
    * This returns the community list used to build the dialog.  The caller
    * of this accessor will own the reference to this <code>List</code> and
    * care should be taken when calling it.  This accessor was added to bridge
    * a design flaw.
    *
    * @return see above.  Never <code>null</code>.
    */
   List getCommunityList()
   {
      return m_communList;
   }

   /**
    * This returns the <code>PSContentEditorFieldCataloger</code> used to
    * build the dialog.  The caller of this accessor will own the reference to
    * this <code>PSContentEditorFieldCataloger</code> and care should be taken
    * when calling it.  This accessor was added to bridge a design flaw.
    *
    * @return see above.  Never <code>null</code>.
    */
   PSContentEditorFieldCataloger getCataloger()
   {
      return m_ceCatlg;
   }

   /**
    * This returns the <code>PSDisplayFormatCollection</code> used to
    * build the dialog.  The caller of this accessor will own the reference to
    * this <code>PSDisplayFormatCollection</code> and care should be taken
    * when calling it.  This accessor was added to bridge a design flaw.
    *
    * @return see above.  Never <code>null</code>.
    */
   PSDisplayFormatCollection getDisplayFormatCollection()
   {
      return m_dbCollect;
   }

   /**
    * This returns the <code>PSComponentProcessorProxy</code> used to
    * build the dialog.  The caller of this accessor will own the reference to
    * this <code>PSComponentProcessorProxy</code> and care should be taken
    * when calling it.  This accessor was added to bridge a design flaw.
    *
    * @return see above.  Never <code>null</code>.
    */
   PSComponentProcessorProxy getProcessorProxy()
   {
      return m_proxy;
   }

   /**
    * Refreshes the display name for the node of the given path from
    * its underlying user object, then reloads the tree.
    *
    * @param prePath the path to refresh, if <code>null</code> it returns
    * immediately.
    */
   private void reloadNode(TreePath prePath)
   {
      if(prePath == null)
         return;

      DefaultMutableTreeNode dmt = (DefaultMutableTreeNode)
         prePath.getLastPathComponent();

      PSTreeObject usr = (PSTreeObject)dmt.getUserObject();
      PSDisplayFormat dformat = (PSDisplayFormat)usr.getDataObject();

      if(dformat != null)
         usr.setName(dformat.getDisplayName());

      ((DefaultTreeModel)m_treePanel.getModel()).reload(dmt);
   }

   /**
    * Sets the tabbed data for the tabbed panel.
    *
    * @param selectedNode the node from which to get the data. The node
    * is expected to contain <code>PSTreeObject</code> as its user object.
    * If this argument is <code>null</code> nothing happens and returns
    * immediately.
    */
   void setTabbedData(DefaultMutableTreeNode selectedNode)
   {
      if (selectedNode == null)
         return;

      if(m_split.getRightComponent() != m_tabPanel)
         m_split.setRightComponent(m_tabPanel);

      PSTreeObject selectedNodeInfo =
         (PSTreeObject)selectedNode.getUserObject();

      save();

      // load new stuff:
      m_tabPanel.loadData((PSDisplayFormat)selectedNodeInfo.getDataObject());
   }

   /**
    * Saves the data in the tabbed panel and refreshes the selected tree node.
    */
   private boolean save()
   {
      boolean saved = false;
      // save existing data if there is any:
      if(m_tabPanel.isDataLoaded())
         saved = m_tabPanel.save();

      return saved;
   }

   /**
    * Overriding the same method in {@link PSDialog}.
    */
   public void onApply()
   {
      persistData();
   }

   /**
    * Calls save and if save returns <code>true</code> this method persists
    * the data to the system.
    *
    * @return <code>true</code> if persisted to the system, <code>false</code>,
    * if a failure occurred, including validation failure.
    */
   private boolean persistData()
   {
      boolean saved = false;
      try
      {
         if(save())
         {
            m_proxy.save(new IPSDbComponent[] {m_dbCollect});
            m_treePanel.refreshSelectedNode();
            saved = true;
         }
      }
      catch(PSCmsException e)
      {
         //todo
         e.printStackTrace();
      }
      return saved;
   }

   /**
    * Overriding the same method in {@link PSDialog}.
    */
   public void onOk()
   {
      if(persistData())
         super.onOk();
   }

   /**
    * Overridden to check for unsaved data. If any data is unsaved, a dialog
    * is presented to the user allowing them to either save, ignore or return
    * to editing.
    */
   public void onCancel()
   {
      boolean changed = m_dbCollect.isModified();
      PSDisplayFormat format = m_treePanel.getSelectedObject();

      // update the one currently being edited
      if (!changed && format != null)
      {
         /* Has the current one been changed? It must be done this way because
            we don't want to force the user to correctly fill in fields if
            they are really cancelling. */
         if (m_tabPanel.validateData())
         {
            m_tabPanel.loadData(format);
            changed = changed || format.isModified();
         }
         else
            //if it fails validation, they must have changed something
            changed = true;
      }

      if (changed)
      {
         int choice = JOptionPane.showConfirmDialog(this,
               ms_res.getString("error.msg.unsaveddata"),
               ms_res.getString("error.title.unsaveddata"),
               JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
         if (choice == JOptionPane.YES_OPTION)
         {
            onOk();
            return;
         }
         else if (choice == JOptionPane.CANCEL_OPTION)
            return;
      }
      super.onCancel();
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
      String[] tabs = {"General","Columns","Communities","Properties"};
      int selected = m_tabPanel.getSelectedTabIndex();
      DefaultMutableTreeNode currentTreeNode = m_treePanel.getSelectedNode();
      if(null != helpId && null != currentTreeNode && !currentTreeNode.isRoot())
         helpId += "_"+tabs[selected];

      return helpId ;

   }


   private PSComponentProcessorProxy m_proxy;

   private PSDisplayFormatCollection m_dbCollect;

   private PSContentEditorFieldCataloger m_ceCatlg;

   private List m_communList;


   /**
    * Panel containing display format tree. Initialized in the {@link #init()},
    * never <code>null</code> or modified after that.
    */
   private TreePanel m_treePanel;

   /**
    * Contains all of the tabbed panels.
    */
   private DisplayFormatTabbedPanel m_tabPanel;

   /**
    * Split pane containing the left and right panel. The left panel shows the
    * tree and the right panel displays node sensitive panel. Initialized in the
    * {@link #init()}, never <code>null</code> or modified after that.
    */
   private JSplitPane m_split;

   /**
    * Root node of the tree. Initialized in the {@link #init()}, never <code>
    * null</code> or modified after that.
    */
   private DefaultMutableTreeNode m_rootNode;

   /**
    * While changing nodes, we may need to 'prevent' the change (because the
    * data in the current node is not valid and our rule says we cannot leave
    * a node until it passes validation). During processing of the valueChanged
    * method, we need to disable this method when we reset the path back to
    * its original value. Defaults to <code>false</code>.
    */
   private boolean m_restoringPreviousNode = false;

   /**
    * Resource bundle for this class. Initialized in {@link init()}.
    * It's not modified after that. Never <code>null</code>, equivalent to a
    * variable declared final.
    */
   private static ResourceBundle ms_res;
}
