/******************************************************************************
 *
 * [ SecurityProviderEditorFactory.java ]
 *
 * COPYRIGHT (c) 1999 - 2011 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.E2Designer.admin;

import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.security.PSSecurityProvider;

import javax.swing.*;


/**
 * Provides a factory that creates provider editors based on a type id.
 *
 * @see SecurityProviderMetaData
 */
public class SecurityProviderEditorFactory
{
   /**
    * Creates an editor appropriate for the requested security provider type
    * and returns it.
    *
    * @param id The id of the security provider. Must be one of the types
    * returned by <code>getSecurityProvidersById</code>.
    *
    * @param parent A valid frame to use as the parent of the returned dialog so
    * it behaves correctly w/ z-ordering.
    *
    * @return An editor that can be used to edit/create a security provider
    * type specified by the id.
    *
    * @throws IllegalArgumentException If the supplied id does not match a
    * known provider or parent is null. This is possible when the server adds
    * a provider but this class hasn't been updated.
    */
   public static ISecurityProviderEditor getSecurityProviderEditor(int id,
      JFrame parent, PSServerConfiguration config)
   {
      if ( null == parent )
         throw new IllegalArgumentException( "parent can't be null" );

      ISecurityProviderEditor editor = null;
      switch ( id )
      {
         case PSSecurityProvider.SP_TYPE_BETABLE:
            editor = new DbmsTableSecurityProviderDialog( parent,
                AppletMainDialog.getServerConnection(), config);
            break;
         case PSSecurityProvider.SP_TYPE_DIRCONN:
            editor = new JndiSecurityProviderDialog(parent, config);
            break;
         case PSSecurityProvider.SP_TYPE_WEB_SERVER:
            editor = new PSWebServerSecurityProviderDialog(parent, id);
            break;
         case PSSecurityProvider.SP_TYPE_SPNEGO:
            editor = new PSSpnegoProviderDialog(parent, id);
            break;            
         case PSSecurityProvider.SP_TYPE_HOST_ADDRESS:
            editor = new SimpleSecurityProviderDialog( parent, id );
            break;
         case PSSecurityProvider.SP_TYPE_ODBC:
            editor = new ODBCSecurityProviderDialog( parent );
            break;
         default:
            // todo internationalize
            throw new IllegalArgumentException( "Security provider " + id
               + " not supported." );
      }
      return editor;
   }

   /**
    * All methods are static, so no instances need to be created.
    */
   private SecurityProviderEditorFactory()
   {}
}

