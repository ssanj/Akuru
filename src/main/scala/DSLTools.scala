/*
 * Copyright (c) 2011 Sanjiv Sahayam
 * Licensed under the Apache License, Version 2.0
 */
package akuru;

import MongoTypes.MongoSortObject
import MongoTypes.MongoObject.mongo

trait DSLTools {

  def orderToSortObject[T <: DomainObject](orders:List[(Field[T, _], SortOrder)]): MongoSortObject = {
    MongoSortObject(orders.foldLeft(mongo){ case (mo, (k, v)) => mo.putPrimitiveObject(k.name, v.id) })
  }

  def noSorting: MongoSortObject = MongoSortObject(mongo)

}