/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import org.scalatest.FunSuite
import org.scalatest.matchers.ShouldMatchers

final class MongoWriteResultSuite extends FunSuite with ShouldMatchers with MongoWriteResultTrait {

  test("A MongoWriteResult should return Some(MongoError) if there is a error") {
    MongoWriteResult(new WriteResultTrait {
      def getError = Some("There was an error")
      def getLastErrorTrace = Some("Some exception was thrown")
      def getN = None
      def getFields = Map.empty[String,AnyRef]
    }).getMongoError match {
      case Some(me) => {
        me.message should equal ("There was an error")
        me.stackTrace should equal ("Some exception was thrown")
      }
      case None => fail("Expected a MongoError")
    }
  }

  test("A MongoWriteResult should return None if there are no errors") {
    MongoWriteResult(new WriteResultTrait{
      def getError = None
      def getLastErrorTrace = Some("blah") //Even if this has a value (and it shouldn't) the error is keyed off of the getError message.
      def getN = None
      def getFields = Map.empty[String,AnyRef]
    }).getMongoError match {
      case Some(me) => fail("Expected None but got Some(" + me + ")")
      case None =>
    }
  }

  test("A MongoWriteResult should return a Some(MongoError) of the Exception, if an Exception is thrown when accessing error information") {
    MongoWriteResult(new WriteResultTrait{
      def getError = throw new RuntimeException("boom!")
      def getLastErrorTrace = None
      def getN = None
      def getFields = Map.empty[String,AnyRef]
    }).getMongoError match {
      case Some(me) =>  me.message should equal ("boom!")
      case None => fail("Expected a Some(MongoError) but got None")
    }
  }
}