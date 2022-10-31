/*[ PSEditorDialog.java ]******************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.validation.constraints.NotNull;
import java.awt.*;

/**
 * This class is designed to be used as the base class for all modal dialogs 
 * that are used to edit figure properties.
 */
abstract class PSEditorDialog extends PSDialog implements IEditor
{
  public PSEditorDialog()
  {
     super();
  }

  public PSEditorDialog(@NotNull Window dialog)
  {
    super(dialog);
  }

  protected PSEditorDialog(@NotNull Frame f)
  {
   super(f);
  }

  /*
  // this constructor was added for testing purpose only! DO NOT use this in the
  // product.
  protected PSEditorDialog(Frame f)
  {
   super(f);
  }
  */

   /**
    * @return Always non-zero.
   **/
   public int canClose()
   {
      return 1;
   }

   /**
    * IEditor interface method. Disposes the window peer stuff.
    *
    * @param cookie Ignored
    *
    * @param bForce Ignored
    *
    * @return Always <code>true</code>
   **/
   public boolean close( int cookie, boolean bForce )
   {
      dispose();
      return true;
   }

   /**
    * IEditor interface method. This method is not implemented by modal dialogs.
    *
    * @param context Is ignored.
    *
    * @throws UnsupportedOperationException Always.
   **/
   public boolean saveData( Object context )
   {
      throw new UnsupportedOperationException();
   }

   /**
    * IEditor interface method. Dialogs derived from this base are always modal.
    *
    * @return always returns <code>true</code>
    */
   public boolean isModal()
   {
      return true;
   }

   /**
    * IEditor interface method.
    *
    * @return Always <code>true</code>
   **/
   public boolean isDataChanged()
   {
      return true;
   }


}
