/*[ DTHtmlParameter.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSHtmlParameter;

import java.util.Enumeration;

public class DTHtmlParameter extends AbstractDataTypeInfo
{
   /**
    * Creates a PSHtmlParameter object, using the supplied strValue as the name.
    *
    * @throws IllegalArgumentException if strValue is null or empty
    */
   public Object create( String strValue ) 
         throws IllegalArgumentException
   {
      if ( null == strValue || 0 == strValue.trim().length())
         throw new IllegalArgumentException();

    // adds newly entered parameter into the html parameter list
    CatalogHtmlParam.addNewParamToMap( strValue );
    
      return new PSHtmlParameter( strValue );
   }

   /**
    * @returns Enumeration Containing Strings of all available HTML parameters
    * by forcing a catalog from all adjacent objects to the current OSDataset
   * object.
    */
   public Enumeration catalog()
   {
    return CatalogHtmlParam.getCatalog( Util.getFigure(), false ).elements();
   }
}
