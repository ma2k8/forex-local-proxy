package forex.services.oneforge

import scala.util.control.NoStackTrace

sealed trait Error extends Throwable with NoStackTrace
object Error {
  final case object Generic extends Error
  final case class System(underlying: Throwable) extends Error
  final case class Api(underlying: String) extends Error
  final case class Json(underlying: io.circe.Error) extends Error
}
