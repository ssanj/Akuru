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

  def save[T <% MongoObject : CollectionName](f:  => T)(col:String => MongoCollection): Option[String] = {
    col(implicitly[CollectionName[T]].name).save3(f)
  }

  def findOne[T](f: => MongoObject)(g: T => Option[String])(col:String => MongoCollection)
                (implicit f1: MongoObject => T, f2: CollectionName[T]): Option[String] = {
    col(implicitly[CollectionName[T]].name).findOne3[T](f).fold(l => Some(l), r => foldOption(r)(None:Option[String])(g))
  }

  def find[T](f: => MongoObject)(g: Seq[T] => Option[String])(col:String => MongoCollection)
                (implicit f1: MongoObject => T, f2: CollectionName[T]): Option[String] = {
    col(implicitly[CollectionName[T]].name).find3[T](f).fold(l => Some(l), r => g(r))
  }

  def update[T : CollectionName](f: => MongoObject)(r: => MongoObject)(col:String => MongoCollection): Option[String] = {
    col(implicitly[CollectionName[T]].name).update3(f, r)
  }

  def upsert[T : CollectionName](f: => MongoObject)(r: => MongoObject)(col:String => MongoCollection): Option[String] = {
    col(implicitly[CollectionName[T]].name).update3(f, r, true)
  }

  def drop[T : CollectionName](col:String => MongoCollection): Option[String] = {
    col(implicitly[CollectionName[T]].name).drop3
  }

  type UserFunction = (String => MongoCollection) => Option[String]

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
      }(e => Some(e.getMessage + " " + e.getStackTraceString))
    }

    def getServer: Either[String, MongoServer] = runSafelyWithEither(fserver.apply)

    def getDatabase(server:MongoServer): Either[String, MongoDatabase] = runSafelyWithEither(server.getDatabase(dbName))
  }

}