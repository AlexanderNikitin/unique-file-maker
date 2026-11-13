package command

import db.FileRepository
import picocli.CommandLine

@CommandLine.Command(
    name = "mark",
    description = ["Помечает файлы для удаления"]
)
class MarkCommand : Runnable {
    private val fileRepository = FileRepository()

    override fun run() {
        fileRepository.findDuplicates().values
            .filter { it.size > 1 }
            .map { it.subList(1, it.size) }
            .forEach {
                it.forEach { f ->
                    f.delete = true;
                    fileRepository.save(f)
                }
            }
    }
}