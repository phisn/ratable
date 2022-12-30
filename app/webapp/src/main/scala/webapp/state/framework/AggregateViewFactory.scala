package webapp.state.framework

import com.github.plokhotnyuk.jsoniter_scala.core.*
import core.framework.ecmrdt.*
import core.messages.common.*
import scala.concurrent.*

trait AggregateViewProvider[A : JsonValueCodec, C <: IdentityContext : JsonValueCodec, E <: Event[A, C]]:
  def get(gid: AggregateGid): Future[Option[AggregateView[A, C, E]]]
