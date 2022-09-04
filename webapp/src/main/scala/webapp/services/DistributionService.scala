package webapp.services

import java.util.concurrent.ThreadLocalRandom

class DistributionService:
  val replicaId: String = ThreadLocalRandom.current().nextLong().toHexString
