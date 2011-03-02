/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.DBCollection
import MongoTypes._
import scala.Either

trait MongoCollectionTrait extends MongoFunctions { this:Tools =>

  //TODO:Once all methods ar tested remove dbc and replace with newdbc.
  case class MongoCollection(dbc:DBCollection, newdbc:DBCollectionTrait) {

    import com.mongodb.WriteResult

   def save[T <: DomainObject : DomainToMongo](value: => T, handler: MongoWriteResult => Option[String]): Option[String] =
     writeResultToOption(() => dbc.save(value.toDBObject), handler)

    def findOne[T <: DomainObject : MongoToDomain](mo:MongoObject): Either[String, Option[T]] = {
      runSafelyWithEither { nullToOption(dbc.findOne(mo.toDBObject)).flatMap(t => implicitly[MongoToDomain[T]].apply(t)) }
    }

    def find[T <: DomainObject : MongoToDomain](mo:MongoObject)(f: MongoCursor => MongoCursor): Either[String, Seq[T]] = {
      runSafelyWithEither { f(dbc.find(mo)).asSeq[T] }
    }

    def update3(query:MongoObject, update:MongoObject, upsert:Boolean = false, multi:Boolean = false, handler: MongoWriteResult => Option[String]):
      Option[String] = {
      writeResultToOption(() => dbc.update(query.toDBObject, update.toDBObject, upsert, multi), handler)
    }

    def writeResultToOption(f:() => WriteResult, g: MongoWriteResult => Option[String]): Option[String] =  {
      runSafelyWithEither(f.apply).fold(l => Some(l), r => g(r))
    }

    def findAndModify[T <: DomainObject : MongoToDomain](query:MongoObject, sort:MongoObject, remove:Boolean, update:MongoObject,
      returnNew:Boolean, upsert:Boolean): Either[String, Option[T]] = {
      import MongoObject.empty
      runSafelyWithEither {
        foldOption(
          nullToOption(dbc.findAndModify(query, empty, sort, remove, update, returnNew, upsert))){
          None:Option[T]}{t => implicitly[MongoToDomain[T]].apply(t) }
      }
    }

    def drop3: Option[String] = runSafelyWithOptionReturnError(dbc.drop)
  }

  object MongoCollection {
    import DBCollectionTrait._
    implicit def dbCollectionToMongoCollection(dbc:DBCollection): MongoCollection = MongoCollection(dbc, createDBCollectionTrait(dbc))
  }
}