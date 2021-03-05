/*[ PSErrorHandlingDef.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.PSLoaderUtils;
import com.percussion.xml.PSXmlTreeWalker;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Represents a PSErrorHandlingDef xml object.
 */
public class PSErrorHandlingDef extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Default Constructor.
    */
   public PSErrorHandlingDef()
   {
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
   public PSErrorHandlingDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Set the <code>STOP_ON_ERROR_PROP</code> property.
    *
    * @return <code>true</code> we should stop on error; <code>false</code>
    *    otherwise.
    *
    * @throws PSLoaderException if the property does not exist
    */
   public boolean getStopOnError() throws PSLoaderException
   {
      PSProperty prop = getProperty(STOP_ON_ERROR_PROP);
      return prop.getValue().equals(XML_TRUE);
   }

   /**
    * Set the <code>STOP_ON_ERROR_PROP</code> property.
    *
    * @param b <code>true</code> if we should stop on error;
    *    <code>false</code> otherwise.
    *
    * @throws PSLoaderException if no such property exists
    */
   public void setStopOnError(boolean b) throws PSLoaderException
   {
      PSProperty prop = getProperty(STOP_ON_ERROR_PROP);
      prop.setValue(b ? XML_TRUE : XML_FALSE);
   }

   /**
    * Get the <code>EMAIL_ON_ERROR_PROP</code> property.
    *
    * @return <code>true</code> we should email on error; <code>false</code>
    *    otherwise.
    *
    * @throws PSLoaderException if the property does not exist
    */
   public boolean getEmailOnError() throws PSLoaderException
   {
      PSProperty prop = getProperty(EMAIL_ON_ERROR_PROP);
      return prop.getValue().equals(XML_TRUE);
   }

   /**
    * Set the <code>EMAIL_ON_ERROR_PROP</code> property.
    *
    * @param b <code>true</code> if we should email on error;
    *    <code>false</code> otherwise.
    *
    * @throws PSLoaderException if no such property exists
    */
   public void setEmailOnError(boolean b) throws PSLoaderException
   {
      PSProperty prop = getProperty(EMAIL_ON_ERROR_PROP);
      prop.setValue(b ? XML_TRUE : XML_FALSE);
   }

   /**
    * Get the <code>EMAIL_ON_SUCCESS_PROP</code> property.
    *
    * @return <code>true</code> we should email on success;
    *    <code>false</code> otherwise.
    *
    * @throws PSLoaderException if the property does not exist
    */
   public boolean getEmailOnSuccess() throws PSLoaderException
   {
      PSProperty prop = getProperty(EMAIL_ON_SUCCESS_PROP);
      return prop.getValue().equals(XML_TRUE);
   }

   /**
    * Set the <code>EMAIL_ON_SUCCESS_PROP</code> property.
    *
    * @param b <code>true</code> if we should email on success;
    *    <code>false</code> otherwise.
    *
    * @throws PSLoaderException if no such property exists
    */
   public void setEmailOnSuccess(boolean b) throws PSLoaderException
   {
      PSProperty prop = getProperty(EMAIL_ON_SUCCESS_PROP);
      prop.setValue(b ? XML_TRUE : XML_FALSE);
   }


   /**
    * Get the value of the <code>m_email</code> object. This object
    * contains all the recipients and cc recipients.
    *
    * @return The value of the <code>m_email</code> object,
    *    may be <code>null</code>.
    */
   public PSEmailDef getEmail()
   {
      return m_email;
   }

   /**
    * Set the value of the <code>m_email</code> object. This object
    * contains all the recipients and cc recipients.
    *
    * @param email a PSEmailDef. May be <code>null</code> to remove
    *    object.
    */
   public void setEmail(PSEmailDef email)
   {
      m_email = email;
   }

   /**
    * Get the list of PSProperty objects.
    *
    * @return An iterator over one or more <code>PSPropertyDef</code>
    *    objects, never <code>null</code>, but may be empty.
    */
   public Iterator getProperties()
   {
      return m_properties.getComponents();
   }

   /**
    * Add a property.
    *
    * @param comp The {@link PSProperty} to be added,
    *    may not <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>comp</code> is
    *    <code>null</code>.
    */
   public void addProperty(PSProperty comp)
   {
      if (comp == null)
         throw new IllegalArgumentException("comp may not be null");

      m_properties.addComponent(comp);
   }

   /**
    * Serializes this object's state to its XML representation.
    * The format is:
    * &lt;!ELEMENT PSXErrorHandlingDef (Properties, PSXEmailDef?)&gt;
    * </code></pre>
    *
    * @throws IllegalArgumentException If <code>doc</code>
    * is <code>null</code>
    *
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);

      /**
       * Write out the component list objects below
       */
      if (!m_properties.isEmpty())
         root.appendChild(m_properties.toXml(doc));

      if (m_email != null)
         root.appendChild(m_email.toXml(doc));

      return root;
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      PSXmlTreeWalker tree = new PSXmlTreeWalker(sourceNode);
      Element listEl = tree.getNextElement(
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);

      // get the property list
      m_properties.fromXml(listEl);

      // get the email info
      listEl = tree.getNextElement(PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      if (listEl != null)
         m_email = new PSEmailDef(listEl);
         
      // make sure the required properties exist
      boolean emailOnError = getEmailOnError();
      boolean emailOnSuccess = getEmailOnSuccess();
      getStopOnError();
      
      if ( (emailOnError || emailOnSuccess) && (m_email == null) )
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_NULL, PSEmailDef.XML_NODE_NAME);
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_email.hashCode()
         + m_properties.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSErrorHandlingDef))
         return false;

      PSErrorHandlingDef obj2 = (PSErrorHandlingDef) obj;

      if (obj2.m_email == null && m_email != null)
         return false;

      if (m_email == null && obj2.m_email != null)
         return false;

      // At this point either m_email, obj2.m_email
      // are either both null or both not null
      if (m_email != null)
      {
         return m_email.equals(obj2.m_email)
            && m_properties.equals(obj2.m_properties);
      }
      else
      {
         return m_properties.equals(obj2.m_properties);
      }
   }

   /**
    * Copy construction.
    *
    * @param obj2 The to be copied or cloned object, must be an instance of
    *    <code>PSErrorHandlingDef</code>.
    */
   public void copyFrom(PSErrorHandlingDef obj2)
   {
      if (!(obj2 instanceof PSErrorHandlingDef))
         throw new IllegalArgumentException(
            "obj2 must be an instance of PSErrorHandlingDef");

      m_email = obj2.m_email;
      m_properties = obj2.m_properties;
   }

  /**
   * Get the value of a property.
   *
   * @return The name of the property, may not <code>null</code> or empty.
   *
   * @throws PSLoaderException if cannot find the property
   */
   protected PSProperty getProperty(String name) throws PSLoaderException
   {
      return PSLoaderUtils.getProperty(name, getProperties());
   }


   /**
    * The XML node name of this object.
    */
   final public static String XML_NODE_NAME = "PSXErrorHandlingDef";


   /**
    * The {@link #PSXEmailDef} node, initialized by <code>setEmail</code>,
    * may be <code>null</code>
    */
   protected PSEmailDef m_email = null;

   /**
    * A list of properties, initialized in definition,
    * never <code>null</code>, may be empty.
    */
   protected PSComponentList m_properties = new PSComponentList(
      XML_NODE_PROPERTIES, PSProperty.XML_NODE_NAME, PSProperty.class);

   // public static constants - property names
   public final static String STOP_ON_ERROR_PROP = "stoponerror";
   public final static String EMAIL_ON_ERROR_PROP = "emailonerror";
   public final static String EMAIL_ON_SUCCESS_PROP = "emailonsuccess";
}
