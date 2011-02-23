package akuru

import MongoTypes.FieldValue
import MongoTypes.Field
import MongoTypes.MongoSortObject
import MongoTypes.OperatorObject
import MongoTypes.MongoObject
import MongoObject.mongo
import MongoObject.fieldToMongo1
import MongoObject.DBOToMongo
import MongoObject.SequencedFVTOMongo
import MongoObject.MongoJoiner
import MongoObject.SortObjectJoiner


trait AkuruImplicits {

  import com.mongodb.DBObject

  implicit def fieldValueToMongo[T](fv: FieldValue[T]): MongoObject = fieldToMongo1[T](fv)

  implicit def dbObjectToMongoObject(dbo: DBObject): MongoObject = MongoObject(dbo)

  implicit def MongoObjectToDBObject(mo: MongoObject): DBObject = mo.toDBObject

  implicit def tuple2PrimitiveToMongoObject(tuple2: Tuple2[String, AnyRef]): MongoObject = mongo.putPrimitive(tuple2._1, tuple2._2)

  implicit def tuple2MongoToMongoObject(tuple2: Tuple2[String, MongoObject]): MongoObject = mongo.putMongo(tuple2._1, tuple2._2)

  //TODO: remove this
  implicit def dboToDBOToMongo(dbo: DBObject): DBOToMongo = DBOToMongo(dbo)

  implicit def sequencedFVTOMongo[T](fv:FieldValue[Seq[T]]): SequencedFVTOMongo[T] = SequencedFVTOMongo[T](fv)

  implicit def mongoToMongoJoiner(mo:MongoObject): MongoJoiner = MongoJoiner(mo)

  implicit def fvToMongoJoiner[T](fv:FieldValue[T]): MongoJoiner = MongoJoiner(mongo.putPrimitive[T](fv))

  implicit def mongoJoinerToMongo(mj:MongoJoiner): MongoObject = mj.done

  implicit def fieldToOperation[T <% Number](f:Field[T]): OperatorObject[T] = OperatorObject[T](f)

  implicit def mongoSortToSortObjectJoiner(mso:MongoSortObject): SortObjectJoiner = SortObjectJoiner(mso)
}