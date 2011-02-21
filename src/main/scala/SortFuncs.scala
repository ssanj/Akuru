package akuru

import MongoTypes.SortOrder
import MongoTypes.Field
import MongoTypes.MongoSortObject
import MongoTypes.MongoObject
import MongoTypes.MongoObject.mongo

trait SortFuncs {

  def sort[T](fv:Field[T], order:SortOrder.Value): MongoSortObject = MongoSortObject(mongo.putPrimitive(fv.name, order.id))

  def noSort: MongoSortObject = MongoSortObject(mongo)

  case class SortObjectJoiner(mso: MongoSortObject) {

    def and(another: MongoSortObject): SortObjectJoiner = SortObjectJoiner(MongoSortObject(mso.value.merge(another.value)))

    def done: MongoObject = mso.value
  }

  object SortObjectJoiner {
    implicit def mongoSortToSortObjectJoiner(mso:MongoSortObject): SortObjectJoiner = SortObjectJoiner(mso)
  }
}