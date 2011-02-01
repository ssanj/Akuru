/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject._

object AkuruMain extends Tools {

  trait Ided {
    val id:Option[MongoObjectId]
  }

  trait Versioned {

    val version:Int

    def nextVersion: Int = version + 1
  }

  trait DomainObject[T] extends Ided with Versioned {
    def createUpdatedVersion:T
  }

  case class Blog(override val id:Option[MongoObjectId] = None, title:String, labels:Seq[String], override val version:Int = 1) extends DomainObject[Blog] {
    override def createUpdatedVersion:Blog = Blog(id, title, labels, version = nextVersion)
  }

  case class Label(override val id:Option[MongoObjectId] = None, value:String, override val version:Int = 1) extends DomainObject[Label] {
    override def createUpdatedVersion:Label = Label(id, value, version = nextVersion)
  }


  implicit def mongoToBlogConverter(mo:MongoObject): Blog = {
    Blog(Some(mo.getId), mo.get[String]("title"), Seq.empty, mo.get[Int]("version"))
  }

  implicit def blogToMongoConverter(domain:Blog): MongoObject = {
      val mo = empty
      domain.id.foreach(mo.putId)
      mo.put("title", domain.title)
      mo.putArray2("labels", domain.labels)
      mo.put("version", domain.version)
      mo
  }

  implicit def mongoToLabelConverter(mo:MongoObject): Label = Label(Some(mo.getId), mo.get[String]("value"))

  implicit def labelToMongoConverter(domain:Label): MongoObject = {
      val mo = empty
      domain.id.foreach(mo.putId)
      mo.put("value", domain.value)
      mo
  }

  def main(args: Array[String]) {
    val blog = Blog(title = "Lessons learned", labels = Seq("work", "movement"))
    workOnBlog[Unit](blog){
      (c, d) => doSave(d)(c.col)
    }.right.flatMap{r =>
      reduce(blog.labels.map(l => workOnLabel[Unit](Label(value = l)){ (c,d) => doSave(d)(c.col) })).toLeft({})
    }.fold(l => println("error ->" + l), r => println("success"))
  }

  def reduce(seq:Seq[Either[String, Unit]]): Option[String] = {
      seq.filter(e => e.isLeft) match {
        case Seq() => None
        case Seq(Left(error)) => Some(error)
        case s @ Seq(Left(e1), Left(_)) => stringToOption(s.tail.foldLeft(e1)((a, b) => a + ("\n" + b.left.get)))
        case x @ _ => Some("Unexpected match ->  " + x)
      }
  }

  def workOnBlog[R] = workOnDomain[R, Blog]("blog")

  def workOnLabel[R] = workOnDomain[R, Label]("label")

  def workOnDomain[R, T]: String => (=> T) => ((Connection, T) => R) => Either[String, R] = col => doWork[T, R](onAkuru(col))

  def onAkuru: String => PossibleConnection = withConnection(createServer)("akuru")

  def doWork[P, R](pc:PossibleConnection)(p: => P)(f:(Connection, P) => R): Either[String, R] = {
    runSafelyWithEither[R]{
      f(pc.apply, p)
    }
  }

  def withConnection(s:() => MongoServer)(dbName:String)(colName:String): PossibleConnection = () => {
    val server = s.apply
    val db = server.getDatabase(dbName)
    val col = db.getCollection(colName)
    Connection(server, db, col)
  }

  def createServer = () => new MongoServer


  def doSave[T <: DomainObject[T]](t:T)(col: => MongoCollection)(implicit con:T => MongoObject):
    Either[String, Unit] = runSafelyWithEither(col.save2(t))

  case class Connection(server:MongoServer, db:MongoDatabase, col:MongoCollection)

  type PossibleConnection = () => Connection
}