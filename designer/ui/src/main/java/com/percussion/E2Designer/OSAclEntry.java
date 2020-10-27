/*[ OSAclEntry.java ]**********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/
package com.percussion.E2Designer;

import com.percussion.design.objectstore.PSAclEntry;

public class OSAclEntry extends PSAclEntry
{
   /*
   * Construct a new ACL entry with a name <unspecified> and type USER.
    */
   public OSAclEntry()
   {
      super("<unspecified>", PSAclEntry.ACE_TYPE_USER);
   }


   public OSAclEntry(PSAclEntry entry)
   {
      super("<unspecified>",PSAclEntry.ACE_TYPE_USER);
      super.copyFrom(entry);
   }

   public OSAclEntry(OSAclEntry entry)
   {
      super("<unspecified>", PSAclEntry.ACE_TYPE_USER);
      super.copyFrom((PSAclEntry)entry);
   }

   public void copyFrom(RoleMemberData memData)
   {
      setName(memData.getName());

      if(memData.isUser())
         setUser();
      else if(memData.isGroup())
      {
         setGroup();
      }
      else
      {
         setRole();
      }
   }

}
