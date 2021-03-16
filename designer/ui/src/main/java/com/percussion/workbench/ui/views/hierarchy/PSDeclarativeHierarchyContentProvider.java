/******************************************************************************
 *
 * [ PSDeclarativeHierarchyContentProvider.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.views.hierarchy;

import com.percussion.client.PSModelException;
import com.percussion.workbench.ui.PSUiReference;
import com.percussion.workbench.ui.PSWorkbenchPlugin;
import com.percussion.workbench.ui.model.IPSHierarchyChangeListener;
import com.percussion.workbench.ui.model.PSDesignObjectHierarchy;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import java.text.MessageFormat;
import java.util.*;

/**
 * This class uses a {@link PSDesignObjectHierarchy} model as the source of its
 * content. (It uses a fragment of this model as specified in the ctor.) The
 * model is supplied in the {@link #inputChanged(Viewer, Object, Object)}
 * method. As calls to <code>getChildren</code> are made, they are passed on
 * to the model.
 * 
 * This class registers for certain changes in model objects and updates the
 * viewer supplied in the {@link #inputChanged(Viewer, Object, Object)} method
 * as the changes occur. The supported changes are create, delete and modify.
 * 
 * @version 6.0
 * @author paulhoward
 */
public class PSDeclarativeHierarchyContentProvider implements
   ITreeContentProvider, IPSHierarchyChangeListener
{
   /**
    * The only ctor. 
    * 
    * @param path Must be appropriate as a parameter to
    * {@link PSDesignObjectHierarchy#getChildren(String)}. Never
    * <code>null</code> or empty.
    */
   public PSDeclarativeHierarchyContentProvider(String path)
   {
      if (StringUtils.isBlank(path))
      {
         throw new IllegalArgumentException("path cannot be null or empty");
      }
      m_defName = path;
      if (m_defName == null)
         throw new IllegalArgumentException(path + " is not a valid root name");  
   }
   
   //see IPSHierarchyChangeListener
   public void changeOccurred(final HierarchyChangeType type,
         final PSUiReference[] nodes, final PSUiReference[] parents)
   {
      if (null == type)
      {
         throw new IllegalArgumentException("type cannot be null");  
      }
      if (isIgnoreChangeEvents())
         return;
      final Display display = Display.getDefault();
      if (getLogger().isDebugEnabled())
      {
         String pattern = "Change occurred for {0}: {1} - {2}";
         StringBuffer nameBuf = new StringBuffer();
         if (nodes != null)
         {
            for (PSUiReference node : nodes)
               nameBuf.append(node.getName() + ", ");
         }
         getLogger().debug(MessageFormat.format(pattern, getName(), type
               .toString(), nameBuf.toString()));
      }
      
      
      if (type == HierarchyChangeType.NODE_MOVED)
      {
         display.syncExec(new Runnable()
         {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
               Set<String> paths = new HashSet<String>();
               for (int i = 0; i < nodes.length; i++)
               {
                  String parentPath = parents[i].getPath();
                  if (!paths.contains(parentPath))
                  {
                     paths.add(parentPath);
                     getViewer().refresh(parents[i]);
                  }
                  getViewer().add(nodes[i].getParentNode(), nodes[i]);   
               }
            }
         });            
      }
      if (type == HierarchyChangeType.NODE_CREATED)
      {
         display.syncExec(new Runnable()
         {
            
            @SuppressWarnings("synthetic-access")
            public void run()
            {
               for (PSUiReference node : nodes)
               {
                  getViewer().add(node.getParentNode(), node);   
               }
            }
            
         });            
      }
      else if (type == HierarchyChangeType.NODE_DELETED)
      {
         getLogger().debug(
            "(Path may not be correct if immediately followed by a create.)");
        
         try
         {
            m_disableCataloging = true;
            display.syncExec(new Runnable()
            {
               @SuppressWarnings("synthetic-access")
               public void run()
               {
                  getViewer().remove(nodes);
               }            
            }); 
         }
         finally
         {
            m_disableCataloging = false;
         }
      }
      else if (type == HierarchyChangeType.NODE_CHILDREN_MODIFIED
            || type == HierarchyChangeType.NODE_MODIFIED)
      {
         /*
          * we have to do a refresh for modified because sub-nodes may depend
          * on the content of the node that has changed (generally links)
          */
         display.syncExec(new Runnable()
         {
            
            @SuppressWarnings("synthetic-access")
            public void run()
            {
               for (PSUiReference node : nodes)
               {                     
                  getViewer().refresh(node);
               }
            }            
         }); 
         
      }
      else if (type == HierarchyChangeType.MODEL_ACTIVATED
            || type == HierarchyChangeType.MODEL_DEACTIVATED)
      {
         /*
          * need to remove all nodes
          */
         display.syncExec(new Runnable()
         {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
               getViewer().refresh();
            }            
         }); 
         
      }      
   }

   /**
    * Returns the name supplied in the ctor.
    * 
    * @return Never <code>null</code> or empty.
    */
   protected String getName()
   {
      return m_defName;
   }
   
   /**
    * This flag is set by the base class while children are cataloged to prevent
    * processing events during this time. Derived classes may need this if they
    * are implementing their own content provider.
    * 
    * @return <code>true</code> if change events from the core should be
    * ignored, <code>false</code> if they should be processed (i.e. they weren't
    * caused by our own processing.)
    */
   protected boolean isIgnoreChangeEvents()
   {
      return m_ignoreChangeEvents;
   }
   
   /**
    * Returns the viewer set by the {@link #inputChanged(Viewer, Object, Object)}
    * method.
    * 
    * @return May be <code>null</code>.
    */
   protected TreeViewer getViewer()
   {
      return m_viewer;
   }
   
   /**
    * Returns the logger for this class for use by derived classes.
    * 
    * @return Never <code>null</code>.
    */
   static protected Logger getLogger()
   {
      return ms_logger;
   }
   
   //see ITreeContentProvider interface
   public Object[] getChildren(Object node)
   {
      if (m_disableCataloging)
         return new Object[0];
      
      PSUiReference parent = null;
      try
      {
         m_ignoreChangeEvents = true;
         parent = (PSUiReference) node;
         List<PSUiReference> results = node == null
               ? m_model.getChildren(m_defName)
               : m_model.getChildren(parent);
         addDndTypes(getViewer(), results);
         return toArray(results);
      }
      catch (PSModelException e)
      {
         return generateResultsForException(parent, e);
      }
      catch (Exception e)
      {
         return generateResultsForException(parent, e);
      }
      finally
      {
         m_ignoreChangeEvents = false;
      }
   }

   /**
    * This method is part of a hack. We want to use the DND stuff provided by
    * the viewer, but transfer types are set up front. And only objects that
    * meet the transfer types set will ever call our code. The problem is we
    * use dynamically generated transfer objects, and we don't know all of them
    * ahead of time. However, we do find them as the tree is expanded. Thus,
    * this method adds newly found ones as the tree is expanded so that the
    * drop handler will pass thru to our code any possible type we might be 
    * interested in. Our code then further checks if the current transfer type
    * is appropriate for the current selection. 
    * <p>
    * The biggest hack part is we are using a property that the DropTarget
    * class sets on the control whose name is not publicly exposed.
    * 
    * @param viewer Assumed not <code>null</code>.
    * 
    * @param results Assumed not <code>null</code>.
    */
   private void addDndTypes(TreeViewer viewer, List<PSUiReference> results)
   {
      //todo - hack code - OK for release
      Object o = viewer.getControl().getData("DropTarget");
      if (o == null || !(o instanceof DropTarget))
      {
         String title = "Incompatible Versions";
         String msg = 
            "This version of the workbench is not compatible with the version "
            + "of SWT visible to it. Drag and drop will not work correctly.";
         PSWorkbenchPlugin.displayWarning("DnD", title, msg, (Throwable[]) null);
         return;
      }
      
      DropTarget dt = (DropTarget) o;
      Transfer[] currentTransfers = dt.getTransfer();
      Set<Transfer> foundTransfers = new HashSet<Transfer>();
      foundTransfers.addAll(Arrays.asList(currentTransfers));
      boolean found = false;
      for (PSUiReference node : results)
      {
         if (node.getHandler() == null)
         {
            continue;
         }
         Transfer[] allowedTypes = node.getHandler().getAcceptedTransfers();
         for (Transfer t : allowedTypes)
         {
            if (!foundTransfers.contains(t))
            {
               foundTransfers.add(t);
               found = true;
            }
         }
      }
      if (found)
      {
         getLogger().debug("Resetting drop transfer list");
         dt.setTransfer(foundTransfers.toArray(new Transfer[foundTransfers
               .size()]));
      }
   }

   /**
    * Logs the exception and creates a node whose name is the exception text.
    * 
    * @param parent Assumed not <code>null</code>.
    * @param e Assumed not <code>null</code>.
    * @return A new array w/ the created node. The caller takes ownership.
    */
   private PSUiReference[] generateResultsForException(PSUiReference parent,
         Exception e)
   {
      final List<PSUiReference> results = new ArrayList<PSUiReference>();
      getLogger().error(
            parent == null
               ? "Catalogin root node"
               : "Cataloging node " + parent.getPath(), e);
      String desc = 
         e.getCause() != null ? e.getCause().getLocalizedMessage() : "";
      results.add(new PSUiReference(parent, "<Error occurred>: "
            + e.getLocalizedMessage(), desc, e, null, false));
      return toArray(results);
   }

   private PSUiReference[] toArray(List<PSUiReference> results)
   {
      return results.toArray(new PSUiReference[results.size()]);
   }

   //see ITreeContentProvider interface
   public Object[] getElements(@SuppressWarnings("unused") Object parent)
   {
      return getChildren(null);
   }

   //see ITreeContentProvider interface
   public Object getParent(Object child)
   {
      return ((PSUiReference) child).getParentNode();
   }

   //see ITreeContentProvider interface
   public boolean hasChildren(Object node)
   {
      if (node == null)
         return true;
      PSUiReference ref = (PSUiReference) node;
      return ref.isFolder() || ref.getCatalogFactory() != null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.IContentProvider#dispose()
    */
   public void dispose()
   {
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(
    * org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
    */
   public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
   {
      System.out.println("inputChanged");
      PSDesignObjectHierarchy oldModel = (PSDesignObjectHierarchy) oldInput;
      if (oldModel != null)
         oldModel.removeListener(this, m_defName);
      m_model = (PSDesignObjectHierarchy) newInput;
      if (m_model != null)
         m_model.addListener(this, m_defName);
      m_viewer = (TreeViewer) viewer;
   }

   /**
    * Contains the model that is the source for this content provider. Set in
    * {@link #inputChanged(Viewer, Object, Object)}.
    */
   private PSDesignObjectHierarchy m_model;

   /**
    * The UI widget that uses the data supplied by this provider. Used to
    * process changes that occur after a node has been cataloged. Never
    * <code>null</code>.
    */
   private TreeViewer m_viewer;

   /**
    * The name of the hierarchy presented by this provider. Never
    * <code>null</code> or empty after ctor.
    */
   private final String m_defName;

   /**
    * This flag is used to temporarily turn off the cataloging functionality.
    * This is here to work around a behavior in the viewer. Here's the scenario:
    * Say a node is empty. Then dnd a node into this folder. A NODE_DELETED
    * message is sent. This causes us to call remove() on the viewer. While
    * processing this method, it also catalogs the currently selected node,
    * which happens to be the node we just dropped on. The catalog returns
    * the new node as it has already been moved. We then receive a NODE_CREATED
    * message, which causes us to call add() on the viewer. This adds the same
    * node that was just cataloged with the previous message, resulting in 2
    * identical entries. This only happens if the folder is empty.
    */
   private boolean m_disableCataloging = false;

   /**
    * This flag is used during cataloging. We want to ignore create events
    * when processing the cataloging method.
    */
   private boolean m_ignoreChangeEvents = false;
   
   /**
    * The logging target for all instances of this class. Never
    * <code>null</code>.
    */
   private static Logger ms_logger = LogManager
         .getLogger(PSDeclarativeHierarchyContentProvider.class);
}
