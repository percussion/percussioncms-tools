/******************************************************************************
 *
 * [ PSContentTypeAssociateTemplateNodeHandlerTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.handlers;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.models.IPSCmsModel;
import com.percussion.client.models.IPSContentTypeModel;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * This class tests the {@link PSContentTypeAssociateTemplateNodeHandler}
 * class. 
 * <p>Notes on running this test:<p>
 * 1. Set the useLocalData and useTestCreds props in conn_rxserver.properties file.
 * 2. Create 2 directories under <rxdev>/designer/ui called: build/unitTestConfig and ear/config/spring
 * 3. Copy local-beans.xml from <rxdev>/System/build/unitTestConfig to the new directory by the same name.
 * 4. Copy server-beans.xml from <rxdev>/System/ear/config/spring to the new directory by the same name.
 * 5. Run the test.
 * 6. Delete the local repository files before running it again (<rxdev>/designer/ui/*_repository.xml)
 * 7. I also had to comment out lines 238-254 in 
 * ...impl.test.PSContentTypeModelProxy (the code that added associations.) 
 * The associations should only be added by the association repository.
 * 
 * @todo These steps need to be done by the ant script w/ other test setup steps
 * and it needs to be fixed so the repository will reload.
 * 
 * @author paulhoward
 */
public class PSContentTypeAssociateTemplateNodeHandlerTest extends TestCase
{
   /**
    * Attempts to add 1 template that is not currently associated with the
    * content type.
    * 
    * @throws Exception
    */
   public void testAddOneTemplate()
      throws Exception
   {
      PSLinkNodeHandler handler = createHandler();
      IPSContentTypeModel ctypeModel = (IPSContentTypeModel) PSCoreFactory
            .getInstance().getModel(PSObjectTypes.CONTENT_TYPE);
      Collection<IPSReference> ctypeRefs = ctypeModel.catalog();
      assertFalse("Test data misconfigured for this test.", ctypeRefs
            .isEmpty());
      IPSReference ctypeRef = ctypeRefs.iterator().next();

      Collection<IPSReference> templateRefs = new ArrayList<IPSReference>();
      IPSCmsModel templateModel = PSCoreFactory.getInstance().getModel(
            PSObjectTypes.TEMPLATE);
      Collection<IPSReference> catalogedTemplateRefs = templateModel.catalog(
            false, new PSObjectType[] { PSObjectTypeFactory.getType(
                  PSObjectTypes.TEMPLATE,
                  PSObjectTypes.TemplateSubTypes.SHARED) });
      assertFalse("Test data misconfigured for this test.",
            catalogedTemplateRefs.isEmpty());

      // find a template that is not associated
      Map<IPSReference, Collection<IPSReference>> associations = ctypeModel
            .getTemplateAssociations(Collections.singletonList(ctypeRef),
                  false, true);
      IPSReference toAddTemplateRef = null;
      Collection<IPSReference> assocTemplateRefs = associations.get(ctypeRef);
      if (assocTemplateRefs == null)
      {
         assocTemplateRefs = new ArrayList<IPSReference>();
         associations.put(ctypeRef, assocTemplateRefs);
      }

      for (IPSReference ref : catalogedTemplateRefs)
      {
         if (!assocTemplateRefs.contains(ref))
         {
            toAddTemplateRef = ref;
            templateRefs.add(ref);
            break;
         }
      }
      
      assertEquals(1, templateRefs.size());
      handler.doSaveAssociations(ctypeRef, templateRefs);
      
      //validate
      associations = ctypeModel.getTemplateAssociations(Collections
            .singletonList(ctypeRef), false, true);
      assocTemplateRefs = associations.get(ctypeRef);
      assertTrue(assocTemplateRefs.contains(toAddTemplateRef));
      
      //restore
      assocTemplateRefs.remove(toAddTemplateRef);
      ctypeModel.setTemplateAssociations(associations);
   }
   
   /**
    * Creates an instance of the type being tested by this class.
    * 
    * @return Never <code>null</code>.
    */
   private PSLinkNodeHandler createHandler()
   {
      PSObjectType[] allowedTypes = 
      {
         PSObjectTypeFactory.getType(
               PSObjectTypes.TEMPLATE, PSObjectTypes.TemplateSubTypes.SHARED)
      };
      Properties props = new Properties();
      return new PSContentTypeAssociateTemplateNodeHandler(props, "",
            allowedTypes);
   }
}
