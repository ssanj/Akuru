/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb._
import MongoTypes._

trait MongoFunc {

  def connect(db:String, col:String) : (MongoServer, MongoDatabase, MongoCollection) = {
      val server = new Mongo
      val database = server.getDB(db)
      val collection = database.getCollection(col)
      (server, database, collection)
  }

  def connect(db:String) : (MongoServer, MongoDatabase) = {
      val server = new Mongo
      val database = server.getDB(db)
      (server, database)
  }
}

