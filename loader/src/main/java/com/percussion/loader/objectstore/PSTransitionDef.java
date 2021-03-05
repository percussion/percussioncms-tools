/*[ PSTransitionDef.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @see {@link #PSSingleAttElem} for description
 */
public class PSTransitionDef 
   extends PSLoaderComponent 
   implements java.io.Serializable
{
   /**
    * Constructs an instance from a name and trigger.
    * 
    * @param name The name of the transition, may not be <code>null</code>
    *    or empty.
    * @param trigger The trigger of the transition, may not be <code>null</code>
    *    or empty.
    */
   public PSTransitionDef(String name, String trigger)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");
      if (trigger == null || trigger.trim().length() == 0)
         throw new IllegalArgumentException("trigger may not be null or empty");
         
      m_name = name;
      m_trigger = trigger;
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
   public PSTransitionDef(Element source)
      throws PSUnknownNodeTypeException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Serializes this object's state to its XML representation.  
    * 
    * The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXTransitionDef EMPTY&gt;
    * &lt;!ATTLIST PSXTransitionDef
    * name CDATA #REQUIRED
    * trigger CDATA #REQUIRED
    * &gt;
    * </code></pre>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      Element root = doc.createElement(XML_NODE_NAME);
      
      root.setAttribute(XML_ATTR_NAME, m_name);
      root.setAttribute(XML_ATTR_TRIGGER, m_trigger);
      
      return root;
   }
      

   // See PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException
   {
      validateElement(sourceNode, XML_NODE_NAME);

      m_name = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      m_trigger = getRequiredAttribute(sourceNode, XML_ATTR_TRIGGER);
   }

   // See PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_name.hashCode() + m_trigger.hashCode();
   }

   // See PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSTransitionDef))
         return false;

      PSTransitionDef obj2 = (PSTransitionDef) obj;

      return m_name.equals(obj2.m_name) 
            && (m_trigger == obj2.m_trigger);
   }


   /**
    * Get transition name. The transition name (or label) may not be unique
    * in a given workflow.
    * 
    * @return Transition name, which may be the label or the transition, 
    *    never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }
   
   /**
    * Get the trigger of the transition. The trigger name must be unique in a 
    * given workflow.
    * 
    * @return The trigger name, never <code>null</code> or empty.
    */
   public String getTrigger()
   {
      return m_trigger;
   }
   
   /**
    * Root node name of this object's XML representation.
    */
   public static final String XML_NODE_NAME = "PSXTransitionDef";

   // Private XML constants   
   private final static String XML_ATTR_NAME = "name";
   private final static String XML_ATTR_TRIGGER = "trigger";
   
   /**
    * The name of the transition. Initialized by ctor, never <code>null</code>
    * or empty after that.
    */
   private String m_name;
   
   /**
    * The trigger of the transition. Initialized by ctor, never <code>null</code>
    * or empty after that.
    */
   private String m_trigger;
}
