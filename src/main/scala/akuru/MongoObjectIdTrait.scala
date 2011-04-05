/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import org.bson.types.ObjectId

trait MongoObjectIdTrait {

  case class MongoObjectId(id:ObjectId) {
    def toObjectId: ObjectId = id
  }

  object MongoObjectId {
    implicit def objectIdToMongoObjectId(id:ObjectId): MongoObjectId = MongoObjectId(id)

    implicit def stringToMongoObjectId(id:String): MongoObjectId = MongoObjectId(new ObjectId(id))
  }

}