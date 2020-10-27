/*[ UTMultiLineTable.java ]****************************************************
 *
 * COPYRIGHT (c) 1999 - 2003 by Percussion Software, Inc., Stoneham, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 ******************************************************************************/

package com.percussion.E2Designer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.Segment;
import javax.swing.text.TabExpander;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Enumeration;
import java.util.Vector;


   /**
    * A utility class to generate a table with cells having multiple lines.
    */
public class UTMultiLineTable extends JTable {
  
  public UTMultiLineTable() {
    this(null, null, null);
  }

  public UTMultiLineTable(TableModel dm) {
    this(dm, null, null);
  }

  public UTMultiLineTable(TableModel dm, TableColumnModel cm) {
    this(dm, cm, null);
  }

  public UTMultiLineTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
    super(dm,cm,sm);
    setUI( new UTMultiLineBasicTableUI() );

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {    
            revalidate();
      }
    });
  }

  public UTMultiLineTable(int numRows, int numColumns) {
    this(new DefaultTableModel(numRows, numColumns));
  }

  public UTMultiLineTable(final Vector rowData, final Vector columnNames) {
    super( rowData, columnNames );
    setUI( new UTMultiLineBasicTableUI() );
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {    
            revalidate();
      }
    });
  }

  public UTMultiLineTable(final Object[][] rowData, final Object[] columnNames) {
    super( rowData, columnNames );
    setUI( new UTMultiLineBasicTableUI() );
    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {    
            revalidate();
      }
    });
  }
   
  public int rowAtPoint(Point point) {
    int y = point.y;
    int rowSpacing = getIntercellSpacing().height;
    int rowCount = getRowCount();
    int rowHeight = 0;
    for (int row=0; row<rowCount; row++) {
      rowHeight += getRowHeight(row) + rowSpacing;
      if (y < rowHeight) {
            return row;
      }
    }
    return -1;
  }

  public int getHeight(String text, int width) {
    FontMetrics fm = getFontMetrics(getFont());
    int numLines = 1;
      if(text == null)
         return numLines;
    Segment s = new Segment(text.toCharArray(), 0, 0);
    s.count = s.array.length;
    TabExpander te = new MyTabExpander(fm);
    int breaks = getBreakLocation(s, fm, 0, width, te, 0);
    while((breaks+s.offset) < s.array.length) {
      s.offset += breaks;
      s.count = s.array.length - s.offset;
      numLines++;
      breaks = getBreakLocation(s, fm, 0, width, te, 0);
    }
    return numLines * fm.getHeight();
  }

  public int getTabbedTextOffset(Segment s, 
             FontMetrics metrics,
             int x0, int x, TabExpander e,
             int startOffset, 
             boolean round) {
    int currX = x0;
    int nextX = currX;
    char[] txt = s.array;
    int n = s.offset + s.count;
    for (int i = s.offset; i < n; i++) {
      if (txt[i] == '\t') {
   if (e != null) {
     nextX = (int) e.nextTabStop((float) nextX,
                  startOffset + i - s.offset);
   } else {
     nextX += metrics.charWidth(' ');
   }
      } else if (txt[i] == '\n') {
   return i - s.offset;
      } else if (txt[i] == '\r') {
   return i + 1 - s.offset; // kill the newline as well
      } else {
   nextX += metrics.charWidth(txt[i]);
      }
      if ((x >= currX) && (x < nextX)) {
   // found the hit position... return the appropriate side
   if ((round == false) || ((x - currX) < (nextX - x))) {
     return i - s.offset;
   } else {
     return i + 1 - s.offset;
   }
      }
      currX = nextX;
    }

    return s.count;
  }

  public int getBreakLocation(Segment s, FontMetrics metrics,
               int x0, int x, TabExpander e,
               int startOffset) {
    
    int index = getTabbedTextOffset(s, metrics, x0, x, 
                     e, startOffset, false);

    if ((s.offset+index) < s.array.length) {
      for (int i = s.offset + Math.min(index, s.count - 1); 
      i >= s.offset; i--) {
   
   char ch = s.array[i];
   if (Character.isWhitespace(ch)) {
     // found whitespace, break here
     index = i - s.offset + 1;
     break;
   }
      }
    }
    return index;
  }

  class MyTabExpander implements TabExpander {
    int tabSize;
    public MyTabExpander(FontMetrics metrics) {
      tabSize = 5 * metrics.charWidth('m');
    }
    public float nextTabStop(float x, int offset) {
      int ntabs = (int) x / tabSize;
      return (ntabs + 1) * tabSize;
    }
  }


  public int getRowHeight() {
    // this is used for scrolling. for multiline tables we do not know the
    // exact height (its might be different for each row). Therfore we return
    // the height of one line of text in a row.
      if (getFont() == null)
         return DEFAULT_FONT_HEIGHT;

    return getFontMetrics(getFont()).getHeight();
  }

  public int getRowHeight(int row) {
    int numCols = getColumnCount();
    TableModel tm = getModel();
      if(getFont() == null)
         return DEFAULT_FONT_HEIGHT;
    int fontHeight = getFontMetrics(getFont()).getHeight();
    int height = fontHeight;
    Enumeration cols = getColumnModel().getColumns();
    int i = 0;
    while(cols.hasMoreElements()) {
      TableColumn col = (TableColumn) cols.nextElement();
      TableCellRenderer tcr = col.getCellRenderer();
      int colWidth = col.getWidth();
      if (tcr instanceof UTMultiLineCellRenderer) {
            if(row < getRowCount())
               height = Math.max(height, getHeight((String)tm.getValueAt(row,i), colWidth));
      }
      i++;
    }
    return height;
  }

  public Rectangle getCellRect(int row, int column, boolean includeSpacing) {
      int index = 0;
      Rectangle cellFrame;
      int columnMargin = getColumnModel().getColumnMargin();
      Enumeration enumeration = getColumnModel().getColumns();
      TableColumn aColumn;

      cellFrame = new Rectangle();
      cellFrame.height = getRowHeight(row) + rowMargin;

      int rowSpacing = getIntercellSpacing().height;
      int y = 0;
      for( int i = 0; i < row; i++ )
      {
         y += getRowHeight(i) + rowSpacing;
      }
      cellFrame.y = y;

      while (enumeration.hasMoreElements()) {
         aColumn = (TableColumn)enumeration.nextElement();
         cellFrame.width = aColumn.getWidth() + columnMargin;

         if (index == column)
            break;

         cellFrame.x += cellFrame.width;
         index++;
      }

      if (!includeSpacing) {
         Dimension spacing = getIntercellSpacing();
         // This is not the same as grow(), it rounds differently.
         cellFrame.setBounds(cellFrame.x +     spacing.width/2,
                        cellFrame.y +     spacing.height/2,
                        cellFrame.width -  spacing.width,
                        cellFrame.height - spacing.height);
      }
      return cellFrame;
  }

   static int DEFAULT_FONT_HEIGHT = 20;


} // UTMultiLineTable
