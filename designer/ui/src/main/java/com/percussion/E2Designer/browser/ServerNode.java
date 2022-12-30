/*[ ServerNode.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer.browser;

import com.percussion.E2Designer.E2Designer;
import com.percussion.E2Designer.PSDlgUtil;
import com.percussion.E2Designer.UIMainFrame;
import com.percussion.E2Designer.Util;
import com.percussion.conn.PSServerException;
import com.percussion.design.objectstore.PSLockedException;
import com.percussion.error.PSNonUniqueException;
import com.percussion.error.PSNotFoundException;
import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.security.PSAuthenticationFailedException;
import com.percussion.security.PSAuthorizationException;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.Vector;

/**
 * A server node is a special node that can only be a root in the tree. It has
 * a hierarchy constraint object that defines how the tree that has this node
 * as the root is organized.
 */
public class ServerNode extends RemovableNode
{
  //create static resource bundle object
  static ResourceBundle res = BrowserFrame.getBrowser().getResources();

   // constructors
   public ServerNode( ICatalogEntry Entry )
   {
      super( Entry );
   }

   /**
    * Returns the constraints previously set by setConstraints(). The constraints
    * determine the order of objects in the tree and whether certain types of
    * objects will be displayed. By changing the constraint object, the user
    * can easily switch between different display formats for the tree.
    */
   public IHierarchyConstraints getConstraints( )
   {
      return m_Constraints;
   }


   /**
    * Sets the display constraints object for this root node.
    */
   public void setConstraints( IHierarchyConstraints Constraints )
   {
    m_Constraints = Constraints;
    }


   /**
    * Creates the proper kind of node for each child. It does this by using the
    * Server Node's HierarchyConstraints object and calling the
    * HierarchyConstraints object's getChildren method.
    * 
    * @see IHierarchyConstraints
    */
   public void loadChildren()
   {
      if (m_Constraints == null)
         return;

      if (!getAllowsChildren())
         return;

      // set the cursor to wait cursor when loading children
      E2Designer e2designer = E2Designer.getApp();
      UIMainFrame uiMF = null;
      if (e2designer != null)
         uiMF = e2designer.getMainFrame();

      if (uiMF != null)
         uiMF.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (!loadedChildren || isDynamicEnum())
      {
         m_vChildren = m_Constraints.getChildren(this);
         if (m_vChildren == null)
            m_vChildren = new Vector();

         if (m_vChildren != null && !m_vChildren.isEmpty())
         {
            // sort m_vChildren by display name
            if (sorting())
               this.sortNodes(m_vChildren);

            Enumeration e = m_vChildren.elements();
            int i = 0;
            while (e.hasMoreElements())
            {
               DefaultBrowserNode dbn = (DefaultBrowserNode) (e.nextElement());

               this.insert(dbn, i); // add the children nodes WARNING: do not
                                    // use add() method
               // calls getChildCount which in turn calls loadChildren =>
               // infinite loop
               i++;
            }
         }
      }
      loadedChildren = true;
      m_bDynamicEnum = false;
      
      if (uiMF != null)
         uiMF.setCursor(Cursor.getDefaultCursor());
   }

   /**
    * Adds action item to edit define subsets.
    */
   public JPopupMenu getContextMenu( )
   {
      JPopupMenu menu = super.getContextMenu( );
      if ( null == menu )
         menu = new JPopupMenu();
      if(m_Constraints instanceof SQLHierarchyConstraints)
         menu.add(new JMenuItem(res.getString("DEFINE_SUBSET")));
      if(m_Constraints instanceof ApplicationHierarchyConstraints)
      {
         // the following is for reordering the menu items in the popup menu for Applications
         // Since Double clicking now opens the application, we want the
         // first menu item to be OPEN_APPLICATION
         MenuElement [] menuElems = menu.getSubElements();
         menu = new JPopupMenu();
         menu.add(new JMenuItem(res.getString("OPEN_APPLICATION")));
         for(int i=0; i< menuElems.length; i++)
         {
            menu.add((JMenuItem)menuElems[i]);
         }

         boolean bActive = BrowserFrame.getBrowser().isApplicationRunning(getInternalName());
         if (bActive)
              menu.add(new JMenuItem(res.getString("DISABLE")));
         else
            menu.add(new JMenuItem(res.getString("ENABLE")));

         menu.add(new JMenuItem(res.getString("EXPORT")));
      }
      return menu;
   }

   /**
    * If this is a server node for the application then return true so that
    * in-place editing is enabled.
    */
   public boolean isRenamable( )
   {
      return (m_Constraints instanceof ApplicationHierarchyConstraints &&
            null == E2Designer.getApp().getMainFrame().getApplicationFrame(getInternalName()));
   }

   /**
    * If this is an application node and it is open, don't allow it to be removed.
   **/
   public boolean isRemovable()
   {
      boolean bRemovable;
      if ( m_Constraints instanceof ApplicationHierarchyConstraints )
         bRemovable = null == E2Designer.getApp().getMainFrame().getApplicationFrame(getInternalName());
      else
         bRemovable = super.isRemovable();
      return bRemovable;
   }


   /**
    * When the entry is removed from the tree, we have to remove it from the
    * user configuration as well.  If the entry is an application, the user
    * is asked if they want to proceed.  If not, the entry is not removed.
    */
   protected void removeEntry( )
   {
      if (m_Constraints instanceof ApplicationHierarchyConstraints)
      {
         // Ask the user if they really want to remove it
         Object[] dispName =
         {this.getDisplayName()};
         int option = PSDlgUtil.showConfirmDialog(
               MessageFormat.format(E2Designer.getResources().getString("RemoveApplication"), dispName),
               E2Designer.getResources().getString("ConfirmOperation"),
               JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

         // the user canceled the operation: the node is not removed
         if (!(option == JOptionPane.YES_OPTION))
            return;

         String nodeName = this.getInternalName();

         // delete it from the server
         try
         {
            BrowserFrame.getBrowser().getObjectStore().removeApplication(
               nodeName);
         }
         catch (PSLockedException e)
         {
            final Object[] params =
            {(String) e.getErrorArguments()[1],};
            PSDlgUtil.showErrorDialog(
               Util.cropErrorMessage(MessageFormat.format(E2Designer
                  .getResources().getString("ApplicationLocked"), params)),
               E2Designer.getResources().getString("ApplicationErr"));
            return;
         }
         catch (PSAuthorizationException e)
         {
            final Object[] params =
            {e.toString()};
            PSDlgUtil.showErrorDialog(
               Util.cropErrorMessage(MessageFormat.format(E2Designer
                  .getResources().getString("AuthException"), params)),
               E2Designer.getResources().getString("AuthErr"));
            return;
         }
         catch (PSAuthenticationFailedException e)
         {
            e.printStackTrace();
         }
         catch (PSServerException e)
         {
            e.printStackTrace();
            return;
         }
      }
      // remove from parent by calling the method in super
      super.removeEntry();
   }

   public boolean isLoadedChildren()
   {
      return loadedChildren;
   }

   /**
    * Used to rename the object
    * 
    * @Returns true if renamed
    */
   public boolean Rename(TreePath path)
   {
      String currentName = getInternalName();
      PSObjectStore os = BrowserFrame.getBrowser().getObjectStore();
      if(m_Constraints instanceof ApplicationHierarchyConstraints)
      {
         try
         {
            // rename the application
            String newName = toString();
            os.renameApplication(currentName, newName);
            setInternalName( newName );
            return true;
         }
         catch (PSAuthorizationException e)
         {
            final Object[] params =
            {
               e.toString()
            };
            PSDlgUtil.showErrorDialog(
                             Util.cropErrorMessage(MessageFormat.format(E2Designer.getResources().getString("AuthException"), params)),
                                   E2Designer.getResources().getString("AuthErr" ));
         }
      catch (PSAuthenticationFailedException e)
      {
        e.printStackTrace();
      }
         catch (PSLockedException e)
         {
            final Object[] params =
            {
               e.getErrorArguments()[1],
            };
            PSDlgUtil.showErrorDialog(
                  Util.cropErrorMessage(MessageFormat.format(
                        E2Designer.getResources().getString("ApplicationLocked"), params)),
                  E2Designer.getResources().getString("OpErrorTitle"));
         }
         catch (PSNotFoundException e)
         {
            final Object[] params =
            {
               e.toString()
            };
            PSDlgUtil.showErrorDialog(
                  Util.cropErrorMessage(MessageFormat.format(
                        E2Designer.getResources().getString("FileNotFound"), params)),
                  E2Designer.getResources().getString("OpErrorTitle"));
         }
         catch (PSNonUniqueException e)
         {
            final Object[] params =
            {
               e.toString()
            };
            PSDlgUtil.showErrorDialog(
                  Util.cropErrorMessage(MessageFormat.format(
                        E2Designer.getResources().getString("NotUniqueApplicationException"), params)),
                  E2Designer.getResources().getString("OpErrorTitle"));
         }
         catch (PSServerException e)
         {
            final Object[] params =
            {
               e.toString()
            };
            PSDlgUtil.showErrorDialog(
                  Util.cropErrorMessage(MessageFormat.format(
                        E2Designer.getResources().getString("GenericServerError"), params)),
                  E2Designer.getResources().getString("ServerError"));
         }
         finally
         {
            E2Designer.getApp().getMainFrame().setCursor(Cursor.getDefaultCursor());
         }
      }

      return false;
   }

  // members

  private IHierarchyConstraints m_Constraints = null;

}



