/*[ UIConnector.java ]*********************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;


import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
/**
 * This class defines a resizable line that can connect 2 connection points
 * together. The 2 end points can be moved to arbitrary positions if they are
 * not attached. Either endpoint can attach to a connector. The ID that an
 * endpoint claims to be is the same as the ID of the object attached to the
 * other end. If no object is attached, a generic connector ID is returned,
 * If the end user clicks and drags a resizing handle, the line can be moved
 * within constraints to make it look good on the screen. Each line segment will
 * have a resize handle.
 */
public class UIConnector extends UIConnectableFigure
{
   /**
    * Width of each segment of the poly-line making up the connector, in pixels.
    */
   public static final int LINE_WIDTH = 1;

   // constructors
   public UIConnector(String strName, Object Data,
         String strEditorClassName, int ID, String strFactoryClassName)
   {
      super(strName, Data, strEditorClassName, ID, strFactoryClassName);
      // add points for default polyline
      m_points.addElement(new AttachedPoint(new Point(0, 0)));
      m_points.addElement(new Point(75, 0));
      Point endPoint = new AttachedPoint(new Point(75, 60));
      m_points.addElement(endPoint);
      createHandles();
      Vector<Point> oldPoints = new Vector<Point>( m_points );
      adjustBounds(oldPoints ); //, new Point(), endPoint);
      setOpaque(false);
   }

   /**
   * Function for printing
   */
    public int print(Graphics g, PageFormat pf, int pageIndex)
              throws PrinterException 
   {
      Vector<Point> points = new Vector<Point>();
      UIFigureFrame figframe = getFigureFrame();

      //check for the page
      Point pageLoc = getPrintLocation();
      int iMovex = 0;
      int iMovey = 0;
      if(pageLoc != null)
      {
         if(pageLoc.x > 0)
            iMovex = pageLoc.x * (int)pf.getImageableWidth();
         
         if(pageLoc.y > 0)
            iMovey = pageLoc.y * (int)pf.getImageableHeight();
      }


      if(figframe != null)
      {
         for(int iPoint = 0; iPoint < m_points.size(); ++iPoint)
         {
            Point loc = SwingUtilities.convertPoint(this, (Point)m_points.get(iPoint), figframe);
            loc.translate(-iMovex, -iMovey);
            points.add(loc);
         }
      }

      Vector handles = new Vector();
      paintLine(g, points, handles);
      return Printable.PAGE_EXISTS;
   }






   /**
   * This function is used to decide if this object can be edited
   */
   public boolean isEditable()
   {
      if(super.isEditable()
         && ( weAreConnectedToAnOutputResultPage()
            && weAreConnectedToAnInputDataset()))
            return(true);

      return(false);
   }

   public UIConnector.AttachedPoint getFirstAttached()
   {
      if(m_points.size() > 0 && m_points.get(0) instanceof UIConnector.AttachedPoint)
         return (UIConnector.AttachedPoint)m_points.get(0);

      return(null);
   }

   /**
    * @returns the data object associated with this figure, or null if there isn't
    * one
    */
   public Object getData()
   {
      //right now we only have link properties when linking the result page output
      //and dataset input; and link properties when linking result page/dataset
    //output to the BinaryDataset input
      if(((weAreConnectedToAnOutputDataset() || weAreConnectedToAnOutputResultPage()) &&
            weAreConnectedToAnInputDataset()))
      {
//         try
//         {
         if(super.getData() == null ||
            !(super.getData() instanceof OSRequestLinkSet))
         {
            setData(new OSRequestLinkSet());
         }
//         }
/*         catch (PSIllegalArgumentException e)
         {
            PSDlgUtil.showError(e.getMessage(), true,
                                    E2Designer.getResources().getString("OpErrorTitle"));
            e.printStackTrace();
         }*/

         setEditorName("RequestLinkPropertyDialog");
         return(super.getData());
      }

      //if we got here this is not an editable link
      return(super.getData());
   }





   private boolean weAreConnectedToAnOutputDataset()
   {
      return(connectedTo(AppFigureFactory.DATASET_ID, false)
         || connectedTo(AppFigureFactory.BINARY_RESOURCE_ID, false));
   }


   private boolean weAreConnectedToAnOutputResultPage()
   {
      return(connectedTo(AppFigureFactory.RESULT_PAGE_ID, false));
   }

   private boolean weAreConnectedToAnInputDataset()
   {
      return(connectedTo(AppFigureFactory.DATASET_ID, true)
      || connectedTo(AppFigureFactory.BINARY_RESOURCE_ID, true));
   }

   private boolean connectedTo(int id, boolean bInput)
   {
            //create our data depending on our link
      if(m_points.size() > 0)
      {

         AttachedPoint left = (AttachedPoint)m_points.get(0);
         AttachedPoint right = (AttachedPoint)m_points.get(m_points.size() - 1);

         UIConnectionPoint leftPoint = left.getConnectionPoint();
         UIConnectionPoint rightPoint = right.getConnectionPoint();
         if(leftPoint != null && rightPoint != null)
         {
            UIConnectableFigure leftOwner = leftPoint.getOwner();
            UIConnectableFigure rightOwner = rightPoint.getOwner();
            if(leftOwner != null && rightOwner != null)
            {
               if(leftPoint instanceof UIFlexibleConnectionPoint &&
                  rightPoint instanceof UIFlexibleConnectionPoint)
               {
                  UIFlexibleConnectionPoint leftFlex = (UIFlexibleConnectionPoint)leftPoint;
                  UIFlexibleConnectionPoint rightFlex = (UIFlexibleConnectionPoint)rightPoint;

                  //right now we only have link properties when linking the result page output
                  //and dataset input
                  if((leftOwner.getId() == id && leftFlex.isAnInput() == bInput ||
                     rightOwner.getId() == id && rightFlex.isAnInput() == bInput))
                  {
                     return(true);
                  }
               }
            }
         }
      }

      return(false);
   }
   /**
    * Walks thru the list of line segment endpoints and creates the appropriate
    * resize handles, placing a handle object for each handle. If there are N
    * line segments, a resize handle will be created for each line segment where
    * N > 2. In addition, a resize handle will be created for each end point of
    * the entire poly line. </p>
    * Endpoint resizers have no constraints. Intermediate line segment resizers
    * are constrained to move perpendicular to the direction of the line segment.
    * This method should be called whenever a poly-line segment end-point changes.
    */
   protected void createHandles()
   {
      try
      {
         m_handles.clear();
//         System.out.println("Creating handles...");
         // there are always 2 END_POINT_TYPE handles
         int index = 0;
         int points = m_points.size();
         m_handles.addElement(
               createHandle((Point) m_points.get(index++), null));
         if (points > 3)
         {
            // create segment move handles
            Point begin = new Point((Point) m_points.get( index++ ));
            while (index < (points - 1))
            {
               Point end = (Point) m_points.get( index++ );
               m_handles.addElement(createHandle( begin, end ));
               begin.setLocation( end );
            }
         }
         else if (3 == points)
         {
            /* there are 2 line segments but no move handles, skip the middle
               point */
            index++;
         }
         // create the 2nd endpoint handle
         m_handles.addElement(
               createHandle((Point) m_points.get(index++), null));

         Debug.assertTrue(index >= points-1, E2Designer.getResources(), 
               "UnprocessedPoints", null );
      }
      catch (ClassCastException e)
      {
         final String [] astrParams =
         {
            "Point"
         };
         Debug.assertTrue( false, E2Designer.getResources(), "UnexpectedType",
               astrParams );
      }
   }


   /**
    * The connector is just a conduit between 2 figures. Whenever one end of 
    * the connector wants to connect, it acts as a proxy for the other end.
    * It makes it appear that the figure being connected is the figure attached
    * to the other end. If no figure is attached, then it looks like a connector.
    *
    * @param loc a point used to determine which end should act as proxy
    *
    * @returns the id for the figure on the opposite end, or the id of the
    * connector if no figure is attached to the other end
    */
   public int getId(Point loc)
   {
      int index = getHitHandleIndex(loc);
      if (-1 == index)
         return (getId());

      AttachedPoint last;
      if (0 == index)
      {
         last = (AttachedPoint) m_points.get(m_points.size()-1);
      }
      else
      {
         last = (AttachedPoint) m_points.get(0);
      }
      
      int id;
      if (null != last.getConnectionPoint())
         id = last.getConnectionPoint().getOwner().getId();
      else
         id = getId();
      return (id);
   }

   /**
   * This function returns the other end given a connection point
   */
   public UIConnectionPoint getOtherEnd(UIConnectionPoint cp)
   {
      AttachedPoint apt = (AttachedPoint)m_points.get(m_points.size()-1);
      if(apt.getConnectionPoint() != null && apt.getConnectionPoint() != cp)
         return(apt.getConnectionPoint());

      apt = (AttachedPoint)m_points.get(0);
      if(apt.getConnectionPoint() != null && apt.getConnectionPoint() != cp)
         return(apt.getConnectionPoint());


      return(null);
   }

   public List<IConnectionConstraint> getConnectionConstraint()
   {
//      System.out.println("getConnectionConstraint in UIConnector.");

      List<IConnectionConstraint> constraint = null;
      if ( isAttached())
      {
         if ( 1 == m_dynamicConnections.size())
            constraint = ((UIConnectionPoint) m_dynamicConnections.get( 0 )).getAttacherConstraints();
         else if ( null != m_currentDragHandler )
         {
//            System.out.println( "checking for proper end" );
        
            /* we must check the hitPoint for null to avoid an exception as
             * noted in bug id CLOR-4B7VRT. hitPoint will be null if the
             * mousePressed action was not called. This often happens when
             * a drag is started. As such, we must not make assumptions
             * based on hitPoint being set.
             */
            Point hitPoint = m_currentDragHandler.getHitPoint();
            Debug.assertTrue( null != hitPoint, E2Designer.getResources().getString("EndPtNotFd"));

            int hitPointIndex
               = (hitPoint == null) ? 0 : getHitHandleIndex( hitPoint );
            if ( 0 == hitPointIndex )
               constraint = ((AttachedPoint) m_points.get( m_points.size() - 1 ))
                     .getConnectionPoint().getAttacherConstraints();
            else if ( m_points.size() == hitPointIndex )
            {
               constraint = ((AttachedPoint) m_points.get( 0 )).getConnectionPoint()
                     .getAttacherConstraints();
            }
         }
      }
   
//      if(constraint == null)
//         System.out.println("uiconnector constraints are null");

      return constraint;
   }

   /**
    * @returns <code>true</code> if the entire poly segment is contained within
    * the passed rect. Contained means inside or on the supplied rect.
    */
   public boolean isContainedBy( Rectangle rect )
   {
      // calculate the bounding box of the all the line segments
      int xmin = 0;
      int xmax = 0;
      int ymin = 0;
      int ymax = 0;
      for ( int index = m_points.size()-1; index >= 0; index-- )
      {
         Point pt = (Point) m_points.get( index );
         if ( pt.x < xmin )
            xmin = pt.x;
         else if ( pt.x > xmax )
            xmax = pt.x;
         if ( pt.y < ymin )
            ymin = pt.y;
         else if ( pt.y > ymax )
            ymax = pt.y;
      }
      Rectangle bounding = new Rectangle( xmin, ymin, xmax - xmin, ymax - ymin );
//      System.out.println( "Is contained by (connector): " + rect.toString() + ": "
//            + bounding.toString());
      return rect.contains( bounding );
   }


   /**
    * Creates a new handle of the correct type. If 1 point is passed in, an
    * endpoint (resizable) handle is created. If 2 points are passed in, a
    * midpoint (movable) handle is created.
    *
    * @param start one end of the line segment, or the endpoint
    *
    * @param end other end of the line segment for midpoint handles, or null
    * for endpoint handles
    *
    * @returns a new handle object of the correct type
    *
    * @throws IllegalArgumentException if start is null
    */
   private Handle createHandle(Point start, Point end)
   {
      if (null == start)
      {
         final Object[] astrParams =
         {
            "Point"
         };
         throw new IllegalArgumentException( MessageFormat.format( 
               E2Designer.getResources().getString( "CantBeNull" ), astrParams ));
      }

      Handle h = null;
      int Offset = HANDLE_SIZE / 2;
      if (null == end)
      {
         Point endpointLocation = new Point(start);
         endpointLocation.translate(-Offset, -Offset);
         h = new Handle(endpointLocation, END_POINT_TYPE);
      }
      else
      {
         Point handleLocation = new Point(
            (end.x - start.x)/2 + start.x - Offset,
            (end.y - start.y)/2 + start.y - Offset );
         h = new Handle(handleLocation, SEGMENT_TYPE);
      }
//      System.out.println("Handle: " + h.toString());
      return (h);
   }

   /**
    * If a line segment is shorter than this value, the bounding box will be 
    * longer than the segment, otherwise the bounding box is the same len as
    * the segment it surrounds. The value is in pixels.
    */
   private static final int MIN_SEGMENT_LEN = 5;
   /**
    * If a line segment is shorter than MIN_SEGMENT_LEN, the bounding box
    * will be extended beyond the segment on both ends by this amount. The
    * value is in pixels.
    */
   private static final int SEGMENT_BOUNDS_OFFSET = 2;

   /**
    * Resizes the container component based on the poly line, resize handles,
    * and extra area around the line segments to improve clickability. This
    * method should be called whenever the handles are created. </p>
    * To determine the size, rectangles are created for each line segment,
    * adding in the extra. Then, all segment rects and handle rects are
    * unioned to find the new size. 
    * </p>
    * The passed in params are used to determine the new location of the
    * component.
    *
    * @param oldPoints the m_points array before any points were modified
    *
    * @param originalPoint the location of the endpoint of the polyline before
    * it was moved, in component coords.
    *
    * @param newEndPoint location of the endpoint after the move, in the coord
    * system of comp.
    *
    * @see #createHandles
    */
   protected void adjustBounds( @SuppressWarnings("unused") Vector oldPoints ) //, Point originalPoint, Point newEndPoint)
   {

      int index = 0;
      int pointCt = m_points.size();
      Point startPt = new Point((Point) m_points.get(index++));
      Rectangle unionRect = new Rectangle(startPt);
//      System.out.println("Start pt = " + startPt.toString());

      /* if we have a vertical or horizontal match -- that is, all points
       * in a straight vertical or horizontal line, we can throw out all
       * the points other than the start and end point. This is an
       * important step to perform. If we have a vertical or horizontal
       * line, we will end up with an invalid boundRect which will cause
       * errors as seen in bug id CLOR-4B7VRT
       */
      boolean horizontalMatch = true;
      boolean verticalMatch = true;
      while (index < pointCt)
      {
         Point endPt = (Point) m_points.get(index++);

         if (horizontalMatch && (startPt.x != endPt.x))
            horizontalMatch = false;

         if (verticalMatch && (startPt.y != endPt.y))
            verticalMatch = false;

//         System.out.println("Next pt = " + endPt.toString());
         Rectangle segmentBounds = getBoundingRect(index-2);
         unionRect = unionRect.union(segmentBounds);
         startPt.setLocation(endPt);
      }

      if (horizontalMatch || verticalMatch)
      {   // if we have either/both a horizontal or vertical line, throw out
         // the other points now
         for ( ; pointCt > 2; pointCt--)
         {
            m_points.remove(pointCt - 2);
         }
      }

      // now union in all the handles
      int handleCt = m_handles.size();
      if (pointCt == 2)
      {   // make sure we also have only two handles
         for ( ; handleCt > 2; handleCt--)
         {
            m_handles.remove(handleCt - 2);
         }
      }

      index = 0;            
      while (index < handleCt)
      {
         Handle h = (Handle) m_handles.get(index++);
         unionRect = unionRect.union(h);
//         System.out.println("Handle Union rect: " + unionRect.toString());
      }

//      System.out.println("connector bounds: " + unionRect.toString());
      // how much to move the points to get them properly placed in the comp
      int xTranslation = 0;
      int yTranslation = 0;
      // do we need to translate the polyline points?

      // Find which endpoint is the original
//      boolean bFirst = false;   // true if point(0) is the point that was moved
//      System.out.println("Point(0) = " + ((Point) m_points.get(0)).toString());
//      System.out.println("Point(size-1) = " + ((Point) m_points.get(m_points.size()-1)).toString());
//      System.out.println("new Point = " + newEndPoint.toString());
      xTranslation = -unionRect.x;
      yTranslation = -unionRect.y;

//      System.out.println("Translating by " + Integer.toString(xTranslation) + " " + Integer.toString(yTranslation));

      translate(xTranslation, yTranslation);

      Point loc = getLocation();
//      System.out.println("Location before translation = " + loc.toString());
      loc.translate( unionRect.x, unionRect.y );
      setLocation(loc);
      setSize(new Dimension(unionRect.width, unionRect.height));
   }


   /**
    * @returns the bounding rectangle for the segmentIndex'th line segment. This
    * includes the line itself, plus some extra to make it more clickable for
    * dragging.
    * 
    * @param segmentIndex If there are N points, segmentIndex must range between
    * 0 and N-2.
    *
    * @throws ArrayIndexOutOfBounds if segmentIndex doesn't specify a valid
    * index for a segment
    */
   private Rectangle getBoundingRect(int segmentIndex)
   {
      if (segmentIndex < 0 || (segmentIndex > m_points.size()-2))
         throw new ArrayIndexOutOfBoundsException( 
               E2Designer.getResources().getString( "BadSegIndex" ));
      Point startPt = (Point) m_points.get(segmentIndex);
      Point endPt = (Point) m_points.get(segmentIndex+1);

      /* what we'd like to do here is give the user a bit of breathing room
       * in their clickable area. We'll look at the line to see if it's
       * horizontal or vertical (it can't be diagonal at this time). From
       * this, we decide to give them a width of 5 pixels to click on when
       * using a vertical line. We give them a height of 5 pixels when
       * using a horizontal line. If the line's too short (under 5 pixels)
       * we'll also try to adjust the clickable area by moving it up
       * and to the left for vertical lines and ending it down and to the
       * right (this effectively puts a box around the line segment that's
       * two pixels in each direction). A similar action is taken for
       * horizontal lines.
       * This fix is related to bug id: CLOR-4B7VRT
       */
      boolean bHorizontal = startPt.y == endPt.y;
      int xDelta, yDelta;
      Point ul = new Point();
      int width = 0;
      int height = 0;
      if (bHorizontal)
      {
         // we always want to extend the height for horizontal lines
         yDelta = SEGMENT_BOUNDS_OFFSET;
         height = (2 * yDelta) + LINE_WIDTH;

         // for the width, we will make it at least 5 pixels, or keep
         // it as is
         xDelta = ((int)java.lang.Math.abs((double) (startPt.x - endPt.x)))
            < MIN_SEGMENT_LEN ? SEGMENT_BOUNDS_OFFSET : 0;
         width = java.lang.Math.max( ((startPt.x < endPt.x)
            ? endPt.x - startPt.x : startPt.x - endPt.x), MIN_SEGMENT_LEN);

         ul.setLocation((startPt.x < endPt.x) ? startPt : endPt);
      }
      else
      {
         // we always want to extend the width for vertical lines
         xDelta = SEGMENT_BOUNDS_OFFSET;
         width = (2 * xDelta) + LINE_WIDTH;

         // for the height, we will make it at least 5 pixels, or keep
         // it as is
         yDelta = ((int)java.lang.Math.abs((double)(startPt.y - endPt.y))) 
            < MIN_SEGMENT_LEN ? SEGMENT_BOUNDS_OFFSET : 0;
         height = java.lang.Math.max( ((startPt.y < endPt.y)
            ? endPt.y - startPt.y : startPt.y - endPt.y), MIN_SEGMENT_LEN);

         ul.setLocation((startPt.y < endPt.y) ? startPt : endPt);
      }

      // now shift by the delta amount
      ul.translate( -xDelta, -yDelta );

      return new Rectangle(ul.x-5, ul.y-5, width+10, height+10);
   }

   /**
    * @param pt the location to check against all handles for a hit, in
    * component coordinates.
    *
    * @returns a reference to the handle that contains the supplied point. If no
    * handles contain the point, null is returned.
    */
   private Handle getHitHandle(Point pt)
   {
      int index = getHitHandleIndex(pt);
//      System.out.println( "index for hit handle = " + Integer.toString( index ));
      return ( -1 != index ? (Handle) m_handles.get(index) : null );
   }

   /**
    * @param pt the location to check against all handles for a hit, in
    * component coordinates.
    *
    * @returns the index of the handle that contains the supplied point. If no 
    * handles contain the point, -1 is returned. Indexes start at 0.
    *
    * @throws IllegalArgumentException if pt is null
    */
   protected int getHitHandleIndex(Point pt)
   {
      if ( null == pt )
         throw new IllegalArgumentException( 
               E2Designer.getResources().getString( "CantBeNull" ));

      Handle h = null;
      boolean bFound = false;
      int index = m_handles.size() - 1;
      for (; index >= 0 && !bFound; index--)
      {
         h = (Handle) m_handles.get(index);
         if (h.contains(pt))
            bFound = true;
      }
      return(bFound ? index+1 : -1);
   }

   /**
    * @param h a current handle on the polyline
    *
    * @returns a point associated with the supplied handle. If h is not equal
    * to a current handle, an exception is thrown.
    *
    * @throws IllegalArgumentException if a point can't be found for the handle
    */
   private Point getPointForHandle(Handle h)
   {
//      System.out.println( "Handle = " + h.toString());
      int size = m_points.size();

      Point pt = (Point) m_points.get( 0 );
      if ( h.contains( pt ))
         return pt;

      pt = (Point) m_points.get( size - 1 );
      if ( h.contains( pt ))
         return pt;

      // is it a midpoint? Take center of handle and see if it is on the line
      pt = new Point( h.getLocation());
      pt.translate( h.width/2, h.height/2 );
      boolean bFound = false;
      int segmentIndex = size - 3;
      for (; segmentIndex >= 1 && !bFound; segmentIndex--)
      {
//         System.out.println("Checking point " + pt.x + "," + pt.y);
//         Rectangle r = getBoundingRect(segmentIndex);
//         System.out.println("  boundrect = " + r.x + "," + r.y + "," + r.width + "," + r.height);

         bFound = getBoundingRect(segmentIndex).contains(pt);
      }
      if ( !bFound )
      {
         throw new IllegalArgumentException(
               E2Designer.getResources().getString( "PtNotFoundForHandle" ));
      }
      else
      {
         // get the exact value for the location
         Point pt1 = (Point) m_points.get( segmentIndex + 1 );
         Point pt2 = (Point) m_points.get(segmentIndex + 2 );
         if ( pt1.x == pt2.x )
         {
            pt.x = pt1.x;
         }
         else
         {
            Debug.assertTrue( pt1.y == pt2.y, E2Designer.getResources(),
                  "InvalidPointForLine", null );
            pt.y = pt1.y;
         }
      }
      return pt;
   }

   /**
    * Shifts all points in the polyline and all move/resize handles by the
    * passed in amounts. Values are in pixels.
    *                           s
    * @param x amount to shift left/right. Positive values cause right shifts.
    *
    * @param y amount to shift up/down. Positive values cause down shifts.
    */
   private void translate(int x, int y)
   {
      int index = m_points.size() - 1;
      for (; index >= 0; index--)
      {
         ((Point) m_points.get(index)).translate(x, y);
      }
      for (index = m_handles.size() - 1; index >= 0; index--)
      {
         ((Handle) m_handles.get(index)).translate(x, y);
      }
   }


   /**
    * Constants used by setEndpoint() to indicate where the attachment is.
    * If AP_ANY is chosen, the poly line generator is free to choose the
    * most appropriate one. AP_FIRST and AP_LAST can be used for validating
    * params.
    */
   private static final int AP_FIRST   = 1;
   private static final int AP_TOP    = 1;
   private static final int AP_BOTTOM    = 2;
   private static final int AP_LEFT    = 3;
   private static final int AP_RIGHT    = 4;
   private static final int AP_ANY      = 5;   
   private static final int AP_NONE   = 6;
   private static final int AP_LAST   = 6;

   /**
    * When drawing a poly line that has one or more ends attached, we require
    * that the segment emanating from the attached end(s) to have a minimum 
    * length. This value specifies that len, in pixels. The value is chosen
    * based on looks only.
    */
   private static final int ATTACHED_SEGMENT_MIN_LEN = 5;

   /**
    * Fixes up the segment endpoints to maintain the contstraint that all lines
    * are either horizontal or vertical and that the line coming out of the
    * connected figure is perpendicular to the figure.
    * </p>
    * The original point is used to determine which endpoint was moved.
    *
    * @param original the endpoint before it was modified, in component coords.
    * This must be a point currently on the polyline.
    *
    * @param modified the location of the original endpoint after it was moved,
    * in component coords
    *
    * @param attachPos one of the AP_... constants, indicating which side of
    * the figure the connector is attached to. If not attached to anything,
    * use AP_NONE.
    *
    * @param cp the new connection point that this endpoint is attaching to,
    * or null if not attaching
    *
    * @throws IllegalArgumentException if original is not an existing endpoint.
    */
   private void setEndpoint(Point original, Point modified, int attachPos,
         UIConnectionPoint cp)
   {
      int indexOfOther;
      if (((Point) m_points.get(0)).equals(original))
      {
         indexOfOther = m_points.size() - 1;
      }
      else if (((Point) m_points.get(m_points.size()-1)).equals(original))
      {
         indexOfOther = 0;
      }
      else
      {
         throw new IllegalArgumentException( 
               E2Designer.getResources().getString( "EndPtNotFd" ));
      }

      AttachedPoint other = (AttachedPoint) m_points.get(indexOfOther);
      AttachedPoint left, right;
      if (other.x < modified.x)
      {
         left = other;
         right = new AttachedPoint( modified, attachPos, cp );
      }
      else
      {
         left = new AttachedPoint( modified, attachPos, cp );
         right = other;
      }

      m_points.clear();

      // variables used to add midpoints later on
      int index = 0;
      int delta = 1;
      int lastIndex = 0;
      boolean bSimple = false;
      Vector<Point> midPoints = new Vector<Point>(5);
      int transformType = ROTATION_0;
      AttachedPoint transformOrigin = null;

//      System.out.println( "right = " + right.toString());
//      System.out.println( "left = " + left.toString());

      if (AP_NONE == left.getAttachPos() && AP_NONE == right.getAttachPos())
      {
         // draw from left to right
         bSimple = true;
      }
      else if (AP_NONE == left.getAttachPos() || AP_NONE == right.getAttachPos())
      {
         /* Draw from attached point to other point. The idea here is to 
            transform the point so it is AP_RIGHT, build the connection,
            then transform all points back to the original attach pos and
            location.
            When building the poly line, we require a minimum segment len 
            for the line emanating from the attachment. If the other end
            is to the right of this point, we just create a two segment
            line (one if the other pt is straight over). If the other end
            pt is to the left of the minimum segment, we create a 3 segment
            line. */
//         System.out.println("Creating line for one attachment");


         if ( AP_NONE == left.getAttachPos())
         {
            if ( AP_ANY == right.getAttachPos())
            {
               // we can choose the attach position best for us
               bSimple = true;
            }
            else
            {
               transformType = getTransformType( right );
               transformOrigin = right;
               midPoints = createMidPointsForOneEndAttached( right, left );
               index = midPoints.size()-1;
               delta = -1;
               lastIndex = -1;
            }
         }
         else
         {
            if ( AP_ANY == left.getAttachPos())
            {
               // we can choose the attach position best for us
               bSimple = true;
            }
            else
            {
               transformType = getTransformType( left );
               transformOrigin = left;
               midPoints = createMidPointsForOneEndAttached( left, right );
               index = 0;
               delta = 1;
               lastIndex = midPoints.size();
            }
         }

      }
      else
      {
         /* There are many possible cases for this scenario. The plan here is
            to break it up into 3 major categories based on the attachment direction.
            Within each category, we only need to look at the cases for left point
            being AP_RIGHT. All other directions are handled with transforms.
            The major cases are:
               1. Attachment points point in opposite directions
               2. Attachment points point in same direction
               3. Attachment points are 90 degrees relative to each other
            Each of these cases has 1 or more sub cases. At this point in time, I 
            am only implementing case 1. The other cases don't apply to V1 of the
            program.
            Case 1 has 2 sub cases:
               A. Pointing at each other
               B. Pointing away from each other
            Because of the min segment len, there are 2 slightly different cases
            that occur when there is overlap in the min segment. Both of these are
            being handled under case B.
            For case A, we create a 3 segment line.
            For case B, we create a 5 segement line.
         */
//         System.out.println("Creating line for two attachments");

         transformType = getTransformType( left );
         transformOrigin = left;
//         System.out.println( "transformtype = " + Integer.toString( transformType ));
         Point transformedEnd = transformPoint( transformType, right, transformOrigin );

         int deltaX = 0;
         int rpos = right.getAttachPos();
         if (    ( AP_TOP == rpos && ROTATION_270 == transformType )
               || ( AP_BOTTOM == rpos && transformType == ROTATION_90 ) 
               || ( AP_LEFT == rpos && ROTATION_0 == transformType )
               || ( AP_RIGHT == rpos && ROTATION_180 == transformType ))
         {
            deltaX = -ATTACHED_SEGMENT_MIN_LEN;
         }
         else if (( AP_TOP == rpos && ROTATION_90 == transformType )
               || ( AP_BOTTOM == rpos && transformType == ROTATION_270 ) 
               || ( AP_LEFT == rpos && ROTATION_180 == transformType )
               || ( AP_RIGHT == rpos && ROTATION_0 == transformType ))
         {
            deltaX = ATTACHED_SEGMENT_MIN_LEN;
         }
         int minX = transformedEnd.x + deltaX;

//         System.out.println( "transformedEnd = " + transformedEnd.toString());

         int lpos = left.getAttachPos();
         if (!(( AP_LEFT == lpos && AP_RIGHT == rpos )
               || ( AP_LEFT == rpos && AP_RIGHT == lpos )
               || ( AP_TOP == lpos && AP_BOTTOM == rpos ) 
               || ( AP_TOP == rpos && AP_BOTTOM == lpos )))
         {
            Debug.assertTrue( false, E2Designer.getResources(), "ConnNotSupp", null );
            bSimple = true;
            
         }
         else if ( left.x + ATTACHED_SEGMENT_MIN_LEN < minX )
         {
            // case 1A
            int midX = left.x + ( transformedEnd.x - left.x ) / 2;
            midPoints.add( new Point( midX, left.y ));
            midPoints.add( new Point( midX, transformedEnd.y ));
         }
         else
         {
            // case 1B
            Point p = new Point( left );
            p.translate( ATTACHED_SEGMENT_MIN_LEN, 0 );
            midPoints.add( new Point( p ));
            p.translate( 0, ( transformedEnd.y - left.y ) / 2 );
            midPoints.add( new Point( p ));
            p = new Point( transformedEnd.x - ATTACHED_SEGMENT_MIN_LEN, p.y );
            midPoints.add( new Point( p ));
            midPoints.add( new Point( p.x, transformedEnd.y ));
         }

         // set up indexes and limits for adding points later
         index = 0;
         delta = 1;
         lastIndex = midPoints.size();
      }

      //add new points to vector
      if ( bSimple )
      {
         // we can connect the lines any way we want
         m_points.add( left );
         m_points.add( new Point( right.x, left.y ));
         m_points.add( right );
      }
      else
      {
         m_points.add( left );
         Debug.assertTrue( null != midPoints, E2Designer.getResources(), "MPNotInit", null );

         int undoTransform = transformToUndo( transformType );
         for ( ; index != lastIndex; index+=delta )
         {
            Point mid = (Point) midPoints.get(index);
//            System.out.println("mid before transform = " + mid.toString());
            mid = transformPoint( undoTransform, mid, transformOrigin );
//            System.out.println("mid after transform = " + mid.toString());
            m_points.add( mid );
         }
         m_points.add( right );
      }
   }

   /**
    * Creates the midpoints for a polyline with either but not both ends attached.
    * The midpoints are returned in a vector.
    *
    * @param attached the endpoint that is attached
    *
    * @param unAttached the endpoint that is not attached to another figure
    */
   private Vector<Point> createMidPointsForOneEndAttached( AttachedPoint attached, 
         AttachedPoint unAttached )
   {
      int transformType = getTransformType( attached );

//      System.out.println( "attached = " + attached.toString());
//      System.out.println("POS_ = " + Integer.toString(attached.getConnectionPoint().getPreferredAttachPosition())
//            + "\nattach pos = " + Integer.toString( attached.getAttachPos())
//            + "\ntransform type = " + Integer.toString( transformType ));

//      System.out.println("Unattached end before transform: " + unAttached.toString());
      Point end = transformPoint( transformType, unAttached, attached );
//      System.out.println("Unattached end after transform: " + end.toString());

      Vector<Point> midPoints = new Vector<Point>(2);
      if ( end.x >= attached.x + ATTACHED_SEGMENT_MIN_LEN )
      {
         // 2 segment poly line w/ 90 deg angle
         midPoints.add( new Point(end.x, attached.y ));
      }
      else if ( end.y == 0 )
      {
         // 2 segment poly line w/ 180 deg angle
         midPoints.add( new Point( attached.x + ATTACHED_SEGMENT_MIN_LEN, attached.y ));
      }
      else
      {
         // 3 segment poly line
         Point mid1 = new Point( attached.x + ATTACHED_SEGMENT_MIN_LEN, attached.y );
         midPoints.add( mid1 );
         midPoints.add( new Point( mid1.x, end.y ));
      }
      return midPoints;
   }



   /**
    * Point transformation methods use these constants to indicate how much
    * rotation must be applied to an AttachedPoint to make its attach pos
    * AP_RIGHT. The value returned by this method is then passed to transform 
    * standard points.
    *
    * @see #transformPoint
    */
   private static final int ROTATION_0      = 0;
   private static final int ROTATION_90   = 1;
   private static final int ROTATION_180   = 2;
   private static final int ROTATION_270   = 3;

   /**
    * @returns a value of the type ROTATION_... that indicates the transformation
    * required to make the supplied point into AP_RIGHT.
    */
   private int getTransformType( AttachedPoint cp )
   {
      int currentPos = cp.getAttachPos();
      if (( AP_ANY == currentPos ) || ( AP_NONE == currentPos ))
         return ROTATION_0;

      int transformType = ROTATION_0;
      switch ( currentPos )
      {
         case AP_TOP:
            transformType = ROTATION_90;
            break;
         case AP_LEFT:
            transformType = ROTATION_180;
            break;
         case AP_BOTTOM:
            transformType = ROTATION_270;
            break;
         default:
            Debug.assertTrue(AP_RIGHT == currentPos, E2Designer.getResources(),
                  "NewAP", null );
            break;
      }
      return transformType;
   }

   /**
    * Rotates a point by the amount specified in transformType about the supplied
    * origin point.
    *
    * @param transformType one of the ROTATION_.. types
    *
    * @param pt the location that will be transformed to a new location based
    * on the supplied transform
    *
    * @param origin pt about which src is rotated
    *
    * @returns a new point that is the supplied pt transformed based on the
    * supplied transform type. The supplied point is unchanged.
    */
   private Point transformPoint( int transformType, Point src, Point origin )
   {
      // src pt translated to 0, 0, prepared for rotation
      Point translated = new Point( src );
      translated.translate( -origin.x, -origin.y );

      int x = translated.x;
      int y = translated.y;
      switch ( transformType )
      {
         case ROTATION_90:
            x = -translated.y;
            y = translated.x;
            break;

         case ROTATION_180:
            x = -translated.x;
            y = -translated.y;
            break;

         case ROTATION_270:
            x = translated.y;
            y = -translated.x;
            break;

         default:
         {
            final String [] astrParams =
            {
               Integer.toString( transformType )
            };
            Debug.assertTrue( ROTATION_0 == transformType, E2Designer.getResources(),
                  "InvTransformType", astrParams );
            break;
         }
      }
      // new point, rotated by requested amount
      Point rotated = new Point( x, y );
      rotated.translate( origin.x, origin.y );
      return rotated;
   }

   /**
    * @param transformType one of the ROTATION_... types returned by the 
    * attached point transform method
    *
    * @returns a ROTATION_... type that can be used to restore a point that
    * was previously transformed using the passed in transform type
    */
   private int transformToUndo( int transformType )
   {
      int undoTransformType = ROTATION_0;
      switch ( transformType )
      {
         case ROTATION_90:
            undoTransformType = ROTATION_270;
            break;

         case ROTATION_180:
            undoTransformType = ROTATION_180;
            break;

         case ROTATION_270:
            undoTransformType = ROTATION_90;
            break;

         default:
         {
            final String [] astrParams = 
            {
               Integer.toString( transformType )
            };
            Debug.assertTrue( ROTATION_0 == transformType, E2Designer.getResources(),
                  "InvTransformType", astrParams );
            break;
         }
      }
      return undoTransformType;
   }


   /**
    * A small class that extends rectangle, adding a flag to indicate the type
    * of handle, either a segment handle or a poly-line end-point handle. Only
    * the location of the rect is supplied since handles have a pre-defined
    * size. In addition, this object can draw itself.
    */

      /**
       * These are constants defining the types of allowed handles. One of
       * these must be passed into the constructor.
       */
      public static final int FIRST_TYPE       = 1;
      public static final int END_POINT_TYPE    = 1;
      public static final int SEGMENT_TYPE    = 2;
      public static final int LAST_TYPE       = 2;
      
      /**
       * The width and height of a resize handle, in pixels. It is chosen so
       * that the handle can be drawn symmetrically relative to the line
       * segment.
       */
      public static final int HANDLE_SIZE = LINE_WIDTH + 6;

   public class Handle extends Rectangle
   {
      /**
       * Copy constructor
       */
      public Handle(final Handle handle)
      {
         super(handle);
         m_type = handle.m_type;
         m_color = handle.m_color;
      }

      /**
       * Creates a new handle of the requested type. END_POINT_TYPE handles are
       * drawn at both ends of the entire poly-line. SEGMENT_TYPE handles are
       * drawn on all line segments that don't have an END_POINT_TYPE handle
       * already present.
       *
       * @param pt the location of the upper left hand corner of the handle
       *
       * @param Type one of the ..._TYPE constants defined above
       *
       * @throws IllegalArgumentException if Type is not a valid constant
       */
      public Handle(Point pt, int type)
      {
         super(pt.x, pt.y, HANDLE_SIZE, HANDLE_SIZE);
         if (type < FIRST_TYPE && type > LAST_TYPE)
         {
            final Object[] astrParams = 
            {
               Integer.toString(type)
            };
            throw new IllegalArgumentException( MessageFormat.format(
                  E2Designer.getResources().getString( "InvalidHandleType" ), astrParams )); 
         }
         m_type = type;
      }

      public int getType()
      {
         return (m_type);
      }

      public void setColor(Color c)
      {
         m_color = c;
      }

      public Color getColor()
      {
         return (m_color);
      }

      /**
       * @returns the point that is the center of this handle, in the same
       * coord system as the location of the handle
       */
      public Point getCenterLocation()
      {
         Point center = new Point(getLocation());
         center.translate(HANDLE_SIZE/2, HANDLE_SIZE/2);
         return(center);
      }

      /**
       * Paints this handle to the specified context. The style of the handle
       * varies with the type.
       */
      public void paint(Graphics g)
      {
         switch (m_type)
         {
            case END_POINT_TYPE:
               g.setColor( m_color );
               break;

            case SEGMENT_TYPE:
               g.setColor( Color.cyan );
               break;

            default:
               break;
         }
         g.setPaintMode();
         g.fillRect(x, y, width, height);
      }

      
      // Storage
      private int m_type = 0;
      private Color m_color = Color.blue;
   }

   // Properties
   /**
    * The connector manages all drawing itself.
    *
    * @returns <code>true</code> always
    */
   public boolean isOwnerDrawn()
   {
      return(true);
   }

   /**
    * Since this class uses flexible connections, if the master moves, we have
    * to resize ourself.
    *
    * @param cp connection point notifying us of the change
    *
    * @param offset relative change in the location of the cp
    */
   public void connectionPointLocChanged(UIConnectionPoint cp, Dimension offset )
   {
      AttachedPoint original = new AttachedPoint(getEndPointForCP(cp)); //getLocation();
      Point moved = new Point(original);
      moved.translate(offset.width, offset.height);
//      System.out.println("cur loc: " + moved.toString());
      modifyEndpoint( false, original, moved, cp );
   }
   

   /**
    * Since this object can be resized as well as moved, we don't want to go
    * into dnd mode if the user has clicked and dragged on a resize handle.
    *
    * @returns true if the point is over a resize handle, false otherwise
    */
   public boolean wantsMouse( Point pt )
   {
      return null != getHitHandle(pt);
   }

   /**
    * @returns <code>true</code> if the point is on a line segment or within a
    * certain distance (~2 pixels) of the line or over any handle.
    */
   public boolean isHit(Point pt)
   {
      int segmentIndex = m_points.size() - 2;
      boolean bFound = false;
      for (; segmentIndex >= 0 && !bFound; segmentIndex--)
      {
         bFound = getBoundingRect(segmentIndex).contains(pt);
      }
      if ( !bFound )
      {
         // try all handles
         for ( int index = m_handles.size()-1; index >= 0 && !bFound; index-- )
         {
            bFound = ((Handle) m_handles.get( index )).contains( pt );
         }
      }
//      System.out.println( "Connector isHit returning " + String.valueOf( bFound ));
      return(bFound);
   } 

   /**
    * Only returns true if the point is in a polyline endpoint, or on/near
    * a line segment, but not over a midpoint handle.
    *
    * @param pt the pt to check, in component coords
    *
    * @see #isHit
    */
   public boolean isDragPoint(Point pt)
   {
      final String [] astrParams = 
      {
         Integer.toString( m_handles.size())
      };
      Debug.assertTrue( m_handles.size() >= 2, E2Designer.getResources(), 
            "MissingHandles", astrParams );
      if ( !isHit( pt ))
         return false;
      // walk midpoint handles and see if any contain the supplied point
      boolean bFound = false;
      for ( int index = m_handles.size() - 2; index >= 1 && !bFound; index-- )
      {
         if (((Handle) m_handles.get( index )).contains( pt ))
            bFound = true;
      }
      return !bFound;
   }

   // Operations
   protected void setAttachOffset(Dimension offset)
   {
      // do nothing
   }

   /**
    * Detaches from any connections it had before the move.
    */
   public void dragEnd(boolean bCopy)
   {
      Debug.assertTrue(m_points.size() >= 2, E2Designer.getResources(), "NoPoints", null );
      if ( bCopy )
         return;
//      System.out.println( "dragEnd called with bCopy = " + String.valueOf(bCopy));
      AttachedPoint p = (AttachedPoint) m_points.get(0);
      if ( null != p.getConnectionPoint() )
         removeDynamicConnection( p.getConnectionPoint() );
      p = (AttachedPoint) m_points.get( m_points.size()-1 );
      if ( null != p.getConnectionPoint() )
         removeDynamicConnection( p.getConnectionPoint() );
   }

   /**
    * @returns the endpoint that is attached to the passed in conn. pt, or 
    * null if no end points are attached to this point
    */
   private AttachedPoint getEndPointForCP(UIConnectionPoint cp)
   {
      if ((null == m_points) || (0 == m_points.size()))
         return (null);
      AttachedPoint end = (AttachedPoint) m_points.get(0);
//      System.out.println("end is null?" + String.valueOf(null == end));
      UIConnectionPoint existingCP = end.getConnectionPoint();
      if (null != existingCP && existingCP.equals(cp))
         return (end);
      end = (AttachedPoint) m_points.get(m_points.size()-1);
      existingCP = end.getConnectionPoint();
      if (null != existingCP && existingCP.equals(cp))
         return (end);
      return (null);
   }

   /**
    * Get the location of cp, convert it to drawing pane coords, then offset
    * by the supplied offset. This becomes the new location of the endpoint.
    * If the endpoint was previously attached, it is detached. This method
    * should be called by the cp we are attaching to. This method should only
    * be called during a mouse drag operation being handled by a DragHandler.
    *
    * @param cp the connection point that we are connecting too
    *
    * @param offset # of pixels to translate the location of cp to get the 
    * desired location of this connection. Positive values are translated right
    * and down.
    */
   public void setConnectionLocation(UIConnectionPoint cp, Dimension offset)
   {
      if ( m_bProgrammatic )
      {

         Point loc = cp.getLocation();
         loc.translate(offset.width, offset.height);
//         System.out.println("cp loc = " + loc.toString());
//         System.out.println("cp loc rel to drawing pane = " + (SwingUtilities.convertPoint(cp.getParent(), loc, m_drawingPane)).toString());
         modifyEndpoint(true, (AttachedPoint) m_attachPoint,
               SwingUtilities.convertPoint(cp.getParent(), loc, this), cp);
         return;
      }
      if ( null == m_currentDragHandler || null == m_currentDragHandler.getHitPoint())
         return;
      Point loc = cp.getLocation();
      m_currentDragHandler.setConnectionCreated( true );
      loc.translate(offset.width, offset.height);
//      System.out.println("cp loc = " + loc.toString());
//      System.out.println("cp loc rel to drawing pane = " + (SwingUtilities.convertPoint(cp.getParent(), loc, m_drawingPane)).toString());
      modifyEndpoint(true, (AttachedPoint) m_currentDragHandler.getHitPoint(), 
            SwingUtilities.convertPoint(cp.getParent(), loc, this), cp);
   }


   /**
    * Generally, connectors are attached to other figures via the mouse. This 
    * method can be used to make the connection w/o using the mouse. 
    *
    * @param bFirst <code>true</code> if you want to attach to the first point in
    * the polyline, <code>false</code> to attach to the last point in the line
    */
   public boolean createDynamicConnectionProgrammatic( UIConnectionPoint cp, boolean bFirst )
   {
      m_bProgrammatic = true;
      m_attachPoint = (AttachedPoint) m_points.get( bFirst ? 0 : m_points.size()-1 );
      boolean bSuccess = super.createDynamicConnection( cp );
      m_attachPoint = null;
      m_bProgrammatic = false;
      return bSuccess;
   }

   /**
    * Call this method when the user changes the position of one of the
    * endpoints.
    *
    * @param bDetach if <code>true</code>, the the current connection point
    * attached to this endpoint will be removed.
    *
    * @param movedPoint original point, before it was moved, in connector coords
    *
    * @param loc final location of the moved endpoint, in connector coords
    *
    * @param newConnection the connection point this endpoint is attaching to,
    * or null if not attaching
    */
   void modifyEndpoint(boolean bDetach, AttachedPoint movedPoint, 
         Point loc, UIConnectionPoint newConnection)
   {
      if (bDetach)
      {
         // detach, if we were attached
         UIConnectionPoint cp = ((AttachedPoint) movedPoint).getConnectionPoint();
         if (null != cp)
            removeDynamicConnection(cp);
      }

      int attachPos = null == newConnection ? AP_NONE :
            convertPos( newConnection.getPreferredAttachPosition() );
      Vector<Point> oldPoints = new Vector<Point>( m_points );
      setEndpoint(movedPoint, loc, attachPos, newConnection); 
      createHandles();
      adjustBounds( oldPoints ); //, movedPoint, loc );
   }

   /**
    * Finds the poly segment that contains original moves its 2 endpoints to the
    * location specified by modified. If original is not a midpoint on this line,
    * an exception is thrown.
    *
    * @param original location of the point before it moved, in comp coords
    *
    * @param modified location of the point after it moved, in comp coords
    *
    * @throws IllegalArgumentException if original is not a point on the poly line
    */
   void modifyMidpoint( Point original, Point modified )
   {
      // find the point to modify and change it
      boolean bFound = false;
      int segmentIndex = m_points.size() - 3;
      Vector<Point> oldPoints = new Vector<Point>( m_points );

      for (; segmentIndex >= 1 && !bFound; segmentIndex--)
      {
         bFound = getBoundingRect(segmentIndex).contains(original);
      }
      if ( bFound )
      {
         Point pt1 = (Point) m_points.get( segmentIndex + 1 );
         Point pt2 = (Point) m_points.get(segmentIndex + 2 );
         if ( original.x == pt1.x )
         {
            pt1.x = modified.x;
            pt2.x = modified.x;
         }
         else
         {
            Debug.assertTrue( original.y == pt2.y, E2Designer.getResources(),
                  "InvalidPtForLine", null );
            pt1.y = modified.y;
            pt2.y = modified.y;
         }
      }
      else
         throw new IllegalArgumentException(
               E2Designer.getResources().getString( "OrigPtNotFd" ));

      createHandles();
      adjustBounds( oldPoints );
      /* if the component doesn't change size or location, it won't repaint, so
         force it */
      repaint();
   }

   /**
    * Converts the passed in connection point attach position to a connector
    * attach position. If the connector is on the left, the line is assumed
    * to come out to the left, similarly for the right. If the position is 
    * top or bottom center, it is assumed the line goes up or down, respectively.
    * Otherwise, AP_ANY is returned.
    *
    * @param connectionPointPos one or more of the UIConnectionPoint.POS_... flags
    *
    * @returns one of the AP_... values corresponding to the POS_.. value
    */
   private int convertPos( int connectionPointPos )
   {
      int newPos = AP_ANY;
      if ( 0 != ( UIConnectionPoint.POS_LEFT & connectionPointPos )
         && 0 == ( UIConnectionPoint.POS_RIGHT & connectionPointPos ))
      {
         newPos = AP_LEFT;
      }
      else if ( 0 != ( UIConnectionPoint.POS_RIGHT & connectionPointPos )
         && 0 == ( UIConnectionPoint.POS_LEFT & connectionPointPos ))
      {
         newPos = AP_RIGHT;
      }
      else if ( UIConnectionPoint.POS_TOP == connectionPointPos )
         newPos = AP_TOP;
      else if ( UIConnectionPoint.POS_BOTTOM == connectionPointPos )
         newPos = AP_BOTTOM;
//      System.out.println( "convertPos: POS_.. = " + Integer.toString(connectionPointPos)
//            + " AP_.. = " + Integer.toString( newPos ));
      return newPos;
   }

   /**
    * We need to remove the reference in the end point object too.
    */
   public boolean removeDynamicConnection( UIConnectionPoint cp )
   {
      boolean bSuccess = super.removeDynamicConnection( cp );
      if (bSuccess)
      {
         AttachedPoint p = (AttachedPoint) m_points.get(0);
         if ( cp == p.getConnectionPoint() )
         {
            p.clearAttachment();
         }
         else
         {
            p = (AttachedPoint) m_points.get( m_points.size()-1 );
            if ( cp == p.getConnectionPoint() )
            {
               p.clearAttachment();
            }
            else
               Debug.assertTrue( false, E2Designer.getResources(), "ConnPtNotFd", null );
         }
      }
    repaint();
      return (bSuccess);
   }

   /**
    * Instead of displaying an image, we draw the connector between its two
    * endpoints (using the control points).
    */
   public void paint( Graphics g )
   {
      super.paint(g);
      paintLine(g, m_points, m_handles);
   }
 /**
 *draw the arrow at the center location provided ( the center is calculate from
 * to be: horz line = (x-x')/2+x, vert line = (y-y')/2+y
 *
 *@param g the graphic content
 *
 *@param startPt the starting point
 *
 *@param endPt   the ending point
 *
 *@param isOuputConnector if <code> true </code> the source ( startPt )is from
 * an output connector <code> false </code> is an input connector
 *
 *
 */
  private void drawArrow(Graphics g,Point startPt,Point endPt,boolean isOuputConnector)
  {
     int hCenter,yCenter;


     Point p1=null;
     Point p2=null;
      // set our minimun distance
     int limit=MINIMUM_DISTANCE;

     // is a horizontal line?
     if( startPt.y == endPt.y )
     {
       boolean bRightToLeft=true;

       // if our points are too close return
       if( endPt.x-startPt.x < limit)
       {
           return;
       }

       // always start from left to right
       // so see if we need to normalize the points
       if( startPt.x >endPt.x )
       {
          p1=new Point(endPt);
          p2=new Point(startPt);
       }
       else if( endPt.x > startPt.x )
       {
          p1=new Point(startPt);
          p2=new Point(endPt);
          // this indicates that we normalized the points
          bRightToLeft=false;
       }

       // our center point
       hCenter=((p2.x-p1.x)/2)+p1.x;
       // our vertical center
       yCenter=p1.y;

       boolean  bDrawLeftArrow=false;

       // if is an left ( output are ) and we did not normalized the point
       if( isOuputConnector && bRightToLeft == false)
       {
              bDrawLeftArrow=true;  // draw a left arrow
       }
       // if is an input connector, but we normalized the point invert it
       if( isOuputConnector == false && bRightToLeft )
       {
                bDrawLeftArrow=true;
       }
       // create the point
       Point point=new Point(hCenter,yCenter);
       // and draw it
       drawArrowAt(g,point,true,bDrawLeftArrow);

    }//  end of horizontal line
    else // is a vertical line
    {
       boolean bGoingUp=false;
       // if is a normalized line
       if( startPt.y < endPt.y )
       {
          p1=new Point(startPt);
          p2=new Point(endPt);
       }
       else
       {
          //normalize
          p1=new Point(endPt);
          p2=new Point(startPt);
          bGoingUp=true; // and remember it
       }
       // see if this line is greater than our limit
       if( p2.y-p1.y < limit )
       {
           return;
       }

       hCenter=p1.x;
       yCenter=0;

       yCenter=((p2.y-p1.y)/2)+p1.y;
       boolean bDrawArrowUP=false;
       // if drawing an output (at up arrow) and no normalized make up arrow
       if( isOuputConnector && bGoingUp == false )
       {
                bDrawArrowUP=true;
       }
       // if is an input ( at right arrow ), but normalized line
       if( isOuputConnector== false && bGoingUp )
       {
                bDrawArrowUP=true; // make an up arrow
       }
       // make the point and draw the arrow
       Point point=new Point(hCenter,yCenter);
       drawArrowAt(g,point,false,bDrawArrowUP);
    }
 }

 /**
 *@param index the connector index from the point array
 *
 *@return the  connector at the point or <code>null </code> if not found, or
 * point is not an instanceof UIConnector.AttachedPoint
 *
 */
 private UIConnectionPoint getConnectorAt(int index)
 {
    UIConnectionPoint point=null;
    if( index >=0 && index < m_points.size() )
    {
        Object obj=m_points.get(index);
        if( obj instanceof UIConnector.AttachedPoint)
        {
             AttachedPoint apt = (AttachedPoint)obj;
               if(apt != null)
            {
                  point=apt.getConnectionPoint();
            }
        }
    }
    return(point);
 }
 /**
 *draw the arrow
 *
 *@param g graphic
 *
 *@param centerPoint the origin to draw the arrow
 *
 *@param bHorizontalLine if <code> true </code> is a horizontal line, else
 * is a vertical line
 *
 *@param bDir  if bHorizontalLine is <code> true </code> and bDir is <code> true
 * </code>  draw an left arrow, else if bDir is <code> false </code> draw a right
 *arrow. if bHorizontalLine is <code> false</code> and bDir <code> true </code>
 *draw an up arrow, if bDir <code> false </code> draw an down arrow
 *
 *
 */

 private void drawArrowAt(Graphics g,Point centerPoint,boolean bHorizontalLine,boolean bDir)
 {
    Polygon points=new Polygon(); // our points

    Point newPoint=new Point(centerPoint);
    if( bHorizontalLine ) // horizontal line
    {
       if( bDir  ) // if left
       {
          points.addPoint(newPoint.x,newPoint.y);
               points.addPoint(newPoint.x,newPoint.y+ARROW_SIZE);
               /* please note the +1 this is because the Java pen overhangs at
               the right and  bottom of the position, so the plus one moves
               the upper side ( in this case ) one more pixel, so the result
               is an normalized arrow
               */
               points.addPoint(newPoint.x-(ARROW_SIZE+1),newPoint.y);
               points.addPoint(newPoint.x,newPoint.y-ARROW_SIZE);
          points.addPoint(newPoint.x,newPoint.y);

       }
       else  // else is a right arrow
       {
            points.addPoint(newPoint.x,newPoint.y);
                 points.addPoint(newPoint.x,newPoint.y+ARROW_SIZE);
                 points.addPoint(newPoint.x+(ARROW_SIZE+1),newPoint.y);
                 points.addPoint(newPoint.x,newPoint.y-ARROW_SIZE);
            points.addPoint(newPoint.x,newPoint.y);


       }

    }
    else   // vertical line
    {

      if( bDir )   // is up arrow
      {
        points.addPoint(newPoint.x,newPoint.y);
           points.addPoint(newPoint.x-ARROW_SIZE,newPoint.y);
           points.addPoint(newPoint.x,newPoint.y-(ARROW_SIZE+1));
           points.addPoint(newPoint.x+ARROW_SIZE,newPoint.y);
        points.addPoint(newPoint.x,newPoint.y);

      }
      else  // is a down arrow
      {
           points.addPoint(newPoint.x,newPoint.y);
             points.addPoint(newPoint.x-ARROW_SIZE,newPoint.y);
             points.addPoint(newPoint.x,newPoint.y+ARROW_SIZE);
             points.addPoint(newPoint.x+(ARROW_SIZE+1),newPoint.y);
           points.addPoint(newPoint.x,newPoint.y);
      }
   }
    // draw the arrow
    g.fillPolygon(points);


 }



   protected void paintLine(Graphics g, Vector points, Vector handles)
   {
      g.setColor(Color.darkGray);
      int pointCt = points.size();
      Debug.assertTrue( pointCt >= 2, E2Designer.getResources(), "MissingPoints", null );
      int index = 0;
      Point startPt = new Point((Point) points.get(index++));
      Debug.assertTrue( LINE_WIDTH == 1, E2Designer.getResources(),
                "NotImplForOtherSizes", null );


    boolean isOuputConnector=true;
    boolean bCanShowArrows=canDisplayArrows();
    int iPoints=m_points.size();
    int iFoundAt=0;

    int  iConnectorPositions[] = new int[iPoints+2];

    int iDataSetIndex=-1;

   // if not points do not show the arrows
   if( iPoints == 0 )
       bCanShowArrows=false;

    // if can show the arrows and have 1 or more points
    if(bCanShowArrows )
    {
       UIConnectionPoint connected=null;

       // scan the list of points for connectors
       for(int count=0;count<iPoints;count++)
       {
           // get the connector
           connected=getConnectorAt(count);
           if( connected != null  ) // found one ?
           {
               // store the position on the list
               iConnectorPositions[iFoundAt]=count;
               iFoundAt++;
           }
       }
       // if no attached points do not show the arrows
       if( iFoundAt == 0 )
       {
           bCanShowArrows=false;
       }
       else
       {
             // get the attached object
             connected=getConnectorAt(iConnectorPositions[0]);
             if( connected != null  )
             {
                  if( connected instanceof UIFlexibleConnectionPoint )
                  {
                        UIFlexibleConnectionPoint point=(UIFlexibleConnectionPoint)connected;
                        if( point != null  )
                        {

                          isOuputConnector=point.isAnInput();
                        }
                  }

           }
           // if we are attached to two points
           if( iFoundAt > 1 )
           {
               bCanShowArrows=true;  // set the flags and we are done
           }
           else  // we have a single connection
           {
             Point p1=null;
             Point p2=null;

              // get the connected object index
             iDataSetIndex=iConnectorPositions[0];
             bCanShowArrows=false;
             boolean bFirstSet=false;

             // check if the connected object is at the end
             if( iDataSetIndex == iPoints-1 )
             {
              // get the first and second point
               p1 = new Point((Point) points.get(0));
               p2 = new Point((Point) points.get(1));
             }
             else
             {
                 // else get the last and prior to last point
               p1 = new Point((Point) points.get(iPoints-1));
               p2 = new Point((Point) points.get(iPoints-2));
               bFirstSet=true; // indicate so
             }

             // if  start point is lower than end point
             if( p1.y  < p2.y )
             {
                 /* if the first point is our object then draw an up arrow
                 * else draw an down arrow ( please remember where the arrows can
                   be, so this is why )
                 */
                 drawArrowAt(g,p1,false,bFirstSet);
             }
             else if( p1.y > p2.y )  // starting point is lower that the end
             {
                  Point point=new Point(p1);
                  // if our connected object is at position zero and is an input
                  if( bFirstSet == false && isOuputConnector== false )
                  {
                         point=p2; // use the other side
                  }
                  // draw the arrow
                  drawArrowAt(g,point,false,isOuputConnector);
             }
             else  // horizontal line
             {
                 // starting point at the right of end point
                 // is a normal line do nothing special
                 if( p1.x > p2.x )
                 {
                    drawArrowAt(g,p1,true,isOuputConnector);
                 }
                 else if( p1.x < p2.x )  // start point at the left of end point
                 {
                     if( bFirstSet  == false  )
                     {
                         // use the rightmost point and invert the arrow
                         drawArrowAt(g,p1,true,!isOuputConnector);
                     }
                     else
                     {
                       // else use the leftmost point
                       drawArrowAt(g,p2,true,isOuputConnector);
                     }
                 }

             }
           }
       }
       // see if we need to draw the arrows (we have two connected points )
       if(bCanShowArrows )
       {
         Point p1=null;
         Point p2=null;
         // start at the first point
         for(int count=0; count< iPoints-1; count++)
         {
                p1 = (Point) points.get(count);
              p2 = (Point) points.get(count+1);
              drawArrow(g,p1,p2,isOuputConnector);
         }
       }
    }

     while ( index < pointCt )
      {
         Point endPt = (Point) points.get(index++);
        g.drawLine(startPt.x, startPt.y, endPt.x, endPt.y);
         startPt.setLocation(endPt);
      }

      // Draw the resize handles, if needed
      if (!handles.isEmpty() && isSelected())
      {
         int iHandles = handles.size();
         for (int i = 0; i < iHandles; i++)
         {
            Handle h = (Handle) handles.get(i);
            if ((0 == i) || (handles.size()-1 == i))
               h.setColor( null == ((AttachedPoint) points.get(0 == i ? 0 : points.size()-1)).
                     getConnectionPoint() ? Color.blue : Color.green );
            h.paint(g);
         }
      }
   }

   /**
    * Returns a listener to handle mouse messages that occur after a user has
    * clicked on this component and it has returned <code>true</code> when
    * wantsMouse(pt) was called.
    *
    * @param pt location the mouse was clicked, in connector coords
    * 
    * @param drawingPane the component where drawing will take place during the
    * drag. Typically, this will be the glass pane of the frame drawing pane.
    * This parameter must match the drawing pane passed to getMouseListener.
    *
    * @throws IllegalArgumentException if drawingPane is null, or wantsMouse()
    * returns <code>false</code> on the supplied point
    *
    * @see #getMouseMotionListener
    */
   public MouseListener getMouseListener( Point pt, JComponent drawingPane )
   {
      if (null == drawingPane || !wantsMouse( pt ))
         throw new IllegalArgumentException("null drawing pane or invalid point passed");
      Handle hitHandle = getHitHandle( pt );
      if ( END_POINT_TYPE == hitHandle.getType())
         m_currentDragHandler = new EndPointHandler( this, drawingPane );
      else
      {
         Debug.assertTrue( SEGMENT_TYPE == hitHandle.getType(), "Unrecognized handle type.");
         m_currentDragHandler = new MidPointHandler( this, drawingPane );
      }
      return m_currentDragHandler;
   }

   /**
    * Returns a listener to handle mouse messages that occur after a user has
    * clicked on this component and it has returned <code>true</code> when
    * wantsMouse() was called.
    *
    * @param pt this param is ignored, the value passed to getMouseListener is
    * used
    * 
    * @param drawingPane this param is ignored, the value passed to 
    * getMouseListener is used
    *
    * @see #getMouseListener
    */
   public MouseMotionListener getMouseMotionListener( Point pt, JComponent drawingPane )
   {
      Debug.assertTrue( m_currentDragHandler != null, "getMouseListener must be called before getMouseMotionListener");
      return (MouseMotionListener) m_currentDragHandler;
   }

   // Inner classes
   /**
    * This is a very simple point that has an associated attach position. It is
    * used by the transform methods.
    */
   class AttachedPoint extends Point
   {
      /**
       * Creates an unattached point.
       *
       * @param a valid Point object
       */
      public AttachedPoint(Point pt)
      {
         super(pt);
      }

      /**
       * Copy constructor
       */
      public AttachedPoint(AttachedPoint pt)
      {
         this(pt, pt.getAttachPos(), pt.getConnectionPoint());
      }

      /**
       * Creates an attached point.
       */
      public AttachedPoint(Point pt, int attachPos, UIConnectionPoint cp)
      {
         this(pt);
         setAttachment(attachPos, cp);
      }

      public int getAttachPos()
      {
         return (m_attachPos);
      }

      /**
       * This is the cp that an endpoint is attached too.
       *
       * @param cp the connection point that this endpoint is attaching
       * too. Should be null if attachPos is AP_NONE.
       *
       * @param attachPos One of the AP_... values indicating which direction
       * the line segment eminates from the attach point.
       *
       * @throws IllegalArgumentException if attachPos is not one of the
       * available values or cp is null when attachPos is not AP_NONE.
       */
      public void setAttachment(int attachPos, UIConnectionPoint cp)
      {
         if (( attachPos < AP_FIRST ) || ( attachPos > AP_LAST ))
         {
            final Object[] astrParams =
            {
               String.valueOf(attachPos)
            };
            throw new IllegalArgumentException( MessageFormat.format(
                  E2Designer.getResources().getString( "InvalidAPFlags" ), astrParams ));
         }
         else if (AP_NONE != attachPos && null == cp)
         {
            final Object[] astrParams =
            {
               "UIConnectionPoint"
            };
            throw new IllegalArgumentException( MessageFormat.format(
                  E2Designer.getResources().getString( "CantBeNull" ), astrParams ));
         }
         m_attachPos = attachPos;
         m_cp = cp;
      }

      /**
       * Removes connection information from the point.
       */
      public void clearAttachment()
      {
         m_attachPos = AP_NONE;
         m_cp = null;
      }

      public UIConnectionPoint getConnectionPoint()
      {
         return (m_cp);
      }

      // storage
      private int m_attachPos = AP_NONE;
      private UIConnectionPoint m_cp = null;
   }
  //////////////////////////////////////////////////////////////////////////////
  class EndPointHandler extends UIConnector.DragHandler
  {
     public EndPointHandler( UIConnector conn, JComponent drawingPane )
     {
        conn.super( drawingPane );
        m_conn = conn;
     }

     public void dragFinished()
     {
        if(EscMouseAdapter.wasEscPressed())
        {
           return;
        }

        /* If a connection is made during this dnd, then the component has
           already been modified to connect to the figure. */
        if (!m_bConnectionCreated && ( UNDEFINED_X != m_lastEndPoint.x ))
        {
        // force the end point to the edge of the FigureFrame if it is dragged out
        // of the FigureFrame's bounds.
        if (m_lastEndPoint.x < 0)
          m_lastEndPoint.x = 0;
        if (m_lastEndPoint.x >= m_drawingPane.getPreferredSize().width)
          m_lastEndPoint.x = m_drawingPane.getPreferredSize().width - 1;

        if (m_lastEndPoint.y < 0)
          m_lastEndPoint.y = 0;
        if (m_lastEndPoint.y >= m_drawingPane.getPreferredSize().height)
          m_lastEndPoint.y = m_drawingPane.getPreferredSize().height - 1;

           // resize the underlying component
           m_conn.modifyEndpoint(true, (UIConnector.AttachedPoint) m_hitPoint, SwingUtilities.convertPoint( m_drawingPane,
                 m_lastEndPoint, m_conn ), null);
        }
        else
           // reset flag
           m_bConnectionCreated = false;
     }

     private UIConnector m_conn = null;
  }

  //////////////////////////////////////////////////////////////////////////////
  class MidPointHandler extends UIConnector.DragHandler
  {
     public MidPointHandler( UIConnector conn, JComponent drawingPane )
     {
        conn.super( drawingPane );
        m_conn = conn;
     }

     public void mousePressed( MouseEvent mouseEvent )
     {
        super.mousePressed( mouseEvent );
        // set up limits of travel for the mouse
        Enumeration e = getPolyPoints();
        Point pta = (Point) e.nextElement();

        // these are the segment endpoints for the segment located before the one moving
        Point pt11 = new Point();
        Point pt12 = new Point();

        while ( e.hasMoreElements())
        {
           Point ptb = (Point) e.nextElement();
           if ( m_hitPoint.x == ptb.x || m_hitPoint.y == ptb.y )
           {
              // we found the segment located before the segment being moved
              pt11 = SwingUtilities.convertPoint( m_conn, pta, m_drawingPane );
              pt12 = SwingUtilities.convertPoint( m_conn, ptb, m_drawingPane );
              break;
           }
        }
        Debug.assertTrue( null != pt11 && null != pt12, E2Designer.getResources(),
              "InvHitPt", null );

        // these are the segment endpoints for the segment located after the one moving
        Point pt21 = SwingUtilities.convertPoint( m_conn, (Point) e.nextElement(),
              m_drawingPane );
        Point pt22 = SwingUtilities.convertPoint( m_conn, (Point) e.nextElement(),
              m_drawingPane );

        int minLen = getAttachedSegMinLen();
        // is the moving segment horizontal or vertical?
        if ( pt12.x == pt21.x )
        {
           // vertical line segment
           m_bVertical = true;
           if ( pt12.x > pt11.x && pt22.x > pt21.x )
           {
              // segments on opposite sides of moving line
              m_lowLimit = pt11.x + minLen;
              m_highLimit = pt22.x - minLen;
           }
           else if ( pt12.x > pt11.x )
           {
              // both segments on left
              m_lowLimit = Math.max( pt11.x, pt22.x ) + minLen;
              m_highLimit = m_drawingPane.getSize().width - 1;
           }
           else
           {
              // both segments on right
              m_lowLimit = 0;
              m_highLimit = Math.min( pt11.x, pt22.x ) - minLen;
           }
        }
        else
        {
           // horizontal line segment
           Debug.assertTrue( pt12.y == pt21.y, E2Designer.getResources(),
                 "InvPtInArray", null );
           m_bVertical = false;
           if (( pt12.y > pt11.y && pt22.y > pt21.y )
                 || ( pt11.y > pt12.y && pt21.y > pt22.y ))
           {
              // segments on opposite sides of moving line
              if ( pt11.y < pt12.y )
              {
                 m_lowLimit = pt11.y + minLen;
                 m_highLimit = pt22.y - minLen;
              }
              else
              {
                 m_lowLimit = pt22.y + minLen;
                 m_highLimit = pt11.y - minLen;
              }
           }
           else if ( pt12.y > pt11.y )
           {
              // both segments on left
              m_lowLimit = Math.max( pt11.y, pt22.y ) + minLen;
              m_highLimit = m_drawingPane.getSize().height - 1;
           }
           else
           {
              // both segments on right
              m_lowLimit = 0;
              m_highLimit = Math.min( pt12.y, pt21.y ) - minLen;
           }
        }
  //      System.out.println( "low limit = " + Integer.toString( m_lowLimit )
  //            + "\nhi limit = " + Integer.toString( m_highLimit ));
     }

     /**
      * Limits the range of movement depending on the line segment.
      */
     public Point limitPoint( Point pt )
     {
        Debug.assertTrue( m_lowLimit >= 0 && m_highLimit >= 0,
              E2Designer.getResources(), "LimitsNotInit", null );

        Point limited = new Point( pt );
        if ( m_bVertical )
        {
           limited.y = m_lineOrigin.y;
           if ( limited.x < m_lowLimit )
              limited.x = m_lowLimit;
           else if ( limited.x > m_highLimit )
              limited.x = m_highLimit;
        }
        else
        {
           limited.x = m_lineOrigin.x;
           if ( limited.y < m_lowLimit )
              limited.y = m_lowLimit;
           else if ( limited.y > m_highLimit )
              limited.y = m_highLimit;
        }
  //      System.out.println( "pt before limiting: " + pt.toString()
  //            + "\npt after limiting: " + limited.toString());
        return limited;
     }

     public void dragFinished()
     {
        if(EscMouseAdapter.wasEscPressed())
        {
           return;
        }

        if (( UNDEFINED_X != m_lastEndPoint.x ))
        {
           m_conn.modifyMidpoint( m_hitPoint, SwingUtilities.convertPoint(
                 m_drawingPane, m_lastEndPoint, m_conn ));
        }
     }

     // storage
     // we're handling a vertical line segment if true
     private boolean m_bVertical;
     private int m_lowLimit = -1;
     private int m_highLimit = -1;
     private UIConnector m_conn = null;
  }

  //////////////////////////////////////////////////////////////////////////////
   abstract class DragHandler extends EscMouseAdapter
         implements MouseMotionListener, Serializable
   {
      public DragHandler( JComponent drawingPane )
      {
         m_drawingPane = drawingPane;
      }
      /**
       * This value is set in the x value of a point to indicate that the point is
       * not a currently valid point. In this case, the y value of the point is
       * undefined. The value is not important as long is it is not a valid screen
       * coordinate.
       */
      protected int UNDEFINED_X = 1000000;

      /**
       * @param e the mouse event to be processed. The point should be in the coord
       * system of the drawing pane passed in when the listener was requested.
       */
      public void mousePressed(MouseEvent e)
      {
         super.mousePressed(e);
         Debug.assertTrue(null != m_drawingPane, E2Designer.getResources(),
               "PaneMustBeInit", null );
//         System.out.println("connector: Caught mouse pressed");
         // what was the click on?
         Handle hitHandle = getHitHandle(SwingUtilities.convertPoint( 
               m_drawingPane, e.getPoint(), UIConnector.this));
         m_hitPoint = getPointForHandle(hitHandle);
         Debug.assertTrue(null != m_hitPoint, E2Designer.getResources(), "BadMouseEvt",
               null );
         m_lineOrigin = SwingUtilities.convertPoint(UIConnector.this, 
               m_hitPoint, m_drawingPane);
         m_lastEndPoint.setLocation(UNDEFINED_X, 0);
      }

      /**
       * @param e the mouse event to be processed. The point should be in the coord
       * system of the drawing pane passed in when the listener was requested.
       */
      public void mouseReleased(MouseEvent e)
      {
//         System.out.println("connector: Caught mouse release");
         if(m_drawingPane != null)
         {
            Graphics g = m_drawingPane.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);
            if (UNDEFINED_X != m_lastEndPoint.x)
            {
                // erase the last line
                g.drawLine(m_lineOrigin.x, m_lineOrigin.y, m_lastEndPoint.x, m_lastEndPoint.y);
            }

            dragFinished();
         }

         // Clean up
         m_hitPoint = null;
         m_lineOrigin = null;
         m_lastEndPoint.x = UNDEFINED_X;
         m_drawingPane = null;
      }
      
      public abstract void dragFinished();

      /**
       * A hook to allow derived classes to limit the motion of the mouse cursor. The
       * default puts no limits on its movement.
       *
       * @param pt the location to check, in drawing pane coords
       *
       * @returns the limited point in drawing pane coords
       */
      public Point limitPoint( Point pt )
      {
         return new Point( pt );
      }

      /**
       * @returns an iterator to get all the points that make up the poly line.
       * Derived classes may need this information to limit the moving point.
       * The objects returned by the enumeration should be treated as read-only.
       */
      protected Enumeration getPolyPoints()
      {
         return m_points.elements();
      } 

      /**
       * @returns the minimum length of an attached line segment. This should
       * be used by derived classes when limiting point movement.
       */
      protected int getAttachedSegMinLen()
      {
         return ATTACHED_SEGMENT_MIN_LEN;   
      }

      /**
       * When doing connection stuff while handling mouse, other code may end up
       * creating the line. This flag tells the handler not to modify the line
       * after the mouse is released.
       */
      public void setConnectionCreated( boolean bCreated )
      {
         m_bConnectionCreated = bCreated;
      }

      /**
       * Returns the connector point that was hit during the current/previous
       * drag operation. This is a reference to the point, so if an endpoint was
       * hit, the returned type will be AttachedPoint. If no drag operation has
       * ever been performed by this object, null is returned.
       */
      public Point getHitPoint()
      {
         return m_hitPoint;
      }
      
      /**
       * @param e the mouse event to be processed. The point should be in the coord
       * system of the drawing pane passed in when the listener was requested.
       */
      public void mouseDragged(MouseEvent e)
      {
         if(m_drawingPane != null)
         {
            Graphics g = m_drawingPane.getGraphics();
            g.setColor(Color.black);
            g.setXORMode(Color.white);

            Point limited = limitPoint( e.getPoint());
            // if the point has reached a limit, we don't need to redraw the line
   //         System.out.println( "limited = " + limited.toString()
   //               + "\nlast = " + m_lastEndPoint.toString());
            if ( !limited.equals( m_lastEndPoint ))
            {
               if (UNDEFINED_X != m_lastEndPoint.x)
               {
                   // erase the last line
                   g.drawLine(m_lineOrigin.x, m_lineOrigin.y, m_lastEndPoint.x, m_lastEndPoint.y);
               }
               // draw the new line
               m_lastEndPoint = limited;
               g.drawLine(m_lineOrigin.x, m_lineOrigin.y, m_lastEndPoint.x, m_lastEndPoint.y);
            }
         }
      }

      /**
       * We don't care about this message
       */
      public void mouseMoved( MouseEvent e )
      {
         // no op
      }

      // storage
      /**
       * The component where drawing will occur during a drag/drop. It is null
       * except during a resize operation.
       */
      protected JComponent m_drawingPane = null;

      /**
       * While resizing, this is the end point whose handle was clicked. After 
       * the resize is complete, the point's location is set to the new endpoint. 
       * The location is in component coords. It is null except during a resize 
       * operation.
       */
      protected Point m_hitPoint = null;

      /**
       * When a user initiates a resize on a poly-line endpoint, we save the
       * beginning of that line here, in the coord system of the drawing pane.
       * The origin is set to the center of the handle. It is null except during 
       * a resize operation.
       */
      protected Point m_lineOrigin = null;
   
      /**
       * While resizing, this is the other endpoint of the xor line, in the coord
       * system of the drawing pane.
       */
      protected Point m_lastEndPoint = new Point(UNDEFINED_X, 0);

      /**
       * This flag is normally false. It gets set to true when an endpoint is
       * being dragged and is dropped on a figure that accepts it for connection.
       * This flag indicates the the mouseReleased handler not to modify the
       * connector because it has already been done.
       */
      protected boolean m_bConnectionCreated = false;

   }


   // storage
   /**
    * Stores all the points required to draw the line segments that make up the
    * connector. The polyline is drawn by drawing a line between pts 0, 1; then
    * between pt 1 and 2, etc.
    */
   protected Vector<Point> m_points = new Vector<Point>(5);

   /**
    * Stores a rectangle for each resize handle. There should be 1 handle for
    * each endpoint (total of 2), plus 1 handle for every line segment that
    * is not touching an endpoint.
    */
   protected Vector<Handle> m_handles = new Vector<Handle>(5);

   private DragHandler m_currentDragHandler = null;

   /*
    * These are used to programmatically attach to a connection point.
    */
   private Point m_attachPoint = null;
   private boolean m_bProgrammatic = false;

  /** size the triangle side*/
  private static final int ARROW_SIZE=5;

  /** the minimum distance to which display the arrows */
  private static final int MINIMUM_DISTANCE=30;

}

