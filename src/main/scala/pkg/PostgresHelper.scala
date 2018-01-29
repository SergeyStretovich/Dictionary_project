package pkg

import java.sql.{DriverManager, ResultSet, Statement}

import com.typesafe.config.ConfigFactory
import scala.collection.mutable.ListBuffer
import pkg.User

object PostgresHelper {
  var postgresAccessString = ""

  //  Для чего нужна эта функция ?
  def getPostgresDbConfig(): String = {
    if (postgresAccessString == "") {
      val dbName = ConfigFactory.load().getString("conf.db.postgres.name.value")
      val host = ConfigFactory.load().getString("conf.db.postgres.host.value")
      val user = ConfigFactory.load().getString("conf.db.postgres.user.value")
      val password = ConfigFactory.load().getString("conf.db.postgres.password.value")

      postgresAccessString = s"jdbc:postgresql://${host}/${dbName}?user=${user}&password=${password}"
    }
    postgresAccessString
  }
  //  Для чего нужна эта функция ?
  def selectAllUsers(): List[User] = {
    var users = new ListBuffer[User]

    classOf[org.postgresql.Driver]
    val con_st = getPostgresDbConfig()
    val conn = DriverManager.getConnection(con_st)
    try {
      val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

      val rs = stm.executeQuery("SELECT * from Users")

      while (rs.next) {
        users += User(rs.getLong("id"), rs.getString("email"), rs.getString("nickname"), rs.getString("password"))
      }
    } finally {
      conn.close()
    }
    users.toList
  }
  //  Для чего нужна эта функция ?
  def selectOneUser(id: Int): User = {
    var usr: User = null
    classOf[org.postgresql.Driver]
    val con_st = getPostgresDbConfig()
    val conn = DriverManager.getConnection(con_st)
    try {
      val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
      val rs = stm.executeQuery(s"SELECT * from Users where id=${id}")

      while (rs.next) {
        usr = User(rs.getLong("id"), rs.getString("email"), rs.getString("nickname"), rs.getString("password"))
      }
      if (usr == null) {
        usr = User(0, "declined", "declined", "declined")
      }
    } finally {
      conn.close()
    }
    usr
  }
  //  Для чего нужна эта функция ?
  def selectOneUser(email: String, password: String): User = {
    var usr: User = null
    classOf[org.postgresql.Driver]
    val con_st = getPostgresDbConfig()
    val conn = DriverManager.getConnection(con_st)
    try {
      val stm = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)

      val rs = stm.executeQuery(s"SELECT * from Users where email='${email}' and password='${password}';")
      while (rs.next) {
        usr = User(rs.getLong("id"), rs.getString("email"), rs.getString("nickname"), rs.getString("password"))
      }
      if (usr == null) {
        usr = User(0, "declined", "declined", "declined")
      }
    } finally {
      conn.close()
    }
    usr
  }
  //  Для чего нужна эта функция ?
  def insertUser(email: String, nickName: String, password: String): Int = {
    var insertedId: Int = 0
    classOf[org.postgresql.Driver]
    val con_st = getPostgresDbConfig()
    val conn = DriverManager.getConnection(con_st)
    try {
      val stm = conn.createStatement()

      val rs = stm.executeUpdate(s"INSERT INTO users (email,nickname, password)   VALUES ('${email}', '${nickName}','${password}'); ", Statement.RETURN_GENERATED_KEYS)
      val keyset = stm.getGeneratedKeys()
      keyset.next()
      insertedId = keyset.getInt(1)
    } finally {
      conn.close()
    }
    insertedId
  }


}
