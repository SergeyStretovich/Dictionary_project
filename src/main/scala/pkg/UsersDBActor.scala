package pkg

import akka.actor.{Actor, PoisonPill}

class UsersDBActor extends Actor
{
  def receive =
  {
    case "select"=>
    {
      val allUsers=PostgresHelper.selectAllUsers()
      sender()!allUsers
      self ! PoisonPill
    }
    case (query:String,id:Int)=>
    {
      if(query=="select") {
        val user=PostgresHelper.selectOneUser(id)
        sender() ! user
      }
      self ! PoisonPill
    }
    case (email:String,password:String)=>
    {
       val user=PostgresHelper.selectOneUser(email,password)
      sender() ! user
      self ! PoisonPill
    }
    case (email:String,nickName:String,password:String)=>
    {
        val userId=PostgresHelper.insertUser(email,nickName,password)
        sender() ! userId

      self ! PoisonPill
    }
    case _=>  sender() ! "something wrong"
      self ! PoisonPill
  }
}
