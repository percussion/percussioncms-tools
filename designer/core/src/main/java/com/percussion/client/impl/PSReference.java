/******************************************************************************
 *
 * [ PSReference.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectType;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.DateUtils;

/**
 * See {@link IPSReference} for details. 
 * <p>
 * Generally, instances are created from xml serialized streams. Thus the
 * empty ctor and the various set methods. If the empty ctor is used, the 
 * various properties MUST be set before it is passed off for use. 
 * <p>
 * It is also possible to create one from parts in 1 shot with the multi-param
 * ctor.
 * <p>
 * The object should be considered immutable after construction is complete
 * except for the name, which may be changed.
 * 
 * @author Paul Howard
 * @version 6.0
 */
public class PSReference implements IPSReference
{
   /**
    * This ctor is for creating references to objects that have never been
    * persisted.
    * 
    * @param name See {@link #setName(String)}.
    * @param labelKey See {@link #setLabelKey(String)}.
    * @param description See {@link #setDescription(String)}.
    * @param objectType See {@link #setObjectType(PSObjectType)}.
    * @param id May be <code>null</code>.
    * 
    * @throws PSModelException If a model for <code>objectType</code> cannot
    * be found. 
    */
   public PSReference(String name, String labelKey, String description,
         PSObjectType objectType, IPSGuid id)
      throws PSModelException
   {
      setName(name);
      setLabelKey(labelKey);
      setDescription(description);
      setObjectType(objectType);
      if (id != null)
         setId(id);
      refresh();
   }

   /**
    * Copy ctor may be useful by derived classes. Performs no validation,
    * fields are copied directly.
    * 
    * @param ref Never <code>null</code>.
    */
   protected PSReference(IPSReference ref)
   {
      m_age = ref.getAge();
      m_description = ref.getDescription();
      m_id = ref.getId();
      m_isPersisted = ref.isPersisted();
      m_isReadOnly = ref.isReadOnly();
      m_labelKey = ref.getLabelKey();
      m_name = ref.getName();
      m_objectType = ref.getObjectType();
      m_lockSessionId= ref.getLockSessionId();
      m_lockUserName = ref.getLockUserName();
   }
   
   

   /* 
    * @see java.lang.Object#clone()
    */
   @Override
   public Object clone()
   {
      PSReference ref = 
         new PSReference();
      ref.m_age = m_age;
      ref.m_description = m_description;
      ref.m_id = m_id;
      ref.m_isPersisted = m_isPersisted;
      ref.m_isReadOnly = m_isReadOnly;
      ref.m_labelKey = m_labelKey;
      ref.m_name = m_name;
      ref.m_objectType = m_objectType;
      ref.m_lockSessionId = m_lockSessionId;
      ref.m_lockUserName = m_lockUserName;
      return ref;
   }

   /**
    * The caller may wish to force a recatalog if objects get too old. The
    * age can be reset by calling {@link #refresh()}.
    * 
    * @return The number of seconds since this object was last refreshed.
    */
   public long getAge()
   {
      return (System.currentTimeMillis() - m_age) / DateUtils.MILLIS_PER_SECOND;
   }

   /**
    * Reset the age to 0. The purpose of this is to indicate that a catalog has
    * been performed and this object is still the same. 
    */
   public void refresh()
   {
      m_age = System.currentTimeMillis();
   }
   
   /**
    * An object type contains one or 2 enumerations that uniquely identify the
    * type, or category that an object belongs to. Objects in the core are
    * typed by the {@link com.percussion.client.PSObjectTypes} enumerations.
    * 
    * @return Never <code>null</code>.
    */
   public PSObjectType getObjectType()
   {
      if (null == m_objectType)
         throw new IllegalStateException("Object not properly initialized.");
      return m_objectType;
   }

   /**
    * See {@link #getObjectType()} for details. This method is only public
    * for use by serialization mechanisms. 
    * The type is validated by searching for a model that matches it.
    * The model and type must match, meaning that the model must support the
    * specified type. 
    * @param type Never <code>null</code>.
    * 
    * @throws PSModelException If a model for this type cannot be found.
    */
   public void setObjectType(PSObjectType type)
      throws PSModelException
   {
      if ( null == type)
      {
         throw new IllegalArgumentException("type cannot be null");  
      }
      getModel(type.getPrimaryType());
      m_objectType = type;
   }

   /**
    * The model is used to manipulate the data associated with this reference.
    * 
    * @return Never <code>null</code>.
    */
   public IPSCmsModel getParentModel()
   {
      try
      {
         if (null == m_objectType)
            throw new IllegalStateException("Object not properly initialized.");
         return getModel(m_objectType.getPrimaryType());
      }
      catch (PSModelException e)
      {
         //this should never happen because we validated the type when it was set
         throw new RuntimeException("should never happen", e);
      }
   }

   /**
    * Read-only objects cannot be locked when they are loaded. This is a
    * fundamental property, not related to ACL settings. Defaults to 
    * <code>false</code> if never set.
    */
   public boolean isReadOnly()
   {
      return m_isReadOnly;
   }

   /**
    * See {@link #isReadOnly()} for details.
    * 
    * @param readOnly <code>true</code> to indicate that the object associated
    * with this reference can never be locked for updating, <code>false</code>
    * otherwise.
    */
   public void setReadOnly(boolean readOnly)
   {
      m_isReadOnly = readOnly;
   }
   
   /**
    * This flag indicates whether an object has been created (via one of the
    * <code>create</code> methods), but never saved to the server. Do not
    * depend on the presence of a GUID to make such a decision as the core
    * may create local guids.
    */
   public boolean isPersisted()
   {
      return m_isPersisted;
   }

   /**
    * After the object is first persisted, this method should be called to
    * indicate that.
    */
   public void setPersisted()
   {
      m_isPersisted = true;
   }

   // see interface
   public String getDescription()
   {      
      return m_description;
   }
   
   /* 
    * @see com.percussion.client.IPSReference#getLocalDescription()
    */
   public String getLocalDescription()
   {
      IPSCmsModel model = getParentModel();
      return model.getDescription(this);
   }

   /**
    * See {@link #getDescription()} for details.
    * 
    * @param desc May be <code>null</code> or empty. 
    */
   public void setDescription(String desc)
   {
      m_description = desc == null ? "" : desc;
   }
   
   // see interface
   public IPSGuid getId()
   {
      return m_id;
   }

   /**
    * See {@link #getId()} for details.
    * 
    * @param id Never <code>null</code>.
    */
   public void setId(IPSGuid id)
   {
      if ( null == id)
      {
         throw new IllegalArgumentException("id cannot be null");  
      }
      m_id = id;
   }
   
   // see interface
   public String getName()
   {
      if (null == m_name)
         throw new IllegalStateException("Object not properly initialized.");
      return m_name;
   }
   
   /* 
    * @see com.percussion.client.IPSReference#getLocalName()
    */
   public String getLocalName()
   {
      IPSCmsModel model = getParentModel();
      return model.getName(this);
   }
   
   /**
    * See {@link #getLabelKey()} for details.
    * 
    * @param key May be <code>null</code> or empty, which means "use the name
    * as the key".
    */
   public void setLabelKey(String key)
   {
      m_labelKey = StringUtils.isBlank(key) ? null : key;
   }
   
   // see interface
   public String getLabelKey()
   {
      return m_labelKey == null ? getName() : m_labelKey;
   }
   
   /* 
    * @see com.percussion.client.IPSReference#getLocalLabelKey()
    */
   public String getLocalLabelKey()
   {
      IPSCmsModel model = getParentModel();
      return model.getLabelKey(this);
   }
   
   /**
    * See {@link #getName()} for details.
    * 
    * @param name Never <code>null</code> or empty. May not contain white space.
    */
   public void setName(String name)
   {
      //fixme: remove all code except the commented line and uncomment it when
      //  the fast-forward data no longer has spaces in the names
      //TODO: also uncomment the corresponding unit test (see TODOs in the test)
      if (PSCoreUtils.isValidObjectName(name))
      {
         m_name = name; 
      }
      else if (PSCoreUtils.isValidHierarchyName(name))
         m_name = name;
      else
         throw new IllegalArgumentException("Invalid name");
//      setName(name, false);
   }
   
   /**
    * This method allows a derived class more flexibility with the name. The
    * flag controls what is considered a valid name.
    * 
    * @param name Never <code>null</code> or empty.
    * 
    * @param allowHierarchyName If <code>true</code>, then the name is
    * validated by calling {@link PSCoreUtils#isValidHierarchyName(String)}.
    * Otherwise, the name is validated by calling
    * {@link PSCoreUtils#isValidObjectName(String)}.
    */
   protected void setName(String name, boolean allowHierarchyName)
   {
      boolean valid = true;
      if (allowHierarchyName)
         valid = PSCoreUtils.isValidHierarchyName(name);
      else
         valid = PSCoreUtils.isValidObjectName(name);
      if (!valid)
      {
         throw new IllegalArgumentException("Invalid name");
      }
      m_name = name;
   }
   
   /**
    * This ctor must not be used except by the de-serialization mechanism. It is
    * public as a side effect of the implementation. When this method is used,
    * the following setter methods MUST be called before the object is returned
    * to a client:
    * <ol>
    * <li>{@link #setName(String)}</li>
    * <li>{@link #setObjectType(PSObjectType)}</li>
    * <ol>
    * If not, the <code>get</code> methods will throw 
    * <code>IllegalStateException</code>. 
    * <p>
    * It is assumed that refs created this way are associated with persisted
    * objects.
    */
   public PSReference()
   {
      m_isPersisted = true;
   }

   //see interface
   public boolean referencesSameObject(IPSReference other)
   {
      if (other == null)
         return false;
      
      if (!getObjectType().equals(other.getObjectType()))
         return false;
      
      IPSGuid id1 = getId();
      IPSGuid id2 = other.getId();
      
      if (id1 != null && id2 != null)
         return id1.equals(id2);

      if (id1 == null && id2 == null)
         return getName().equalsIgnoreCase(other.getName());
      
      return false;
   }

   /**
    * @see com.percussion.client.IPSReference#getLockUserName()
    */
   public String getLockUserName()
   {
      return m_lockUserName;
   }

   /**
    * Set the lock sessionid.
    * @param lockUserName locke user name to set, may be <code>null</code> or empty.
    */
   public void setLockUserName(String lockUserName)
   {
      if(StringUtils.isEmpty(lockUserName))
         lockUserName = null;
      m_lockUserName = lockUserName;
   }

   /**
    * @see com.percussion.client.IPSReference#getLockSessionId()
    */
   public String getLockSessionId()
   {
      return m_lockSessionId;
   }

   /**
    * Set the lock sessionid.
    * @param lockSessionId session id to set, may be <code>null</code> or empty.
    */
   public void setLockSessionId(String lockSessionId)
   {
      if(StringUtils.isEmpty(lockSessionId))
         lockSessionId = null;
      m_lockSessionId = lockSessionId;
   }

   /* (non-Javadoc)
    * @see com.percussion.client.IPSReference#isLocked()
    */
   public boolean isLocked()
   {
      return getLockSessionId() != null;
   }

   //see interface
   public int[] getPermissions()
   {
      return m_permissions;
   }

   /**
    * Set permission values. No validation is done for the valid permissions.
    * @param permissions must not be <code>null</code>, may be empty.
    */
   public void setPermissions(int[] permissions)
   {
      m_permissions = permissions;
   }
   
   /**
    * @inheritDoc
    * Age is not considered in the comparison.
    */
   @Override public boolean equals(Object o)
   {
      if (o == null)
         return false;
      if (!(o instanceof PSReference))
         return false;
      if (o == this)
         return true;
      PSReference rhs = (PSReference) o;
      boolean result = new EqualsBuilder()
            .append(m_description, rhs.m_description)
            .append(m_id, rhs.m_id)
            .append(m_isReadOnly, rhs.m_isReadOnly)
            .append(m_labelKey, rhs.m_labelKey)
            .append(m_name, rhs.m_name)
            .append(m_objectType, rhs.m_objectType)
            .isEquals();
      return result;
   }
   
   /**
    * @inheritDoc
    * Only the id or name and object type are considered for the hashcode.
    * The id is used if it is not <code>null</code>, otherwise, the name is
    * used.
    */
   @Override public int hashCode()
   {
      HashCodeBuilder builder = new HashCodeBuilder();
      if (m_id != null)
         builder.append(m_id);
      else
         builder.append(m_name);
      
      return builder.append(m_objectType)
            .toHashCode();
   }
   
   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return new ToStringBuilder(this)
            .append("id", getId())
            .append("name", getName())
            .append("objectType", getObjectType())
            .toString();
   }
   
   /**
    * Makes sure a model for the specified type exists.
    * @param type the type to check model for. Should not be <code>null</code>. 
    * @return the model for given type.
    * @throws PSModelException If a model for this type cannot be found.
    */
   protected IPSCmsModel getModel(final Enum type) throws PSModelException
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type cannot be null");
      }
      return PSCoreFactory.getInstance().getModel(type);
   }

   /**
    * The name of the object this reference represents. Never <code>null</code>
    * or empty after construction. It may be modified if a client changes
    * the object name through the model.  This has been made protected so that
    * the name validation can be overridden.
    */
   protected String m_name;
   
   /**
    * A brief overview of the object pointed to by this ref. Never
    * <code>null</code>, may be empty
    */
   private String m_description = "";
   
   /**
    * The specific type of the object. Never <code>null</code> after properly
    * constructed.
    */
   private PSObjectType m_objectType;
   
   /**
    * A unique identifier for the object if it has ever been persisted.
    * If never persisted, it may be <code>null</code>, or it may be a 'fake'
    * id. 
    */
   private IPSGuid m_id;
   
   /**
    * How long since this object was created, or since {@link #refresh()} had
    * been called, whichever was more recent; in seconds.
    */
   private long m_age = 0;
   
   /**
    * Has the associated object ever been persisted to permanent storage?
    * Defaults to <code>false</code>.
    */
   private boolean m_isPersisted = false;
   
   /**
    * Specifies whether the associated object can be locked when loaded.
    * Defaults to <code>false</code>.
    */
   private boolean m_isReadOnly = false;

   /**
    * The label for this object when displayed to a business user, translated
    * to the current locale. If <code>null</code>, then the name is returned.
    * Either <code>null</code> or non-empty.
    */
   private String m_labelKey = null;
   
   /**
    * @see #getLockSessionId()
    */
   private String m_lockSessionId = null;
   
   /**
    * @see #getLockUserName()
    */
   private String m_lockUserName = null;

   /**
    * @see #getPermissions()
    */
   private int[] m_permissions = new int[0];

   /**
    * For serializable impl.
    */
   private static final long serialVersionUID = 1L;
}
