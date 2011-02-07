/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
import MongoTypes._

trait MongoFunctions extends Tools with DomainSupport {

  def withConnection(s:() => MongoServer)(dbName:String): FutureConnection =  FutureConnection(s, dbName)

  def onDatabase: String => FutureConnection = dbName => withConnection(createServer)(dbName)

  def createServer = () => new MongoServer

  def collectionName[T : CollectionName]: String = implicitly[CollectionName[T]].name

  def save[T <% MongoObject : CollectionName](f: => T): UserFunction = col => col(collectionName[T]).save3(f)

  def findOne[T : CollectionName, R >: MongoObject <% T](f: => MongoObject)(g: T => Option[String])(h: => Unit):UserFunction =
    col => col(collectionName[T]).findOne3[T](f).fold(l => Some(l), r => foldOption(r){h;None:Option[String]}(g))

  def ignoreError = () => {}

  def find[T](f: => MongoObject)(g: Seq[T] => Option[String])(col:String => MongoCollection)
                (implicit f1: MongoObject => T, f2: CollectionName[T]): Option[String] = {
    col(collectionName[T]).find3[T](f).fold(l => Some(l), r => g(r))
  }

  def update[T : CollectionName](f: => MongoObject)(r: => MongoObject)(col:String => MongoCollection): Option[String] = {
    col(collectionName[T]).update3(f, r)
  }

  def upsert[T : CollectionName](f: => MongoObject)(r: => MongoObject)(col:String => MongoCollection): Option[String] = {
    col(collectionName[T]).update3(f, r, true)
  }

  def drop[T : CollectionName](col:String => MongoCollection): Option[String] = {
    col(collectionName[T]).drop3
  }

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

}
