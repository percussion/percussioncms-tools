/*[ ApplicationFileFilter.java ]***********************************************
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
public class ApplicationFileFilter extends FileFilter
{
   //////////////////////////////////////////////////////////////////////////////
   // implementation for FileFilter
  public boolean accept(File file)
  {
     if (file.isDirectory())
         return true;

    String name = file.getName().toLowerCase();
    for (int i=0; i<m_extensions.length; i++)
    {
       if (name.endsWith(m_extensions[i]))
         return true;
    }

    return false;
  }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for FileFilter
  public String getDescription()
  {
     return m_description;
  }

   //////////////////////////////////////////////////////////////////////////////
   private static final String[] m_extensions = new String[] { ".xml" };
  private static final String m_description = "Application files (*.xml)";
}

