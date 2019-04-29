package forex.adapter.http

import com.softwaremill.sttp.circe.asJson
import com.softwaremill.sttp.{ sttp, Request, Response, SttpBackend, Uri }

import io.circe.Decoder

import cats.Monad
import cats.syntax.either._
import cats.syntax.functor._

import forex.services.oneforge.Error

import scala.util.{ Failure, Success, Try }

class HttpClient[F[_]: Monad]()(implicit _sttpBackend: SttpBackend[F, Nothing]) {

  def getRequest[A](uri: Uri)(implicit decoder: Decoder[A]): F[Either[Error, A]] =
    execHttpRequest { sttp.get(uri).response(asJson[A]) }

  private def execHttpRequest[A](request: ⇒ Request[Either[io.circe.Error, A], Nothing]): F[Either[Error, A]] =
    Try(request.send()) match {
      case Success(response)  ⇒ response.map(handleResponse)
      case Failure(throwable) ⇒ Monad[F].pure(Left(Error.System(throwable)))
    }

  private def handleResponse[A](response: Response[Either[io.circe.Error, A]]): Either[Error, A] =
    for {
      body ← response.body.leftMap(Error.Api)
      result ← body.leftMap(Error.Json)
    } yield result
}
