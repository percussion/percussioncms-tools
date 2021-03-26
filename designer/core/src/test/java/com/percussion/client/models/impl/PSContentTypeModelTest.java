/******************************************************************************
 *
 * [ PSTemplateModelTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models.impl;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.PSLockException;
import com.percussion.utils.testing.IntegrationTest;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSContentTypeModelTest extends TestCase

{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSCoreFactory.getInstance().setClientSessionId(CLIENT_SESSION_ID);
   }

   /**
    * Tests {@link IPSCmsModel#flush(IPSReference) IPSCmsModel.flush(ref)}
    * for a not-null reference..
    */
   public void testFlush_singleRef() throws PSModelException, PSLockException
   {
      final PSContentTypeModel model = createModel();
      assertTrue(model.m_cache.isEmpty());
      assertFalse(model.m_fullCache);

      model.getTemplateAssociations(null, true, false);
      assertFalse(model.m_cache.isEmpty());

      final IPSReference ref = model.m_cache.keySet().iterator().next();
      assertTrue(model.m_cache.containsKey(ref));
      assertTrue(model.m_fullCache);
      model.flush(ref);
      assertFalse(model.m_cache.containsKey(ref));
      assertFalse(model.m_cache.isEmpty());
      assertFalse(model.m_fullCache);
   }

   /**
    * Tests {@link IPSCmsModel#flush(IPSReference) IPSCmsModel.flush(null)}.
    */
   public void testFlush_All() throws PSModelException, PSLockException
   {
      final PSContentTypeModel model = createModel();
      assertTrue(model.m_cache.isEmpty());

      model.getTemplateAssociations(null, true, false);
      assertFalse(model.m_cache.isEmpty());
      assertTrue(model.m_fullCache);
      model.flush(null);
      assertTrue(model.m_cache.isEmpty());
      assertFalse(model.m_fullCache);
   }

   /**
    * Creates {@link PSContentTypeModel}.
    */
   private PSContentTypeModel createModel()
   {
      return new PSContentTypeModel("name", "descr", PSObjectTypes.CONTENT_TYPE);
   }

   /**
    * For unit tests session identification.
    */
   private static final String CLIENT_SESSION_ID = "WORKBENCH_UNIT_TEST";
}
