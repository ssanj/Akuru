package akuru

import MongoTypes.MongoSortObject
import MongoTypes.MongoObject.mongo

trait SortFuncs {

  def sort[T](fv:Field[T], order:SortOrder): MongoSortObject = MongoSortObject(mongo.putPrimitiveObject(fv.name, order.id))

  def noSort: MongoSortObject = MongoSortObject(mongo)

  case class SortObjectJoiner(mso: MongoSortObject) {

    def and(another: MongoSortObject): SortObjectJoiner = SortObjectJoiner(MongoSortObject(mso.value.merge(another.value)))

    def done: MongoObject = mso.value
  }
}