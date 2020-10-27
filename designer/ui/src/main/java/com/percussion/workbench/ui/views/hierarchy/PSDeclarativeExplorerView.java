/******************************************************************************
 *
 * [ PSDeclarativeExplorerView.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSUiReference.SortOrder;
import com.percussion.workbench.ui.actions.PSMainActionGroup;
import com.percussion.workbench.ui.help.IPSHelpProvider;
import com.percussion.workbench.ui.help.PSHelpManager;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.part.ViewPart;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Renders a hierarchical view of nodes as defined in a declarative way in
 * xml that conforms to the viewHierarchyDef.xsd schema. The derived class
 * must provide this content. This class then handles everything else.
 * 
 * @version 6.0
 * @author paulhoward
 */
public abstract class PSDeclarativeExplorerView extends ViewPart
   implements IPSHelpProvider 
{
   /**
    * This class is only meant to be derived.
    */
   protected PSDeclarativeExplorerView()
   {
   }

   /**
    * This sorter works like Window's explorer. Folders are grouped together and
    * they appear at the top or bottom depending on the specified sort order.
    * The sort order is obtained from the parent node of the nodes being sorted.
    * If the node doesn't have a parent, the special sorting property 
    * ({@link PSUiReference#ROOT_SORT_ORDER_PROPNAME}) is used.
    * 
    * @author paulhoward
    */
   private class DeclarativeSorter extends ViewerSorter
   {
      /**
       * Groups folders and files together.
       * 
       * @param element May be <code>null</code>. Assumed to be a
       * {@link PSUiReference}.
       * 
       * @return 0 if the supplied node is a folder, else 1.
       */
      @Override
      public int category(Object element)
      {
         if (element == null)
            return 1;
         PSUiReference node = (PSUiReference) element;
         if (node.getParentNode() == null)
            return 1;
         if (getSortOrder(node) == PSUiReference.SortOrder.ASCENDING)
         {
            return node.isFolder() ? 0 : 1;
         }
         else
            return node.isFolder() ? 1 : 0;
      }

      /**
       * Orders the 2 nodes depending on the sort order attribute of the parent
       * node.
       */
      @Override
      public int compare(Viewer viewer, Object e1, Object e2)
      {
         int comp = super.compare(viewer, e1, e2);
         PSUiReference node = (PSUiReference) e1;
         if (node.getParentNode() == null)
            return comp;
         if (node.getParentNode().getSortOrder() 
               == PSUiReference.SortOrder.ASCENDING)
         {
            return comp;
         }
         else //descending sort, so reverse the comparison operation
            return comp==1 ? -1 : (comp==-1 ? 1 : 0);
      }

      /**
       * Skips sorting if the parent of these elements has its sort order
       * set to none.
       */
      @Override
      public void sort(Viewer viewer, Object[] elements)
      {
         if (elements == null || elements.length == 0)
            return;
         PSUiReference node = (PSUiReference) elements[0];
         SortOrder sorting = getSortOrder(node);
         if (sorting == SortOrder.NONE)
            return;
         super.sort(viewer, elements);
      }
      
      /**
       * Finds the sort order that should be applied to the specified node,
       * taking into account special cases such as the root node. 
       * 
       * @param node Assumed not <code>null</code>.
       * 
       * @return Never <code>null</code>.
       */
      private SortOrder getSortOrder(PSUiReference node)
      {
         if (node.getParentNode() != null)
         {
            return node.getParentNode().getSortOrder();
         }
         else
         {
            Object o = node
               .getProperty(PSUiReference.ROOT_SORT_ORDER_PROPNAME);
            if (o instanceof SortOrder)
            {
               return (SortOrder) o; 
            }
            else
               //default
               return SortOrder.ASCENDING;
         }
      }
   }
   
   /**
    * This method is provided so derived classes can override the default
    * content provider, which is {@link PSDeclarativeHierarchyContentProvider}.
    * 
    * @return Never <code>null</code>.
    */
   protected PSDeclarativeHierarchyContentProvider getContentProvider()
   {
      return new PSDeclarativeHierarchyContentProvider(getRootName());
   }
   
   /**
    * 
    * @param parent
    */
   public void createPartControl(Composite parent)
   {
      m_viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL
            | SWT.V_SCROLL);
      m_viewer.setSorter(new DeclarativeSorter());
      getViewSite().setSelectionProvider(m_viewer);
//         drillDownAdapter = new DrillDownAdapter(viewer);
      PSDeclarativeHierarchyContentProvider cp = getContentProvider(); 

      m_viewer.setContentProvider(cp);

      initContextMenu();
      
      getSite().setSelectionProvider(m_viewer);
      
      makeActions();

      m_viewer.addDoubleClickListener(new IDoubleClickListener()
      {
         public void doubleClick(DoubleClickEvent event)
         {
            m_actionGroup.handleDoubleClick(event);
         }
      });

      m_viewer.addOpenListener(new IOpenListener()
      {
         public void open(OpenEvent event)
         {
            m_actionGroup.handleOpen(event);
         }
      });
      
      //todo - add appropriate decorator(s) - e.g. reference
      ILabelProvider lp = new LabelProvider()
      {
         @Override
         public String getText(Object o)
         {
            if (o instanceof PSUiReference)
            {
               return ((PSUiReference)o).getDisplayLabel();
            }
            return o.toString();
         }

         @Override
         public Image getImage(Object element)
         {
            PSUiReference node = (PSUiReference) element;
            ISharedImages mgr = PlatformUI.getWorkbench().getSharedImages();

            if (node.getHandler() != null)
            {
               return node.getHandler().getLabelImage(node);
            }
            return mgr.getImage(node.isFolder()
                  ? ISharedImages.IMG_OBJ_FOLDER
                  : ISharedImages.IMG_OBJ_FILE); 
         }
         
      };
      ILabelDecorator decorator = PlatformUI.getWorkbench().
            getDecoratorManager().getLabelDecorator();
      m_viewer.setLabelProvider(
            new DecoratingLabelProvider(lp, decorator));

      initializeDragAndDrop(m_viewer);
      try
      {
         m_viewer.setInput(PSDesignObjectHierarchy.getInstance());
      }
      catch (Exception e)
      {
         //fixme handle this properly
         System.out.println(e);
      }
      m_helpManager = new PSHelpManager(this, m_viewer.getTree());
   }

   /**
    * Configures the provided viewer with the dispatching drag and drop
    * handlers.
    * 
    * @param viewer Assumed not <code>null</code>.
    */
   private void initializeDragAndDrop(TreeViewer viewer)
   {
      //all ops that may be used need to be here
      int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
      Transfer[] supportedTypes = new Transfer[] { TextTransfer.getInstance(),
            FileTransfer.getInstance() };
      viewer.addDragSupport(ops, supportedTypes, new DragSourceListener()
      {
         //see interface
         public void dragFinished(DragSourceEvent event)
         {
            if (mi_dispatchListener != null)
            {
               DragSourceListener l = mi_dispatchListener;
               mi_dispatchListener = null;               
               l.dragFinished(event);
            }
         }

         //see interface
         public void dragSetData(DragSourceEvent event)
         {
            ms_logger.debug("drag set data");
            if (mi_dispatchListener != null)
            {
               mi_dispatchListener.dragSetData(event);
            }
         }

         //see interface
         public void dragStart(DragSourceEvent event)
         {
            ms_logger.debug("drag start");
            ISelection sel = m_viewer.getSelection();
            if (!(sel instanceof IStructuredSelection))
            {
               event.doit = false;
               return;
            }
            IStructuredSelection ssel = (IStructuredSelection) sel;
            DragSourceListener targetHandler = null;
            IPSDeclarativeNodeHandler nodeHandler = null;
            Collection<PSUiReference> nodes = new ArrayList<PSUiReference>();
            //all elements in selection must have the same dnd handler
            for (Object o : ssel.toList())
            {
               if (o instanceof PSUiReference)
               {
                  PSUiReference node = (PSUiReference) o;
                  nodes.add(node);
                  nodeHandler = node.getHandler();
                  if (nodeHandler != null && nodeHandler.getDragHandler() != null)
                  {
                     DragSourceListener dndListener = nodeHandler
                           .getDragHandler();
                     if (node.getObjectType() != null)
                     {
                        if (targetHandler == null)
                        {
                           targetHandler = dndListener;
                           continue;
                        }
                        else if (targetHandler.equals(dndListener))
                           continue;
                     }
                  }
               }
               event.doit = false;
               return;
            }
            
            /* it isn't clearly documented that the source is a DragSource, but
             * we need it to support our dynamic transfer types. If it isn't,
             * the cast will fail.
             */
            DragSource source = (DragSource) event.getSource();
            source.setTransfer(nodeHandler.getSourceTransfers(nodes));
            mi_dispatchListener = targetHandler;
            mi_dispatchListener.dragStart(event);
         }
         
         /**
          * This is the listener that performs the actual work during dnd
          * operations. Set in the {@link #dragStart(DragSourceEvent)}, cleared
          * in {@link #dragFinished(DragSourceEvent)}.
          */
         private DragSourceListener mi_dispatchListener = null;
         
      });
      ViewerDropAdapter dropHandler = new DropAdapter(m_viewer);
      /* disable the 'before' and 'after' effects because these views don't 
       * support ordering
       */
      dropHandler.setFeedbackEnabled(false);
      viewer.addDropSupport(ops, supportedTypes, dropHandler);
   }

   /**
    * This class delegates the real work to the DND handler associated with the
    * drop target. If there is no handler, no drop is allowed.
    *
    * @author paulhoward
    */
   private class DropAdapter extends ViewerDropAdapter
   {
      /**
       * The only ctor. 
       * 
       * @param viewer The control for which this adapter is being used. Never
       * <code>null</code>.
       */
      public DropAdapter(Viewer viewer)
      {
         super(viewer);
         if ( null == viewer)
         {
            throw new IllegalArgumentException("viewer cannot be null");  
         }
      }

      /**
       * @inheritDoc
       * Finds the DND handler for the current node and delegates to it. 
       */
      @Override
      public boolean performDrop(Object data)
      {
         Object target = getCurrentTarget();
         if (target != null && target instanceof PSUiReference)
         {
            // we don't use getCurrentOperation() due to a bug, see description
            // of m_lastTarget
            int op = target.equals(m_lastTarget) ? m_lastValidOp : DND.DROP_NONE;
            PSUiReference node = (PSUiReference) target;
            if (node.getHandler() != null
                  && node.getHandler().getDropHandler() != null)
            {
               return node.getHandler().getDropHandler().performDrop(
                     (PSUiReference) target, op, data);
            }
         }
         return false;
      }

      /**
       * Gets the node handler from the target and checks the supplied
       * <code>transferType</code> against all allowed types from the handler.
       * See the
       * {@link ViewerDropAdapter#validateDrop(Object, int, TransferData) base class}
       * for description of params.
       */
      @Override
      public boolean validateDrop(Object target, int operation,
            TransferData transferData)
      {
         if (target != null && target instanceof PSUiReference)
         {
            PSUiReference node = (PSUiReference) target;

            if (node.getHandler() != null
                  && node.getHandler().getDropHandler() != null)
            {
               Transfer[] allowedTypes = node.getHandler()
                     .getAcceptedTransfers();
               
               for (Transfer t : allowedTypes)
               {
                  if (t.isSupportedType(transferData))
                  {
                     if (node.getHandler().getDropHandler().getValidDndOperation(
                           operation) == operation)
                     {
                        m_lastTarget = target;
                        m_lastValidOp = operation;
                     }
                     return node.getHandler().getDropHandler().validateDrop(
                           node, operation, transferData);
                  }
               }
            }
         }
         return false;
      }

      /**
       * @inheritDoc
       * Overridden to make the feedback cursor conform to our dynamic desires.
       */
      @Override
      public void dragOperationChanged(DropTargetEvent event)
      {
         int before = event.detail;
         fixOperation(event);
         super.dragOperationChanged(event);
         if (ms_logger.isDebugEnabled())
         {
            ms_logger.debug("Drag op: before - " + before + ": after - "
                  + event.detail);
         }
      }

      /**
       * @inheritDoc
       * Overridden to implement support for a workaround for a bug in the base
       * class.
       */
      @Override
      public void dragEnter(DropTargetEvent event)
      {
         m_lastTarget = null;
         m_lastValidOp = DND.DROP_NONE;
         super.dragEnter(event);
      }

      /**
       * @inheritDoc
       * Overridden to make the feedback cursor conform to our dynamic desires.
       */
      @Override
      public void dragOver(DropTargetEvent event)
      {
         fixOperation(event);
         super.dragOver(event);
      }

      /**
       * Gets the node handler from the current target and uses it to validate
       * the current operation. For example, some nodes only allow a link, not
       * a copy or move.
       * 
       * @param event Assumed not <code>null</code>. The <code>detail</code>
       * value may be changed after this operation returns.
       */
      private void fixOperation(DropTargetEvent event)
      {
         Object target = getCurrentTarget();
         if (target != null && target instanceof PSUiReference)
         {
            PSUiReference node = (PSUiReference) target;
            if (node.getHandler() != null
                  && node.getHandler().getDropHandler() != null)
            {
               event.detail = node.getHandler().getDropHandler()
                     .getValidDndOperation(event.detail);
            }
         }
      }

      /**
       * There is a bug in the base class that causes the wrong DND operation 
       * to be sent to the <code>performDrop</code> method in some cases. This
       * member, in conjunction with {@link #m_lastValidOp} are used to work
       * around that bug by performing the op tracking ourselves.
       * <p>
       * Cleared by <code>dragEnter</code>, set by <code>validateDrop</code>.
       */
      private Object m_lastTarget = null;
      
      /**
       * The operation that was last valid on {@link #m_lastTarget}. See 
       * {@link #m_lastTarget} for more details.
       */
      private int m_lastValidOp = DND.DROP_NONE;
   }

   /**
    * 
    * @param site
    * @param memento
    * @throws PartInitException 
    */
   public void init(IViewSite site, IMemento memento) 
      throws PartInitException
   {
      super.init(site);
      //fixme restore state
      System.out.println("restoring state");
   }

   /**
    * 
    * @param memento
    */
   public void saveState(IMemento memento)
   {
      //fixme save state
      System.out.println("saving state");
   }

   /**
    * Derived classes must override this method. It will be called during the
    * {@link #createPartControl(Composite)} processing.
    * 
    * @return The path of a tree fragment within the UI model presented by
    * {@link com.percussion.workbench.ui.model.PSDesignObjectHierarchy}. Never
    * <code>null</code> or empty.
    */
   protected abstract String getRootName();

   /**
    * Called when the context menu is about to open. Adds all menu options
    * appropriate for the current selection.
    */
   protected void fillContextMenu(IMenuManager menu)
   {
      IStructuredSelection selection = (IStructuredSelection) m_viewer
            .getSelection();
      m_actionGroup.setContext(new ActionContext(selection));
      m_actionGroup.fillContextMenu(menu);
      if (System.getProperty("com.percussion.dev") != null)
      {
         menu.add(new Action()
         {
            @Override
            public void run()
            {
               PSDesignObjectHierarchy.getInstance().dumpCache();
            }
   
            @Override
            public String getText()
            {
               return "Dump View's Design Object Cache";
            }
         });
      }
   }

   /**
    * Creates the action group, which encapsulates all actions for the view.
    */
   protected void makeActions()
   {
      m_actionGroup = new PSMainActionGroup(getViewSite(), m_viewer);
   }

   /**
    * Initializes and registers the context menu.
    * 
    * @since 2.0
    */
   protected void initContextMenu()
   {
      MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
      menuMgr.setRemoveAllWhenShown(true);
      menuMgr.addMenuListener(new IMenuListener()
      {
         public void menuAboutToShow(IMenuManager manager)
         {
            fillContextMenu(manager);
         }
      });
      Menu menu = menuMgr.createContextMenu(m_viewer.getTree());
      m_viewer.getTree().setMenu(menu);
      getSite().registerContextMenu(menuMgr, m_viewer);
   }
   
//   /**
//    * 
//    * @param site
//    */
//   public void init(IViewSite site)
//      throws PartInitException
//   {
//      super.init(site);
//   }
//
//   public IViewSite getViewSite()
//   {
//      return super.getViewSite();
//   }
//
   /**
    * Sets focus on the tree control.
    */
   @Override public void setFocus()
   {
      m_viewer.getControl().setFocus();
   }
      
   /**
    * Base class implementation just returns name of the class. 
    * @see com.percussion.workbench.ui.help.IPSHelpProvider#
    * getHelpKey(org.eclipse.swt.widgets.Control)
    */
   public String getHelpKey(Control control)
   {      
      return getClass().getName();
   }

   /**
    * This is the main control that presents the tree to the user. Set in
    * {@link #createPartControl(Composite)}, then never <code>null</code>.
    */
   private TreeViewer m_viewer;
   
   /**
    * Contains all actions used by items in this tree. 
    */
   private PSMainActionGroup m_actionGroup;
   
   /**
    * The help manager for the view
    */
   protected PSHelpManager m_helpManager;
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Log ms_logger = LogFactory
         .getLog(PSDeclarativeExplorerView.class);
}
