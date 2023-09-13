/******************************************************************************
 *
 * [ OSExitCallSet.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSApplication;
import com.percussion.design.objectstore.PSContentEditorPipe;
import com.percussion.design.objectstore.PSExtensionCallSet;
import com.percussion.design.objectstore.PSExtensionParamValue;
import com.percussion.design.objectstore.PSHtmlParameter;
import com.percussion.design.objectstore.PSPipe;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.IPSExtensionDef;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Properties;

/**
 * A wrapper for OSExtensionCallSet which also implements the IGuiLink and IPersist
 * interfaces. It is used to store data for GUI objects that attach to
 * resources.
 */
////////////////////////////////////////////////////////////////////////////////
public class OSExitCallSet extends PSExtensionCallSet implements IGuiLink, IPersist,
                                                            IDataCataloger
{

   public static final String EXT_TYPE_REQUEST_PRE_PROC =
      "com.percussion.extension.IPSRequestPreProcessor";

   public static final String EXT_TYPE_RESULT_DOC_PROC =
      "com.percussion.extension.IPSResultDocumentProcessor";

   /**
    * Constant to indicate 'Effect' interface type.
    */
   public static final String EXT_TYPE_RS_EFFECT =
      "com.percussion.relationship.IPSEffect";
      
      
   /**
    * Constant to indicate 'UDF' interface type.
    */
   public static final String EXT_TYPE_UDF =
      "com.percussion.extension.IPSUdfProcessor";

   /**
    * Constant to indicate one of the content type specific interfaces that runs
    * against an entire item when the item is submitted.
    */
   public static final String EXT_TYPE_ITEM_INPUT_TRANSFORMER = 
      "com.percussion.extension.IPSItemInputTransformer";

   /**
    * Constant to indicate one of the content type specific interfaces that runs
    * against an entire item when the item is requested.
    */
   public static final String EXT_TYPE_ITEM_OUTPUT_TRANSFORMER = 
      "com.percussion.extension.IPSItemOutputTransformer";

   /**
    * Constant to indicate one of the content type specific interfaces that runs
    * against a specific field when the item is submitted.
    */
   public static final String EXT_TYPE_FIELD_INPUT_TRANSFORMER = 
      "com.percussion.extension.IPSFieldInputTransformer";

   /**
    * Constant to indicate one of the content type specific interfaces that runs
    * against a specific field when the item is requested.
    */
   public static final String EXT_TYPE_FIELD_OUTPUT_TRANSFORMER = 
      "com.percussion.extension.IPSFieldOutputTransformer";

   /**
    * Constant to indicate one of the content type specific interfaces that runs
    * against a specific field when the item is submitted.
    */
   public static final String EXT_TYPE_FIELD_VALIDATION = 
      "com.percussion.extension.IPSFieldValidator";

   /**
    * Constant to indicate one of the content type specific interfaces that runs
    * against a specific field when the item is requested.
    */
   public static final String EXT_TYPE_FIELD_VISIBILITY = 
      "com.percussion.extension.IPSFieldVisibilityRule";

   /**
    * Constant to indicate one of the content type specific interfaces that runs
    * against a specific field when the item is requested.
    */
   public static final String EXT_TYPE_FIELD_EDITABILITY = 
      "com.percussion.extension.IPSEditabilityRule";

   /**
    * Default constructor.
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSExitCallSet()
   {
        super();
   }

   /**
    * Copy constructor.
    *
    * @param   source   the source object.
    */
   //////////////////////////////////////////////////////////////////////////////
   public OSExitCallSet(OSExitCallSet source)
   {
        super();
        copyFrom(source);
   }


   /**
    * Copy the passed object into this object. <p>
    * The owner is not copied by this method. If the caller wants to change
    * owners, they should call <code>setFigure( src.getFigure())</code> after
    * this method completes.
    *
    * @param   source   the source object.
    */
   //////////////////////////////////////////////////////////////////////////////
   public void copyFrom(OSExitCallSet source)
   {
      super.copyFrom((PSExtensionCallSet) source);
      m_currentType = source.m_currentType;
   }

   /** @see IDataCataloger */
   public void catalogData(ObjectType iObjType, CatalogReceiver container )
   {
      for ( int i = 0; i < size(); i++ )
      {
         OSExtensionCall exit = (OSExtensionCall) get(i);
         PSExtensionParamValue[] paramList = exit.getParamValues();
         for ( int j = 0; j < paramList.length; j++ )
         {
            if ( null != paramList[j] && paramList[j].isHtmlParameter() )
               container.add( ((PSHtmlParameter)paramList[j].getValue()).getName() );
         }
      }
   }


   //////////////////////////////////////////////////////////////////////////////
   // implementation for IGuiLink
   public void setFigure(UIFigure fig)   { m_owner = fig; }
   public void release()   {   m_owner = null;   }
   public UIFigure getFigure()   {   return m_owner;   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public boolean load(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      try
      {
         if(store instanceof PSPipe)
         {
            PSPipe pPipe = (PSPipe)store;
            /* Either one of result or input data exits or both will be null */
            PSExtensionCallSet pResult = pPipe.getResultDataExtensions();
            if( pResult != null )
            {
               int limit = pResult.size();
               for(int count=0; count<limit; count++)
               {
                  add((OSExtensionCall) pResult.get(count),
                     EXT_TYPE_RESULT_DOC_PROC );
               }
            }

            PSExtensionCallSet pInput = pPipe.getInputDataExtensions();
            if( pInput != null )
            {
               int limit=pInput.size();
               for(int count=0; count<limit; count++)
               {
                  add((OSExtensionCall) pInput.get(count),
                     EXT_TYPE_REQUEST_PRE_PROC );
               }
            }
         }

         else if(store instanceof PSContentEditorPipe)
         {
            PSContentEditorPipe pPipe = (PSContentEditorPipe)store;
            //Either one of result or input data exits or both will be null
            PSExtensionCallSet pResult = pPipe.getResultDataExtensions();
            if( pResult != null )
            {
               int limit = pResult.size();
               for(int count=0; count<limit; count++)
               {
                  add((OSExtensionCall) pResult.get(count),
                     EXT_TYPE_RESULT_DOC_PROC );
               }
            }

            PSExtensionCallSet pInput = pPipe.getInputDataExtensions();
            if( pInput != null )
            {
               int limit=pInput.size();
               for(int count=0; count<limit; count++)
               {
                  add((OSExtensionCall) pInput.get(count),
                     EXT_TYPE_REQUEST_PRE_PROC );
               }
            }
         }

         int id=this.getId();

         // restore GUI information
         OSLoadSaveHelper.loadOwner(id, config, m_owner);

         return true;
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return false;
   }


   /**
    * Override the base class to limit sets to the allowed types.
    *
    * @param index The index of an existing element. An exception will be thrown
    * if no element exists at this index.
    *
    * @param call The call to add to the set. Must be of an allowed type or an
    * exception is thrown. If null, nothing is done.
    *
    * @throws IllegalArgumentException if the call is not of the allowed type.
    *
    * @throws ArrayIndexOutOfBoundsException if index doesn't specify a valid entry
    *
    * @throws ClassCastException This will never be thrown by this method because
    * we narrow the allowed types.
   **/
   public Object set( int index, OSExtensionCall call )
   {
      if ( isAllowed( call.getExtensionDef()))
         return super.set( index, call );
      else
         throw new IllegalArgumentException( "Extension type"
            + " either doesn't match existing (" + m_currentType + ") or is not allowed." );
   }


   /**
    * Override the base class to limit adds to the allowed types.
    *
    * @param call The call to add to the set. Must be of an allowed type or an
    * exception is thrown. If null, nothing is done.
    *
    * @param ifaceType One of the interfaces supported by def. Must not be
    * <code>null</code> or empty.
    *
    * @return <code>true</code> if the call was added, <code>false</code> if the
    * call was not added, or was null
    *
    * @throws IllegalArgumentException if the call is not of the allowed type.
    *
    * @see #isAllowedType(IPSExtensionDef)
   **/
   public boolean add( OSExtensionCall call, String ifaceType )
   {
      if ( null == call )
         return false;

      if ( ifaceType == null || ifaceType.trim().length() == 0 )
         throw new IllegalArgumentException( "specified interface is null or empty" );

      if ( !call.getExtensionDef().implementsInterface( ifaceType ))
         throw new IllegalArgumentException(
            "mismatch between specified interface (" + ifaceType + ") and extension def" );

      if ( isAllowed( call.getExtensionDef()))
      {
         m_currentType = ifaceType;
         return super.add( call );
      }
      else
         throw new IllegalArgumentException( "Extension type"
            + " either doesn't match existing (" + getType() + ") or is not allowed." );
   }


   /**
    * Adds a new call based on the supplied extension, supplying empty values.
    * The type of the extension must match any calls that are already in the set.
    * If there are no calls, any exit type is allowed (i.e. no UDF types allowed).
    *
    * @param def A valid extension def. If null, nothing is done.
    *
    * @param ifaceType One of the interfaces supported by def. Must not be
    * <code>null</code> or empty.
    *
    * @throws IllegalArgumentException if the type of the supplied definition
    * is not allowed in this call set.
   **/
   public void setExtension( IPSExtensionDef def, String ifaceType )
   {
      if ( null == def )
         return;

      if ( ifaceType == null || ifaceType.trim().length() == 0 )
         throw new IllegalArgumentException(
            "specified interface is null or empty");

      if ( !def.implementsInterface( ifaceType ))
         throw new IllegalArgumentException(
            "mismatch between specified interface and extension def");

      if (!isAllowed( def ))
         throw new IllegalArgumentException("Extension type " + 
            "either doesn't match existing (" + m_currentType + 
            ") or is not allowed.");

      try
      {
         Iterator titles = def.getRuntimeParameterNames();
         int count = 0;
         for ( ; titles.hasNext(); titles.next())
            count++;

         PSExtensionParamValue values[] = new PSExtensionParamValue[count];
         /* Default all values to empty text literals. This is a bit of a mess.
            The OSExtensionCall class doesn't know how many params it needs
            so we need to set a valid one for each possible param. */
         if ( count > 0 )
         {
            values[0] = new PSExtensionParamValue(new PSTextLiteral(""));
            for ( int i = 1; i < count; ++i )
               values[i] = values[0];
         }
         OSExtensionCall  exit = new OSExtensionCall(def, values);
         add( exit, ifaceType );
      }
      catch ( IllegalArgumentException e )
      {
         e.printStackTrace();
      }
   }


   /**
    * Checks an extension to see if it would be allowed to be added to this
    * set under some condition. To determine if the extension can be added
    * right now, call {@link #isAllowed(IPSExtensionDef) isAllowed}.
    *
    * @param def An extension defition to check for compatibility.
    *
    * @return <code>true</code> if the supplied def is compatible with this
    * class, <code>false</code> otherwise.
    */
   public static boolean isAllowedType( IPSExtensionDef def )
   {
      Iterator ifaces = def.getInterfaces();
      boolean allowed = false;
      while ( ifaces.hasNext())
         allowed |= isAllowedInterface((String) ifaces.next());

      return allowed;
   }


   /**
    * Checks if the supplied interface name is one of the interfaces that
    * could be allowed into this set at some point in time.
    *
    * @param iface The name of an extension interface. May be <code>null</code>.
    *
    * @return <code>true</code> if the supplied interface name could be added
    * to this set, <code>false</code> otherwise. If <code>null</code> or
    * empty, <code>false</code> is returned.
    */
   private static boolean isAllowedInterface( String iface )
   {
      if ( null == iface || iface.trim().length() == 0 )
         return false;

      String [] allowedIfaces =
      {
         EXT_TYPE_REQUEST_PRE_PROC,
         EXT_TYPE_RESULT_DOC_PROC,
         EXT_TYPE_RS_EFFECT,
         EXT_TYPE_UDF,
         EXT_TYPE_FIELD_EDITABILITY,
         EXT_TYPE_FIELD_INPUT_TRANSFORMER,
         EXT_TYPE_FIELD_OUTPUT_TRANSFORMER,
         EXT_TYPE_FIELD_VALIDATION,
         EXT_TYPE_FIELD_VISIBILITY,
         EXT_TYPE_ITEM_INPUT_TRANSFORMER,
         EXT_TYPE_ITEM_OUTPUT_TRANSFORMER
      };

      boolean found = false;
      for ( int i = 0; i < allowedIfaces.length; i++ )
      {
         found |= allowedIfaces[i].equals( iface );
      }
      return found;
   }


   /**
    * Adds all extensions in the supplied set to the end of this one in the
    * order they appear in the supplied set.
    *
    * @param set The extension calls to add. If <code>null</code>, the method
    * returns immediately.
    *
    * @throws IllegalArgumentException if the type of the supplied set does
    * not match the type of this set.
    */
   public void append(OSExitCallSet set)
   {
      if ( null == set )
         return;

      if ( !set.getType().equals(getType()))
         throw new IllegalArgumentException(
            "input call set type doesn't match this one" );
      int limit=set.size();
      for(int count=0;count< limit;count++)
         super.add( set.get(count));
   }

   /**
    * Checks the supplied type against those allowed by this exit. The allowed
    * types will change dynamically. If the set has no exits, then any exit type
    * is allowed. If any exits are present, then only the type of the currently
    * contained exits is allowed.
    *
    * @param def One of the IPSExtensionDef.EXT_TYPE_... types.
    *
    * @return <code>true</code> if an exit that has the supplied type would be
    * allowed to be added to this set.
    *
    * @see #isAllowedType(IPSExtensionDef)
   **/
   public boolean isAllowed( IPSExtensionDef def )
   {
      if ( null == getType() && isAllowedType( def ))
         return true;

      Iterator iter = def.getInterfaces();
      boolean found = false;
      while ( iter.hasNext() && !found )
      {
         String iface = (String) iter.next();
         if ( iface.equals( m_currentType ))
            found = true;
      }
      return found;
   }

   /**
    * @return the type of exits that are contained in this collection. Only a
    * single type is allowed at any given time. The type is the fully qualified
    * interface name. If there are currently no exits in this set, <code>null
    * </code> is returned.
    */
   public String getType()
   {
      return m_currentType;
   }

   /**
    * @return The name of the first exit in the set, or the empty string if
    * the set has no exits. Never <code>null</code>.
    */
   public String getDropName()
   {
      String csRet = "";
      int limit=super.size();
      if( limit > 0 )
      {
         OSExtensionCall texit = (OSExtensionCall) get(0);
         csRet = texit.getExtensionRef().getExtensionName();
      }
      return(csRet);
   }

   /**
    * Interface method unimplemented.
    * @see com.percussion.E2Designer.IPersist#cleanup(com.percussion.E2Designer.OSApplication)
    */
   public void cleanup(OSApplication app)
   {
   }

   //////////////////////////////////////////////////////////////////////////////
   // implementation for IPersist
   public void save(PSApplication app, Object store, Properties config)
   {
      if (null == store || null == config)
         throw new IllegalArgumentException();

      if (!(store instanceof OSDataset ))
      {
         Object[] astrParams =
         {
            "OSDataset"
         };
         throw new IllegalArgumentException( MessageFormat.format(
            E2Designer.getResources().getString("IncorrectType"), astrParams));
      }

      try
      {
         // store current id temporary and create a new unique id for this object

         int currentId = getId();
         setId(Util.getUniqueId());
         PSPipe pPipe = null;

         if(store instanceof OSDataset)
         {
            OSDataset  set=(OSDataset)store;
            pPipe=set.getPipe();
         }

         String exitType = getType();
         if (exitType != null)
            if (exitType.equals( EXT_TYPE_REQUEST_PRE_PROC ))
               pPipe.setInputDataExtensions(this);
            else if (exitType.equals( EXT_TYPE_RESULT_DOC_PROC ))
               pPipe.setResultDataExtensions(this);

         // save GUI information
         OSLoadSaveHelper.saveOwner(currentId, this.getId(), config, m_owner);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

   }


   //////////////////////////////////////////////////////////////////////////////
   // private storage
   private UIFigure m_owner = null;

   /**
    * The fully qualified name of the interface that this set is currently
    * representing. If no exits are in the set, it is <code>null</code>. We
    * must keep a local copy here rather than determining it dynamically
    * because member exits can have multiple interfaces. If they all had
    * multiple interfaces, we couldn't necessarily figure out which one was
    * supposed to apply to this set.
    */
   private String m_currentType = null;
}
