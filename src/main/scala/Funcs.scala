/*
 * Copyright 2010 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.MongoUpdateObject
import MongoTypes.OperatorObject
import MongoTypes.getElement

trait Funcs {

  import com.mongodb.{BasicDBList, DBObject}

  def $funcMongo(action: String, col: String, value: MongoObject): MongoObject = mongo.putMongo(action, mongo.putMongo(col, value))

  def $funcMongo(action: String, value: MongoObject): MongoObject = mongo.putMongo(action, value)

  def $funcPrimitive(action: String, col: String, value: AnyRef): MongoObject = mongo.putMongo(action, mongo.putPrimitiveObject(col, value))

  def fieldToMongo1[T](fv: FieldValue[T]): MongoObject = mongo.putPrimitiveObject[T](fv)

  def fieldToMongo2[R, T](fv1: FieldValue[R], fv2: FieldValue[T]): MongoObject = mongo.putPrimitiveObject(fv1).merge(mongo.putPrimitiveObject(fv2))

  def fieldToMongo3[R, S, T](fv1: FieldValue[R], fv2: FieldValue[S], fv3: FieldValue[T]): MongoObject =
    mongo.putPrimitiveObject(fv1).merge(mongo.putPrimitiveObject(fv2)).merge(mongo.putPrimitiveObject(fv3))

  def empty = new MongoObject

  def mongo = new MongoObject

  def query(tuples: Tuple2[String, AnyRef]*) = new MongoObject(Map[String, AnyRef](tuples:_*))

  def mongoObject(tuples: Tuple2[String, AnyRef]*) = query(tuples:_*)

  def combine(value:MongoObject*): MongoObject = if (value.isEmpty) mongo else value.foldLeft(value.head)((a, b) => a.merge(b))

  //TODO: remove this
  case class DBOToMongo(dbo: DBObject) {
    def toMongo(): MongoObject = dbo
  }

  case class SequencedFVTOMongo[T](fv:FieldValue[Seq[T]]) {
    println("called!" + fv)
    def splat(): MongoObject = mongo.putPrimitiveObjects[T](fv)
  }

  case class MongoJoiner(mo:MongoObject) {

    def and(another:MongoObject): MongoJoiner = MongoJoiner(mo.merge(another))

    def and[T](another:FieldValue[T]): MongoJoiner = MongoJoiner(mo.merge(another))

    def done: MongoObject = mo
  }

  def anyFunction1[T]: String => FieldValue[T] => MongoObject = fname => fv => $funcMongo(fname, fieldToMongo1[T](fv))

  def anyFunction2[R, T]: String => (FieldValue[R], FieldValue[T]) => MongoObject = fname => (fv1, fv2) =>
    $funcMongo(fname, fieldToMongo2[R,T](fv1, fv2))

  def anyFunction3[R, S, T]: String => (FieldValue[R], FieldValue[S], FieldValue[T]) => MongoObject =
    fname => (fv1, fv2, fv3) => $funcMongo(fname, fieldToMongo3[R,S,T](fv1, fv2, fv3))

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