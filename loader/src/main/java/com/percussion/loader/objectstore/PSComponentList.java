/* *****************************************************************************
 *
 * [ PSComponentList.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.IPSLoaderErrors;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlTreeWalker;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a list of component objects
 */
public class PSComponentList extends PSLoaderComponent 
   implements java.io.Serializable
{
   /**
    * Constructs the object from a given XML node name and name of the object.
    * This object contains a list of single class objects.
    *
    * @param xmlNodeName The XML node name, may not be <code>null</code>
    *    or empty.
    * @param childXmlNode The XML node name of the component in the list, 
    *    may not be <code>null</code> or empty.
    * @param compClass The class of the component in the list, may not be 
    *    <code>null</code>.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSComponentList(String xmlNodeName, String childXmlNode,
      Class compClass)
   {
      this(xmlNodeName, childXmlNode, compClass, true);
   }
   
   /**
    * Constructs the object from a given XML node name and name of the object.
    * This object contains a list of single class objects.
    *
    * @param xmlNodeName The XML node name, may not be <code>null</code>
    *    or empty.
    * @param childXmlNode The XML node name of the component in the list, 
    *    may not be <code>null</code> or empty.
    * @param compClass The class of the component in the list, it may be the
    *    base class of the multiple classes (see <code>singleClass</code>), 
    *    may not be <code>null</code>.
    * @param singleClass Determines whether contains single or multiple class
    *    of objects. <code>true</code> if single class of objects; 
    *    <code>false</code> if multiple class of objects, and the 
    *    <code>compClass</code> will be the base class of the multiple classes.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSComponentList(String xmlNodeName, String childXmlNode,
      Class compClass, boolean singleClass)
   {
      if (xmlNodeName == null || xmlNodeName.trim().length() == 0)
         throw new IllegalArgumentException(
            "xmlNodeName may not be null or empty");
      if (childXmlNode == null || childXmlNode.trim().length() == 0)
         throw new IllegalArgumentException(
            "childXmlNode may not be null or empty");
      if (compClass == null)
         throw new IllegalArgumentException("compClass may not be null");

      m_xmlNodeName = xmlNodeName;
      m_childXmlNode = childXmlNode;
      m_compClass = compClass;
      m_singleClassList = singleClass;
   }

   /**
    * Returns the number of component in this object.
    *  
    * @return the number of component in this object.
    */
   public int size()
   {
      return m_list.size();
   }
   
   /**
    * Get the list of components.
    *
    * @return An iterator over zero or more <code>PSLoaderComponent</code> 
    * objects, never <code>null</code>, but may be empty.
    */
   public Iterator getComponents()
   {
      return m_list.iterator();
   }

   /**
    * Get the list components.
    * 
    * @return A list with zero or more <code>PSLoaderComponent</code> objects,
    *    never <code>null</code>, but may be empty.
    */
   public List getComponentList()
   {
      return m_list;
   }
   
   /**
    * Clear the component list, it will be empty afterwards.
    */
   public void clear()
   {
      m_list.clear();
   }
   
   /**
    * Adds a component to the component list.
    * 
    * @param comp The to be added component. It may not be <code>null</code> and
    *    its class must be <code>m_compClass</code>.
    *
    * @throws IllegalArgumentException if <code>comp</code> is invalid.
    */
   public void addComponent(PSLoaderComponent comp)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");
      if ( m_singleClassList && (comp.getClass() != m_compClass) )
         throw new IllegalArgumentException(
            "comp class (" + comp.getClass().toString() + 
            ") does not match \"" + m_compClass.toString() + "\"");

      m_list.add(comp);      
   }
   
   /**
    * Adds a list of <code>PSLoaderComponent</code> objects.
    * 
    * @param comps The list of <code>PSLoaderComponent</code> objects, may not
    *    <code>null</code>
    */
   public void addComponents(Iterator comps)
   {
      if (comps == null)
         throw new IllegalArgumentException("comps may not be null");

      while (comps.hasNext())
      {
         PSLoaderComponent comp = (PSLoaderComponent)comps.next();
         addComponent(comp);
      }
   }
   
   /**
    * Determines if the object contains any components.
    * 
    * @return <code>true</code> if it contains one or more components; 
    *    <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return m_list.isEmpty();
   }
   
   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXXXX (Component+)>
    * </code></pre>
    * where the <code>PSXXX</code> is the value of <code>m_xmlNodeName</code>
    * and <code>Component</code> is a component element.
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(m_xmlNodeName);

      Iterator elements = m_list.iterator();
      while (elements.hasNext())
      {
         PSLoaderComponent comp = (PSLoaderComponent) elements.next();
         root.appendChild(comp.toXml(doc));
      }

      return root;
   }

   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode) 
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, m_xmlNodeName);
      
      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element compEl = getNextRequiredElement(tree, m_childXmlNode,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      
      while (compEl != null)
      {
         PSLoaderComponent comp = newComponent(compEl);
         m_list.add(comp);
         compEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
   }

   /**
    * Creates new component from a given XML element. The XML element must 
    * contain the component with the class of <code>m_compClass</code>.
    * 
    * @param compEl The XML element contains to be created component, assume
    *    not <code>null</code>.
    * 
    * @return A newly created component, never <code>null</code>.
    * 
    * @throws PSLoaderException if any error occurs.
    */
   private PSLoaderComponent newComponent(Element compEl) 
      throws PSLoaderException
   {
      PSLoaderComponent comp = null;
      try
      {
         Constructor compCtor = m_compClass.getConstructor( new Class[]
            { Element.class });
         comp = (PSLoaderComponent) compCtor.newInstance(
            new Object[] {compEl} );
      }
      catch (Exception e)
      {
         throw new PSLoaderException(IPSLoaderErrors.UNEXPECTED_ERROR,
            e.getLocalizedMessage());
      }

      return comp;
   }
    
   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_list.hashCode();
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if (obj instanceof PSComponentList)
      {
         PSComponentList obj2 = (PSComponentList) obj;
         return m_list.equals(obj2.m_list);
      }
      else
      {
         return false;
      }
   }

   /**
    * Get the XML node name of this object.
    * 
    * @return The XML node name, never <code>null</code> or empty.
    */
   public String getXmlNodeName()
   {
      return m_xmlNodeName;
   }
   
   /**
    * The XML node name of the component list, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   private String m_xmlNodeName;

   /**
    * The XML node name of the child component, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   private String m_childXmlNode;
   
   /**
    * The list component, over zero or more <code>PSLoaderComponent</code>,
    * never <code>null</code>, but may be empty.
    */
   private List m_list = new ArrayList();

   /**
    * The class of the component in the list, <code>m_list</code>. Initialized
    * by constructor, never <code>null</code> after that. This is the base class
    * if the list contains multiple class objects.
    */
   private Class m_compClass;   
   
   /**
    * Determines whether it is a list of single class objects. <code>true</code>
    * if it is single class object list; <code>false</code> if it is multiple 
    * object list. Initlialized by the constructor, never modified after that.
    */
   private boolean m_singleClassList;
   
}
