/*[ SecurityHierarchyConstraints.java ]****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import java.util.Vector;

/**
 * This is an implementation of the IHierarchyConstraints interface for the
 * Security browser tab.
 * <p>
 * Only the users within a security provider instance are dynamically cataloged.
 * In addition, since there may be many users, users are shown in a virtual
 * list.
 * @see IHierarchyConstraints
 */
public class SecurityHierarchyConstraints
      implements IHierarchyConstraints
{
   /*
    * Type identifiers for dynamic nodes and leaves of dynamic nodes.
    */
   public final int NT_PROVIDER_INSTANCE = 1;
   public final int NT_USERS =          2;

   /**
    * No reordering of levels is supported. Always throws an exception.
    *
    * @throws UnsupportedOperationException if called
    */
   public void setOrder( int [] newOrder )
   {
      throw new UnsupportedOperationException( );
   }

   /**
    * Returns an array of types sequenced in the order they will appear in the
    * browser when this constraint object is used. The types are of the form
    * NT_... (NodeType...).
    *
    */
   public int [] getOrder( )
   {
      int [] iaOrder = { NT_PROVIDER_INSTANCE, NT_USERS };
      return iaOrder;
   }

   /**
    * The type of the passed in node is used to determine what type of
    * cataloger to return. The only supported type is NT_PROVIDER_INSTANCE. If any
    * other type node is passed in, null is returned.
    */
/*   public Iterator getCataloger( DefaultBrowserNode node )
   {
      return null;
   } */
  /**
   * Creates and returns a vector of child nodes for the parent node passed in. This is done
   * by looking at the Constraints order and finding the next type of node in the order.
   * It also sets the appropriate cataloger for the child nodes created.
    */
  public Vector getChildren(DefaultBrowserNode nodeParent)
  {
    return null;
  }

}

