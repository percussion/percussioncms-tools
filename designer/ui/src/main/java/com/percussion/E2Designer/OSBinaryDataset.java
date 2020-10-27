/*[ OSBinaryDataset.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSDataSet;
import com.percussion.error.PSIllegalArgumentException;

/**
  * This class converts all the sub-objects of PSDataSet into their OS... 
  * counterparts, just like its parent, OSDataset.  However, added attention to
  * PSRequestor.outputMimeType (or OSRequestor) for <CODE>gif</CODE> and
  * <CODE>jpg</CODE> types to differentiate OSBinaryDataset from OSDataset.
   * Basically, OSBinaryDataset is a restricted version of OSDataset.
   * When created, this class has an OSQueryPipe, OSDataSelector, OSRequestor and
   * OSDataMapper.
*/

public class OSBinaryDataset extends OSDataset
{
//
// CONSTRUCTORS
//

  /**
    * @throws PSIllegalArgumentException should never be thrown when this form of
    * the constructor is called.
    */
   public OSBinaryDataset() throws PSIllegalArgumentException
   {
      this( "BinaryDataset" );
   }

   /**
    * @throws PSIllegalArgumentException if strName is null, empty or invalid (
    * greater than 50 chars).
    */
   public OSBinaryDataset( String strName ) throws PSIllegalArgumentException
   {
      super( strName );

      // Add pieces required for binary dataset, except the backend datatank
      OSQueryPipe pipe = new OSQueryPipe();
      pipe.setDataMapper( new OSDataMapper());
      pipe.setDataSelector( new OSDataSelector());
      setPipe( pipe );
      setRequestor( new OSNonTextRequestor());
   }


   public static final int DST_BINARY = 3;
   public static int getType(PSDataSet ds)
   {
      // todo: This technique sucks, we need to fix it
      int type = OSDataset.getType( ds );
      if ( DST_QUERY == type &&
         null != ds.getRequestor()
         && ds.getRequestor().isDirectDataStream())
      {
         type = DST_BINARY;
      }
      return type;
   }



   /**
    * Creates a new object that takes all of its properties from the supplied
    * dataset.
    *
    * @param dataSet a valid dataset
    *
    * @throws NullPointerException  if dataSet is null
    *
    * @throws PSIllegalArgumentException this exception should never be thrown
    * unless ds is corrupt AND if the OutputMimeType is null (which does not make
   * sense for OSBinaryDataset, an image container object).
    */
   public OSBinaryDataset( PSDataSet dataSet ) throws PSIllegalArgumentException
   {
      super( dataSet.getName());
      copyFrom( dataSet );
   }

   /**
    * Creates a new object that takes all of its properties from the supplied
    * dataset.
    *
    * @param dataSet a valid dataset
    *
    * @throws NullPointerException  if dataSet is null
    *
    * @throws PSIllegalArgumentException this exception should never be thrown
    * unless ds is corrupt AND if the OutputMimeType is null (which does not make
   * sense for OSBinaryDataset, an image container object).
    */
   public OSBinaryDataset( OSBinaryDataset dataSet ) throws  PSIllegalArgumentException
   {
      super( dataSet.getName());
      copyFrom( dataSet );
   }

}
