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
    val result = onAkuru.
                  add("blog"){ save(blog) }.
                  addAll("label"){ labelSeq map (l => save(Label(value = l)) _) }.
                  run.getOrElse("success >>")
    println(result)
  }

  def onAkuru: FutureConnection = withConnection(createServer)("akuru")

  def withConnection(s:() => MongoServer)(dbName:String): FutureConnection =  FutureConnection(s, dbName)

  def createServer = () => new MongoServer

  def save[T <% MongoObject](f:  => T)(col:MongoCollection):Option[String] = { col.save3(f) }

  type UserFunction = MongoCollection => Option[String]

  type ColToUserFunction = (String, UserFunction)

  case class FutureConnection(fserver:() => MongoServer, dbName:String, items:List[ColToUserFunction] = Nil) {

    def add(col:String)(f:UserFunction): FutureConnection = FutureConnection(fserver, dbName, (col, f) :: items)

    def addAll(col:String)(f:List[UserFunction]): FutureConnection = f.foldLeft(this)((a,b) => a.add(col)(b))

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