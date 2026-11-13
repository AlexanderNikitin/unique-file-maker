package command

import Settings
import checksum.CheckSumUpdater
import db.FileRepository
import picocli.CommandLine
import java.util.*

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

        val duplicates = potentialDuplicates
            .mapValues { g ->
                g.value
                    .map { CheckSumUpdater(it, fileRepository) }
                    .filter {
                        if (!it.valid) {
                            val error = "Путь отброшен по причине невалидност хеша"
                            val entity = it.fileEntity
                            entity.errorMessage = error
                            fileRepository.save(entity)
                            return@filter false
                        }
                        return@filter true
                    }
            }
            .mapValues { g ->
                g.value.groupBy {
                    val sum = it.sum
                    if (sum == null) {
                        val entity = it.fileEntity
                        val error = "Путь ассоциирован с нулевым хешем"
                        entity.errorMessage = error
                        fileRepository.save(entity)
                    }
                    sum
                }
            }
            .mapValues { bySize ->
                bySize.value.filterValues {
                    val size = it.size
                    if (size == 1) {
                        val single = it.first()
                        val entity = single.fileEntity
                        val error = "Файл в единственном экземпляре"
                        entity.errorMessage = error
                        fileRepository.save(entity)
                        return@filterValues false
                    } else if (size == 0) {
                        if (Settings.debug) println("Группа размера пуста")
                        return@filterValues false
                    }
                    return@filterValues true
                }
            }

        duplicates.values.forEach {
            it.values.forEach { g ->
                val group = UUID.randomUUID()
                g.forEach { f ->
                    f.fileEntity.groupId = group
                    fileRepository.save(f.fileEntity)
                }
            }
        }
    }
}