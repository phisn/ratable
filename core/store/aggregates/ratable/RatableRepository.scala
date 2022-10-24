package core.store.aggregates.ratable

import core.store.framework.*

type RatableRepository = Repository[String, Ratable]

extension (repo: RatableRepository)
  def create(id: String, title: String, categories: List[String], replicaID: String) =
    repo.mutate(id, Ratable(title, categories, replicaID))
