/*[ JoinFigureFactory.java ]***************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import java.text.MessageFormat;

/**
 * Factory for creating the UI objects that are used for visually creating
 * the join editor.
 */
class JoinFigureFactory extends FigureFactory
{
   /**
    * Unique identifiers for each figure. The actual value is not important,
    * except it must be >= Util.FIRST_VALID_ID.
    */

   public static final int TABLE_ID = Util.FIRST_VALID_ID + 9;
  public static final int JOIN_DIRECTED_CONNECTION_ID = Util.FIRST_VALID_ID + 10;

   // constructors
   /**
    * Creates a new join figure factory. This method should only be
    * used by the FigureFactoryManager. All users of the class should obtain
    * an instance from the FigureFactoryManager.
    * <p>
    * Fills up the base class' vector with the names and categories of the
    * objects that can be added to the process view window.
    *
    * @see FigureFactoryManager
    */
   JoinFigureFactory( )
   {
    String fullname = getClass().getName();
    CLASS_NAME = fullname.substring( fullname.lastIndexOf( "." ) + 1);

      try
      {
         String strFigureName = TABLE;
         m_FigureList.add(new FigureInfo(strFigureName, strFigureName,
               TABLE_ID));

         strFigureName = JOIN_DIRECTED_CONNECTION;
         m_FigureList.add(new FigureInfo(strFigureName, strFigureName,
               JOIN_DIRECTED_CONNECTION_ID));
      }
      catch ( IllegalArgumentException e )
      {
         e.printStackTrace();
      }
   }

   /**
    * The internal names for the insert actions. The e2 resource bundle should
    * have entries matching this name.
    */
   public static final String TABLE   = "TableJoin";
  public static final String JOIN_DIRECTED_CONNECTION      = "JoinDirectedConnection";
  
   /**
    * If strType is a valid type, creates the figure associated with the type
    * and returns it. The type should be obtained from getFigureCategories and
    * getFigureTypes.
    *
    * @param strType name of the type of object to create. The string is case
    * insensitive.
    *
    * @throws UnsupportedOperationException if strType is not an available type
    *
    * @see FigureFactory
    */
   @Override
   public UIConnectableFigure createFigure(String strType)
   {
      UIConnectableFigure figure = null;
   try
   {
      if ( null == strType )
      {
         Object[] astrParams =
         {
            strType
         };
         throw new IllegalArgumentException( MessageFormat.format(
               E2Designer.getResources().getString( "NullArgFormat" ), astrParams ));
      }
      if ( 0 == strType.trim().length())
      {
         Object[] astrParams =
         {
            strType
         };
         throw new IllegalArgumentException( MessageFormat.format(
               E2Designer.getResources().getString( "EmptyStringErr" ), astrParams ));
      }

      // get the figure info from the vector
      FigureInfo fi = getElement( strType );
      if ( null == fi )
      {
         Object[] astrParams =
         {
            strType
         };
         throw new UnsupportedOperationException(MessageFormat.format(
               E2Designer.getResources().getString("UnsupportedFigure"),
               astrParams ));
      }

      if (strType.equals( TABLE ))
      {
         UIConnectableFigure table = new UIConnectableFigure(fi.getName(), new OSBackendTable(), "Dummy",
               fi.getId(), CLASS_NAME)
         {
            public boolean isOwnerDrawn()
            {
               return true;
            }
         };

         figure = table;
      }
      else if (strType.equals(JOIN_DIRECTED_CONNECTION))
      {
         UIJoinConnector conn = new UIJoinConnector(fi.getName(),
               new OSBackendJoin(), "BackendJoinPropertyDialog",
               fi.getId(), CLASS_NAME);
         figure = conn;
      }
      else
      {
         Object[] aParam =
         {
            strType
         };
         throw new UnsupportedOperationException(MessageFormat.format(
               E2Designer.getResources().getString("UnsupportedFigure"), aParam));
      }
   }
   catch(IllegalArgumentException e)
   {
      e.printStackTrace();
   }

   return figure;
   }

   // variables
   private String CLASS_NAME = null;   // calculate from class obj
}

