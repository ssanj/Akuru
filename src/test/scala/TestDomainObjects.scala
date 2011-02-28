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

  case class Label(value:FieldValue[String], override val id:FieldValue[MID] = defaultId) extends DomainObject

  object Label {

    val valueField = Field[String]("value")

    implicit def mongoToLabelConverter(mo:MongoObject): Label = Label(value = valueField(mo.getPrimitive(valueField)), idField(Some(mo.getId)))

    implicit def labelToMongoConverter(domain:Label): MongoObject = putDomainId(domain).putPrimitive(domain.value)

    implicit object LabelCollection extends CollectionName[Label] {
      override val name = "label"
    }
  }

  lazy val mongoCreationException = "Exceptional MongoObject"

  def createExceptionalMongoObject: MongoObject = throw new RuntimeException(mongoCreationException)

  case class Person(name:FieldValue[String], override val id:FieldValue[MID]= defaultId) extends DomainObject

  object Person {

    val nameField = Field[String]("name")

    implicit def personToMongo(p:Person): MongoObject = empty

    implicit def mongoToPerson(mo:MongoObject): Person = Person(name = nameField("testing"), defaultId)

    lazy val expectedError = "no person collection here!"

    implicit object PersonCollectionName extends CollectionName[Person] {

      lazy val name = throw new RuntimeException(expectedError)
    }
  }

  case class Book(name:FieldValue[String],
                  authors:FieldValue[Seq[String]] = Book.authorsField(Seq("misc")),
                  publisher:FieldValue[String],
                  printVersion:FieldValue[Int] = Book.printVersionField(1),
                  price:FieldValue[Double],
                  override val id:FieldValue[MID] = defaultId) extends DomainObject

  object Book {
    val nameField = Field[String]("name")
    val authorsField = Field[Seq[String]]("authors")
    val publisherField = Field[String]("pub")
    val printVersionField = Field[Int]("version")
    val priceField = Field[Double]("price")

    implicit def bookToMongo(b:Book): MongoObject = putDomainId(b).
            putPrimitive(b.name).putPrimitiveArray(b.authors).putPrimitive(b.publisher).putPrimitive(b.printVersion).putPrimitive(b.price)

    implicit def mongoToBook(mo:MongoObject): Book =
      Book(nameField(mo.getPrimitive(nameField)),
            authorsField(mo.getPrimitiveArray(authorsField)),
            publisherField(mo.getPrimitive(publisherField)),
            printVersionField(mo.getPrimitive(printVersionField)),
            priceField(mo.getPrimitive(priceField)),
            idField(Some(mo.getId)))
  }

  implicit object BookCollectionName extends CollectionName[Book] {
    override val name = "book"
  }


  case class Task(val name:FieldValue[String],
                  val priority:FieldValue[Int],
                  val owner:FieldValue[String],
                  override val id:FieldValue[MID] = defaultId) extends DomainObject

  object Task {
    val nameField = Field[String]("name")
    val priorityField = Field[Int]("priority")
    val ownerField = Field[String]("owner")

    implicit def taskToMongoObject(task:Task): MongoObject =
      putDomainId(task).putPrimitive(task.name).putPrimitive(task.priority).putPrimitive(task.owner)

    implicit def mongoToTask(mo:MongoObject): Task =
      Task(nameField(mo.getPrimitive(nameField)), priorityField(mo.getPrimitive(priorityField)), ownerField(mo.getPrimitive(ownerField)),
        idField(Some(mo.getId)))

    implicit object TaskCollection extends CollectionName[Task] {
      override val name = "task"
    }
  }
}