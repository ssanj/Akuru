/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoObjectwithMergeDupesSpec extends CommonSpec {

  import Blog._
  import MongoObject.mongo

  "A MongoObject" should "merge documents with no dupes" in {
    val mo1:MongoObject = mongo.putPrimitiveObject(titleField("blah"))
    val mo2:MongoObject = mongo.putPrimitiveObjects[String](labelsField(Seq("test")))
    val mo3 = mo1.mergeMongoObjectValues(mo2)

    mo3.getPrimitiveObject(titleField) should equal (Some("blah"))
    mo3.getPrimitiveObjects(labelsField) should equal (Some(Seq("test")))
  }

  it should "merge dupes" in {
    val mo1:MongoObject = mongo.putMongo("$sort", mongo.putPrimitiveObject("key1", "blah"))
    val mo2:MongoObject = mongo.putMongo("$sort", mongo.putPrimitiveObject("key2", "bleee"))
    val mo3 = mo1.mergeMongoObjectValues(mo2)
    mo3 should equal (mongo.putMongo("$sort", mongo.putPrimitiveObject("key1", "blah").putPrimitiveObject("key2", "bleee")))
  }

  it should "merge dupes and non-dupes" in {
    val mo1:MongoObject = mongo.putMongo("$sort",
      mongo.putPrimitiveObject("key1", "blah")).putPrimitiveObject("key3", "blue").putPrimitiveObject("key4", "boo")

    val mo2:MongoObject =
      mongo.putPrimitiveObject("key5", "test").putMongo("$sort", mongo.putPrimitiveObject("key2", "bleee")).putPrimitiveObject("key6", "done")

    val mo3 = mo1.mergeMongoObjectValues(mo2)

    mo3.getMongoObject("$sort") should equal (Some(mongo.putPrimitiveObject("key1", "blah").putPrimitiveObject("key2", "bleee")))
    mo3.getPrimitiveObject[String]("key3") should equal (Some("blue"))
    mo3.getPrimitiveObject[String]("key4") should equal (Some("boo"))
    mo3.getPrimitiveObject[String]("key5") should equal (Some("test"))
    mo3.getPrimitiveObject[String]("key6") should equal (Some("done"))
  }

  it should "overwrite values in duplicate keys within MongoObject values for duplicate keys" in {
    val mo1:MongoObject = mongo.putMongo("$sort", mongo.putPrimitiveObject("key1", "blah"))
    val mo2:MongoObject = mongo.putMongo("$sort", mongo.putPrimitiveObject("key1", "bleee"))
    val mo3 = mo1.mergeMongoObjectValues(mo2)
    mo3 should equal (mo2)
  }

  it should "return original key/values if the values of duplicate keys are not MongoObjects" in {
    val mo1:MongoObject = mongo.putPrimitiveObject("key1", "blah")
    val mo2:MongoObject = mongo.putPrimitiveObject("key1", "bleee")
    val mo3 = mo1.mergeMongoObjectValues(mo2)
    mo3 should equal (mo1)
  }
}