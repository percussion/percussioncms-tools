/******************************************************************************
 *
 * [ DefaultBrowserNode.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.UIMainFrame;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;


/**
 * This class extends the tree node by adding an interface to support a context
 * sensitive menu, vetoable, In-place (IP) editing and drag and drop.
 * All operations default to unsupported in this class. It is expected classes
 * will be derived to add implementations for the various operations.
 * <p>
 * The interface defines methods called by the tree to determine supported
 * functionality and to activate that functionality on behalf of the designer.
 * If any method is
 * called other than the accessor methods, a DebugException will be thrown
 * with a message indicating the problem. This will only happen if a derived
 * class overrides an accessor, but forgets to override the mutator or action
 * method.
 * <p>
 * The interface defined by this class is designed to be used by the
 * BrowserTree class. This class expects a user object that implements the
 * ICatalogEntry interface.
 *
 * @see BrowserTree
 */
public class DefaultBrowserNode extends JTree.DynamicUtilTreeNode
{
   // constructors
   public DefaultBrowserNode( Object userObject )
   {
      super( userObject, new Vector() );
    if(userObject != null && (userObject instanceof ICatalogEntry))
    {
      m_CatalogEntry = (ICatalogEntry)userObject;
    }
   }

   /**
    * Returns <code>true</code> if the displayed name does not match the real
    * name. For example, this may happen if the node name is the empty string.
    * This information is obtained from the user object via its ICatalogEntry
    * interface.
    */
   public boolean displaysInternalName( )
   {
       return(m_CatalogEntry.hasDisplayNameDifferentThanInternalName() == false);
   }

   /**
    * Returns displayed name if <code>displaysInternalName</code> returns
    * <code>false</code>, otherwise returns the internal ('real') name.
    * This information is obtained from the user object via its ICatalogEntry
    * interface.
    */
   public String getInternalName( )
   {
     return m_CatalogEntry.getInternalName();
   }

   /**
    * In-place editing support. If isRenamable returns <code>true</code>, then
    * in-place editing is allowed. After the user accepts the changed name,
    * checkName is called. If this returns null, then the
    * editing operation is allowed to complete and setName is called with the
    * new name. If checkName returns a non-empty string, this string is
    * presented to the user and the editing operation is not allowed to complete.
    * The returned string should indicate to the user what is wrong with the
    * entered text.
    */
   public boolean isRenamable( )
   {
      return false;
   }
   
   /**
    * Used to rename the object
    */
   public boolean Rename(@SuppressWarnings("unused") TreePath path)
   {
      return false;
   }
      

  protected boolean isRemovable()
  {
    return false;
  }

   protected boolean isSubset()
   {
      return false;
   }

   public String checkName(@SuppressWarnings("unused") String strName)
   {
      // for debugging
      if (isRenamable())
         throw new UnsupportedOperationException( "You forgot to implement checkName." );
      return null;
   }

   public void setInternalName( String strNewName )
   {
    m_CatalogEntry.setInternalName(strNewName);
   }

  protected void setDisplayName( String strNewName )
   {
      m_CatalogEntry.setDisplayName(strNewName);
   }

  protected String getDisplayName( )
   {
      return m_CatalogEntry.getDisplayName();
   }

   public boolean hasMenu( )
   {
      return false;
   }

   public JPopupMenu getContextMenu( )
   {
      // for debugging
      if (hasMenu( ))
         throw new UnsupportedOperationException( "You forgot to implement getContextMenu." );
      return null;
   }

   /**
    * Drag and drop support. If the user initiates a drag and drop action
    * on an entry and the object implementing this interface returns <code>
    * true</code> when isDraggable is called, then doDragDrop will be
    * subsequently called.
    */
   public boolean isDraggable( )
   {
      return false;
   }

   public Transferable getDragDropObject( )
   {
      // for debugging
      if (isDraggable( ))
         throw new UnsupportedOperationException( "You forgot to implement getDragDropObject." );
      return null;
   }

   /**
    * Returns <code>true</code> if this node will dynically create its children
    * when appropriate.
    */
   public boolean isDynamicEnum( )
   {
      return m_bDynamicEnum;
   }


   /**
    * Creates the proper kind of node for each child. It does this by using the
    * Server Node's HierarchyConstraints object and calling the
    * HierarchyConstraints object's getChildren method.
    * 
    * @see IHierarchyConstraints
    */
   protected void loadChildren()
   {

      if (getAllowsChildren() == false)
      {
         return;
      }

      // get the server node by getting the branch of the tree
      // the array contains all nodes upto this node including the root .
      // Note: the Server node is either the root or the first child of the root
      TreeNode[] naNodes = this.getPath();
      if (naNodes == null)
         return;

      ServerNode servNode = null;
      if (naNodes[0] instanceof ServerNode)
         servNode = (ServerNode) naNodes[0];
      else if (naNodes.length > 1 && naNodes[1] instanceof ServerNode)
         servNode = (ServerNode) naNodes[1];

      if (servNode == null)
         return;

      if (servNode.getConstraints() == null)
         return;

      // set the cursor to wait cursor when loading children
      E2Designer e2designer = E2Designer.getApp();
      UIMainFrame uiMF = e2designer.getMainFrame();

      uiMF.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (!loadedChildren || isDynamicEnum())
      {
         m_vChildren = servNode.getConstraints().getChildren(this);

         if (m_vChildren != null && !m_vChildren.isEmpty())
         {
            if (sorting())
               this.sortNodes(m_vChildren);

            Enumeration e = m_vChildren.elements();
            int i = 0;
            while (e.hasMoreElements())
            {
               /*
                * add the children nodes WARNING: do not use add() method, as
                * this calls getChildCount which in turn calls loadChildren => 
                * infinite loop
                */
               this.insert((DefaultBrowserNode) (e.nextElement()), i);
               i++;
            }

         }
         loadedChildren = true;
         m_bDynamicEnum = false;
      }
      uiMF.setCursor(Cursor.getDefaultCursor());
   }



   /**
    * The very first time a dynamic node is expanded, it will create its children
    * at that point. Each successive expansion will use the same children until
    * this method is called. After this method is called, the next expansion
    * causes all the children to be recataloged.
    */
   public void resetDynamicEnum( )
   {
      m_bDynamicEnum = true;
   }

   /**
    * The catalog is used to dynamically enumerate the children for this node.
    *
    */
   public Iterator getCataloger( )
   {
      return m_Catalog;
   }

   /**
    * The catalog is used to dynamically enumerate the children for this node.
    * Each time the catalog is set, the current enumeration will be marked as
    * invalid and will be re-cataloged the next time the node is expanded.
    */
   public void setCataloger( Iterator catalog )
   {
    m_Catalog = catalog;
   }

   /**
    * The type is the node type defined in the Hierarchy Constraints objects.
    */
  public int getType( )
  {
    return m_CatalogEntry.getType();
  }

   /**
    * The type is the node type defined in the Hierarchy Constraints objects.
    *
    *@see IHierarchyConstraints
    */
  public void setType(int iType)
  {
    m_CatalogEntry.setType(iType);
  }

   /**
    * Returns the is the data Object stored in the ICatalogEntry object.
    *
    *@see ICatalogEntry
    */
  public Object getData()
  {
    return m_CatalogEntry.getData();
  }


   public boolean isLoadedChildren()
   {
      return loadedChildren;
   }

   public void setLoadedChildren(boolean bLoaded)
   {
      loadedChildren = bLoaded;
   }

   // sort case insensitive using default locale
   // vector must contain nodes that are or derived from DefaultBrowserNode
   public Vector sortNodes(Vector v)
   {
      if(v == null)
         return null;

      boolean bCanSort = true;
      Vector<DefaultBrowserNode> sorted = new Vector<DefaultBrowserNode>(
         v.size());
      for(int i =0; i<v.size(); i++)
      {
         if(!(v.elementAt(i) instanceof DefaultBrowserNode))
         {
            bCanSort = false;
            break;
         }
         else
            sorted.add((DefaultBrowserNode)v.elementAt(i));
      }

      if(bCanSort == false)
      {
         System.out.println("Can not sort vector of nodes");
         return v;                        // skip sorting return as is
      }
      
      // use optional comparator if specified
      if (m_comparator != null)
      {
         Collections.sort(sorted, m_comparator);
      }
      else
      {
         // Compare the internal names using the default locale
         Collator c = Collator.getInstance();
         // case insensitive comparison
         c.setStrength(Collator.SECONDARY);     
         for ( int i = 0; i < sorted.size(); i++)
         {
            int min = i;
            for (int j=i; j < v.size(); j++)
            {
               String sj = sorted.elementAt(j).getDisplayName();
               String smin = sorted.elementAt(min).getDisplayName();
               if (c.compare(sj, smin) < 0)
                  min =j;
            }
            DefaultBrowserNode dbnTemp;
            dbnTemp = sorted.elementAt(i);
            sorted.set(i, sorted.elementAt(min));
            sorted.set(min, dbnTemp);
         }         
      }

      
      // since no one looks at the return, need to update v
      v.clear();
      v.addAll(sorted);
      
      return v;
   }


   public String getFullPathName()
   {
      return m_strFullName;   
   }
   
   public void setFullPathName(String strFull)
   {
      m_strFullName = strFull;   
   }
   
   /**
   * Allow implementor to dictate if we sort
   */
   public void disableSorting()
   {
      m_bSort = false;
   }
   
   public boolean sorting()
   {
      return(m_bSort);
   }

   public ImageIcon getIcon()
   {
      if(m_CatalogEntry != null)
         return m_CatalogEntry.getIcon();
      else
         return null;
   }

   public void setIcon(ImageIcon icon)
   {
      if(m_CatalogEntry != null)
         m_CatalogEntry.setIcon(icon);
   }
   
   /**
    * Sets a comparator to use in place of the default, ignored if 
    * {@link #sorting()} is <code>false</code>.
    * 
    * @param comparator The comparator, may be <code>null</code> to clear it.
    */
   public void setComparator(Comparator<DefaultBrowserNode> comparator)
   {
      m_comparator = comparator;
   }
   
   private String m_strFullName = null;
   // storage
  protected ICatalogEntry m_CatalogEntry = null;
   protected Iterator m_Catalog = null; // we no longer set the cataloger for a node therefore this should remain null
  protected Vector m_vChildren = null;
   protected boolean m_bDynamicEnum = false;
   protected boolean m_bSort = true;
   
   /**
    * Optional comparator, may be <code>null</code>.
    */
   private Comparator<DefaultBrowserNode> m_comparator = null;
}





