/******************************************************************************
 *
 * [ PSSecurityUtils.java ]
 *
 * COPYRIGHT (c) 1999 - 2009 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.*;
import com.percussion.services.security.data.PSAclEntryImpl;
import com.percussion.services.security.data.PSAclImpl;
import com.percussion.security.IPSTypedPrincipal.PrincipalTypes;
import com.percussion.webservices.common.PSObjectSummary;
import com.percussion.webservices.security.data.PSCommunityVisibility;
import com.percussion.webservices.securitydesign.GetVisibilityByCommunityRequest;
import com.percussion.webservices.securitydesign.SecurityDesignSOAPStub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.acl.NotOwnerException;
import java.util.*;

/**
 * This class has helper methods that allow easy access to various one off
 * security methods that don't belong in one of the proxies.
 */
public class PSSecurityUtils
{

   private static final Logger log =  LogManager.getLogger(PSSecurityUtils.class);

   /**
    * Private ctor
    */
   private PSSecurityUtils()
   {
   }

   /**
    * Convenience method that calls (roughly)
    * {@link #getVisibilityByCommunity(List, PSTypeEnum) 
    * getVisibilityByCommunity(new List(community))}.
    * 
    * @param community Never <code>null</code>. Must be of object type
    * community.
    */
   public static List<IPSReference> getVisibilityByCommunity(
      IPSReference community, PSTypeEnum type) throws PSModelException
   {
      if (community == null)
         throw new IllegalArgumentException("community cannot be null.");
      List<IPSReference> commList = new ArrayList<>(1);
      commList.add(community);
      return getVisibilityByCommunity(commList, type);

   }

   /**
    * Retrieves references for all visible objects for one or more specified
    * communities.
    * 
    * @param communities list of communities to which we want to find visible
    * objects on. Cannot be <code>null</code> or empty. Entries must be of
    * object type community.
    * @param type the server type for the objects that should be returned, may
    * be <code>null</code> in which case all types will be passed back.
    * @return references for all objects visible in the passed in communities.
    * Never <code>null</code>, may be empty.
    * @throws PSModelException upon any error
    */
   public static List<IPSReference> getVisibilityByCommunity(
      List<IPSReference> communities, PSTypeEnum type) throws PSModelException
   {
      if (communities == null || communities.isEmpty())
         throw new IllegalArgumentException(
            "communities cannot be null or empty.");
      for (IPSReference commRef : communities)
      {
         if (commRef.getObjectType().getPrimaryType() != PSObjectTypes.COMMUNITY)
            throw new IllegalArgumentException(
               "All references passed in must be of object type community.");
      }

      List<IPSReference> results = new ArrayList<>();
      long[] comms = new long[communities.size()];
      int count = 0;
      PSCoreFactory factory = PSCoreFactory.getInstance();
      for (IPSReference ref : communities)
      {
         comms[count++] = ((PSDesignGuid) ref.getId()).getValue();
      }

      try
      {
         SecurityDesignSOAPStub stub = PSProxyUtils.getSecurityDesignStub();
         GetVisibilityByCommunityRequest request = 
            new GetVisibilityByCommunityRequest();
         request.setId(comms);
         if (type != null)
            request.setType((int) type.getOrdinal());
         PSCommunityVisibility[] visible = stub.getVisibilityByCommunity(
            request);
         for (PSCommunityVisibility cv : visible)
         {
            for (PSObjectSummary summary : cv.getVisibleObjects())
            {
               if(summary != null) {
               PSTypeEnum serverType = PSTypeEnum.valueOf(summary.getType());
               Enum primaryType = PSObjectTypeFactory
                  .convertServerTypeToPrimaryType(serverType);
               if (primaryType != null)
               {
                  IPSCmsModel model = factory.getModel(primaryType);
                  IPSReference ref = model.getReference(new PSGuid(summary
                     .getId()));
                  if (ref != null)
                     results.add(ref);
               }
            }
         }
         }

      }
      catch (Exception e)
      {
         log.error("Unexpected error",e);
      }

      return results;
   }

   /**
    * Retrieves the union of references for all visible objects for one or more
    * specified communities.
    * 
    * @param communities list of communities to which we want to find visible
    * objects on. Cannot be <code>null</code> or empty. Must be of object type
    * community.
    * @param type the server type for the objects that should be returned, must
    * not be <code>null</code>.
    * @return references for all objects visible in the passed in communities.
    * Never <code>null</code>, may be empty.
    * @throws PSModelException upon any error
    */
   public static Set<IPSReference> getObjectsByCommunityVisibility(
      List<IPSReference> communities, PSTypeEnum type) throws PSModelException
   {
      if (communities == null || communities.isEmpty())
         throw new IllegalArgumentException(
            "communities cannot be null or empty.");
      if (type == null)
         throw new IllegalArgumentException("type cannot be null.");

      return new HashSet<>(getVisibilityByCommunity(communities, type));
   }

   /**
    * Gets the list of community IPSReference objects that are visible to the
    * supplied IPSAcl object. Catalogs the communities and calls the
    * com.percussion.services.security.data.PSSecurityUtils.getVisibleCommunities(IPSAcl,Collection<IPSGuid>)
    * method to get the visible communities
    * 
    * @param acl Object of IPSAcl for which the community visibility needed
    * @return list of IPSReference objects of community. Never <code>null</code>,
    * may be empty.
    * @throws PSModelException in case of an error
    */
   public static List<IPSReference> getVisibleCommunities(IPSAcl acl)
      throws PSModelException
   {
      List<IPSReference> commList = PSCoreUtils.catalog(
         PSObjectTypes.COMMUNITY, true);
      Map<String, IPSReference> comms = new HashMap<>();
      for (IPSReference ref : commList)
      {
         comms.put(ref.getName(), ref);
      }
      List<String> lst = new ArrayList<>(comms.keySet());
      Collection<String> visibleComms = com.percussion.services.security.data.PSSecurityUtils
         .getVisibleCommunities(acl, lst);
      List<IPSReference> retList = new ArrayList<>();
      if (visibleComms.size() == comms.size())
      {
         retList.addAll(commList);
      }
      else
      {
         for (String comm : visibleComms)
         {
            retList.add(comms.get(comm));
         }
      }
      return retList;
   }

   /**
    * Calls getVisibleCommunities(IPSAcl) method to get the visible communities
    * 
    * @param ref Object of IPSReference for which the community visibility is
    * needed.
    * @return list of IPSReference objects of community. Never <code>null</code>,
    * may be empty.
    * @throws PSModelException in case of an error
    */
   public static List<IPSReference> getVisibleCommunities(IPSReference ref)
      throws PSModelException
   {
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
         PSObjectTypes.CONTENT_TYPE);
      IPSAcl acl = (IPSAcl) model.loadAcl(ref, false);
      return getVisibleCommunities(acl);
   }

   /**
    * Tell if the reference contains ACL owner permission. The logged user
    * permissions are returned part of summaries during catalogging. This method
    * just walks through each permission to see it has
    * {@link com.percussion.services.security.PSPermissions#OWNER}
    * permission. If the supplied reference is not persisted yet to server, then
    * the result is always <code>true</code>.
    * 
    * @param ref the object reference, must not be <code>null</code>.
    * @param forceLoad Load the permissions from server before checking for
    * owner permission.
    * @return <code>true</code> if reference has owner permission or the
    * referenced object s not persisted to server, <code>false</code>
    * otherwise.
    */
   static public boolean hasOwnerPermission(IPSReference ref, boolean forceLoad)
   {
      if (!ref.isPersisted())
      {
         return true;
      }
      int[] permissions = ref.getPermissions();
      if (forceLoad)
      {
         // Load permissions from server
         // permissions = ???;
      }
      for (int permission : permissions)
      {
         if (permission == PSPermissions.OWNER.getOrdinal())
            return true;
      }
      return false;
   }
   
   /**
    * Checks if the referenced object allows the supplied permission.
    *
    * @param ref the object to check, not <code>null</code>.
    * @param permission the permission to check, not <code>null</code>.
    * @return <code>true</code> if the referenced object allows the supplied 
    *    permission, <code>false</code> otherwise.
    */
   public static boolean hasPermission(IPSReference ref, 
      PSPermissions permission)
   {
      if (ref == null)
         throw new IllegalArgumentException("ref cannot be null");
      
      if (permission == null)
         throw new IllegalArgumentException("permission cannot be null");
      
      for (int perm : ref.getPermissions())
      {
         if (perm == permission.getOrdinal())
            return true;
      }
      
      return false;
   }

   /**
    * Create a new ACL with default entries, i.e. one for system entry with all
    * permissions and the other for system community with run time visibile
    * permission.
    * 
    * @return new ACL with default entries, never <code>null</code>.
    */
   public static IPSAcl createNewAcl()
   {
      // Create the entry with owner and read access. This is a bit weird since
      // we had to create a new dummy ACL to create a new entry.
      IPSAclEntry ownerEntry = new PSAclImpl()
         .createDefaultEntry(false, new PSPermissions[]
                                                      {
            PSPermissions.OWNER,
            PSPermissions.READ,
            PSPermissions.UPDATE,
            PSPermissions.DELETE
         });

      IPSAcl acl = new PSAclImpl("Default", ownerEntry);

      // Create the entry with owner and read access
      IPSAclEntry systemCommunity = acl.createDefaultEntry(true,
         new PSPermissions[]
         {
            PSPermissions.RUNTIME_VISIBLE
         });
      try
      {
         acl.addEntry(acl.getFirstOwner(), systemCommunity);
      }
      catch (NotOwnerException e)
      {
         // should never happen
      }

      return acl;
   }

   /**
    * Create and save the community associations for the supplied associated 
    * object references.
    * 
    * @param commRef the reference of the community for which to create the 
    *    associations, not <code>null</code>, must reference a community.
    * @param associatedRefs the references of all objects for which to create
    *    the community associations, may be <code>null</code> or empty.
    * @return the references of all associated objects, may be <code>null</code>
    *    or empty.
    * @throws PSModelException for any other error but multi operations.
    * @throws PSMultiOperationException for failed multi operations.
    */
   public static Collection<IPSReference> saveCommunityAssociations(
      IPSReference commRef, Collection<IPSReference> associatedRefs) 
      throws PSModelException, PSMultiOperationException
   {
      if (commRef == null)
         throw new IllegalArgumentException("commRef cannot be null");
      
      if (commRef.getObjectType().getPrimaryType() != PSObjectTypes.COMMUNITY)
         throw new IllegalArgumentException(
            "commRef must reference a community");
      
      if (associatedRefs == null || associatedRefs.isEmpty())
         return associatedRefs;
      
      PSCoreFactory factory = PSCoreFactory.getInstance();
      String commName = commRef.getName();
      Map<IPSCmsModel, Collection<IPSReference>> validRefsByModel = 
         new HashMap<>();
      IPSCmsModel model = null;
      for(IPSReference ref : associatedRefs)
      {
         model = factory.getModel(ref);
         PSAclImpl acl = (PSAclImpl)model.loadAcl(ref, true);
         IPSAclEntry entry = acl.findEntry(new PSTypedPrincipal(commName,
            PrincipalTypes.COMMUNITY));
         if(entry != null && PSAclUtils.entryHasPermission(entry,
            PSPermissions.RUNTIME_VISIBLE))
         {
            model.releaseAclLock(ref);
            continue;
         }
         
         boolean usingSystemComm = false;
         for(IPSAclEntry tmpEntry : acl.getEntries())
         {
            if (tmpEntry.getTypedPrincipal().isSystemCommunity())
            {
               usingSystemComm = PSAclUtils.entryHasPermission(tmpEntry, 
                  PSPermissions.RUNTIME_VISIBLE);
               break;
            }         
         }
         if (usingSystemComm)
         {
            model.releaseAclLock(ref);
            continue;
         }
         
         if(entry != null)
         {
            // Just need to add permissions
            entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
         }
         else
         {
            // Create new entry
            entry = new PSAclEntryImpl(
               new PSTypedPrincipal(commName, PrincipalTypes.COMMUNITY));
            ((PSAclEntryImpl)entry).setAclId(acl.getId());
            entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
            acl.addEntry((PSAclEntryImpl) entry);
         }
         Collection<IPSReference> validRefs = validRefsByModel.get(model);
         if (validRefs == null)
         {
            validRefs = new ArrayList<>();
            validRefsByModel.put(model, validRefs);
         }
         validRefs.add(ref);
      }
      
      for (IPSCmsModel m : validRefsByModel.keySet())
      {
         Collection<IPSReference> validRefs = validRefsByModel.get(m);
         m.saveAcl(validRefs.toArray(new IPSReference[validRefs.size()]), true);         
      }
      
      return associatedRefs;
   }
   
   /**
    * Deletes the community associations for the supplied associated object 
    * references.
    * 
    * @param commRef the reference of the community for which to delete the 
    *    associations, not <code>null</code>, must reference a community.
    * @param associatedRefs the references of all objects for which to delete
    *    the community associations, may be <code>null</code> or empty.
    * @return the references of all deleted objects for which the associatons 
    *    were deleted, may be <code>null</code> or empty.
    * @throws PSModelException for any other error but multi operations.
    * @throws PSMultiOperationException for failed multi operations.
    */
   public static Collection<IPSReference> deleteCommunityAssociations(
      IPSReference commRef, Collection<IPSReference> associatedRefs)
      throws PSModelException, PSMultiOperationException
   {
      if (commRef == null)
         throw new IllegalArgumentException("commRef cannot be null");
      
      if (commRef.getObjectType().getPrimaryType() != PSObjectTypes.COMMUNITY)
         throw new IllegalArgumentException(
            "commRef must reference a community");
      
      if (associatedRefs == null || associatedRefs.isEmpty())
         return associatedRefs;
      
      PSCoreFactory factory = PSCoreFactory.getInstance();
      String commName = commRef.getName();
      Map<IPSCmsModel, Collection<IPSReference>> validRefsByModel = 
         new HashMap<>();
      IPSCmsModel model;
      for(IPSReference ref : associatedRefs)
      {
         model = factory.getModel(ref);
         PSAclImpl acl = (PSAclImpl)model.loadAcl(ref, true);
         // Find the system community entry and check permission
         boolean usingSystemComm = false;
         for(IPSAclEntry entry : acl.getEntries())
         {
            if (entry.getTypedPrincipal().isSystemCommunity())
            {
               usingSystemComm = PSAclUtils.entryHasPermission(entry, 
                  PSPermissions.RUNTIME_VISIBLE);
               break;
            }         
         }
         if(usingSystemComm)
         {
            setSysCommunityMinus(acl, commRef);
         }
         else
         {
            IPSAclEntry entry = acl.findEntry(new PSTypedPrincipal(commName,
               PrincipalTypes.COMMUNITY));
            if(entry != null)
               acl.removeEntry((PSAclEntryImpl) entry);
         }
         
         Collection<IPSReference> validRefs = validRefsByModel.get(model);
         if (validRefs == null)
         {
            validRefs = new ArrayList<>();
            validRefsByModel.put(model, validRefs);
         }
         validRefs.add(ref);
      }
      
      for (IPSCmsModel m : validRefsByModel.keySet())
      {
         Collection<IPSReference> validRefs = validRefsByModel.get(m);
         m.saveAcl(validRefs.toArray(new IPSReference[validRefs.size()]), true);         
      }
      
      return associatedRefs;
   }
   
   /**
    * Counts all distinct visible community entries.
    * 
    * @param acl assumed not <code>null</code>.
    * @return the count
    */
   private static int getCommunityEntryCount(PSAclImpl acl)
   {
      int count = 0;
      
      List<String> processed = new ArrayList<>();
      for(IPSAclEntry entry : acl.getEntries())
      {
         if (entry.getTypedPrincipal().isCommunity() && 
            !entry.getTypedPrincipal().isSystemCommunity())
         {
            if (PSAclUtils.entryHasPermission(entry, 
               PSPermissions.RUNTIME_VISIBLE) &&
               !processed.contains(entry.getName()))
            {
               count++;
               processed.add(entry.getName());
            }
         }
      }
      
      return count;
   }
   
   /**
    * Removes the permission from the system communtiy entry and adds all 
    * communities except the one passed in.
    * 
    * @param acl assumed not <code>null</code>.
    * @param comm assumed not <code>null</code>.
    * @throws PSModelException for any error.
    */
   private static void setSysCommunityMinus(PSAclImpl acl, IPSReference comm) 
      throws PSModelException
   {
      PSAclUtils.removeAllEntries(acl, PrincipalTypes.COMMUNITY);
      
      // Remove permission from sys_community
      for(IPSAclEntry entry : acl.getEntries())
      {
         if (entry.getTypedPrincipal().isSystemCommunity())
         {
            PSPermissions removePerm = null;
            Enumeration enumPerm = entry.permissions();
            while (enumPerm.hasMoreElements())
            {
               PSPermissions perm = (PSPermissions) enumPerm.nextElement();
               if(perm.equals(PSPermissions.RUNTIME_VISIBLE))
               {
                  removePerm = perm;
                  break;
               }
            }
            entry.removePermission(removePerm);
            break;
         }         
      }
      
      // Add all communities except the specifed comm
      List<IPSReference> communities = 
         PSCoreUtils.catalog(PSObjectTypes.COMMUNITY, false);
      for(IPSReference ref : communities)
      {
         if(!ref.equals(comm))
         {
            PSAclEntryImpl entry = new PSAclEntryImpl(new PSTypedPrincipal(ref
               .getName(), PrincipalTypes.COMMUNITY));
            entry.setAclId(acl.getId());
            entry.addPermission(PSPermissions.RUNTIME_VISIBLE);
            acl.addEntry(entry);
         }
      }
   }
}
