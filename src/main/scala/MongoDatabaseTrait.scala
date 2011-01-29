/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.DB
import MongoTypes.MongoCollection

trait MongoDatabaseTrait {

    case class MongoDatabase(private val db:DB) {

    def getCollection(key:String): MongoCollection = db.getCollection(key)

    def drop { db.dropDatabase }
  }

  object MongoDatabase {
      implicit def dbToMongoDatabase(db:DB): MongoDatabase = MongoDatabase(db)
  }
}
