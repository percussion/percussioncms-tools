/*[ RoleNode.java ]************************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.*;
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
public class RoleNode extends DefaultBrowserNode
{
   // constructors
   RoleNode( Object userObject )
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
               PSRole theRole = null;
               //find the credential
               for(int iRole = 0; iRole < App.getRoles().size(); ++iRole)
               {
                  theRole = (PSRole)App.getRoles().get(iRole);   
                  if(theRole.getName().equals(getInternalName()))
                  {
                     //theRole.setName(toString());
                     break;
                  }
                  
                  theRole = null;
               }
               
               if(theRole != null)
               {
                  BrowserFrame.getBrowser().getObjectStore().saveApplication(App, true, false, false);
                  setInternalName(toString());
               }
            }
               
            return true;
         }
      }
      catch (PSServerException | PSNotLockedException | PSNonUniqueException | PSNotFoundException | PSAuthenticationFailedException | PSLockedException | PSAuthorizationException | PSVersionConflictException | PSSystemValidationException e )
      {
         e.printStackTrace();

      }


       return false;
   }

}
