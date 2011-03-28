/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru
package domain

import MongoTypes.putId
import MongoTypes.Query
import MongoTypes.MongoObject.empty

trait TestDomainObjects extends NestedDomainObjects {

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

    override def domainToMongoObject(domain:Blog): MongoObject = {
      putId(domain.id).putAnything(domain.title).putAnything(domain.labels)
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

    override def domainToMongoObject(domain:Label): MongoObject = putId(domain.id).putAnything(domain.value)
  }

  lazy val mongoCreationException = "Exceptional MongoObject"

  def exceptionalFieldValueJoiner[O <: DomainObject]: Query[O] = throw new RuntimeException(mongoCreationException)

  case class Person(name:Person.nameField.Value, id:Person.idField.Value = Person.defaultId) extends DomainObject

  object Person extends DomainTemplate[Person] {

    val nameField = field[String]("name")

    override def domainToMongoObject(p:Person): MongoObject = empty

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

    override def domainToMongoObject(b:Book): MongoObject = putId(b.id).
            putAnything(b.name).putAnything(b.authors).putAnything(b.publisher).putAnything(b.printVersion).putAnything(b.price)

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

    override def domainToMongoObject(task:Task): MongoObject =
      putId(task.id).putAnything(task.name).putAnything(task.priority).putAnything(task.owner)

    override def mongoToDomain(mo:MongoObject): Option[Task] =
     for {
      name <- mo.getPrimitiveObject(nameField)
      priority <- mo.getPrimitiveObject(priorityField)
      owner <-mo.getPrimitiveObject(ownerField)
      id <- mo.getIdObject
     } yield Task(nameField === name, priorityField === priority, ownerField === owner, idField === id)
  }

  case class User(username:User.usernameField.Value, role:User.roleField.Value, id:User.idField.Value = User.defaultId) extends DomainObject

  object Role extends Enumeration(0, "Administrator", "User", "Developer") {
    type Role = Value
    val ADMIN, USER, DEV = Value
  }

  import Role._
  object User extends DomainTemplate[User] {
    val usernameField = field[String]("username")
    val roleField = enumField[Role]("role")

    override def domainToMongoObject(user:User): MongoObject =  putId(user.id).putAnything(user.username).putAnything(user.role)

    override def mongoToDomain(mo:MongoObject): Option[User] = {
      for {
        username <- mo.getPrimitiveObject(usernameField)
        role <- mo.getEnumObject(roleField, s => values.filter(_.toString == s).headOption)   //TODO: Make this better.
        id <- mo.getIdObject
      } yield User(usernameField === username, roleField === role, idField === id)
    }
  }
}