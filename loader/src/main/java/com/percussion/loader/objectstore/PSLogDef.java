/*[ PSLogDef.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.xml.PSXmlDocumentBuilder;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Represents a PSLogDef xml object. This class derives from
 * {@link #PSLoaderComponent} but it is different in the way it
 * functions. This class encapsulates an <code>Element</code>
 * of xml and provides public methods to Update/Insert/Delete
 * xml data within <code>m_xmlElem</code>. During a to/from xml
 * this class just writes/reads the whole Element into <code>
 * m_xmlElement</code>.
 */
public class PSLogDef extends PSLoaderComponent implements java.io.Serializable
{
   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  May not be <code>null</code>.
    *
    * @throws IllegalArgumentException If <code>source</code> is
    *    <code>null</code>.
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    */
   public PSLogDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Accessor to get the default log level.
    *
    * @param strContext The name of the context
    *    to get the <code>XML_NODE_PRIORITY</code>
    *    from. If <code>null</code> or empty the
    *    <code>XML_NODE_ROOT</code> context is used.
    *
    * @return String. The log level.
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if <code>strContext</code>
    *    does not exist in <code>m_xmlElem</code>.
    */
   public String getDefaultLogLevel(String strContext)
   {
      // If no context is specified return root context
      if (strContext == null || strContext.trim().length() == 0)
         strContext = XML_NODE_ROOT;

      // Threshold if the context does not exist throw
      // exception
      Element contextEl = findElementWithAttr(m_xmlElem,
         strContext, "", "");

      if (contextEl == null)
         throw new IllegalArgumentException(
            strContext + " context does not exist");


      Element prioEl = findElementWithAttr(contextEl,
         XML_NODE_PRIORITY, "", "");

      if (prioEl == null)
         return DEFAULT_LOG_LEVEL;

      NamedNodeMap attrs = prioEl.getAttributes();

      if (attrs == null || attrs.getLength() < 1)
         return DEFAULT_LOG_LEVEL;

      Node prioAttr = attrs.getNamedItem(XML_ATTR_VALUE);

      if (prioAttr == null)
         return DEFAULT_LOG_LEVEL;

      return prioAttr.getNodeValue();
   }

   /**
    * Sets the default log level
    *
    * @param strLogLevel String. Never <code>null</code> or
    *    empty. Assumed to be one of the defines below.
    */
   public void setDefaultLogLevel(String strLogLevel)
   {
      // If no context is specified return root context
      if (strLogLevel == null || strLogLevel.trim().length() == 0)
      {
         throw new IllegalArgumentException(
            "strLogLevel must not be null or empty");
      }

      // Threshold if the context does not exist throw
      // exception
      Element contextEl = findElementWithAttr(m_xmlElem,
         XML_NODE_ROOT, "", "");

      if (contextEl == null)
         throw new IllegalArgumentException(
            "root context does not exist");


      Element prioEl = findElementWithAttr(contextEl,
         XML_NODE_PRIORITY, "", "");

      if (prioEl == null)
         throw new IllegalArgumentException(
            "root priority level does not exist");

      NamedNodeMap attrs = prioEl.getAttributes();
      Node prioAttr = attrs.getNamedItem(XML_ATTR_VALUE);

      prioAttr.setNodeValue(strLogLevel);
   }

   /**
    * Accessor to get the log location.
    *
    * @param strContext The name of the context
    *    to get the <code>XML_NODE_APPENDREF</code>
    *    <code>XML_NODE_REF</code> value from.
    *    If <code>null</code> or empty the
    *    <code>XML_NODE_ROOT</code> context is used.
    *
    * @return String. The log location.
    *    Never <code>null</code>. May be empty.
    *
    * @throws IllegalArgumentException if <code>strContext</code>
    *    does not exist in <code>m_xmlElem</code>.
    */
   public String getLogLocation(String strContext)
   {
      // If no context is specified return root context
      if (strContext == null || strContext.trim().length() == 0)
         strContext = XML_NODE_ROOT;

      // Threshold if the context does not exist throw
      // exception
      Element contextEl = findElementWithAttr(m_xmlElem,
         strContext, "", "");

      if (contextEl == null)
         throw new IllegalArgumentException(
            strContext + " context does not exist");

      Element appendEl = findElementWithAttr(contextEl,
         XML_NODE_APPENDREF, "", "");

      if (appendEl == null)
         return "";

      NamedNodeMap attrs = appendEl.getAttributes();

      if (attrs == null || attrs.getLength() < 1)
         return "";

      Node refAttr = attrs.getNamedItem(XML_ATTR_REF);

      if (refAttr == null)
         return "";

      return refAttr.getNodeValue();
   }


   /**
    * Accessor to check if the fileAppender is logging.
    *
    * @return boolean <code>true</code> if in use, <code>false</code>
    *    otherwise.
    */
   public boolean isFile()
   {
      Element contextEl = findElementWithAttr(m_xmlElem,
         XML_NODE_ROOT, "", "");

      if (contextEl == null)
         throw new IllegalArgumentException(
            "root context does not exist");

      Element appendEl = findElementWithAttr(contextEl,
         XML_NODE_APPENDREF, XML_ATTR_REF, FILE_APPENDER);

      if (appendEl != null)
         return true;

      return false;
   }

   /**
    * Accessor to check if the logDispatcher is logging.
    *
    * @return boolean <code>true</code> if in use, <code>false</code>
    *    otherwise.
    */
   public boolean isConsole()
   {
      Element contextEl = findElementWithAttr(m_xmlElem,
         XML_NODE_ROOT, "", "");

      if (contextEl == null)
         throw new IllegalArgumentException(
            "root context does not exist");

      Element appendEl = findElementWithAttr(contextEl,
         XML_NODE_APPENDREF, XML_ATTR_REF, CONSOLE_APPENDER);

      if (appendEl != null)
         return true;

      return false;
   }

   /**
    * Accessor to set the fileAppender to log
    *
    * @param bOn <code>true</code> to turn it on, otherwise
    *    <code>false</code>.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setFile(boolean bOn)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // create the appender element and associated attributes
      Element elem = doc.createElement(XML_NODE_APPENDREF);
      Attr  valAtt = doc.createAttribute(XML_ATTR_REF);
      valAtt.setValue(this.FILE_APPENDER);
      elem.setAttributeNode(valAtt);

      // Retrieve the root node, the parent of the priority node
      // If doesn't exist add one.
      Element rootEl = findElementWithAttr(config,
         XML_NODE_ROOT, "", "");

      if (rootEl == null)
      {
         // add the root
         rootEl = doc.createElementNS(XML_NODE_NS, XML_NODE_ROOT);
         config.appendChild(rootEl);
      }

      // Threshold if the priority already exists
      // remove it.
      Element elemOld = findElementWithAttr(rootEl,
         XML_NODE_APPENDREF, XML_ATTR_REF, FILE_APPENDER);

      if (elemOld != null)
         rootEl.removeChild(elemOld);

      if (bOn)
      {
         // add the appender element to the configuration element
         // This also functions as an update if the node exists already.
         rootEl.appendChild(elem);
      }

      // Save the changes
      fromXml(config);
   }

   /**
    * Accessor to set the fileAppender to log
    *
    * @param bOn <code>true</code> to turn it on, otherwise
    *    <code>false</code>.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setConsole(boolean bOn)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // create the appender element and associated attributes
      Element elem = doc.createElement(XML_NODE_APPENDREF);
      Attr  valAtt = doc.createAttribute(XML_ATTR_REF);
      valAtt.setValue(CONSOLE_APPENDER);
      elem.setAttributeNode(valAtt);

      // Retrieve the root node, the parent of the priority node
      // If doesn't exist add one.
      Element rootEl = findElementWithAttr(config,
         XML_NODE_ROOT, "", "");

      if (rootEl == null)
      {
         // add the root
         rootEl = doc.createElementNS(XML_NODE_NS, XML_NODE_ROOT);
         config.appendChild(rootEl);
      }

      // Threshold if the priority already exists
      // remove it.
      Element elemOld = findElementWithAttr(rootEl,
         XML_NODE_APPENDREF, XML_ATTR_REF, CONSOLE_APPENDER);

      if (elemOld != null);
      rootEl.removeChild(elemOld);

      if (bOn)
      {
         // add the appender element to the configuration element
         // This also functions as an update if the node exists already.
         rootEl.appendChild(elem);
      }

      // Save the changes
      fromXml(config);
   }

   /**
    * Accessor to get a file specified parameter
    * within <code>m_xmlElem</code>.
    *
    * @param strAppender The name of the file appender.
    *    Never <code>null</code> or empty.
    *
    * @param strParamName The name of the parameter to get the
    *    value of. Never <code>null</code> or empty.
    *
    * @return String. The value location.
    *    Never <code>null</code>. May be empty if parameter does not
    *    exist.
    *
    * @throws IllegalArgumentException if any parameters are invalid. Or,
    *    if the specified appender <code>strAppender</code> does not exist.
    */
   public String getFileParamValue(String strAppender, String strParamName)
   {
      if (strAppender == null || strParamName == null)
         throw new IllegalArgumentException(
            "strAppender and strParamName must not be null");

      if (strAppender.trim().length() == 0 ||
         strParamName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strAppender and strParamName must not be empty");

      // Get the appender
      Element appendEl = findElementWithAttr(m_xmlElem,
         XML_NODE_APPENDER, XML_ATTR_NAME, strAppender);

      if (appendEl == null)
         throw new IllegalArgumentException(
            "Appender '" + strAppender + "' does not exist");

      Element paramEl = findElementWithAttr(appendEl,
         XML_NODE_PARAM, XML_ATTR_NAME, strParamName);

      if (paramEl == null)
         return "";

      NamedNodeMap attrs = paramEl.getAttributes();
      Node valueAttr = attrs.getNamedItem(XML_ATTR_VALUE);
      return valueAttr.getNodeValue();
   }

   /**
    * Accessor to get the xml element encapsulated by this
    * class
    *
    * @return Element. Never <code>null</code>.
    */
   public Element getElement()
   {
      return m_xmlElem;
   }

   /**
    * Adds/Updates a param to the layout identified by
    * <code>strAppendName</code> and <code>strLayoutName</code>.
    * If the param already exists
    * it is first removed.
    *
    * @param strAppendName the name of the appender.
    *    Never <code>null</code>or empty.
    *
    * @param strLayoutName the name of the layout
    *    Never <code>null</code> or empty.
    *
    * @param strName the name of the param
    *    Never <code>null</code> or empty.
    *
    * @param strVal the value of the param
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameters are invalid.
    *    Or if the specified appender does not exist.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setLayoutParam(String strAppendName,
      String strLayoutName,
      String strName,
      String strVal)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (strAppendName == null
         || strLayoutName == null
         || strName == null
         || strVal == null)
         throw new IllegalArgumentException(
            "strAppendName, strLayoutName," +
            "strName and strVal must not be null");

      if (strAppendName.trim().length() == 0
         || strLayoutName.trim().length() == 0
         || strName.trim().length() == 0
         || strVal.trim().length() == 0)
         throw new IllegalArgumentException(
            "strAppendName, strLayoutName," +
            "strName and strVal must not be empty");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // Threshold if the appender does not exist stop
      Element appender = findElementWithAttr(config,
         XML_NODE_APPENDER, XML_ATTR_NAME, strAppendName);

      if (appender == null)
         throw new IllegalArgumentException(
            "appender does not exist");

      // Threshold if the layout does not exist stop
      Element layout = findElementWithAttr(config,
         XML_NODE_LAYOUT, XML_ATTR_CLASS, strLayoutName);

      if (layout == null)
         throw new IllegalArgumentException(
            "layout does not exist");

      // create the param element and associated attributes
      Element elem = doc.createElement(XML_NODE_PARAM);
      Attr  nameAtt = doc.createAttribute(XML_ATTR_NAME);
      nameAtt.setValue(strName);
      Attr  valueAtt = doc.createAttribute(XML_ATTR_VALUE);
      valueAtt.setValue(strVal);
      elem.setAttributeNode(nameAtt);
      elem.setAttributeNode(valueAtt);

      // Threshold if the layout already has the same param
      // remove it.
      Element elemOld = findElementWithAttr(layout,
         XML_NODE_PARAM, XML_ATTR_NAME, strName);

      if (elemOld != null)
         layout.removeChild(elemOld);

      // add the param element to the appender element
      // This also functions as an update if the node exists already.
      layout.appendChild(elem);
      // Save the changes
      fromXml(config);
   }

   /**
    * Adds/Updates a layout to the appender identified by
    * <code>strName</code>. If the layout already exists
    * it is first removed.
    *
    * @param strName the name of the appender. Never <code>null</code>
    *    or empty.
    *
    * @param strClassName the name of the layout class
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameters are invalid.
    *    Or if the specified appender does not exist.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setAppenderLayout(String strName, String strClassName)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (strName == null || strClassName == null)
         throw new IllegalArgumentException(
            "strName, strClassName must not be null");

      if (strName.trim().length() == 0 || strClassName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName, strClassName must not be empty");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // Threshold if the appender does not exist stop
      Element appender = findElementWithAttr(config,
         XML_NODE_APPENDER, XML_ATTR_NAME, strName);

      if (appender == null)
         throw new IllegalArgumentException(
            "appender does not exist");

      // create the param element and associated attributes
      Element elem = doc.createElement(XML_NODE_LAYOUT);
      Attr  classAtt = doc.createAttribute(XML_ATTR_CLASS);
      classAtt.setValue(strClassName);
      elem.setAttributeNode(classAtt);

      // Threshold if the appender already has the same layout
      // remove it.
      Element elemOld = findElementWithAttr(config,
         XML_NODE_LAYOUT, XML_ATTR_CLASS, strClassName);

      if (elemOld != null)
         appender.removeChild(elemOld);

      // add the layout element to the appender element
      // This also functions as an update if the node exists already.
      appender.appendChild(elem);
      // Save the changes
      fromXml(config);
   }

   /**
    * Convenience method for looking up a node based on a specific
    * attribute value of the node.
    *
    * @param config The node to start looking. Never <code>null</code>.
    *
    * @param strElemName The element name to look for. Never <code>null
    *    </code> or empty.
    *
    * @param strAttr An attribute name to match <code>strVal</code>.
    *    Never <code>null</code> but may be empty. If empty if
    *    returns the first <code>strElemName</code> it finds.
    *
    * @param strVal An value to match. Never <code>null</code>
    *    but may be empty. If empty if returns the
    *    first <code>strElemName</code> it finds.
    *
    * @throws IllegalArgumentException given any invalid parameters
    */
   private Element findElementWithAttr(Element config, String strElemName,
      String strAttr, String strVal)
   {

      if (config == null || strElemName == null ||
         strAttr == null || strVal == null)
         throw new IllegalArgumentException(
            "config, strElemName, strAttr, and strVal must not be null");

      if (strElemName.trim().length() == 0)
         throw new IllegalArgumentException(
            "strElemName must not be empty");


      // Check to see if this appender exists
      NodeList list = config.getElementsByTagName(strElemName);
      Element elem = null;

      if (strAttr.trim().length() == 0
         || strVal.trim().length() == 0)
      {
         if (list.item(0) != null)
            return (Element) list.item(0);
      }

      for (int i=0; i<list.getLength(); i++)
      {
         Node n = list.item(i);
         NamedNodeMap attrs = n.getAttributes();

         Node nameAttr = attrs.getNamedItem(strAttr);

         if (nameAttr == null)
            continue;

         if (nameAttr.getNodeValue().trim().equals(strVal.trim()))
            return (Element) n;
      }

      return null;
   }

   /**
    * Adds/Updates a param to the appender identified by
    * <code>strName</code>. If the param already exists
    * it is first removed.
    *
    * @param strName the name of the appender. Never <code>null</code>
    *    or empty.
    *
    * @param strParamName the name of the param
    *    Never <code>null</code> or empty.
    *
    * @param strParamVal the value of the param
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameters are invalid.
    *    Or if the specified appender does not exist.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setAppenderParam(String strName, String strParamName,
      String strParamVal)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (strName == null || strParamName == null || strParamVal == null)
         throw new IllegalArgumentException(
            "strName, strParamName and strParamVal must not be null");

      if (strName.trim().length() == 0 || strParamName.trim().length() == 0
         || strParamVal.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName, strParamName and strParamVal must not be empty");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // Threshold if the appender does not exist stop
      Element appender = findElementWithAttr(config,
         XML_NODE_APPENDER, XML_ATTR_NAME, strName);

      if (appender == null)
         throw new IllegalArgumentException(
            "appender does not exist");

      // create the param element and associated attributes
      Element elem = doc.createElement(XML_NODE_PARAM);
      Attr  nameAtt = doc.createAttribute(XML_ATTR_NAME);
      nameAtt.setValue(strParamName);
      Attr  valueAtt = doc.createAttribute(XML_ATTR_VALUE);
      valueAtt.setValue(strParamVal);
      elem.setAttributeNode(nameAtt);
      elem.setAttributeNode(valueAtt);

      // Threshold if the appender already has the same param
      // remove it.
      Element elemOld = findElementWithAttr(config,
         XML_NODE_PARAM, XML_ATTR_NAME, strParamName);

      if (elemOld != null)
         appender.removeChild(elemOld);

      // add the param element to the appender element
      // This also functions as an update if the node exists already.
      appender.appendChild(elem);
      // Save the changes
      fromXml(config);
   }

   /**
    * Adds/Updates an appender to the log4j configuration object.
    * If the appender already exists it is first removed.
    *
    * @param strName the name of the appender. Never <code>null</code>
    *    or empty.
    *
    * @param strClass the fully qualified class name of the object.
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameters are invalid.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setAppender(String strName, String strClass)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (strName == null || strClass == null)
         throw new IllegalArgumentException(
            "strName and strClass must not be null");

      if (strName.trim().length() == 0 || strClass.trim().length() == 0)
         throw new IllegalArgumentException(
            "strName and strClass must not be empty");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // create the appender element and associated attributes
      Element elem = doc.createElement(XML_NODE_APPENDER);
      Attr  nameAtt = doc.createAttribute(XML_ATTR_NAME);
      nameAtt.setValue(strName);
      Attr  classAtt = doc.createAttribute(XML_ATTR_CLASS);
      classAtt.setValue(strClass);
      elem.setAttributeNode(nameAtt);
      elem.setAttributeNode(classAtt);

      // Threshold if the appender already has the same param
      // remove it.
      Element elemOld = findElementWithAttr(config,
         XML_NODE_APPENDER, XML_ATTR_NAME, strName);

      if (elemOld != null)
         config.removeChild(elemOld);

      // add the appender element to the configuration element
      // This also functions as an update if the node exists already.
      config.appendChild(elem);
      // Save the changes
      fromXml(config);
   }

   /**
    * Adds/Updates the root priority configuration object.
    * If the root priority already exists it is first removed.
    * If there is no root node, one will be added
    *
    * @param strPriority the value of the priority node.
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameters are invalid.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setRootPriority(String strPriority)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (strPriority == null)
         throw new IllegalArgumentException(
            "strPriority must not be null");

      if (strPriority.trim().length() == 0)
         throw new IllegalArgumentException(
            "strPriority must not be empty");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // create the appender element and associated attributes
      Element elem = doc.createElement(XML_NODE_PRIORITY);
      Attr  valAtt = doc.createAttribute(XML_ATTR_VALUE);
      valAtt.setValue(strPriority);
      elem.setAttributeNode(valAtt);

      // Retrieve the root node, the parent of the priority node
      // If doesn't exist add one.
      Element rootEl = findElementWithAttr(config,
         XML_NODE_ROOT, "", "");

      if (rootEl == null)
      {
         // add the root
         rootEl = doc.createElementNS(XML_NODE_NS, XML_NODE_ROOT);
         config.appendChild(rootEl);
      }

      // Threshold if the priority already exists
      // remove it.
      Element elemOld = findElementWithAttr(rootEl,
         XML_NODE_PRIORITY, "", "");

      if (elemOld != null)
         rootEl.removeChild(elemOld);

      // add the appender element to the configuration element
      // This also functions as an update if the node exists already.
      rootEl.appendChild(elem);
      // Save the changes
      fromXml(config);
   }

   /**
    * Adds/Updates the appender reference configuration object.
    * If the appender reference already exists it is first removed.
    * If there is no root node, one will be added
    *
    * @param strValue the value of the appender reference node.
    *    Never <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if any parameters are invalid.
    *
    * @see {@link #fromXml} for description of other exceptions that
    *    may be thrown.
    */
   public void setAppenderRef(String strRef)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (strRef == null)
         throw new IllegalArgumentException(
            "strRef must not be null");

      if (strRef.trim().length() == 0)
         throw new IllegalArgumentException(
            "strRef must not be empty");

      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element config = toXml(doc);

      // create the appender element and associated attributes
      Element elem = doc.createElement(XML_NODE_APPENDREF);
      Attr  valAtt = doc.createAttribute(XML_ATTR_REF);
      valAtt.setValue(strRef);
      elem.setAttributeNode(valAtt);

      // Retrieve the root node, the parent of the priority node
      // If doesn't exist add one.
      Element rootEl = findElementWithAttr(config,
         XML_NODE_ROOT, "", "");

      if (rootEl == null)
      {
         // add the root
         rootEl = doc.createElement(XML_NODE_ROOT);
         config.appendChild(rootEl);
      }

      // Threshold if the appender reference already exists
      // remove it.
      Element elemOld = findElementWithAttr(rootEl,
         XML_NODE_APPENDREF, "", "");

      if (elemOld != null)
         rootEl.removeChild(elemOld);

      // add the appender element to the configuration element
      // This also functions as an update if the node exists already.
      rootEl.appendChild(elem);
      // Save the changes
      fromXml(config);
   }

   /**
    * @see PSLoaderComponent#fromXml(Element)
    */
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      m_xmlElem = sourceNode;
   }

   /**
    * PSLoaderComponent#hashCode()
    */
   public int hashCode()
   {
      return m_xmlElem.hashCode();
   }

   /**
    * @see PSLoaderComponent#equals(Object)
    */
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSLogDef))
         return false;

      PSLogDef obj2 = (PSLogDef) obj;

      return contentEqual(obj2.m_xmlElem);
   }

   /**
    * Convience method to test whether the xml content of
    * <code>e</code> matches <code>m_xmlElement</code>
    * xml by a string comparison.
    *
    * @param e an Element to compare. Assume not
    *    <code>null</code>.
    *
    * @return boolean if <code>true</code> the Element is
    *    equal to <code>m_xmlElem</code>, otherwise not equal.
    */
   private boolean contentEqual(Element e)
   {
      if (e == null)
         return false;

      String strCmp = PSXmlDocumentBuilder.toString(e);
      String strThis = PSXmlDocumentBuilder.toString(m_xmlElem);

      return strCmp.equals(strThis);
   }

   /**
    * see {@link PSLoaderComponent} for description
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc must not be null");

      Element oldEntry = doc.getElementById(XML_NODE_TAG);

      if (oldEntry != null)
      {
         doc.removeChild(oldEntry);
         m_xmlElem = (Element) doc.importNode(m_xmlElem, true);
      }
      else
      {
         m_xmlElem = (Element) doc.importNode(m_xmlElem, true);
      }

      return m_xmlElem;
   }

   /**
    * see {@link #PSLoaderComponent} for description.
    */
   protected void validateElement(Element sourceNode, String xmlNodeName)
      throws PSUnknownNodeTypeException
   {
      super.validateElement(sourceNode, xmlNodeName);
   }

   /**
    * Copy construction.
    *
    * @param obj2 The to be copied or cloned object, must be an instance of
    *    <code>PSLogDef</code>.
    */
   public void copyFrom(PSLogDef obj2)
   {
      if (!(obj2 instanceof PSLogDef))
         throw new IllegalArgumentException(
            "obj2 must be an instance of PSLogDef");

      m_xmlElem = obj2.m_xmlElem;
   }

   /**
    * The namespace of the <code>XML_NODE_TAG</code> node.
    */
   final public static String XML_NODE_NS = "log4j";

   /**
    * The XML node name of this object.
    */
   final public static String XML_NODE_TAG = "configuration";

   /**
    * The XML node full name of this object.
    */
   final public static String XML_NODE_NAME = XML_NODE_NS + ":" + XML_NODE_TAG;

   /**
    * The value of the xml element, initialized in constructor,
    * Never <code>null</code>.
    */
   protected Element m_xmlElem;

   /**
    * If the default log level is not specified in the xml
    * element <code>m_xmlElement</code>. We return this
    * constant.
    */
   final static protected String DEFAULT_LOG_LEVEL = "info";

   // Private constants for XML attribute and element name
   final static protected String XML_NODE_APPENDER = "appender";
   final static protected String XML_NODE_LAYOUT = "layout";
   final static protected String XML_NODE_PRIORITY = "priority";
   final static protected String XML_NODE_APPENDREF = "appender-ref";
   final static protected String XML_NODE_ROOT = "root";
   final static protected String XML_ATTR_NAME = "name";
   final static protected String XML_ATTR_REF = "ref";
   final static protected String XML_ATTR_CLASS = "class";
   final static protected String XML_ATTR_VALUE = "value";
   final static protected String XML_NODE_PARAM = "param";
   //log4j log level constants
   final static public String INFO = "info";
   final static public String DEBUG = "debug";
   final static public String WARN = "warn";
   final static public String ERROR = "error";
   final static public String FATAL = "fatal";
   final static public String OFF = "off";
   final public static String FILE = "File";
   final static public String FILE_APPENDER = "fileAppender";
   final static public String CONSOLE_APPENDER = "logDispatcher";
   final static public String FILE_PARAM_APPEND = "Append";
   final static public String FILE_PARAM_OVERWRITE = "Overwrite";
   final static public String FILE_PARAM_BACKUP = "Backup";
   final static public String FILE_PARAM_MAXBACKUP = "MaxBackups";
}