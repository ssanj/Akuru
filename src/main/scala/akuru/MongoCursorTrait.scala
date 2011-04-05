/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.{DBObject, DBCursor}
import MongoTypes.MongoSortObject
import Tools._

trait MongoCursorTrait {

  case class MongoCursor(private val dbc:DBCursor) {

    def asSeq[T <: DomainObject : MongoToDomain]: Seq[T] = {
        import scala.collection.JavaConversions.asScalaIterable
        val seq:Seq[DBObject] = dbc.toSeq
        seq.foldLeft(Seq[T]())((acc, element) =>  implicitly[MongoToDomain[T]].apply(element) fold (acc, acc :+ _))
    }

    def limit(hits: => Int): MongoCursor = dbc.limit(hits)

    def orderBy(sorting: => MongoSortObject): MongoCursor =  dbc.sort(sorting.value)

    def all: MongoCursor = this
  }

  object MongoCursor {
    implicit def dbCursorToMongoCursor(dbc:DBCursor): MongoCursor = MongoCursor(dbc)
  }

}