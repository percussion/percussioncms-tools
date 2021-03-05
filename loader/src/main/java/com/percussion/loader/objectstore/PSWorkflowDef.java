/*[ PSWorkflowDef.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.loader.objectstore;

import com.percussion.design.objectstore.PSUnknownNodeTypeException;
import com.percussion.loader.PSLoaderException;
import com.percussion.util.PSXMLDomUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The workflow definition, which contains one of more set of transitions.
 *
 * @see {@link #PSTransitionDef}
 */
public class PSWorkflowDef extends PSLoaderComponent
   implements java.io.Serializable
{
   /**
    * Constructs the object from the specified name.
    *
    * @param name The name of the workflow, which may not be <code>null</code>
    *    or empty.
    *
    * @throws IllegalArgumentException if any parameter is invalid.
    */
   public PSWorkflowDef(String name)
   {
      if (name == null || name.trim().length() == 0)
         throw new IllegalArgumentException("name may not be null or empty");

      m_name = name;
      resetTransitions();      
   }

   /**
    * Reset and clear all transitions
    */
   private void resetTransitions()
   {
      m_transitions = new PSComponentList[MAX_TRANS];
      for (int i=0; i < MAX_TRANS; i++)
      {
         m_transitions[i] = new PSComponentList(XML_TRANSITIONS, 
            PSTransitionDef.XML_NODE_NAME, PSTransitionDef.class);
      }
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
   public PSWorkflowDef(Element source)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      if (source == null)
         throw new IllegalArgumentException("source may not be null");

      fromXml(source);
   }

   /**
    * Get the name of the workflow.
    *
    * @return The workflow name, never <code>null</code> or empty.
    */
   public String getName()
   {
      return m_name;
   }

   /**
    * Get a list of the specified transitions.
    *
    * @param whichTrans The transition set, must be one of the 
    *    <code>TRANS_XXX</code> values.
    * 
    * @return A list over zero or more <code>PSTransitionDef</code>
    *    objects, never <code>null</code>.
    */
   public PSComponentList getTransitions(int whichTrans)
   {
      validateTransSet(whichTrans);
      
      return m_transitions[whichTrans];
   }

   /**
    * Validating the supplied transition set.
    * 
    * @param whichTrans The to be validated transition set. It must be one of
    *    the <code>TRANS_XXX</code> values.
    *    
    * @throws IllegalArgumentException if <code>list</code> is <code>null
    *    </code> or if it is the wrong type of list.
    */
   public static void validateTransSet(int whichTrans)
   {
      if (whichTrans != TRANS_INSERT && whichTrans != TRANS_PREUPDATE &&
          whichTrans != TRANS_POSTUPDATE)
      {
         throw new IllegalArgumentException("transition set is not valid");
      }
   }
   
   /**
    * Set the specified transitions.
    *
    * @param trans The to be set transitions, a list over one or more 
    *    <code>PSTransitionDef</code> objects, never <code>null</code>, 
    *    must have at least one <code>PSTransitionDef</code> object.
    *
    * @param whichTrans The transition set, one of the 
    *    <code>TRANS_XXX</code> values.
    * 
    * @throws IllegalArgumentException if <code>trans</code> is <code>null
    *    </code> or if it is the wrong type of list.
    * @throws IllegalStateException if <code>trans</code> is empty.
    */
   public void setTransitions(PSComponentList trans, int whichTrans)
   {
      validateTransSet(whichTrans);
      
      if (trans == null)
         throw new IllegalArgumentException("list must not be null");

      if (trans.isEmpty())
         throw new IllegalStateException("list must not be empty");

      if (trans.getXmlNodeName() != m_transitions[whichTrans].getXmlNodeName())
         throw new IllegalArgumentException(
            "list must be a list of PSTransitionDef objects");

      m_transitions[whichTrans] = trans;
   }

   /**
    * Adds a transition to the specified transition set.
    *
    * @param def The to be added transition definition, may not be 
    *    <code>null</code>.
    *    
    * @param whichTrans The transition set, one of the 
    *    <code>TRANS_XXX</code> values.
    */
   public void addTransition(PSTransitionDef def, int whichTrans)
   {
      if (def == null)
         throw new IllegalArgumentException("def may not be null");

      validateTransSet(whichTrans);
      
      m_transitions[whichTrans].addComponent(def);
   }

   /**
    * Serializes this object's state to its XML representation.  The format is:
    * <pre><code>
    * &lt;!ELEMENT PSXWorkflow (TransitionSet+)>
    * &lt;!ATTLIST PSXWorkflow
    *     name CDATA #REQUIRED
    * &gt;
    * &lt;!ELEMENT TransitionSet (PSXTransitionDef+) &gt;
    * &lt;!ATTLIST TransitionSet
    *     name CDATA #REQUIRED
    * &gt;
    * </code></pre>
    *
    * See {@link PSLoaderComponent#toXml(Document)} for more info.
    * 
    * @throws IllegalStateException If there is no transition defined.
    */
   public Element toXml(Document doc)
   {
      if (doc == null)
         throw new IllegalArgumentException("doc may not be null");

      // make sure the transition lists are not all empty
      boolean isEmpty = true;
      for (int i=0; i < MAX_TRANS; i++)
      {
         if (! m_transitions[i].isEmpty())
         {
            isEmpty = false;
            break;
         }
      }
      if (isEmpty)
      {
         throw new IllegalStateException(
            "transitions must contain at least one transition.");
      }

      // at least one of the transition list is not empty, continue
      Element root = doc.createElement(XML_NODE_NAME);
      root.setAttribute(XML_ATTR_NAME, m_name);
      
      Element transEl = null;
      for (int i=0; i < MAX_TRANS; i++)
      {
         if (! m_transitions[i].isEmpty())
         {
            transEl = m_transitions[i].toXml(doc);
            switch (i)
            {
               case TRANS_INSERT: 
                  transEl.setAttribute(XML_ATTR_NAME, XML_ATTR_INSERT);
                  break;
               case TRANS_PREUPDATE:
                  transEl.setAttribute(XML_ATTR_NAME, XML_ATTR_PREUPDATE);
                  break;
               case TRANS_POSTUPDATE:
                  transEl.setAttribute(XML_ATTR_NAME, XML_ATTR_POSTUPDATE);
                  break;
            }
               
            root.appendChild(transEl);
         }
      }

      return root;
   }

   // See PSLoaderComponent#fromXml(Element)
   public void fromXml(Element sourceNode)
      throws PSUnknownNodeTypeException, PSLoaderException
   {
      validateElement(sourceNode, this.XML_NODE_NAME);
      
      resetTransitions();      
      m_name = getRequiredAttribute(sourceNode, XML_ATTR_NAME);
      
      Element transEl =
         PSXMLDomUtil.getFirstElementChild(sourceNode, XML_TRANSITIONS);
         
      while (transEl != null)
      {
         PSXMLDomUtil.checkNode(transEl, XML_TRANSITIONS);
         String name = PSXMLDomUtil.checkAttribute(transEl, XML_ATTR_NAME,true);
         if (name.equalsIgnoreCase(XML_ATTR_INSERT))
         {
            m_transitions[TRANS_INSERT].fromXml(transEl);
         }
         else if (name.equalsIgnoreCase(XML_ATTR_PREUPDATE))
         {
            m_transitions[TRANS_PREUPDATE].fromXml(transEl);
         }
         else if (name.equalsIgnoreCase(XML_ATTR_POSTUPDATE))
         {
            m_transitions[TRANS_POSTUPDATE].fromXml(transEl);
         }
         transEl = PSXMLDomUtil.getNextElementSibling(transEl);
      }
   }

   // See PSLoaderComponent#hashCode()
   public int hashCode()
   {
      return m_name.hashCode() + m_transitions[0].hashCode() +
         m_transitions[1].hashCode() + m_transitions[2].hashCode();
   }

   // See PSLoaderComponent#equals(Object)
   public boolean equals(Object obj)
   {
      if (obj == null || !(obj instanceof PSWorkflowDef))
         return false;

      PSWorkflowDef obj2 = (PSWorkflowDef) obj;

      return m_name.equals(obj2.m_name) &&
         m_transitions[0].equals(obj2.m_transitions[0]) &&
         m_transitions[1].equals(obj2.m_transitions[1]) &&
         m_transitions[2].equals(obj2.m_transitions[2]);
   }

   /**
    * The XML node name of the component list, initialized by constructor,
    * never <code>null</code> or empty after that.
    */
   final public static String XML_NODE_NAME = "PSXWorkflowDef";
   
   /**
    * The transitions which will be used for inserting new items.
    */
   final public static int TRANS_INSERT = 0;

   /**
    * The transitions which will be used for before updating items.
    */
   final public static int TRANS_PREUPDATE = 1;

   /**
    * The transitions which will be used for after updating items.
    */
   final public static int TRANS_POSTUPDATE = 2;

   /**
    * Maximum transition sets
    */
   final public static int MAX_TRANS = 3;
   
   /**
    * The name of the workflow, initialized by constructor, never
    * <code>null</code> or empty after that.
    */
   private String m_name;

   /**
    * A list of transition set. Initialized by {@link #resetTransitions()},
    * never <code>null</code> after that.
    */
   private PSComponentList[] m_transitions;
      
   // Private constants for XML attribute and element name
   final static private String XML_ATTR_NAME = "name";
   final static private String XML_TRANSITIONS = "TransitionSet";
   final static private String XML_ATTR_INSERT = "insert";
   final static private String XML_ATTR_PREUPDATE = "pre-update";
   final static private String XML_ATTR_POSTUPDATE = "post-update";
}
