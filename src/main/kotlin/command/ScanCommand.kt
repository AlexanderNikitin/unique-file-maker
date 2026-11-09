package command

import fs.FileSearcher
import picocli.CommandLine

@CommandLine.Command(name = "scan")
class ScanCommand : Runnable {
    @CommandLine.Parameters(arity = "1..*")
    private var folders: Array<String> = arrayOf()

    @CommandLine.Option(names = ["-r", "--recursive"])
    private var recursive: Boolean = false

    @CommandLine.Option(names = ["-e", "--extensions"], arity = "0..*")
    private var extensions: Array<String> = arrayOf()

    @CommandLine.Option(names = ["-x", "--excludes"], arity = "0..*")
    var excludes: Array<String> = arrayOf()

    override fun run() {
        val result = FileSearcher(extensions, recursive, excludes).search(folders)
    }
}
