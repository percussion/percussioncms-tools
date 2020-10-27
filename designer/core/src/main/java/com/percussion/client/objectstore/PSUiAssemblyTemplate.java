/******************************************************************************
 *
 * [ PSUiAssemblyTemplate.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.objectstore;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.services.assembly.data.PSAssemblyTemplate;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.utils.xml.PSXmlSerializationHelper;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds UI-specific functionality - keeping track of content types referring the
 * assembly template
 * 
 * @author Andriy Palamarchuk
 */
public class PSUiAssemblyTemplate extends PSAssemblyTemplate
      implements IPSUiAssemblyTemplate
{
   static
   {
      PSXmlSerializationHelper.addType("assemblytemplate",
            PSUiAssemblyTemplate.class);
   }

   // see interface
   public Set<IPSGuid> getSlotGuids()
   {
      Set<IPSGuid> results = new HashSet<IPSGuid>();
      for (IPSGuid guid : m_slots)
         results.add(guid);

      return results;
   }

   /**
    * Add a slot to the collection. Used by serialization. This code will ignore
    * missing slots referenced from the xml.
    * 
    * @param id Never <code>null</code>;
    * @throws PSModelException 
    */
   public void addSlotGuid(IPSGuid id) throws PSModelException
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      IPSReference ref = PSCoreFactory.getInstance()
         .getModel(PSObjectTypes.SLOT).getReference(id);
      if (ref == null) return;
      
      m_slots.add(id);
   }

   /**
    * Added for serialization.
    */
   @Override
   public void addTemplateSlotId(Long slotid)
   {
      try
      {
         addSlotGuid(new PSGuid(PSTypeEnum.SLOT, slotid.longValue()));
      }
      catch (PSModelException e)
      {
         // Ignore
      }
   }

   // see interface
   public void setSlotGuids(Set<IPSGuid> slots)
   {
      m_slots.clear();
      if (slots == null || slots.isEmpty())
      {
         return;
      }
      m_slots.addAll(slots);
   }

   /**
    * Get site references associated with this template.
    * 
    * @return list of site guids, never <code>null</code>, may be empty, no
    * <code>null</code> entries.
    */
   public Set<IPSReference> getSites()
   {
      return m_sites;
   }

   /**
    * Set site references that are associated with this template. No validation
    * on the supplied site ids is performed.
    * 
    * @param siteIds list of site guids, must not be <code>null</code>, may
    *           be empty, no <code>null</code> entries.
    */
   public void setSites(Set<IPSReference> siteIds)
   {
      if (siteIds == null)
      {
         throw new IllegalArgumentException("siteIds must not be null");
      }
      for (IPSReference ref : siteIds)
      {
         if (ref == null)
         {
            throw new IllegalArgumentException(
                  "siteIds array cannot have null entries");
         }
      }
      m_sites = siteIds;
   }

   /**
    * Method for serialization to add a site id. Ignores ids that don't match
    * a known site.
    * 
    * @param id the id, never <code>null</code>
    */
   public void addSiteId(Long id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      IPSGuid guid = new PSGuid(PSTypeEnum.SITE, id);
      try
      {
         IPSReference ref = PSCoreFactory.getInstance()
               .getModel(PSObjectTypes.SITE).getReference(guid);
         if (ref == null) return;
         m_sites.add(ref);
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Get the site ids for serialization
    * @return the site ids, may be empty, never <code>null</code>
    */
   public List<Long> getSiteIds()
   {
      List<Long> rval = new ArrayList<Long>();
      for(IPSReference ref : m_sites)
      {      
         rval.add(ref.getId().longValue());
      }
      return rval;
   }
   
   /**
    * Method for serialization to add a content type id
    * 
    * @param id the id, never <code>null</code>
    */
   public void addContentId(Long id)
   {
      if (id == null)
      {
         throw new IllegalArgumentException("id may not be null");
      }
      IPSGuid guid = new PSGuid(PSTypeEnum.NODEDEF, id);
      if (m_newContentTypes == null)
      {
         m_newContentTypes = new HashSet<IPSReference>();
      }
      try
      {
         IPSReference ref = PSCoreFactory.getInstance()
               .getModel(PSObjectTypes.CONTENT_TYPE).getReference(guid);
         m_newContentTypes.add(ref);
      }
      catch (PSModelException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Get the content type ids for serialization
    * @return the content type ids, may be empty, never <code>null</code>
    */
   public List<Long> getContentIds()
   {
      List<Long> rval = new ArrayList<Long>();
      if(m_newContentTypes != null)
      {
         for(IPSReference ref : m_newContentTypes)
         {      
            rval.add(ref.getId().longValue());
         }
      }
      return rval;
   }   

   /**
    * Content types referring to given template. If not <code>null</code> make
    * only these content types to refer this template. If any other content type
    * refers to this.
    * 
    * @return May be <code>null</code>.
    */
   public Set<IPSReference> getNewContentTypes()
   {
      return m_newContentTypes;
   }

   /**
    * @see #getNewContentTypes()
    * @param newContentTypes set to not-<code>null</code> value to overwrite
    * content types. May be <code>null</code>.
    */
   public void setNewContentTypes(Set<IPSReference> newContentTypes)
   {
      m_newContentTypes = newContentTypes;
      if (newContentTypes != null)
      {
         for (final IPSReference r : newContentTypes)
         {
            if (!r.isPersisted())
            {
               throw new IllegalArgumentException(
                     "Attempt to associate not persisted content type "
                     + r.getName() + " with template " + getName());
            }
            if (!r.getObjectType().getPrimaryType().equals(
                  PSObjectTypes.CONTENT_TYPE))
            {
               throw new IllegalArgumentException("Provided reference " + r
                     + " has unexpected object type " + r.getObjectType());
            }
         }
      }
   }

   /**
    * Returns <code>true</code> when {@link #getNewContentTypes()} returns
    * not-<code>null</code> and not-empty value indicating that
    * content type references
    * should be overwritten with the specified values.
    */
   public boolean areNewContentTypesSpecified()
   {
      return getNewContentTypes() != null && !getNewContentTypes().isEmpty();
   }

   /**
    * See {@link #setGUID(IPSGuid)}.
    */
   @Override
   public IPSGuid getGUID()
   {
      if (m_id == 0)
         return super.getGUID();
      return new PSGuid(PSTypeEnum.TEMPLATE, m_id);
   }

   /**
    * Overridden to allow changing the guid. This is for test purposes only. If
    * the underlying object already has a guid set, we store it here, otherwise
    * it is set on the base object. {@link #getGUID()} is overridden to
    * implement this behavior properly.
    */
   @Override
   public void setGUID(IPSGuid newguid)
   {
      if (super.getGUID().getUUID() == 0)
         super.setGUID(newguid);
      else if (newguid != null)
         m_id = newguid.longValue();
      else
         m_id = 0;
   }


   /**
    * See {@link #setGUID(IPSGuid)}. Initialized to 0.
    */
   private long m_id = 0;

   /**
    * @see #getNewContentTypes()
    */
   private Set<IPSReference> m_newContentTypes;

   /**
    * @see #getSites()
    * @see #setSites(Set)
    */
   private Set<IPSReference> m_sites = new HashSet<IPSReference>();
   
   /**
    * Stores the guids of the slots instead of storing actual slot instances.
    * Slots are handled this way in the workbench, with the actual slot collection
    * in the parent class ignored.
    */
   private Set<IPSGuid> m_slots = new HashSet<IPSGuid>();

}
