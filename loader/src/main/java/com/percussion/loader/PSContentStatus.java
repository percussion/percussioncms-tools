/*[ PSContentStatus.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.objectstore.PSLoaderDescriptor;
import com.percussion.loader.selector.PSContentTreeModel;
import com.percussion.util.PSXMLDomUtil;

import java.util.Iterator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class contains the status of a scanned content tree and its related
 * descriptor.
 */
public class PSContentStatus implements java.io.Serializable
{
   /**
    * Constructs the object with a content tree and its related descriptor
    * 
    * @param tree The content tree, may not be <code>null</code>.
    * 
    * @param descriptor The descriptor that is used to create the content tree.
    */
   public PSContentStatus(IPSContentTree tree, PSLoaderDescriptor descriptor)
   {
      if (tree == null)
         throw new IllegalArgumentException("tree may not be null");
      if (descriptor == null)
         throw new IllegalArgumentException("descriptor may not be null");
      
       m_tree = tree;
       m_descriptor = descriptor; 
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
   public PSContentStatus(Element source)
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
    * <!ELEMENT PSXContentStatus (ItemContexts, PSXLoaderDescriptor)>
    * <!ELEMENT ItemContexts (PSXItemContext*)>
    * </code></pre>
    * <p>
    * Note: The serialized content tree will be a list of serialized
    * <code>PSItemContext</code> objects, which are extracted from a list of 
    * node of the tree. In other words, the saved tree will be flat.
    * 
    * @param doc The document to use to create the element, may not be
    * <code>null</code>.
    *
    * @return the newly created XML element node, never <code>null</code>
    * 
    * @see com.percussion.objectstore.PSLoaderComponent#toXml(Document)
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element itemsEl = doc.createElement(XML_ITEMCONTEXTS);
      Iterator nodes = m_tree.getNodes().iterator();
      while (nodes.hasNext())
      {
         IPSContentTreeNode node = (IPSContentTreeNode) nodes.next();
         itemsEl.appendChild(node.getItemContext().toXml(doc));
      }
      
      Element root = doc.createElement(XML_NODE_NAME);
      root.appendChild(itemsEl);
      root.appendChild(m_descriptor.toXml(doc));

      return root;
   }
   
   /**
    * This method is called to populate an object from its XML representation.
    * This is the opposite operation with {@link #toXml(Document)}.
    * <p>
    * Note: The restored content tree will be flat since it is saved that way.
    * 
    * @param sourceNode the XML element node to populate from not
    * <code>null</code>.
    *
    * @throws PSUnknownNodeTypeException if the XML element node does not
    *    represent a type supported by the class.
    * @throws PSLoaderException if any other error occurs.
    */
   public void fromXml(Element srcNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      PSXMLDomUtil.checkNode(srcNode, XML_NODE_NAME);
      
      // set m_tree
      Element itemCtxsEl = 
         PSXMLDomUtil.getFirstElementChild(srcNode, XML_ITEMCONTEXTS);
      PSContentTreeModel tree = new PSContentTreeModel();
      Element itemEl = PSXMLDomUtil.getFirstElementChild(itemCtxsEl);
      while (itemEl != null)
      {
         PSItemContext itemCtx = new PSItemContext(itemEl);
         tree.addNode(itemCtx);
         itemEl = PSXMLDomUtil.getNextElementSibling(itemEl);
      }
      m_tree = tree;
      
      // set the m_descriptor
      Element descriptorEl = PSXMLDomUtil.getNextElementSibling(itemCtxsEl);
      m_descriptor = new PSLoaderDescriptor(descriptorEl);
   }
   
   /**
    * Get the content tree of the status.
    * 
    * @return The content tree, never <code>null</code>.
    */
   public IPSContentTree getContentTree()
   {
      return m_tree;
   }
   
   /**
    * Get the descriptor of the status.
    * <p>
    * NOTE: The loader descriptor does not contain the <code>PSLogDef</code> 
    * object after it saved and then retrieved from the disk because we cannot 
    * serialize the <code>PSLogDef</code> (or <code>Element</code>) object.
    * 
    * @return The descriptor, never <code>null</code>.
    */
   public PSLoaderDescriptor getDescriptor() throws PSLoaderException
   {
      return m_descriptor;
   }
   
   /**
    * The XML node name of this object.
    */
   final public static String XML_NODE_NAME = "PSXContentStatus";
   
   /**
    * The scanned content tree. Initialized by the constructor, never 
    * <code>null</code> after that.
    */
   private IPSContentTree m_tree;
   
   /**
    * The descriptor that is used to do the content loader operation. 
    * Initiailized by the constructor, never <code>null</code> after that. 
    */
   private PSLoaderDescriptor m_descriptor;
   
   // Constants for XML 
   final private static String XML_ITEMCONTEXTS = "ItemContexts";
}
