/******************************************************************************
 *
 * [ PSCompletionProviderTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.util;

import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Invocation;
import org.jmock.core.stub.CustomStub;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.percussion.workbench.ui.util.PSCompletionProvider.FIELDS_VEDIT_KEY;
import static com.percussion.workbench.ui.util.PSCompletionProvider.MACROS_VEDIT_KEY;
import static com.percussion.workbench.ui.util.PSCompletionProvider.METHODS_VEDIT_KEY;
import static com.percussion.workbench.ui.util.PSCompletionProvider.VARIABLES_VEDIT_KEY;
import static com.percussion.workbench.ui.util.PSCompletionProvider.VEDIT_KEY;

/**
 * @author Andriy Palamarchuk
 */
public class PSCompletionProviderTest extends MockObjectTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      m_resourceProperties.clear();
      PSCoreFactory.getInstance().setClientSessionId(CLIENT_SESSION_ID);
   }

   public void testAttachVelocityEditorCompletionData()
         throws CoreException, PSModelException
   {
      final PSCompletionProvider provider = new PSCompletionProvider();
      final Mock mockResource = new Mock(IResource.class);
      mockResource.expects(atLeastOnce()).method("setSessionProperty")
            .withAnyArguments()
            .will(new SetSessionPropertyStub());
      
      provider.attachVelocityEditorCompletionData(
            (IResource) mockResource.proxy());

      // field completions
      {
         final List<Object[]> completions =
               getResourceProperty(FIELDS_VEDIT_KEY);
         assertNotNull(completions);
         assertFalse(completions.isEmpty());

         for (Object[] completion : completions)
         {
            validateFieldCompletion(completion);
         }
      }
      
      // variable completions
      {
         final List<Object[]> completions =
               getResourceProperty(VARIABLES_VEDIT_KEY);
         assertNotNull(completions);
         assertFalse(completions.isEmpty());
         assertEquals(completions, provider.getVarCompletions());

         for (Object[] completion : completions)
         {
            validateVariableCompletion(completion);
         }
      }

      // method completions
      {
         final List<Object[]> completions =
               getResourceProperty(METHODS_VEDIT_KEY);
         assertNotNull(completions);
         if (!PSCoreFactory.getInstance().isLocalMode())
         {
            assertFalse(completions.isEmpty());
         }
         assertEquals(completions, provider.getMethodCompletions());

         for (Object[] completion : completions)
         {
            validateMethodCompletion(completion);
         }
      }
      
      // macro completions
      {
         final List<Object[]> completions =
               getResourceProperty(MACROS_VEDIT_KEY);
         assertNotNull(completions);
         if (!PSCoreFactory.getInstance().isLocalMode())
         {
            assertFalse(completions.isEmpty());
         }
      }      

      mockResource.verify();
   }

   /**
    * Asserts that provided field completion conforms to the description
    * {@link PSCompletionProvider#attachVelocityEditorCompletionData(
    * IResource)}.
    * @param completion the completion to validate.
    * Assumed not <code>null</code>.
    */
   private void validateFieldCompletion(final Object[] completion)
   {
      assertTrue("With or without description",
            completion.length == 2 || completion.length == 3);
      assertTrue(StringUtils.isNotBlank((String) completion[0]));
   }

   /**
    * Asserts that provided variable completion conforms to the description
    * {@link PSCompletionProvider#attachVelocityEditorCompletionData(
    * IResource)}.
    * @param completion the completion to validate.
    * Assumed not <code>null</code>.
    */
   private void validateVariableCompletion(final Object[] completion)
   {
      assertTrue("With or without description",
            completion.length == 3 || completion.length == 2);
      // name
      assertTrue(StringUtils.isNotBlank((String) completion[0]));

      // type (can be empty)
      assertNotNull(completion[1]);
   }

   /**
    * Asserts that provided variable completion conforms to the description
    * {@link PSCompletionProvider#attachVelocityEditorCompletionData(
    * IResource)}.
    * @param completion the completion to validate.
    * Assumed not <code>null</code>.
    */
   private void validateMethodCompletion(final Object[] completion)
   {
      assertTrue("Minimal size - for method without parameters",
            completion.length >= 3);

      // name
      assertTrue(StringUtils.isNotBlank((String) completion[0]));

      // type
      assertTrue(StringUtils.isNotBlank((String) completion[1]));

      // description
      assertNotNull(completion[2]);
      
      // params, starting with 4th element
      for (int i = 3; i < completion.length; i++)
      {
         final Object[] param = (Object[]) completion[i];
         assertEquals(3, param.length);

         // name
         assertTrue(StringUtils.isNotBlank((String) param[0]));
         // type
         assertNotNull(param[1]);
         // description
         assertNotNull(param[2]);
      }
   }

   /**
    * Gets the specified resource property from {@link #m_resourceProperties}.
    * @param subkey key of the specific property in the set of velocity editor
    * properties. Assumed not <code>null</code>.
    * @return the object to use as a key. Not <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private List<Object[]> getResourceProperty(String subkey)
   {
      return (List<Object[]>) m_resourceProperties.get(
            new QualifiedName(VEDIT_KEY, subkey));
   }

   /**
    * Adds the provided property/key pair to {@link #m_resourceProperties}.
    * Needs to be called on mocking
    * {@link IResource#setSessionProperty(QualifiedName, Object)}.   
    */
   private class SetSessionPropertyStub extends CustomStub
   {

      public SetSessionPropertyStub()
      {
         super("IResource.setSessionProperty mock");
      }

      public Object invoke(final Invocation invocation) throws Throwable
      {
         assertEquals("setSessionProperty", invocation.invokedMethod.getName());

         final QualifiedName key =
               (QualifiedName) invocation.parameterValues.get(0);
         final Object value = invocation.parameterValues.get(1);
         m_resourceProperties.put(key, value);
         return null;
      }
   }
   
   /**
    * Simulated resource properties. Cleared before each test.
    */
   private final Map<QualifiedName, Object> m_resourceProperties =
         new HashMap<QualifiedName, Object>();

   /**
    * Client sessionid used to lock the objects. We do not want to use multiples
    * of this in the tests' life time.
    */
   static private String CLIENT_SESSION_ID = new Date().getTime()+"";
}
