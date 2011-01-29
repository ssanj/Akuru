/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.{DBObject, DBCursor}

trait MongoCursorTrait {

  case class MongoCursor(private val dbc:DBCursor) {
    def toSeq[T](implicit con:MongoConverter[T]): Seq[T] = {
      import scala.collection.JavaConversions._
        val it:Iterator[DBObject] = dbc.iterator
        it.map(con.convert(_)).toSeq
    }
  }

  object MongoCursor {
    implicit def dbCursorToMongoCursor(dbc:DBCursor): MongoCursor = MongoCursor(dbc)
  }

}