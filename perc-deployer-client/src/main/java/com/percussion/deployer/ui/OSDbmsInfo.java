/******************************************************************************
 *
 * [ OSDbmsInfo.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
 
package com.percussion.deployer.ui;

import com.percussion.deployer.objectstore.PSDbmsInfo;
import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.error.PSDeployException;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * The class which holds the information of repository/dbms alias name in 
 * addition to wrapping the base class information.
 */
public class OSDbmsInfo extends PSDbmsInfo
{
   /**
    * Constructor for the dbms info.
    * 
    * @param repositoryAlias the repository alias, may not be <code>null</code> 
    * or empty.
    * @param driver the database driver, may not be <code>null</code> or empty.
    * @param server the database server, may not be <code>null</code> or empty.
    * @param database the database, may be <code>null</code> or empty.
    * @param origin the origin, may be <code>null</code> or empty.
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */   
   public OSDbmsInfo(String repositoryAlias, String driver, String server, 
      String database, String origin)
   {
      super(driver, server, database, origin, null, null, false);
      
      if(repositoryAlias == null || repositoryAlias.trim().length() == 0)
         throw new IllegalArgumentException(
            "repositoryAlias may not be null or empty.");
            
      m_repositoryAlias = repositoryAlias;
   }
   
   /**
    * Constructs this object with the supplied alias and dbms info.
    * 
    * @param repositoryAlias the repository alias, may not be <code>null</code> 
    * or empty.
    * @param dbmsInfo the dbms information, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if any param is invalid.
    */
   public OSDbmsInfo(String repositoryAlias, PSDbmsInfo dbmsInfo)
   {
      if(repositoryAlias == null || repositoryAlias.trim().length() == 0)
         throw new IllegalArgumentException(
            "repositoryAlias may not be null or empty.");
            
      if(dbmsInfo == null)
         throw new IllegalArgumentException("dbmsInfo may not be null");
            
      m_repositoryAlias = repositoryAlias;
      copyFrom(dbmsInfo);
   } 
   
   /**
    * Constructs this object from xml. Please refer to {@link #toXml(Document)}
    * for the xml format.
    * 
    * @param source the source element, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if source is <code>null</code>.
    * @throws PSUnknownNodeTypeException if the source element does not 
    * represent the xml element node of this object or if the required 
    * attributes are missing on this element.
    */
   public OSDbmsInfo(Element source)
           throws PSUnknownNodeTypeException, PSDeployException {
      super(source);
   }
   
   /**
    * Extends the super's method to get the members of this object. Please refer
    * to {@link #toXml(Document) } for the xml format.
    * 
    * @param source the source element, may not be <code>null</code>
    * 
    * @throws IllegalArgumentException if source is <code>null</code>.
    * @throws PSUnknownNodeTypeException if the source element does not 
    * represent the xml element node of this object or if the required 
    * attributes are missing on this element.
    */
   public void fromXml(Element source)
           throws PSUnknownNodeTypeException, PSDeployException {
      if (source == null)
         throw new IllegalArgumentException("sourceNode may not be null");
         
      if (!XML_NODE_NAME.equals(source.getNodeName()))
      {
         Object[] args = { XML_NODE_NAME, source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      Element dbmsInfo = tree.getNextElement(PSDbmsInfo.XML_NODE_NAME, 
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      if (dbmsInfo == null)
      {
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSDbmsInfo.XML_NODE_NAME);
      }
      super.fromXml(dbmsInfo);
      
      String alias = source.getAttribute(XML_ALIAS_ATTR);
      if(alias == null || alias.trim().length() == 0)
      {
         Object[] args = { "XML Node Name", XML_ALIAS_ATTR, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }
      m_repositoryAlias = alias;
   }
   
   /**
    * Extends the super's method to set the members of this object in the xml 
    * element. Format is:
    * <pre><code>
    * &lt;!ELEMENT OSXDbmsInfo (PSXDbmsInfo)>
    * &lt;!ATTLIST OSXDbmsInfo
    *    alias CDATA #REQUIRED
    * >
    * </pre></code>
    * 
    * @param doc the document used to create the element, may not be <code>null
    * </code>
    * 
    * @throws IllegalArgumentException if doc is <code>null</code>
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");
         
      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(super.toXml(doc));      
      root.setAttribute(XML_ALIAS_ATTR, m_repositoryAlias);      
      return root;
   }
   
   /**
    * Gets the alias name of the repository.
    * 
    * @return the alias name, never <code>null</code> or empty.
    */
   public String getRepositoryAlias()
   {
      return m_repositoryAlias;
   }
   
   /**
    * Gets String representation of this object. Uses the repository alias name 
    * to represent this object.
    * 
    * @return the string, never <code>null</code> or empty.
    */
   public String toString()
   {
      return m_repositoryAlias;
   }
   
   /**
    * The alias name of the repository/database, initialized in the constructor
    * and never <code>null</code> or empty or modified after that.
    */
   private String m_repositoryAlias;
   
   /**
    * The root node name of this xml element
    */
   public static final String XML_NODE_NAME = "OSXDbmsInfo";
   
   /**
    * The XML attribute name for alias.
    */
   private static final String XML_ALIAS_ATTR = "alias";
}
