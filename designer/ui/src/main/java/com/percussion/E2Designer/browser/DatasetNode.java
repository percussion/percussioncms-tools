/*[ DatasetNode.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSDataSet;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.design.objectstore.PSSystemValidationException;
import com.percussion.design.objectstore.PSVersionConflictException;
import com.percussion.error.PSNonUniqueException;
import com.percussion.error.PSNotFoundException;
import com.percussion.error.PSNotLockedException;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import javax.swing.*;
import javax.swing.tree.TreePath;

/**
 * A credalias node is designed for enabling in-place editing.
 */
public class DatasetNode extends DefaultBrowserNode
{
   // constructors
   DatasetNode( Object userObject )
   {
      super( userObject );
   }

   /**
   * This node has a menu, so <code>true</code> is returned.
   */
   public boolean hasMenu( )
   {
      return true;
   }

   /**
   * Creates a menu with a single 'Properties' Action.
   */
   public JPopupMenu getContextMenu( )
   {
      return new JPopupMenu();
   }

   public boolean isRenamable( )
   {
      return true;
   }

   /**
    * Used to rename the object
    */
   public boolean Rename(TreePath path)
   {
      /**
       * rename the credential alias
       */
      try
      {
         //get the appliaction
         ServerNode appNode = null;
         for(int iPath = 0; iPath < path.getPathCount(); ++iPath)
         {
            if(path.getPathComponent(iPath) instanceof ServerNode)
            {
               appNode = (ServerNode)path.getPathComponent(iPath);
               break;
            }
         }

         if(appNode != null)
         {
            PSApplication App = BrowserFrame.getBrowser().getObjectStore().getApplication( appNode.getInternalName(), true );
            if(App != null)
            {
               PSDataSet theDataSet = null;
               //find the credential
               for(int iSet = 0; iSet < App.getDataSets().size(); ++iSet)
               {
                  theDataSet = (PSDataSet)App.getDataSets().get(iSet);
                  if(theDataSet.getName().equals(getInternalName()))
                  {
                     theDataSet.setName(toString());
                     break;
                  }

                  theDataSet = null;
               }

               if(theDataSet != null)
               {
                  BrowserFrame.getBrowser().getObjectStore().saveApplication(App, true, false, false);
                  setInternalName(toString());
               }
            }

            return true;
         }
      }
      catch (PSServerException | PSVersionConflictException | PSAuthorizationException | PSAuthenticationFailedException | PSLockedException |
             PSNotFoundException | IllegalArgumentException | PSNonUniqueException | PSNotLockedException | PSSystemValidationException e )
      {
         e.printStackTrace();

      }

      return false;
   }

}
