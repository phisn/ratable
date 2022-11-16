package webapp.usecases

import org.scalatest.flatspec.*
import org.scalatest.matchers.should.Matchers.*
import webapp.*
import webapp.mocks.*
import webapp.usecases.ratable.*
import core.state.aggregates.ratable.Ratable

class RateRatableSpec extends AnyFlatSpec

/*
  val applicationConfigMock = ApplicationConfigMock()

  implicit val services: Services = ServicesMock(
    _config = applicationConfigMock
  )

  "RateRatable usecase" should "rate a Ratable" in {
    val (ratableID, ratable) = internalCreateRatable("title", List("category1", "category2"))

    val rating = Map(
      ratable.categories.head(0) -> 2,
      ratable.categories.tail.head(0) -> 4,
    )

    rateRatable(ratableID, rating)

    internalFindRatable(ratableID).categoriesWithRating.map {
      case (id, (category, rating)) => (id, rating)
     } shouldEqual rating
  }

  it should "update a rating" in {
    val (ratableID, ratable) = internalCreateRatable("title", List("category1", "category2"))

    val rating1 = Map(
      ratable.categories.head(0) -> 1,
      ratable.categories.tail.head(0) -> 1,
    )

    rateRatable(ratableID, rating1)

    val rating2 = Map(
      ratable.categories.head(0) -> 4,
      ratable.categories.tail.head(0) -> 4,
    )

    rateRatable(ratableID, rating2)
    
    internalFindRatable(ratableID).categoriesWithRating.map {
      case (id, (category, rating)) => (id, rating)
     } shouldEqual rating2
  }

  it should "merge ratings from different replicas" in {
    val (ratableID, ratable) = internalCreateRatable("title", List("category1", "category2"))

    val rating1 = Map(
      ratable.categories.head(0) -> 1,
      ratable.categories.tail.head(0) -> 1,
    )

    rateRatable(ratableID, rating1)

    val rating2 = Map(
      ratable.categories.head(0) -> 3,
      ratable.categories.tail.head(0) -> 3,
    )

    val otherReplicaID = applicationConfigMock.withReplicaID(
      applicationConfigMock.replicaID + "other"
    ) {
      rateRatable(ratableID, rating2)
    }

    internalFindRatable(ratableID).categoriesWithRating.map {
      case (id, (category, rating)) => (id, rating)
     } shouldEqual Map(
      ratable.categories.head(0) -> 2,
      ratable.categories.tail.head(0) -> 2,
     )
  }

  private def internalCreateRatable(title: String, categories: List[String]): (String, Ratable) =
    val id = createRatable(title, categories)
    (id, internalFindRatable(id))

  private def internalFindRatable(id: String): Ratable =
    services.state.ratables.map(_.get(id)).now match {
      case Some(ratable) => ratable
      case None => fail("Ratable not found after creation")
    }
*/
