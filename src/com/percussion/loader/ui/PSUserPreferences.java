/*[ PSUserPreferences.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.ui;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Encapsulates user preference settings.
 */
public class PSUserPreferences 
   extends PSLoaderComponent
   implements Serializable
{
  /**
   * Ctor, initializes the preferences with default. There are three mutually
   * exlusive options - Open New Descriptor, Open Last Descriptor, Open None.
   * Default being Open Last Descriptor.
   */
   public PSUserPreferences()
   {
      setIsLastDescriptor(true);
      setIsNewDescriptor(false);
      setIsNoneDescriptor(false);
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSUserPreferences(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }
   
   /**
    * Serializes this object's state to its XML representation.
    * The format is:
    * <pre><code>
    * <!ELEMENT PSXUserPreference EMPTY>
    * <!ATTLIST PSXUserPreference
    * isNew CDATA #REQUIRED
    * isNone CDATA #REQUIRED
    * isLast CDATA #REQUIRED
    * lastPath CDATA #REQUIRED
    * >
    * </code></pre>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_IS_NEW, boolean2String(m_isNew));
      root.setAttribute(XML_ATTR_IS_NONE, boolean2String(m_isNone));
      root.setAttribute(XML_ATTR_IS_LAST, boolean2String(m_isLast));
      root.setAttribute(XML_ATTR_LAST_PATH, m_path);

      return root;
    }

   // See PSLoaderComponent#fromXml(Element)
   public void fromXml(Element srcNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(srcNode, XML_NODE_NAME);

      String isNew = getRequiredAttribute(srcNode, XML_ATTR_IS_NEW);
      String isNone = getRequiredAttribute(srcNode, XML_ATTR_IS_NONE);
      String isLast = getRequiredAttribute(srcNode, XML_ATTR_IS_LAST);
      
      m_path = srcNode.getAttribute(XML_ATTR_LAST_PATH);
      if (m_path == null)
         m_path = "";
      m_isNew = string2Boolean(getRequiredAttribute(srcNode, 
         XML_ATTR_IS_NEW));
      m_isNone = string2Boolean(getRequiredAttribute(srcNode, 
         XML_ATTR_IS_NONE));
      m_isLast = string2Boolean(getRequiredAttribute(srcNode, 
         XML_ATTR_IS_LAST));      
   }
   
   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSUserPreferences))
         return false;

      PSUserPreferences obj2 = (PSUserPreferences) obj;

      return m_path.equals(obj2.m_path)
         && m_isNew == obj2.m_isNew
         && m_isNone == obj2.m_isNone
         && m_isLast == obj2.m_isLast;
   }
   
   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_path.hashCode() + 
         (m_isNew ? 1 : 0) +
         (m_isNone ? 1 : 0) +
         (m_isLast ? 1 : 0);
   }
   
   /**
    * Sets the preference to open a new descriptor.
    *
    * @param isNew, if <code>true</code> sets flag for opening a new decriptor.
    */
   public void setIsNewDescriptor(boolean isNew)
   {
      m_isNew = isNew;
   }

   /**
    * Sets the preference to open the last descriptor.
    *
    * @param isLast, if <code>true</code> sets flag for opening the last
    * decriptor.
    */
   public void setIsLastDescriptor(boolean isLast)
   {
      m_isLast  = isLast;
   }

   /**
    * Sets the preference to open descriptors manually.
    *
    * @param isNone, if <code>true</code> sets the flag to open descriptor.
    */
   public void setIsNoneDescriptor(boolean isNone)
   {
      m_isNone = isNone;
   }

   /**
    * Gets the preference for opening a new decriptor.
    *
    * @return, flag for opening a new descriptor.
    */
   public boolean isNewDescriptor()
   {
      return m_isNew;
   }

   /**
    * Gets the preference for opening the last decriptor.
    *
    * @return, flag for opening a last descriptor.
    */
   public boolean isLastDescriptor()
   {
      return m_isLast;
   }

   /**
    * Gets the preference for opening a decriptor manually.
    *
    * @return, flag for opening a descriptor manually.
    */
   public boolean isNoDescriptor()
   {
      return m_isNone;
   }

   /**
    * Sets the path for last opened descriptor.
    *
    * @param path for last opened descriptor, may not be <code>null</code> or
    * empty.
    *
    * @throws IllegalArgumentException if the arguent is invalid.
    */
   public void setLastDescPath(String path)
   {
      if (path == null || path.length() == 0)
         throw new IllegalArgumentException(
            "path of the last descriptor cannot be null");
      m_path = path;
   }

   /**
    * Gets the path for last opened descriptor.
    *
    * @return path for last opened descriptor, never <code>null</code>, may be
    * empty.
    */
   public String getLastDescPath()
   {
      if (m_path == null)
         return "";
      return m_path;
   }


   /**
    * Save the specified descriptor directory to the file system.
    * 
    * @param descDir The to be saved descriptor directory, may not be 
    *    <code>null</code> or empty.
    */
   public static void saveLastDescPath(String descDir)
   {
      if (descDir == null || descDir.trim().length() == 0)
         throw new IllegalArgumentException("descDir may not be null");
         
      PSUserPreferences pref = deserialize();
      pref.setLastDescPath(descDir);
      PSUserPreferences.serialize(pref);
   }
   
   /**
    * Deserializes the {@link PSUserPreferences} object if serialization file
    * exists else new object is created with default settings. The location of
    * the serialization file is the user's home directory, the file name is
    * "PSPreferences".
    *
    * @return, deserialized  {@link PSUserPreferences} or default object, never
    * <code>null</code>.
    */
   public static PSUserPreferences deserialize()
   {
      FileInputStream fis = null;
      PSUserPreferences prefer = null;
      try
      {
         fis = new FileInputStream(PREF_FILEPATH);
         Document doc = PSXmlDocumentBuilder.createXmlDocument(fis, false);
         Element preferEl = doc.getDocumentElement();
         prefer = new PSUserPreferences(preferEl);
      }
      catch(Exception ex)
      {
         //if the serialized file doesn't exist take the default settings.
         prefer = new PSUserPreferences();
      }
      finally
      {
         try
         {
            if (fis != null)
               fis.close();
         }
         catch(IOException ex)
         {
            //should never be here.
         }
      }
      return prefer;
   }

   /**
    * Serializes the {@link PSUserPreferences} object to a file named
    * "PSPreferences" in the user's home directory.
    *
    * @param prefer, user preference object to be serialized, never <code>null
    * </code>.
    *
    * @throw IllegalArgumentException if the parameter is invalid.
    */
   public static void serialize(PSUserPreferences prefer)
   {
      if (prefer == null)
         throw new IllegalArgumentException(
         "user preference object cannot be null");

      FileOutputStream fos = null;
      try
      {
         Document doc = PSXmlDocumentBuilder.createXmlDocument();
         Element prefEl = prefer.toXml(doc);
         PSXmlDocumentBuilder.replaceRoot(doc, prefEl);
         
         fos = new FileOutputStream(PREF_FILEPATH);
         PSXmlDocumentBuilder.write(doc, fos);         
         fos.close();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
         try {
            if (fos == null)
               fos.close();
         }
         catch (Exception ex){}
      }
   }

   /**
    * Convenient method, get a string constant from a given boolean value.
    * 
    * @param bValue The tested value.
    * 
    * @return <code>PSLoaderComponent.YES_STRING</code> if the <code>bValue
    *    </code> is true; otherwise return <code>PSLoaderComponent.NO_STRING
    *    </code>;
    */
   private String boolean2String(boolean bValue)
   {
      return bValue ? 
         PSLoaderComponent.YES_STRING : PSLoaderComponent.NO_STRING;
   }

   /**
    * Get a boolean value from a given string
    * 
    * @param sValue The tested string, assume not <code>null</code>.
    * 
    * @return <code>true</code> if the string equals (case insensitive)
    *    <code>PSLoaderComponent.YES_STRING</code>; otherwise return
    *    <code>false</code>.
    */   
   private boolean string2Boolean(String sValue)
   {
      return sValue.equalsIgnoreCase(PSLoaderComponent.YES_STRING);
   }
   
   /**
    * Constant specifying the location of serialized data.
    */
   public static final String PREF_FILEPATH = System.getProperty("user.home") +
       File.separator + "PSXUserPreference.xml";

   /**
    * The XML node name of this object.
    */
   final public static String XML_NODE_NAME = "PSXUserPreferences";
   
   /**
    * Path of last opened descriptor. Never <code>null</code>, may be empty.
    */
   private String m_path = "";

   /**
    * Flag for opening a new descriptor. Initalized in the ctor.
    */
   private boolean m_isNew;

   /**
    * Flag for opening the last descriptor. Initalized in the ctor.
    */
   private boolean m_isLast;

   /**
    * Flag for opening  descriptors manually. Initalized in the ctor.
    */
   private boolean m_isNone;
   
   // Private constants for XML document
   final private static String XML_ATTR_IS_NEW = "isNew";
   final private static String XML_ATTR_IS_NONE = "isNone";
   final private static String XML_ATTR_IS_LAST = "isLast";
   final private static String XML_ATTR_LAST_PATH = "lastPath";
}