/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes._
import MongoObject.empty


trait DomainObjects { this:DomainSupport =>

  import Blog.types._

  case class Blog(override val id:idType = None, title:titleType, labels:labelsType) extends DomainObject

  case class Label(override val id:Option[MongoObjectId] = None, value:String) extends DomainObject

  object Blog {

    object types extends CommonTypes {
      type titleType = String
      type labelsType = Seq[String]
      object title_nf extends NamedField[titleType]("title")
      object labels_nf extends NamedField[labelsType]("labels")
    }

    implicit def mongoToBlogConverter(mo:MongoObject): Blog = {
      Blog(Some(mo.getId), mo.get[String]("title"), mo.getPlainArray[String]("labels"))
    }

    implicit def blogToMongoConverter(domain:Blog): MongoObject = {
        val mo = empty
        domain.id.foreach(mo.putId)
        mo.put("title", domain.title)
        mo.putArray2("labels", domain.labels)
        mo
    }

    implicit object BlogCollectionName extends CollectionName[Blog] {
      override val name = "blog"
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

    implicit object LabelCollection extends CollectionName[Label] {
      override val name = "label"
    }
  }

}

