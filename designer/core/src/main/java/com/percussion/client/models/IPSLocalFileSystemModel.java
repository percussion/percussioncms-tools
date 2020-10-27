/******************************************************************************
 *
 * [ IPSLocalFileSystemModel.java ]
 * 
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.client.models;

import com.percussion.client.IPSReference;

import java.io.File;

/**
 * Provides additional functionality of getting file object from the file
 * reference.
 *
 * @author Andriy Palamarchuk
 */
public interface IPSLocalFileSystemModel extends IPSCmsModel
{
   /**
    * File corresponding the reference.
    */
   File getFile(IPSReference reference);
}
