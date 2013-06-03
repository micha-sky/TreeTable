/*
 * TreeTableCellRenderer.scala
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

import scala.swing.{Label, Component}

object TreeTableCellRenderer {
  final case class TreeState(expanded: Boolean, leaf: Boolean)
  final case class State(selected: Boolean, focused: Boolean, tree: Option[TreeState])

  object Default extends Label with Wrapped {
    override lazy val peer: j.DefaultTreeTableCellRenderer = new j.DefaultTreeTableCellRenderer

    def getRendererComponent(treeTable: TreeTable[_, _], value: Any, row: Int, column: Int, state: State): Component = {
      state.tree match {
        case Some(TreeState(expanded, leaf)) =>
          peer.getTreeTableCellRendererComponent(treeTable.peer, value, state.selected, state.focused, row, column,
            expanded, leaf)
        case _ =>
          peer.getTreeTableCellRendererComponent(treeTable.peer, value, state.selected, state.focused, row, column)
      }
      this
    }
  }

  trait Wrapped extends TreeTableCellRenderer {
    def peer: j.TreeTableCellRenderer
  }
}
trait TreeTableCellRenderer {
  import TreeTableCellRenderer._

  def getRendererComponent(treeTable: TreeTable[_, _], value: Any, row: Int, column: Int, state: State): Component
}