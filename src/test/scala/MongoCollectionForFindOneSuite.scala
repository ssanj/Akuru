/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.DBObject
import MongoTypes.MongoObject._

final class MongoCollectionForFindOneSuite extends FunSuite with ShouldMatchers with MongoCollectionTrait with MongoDummies {

  test("A MongoCollection should findOne existing object") {
    MongoCollection(null, ObjectFoundDBCollection).findOne[Person](empty) should equal (Right(Some(Person("sanj", 36))))
  }

  test("A MongoCollection should not findOne non-existant object") {
    MongoCollection(null, ObjectNotFoundDBCollection).findOne[Person](empty) should equal (Right(None))
  }

  test("A MongoCollection should handle findOne with Exceptions") {
    MongoCollection(null, ObjectThrowsExceptionDBCollection).findOne[Person](empty) match {
      case Left(me) => me.message should equal ("throwing an Exy")
      case Right(x) => fail("Expected an Left(MongoError) but got a Right(" + x + ")")
    }
  }

  trait FindOneDBCollectionTrait extends DBCollectionTrait {

    import com.mongodb.DBCursor
    def find(dbo:DBObject): DBCursor = throw new IllegalStateException("find should not be tested in this context")
  }

  object ObjectFoundDBCollection extends FindOneDBCollectionTrait {
    def findOne(q:DBObject): Option[DBObject] = Some(mongoObject("name" -> "sanj", "age" -> 36).toDBObject)
  }

  object ObjectNotFoundDBCollection extends FindOneDBCollectionTrait {
    def findOne(q:DBObject): Option[DBObject] = None
  }

  object ObjectThrowsExceptionDBCollection extends FindOneDBCollectionTrait {
    def findOne(q:DBObject): Option[DBObject] = throw new RuntimeException("throwing an Exy")
  }
}