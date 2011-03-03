/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

trait MongoObjectTrait {

  case class MongoObject(override val dbo:Map[String, AnyRef]) extends MongoObjectBehaviour with Tools  {

    def this() = this(Map.empty[String, AnyRef])

    //def this(tuples:Seq[Tuple2[String, Any]]) = this(new BasicDBObject(scala.collection.JavaConversions.asJavaMap(tuples.toMap)))
  }

  object MongoObject extends
      SetFuncs with
      PullFuncs with
      PushFuncs with
      SortFuncs with
      Funcs


}
