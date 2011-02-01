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

    import _root_.akuru.AkuruMain.{DomainObject, Versioned}
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

    def save[T](value:T)(implicit mc:MongoConverter[T]): Either[MongoError, Unit] =  save(mc.convert(value))

    def save2[T <: DomainObject[T]](domain: => T)(implicit con:T => MongoObject): Either[String, Unit] = {
        import MongoWriteResult._
        runSafelyWithEither{
          dbc.save(con(domain))
        } match {
          case Left(msg) => Left(msg)
          case Right(wr) => wr.getMongoError.map(_.message).toLeft({})
        }
    }

//    def save2[T <: DomainObject[T]](f: T)(implicit con:T => MongoObject, blah:WriteResult => MongoWriteResult, con2: MongoObject => T): Either[MongoError, T] = {
//      import MongoObject._
//      println("called")
//      if (f.id.isDefined) {
//          val find = mongoObject("_id" -> toObjectId(f.id), "version" -> f.version)
//          val update = con(f.createUpdatedVersion)
//          println("called1")
//          findAndModify2(find , empty, update)
//      }
//      else {
//        println("called2")
//          runSafelyWithEitherCustomError[MongoError, MongoWriteResult]{ dbc.save(con(f)) }{e => MongoError(e.getMessage, e.getStackTraceString)}.
//                  fold(l => Left(l), r => r.getMongoError match {
//            case None => Right(f)
//            case error @ Some(me) => error.toLeft(f)
//          })
//      }
//    }

    def someSave[T <: DomainObject[T], R](f:(MongoCollection, MongoObject) => R)(t:T)(col:() => MongoCollection)(implicit con:T => MongoObject): Either[String, R] = {
      runSafelyWithEither{
        f(col.apply, con(t))
      }
    }

    def update(query:MongoObject, upate:MongoObject, upsert:Boolean):Either[MongoError, Unit] = {
      import MongoTypes.MongoWriteResult._
      wrapWith {
        dbc.update(query.toDBObject, upate.toDBObject, upsert, false)
      } match {
        case Right(result) => result.getMongoError match { case None => Right();  case Some(me) => Left(me) }
        case Left(me) => Left(me)
      }
    }

    def findAndModify[T](query:MongoObject, sort:MongoObject, update:MongoObject, returnNew:Boolean)(implicit mc:MongoConverter[T]) :
      Either[MongoError, T] = {
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
