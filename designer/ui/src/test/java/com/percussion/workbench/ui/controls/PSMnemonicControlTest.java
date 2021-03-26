/******************************************************************************
 *
 * [ PSMnemonicControlTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.workbench.ui.controls;

import com.percussion.workbench.ui.PSUiTestBase;
import org.eclipse.swt.SWT;

/**
 * @author Andriy Palamarchuk
 */
public class PSMnemonicControlTest extends PSUiTestBase
{
   /**
    * Tests {@link PSMnemonicControl#isValidMnemonic(String, char)}.
    */
   public void testIsValidMnemonic()
   {
      final PSMnemonicControl c = new PSMnemonicControl(m_shell, SWT.NONE);
      
      assertTrue(c.isValidMnemonic("abc", 'a'));
      assertTrue(c.isValidMnemonic("abc", 'A'));
      assertFalse(c.isValidMnemonic("abc", 'z'));
      assertFalse(c.isValidMnemonic("", 'z'));
      assertFalse(c.isValidMnemonic(null, 'z'));
   }
}
