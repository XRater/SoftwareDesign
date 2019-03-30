package ru.spbau.lupuleac.cli.commands

import java.io
import java.nio.file
import java.nio.file.Paths

import ru.spbau.lupuleac.cli.Interpreter

import scala.reflect.io.{File, Path}

/**
  * Returns a file (or files) contents.
  */
case class LsCommand(stdin: Input, arguments: List[String]) extends Command {
  override val name: String = "ls"

  override def isValid: Boolean = true

  override def execute(interpreter: Interpreter): String = {
    if (arguments.size > 1) throw new Exception("Too much arguments")
    val target : String = if (arguments.isEmpty) "" else arguments.head
    val targetPath : file.Path = if (Paths.get(target).isAbsolute) {
      Paths.get(target)
    } else {
      Paths.get(interpreter.currentDirectory.toString, target)
    }
    val targetFile : io.File = targetPath.toFile
    if (!targetFile.exists()) {
      return "bash: no such file or directory " + targetPath.toString
    }
    if (targetFile.isDirectory) {
      targetFile.listFiles().map(f => f.getName).mkString(System.lineSeparator())
    } else {
      targetFile.getName
    }
  }
}
