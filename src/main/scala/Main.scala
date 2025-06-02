import com.tsurugidb.iceaxe.TsurugiConnector
import com.tsurugidb.iceaxe.metadata.TgTableMetadata
import com.tsurugidb.iceaxe.session.TsurugiSession
import com.tsurugidb.iceaxe.sql.{TsurugiSqlStatement, TsurugiSqlQuery}
import com.tsurugidb.iceaxe.sql.result.{
  TsurugiResultEntity,
  TsurugiStatementResult,
  TsurugiQueryResult
}
import com.tsurugidb.iceaxe.transaction.manager.{
  TgTmSetting,
  TsurugiTransactionManager
}
import com.tsurugidb.iceaxe.transaction.option
import com.tsurugidb.iceaxe.transaction.option.TgTxOption
import com.tsurugidb.iceaxe.transaction.option.TgTxOptionLtx
import com.tsurugidb.iceaxe.transaction.exception.TsurugiTransactionException
import com.tsurugidb.iceaxe.transaction.TsurugiTransaction

import com.tsurugidb.tsubakuro.common.{Session, SessionBuilder}
import com.tsurugidb.tsubakuro.sql.{SqlClient, Transaction}
import com.tsurugidb.tsubakuro.kvs.{KvsClient, RecordBuffer, TransactionHandle}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext}
import scala.jdk.CollectionConverters._
import scala.util.{Using, Try, Success, Failure}
import java.net.URI
class Table(
    val tableName: String,
    val format: String,
    val rowCount: Int,
    val columnCount: Int
) {
  def getTableName: String = tableName
  def getFormat: String = format
  def getRowCount: Int = rowCount
  def getColumnCount: Int = columnCount

  def createRecordBuffer(id: Int, name: String): RecordBuffer = {
    val record = new RecordBuffer()
    record.add("id", id)
    record.add("name", name)
  }
}
private val Connect = "ipc://tsurugi"
private val TableName = "like_table"
private val ColumnString = "(id int primary key, name varchar)"
private val inputString = "abcdefghijklmnop"
private val Columncount = 10

def sqlExecute(
    kvs: KvsClient,
    table: Table
): Unit = {
  insert(kvs, table)
}
def insert(kvs: KvsClient, table: Table)(implicit
    ec: ExecutionContext
): Unit = {
  println(s"insert ${table.getTableName} column ${table.getColumnCount}")
  Try {
    val tx = kvs.beginTransaction().await
    val range = Range(0, table.getColumnCount, 1)
    range.foreach { i =>
      val record = table.createRecordBuffer(i, inputString)
      kvs.put(tx, table.getTableName, record).await

    }
    kvs.commit(tx).await
    tx.close()
  } recover { case e: Exception =>
    println(e.getMessage)
  }
}
@main def run(): Unit = {
  println("start")
  val endpoint = URI.create(Connect)
  val connector = TsurugiConnector.of(endpoint)
  val table = new Table(
    TableName,
    "(id int primary key, name varchar)",
    2,
    Columncount
  )
  val drop = s"DROP TABLE IF EXISTS ${table.getTableName}"
  val create = s"CREATE TABLE ${table.getTableName} ${table.getFormat}"
  val session = SessionBuilder.connect(endpoint).create()
  val sql = SqlClient.attach(session)
  val kvs = KvsClient.attach(session)
  println(s"drop and create table: ${table.getTableName}")
  Try {
    val transaction = sql.createTransaction().await
    transaction.executeStatement(drop).await
    transaction.commit().await
    transaction.close()
  } recover { case e: Exception =>
    println(e.getMessage)
  }
  Try {
    val transaction = sql.createTransaction().await
    transaction.executeStatement(create).await
    transaction.commit().await
    transaction.close()
  } recover { case e: Exception =>
    println(e.getMessage)
  }
  Try {
    insert(kvs, table)
  } recover { case e: Exception =>
    println(e.getMessage)
  }
}

