/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.{DBObject, DBCursor}
import MongoTypes._
import MongoTypes.MongoObject.SortObjectJoiner

trait MongoCursorTrait {

  //TODO: Should this be typed? MongoCursor[T]
  case class MongoCursor(private val dbc:DBCursor) {

    def asSeq[T <: DomainObject : MongoToDomain]: Seq[T] = {
        import scala.collection.JavaConversions.asScalaIterable
        val seq:Seq[DBObject] = dbc.toSeq
        seq.foldLeft(Seq[T]())((acc, element) =>  implicitly[MongoToDomain[T]].apply(element) fold (acc, acc :+ _))
    }

    def limit(hits: => Int): MongoCursor = dbc.limit(hits)

    def orderBy(sorting: => SortObjectJoiner): MongoCursor = dbc.sort(sorting.done)
  }

  object MongoCursor {
    implicit def dbCursorToMongoCursor(dbc:DBCursor): MongoCursor = MongoCursor(dbc)
  }

}