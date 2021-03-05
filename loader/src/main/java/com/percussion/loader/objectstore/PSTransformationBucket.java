/*[ PSTransformationBucket.java ]**********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Encapsulates a container for Transformations.
 */
public abstract class PSTransformationBucket extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Default constructor
    */
   public PSTransformationBucket()
   {
      /**
       * Initialize the component list
       */
      setComponentList();
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    * <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSTransformationBucket(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      setComponentList();
      fromXml(source);
   }

   /**
    * Determines if the object contains any components.
    *
    * @return <code>true</code> if it contains one or more components;
    *    <code>false</code> otherwise.
    */
   public boolean isEmpty()
   {
      return m_transformations.isEmpty();
   }

   /**
    * Get the list of parameter definitions.
    *
    * @return An iterator over one or more {@link #PSTransformationDef}
    *    objects, never <code>null</code>.
    */
   public Iterator getTransformations()
   {
      return m_transformations.getComponents();
   }

   /**
    *
    * @return
    */
   public PSComponentList getTransformationsList()
   {
      return m_transformations;
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * <!ELEMENT XXX (PSXTransformationDef+)>
    * </code></pre>
    * where XXX is the result of {@link #getXmlNodeName()}.
    *
    * @throws IllegalArgumentException If <code>doc</code> is <code>null</code>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      return m_transformations.toXml(doc);
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      m_transformations.fromXml(sourceNode);
   }

   /**
    * Add a {@link #PSTransformationDef} to this bucket
    *
    * @param p A {@link #PSTransformationDef} to add, may not
    *    be <code>null</code>.
    */
   public void addTransformation(PSTransformationDef p)
   {
      if (p == null)
         throw new IllegalArgumentException("p must not be null.");

      m_transformations.addComponent(p);
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_transformations.hashCode();
   }

   /**
    *  see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;

      PSTransformationBucket obj2 = (PSTransformationBucket) obj;
      return m_transformations.equals(obj2.m_transformations);
   }

   /**
    * Set the component list, may be override by derived classes if needed
    */
   protected void setComponentList()
   {
      m_transformations = new
         PSComponentList(
         getXmlNodeName(),
         PSTransformationDef.XML_NODE_NAME,
         PSTransformationDef.class);
   }

   /**
    * abstract method to return xml node name.
    */
   public abstract String getXmlNodeName();

   /**
    * A list of transformations, initialized by constructor,
    * never <code>null</code>, but may be
    * empty after that.
    */
   protected PSComponentList m_transformations;
}
