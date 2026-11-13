import command.CalcCommand
import command.FindCommand
import command.MarkCommand
import command.ScanCommand
import picocli.CommandLine
import kotlin.system.exitProcess

@CommandLine.Command(name = "df", subcommands = [
    ScanCommand::class,
    CalcCommand::class,
    FindCommand::class,
    MarkCommand::class,
    CommandLine.HelpCommand::class])
class DuplicateFinder

fun main(args: Array<String>) {
    exitProcess(CommandLine(DuplicateFinder()).execute(*args))
}
