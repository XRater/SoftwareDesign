package ru.spbau.lupuleac.cli
import scala.collection.mutable.ListBuffer

trait Token

case class VarName(value : String) extends Token

case class Plain(value : String) extends Token

case class NotFinished() extends Token

trait LexerAction {
  val buffer : String
  def apply(c : Char) : (Token, LexerAction)
}

case class SingleQuoted(buffer : String) extends LexerAction {
  override def apply(c : Char) : (Token, LexerAction) = {
    c match {
      case '\'' => (Plain(buffer), Simple(""))
      case _ => (NotFinished(), SingleQuoted(buffer + c))
    }
  }
}


case class DoubleQuoted(buffer : String) extends LexerAction {
  override def apply(c : Char) : (Token, LexerAction) = {
    c match {
      case '\"' => (Plain(buffer), Simple(""))
      case '$' => (Plain(buffer), VarCall(DoubleQuoted(""), ""))
      case _ => (NotFinished(), DoubleQuoted(buffer + c))
    }
  }
}

case class VarCall(parent : LexerAction, buffer : String) extends LexerAction {
  override def apply(c : Char) : (Token, LexerAction) = {
    val stopRegex = "(\\||\\s|\"|\')"
    if (c.toString matches stopRegex) {
      (VarName(buffer), parent(c)._2)
    } else {
      (NotFinished(), VarCall(parent, buffer + c))
    }
  }
}

case class Simple(buffer: String) extends LexerAction {
  override def apply(c : Char) : (Token, LexerAction) = {
    c match {
      case ' ' => (Plain(buffer + "\n"), Simple(""))
      case '|' => (Plain(buffer + "\n|\n"), Simple(""))
      case '\'' => (Plain(buffer), SingleQuoted(""))
      case  '\"' => (Plain(buffer), DoubleQuoted(""))
      case '$' => (Plain(buffer), VarCall(Simple(""), ""))
      case _ => (NotFinished(), Simple(buffer + c))
    }
  }
}

class Lexer(scope : Scope) {
  def splitLineToTokens(line : String) : Array[String] = {
    var action  = Simple("") : LexerAction
    var tokens = ListBuffer[String]()
    for (c <- line + " ") {
      val (token, newAction) = action(c)
      action = newAction
      token match {
        case NotFinished() =>
        case VarName(name) => tokens += scope(name)
        case Plain(text) => tokens += text
      }
    }
    tokens.toList.mkString("").split("[\n]+")
  }
}
