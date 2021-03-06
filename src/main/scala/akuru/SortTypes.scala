/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait SortTypes {

  sealed trait SortObject {
    val value:MongoObject
  }

  object SortOrder extends Enumeration {
    val ASC =  Value(1)
    val DSC  = Value(-1)
  }

  private[akuru] case class MongoSortObject(mo:MongoObject) extends SortObject {
    override val value:MongoObject = mo
  }
}