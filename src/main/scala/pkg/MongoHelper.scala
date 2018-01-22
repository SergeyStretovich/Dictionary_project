package pkg

import com.typesafe.config.ConfigFactory
import org.mongodb.scala._
//import org.bson.{BsonObjectId, BsonString}
import org.mongodb.scala.bson.{BsonArray, BsonBoolean, BsonInt32, BsonObjectId,BsonString}
import org.mongodb.scala.{Document, MongoClient, MongoCollection, MongoDatabase}
//import org.mongodb.scala.bson.{BsonObjectId, BsonString}
import org.mongodb.scala.model.Filters.regex
import pkg.MongoAccessWrapper._
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model._
import scala.collection.immutable.IndexedSeq
import org.mongodb.scala._
import org.mongodb.scala.bson.conversions
import org.mongodb.scala.model.Aggregates._
import org.mongodb.scala.model.Filters._
//import org.mongodb.scala.model.
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Sorts._
import org.mongodb.scala.model.Updates._
import org.mongodb.scala.model._
import org.mongodb.scala.model.FindOptions

object MongoHelper
{
  var mongoAccessString=""
  def getMongoDbConfig():String=
  {
    if(mongoAccessString=="") {
      //val dbName = ConfigFactory.load().getString("conf.db.mongo.name.value")
      val host = ConfigFactory.load().getString("conf.db.mongo.host.value")
      /*
      val user = ConfigFactory.load().getString("conf.db.user.value")
      val password = ConfigFactory.load().getString("conf.db.password.value")
    */
      mongoAccessString = s"mongodb://${host}"
    }
    mongoAccessString
  }

  def getWordsByPrefix(prefix:String): List[WordItem] =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("testwords")
    val pattern="^"+prefix;
    collection.find(regex("word", pattern)).results().map(x=>WordItem(0,x.get("word").getOrElse(0).asInstanceOf[BsonString].getValue().toString,
      x.get("translation").getOrElse(0).asInstanceOf[BsonString].getValue().toString)).toList

    // mongoClient.close();
  }
  def getWordsTotal(): List[WordItem] =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("testwords")
    collection.find().results().map(x=>WordItem(x.get("_id").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
      x.get("word").getOrElse(0).asInstanceOf[BsonString].getValue().toString,
      x.get("translation").getOrElse(0).asInstanceOf[BsonString].getValue().toString)).toList

    // mongoClient.close();
  }
   def getUserWordsCards_check(): List[WordCard] =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("userwords")
    collection.find().results().map(x=>WordCard(x.get("_id").getOrElse(0).asInstanceOf[BsonObjectId].getValue().toString,
      x.get("userId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
      x.get("wordId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
      "nothing",
      x.get("customTranslation").getOrElse(0).asInstanceOf[BsonString].getValue().toString,
      x.get("isLearned").getOrElse(0).asInstanceOf[BsonBoolean].getValue()
    )
   // println(x.get("_id").getOrElse(0).asInstanceOf[BsonObjectId].getValue())
    ).toList
  }
  def insertWordCardsTotal(userId:Int): Unit =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("userwords")
    val allWords=getWordsTotal()
    val documents: List[Document] = allWords.map(x=>Document("userId"->userId,"wordId"->x.id,"customTranslation"->x.translation,"isLearned"->false))
    collection.insertMany(documents).results()
  }
  def print(): Unit =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("testwords")
    collection.find().results().foreach(x=>println(x.get("_id").getOrElse(0).asInstanceOf[BsonInt32].getValue()))
  }
  def dropCollection(name:String): Unit =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection(name)
    collection.drop().results()
  }
  def getUserWordCardsToLearn(userId:Int, wordIndex:String): List[WordCard] =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val testWordsCollection: MongoCollection[Document] = database.getCollection("userwords")

   val something= testWordsCollection.aggregate(Seq(
      filter(and(equal("userId", userId),equal("isLearned",false))),
      lookup("testwords","wordId","_id","joinedOutput")
    )).results()
     .map(x=>
     WordCard(x.get("_id").getOrElse(0).asInstanceOf[BsonObjectId].getValue().toString,
       x.get("userId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
         x.get("wordId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
         x.get("joinedOutput").getOrElse(0).asInstanceOf[BsonArray].getValues().get(0).toString.split(",")(1).toString.replace(""""word" : ""","").replace('"',' ').trim,
       x.get("customTranslation").getOrElse(0).asInstanceOf[BsonString].getValue().toString,
       false
     )
   ).toList.filter(x=>x.word.startsWith(wordIndex))
/**/
/*
   something.foreach(x=>
    println( x.get("_id").getOrElse(0).asInstanceOf[BsonObjectId].getValue().toString+" "+
     x.get("userId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt+" "+
     x.get("wordId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt+" "+
     x.get("custom").getOrElse(0).asInstanceOf[BsonString].getValue().toString +" "+
     x.get("total").getOrElse(0).asInstanceOf[BsonArray].getValues().get(0).toString.split(",")(1).toString.replace(""""word" : ""","").replace('"','\0').trim
   )
   )
*/
    something
  }
  def getUserWordCardsToEdit(userId:Int, wordIndex:String): List[WordCard] =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val testWordsCollection: MongoCollection[Document] = database.getCollection("userwords")

    val something= testWordsCollection.aggregate(Seq(
      filter(equal("userId", userId)),
      lookup("testwords","wordId","_id","joinedOutput")
    )).results()
      .map(x=>
        WordCard(x.get("_id").getOrElse(0).asInstanceOf[BsonObjectId].getValue().toString,
          x.get("userId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
          x.get("wordId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
          x.get("joinedOutput").getOrElse(0).asInstanceOf[BsonArray].getValues().get(0).toString.split(",")(1).toString.replace(""""word" : ""","").replace('"',' ').trim,
          x.get("customTranslation").getOrElse(0).asInstanceOf[BsonString].getValue().toString,
          x.get("isLearned").getOrElse(0).asInstanceOf[BsonBoolean].getValue()
        )
      ).toList.filter(x=>x.word.startsWith(wordIndex))
    /**/
    /*
       something.foreach(x=>
        println( x.get("_id").getOrElse(0).asInstanceOf[BsonObjectId].getValue().toString+" "+
         x.get("userId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt+" "+
         x.get("wordId").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt+" "+
         x.get("custom").getOrElse(0).asInstanceOf[BsonString].getValue().toString +" "+
         x.get("total").getOrElse(0).asInstanceOf[BsonArray].getValues().get(0).toString.split(",")(1).toString.replace(""""word" : ""","").replace('"','\0').trim
       )
       )
    */
    something
  }
  def getRandomWords(count:Int): List[WordItem] =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val testWordsCollection: MongoCollection[Document] = database.getCollection("testwords")
    val randomWords=testWordsCollection.find(sample(count)).results().toList.map(x=>WordItem(x.get("_id").getOrElse(0).asInstanceOf[BsonInt32].getValue().toInt,
      x.get("word").getOrElse(0).asInstanceOf[BsonString].getValue().toString,
      x.get("translation").getOrElse(0).asInstanceOf[BsonString].getValue().toString)).toList
    randomWords
  }

   def getUserLearnedWordsCount(userId:Int): String =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("userwords")
   // val result:Long =collection.count(filter((and(equal("userId", userId),equal("isLearned",true))))).results().asInstanceOf[Long]
    val result =collection.find(and(equal("userId", userId),equal("isLearned",true))).results().size

    //val result =collection.count().results()(0)
    val stringified=result.toString
    //println(result)
    stringified
  }
  def UpdateWordCardsetLearned(wordCardId:String): Unit =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("userwords")
    // val result:Long =collection.count(filter((and(equal("userId", userId),equal("isLearned",true))))).results().asInstanceOf[Long]
    val result =collection.updateOne(equal("_id", BsonObjectId(wordCardId)), set("isLearned", true)).headResult()
  }
  def UpdateWordCardsetTranslation(wordCardId:String,translation:String): Unit =
  {
    val mongoClient: MongoClient =  MongoClient(getMongoDbConfig(),None)
    val database: MongoDatabase = mongoClient.getDatabase("mydb")
    val collection: MongoCollection[Document] = database.getCollection("userwords")
    // val result:Long =collection.count(filter((and(equal("userId", userId),equal("isLearned",true))))).results().asInstanceOf[Long]
    val result =collection.updateOne(equal("_id", BsonObjectId(wordCardId)), set("customTranslation", translation)).headResult()
  }
}


























































