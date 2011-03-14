/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoUpdateObject
import MongoTypes.getElement

trait Funcs {

  import com.mongodb.{BasicDBList, DBObject}

  def $funcMongo(action: String, col: String, value: MongoObject): MongoObject = mongo.putMongo(action, mongo.putMongo(col, value))

  def $funcMongo(action: String, value: MongoObject): MongoObject = mongo.putMongo(action, value)

  def $funcPrimitive(action: String, col: String, value: AnyRef): MongoObject = mongo.putMongo(action, mongo.putPrimitiveObject(col, value))

  def fieldToMongo1[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): MongoObject = mongo.putAnything[O, T](fv)

  def empty = new MongoObject

  def mongo = new MongoObject

  def query(tuples: Tuple2[String, AnyRef]*) = new MongoObject(Map[String, AnyRef](tuples:_*))

  def mongoObject(tuples: Tuple2[String, AnyRef]*) = query(tuples:_*)

  def combine(value:MongoObject*): MongoObject = if (value.isEmpty) mongo else value.foldLeft(value.head)((a, b) => a.merge(b))

  def anyFunction1[O <: DomainObject, T : ClassManifest](fname:String, fv:FieldValue[O, T]): MongoObject = $funcMongo(fname, fieldToMongo1[O, T](fv))

  def toMongoUpdateObject(mo: => MongoObject): MongoUpdateObject = MongoUpdateObject(mo)

  def fromList[T <: DomainObject : MongoToDomain : ClassManifest](list:BasicDBList): Seq[T] = {
    fromSomeList[T](list, _ collect { case x:DBObject => implicitly[MongoToDomain[T]].apply(x) })
  }

  def fromPrimitiveList[T : ClassManifest](list:BasicDBList): Seq[T] = {
    fromSomeList[T](list, _ map (getElement[T]))
  }

  def fromSomeList[T : ClassManifest](list:BasicDBList, f:(Seq[AnyRef]) => Seq[Option[T]]): Seq[T] = {
    import scala.collection.JavaConversions._
    val seq:Seq[AnyRef] = list.toSeq
    f(seq) flatten
  }
}