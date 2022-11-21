package functions.device

import functions.device.services.*

trait DeviceServices:
  lazy val storage: StorageServiceInterface
