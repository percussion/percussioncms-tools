/******************************************************************************
 *
 * [ PSFigureAbstractAction.java ]
 *
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSObjectStore;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditor;

import javax.swing.*;

/**
 * Action storing additional figure data.
 *
 * @author Andriy Palamarchuk
 */
public abstract class PSFigureAbstractAction extends AbstractAction
{
   public PSFigureAbstractAction(final String name) {
      super(name);
   }
   
   /**
    * As {@link IEditor#onEdit(UIFigure, Object)} first parameter.
    */
   public Object getData()
   {
      return m_data;
   }
   
   public void setData(Object data)
   {
      m_data = data;
   }
   
   /**
    * Returns {@link #getData()} as an application.
    * @return
    */
   protected OSApplication getApplication()
   {
      return (OSApplication) getData();
   }

   /**
    * As {@link IEditor#onEdit(UIFigure, Object)} second parameter.
    */
   public UIFigure getFigure()
   {
      return m_figure;
   }
   
   public void setFigure(UIFigure figure)
   {
      m_figure = figure;
   }
   
   /**
    * The editor editiing this application.
    */
   protected PSXmlApplicationEditor getEditor()
   {
      return getFigure().getFigureFrame().getXmlApplicationEditor();
   }
   
   /**
    * Returns current object store.
    */
   protected PSObjectStore getObjectStore()
   {
      return E2Designer.getApp().getMainFrame().getObjectStore();
   }
   
   private UIFigure m_figure;
   private Object m_data;
}
