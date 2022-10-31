/******************************************************************************
 *
 * [ UIFigure.java ]
 *
 * COPYRIGHT (c) 1999 - 2008 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/

package com.percussion.E2Designer;

import com.percussion.workbench.ui.editors.form.PSFrameProvider;
import com.percussion.workbench.ui.editors.form.PSXmlApplicationEditor;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.FilteredImageSource;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * UIFigure is the base class for a group of figures that represent
 * objects on the screen. It provides an interface to support the expected
 * actions the user will take such as basic editing (cut, copy, paste) and
 * property editing.
 * <p>
 * Each data object is typically represented on the screen using an image.
 * The end user can edit each object by initiating a property editing gesture,
 * if the object was created with an editor.
 */
public class UIFigure extends JPanel implements PageableAndPrintable
{
   // constructors
   /**
    * Creates a new visual object that appears on the screen like the image
    * found by loading the icon associated with strName and has the associated
    * Data object.
    *
    * @param strName name of the figure. Must be a valid name. See <b>
    * getName</b> for the definition of a valid name. The user displayable
    * name and the image icon are loaded using the ResourceHelper class. If a
    * filename is not supplied, the derived class is expected to handle the
    * paint message.
    *
    * @param data the object that contains the data that this UI object is
    * representing. This may be null if there is no associated data.   This object
    * must implement the IGuiLink interface or an exception is thrown.
    *
    * @param strEditorClassName the object that will created when the user wants
    * to edit the properties of this object. Typically this will be a dialog.
    * This may be null if there are no user editable properties. It is ignored
    * if Data is null. Originally I was planning on passing in the IEditor
    * object. However, this would have created a lot of dialogs that may never have
    * been used. Now the dialog will be created when needed. The passed in name
    * is the base name, not the fully qaulified name.
    *
    * @param ID a unique identifier within the set of objects created by the
    * factory that created this object.
    * 
    * @throws IllegalArgumentException if strName is null, or data does not
    * implement the IGuiLink interface.
    */
   public UIFigure( String strName, Object data,
         String strEditorClassName, int ID)
   {
        super( (LayoutManager) null );
      if (null == strName || (0 == strName.trim().length()))
         throw new IllegalArgumentException( E2Designer.getResources().getString( "InvName" ));

      if ( null != data && !( data instanceof IGuiLink ))
              throw new IllegalArgumentException( E2Designer.getResources().getString( "MissingInterface" ));

      m_data = (IGuiLink)data;
      if ( null != data )
          m_data.setFigure( this );

      m_Id = ID;
      //make on and off images we default to on
      ImageIcon image = ResourceHelper.getIcon( E2Designer.getResources(),
                     strName );
      if(image != null)
         m_onImage = image;

      if (null != image)
      {
         // setting JPanel size
         setSize( new Dimension(image.getIconWidth(),
               image.getIconHeight()));

         setOpaque(false);
//         setBackground( Color.yellow );
         m_imageComp = new JLabel(image);
        m_imageComp.setSize(getSize());
         m_imageComp.setVisible(false);
         add(m_imageComp, 0);
      }

      m_strEditorClassName = strEditorClassName;
      m_strName = strName;
   }

  /**
  * sets the new image and moves the attached controls
  *
  *@param bIsCenter if <code> true </code> then both exits attached
  *
  *@param strName the name of the object ( is used to create the on/off images
  *
  *@param image the new image
  *
  *@param bNeedOffset if <code> true </code> image is larger than the original,
  * move it.
  *
  */
  
   public void setNewIcon(@SuppressWarnings("unused") boolean bIsCenter,
         @SuppressWarnings("unused") boolean bIsQuery,String strName,ImageIcon image,
         @SuppressWarnings("unused") boolean bNeedOffset)
  {

    if( image != null )
    {
       Point before=new Point(getLocation());
       ImageIcon mCup=null;
       if(m_onImage != null)
       {
           m_onImage=null;  // destroy the old on image
       }
       m_onImage=image;   // set the new image

       // get the image dimensions
       Dimension figureSize = new Dimension(image.getIconWidth(),
                                                   image.getIconHeight());

//       System.out.println("panel position x="+before.x+" y="+before.y);


       int limit=getComponentCount();
       Component child=null;
       Point oldposition=new Point();
       for(int count=0;count<limit;count++)
       {
           child=getComponent(count);
           if( child instanceof JLabel  )
           {
              JLabel cp=(JLabel)child;
             // get the old location
              cp.getLocation(oldposition);
  //            System.out.println("figure position x="+oldposition.x+" y="+oldposition.y);
              break;
           }
       }

       // set the new size
        setSize(figureSize);
       // set opaque
          setOpaque(false);

       // remove the old label
       remove(m_imageComp);

       //  and set to null
       m_imageComp=null;
       // create the new label
         m_imageComp = new JLabel(image);
        m_imageComp.setSize(getSize());
         m_imageComp.setVisible(false);
       // and add it
        add(m_imageComp, 0);
       // save the name
       m_strName=strName;

       int iEdge=0;
       int iCupSize;

       // notify the attached objects of the new size

       if( limit > 0   )
       {
            for(int count=0;count<limit;count++)
            {
                // get the child
                child=getComponent(count);
                // is the label and we need to move it?
                if( child instanceof JLabel  )
                {
                     JLabel cp=(JLabel)child;
                     cp.setLocation(oldposition);
                     iEdge=oldposition.x;
                }
                // is a rigid connector

                if( child  instanceof  UIRigidConnectionPoint )
                {

                    UIRigidConnectionPoint rigid=(UIRigidConnectionPoint)child;

                    String name=rigid.getName();
                    // if is the pre
                    if( name.equals("RigidConnPtPreIcon") )
                    {
                          int off=iEdge; // 5 is the original position (when created)

                          // move the connector
                          Point rv=new Point(off,(figureSize.height/2)-3);
                          rigid.setLocation(rv);
                    }
                    else
                    {
                          int diff=(figureSize.width+iEdge)-10;
                          // move the connector
                          Point rv=new Point(diff,(figureSize.height/2)-3);
                          rigid.setLocation(rv);
                    }
                }

                // is the flex connector
                if(child instanceof UIFlexibleConnectionPoint )
                {
                    UIFlexibleConnectionPoint flex=(UIFlexibleConnectionPoint)child;
                    int id=flex.getConnectionPointId();
                    if( id == flex.CP_ID_LEFT  )
                    {
                          int diff=iEdge-10; // start at the edge - 10
                          // move the connector
                          Point rv=new Point(diff,(figureSize.height/2)-3);
                          flex.setLocation(rv);
                    }
                    else
                    {
                           // move to the left of the new figure
                          int diff=figureSize.width+iEdge;
                           // move the connector
                          Point rv=new Point(diff,(figureSize.height/2)-3);
                          flex.setLocation(rv);
                    }
                }

                if( child instanceof UIConnectableFigure )
                {
                    UIConnectableFigure cp=(UIConnectableFigure)child;
                    String name=cp.getType();
                    if( mCup ==null )
                    {
                         mCup = ResourceHelper.getIcon(E2Designer.getResources(),name );
                    }
                    if(mCup != null )
                    {
                        iCupSize=mCup.getIconWidth();
                    }
                    else
                    {
                       iCupSize=35;
                    }
                   //
                   if( name.equals("PreJavaExit") )
                   {

                          int off=iEdge;
                          // move the connector
                          Point rv=new Point(off,figureSize.height/3);
                          cp.setLocation(rv);
                   }
                   else if( name.equals("PostJavaExit") )
                   {
                          // move to the right of the figure
                          int diff=figureSize.width+iEdge-iCupSize;
                          Point rv=new Point(diff,figureSize.height/3);
                          cp.setLocation(rv);

                  }
             }


            }// for c
         } // if comp

         // adjust the bounds and we are done
      adjustBounds();
      // move to the prior location
      super.setLocation(before);


    } // if image != null
  }

   /**
    * This returns the status if this figure is currently edited (the editor
   * is open) or not.
   *
   * @return the editor status (true if open, false otehrwise)
    */
  //////////////////////////////////////////////////////////////////////////////
   public boolean isEdited()
   {
   return m_editor != null;
   }



   /**
   *  This function determines if this object is "on".  This method should be overriden
   *  in the figure factories
   */
   public boolean isOn()
   {
      return true;

   }

  public boolean canDisplayArrows()
  {
     m_bShowArrows=UserConfig.getConfig().getBoolean(E2Designer.getResources().getString("ENABLEARROWS"),true);
     return(m_bShowArrows);
  }

   /**
   * We will take care of the painting
   */
   @Override
   public void paint(Graphics g)
   {

    m_bShowArrows=UserConfig.getConfig().getBoolean(E2Designer.getResources().getString("ENABLEARROWS"),true);


      Point loc = getBaseImageLocation();
      if(loc == null)
         return;

      paintIcon(g, loc);

      super.paint(g);
   }


   protected void paintIcon(Graphics g, Point loc)
   {
      //do not try to paint the icon if there is no icon
      if (m_onImage == null)
         return;

      //if we are being edited change appearance
      if (m_editor != null)
      {
         if(isOn())
         {
            paintDisabled(g, loc);
         }
         else
         {
            paintOffDisabled(g, loc);
         }
      }
      //on or off
      else if(isOn())
      {
         paintOnImage(g, loc);
      }
      else
      {
         paintOffImage(g, loc);
      }      
   }

   private void paintDisabled(Graphics g, Point loc)
   {
      loadOn();

      if(m_disabledImage == null)
      {
         FilteredImageSource source = 
            new FilteredImageSource(m_onImage.getImage().getSource(),
                               new DissolveFilter(m_iBleachPercentage));
         m_disabledImage = new ImageIcon(createImage(source));                           
      }

      m_disabledImage.paintIcon(this, g, loc.x, loc.y);
   }

   private void paintOffDisabled(Graphics g, Point loc)
   {
      loadOff();

      if(m_offdisabledImage == null)
      {
         FilteredImageSource source =
            new FilteredImageSource(m_offImage.getImage().getSource(),
                               new DissolveFilter(m_iBleachPercentage));
         m_offdisabledImage = new ImageIcon(createImage(source));
      }

      m_offdisabledImage.paintIcon(this, g, loc.x, loc.y);
   }

   private void paintOnImage(Graphics g, Point loc)
   {
      loadOn();
      
      if(m_onImage != null)
         m_onImage.paintIcon(this, g, loc.x, loc.y);                     
   }

   private void paintOffImage(Graphics g, Point loc)
   {
      loadOff();

      m_offImage.paintIcon(this, g, loc.x, loc.y);
   }

   private void loadOn()
   {
      if(m_onImage == null)
      {
         m_onImage = ResourceHelper.getIcon( E2Designer.getResources(),
                     m_strName);
      }
      
   }
   
   private void loadOff()
   {
      if(m_offImage == null)
      {
         m_offImage = ResourceHelper.getIcon( E2Designer.getResources(), 
                     m_strName + "_off");
      }
      
   }

   // properties
   
   /**
    * @returns <code>true</code> if an image was loaded during construction
    */
   public boolean hasImage()
   {
      return(null != m_imageComp);
   }

   /**
    * @returns the identifier for this figure
    */
   public int getId()
   {
      return(m_Id);
   } 

   public void setId(int id)
   {
      m_Id = id;
   }

   /**
    * Provided for derived classes to return different ids based on location
    * of a mouse click.
    *
    * @param loc location of a mouse click, in component coords
    *
    * @returns the identifier for this figure, based on the passed in location.
    * By default, returns the same Id as getId().
    */
   public int getId(@SuppressWarnings("unused") Point loc)
   {
      return getId();
   } 

   /**
    * Returns the name of this object. The object cannot exist without a valid
    * name. A valid name is any non-empty string that does not contain
    * characters in the set
    * <p>
    * []+=-()*&^%$#@!`'";:.,><?/|\{}
    * <p>
    * (spaces are allowed)
    */
   @Override
   public String getName()
   {
      return m_strName;
   }

   /**
    * @returns the data object associated with this figure, or null if there isn't
    * one
    */
   public Object getData()
   {
      return m_data;
   }

   /**
   * Sometimes we need the data set back to null
   */
   public void resetData()
   {
      m_data = null;   
   }
   
   /**
    * Sets the data object for this figure and refreshes the figure display from
    * data object.
    *
    * @param data the data object to be set must be an instance of <code>
    * IGuiLink</code>, can be <code>null</code> if the current data object is
    * <code>null</code>.
    *
    * @throws IllegalArgumentException if data is not of the same type or a
    * sub-type of the current data object for this figure or not an instance of
    * <code>IGuiLink</code>
    */
   public void setData( Object data )
   {
      //if data is null then just set it
      //this is needed when we do a copy
      if(m_data == null)
      {
         if(data instanceof IGuiLink)
            m_data = (IGuiLink) data;
         else
         {
            if(data != null)
               throw new IllegalArgumentException(
                  "data object must be instance of IGuiLink");
         }
      }
      else
      {
         if ( null == data ||
            !m_data.getClass().isAssignableFrom( data.getClass()))
         {
            throw new IllegalArgumentException("invalid data object");
         }

         // release ownership
         m_data.release();
         m_data = (IGuiLink) data;

         // take ownership
         m_data.setFigure( this );

         //Refreshes the figure to represent the current data. This is not done
         //in setFigure because setFigure is also called initially when the
         //figure is getting created and calling this method in setFigure causes
         //exceptions for that case.
         if(m_data instanceof IRefreshableGuiLink)
            ((IRefreshableGuiLink) m_data).refreshFigure( );
      }
   }


   /**
    * Renames the object to the passed in name, if strName is a valid name.
    * See <b>getName()</b> for definition of a valid name. If strName is invalid,
    * the object is unchanged.
    *
    * @throws IllegalArgumentException if an invalid name is passed in
    */
   @Override
   @SuppressWarnings("unused")
   public void setName(String strName)
   {

   }

   /**
    * Determines whether the supplied point is on a relevent part of the figure.
    * A figure is comprised of a panel and possibly additional components. A
    * figure is not necessarily rectangular. This method allows a non-rectangular
    * figure to indicate whether a point is within the 'active' or visible part
    * of the component. The 'active' part is the part that will respond to
    * user input such as mouse clicks. If no image was loaded during construction,
    * this method will check if the point is within the bounds of the JPanel and
    * return <code>true</code> if it is.
    * <p> The frame uses this method to determine if dragging or selection should
    * occur.
    * <p> The derived class can override this method to handle more complex
    * hit testing.
    *
    * @param pt a point in component coordinates to be tested
    *
    * @returns <code>true</code> if the supplied point is over an 'active'
    * part of the component, otherwise <code>false</code> is returned. <code>
    * false</code is always returned if no image was loaded during construction.
    */
   public boolean isHit(Point pt)
   {
      boolean bHit = false;
      if (null != m_imageComp)
      {
         //todo: check if point is in transparent region of the image
         Rectangle rectBounds = m_imageComp.getBounds();
//         System.out.println("Hit test rets " +
//            String.valueOf(rectBounds.contains(pt)) + " " + rectBounds + pt);
         bHit = rectBounds.contains(pt);
      }
      else
         bHit = getBounds().contains(pt);

      if(!bHit && m_label != null)
         bHit = m_label.getBounds().contains(pt);

//      System.out.println( "Figure::isHit returns " + String.valueOf(bHit));
      return(bHit);

   }


/** @returns String The text filtered by IFigureLabelHelper.  If empty, it will
  * return null.
*/
  public String getLabelText()
  {
     if (null != m_labelHelper)
       return m_labelHelper.getLabelText(getData());

    return null;
  }


/** @returns JTextArea The label component of the figure.
*/
  public JTextArea getFigureLabel()
  {
    return m_label;
  }


/** @returns IFigureLabelHelper The instance of the abstract class defined by
  * the figure using this helper.
*/
  public IFigureLabelHelper getLabelHelper()
  {
    return m_labelHelper;
  }


/** @returns String The text filtered by IFigureLabelHelper.  If empty, it will
  * return null.
*/
  public String getLabelToolTip()
  {
   if (null != m_labelHelper)
     return m_labelHelper.getToolTipText(getData());
   else
     return null;
  }


   public void removeLabel()
   {
      remove( m_label );
      m_label = null;
      adjustBounds();
   }

   /**
    * Sets the label helper, which is a small class that looks up the figure
    * label and tooltip based on the data object. It allows customized labels
    * for different instances of the same class. If the helper has text for
    * the label and/or tooltip, a new label and tooltip is created (respectively).
    *
    * @param elf The little helper. To clear all labels and tooltips, pass in
    * null.
    *
    * @see IFigureLabelHelper
   **/
   public void setLabelHelper(IFigureLabelHelper elf)
   {
      m_labelHelper = elf;

      // add controls if there is text
      boolean bNewLabel = false;
      if ( null != m_labelHelper )
      {
         if ( null != getLabelText())
         {
            bNewLabel = true;
            createFigureLabel();
         }
         if ( null != getToolTipText())
            createFigureToolTip();
      }

      // if helper was removed and not replaced, remove any existing label
      if ( !bNewLabel && null != m_label )
         removeLabel();
   }


   // operations
   /**
    * Initiates the editor for this object. Usually called when the user
    * wants to edit the object properties.
    *
    * @param frameData the data object associated with the frame that this figure
    * is currently in. This data is passed directly to the editing dialog via
    * the IEditor interface.
    *
    * @returns <code>true</code> if the edit changed any properties in the object
    */

   public boolean onEdit( Object frameData)
   {
    boolean b = false;

      //if we have a proxy editor use it
     if(m_proxyEditor != null)
      {
      b = m_proxyEditor.onEdit(frameData);
      CatalogHtmlParam.resetParams();
        return b;
      }

      if ( null == m_strEditorClassName )
         return false;
         
      if ( null != m_editor )
      {
         if ( m_editor.isModal())
         {
            // it's possible to get the editor called more than once if it is slow to come up
            return false;
         }
         else
         {
            // bring the current editor to the top
        // sets current figure into memory
        Util.setFigure( this );
            b = m_editor.onEdit( this, frameData );
        CatalogHtmlParam.resetParams();
          return b;
         }
      }
     Cursor restoreCursor = E2Designer.getApp().getMainFrame().getCursor();
     Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
     UIMainFrame uiMF = E2Designer.getApp().getMainFrame();
      uiMF.setCursor(waitCursor);

      try
      {
         final Class<?> editorClass =
            Class.forName("com.percussion.E2Designer." + m_strEditorClassName );
         final Constructor ctor = getConstructorWithXmlApp(editorClass);
         final Constructor ctorWin = editorClass.getConstructor(
                new Class[] {Window.class, OSExitCallSet.class});

         m_editor = (IEditor) (ctor == null
               ? ctorWin.newInstance((Window)this.getParent(),null )
               : ctor.newInstance(getFigureFrame().getXmlApplicationEditor()));
         //change icon now that we are being edited
         repaint();

         if ( !m_editor.isModal())
         {
            if ( m_editor instanceof JInternalFrame )
            {
               ((JInternalFrame) m_editor).addInternalFrameListener( new InternalFrameAdapter ()
               {
                  @Override
                  @SuppressWarnings("unused")
                  public void internalFrameClosed(InternalFrameEvent e)
                  {
                     m_editor = null;
                     //change icon to indicate the object is not being edited
                     repaint();
                  }

                  //we need to change the icon when the activated state changes
                  @Override
                  @SuppressWarnings("unused")
                  public void internalFrameActivated(InternalFrameEvent e)
                  {
                     repaint();
                  }

                  @Override
                  @SuppressWarnings("unused")
                  public void internalFrameDeactivated(InternalFrameEvent e)
                  {
                     repaint();
                  }

               });
            }
            else if ( m_editor instanceof Window )
            {
               System.out.println( "Adding window listener" );
               ((Window) m_editor).addWindowListener( new WindowAdapter ()
               {
                  @Override
                  @SuppressWarnings("unused")
                  public void windowClosing( WindowEvent e )
                  {
                     m_editor = null;
                     repaint();
                  }
                  
                  @Override
                  @SuppressWarnings("unused")
                  public void windowActivated( WindowEvent e )
                  {
                      repaint();
                  }
                  
                  @Override
                  @SuppressWarnings("unused")
                  public void windowDeactivated( WindowEvent e )
                  {
                      repaint();
                  }
               });
            }
         }

      // sets current figure into memory
      Util.setFigure( this );
         boolean bChanged = m_editor.onEdit( this, frameData );
         if ( m_editor.isModal())
         {
            m_editor = null;
            //change icon to indicate the object is not being edited
            repaint();
         }
         uiMF.setCursor(restoreCursor);
      CatalogHtmlParam.resetParams();
         return bChanged;
      }
      catch ( Exception e )
      {
         /* There are so many errors to catch, we just want to tell user. This will
            almost always be a design time error. */
         PSDlgUtil.showError(e, true, E2Designer.getResources().getString( "OpErrorTitle" ));
         m_editor = null;
      }

     uiMF.setCursor(restoreCursor);
    CatalogHtmlParam.resetParams();
      return false;
   }

   /**
    * Returns constructor with {@link PSXmlApplicationEditor} as a parameter.
    */
   private Constructor getConstructorWithXmlApp(final Class<?> editorClass)
   {
      try
      {
         return editorClass.getConstructor(
               new Class [] { PSXmlApplicationEditor.class });
      }
      catch (NoSuchMethodException ignore) {
         return null;
      }
   }

   /**
    * @returns the size of the image that is drawn on the base panel. This can be
    * used by derived classes to change the layout if additional components are
    * added (or additional panel space is desired for drawing). If no image was
    * set in the constructor, a size of (0,0) is returned.
    */
   public Dimension getBaseImageSize()
   {
      Dimension size = null;

      if (null == m_imageComp)
         size = new Dimension(0, 0);
      else
         size = m_imageComp.getSize();
      return(size);
   }


   /**
    * If the figure has an image, the image bounds are used, otherwise the figure
    * bounds are used to determine if the supplied rectangle contains this figure.
    *
    * @param rect a rectangle in coord system of this figure
    *
    * @returns <code>true</code> if the visible part of this object is totally
    * contained in the supplied rect. If a line is on a line of the rect, it
    * is considered to be contained.
    */
   public boolean isContainedBy( Rectangle rect )
   {
      if ( null != m_imageComp )
         return rect.contains( m_imageComp.getBounds());
      else
         return rect.contains( getBounds());
   }

   /**
    * Calls the base class and modifies the bounds of the JPanel so that
    * the added component is visible.
    *
    * see the super class's documentation of the parameters except the
    * parameter constraint, noted below.
    *
    * @param constraint This parameter will be ignored and
    * <code>null</code> will be passed to the super class.
    */
   @Override
   @SuppressWarnings("unused")
   public void addImpl( Component c, Object constraint, int index )
   {
      // adjust the size, add property listener to veto moves
//      System.out.println( "Adding component to UIC" );
      super.addImpl( c, null, index );
      // adjust size of the component to encompass all children.
      adjustBounds();

   }

   /**
    * Modifies the size so all children can be fully seen; and the location so
    * the base image doesn't move with the resize. A border the thickness of
    * m_baseImageBorderWidth is left around the base image. Any label is always
    * placed at the bottom of all other components and border, and centered
    * under the base image.
    */
   protected void adjustBounds()
   {
      Point imageLoc = getBaseImageLocation();
      Dimension imageSize = getBaseImageSize();
      Rectangle bounding = new Rectangle( imageLoc, imageSize );
      Component [] comps = getComponents();
      for ( int i = getComponentCount()-1; i >= 0; i-- )
      {
//         System.out.println( "sub comp loc = " + comps[i].getBounds().toString());
         Component c = comps[i];
         if ( c == m_imageComp )
         {
            // acct for any border around the base image
            Rectangle rect = new Rectangle( c.getBounds());
            rect.grow( m_baseImageBorderWidth, m_baseImageBorderWidth );
            bounding = bounding.union( rect );
         }
         else if ( c != m_label )
            bounding = bounding.union( c.getBounds());
      }
//      System.out.println( "final bounding = " + bounding.toString());

      if ( null != m_label )
      {
         int labelCenter = imageLoc.x + imageSize.width / 2;
         m_label.setLocation( labelCenter - m_label.getSize().width / 2,
               bounding.height );
         bounding = bounding.union( m_label.getBounds());
      }

      for ( int i = getComponentCount()-1; i >= 0; i-- )
      {
         Point loc = comps[i].getLocation();
         loc.translate( -bounding.x, -bounding.y );
         comps[i].setLocation( loc );
      }

      Point location = getLocation();
//      System.out.println( "location before translation = " + location.toString());
      location.translate( bounding.x, bounding.y );
      setLocation( location.x, location.y );
      setSize( bounding.width, bounding.height );
   }


   /**
    * Derived classes may want to add a transparent border around the base
    * image, e.g. for adding selection indicator. This method sets the minimum
    * amount of space that will exist between the bounds of the image and the
    * bounds of the underlying panel. This border will be transparent.
    *
    * @param width The width of the transparent border around the image, in
    * pixels.
    */
   public void setBaseImageBorderWidth( int width )
   {
      if (null != m_imageComp)
      {
         m_imageComp.setLocation( width, width );
         m_baseImageBorderWidth = width;
         adjustBounds();
      }
   }

   /**
    * The default figure is a panel with a component for the image. This image
    * component is placed so there is a border around
    *
    * @returns the location of the ul corner of the main image, if there is
    * one, otherwise (-1,-1) is returned. A valid location will never be negative.
    */
   public Point getBaseImageLocation()
   {
      if (null != m_imageComp)
      {
         return m_imageComp.getLocation();
      }
      else
         return (new Point());
   }

   public void setEditorName(String name)
   {
      m_strEditorClassName = name;   
   }
   
   public String getEditorName()
   {
      return m_strEditorClassName;
   }
   
   public IEditor getEditor()
   {
      return m_editor;   
   }


/** 
  * Returns the top left point location of the JTextArea label of this figure 
  * label. If the label is null (does not exist), return a new Point.
  *
  * @returns Point The top left point of the JTextArea.
*/
  public Point getLabelLocation()
  {
    if (null != m_label)
      {
         return m_label.getLocation();
      }
      else
         return (new Point());
  }


/** Creates a figure ToolTip from the String supplied by getLabelToolTip.
  * No work is done if the String is null or empty. Should be called by the
  * derived UIFigure classes in AppFigureFactory or PipeFigureFactory.
  *
  * @see AppFigureFactory
  * @see PipeFigureFactory
*/
  private void createFigureToolTip()
  {
   if (null == getLabelToolTip() || getLabelToolTip().trim().equals(""))
   {
     System.out.println("ToolTip is null!");
     return;
   }
//    setToolTipText(getLabelToolTip());
   //m_imageComp.setToolTipText(getLabelToolTip());
   //System.out.println("(UIFigure:m_imageComp) ToolTip is: " + m_imageComp.getToolTipText());
   //System.out.println("(UIFigure:UIFigure) ToolTip is: " + m_imageComp.getToolTipText());
  }

  /**
  *   Whenever the data that is used to calculate the display name changes, call
  * this method to modify the text in the label and re-size and re-position it
  * based on the new text. If the label didn't exist, it will be created.
  * <p>
  * A UIFigure object is a JPanel with 0 or more objects contained within it.
  * The standard figure has an image based on a GIF file. The container JPanel is
  * transparent. Derived classes can add objects to this container, and they
  * can set a transparent border around the image. The text will be centered
  * on the base image, placed below the lowest component and have a max width
  * of twice the image width. The max line count is 3. Labels too long for the
  * maximum text box size are truncated and appended with "...".
  */
  public void invalidateLabel()
  {
    if(m_label == null)
    {
      createFigureLabel();
      return;
    }
   String text = getLabelText();
   FontMetrics fm = m_label.getFontMetrics(m_label.getFont());

   int textHeight = fm.getHeight();
   int textWidth = fm.stringWidth(text);

   Dimension labelSize = m_label.getSize();
   if ( labelSize.width == textWidth && labelSize.height == textHeight )
   {
      m_label.setText( text );
      return;
   }

   int iconWidth = getBaseImageSize().width;

   // Find the:
   //   Number of characters in the label
   //   Total width of the label (font points)
   //   Widest character in the label.
   // Stop looking after the total width is greater than 
   // than the (image.width * 2).
   int nChars;
   int charWidth = 0;
   int maxCharWidth = 0;
   int totalWidth = 0;
   for (nChars = 0; nChars < text.length(); nChars++)
   {
      charWidth = fm.charWidth(text.charAt(nChars));
      maxCharWidth = (charWidth > maxCharWidth) ? charWidth : maxCharWidth;
      totalWidth += charWidth;
      if (totalWidth > iconWidth * 2)
      {
         break;
      }
   }

   // if the text string is actually greater than (image.width * 4); meaning
   // the text is greater than 2 lines.
   if (textWidth > iconWidth * 4)
   {
      m_label.setRows(3);
      m_label.setPreferredSize( new Dimension( iconWidth * 2, textHeight * 3 ));

      // making large string fit in 3 rows
      String more = "...";
      // extra 2 pixels is to allow for variations in characters per line
      int maxWidth = iconWidth*6 - fm.stringWidth( more ) - 2;
      for (; nChars < text.length(); nChars++)
      {
         totalWidth += fm.charWidth(text.charAt(nChars));
         if ( totalWidth > maxWidth )
            break;
      }
      text = new String(text.substring(0, nChars-1) + more );
   }
   // if the width of the text string is greater than (image.width * 2).
   else if (textWidth > iconWidth * 2 && textWidth <= iconWidth * 4)
   {
     m_label.setRows(2);
     m_label.setPreferredSize( new Dimension( iconWidth * 2, textHeight * 2));
   }
   else // if text width is < image * 2 (height would be 1 row)
   {
     m_label.setRows(1);
     m_label.setPreferredSize( // char of padding added to width
           new Dimension( (textWidth + maxCharWidth) , textHeight ));
   }

   m_label.setSize(m_label.getPreferredSize());
   m_label.setText( text );

   adjustBounds();
  }


   /**
    * Creates a figure label (JTextArea) if the label text is not null, then
    * invalidates it so it is properly sized and positioned.
   **/
   @SuppressWarnings("unchecked")
   private void createFigureLabel()
   {
      // if there is no labelText, no label is created.
      //we may want to create the label with no text and then change the text later
      if (null == getLabelText())// || getLabelText().trim().equals(""))
         return;

      if ( null != m_label )
         remove( m_label );
      m_label = new JTextArea();
      m_label.setLineWrap(true);
      m_label.setWrapStyleWord(true);
    m_label.setEditable(false);
      m_label.setAlignmentY( Component.CENTER_ALIGNMENT );
//      m_label.setBackground( Color.lightGray );

      m_label.setBorder(null);

      // setting basic font sizes, text string dimensions and the image dimensions
      m_label.setFont( new Font((java.util.Map) null ));
//      Font f = getFont();
//      System.out.println( "::name: " + f.getName());
      // adding figure label to the display panel area.
      add(m_label, 1);
      invalidateLabel();
   }


   /**
   * Returns the image to use when dragging
   */
   public ImageIcon getDragImage()
   {
      return(m_onImage);
   }

   /**
   * Sets the proxy editor.  This is useful if you want 2 objects to share the same
   * data object for editing
   */
   public void setProxyEditor(UIFigure fig)
   {
      m_proxyEditor = fig;
   }

   /**
   * This function will return the UIFigureFrame that this component is in.
   * <code>null</code> will be returned if not in a UIFigureFrame
   */   
   protected UIFigureFrame getFigureFrame()
   {
      Component comp = getParent();
      while (true)
      {
         if (comp == null || comp instanceof UIFigureFrame)
         {
            return (UIFigureFrame) comp;
         }
         if (comp instanceof PSFrameProvider)
         {
            final PSFrameProvider provider = (PSFrameProvider) comp;
            if (provider.getFrame() instanceof UIFigureFrame)
            {
               return (UIFigureFrame) provider.getFrame();
            }
         }
         comp = comp.getParent();
      }
   }

   /**
    * {@link Action} class names to add to the local menu.
    * Class names are used to make sure it is correctly serialized.
    */
   public List<String> getMenuActions()
   {
      return m_menuActions;
   }

   /**
    * Add's an auxiliary editor to this figure.  Editor is invoked by the
    * {@link #onAuxEvent(String, Object)}.
    *
    * @param actionCommand The name of the command that will be used to invoke
    * the editor.  Never <code>null</code> or empty.
    * @param className The fully qualified name of the class to instantiate when
    * invoking the editor.  Never <code>null</code> or empty.  Class must
    * implement the IEditor interface and it's <code>isModal()</code> method
    * must return <code>true</code>,  however these two requirements are not
    * validated until the editor is invoked.
    *
    * @throws IllegalArgumentException if any param is invalid.
    */
   public void addAuxEditor(String actionCommand, String className)
   {
      if (actionCommand == null || actionCommand.trim().length() ==0)
         throw new IllegalArgumentException(
            "actionCommand may not be null or empty");

      if (className == null || className.trim().length() ==0)
         throw new IllegalArgumentException(
            "className may not be null or empty");

      m_auxEditorMap.put(actionCommand, className);
   }


   /**
    * Returns an Iterator over zero or more Strings, each is the action command
    * of the auxilliary editor, and may also be used to retrieve the menu item
    * text.
    *
    * @return The Iterator, never <code>null</code>, may be empty.
    */
   public Iterator getAuxEditorCommands()
   {
      return m_auxEditorMap.keySet().iterator();
   }

   /**
    * Invokes the editor that has the specified action command as its key, added
    * by a call to {@link #addAuxEditor(String, String)}.
    *
    * @param actionCommand The key used to identify the class that is to be
    * instantiated.  Never <code>null</code> or emtpy.
    * @param frameData the data object associated with the frame that this
    * figure is currently in. This data is passed directly to the editing dialog
    * via the IEditor interface.  May not be <code>null</code>.
    *
    * @return <code>true</code> if an editor matching the actionCommand is
    * found.
    *
    * @throws IllegalArgumentException if actionCommand is <code>null</code> or
    * empty, or if FrameData is <code>null</code>.
    */
   public boolean onAuxEvent(String actionCommand, Object frameData)
   {
      if (actionCommand == null || actionCommand.trim().length() ==0)
         throw new IllegalArgumentException(
            "actionCommand may not be null or empty");

      if (frameData == null)
         throw new IllegalArgumentException("frameData may not be null");

      /* this will only happen if someone manages to invoke a second time before
       * the first editor can come up
       */
      if (m_auxEditor != null)
         return false;

      // set the cursor to waiting
      Cursor restoreCursor = E2Designer.getApp().getMainFrame().getCursor();
      Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
      UIMainFrame uiMF = E2Designer.getApp().getMainFrame();
      uiMF.setCursor(waitCursor);

      // see if we have an editor to handle this
      try
      {
         final String className = m_auxEditorMap.get(actionCommand);
         if (className != null)
         {
            final Object editor = Class.forName(className).newInstance();

            // check that this is a legal editor
            if (!isValidEditor(editor))
            {
               throw new IllegalStateException("class " + className +
                  " is not a valid editor");
            }
            
            m_auxEditor = (IEditor) editor;

            // sets current figure into memory
            Util.setFigure(this);

            // invoke the editor
            m_auxEditor.onEdit(this, frameData);
            
            return true;
         }

      }
      catch ( Exception e )
      {
         /* There are so many errors to catch, we just want to tell user.
          * This will almost always be a design time error.
          */
         PSDlgUtil.showError(e,
            true,
            E2Designer.getResources().getString( "OpErrorTitle" ));
      }
      finally
      {
         // clear the editor
         m_auxEditor = null;

         // restore the cursor
         uiMF.setCursor(restoreCursor);
      }

      return false;
   }

   /**
    * Returns <code>true</code> if the editor is valid.
    * @param editor the object to check whether it is valid.
    */
   private boolean isValidEditor(Object editor)
   {
      return editor instanceof IEditor
            && ((IEditor) editor).isModal();
   }

   /**
   * Print objects to printer
   * PageableAndPrintable interface implementation
   */
    public int print(Graphics g, PageFormat pf, int pageIndex)
              throws PrinterException
   {
      if(!isVisible())
         return Printable.NO_SUCH_PAGE;
         
      Point pt = getLocation();
      
      //all object locations need to be on the same component level
      Component parent = getParent();
      UIFigureFrame figframe = getFigureFrame();
      if(parent != null && figframe != null)
         pt = SwingUtilities.convertPoint(parent, pt, figframe);

      //move down to the image
      Point loc = getBaseImageLocation();
      pt.translate(loc.x,loc.y);

      //check for the page
      Point pageLoc = getPrintLocation();
      if(pageLoc != null)
      {
         int iMovex = 0;
         int iMovey = 0;
         if(pageLoc.x > 0)
            iMovex = pageLoc.x * (int)pf.getImageableWidth();
         
         if(pageLoc.y > 0)
            iMovey = pageLoc.y * (int)pf.getImageableHeight();
         
         pt.translate(-iMovex, -iMovey);   
      }

      paintIcon(g, pt);
      
      //print our objects also
         Component [] comps = getComponents();
      for ( int index = comps.length-1; index >= 0; index-- )
      {
         if ( comps[index] instanceof PageableAndPrintable)
         {
            PageableAndPrintable uic = (PageableAndPrintable) comps[index];
            uic.setPrintLocation(m_printLocation);
            uic.print(g, pf, pageIndex);
         }
      }
      
      return Printable.PAGE_EXISTS;
   }

   public void setPrintLocation(Point pt)
   {
      m_printLocation = pt;
   }

   public Point getPrintLocation()
   {
      return(m_printLocation);
   }

   public void setLabelEditor(UIFigure editor)
   {
      m_labelEditor = editor;
      // the label is dependent on this editor
      invalidateLabel();
   }

   // variables
   private UIFigure m_proxyEditor = null;

   private String m_strName = null;
   private int m_Id;
   /**
    * The name of the class used to edit this figure. It must implement the
    * IEditor interface.
    */
   private String m_strEditorClassName = null;
   /**
    * Holds the currently executing editor. If dragged while the editor is open,
    * we don't want the editor when we are dropped.
    */
   transient private IEditor m_editor;

  private IFigureLabelHelper m_labelHelper;
  protected JTextArea m_label = null;

   /**
    * The data object that this figure owns. Set in {@link #setData setData}.
    */
   private IGuiLink m_data = null;
   private ImageIcon m_onImage = null;
   private ImageIcon m_offImage = null;
   private ImageIcon m_disabledImage = null;
   private ImageIcon m_offdisabledImage = null;

   private JLabel m_imageComp = null;
   /**
    * The figure will be the size of the image + x*2 in width and y*2 in height.
   **/
   private int m_baseImageBorderWidth = 0;

   static private int m_iBleachPercentage =65;
   private Point m_printLocation = null;
   protected UIFigure m_labelEditor = null;
   private boolean m_bShowArrows=true;

   /**
    * Map of auxiliary editors for this figure that can be accessed from the
    * popup menu.  Key is a String that specifies the action command used
    * when building the menu items, and the value is a String that specifies the
    * fully qualified class name of the class to use when the command is
    * activated.  The class specified must implement the IEditor interface.
    * Never <code>null</code>, may be empty.  Modified by calls to {@link
    * #addAuxEditor(String, String)}
    */
   private Map<String, String> m_auxEditorMap = new HashMap<String, String>();

   /**
    * Holds the currently executing auxilliary editor. If dragged while the
    * editor is open, we don't want the editor when we are dropped, so it is
    * transient.  However, this editor should always be modal, so this is
    * unlikely to be an issue.  Initialized during a call to {@link
    * #onAuxEvent(String, Object)}, <code>null</code> otherwise.
    * TODO: validate link signature
    */
   transient private IEditor m_auxEditor = null;
   
   final List<String> m_menuActions = new ArrayList<String>();
}



