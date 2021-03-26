/******************************************************************************
 *
 * [ SQLHierarchyConstraints.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer.browser;

import javax.swing.tree.TreeNode;
import java.util.Iterator;
import java.util.Vector;

/**
 * This is an implementation of the IHierarchyConstraints interface for an
 * ODBC browser tab. The standard level is :
 * server -> database -> schema (owner) -> db object type -> db object
 * <p>
 * The children of all entries are dynamically cataloged. The schema will be
 * allowed to change its order in the sequence and whether it appears at all at
 * some future time. This is not required for V1 however.
 *
 * @see IHierarchyConstraints
 */
public class SQLHierarchyConstraints
      implements IHierarchyConstraints
{
   /**
    * Type identifiers for dynamic nodes and leaves of dynamic nodes.
    */
   public static final int NT_DATASOURCE = 0;
   public static final int NT_DATASOURCE_OBJ = 1;
   public static final int NT_DBOBJ_TYPE = 2;
   public static final int NT_DBOBJ = 3;
   public static final int NT_COLUMN = 4;

   /**
    * Initially, no reordering of levels is supported.
    * In the future, if and where schema appears in the hierarchy will be able
    * to be modified.
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
      int [] iaOrder = { NT_DATASOURCE, NT_DATASOURCE_OBJ, NT_DBOBJ_TYPE,
              NT_DBOBJ, NT_COLUMN }; //the NT_COLUMN is just to set the cataloger for NT_DBOBJ
                                                    // NT_COLUMN does not appear in the browser
      return iaOrder;
   }


   /**
    * Gets the cataloger for child node passed in. The parent node is needed to
    * get the branch of the tree. The child's node type determines the type of
    * cataloger to return. The cataloger returned for the child is the cataloger
    * for catalogging the child node's children. Returns a cataloger for the
    * child node if the child node type NT_... is other than NT_COLUMN. No
    * cataloger is returned if the child type is NT_COLUMN because it is the
    * last one in the Hierarchy Constraint order. In this case, null is
    * returned.
    */
   public Iterator getCatalogerForChild(DefaultBrowserNode nodeParent,
      DefaultBrowserNode nodeChild)
   {
    if (nodeParent == null || nodeChild == null)
         return null;

      int iParentType = nodeParent.getType();
      int iChildType = nodeChild.getType();

      if (iChildType < 0 || iParentType < 0)
         return null;

      // get the branch of the tree, we will need this for getting internal
      // names
      // the array contains all nodes upto the passed in nodeParent including
      // the root .
      TreeNode[] naNodes = nodeParent.getPath();

      // fill in the node names as we will need them for constructing the
      // cataloger
      // the names start at the server node which is first child to root
      String[] straNodeNames = new String[naNodes.length + 1];
      String[] straDisplayNames = new String[naNodes.length + 1];
      for (int i = 0; i < straNodeNames.length - 1; i++)
      {
         DefaultBrowserNode dbn = (DefaultBrowserNode) naNodes[i];
         straNodeNames[i] = dbn.getInternalName();
         straDisplayNames[i] = dbn.getDisplayName();
      }

      // add the internal name for the child
      straNodeNames[straNodeNames.length - 1] = nodeChild.getInternalName(); 

      int iNextType = getNextNodeType(iChildType);

      // datasource name may be needed
      String strDatasource = getInternalNameForNodeType(NT_DATASOURCE_OBJ, 
         straNodeNames);

      switch (iNextType)
      {
         case NT_DATASOURCE_OBJ :
            DatasourceCataloger dsCat = new DatasourceCataloger();
            if (dsCat != null)
               return dsCat.iterator();
            break;

         case NT_DBOBJ_TYPE :
            ObjectTypeCataloger otCat = new ObjectTypeCataloger(strDatasource);
            if (otCat != null)
               return otCat.iterator();
            break;

         case NT_DBOBJ :
            String strType = getInternalNameForNodeType(NT_DBOBJ_TYPE,
               straNodeNames);
            TableCataloger tCat = new TableCataloger(strDatasource, strType, 
               null);
            if (tCat != null)
               return tCat.iterator();
            break;

         case NT_COLUMN :
            String strTable = getInternalNameForNodeType(NT_DBOBJ,
               straNodeNames);
            ColumnCataloger cCat = new ColumnCataloger(strDatasource, strTable);
            if (cCat != null)
               return cCat.iterator();
            break;
      }
      return null; // if everything went ok we should not get here
   }

   /**
    * Gets the cataloger for the node passed in. The parent node is needed to
    * get the branch of the tree. The child's node type determines the type of
    * cataloger to return. The cataloger returned for the node is the cataloger
    * for catalogging it's children. Returns a cataloger for the if the node
    * type NT_... is other than NT_COLUMN. No cataloger is returned if the
    * NT_COLUMN because it is the last one in the Hierarchy Constraint order. In
    * this case, null is returned.
    */
   public Iterator getCatalogerForNode(DefaultBrowserNode node)
   {
      if (node == null)
         return null;

      int iParentType = node.getType();
      int iChildType = getNextNodeType(iParentType);
      // the parent has the cataloger to catalog its children

      if (iChildType < 0 || iParentType < 0)
         return null;

      /*
       * get the branch of the tree, we will need this for getting internal
       * names the array contains all nodes upto the passed in nodeParent
       * including the root . Note: the Server node is not the root, but the
       * first child of the root
       */
      TreeNode[] naNodes = node.getPath();

      /*
       * fill in the node names as we will need them for constructing the
       * cataloger the names start at the datasource node which is the root
       */
      String[] straNodeNames = new String[naNodes.length];
      String[] straDisplayNames = new String[naNodes.length];
      for (int i = 0; i < straNodeNames.length; i++)
      {
         DefaultBrowserNode dbn = (DefaultBrowserNode) naNodes[i]; 
         straNodeNames[i] = dbn.getInternalName();
         straDisplayNames[i] = dbn.getDisplayName();
      }

      // datasource name is always needed except for root
      String strDatasource = getInternalNameForNodeType(NT_DATASOURCE_OBJ, 
         straNodeNames);

      switch (iChildType)
      {
         case NT_DATASOURCE_OBJ :
            DatasourceCataloger dsCat = new DatasourceCataloger();
            return dsCat.iterator();
            

         case NT_DBOBJ_TYPE :
            ObjectTypeCataloger otCat = new ObjectTypeCataloger(strDatasource);
            if (otCat != null)
            {
               // System.out.println(" in SQLHierarcy returning DbObjectType
               // cataloger");
               return otCat.iterator();
            }
            break;

         case NT_DBOBJ :
            // TableCataloger constructor needs names of driver, server,
            // database, schema and type
            String strType = getInternalNameForNodeType(NT_DBOBJ_TYPE,
               straNodeNames);
            TableCataloger tCat = new TableCataloger(strDatasource, strType, 
               null);
            if (tCat != null)
            {
               return tCat.iterator();
            }
            break;

         case NT_COLUMN :
            // ColumnCataloger constructor needs names of driver, server,
            // database, schema and type
            String strTable = getInternalNameForNodeType(NT_DBOBJ,
               straNodeNames);
            ColumnCataloger cCat = new ColumnCataloger(strDatasource, strTable);
            if (cCat != null)
            {
               return cCat.iterator();
            }
            break;
      }
      return null; // if everything went ok we should not get here
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
   * @returns the index of this node type  NT_ ... in the SQLHierarchyConstraints Order array. If the
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
    */
  public Vector getChildren(DefaultBrowserNode nodeParent)
  {
    if(nodeParent == null)
      return null;
    int iParentType = nodeParent.getType();
    int iChildType = getNextNodeType(iParentType);

    if(iChildType < 0 || iParentType < 0)
      return null;

    Iterator parentCataloger = getCatalogerForNode(nodeParent);

    if(parentCataloger == null)
     throw new IllegalArgumentException("parent node's cataloger is null");

    //  fill this vector with children and return it
    Vector<DefaultBrowserNode> vChildren = new Vector<DefaultBrowserNode>(); 

    TreeNode[] naNodes = nodeParent.getPath();

    //fill in the node names as we will need them for constructing the cataloger
    //the names start at the datasource node which is the root
    String [] straNodeNames = new String[naNodes.length];
    for(int i=0; i<straNodeNames.length; i++)
    {
      DefaultBrowserNode dbn = (DefaultBrowserNode)naNodes[i];
      straNodeNames[i] = dbn.getInternalName();
    }
   
   while(parentCataloger.hasNext())
    {
      ICatalogEntry entry = (ICatalogEntry)(parentCataloger.next());
      switch(iChildType)
      {
        case(NT_DBOBJ):
            {
              TableNode tn = new TableNode(entry);
              Iterator tIter = getCatalogerForChild(nodeParent, tn);
              tn.setCataloger(tIter);
              tn.setAllowsChildren(false);   // no more children after NT_DBOBJ
              vChildren.add(tn);
            }
            break;

        default:
            {
              DefaultBrowserNode dbn = new DefaultBrowserNode(entry);
              vChildren.add(dbn);
            }
            break;

      }
    }// end while
    return vChildren;
  }

}

