/*[ AllFileFilter.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.filechooser.FileFilter;
import java.io.File;

////////////////////////////////////////////////////////////////////////////////
public class AllFileFilter extends FileFilter
{
   //////////////////////////////////////////////////////////////////////////////
   // implementation for FileFilter
   public boolean accept(File file)
   {
      return true;
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for FileFilter
   public String getDescription()
   {
      return m_description;
   }

   //////////////////////////////////////////////////////////////////////////////
   private static final String m_description = "All files (*.*)";
}

