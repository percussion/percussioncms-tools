/*[ PSDefaultContentTree.java ]************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.selector;

import com.percussion.loader.IPSContentTree;
import com.percussion.loader.IPSContentTreeNode;

import java.util.Iterator;
import java.util.List;

/**
 * This is the default implementation of an {@link #IPSContentTree}.
 * This tree is passed during the <code>scan()</code> method of an 
 * {@link #IPSContentSelector}. The tree encapsulates a data model that
 * represents the scanned data source.
 * 
 * @see PSContentTreeModel
 */
public class PSDefaultContentTree implements IPSContentTree
{
   /**
    * Constructor
    * 
    * @param {@link #IPSContentTree} a data model for this tree
    *    Never <code>null</code>
    * 
    * @throws IllegalArgumentException if m is <code>null</code>
    */
   public PSDefaultContentTree(IPSContentTree m)
   {
      if (m == null)
         throw new IllegalArgumentException("model in tree cannot be null.");

      m_model = m;
   }
   
   /**
    * Public accessor method to get the {@link #IPSContentTree}
    * used.
    * 
    * @return {@link #IPSContentTree} the data model. Never <code>null</code>.
    */
   public IPSContentTree getModel()
   {
      return m_model;
   }
   
   /**
    * * @see {@link com.percussion.loader.IPSContentTree} 
    * interface for description.
    */
   public IPSContentTreeNode getChild(IPSContentTreeNode parent, int index)
   {
      if (parent == null || index < 0)
         throw new IllegalArgumentException
            ("parent node must not be null and index must be >= 0");
      
      return m_model.getChild(parent, index);
   }
   
   /**
    * * @see {@link com.percussion.loader.IPSContentTree} 
    * interface for description.
    */
   public int getChildCount(IPSContentTreeNode parent)
   {
      if (parent == null)
         throw new IllegalArgumentException("parent must not be null.");

      return m_model.getChildCount(parent);
   }
   
   /**
    * * @see {@link com.percussion.loader.IPSContentTree} 
    * interface for description.
    */
   public int getIndexOfChild(IPSContentTreeNode parent, 
      IPSContentTreeNode child)
   {
      if (child == null || parent == null)
         throw new IllegalArgumentException(
            "both parent and child must not be null.");

      return m_model.getIndexOfChild(child, parent);
   }
   
   /**
    * @see {@link com.percussion.loader.IPSContentTree} 
    * interface for description.
    */
   public Iterator getRoots()
   {
      return m_model.getRoots();
   }
   
   /**
    * * @see {@link com.percussion.loader.IPSContentTree} 
    * interface for description.
    */
   public boolean isLeaf(IPSContentTreeNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node must not be null.");

      return m_model.isLeaf(node);
   }

   // see {@link com.percussion.loader.IPSContentTree} for description
   public List getNodes()
   {
      return m_model.getNodes();
   }
   
   /**
    * The data model represents a dependency tree of 
    * {@link #PSItemContext} nodes. Never <code>null</code>.
    * This tree cannot exist without a model and the model
    * is passed in when constructing this object.
    */
   private IPSContentTree m_model;
}
