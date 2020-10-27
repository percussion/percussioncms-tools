/******************************************************************************
 *
 * [ OSExtensionCall.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.conn.PSDesignerConnection;
import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionRef;

import java.util.Iterator;
import java.util.List;

/**
 * Our GUI wrapper for the <CODE>PSExtensionCall</CODE> object. We add
 * an additional flag to denote the type of extension call it is and we keep
 * the extension definition w/ the call to ease use throughout the GUI.
 * <p>
 * When the application is loaded, all occurrences of PSExtensionCall (which
 * doesn't contain an extension def) are replaced w/ OSExtensionCalls, which
 * will contain the appropriate def. If the definition can't be found, then
 * it is set to <code>null</code>. In this case, the user cannot be allowed
 * to edit the call.
 *
 * @see OSUdfConstants
 */
public class OSExtensionCall extends PSExtensionCall
{
   /**
    * Construct a new extension call for the supplied definition and parameter
    * values.
    * 
    * @param def the extension definition, may not be <code>null</code>.
    * @param params the extensions parameter values, may be <code>null</code>.
    */
   public OSExtensionCall(IPSExtensionDef def, PSExtensionParamValue[] params)
   {
      super(def.getRef(), params);
      
      init(def);
   }

   /**
    * Constructor for creating this object from the OS object. Typically used
    * when creating this object from an application.
    *
    * @param call An Rx ObjectStore object. Since we can't get the OS to
    *    create our objects, we need to recreate them from existing objects
    *    created by the OS.
    * @param def The extension definition for the supplied call. If the ref
    *    in the call and def don't match, an exception is thrown. May be <code>
    *    null</code>, in which case the user will not be allowed to exit the
    *    call.
    * @throws IllegalArgumentException if <code>call</code> is <code>null
    *    </code> or the call reference and def reference don't match.
    */
   public OSExtensionCall(PSExtensionCall call, IPSExtensionDef def)
   {
      super(call);
      if (null != def && !def.getRef().equals(call.getExtensionRef()))
         throw new IllegalArgumentException(
            "mismatch between call and definition");
            
      init(def);
   }


   /**
    * Constructor for creating an OSExtensionCall wrapper for PSExtensionCall
    * object. This catalogs the server exits for the extension definition using
    * the extension reference parameters. The extension type is set as {@link
    * OSUdfConstants#UDF_UNKNOWN }. This should be called for an extension call
    * which is an exit only.
    *
    * @param call the extension call, may not be <code>null</code> and should
    * represent an exit.
    *
    * @throws IllegalArgumentException if call is not valid or could not find
    * definition for this extension.
    */
   public OSExtensionCall(PSExtensionCall call)
   {
      super(call);
      PSExtensionRef ref = call.getExtensionRef();
      PSDesignerConnection conn = E2Designer.getDesignerConnection();

      List defList = CatalogServerExits.getCatalog(conn, ref.getHandlerName(),
         null, null, false, true);

      if(defList != null)
      {
         Iterator defs = defList.iterator();
         while (defs.hasNext())
         {
            final IPSExtensionDef def = (IPSExtensionDef)defs.next();
            if (def.getRef().equals(call.getExtensionRef()))
            {
               init(def);
               return;
            }
         }
      }
      throw new IllegalArgumentException(
            "Invalid call, could not get definition for ref "
            + call.getExtensionRef());

   }

   /**
    * A copy constructor, with a type override.
    *
    * @param call The call to duplicate. The call's type will be overridden
    *    by the supplied type.
    */
   public OSExtensionCall(OSExtensionCall call)
   {
      super(call.getExtensionRef(), call.getParamValues());
      
      init(call.getExtensionDef());
   }

   public IPSExtensionDef getExtensionDef()
   {
      return m_extensionDef;
   }


   /**
    * Converts this object into a String suitable for displaying in a table
    * cell.
    * @return the string representation of the extension reference used by
    * this extension call in the format: <i>name(param, param)</i>.
    *
    * @todo move this method to the superclass
    */
   @Override
   public String toString()
   {
      StringBuffer function = new StringBuffer();
      PSExtensionParamValue[] params = getParamValues();
      function.append( getExtensionRef().getExtensionName() );
      function.append( "(" );
      for (int i=0; i<params.length; i++)
      {
         if (params[i] == null)
            function.append( "" );
         else
            function.append( params[i].getValue().getValueDisplayText() );
         if (i < (params.length - 1))
            function.append( ", " );
      }
      function.append( ")" );
      return function.toString();
   }
   
   /**
    * Test if this is an application UDF.
    * 
    * @return <code>true</code> if this is an application UDF, 
    *    <code>false</code> otherwise.
    */
   public boolean isApplicationUdf()
   {
      return m_udfType == OSUdfConstants.UDF_APP;
   }
   
   /**
    * Test if this is a global UDF.
    * 
    * @return <code>true</code> if this is a global UDF, <code>false</code> 
    *    otherwise.
    */
   public boolean isGlobalUdf()
   {
      return m_udfType == OSUdfConstants.UDF_GLOBAL;
   }
   
   /**
    * Set the UDF type.
    * 
    * @param type the new UDF type, must be one of the 
    *    <code>OSUdfConstants.UDF_xxx</code> constants.
    */
   public void setUdfType(int type)
   {
      if (type != OSUdfConstants.UDF_APP && 
         type != OSUdfConstants.UDF_GLOBAL && 
         type != OSUdfConstants.UDF_UNKNOWN)
         throw new IllegalArgumentException();
         
      m_udfType = type;
   }
   
   private int m_udfType = OSUdfConstants.UDF_UNKNOWN;

   /**
    * Sets an exit for this extension call
    * object. Additionally, it requires a type to be specified.
    *
    * @param def The extension ref object to be set into this extension
    * call.
    */
   public void setExtension(IPSExtensionDef def)
   {
      super.setExtensionRef(def.getRef());
      
      m_extensionDef = def;
   }

   /**
    * Common init routine for all constructors.
    *
    * @param def The extension definition for the supplied call. May be
    *    <code>null</code>.
    *
    * @see OSUdfConstants
    */
   private void init(IPSExtensionDef def)
   {
      m_extensionDef = def;
   }
   /**
    * Creates a deep copy of this OSExtensionCall instance
    * @return a clone of OSExtensionCall
    */
   @Override
   public Object clone()
   {
      OSExtensionCall copy = (OSExtensionCall) super.clone();
      return copy;
   }

   /**
    * This is the definition for this exit.
    */
   private IPSExtensionDef m_extensionDef;
}
