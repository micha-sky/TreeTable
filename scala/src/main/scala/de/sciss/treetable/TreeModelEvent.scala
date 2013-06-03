/*
 * TreeModelEvent.scala
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

import javax.swing.{event => jse}
import scala.swing.event.Event
import collection.breakOut

sealed trait TreeModelEvent[A] extends Event {
  def model: TreeModel[A]
  def parentPath: TreeTable.Path[A]
  def children: Seq[(Int, A)]

  final private[treetable] def toJava(source: Any): jse.TreeModelEvent = {
    import TreeTable.pathToTreePath
    val (idxSeq, nodesSeq) = children.unzip
    val indices = idxSeq.toArray
    val nodes: Array[AnyRef] = nodesSeq.map(_.asInstanceOf[AnyRef])(breakOut)
    new jse.TreeModelEvent(source, parentPath, indices, nodes)
  }
}

final case class TreeNodesChanged[A](model: TreeModel[A], parentPath: TreeTable.Path[A], children: (Int, A)*)
  extends TreeModelEvent[A]

final case class TreeNodesInserted[A](model: TreeModel[A], parentPath: TreeTable.Path[A], children: (Int, A)*)
  extends TreeModelEvent[A]

final case class TreeNodesRemoved[A](model: TreeModel[A], parentPath: TreeTable.Path[A], children: (Int, A)*)
  extends TreeModelEvent[A]

final case class TreeStructureChanged[A](model: TreeModel[A], parentPath: TreeTable.Path[A], children: (Int, A)*)
  extends TreeModelEvent[A]
