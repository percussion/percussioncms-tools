/******************************************************************************
 *
 * [ PSLogMessage.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.percussion.design.objectstore.IPSObjectStoreErrors;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;

/**
 * Handles formatting of messages stored in the resource bundle
 * (supplied by derived classes) using code ids and their arguments.
 * Localization is also supported.
 */
public class PSLogMessage
{
   /**
    * Convenient constructor, calls {@link #PSLogMessage(int,Object,
    * PSItemContext) PSLogMessage(int,Object, null)}.
    */
   public PSLogMessage(int msgCode, Object singleArg)
   {
      this(msgCode, singleArg, null);
   }

   /**
    * Convenient constructor, calls {@link #PSLogMessage(int,Object[],
    * PSItemContext) PSLogMessage(int,Object[]{Object}, PSItemContext)}
    */
   public PSLogMessage(int msgCode, Object singleArg,
      PSItemContext item)
   {
      this(msgCode, new Object[] { singleArg }, item);
   }

   /**
    * Convenient constructor, calls {@link #PSLogMessage(int,Object[],
    * PSItemContext,int) PSLogMessage(int,Object[], null, LEVEL_INFO)}
    */
   public PSLogMessage(int msgCode, Object[] arrayArgs)
   {
      this(msgCode, arrayArgs, null, LEVEL_INFO);
   }

   /**
    * Convenient constructor, calls {@link #PSLogMessage(int,Object[],
    * PSItemContext,int) PSLogMessage(int,Object[],PSItemContext,
    * LEVEL_INFO)}
    */
   public PSLogMessage(int msgCode, Object[] arrayArgs,
      PSItemContext item)
   {
      this(msgCode, arrayArgs, item, LEVEL_INFO);
   }
   
   /**
    * Convenient constructor, calls {@link #PSLogMessage(int,Object[],
    * PSItemContext,int) PSLogMessage(int,Object[],null,int)}
    */
   public PSLogMessage(int msgCode, Object[] arrayArgs, int logLevel)
   {
      this(msgCode, arrayArgs, null, logLevel);
   }
   
   /**
    * Constructs an instance from the given parameters.
    *
    * @param msgCode message id within the Resource file.
    * 
    * @param arrayArg format parameters, may be <code>null</code> or empty.
    * 
    * @param item the item context assocciatd with this log message, may be
    *    <code>null</code>.
    *    
    * @param level The log level, one of the <code>LEVEL_XXX</code>
    */
   public PSLogMessage(int msgCode, Object[] arrayArgs,
      PSItemContext item, int level)
   {
      if ( (level != LEVEL_INFO) &&
           (level != LEVEL_DEBUG) &&
           (level != LEVEL_WARN) &&
           (level != LEVEL_ERROR) &&
           (level != LEVEL_FATAL) )
      {
         throw new IllegalArgumentException(
            "level must be one of the LEVEL_XXX values");
      }
      m_level = level;
      
      for (int i = 0; arrayArgs != null && i < arrayArgs.length; i++)
      {
         if (arrayArgs[i] == null)
            arrayArgs[i] = "";
      }

      m_code = msgCode;
      m_args = arrayArgs;
      m_itemContext = item;
   }

   /**
    * Construct a message from its XML representation.
    *
    * @param source The root element of this object's XML representation.
    * Format expected is defined by the {@link #toXml(Document) toXml} method
    * documentation.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException if <code>source</code> is
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException if the XML element node does not
    *    represent a type supported by the class.
    */
   public PSLogMessage(Element source) throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      if (!getXmlNodeName().equals(source.getNodeName()))
      {
         Object[] args = { getXmlNodeName(), source.getNodeName() };
         throw new PSUnknownNodeTypeException(
            IPSObjectStoreErrors.XML_ELEMENT_WRONG_TYPE, args);
      }

      m_code = PSXMLDomUtil.checkAttributeInt(source, XML_ATTR_MSG_CODE, true);
      m_level = PSXMLDomUtil.checkAttributeInt(source, XML_ATTR_LEVEL, true);

      // get args
      List argList = new ArrayList();
      PSXmlTreeWalker tree = new PSXmlTreeWalker(source);
      Element arg = tree.getNextElement(XML_ELEMENT_ARG,
         PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN);
      while (arg != null)
      {
         argList.add(tree.getElementData());
         arg = tree.getNextElement(XML_ELEMENT_ARG,
            PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS);
      }
      m_args = argList.toArray();
      m_itemContext = null;
   }

   /**
    * This method is called to create an XML element node with the
    * appropriate format for this object. The format is:
    *
    * <pre><code>
    * <!ELEMENT PSXLoaderLogMessage (Arg*)
    * <!ATTLIST PSXLoaderLogMessage
    *    msgCode CDATA #REQUIRED
    *    level CDATA #REQUIRED
    * >
    * <!ELEMENT Arg (#PCDATA)>
    * </code></pre>
    *
    * @param doc The document to use to create the element, may not be
    * <code>null</code>.
    *
    * @return the newly created XML element node, never <code>null</code>
    *
    * @throws IllegalArgumentException if <code>doc</code> is <code>null</code>.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(getXmlNodeName());
      root.setAttribute(XML_ATTR_MSG_CODE, String.valueOf(m_code));
      root.setAttribute(XML_ATTR_LEVEL, Integer.toString(m_level));

      for (int i = 0; m_args != null && i < m_args.length; i++)
      {
         if (m_args[i] == null)
            PSXmlDocumentBuilder.addEmptyElement(doc, root, XML_ELEMENT_ARG);
         else
            PSXmlDocumentBuilder.addElement(doc, root, XML_ELEMENT_ARG,
               m_args[i].toString());
      }

      return root;
   }

   /**
    * Returns the localized detail message of this message.
    *
    * @param locale The locale to generate the message in.  If <code>null
    *    </code>, the default locale is used.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getLocalizedMessage(Locale locale)
   {
      return createMessage(m_code, m_args, locale);
   }

   /**
    * Returns the localized detail message of this exception in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getLocalizedMessage()
   {
      return getLocalizedMessage(Locale.getDefault());
   }

   /**
    * Returns the localized detail message of this message in the
    * default locale for this system.
    *
    * @return  The localized detail message, never <code>null</code>, may be
    * empty.
    */
   public String getMessage()
   {
      return getLocalizedMessage();
   }

   /**
    * Returns a description of this message. The format used is
    * "Class: Message"
    *
    * @return the description, never <code>null</code> or empty.
    */
   public String toString()
   {
      return getLocalizedMessage();
   }

   /**
    * Get the parsing code associated with this message.
    *
    * @return The code id
    */
   public int getCode()
   {
      return m_code;
   }

   /**
    * Get the log level.
    * 
    * @return The log level, it is one of the <code>LEVEL_XXX</code> values.
    */
   public int getLevel()
   {
      return m_level;
   }
   
   /**
    * Get the parsing arguments associated with this log message.
    *
    * @return The arguments, may be <code>null</code>.
    */
   public Object[] getArguments()
   {
      return m_args;
   }

   /**
    * Get the item context associated with this log message.
    *
    * @return the item context associated wwith this log message, may be
    *    <code>null</code>.
    */
   public PSItemContext getItemContext()
   {
      return m_itemContext;
   }

   /**
    * Create a formatted message for messages taking an array of
    * arguments. Be sure to store the arguments in the correct order in
    * the array, where {0} in the string is array element 0, etc.
    *
    * @param msgCode The code of the error string to load.
    *
    * @param arrayArgs  The array of arguments to use as the arguments
    *    in the error message, may be <code>null</code> or empty.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    *    default locale is used.
    *
    * @return The formatted message, never <code>null</code>. If the appropriate
    *    message cannot be created, a message is constructed from the msgCode
    *    and args and is returned.
    *
    */
   private String createMessage(int msgCode, Object[] arrayArgs,
      Locale loc)
   {
      if (arrayArgs == null)
         arrayArgs = new Object[0];

      String msg = getLogText(msgCode, true, loc);

      if (msg != null)
      {
         try
         {
            msg = MessageFormat.format(msg, arrayArgs);
         }
         catch (IllegalArgumentException e)
         {
            // some problem with formatting
            msg = null;
         }
      }

      if (msg == null)
      {
         String sArgs = "";
         String sep = "";

         for (int i = 0; i < arrayArgs.length; i++) {
            sArgs += sep + arrayArgs[i].toString();
            sep = "; ";
         }

         msg = "";
         msg += String.valueOf(msgCode) + ": " + sArgs;
      }

      return msg;
   }

   /**
    * Get the log text associated with the specified error code.
    *
    * @param code The error code.
    *
    * @param nullNotFound  If <code>true</code>, return <code>null</code> if the
    *    log string is not found, if <code>false</code>, return the code as
    *    a String if the log string is not found.
    *
    * @param loc The locale to use, may be <code>null</code>, in which case the
    * default locale is used.
    *
    * @return the log text, may be <code>null</code> or empty.
    */
   public String getLogText(int code, boolean nullNotFound, Locale loc)
   {
      if (loc == null)
         loc = Locale.getDefault();

      try
      {
         ResourceBundle logList = getLoggingStringBundle(loc);
         if (logList != null)
            return logList.getString(String.valueOf(code));
      }
      catch (MissingResourceException e)
      {
         /* don't exception, return below based on nullNotFound value */
      }

      return (nullNotFound ? null : String.valueOf(code));
   }

   /**
    * Override {@link Object#equals(Object)}. This is a logical comparison, 
    * not a full (property) comparison.
    * 
    * @param otherObj The to be compared object. It may be <code>null</code>.
    * 
    * @return <code>true</code> if the specified object is logically equal to
    *    this object, which means all primary properties are equal; otherwise
    *    return <code>false</code>.
    */
   public boolean equals(Object otherObj)
   {
      if (! (otherObj instanceof PSLogMessage))
         return false;
      
      PSLogMessage other = (PSLogMessage) otherObj;
      
      final boolean contextsAreEqual =
         m_itemContext == other.m_itemContext
               || (m_itemContext != null && other.m_itemContext != null
                     && m_itemContext.getResourceId().equals(
                           other.m_itemContext.getResourceId())); 
      
      return (m_level == other.m_level) &&
             (m_code == other.m_code)   &&
             contextsAreEqual;
   }
   

   /**
    * Overrides {@link Object#hashCode()}.
    */
   @Override
   public int hashCode()
   {
      return m_level + m_code
            + hashCodeOr0(m_itemContext == null ? null : m_itemContext.getResourceId());
   }

   /**
    * Hash code of the provided object or 0 if the object is <code>null</code>.
    */
   private int hashCodeOr0(Object object)
   {
      return object == null ? 0 : object.hashCode();
   }


   /**
    * This method is used to get the string resources hash table for a
    * locale. If the resources are not already loaded for the locale,
    * they will be.
    *
    * @param loc The locale, assumed not <code>null</code>.
    *
    * @return the bundle, never <code>null</code>.
    */
   private ResourceBundle getLoggingStringBundle(Locale loc)
      throws MissingResourceException
   {
      if (ms_bundle == null)
      {
         ms_bundle = ResourceBundle.getBundle(getResourceBundleBaseName(), loc);
      }

      return ms_bundle;
   }

   /**
    * Get the base name of the resource bundle, a fully qualified class name
    *
    * @return The base name of the resource bundle, never <code>null</code>
    *    or empty.
    */
   protected String getResourceBundleBaseName()
   {
      return "com.percussion.loader.PSLoggingStringBundle";
   }

   /**
    * Get the XML node name (or the root element name) for this object when
    * serialized to and from XML.
    *
    * @return The XML node name, never <code>null</code> or empty.
    */
   protected String getXmlNodeName()
   {
      return "PSXLoaderLogMessage";
   }

   /**
    * See {@link org.apache.log4j.Priority.INFO_INT}
    */
   public final static int LEVEL_INFO = Level.INFO_INT;

   /**
    * See {@link org.apache.log4j.Priority.DEBUG_INT}
    */
   public final static int LEVEL_DEBUG = Level.DEBUG_INT;

   /**
    * See {@link org.apache.log4j.Priority.WARN_INT}
    */
   public final static int LEVEL_WARN = Level.WARN_INT;

   /**
    * See {@link org.apache.log4j.Priority.ERROR_INT}
    */
   public final static int LEVEL_ERROR = Level.ERROR_INT;

   /**
    * See {@link org.apache.log4j.Priority.FATAL_INT}
    */
   public final static int LEVEL_FATAL = Level.FATAL_INT;
   
   /**
    * The code of this log message defined in the resource bundle.
    */
   private int m_code;

   /**
    * The array of arguments to use to format the message with.  Set during
    * ctor, may be <code>null</code>, never modified after that.
    */
   private Object[] m_args;

   /**
    * The item ccontext associated with this log message. Initialized in
    * construcctors. May be <code>null</code>.
    */
   private PSItemContext m_itemContext;

   /**
    * 
    */
   private int m_level = LEVEL_INFO;
   
   /**
    * The resource bundle containing message formats.  <code>null</code>
    * until the first call to {@link #getErrorStringBundle(Locale)
    * getErrorStringBundle}, never <code>null</code> or modified after that
    * unless an exception occurred loading the bundle.
    */
   private static ResourceBundle ms_bundle = null;

   // xml serialization constants
   private static final String XML_ELEMENT_ARG = "Arg";
   private static final String XML_ATTR_MSG_CODE = "msgCode";
   private static final String XML_ATTR_LEVEL = "level";
}



