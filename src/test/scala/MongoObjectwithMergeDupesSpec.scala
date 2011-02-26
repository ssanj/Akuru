/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoObjectwithMergeDupesSpec extends CommonSpec {

  import Blog._
  import MongoTypes.MongoObject
  import MongoTypes.MongoObject.mongo

  "A MongoObject" should "should merge documents with no dupes" in {
    val mo1:MongoObject = mongo.putPrimitive(titleField("blah"))
    val mo2:MongoObject = mongo.putPrimitiveArray[String](labelsField(Seq("test")))
    val mo3 = mo1.mergeDupes(mo2)

    mo3.getPrimitive(titleField) should equal ("blah")
    mo3.getPrimitiveArray(labelsField) should equal (Seq("test"))
  }

  it should "merge dupes" in {
    val mo1:MongoObject = mongo.putMongo("$sort", mongo.putPrimitive("key1", "blah"))
    val mo2:MongoObject = mongo.putMongo("$sort", mongo.putPrimitive("key2", "bleee"))
    val mo3 = mo1.mergeDupes(mo2)
    mo3 should equal (mongo.putMongo("$sort", mongo.putPrimitive("key1", "blah").putPrimitive("key2", "bleee")))
  }
}