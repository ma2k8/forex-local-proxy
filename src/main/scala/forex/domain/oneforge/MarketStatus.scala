package forex.domain.oneforge

import io.circe.Decoder
import io.circe.generic.semiauto._

case class MarketStatus(
    marketIsOpen: Boolean
)

object MarketStatus {
    implicit val decoder: Decoder[MarketStatus] = deriveDecoder[MarketStatus]
}
