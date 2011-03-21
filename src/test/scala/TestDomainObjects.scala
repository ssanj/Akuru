/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.putId
import MongoTypes.Query
import MongoTypes.MongoObject.empty

trait TestDomainObjects extends NestedDomainObjects {

  case class Blog(title:Blog.titleField.Value,
                  labels:Blog.labelsField.Value = Blog.labelsField === Seq.empty[String],
                  id:Blog.idField.Value = Blog.defaultId) extends DomainObject

  object Blog extends DomainTemplate[Blog] {
    val titleField = field[String]("title")
    val labelsField = field[Seq[String]]("labels")

    override def mongoToDomain(mo:MongoObject): Option[Blog] = {
       for {
        title <- mo.getPrimitiveObject(titleField)
        labels <- mo.getPrimitiveObjects(labelsField)
      } yield (Blog(titleField === title, labelsField === labels, idField === mo.getId))
    }

    override def domainToMongoObject(domain:Blog): MongoObject = {
      putId(domain.id.value).putAnything(domain.title).putAnything(domain.labels)
    }

    val collectionName = "blog"
  }

  case class Label(value:Label.valueField.Value, id:Label.idField.Value = Label.defaultId) extends DomainObject

  object Label extends DomainTemplate[Label] {

    val valueField = field[String]("value")

    override def mongoToDomain(mo:MongoObject): Option[Label] =
      for {
        value <- mo.getPrimitiveObject(valueField)
      } yield Label(valueField === value, idField === mo.getId)

    override def domainToMongoObject(domain:Label): MongoObject = putId(domain.id.value).putAnything(domain.value)

    val collectionName = "label"
  }

  lazy val mongoCreationException = "Exceptional MongoObject"

  def exceptionalFieldValueJoiner[O <: DomainObject]: Query[O] = throw new RuntimeException(mongoCreationException)

  case class Person(name:Person.nameField.Value, id:Person.idField.Value = Person.defaultId) extends DomainObject

  object Person extends DomainTemplate[Person] {

    val nameField = field[String]("name")

    override def domainToMongoObject(p:Person): MongoObject = empty

    override def mongoToDomain(mo:MongoObject): Option[Person] = Some(Person(name = nameField === "testing"))

    lazy val expectedError = "no person collection here!"

    lazy val collectionName = throw new RuntimeException(expectedError)
  }

  case class Book(name:Book.nameField.Value,
                  authors:Book.authorsField.Value = Book.authorsField === Seq("misc"),
                  publisher:Book.publisherField.Value,
                  printVersion:Book.printVersionField.Value = Book.printVersionField === 1,
                  price:Book.priceField.Value,
                  id:Book.idField.Value = Book.defaultId) extends DomainObject

  object Book extends DomainTemplate[Book] {
    val nameField = field[String]("name")
    val authorsField = field[Seq[String]]("authors")
    val publisherField = field[String]("pub")
    val printVersionField = field[Int]("version")
    val priceField = field[Double]("price")

    override def domainToMongoObject(b:Book): MongoObject = putId(b.id.value).
            putAnything(b.name).putAnything(b.authors).putAnything(b.publisher).putAnything(b.printVersion).putAnything(b.price)

    override def mongoToDomain(mo:MongoObject): Option[Book] =
    for {
      name <- mo.getPrimitiveObject(nameField)
      authors <- mo.getPrimitiveObjects(authorsField)
      publisher <- mo.getPrimitiveObject(publisherField)
      printVersion <- mo.getPrimitiveObject(printVersionField)
      price <- mo.getPrimitiveObject(priceField)
    } yield
      Book(nameField === name, authorsField === authors, publisherField === publisher, printVersionField === printVersion, priceField === price,
        idField === mo.getId)

    val collectionName = "book"
  }

  case class Task(name:Task.nameField.Value,
                  priority:Task.priorityField.Value,
                  owner:Task.ownerField.Value,
                  id:Task.idField.Value = Task.defaultId) extends DomainObject

  object Task extends DomainTemplate[Task] {
    val nameField = field[String]("name")
    val priorityField = field[Int]("priority")
    val ownerField = field[String]("owner")

    override def domainToMongoObject(task:Task): MongoObject =
      putId(task.id.value).putAnything(task.name).putAnything(task.priority).putAnything(task.owner)

    override def mongoToDomain(mo:MongoObject): Option[Task] =
     for {
      name <- mo.getPrimitiveObject(nameField)
      priority <- mo.getPrimitiveObject(priorityField)
      owner <-mo.getPrimitiveObject(ownerField)
     } yield Task(nameField === name, priorityField === priority, ownerField === owner, idField === mo.getId)

    val collectionName = "task"
  }
}