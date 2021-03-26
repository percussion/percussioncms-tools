/******************************************************************************
 *
 * [ PSXmlApplicationConverterTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSApplicationType;
import com.percussion.error.PSException;
import junit.framework.TestCase;

/**
 * @author Andriy Palamarchuk
 */
public class PSXmlApplicationConverterTest extends TestCase
{
   public void testConvert() throws PSException
   {
      final PSXmlApplicationConverter converter =
         new PSXmlApplicationConverter();
      assertEquals(null, converter.convert(null));
      final PSApplication app = new PSApplication("Application1") {};
      app.setId(32);
      app.setApplicationType(PSApplicationType.CONTENT_EDITOR);
      app.setDescription("Description");
      app.setRevision("agent", "version");
      assertTrue(converter.convert(app) instanceof OSApplication);

      final OSApplication app2 = (OSApplication) converter.convert(app);
      assertEquals(app, app2);
      // check that actually has the data
      assertEquals(app.getId(), app2.getId());
      assertEquals(app.getApplicationType(), app2.getApplicationType());
      assertEquals(app.getDescription(), app2.getDescription());
      assertEquals(app.getRevisionHistory(), app2.getRevisionHistory());
      assertSame(app2, converter.convert(app2));
   }
}
