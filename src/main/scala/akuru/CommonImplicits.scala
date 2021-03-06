/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */

package akuru

import MongoTypes.RegexConstants
import MongoTypes.RegExWithOptions
import MongoTypes.FieldRegEx
import MongoTypes.NumericOperations
import MongoTypes.MongoUpdateObject
import MongoTypes.Query
import MongoTypes.FieldValueQueryJoiner
import MongoObject.fieldToMongo1
import MongoObject.nestedPath

trait CommonImplicits {

  import com.mongodb.DBObject

  implicit def fieldValueToMongo[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): MongoObject = fieldToMongo1[O, T](fv)

  implicit def fieldValueToFieldValueJoiner[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): Query[O] =
    Query[O](FieldValueQueryJoiner[O, T](fv))

  implicit def fieldValueToUpdateObject[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): MongoUpdateObject[O] =
    MongoUpdateObject[O](fieldToMongo1[O, T](fv, nestedPath))

  implicit def dbObjectToMongoObject(dbo: DBObject): MongoObject = {
    import scala.collection.JavaConversions._
    MongoObject(dbo.keySet.toSeq map (key => (key, dbo.get(key))) toMap)
  }

  implicit def MongoObjectToDBObject(mo: MongoObject): DBObject = mo.toDBObject

  implicit def fieldToOperation[O <: DomainObject, T <% Number](f:FieldType[O, T]): NumericOperations[O, T] = NumericOperations[O, T](f)

  implicit val defaultRegExOption: RegexConstants.Value = RegexConstants.none

  implicit def stringToRegX(reg: String): RegExWithOptions = RegExWithOptions(reg)

  implicit def fieldToFieldRegEx[O <: DomainObject, T](f:FieldType[O, T]): FieldRegEx[O, T] = FieldRegEx[O, T](f)

}