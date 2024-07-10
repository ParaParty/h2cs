package party.para

import org.gradle.api.Plugin
import org.gradle.api.Project
import party.para.tasks.H2CSTask

class H2CSPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Milize H2CS, Launched!")
        target.tasks.register("h2cs", H2CSTask::class.java)
    }
}
