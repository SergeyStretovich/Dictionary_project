package pkg

import akka.actor.{Actor, PoisonPill}

class WordsDBActor extends Actor
{
  def receive =
  {
    //  Для чего ?
    case ("stats",id:Int)=>
    {
      val learnedWordsStats=MongoHelper.getUserLearnedWordsCount(id)
      sender()!learnedWordsStats
      self ! PoisonPill
    }
    //  Для чего ?
    case ("setwordlearned",wordCardId:String)=>
    {
        MongoHelper.UpdateWordCardsetLearned(wordCardId)
        sender() ! "true"

      self ! PoisonPill
    }
    //  Для чего ?
    case ("wordcards",userId:Int,prefix:String)=>
    {
      val wordCards=MongoHelper.getUserWordCardsToLearn(userId,prefix)
      sender() ! wordCards
      self ! PoisonPill
    }
    //  Для чего ?
    case ("getcardsedit",userId:Int,prefix:String)=>
    {
      val wordCards=MongoHelper.getUserWordCardsToEdit(userId,prefix)
      sender() ! wordCards

      self ! PoisonPill
    }
    //  Для чего ?
    case ("updatewordcard",wordCardId:String,translation:String)=>
    {
      MongoHelper.UpdateWordCardsetTranslation(wordCardId,translation)
      sender() ! "true"
      self ! PoisonPill
    }
    //  Для чего ?
    case ("insertwordcard",userId:Int,word:String,translation:String)=>
    {
      MongoHelper.insertUserCard(userId,word,translation)
      sender() ! "true"
      self ! PoisonPill
    }
    //  Для чего ?
    case _=>  sender() ! "something wrong"
      self ! PoisonPill
  }
}
