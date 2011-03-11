package akuru

import MongoTypes.RegexConstants
import MongoTypes.RegExWithOptions
import MongoTypes.FieldRegEx
import MongoTypes.MongoSortObject
import MongoTypes.OperatorObject
import MongoTypes.MongoUpdateObject
import MongoObject._

trait AkuruImplicits {

  import com.mongodb.DBObject

  implicit def fieldValueToMongo[T : ClassManifest](fv: FieldValue[T]): MongoObject = fieldToMongo1[T](fv)

  implicit def fieldValueToUpdateObject[T : ClassManifest](fv: FieldValue[T]): MongoUpdateObject = MongoUpdateObject(fieldToMongo1[T](fv))

  implicit def dbObjectToMongoObject(dbo: DBObject): MongoObject = {
    import scala.collection.JavaConversions._
    MongoObject(dbo.keySet.toSeq map (key => (key, dbo.get(key))) toMap)
  }

  implicit def MongoObjectToDBObject(mo: MongoObject): DBObject = mo.toDBObject

  implicit def mongoToMongoJoiner(mo:MongoObject): MongoJoiner = MongoJoiner(mo)

  implicit def fvToMongoJoiner[T](fv:FieldValue[T]): MongoJoiner = MongoJoiner(mongo.putPrimitiveObject[T](fv))

  implicit def mongoJoinerToMongo(mj:MongoJoiner): MongoObject = mj.done

  implicit def fieldToOperation[T <% Number](f:Field[T]): OperatorObject[T] = OperatorObject[T](f)

  implicit def mongoSortToSortObjectJoiner(mso:MongoSortObject): SortObjectJoiner = SortObjectJoiner(mso)

  implicit def defaultRegExOption: RegexConstants.Value = RegexConstants.none

  implicit def stringToRegX(reg: String): RegExWithOptions = RegExWithOptions(reg)

  implicit def fieldToFieldRegEx[T](f:Field[T]): FieldRegEx[T] = FieldRegEx[T](f)
}