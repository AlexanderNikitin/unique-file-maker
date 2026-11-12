package command

import Settings
import checksum.CheckSumUpdater
import db.FileRepository
import picocli.CommandLine

@CommandLine.Command(
    name = "calc",
    description = [
        "Подсчет контрольных сумм найденых ранее файлов.",
        "Считает контрольные суммы для файлов, если они изменились или контрольная сума не считалась ранее"
    ]
)
class CalcCommand : Runnable {
    private val fileRepository = FileRepository()

    override fun run() {
        val potentialDuplicates = fileRepository.findPotentialDuplicates()
        if (Settings.debug) potentialDuplicates.entries.forEach {
            println("${it.key} ${it.value.size}")
            it.value.forEach { f -> println("${f.path}") }
        }
        potentialDuplicates.entries.forEach {
            it.value.forEach { f ->
                CheckSumUpdater(f, fileRepository).validate()
            }
        }
    }
}