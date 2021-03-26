/******************************************************************************
 *
 * [ PSEditDtdAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.workbench.ui.PSMessages;

import java.awt.event.ActionEvent;

public class PSEditDtdAction extends PSFigureAbstractAction
{
   public PSEditDtdAction()
   {
      super(PSMessages.getString("PSEditDtdAction.name"));
   }

   public void actionPerformed(ActionEvent e)
   {
      if (getFigure().getData() instanceof OSPageDatatank)
      {
         final OSPageDatatank datatank = (OSPageDatatank) getFigure().getData();
         assert datatank.getSchemaSource() != null;
         try
         {
            getEditor().openEditorForResource(datatank.getSchemaSource());
         }
         catch (Exception ex)
         {
            PSDlgUtil.showError(ex);
         }
      }
   }
   
   /**
    * Indicates whether has a DTD. 
    */
   public boolean hasDtd()
   {
      if (getFigure().getData() instanceof OSPageDatatank)
      {
         final OSPageDatatank datatank = (OSPageDatatank) getFigure().getData();
         return datatank.getSchemaSource() != null;
      }
      return false;
   }

   @Override
   public void setFigure(UIFigure figure)
   {
      super.setFigure(figure);
      setEnabled(hasDtd());
   }
}
