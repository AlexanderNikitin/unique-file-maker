import command.ScanCommand
import deleting.DuplicateDeleteSolver
import deleting.Parameters
import deleting.Rule
import picocli.CommandLine

@CommandLine.Command(name = "df", subcommands = [ScanCommand::class])
class DuplicateFinder : Runnable {
    @CommandLine.Option(names = ["-g", "--groupbyext"])
    var groupbyext: Boolean = false

    @CommandLine.Option(names = ["-o", "--saveonlyone"])
    var saveOnlyOne: Boolean = false

    @CommandLine.Option(names = ["-c", "--check"])
    var check: Boolean = false

    @CommandLine.Option(names = ["-d", "--delete"])
    var delete: Boolean = false

    @CommandLine.Option(names = ["-s", "--savepreferes"], arity = "0..*")
    var savePreferes: Array<String?>? = null

    @CommandLine.Option(names = ["-n, --nodelete"], arity = "0..*")
    var noDelete: Array<String?>? = null

    override fun run() {
        //TODO
    }

    private val solver: DuplicateDeleteSolver
        get() {
            val lSaveRules: MutableList<Rule?> = ArrayList<Rule?>()

            lSaveRules.add(Rule(Parameters.IS_ENGLISH_FILE_NAME, false))
            lSaveRules.add(Rule(Parameters.IS_COPY, false))
            lSaveRules.add(Rule(Parameters.DIRECTORY_DEPTH, true))
            lSaveRules.add(Rule(Parameters.FILENAME_LENGTH, true))
            lSaveRules.add(Rule(Parameters.PATH_LENGTH, true))

            return DuplicateDeleteSolver(lSaveRules, saveOnlyOne, savePreferes, noDelete)
        }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.exit(CommandLine(DuplicateFinder()).execute(*args))
        }
    }
}
