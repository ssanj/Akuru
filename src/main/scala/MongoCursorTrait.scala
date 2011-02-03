/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.{DBObject, DBCursor}
import MongoTypes._

trait MongoCursorTrait {

  //TODO: Should this be typed? MongoCursor[T]
  case class MongoCursor(private val dbc:DBCursor) {
    def toSeq[T](implicit con:MongoConverter[T]): Seq[T] = {
      import scala.collection.JavaConversions._
        val it:Iterator[DBObject] = dbc.iterator
        it.map(con.convert(_)).toSeq
    }

    def asSeq[T](implicit con:MongoObject => T): Seq[T] = {
        import scala.collection.JavaConversions.asScalaIterable
        val seq:Seq[DBObject] = dbc.toSeq
        seq.map(con(_))
    }
  }

  object MongoCursor {
    implicit def dbCursorToMongoCursor(dbc:DBCursor): MongoCursor = MongoCursor(dbc)
  }

}