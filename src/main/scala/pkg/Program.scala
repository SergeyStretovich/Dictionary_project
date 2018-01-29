package pkg

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST._
import net.liftweb.json.{compactRender => _, _}
import pkg.LoginJsonSupport.jsonFormat2

import scala.concurrent.duration._
//import scala.collection.immutable.IndexedSeq
import org.mongodb.scala._
import org.mongodb.scala.bson.BsonString
//import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
//import org.mongodb.scala.model.Projections._
//import org.mongodb.scala.model.Sorts._
//import org.mongodb.scala.model.Updates._
//import org.mongodb.scala.model._
import java.sql.{DriverManager, ResultSet}

import akka.pattern.ask
import pkg.LoginJsonSupport._
import pkg.MongoAccessWrapper._

case class Login(email: String, password: String)
case class LoginAnswer(id:Int,answer:String)
case class User(id:BigInt,email:String,nickname:String,password:String)
case class WordsQuestion(userId:Int,pattern:String)
case class WordItem(id:Int,word:String,translation:String)
case class WordCard(id:String, userId:Int, wordId:Int,word:String, customTranslation:String, isLearned:Boolean)
case class UpdateWordCard(id:String,translation:String)
case class InsertWordCard(userId:Int,word:String,translation:String)



object Program extends App
{

  implicit val actorSystem = ActorSystem("system")
  implicit val actorMaterializer = ActorMaterializer()
  implicit val formats = net.liftweb.json.DefaultFormats
  implicit val askTimeout: Timeout = akka.util.Timeout(3.second)
  implicit val PortofolioFormats = jsonFormat2(Login)
  val route =
    post {
      pathSingleSlash {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body>single slash test page</body></html>"))
      } ~
        path("login") {
          entity(as[String]) {
            userJson=> {
              val loginTry = parse(userJson).extract[Login];
              val actor = actorSystem.actorOf(Props[UsersDBActor])
              onSuccess((actor ? (loginTry.email, loginTry.password)).mapTo[User]) { result =>
                complete(compactRender(decompose(LoginAnswer(result.id.toInt,result.nickname))))
              }
            }
          }
        } ~
        path("adduser")
      {
        entity(as[String]) {
          addUserTryJson=> {
            val addUserTry = parse(addUserTryJson).extract[User];
            val actor = actorSystem.actorOf(Props[UsersDBActor])
            onSuccess((actor ? (addUserTry.email,addUserTry.nickname, addUserTry.password)).mapTo[Int]) { result =>
              complete(compactRender(decompose(LoginAnswer(result,addUserTry.nickname))))
            }
          }
        }
      }~
        path("words") {
            entity(as[String]) {
              wordJson =>
                complete {
                  val question = parse(wordJson).extract[WordsQuestion];
                  val result = MongoHelper.getWordsByPrefix(question.pattern)
                  compactRender(decompose(result))
                }
            }
        }~
        (path("stats")& parameter("ids"))
        {
              {
            idUserParameter =>
               {
                 val userIdInt = parse(idUserParameter).extract[Int]
                val actor = actorSystem.actorOf(Props[WordsDBActor])
                onSuccess((actor ? ("stats",userIdInt)).mapTo[String]) { result =>
                  complete(compactRender(decompose(result)))
                }
              }
          }
        }~
        (path("setwordlearned")& parameter("ids"))
        {
          wordId =>
            {
              //val wordId = parse(idUserParameter).extract[Int]
              val actor = actorSystem.actorOf(Props[WordsDBActor])
              onSuccess((actor ? ("setwordlearned",wordId)).mapTo[String]) { result =>
                complete(compactRender(decompose(result)))
              }
            }
        }~
        (path("getcardsedit")& parameters('userId.as[Int], 'prefix.as[String]))
        {
          (userId,prefix) =>
          {
            //val userIdInt = parse(userIdString).extract[Int]
            val actor = actorSystem.actorOf(Props[WordsDBActor])
            onSuccess((actor ? ("getcardsedit",userId,prefix)).mapTo[List[WordCard]]) { result =>
              complete(compactRender(decompose(result)))
            }
          }
        }~
        (path("wordcards")& parameters('userId.as[Int], 'prefix.as[String]))
        {
          (userId,prefix) =>
          {
            val actor = actorSystem.actorOf(Props[WordsDBActor])
            onSuccess((actor ? ("wordcards",userId,prefix)).mapTo[List[WordCard]]) { result =>
              complete(compactRender(decompose(result)))
            }
          }
        }~
        path("updatewordcard")
        {
          entity(as[String]) {
            wordCardUpdateJson => {
              val updateWordTry = parse(wordCardUpdateJson).extract[UpdateWordCard];
              val actor = actorSystem.actorOf(Props[WordsDBActor])
              onSuccess((actor ? ("updatewordcard", updateWordTry.id, updateWordTry.translation)).mapTo[String]) { result =>
                complete(compactRender(decompose(result)))
              }
            }
          }
        }~
        path("insertwordcard")
        {
          entity(as[String]) {
            wordCardInsertJson => {
              val insertWordTry = parse(wordCardInsertJson).extract[InsertWordCard];
              val actor = actorSystem.actorOf(Props[WordsDBActor])
              onSuccess((actor ? ("insertwordcard", insertWordTry.userId,insertWordTry.word, insertWordTry.translation)).mapTo[String]) { result =>
                complete(compactRender(decompose(result)))
              }
            }
          }
        }~
      path("allusers") {
        entity(as[String]) {
          wordJson =>
           {
              val actor = actorSystem.actorOf(Props[UsersDBActor])
             onSuccess((actor ? "select").mapTo[List[User]]) { result =>
               complete(compactRender(decompose(result)))
             }
           }
        }
      }
    }


  Http().bindAndHandle(route,"localhost",8080)

  println("server started at 8080")

}
