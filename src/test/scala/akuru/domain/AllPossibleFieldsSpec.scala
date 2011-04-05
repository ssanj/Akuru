/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru
package domain

import akuru.dsl.AkuruDSL


final class AllPossibleFieldsSpec extends CommonSpec with AkuruDSL with AllPossibleFieldsDomainObject {

  import Student._
  import Course._
  import Room._
  import Comment._
  import Address._
  import PostCode._

  "A Complex Object" should "serialize itself" in {
    val student = Student(
      Student.nameField === "Mickey",
      givenNamesField === Seq("mikka", "mazzy"),
      Student.addressField ===
              Address(Address.addressField === "West Hollywood",
                postCodeField === PostCode(codeField === "CA", PostCode.nameField === "Hollywood"),
                                            stateField === "California", countryField === "USA"),
      coursesField ===
            Seq(Course(Course.nameField === "Cheese eating",
                                    readingField === Seq("How to eat cheeze", "Holes: How to make cheese better"),
                                    locationField === Room(sizeField === 120),
                                    commentsField ===
                                            Seq(Comment(commentField === "Quite Roomy", authorField === "Razza"),
                                                Comment(commentField === "Noisy AC", authorField === "Hot n Stuffy"))),
                Course(Course.nameField === "Hiding in small places",
                      readingField === Seq("How to make a Rats hole", "Fitting in small spaces", "Claustraphobia: Not an option"),
                      locationField === Room(sizeField === 300),
                      commentsField ===
                              Seq(Comment(commentField === "Tiny rooms", authorField === "Meh"),
                                  Comment(commentField === "Overcrowded", authorField === "stuffed"),
                                  Comment(commentField === "a hole in the wall", authorField === "ironic")))))

    (initStudent ~~> save(student)) ~~>() verifySuccess
  }
}