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

    def asSeq[T <: DomainObject : MongoToDomain]: Seq[T] = {
        import scala.collection.JavaConversions.asScalaIterable
        val seq:Seq[DBObject] = dbc.toSeq
        seq.map(implicitly[MongoToDomain[T]].apply(_))
    }
  }

  object MongoCursor {
    implicit def dbCursorToMongoCursor(dbc:DBCursor): MongoCursor = MongoCursor(dbc)
  }

}