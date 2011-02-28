/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoTypes.MongoObject.empty
import DomainObject._

trait TestDomainObjects {

  case class Blog(title:FieldValue[String],
                  labels:FieldValue[Seq[String]] = Blog.labelsField(Seq[String]()), override val id:FieldValue[MID] = defaultId) extends DomainObject

  object Blog {

    type TitleField = Field[String]
    type LabelsField = Field[Seq[String]]

    val titleField = Field[String]("title")
    val labelsField = Field[Seq[String]]("labels")

    implicit def mongoToBlogConverter(mo:MongoObject): Blog =  {
      Blog(titleField(mo.getPrimitive(titleField)), labelsField(mo.getPrimitiveArray(labelsField)), idField(Some(mo.getId)))
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

  case class Book(override val id:FieldValue[MID] = defaultId,
                  name:FieldValue[String],
                  authors:FieldValue[Seq[String]] = Book.authorsField(Seq("misc")),
                  publisher:FieldValue[String],
                  printVersion:FieldValue[Int] = Book.printVersionField(1),
                  price:FieldValue[Double]) extends DomainObject

  object Book {
    val nameField = Field[String]("name")
    val authorsField = Field[Seq[String]]("authors")
    val publisherField = Field[String]("pub")
    val printVersionField = Field[Int]("version")
    val priceField = Field[Double]("price")

    implicit def bookToMongo(b:Book): MongoObject = putDomainId(b).
            putPrimitive(b.name).putPrimitiveArray(b.authors).putPrimitive(b.publisher).putPrimitive(b.printVersion).putPrimitive(b.price)

    implicit def mongoToBook(mo:MongoObject): Book =
      Book(id = idField(Some(mo.getId)),
        name = nameField(mo.getPrimitive(nameField)),
        authors = authorsField(mo.getPrimitiveArray(authorsField)),
        publisher = publisherField(mo.getPrimitive(publisherField)),
        printVersion = printVersionField(mo.getPrimitive(printVersionField)),
        price = priceField(mo.getPrimitive(priceField)))
  }

  implicit object BookCollectionName extends CollectionName[Book] {
    override val name = "book"
  }


  case class Task(override val id:FieldValue[MID] = defaultId,
                  val name:FieldValue[String],
                  val priority:FieldValue[Int],
                  val owner:FieldValue[String]) extends DomainObject

  object Task {
    val nameField = Field[String]("name")
    val priorityField = Field[Int]("priority")
    val ownerField = Field[String]("owner")

    implicit def taskToMongoObject(task:Task): MongoObject =
      putDomainId(task).putPrimitive(task.name).putPrimitive(task.priority).putPrimitive(task.owner)

    implicit def mongoToTask(mo:MongoObject): Task =
      Task(id = idField(Some(mo.getId)), name = nameField(mo.getPrimitive(nameField)), priority = priorityField(mo.getPrimitive(priorityField)),
        owner = ownerField(mo.getPrimitive(ownerField)))

    implicit object TaskCollection extends CollectionName[Task] {
      override val name = "task"
    }
  }
}