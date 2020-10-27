/*[ ResizableObject.java ]*****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import javax.swing.*;
import java.awt.*;


/**
 * this class overwrites UIConnectableFigure, to allows the resize of the bitmap
 * and move the attached objects
 *
 */
public class ResizableObject extends UIConnectableFigure
{

  public ResizableObject(String strName, Object Data,String strEditorClassName,
                         int ID, String strFactoryClassName)

  {
     super(strName,Data,strEditorClassName,ID,strFactoryClassName);
     
     // see the object type, and store the type on an boolean
     if( strName.equals(AppFigureFactory.QUERY_DATASET) )
     {
        m_bIsQueryObject=true;
     }
     else if (strName.equals(AppFigureFactory.UPDATE_DATASET))
     {
       m_bIsQueryObject=false;
     }
     m_elementsAttached=NONE_ATTACHED;  // <------ initial state
  }

  /**
   *this routine loads the bitmap based on how many childs are attached
   */
  public void updateBitmap()
  {
      String name = null;
      // counter to see if both objects are attached
      int nAttached=0;
      // start creating the bitmap name
      if( m_bIsQueryObject )
            name=AppFigureFactory.QUERY_DATASET;

      else if(!m_bIsQueryObject)
            name=AppFigureFactory.UPDATE_DATASET;

      // reset the state
      m_elementsAttached=NONE_ATTACHED;
      // find all the components
      int limit = getComponentCount();
      if( limit > 0 )
      {
         Component cp;
         for(int count=0;count<limit;count++)
         { 
            cp = getComponent(count);
            // found the rigid connector
            if( cp instanceof  UIRigidConnectionPoint )
            {
               UIRigidConnectionPoint pt=(UIRigidConnectionPoint)cp;
               String objname=pt.getName();
               // anything attached?
               if( pt.getAttachedFigureCount() > 0 )
               {
                  System.out.println("getting attached figures");
                  // get the name
                  if( objname.equals(PRE_PROCESS_CP) )
                  {
                     nAttached++; // increase the counter
                     m_elementsAttached=LEFT_ATTACHED;   // and set the state
                  }
                  else if ( objname.equals(POST_PROCESS_CP) )
                  {
                     nAttached++; // increase the counter
                     m_elementsAttached=RIGHT_ATTACHED;  // and set the state
                  }
               }
            }
         }
         // are both attached?
         if( nAttached == 2 )
            m_elementsAttached=BOTH_ATTACHED;
      }
      
      // finish to create the name
      switch(m_elementsAttached)
      {
         case NONE_ATTACHED:
            m_bNeedOffset=false; // original dimensions, no offset need
            break;

         case RIGHT_ATTACHED:
            name+="_right";
            m_bNeedOffset=true;
            break;

         case LEFT_ATTACHED:
            name+="_left";
            m_bNeedOffset=true;
            break;

         case BOTH_ATTACHED:
            name+="_with2";
            m_bNeedOffset=true;
            break;
      }
      // remove the copy
      if( m_image != null )
          m_image=null;

        //load the image
      m_image = ResourceHelper.getIcon( E2Designer.getResources(), name );
      // set the image
      this.setNewIcon( m_elementsAttached == BOTH_ATTACHED,
         m_bIsQueryObject, name, m_image, m_bNeedOffset );
   }

   private boolean m_bNeedOffset=false;
   private static final int NONE_ATTACHED=0;
   private static final int RIGHT_ATTACHED=1;
   private static final int LEFT_ATTACHED=2;
   private static final int BOTH_ATTACHED=3;
   private int   m_elementsAttached=NONE_ATTACHED;
   private boolean m_bIsQueryObject=true;
   private static final String PRE_PROCESS_CP = "RigidConnPtPreIcon";
   private static final String POST_PROCESS_CP = "RigidConnPtPostIcon";
   private ImageIcon m_image=null;
}
