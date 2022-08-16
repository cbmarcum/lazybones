package uk.co.cacoethes.gradle.tasks

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Rule
import org.gradle.api.tasks.bundling.Zip

import uk.co.cacoethes.gradle.lazybones.LazybonesConventions
import uk.co.cacoethes.gradle.util.NameConverter

/**
 * <p>A rule that creates tasks for publishing a Lazybones template package for a
 * Simple URL repository. The tasks have the name 'publishTemplate<templateName>',
 * where the template name is in camel-case. The publish tasks are automatically
 * configured to depend on the corresponding package task defined by
 * {@link PackageTemplateRule}.</p>
 * <p>The tasks will create a manifest file along with the template package.</p>
 */
class PublishTemplateRule implements Rule {
    Project project

    PublishTemplateRule(Project project) {
        this.project = project
    }

    @Override
    void apply(String taskName) {
        def m = taskName =~ /publishTemplate([A-Z\-]\S+)/
        if (m) {
            def camelCaseTmplName = m[0][1]

            def tmplName = taskToTemplateName(m[0][1])
            def tmplDir = project.extensions.lazybones.templateDirs.files.find { f -> f.name == tmplName }
            def tmplDescr = "Missing DESCRIPTION file"
            if (new File("$tmplDir/DESCRIPTION").exists()) {
                tmplDescr = project.file("$tmplDir/DESCRIPTION").text.trim()
            }

            def pkgTask = (Zip) project.tasks.getByName("packageTemplate${camelCaseTmplName}")
            if (!pkgTask) return

            def lzbExtension = project.extensions.lazybones

            project.tasks.create(taskName, SimpleManifestEntry).with { t ->
                dependsOn pkgTask
                artifactFile = pkgTask.archivePath

                // for publishing manifest
                // name,version,owner,description
                packageName = pkgTask.baseName
                version = pkgTask.version
                tmplOwner = lzbExtension.templateOwner
                tmplDescription = tmplDescr
                tmplDestination = pkgTask.destinationDirectory.get()

                doFirst {
                    def missingProps = verifyPublishProperties(t)
                    if (!tmplOwner && !tmplDescription) missingProps << "repositoryName"
                    if (missingProps) {
                        throw new GradleException(
                                """\
You must provide values for these settings:

    ${missingProps.join(", ")}

For example, in your build file:

    lazybones {
        templateOwner = "Your Name"
    }
""")
                    }

                    if (!artifactFile.exists()) {
                        throw new GradleException("Bad build file: zip archive '${pkgTask.archiveName}' does not exist," +
                                " but should have been created automatically.")
                    }
                } // doFirst

            } // with
        } // m (matcher)
    }


    protected String taskToTemplateName(String requestedTemplateName) {
        // The rule supports tasks of the form packageTemplateMyTmpl and
        // packageTemplate-my-tmpl. Only the former requires conversion of
        // the name to lowercase hyphenated.
        return requestedTemplateName.startsWith("-") ? requestedTemplateName.substring(1) :
                NameConverter.camelCaseToHyphenated(requestedTemplateName)
    }


    /**
     * @return a list of convention properties that are required for publishing
     * and need supplied in the build file.
     */
    protected List verifyPublishProperties(task) {
        ["tmplOwner"].findAll { !task.getProperty(it) }
    }

    @Override
    String getDescription() {
        return "publishTemplate<TmplName> - Publishes the named template package for a URL repository"
    }

    @Override
    String toString() { return "Rule: $description" }
}
