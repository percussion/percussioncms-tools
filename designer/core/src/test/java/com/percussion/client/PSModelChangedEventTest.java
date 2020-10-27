/******************************************************************************
 *
 * [ PSModelChangedEventTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client;

import com.percussion.client.PSModelChangedEvent.ModelEvents;
import com.percussion.client.impl.PSReference;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Test cases for the {@link PSModelChangedEvent} class.
 *
 * @author paulhoward
 */
public class PSModelChangedEventTest extends TestCase
{
   public PSModelChangedEventTest(String name)
   {
      super(name);
   }

   public static TestSuite suite()
   {
      return new TestSuite(PSModelChangedEventTest.class);
   }

   /**
    * Verify that 2 equal objects report so and that un-equal objects report
    * so.
    */
   public void testEquals()
      throws Exception
   {
      PSObjectType t = new PSObjectType(PSObjectTypes.KEYWORD);
      IPSReference ref1 = new PSReference("test", "", "", t, null);
      IPSReference ref2 = new PSReference("test2", "", "", t, null);
      PSModelChangedEvent evt = new PSModelChangedEvent(ref1,
            ModelEvents.CREATED, null);
      PSModelChangedEvent evt2 = new PSModelChangedEvent(ref1,
            ModelEvents.CREATED, null);
      PSModelChangedEvent evt3 = new PSModelChangedEvent(ref2,
            ModelEvents.CREATED, null);
      assertTrue(evt.equals(evt2));
      assertFalse(evt.equals(evt3));
      
      Collection<IPSReference> sources = new ArrayList<IPSReference>();
      sources.add(ref1);
      sources.add(ref2);
      evt = new PSModelChangedEvent(sources, ModelEvents.CREATED, null);
      evt2 = new PSModelChangedEvent(ref1, ModelEvents.CREATED, null);
      assertFalse(evt.equals(evt2));
      
      IPSReference[] sources2 = new IPSReference[2];
      sources2[0] = ref2;
      sources2[1] = ref1;
      evt = new PSModelChangedEvent(sources, ModelEvents.CREATED, null);
      evt2 = new PSModelChangedEvent(sources2, ModelEvents.CREATED, null);
      assertTrue(evt.equals(evt2));

      evt2 = new PSModelChangedEvent(ref1, ModelEvents.DELETED, null);
      assertFalse(evt.equals(evt2));      
   }   

   /**
    * Verify that 2 equal objects return same hash.
    */
   public void testHash()
      throws Exception
   {
      PSObjectType t = new PSObjectType(PSObjectTypes.KEYWORD);
      IPSReference ref = new PSReference("test", "", "", t, null);
      PSModelChangedEvent evt = new PSModelChangedEvent(ref,
            ModelEvents.CREATED, null);
      PSModelChangedEvent evt2 = new PSModelChangedEvent(ref,
            ModelEvents.CREATED, null);
      assertTrue(evt.hashCode() == evt2.hashCode());
   }
}
