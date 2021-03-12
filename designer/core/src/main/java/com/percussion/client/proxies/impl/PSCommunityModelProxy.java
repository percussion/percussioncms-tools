/******************************************************************************
 *
 * [ PSCommunityModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.PSSecurityUtils;
import com.percussion.client.objectstore.PSUiCommunity;
import com.percussion.client.proxies.PSObjectFactory;
import com.percussion.client.proxies.PSProxyUtils;
import com.percussion.client.proxies.PSUiCommunityConverter;
import com.percussion.services.security.data.PSCommunity;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.transformation.impl.PSTransformerFactory;
import org.apache.axis.client.Stub;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.commons.logging.LogFactory;

import javax.xml.rpc.ServiceException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides CRUD and cataloging services for the object type
 * {@link PSObjectTypes#COMMUNITY}. Uses base class implementation whenever
 * possible and does type specific work here.
 * 
 * @see com.percussion.client.proxies.impl.PSCmsModelProxy
 * 
 * @version 6.0
 * @created 03-Sep-2005 4:39:27 PM
 */
public class PSCommunityModelProxy extends PSCmsModelProxy
{
   /**
    * Ctor. Invokes base class version with the object type
    * {@link PSObjectTypes#COMMUNITY} and for main type and <code>null</code>
    * sub type since this object type does not have any sub types.
    */
   public PSCommunityModelProxy()
   {
      super(PSObjectTypes.COMMUNITY);
      
      PSTransformerFactory.getInstance().register(PSUiCommunityConverter.class,
         PSUiCommunity.class, 
         com.percussion.webservices.security.data.PSCommunity.class);
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(
    * java.lang.Object[], java.lang.String[], java.util.List)
    */
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names, 
      List<Object> results) throws PSMultiOperationException, PSModelException
   {
      Exception ex = null;

      // clone all source communities
      Object[] clones = null;
      try
      {
         clones = super.create(sourceObjects, names, results);
      }
      catch (PSMultiOperationException e)
      {
         // save exception but keep going for successfully created clones
         ex = e;
         clones = e.getResults();
      }
   
      // weed out the references for successfully cloned communities
      List<IPSReference> originalRefs = new ArrayList<IPSReference>();
      List<IPSReference> cloneRefs = new ArrayList<IPSReference>();
      List<PSUiCommunity> cloneCommunities = new ArrayList<PSUiCommunity>();
      for (int i=0; i<clones.length; i++)
      {
         if (!(clones[i] instanceof Throwable))
         {
            originalRefs.add(PSObjectFactory.objectToReference(
               sourceObjects[i], m_objectPrimaryType, true));
            cloneRefs.add((IPSReference) clones[i]);
            cloneCommunities.add((PSUiCommunity) results.get(i));
         }
      }
      
      /*
       * Load the originals community visibilities and save them to the
       * cloned community.
       */
      for (int i=0; i<originalRefs.size(); i++)
      {
         try
         {
            // get the visibility for the original community
            Set<IPSReference> visibleRefs = new HashSet<IPSReference>();
            visibleRefs.addAll(PSSecurityUtils.getVisibilityByCommunity(
               originalRefs.get(i), null));
            
            // save the visibility for the clone
            cloneCommunities.get(i).setVisibleRefs(visibleRefs);
         }
         catch (PSModelException e)
         {
            // should never happen
            PSProxyUtils.logError(ms_log, e);
            throw new RuntimeException(e);
         }
      }

      if (ex != null)
         processAndThrowException(sourceObjects.length, ex);

      return cloneRefs.toArray(new IPSReference[cloneRefs.size()]);
   }

   /* (non-Javadoc)
    * @see PSCmsModelProxy#save(IPSReference[], Object[], boolean)
    */
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock) 
      throws PSMultiOperationException, PSModelException
   {
      try
      {
         super.save(refs, data, releaseLock);
         saveCommunityAssociations(refs, data);
      }
      catch (PSMultiOperationException e)
      {
         List<IPSReference> successRefList = new ArrayList<IPSReference>();
         List<Object> successDataList = new ArrayList<Object>();
         
         Object[] results = e.getResults();
         for (int i=0; i<refs.length; i++)
         {
            if (!(results[i] instanceof Throwable))
            {
               successRefList.add(refs[i]);
               successDataList.add(data[i]);
            }
         }
         
         try
         {
            saveCommunityAssociations(
               successRefList.toArray(new IPSReference[successRefList.size()]), 
               successDataList.toArray(new Object[successDataList.size()]));
         }
         catch (Exception e2)
         {
            // ignore, want to throw the original exception
         }
         
         throw e;
      }
   }
   
   /**
    * Save the community associations from all supplied source communities to
    * the specified target communities.
    * 
    * @param tgtCommunities the community references for which to store the 
    *    community associations, assumed not <code>null</code>, may be empty.
    * @param srcCommunities the source communities from which to copy the 
    *    community associations, assumed not <code>null</code> or empty. It is 
    *    expected that sources have the same length as targets.
    * @throws PSMultiOperationException for errors saving the community 
    *    associations.
    * @throws PSModelException for errors saving the community 
    *    associations.
    */
   private void saveCommunityAssociations(IPSReference[] tgtCommunities, 
      Object[] srcCommunities) 
      throws PSMultiOperationException, PSModelException
   {
      for (int i=0; i<tgtCommunities.length; i++)
      {
         PSUiCommunity source = (PSUiCommunity) srcCommunities[i];
         PSSecurityUtils.saveCommunityAssociations(tgtCommunities[i], 
            source.getVisibleRefs());
      }
   }

   /**
    * Get the {@link IPSGuid}s for the roles that is member of the community
    * with supplied community reference. If the supplied communiyt reference is
    * <code>null</code> all communities are catalogged from the server and a
    * set of all community roles is returned.
    * 
    * @param commRef Reference o fthe community whose member roles are asked
    * for. If <code>null</code> all the communities are cataloged and and set
    * of all roles from all communities is returned.
    * @return set of community roles as explained above. Never <code>null</code>
    * may be empty.
    * @throws PSModelException
    */
   public Collection<IPSGuid> getCommunityRoleIds(IPSReference commRef)
      throws PSModelException
   {
      Set<IPSGuid> result = new HashSet<IPSGuid>();
      try
      {
         Collection<IPSReference> commRefs = null;
         if (commRef == null)
         {
            commRefs = catalog();
         }
         else
         {
            commRefs = new ArrayList<IPSReference>(1);
            commRefs.add(commRef);
         }
         PSCommunity[] comms = (PSCommunity[]) load(commRefs
            .toArray(new IPSReference[0]), false, false);
         for (PSCommunity comm : comms)
         {
            result.addAll(comm.getRoleAssociations());
         }
      }
      catch (PSMultiOperationException e)
      {
         throw new PSModelException(e);
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#getSoapBinding(
    *    com.percussion.client.proxies.IPSCmsModelProxy.METHOD)
    */
   @Override
   protected Stub getSoapBinding(@SuppressWarnings("unused") METHOD method) 
      throws MalformedURLException,
      ServiceException
   {
      return PSProxyUtils.getSecurityDesignStub();
   }

   /**
    * Logger object to log any errors.
    */
   private static Logger ms_log = LogManager.getLogger(PSCommunityModelProxy.class);
}
