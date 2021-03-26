/******************************************************************************
 *
 * [ MapBrowserTreeNode.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.xml.PSDtdElementEntry;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Iterator;

/**
 * A transferrable tree node.
 */
public class MapBrowserTreeNode extends DefaultMutableTreeNode implements
   Transferable
{
   /**
    * Constructor for a tree node that contains an UDF.
    *
    * @param userObject the user object
    * @param type the browser type (XML or BACKEND)
    * @param path the path string
    * @param exit the active UDF exit
    * @param iUdfType the UDF type, one of <code>OSUdfConstants.UDF_APP</code> 
    *    or <code>OSUdfConstants.UDF_GLOBAL</code>.
    * @param bIsQuery
    */
   public MapBrowserTreeNode(Object userObject, int type, String path,
      IPSExtensionDef exit, int iUdfType, Boolean bIsQuery)
   {
      super(userObject);
      
      m_type = type;
      m_path = path;
      m_bIsQuery = bIsQuery;

      int count = 0;
      Iterator paramNames = exit.getRuntimeParameterNames();
      for (; paramNames.hasNext(); paramNames.next())
         count++;

      PSExtensionParamValue params[] = new PSExtensionParamValue[count];
      m_call = new OSExtensionCall(exit, params);
      m_call.setUdfType(iUdfType);
   }
   
   /**
    * Constructor.
    * 
    * @userObject the user object
    * @type the browser type (XML or BACKEND)
    * @path the path string
    */
   public MapBrowserTreeNode(Object userObject, int type, String path,
      Boolean bIsQuery)
   {
      super(userObject);
      
      m_type = type;
      m_path = path;
      m_call = null;
      m_bIsQuery = bIsQuery;
   }

   /**
    * new constructor accepts the at symbol only
    */
   public MapBrowserTreeNode(int icon, Object userObject, int type,
      String path, Boolean bIsQuery)
   {
      super(userObject);
      
      m_entry = null;
      m_type = type;
      m_path = path;
      m_call = null;
      m_icon = icon;
      m_bIsQuery = bIsQuery;
   }

   /**
    * new constructor stores the PSDtdElementEntry inside this node
    */
   public MapBrowserTreeNode(PSDtdElementEntry entry, Object userObject,
      int type, String path, Boolean bIsQuery)
   {
      super(userObject);
      
      m_entry = entry;
      m_type = type;
      m_path = path;
      m_call = null;
      m_bIsQuery = bIsQuery;
      
      // temp fix for problem with copy DTD
      m_originalDTDRepeat = m_entry.getOccurrenceType();
   }

   /**
    * Copy constructor.
    *
    * @source the copy source
    */
   public MapBrowserTreeNode(MapBrowserTreeNode source)
   {
      super(source.getUserObject());
      
      data = source.data;
      m_path = source.m_path;
      m_type = source.m_type;
      m_call = source.m_call;
      m_bIsQuery = source.m_bIsQuery;
      m_entry = source.m_entry;
      m_nodeType = source.m_nodeType;
      m_readOnly = source.m_readOnly;
      m_originalDTDRepeat = source.m_originalDTDRepeat;
      m_icon = source.m_icon;
   }

   /**
    * Used to patch the path ( when item is a attribute ).
    * 
    * @param path the new path
    */
   public void overWritePath(String path)
   {
      m_path = path;
   }

   /**
    * @return <code> true </code> if is DTD
    */
   public boolean isDTD()
   {
      boolean bRet = false;
      if (m_entry != null || m_icon != 0)
      {
         bRet = true;
      }
      return (bRet);
   }

   // temp fix for problem with copy DTD
   public void restoreOriginalDTDRepeat()
   {
      setRepeatAttribute(m_originalDTDRepeat);
   }

   /**
    * @return the <CODE>PSDtdElementEntry</CODE> stored on this node, or null if
    * <CODE>PSDtdElementEntry</CODE> is not present.
    *
    * @see PSDtdElementEntry
    */
   public PSDtdElementEntry getElement()
   {
      return m_entry;
   }

   /**
    * @return the DTD repeat attribute contained on the
    * <CODE>PSDtdElementEntry</CODE>.
    *
    * @see PSDtdElementEntry
    */
   public int getRepeatAtribute()
   {
      int iRepeat = PSDtdElementEntry.OCCURS_UNKNOWN;
      if (m_entry != null)
      {
         iRepeat = m_entry.getOccurrenceType();
      }
      if (m_icon != 0)
      {
         iRepeat = m_icon;
      }
      return (iRepeat);
   }

   /**
    * set this node read only, if so the DTD repeat can not be modified
    */
   public void setNodeReadOnly()
   {
      m_readOnly = true;
   }

   /**
    * @return <code> true </code> if node is readonly, <code> false </code> if
    * can be modified
    */
   public boolean isNodeReadOnly()
   {
      return (m_readOnly);
   }

   /**
    * set/change the DTD repeat attribute stored on the PSDtdElementEntry
    * 
    * @param newAttribute
    * <UL>
    * <LI><code>PSDtdNode.OCCURS_ONCE</code> if the node is required;</LI>
    * <LI><code>PSDtdNode.OCCURS_OPTIONAL</code> if the node is optional;</LI>
    * <LI><code>PSDtdNode.OCCURS_ANY</code> if the node can occur 0 or more
    * times;</LI>
    * <LI><code>PSDtdNode.OCCURS_ATLEASTONCE</code> if the node can occur 1
    * or more times;</LI>
    * </UL>
    */
   public void setRepeatAttribute(int newAttribute)
   {
      if (m_entry != null)
      {
         m_entry.setOccurrences(newAttribute);
      }
   }

   /**
    * @return the node type
    * <UL>
    * <LI>NODETYPEFOLDER = node is a folder</LI>
    * <LI>NODETYPECHILD = node is a entry under a folder</LI>
    * <LI>NODETYPEROOT = node is the root</LI>
    * <LI>NODETYPEUNK = node is unknown</LI>
    * </UL>
    */
   public int getNodeType()
   {
      return (m_nodeType);
   }

   /**
    * sets the node type
    * 
    */
   public void setNodeType(int nodeType)
   {
      m_nodeType = nodeType;
   }

   /**
    * Get the path, stripping the leading slash. In an update pipe, elements
    * that are HTML params cannot have a leading slash in a backend mapping.
    * Elements from a DTD or real XML source are allowed either way. Therefore,
    * to be consistent, we will always return the fully qualified name w/o the
    * leading slash.
    * 
    * @return String the path, never <code>null</code>, may be empty.
    */
   public String getPathString()
   {
      String elementName = new String();

      if (m_path.startsWith("/"))
         elementName = m_path.substring(1);
      else
         elementName = m_path;

      return elementName;
   }

   /**
    * Returns the original path string, whether the node is a child or an
    * attribute. If the node is an attribute, an '@' is added before the name of
    * the attribute.
    * 
    * @return the original path with an '@' when the node is an attribute.
    */
   public String getOriginalPathString()
   {
      String originalPathString = this.m_path;

      // if attribute = @
      if ((this.getRepeatAtribute() == MapBrowser.ATTRIBUTE_ICON)
         && this.isLeaf())
      {
         String scPath = this.getPathString();
         int last_slash = scPath.lastIndexOf("/");

         if (last_slash == -1)
         {
            originalPathString = "@" + scPath;
         }
         else
         {
            String stringBegin = scPath.substring(0, last_slash);
            String stringEnd = scPath.substring(last_slash + 1);
            originalPathString = stringBegin + "/@" + stringEnd;
         }
      }

      return originalPathString;
   }

   /**
    * Get UDF call from the tree node.
    * 
    * @return OSExtensionCall the exit
    */
   public OSExtensionCall getUdfCall()
   {
      return m_call;
   }

   /**
    * Sets the Udf call data ONLY IF this node has been designated as an UDF
    * node. If this node is not a UDF node, an <CODE>IllegalStateException</CODE>
    * will be thrown.
    * 
    * @param call The udf call you wan to set. May be <CODE>null</CODE>.
    */
   public void setUdfCall(OSExtensionCall call)
   {
      if (isUdf())
         m_call = call;
      else
         throw new IllegalStateException(
            "Setting UDF calls are only allowed for UDF nodes!");
   }

   /**
    * Get UDF exit
    * 
    * @return IPSExtensionDef the exit
    */
   public IPSExtensionDef getUdfExit()
   {
      if (m_call != null)
         return m_call.getExtensionDef();

      return null;
   }

   /**
    * Is this a UDF type.
    * 
    * @return boolean true for UDF, false otherwise
    */
   public boolean isUdf()
   {
      return (m_type == UDF);
   }


   /**
    * Is this a CGI type.
    * 
    * @return boolean true for CGI, false otherwise
    */
   public boolean isCgi()
   {
      return (m_type == CGI);
   }

   /**
    * Is this a UserContext type.
    * 
    * @return boolean true for UserContext, false otherwise
    */
   public boolean isUserContext()
   {
      return (m_type == USER_CONTEXT);
   }

   /**
    * Is this a backend type.
    * 
    * @return boolean true for BACKEND, false otherwise
    */
   public boolean isBackend()
   {
      return (m_type == BACKEND);
   }

   /**
    * Is this an XML type.
    * 
    * @return boolean true for XML, false otherwise
    */
   public boolean isXml()
   {
      return (m_type == XML);
   }

   /**
    * Is this a FORM type.
    * 
    * @return boolean true for FORM, false otherwise
    */
   public boolean isForm()
   {
      return (m_type == FORMS);
   }

   // implementation for Transferable
   public DataFlavor[] getTransferDataFlavors()
   {
      boolean isSourceType = isUdf() || isCgi() || isUserContext() || isForm();
      if (this.isBackend() || (m_bIsQuery.booleanValue() && isSourceType))
         return m_backendFlavors;
      else if (this.isXml() || (!m_bIsQuery.booleanValue() && isSourceType))
         return m_xmlFlavors;

      return m_flavors;
   }

  // implementation for Transferable
   @SuppressWarnings("unused")
   public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
   {
      Object returnObject;
      if (flavor.equals(m_flavors[XML_TREE])
         || flavor.equals(m_flavors[BACKEND_TREE]))
         returnObject = this;
      else
         throw new UnsupportedFlavorException(flavor);

      return returnObject;
   }

   // implementation for Transferable
   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      boolean returnValue = false;
      for (int i = 0, n = m_flavors.length; i < n; i++)
      {
         if (flavor.equals(m_flavors[i]))
         {
            returnValue = true;
            break;
         }
      }

      return returnValue;
   }

   final static int XML_TREE = 0;

   final static int BACKEND_TREE = 1;

   final public static DataFlavor MAPPER_XML_TREENODE_FLAVOR = new DataFlavor(
      DefaultMutableTreeNode.class, "Mapper XML Tree Node");

   final public static DataFlavor MAPPER_BACKEND_TREENODE_FLAVOR = new DataFlavor(
      DefaultMutableTreeNode.class, "Mapper Backend Tree Node");

   static DataFlavor m_flavors[] = { MAPPER_XML_TREENODE_FLAVOR,
      MAPPER_BACKEND_TREENODE_FLAVOR, };

   static DataFlavor m_backendFlavors[] = { MAPPER_BACKEND_TREENODE_FLAVOR, };

   static DataFlavor m_xmlFlavors[] = { MAPPER_XML_TREENODE_FLAVOR, };

   private DefaultMutableTreeNode data;

   // valid types
   public final static int XML = 0;

   public final static int FORMS = 1;

   public final static int BACKEND = 21;

   public final static int UDF = 3;

   public final static int CGI = 4;

   public final static int USER_CONTEXT = 5;

   private String m_path = new String("");

   private int m_type = -1;

   private OSExtensionCall m_call = null;

   private Boolean m_bIsQuery = Boolean.FALSE;

   /** the <code>  PSDtdElementEntry </code> contained on this node. */
   private PSDtdElementEntry m_entry = null;

   /** node is a folder. */
   public final static int NODETYPEFOLDER = 1;

   /** node is a child ( element under a node ). */
   public final static int NODETYPECHILD = 2;

   /** node is the root. */
   public final static int NODETYPEROOT = 3;

   /** node type is not set. */
   public final static int NODETYPEUNK = 0;

   /** the actual node type. */
   private int m_nodeType = NODETYPEUNK;

   /** node is read only if this variable is <code>true<code>. */
   private boolean m_readOnly = false;

   /** temp fix until copyDTD works. */
   private int m_originalDTDRepeat;

   /** the icon. */
   private int m_icon = 0;
}
