/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._

object AkuruMain extends DomainObjects with Tools with SideEffects {


  def main(args: Array[String]) {
    val labelList = List("work", "movement")
    val blog = Blog(title = "Lessons learned", labels = labelList.toSeq)
    val result = (withAkuru ->
                    (save(blog)) ->>
                    (labelList.map(l => save(Label(value = l)) _))
                  run) getOrElse("success >>")
    println(result)
  }

  def withAkuru: FutureConnection = withConnection(createServer)("akuru")

  def withConnection(s:() => MongoServer)(dbName:String): FutureConnection =  FutureConnection(s, dbName)

  def createServer = () => new MongoServer

  def save[T <% MongoObject : CollectionName](f:  => T)(col:String => MongoCollection):Option[String] = {
    col(implicitly[CollectionName[T]].name).save3(f)
  }

  type UserFunction = (String => MongoCollection) => Option[String]

  case class FutureConnection(fserver:() => MongoServer, dbName:String, items:List[UserFunction] = Nil) {

    def ->(uf:UserFunction): FutureConnection = ->>(List(uf))

    def ->>(f:List[UserFunction]): FutureConnection = FutureConnection(fserver, dbName,  f ::: items)

    def run: Option[String] = {
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