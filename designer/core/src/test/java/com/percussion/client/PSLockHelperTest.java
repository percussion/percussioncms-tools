/*******************************************************************************
 *
 * [ PSLockHelperTest.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.client;

import com.percussion.client.impl.PSReference;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.data.PSDesignGuid;
import junit.framework.TestCase;

/**
 * Tests lock helper.
 */
public class PSLockHelperTest extends TestCase
{
   public void testall() throws PSModelException
   {
      PSReference ref = new PSReference("foo", "fooLabel", "fooDesc",
         PSObjectTypeFactory.getType(PSObjectTypes.SLOT), new PSDesignGuid(
            PSTypeEnum.SLOT, 100));

      // In real life, you would use get the instance this way
      // PSLockHelper lockHelper = PSCoreFactory.getInstance().getLockHelper();
      PSLockHelper lockHelper = new PSLockHelper("xxx-xxx", "admin1");

      // not locked to anyone
      assertFalse(lockHelper.isLocked(ref));
      assertFalse(lockHelper.isLockedToMe(ref));
      assertFalse(lockHelper.isLockedToMeElsewhere(ref));
      assertFalse(lockHelper.isLockedToOther(ref));

      ref.setLockSessionId("xxx-xxx");
      ref.setLockUserName("admin1");
      // locked to current user in current session
      assertTrue(lockHelper.isLocked(ref));
      assertTrue(lockHelper.isLockedToMe(ref));
      assertFalse(lockHelper.isLockedToMeElsewhere(ref));
      assertFalse(lockHelper.isLockedToOther(ref));

      ref.setLockSessionId("xxx-yyy");
      ref.setLockUserName("admin1");
      // locked to current user in a different session
      assertTrue(lockHelper.isLocked(ref));
      assertFalse(lockHelper.isLockedToMe(ref));
      assertTrue(lockHelper.isLockedToMeElsewhere(ref));
      assertFalse(lockHelper.isLockedToOther(ref));

      ref.setLockSessionId("xxx-xxx");
      ref.setLockUserName("admin2");
      // locked to some one else in the current session
      assertTrue(lockHelper.isLocked(ref));
      assertFalse(lockHelper.isLockedToMe(ref));
      assertFalse(lockHelper.isLockedToMeElsewhere(ref));
      assertTrue(lockHelper.isLockedToOther(ref));

      ref.setLockSessionId("xxx-yyy");
      ref.setLockUserName("admin2");
      // locked to some one else in other session
      assertTrue(lockHelper.isLocked(ref));
      assertFalse(lockHelper.isLockedToMe(ref));
      assertFalse(lockHelper.isLockedToMeElsewhere(ref));
      assertTrue(lockHelper.isLockedToOther(ref));
   }
}
