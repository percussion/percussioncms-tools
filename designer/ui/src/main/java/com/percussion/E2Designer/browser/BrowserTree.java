/******************************************************************************
 *
 * [ BrowserTree.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.ApplicationImportExport;
import com.percussion.E2Designer.CatalogExtensionCatalogHandler;
import com.percussion.E2Designer.CatalogServerExits;
import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.OSApplication;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.UIAppFrame;
import com.percussion.E2Designer.UIMainFrame;
import com.percussion.E2Designer.Util;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSFieldCataloger;
import com.percussion.cms.objectstore.PSAction;
import com.percussion.cms.objectstore.PSActionParameter;
import com.percussion.cms.objectstore.PSComponentProcessorProxy;
import com.percussion.cms.objectstore.PSDbComponentCollection;
import com.percussion.cms.objectstore.PSDisplayFormat;
import com.percussion.cms.objectstore.PSDisplayFormatCollection;
import com.percussion.cms.objectstore.PSMenuContext;
import com.percussion.cms.objectstore.PSMenuMode;
import com.percussion.cms.objectstore.PSSearch;
import com.percussion.cms.objectstore.PSSearchCollection;
import com.percussion.cms.objectstore.client.PSContentEditorFieldCataloger;
import com.percussion.cms.objectstore.client.PSRemoteCataloger;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSCloneHandlerConfig;
import com.percussion.design.objectstore.PSCloneHandlerConfigSet;
import com.percussion.design.objectstore.PSConfigurationFactory;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.design.objectstore.PSRelationshipConfigSet;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.IPSServerErrors;
import com.percussion.server.config.PSServerConfigException;
import com.percussion.util.PSRemoteRequester;
import org.w3c.dom.Element;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;


/**
 * This tree adds a couple neat features to the basic JTree.
 * <ul>
 * <li> Context sensitive menus </li>
 * <li> Per-node In-place editing </li>
 * <li> Drag and drop support </li>
 * </ul>
 * Dynamic enumeration allows the creation of children of any node to be delayed
 * until the user actually expands a node. This is very useful where a tree may
 * have several nodes, each of which could take a long time to fill. It could
 * also be useful if the child nodes are changing frequently. Every time the node
 * was expanded the children could be recreated.
 * <p>
 * Each node can have its own popup menu, each node can specify its editability
 * (instead of the tree level granularity that JTree provides) and each node
 * determines if it supports drag and drop.
 * <p>
 * This class adds a mouse listener to the base class so it can handle context
 * sensitive menus and the IP editing.
 */
public class BrowserTree extends JTree
      implements    ClipboardOwner,
                    DragGestureListener,
                    DragSourceListener
{
   static ResourceBundle res = BrowserFrame.getBrowser().getResources();
   static ResourceBundle getResources()
   {
      return(res);
   }
   // constructors
   /**
    * Creates a BrowserTree with the passed in node as the root.
    */
   BrowserTree( DefaultBrowserNode root )
   {
      super( root );

      m_Browser=BrowserFrame.getBrowser();

      initListeners();
      m_DragSource = new DragSource();
      // WARNING: if we change DndConstants to ACTION_COPY it does not work
      // therefore we are using ACTION_COPY_OR_MOVE (...guessing java bug)
      m_DragSource.createDefaultDragGestureRecognizer(this, 
            DnDConstants.ACTION_COPY_OR_MOVE, this);

      setCellRenderer(new BrowserCellRenderer());
      setCellEditor(new BrowserCellEditor(this,
                                        (BrowserCellRenderer) getCellRenderer()));
      setEditable(true);
   }


   /**
    * ClipboardOwner interface implementation.
    */
   @SuppressWarnings("unused")
   public void lostOwnership(Clipboard clipbd, Transferable Data )
   {

   }


   // DragGestureListener interface implementation
   /**
    * Finds the node that the gesture was performed upon and queries it to
    * determine if it supports drag and drop. If it does, the Transferable
    * object is obtained from the node and the dragging is initiated.
    */
   public void dragGestureRecognized( DragGestureEvent dge )
   {
//      System.out.println("drag_gesture recognized");

      Point ptDragOrigin = dge.getDragOrigin();
      TreePath tpc = this.getPathForLocation(ptDragOrigin.x, ptDragOrigin.y);
    if(tpc != null)
    {
      this.setSelectionPath(tpc); //set this node as selected - enforce selection
    }
    TreePath tp = this.getSelectionPath();
    int iSel = this.getMinSelectionRow();
    Rectangle rectSel = this.getRowBounds(iSel);
      DefaultBrowserNode dbn = null;

      if(rectSel == null)
         return;
    if(rectSel.contains(ptDragOrigin))
      dbn = (DefaultBrowserNode)tp.getLastPathComponent();

      if(dbn == null)
         return;

      if(dbn.isDraggable())
      {
//         System.out.println("Where are you taking me ...!!");
         try
         {
            Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable data = dbn.getDragDropObject();
            cb.setContents(data, this);
            dge.startDrag(DragSource.DefaultMoveDrop,
               null,
               dge.getDragOrigin(),
               data,
               this);
            dge.getDragSource().addDragSourceListener(new DragSourceListener() {
               public void dragEnter(@SuppressWarnings("unused")
                     DragSourceDragEvent dsde)
               {
               }

               public void dragOver(DragSourceDragEvent dsde)
               {
                  DragSourceContext ctx = dsde.getDragSourceContext();
                  
                  int action = dsde.getDropAction();
                  
                  if (action == DnDConstants.ACTION_MOVE)
                  {
                     ctx.setCursor(DragSource.DefaultMoveDrop);
                  }
                  else
                  {
                     ctx.setCursor(DragSource.DefaultMoveNoDrop);
                  } 
               }

               public void dropActionChanged(
                     @SuppressWarnings("unused") DragSourceDragEvent dsde)
               {
               }

               public void dragDropEnd(
                     @SuppressWarnings("unused") DragSourceDropEvent dsde)
               { 
               }

               public void dragExit(
                     @SuppressWarnings("unused") DragSourceEvent dse)
               {
               }});
         }
         catch (InvalidDnDOperationException e)
         {
            System.out.println("Invalid dnd operation");
         }

      }

   }

   /**
    * DragSourceListener interface implementation.
    */
   public void dragEnter( DragSourceDragEvent dsde )
   {
      dsde.getDragSourceContext().setCursor(Cursor.getDefaultCursor());
   }

   /**
    * DragSourceListener interface implementation.
    */
   public void dragOver(@SuppressWarnings("unused") DragSourceDragEvent dsde)
   {
      //System.out.println("src dragOver");

   }

   /**
    * DragSourceListener interface implementation.
    */
   public void dragExit(@SuppressWarnings("unused")   DragSourceEvent dse)
   {
      //System.out.println("src dragExit");
   }

   /**
    * DragSourceListener interface implementation.
    */
   public void dragDropEnd(@SuppressWarnings("unused") DragSourceDropEvent dsde)
   {
      //System.out.println("src dragDropEnd");
   }

   /**
    * DragSourceListener interface implementation.
    */
   @SuppressWarnings("unused")
   public void dropActionChanged(DragSourceDragEvent dte)
   {
      //System.out.println("src dragChanged");
   }

   /**
    * Initializes the listeners associated with the BrowserTree.
    */
  private void initListeners()
  {
    //Listeners
      m_treeExpansionListener = new TreeExpListener();
    m_popupTriggerListener = new  PopupTriggerListener();
    m_menuItemListener = new MenuItemListener();
    m_keyPressedListener = new KeyPressedListener();
      m_doubleClickListener = new MouseDoubleClickListener();

      this.addTreeExpansionListener(m_treeExpansionListener);
      this.addMouseListener(m_popupTriggerListener);
      this.addKeyListener(m_keyPressedListener);
      this.addMouseListener(m_doubleClickListener);

  }

   /**
    * Inner class to listen for a mouse doubleclick.
    */
  class MouseDoubleClickListener extends MouseAdapter
   {
    @Override
   public void mouseClicked( MouseEvent e )
      {
      if(e.isPopupTrigger())
            return;

         if(e.getClickCount() == 2)
      {
        onMouseDoubleClick(e.getPoint());
      }
      }
   }



   /**
    * Inner class to listen for a popup trigger.
    */
  class PopupTriggerListener extends MouseAdapter
   {
    @Override
   public void mouseReleased( MouseEvent e )
      {
      if(e.isPopupTrigger())
      {
        createPopupMenu(e.getPoint());
      }
      }
   }

   /**
    * Inner class to listen for a key pressed event.
    */
  class KeyPressedListener extends KeyAdapter
  {
    @Override
   public void keyPressed( KeyEvent e )
    {
      if (e.getKeyCode() == KeyEvent.VK_DELETE)
      {
        //onRemoveSelectedNode();
      }
    }
  }

   /**
    * Inner class to listen for a menu item selection.
    */
   class MenuItemListener implements ActionListener
   {
      public void actionPerformed( ActionEvent e)
      {
         JMenuItem mi = (JMenuItem)e.getSource();
         if (mi.getActionCommand().equals(res.getString("REMOVE")))
            onRemoveSelectedNode();
         else if (mi.getActionCommand().equals(res.getString("AUTO_REFRESH")))
           onAutoRefresh();
         else if (mi.getActionCommand().equals(res.getString("OPEN_APPLICATION")))
           onOpenApplication();
         else if (mi.getActionCommand().equals(res.getString("EDIT")))
            onRename();
         else if (mi.getActionCommand().equals(res.getString("ENABLE")))
            onApplicationStatusChange();
         else if (mi.getActionCommand().equals(res.getString("DISABLE")))
            onApplicationStatusChange();
         else if (mi.getActionCommand().equals(res.getString("FILETYPES")))
            onFileTypes();
         else if (mi.getActionCommand().equals(res.getString("EXPORT")))
            onExportApplication();
      }
   }

   /**
    * Inner class to listen for a tree node expansion.
    */
   class TreeExpListener implements TreeExpansionListener
   {
      @SuppressWarnings("unused")
      public void treeCollapsed(TreeExpansionEvent e)
      {

      }
      public void treeExpanded(TreeExpansionEvent e)
      {
         onNodeExpansion(e);
      }

   }

   /**
    * Handler for node expansion. If autorefresh flag defined the BrowserFrame is on
    * then this method will remove all children and requery the E2 server to catalog
    * and reload the children.
    */
   private void onNodeExpansion(TreeExpansionEvent e)
   {
//      System.out.println("Got node expansion event");
      if(!m_Browser.isAutoRefreshOn())
            return;

         // need to do the following only if AutoRefresh is on
         TreePath tp = e.getPath();
         if(tp == null)
            return;

         DefaultBrowserNode dbn = (DefaultBrowserNode)tp.getLastPathComponent();
         if(dbn == null)
            return;

         // check to see if Applications are to be refreshed
       BrowserPane btp = m_Browser.getBrowserPane();
       int iSelTabIndex = btp.getSelectedIndex();
         int iAppTabIndex = btp.indexOfTab(m_Browser.getResources().getString("APPLICATIONS"));
//         System.out.println("Selected index = "+iSelTabIndex+"   Applications tab index ="+iAppTabIndex);

         if(iSelTabIndex == iAppTabIndex)   // selected tab is applications Tab
         {
            BrowserTree bt = (BrowserTree)((JScrollPane)btp.getComponentAt(iSelTabIndex)).getViewport().getView();
            DefaultBrowserNode root = (DefaultBrowserNode)bt.getModel().getRoot();

            if(root.equals(dbn))
            {
               refreshApplications(dbn, iAppTabIndex, tp);
               return;
            }
         }

//         System.out.println("Node Expanded "+dbn.getInternalName());
         if(dbn.isLoadedChildren())
         {
//            System.out.println("children are loaded,   removing all children");
            dbn.removeAllChildren();
            dbn.resetDynamicEnum();
            dbn.setLoadedChildren(false);
            BrowserTree bt = (BrowserTree)((JScrollPane)btp.getComponentAt(iSelTabIndex)).getViewport().getView();
            ((DefaultTreeModel)bt.getModel()).nodeStructureChanged(dbn);
            dbn.loadChildren();

         }
   }

   /**
    * Refreshes the applications. If autorefresh flag defined the BrowserFrame is on
    * then this method will remove all children and requery the E2 server to catalog
    * and reload the children.
    *@param root - the applications root node
    *@param iTabIndex - the index of the applications tab
    *@param tp - tree path to the root
    */
   private void refreshApplications(DefaultBrowserNode root, int iTabIndex,
         @SuppressWarnings("unused") TreePath tp)
   {
      if(!m_Browser.isAutoRefreshOn())
         return;
//      System.out.println("in refresh Applications");
      if(root.isLoadedChildren())
      {
//      System.out.println("children are loaded,   removing all children");
         root.removeAllChildren();
         root.setLoadedChildren(false);
       BrowserPane btp = m_Browser.getBrowserPane();
         BrowserTree bt = (BrowserTree)((JScrollPane)btp.getComponentAt(iTabIndex)).getViewport().getView();
         ((DefaultTreeModel)bt.getModel()).nodeStructureChanged(root);
         m_Browser.addAppsToAppsTab();
      }
   }

   /**
    * Action method for mouse double-click event on a tree node. This action is
    * supported for two cases.
    * <ol>
    * <li>Any Application node in Applications tab - opens the application </li>
    * <li>Relationships node in Server Objects tab - opens a dialog to edit the
    * relationships configuration</li>
    * </ol>
    *
    * @param point the point where the mouse-click occurred, assumed not <code>
    * null</code>
    *
    */
   private void onMouseDoubleClick(Point point)
   {
      TreePath tpc = this.getPathForLocation(point.x, point.y);

      if(tpc != null)
         setSelectionPath(tpc); //set this node as selected - enforce selection
      else
         return;

      TreePath tp = getSelectionPath();
      int iSel = getMinSelectionRow();
      Rectangle rectSel = getRowBounds(iSel);

      if(rectSel.contains(point))
      {
         DefaultBrowserNode dbn =
            (DefaultBrowserNode)tp.getLastPathComponent();
         if(dbn instanceof ServerNode)
         {
            ServerNode servNode = (ServerNode)dbn;
            if(servNode.getConstraints() instanceof
               ApplicationHierarchyConstraints)
            {
               onOpenApplication();
            }
         }
         else if( dbn.getUserObject() != null &&
            E2Designer.getResources().getString("RELATIONSHIPS").equals(
            dbn.getUserObject().toString()) )
         {
            E2Designer designer = E2Designer.getApp();
            UIMainFrame mainFrame = null;
            if (designer != null)
            {
               mainFrame = designer.getMainFrame();
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }

            try
            {
               onEditRelationships();
            }
            finally
            {
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getDefaultCursor());
            }
         }
         else if( dbn.getUserObject() != null &&
            E2Designer.getResources().getString("DISPLAYFORMATS").equals(
            dbn.getUserObject().toString()) )
         {
            E2Designer designer = E2Designer.getApp();
            UIMainFrame mainFrame = null;
            if (designer != null)
            {
               mainFrame = designer.getMainFrame();
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }

            try
            {
               onEditDisplayFormats();
            }
            finally
            {
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getDefaultCursor());
            }
         }
         else if( dbn.getUserObject() != null &&
            E2Designer.getResources().getString("ACTIONMENUS").equals(
            dbn.getUserObject().toString()) )
         {
            E2Designer designer = E2Designer.getApp();
            UIMainFrame mainFrame = null;
            if (designer != null)
            {
               mainFrame = designer.getMainFrame();
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }

            try
            {
               onEditActionMenus();
            }
            finally
            {
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getDefaultCursor());
            }
         }
         else if( dbn.getUserObject() != null &&
              E2Designer.getResources().getString("SEARCHES").equals(
              dbn.getUserObject().toString()) )
         {
            E2Designer designer = E2Designer.getApp();
            UIMainFrame mainFrame = null;
            if (designer != null)
            {
               mainFrame = designer.getMainFrame();
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }

            try
            {
               onEditContentViews();
            }
            finally
            {
               if (mainFrame != null)
                  mainFrame.setCursor(Cursor.getDefaultCursor());
            }
         }
      }
   }

   private void onEditActionMenus()
   {
      try
      {
         Frame parent = E2Designer.getApp().getMainFrame();
         PSRemoteRequester appReq = new PSRemoteRequester(
            E2Designer.getLoginProperties());
         new PSRemoteCataloger(appReq);

         PSComponentProcessorProxy proxy = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE, appReq);

         Element [] elems = proxy.load(PSAction.getComponentType(
            PSAction.class) , null);
         //get all the actions
         PSDbComponentCollection actionColl = new
            PSDbComponentCollection(PSAction.class);
         if (elems != null && elems.length != 0)
         {
            for (int i=0; i<elems.length; i++)
            {
               PSAction s = new PSAction(elems[i]);
               actionColl.add(s);
            }
         }
         //get all the menumodes
         PSDbComponentCollection menuModeColl = new
            PSDbComponentCollection(PSMenuMode.class);
         elems = proxy.load(PSMenuMode.getComponentType(
            PSMenuMode.class), null);

         if (elems != null && elems.length != 0)
         {
            for (int i=0; i<elems.length; i++)
            {
               PSMenuMode s = new PSMenuMode(elems[i]);
               menuModeColl.add(s);
            }
         }

         //get all the menumodecontexts
         PSDbComponentCollection modeContextColl = new
               PSDbComponentCollection(PSMenuContext.class);
         elems = proxy.load(PSMenuContext.getComponentType(
            PSMenuContext.class), null);
         if (elems != null && elems.length != 0)
         {
            for (int i=0; i<elems.length; i++)
            {
               PSMenuContext s = new PSMenuContext(elems[i]);
               modeContextColl.add(s);
            }
         }

         // build the list of known context parameters
         List<String> contextParameters = new ArrayList<String>();
         Iterator actions = actionColl.iterator();
         while (actions.hasNext())
         {
            PSAction action = (PSAction) actions.next();
            Iterator params = action.getParameters().iterator();
            while (params.hasNext())
            {
               PSActionParameter param = (PSActionParameter) params.next();
               String value = param.getValue();
               if (value != null && value.startsWith("$") &&
                  !contextParameters.contains(value))
                  contextParameters.add(value);
            }
         }

         List<Object> list = new ArrayList<Object>();
         list.add(actionColl);
         list.add(menuModeColl);
         list.add(modeContextColl);
         list.add(contextParameters);
         PSActionDialog actionDlg = new PSActionDialog(parent, list, proxy);
         actionDlg.setVisible(true);
      }
      catch(PSCmsException e)
      {
         e.printStackTrace();
      }
      catch(PSUnknownNodeTypeException u)
      {
         u.printStackTrace();
      }
   }

   /**
    * Displays display format dialog and catalogs communities.
    */
   private void onEditDisplayFormats()
   {
      try
      {
         Frame parent = E2Designer.getApp().getMainFrame();
         List list = CommunitiesCataloger.getAllCommunities();
         PSRemoteRequester appReq = new PSRemoteRequester(
               E2Designer.getLoginProperties());
         PSRemoteCataloger remCatlg = new PSRemoteCataloger(appReq);
         PSContentEditorFieldCataloger fieldCatlgObj = new
               PSContentEditorFieldCataloger(remCatlg, null,
               IPSFieldCataloger.FLAG_INCLUDE_RESULTONLY);

         PSComponentProcessorProxy proxy = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE, appReq);

         PSDisplayFormatCollection c = getDbComponents(proxy);
         DisplayFormatDialog displayFormatDlg = new DisplayFormatDialog(
               parent, list, fieldCatlgObj, c, proxy);
         displayFormatDlg.setVisible(true);
      }
      catch(PSCmsException e)
      {
         e.printStackTrace();
      }
      catch(PSUnknownNodeTypeException u)
      {
         u.printStackTrace();
      }
      catch(ClassNotFoundException c)
      {
         c.printStackTrace();
      }
   }

   /**
    * Content Views format dialog. Loads the existing content views using
    * the remote processor, then launches the dialog.
    */
   private void onEditContentViews()
   {
      Frame parent = E2Designer.getApp().getMainFrame();
      try
      {
         PSRemoteRequester appReq = new PSRemoteRequester(
            E2Designer.getLoginProperties());

         PSComponentProcessorProxy proxy = new PSComponentProcessorProxy(
            PSComponentProcessorProxy.PROCTYPE_REMOTE, appReq);

         org.w3c.dom.Element [] elems = proxy.load(PSSearch.getComponentType(
            PSSearch.class) , null);

         // Construct a component collection of
         // PSSearch objects for the ui
         PSSearchCollection c = new PSSearchCollection();

         /**
          * The Content Views only need PSSearch objects with the type of
          * 'View'.  We can check the elements TYPE node for View as it's value,
          * but we'd have to do the xml walking ourselves.  We might as well
          * just create the PSSearch object from the element, letting it handle
          * the xml, then check the value by a isView() call.  If it's true add
          * to the collection, if false lose the reference.
          */
         PSSearch temp = null;
         for (int i=0; i<elems.length; i++)
         {
            temp = new PSSearch(elems[i]);
            if(!temp.isUserSearch()) //skip all user searches
               c.add(temp);
         }

         SearchViewDialog dlg = new SearchViewDialog(parent, c, proxy);
         dlg.setVisible(true);
      }
      catch (Exception e)
      {
         PSDlgUtil.showError(e, true,
               E2Designer.getResources().getString("UnexpectedProcessorError"));
      }
   }


   /**
    *
    * @param proxy
    * @throws PSCmsException
    */
   private PSDisplayFormatCollection getDbComponents(
      PSComponentProcessorProxy proxy)
      throws PSCmsException, PSUnknownNodeTypeException, ClassNotFoundException
   {
      Element[] elems = proxy.load(PSDisplayFormat.getComponentType(
         PSDisplayFormat.class) , null);
      PSDisplayFormatCollection dbColl = new  PSDisplayFormatCollection();
      if (elems != null && elems.length != 0)
      {
         for (int i=0; i<elems.length; i++)
         {
            PSDisplayFormat s = new PSDisplayFormat(elems[i]);
            dbColl.add(s);
         }
      }
      return dbColl;
   }



   /**
    * Locks the relationships configuration and displays the editor dialog to
    * edit it. Displays a confirmation message to the user to override if the
    * configuration is locked by the same user or different user. Displays
    * appropriate error messages when exceptions happen.
    *
    * @throws IllegalStateException if Rhythmyx(System) clone handler
    * configuration is not found.
    */
   private void onEditRelationships()
   {
      Frame parent = E2Designer.getApp().getMainFrame();

      PSObjectStore objStore = m_Browser.getObjectStore();

      PSRelationshipConfigSet rsConfigSet = null;
      boolean overrideSame = false;
      boolean overrideDiff = false;
      try {
         rsConfigSet =
            (PSRelationshipConfigSet)objStore.getRxConfiguration(
            PSConfigurationFactory.RELATIONSHIPS_CFG, true,
            overrideSame, overrideDiff);
      }
      catch(PSLockedException e)
      {
         String msg = e.getLocalizedMessage();
         if(e.getErrorCode() == IPSServerErrors.CONFIG_LOCKED)
            overrideDiff = true;
         else if(e.getErrorCode() == IPSServerErrors.CONFIG_LOCKED_SAME)
            overrideSame = true;

         int option = PSDlgUtil.showConfirmDialog(msg + "\n" +
            E2Designer.getResources().getString("OverrideLockMsg"),
            E2Designer.getResources().getString("OverrideLockTitle"),
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

         if(option == JOptionPane.YES_OPTION)
         {
            try {
               rsConfigSet =
                  (PSRelationshipConfigSet)objStore.getRxConfiguration(
                  PSConfigurationFactory.RELATIONSHIPS_CFG, true,
                  overrideSame, overrideDiff);
            }
            catch(PSLockedException ex) //in general should not happen
            {
               PSDlgUtil.showError(ex, false,
                     E2Designer.getResources().getString("OverrideLockFailedTitle"));
            }
            catch(Exception exc)
            {
               PSDlgUtil.showError(exc, false,
                     E2Designer.getResources().getString("error"));
            }
         }
      }
      catch(PSException exc)
      {
         PSDlgUtil.showError(exc, false,
               E2Designer.getResources().getString("error"));
      }

      //Get clone handler config, exits and display the dialog.
      if(rsConfigSet != null)
      {
         try {
            PSCloneHandlerConfig sysCHConfig =
                  getCloneHandlerConfigSet(objStore, overrideSame, overrideDiff);

            Vector exits = CatalogServerExits.getCatalog(
               E2Designer.getDesignerConnection(),
               CatalogExtensionCatalogHandler.JAVA_EXTENSION_HANDLER_NAME,
               false);

            //above method will return null in case of exception after
            //displaying an error message.
            if(exits != null)
            {
               PSRelationshipEditorDialog dlg = new PSRelationshipEditorDialog(
                  parent, rsConfigSet, sysCHConfig, exits.iterator());
               dlg.setVisible(true);
            }
         }
         catch(PSException exc)
         {
            PSDlgUtil.showError(exc, false, E2Designer.getResources().getString("error"));
         }
         finally
         {
            try {
               objStore.saveRxConfiguration(
                  PSConfigurationFactory.RELATIONSHIPS_CFG, null, true);
            }
            catch(PSException exc)
            {
               PSDlgUtil.showError(exc, false, E2Designer.getResources().getString("error"));
            }
         }
      }
   }
   
   /**
    * Retrieves clone handler config.
    * @param overrideSame default value is <code>false</code>
    * @param overrideDiff default value is <code>false</code>
    */
   public static PSCloneHandlerConfig getCloneHandlerConfigSet(
         PSObjectStore objStore, boolean overrideSame, boolean overrideDiff)
               throws PSServerException, PSAuthenticationFailedException,
               PSAuthorizationException, PSLockedException,
               PSServerConfigException, PSUnknownNodeTypeException
   {
      final PSCloneHandlerConfigSet chConfigSet =
         (PSCloneHandlerConfigSet)objStore.getRxConfiguration(
         PSConfigurationFactory.CLONE_HANDLERS_CFG, false,
         overrideSame, overrideDiff);

      PSCloneHandlerConfig sysCHConfig = chConfigSet.getConfig(
         PSConfigurationFactory.SYS_CLONE_CFG_NAME);

      if(sysCHConfig == null)
         throw new IllegalStateException(
            "Rhythmyx clone handler configuration is not found.");
      return sysCHConfig;
   }


   /**
    * Creates the popup menu. Either a general popup menu or a context sensitive popup
    * menu depending on the location of the click.
    */
  private void createPopupMenu(Point point)
  {
    JMenuItem mi = null;
    JPopupMenu popup = null;
    TreePath tpc = this.getPathForLocation(point.x, point.y);
    if(tpc != null)
    {
      this.setSelectionPath(tpc); //set this node as selected - enforce selection
    }

    if(isSelectionEmpty() || getSelectionCount() > 1)
    {
      createGeneralPopupMenu(point);
    }
    else
    {
      TreePath tp = this.getSelectionPath();
      int iSel = this.getMinSelectionRow();
      Rectangle rectSel = this.getRowBounds(iSel);

      if(rectSel.contains(point))
      {
        DefaultBrowserNode dbn = (DefaultBrowserNode)tp.getLastPathComponent();
        if(dbn.hasMenu())
        {
          popup = dbn.getContextMenu();
        if(popup == null)
         return;

          if(dbn.isRenamable())
         popup.add(new JMenuItem(res.getString("EDIT")));

        MenuElement [] mItems = popup.getSubElements();
          for(int i=0; i<mItems.length; i++)
          {
            mi = (JMenuItem)mItems[i];
            mi.addActionListener(m_menuItemListener);
          }
          popup.show(this, point.x, point.y);
        }
        else
          createGeneralPopupMenu(point);
      }
      else
        createGeneralPopupMenu(point);
    }

  }

   /**
    * Creates a general popup menu.
    */
  private void createGeneralPopupMenu(Point point)
  {
      JMenuItem mi = null;
      JPopupMenu popup = null;
      popup = new JPopupMenu();
         int iGroupID = m_Browser.getGroupIDForCurrentTab();

     popup.addSeparator();

     JCheckBoxMenuItem jcbmi = new JCheckBoxMenuItem(res.getString("AUTO_REFRESH"),  m_Browser.isAutoRefreshOn());
     jcbmi.addActionListener(m_menuItemListener);
     popup.add(jcbmi);

     if(iGroupID == BrowserPaneTabData.XML)
     {
      popup.addSeparator();
      mi = popup.add(new JMenuItem(res.getString("FILETYPES")));
        mi.addActionListener(m_menuItemListener);
     }
      popup.show(this, point.x, point.y);
  }

   /**
    * Handler for menu item selection open application.
    */
   private void onOpenApplication()
   {
      E2Designer designer = E2Designer.getApp();
      UIMainFrame mainFrame = null;
      if (designer != null)
      {
         mainFrame = designer.getMainFrame();
         if (mainFrame != null)
            mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      }

      try
      {
         // TODO: Andriy - review
         throw new AssertionError("Commented out");
         
//         TreePath tp = this.getSelectionPath(); //returns the first selection path
//         DefaultBrowserNode dbn = (DefaultBrowserNode) tp.getLastPathComponent();
//         String strApp = dbn.getInternalName();
//         if (mainFrame != null)
//            mainFrame.openApplication(strApp);
      }
      finally
      {
         if (mainFrame != null)
            mainFrame.setCursor(Cursor.getDefaultCursor());
      }
   }

   /**
    * Handler for menu item selection export application.
    */
   private void onExportApplication()
   {
      TreePath tp = this.getSelectionPath(); //returns the first selection path
      DefaultBrowserNode dbn = (DefaultBrowserNode)tp.getLastPathComponent();
      String strApp = dbn.getInternalName();

      E2Designer e2designer = E2Designer.getApp();
      UIMainFrame uiMF = e2designer.getMainFrame();
      UIAppFrame uiApp = uiMF.getApplicationFrame(strApp);

      /*
      * If selected application for export is not open, read the application
      * from server and export, otherwise activate the application and use
      * the application's export function to save and export
      */

      if(uiApp == null)
      {
         try
         {
            ApplicationImportExport impExp = new ApplicationImportExport(
                              new JFileChooser(System.getProperty("user.dir")));
            PSApplication app =
                  BrowserFrame.getBrowser().getObjectStore().getApplication(
                                                                  strApp, true);
            impExp.exportApplication( new OSApplication( app.toXml() ) );
         }
         // 5 different exceptions are thrown, so catch em all and show error
         catch ( Exception ex )
         {
            PSDlgUtil.showError(ex);
         }
      }
      else
      {
         // TODO: Andriy - review
         throw new AssertionError("Commented out");
//         uiMF.openApplication(strApp);
//         uiApp.exportApplication();
      }
   }

   /**
    * Handler for menu item selection enable/disable application.
    */
   private void onApplicationStatusChange()
   {
      try
      {
         TreePath tp = this.getSelectionPath(); //returns the first selection
                                                // path
         DefaultBrowserNode dbn = 
            (DefaultBrowserNode) tp.getLastPathComponent();
         String strApp = dbn.getInternalName();
         PSApplication app = 
            BrowserFrame.getBrowser().getObjectStore().getApplication(strApp, 
               true);
         /* if app failed to start or was abnormally terminated, the enabled
          * state of the application object may be out of synch with the actual
          * running state of the app.  Fix it before calling the toggle method.
          */ 
         app.setEnabled(BrowserFrame.getBrowser().isApplicationRunning(
            dbn.getInternalName())); 
         E2Designer.getApp().getMainFrame().toggleApplicationStatus(app, true,
            null);

         // update the icon
         setApplicationIcon(dbn, BrowserFrame.getBrowser().isApplicationRunning(
            dbn.getInternalName()));
      }
      catch (PSLockedException e)
      {
         e.printStackTrace();
      }
      catch (PSAuthorizationException e)
      {
         final Object[] params = {e.toString()};
         PSDlgUtil.showErrorDialog(
         Util.cropErrorMessage(
               MessageFormat.format(
                     E2Designer.getResources().getString("AuthException"), params)),
               E2Designer.getResources().getString("AuthErr"));
      }
      catch (PSAuthenticationFailedException e)
      {
         e.printStackTrace();
      }
      catch (PSServerException e)
      {
         // this should never happen
         e.printStackTrace();
      }
      catch (PSNotFoundException e)
      {
         // this should never happen
         e.printStackTrace();
      }
   }

   /**
    * Sets the application icon according to the provided state.
    *
    * @param appName the application name to set the icon for
    * @param active the current state, true for avtive, false for incative
    */
   public void setApplicationIcon(String appName, boolean active)
   {
       DefaultBrowserNode dbn = getApplicationNode(appName);
       if(dbn != null)
         setApplicationIcon(dbn, active);
   }

   /**
    * Gets the application node based on name
    * Returns  <code>null</code> if matching node not found
    *
    * @param appName the application name, assumed not <code>null</code>.
    * @return The Matching node. May be <code>null</code>.
    */
   private DefaultBrowserNode getApplicationNode(String appName)
   {
      TreePath tp = null;
      DefaultBrowserNode dbn = null;

      for (int i=0; i<getRowCount(); i++)
      {
         tp = getPathForRow(i);
         dbn =  (DefaultBrowserNode) tp.getLastPathComponent();
         if (dbn.getInternalName().equals(appName))
            return dbn;
      }
      return null;
   }

   /**
    * Sets the application icon according to the provided state.
    *
    * @param node the node for which we should update the icon
    * @param active the current state, true for avtive, false for incative
    */
   private void setApplicationIcon(DefaultBrowserNode node, boolean active)
   {
      if (active)
         node.setIcon(m_iconActiveApp);
      else
         node.setIcon(m_iconApp);

      validate();
      repaint();
   }

   /**
    * Refreshes node in tree with new name
    * This is called when application name has changed via properties
    *
    * @param appName current name of application, must not be <code>null</code>
    *        or empty
    * @param newName name to update, must not be <code>null</code> or empty
    */
   public void refreshAppName(String appName, String newName)
   {
      if(appName == null || appName.length() == 0)
         throw new IllegalArgumentException(
            "Application Name can not be null or empty");

      if(newName == null || newName.length() == 0)
         throw new IllegalArgumentException(
            "New Application Name can not be null or empty");

      DefaultBrowserNode dbn = getApplicationNode(appName);
      if(dbn != null)
      {
         dbn.setInternalName(newName);
         ((DefaultTreeModel)this.getModel()).reload(dbn);
      }
   }

   /**
    * Handler for menu item selection auto refresh.
    */
   private void onAutoRefresh()
   {
      boolean bAutoRefresh = m_Browser.isAutoRefreshOn();
      m_Browser.setAutoRefresh(!bAutoRefresh);

      //if we just turned auto refresh on collapse all
      if(!bAutoRefresh)
      {
         Enumeration e = getExpandedDescendants(getPathForRow(0));
         if(e != null)
         {
            while(e.hasMoreElements())
            {
               TreePath path = (TreePath)e.nextElement();
               if(path != null)
               {
                  if(isExpanded(path))
                     collapsePath(path);
               }
            }

            expandRow(0);
         }
      }
   }

   /**
    * Handler for menu item selection remove.
    */
  public void onRemoveSelectedNode()
  {
    if(isSelectionEmpty() || getSelectionCount() > 1)
      return;

    TreePath tp = this.getSelectionPath(); //returns the first selection path
    DefaultBrowserNode dbn = (DefaultBrowserNode)tp.getLastPathComponent();
      if(!dbn.isRemovable())
         return;

    //  remove the node from the tree
      if (dbn instanceof ServerNode)
      ((ServerNode)dbn).removeEntry();

    this.updateUI();
  }


   /**
   *
   * Edit the file types that should be listed in the browser
   */
   public void onFileTypes()
   {
      String[] types = m_Browser.getValuesFromUserConfig(res.getString("FILETYPES"));
      if(types == null)
      {
         types = FileHierarchyConstraints.straFILETYPES;
      }

      FileTypesDialog dlg = new FileTypesDialog(E2Designer.getApp().getMainFrame(), types);
      if(dlg.wasOKPressed())
      {
         m_Browser.setUserConfigFor(res.getString("FILETYPES"), dlg.getData(), true);
         //if autorefresh is on collapse all
         if(m_Browser.isAutoRefreshOn())
         {
            Enumeration e = getExpandedDescendants(getPathForRow(0));
            if(e != null)
            {
               while(e.hasMoreElements())
               {
                  TreePath path = (TreePath)e.nextElement();
                  if(path != null)
                  {
                     if(isExpanded(path))
                        collapsePath(path);
                  }
               }

               expandRow(0);
            }
         }
      }
   }

   /**
    * Start renaming from menu
    */
   public void onRename()
   {
      TreePath tp = getSelectionPath();
      
      if(tp != null)
      {
         DefaultBrowserNode dbn = (DefaultBrowserNode)tp.getLastPathComponent();
         if(dbn != null)
         {
            if(dbn.isRenamable())
               startEditingAtPath(tp);
         }
      }
   }

   /**
    * Renderer class used to render the tree ccell in edit mode
    */
  class BrowserCellRenderer extends DefaultTreeCellRenderer
  {
    @Override
   public Icon getLeafIcon()
    {
      TreePath tp = getSelectionPath();
      if (tp != null)
      {
        DefaultBrowserNode dbn = (DefaultBrowserNode) tp.getLastPathComponent();
        return dbn.getIcon();
      }

      // return the default icon if all previous code failed
      return m_iconApp;
    }
  }

   /**
    * Editor class used to determine if the node is editable.
    */
   class BrowserCellEditor extends DefaultTreeCellEditor implements CellEditorListener
   {
      BrowserCellEditor(JTree tree, BrowserCellRenderer renderer)
      {
         super(tree, renderer);
         addCellEditorListener(this);
      }

      /**
       * Determine if this cell is editable
       */
      @Override
      public boolean isCellEditable(EventObject anEvent)
      {
         if (super.isCellEditable(anEvent))
         {
            TreePath tp = getSelectionPath();

            if (tp != null)
            {
               DefaultBrowserNode dbn = (DefaultBrowserNode) tp.getLastPathComponent();

               if (dbn != null)
                  return dbn.isRenamable();
            }
         }

         return false;
      }

      /**
       * Make change take affect
       */
      public void editingStopped(@SuppressWarnings("unused") ChangeEvent e)
      {
         TreePath tp = getSelectionPath();

         if (tp != null)
         {
            DefaultBrowserNode dbn = (DefaultBrowserNode) tp.getLastPathComponent();
            if (dbn != null && !dbn.toString().equals(dbn.getInternalName()))
            {
               // rename it
               dbn.Rename(tp);
            }
         }
      }

      /**
       * Canceled the editing do nothing
       */
      public void editingCanceled(@SuppressWarnings("unused") ChangeEvent e)
      {

      }
   }

   /**
    * a reference to the browser for internal use.
    */
  private BrowserFrame m_Browser;

   /**
    * member for storing the mouse doubleclick listener.
    */
   private MouseDoubleClickListener m_doubleClickListener;

   /**
    * member for storing the tree expansion listener.
    */
   private TreeExpListener m_treeExpansionListener;

   /**
    * member for storing the menu item listener.
    */
  private MenuItemListener m_menuItemListener;

   /**
    * member for storing the key pressed listener.
    */
  private KeyPressedListener m_keyPressedListener;

   /**
    * member for storing the popup trigger listener.
    */
  private PopupTriggerListener m_popupTriggerListener;

   /**
    * member for storing the drag source object.
    */
   private DragSource m_DragSource;

  /**
   * the application icons
   */
  ImageIcon m_iconApp = new ImageIcon(getClass().getResource(
        BrowserFrame.getBrowser().getResources().getString("gif_app")));
  ImageIcon m_iconActiveApp = new ImageIcon(getClass().getResource(
        BrowserFrame.getBrowser().getResources().getString("gif_app_active")));
}


