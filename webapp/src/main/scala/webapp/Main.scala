package webapp

import cats.effect.SyncIO
import colibri.*
import outwatch.*
import outwatch.dsl.*
import rescala.default.*
import rescala.compat.*
import rescala.operator.*
import scalajs.*

// Outwatch documentation:
// https://outwatch.github.io/docs/readme.html

@main
def main(): Unit =
  Outwatch.renderInto[SyncIO]("#app", app).unsafeRunSync()

def app =
  val e = Events.fromCallback[Int](cb => {
    js.timers.setTimeout(5000) {
      cb(5123123)
    }
  })

  div(
    h1("Hello World!"),
    counter,
    inputField,
    e.event
  )

def counter = SyncIO {
  // https://outwatch.github.io/docs/readme.html#example-counter
  // val number = Subject.behavior(0)
  val number = Var(0)
  val counter = Subject.behavior(0)

  // val k = onClick.map(_ => 1).forwardTo[Var, Int](number)
  val k1 = onClick(Signal {
  })

  val s = Var[Int](0)
  val kk: SignalCompat[Int] = s
  kk.map(_ + 1)
  val s_MAP = s.map((x: Int) => x + 199)

  val k2 = onClick.as(1)
  val k3 = onClick(counter.map(_ - 1))

  div(
    button("+", onClick(number.map(_ + 1)) --> number, marginRight := "10px"),
    number
  )
}

def inputField = SyncIO {
  // https://outwatch.github.io/docs/readme.html#example-input-field
  val text = Subject.behavior("")
  div(
    input(
      tpe := "text",
      value <-- text,
      onInput.value --> text,
    ),
    button("clear", onClick.as("") --> text),
    div("text: ", text),
    div("length: ", text.map(_.length)),
  )
}