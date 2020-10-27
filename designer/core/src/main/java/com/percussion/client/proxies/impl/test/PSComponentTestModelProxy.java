/******************************************************************************
 *
 * [ PSComponentTestModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.PSObjectTypes;
import com.percussion.cms.objectstore.PSDbComponent;
import com.percussion.cms.objectstore.PSKey;
import com.percussion.design.objectstore.IPSComponent;
import com.percussion.xml.PSXmlDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

public abstract class PSComponentTestModelProxy extends PSTestModelProxy
{

   public PSComponentTestModelProxy(PSObjectTypes type)
   {
      super(type);
   }

   /**
    * Serializes the the specified repository map and saves it to the specified
    * file on the file system.
    * 
    * @param map the repository map, cannot be <code>null</code>.
    * @param repositoryFile the file to save the xml to, cannot be
    * <code>null</code>.
    */
   @Override
   protected void saveRepository(IPSRepositoryMap map, File repositoryFile)
      throws PSProxyTestException
   {
      // Create xml
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element root = doc.createElement(ELEM_OBJECTS);
      Iterator it = map.getAll();
      boolean isFirst = true;
      while (it.hasNext())
      {
         Object obj = it.next();
         if (obj instanceof PSDbComponent)
         {
            if (isFirst)
               root.setAttribute(ATTR_TYPE, "PSDbComponent");
            PSDbComponent comp = (PSDbComponent) obj;
            if (StringUtils.isBlank(comp.getLocator().getPart()))
               continue;
            Element object = doc.createElement(ELEM_OBJECT);
            root.appendChild(object);
            object.appendChild(comp.toXml(doc));
         }
         else if (obj instanceof IPSComponent)
         {
            if (isFirst)
               root.setAttribute(ATTR_TYPE, "IPSComponent");
            IPSComponent comp = (IPSComponent) obj;
            Element object = doc.createElement(ELEM_OBJECT);
            root.appendChild(object);
            object.appendChild(comp.toXml(doc));
         }

      }

      FileWriter writer = null;
      try
      {
         writer = new FileWriter(repositoryFile);
         writer.write(PSXmlDocumentBuilder.toString(root));
         writer.flush();
      }
      catch (Exception e)
      {
         throw new PSProxyTestException(e);
      }
      finally
      {
         if (writer != null)
         {
            try
            {
               writer.close();
               writer = null;
            }
            catch (IOException ignore)
            {
            }
         }

      }
   }

   /**
    * Loads the xml repository file from the filesystem and unserializes it into
    * the <code>PSRepositoryMap</code> it represents.
    * 
    * @param repositoryFile cannot be <code>null</code>.
    * @return the repository map file that the specified file represents. Never
    * <code>null</code>.
    * @throws PSProxyTestException upon any error
    */
   protected Object loadRepository(File repositoryFile)
      throws PSProxyTestException
   {
      if (repositoryFile == null)
         throw new IllegalArgumentException("repositoryFile cannot be null.");
      Object obj = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument(
            new FileInputStream(repositoryFile), false);
         PSRepositoryMap map = new PSRepositoryMap();
         Element root = doc.getDocumentElement();
         String type = root.getAttribute(ATTR_TYPE);
         NodeList nl = root.getElementsByTagName(ELEM_OBJECT);
         int len = nl.getLength();

         for (int i = 0; i < len; i++)
         {
            Element el = getFirstChildElement(nl.item(i));
            if (type.equals("PSDbComponent"))
            {
               PSDbComponent comp = (PSDbComponent) newComponentInstance();
               comp.fromXml(el);
               map.put(objectToReference(comp), comp);
            }
            else if (type.equals("IPSComponent"))
            {
               IPSComponent comp = (IPSComponent) newComponentInstance();
               comp.fromXml(el, null, null);
               map.put(objectToReference(comp), comp);
            }
         }
         obj = map;

      }
      catch (Exception e)
      {
         throw new PSProxyTestException(e);
      }
      return obj;

   }

   /**
    * Utility method to get the first child element under a specified node.
    * 
    * @param elem
    * @return the child element or <code> null</code> if none found.
    */
   private Element getFirstChildElement(Node elem)
   {
      if (!elem.hasChildNodes())
         return null;
      NodeList nl = elem.getChildNodes();
      int len = nl.getLength();
      for (int i = 0; i < len; i++)
      {
         Node node = nl.item(i);
         if (node instanceof Element)
            return (Element) node;
      }
      return null;
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#clone(java.lang.Object)
    */
   @Override
   public Object clone(Object source)
   {
      if (source instanceof PSDbComponent)
      {
         // We want to be sure that the locator also gets cloned.
         PSDbComponent comp = (PSDbComponent) source;
         PSKey key = comp.getLocator();
         comp = (PSDbComponent) comp.clone();
         comp.setLocator(key);
         return comp;
      }
      else if (source instanceof IPSComponent)
      {
         return ((IPSComponent) source).clone();
      }
      else
      {
         return super.clone(source);
      }
   }

   /**
    * This method should be implemented by the subclass to return a new instance
    * of the PSDbComponent or IPSComponent object that the proxy represents.
    * (i.e. for the ui search model proxy it will return a <code>PSSearch</code>
    * object.
    * 
    * @return new instance of an object, cannot be <code>null</code>.
    */
   protected abstract Object newComponentInstance();

   // xml element constants
   private static final String ELEM_OBJECTS = "DB_Objects";

   private static final String ELEM_OBJECT = "DB_Comp";

   // xml attribute constants
   private static final String ATTR_TYPE = "type";

}
