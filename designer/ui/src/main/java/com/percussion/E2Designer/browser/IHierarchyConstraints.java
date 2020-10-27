/*[ IHierarchyConstraints.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import java.util.Vector;

/**
 * Hierarchy constraints defines an interface for specifying the order in which
 * the levels in a tree will be ordered. This will allow the end user to easily
 * choose different ordering / display schemes that most fit his use of the
 * product. This interface is designed to be used with the E2 browser. It is not
 * a generic interface for arbitrarily re-ordering levels in a tree.
 * <p>
 * It requires that the tree be created dynamically. As the tree is created,
 * each child of the current node that is being expanded is passed to the
 * getCataloger method of this interface. The catalog that will enumerate the
 * next desired level for this hierarchy will be returned (based on the type
 * of the passed in node). Depending on the hierarchy, the order may be allowed
 * to be changed within limited constraints.
 */
public interface IHierarchyConstraints
{
   /**
    * Allows the user to re-order the tree levels. Only certain types of
    * re-ordering will be allowed. See the specific implementation to
    * determine if re-ordering is allowed and what different orders are
    * allowed.
    *
    * @throws UnsupportedOperationException if the implementation doesn't support
    * reordering
    */
   public void setOrder( int [] newOrder );

   /**
    * Returns an array of integers that represent the types of objects that
    * can be displayed in this tree. The first element in the array is the
    * type of the root element in the tree. The 2nd element in the array is
    * the type of the children of the root node, and so on. Each element in
    * the array must be a unique type within the array.
    * <p>
    * See each implementation for the types that are supported and the order
    * they are supported in.
    */
   public int [] getOrder( );

   /*
    * Returns some form of cataloger object as an iterator. The type of
    * cataloger will be appropriate for setting in the passed in node. When
    * used, the cataloger will return child nodes for the passed in node.
    * <p>
    * If the type of the passed in Node does not match one of the supported
    * types, null is returned.
    */
/*   public Iterator getCataloger( DefaultBrowserNode node );  */


     /**
   * Creates and returns a vector of child nodes for the parent node passed in. This is done
   * by looking at the Constraints order and finding the next type of node in the order.
   * It also sets the appropriate cataloger for the child nodes created.
    */
  public Vector getChildren(DefaultBrowserNode nodeParent);


}


