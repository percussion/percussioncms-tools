/*[ JavaExitsPropertyDialogData.java ]*****************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import com.percussion.EditableListBox.EditableListBoxCellNameHelper;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.IPSExtensionParamDef;

import java.util.ArrayList;
import java.util.Iterator;



public class JavaExitsPropertyDialogData implements EditableListBoxCellNameHelper
{

   public JavaExitsPropertyDialogData( OSExtensionCall exitCall)
   {
      if(exitCall == null)
         throw new IllegalArgumentException("Cannot create data OSExtensionCall object is null");

      m_javaExit = exitCall.getExtensionDef();

      storeParamValues( m_javaExit, exitCall.getParamValues());
   }

   public JavaExitsPropertyDialogData( IPSExtensionDef exit)
   {
      if(exit == null)
         throw new IllegalArgumentException("Cannot create data IPSExtensionDef object is null");

      m_javaExit = exit;

      storeParamValues( exit, null );
   }


   public String toString()
   {
      return getName();
   }

   public String getName()
   {
      String exitPath = m_javaExit.getRef().getCategory()+ "/" +
          m_javaExit.getRef().getExtensionName();
      return exitPath;
   }

   public void setName(String name)
   {
      // noop; just there to implement EditableListBoxCellNameHelper
   }

   // the collection of PSExtensionParamDefs for the m_javaExit
   public IPSExtensionParamDef[] getParamDefCollection()
   {
      return m_collParamDefs;
   }


   public IPSExtensionDef getExit()
   {
      return  m_javaExit;
   }


   public void setParamValues( PSExtensionParamValue[] params)
   {
      if(params != null)
      {
         if(params.length != m_aParams.length)
                           return;

         for(int i=0; i<params.length; i++)
         {
            m_aParams[i] = params[i];
//            System.out.println("...parameter: " + params[i].getValue().getValueDisplayText());
         }
      }
   }

   public PSExtensionParamValue[] getParamValues()
   {
      return m_aParams;
   }

   /**
    * This flag is set when this object is created if the # of values in the
    * current call do not match the # or params in the definition. This flag
    * is set during construction and is never cleared. If <code>true</code>
    * is returned, the values were assigned to the params in their current
    * order, which may be different than how they were originally assigned.
    *
    * @return <code>true</code> if the value and parameter def counts match
    * <code>false</code> false otherwise.
    */
   public boolean hasParamValueMismatch()
   {
      return m_mismatch;
   }

   /**
    * @param values May be <code>null</code>, in which case the m_aParams
    * array will be an empty array with the same # of elements as m_collParamDefs.
    *
    * @throws IllegalArgumentException if the values array is not <code>null
    * </code> and the size of the values array does not match the number of
    * params in the supplied def.
    */
   private void storeParamValues( IPSExtensionDef def,
      PSExtensionParamValue [] values )
   {
      Iterator iter = def.getRuntimeParameterNames();
      ArrayList list = new ArrayList();
      while ( iter.hasNext())
      {
         list.add( def.getRuntimeParameter((String) iter.next()));
      }
      if (  null == values && list.size() > 0
            || values != null && values.length != list.size())
      {
         m_mismatch = true;
      }
      m_collParamDefs = new IPSExtensionParamDef[list.size()];
      list.toArray( m_collParamDefs );

      m_aParams = new PSExtensionParamValue[list.size()];
/*
      System.out.println( "  Name: "+def.getRef().getExtensionName() );
      System.out.println( "  m_aParams: "+m_aParams.length );
      System.out.println( "  values: "+ ((values == null) ? -1 : values.length) );
      System.out.println();
*/
      if ( null != values )
      {
         System.arraycopy( values, 0, m_aParams, 0,
            Math.min( values.length, m_aParams.length ));
      }
   }

   private IPSExtensionDef m_javaExit = null;

   // if the collection is modified, it will have to be saved in the end in the
   // IPSExtensionDef object collection of paramdefs to be displayed in the
   // table
   private IPSExtensionParamDef m_collParamDefs[] = null;
   private PSExtensionParamValue m_aParams[] = null;
   /**
    * This flag is set when this object is created if the # of values in the
    * current call do not match the # or params in the definition. Once set,
    * it is never cleared.
    */
   private boolean m_mismatch = false;
}
