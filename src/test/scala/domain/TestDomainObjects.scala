/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

import MongoTypes.putId
import MongoTypes.Query
import MongoTypes.MongoObject.empty

trait TestDomainObjects extends NestedDomainObjects with AllPossibleFieldsDomainObject {

  case class Blog(title:Blog.titleField.Value,
                  labels:Blog.labelsField.Value = Blog.labelsField === Seq.empty[String],
                  id:Blog.idField.Value = Blog.defaultId) extends DomainObject

  object Blog extends DomainTemplate[Blog] {
    val titleField = field[String]("title")
    val labelsField = arrayField[String]("labels")

    override def mongoToDomain(mo:MongoObject): Option[Blog] = {
       for {
        title <- mo.getPrimitiveObject(titleField)
        labels <- mo.getPrimitiveObjects(labelsField)
        id <- mo.getIdObject
      } yield (Blog(titleField === title, labelsField === labels, idField === id))
    }
  }

  case class Label(value:Label.valueField.Value, id:Label.idField.Value = Label.defaultId) extends DomainObject

  object Label extends DomainTemplate[Label] {

    val valueField = field[String]("value")

    override def mongoToDomain(mo:MongoObject): Option[Label] =
      for {
        value <- mo.getPrimitiveObject(valueField)
        id <- mo.getIdObject
      } yield Label(valueField === value, idField === id)
  }

  lazy val mongoCreationException = "Exceptional MongoObject"

  def exceptionalFieldValueJoiner[O <: DomainObject]: Query[O] = throw new RuntimeException(mongoCreationException)

  case class Person(name:Person.nameField.Value, id:Person.idField.Value = Person.defaultId) extends DomainObject

  object Person extends DomainTemplate[Person] {

    val nameField = field[String]("name")

    override def mongoToDomain(mo:MongoObject): Option[Person] = Some(Person(name = nameField === "testing"))

    lazy val expectedError = "no person collection here!"

    override lazy val collectionName = throw new RuntimeException(expectedError)
  }

  case class Book(name:Book.nameField.Value,
                  authors:Book.authorsField.Value = Book.authorsField === Seq("misc"),
                  publisher:Book.publisherField.Value,
                  printVersion:Book.printVersionField.Value = Book.printVersionField === 1,
                  price:Book.priceField.Value,
                  id:Book.idField.Value = Book.defaultId) extends DomainObject

  object Book extends DomainTemplate[Book] {
    val nameField = field[String]("name")
    val authorsField = arrayField[String]("authors")
    val publisherField = field[String]("pub")
    val printVersionField = field[Int]("version")
    val priceField = field[Double]("price")

    override def mongoToDomain(mo:MongoObject): Option[Book] =
    for {
      name <- mo.getPrimitiveObject(nameField)
      authors <- mo.getPrimitiveObjects(authorsField)
      publisher <- mo.getPrimitiveObject(publisherField)
      printVersion <- mo.getPrimitiveObject(printVersionField)
      price <- mo.getPrimitiveObject(priceField)
      id <- mo.getIdObject
    } yield
      Book(nameField === name, authorsField === authors, publisherField === publisher, printVersionField === printVersion, priceField === price,
        idField === id)
  }

  case class Task(name:Task.nameField.Value,
                  priority:Task.priorityField.Value,
                  owner:Task.ownerField.Value,
                  id:Task.idField.Value = Task.defaultId) extends DomainObject

  object Task extends DomainTemplate[Task] {
    val nameField = field[String]("name")
    val priorityField = field[Int]("priority")
    val ownerField = field[String]("owner")

    override def mongoToDomain(mo:MongoObject): Option[Task] =
     for {
      name <- mo.getPrimitiveObject(nameField)
      priority <- mo.getPrimitiveObject(priorityField)
      owner <-mo.getPrimitiveObject(ownerField)
      id <- mo.getIdObject
     } yield Task(nameField === name, priorityField === priority, ownerField === owner, idField === id)
  }
}