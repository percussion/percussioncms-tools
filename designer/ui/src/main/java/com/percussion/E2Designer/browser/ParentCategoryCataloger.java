/*[ ParentCategoryCataloger.java ]****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.CatalogHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Catalogs all the parent categories.
 */
public class ParentCategoryCataloger  extends CatalogHelper
{
   /**
    * Use the static method, no new instances need to be created.
    */
   private ParentCategoryCataloger()
   {
   }

   /**
    * Returns a map of (display id, display name).
    */
   public static Map getAllParentCategories()
   {
      Map result = new HashMap();
      //Add all categories of views.
      //When we make the CX view categories custmoizable then we have
      //to make these values dynamic.
      result.put("1", "My Content");
      result.put("2", "Community Content");
      result.put("3", "All Content");
      result.put("4", "Other Content");
      
      return result;
   }

}