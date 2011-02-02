/*
  * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._

object AkuruMain extends DomainObjects with Tools with SideEffects {


  def main(args: Array[String]) {
    val labelSeq = List("work", "movement")
    val blog = Blog(title = "Lessons learned", labels = labelSeq.toSeq)
    val result = (withAkuru
                    on("blog", save(blog) _)
                    on("label", labelSeq map (l => save(Label(value = l)) _))
                  run) getOrElse("success >>")
    println(result)
  }

  def withAkuru(): FutureConnection = withConnection(createServer)("akuru")

  def withConnection(s:() => MongoServer)(dbName:String): FutureConnection =  FutureConnection(s, dbName)

  def createServer = () => new MongoServer

  def save[T <% MongoObject : CollectionName](f:  => T)(col:MongoCollection):Option[String] = { col.save3(f) }

  type UserFunction = MongoCollection => Option[String]

  type ColToUserFunction = (String, UserFunction)

  case class FutureConnection(fserver:() => MongoServer, dbName:String, items:List[ColToUserFunction] = Nil) {

    def on(col:String, uf:UserFunction): FutureConnection = on(col, List(uf))

    def on(col:String, f:List[UserFunction]): FutureConnection = f.foldLeft(this)((a,b) => FutureConnection(fserver, dbName, (col, b) :: items))

    def run: Option[String] = {
      runSafelyWithDefault{
        getServer.right.flatMap(getDatabase(_).right.map(db => items.foldRight(None:Option[String]){(t, a) =>
          if (!a.isDefined) t._2(db.getCollection(t._1)) else a})) match {
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