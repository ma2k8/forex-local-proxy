package forex.services.oneforge.api

import cats.Monad
import cats.syntax.functor._
import com.softwaremill.sttp._

import forex.adapter.http.HttpClient
import forex.config.OneforgeConfig
import forex.domain.Rate
import forex.domain.oneforge.{ MarketStatus, Quota, Quote }
import forex.services.oneforge.Error

trait OneforgeApiBase[F[_]] {
  val oneforgeConfig: OneforgeConfig
  def quote(apiKey: String, pair: Rate.Pair): F[Either[Error, Quote]]
  def marketStatus(apiKey: String): F[Either[Error, MarketStatus]]
  def quota(apiKey: String): F[Either[Error, Quota]]
}


/**
  * External-api of 1forge
  *
  * Doc: https://1forge.com/forex-data-api/api-documentation
  */
class OneforgeApi[F[_]: Monad](override val oneforgeConfig: OneforgeConfig, httpClient: HttpClient[F]) extends OneforgeApiBase[F] {

  /**
    * Get quotes for specific currency pair
    *
    * @param pair currency pair
    * @return
    */
  override def quote(apiKey: String, pair: Rate.Pair): F[Either[Error, Quote]] = {
    val uri = uri"${oneforgeConfig.baseUri}/quotes?pairs=${pair.from}${pair.to}&api_key=$apiKey"
    httpClient.getRequest[List[Quote]](uri).map(_.map(_.head))
  }

  /**
    * Check if the market is open
    *
    * @return
    */
  override def marketStatus(apiKey: String): F[Either[Error, MarketStatus]] = {
    val uri = uri"${oneforgeConfig.baseUri}/market_status?api_key=$apiKey"
    httpClient.getRequest[MarketStatus](uri) // TODO: Perhaps, if the market is closed, the rate may not change
  }

  /**
    * Check your current usage and remaining quota
    *
    * @return
    */
  override def quota(apiKey: String): F[Either[Error, Quota]] = {
    val uri = uri"${oneforgeConfig.baseUri}/quota?api_key=$apiKey"
    httpClient.getRequest[Quota](uri)
  }

}
