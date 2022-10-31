package core.state.aggregates.ratable

import com.github.plokhotnyuk.jsoniter_scala.core.*
import com.github.plokhotnyuk.jsoniter_scala.macros.*
import core.state.framework.*

type RatableRepository = Repository[String, Ratable]

extension (repo: RatableRepository)
  def create(id: String, title: String, categories: List[String], replicaID: String) =
    repo.mutate(id, Ratable(title, categories, replicaID))

object RatableRepository:
  given JsonValueCodec[RatableRepository] = JsonCodecMaker.make[RatableRepository]
