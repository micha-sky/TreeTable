/*
 * TreeColumnModel.scala
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

import scala.reflect.ClassTag
import scala.swing.Publisher
import TreeTable.Path
import scala.annotation.tailrec
import collection.immutable.{IndexedSeq => IIdxSeq}

object TreeColumnModel {
  abstract class Column[A, T](val name: String)(implicit val ct: ClassTag[T]) {
    def apply(node: A): T
    def update(node: A, value: T): Unit
    def isEditable(node: A): Boolean
  }

  trait TupleLike[A] extends TreeColumnModel[A] {
    protected def columns: IIdxSeq[Column[A, _]]

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

    final def getColumnName (column: Int): String   = columns(column).name
    final def getColumnClass(column: Int): Class[_] = columns(column).ct.runtimeClass

   	final def columnCount: Int = columns.length

    def getValueAt(node: A, column: Int): Any = columns(column)(node)

    def setValueAt(value: Any, node: A, column: Int) {
      columns(column).asInstanceOf[Column[A, Any]](node) = value
      val path = pathToRoot(node)
      publish(TreeColumnChanged(this, path, column))
    }

   	def isCellEditable(node: A, column: Int): Boolean = columns(column).isEditable(node)

    def hierarchicalColumn = 0
  }

  abstract class Tuple1[A, T1](val _1: Column[A, T1]) extends TupleLike[A] {
    protected val columns = IIdxSeq(_1)
  }

  abstract class Tuple2[A, T1, T2](val _1: Column[A, T1], val _2: Column[A, T2]) extends TupleLike[A] {
    protected val columns = IIdxSeq(_1, _2)
  }

  abstract class Tuple3[A, T1, T2, T3](val _1: Column[A, T1], val _2: Column[A, T2], val _3: Column[A, T3])
    extends TupleLike[A] {
    protected val columns = IIdxSeq(_1, _2, _3)
  }

  abstract class Tuple4[A, T1, T2, T3, T4](val _1: Column[A, T1], val _2: Column[A, T2], val _3: Column[A, T3],
                                           val _4: Column[A, T4])
    extends TupleLike[A] {
    protected val columns = IIdxSeq(_1, _2, _3, _4)
  }

  abstract class Tuple5[A, T1, T2, T3, T4, T5](val _1: Column[A, T1], val _2: Column[A, T2], val _3: Column[A, T3],
                                               val _4: Column[A, T4], val _5: Column[A, T5])
    extends TupleLike[A] {
    protected val columns = IIdxSeq(_1, _2, _3, _4, _5)
  }

  abstract class Tuple6[A, T1, T2, T3, T4, T5, T6](val _1: Column[A, T1], val _2: Column[A, T2], val _3: Column[A, T3],
                                                   val _4: Column[A, T4], val _5: Column[A, T5], val _6: Column[A, T6])
    extends TupleLike[A] {
    protected val columns = IIdxSeq(_1, _2, _3, _4, _5, _6)
  }
}
trait TreeColumnModel[A] extends Publisher {
  // def peer: j.TreeColumnModel

  def getColumnName (column: Int): String
  def getColumnClass(column: Int): Class[_]

 	def columnCount: Int

 	def getValueAt(node: A, column: Int): Any
 	def setValueAt(value: Any, node: A, column: Int): Unit

 	def isCellEditable(node: A, column: Int): Boolean

  def hierarchicalColumn: Int
}