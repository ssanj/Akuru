/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

final class MongoObjectWithGetMongoObjectSpec extends CommonSpec {

  import MongoTypes.MongoObject.mongo
  "A MongoObject with getMongoObject" should "return None for a key that does not exist" in {
    mongo getMongoObject ("blah") should equal (None)
  }

  it should "return None if the key is not a MongoObject" in {
    val m = mongo putPrimitiveObject ("blah", 100)
    m getMongoObject ("blah") should equal (None)
  }

  it should  "return Some(Value) if the key is present and the value is a MongoObject" in {
    val m1 = mongo putPrimitiveObject ("count", 100)
    val m2 = mongo putMongo ("blah", m1)

    m2 getMongoObject ("blah") should equal (Some(m1))
  }
}