/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.Mongo
import MongoTypes._

trait MongoServerTrait {

  case class MongoServer(private val m:Mongo) {

    def this() = this(new Mongo)

    def getDatabase(name:String): MongoDatabase = m.getDB(name)
  }

  object MongoServer {
    implicit def mongoToMongoDB(m:Mongo): MongoServer = MongoServer(m)
  }
}