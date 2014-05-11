/*
 * TreeTable.scala
 * (TreeTable)
 *
 * Copyright (c) 2013 Hanns Holger Rutz. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *
 * For further information, please contact Hanns Holger Rutz at
 * contact@sciss.de
 */

package de.sciss.treetable

import scala.swing.{Dimension, Color, Publisher, Reactions, Component}
import javax.swing.{table => jtab, tree => jtree, event => jse, Icon, DropMode}
import collection.breakOut
import language.implicitConversions
import javax.swing.tree.TreePath
import j.event.TreeColumnModelListener
import java.awt
import scala.collection.mutable

object TreeTable {
  private trait JTreeTableMixin { def tableWrapper: TreeTable[_, _] }

  val Path = collection.immutable.IndexedSeq
  type Path[+A] = collection.immutable.IndexedSeq[A]

  implicit private[treetable] def pathToTreePath(p: Path[Any]): jtree.TreePath = {
    // TreePath must be non null and not empty... SUCKERS
    // if (p.isEmpty) null else {
      val array: Array[AnyRef] = p.map(_.asInstanceOf[AnyRef])(breakOut)
      new jtree.TreePath(array)
    // }
  }

  implicit private[treetable] def treePathToPath[A](tp: jtree.TreePath): Path[A] = {
    if (tp == null) Path.empty
    else tp.getPath.map(_.asInstanceOf[A])(breakOut) // .toIndexedSeq
  }

  case class DropLocation[+A](private val peer: j.TreeTable.DropLocation) {
    def path: Path[A] = peer.getPath

    /** Returns the child index within the last branch of the path.
      *
      * @return  the index at which a drop occurs within the children of
      *          the branch denoted by `getPath`. For example, `0` means
      *          the drop happens before the first child, `1` means it
      *          happens after the first child. For `ON` drop mode, `-1`
      *          indicates that the drop occurs above the parent node.
      */
    def index : Int = peer.getIndex
    def row   : Int = peer.getRow
    def column: Int = peer.getColumn

    def isInsertRow   : Boolean = peer.isInsertRow
    def isInsertColumn: Boolean = peer.isInsertColumn
  }
}
class TreeTable[A, Col <: TreeColumnModel[A]](treeModel0: TreeModel[A], treeColumnModel0: Col,
                                              tableColumnModel0: jtab.TableColumnModel)
  extends Component /* with Scrollable.Wrapper */ {

  me =>

  import TreeTable.{Path, pathToTreePath, treePathToPath}

  def this(treeModel0: TreeModel[A], treeColumnModel0: Col) {
    this(treeModel0, treeColumnModel0, null)
  }

  private val /* var */ _treeModel        = treeModel0
  private val /* var */ _treeColumnModel  = treeColumnModel0
  // private var _tableColumnModel = tableColumnModel0
  private var _renderer: TreeTableCellRenderer = _

  def treeModel: TreeModel[A]                 = _treeModel
  def treeColumnModel: Col                    = _treeColumnModel
  // def tableColumnModel: jtab.TableColumnModel = _tableColumnModel
  // def tableModel: jtab.TableModel

  def renderer = _renderer
  def renderer_=(r: TreeTableCellRenderer) {
    val rp = r match {
      case w: TreeTableCellRenderer.Wrapped => w.peer
      case _ => new j.TreeTableCellRenderer {
        def getTreeTableCellRendererComponent(treeTable: j.TreeTable, value: Any, selected: Boolean, hasFocus: Boolean,
                                              row: Int, column: Int): awt.Component = {
          val state = TreeTableCellRenderer.State(selected = selected, focused = hasFocus, tree = None)
          r.getRendererComponent(me, value, row = row, column = column, state).peer
        }

        def getTreeTableCellRendererComponent(treeTable: j.TreeTable, value: Any, selected: Boolean, hasFocus: Boolean,
                                              row: Int, column: Int, expanded: Boolean, leaf: Boolean): awt.Component = {
          val state = TreeTableCellRenderer.State(selected = selected, focused = hasFocus,
            tree = Some(TreeTableCellRenderer.TreeState(expanded = expanded, leaf = leaf)))
          r.getRendererComponent(me, value, row = row, column = column, state).peer
        }
      }
    }
    _renderer = r
    peer.setDefaultRenderer(classOf[AnyRef], rp)
  }

  // def editable: Boolean = ...
  // def cellValues: Iterator[A] = ...

  private def wrapTreeModel(_peer: TreeModel[A]): jtree.TreeModel = new {
    val peer = _peer
  } with jtree.TreeModel {
    jModel =>

    // val peer = _treeModel

    def getRoot: AnyRef = peer.root.asInstanceOf[AnyRef]
    def getChild(parent: Any, index: Int): AnyRef = peer.getChild(parent.asInstanceOf[A], index).asInstanceOf[AnyRef]
    def getChildCount(parent: Any): Int = peer.getChildCount(parent.asInstanceOf[A])
    def isLeaf(node: Any): Boolean = peer.isLeaf(node.asInstanceOf[A])

    def valueForPathChanged(path: TreePath, newValue: Any) {
      peer.valueForPathChanged(path, newValue.asInstanceOf[A])  // XXX TODO: is newValue really an `A`?
    }

    def getIndexOfChild(parent: Any, child: Any): Int =
      peer.getIndexOfChild(parent.asInstanceOf[A], child.asInstanceOf[A])

    private val sync = new AnyRef
    private var listeners = Vector.empty[jse.TreeModelListener]

    private val reaction: Reactions.Reaction = {
      case te: TreeNodesChanged[_] =>
        val evt = te.toJava(jModel)
        listeners.foreach { l => l.treeNodesChanged(evt) }

      case te: TreeNodesInserted[_] =>
        val evt = te.toJava(jModel)
        listeners.foreach { l => l.treeNodesInserted(evt) }

      case te: TreeNodesRemoved[_] =>
        val evt = te.toJava(jModel)
        listeners.foreach { l => l.treeNodesRemoved(evt) }

      case te: TreeStructureChanged[_] =>
        val evt = te.toJava(jModel)
        listeners.foreach { l => l.treeStructureChanged(evt) }
    }

    def addTreeModelListener(l: jse.TreeModelListener) {
      sync.synchronized {
        val start = listeners.isEmpty
        listeners :+= l
        if (start) peer.reactions += reaction
      }
    }

    def removeTreeModelListener(l: jse.TreeModelListener) {
      sync.synchronized {
        val idx = listeners.indexOf(l)
        if (idx >= 0) {
          listeners = listeners.patch(idx, Vector.empty, 1)
          if (listeners.isEmpty) peer.reactions -= reaction
        }
      }
    }
  }

  private def wrapTreeColumnModel(_peer: Col): j.TreeColumnModel = new {
    val peer = _peer
  } with j.TreeColumnModel {
    // val peer = _treeColumnModel

    def getHierarchicalColumn = peer.hierarchicalColumn
    def getColumnClass(column: Int): Class[_] = peer.getColumnClass(column)
    def isCellEditable(node: Any, column: Int): Boolean = peer.isCellEditable(node.asInstanceOf[A], column)
    def getColumnCount: Int = peer.columnCount
    def getColumnName(column: Int): String = peer.getColumnName(column)
    def getValueAt(node: Any, column: Int): AnyRef = peer.getValueAt(node.asInstanceOf[A], column).asInstanceOf[AnyRef]
    def setValueAt(value: Any, node: Any, column: Int) { peer.setValueAt(value, node.asInstanceOf[A], column) }

    private val sync      = new AnyRef
    private var listeners = Vector.empty[TreeColumnModelListener]

    private val reaction: Reactions.Reaction = {
      case TreeColumnChanged(_, path, column) =>
        val evt = new j.event.TreeColumnModelEvent(this, path, column)
        listeners.foreach { l =>
          l.treeColumnChanged(evt)
        }
    }

    def addTreeColumnModelListener(l: TreeColumnModelListener) {
      sync.synchronized {
        val start = listeners.isEmpty
        listeners :+= l
        if (start) peer.reactions += reaction
      }
    }

    def removeTreeColumnModelListener(l: TreeColumnModelListener) {
      sync.synchronized {
        val idx = listeners.indexOf(l)
        if (idx >= 0) {
          listeners = listeners.patch(idx, Vector.empty, 1)
          if (listeners.isEmpty) peer.reactions -= reaction
        }
      }
    }
  }

  override lazy val peer: j.TreeTable =
    new j.TreeTable(wrapTreeModel(treeModel0), wrapTreeColumnModel(treeColumnModel0), tableColumnModel0)
      with TreeTable.JTreeTableMixin with SuperMixin {

      def tableWrapper = TreeTable.this

      //    override def getCellRenderer(r: Int, c: Int) = new TableCellRenderer {
      //      def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) =
      //        Table.this.rendererComponent(isSelected, hasFocus, row, column).peer
      //    }
      //    override def getCellEditor(r: Int, c: Int) = editor(r, c)
      //    override def getValueAt(r: Int, c: Int) = Table.this.apply(r,c).asInstanceOf[AnyRef]
    }

  def autoCreateColumnHeader           : Boolean          = peer.getAutoCreateColumnHeader
  def autoCreateColumnHeader_=   (value: Boolean  ): Unit = peer.setAutoCreateColumnHeader(value)

  def autoCreateColumnsFromModel       : Boolean          = peer.getAutoCreateColumnsFromModel
  def autoCreateColumnsFromModel_(value: Boolean  ): Unit = peer.setAutoCreateColumnsFromModel(value)

  def autoCreateRowHeader              : Boolean          = peer.getAutoCreateRowHeader
  def autoCreateRowHeader_=      (value: Boolean  ): Unit = peer.setAutoCreateRowHeader(value)

  def autoCreateRowSorter              : Boolean          = peer.getAutoCreateRowSorter
  def autoCreateRowSorter_=      (value: Boolean  ): Unit = peer.setAutoCreateRowSorter(value)

  def cellSelectionEnabled             : Boolean          = peer.getCellSelectionEnabled
  def cellSelectionEnabled_=     (value: Boolean  ): Unit = peer.setCellSelectionEnabled(value)

  def columnFocusEnabled               : Boolean          = peer.isColumnFocusEnabled
  def columnFocusEnabled_=       (value: Boolean  ): Unit = peer.setColumnFocusEnabled(value)

  def columnSelectionAllowed           : Boolean          = peer.getColumnSelectionAllowed
  def columnSelectionAllowed_=   (value: Boolean  ): Unit = peer.setColumnSelectionAllowed(value)

  def rowSelectionAllowed              : Boolean          = peer.getRowSelectionAllowed
  def rowSelectionAllowed_=      (value: Boolean  ): Unit = peer.setRowSelectionAllowed(value)

  def expandsSortedNodes               : Boolean          = peer.getExpandsSortedNodes
  def expandsSortedNodes_=       (value: Boolean  ): Unit = peer.setExpandesSortedNodes(value)

  def expandsSelectedPaths             : Boolean          = peer.getExpandsSelectedPaths
  def expandsSelectedPaths_=     (value: Boolean  ): Unit = peer.setExpandsSelectedPaths(value)

  def largeModel                       : Boolean          = peer.isLargeModel
  def largeModel_=               (value: Boolean  ): Unit = peer.setLargeModel(value)

  def nodeSortingEnabled               : Boolean          = peer.isNodeSortingEnabled
  def nodeSortingEnabled_=       (value: Boolean  ): Unit = peer.setNodeSortingEnabled(value)

  def rootVisible                      : Boolean          = peer.isRootVisible
  def rootVisible_=              (value: Boolean  ): Unit = peer.setRootVisible(value)

  def scrollsOnExpand                  : Boolean          = peer.getScrollsOnExpand
  def scrollsOnExpand_=          (value: Boolean  ): Unit = peer.setScrollsOnExpand(value)

  // isShowGrid

  def showHorizontalLines              : Boolean          = peer.getShowHorizontalLines
  def showHorizontalLines_=      (value: Boolean  ): Unit = peer.setShowHorizontalLines(value)

  def showVerticalLines                : Boolean          = peer.getShowVerticalLines
  def showVerticalLines_=        (value: Boolean  ): Unit = peer.setShowVerticalLines(value)

  def showsRootHandles                 : Boolean          = peer.getShowsRootHandles
  def showsRootHandles_=         (value: Boolean  ): Unit = peer.setShowsRootHandles(value)

  def rowHeight                        : Int              = peer.getRowHeight
  def rowHeight_=                (value: Int      ): Unit = peer.setRowHeight(value)

  def rowMargin                        : Int              = peer.getRowMargin
  def rowMargin_=                (value: Int      ): Unit = peer.setRowMargin(value)

  def toggleClickCount                 : Int              = peer.getToggleClickCount
  def toggleClickCount_=         (value: Int      ): Unit = peer.setToggleClickCount(value)

  def visibleRowCount                  : Int              = peer.getVisibleRowCount
  def visibleRowCount_=          (value: Int      ): Unit = peer.setVisibleRowCount(value)

  def intercellSpacing                 : Dimension        = peer.getIntercellSpacing
  def intercellSpacing_=         (value: Dimension): Unit = peer.setIntercellSpacing(value)

  def alternativeRowColor              : Color            = peer.getAlternateRowColor
  def alternativeRowColor_=      (value: Color    ): Unit = peer.setAlternateRowColor(value)

  def gridColor                        : Color            = peer.getGridColor
  def gridColor_=                (value: Color    ): Unit = peer.setGridColor(value)

  def selectionBackground               : Color            = peer.getSelectionBackground
  def selectionBackground_=       (value: Color    ): Unit = peer.setSelectionBackground(value)

  def selectionForeground               : Color            = peer.getSelectionForeground
  def selectionForeground_=       (value: Color    ): Unit = peer.setSelectionForeground(value)

  def openIcon                         : Icon             = peer.getOpenIcon
  def openIcon_=                 (value: Icon     ): Unit = peer.setOpenIcon(value)

  def leafIcon                         : Icon             = peer.getLeafIcon
  def leafIcon_=                 (value: Icon     ): Unit = peer.setLeafIcon(value)

  def closedIcon                       : Icon             = peer.getClosedIcon
  def closedIcon_=               (value: Icon     ): Unit = peer.setClosedIcon(value)

  def ascendingSortIcon                : Icon             = peer.getAscendingSortIcon
  def ascendingSortIcon_=        (value: Icon     ): Unit = peer.setAscendingSortIcon(value)

  def descendingSortIcon               : Icon             = peer.getDescendingSortIcon
  def descendingSortIcon_=       (value: Icon     ): Unit = peer.setDescendingSortIcon(value)

  // def autoscrolls                      : Boolean =  peer.getAutoscrolls
  // def autoscrolls_=(value              : Boolean) { peer.getAutoscrolls }

  def dragEnabled                      : Boolean          = peer.getDragEnabled
  def dragEnabled_=              (value: Boolean  ): Unit = peer.setDragEnabled(value)

  def dropMode                        : DropMode          = peer.getDropMode
  def dropMode_=                (value: DropMode  ): Unit = peer.setDropMode(value)

  def expandPath(path: Path[A]): Unit = peer.expandPath(path)

  def hierarchicalColumn: Int = peer.getHierarchicalColumn

  def isCellEditable(row: Int, column: Int): Boolean = peer.isCellEditable(row, column)
  def isCellSelected(row: Int, column: Int): Boolean = peer.isCellSelected(row, column)

  def isCollapsed(path: Path[A]): Boolean = peer.isCollapsed(path)
  def isCollapsed(row : Int    ): Boolean = peer.isCollapsed(row )

  def isExpanded (path: Path[A]): Boolean = peer.isExpanded (path)
  def isExpanded (row : Int    ): Boolean = peer.isExpanded (row )

  def isLeaf     (path: Path[A]): Boolean = peer.isLeaf     (path)

  // = selection.path.contains
  // def isPathSelected  (path  : Path[A]): Boolean = peer.isPathSelected  (path  )

  def isColumnSelected(column: Int    ): Boolean = peer.isColumnSelected(column)
  def isRowSelected   (row   : Int    ): Boolean = peer.isRowSelected   (row   )

  def isEditing       : Boolean = peer.isEditing
  def isFixedRowHeight: Boolean = peer.isFixedRowHeight

  // def apply(row: Int, column: Int): Any = peer.getValueAt(row, column)
  def getNode(row: Int): A = peer.getNode(row).asInstanceOf[A]

  def getRowForPath(path: Path[A]): Int = peer.getRowForPath(path)

  def editCellAt(row: Int, column: Int): Boolean = peer.editCellAt(row, column)
  def startEditingAtPath(path: Path[A]): Boolean = peer.startEditingAtPath(path)
  def startEditing(row: Int)           : Boolean = peer.startEditingAtRow(row)

  def dropLocation: Option[TreeTable.DropLocation[A]] = Option(peer.getDropLocation).map(TreeTable.DropLocation[A])

  def changeSelection(row: Int, column: Int, toggle: Boolean = false, extend: Boolean = false): Unit =
    peer.changeSelection(row, column, toggle, extend)

  object selection extends Publisher {
    protected abstract class SelectionSet[B](a: => Seq[B]) extends mutable.Set[B] {
      def -=(n: B): this.type
      def +=(n: B): this.type
      def contains(n: B) = a.contains(n)
      override def size = a.length
      def iterator = a.iterator
    }

    object paths extends SelectionSet[Path[A]]({
      val p = peer.getSelectionPaths
      if (p == null) Seq.empty else p.map(treePathToPath)(breakOut)
    }) {
      def -= (p :     Path[A] ) = { peer.removeSelectionPath (p)                               ; this }
      def += (p :     Path[A] ) = { peer.addSelectionPath    (p)                               ; this }
      def --=(ps: Seq[Path[A]]) = { peer.removeSelectionPaths(ps.map(pathToTreePath)(breakOut)); this }
      def ++=(ps: Seq[Path[A]]) = { peer.addSelectionPaths   (ps.map(pathToTreePath)(breakOut)); this }

      def leadSelection: Option[Path[A]] = Option(peer.getLeadSelectionPath)

      override def size = peer.getSelectionCount
    }

    object rows extends SelectionSet(peer.getSelectedRows) {
      def -=(n: Int) = { peer.removeSelectionRow(n); this }
      def +=(n: Int) = { peer.addSelectionRow   (n); this }

      def leadIndex  : Int = peer.getSelectionModel.getLeadSelectionRow
      // def anchorIndex: Int = peer.getSelectionModel.getAnchorSelectionRow

      override def size = peer.getSelectionCount
    }

    // cells is a PITA peer-wise

    //    object cells extends SelectionSet[(Int, Int)]({
    //      // (for(r <- selection.rows; c <- selection.columns) yield (r,c)).toSeq
    //      ...
    //
    //    }) {
    //      def -=(n: (Int, Int)) = {
    //        // peer.removeRowSelectionInterval(n._1,n._1)
    //        // peer.removeColumnSelectionInterval(n._2,n._2)
    //        peer.removeSelectionRow(n._1)
    //        peer.removeColumn
    //        this
    //      }
    //      def +=(n: (Int, Int)) = {
    //        // peer.addRowSelectionInterval(n._1,n._1)
    //        // peer.addColumnSelectionInterval(n._2,n._2)
    //        this
    //      }
    //      override def size = peer.getSelectedRowCount * peer.getSelectedColumnCount
    //    }

    peer.getSelectionModel.addTreeSelectionListener(new jse.TreeSelectionListener {
      def valueChanged(e: jse.TreeSelectionEvent) {
        val (pathsAdded, pathsRemoved) = e.getPaths.toVector.partition(e.isAddedPath)

        publish(new TreeTableSelectionChanged(me,
                pathsAdded   map treePathToPath,
                pathsRemoved map treePathToPath,
                Option(e.getNewLeadSelectionPath: Path[A]),
                Option(e.getOldLeadSelectionPath: Path[A])))
      }
    })

    // TODO: rows, cells, ...

    // def cellValues: Iterator[A] = ...

    // def isEmpty = size == 0
  }
}