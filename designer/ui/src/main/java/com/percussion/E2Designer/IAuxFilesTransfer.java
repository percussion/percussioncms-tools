/*[ IAuxFilesTransfer.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/


package com.percussion.E2Designer;

/**
 * This interface defines methods for managing application auxiliary files
 * during data transfer operations. It is designed to be implemented by data
 * objects so a helper class can retrieve file information, load the file(s)
 * store them into a temporary file and return the temp filename thru this
 * interface.
**/
public interface IAuxFilesTransfer
{

   /**
    * @return the relative name of the file on the server. This name can be
    * used to retrieve the file from the server using the original application
    * object. If there is no file, null is returned.
   **/
   public String getServerFileName();

   /**
    * returns the fully qualified name of the temporary file that contains the
    * data from the auxiliary file. If there is no file, null is returned.
   **/
   public String getTempFilePath();

   /**
    * Sets the fully qualified name of the temporary file that contains the
    * data from the auxiliary file. The data object will use this name to
    * read the data and store it on the server.
    *
    * @param path the fully qualified path of the file on local server.
   **/
   public void setTempFilePath( String path );

   /**
    * @return <code>true</code> if there are files to be transferred.
   **/
   public boolean hasFiles();


}
