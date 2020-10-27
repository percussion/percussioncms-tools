/*[ JndiGroupProviderObjectClasses.java ]**************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSJndiObjectClass;
import com.percussion.design.objectstore.PSUnknownDocTypeException;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * The utility class to load Jndi group provider definition from the provided
 * system resource file 'sys_JndiGroupProviderDef.xml'.
 */
public class JndiGroupProviderObjectClasses
{
   /**
    * Constructor to create a new object. Loads the resource file and caches the
    * group provider objectclass properties for each provider defined in the
    * file.
    */
   private JndiGroupProviderObjectClasses()
   {
       loadGroupProviderObjectClasses();
   }

   /**
    * Loads the resource file and caches the group provider objectclass
    * properties for each provider defined in the file.
    */
   private void loadGroupProviderObjectClasses()
   {
      URL url = getClass().getResource(PROVIDER_DEF_FILENAME);
      if(url != null)
      {
         InputStream in = null;
         try {
            in = url.openStream();

              Document doc = PSXmlDocumentBuilder.createXmlDocument(in, false);
            if(doc != null)
            {
               loadObjectClassDefs(doc);
            }
         }
         catch(SAXException e)
         {
            System.out.println(
               "Exception parsing the xml file to load the objectclass defs "
               + "for Jndi group providers." );
            System.out.println(e.getLocalizedMessage());

         }
         catch(PSUnknownDocTypeException e)
         {
            System.out.println(
               "Exception loading object classes defined in the definition file"
               + e.getLocalizedMessage());

         }
         catch(PSUnknownNodeTypeException e)
         {
            System.out.println(
               "Exception loading object classes defined in the definition file"
               + e.getLocalizedMessage());

         }
         catch(IOException e)
         {
            System.out.println("Exception in reading default resource file");
            System.out.println(e.getLocalizedMessage());
         }
         finally
         {
            try {
               if(in != null)
                  in.close();
            } catch(IOException ie){}
         }
      }
   }

   /**
    * Loads objectclass properties definition for each Jndi group provider.
    *
    * The document should conform to the following dtd.
    * <br>
    * &lt;!ELEMENT groupProviderObjectClasses (objectClassDefs*)>
    * &lt;!-- Attributes: <br>
    * jndiClassName - the name of the Jndi provider class that the
    * objectClass defintions are for.
    * <br>
    * -->
    * &lt;!ELEMENT objectClassDefs (objectClassDef+)>
    * &lt;!ATTLIST objectClassDefs
    *      jndiClassName   CDATA  #REQUIRED >
    * &lt;!-- The objectClass element defines a supported objectClass type for a
    * particular Jndi provider.
    * <br>
    * Attributes:
    * <br>
    * name - The name of the objectClass.
    * <br>
    * memberAttr - The name of the attribute the member list of this objectclass
    * is stored in.
    * <br>
    * type - Defines the value of the member attribute to specify a static or
    * dynamic list.
    * <br>
    * -->
    * &lt;!ELEMENT objectClassDef (#PCDATA)>
    * &lt;!ATTLIST objectClassDef
    *      name            CDATA               #REQUIRED
    *      memberAttribute CDATA               #REQUIRED
    *      type            (static | dynamic) "static" >
    *
    * @param source the source document to read the definition, assumed not to
    * be <code>null</code>
    *
    * @throws PSUnknownDocTypeException if the document does not have root
    * element or document root element name does not match the root element name
    * specified in the dtd.
    * @throws PSUnknownNodeTypeException if any of the required element or
    * attribute is missing.
    */
   private void loadObjectClassDefs(Document source)
      throws PSUnknownDocTypeException, PSUnknownNodeTypeException
   {
      if(source.getDocumentElement() == null)
      {
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, XML_ROOT_NODE);
      }

      if(!source.getDocumentElement().getTagName().equals(XML_ROOT_NODE))
      {
         throw new PSUnknownDocTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, XML_ROOT_NODE);

      }
      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);

      // load objectClassDefs
      Element objectClasses = tree.getNextElement(XML_OBJECT_CLASSES_NODE,
         firstFlags);
      while(objectClasses != null)
      {
         loadObjectClasses(objectClasses);
         objectClasses = tree.getNextElement(XML_OBJECT_CLASSES_NODE,
            nextFlags);
      }
   }

   /**
    * Loads objectclass properties from {@link #XML_OBJECT_CLASSES_NODE } node.
    * Caches all properties in <code>m_objectClassDefMap</code> with
    * <code>jndiClassName</code> <code>String</code> as key and <code>List
    * </code> of {@link com.percussion.design.objectstore.PSJndiObjectClass
    * PSJndiObjectClass} objects as value.
    *
    * @param objectClasses the <code>XML_OBJECT_CLASSES_NODE</code> element,
    * assumed not to be <code>null</code>
    *
    * @throws PSUnknownNodeTypeException if any of the required element or
    * attribute is missing or has an invalid value.
    */
   private void loadObjectClasses(Element objectClasses)
      throws PSUnknownNodeTypeException
   {
      String jndiClassName =
         objectClasses.getAttribute(XML_JNDI_CLASS_NAME_ATTR);
      if (jndiClassName == null || jndiClassName.trim().length() == 0)
      {
         Object[] args =
            { XML_OBJECT_CLASSES_NODE, XML_JNDI_CLASS_NAME_ATTR, "null" };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
      }

      List objectClassesList = new ArrayList();

      int firstFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;
      int nextFlags = PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS |
         PSXmlTreeWalker.GET_NEXT_RESET_CURRENT;

      PSXmlTreeWalker tree = new PSXmlTreeWalker(objectClasses);

      //load each objectClassDef and map the jndiClassName with the list of
      //objectClassDefs
      Element objectClass = tree.getNextElement(XML_OBJECT_CLASS_NODE,
            firstFlags);
     if (objectClass == null)
      {
         Object[] args =
            { XML_OBJECT_CLASSES_NODE, XML_OBJECT_CLASS_NODE, "null" };
         throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_CHILD, args);
      }

      while (objectClass != null)
      {
         String name = objectClass.getAttribute(XML_NAME_ATTR);
         if (name == null || name.trim().length() == 0)
         {
            Object[] args = {XML_OBJECT_CLASS_NODE, XML_NAME_ATTR, "null"};
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         String memberAttr = objectClass.getAttribute(XML_MEMBER_ATTR);
         if (memberAttr == null || memberAttr.trim().length() == 0)
         {
            Object[] args = { XML_OBJECT_CLASS_NODE, XML_MEMBER_ATTR, "null" };
            throw new PSUnknownNodeTypeException(
               IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
         }

         int type = -1;
         String attrType = objectClass.getAttribute(XML_TYPE_ATTR);
         if (attrType == null || attrType.trim().length() == 0)
            type = 0;
         else
         {
            for (int i = 0; i < PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM.length;
               i++)
            {
               if (attrType.equals(PSJndiObjectClass.MEMBER_ATTR_TYPE_ENUM[i]))
               {
                  type = i;
               }
            }

            if (type == -1)
            {
               Object[] args =
                  { XML_OBJECT_CLASS_NODE, XML_TYPE_ATTR, attrType };
               throw new PSUnknownNodeTypeException(
                  IPSObjectStoreErrors.XML_ELEMENT_INVALID_ATTR, args);
            }
         }

         PSJndiObjectClass objClass = new PSJndiObjectClass(
            name, memberAttr, type);
         objectClassesList.add(objClass);

         objectClass = tree.getNextElement(XML_OBJECT_CLASS_NODE, nextFlags);
      }

      m_objectClassDefMap.put(jndiClassName, objectClassesList);
   }

   /**
    * Gets the instance of this class. If the instance does not exist, creates
    * the instance and returns.
    *
    * @return the instance of this class, never <code>null</code>
    */
   public static JndiGroupProviderObjectClasses getInstance()
   {
      if(ms_instance == null)
         ms_instance = new JndiGroupProviderObjectClasses();
      return ms_instance;
   }

   /**
    * Gets the list of objectclass properties for the given class name of Jndi
    * group provider.
    *
    * @param jndiClassName The class name for the Jndi group provider, may not
    * be <code>null</code> or empty.
    *
    * @return the list of one or more <code>PSJndiObjectClass</code> objects for
    * the specified class name, may be <code>null</code> if the properties are
    * not defined for this class name.
    */
   public Iterator getObjectClasses(String jndiClassName)
   {
      if(jndiClassName == null || jndiClassName.trim().length() == 0)
         throw new IllegalArgumentException(
            "jndiClassName can not be null or empty");

      List objectClasses = (List)m_objectClassDefMap.get(jndiClassName);
      if(objectClasses != null)
         return objectClasses.iterator();
      else
         return null;
   }

   /**
    * The map with Jndi class name <code>String</code> as key and
    * <code>List</code> of <code>PSJndiObjectClass</code> objects as value. 
    */
   private Map m_objectClassDefMap = new HashMap();

   /**
    * The single instance of this class.
    */
   private static JndiGroupProviderObjectClasses ms_instance = null;

   /**
    * The name of the resource file for loading the objectclass properties for
    * Jndi group providers.
    */
   private static final String PROVIDER_DEF_FILENAME =
      "sys_JndiGroupProviderDef.xml";

   /*
    * The following variables define all xml elements and attributes in the
    * xml file.
    */
   private static final String XML_ROOT_NODE = "groupProviderObjectClasses";
   private static final String XML_OBJECT_CLASSES_NODE = "objectClassDefs";
   private static final String XML_OBJECT_CLASS_NODE = "objectClassDef";
   private static final String XML_JNDI_CLASS_NAME_ATTR = "jndiClassName";
   private static final String XML_NAME_ATTR = "name";
   private static final String XML_MEMBER_ATTR = "memberAttr";
   private static final String XML_TYPE_ATTR = "type";
}
