/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.empty
import DomainObject._

trait TestDomainObjects {

  case class Blog(override val id:FieldValue[MID] = defaultId,
                  title:FieldValue[String],
                  labels:FieldValue[Seq[String]] = Blog.labelsField(Seq[String]())) extends DomainObject

  object Blog {

    val titleField = Field[String]("title")
    val labelsField = Field[Seq[String]]("labels")

    implicit def mongoToBlogConverter(mo:MongoObject): Blog =  {
      Blog(idField(Some(mo.getId)), titleField(mo.getPrimitive(titleField)), labelsField(mo.getPrimitiveArray(labelsField)))
    }

    implicit def blogToMongoConverter(domain:Blog): MongoObject = putDomainId(domain).putPrimitive(domain.title).putPrimitiveArray(domain.labels)

    implicit object BlogCollection extends CollectionName[Blog] {
      override val name = "blog"
    }
  }

  case class Label(override val id:FieldValue[MID] = defaultId, value:FieldValue[String]) extends DomainObject

  object Label {

    val valueField = Field[String]("value")

    implicit def mongoToLabelConverter(mo:MongoObject): Label = Label(idField(Some(mo.getId)), value = valueField(mo.getPrimitive(valueField)))

    implicit def labelToMongoConverter(domain:Label): MongoObject = putDomainId(domain).putPrimitive(domain.value)

    implicit object LabelCollection extends CollectionName[Label] {
      override val name = "label"
    }
  }

  lazy val mongoCreationException = "Exceptional MongoObject"

  def createExceptionalMongoObject: MongoObject = throw new RuntimeException(mongoCreationException)

  case class Person(override val id:FieldValue[MID]= defaultId, name:FieldValue[String]) extends DomainObject

  object Person {

    val nameField = Field[String]("name")

    implicit def personToMongo(p:Person): MongoObject = empty

    implicit def mongoToPerson(mo:MongoObject): Person = Person(defaultId, name = nameField("testing"))

    lazy val expectedError = "no person collection here!"

    implicit object PersonCollectionName extends CollectionName[Person] {

      lazy val name = throw new RuntimeException(expectedError)
    }
  }
}