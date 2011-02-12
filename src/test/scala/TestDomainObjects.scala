/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.empty

trait TestDomainObjects {

  case class Blog(override val id:Option[MongoObjectId] = None, title:String, labels:Seq[String] = Seq[String]()) extends DomainObject

  case class Label(override val id:Option[MongoObjectId] = None, value:String) extends DomainObject

  case class Blah()

  object Blog {

    val title = "title"
    val labels = "labels"

    implicit def mongoToBlogConverter(mo:MongoObject): Blog = {
      Blog(Some(mo.getId), mo.getPrimitive[String](title), mo.getPrimitiveArray[String](labels))
    }

    implicit def blogToMongoConverter(domain:Blog): MongoObject =
      putDomainId(domain).putPrimitive[String](title, domain.title).putPrimitiveArray[String](labels, domain.labels)

    implicit object BlogCollection extends CollectionName[Blog] {
      override val name = "blog"
    }
  }

  object Label {

    val value = "value"

    implicit def mongoToLabelConverter(mo:MongoObject): Label = Label(Some(mo.getId), mo.getPrimitive[String](value))

    implicit def labelToMongoConverter(domain:Label): MongoObject = putDomainId(domain).putPrimitive[String](value, domain.value)

    implicit object LabelCollection extends CollectionName[Label] {
      override val name = "label"
    }
  }

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