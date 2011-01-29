/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSuite
import MongoTypes.MongoObject._
import MongoTypes._
import com.mongodb.{DBObject, DBCursor}

final class MongoCollectionForFindSuite extends FunSuite with ShouldMatchers with MongoCollectionTrait with MongoDummies {

  import com.mongodb.DBCursor

  test("find should find all existing objects that match a criteria") {
    MongoCollection(null, ObjectsFoundDBCollection).find[Person](empty) match {
      case Left(me) => fail("Expected Right(Seq[Person]) but got Left(me) -> " + me.message)
      case Right(people) => people should equal (Seq(Person("jazzy", 6), Person("sam", 26)))
    }
  }

  test("find should return an empty Seq if there are no matches") {
    MongoCollection(null, ObjectsNotFoundDBCollection).find[Person](empty) match {
      case Left(me) => fail("Expected Right(Seq()) but got Left(me) -> " + me.message)
      case Right(people) => people.isEmpty should equal (true)
    }
  }

  test("find should return an error as a Left(MongoError)") {
    MongoCollection(null, ObjectsThrowingExceptions).find[Person](empty) match {
      case Left(me) => me.message should equal (ObjectsThrowingExceptions.message)
      case Right(people) => fail("Expected Left(MongoError) but got Right(" +  people + ")")
    }
  }

  trait DBCollectionForFindTrait extends DBCollectionTrait {
    def findOne(dbo:DBObject): Option[DBObject] = throw new IllegalStateException("findOne should not be called from this context")
  }

  private class TestDBCursor(items:MongoObject*) extends DBCursor(null, null, null) {
    import scala.collection.JavaConversions._
    override def iterator: java.util.Iterator[DBObject] = {
      val it:java.util.Iterator[DBObject] = items.map(_.toDBObject).toIterator
      it
    }
  }

  object ObjectsFoundDBCollection extends DBCollectionForFindTrait {
    def find(dbo:DBObject): DBCursor = new TestDBCursor(mongoObject("name" -> "jazzy", "age" -> 6), mongoObject("name" -> "sam", "age" -> 26))
  }

  object ObjectsNotFoundDBCollection extends DBCollectionForFindTrait {
    def find(dbo:DBObject): DBCursor = new TestDBCursor()
  }

  object ObjectsThrowingExceptions extends DBCollectionForFindTrait {
    val message = "find threw an Exception"
    def find(dbo:DBObject): DBCursor = throw new IllegalStateException(message)
  }
}
