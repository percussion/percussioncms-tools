/*******************************************************************************
 *
 * [ PSTransferFactory.java ]
 * 
 * COPYRIGHT (c) 1999 - 2005 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.workbench.ui.dnd;

import com.percussion.client.PSConnectionInfo;
import com.percussion.client.PSCoreFactory;
import com.percussion.client.PSObjectType;
import com.percussion.client.PSObjectTypes;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

import java.util.HashMap;
import java.util.Map;

/**
 * This class follows the singleton pattern. The factory creates
 * {@link Transfer} objects that represent a supplied identifier (transfer
 * type). The returned objects are capable of handling
 * {@link com.percussion.client.IPSReference} objects only.
 * <p>
 * Many different types are provided so that drop handlers can provide more
 * accurate feedback as to what is allowed without having to get the actual
 * transferred object.
 * <p>
 * Only 1 instance for each requested type is maintained and that same instance
 * will be returned if the same type is requested.
 * 
 * @version 6.0
 * @author Paul Howard
 */
public class PSTransferFactory
{
   /**
    * Get the single instance of this class.
    * 
    * @return Never <code>null</code>.
    */
   public static PSTransferFactory getInstance()
   {
      return ms_instance;
   }

   /**
    * Walks through all known transfers and checks each one as to whether it
    * supports the supplied type.
    * 
    * @param dataType Never <code>null</code>.
    * 
    * @return The transfer whose {@link Transfer#isSupportedType(TransferData)}
    * method returns <code>true</code> on the supplied type, or
    * <code>null</code> if one is not found.
    */
   public Transfer getTransfer(TransferData dataType)
   {
      if ( null == dataType)
      {
         throw new IllegalArgumentException("dataType cannot be null");  
      }
      for (Transfer t : m_instances.values())
      {
         if (t.isSupportedType(dataType))
            return t;
      }
      return null;
   }
   
   /**
    * The returned transfer can handle transforming a
    * {@link com.percussion.workbench.ui.PSUiReference} during drag and drop.
    * Each returned transfer only differs in the type that it claims to
    * support.
    * 
    * @param type Only the primary type is considered. Never <code>null</code>
    * or empty.
    * 
    * @param subType If provided, it is used to build the key to find a matching
    * transfer. May be <code>null</code> or empty to ignore.
    * 
    * @return Never <code>null</code>.
    */
   public Transfer getTransfer(PSObjectType type, String subType)
   {
      PSReferenceTransfer t = m_instances.get(getInstanceKey(type, subType));
      if (t == null)
      {
         StringBuffer suffix = new StringBuffer();
         if (!StringUtils.isBlank(subType))
            suffix.append(subType);
         PSConnectionInfo info = PSCoreFactory.getInstance().getConnectionInfo();
         if (info != null) 
         {
            if (suffix.length() > 0)
               suffix.append(":");
            suffix.append(info.getProtocol());
            suffix.append(":");
            suffix.append(info.getServer());
            suffix.append(":");
            suffix.append(info.getPort());
         }
         t = new PSReferenceTransfer(type, suffix.toString().toLowerCase(), 
               isSecondaryTypeDependent(type));
         m_instances.put(getInstanceKey(type, subType), t);
      }
      return t;
   }
   
   /**
    * Returns a key for use with the {@link #m_instances} map that is
    * appropriate for the supplied type and sub type. It takes into
    * consideration whether the secondary type of the object type is needed by
    * calling {@link #isSecondaryTypeDependent(PSObjectType)}.
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @param subType May be <code>null</code> or empty to ignore.
    * 
    * @return Never <code>null</code> or empty.
    */
   private String getInstanceKey(PSObjectType type, String subType)
   {
      String key = isSecondaryTypeDependent(type) ? type.toString() : type
            .getPrimaryType().name();
      if (!StringUtils.isBlank(subType))
         key += ":" + subType;
      return key;
   }
   
   /**
    * It may be desirable to make the transfer dependent on the secondary type
    * if there are dnd operations that are dependent on the secondary type. 
    * 
    * @param type Assumed not <code>null</code>.
    * 
    * @return <code>true</code> if the transfers should be created for each
    * secondary type, <code>false</code> otherwise.
    */
   private boolean isSecondaryTypeDependent(PSObjectType type)
   {
      return 
         type.getPrimaryType() == PSObjectTypes.TEMPLATE
         || type.getPrimaryType() == PSObjectTypes.UI_VIEW
         || type.getPrimaryType() == PSObjectTypes.UI_SEARCH
         || type.getPrimaryType() == PSObjectTypes.UI_ACTION_MENU         
         || type.getPrimaryType() == PSObjectTypes.XML_APPLICATION         
         ;
   }

   /**
    * Made private to implement the singleton pattern. Use 
    * {@link #getInstance()}.
    * 
    */
   private PSTransferFactory()
   {}

   /**
    * The single instance of this class. <code>null</code> until 
    * {@link #getInstance()} called the first time, then never <code>null</code>
    * or modified.
    */
   private static PSTransferFactory ms_instance = new PSTransferFactory();

   /**
    * Each key is the string representation of the object type and an optional
    * supplied subType string. Entries are created as needed. The key for this
    * map must be obtained using the
    * {@link #getInstanceKey(PSObjectType, String)} method.
    */
   private Map<String, PSReferenceTransfer> m_instances = 
      new HashMap<String, PSReferenceTransfer>();
}