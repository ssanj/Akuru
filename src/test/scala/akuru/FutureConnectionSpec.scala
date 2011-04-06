/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import MongoTypes.MongoServer
import MongoTypes.MongoDatabase
import MongoTypes.MongoCollection

final class FutureConnectionSpec extends CommonSpec {

  "A FutureConnection" should "be immutable" in {
    val fc1 = createFutureConnection
    val fc2 = fc1 ~~> (createUserFunction)
    val fc3 = fc2 ~~> (List(createUserFunction))

    fc1.items.size should equal (0)
    fc1 should not be theSameInstanceAs (fc2)

    fc2.items.size should equal (1)
    fc2 should not be theSameInstanceAs (fc3)

    fc3.items.size should equal (2)
    fc3 should not be theSameInstanceAs (fc1)
  }

  it should "add UserFunctions at the front" in {
    val fc1 = createFutureConnection
    val fc2 = fc1 ~~> (createUserFunctionWithIndex(1)) ~~> createUserFunctionWithIndex(2) ~~> createUserFunctionWithIndex(3)
    fc2.items.map((t:UserFunction) => t(collection)).flatten should equal (Seq("3", "2", "1"))
  }

  it should "execute valid UserFunctions" in {
    val result = createFutureConnection ~~> createUserFunction ~~> createUserFunction ~~> createUserFunction ~~>()
    result should equal (None)
  }

  it should "return the first exception encountered" in {
    ( createFutureConnection ~~>
                      createUserFunction ~~>
                      createExceptionalUserFunction("error1") ~~>
                      createExceptionalUserFunction("error2") ~~>
                      createUserFunction
    ) ~~>() verifyError(_ should equal ("error1"))
  }

  it should "return the first error encountered" in {
    ( createFutureConnection ~~>
                      createUserFunction ~~>
                      createErroneousUserFunction("some error1") ~~>
                      createExceptionalUserFunction("some error2") ~~>
                      createUserFunction
    ) ~~>() verifyError(_ should equal ("some error1"))

  }

  private def createUserFunction: UserFunction = fc => None

  private def createExceptionalUserFunction(msg:String): UserFunction = fc => throw new RuntimeException(msg)

  private def createErroneousUserFunction(msg:String): UserFunction = fc => Some(msg)

  private def collection(name:String): MongoCollection = new TestMongoCollection

  private def createUserFunctionWithIndex(n:Int): UserFunction = fc => Some(n.toString)

  private def createFutureConnection: FutureConnection = new FutureConnection(() => new TestMongoServer, "blah")

  private class TestMongoServer extends MongoServer {
    override def getDatabase(name:String): MongoDatabase = new TestMongoDatabase
  }

  private class TestMongoDatabase extends MongoDatabase(null) {
    override def getCollection(key:String): MongoCollection = new TestMongoCollection
  }

  private class TestMongoCollection extends MongoCollection(null, null)
}