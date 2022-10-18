package core.store.aggregates.ratable

import core.store.framework.*

type RatableRepository = Repository[String, Ratable]

extension (repo: RatableRepository)
  def create(id: String, title: String, categories: List[String], replicaID: String) =
    repo.mutate(id, Ratable(
      _title = LWW.apply(title, replicaID),
      categories = categories
        .zipWithIndex
        .map((title, index) => 
          (
            index, 
            Category(LWW.apply(title, replicaID))
          ))
        .toMap
    ))
