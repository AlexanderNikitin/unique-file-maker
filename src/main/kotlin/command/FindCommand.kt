package command

import Settings
import checksum.CheckSumUpdater
import db.FileRepository
import picocli.CommandLine
import java.util.UUID

@CommandLine.Command(
    name = "find",
    description = [
        "Группировка файлов по дублям на основе размера, хеша и сравнения контента"
    ]
)
class FindCommand : Runnable {
    private val fileRepository = FileRepository()

    override fun run() {
        val potentialDuplicates = fileRepository.findPotentialDuplicates()

        if (Settings.debug) potentialDuplicates.entries.forEach {
            println("${it.key} ${it.value.size}")
            it.value.forEach { f -> println("${f.path}") }
        }

        val duplicates = potentialDuplicates
            .mapValues { g ->
                g.value
                    .map { CheckSumUpdater(it, fileRepository) }
                    .filter { it.valid }
            }
            .filter { it.value.size > 1 }
            .mapValues { g -> g.value.associateBy { it.sum } }
            .filterValues { it.size > 1 }
            .filter { it.value.isNotEmpty() }

        duplicates.values.forEach {
            val group = UUID.randomUUID()
            it.values.forEach { f ->
                f.fileEntity.groupId = group
                fileRepository.save(f.fileEntity)
            }
        }

        duplicates.entries.forEach {
            println("${it.key} ${it.value.size}")
            it.value.forEach { f -> println("${f.value.fileEntity.path}") }
        }
    }
}