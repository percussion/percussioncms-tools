/*[ CatalogHelper.java ]*******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

/**
 * Provides static utility methods for {@link ApplicationRequestor} Catalog
 * clients.
 */
public abstract class CatalogHelper
{
   /**
    * Gets the current server request root by querying the server configuration
    * held by the E2Designer.
    *
    * @return valid String representing current server request root, or the
    *    default root if the server can't be accessed.
    */
   protected static String getRequestRoot()
   {
      final String DEFAULT_ROOT = "Rhythmyx";
      if (null == E2Designer.getApp())
         return DEFAULT_ROOT; // provides a default when debugging w/o designer

      PSObjectStore objStore =
            E2Designer.getApp().getMainFrame().getObjectStore();
      try
      {
         if (null == ms_requestRoot)
            ms_requestRoot = objStore.getServerConfiguration().getRequestRoot();
      } catch (PSServerException e)
      {
         handleException( e );
      } catch (PSAuthorizationException e)
      {
         handleException( e );
      } catch (PSAuthenticationFailedException e)
      {
         handleException( e );
      }
      return ms_requestRoot == null ? DEFAULT_ROOT : ms_requestRoot;
   }


   /**
    * Shows a modal error message dialog and print the exception stack to the
    * console.  The method returns after the user dismisses the dialog.
    *
    * @param failure The exception that occurred.  Cannot be <code>null</code>.
    */
   protected static void handleException(Exception failure)
   {
      PSDlgUtil.showErrorDialog(failure.toString(),
            E2Designer.getResources().getString( "CatalogerExceptionTitle" ));
      failure.printStackTrace();
   }

   /**
    * Local storage to cache the server's request root to eliminate multiple
    * calls to obtain the server config just for this name. The probability of
    * it changing is extremely small.
    */
   private static String ms_requestRoot = null;
}
