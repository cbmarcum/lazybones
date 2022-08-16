package uk.co.cacoethes.gradle.tasks

import org.gradle.api.*
import org.gradle.api.tasks.*

/**
 * Task for creating a manifest entry for a Simple Package repository.
 */
class SimpleManifestEntry extends DefaultTask {

    /** The location on the local filesystem of the artifact to publish. */
    @InputFile
    File artifactFile

    /** The name of the package that this task will publish. */
    @Input
    String packageName

    /** The version of the package that this task will publish. */
    @Input
    String version

    /** The owner of the package that this task will publish. */
    @Input
    String tmplOwner

    /** The description of the package that this task will publish. */
    @Input
    String tmplDescription

    /** The destination of the package that this task will publish. */
    @Input
    String tmplDestination

    /**
     *
     * @return
     */
    @TaskAction
    def publish() {

        addManifestEntry()
    }

    /**
     * Method to create manifest file if needed and add this entry.
     */
    protected void addManifestEntry() {

        File destinationDir = new File(tmplDestination)
        destinationDir.mkdirs()
        String fs = File.separator
        File manifest = new File("${tmplDestination}${fs}skeletor-manifest.txt")

        if (manifest.createNewFile()) {
            manifest << "name,version,owner,description\n"
        }
        manifest << "${packageName},${version},${tmplOwner},${tmplDescription}\n"
    }

} // end Task