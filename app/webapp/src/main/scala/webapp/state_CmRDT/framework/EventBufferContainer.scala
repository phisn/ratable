package webapp.state_CmRDT.framework

import core.framework.ecmrdt.*

case class EventBufferContainer[A, C](
  val inner: ECmRDT[A, C],
  val events: Set[ECmRDTEventWrapper[A, C]]
):
  def prepare(eventWithContext: EventWithContext[A, C])(using effectPipeline: EffectPipeline[A, C]) =
    inner.prepare(eventWithContext)

  def effect(wrapper: ECmRDTEventWrapper[A, C])(using effectPipeline: EffectPipeline[A, C]) =
    wrapper.
    ???

