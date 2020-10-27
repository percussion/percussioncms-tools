/*[ OSNonTextRequestor.java ]**************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSRequestor;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.error.PSIllegalArgumentException;

import java.util.HashMap;

/**
 * A thin wrapper around the base class to set different default values. The
 * extension/mime types are different for non-text resources.
 */
public class OSNonTextRequestor extends OSRequestor
{
   /**
    * The single constructor. Overrides the standard defaults to make them
    * appropriate for a Non-text resource object. 3 mime types for gif and
    * jpeg are added.
    */
   public OSNonTextRequestor()
   {
      HashMap mimeMap = new HashMap();
      mimeMap.put( "gif", new PSTextLiteral( "image/gif" ));
      mimeMap.put( "jpg", new PSTextLiteral( "image/jpeg" ));
      mimeMap.put( "jpeg", new PSTextLiteral( "image/jpeg" ));
      setMimeProperties( mimeMap );
      setCharacterEncoding( "" );
      setDirectDataStream( true );
   }

   /**
    * Sort of a copy constructor. Create a new one of these based on the
    * object store&apos;s underlying object.
    *
    * @param req The server&apos;s version that this object is derived from.
    */
   public OSNonTextRequestor( PSRequestor req )
      throws PSIllegalArgumentException
   {
      super( req );
   }

}
