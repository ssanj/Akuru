/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

/**
 * Captures all Mongo functionality. import MongoTypes._ to work with MongoDB.
 */
object MongoTypes extends MongoServerTrait with
                          MongoDatabaseTrait with
                          MongoCollectionTrait with
                          MongoObjectTrait with
                          MongoObjectIdTrait with
                          MongoCursorTrait with
                          MongoWriteResultTrait with
                          MongoErrorTrait with
                          WrapWithTrait with
                          MongoFunc with
                          MongoTools with
                          MongoRegEx with
                          DomainSupport {
}