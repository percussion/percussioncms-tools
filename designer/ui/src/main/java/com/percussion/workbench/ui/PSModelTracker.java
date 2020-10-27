/******************************************************************************
 *
 * [ PSModelTracker.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSDuplicateNameException;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSModelListener;
import com.percussion.client.models.IPSObjectDefaulter;
import com.percussion.client.models.PSLockException;
import com.percussion.workbench.ui.model.IPSDeclarativeNodeHandler;
import com.percussion.workbench.ui.model.IPSDesignObjectChangeListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the 'control center' for anyone editing core models to notify of
 * property level changes (editors and views) as well as register for said
 * changes.
 * <p>
 * The following is assumed: at most, 1 editor is editing an object and 0 or
 * more views may possibly edit the object.
 * <p>
 * If the object is open in an editor when a view makes a change, that change
 * will only affect the in-memory copy, it will not be persisted to the server
 * until the editor calls save. If no editor is open when a view makes a change,
 * it is immediately persisted.
 * <p>
 * This should not happen in practice, but if the view opens an object for 
 * editing while the editor has it open and doesn't save it before the editor
 * closes, the save will be disallowed as the lock will have been released.
 * <p>
 * The object should be updated for every 'complete' property change. A complete
 * change is one in which the user's input has been accepted (e.g. after focus
 * leaves a field that has been modified and the content of that field has
 * passed validation.) This is done by calling the
 * {@link #propertyChanged(IPSReference, Map)} method.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSModelTracker
{
   /**
    * Returns the single copy of this tracker.
    * 
    * @return Never <code>null</code>.
    */
   synchronized public static PSModelTracker getInstance()
   {
      if (ms_tracker == null)
         ms_tracker = new PSModelTracker();
      return ms_tracker;
   }

   /**
    * Just like {@link IPSCmsModel#create(IPSReference[], String[])}, except the
    * <code>parent</code> is used to manage the design view model.
    * 
    * @param parent The node chosen by the user, may be <code>null</code>.
    * If <code>null</code> and the view is open, the object is added to the
    * 'root' location for this object type, otherwise, it is just created in the
    * core model.
    */
   public IPSReference[] create(IPSReference[] sources, String[] names, 
         PSUiReference parent)
      throws PSMultiOperationException, PSModelException
   {
      if (sources.length == 0)
         return new IPSReference[0];
      try
      {
         IPSReference[] results = sources[0].getParentModel().create(sources, 
               names);
         notifyCreate(parent, results);
         return results;
      }
      catch (PSMultiOperationException e)
      {
         Collection<IPSReference> goodRefs = new ArrayList<IPSReference>();
         for (Object o : e.getResults())
         {
            if (o instanceof IPSReference)
               goodRefs.add((IPSReference)o);
         }
         notifyCreate(parent, goodRefs
               .toArray(new IPSReference[goodRefs.size()]));
         throw e;
      }
   }
   
   /**
    * Sends a notification to all registered design object change listeners
    * that some objects were created.
    * 
    * @param parent The node the user wants the newly created object added to.
    * May be <code>null</code>. In this case, it is added to the proper node
    * in the tree if the view is open, otherwise, nothing is done.
    * 
    * @param results The newly created design objects that will be added.
    * Assumed not <code>null</code> and no <code>null</code> entries, may be
    * empty.
    */
   private void notifyCreate(PSUiReference parent, IPSReference[] results)
   {
      for (IPSDesignObjectChangeListener l : m_uiListeners)
      {
         l.addChildren(parent, results);
      }
   }

   /**
    * Just like {@link IPSCmsModel#create(PSObjectType, List)}, except the
    * <code>parent</code> is used to manage the design view model.
    * 
    * @param parent The node chosen by the user, may be <code>null</code>.
    * If <code>null</code> and the view is open, the object is added to the
    * 'root' location for this object type, otherwise, it is just created in the
    * core model. 
    */
   public IPSReference[] create(PSObjectType objectType, List<String> names,
         PSUiReference parent)
      throws PSMultiOperationException
   {
      try
      {
         IPSReference[] results = PSCoreFactory.getInstance().getModel(
               objectType.getPrimaryType()).create(objectType, names);
         notifyCreate(parent, results);
         return results;
      }
      catch (PSMultiOperationException e)
      {
         Collection<IPSReference> goodRefs = new ArrayList<IPSReference>();
         for (Object o : e.getResults())
         {
            if (o instanceof IPSReference)
               goodRefs.add((IPSReference)o);
         }
         notifyCreate(parent, goodRefs
               .toArray(new IPSReference[goodRefs.size()]));
         throw e;
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Just like {@link IPSCmsModel#create(PSObjectType, String)}, except the
    * <code>parent</code> is used to manage the design view model.
    * 
    * @param parent The node chosen by the user, may be <code>null</code>.
    * If <code>null</code> and the view is open, the object is added to the
    * 'root' location for this object type, otherwise, it is just created in the
    * core model. 
    */
   public IPSReference create(PSObjectType objectType, String name,
         PSUiReference parent)
      throws PSDuplicateNameException
   {
      try
      {
         IPSReference result = PSCoreFactory.getInstance().getModel(
               objectType.getPrimaryType()).create(objectType, name);
         notifyCreate(parent, new IPSReference[] {result});
         return result;
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Just like
    * {@link IPSCmsModel#create(PSObjectType, String, IPSObjectDefaulter)},
    * except the <code>parent</code> is used to manage the design view model.
    * 
    * @param parent The node chosen by the user, may be <code>null</code>. If
    * <code>null</code> and the view is open, the object is added to the
    * 'root' location for this object type, otherwise, it is just created in the
    * core model. If the supplied node is not applicable for the object's type,
    * the object will be created in it's home node.
    */
   public IPSReference create(PSObjectType objectType, String name,
         PSUiReference parent, IPSObjectDefaulter defaulter)
      throws PSDuplicateNameException
   {
      try
      {
         IPSReference result = PSCoreFactory.getInstance().getModel(
               objectType.getPrimaryType()).create(objectType,
                  name, defaulter);
         if (parent != null)
         {
            IPSDeclarativeNodeHandler handler = parent.getHandler();
            if (handler != null && !handler.isAcceptedType(result))
            {
               parent = null;
            }
         }
         notifyCreate(parent, new IPSReference[] {result});
         return result;
      }
      catch (PSModelException e)
      {
         // should never happen
         throw new RuntimeException(e);
      }
   }
   
   /**
    * Convenience method that calls
    * {@link #load(IPSReference, boolean, boolean,  boolean) load(ref, locked,
    * <code>false</code>)}.
    */
   public Object load(IPSReference ref, boolean locked)
      throws Exception
   {
      if ( null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      return load(ref, locked, false, false);
   }

   /**
    * See {@link com.percussion.client.models.IPSCmsModel#load(IPSReference, 
    * boolean, boolean)} for details.
    * 
    * @param lifecycleOwner Only a single <code>EditorPart</code> part should
    * call this with <code>true</code>. All others must supply
    * <code>false</code> or call the 1 parameter version of this method. Once
    * this flag has been set, all calls to save will not be persisted to the
    * server until <code>save</code> is called with the
    * <code>lifecycleOwner</code> flag set to <code>true</code>, which
    * should only be done by the class that originally set this flag.
    */
   public Object load(IPSReference ref, boolean locked, boolean overrideLock,
         boolean lifecycleOwner)
      throws Exception
   {
      if ( null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      Object result = ref.getParentModel().load(ref, locked, overrideLock);
      if (lifecycleOwner && locked)
         m_lifecycleRefs.add(ref);
      return result;
   }

   /**
    * Convenience method that calls {@link #save(IPSReference, boolean, boolean)
    * save(ref, true, false)}.
    * <p>
    * This method is meant for use by views.
    */
   public void save(IPSReference ref)
      throws Exception
   {
      save(ref, true, false);
   }

   /**
    * Notify this class that you would like your changes persisted. When the
    * persistence occurs is determined by the rules described in the class 
    * description. Note that it is possible that changes made in a view are
    * never persisted to the server.
    * <p>
    * Views will generally use the 1 parameter version.
    * <p>
    * Note that the underlying object after this call may change from what it
    * was before the call. This will generally happen the first time the object
    * is persisted, as a new ID will be attached to the object. If any changes
    * are made, a change notification will be sent.
    * 
    * @param ref The object to persist. Never <code>null</code>.
    * 
    * @param releaseLock When done editing, supply <code>true</code>. If you
    * wish to continue editing, supply <code>false</code>.
    * 
    * @param lifecycleOwner Must be <code>true</code> if the caller supplied
    * <code>true</code> for this flag in the <code>load</code> method,
    * otherwise it must be <code>false</code>.
    * 
    * @throws Exception If any problems communicating with the server.
    */
   public void save(IPSReference ref, boolean releaseLock,
         boolean lifecycleOwner)
      throws Exception
   {
      if ( null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      if (lifecycleOwner || (!lifecycleOwner && !isLifecycleControlled(ref)))
      {
         ref.getParentModel().save(ref, releaseLock);
         if (releaseLock)
            removeLifecycleControl(ref);
      }
   }

   /**
    * Convenience method that calls {@link #releaseLock(IPSReference, boolean)
    * releaseLock(ref, <code>false</code>)}.
    * <p>
    * This method is meant for view use.
    */
   public void releaseLock(IPSReference ref)
      throws Exception
   {
      releaseLock(ref, false);
   }
   
   /**
    * Any object that was locked must be released. If you decide you don't want
    * to keep your changes (or didn't make any), use this method to free the
    * object.
    * 
    * @param ref Never <code>null</code>. If the object is not locked,
    * returns immediately.
    * 
    * @param lifecycleOwner Should be the same value passed to the
    * {@link #load(IPSReference, boolean, boolean, boolean) load} method.
    * 
    * @throws Exception If any problems communicating with the server.
    */
   public void releaseLock(IPSReference ref, boolean lifecycleOwner)
      throws Exception
   {
      if ( null == ref)
      {
         throw new IllegalArgumentException("ref cannot be null");  
      }
      if (lifecycleOwner || (!lifecycleOwner && !isLifecycleControlled(ref)))
      {
         ref.getParentModel().releaseLock(ref);
         ref.getParentModel().releaseAclLock(ref);
         removeLifecycleControl(ref);
      }
   }
   
   /**
    * Every time a design object is created, all registered listeners will be
    * notified. Generally, this mechanism should be used rather than directly
    * listening to the core model for creates. For other types of modifications,
    * register with the core model. 
    * 
    * @param l Never <code>null</code>. If already registered, the supplied
    * one replaces the existing one.
    */
   public void addListener(IPSDesignObjectChangeListener l)
   {
      if ( null == l)
      {
         throw new IllegalArgumentException("listener cannot be null");  
      }
      m_uiListeners.add(l);
   }
   
   /**
    * Register for low-level core model change notifications. Generally, the
    * other signature of this method should be used. 
    * <p>
    * This is a convenience method. Callers may register directly with models
    * for events.
    * 
    * @param listener Never <code>null</code>.
    * 
    * @param filter Only events caused by objects of the type(s) included in
    * this filter will be sent to this listener. Never <code>null</code>.
    * If empty, returns immediately w/o registering the listener.
    * 
    * @param notifications See {@link com.percussion.client.PSModelChangedEvent
    * PSModelChangedEvent} for possible notifications.
    */
   public void addListener(IPSModelListener listener,
         Set<PSObjectType> filter, int notifications)
   {
      if ( null == listener)
      {
         throw new IllegalArgumentException("listener cannot be null");  
      }
      if ( null == filter)
      {
         throw new IllegalArgumentException("filter cannot be null");  
      }
      
      if (filter.size() == 0)
         return;
      
      try
      {
         for (PSObjectType type : filter)
         {
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
                  type.getPrimaryType());
            model.addListener(listener, notifications);
         }
      }
      catch (PSModelException e)
      {
         //not expected to happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Removes the supplied listener from the registered list. If l is not
    * currently registered, no action is taken.
    * 
    * @param l Never <code>null</code>.
    */
   public void removeListener(IPSDesignObjectChangeListener l)
   {
      if ( null == l)
      {
         throw new IllegalArgumentException("listener cannot be null");  
      }
      m_uiListeners.remove(l);
   }
   
   /**
    * Un-register for change notifications.
    * <p>
    * This is a convenience method. Callers may unregister directly with each
    * model.
    * 
    * @param listener Never <code>null</code>. If this listener is not
    * registered, returns immediately.
    * 
    * @param filter Only models for these object types will be processed. Never
    * <code>null</code>. If empty, returns immediately.
    */
   public void removeListener(IPSModelListener listener,
         Set<PSObjectType> filter)
   {
      if ( null == listener)
      {
         throw new IllegalArgumentException("listener cannot be null");  
      }
      if ( null == filter)
      {
         throw new IllegalArgumentException("filter cannot be null");  
      }
      
      if (filter.size() == 0)
         return;
      
      try
      {
         for (PSObjectType type : filter)
         {
            IPSCmsModel model = PSCoreFactory.getInstance().getModel(
                  type.getPrimaryType());
            model.removeListener(listener);
         }
      }
      catch (PSModelException e)
      {
         //not expected to happen
         throw new RuntimeException(e);
      }
   }

   /**
    * Any client that makes changes to an object must call this method for each
    * change, as noted in the class description. This is a convenience method
    * that calls {@link IPSCmsModel#propertyChanged(IPSReference, Map)
    * ref.getParentModel().propertyChanged(ref, hint)}.
    */
   public void propertyChanged(IPSReference ref, Map<String, String> hint)
      throws PSLockException
   {
      //let the called method validate
      ref.getParentModel().propertyChanged(ref, hint);
   }

   /**
    * Removes the supplied ref from {@link #m_lifecycleRefs}, if present.
    * 
    * @param testRef Assumed not <code>null</code>.
    */
   private void removeLifecycleControl(IPSReference testRef)
   {
      for (IPSReference ref : m_lifecycleRefs)
      {
         if (ref.equals(testRef))
         {
            m_lifecycleRefs.remove(ref);
            return;
         }
      }
   }

   /**
    * Scans {@link #m_lifecycleRefs} for a matching ref.
    * 
    * @param testRef Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if one is found, <code>false</code> otherwise.
    */
   private boolean isLifecycleControlled(IPSReference testRef)
   {
      for (IPSReference ref : m_lifecycleRefs)
      {
         if (ref.equals(testRef))
            return true;
      }
      return false;
   }

   /**
    * This is a singleton object. Use {@link #getInstance()}.
    */
   private PSModelTracker()
   {}

   /**
    * The single instance of this class. Initialized during first call to 
    * {@link #getInstance()}, then never changed.
    */
   private static PSModelTracker ms_tracker;

   /**
    * Contains the references that have been loaded and locked by a lifecycle
    * owner. This information is used to determine how to save data and 
    * release locks. Never <code>null</code>, may be empty.
    * <p>
    * A collection is used instead of a map because the references can change
    * unexpectedly and that would screw up our hash map.
    */
   private Collection<IPSReference> m_lifecycleRefs = 
      new ArrayList<IPSReference>();

   /**
    * Maintains the set of all known design object change listeners. Never 
    * <code>null</code>, may be empty.
    */
   private final Set<IPSDesignObjectChangeListener> m_uiListeners = 
      new HashSet<IPSDesignObjectChangeListener>();
}
