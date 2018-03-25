package forex.domain.oneforge

import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._

case class Quota(
    quota_used: Int,
    quota_limit: Int,
    quota_remaining: Int,
    hours_until_reset: Int
)

object Quota {
  implicit val decoder: Decoder[Quota] = deriveDecoder[Quota]
  implicit val encoder: Encoder[Quota] = deriveEncoder[Quota]
}
