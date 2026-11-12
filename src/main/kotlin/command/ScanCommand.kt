package command

import collect.DbFilesCollector
import org.apache.commons.lang3.SystemUtils
import picocli.CommandLine
import scan.*
import java.io.FilenameFilter
import java.util.regex.Pattern

@CommandLine.Command(name = "scan", description = ["Составление списка файлов в указанных директориях"])
class ScanCommand : Runnable {
    @CommandLine.Parameters(arity = "1..*", description = ["Список корневых директорий для сканирования"])
    private var folders: Array<String> = arrayOf()

    @CommandLine.Option(names = ["-r", "--recursive"], description = ["Сканировать дректории рекурсивно"])
    private var recursive: Boolean = false

    @CommandLine.Option(names = ["-e", "--extensions"], arity = "0..*", description = ["Только файлы с этими расширениями"])
    private var extensions: Array<String> = arrayOf()

    @CommandLine.Option(names = ["-x", "--excludes"], arity = "0..*", description = ["Исключения (список RegExp)"])
    var excludes: Array<String> = arrayOf()

    override fun run() {
        val filterList: MutableList<FilenameFilter> = mutableListOf()

        excludes.forEach { filterList.add(RegExpExcludeFilenameFilter(Pattern.compile(it))) }

        extensions.forEach { filterList.add(MatchExtensionFileNameFilter(it)) }

        filterList.add(MatchFileNameFilter("Vivaldi"))

        if (SystemUtils.IS_OS_WINDOWS) {
            filterList.add(RegExpExcludeFilenameFilter(Pattern.compile("_files$")))
            filterList.add(MatchFileNameFilter($$"$RECYCLE.BIN"))
        }

        val collector = DbFilesCollector()
        val fileNameFilter = AgregateFileNameFilter(filterList)
        val scanner = FilesScanner(collector, fileNameFilter)
        folders.forEach { scanner.scan(it, recursive) }
    }
}
