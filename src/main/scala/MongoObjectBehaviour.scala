/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru

import com.mongodb.DBObject
import com.mongodb.BasicDBObject
import com.mongodb.BasicDBList
import org.bson.types.ObjectId
import MongoObject.PathProvider
import MongoObject.namePath

trait MongoObjectBehaviour { this:Tools =>

  val dbo:Map[String, AnyRef]

  private[akuru] def getMongoObject(key:String): Option[MongoObject] = getTypeSafeObject(key, { case x:DBObject => Some(x) })

  def getNestedObject[O <: DomainObject, T <: NestedObject : MongoToNested](ft:FieldType[O, T]): Option[T] = {
    getTypeSafeObject(ft.name, { case x:DBObject => implicitly[MongoToNested[T]].apply(x) })
  }

  def getNestedObjectArray[O <: DomainObject, T <: NestedObject : MongoToNested : ClassManifest](ft:FieldType[O, Seq[T]]): Option[Seq[T]] = {
      getTypeSafeObject(ft.name, { case list:BasicDBList =>
        Some(MongoObject.fromSomeList[T](list,
          _ map (_ match {
            case dbo:DBObject => implicitly[MongoToNested[T]].apply(dbo)
            case _ => None
          })
        ))
    })
  }

  def getPrimitiveObject[T : Primitive : ClassManifest](key:String): Option[T] = {
    getTypeSafeObject[T](key, { case x:AnyRef => getElement[T](x) })
  }

  def getEnumObject[O <: DomainObject, T <: Enumeration#Value : ClassManifest](f:FieldType[O, T], toEnum: String => Option[T]): Option[T] = {
    getPrimitiveObject[String](f.name) fold (None, value => toEnum(value))
  }

  def getPrimitiveObject[O <: DomainObject, T : Primitive : ClassManifest](f:FieldType[O, T]): Option[T] = getPrimitiveObject[T](f.name)

  def getPrimitiveObjects[T : Primitive : ClassManifest](key:String): Option[Seq[T]] = {
    getTypeSafeObject[Seq[T]](key, { case o:BasicDBList => Some(MongoObject.fromPrimitiveList[T](o)) })
  }

  def getPrimitiveObjects[O <: DomainObject, T : Primitive : ClassManifest](f:FieldType[O, Seq[T]]): Option[Seq[T]] = getPrimitiveObjects[T](f.name)

  def getId: Option[MongoObjectId] = getPrimitiveObject[ObjectId]("_id") map (MongoObjectId(_))

  /**
   * This has been introduced so it can be used like every other variable in for-comprehension.
   */
  def getIdObject: Option[Option[MongoObjectId]] = getPrimitiveObject[ObjectId]("_id") map (MongoObjectId(_)) map(Some(_))

  /**
   * This method only works if the duplicate keys have values of MongoObjects themselves. If the values themselves are not MongoObjects
   * the original key/values are returned unmerged.
   *
   * Eg.
   *
   * {key1: {field1:value1, field2:value2}} mergeMongoObjectValues {key1: {field3:value3, field4:value4}} will give:
   * {key1: {field1:value1, field2:value2, field3:value3, field4:value4}}.
   *
   * Any values in (duplicate keys within the values) are trashed by the last value:
   * {key1: {field1:value1, field2:value2}} mergeMongoObjectValues {key1: {field1:value3, field2:value4}} will give:
   * {key1: {field1:value3, field2:value4}}
   *
   * While with non-MongObject values the original document is unchanged:
   * {key1: value1} mergeMongoObjectValues {key1: value2} will give:
   * {key1: value1}
   *
   * Within a document of mixed MongoObject and non-MongoObject values:
   *
   * 1. The duplicates keys with MongoObjects values are merged
   * 2. The the values of unique keys are used as is (whether values are MongObjects or not)
   * 3. Duplicate keys within (within the values) of the MongoObject value are trashed by the last value:
   *
   * {key1: {field1:value1, field2:value2, field3:value11}, key2: value10 } mergeMongoObjectValues {key1: {field3:value3, field4:value4}, key3:value16} will give:
   * {key1: {field1:value1, field2:value2, field3:value3, field4:value4}, key2: value10, key3:value16}
   */
  def mergeMongoObjectValues(incoming:MongoObject): MongoObject = {
    val dupes = dbo.keySet & incoming.dbo.keySet
    val merged = dupes.foldLeft(copyMongoObject)((container, key) =>
      container.getMongoObject(key) fold (container,
              m1 => incoming.getMongoObject(key) fold (container,
                      m2 => container.putMongo(key, m1.merge(m2)))))

    merged merge MongoObject(incoming.dbo.filter(t => incoming.dbo.keySet &~ dupes contains (t._1)))
  }

 def putPrimitiveObject[T](key:String, value:T): MongoObject = { putAnyArray(asJavaObject)(key, Seq(value.asInstanceOf[AnyRef])) }

  def putPrimitiveObject[O <: DomainObject, T](fv:FieldValue[O, T]): MongoObject = { putPrimitiveObject(fv.name, fv.value) }

  def putPrimitiveObjects2[O <: DomainObject](fv:FieldValue[O, Seq[Any]]): MongoObject = {
     merge(fv.name -> convertToJavaList(fv.value map (_.asInstanceOf[AnyRef])))
  }

  def putMongo(key:String, mongo:MongoObject): MongoObject = putAnyArray(asJavaObject)(key, Seq(mongo.toDBObject))

  def putMongoArray(key:String, mongos:Seq[MongoObject]): MongoObject = putAnyArray(convertToJavaList)(key, mongos map (_.toDBObject))

  //todo: a DomainObject should already have a FieldValue[MongoObjectId]
  def putId(id:MongoObjectId): MongoObject = MongoObject(dbo + ("_id" -> id.toObjectId))

  def merge(mo:MongoObject): MongoObject = MongoObject(dbo ++ (mo.dbo))

  def merge[O <: DomainObject, T](fv:FieldValue[O, T]): MongoObject = MongoObject(dbo + (fv.name -> fv.value.asInstanceOf[AnyRef]))

  def merge(t:(String, Any)): MongoObject = MongoObject(dbo + (t._1 -> t._2.asInstanceOf[AnyRef]))

  private def getTypeSafeObject[T](key:String, pf:PartialFunction[Any, Option[T]]): Option[T] = {

    def pfNone:PartialFunction[Any, Option[T]] = { case _:Any => None }

    dbo.get(key) fold (None, value => pf orElse (pfNone) apply (value))
  }

  private[akuru] def mongoConverter[T <: DomainObject : MongoToDomain](element: AnyRef): Option[T] =
    implicitly[MongoToDomain[T]].apply(element.asInstanceOf[DBObject])

  private[akuru] def putAnyArray(f: Seq[AnyRef] => AnyRef)(key: => String, values: => Seq[AnyRef]): MongoObject = {
    val mo = MongoObject(dbo + (key -> f(values)))
    mo
  }

  def putNested[O <: DomainObject, T <: NestedObject : NestedToMongo](nested: FieldValue[O, T]): MongoObject = {
      putMongo(nested.name, implicitly[NestedToMongo[T]].apply(nested.value))
  }

  def putNestedArray[O <: DomainObject, T <: NestedObject : NestedToMongo](nested: FieldValue[O, Seq[T]]): MongoObject = {
      putMongoArray(nested.name, nested.value map (implicitly[NestedToMongo[T]].apply(_)))
  }

  def putAnything[O <: DomainObject, T : ClassManifest](fv:FieldValue[O, T],
                                                        pp: PathProvider[O, T] = namePath[O, T]): MongoObject = {
    getElement[T](fv.value.asInstanceOf[AnyRef]) match {
      case Some(element) => element match {
        case seq:Seq[_] => merge(putPrimitiveObjects2[O](new Field[O, Seq[Any]](pp(fv)) === seq))
        case mo:MongoObject => merge(putMongo(pp(fv), mo))
        case id:MongoObjectId =>   merge(putId(id))
        case en:Enumeration#Value =>   MongoObject(dbo + (pp(fv) ->  en.toString.asInstanceOf[AnyRef]))
        case _:Any => MongoObject(dbo + (pp(fv) -> fv.value.asInstanceOf[AnyRef]))
      }
      case None => copyMongoObject
    }
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

  private[akuru] def copyMongoObject: MongoObject = new MongoObject(dbo)
}