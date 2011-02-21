/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
import MongoTypes._
import MongoTypes.MongoObject.SortObjectJoiner
import MongoTypes.MongoObject.empty

trait MongoFunctions { this:Tools  =>

  def withConnection(s:() => MongoServer)(dbName:String): FutureConnection =  FutureConnection(s, dbName)

  def onDatabase: String => FutureConnection = dbName => withConnection(createServer)(dbName)

  def createServer = () => new MongoServer

  def collectionName[T <: DomainObject : CollectionName]: String = implicitly[CollectionName[T]].name

  def safeSave[T <: DomainObject : DomainToMongo : CollectionName](f: => T)(g: MongoWriteResult => Option[String]): UserFunction =
    col => col(collectionName[T]).save[T](f, g)

  def save[T <: DomainObject : DomainToMongo : CollectionName](f: => T): UserFunction = col => col(collectionName[T]).save[T](f, defaultHandler)

  def findOne[T <: DomainObject : CollectionName : MongoToDomain](f: => MongoObject)(g: T => Option[String])(h: => Unit):UserFunction =
    col => col(collectionName[T]).findOne[T](f).fold(l => Some(
      l), r => foldOption(r){h;None:Option[String]}(g))

  def findAndModifyAndReturn[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject)(sort: => SortObjectJoiner
          )(update: => UpdateObject)(f: T => Option[String])(h: => Option[String]): UserFunction =
  { col => col(collectionName[T]).findAndModify[T](query, sort.done, false, update.value, true, false).fold(l => Some(l), r=> foldOption(r){h}(f)) }

  def findAndModifyAndRemove[T <: DomainObject : CollectionName : MongoToDomain](query: => MongoObject)(sort: => SortObjectJoiner)
      (f: T => Option[String])(h: => Option[String]): UserFunction = {
      col => col(collectionName[T]).findAndModify[T](query, sort.done, true, empty, false, false).fold(l => Some(l), r=> foldOption(r){h}(f))
  }

  def ignoreError = () => {}

  def ignoreSuccess: Option[String] = None:Option[String]

  def find[T <: DomainObject : CollectionName : MongoToDomain](f: => MongoObject)(g: Seq[T] => Option[String]): UserFunction =
    col => col(collectionName[T]).find[T](f).fold(l => Some(l), r => g(r))

  def update[T <: DomainObject : CollectionName](q: => MongoObject)(u: => UpdateObject): UserFunction =
    col => col(collectionName[T]).update3(query = q, update = u.value, handler = defaultHandler)

  def safeUpdate[T <: DomainObject : CollectionName](q: => MongoObject)(u: => UpdateObject)(g: MongoWriteResult => Option[String]): UserFunction =
    col => col(collectionName[T]).update3(query = q, update = u.value, handler = g)

  def upsert[T <: DomainObject : CollectionName : DomainToMongo](q: => MongoObject)(u: => T): UserFunction =
    col => col(collectionName[T]).update3(q, u, true, handler = defaultHandler)

  def safeUpsert[T <: DomainObject : CollectionName: DomainToMongo](q: => MongoObject)(u: => T)(g: MongoWriteResult => Option[String]):
    UserFunction = col => col(collectionName[T]).update3(q, u, true, handler = g)

  def drop[T <: DomainObject : CollectionName]: UserFunction = col => col(collectionName[T]).drop3

  type UserFunction = CollectionFunction => Option[String]

  type CollectionFunction = (String => MongoCollection)

  case class FutureConnection(fserver:() => MongoServer, dbName:String, items:List[UserFunction] = Nil) {

    def ~~>(uf:UserFunction): FutureConnection = ~~>(List(uf))

    def ~~>(f:List[UserFunction]): FutureConnection = FutureConnection(fserver, dbName,  f ::: items)

    def ~~>() : Option[String] = {
      runSafelyWithDefault{
        getServer.right.flatMap(getDatabase(_).right.map(db => items.foldRight(None:Option[String]){(t, a) =>
          if (!a.isDefined) t(db.getCollection(_)) else a})) match {
          case Right(e @ Some(error)) => e
          case Right(n @ None) => n
          case Left(e @ error) => Some(e)
        }
      }(e => Some(addWithNewLine(e.getMessage + " --> ", e.getStackTraceString)))
    }

    def getServer: Either[String, MongoServer] = runSafelyWithEither(fserver.apply)

    def getDatabase(server:MongoServer): Either[String, MongoDatabase] = runSafelyWithEither(server.getDatabase(dbName))
  }

  def defaultHandler(wr:MongoWriteResult): Option[String] = wr.getStringError
}