/******************************************************************************
 *
 * [ IPSReference.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.models.IPSCmsModel;
import com.percussion.utils.guid.IPSGuid;

import java.io.Serializable;
import java.util.Comparator;

/**
 * This class contains the basic information for a design object, including a
 * unique identifier. These objects are used to manage design objects within the
 * core.
 * <p>References should not be used as hash keys because they may change if 
 * the caller renames them. 
 * 
 * @author paulhoward
 * @version 6.0
 */
public interface IPSReference extends Serializable
{
   /**
    * An optional message describing the object. It is not internationalized.
    * <p>
    * This is the persisted description. If you want local changes (if any),
    * use {@link #getLocalDescription()}.
    * 
    * @return Never <code>null</code>, may be empty.
    */
   public String getDescription();   

   /**
    * Unique object identifier across the CMS system. It is possible that
    * objects that have been persisted will return a <code>null</code> value.
    * In this case, the name is the unique identifier. If you want to know if
    * the object has ever been persisted, use the {@link #isPersisted()} method.
    * If an object has not been persisted, this method will return
    * <code>null</code>.
    * 
    * @return May be <code>null</code>.
    */
   public IPSGuid getId();

   /**
    * This is the name of the object. It is unique among all objects of the same
    * type. This name is not internationalized.
    * <p>
    * This is the persisted name. If you want local changes (if any),
    * use {@link #getLocalName()}.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getName();

   /**
    * This is the key used to find the display label based on the user's locale.
    * The label is a (typically) more friendly name for the object. There is no 
    * guaranatee that it is unique among objects of the same type, like the 
    * name is. If there is no entry found, the key becomes the label.
    * <p>
    * This is the persisted label. If you want local changes (if any),
    * use {@link #getLocalLabelKey()}.
    * 
    * @return Never <code>null</code> or empty. If there is no key, then
    * returns the name.
    */
   public String getLabelKey();
   
   /**
    * Same as {@link #getDescription()}, except that the description is
    * retrieved from the model's local cached data object, if there is one.
    * Otherwise, returns the same value as <code>getDescription</code>. Great
    * care should be used when calling this method from the model and proxy as
    * it is quite easy to cause an infinite loop.
    * 
    * @return Never <code>null</code> may be empty.
    */
   public String getLocalDescription();
   
   /**
    * Same as {@link #getName()}, except that the name is retrieved from the
    * model's local cached data object, if there is one. Otherwise, returns the
    * same value as <code>getName</code>. Great care should be used when
    * calling this method from the model and proxy as it is quite easy to cause
    * an infinite loop.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getLocalName();
   
   /**
    * Same as {@link #getLabelKey()}, except that the label is retrieved from
    * the model's local cached data object, if there is one. Otherwise, returns
    * the same value as <code>getLabelKey</code>. Great care should be used
    * when calling this method from the model and proxy as it is quite easy to
    * cause an infinite loop.
    * 
    * @return Never <code>null</code> or empty.
    */
   public String getLocalLabelKey();

   /**
    * Identifies the primary type and possibly a sub-type, depending on the
    * primary type.
    * 
    * @return Never <code>null</code>.
    */
   public PSObjectType getObjectType();

   /**
    * This model can be used to retrieve and persist the data referenced by this
    * object.
    * 
    * @return Never <code>null</code>.
    */
   public IPSCmsModel getParentModel();

   /**
    * Read-only objects cannot be locked when they are loaded. This is a
    * fundamental property, not related to ACL settings. In other words, this
    * method could return <code>false</code>, but the object may not be
    * lockable because the caller does not have write access.
    */
   public boolean isReadOnly();

   /**
    * This flag indicates whether an object has been created (via one of
    * <code>IPSCmsModel's</code> <code>create</code> methods), but never
    * saved to the server. Do not depend on the presence of a GUID to make such
    * a decision as the core creates local guids.
    */
   public boolean isPersisted();

   /**
    * The caller may wish to force a re-catalog if info gets too old.
    * 
    * @return The number of seconds since this information was retrieved from
    * the server, or if it was created locally, since it was created.
    */
   public long getAge();
   
   /**
    * If the supplied object is an instance of the implementing class, then all
    * properties except age are checked for equality. Strings are compared
    * case-insensitive.
    * <p>
    * Generally, 2 references that refer to the same object are equal, but there
    * are situations where that is not the case. Therefore, this method should
    * not be used to determine if 2 references refer to the same object. To make
    * that distinction, use the {@link #referencesSameObject(IPSReference)}
    * method.
    * 
    * @param other The object to test against. May be <code>null</code>.
    * 
    * @return <code>true</code> if they are equal as described above,
    * <code>false</code> otherwise.
    */
   public boolean equals(Object other);
   
   /**
    * Must be defined whenever {@link #equals(Object)} is defined.
    * @see Object#hashCode()
    */
   public int hashCode();
   
   /**
    * Determines if this reference refers to the same design object as the
    * supplied reference. See {@link #equals(Object)} for additional details on
    * equality of 2 references versus referencing the same design object.
    * 
    * @param other The comparand. If <code>null</code>, <code>false</code>
    * is returned.
    * 
    * @return This condition is checked as follows. If both references are not
    * the same type, <code>false</code> is returned. If both references have a
    * valid id, then if both ids are equal, this method returns
    * <code>true</code>. Otherwise if neither reference has an id, then the
    * names are compared, case-insensitive. Otherwise, <code>false</code> is
    * returned.
    */
   public boolean referencesSameObject(IPSReference other);
   
   /**
    * Get the name of the locker of the design object.
    * @return locker's name, <code>null</code> if not locked to anyone.
    */
   public String getLockUserName();

   /**
    * The session id this design object locked by. 
    * @return locked session id, <code>null</code> if not locked to anyone.
    */
   public String getLockSessionId();
   
   /**
    * Is the object which this references locked?
    * 
    * @return <code>true</code> if its locked, <code>false</code> otherwise.
    */
   public boolean isLocked();
   
   /**
    * Get current user permissions as received from the server during
    * cataloging. These values correspond to the permission ordinals defined in
    * {@link com.percussion.services.security.PSPermissions}.
    * 
    * @return pemission values, never <code>null</code> may be empty.
    *///todo - ph: these should be PSPermissions
   public int[] getPermissions();
   
   /**
    * Sorts references based on labels.
    */
   public static class LabelKeyComparator implements Comparator<IPSReference>
   {
      public int compare(IPSReference ref1, IPSReference ref2)
      {
         return ref1.getLabelKey().toLowerCase().compareTo(ref2.getLabelKey().toLowerCase());
      }
   }
   
   /**
    * Sorts references based on names.
    */
   public static class NameKeyComparator implements Comparator<IPSReference>
   {
      public int compare(IPSReference ref1, IPSReference ref2)
      {
         return ref1.getName().toLowerCase().compareTo(ref2.getName().toLowerCase());
      }
   }
}
