package pkg

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import pkg.LoginJsonSupport.jsonFormat2
import spray.json.DefaultJsonProtocol

object LoginJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val PortofolioFormats = jsonFormat2(Login)
}
