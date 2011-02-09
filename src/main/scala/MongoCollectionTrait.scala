/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.DBCollection
import MongoTypes._
import scala.Either

trait MongoCollectionTrait extends Tools {

  //TODO:Once all methods ar tested remove dbc and replace with newdbc.
  case class MongoCollection(dbc:DBCollection, newdbc:DBCollectionTrait) {

    import com.mongodb.WriteResult

   def save3[T <% MongoObject](value: => T): Option[String] =  writeResultToOption(() => dbc.save(value.toDBObject))

    def findOne3[T](mo:MongoObject)(implicit f3: MongoObject => T): Either[String, Option[T]] = {
      runSafelyWithEither { nullToOption(dbc.findOne(mo.toDBObject)).map(t => f3(t)) }
    }

    def find3[T](mo:MongoObject)(implicit f3: MongoObject => T): Either[String, Seq[T]] = {
      runSafelyWithEither {
        val mc:MongoCursor = dbc.find(mo)
        mc.asSeq[T]
      }
    }

    def update3(query:MongoObject, upate:MongoObject, upsert:Boolean = false, multi:Boolean = false):Option[String] = {
      writeResultToOption(() => dbc.update(query.toDBObject, upate.toDBObject, upsert, multi))
    }

    def writeResultToOption(f:() => WriteResult): Option[String] =  {
      import MongoTypes.MongoWriteResult._
      runSafelyWithEither(f.apply).fold(l => Some(l), r => r.getStringError)
    }

    def findAndModify[T](query:MongoObject, sort:MongoObject, update:MongoObject, returnNew:Boolean)(implicit mc:MongoConverter[T]):
      Either[MongoError, T] = {
      import MongoObject.empty
      wrapWith {
        mc.convert(dbc.findAndModify(query, empty, sort, false, update, returnNew, false))
      }
    }

    def drop3: Option[String] = runSafelyWithOptionReturnError(dbc.drop)
  }

  object MongoCollection {
    import DBCollectionTrait._
    implicit def dbCollectionToMongoCollection(dbc:DBCollection): MongoCollection = MongoCollection(dbc, createDBCollectionTrait(dbc))
  }
}