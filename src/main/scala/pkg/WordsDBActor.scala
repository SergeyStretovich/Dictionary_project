package pkg

import akka.actor.{Actor, PoisonPill}

class WordsDBActor extends Actor
{
  def receive =
  {
    case ("stats",id:Int)=>
    {
      val learnedWordsStats=MongoHelper.getUserLearnedWordsCount(id)
      sender()!learnedWordsStats
      self ! PoisonPill
    }
    case ("setwordlearned",wordCardId:String)=>
    {
        val user=MongoHelper.setWordCardLearned(wordCardId)
        sender() ! "true"

      self ! PoisonPill
    }
    case ("wordcards",userId:Int,prefix:String)=>
    {
      val wordCards=MongoHelper.getUserWordCardsToLearn(userId,prefix)
      sender() ! wordCards
      self ! PoisonPill
    }
    case ("getcardsedit",userId:Int,prefix:String)=>
    {
      val wordCards=MongoHelper.getUserWordCardsToEdit(userId,prefix)
      sender() ! wordCards

      self ! PoisonPill
    }
    case _=>  sender() ! "something wrong"
      self ! PoisonPill
  }
}
