/******************************************************************************
 *
 * [ PSModelTrackerTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PSModelTrackerTest extends TestCase
{
   public PSModelTrackerTest(String name)
   {
      super(name);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSModelTrackerTest.class);
   }

   /**
    * Loads and saves with all variations of the owner lifecycle flag.
    * 
    * @throws Exception
    */
   public void testLoadAndSave()
      throws Exception
   {
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.SLOT);
      PSModelTracker tracker = PSModelTracker.getInstance();
      
      //load / save as lifecycle owner
      IPSReference ref1 = model.catalog(true).iterator().next(); 
      Object data = model.load(ref1, false, false);
      assertFalse(model.isLockedInThisSession(ref1));
      Object data2 = tracker.load(ref1, true, false, true);
      assertTrue(model.isLockedInThisSession(ref1));
      assertTrue(data != data2);
      tracker.save(ref1, false, true);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.save(ref1, true, true);
      assertFalse(model.isLockedInThisSession(ref1));
      
      
      //load /save as non-lifecycle owner
      data = model.load(ref1, false, false);
      assertFalse(model.isLockedInThisSession(ref1));
      data2 = tracker.load(ref1, true);
      assertTrue(model.isLockedInThisSession(ref1));
      assertTrue(data != data2);
      tracker.save(ref1, false, false);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.save(ref1);
      assertFalse(model.isLockedInThisSession(ref1));
      
      
      //load as owner, load as non, save as non, save as owner
      data = model.load(ref1, false, false);
      assertFalse(model.isLockedInThisSession(ref1));
      data2 = tracker.load(ref1, true, false, true);
      assertTrue(model.isLockedInThisSession(ref1));
      assertTrue(data != data2);
      tracker.load(ref1, true);
      tracker.save(ref1);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.save(ref1, true, true);
      assertFalse(model.isLockedInThisSession(ref1));
      
      
      //load as owner unlocked, load as non, save as non
      data = tracker.load(ref1, false, false, true);
      assertFalse(model.isLockedInThisSession(ref1));
      data2 = tracker.load(ref1, true);
      assertTrue(data != data2);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.save(ref1);
      assertFalse(model.isLockedInThisSession(ref1));
      
      
      //load as non, load as owner, save as owner, save as non
      assertFalse(model.isLockedInThisSession(ref1));
      data = tracker.load(ref1, true);
      assertTrue(model.isLockedInThisSession(ref1));
      data2 = tracker.load(ref1, true, false, true);
      assertTrue(data == data2);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.save(ref1, true, true);
      assertFalse(model.isLockedInThisSession(ref1));
      try
      {
         tracker.save(ref1);
         fail("Save allowed after lock released.");
      }
      catch (Exception success)
      {}      
            
      //load as non, load as owner, save as non, save as owner
      assertFalse(model.isLockedInThisSession(ref1));
      data = tracker.load(ref1, true);
      assertTrue(model.isLockedInThisSession(ref1));
      data2 = tracker.load(ref1, true, false, true);
      assertTrue(data == data2);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.save(ref1);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.save(ref1, true, true);
      assertFalse(model.isLockedInThisSession(ref1));
      
   }

   /**
    * Release the lock with all variations of the owner lifecycle flag.
    * 
    * @throws Exception
    */
   public void testReleaseLock()
      throws Exception
   {
      IPSCmsModel model = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.SLOT);
      PSModelTracker tracker = PSModelTracker.getInstance();

      //load as owner - release as owner
      IPSReference ref1 = model.catalog(true).iterator().next(); 
      tracker.load(ref1, true, false, true);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.releaseLock(ref1, true);
      assertFalse(model.isLockedInThisSession(ref1));
      
      
      //load as owner - release as non
      tracker.load(ref1, true, false, true);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.releaseLock(ref1);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.releaseLock(ref1, true);
      
      //load as non - release as non
      tracker.load(ref1, true);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.releaseLock(ref1);
      assertFalse(model.isLockedInThisSession(ref1));
      
      
      //load as owner - load as non - release as non - release as owner
      tracker.load(ref1, true, false, true);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.load(ref1, true);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.releaseLock(ref1);
      assertTrue(model.isLockedInThisSession(ref1));
      tracker.releaseLock(ref1, true);
      assertFalse(model.isLockedInThisSession(ref1));
   }
}
