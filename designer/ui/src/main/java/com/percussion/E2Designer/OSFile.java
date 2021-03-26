/******************************************************************************
 *
 * [ OSFile.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import java.io.Serializable;

/**
 * This class is used only to store source file information when dragging
 * a file from the browser to the application.  It has a very limited
 * lifespan and does not live beyond the drop into UIFigureFrame.  Because of
 * the transient nature of this class and the fact that it is
 * not used in the same way as other "OS" objects, it does not implement the
 * same interfaces as these other classes.  It is contained by a
 * UIConnectableFigure with an id of <code>AppFigureFactory.XSL_FILE</code>
 * 
 */
public class OSFile implements IGuiLink, Serializable
{
   /**
    * Returns the file path set by {@link #setFilePath(String) setFilePath}.
    *
    * @return The file path. See {@link #setFilePath(String) setFilePath} for
    * a description of allowed values. If <code>setFilePath</code> has never
    * been called, <code>null</code> is returned.
    *
    */
   public String getFilePath()
   {
      return m_filePath;
   }

   /**
    * Sets the source file path.
    *
    * @param filePath the the source file path for the file.  May be <CODE>null</CODE>.
    *
    */
   public void setFilePath(String filePath)
   {
      m_filePath = filePath;
   }

   // IGuiLink interface implementation
   public void setFigure(UIFigure fig)
   {
      m_owner = fig;
   }

   public void release()
   {
      m_owner = null;
   }

   public UIFigure getFigure()
   {
      return m_owner;
   }

   private UIFigure m_owner;

   /**
    * The file path name for this styleSheet. See {@link #getFilePath()
    * getFilePath} for the correct default value.
    */
   private String m_filePath;
}
