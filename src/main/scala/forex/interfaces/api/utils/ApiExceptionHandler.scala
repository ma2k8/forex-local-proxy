package forex.interfaces.api.utils

import com.typesafe.scalalogging.LazyLogging

import akka.http.scaladsl._
import forex.processes._

object ApiExceptionHandler extends LazyLogging {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case e: RatesError ⇒
        ctx ⇒
          logger.error(e.getMessage, e.getStackTrace.mkString("\n"))
          ctx.complete("Something went wrong in the rates process")
      case e: Throwable ⇒
        ctx ⇒
          logger.error(e.getMessage, e.getStackTrace.mkString("\n"))
          ctx.complete("Something else went wrong")
    }

}
