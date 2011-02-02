/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.{DBCollection}
import MongoTypes._
import scala.{Either}

trait MongoCollectionTrait extends Tools {

  //TODO:Once all methods ar tested remove dbc and replace with newdbc.
  case class MongoCollection(dbc:DBCollection, newdbc:DBCollectionTrait) {

    import com.mongodb.WriteResult
    import org.bson.types.ObjectId

    def findOne[T](mo:MongoObject)(implicit con:MongoConverter[T]): Either[MongoError, Option[T]] = {
      wrapWith{ newdbc.findOne(mo.toDBObject).map(con.convert(_)) }
    }

    def find[T](mo:MongoObject)(implicit con:MongoConverter[T]): Either[MongoError, Seq[T]] = {
      wrapWith{
        val mc:MongoCursor = newdbc.find(mo.toDBObject)
        mc.toSeq[T]
      }
    }

    def save(mo:MongoObject): Either[MongoError, Unit] = {
      import MongoTypes.MongoWriteResult._
      dbc.save(mo.toDBObject).getMongoError match {
        case None => Right()
        case Some(me) => Left(me)
      }
    }

    def saveToOption(mo:MongoObject): Option[String] = {
      import MongoTypes.MongoWriteResult._
      dbc.save(mo.toDBObject).getMongoError.map(me => me.message + " " + me.stackTrace)
    }

    def save[T](value:T)(implicit mc:MongoConverter[T]): Either[MongoError, Unit] =  save(mc.convert(value))

    def save3[T <% MongoObject](value:T): Option[String] = { saveToOption(value) }

    def update(query:MongoObject, upate:MongoObject, upsert:Boolean):Either[MongoError, Unit] = {
      import MongoTypes.MongoWriteResult._
      wrapWith {
        dbc.update(query.toDBObject, upate.toDBObject, upsert, false)
      } match {
        case Right(result) => result.getMongoError match { case None => Right();  case Some(me) => Left(me) }
        case Left(me) => Left(me)
      }
    }

    def findAndModify[T](query:MongoObject, sort:MongoObject, update:MongoObject, returnNew:Boolean)(implicit mc:MongoConverter[T]) : Either[MongoError, T] = {
      import MongoObject.empty
      wrapWith {
        mc.convert(dbc.findAndModify(query, empty, sort, false, update, returnNew, false))
      }
    }

    def drop: Either[MongoError, Unit] = wrapWith(dbc.drop)
  }

  object MongoCollection {
    import DBCollectionTrait._
    implicit def dbCollectionToMongoCollection(dbc:DBCollection): MongoCollection = MongoCollection(dbc, createDBCollectionTrait(dbc))
  }
}