package akuru

import MongoTypes.RegexConstants
import MongoTypes.RegExWithOptions
import MongoTypes.FieldRegEx
import MongoTypes.MongoSortObject
import MongoTypes.OperatorObject
import MongoTypes.MongoUpdateObject
import MongoTypes.FieldValueJoiner
import MongoTypes.FieldValueJoinerValue
import MongoTypes.MongoJoiner
import MongoObject.fieldToMongo1
import MongoObject.mongo

trait AkuruImplicits {

  import com.mongodb.DBObject

  implicit def fieldValueToMongo[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): MongoObject = fieldToMongo1[O, T](fv)

  implicit def fieldValueToFieldValueJoiner[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): FieldValueJoiner[O] =
    FieldValueJoiner[O](FieldValueJoinerValue[O, T](fv))

  implicit def dbObjectToMongoObject(dbo: DBObject): MongoObject = {
    import scala.collection.JavaConversions._
    MongoObject(dbo.keySet.toSeq map (key => (key, dbo.get(key))) toMap)
  }

  implicit def MongoObjectToDBObject(mo: MongoObject): DBObject = mo.toDBObject

  implicit def mongoToMongoJoiner(mo:MongoObject): MongoJoiner = MongoJoiner(mo)

  implicit def fvToMongoJoiner[O <: DomainObject, T : ClassManifest](fv:FieldValue[O, T]): MongoJoiner = MongoJoiner(mongo.putAnything[O, T](fv))

  implicit def fieldValueToUpdateObject[O <: DomainObject, T : ClassManifest](fv: FieldValue[O, T]): MongoUpdateObject[O] =
    MongoUpdateObject[O](fieldToMongo1[O, T](fv))

  implicit def mongoJoinerToMongo(mj:MongoJoiner): MongoObject = mj.done

  implicit def fieldToOperation[O <: DomainObject, T <% Number](f:Field[O, T]): OperatorObject[O, T] = OperatorObject[O, T](f)

  implicit def defaultRegExOption: RegexConstants.Value = RegexConstants.none

  implicit def stringToRegX(reg: String): RegExWithOptions = RegExWithOptions(reg)

  implicit def fieldToFieldRegEx[O <: DomainObject, T](f:Field[O, T]): FieldRegEx[O, T] = FieldRegEx[O, T](f)
}