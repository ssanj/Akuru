/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.Mongo

trait MongoServerTrait {

  case class MongoServer(private val m:Mongo)

  object MongoServer {
    implicit def mongoToMongoDB(m:Mongo): MongoServer = MongoServer(m)
  }
}