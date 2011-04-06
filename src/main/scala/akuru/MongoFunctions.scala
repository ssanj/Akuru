/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
import MongoTypes.MongoSortObject
import MongoTypes.MongoObject.empty
import MongoTypes.MongoServer
import MongoTypes.MongoCursor
import MongoTypes.UpdateObject
import MongoTypes.MongoWriteResult
import MongoTypes.MongoDatabase
import MongoTypes.MongoCollection
import Tools._

trait MongoFunctions { this:SideEffects  =>

  def withConnection(s:() => MongoServer)(dbName:String): FutureConnection =  FutureConnection(s, dbName)

  def onDatabase: String => FutureConnection = dbName => withConnection(createServer)(dbName)

  def createServer = () => new MongoServer

  def collectionName[T <: DomainObject : CollectionName]: String = implicitly[CollectionName[T]].name

  def safeSave[T <: DomainObject : DomainToMongo : CollectionName](f: => T)(g: MongoWriteResult => Option[String]): UserFunction =
    col => col(collectionName[T]).save[T](f, g)

  def save[T <: DomainObject : DomainToMongo : CollectionName](f: => T): UserFunction = col => col(collectionName[T]).save[T](f, defaultHandler)

  def mfind[T <: DomainObject : CollectionName : MongoToDomain](f: => MongoObject)(c: MongoCursor => MongoCursor)(g: Seq[T] => Option[String])
                                                              (h: => Option[String]):
    UserFunction = col =>  col(collectionName[T]).find[T](f)(c).fold(l => Some(l), r => if (r.isEmpty) h else g(r))

  def msafeUpdate[T <: DomainObject : CollectionName](q: => MongoObject)(u: => UpdateObject[T])(g: MongoWriteResult => Option[String])
                                                     (multiple: => Boolean)(up: => Boolean): UserFunction =
    col => col(collectionName[T]).update3(query = q, update = u.value, handler = g, multi = multiple, upsert = up)

  def mfindAndModifyAndRemove[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject)(sort: => MongoSortObject)
      (f: T => Option[String])(h: => Option[String]): UserFunction = {
      col => col(collectionName[T]).findAndModify[T](query, sort.value, true, empty, true, false).fold(l => Some(l), r=> foldOption(r){h}(f))
  }

  def mfindAndModifyAndReturn[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject)(sort: => MongoSortObject
          )(upsert: => Boolean)(update: => UpdateObject[T])(f: T => Option[String])(h: => Option[String]): UserFunction =
  { col => col(collectionName[T]).findAndModify[T](query, sort.value, false, update.value, true, upsert).fold(l => Some(l), r=> foldOption(r){h}(f)) }

  def noOp: Option[String] = None

  def all(mc:MongoCursor): MongoCursor = identity(mc)

  def ignoreSuccess: Option[String] = None:Option[String]

  def drop[T <: DomainObject : CollectionName]: UserFunction = col => col(collectionName[T]).drop3

  type UserFunction = CollectionFunction => Option[String]

  type CollectionFunction = (String => MongoCollection)

  case class FutureConnection(fserver:() => MongoServer, dbName:String, items:List[UserFunction] = Nil) {

    def ~~>(uf:UserFunction): FutureConnection = ~~>(List(uf))

    def ~~>(f:List[UserFunction]): FutureConnection = FutureConnection(fserver, dbName,  f ::: items)

    def ~~>() : Option[String] = {
      runSafelyWithDefault {
        getServer.right.map{ server => addOption(processUserFunctions(server), closeServerConnection(server))(addWithNewLine) } match {
          case Right(e @ Some(error)) => e
          case Right(n @ None) => n
          case Left(e) => Some(e)
        }
      }(e => Some(addWithNewLine(e.getMessage + " --> ", e.getStackTraceString))) //if an exception is thrown.
    }

    private def processUserFunctions(server:MongoServer): Option[String] = {
      runSafelyWithEither {
        getDatabase(server).right.map(db => items.foldRight(None:Option[String]){(t, a) => if (!a.isDefined) t(db.getCollection(_)) else a})
      } match {
        case Right(Right(e @ Some(error))) => e
        case Right(Right(None)) => None
        case Right(Left(error)) => Some(error)
        case Left(exception) => Some(exception) //if an exception was thrown.
      }
    }

    private def closeServerConnection(server:MongoServer): Option[String] =  runSafelyWithOptionReturnError(server.close)

    def getServer: Either[String, MongoServer] = runSafelyWithEither(fserver.apply)

    def getDatabase(server:MongoServer): Either[String, MongoDatabase] = runSafelyWithEither(server.getDatabase(dbName))
  }

  def defaultHandler(wr:MongoWriteResult): Option[String] = wr.getStringError
}