/*[ FileHierarchyConstraints.java ]********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.Util;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * This is an implementation of the IHierarchyConstraints interface for a
 * FILE browser tab. The standard level is :
 * server -> root directory -> [dir | file] -> [dir | file ] ...
 * <p>
 * The children of all entries except files are dynamically cataloged.
 *
 * @see IHierarchyConstraints
 */
public class FileHierarchyConstraints
      implements IHierarchyConstraints
{
   /*
    * Type identifiers for dynamic nodes and leaves of dynamic nodes.
    */
   public static final int NT_SERVER =    1;
   public static final int NT_PATHROOT =  2;
   public static final int NT_FILESYSTEM_ENTRY = 3;
   public static final int NT_DIRECTORY = 4;
   public static final int NT_FILE =      5;

   /**
    * Default ctor.
    */
   public FileHierarchyConstraints()
   {
   }

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
    * NT_... (NodeType...). Note that the returned order is not exact. A type
    * of NT_PATHROOT returns a File entry cataloger which can return both
    * directories and files. Similarly for NT_DIRECTORY.
    *
    */
   public int [] getOrder( )
   {
      int [] iaOrder = { NT_SERVER, NT_PATHROOT, NT_DIRECTORY, NT_FILE };
      return iaOrder;
   }


   /**
    * The type of the passed in node is used to determine what type of
    * cataloger to return. NT_PATHROOT and NT_DIRECTORY both return catalogers
    * for File entries, which can be either directories or files. A passed in
    * type of NT_FILE returns a null cataloger.
    */
/* public Iterator getCataloger( DefaultBrowserNode node )
   {
      return null;
   }  */


   /**
    * Gets the cataloger for node passed in. The node's child's type determines the type of
    * cataloger to return. The cataloger returned for the node is the cataloger for catalogging
    * it's children. For nodes that have types of NT_PATHROOT and NT_DIRECTORY both return
    * catalogers for File entries, which can be either directories or files. If the passed in
    * node's child is of type NT_FILE a null cataloger is returned.
    */
   private Iterator getCatalogerForNode( DefaultBrowserNode node)
   {
      if(node == null)
         return null;

      int iParentType = node.getType();
      int iChildType = getNextNodeType(iParentType);

      if(iChildType < 0 || iParentType < 0)
         return null;

      // get the driver name. Since we have no way of getting to the tab based on the nodes
      // get the driver from the current tab.
      BrowserFrame bf = null;
      bf = bf.getBrowser();
      String strDriver = "psxml";
      //System.out.println(" in FileHierarcyConstraints Cur Driver = "+strDriver);

      // get the server name
      //get the branch of the tree,  we will need this for getting internal names
      //the array contains all nodes upto the passed in nodeParent  including the root .
      //Note: the Server node is not the root, but the first child of the root
      TreeNode[] naNodes = node.getPath();
      //System.out.println("length of tree branch to the passed in node ="+naNodes.length );

      //fill in the node names as we will need them for constructing the cataloger
      //the names start at the server node which is first child to root
      String [] straNodeNames = new String[naNodes.length-1];
      for(int i=0; i<straNodeNames.length; i++)
      {
         DefaultBrowserNode dbn = (DefaultBrowserNode)naNodes[i+1]; //skip the root
         straNodeNames[i] = dbn.getInternalName();
         //System.out.println("Array of Names " +straNodeNames[i]);
      }
      //System.out.println("Current node's Child's Type = "+iChildType );

      //server name is always needed
      String strServer = getInternalNameForNodeType(NT_SERVER, straNodeNames);
      String strPath = null;

      FileRootCataloger frCat = null;
      FileCataloger fCat = null;

      switch (iChildType)
      {
         case NT_PATHROOT:
            // FileRootCataloger constructor needs driver name and server name
            frCat = new FileRootCataloger(strDriver, strServer);
            if(frCat != null)
               return frCat.iterator();
            break;

         case NT_DIRECTORY:
            // FileCataloger constructor needs names of driver, server and path
            strPath = getInternalNameForNodeType(NT_PATHROOT, straNodeNames);

            fCat = new FileCataloger(strPath);
            if(fCat != null)
               return fCat.iterator();
            break;

         case NT_FILE:
            // FileCataloger constructor needs names of driver, server and path
            strPath = getInternalNameForNodeType(NT_PATHROOT, straNodeNames);
            //System.out.println(" in FileHierarcyConstraints Driver = "+strDriver+" Server = "+strServer+" Path = "+strPath);

            fCat = new FileCataloger(strPath);
            if(fCat != null)
               return fCat.iterator();
            break;

         }
      return null;   // if everything went ok we should not get here
   }

   /**
    * Gets the cataloger for child node passed in. The parent node is needed to get the branch of the tree.
    * The child's node type determines the type of cataloger to return. The cataloger returned
    * for the child is the cataloger for catalogging the child node's children.
    * For child nodes that have types of NT_PATHROOT and NT_DIRECTORY both return catalogers
    * for File entries, which can be either directories or files. A passed in child node with
    * type of NT_FILE returns a null cataloger.
    */
   private Iterator getCatalogerForChild( DefaultBrowserNode nodeParent, DefaultBrowserNode nodeChild)
   {
      if(nodeParent == null || nodeChild == null)
         return null;

      int iParentType = nodeParent.getType();
      int iChildType = nodeChild.getType();

      if(iChildType < 0 || iParentType < 0)
         return null;

      // get the driver name. Since we have no way of getting to the tab based on the nodes
      // get the driver from the current tab.
      BrowserFrame bf = null;
      bf = bf.getBrowser();
      String strDriver = "psxml";
      //System.out.println(" in FileHierarcyConstraints Cur Driver = "+strDriver);

      // get the server name
      //get the branch of the tree,  we will need this for getting internal names
      //the array contains all nodes upto the passed in nodeParent  including the root .
      //Note: the Server node is not the root, but the first child of the root
      TreeNode[] naNodes = nodeParent.getPath();
      //System.out.println("length of tree branch to Parent node ="+naNodes.length );

      //fill in the node names as we will need them for constructing the cataloger
      //the names start at the server node which is first child to root
      String [] straNodeNames = new String[naNodes.length];
      for(int i=0; i<straNodeNames.length-1; i++)
      {
         DefaultBrowserNode dbn = (DefaultBrowserNode)naNodes[i+1]; //skip the root
         straNodeNames[i] = dbn.getInternalName();
         //System.out.println("Array of Names " +straNodeNames[i]);
      }
      straNodeNames[straNodeNames.length-1] = nodeChild.getInternalName(); // add the internal name for the child
      //System.out.println("Array of Names -- childNodeName" + straNodeNames[straNodeNames.length-1]);

      int iNextType = getNextNodeType(iChildType);
      //System.out.println("Current Type = "+iChildType +" Next Type = "+iNextType);

      //server name is always needed
      String strServer = getInternalNameForNodeType(NT_SERVER, straNodeNames);
      String strPath = null;

      FileRootCataloger frCat = null;
      FileCataloger fCat = null;


      switch (iNextType)
      {
         case NT_PATHROOT:
            // FileRootCataloger constructor needs driver name and server name
            frCat = new FileRootCataloger(strDriver, strServer);
            if(frCat != null)
               return frCat.iterator();
            break;


         case NT_DIRECTORY:
            // FileCataloger constructor needs names of driver, server and path
            strPath = getInternalNameForNodeType(NT_PATHROOT, straNodeNames);
            //System.out.println(" in FileHierarcyConstraints Driver = "+strDriver+" Server = "+strServer+" Path = "+strPath);


            fCat = new FileCataloger(strPath);
            if(fCat != null)
               return fCat.iterator();
            break;

         case NT_FILE:
            // FileCataloger constructor needs names of driver, server and path
            strPath = getInternalNameForNodeType(NT_PATHROOT, straNodeNames);
            //System.out.println(" in FileHierarcyConstraints Driver = "+strDriver+" Server = "+strServer+" Path = "+strPath);

            fCat = new FileCataloger(strPath);
            if(fCat != null)
               return fCat.iterator();
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
   @SuppressWarnings("unchecked")
   public Vector getChildren(DefaultBrowserNode nodeParent)
   {
      if(nodeParent == null)
         return null;

      nodeParent.disableSorting();

      int iParentType = nodeParent.getType();

      if(iParentType < 0)
         return null;

      Vector vChildren = new Vector(); //fill this vector with children and return it

      if(iParentType == FileHierarchyConstraints.NT_FILE)
         return vChildren;

      File parentDirectory = new File(nodeParent.getFullPathName());

      String[] types = BrowserFrame.getBrowser().getValuesFromUserConfig(BrowserTree.getResources().getString("FILETYPES"));
      if(types != null)
         m_filter = new ExtensionFileFilter(types);

      File[] fChildren = parentDirectory.listFiles(m_filter);

      if(fChildren != null)
      {
         Vector<DefaultBrowserNode> vDirectories = new Vector<DefaultBrowserNode>();
         for(int f = 0; f < fChildren.length; ++f)
         {
            if(!fChildren[f].isDirectory())
               continue;

            //get full path name
            DefaultBrowserNode dbn = new DefaultBrowserNode(new CatalogEntry());
            dbn.disableSorting();

            dbn.setInternalName(fChildren[f].getName());
            try
            {
               File filePath = new File(nodeParent.getFullPathName() + "/" + dbn.getInternalName());
               String strPath = filePath.getCanonicalPath();
               dbn.setFullPathName(strPath);
            }
            catch(java.io.IOException e)
            {
               e.printStackTrace();
            }

            dbn.setType(FileHierarchyConstraints.NT_DIRECTORY);
            vDirectories.add(dbn);

         }// end while

         vChildren = nodeParent.sortNodes(vDirectories);

         Vector<XMLNode> vFiles = new Vector<XMLNode>();
         //now add xml files
         for(int f = 0; f < fChildren.length; ++f)
         {
            if(fChildren[f].isDirectory())
               continue;

            XMLNode filenode = new XMLNode(new CatalogEntry(), nodeParent.getFullPathName());

            filenode.setInternalName(fChildren[f].getName());
            //System.out.println(".....File name ="+fChildren[f].getName());
            ImageIcon icon = getIcon(fChildren[f].getName());
            if(icon != null)
               filenode.setIcon(icon);

            filenode.setType(FileHierarchyConstraints.NT_FILE);
            filenode.setAllowsChildren(false);
            vFiles.add(filenode);
         }

         nodeParent.sortNodes(vFiles);
         for(int iFile = 0; iFile < vFiles.size(); ++ iFile)
            vChildren.add(vFiles.get(iFile));
      }

      return vChildren;
   }

   /**
    * Determines an appropriate image for the type of the supplied filename
    * and creates an icon that can be used in the browser.
    *
    * @param strFileName The filename which needs an icon.
    *
    * @return An icon object that contains a small image to represent the
    * type of the supplied filename. null if strFileName is null or empty.
    */
   public static ImageIcon getIcon(String strFileName)
   {
      if(strFileName == null || 0 == strFileName.trim().length())
         return null;

      ResourceBundle res = BrowserFrame.getBrowser().getResources();
      ImageIcon icon = null;

      final Class thisClass = FileHierarchyConstraints.class;
      String fileExt = ExtensionFileFilter.getExtension( strFileName );
      if( fileExt.equalsIgnoreCase("htm")
            || fileExt.equalsIgnoreCase("html")
            || fileExt.equalsIgnoreCase("xhtm")
            || fileExt.equalsIgnoreCase("xhtml"))
      {
         icon =
               new ImageIcon(thisClass.getResource(res.getString("gif_html")));
      }
      else if ( fileExt.equalsIgnoreCase("xml")
            || fileExt.equalsIgnoreCase("dtd"))
      {
         icon = new ImageIcon(thisClass.getResource(res.getString("gif_xml")));
      }
      else if(fileExt.equalsIgnoreCase("xsl"))
         icon = new ImageIcon(thisClass.getResource(res.getString("gif_xsl")));
      else if ( fileExt.equalsIgnoreCase("gif")
            || fileExt.equalsIgnoreCase("jpeg")
            || fileExt.equalsIgnoreCase("jpg"))
      {
         icon = new ImageIcon(thisClass.getResource(res.getString("gif_img")));
      }
      else
         icon = new ImageIcon(thisClass.getResource(
               res.getString("gif_object" )));
      return icon;
   }

    /**
    * Used to filter files that will be displayed to the user. Defaults to the
    * types specified in {@link #straFILETYPES}, but may be overridden by the
    * user (overridden values are stored on the server).
    * Never <code>null</code>.
    */
  private ExtensionFileFilter m_filter =
         new ExtensionFileFilter(FileHierarchyConstraints.straFILETYPES);


  /** String array containing the default file types to be displayed.
    * "xml","dtd","htm*", "xsl", "gif", "jpg" and "xhtm*" are the default types
    * at this time.
    */
   public static String [] straFILETYPES = new String[]
   {
      "dtd", "gif", "jpg", "xml", "xsl"
   };

   // sorted array obtained from the Lsit - m_lst
   static
   {
      final List<String> lst = Util.getSplitableFileExts();
      lst.addAll(Arrays.asList(straFILETYPES));
      straFILETYPES = Util.listToArray(lst);
   }
}

