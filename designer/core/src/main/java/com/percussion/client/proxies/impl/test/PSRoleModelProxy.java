/******************************************************************************
 *
 * [ PSRoleModelProxy.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.proxies.impl.test;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreUtils;
import com.percussion.client.PSModelException;
import com.percussion.client.PSMultiOperationException;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.security.IPSAcl;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PSRoleModelProxy extends PSTestModelProxy
{

   public PSRoleModelProxy()
   {
      super(PSObjectTypes.ROLE);
   }

   @Override
   protected IPSRepositoryMap getRepositoryMap()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   protected File getRepositoryFile()
   {
      // TODO Auto-generated method stub
      return null;
   }

   @SuppressWarnings("unused")
   @Override
   public void rename(IPSReference ref, String name, Object data)
      throws PSModelException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#renameLocal(
    * com.percussion.client.IPSReference, java.lang.String, java.lang.Object)
    */
   @SuppressWarnings("unused")
   @Override
   public void renameLocal(IPSReference ref, String name, Object data)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#catalog(com.percussion.client.IPSReference)
    */
   @Override
   public Collection<IPSReference> catalog()
   {
      Collection<IPSReference> roles = new ArrayList<IPSReference>();
      roles.add(createNewReference("Admin", 10));
      roles.add(createNewReference("Artist", 20));
      roles.add(createNewReference("Author", 30));
      roles.add(createNewReference("Default", 40));
      roles.add(createNewReference("Designer", 50));
      roles.add(createNewReference("Editor", 60));
      roles.add(createNewReference("CI_Members", 70));
      roles.add(createNewReference("CI_Admin_Members", 80));
      roles.add(createNewReference("EI_Members", 90));
      roles.add(createNewReference("EI_Admin_Members", 100));
      roles.add(createNewReference("QA", 110));
      roles.add(createNewReference("ReportAdmin", 120));
      roles.add(createNewReference("RxPublisher", 130));
      roles.add(createNewReference("WebAdmin", 140));
      return roles;
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#delete(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   @Override
   public void delete(IPSReference[] references)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#load(com.percussion.client.IPSReference[],
    * boolean, boolean)
    */
   @SuppressWarnings("unused")
   @Override
   public Object[] load(IPSReference[] references, boolean lock,
      boolean overrideLock) throws PSMultiOperationException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#releaseLock(com.percussion.client.IPSReference[])
    */
   @SuppressWarnings("unused")
   @Override
   public void releaseLock(IPSReference[] references)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.test.impl.PSTestModelProxy#save(com.percussion.client.IPSReference[],
    * java.lang.Object[], boolean)
    */
   @SuppressWarnings("unused")
   @Override
   public void save(IPSReference[] refs, Object[] data, boolean releaseLock)
      throws PSMultiOperationException
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#clone(java.lang.Object)
    */
   @SuppressWarnings("unused")
   @Override
   public Object clone(Object source)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(java.lang.Object[],
    * java.util.List)
    */
   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(Object[] sourceObjects, String[] names,
         List results)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#create(com.percussion.client.PSObjectType,
    * java.util.Collection, java.util.List)
    */
   @SuppressWarnings("unused")
   @Override
   public IPSReference[] create(PSObjectType objType, Collection<String> names,
      List results)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#flush(com.percussion.client.IPSReference)
    */
   @SuppressWarnings("unused")
   @Override
   public void flush(IPSReference ref)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * @see com.percussion.client.proxies.impl.PSCmsModelProxy#loadAcls(com.percussion.client.IPSReference[],
    * boolean)
    */
   @SuppressWarnings("unused")
   @Override
   public IPSAcl[] loadAcl(IPSReference[] ref, boolean lock)
   {
      throw new UnsupportedOperationException();
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.percussion.client.proxies.IPSCmsModelProxy#saveAcl(com.percussion.client.IPSReference[],
    * com.percussion.services.security.IPSAcl[], boolean)
    */
   @SuppressWarnings("unused")
   @Override
   public void saveAcl(IPSReference[] ref, IPSAcl[] acl, boolean releaseLock)
   {
      throw new UnsupportedOperationException();
   }

   private IPSReference createNewReference(String name, long id)
   {
      return PSCoreUtils.createReference(name, name, StringUtils.EMPTY,
         PSObjectTypeFactory.getType((Enum) m_objectPrimaryType), new PSGuid(
            PSTypeEnum.ROLE, id));
   }

}
