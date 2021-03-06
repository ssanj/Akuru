/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package domain

trait AllPossibleFieldsDomainObject {

  case class Student(name:Student.nameField.Value,
                     givenNames:Student.givenNamesField.Value,
                     address:Student.addressField.Value,
                     courses:Student.coursesField.Value,
                     id: Student.idField.Value = Student.defaultId) extends DomainObject

  object Student extends DomainTemplate[Student] {
    val nameField = field[String]("name")
    val givenNamesField = arrayField[String]("givenNames")
    val addressField = embeddedField[Address]("address")
    val coursesField = embeddedArrayField[Course]("courses")

    override def mongoToDomain(mo:MongoObject): Option[Student] = {
      for {
        name <- mo.getPrimitiveObject(nameField)
        givenNames <- mo.getPrimitiveObjects(givenNamesField)
        address <- mo.getNestedObject(addressField)
        courses <- mo.getNestedObjectArray(coursesField)
        id <- mo.getIdObject
      } yield Student(nameField === name, givenNamesField === givenNames, addressField === address, coursesField === courses, idField === id)
    }
  }

  case class Address(address:Address.addressField.Value,
                     postCode:Address.postCodeField.Value,
                     state:Address.stateField.Value,
                     country:Address.countryField.Value)  extends NestedObject

  object Address extends NestedTemplate[Student, Address] {
    override val parentField:FieldType[Student, Address] = Student.addressField
    val addressField = field[String]("address")
    val postCodeField = embeddedField[PostCode]("postCode")
    val stateField = field[String]("state")
    val countryField = field[String]("country")

    override def mongoToNested(mo:MongoObject): Option[Address] = {
      for {
        address <- mo.getPrimitiveObject(addressField)
        postCode <- mo.getNestedObject(postCodeField)
        state <- mo.getPrimitiveObject(stateField)
        country <- mo.getPrimitiveObject(countryField)
      } yield Address(addressField === address, postCodeField === postCode, stateField === state, countryField === country)
    }
  }

  case class PostCode(code:PostCode.codeField.Value, name:PostCode.nameField.Value) extends NestedObject

//  case object Brisbane extends PostCode(PostCode.codeField === "4000", PostCode.codeField === "Brisbane")
//  case object ForestLake extends PostCode(PostCode.codeField === "4078", PostCode.codeField === "ForestLake")

  object PostCode extends NestedTemplate[Student, PostCode] {
    override val parentField:FieldType[Student, PostCode] = Address.postCodeField
    val codeField = field[String]("code")
    val nameField = field[String]("name")

    override def mongoToNested(mo:MongoObject): Option[PostCode] = {
      for {
        code <- mo.getPrimitiveObject(codeField)
        name <- mo.getPrimitiveObject(nameField)
      } yield PostCode(codeField === code, nameField === name)
    }
  }

  case class Course(name:Course.nameField.Value,
                    reading:Course.readingField.Value,
                    location:Course.locationField.Value,
                    comments:Course.commentsField.Value) extends NestedObject

  object Course extends NestedTemplate[Student, Course] {
    override val parentField:FieldType[Student, Course] = Student.coursesField
    val nameField = field[String]("name")
//    val addressField = embeddedField[Address]("address")
    val readingField = arrayField[String]("reading")
    val locationField = embeddedField[Room]("room")
    val commentsField = embeddedArrayField[Comment]("comments")

    override def mongoToNested(mo:MongoObject): Option[Course] = {
      for {
        name <- mo.getPrimitiveObject(nameField)
        reading <- mo.getPrimitiveObjects(readingField)
        location <- mo.getNestedObject(locationField)
        comments <- mo.getNestedObjectArray(commentsField)
      } yield Course(nameField === name, readingField === reading, locationField === location, commentsField === comments)
    }
  }

  case class Room(size:Room.sizeField.Value) extends NestedObject

  object Room extends NestedTemplate[Student, Room] {
    override val parentField:FieldType[Student, Room] = Course.locationField
    val sizeField = field[Int]("size")

    override def mongoToNested(mo:MongoObject): Option[Room] = {
      for {
        size <- mo.getPrimitiveObject(sizeField)
      } yield Room(sizeField === size)
    }
  }

  case class Comment(comment:Comment.commentField.Value, author:Comment.authorField.Value) extends NestedObject

  object Comment extends NestedTemplate[Student, Comment] {
    override val parentField:FieldType[Student, Comment] = Course.commentsField
    val commentField = field[String]("comment")
    val authorField = field[String]("author")

    override def mongoToNested(mo:MongoObject): Option[Comment] = {
      for {
        comment <- mo.getPrimitiveObject(commentField)
        author <- mo.getPrimitiveObject(authorField)
      } yield Comment(commentField === comment, authorField === author)
    }
  }
}