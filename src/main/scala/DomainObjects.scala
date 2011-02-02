/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoObject.empty

trait DomainObjects { this:DomainSupport =>

  case class Blog(override val id:Option[MongoObjectId] = None, title:String, labels:Seq[String]) extends DomainObject

  case class Label(override val id:Option[MongoObjectId] = None, value:String) extends DomainObject

  object Blog {
    implicit def mongoToBlogConverter(mo:MongoObject): Blog = {
      Blog(Some(mo.getId), mo.get[String]("title"), Seq.empty)
    }

    implicit def blogToMongoConverter(domain:Blog): MongoObject = {
        val mo = empty
        domain.id.foreach(mo.putId)
        mo.put("title", domain.title)
        mo.putArray2("labels", domain.labels)
        mo
    }
  }

  object Label {
    implicit def mongoToLabelConverter(mo:MongoObject): Label = Label(Some(mo.getId), mo.get[String]("value"))

    implicit def labelToMongoConverter(domain:Label): MongoObject = {
        val mo = empty
        domain.id.foreach(mo.putId)
        mo.put("value", domain.value)
        mo
    }
  }


  implicit object BlogCollection extends CollectionName[Blog] {
    override val name = "blog"
  }

  implicit object LabelCollection extends CollectionName[Label] {
    override val name = "label"
  }
}

