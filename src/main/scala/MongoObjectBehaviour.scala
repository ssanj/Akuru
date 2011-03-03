/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.DBObject
import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import org.bson.types.ObjectId

trait MongoObjectBehaviour { this:Tools =>

  val dbo:Map[String, AnyRef]

  def getMongoObject(key:String): Option[MongoObject] = getTypeSafeObject(key, { case x:DBObject => Some(x) })

  def getDomainObject[T <: DomainObject : MongoToDomain](f:Field[T]): Option[T] = getTypeSafeObject[T](f.name, {
    case o:DBObject => implicitly[MongoToDomain[T]].apply(o) })

  def getDomainObjects[T <: DomainObject : MongoToDomain : ClassManifest](key:String): Seq[T] = getTypeSafeObject[Seq[T]](key, {
    case o:BasicDBList => Some(MongoObject.fromList[T](o)) }) getOrElse(Seq.empty[T])

  def getDomainObjects[T <: DomainObject : MongoToDomain : ClassManifest, R](f:Field[R]): Seq[T] = getDomainObjects[T](f.name)

  def getPrimitiveObject[T : ClassManifest](key:String): Option[T] = {
    getTypeSafeObject[T](key, { case x:AnyRef => getElement[T](x) })
  }

  def getPrimitiveObject[T : ClassManifest](f:Field[T]): Option[T] = getPrimitiveObject[T](f.name)

  def getPrimitiveObjects[T : ClassManifest](key:String): Option[Seq[T]] = {
    getTypeSafeObject[Seq[T]](key, { case o:BasicDBList => Some(MongoObject.fromPrimitiveList[T](o)) })
  }

  def getPrimitiveObjects[T : ClassManifest](f:Field[Seq[T]]): Option[Seq[T]] = getPrimitiveObjects[T](f.name)

  def getId: Option[MongoObjectId] = getPrimitiveObject[ObjectId]("_id") map (MongoObjectId(_))

  /**
   * TODO: Find a simpler way to write this.
   * This method only works if the duplicate keys have values of MongoObjects themselves. If the values themselves are not MongoObjects
   * the original key/values are returned unmerged.
   *
   * Eg.
   *
   * {key1: {field1:value1, field2:value2}} mergeDupes {key1: {field3:value3, field4:value4}} will give:
   * {key1: {field1:value1, field2:value2, field3:value3, field4:value4}}.
   *
   * Any values in duplicate keys within the values are trashed by the last value:
   * {key1: {field1:value1, field2:value2}} mergeDupes {key1: {field1:value3, field2:value4}} will give:
   * {key1: {field1:value3, field2:value4}}
   *
   * While with non-mongo values the original document unchanged.:
   * {key1: value1} mergeDupes {key1: value2} will give:
   * {key1: value1}
   */
  def mergeDupes(mo:MongoObject): MongoObject = {
    val dupes = dbo.keySet.filter(mo.dbo.keySet.contains(_))
    val allDupes = dupes.foldLeft(copyMongoObject)((container, key) =>
      container.getMongoObject(key) fold (container, m1 => mo.getMongoObject(key) fold (container, m2 => container.putMongo(key, m1.merge(m2)))))

    allDupes merge MongoObject(mo.dbo.filterNot(t => dupes.contains(t._1)))
  }

  def putPrimitiveObject[T](key:String, value:T): MongoObject = { putAnyArray(asJavaObject)(key, Seq(value.asInstanceOf[AnyRef])) }

  def putPrimitiveObject[T](fv:FieldValue[T]): MongoObject = { putPrimitiveObject(fv.name, fv.value) }

  //TODO: Test
  def putPrimitiveObjects[T](key:String, values: => Seq[T]): MongoObject = putAnyArray(convertToJavaList)(key, values.map(_.asInstanceOf[AnyRef]))

  //TODO: Test
  def putPrimitiveObjects[T](fv:FieldValue[Seq[T]]): MongoObject = putPrimitiveObjects[T](fv.name, fv.value)

  def putMongo(key:String, mongo:MongoObject): MongoObject = putAnyArray(asJavaObject)(key, Seq(mongo.toDBObject))

  //todo: a DomainObject should already have a FieldValue[MongoObjectId]
  def putId(id:MongoObjectId): MongoObject = MongoObject(dbo + ("_id" -> id.toObjectId))

  def merge(mo:MongoObject): MongoObject = MongoObject(dbo ++ (mo.dbo))

  private def getTypeSafeObject[T](key:String, pf:PartialFunction[Any, Option[T]]): Option[T] = {

    def pfNone:PartialFunction[Any, Option[T]] = { case _:Any => None }

    //if (dbo.contains(key)) pf orElse (pfNone) apply (dbo.get(key)) else None
    dbo.get(key) fold (None, value => pf orElse (pfNone) apply (value))
  }

  private[akuru] def mongoConverter[T <: DomainObject : MongoToDomain](element: AnyRef): Option[T] =
    implicitly[MongoToDomain[T]].apply(element.asInstanceOf[DBObject])

  private[akuru] def putAnyArray(f: Seq[AnyRef] => AnyRef)(key: => String, values: => Seq[AnyRef]): MongoObject = {
    MongoObject(dbo + (key -> f(values)))
  }

  private[akuru] def convertToJavaList(values: Seq[AnyRef]): AnyRef = {
    import scala.collection.JavaConversions.asJavaList
    val list:java.util.List[AnyRef] = values.toList
    val bslist:BasicDBList = new BasicDBList()
    bslist.addAll(list)
    bslist
  }

  private[akuru] def asJavaObject(values: Seq[AnyRef]): AnyRef =  values.head

  private[akuru] def toDBObject: DBObject = {
    import scala.collection.JavaConversions._
    new BasicDBObject(dbo)
  }

  private[akuru] def copyMongoObject: MongoObject = new MongoObject
}