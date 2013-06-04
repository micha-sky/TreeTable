/*
 * AbstractTreeModel.scala
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

import scala.annotation.tailrec
import collection.immutable.{IndexedSeq => IIdxSeq}

trait AbstractTreeModel[A] extends TreeModel[A] {
  import TreeTable.Path

  def getParent(node: A): Option[A]

  private def pathToRoot(node: A): TreeTable.Path[A] = {
    @tailrec def loop(partial: Path[A], n: A): Path[A] = {
      val res = n +: partial
      getParent(n) match {
        case Some(parent) => loop(res, parent)
        case _ => res
      }
    }
    loop(Path.empty, node)
  }

  final protected def fireNodesChanged(nodes: A*) {
    fire(nodes)(TreeNodesChanged[A])
  }

  /** Fire a `TreeNodesChanged` for the root node. */
  final protected def fireRootChanged() {
    publish(TreeNodesChanged(this, Path(root)))
  }

  final protected def fireNodesInserted(nodes: A*) {
    fire(nodes)(TreeNodesInserted[A])
  }

  final protected def fireNodesRemoved(nodes: A*) {
    fire(nodes)(TreeNodesRemoved[A])
  }

  final protected def fireStructureChanged(node: A) {
    publish(TreeStructureChanged(this, pathToRoot(node)))
  }

  private def fire(nodes: Seq[A])(fun: (TreeModel[A], Path[A], Seq[(Int, A)]) => TreeModelEvent[A]) {
    var pred  = Map.empty[A, Path[A]]
    var paths = Map.empty[Path[A], IIdxSeq[(Int, A)]] withDefaultValue Vector.empty
    nodes.foreach { n =>
      val (path, idx) = getParent(n).fold(throw new IllegalArgumentException(s"$n does not have parent")) { parent =>
        val _path = pred.getOrElse(parent, {
          val res = pathToRoot(parent)
          pred += parent -> res
          res
        })
        _path -> getIndexOfChild(parent, n)
      }
      paths += path -> (paths(path) :+ (idx, n))
    }

    paths.foreach { case (parentPath, indexed) =>
      publish(fun(this, parentPath, indexed))
    }
  }
}