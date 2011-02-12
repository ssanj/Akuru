/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.empty

trait TestDomainObjects {

  lazy val mongoCreationException = "Exceptional MongoObject"

  def createExceptionalMongoObject: MongoObject = throw new RuntimeException(mongoCreationException)

  case class Person(override val id:Option[MongoObjectId] = None, name:String) extends DomainObject

  object Person {

    val name = "name"

    implicit def personToMongo(p:Person): MongoObject = empty

    implicit def mongoToPerson(mo:MongoObject): Person = Person(None, name = "testing")

    lazy val expectedError = "no person collection here!"

    implicit object PersonCollectionName extends CollectionName[Person] {

      lazy val name = throw new RuntimeException(expectedError)
    }
  }
}