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

    val title = "title"
    val labels = "labels"

    implicit def mongoToBlogConverter(mo:MongoObject): Blog = {
      Blog(Some(mo.getId), mo.getPrimitive[String](title), mo.getPrimitiveArray[String](labels))
    }

    implicit def blogToMongoConverter(domain:Blog): MongoObject =
      putDomainId(domain).put[String](title, domain.title).putArray2[String](labels, domain.labels)

    implicit object BlogCollection extends CollectionName[Blog] {
      override val name = "blog"
    }
  }

  object Label {

    val value = "value"

    implicit def mongoToLabelConverter(mo:MongoObject): Label = Label(Some(mo.getId), mo.getPrimitive[String](value))

    implicit def labelToMongoConverter(domain:Label): MongoObject = putDomainId(domain).put[String](value, domain.value)

    implicit object LabelCollection extends CollectionName[Label] {
      override val name = "label"
    }
  }
}

