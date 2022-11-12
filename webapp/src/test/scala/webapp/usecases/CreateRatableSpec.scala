package webapp.usecases

import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.*
import webapp.*
import webapp.mocks.*
import webapp.usecases.ratable.*

class CreateRatableSpec extends AnyFlatSpec

/*
  implicit val services: Services = ServicesMock()

  "CreateRatable usecase" should "create a new Ratable" in {
    val title = "title"
    val categories = List("category1", "category2")

    val id = createRatable(title, categories)
    val ratable = services.state.ratables.map(_.get(id)).now match {
      case Some(ratable) => ratable
      case None => fail("Ratable not found after creation")
    }

    ratable.title shouldEqual title
    ratable.categories.map(
      (_, category) => category.title.map(_.value).getOrElse("")
    ) shouldEqual categories
    ratable._ratings.isEmpty shouldEqual true
  }

  it should "create a new Ratable with a unique id" in {
    val title = "title"
    val categories = List("category1", "category2")

    val id1 = createRatable(title, categories)
    val id2 = createRatable(title, categories)

    id1 should not equal id2
  }
*/
