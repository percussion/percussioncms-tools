/******************************************************************************
 *
 * [ PSItemContext.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.loader;

import com.percussion.cms.objectstore.ws.PSClientItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.objectstore.PSExtractorDef;
import com.percussion.loader.objectstore.PSLoaderComponent;
import com.percussion.util.PSXMLDomUtil;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class is used to hold all of the information associated with the
 * transformation of a resource into content items, including the original data
 * and the resulting standard item. This is passed from step to step 
 * throughout the processing of the data. (For those of you familiar with the
 * Rhythmyx server, it is very similar to the PSExecutionData.)
 * <p>Throughout this class, any reference to a type of 'text' is determined by
 * looking at the leading part of the mime type. If it is 'text' (e.g., as in 
 * text/plain) or application/xml, then the resource is considered text, 
 * otherwise it is considered binary.
 */
public class PSItemContext 
   extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Creates and initializes the context using the supplied properties of a 
    * resource.
    * 
    * @param resourceId a resource identifier that uniquely identifies the 
    *    item being created. This depends on the content selector 
    *    and might be a url, file, etc. Not <code>null</code> or empty.
    * 
    * @param data The raw data from the resource. May be 
    *    <code>null</code>. This class does not take ownership 
    *    of the stream. However, it does immediately read all 
    *    available data during ctor initialization so the
    *    calling method can close the stream upon return.
    * 
    * @param mimetype The IANA name for the type (e.g., text/plain). Must be
    *    supplied.
    * 
    * @throws IllegalArgumentException if data is <code>null</code> 
    *    or mimetype is <code>null</code> or empty. 
    */
   public PSItemContext(String resourceId, InputStream data, String mimetype)
   {
      if (resourceId == null || mimetype == null)
         throw new IllegalArgumentException(
            "resourceId and mimetype must not be null.");

      if (resourceId.trim().length() == 0 
         || mimetype.trim().length() == 0)
         throw new IllegalArgumentException(
            "resourceId and mimetype must not be empty.");

      m_resId = resourceId;
      m_mimeType = mimetype;

      // if data is not null try to read in the content
      // setting the checksum and length
      try
      {
         if (data != null)
         {
            byte[] rawData = PSLoaderUtils.getRawData(data);
            setChecksum(PSLoaderUtils.calcChecksum(rawData));
            setResourceDataLength(rawData.length);
         }
      }
      catch (IOException ignored)
      {
         ignored.printStackTrace();
      }
   }

   /**
    * Create this object from its XML representation
    *
    * @param source The source element.  See {@link #toXml(Document)} for
    * the expected format.  May not be <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException <code>source</code> is malformed.
    * @throws PSLoaderException if any other error occurs.
    */
   public PSItemContext(Element source)
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
    * <!ELEMENT PSXItemContext (PSXLocator?, PSExtractorDef?)>
    * <!ATTLIST PSXItemContext
    *    resId CDATA #REQUIRED
    *    rootResId CDATA #REQUIRED
    *    statuse CDATA #REQUIRED
    *    mimeType CDATA #REQUIRED
    *    checkSum CDATA #REQUIRED
    *    totalLength CDATA #REQUIRED
    *    charset CDATA
    *    lastScanTime CDATA
    *    lastLoadTime CDATA
    * >
    * </code></pre>
    * <p>
    * Note: <code>transient</code> objects will not be serialized.
    * 
    * @throws IllegalArgumentException If <code>doc</code>
    *    is <code>null</code>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_RESID, m_resId);
      root.setAttribute(XML_ATTR_ROOTRESID, m_rootResourceId);
      root.setAttribute(XML_ATTR_STATUS, m_status);
      root.setAttribute(XML_ATTR_MIMETYPE, m_mimeType);
      root.setAttribute(XML_ATTR_CHECKSUM, Long.toString(m_checkSum));
      root.setAttribute(XML_ATTR_TOTALLENGTH, Long.toString(m_totalLength));
      if (m_charset != null)
         root.setAttribute(XML_ATTR_CHARSET, m_charset);
      if (m_lastScan != null)
         root.setAttribute(XML_ATTR_LASTSCANTIME, 
            Long.toString(m_lastScan.getTime()));
      if (m_lastLoad != null)
         root.setAttribute(XML_ATTR_LASTLOADTIME, 
            Long.toString(m_lastLoad.getTime()));
      
      if (m_locator != null)
      {
         Element locatorEl = m_locator.toXml(doc);
         root.appendChild(locatorEl);
      }
      
      if (m_extractorDef != null)
      {
         Element extractorEl = m_extractorDef.toXml(doc);
         root.appendChild(extractorEl);
      }
      
      return root;
   }
   
   // see PSLoaderComponent#fromXml(Element)
   public void fromXml(Element srcNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(srcNode, XML_NODE_NAME);

      m_resId = getRequiredAttribute(srcNode, XML_ATTR_RESID);
      m_rootResourceId = getRequiredAttribute(srcNode, XML_ATTR_ROOTRESID);
      m_status = getRequiredAttribute(srcNode, XML_ATTR_STATUS);
      m_mimeType = getRequiredAttribute(srcNode, XML_ATTR_MIMETYPE);
      m_checkSum = PSXMLDomUtil.checkAttributeLong(srcNode, XML_ATTR_CHECKSUM,
         true);
      m_totalLength = PSXMLDomUtil.checkAttributeLong(srcNode, 
         XML_ATTR_TOTALLENGTH, true);
         
      m_charset =  srcNode.getAttribute(XML_ATTR_CHARSET);
      
      String lastScanTime = srcNode.getAttribute(XML_ATTR_LASTSCANTIME);
      if (lastScanTime != null && lastScanTime.trim().length() > 0)
         m_lastScan = new Date(Long.parseLong(lastScanTime));
      
      String lastLoadTime = srcNode.getAttribute(XML_ATTR_LASTLOADTIME);
      if (lastLoadTime != null && lastLoadTime.trim().length() > 0)
         m_lastLoad = new Date(Long.parseLong(lastLoadTime));
         
      Element elem = PSXMLDomUtil.getFirstElementChild(srcNode);
      if (elem != null)
      {
         if (elem.getNodeName().equals(PSLocator.XML_NODE_NAME))
         {
            m_locator = new PSLocator(elem);
            elem = PSXMLDomUtil.getNextElementSibling(elem);
         }
         if ( (elem != null) &&
              elem.getNodeName().equals(PSExtractorDef.XML_NODE_NAME) )
         {
            m_extractorDef = new PSExtractorDef(elem);
         }
      }
   }

   // see PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if (!(obj instanceof PSExtractorDef))
         return false;

      PSItemContext obj2 = (PSItemContext) obj;

      return equals(m_lastScan, obj2.m_lastScan) &&
             equals(m_lastLoad, obj2.m_lastLoad) &&
             equals(m_resId, obj2.m_resId) &&
             equals(m_rootResourceId, obj2.m_rootResourceId) &&
             equals(m_mimeType, obj2.m_mimeType) &&
             equals(m_charset, obj2.m_charset) &&
             equals(m_status, obj2.m_status) &&
             equals(m_locator, obj2.m_locator) &&
             equals(m_extractorDef, obj2.m_extractorDef) &&
             equals(m_staticData, obj2.m_staticData) &&
             equals(m_node, obj2.m_node) &&
             equals(m_dataObj, obj2.m_dataObj) &&
             equals(m_item, obj2.m_item) &&
             m_checkSum == obj2.m_checkSum &&
             m_totalLength == obj2.m_totalLength;
   }
   
   /**
    * Compare 2 given objects.
    * 
    * @param value1 The first compared object, it may be <code>null</code>
    * @param value2 The second compared object, it may be <code>null</code>
    * 
    * @return <code>true</code> if they are equal, which means either both
    *    of them are <code>null</code>, or <code>equals()</code> method return
    *    <code>true</code>; otherwise return <code>false</code>.
    */
   private boolean equals(Object value1, Object value2)
   {
      if (value1 == null || value2 == null)
         return value1 == null && value2 == null;
      else 
         return value1.equals(value2);
   }
   
   // see PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return (m_lastScan != null ? m_lastScan.hashCode() : 0) +
             (m_lastLoad != null ? m_lastLoad.hashCode() : 0) +
             (m_resId != null ? m_resId.hashCode() : 0) +
             (m_rootResourceId != null ? m_rootResourceId.hashCode() : 0) +
             (m_mimeType != null ? m_mimeType.hashCode() : 0) +
             (m_charset != null ? m_charset.hashCode() : 0) +
             (m_status != null ? m_status.hashCode() : 0) +
             (m_locator != null ? m_locator.hashCode() : 0) +
             (m_extractorDef != null ? m_extractorDef.hashCode() : 0) +
             (m_staticData != null ? m_staticData.hashCode() : 0) +
             (m_node != null ? m_node.hashCode() : 0) +
             (m_dataObj != null ? m_dataObj.hashCode() : 0) +
             (m_item != null ? m_item.hashCode() : 0) +
             (int)m_checkSum +
             (int)m_totalLength;
   }
      
   /**
    * Get the resource identifier of this item.
    * 
    * @return the resource identifier uniquely identifying this item in the
    *    tree, never <code>null</code> or empty.
    */
   public String getResourceId()
   {
      return m_resId;
   }
   
   /**
    * Get the file name from the resource identifier, which does not include
    * the file extension if there is any. For example, if the resource
    * identifier is: ../country/flag.gif, then the file name is: "flag".
    * 
    * @return The retrieved file name, never <code>null</code> or empty.
    */
    public String getResourceFileName()
    {
      String fileName = new File(m_resId).getName();
      int dotIndex = fileName.lastIndexOf(".");
      if (dotIndex != -1 && fileName.length() > dotIndex)
      {
         fileName = fileName.substring(0, dotIndex);
      }
      
      return fileName;
    }

   /**
    * This is the mime type of the resource obtained during scanning. If the
    * type could not be determined during scanning, then the mime-type will 
    * attempt to be determined using the resource's extension and a mime-map.
    * If the resource does not have an extension, a guess is made by looking at
    * a few characters and picking either text/plain or 
    * application/octet-stream.
    * 
    * @return The IANA mime type. Never <code>null</code> or empty.
    */
   public String getResourceMimeType()
   {
      return m_mimeType;  
   } 
   
   /**
    * Accessor to set the mime type of this resource.
    * 
    * @param strType mimetype string. Never <code>null</code> or 
    *    empty.
    * 
    * @throws IllegalArgumentException if <code>strType</code> is 
    *    invalid.
    */
   public void setResourceMimeType(String strType)
   {
      if (strType == null || strType.trim().length() == 0)
         throw new IllegalArgumentException(
            "strType must not be null or empty");
                          
      m_mimeType = strType;  
   }

   /**
    * This is the IANA character set of the original resource, if available.
    * If not available, the default platform character set is used for text 
    * resources and the empty string is returned for non-text resources.
    * 
    * @return The IANA name for the character set of the original resource.
    */
   public String getResourceCharset()
   {
      return m_charset;   
   }
   
   /**
    * @return The number of bytes in the resource read by the scanner (not 
    *    necessarily the number of chars in the String). May be -1 to 
    *    represent it has not been set.
    */
   public long getResourceDataLength()
   {
      return m_totalLength;
   }
   
   /**
    * @param bytes The number of bytes in the resource read by the scanner (not 
    *    necessarily the number of chars in the String).
    */
   public void setResourceDataLength(long bytes)
   {
      m_totalLength = bytes;
   }

   /**
    * This object is created with the data from a resource. It is then 
    * analyzed and converted to some standard item format by an static 
    * entity. Once that processed item has been placed in this context, it
    * is available with this method.
    * 
    * @return The object representation of the item, never <code>null</code>.
    *    This is an object representing the StandardItem.xsd schema. The 
    *    returned object is not a copy, so any changes made affect the object
    *    owned by this context. Even though this is true, if the caller makes
    *    any changes to the object, they should call {@link #setItem} again
    *    to notify the context of these changes. This allows the context to 
    *    perform caching and other optimizations wisely.
    * 
    * @throws IllegalStateException If this method is called before the {@link
    *    #setItem(PSClientItem)} method has been called. 
    */
   public PSClientItem getItem()
   {
      if (m_item == null)
         throw new IllegalStateException("m_item cannot be null");
         
      return m_item;  
   }
   
   /**
    * See {@link #getItem} for a description.
    * 
    * @param item The Rhythmyx item extracted from the resource associated with
    *    this context. Never <code>null</code>. This method takes ownership of
    *    the item and the calling class should not change the item after it 
    *    has called this method.
    *    
    * @throws IllegalArgumentException if item is <code>null</code>.
    */
   public void setItem( PSClientItem item )
   {
      if (item == null)
         throw new IllegalArgumentException("item may not be null");
         
      m_item = item;
   }
   
   /**
    * Clear the cached content, which may be set by {@link 
    * #setItem(PSClientItem)} or {@link #setStaticData(byte[])}. This should be
    * called after the content is no longer needed, so that the memory can be
    * G.C.'ed.
    */
   public void clearCachedContent()
   {
      m_item = null;
      m_staticData = null;
   }
   
   /**
    * If an item has been set on this context, that item is converted to the
    * StandardItem.xsd schema format and returned. 
    * 
    * @return The current item as a Standard Item formatted document, never
    *    <code>null</code>.
    *    
    * @throws IllegalStateException If called before the item has been set on
    *    this context.
    */
   public Document getStandardItemDoc()
   {
      if (m_item == null)
         throw new IllegalStateException(
            "The current client item must not be null");
      
      Document doc = PSXmlDocumentBuilder.createXmlDocument();
      Element itemEl = m_item.toMinXml(doc, true, true, true, true);
      PSXmlDocumentBuilder.replaceRoot(doc, itemEl);
      
      return doc;  
   }
      
   /**
    * The item status is an indicator of this item's 'newness'. i.e., it is
    * one of the STATUS_xxx values. 
    *  
    * @return The value set by the {@link #setStatus(String)} method.
    *    The default status is new. Never <code>null</code> or empty.
    *    
    * @see #STATUS_ERROR
    * @see #STATUS_EXCLUDED
    * @see #STATUS_MODIFIED
    * @see #STATUS_NEW
    * @see #STATUS_UNCHANGED
    */
   public String getStatus()
   {  
      return m_status;
   }
   
   /**
    * See {@link #getStatus} for details.
    * 
    * @param status One of the STATUS_xxx values. The locator will be set
    *    to <code>null</code> if it is <code>STATUS_NEW</code>.
    *    
    * @throws IllegalArgumentException if newStatus is not one of the STATUS_xxx
    *    values.
    */
   public void setStatus(String status)
   {
      if ((! status.equalsIgnoreCase(STATUS_ERROR))    &&
          (! status.equalsIgnoreCase(STATUS_EXCLUDED)) &&
          (! status.equalsIgnoreCase(STATUS_MODIFIED)) &&
          (! status.equalsIgnoreCase(STATUS_NEW))      &&
          (! status.equalsIgnoreCase(STATUS_UNCHANGED)))
      {
         throw new IllegalArgumentException("status must be one of STATUS_XXXX");
      }
   
      m_status = status;
      
      if (status.equalsIgnoreCase(STATUS_NEW))
         m_locator = null;
   }

   /**
    * Public Accessor for data object
    *
    * @param o user object to attach to this item, may be <code>null</code>. 
    */
   public void setDataObject(Object o)
   {
      m_dataObj = o;     
   }

   /**
    * Public Accessor for the user data object.
    * 
    * @return the user object, may be <code>null</code>.
    */
   public Object getDataObject()
   {
      return m_dataObj;
   }
   
   /**
    * Accessor to read the checksum for the item. This checksum
    * is calculated over the original bytes, which is from {@link 
    * IPSContentSelector#retrieve(IPSContentTreeNode)}.
    * 
    * @return long value representing the checksum, <code>-1</code> if has not
    *    been set yet.
    */
   public long getChecksum()
   {
      return m_checkSum;
   }
   
   /**
    * Set the checksum for this object.
    * 
    * @param checksum The to be set checksum
    */
   public void setChecksum(long checksum)
   {
      m_checkSum = checksum;
   }
   
   /**
    * Gets the extension of the resource. 
    * 
    * @return String of the file extension. May be empty.
    */
   public String getResourceExtension()
   {                       
      int nIndex = m_resId.lastIndexOf(".");
      
      if (nIndex < 0)
         return "";

      return m_resId.substring(nIndex + 1, m_resId.length());
   }
   
   /**
    * Set the locator, the object that can be used to locate the content in
    * Rhythmyx server.
    * 
    * @param locator The to be set locator object, may not be <code>null</code>.
    */
   public void setLocator(PSLocator locator)
   {
      if (locator == null)
         throw new IllegalArgumentException("locator may not be null");
      
      m_locator = locator;
   }
   
   /**
    * Set the locator, the object that can be used to locate the content in
    * Rhythmyx server.
    * 
    * @return The locator. It may be <code>null</code> if has not been set or
    *    it is an static item.
    */
   public PSLocator getLocator()
   {
      return m_locator;
   }

   /**
    * Set the extractor definition.
    * 
    * @param extractorDef The to be set extractor definition, may not be
    *    <code>null</code>
    */
   public void setExtractorDef(PSExtractorDef extractorDef)
   {
      if (extractorDef == null)
         throw new IllegalStateException("extractorDef may not be null");

      m_extractorDef = extractorDef;
   }
   
   /**
    * Get the extractor definition.
    * 
    * @return The extractor definition, never <code>null</code>.
    * 
    * @throws IllegalStateException if it has not been set yet.
    */
   public PSExtractorDef getExtractorDef()
   {
      if (m_extractorDef == null)
         throw new IllegalStateException("m_extractorDef must not be null");
         
      return m_extractorDef;
   }
   
   /**
    * Determines whether the object is an static item or not.
    * 
    * @return <code>true</code> if the object is any static item; 
    *    <code>false</code> otherwise.
    */
   public boolean isStaticItem()
   {
      if (m_extractorDef == null)
         throw new IllegalStateException("m_extractorDef must not be null");
         
      return m_extractorDef.getType().equals(PSExtractorDef.TYPE_STATIC);
   }
   
   /**
    * Set the data that is extracted by an static extractor.
    * 
    * @param data The data that is extracted by an static extractor, may not 
    *    <code>null</code>
    */
   public void setStaticData(byte[] data)
   {
      if (data == null)
         throw new IllegalArgumentException("data may not be null");
      
      m_staticData = data;
   }
   
   /**
    * Get the data that was extracted by an static extractor. This must be
    * called after the {@link #setStaticData(byte[])}
    * 
    * @return The extracted data, never <code>null</code>
    */
   public byte[] getStaticData()
   {
      if (m_staticData == null)
         throw new IllegalStateException("m_staticData must not be null");

      return m_staticData;
   }

   /**
    * Set the content tree node for this object.
    * 
    * @param node The to be set content tree node, may not be <code>null</code>
    */
   public void setContentTreeNode(IPSContentTreeNode node)
   {
      if (node == null)
         throw new IllegalArgumentException("node may not be null");
      
      m_node = node;
   }
   
   /**
    * Get the content tree node of this object. This can only be called 
    * after the {@link #setContentTreeNode(IPSContentTreeNode)}
    * 
    * @return The content tree node, never <code>null</code>.
    * 
    * @throws IllegalStateException if the node has not been set yet.
    */
   public IPSContentTreeNode getContentTreeNode()
   {
      if (m_node == null)
         throw new IllegalStateException("m_node has not been set yet");

      return m_node;
   }
   
   /**
    * Accessor to set last scan time.
    * 
    * @param d never May be <code>null</code>
    */
   public void setLastScan(Date d)
   {
      m_lastScan = d;
   }
   
   /**
    * Accessor to set last load time.
    * 
    * @param d never May be <code>null</code>
    */
   public void setLastLoad(Date d)
   {
      m_lastLoad = d;
   }

   /**
    * Last scan time.
    * 
    * @return Date may be null.
    */
   public Date getLastScan()
   {
      return m_lastScan;
   }
   
   /**
    * Last load time.
    * 
    * @return Date may be null.
    */
   public Date getLastLoad()
   {
      return m_lastLoad;
   }

   /**
    * Get the root resource id.
    * 
    * @return non-null <codeString</code> if exist; otherwise return 
    *    <code>null</code>.
    */
   public String getRootResourceId()
   {
      return m_rootResourceId;
   }
   
   /**
    * Set the root resource id.
    * 
    * @param rootResourceId The to be set root resource id, it may not be 
    *    <code>null</code> or empty.
    */
   public void setRootResourceId(String rootResourceId)
   {
      if (rootResourceId == null || rootResourceId.trim().length() == 0)
         throw new IllegalArgumentException(
            "rootResourceId may not be null or emptry");
         
      m_rootResourceId = rootResourceId;
   }

   /**
    * One of the allowed values for the item's delta status. The meaning of 
    * this value is that a new item will be created on the Rhythmyx server.
    * 
    * @see #getStatus
    * @see #setStatus
    */
   public static final String STATUS_NEW = "New";
   
   /**
    * One of the allowed values for the item's delta status. The meaning of 
    * this value is that this item matches an existing item on the Rhythmyx 
    * server. The existing item will be updated with the content from this item.
    * 
    * @see #getStatus
    * @see #setStatus
    */
   public static final String STATUS_MODIFIED = "Modified";
   
   /**
    * One of the allowed values for the item's delta status. The meaning of 
    * this value is that this item will not be sent to the server because it
    * has not been modified since the last time it was loaded.
    * 
    * @see #getStatus
    * @see #setStatus
    */
   public static final String STATUS_UNCHANGED = "Unchanged";

   /**
    * One of the allowed values for the item's delta status. The meaning of 
    * this value is that this item will not be sent to the server because it
    * is excluded.
    * 
    * @see #getStatus
    * @see #setStatus
    */
   public static final String STATUS_EXCLUDED = "Excluded";
   
   /**
    * One of the allowed values for the item's delta status. The meaning of 
    * this value is that this item will not be sent to the server because its
    * content is invalid. For example, its contents may not be a well formed 
    * XML, but well formed XML is required for the extractor of this item.
    * 
    * @see #getStatus
    * @see #setStatus
    */
   public static final String STATUS_ERROR = "Error";

   /**
    * The XML node name of this object.
    */
   final public static String XML_NODE_NAME = "PSXItemContext";

   
   /**
    * Date of last scan. Set using <code>setLastScan</code>. May be 
    * <code>null</code>
    */
   private Date m_lastScan = null;
   
   /**
    * Date of last update. Set using <code>setLastLoad</code>.
    * May be <code>null</code>
    */
   private Date m_lastLoad = null;

   /**
    * A resource identifier that uniquely identifies the item 
    * beeing created. This depends on the content selector and might be a 
    * url, file, etc.. Initialized in constructor, never <code>null</code> 
    * or empty.
    */
   protected String m_resId;
   
   /**
    * The IANA name for the type (e.g., text/plain). Initialized in
    * constructor, never <code>null</code> or empty.
    */
   protected String m_mimeType;
   
   /**
    * The java name of the charset, if the data is text. Initialized
    * in constructor and may be<code>null</code> or empty 
    * if mime type is binary.
    */
   protected String m_charset;
   
   /**
    * Checksum over m_streamData, set by {@link #setChecksum(long)}, 
    * defaults to -1. 
    */
   protected long m_checkSum = -1;
   
   /**
    * The actual length of the content. This defaults to -1 to 
    * specified it hasn't been calculated. Set using 
    * {@link #setResourceDataLength(long)} accessors. Get using
    * {@link #getResourceDataLength()}. 
    */
   protected long m_totalLength = -1;
   
   /**
    * The status of the object. Initialized to <code>null</code>, only modified
    * by <code>setStatus(String)</code>
    * 
    */
   protected String m_status = null; 
   
   /**
    * The information that can be used to locate the content in the Rhythmyx
    * server. Initialized to <code>null</code>, only modified by 
    * <code>setLocator</code>. It will remain to be <code>null</code> for 
    * static items.
    */
   protected PSLocator m_locator = null;

   /**
    * The definition of the extractor that is interested or can extract thing
    * from this item. Initialized to <code>null</code>, only modified by
    * <code>setExtractorDef()</code>.
    */   
   private PSExtractorDef m_extractorDef = null; 
   
   /**
    * The (search) root resource id (or URL), which may be <code>null</code> 
    * if not set.
    */
   private String m_rootResourceId = null;
   
   /**
    * This is a place holder for the data that is extracted by an static
    * extractor. This data is used for run-time only, it will not be 
    * saved in XML (serialized) file.
    */
   private transient byte[] m_staticData = null;
   
   /**
    * The content tree node that contains this object. It may be 
    * <code>null</code> if has not been set by <code>setContentTreeNode()</code>
    * This data is used for run-time only, it will not be saved in XML 
    * (serialized) file.    
    */
   private transient IPSContentTreeNode m_node = null;

   /**
    * Data object storage that may possibly represent target content
    * that this node references. May be <code>null</code>. Note: this is 
    * a transient data, it will not be serialized, so that it will not be 
    * available after un-serialize the object.
    * 
    */
   private transient Object m_dataObj = null;

   /**
    * The item that contains extracted data. May be <code>null</code> if has
    * not been set yet. This data is used for run-time only, it will not be 
    * saved in XML (serialized) file.
    */
   private transient PSClientItem m_item = null;
   
   // Constants for XML element and attributes
   final private static String XML_ATTR_LASTSCANTIME = "lastScanTime";
   final private static String XML_ATTR_LASTLOADTIME = "lastLoadTime";
   final private static String XML_ATTR_MIMETYPE = "mimeType";
   final private static String XML_ATTR_CHARSET = "charset";
   final private static String XML_ATTR_CHECKSUM = "checkSum";
   final private static String XML_ATTR_TOTALLENGTH = "totalLength";
   final private static String XML_ATTR_STATUS = "status";
   final private static String XML_ATTR_RESID = "resId";
   final private static String XML_ATTR_ROOTRESID = "rootResId";
   
}
