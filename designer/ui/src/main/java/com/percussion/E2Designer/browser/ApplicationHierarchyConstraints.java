/*[ ApplicationHierarchyConstraints.java ]*************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import javax.swing.tree.TreeNode;
import java.util.Iterator;
import java.util.Vector;

/**
 * This is an implementation of the IHierarchyConstraints interface for an
 * Application browser tab. The standard level is:
 * <p>   Server Application object types
 * <p>      Applications
 * <p>         App object types
 * <p>            Credential aliases
 * <p>              Roles
 * <p>                 Members
 * <p>              Transfers
 * <p>            Webpages
 * <p>
 * Only the children of Applications are dynamically cataloged. Application
 * objects were not cataloged for the following reason: app objects are cataloged
 * by loading the app (a slow process). Once the app is loaded, we have access
 * to all the objects it contains. Therefore, rather than loading the app for
 * each object type, we create the whole tree the first time we open the app.
 *
 * @see IHierarchyConstraints
 */
public class ApplicationHierarchyConstraints
      implements IHierarchyConstraints
{
   /*
    * Type identifiers for dynamic nodes and leaves of dynamic nodes.
    */
   public static final int NT_APPLICATION   =    1;
   public static final int NT_APPOBJ_TYPE   =      2;
   public static final int NT_APPOBJECTS      =    3;

   
   //the following are a subset of NT_APPOBJECTS
   public static final int NT_APPOBJECT_CREDALIAS = 4;
   public static final int NT_APPOBJECT_DATASET = 5;
   public static final int NT_APPOBJECT_ROLE = 6;

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
    */
   public int [] getOrder( )
   {
      int [] iaOrder = { NT_APPLICATION, NT_APPOBJ_TYPE, NT_APPOBJECTS };
      return iaOrder;
   }


   /*
    * The type of the passed in node is used to determine what type of
    * cataloger to return. The only supported type is NT_APPLICATION. If any
    * other type node is passed in, null is returned.
    */
/*   public Iterator getCataloger( DefaultBrowserNode node )
   {
      return null;
   } */

   
   /**
    * Gets the cataloger for the node passed in. The parent node is needed to get the branch of the tree.
   * The child's node type determines the type of cataloger to return. The cataloger returned
   * for the node is the cataloger for catalogging it's children.
    * The only supported types are NT_APPLICATION and NT_APPOBJ_TYPE. Parent node passed in must be of type
    * NT_APPLICATION or NT_APPOBJ_TYPE. If any other type node is passed in, null is returned.
    */
   public Iterator getCatalogerForNode(DefaultBrowserNode node)
   {
    if(node == null)
      return null;

    int iParentType = node.getType();
    int iChildType = getNextNodeType(iParentType);
      //the parent has the cataloger to catalog its children

    if(iChildType < 0 || iParentType < 0)
      return null;

    // get the application name
     //get the branch of the tree,  we will need this for getting internal names
     //the array contains all nodes upto the passed in node  including the root .
     //Note: the Server node is not the root, but the first child of the root
    TreeNode[] naNodes = node.getPath();
//    System.out.println("length of tree branch to Parent node ="+naNodes.length );

    //fill in the node names as we will need them for constructing the cataloger
    //the names start at the server node which is first child to root
    String [] straNodeNames = new String[naNodes.length-1];
    for(int i=0; i<straNodeNames.length; i++)
    {
      DefaultBrowserNode dbn = (DefaultBrowserNode)naNodes[i+1]; //skip the root
      straNodeNames[i] = dbn.getInternalName();
//      System.out.println("Array of Names " +straNodeNames[i]);
    }
//    straNodeNames[straNodeNames.length-1] = ""; // add the internal name for the child
//    System.out.println("Array of Names -- childNodeName" + straNodeNames[straNodeNames.length-1]);

//    int iNextType = getNextNodeType(iChildType);
//    System.out.println("Current Type = "+iChildType +" Next Type = "+iNextType);

    //application name is always needed
    String strApp = getInternalNameForNodeType(NT_APPLICATION, straNodeNames);

    switch (iChildType)
    {
      case NT_APPOBJ_TYPE:
        // AppObjectTypeCataloger constructor needs nothing
        AppObjectTypeCataloger appObjTypeCat = new AppObjectTypeCataloger();
        if(appObjTypeCat != null)
            {
//               System.out.println(" in ApplicationHierarcyConstraints   returning AppObjectTypeCataloger");
          return appObjTypeCat.iterator();
            }
        break;


      case NT_APPOBJECTS:
        // AppObjectCataloger constructor needs name of the Application
//        System.out.println(" in ApplicationHierarcyConstraints Application= "+strApp);

        AppObjectCataloger appObjCat = new AppObjectCataloger(strApp);
        if(appObjCat != null)
            {
//               System.out.println(" in ApplicationHierarcyConstraints   returning AppObjectCataloger");
          return appObjCat.iterator();
            }
        break;

     }
    return null;   // if everything went ok we should not get here
   }

   /**
   * @returns the index of this node after looking up it's Type in the SQLHierarchyConstraints
   * Order array. If not found, it will return -1.
    */
  public int getIndexOfNode(DefaultBrowserNode node)
  {
    if(node == null)
      return -1;
    int iType = node.getType();
    if(iType < 0)
      return -1;

    int [] iaOrder = getOrder();

    int index = -1;
    for(int i=0; i<iaOrder.length; i++)
    {
      if(iType == iaOrder[i])
      {
        index = i;
        break;
      }
    }
    return index;
  }

   /**
   * @returns the index of this node type  NT_ ... in the ApplicationHierarchyConstraints Order array. If the
   * if not found, it will return -1.
    */
  public int getIndexOfNodeType(int iType)
  {
    if(iType < 0)
      return -1;

    int [] iaOrder = getOrder();

    int index = -1;
    for(int i=0; i<iaOrder.length; i++)
    {
      if(iType == iaOrder[i])
      {
        index = i;
        break;
      }
    }
    return index;
  }

   /**
   * @returns the internal name of the Node Type passed in and the array of
   * internal names passed in. The array of names passed in is the same size as the
   * Order array for the SQLHierarchy Constraints and the names are in the same order.
   *
    */
  private String getInternalNameForNodeType(int iType, String [] arrayNodeNames)
  {
    if (arrayNodeNames == null ||  iType <= 0)
      return null;

    int i = getIndexOfNodeType(iType);
    if( i < 0 || i >= arrayNodeNames.length)
      return null;
    else
      return arrayNodeNames[i];
  }

   /**
   * @returns the next Node Type in the Hierarchy Constraints Order based on the
   * Node Type passed in.
   * <p>
   * Will return -1 if iType is the last one or in case of error.
    */
  public int getNextNodeType(int iType)
  {
    int [] iaOrder = getOrder();
    int iCurNodeIndex = getIndexOfNodeType(iType);
    if (iCurNodeIndex < 0)
      return -1;
    int iNextType = iCurNodeIndex < iaOrder.length-1 ? iaOrder[iCurNodeIndex+1] : -1;
    return iNextType;
  }

  
   /**
   * Creates and returns a vector of child nodes for the parent node passed in. This is done
   * by looking at the Constraints order and finding the next type of node in the order.
   * It also sets the appropriate cataloger for the child nodes created.
    * The only supported types are NT_APPLICATION and NT_APPOBJ_TYPE. Parent node passed in must be of type
    * NT_APPLICATION or NT_APPOBJ_TYPE. If any other type node is passed in, null is returned.
    */
  public Vector getChildren(DefaultBrowserNode nodeParent)
  {
    if(nodeParent == null)
      return null;
    int iParentType = nodeParent.getType();
//      System.out.println("parent node type = "+iParentType);

      String strParentName = nodeParent.getInternalName();


//      System.out.println("In AppHierarchyConstraints getChildren --- Parent node = "+nodeParent.getInternalName());
    Iterator parentCataloger = getCatalogerForNode(nodeParent);

    if(parentCataloger == null)
     throw new IllegalArgumentException("parent node's cataloger is null");

    Vector vChildren = new Vector(); //fill this vector with children and return it

    while(parentCataloger.hasNext())
    {
      ICatalogEntry entry = (ICatalogEntry)(parentCataloger.next());
         DefaultBrowserNode dbn = null;

         switch(iParentType)
         {
            case NT_APPLICATION:
               dbn = new DefaultBrowserNode(entry);
//               AppObjectCataloger cat = new AppObjectCataloger(strParentName);
//               Iterator iter = cat.iterator();
//               dbn.setCataloger(iter);
               vChildren.add(dbn);
               break;

            case NT_APPOBJ_TYPE:
               boolean bAdd = false;
               if(entry.getType() == NT_APPOBJECT_DATASET && strParentName.equals(AppObjectTypeCataloger.DATASET))
               {
                  bAdd = true;
                  dbn = new DatasetNode(entry);
               }
               else if(entry.getType() == NT_APPOBJECT_ROLE && strParentName.equals(AppObjectTypeCataloger.ROLE))
               {
                  bAdd = true;
                  dbn = new RoleNode(entry);
               }
               
               if(bAdd == false)
                  break;
               
//               dbn.setCataloger(null);      // no more catalogging 
               dbn.setAllowsChildren(false);   // no more children after NT_APPOBJECTS
               vChildren.add(dbn);
               break;

            default:
               return null;
         }
    }

    return vChildren;

  }


}


