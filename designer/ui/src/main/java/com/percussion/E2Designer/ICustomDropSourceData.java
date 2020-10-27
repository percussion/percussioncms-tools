/*[ ICustomDropSourceData.java ]***********************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

/**
 * Defines an interface that is used to prepare the drag source from the local
 * file system after a drop action. Since all files used from the local file
 * system, this also forces the user to implement a save function, which copies
 * the local files to the application.
 * The source object could also be prepared within the drag. But first
 * of all this are time consuming preparations and second we would have to
 * prepare way to much because we do not know the action the user is going to
 * choose.
 */
public interface ICustomDropSourceData
{
   public enum DropAction
   {
      UPDATE,
      QUERY,
      STATIC,
      XSL,
      IMAGE
   }
   /**
    * This method is call after a drop, telling us the action, for what the
    * implementing object has to prepare itself.
    *
    * @param action the action to prepare for
    * @param filePath the complete path of the dropped file
    * @param rootName the root name to be used for creating stylesheets.
    *   If <code>null</code> we will take the base name of the provided filepath.
    * @returns boolean true if preparation was successful, false otherwise
    */
   public boolean prepareSourceForDrop(DropAction action,
                                       String filePath,
                                       String rootName);

   /**
    * All implementing objects must have an attribute to store the source file
    * path. This allows us to set it.
    *
    * @param filePath the complete file path
    */
   public void setFilePath(String filePath);


   /**
    * All implementing objects must have an attribute to store the source file
    * path. This allows us to get it.
    *
    * @return String the complete file path
    */
   public String getFilePath();


   /**
    * All implementing objects must have a flag which indicates that it was
    * created from the local file system. This allows us to set it. The flag
    * itself should only be used from within the implementing object.
    */
   public void setUsedLocalFileSystem();


   /**
    * A constant for a sub-directory of the application directory where
    * a copy of files dropped in the workbench may be saved.
    */
   public static final String SRC_DIR = "src";
}
