/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._


trait MongoDummies {
  case class Person(name:String, age:Int)

  implicit object PersonConverter extends MongoConverter[Person] {
    def convert(mo:MongoObject): Person =  Person(mo.get[String]("name"), mo.get[Int]("age"))

    def convert(person:Person): MongoObject = {
      val mo = new MongoObject
      mo.put("name", person.name)
      mo.put("age", person.age)
      mo
    }
  }

}
