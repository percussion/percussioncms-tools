/******************************************************************************
 *
 * [ PSModelChangedEvent.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is created and returned by
 * {@link com.percussion.client.models.IPSCmsModel} objects to notify registered
 * listeners of interesting events.
 * <p>
 * This class is immutable.
 * 
 * @author paulhoward
 * @version 6.0
 */
public class PSModelChangedEvent
{
   /**
    * The different events for which notifications are sent.
    */
   public enum ModelEvents
   {
      /**
       * When the name of a design object is successfully changed (meaning the
       * change has been persisted,) this event type is sent. This change
       * affects the reference as well as the data object.
       */
      RENAMED,

      /**
       * When a new design object is created, this event type is sent.
       */
      CREATED,

      /**
       * When a design object is modified in memory, this event type is sent.
       * Name changes do not cause this event to be sent. See {@link #RENAMED}.
       */
      MODIFIED,

      /**
       * When a design object is first locked for editing. Subsequent loads with
       * lock enabled do not generate this event.
       */
      LOCKED,

      /**
       * When a design object is no longer locked for editing.
       */
      UNLOCKED,

      /**
       * When a design object is successfully persisted, this event type is
       * sent.
       */
      SAVED,

      /**
       * When a design object is permanently removed, this event type is sent.
       * It is only sent on successful deletion.
       */
      DELETED,
      
      /**
       * Children have been added or removed from the associated node. In this
       * case, 0 or more source objects of the event may be <code>null</code> to 
       * indicate the root children have changed.
       */
      CHILDREN_ADDED,
      CHILDREN_REMOVED,

      /**
       * When the ACL associated with some design object locked for editing,
       * this event type is sent.
       */
      ACL_LOCKED,

      /**
       * When the ACL associated with some design object unlocked for editing,
       * this event type is sent.
       */
      ACL_UNLOCKED,

      /**
       * When the ACL associated with some design object has been changed in
       * memory, this event type is sent.
       */
      ACL_MODIFIED,

      /**
       * When the ACL associated with some design object has been successfully
       * persisted, this event type is sent.
       */
      ACL_SAVED,
      
      /**
       * Some type of association has been created between 2 first-class design
       * objects. For example, a content type was linked to a template.
       * <p>
       * {@link PSModelChangedEvent#getLinkOwner()} returns the owner, while
       * {@link PSModelChangedEvent#getSource()} returns references to the
       * newly added objects.
       */
      LINKS_ADDED,
      
      /**
       * Just like {@link #LINKS_ADDED}, except the children are the deleted 
       * ones. 
       */
      LINKS_DELETED;

      /**
       * A bit position such that these events can be OR'd together into an int.
       * Initialized at construction with the bit offset based on the ordinal
       * value of this enum.
       */
      private int m_flag = 1 << ordinal();

      /**
       * Returns a value for this enum that can be OR'd together with other
       * enums of this type into an int to easily provide multiple events.
       * 
       * @return Always a value of {@code 1 << n}, where n is the ordinal value
       * of the enum.
       */
      public int getFlag()
      {
         return m_flag;
      }
   }

   /**
    * This ctor is used with the <code>LINKS_xxx</code> events.
    * 
    * @param linkOwner A handle to the owner of the links that were modified.
    * Never <code>null</code>.
    * 
    * @param linkedRefs Handles to the objects on the other side of the link
    * that were either deleted or added, as the event type states. Never
    * <code>null</code> or empty.
    * 
    * @param eventType One of the LINK_xxx types. Never <code>null</code>.
    */
   public PSModelChangedEvent(IPSReference linkOwner,
         Collection<IPSReference> linkedRefs, ModelEvents eventType)
   {
      if (null == linkOwner)
      {
         throw new IllegalArgumentException("linkOwner cannot be null");  
      }
      if (null == linkedRefs || linkedRefs.isEmpty())
      {
         throw new IllegalArgumentException("linkedRefs cannot be null or empty");  
      }
      if (null == eventType
            || (eventType != ModelEvents.LINKS_ADDED 
                  && eventType != ModelEvents.LINKS_DELETED))
      {
         throw new IllegalArgumentException(
               "eventType must be LINK_ADDED or LINK_DELETED");  
      }
      m_linkOwner = linkOwner;
      m_sources = linkedRefs.toArray(new IPSReference[linkedRefs.size()]);
      m_eventType = eventType;
      m_children = null;
      m_hint = new HashMap<String,String>();
      m_parent = null;
   }
   
   /**
    * Convenience ctor that calls
    * {@link #PSModelChangedEvent(IPSReference[], ModelEvents, Map)  this(new
    * IPSReference[] &#125; source &#125;, eventType, hint}.
    */
   public PSModelChangedEvent(IPSReference source, ModelEvents eventType,
         Map<String, String> hint)
   {
      this(new IPSReference[] { source }, eventType, hint);
   }

   /**
    * Convenience ctor that calls
    * {@link #PSModelChangedEvent(IPSReference[], ModelEvents, Map) 
    * this((IPSReference[]) sources.toArray(new IPSReference[sources.size()]),
    * eventType, hint)}.
    */
   public PSModelChangedEvent(Collection<IPSReference> sources,
         ModelEvents eventType, Map<String, String> hint)
   {
      this(sources.toArray(new IPSReference[sources.size()]), eventType, hint);
   }
   
   /**
    * Create it.
    * 
    * @param sources Never <code>null</code> and no <code>null</code>
    * entries.
    * @param eventType Never <code>null</code>. Must a type other than
    * <code>CHILDREN_xxx</code>. For that event type, use the 3 param ctor.
    * @param hint Optional extra information that may allow a listener to 
    * optimize his response to the event. Generally, this is only used with the 
    * eventType {@link ModelEvents#MODIFIED}. May be <code>null</code> or empty.
    */
   public PSModelChangedEvent(IPSReference[] sources, ModelEvents eventType,
         Map<String, String> hint)
   {
      // validate contract
      if (null == sources)
      {
         throw new IllegalArgumentException("sources cannot be null");
      }

      if (null == eventType)
      {
         throw new IllegalArgumentException("eventType cannot be null");
      }
      
      if (eventType == ModelEvents.CHILDREN_ADDED
            || eventType == ModelEvents.CHILDREN_REMOVED)
      {
         throw new IllegalArgumentException(
               "eventType CHILDREN_xxx not valid for this ctor");
      }

      for (int i = 0; i < sources.length; i++)
      {
         if (sources[i] == null)
         {
            throw new IllegalArgumentException("null source found at index "
                  + i);
         }
      }
      
      m_sources = sources;
      m_eventType = eventType;
      m_hint = hint == null ? new HashMap<String,String>() : hint;
      m_parent = null;
      m_children = null;
      m_linkOwner = null;
   }

   /**
    * Create it.
    * 
    * @param parent If this is a <code>CHILDREN_xxx</code> event, then
    * this node indicates the parent and <code>sources</code> contains the nodes
    * that were either added or removed.
    * 
    * @param children Never <code>null</code> and no <code>null</code> elements.
    * 
    * @param eventType Never <code>null</code>.
    * @todo - add hint param
    */
   public PSModelChangedEvent(IPSHierarchyNodeRef parent,
         IPSHierarchyNodeRef[] children, ModelEvents eventType)
   {
      // validate contract
      if (null == children)
      {
         throw new IllegalArgumentException("children cannot be null");
      }

      if (null == eventType)
      {
         throw new IllegalArgumentException("eventType cannot be null");
      }

      if (eventType != ModelEvents.CHILDREN_ADDED
            && eventType != ModelEvents.CHILDREN_REMOVED)
      {
         throw new IllegalArgumentException(
               "eventType must be one of the CHILDREN_xxx types");
      }

      for (int i = 0; i < children.length; i++)
      {
         if (children[i] == null)
         {
            throw new IllegalArgumentException("null child found at index "
                  + i);
         }
      }

      m_sources = null;
      m_eventType = eventType;
      m_hint = null;
      m_parent = parent;
      m_children = children;
      m_linkOwner = null;
      
   }

   /**
    * Each event has exactly 1 cause, which is indicated by the event type.
    * 
    * @return One of the {@link ModelEvents} enums that caused this event to be
    * sent. Never <code>null</code>.
    */
   public ModelEvents getEventType()
   {
      return m_eventType;
   }

   /**
    * A reference to the object(s) that caused the event. If the event was
    * {@link ModelEvents#DELETED}, then this reference is to the object that no
    * longer exists and thus cannot be used for anything (i.e. it can't be
    * passed into the model), it is for informational purposes only.
    * 
    * @return Always at least 1 element. Each entry is non-<code>null</code>.
    * The caller takes ownership of the array and may modify it.
    */
   public IPSReference[] getSource()
   {
      IPSReference[] results = new IPSReference[m_sources.length];
      System.arraycopy(m_sources, 0, results, 0, m_sources.length);
      return results;
   }

   /**
    * The hint map supplied in the ctor. 
    * 
    * @return Never <code>null</code>, may be empty. The map is unmodifiable
    * and ownership is retained by this class.
    */
   public Map<String,String> getHint()
   {
      return Collections.unmodifiableMap(m_hint);
   }

   /**
    * Only meaningful if the event type is one of the LINK_xxx types. In that
    * case, this is a handle to the object that owned the links that caused
    * this event to be generated.
    * 
    * @return If this event is for a LINK_xxx type, never <code>null</code>, 
    * otherwise, always <code>null</code>.
    */
   public IPSReference getLinkOwner()
   {
      return m_linkOwner;
   }
   
   /**
    * If the event type is one of the CHILDREN_xxx values, then this may return
    * a non-<code>null</code> value. Otherwise, it returns <code>null</code>.
    * This is the ref which had the sources added or removed.
    * 
    * @return The value supplied in the ctor. May be <code>null</code> to
    * indicate the root node.
    */
   public IPSHierarchyNodeRef getParent()
   {
      return m_parent;
   }

   /**
    * If the event type is one of the CHILDREN_xxx values, then this will return
    * a non-<code>null</code> value. Otherwise, it returns <code>null</code>.
    * These are the refs which were added or removed.
    * 
    * @return The value supplied in the ctor. Never <code>null</code> if the
    * event type is CHILDREN_xxx, otherwise, always <code>null</code>.
    */
   public IPSHierarchyNodeRef[] getChildren()
   {
      return m_children;
   }
   
   @Override
   public boolean equals(Object o)
   {
      if (o == null || !(o instanceof PSModelChangedEvent))
         return false;
      
      PSModelChangedEvent test = (PSModelChangedEvent) o;
      if (m_eventType != test.m_eventType)
         return false;
      
      if (m_sources.length != test.m_sources.length)
         return false;
      
      // considered same if sources are same, regardless of order
      Collection<IPSReference> testRefs = new ArrayList<IPSReference>();
      testRefs.addAll(Arrays.asList(test.m_sources));
      for (IPSReference ref : m_sources)
      {
         if (!testRefs.remove(ref))
            return false;
      }
      return true;
   }
   
   @Override
   public int hashCode()
   {
      int hash = m_eventType.hashCode();
      //should be independent of order of sources
      for (IPSReference ref : m_sources)
      {
         hash += ref.hashCode();
      }
      return hash;
   }

   /**
    * See {@link #getParent()}. <code>null</code> unless the appropriate 
    * message type.
    */
   private final IPSHierarchyNodeRef m_parent;
   
   /**
    * See {@link #getChildren()}. <code>null</code> unless the appropriate 
    * message type.
    */
   private final IPSHierarchyNodeRef[] m_children;
   
   /**
    * The objects that caused the event. Always at least 1 entry. Never
    * <code>null</code> and no <code>null</code> entries after construction.
    */
   private final IPSReference[] m_sources;

   /**
    * Used to store the owner's handle for the LINK_xxx events.
    * <code>null</code> unless the appropriate ctor is called, then never
    * <code>null</code>.
    */
   private final IPSReference m_linkOwner;

   /**
    * The actual event that caused this object to be created. Never
    * <code>null</code> or modified after construction.
    */
   private final ModelEvents m_eventType;

   /**
    * Optional hints generally supplied with a modify event that can allow a
    * client to optimize her response. Never <code>null</code> after ctor.
    */
   private final Map<String, String> m_hint;
}
