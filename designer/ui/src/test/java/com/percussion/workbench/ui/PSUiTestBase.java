/******************************************************************************
 *
 * [ PSUiTestBase.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui;

import com.percussion.client.IPSReference;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSModelException;
import com.percussion.client.PSObjectTypeFactory;
import com.percussion.client.PSObjectTypes;
import com.percussion.client.impl.PSReference;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jmock.cglib.MockObjectTestCase;

/**
 * Common functionality for UI testing.
 * Creates shell, script for UI testing.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSUiTestBase extends MockObjectTestCase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      PSCoreFactory.getInstance().setClientSessionId(CLIENT_SESSION_ID);
      assert !ms_displayInitialized || Display.getCurrent() != null 
         : "Display must be initialized only once"; 
      m_display = Display.getCurrent() == null ? new Display() : Display.getCurrent();
      ms_displayInitialized = true;
      m_shell = new Shell(m_display);
   }

   @Override
   protected void tearDown() throws Exception
   {
      m_shell.dispose();
   }

   /**
    * Creates a reference.
    */
   protected IPSReference createRef(final String name)
   {
      final PSReference ref = new PSReference();
      ref.setName(name);
      ref.setLabelKey("LABEL: " + name);
      try
      {
         ref.setObjectType(PSObjectTypeFactory.getType(PSObjectTypes.TEMPLATE,
               PSObjectTypes.TemplateSubTypes.SHARED));
      }
      catch (PSModelException e)
      {
         throw new AssertionError(e);
      }
      return ref;
   }
   
   /**
    * Insures the string is not blank.
    */
   public void assertNotBlank(final String s)
   {
      assertTrue(StringUtils.isNotBlank(s));
   }
   
   /**
    * Dummy wizard container used for testing.
    */
   protected class TestWizardContainer implements IWizardContainer
   {
      /**
       * Creates the wizard container returning the specified page as current page.
       */
      public TestWizardContainer(IWizardPage currentPage)
      {
         m_currentPage = currentPage;
      }
      
      /**
       * Returns the page specified in constructor as current page.
       */
      public IWizardPage getCurrentPage()
      {
         return m_currentPage;
      }

      /**
       * Returns {@link #m_shell}.
       */
      public Shell getShell()
      {
         return m_shell;
      }

      // these method do nothing
      @SuppressWarnings("unused")
      public void showPage(IWizardPage page) {}
      public void updateButtons() {}
      public void updateMessage() {}
      public void updateTitleBar() {}
      public void updateWindowTitle() {}
      @SuppressWarnings("unused")
      public void run(boolean fork, boolean cancelable,
            IRunnableWithProgress runnable)
      {}
      
      /**
       * Page to return as current page.
       */
      private final IWizardPage m_currentPage;
   }
   
   /**
    * For unit tests session identification.
    */
   static public final String CLIENT_SESSION_ID = "WORKBENCH_UNIT_TEST";

   /**
    * Indicates that display was already once initialized.
    */
   private static boolean ms_displayInitialized;
   
   /**
    * Display used for UI initialization.
    */
   protected Display m_display;
   
   /**
    * Shell used for UI initialization.
    */
   protected Shell m_shell;
}
