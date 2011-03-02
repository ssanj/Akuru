/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoObjectwithMergeDupesSpec extends CommonSpec {

  import Blog._
  import MongoTypes.MongoObject
  import MongoTypes.MongoObject.mongo

  "A MongoObject" should "merge documents with no dupes" in {
    val mo1:MongoObject = mongo.putPrimitive(titleField("blah"))
    val mo2:MongoObject = mongo.putPrimitiveArray[String](labelsField(Seq("test")))
    val mo3 = mo1.mergeDupes(mo2)

    mo3.getPrimitive(titleField) should equal (Some("blah"))
    mo3.getPrimitives(labelsField) should equal (Some(Seq("test")))
  }

  it should "merge dupes" in {
    val mo1:MongoObject = mongo.putMongo("$sort", mongo.putPrimitive("key1", "blah"))
    val mo2:MongoObject = mongo.putMongo("$sort", mongo.putPrimitive("key2", "bleee"))
    val mo3 = mo1.mergeDupes(mo2)
    mo3 should equal (mongo.putMongo("$sort", mongo.putPrimitive("key1", "blah").putPrimitive("key2", "bleee")))
  }

  it should "merge dupes and non-dupes" in {
    val mo1:MongoObject = mongo.putMongo("$sort", mongo.putPrimitive("key1", "blah")).putPrimitive("key3", "blue").putPrimitive("key4", "boo")
    val mo2:MongoObject = mongo.putPrimitive("key5", "test").putMongo("$sort", mongo.putPrimitive("key2", "bleee")).putPrimitive("key6", "done")
    val mo3 = mo1.mergeDupes(mo2)

    mo3.getMongoObject("$sort") should equal (Some(mongo.putPrimitive("key1", "blah").putPrimitive("key2", "bleee")))
    mo3.getPrimitive[String]("key3") should equal (Some("blue"))
    mo3.getPrimitive[String]("key4") should equal (Some("boo"))
    mo3.getPrimitive[String]("key5") should equal (Some("test"))
    mo3.getPrimitive[String]("key6") should equal (Some("done"))
  }
}