package uk.co.cacoethes.lazybones

import org.junit.*
import spock.lang.Ignore

class CreateFunctionalSpec extends AbstractFunctionalSpec {

    void setup() {
        filesToDelete << new File(cacheDirPath, "aoo-addin-0.3.0.zip")
    }

    def "Create command installs a packaged template"() {
        when: "I run skeletor with the create command for the aoo-addin template"
        def exitCode = runCommand(["create", "aoo-addin", "0.3.0", "test-addin", "-Pgroup=org.example", "-PartifactId=test-addin", "-Pversion=0.1.0", "-Ppackage=org.example", "-PclassName=TestAddin"], baseWorkDir)

        then: "It unpacks the template, retaining file permissions"
        exitCode == 0

        def appDir = new File(baseWorkDir, "test-addin")
        appDir.exists()
        new File(appDir, "gradlew").canExecute()
        new File(appDir, "src/main/groovy").isDirectory()
        new File(appDir, "src/main/groovy/org/example/TestAddinImpl.groovy").isFile()

        and: "It says that the given version of the package is being installed in the target directory"
        output =~ /Creating project from template aoo-addin 0.3.0 in 'test-addin'/
    }

    def "Create command installs latest version of a packaged template if version not specified"() {
        when: "I run skeletor with the create command for the aoo-addin template"
        def exitCode = runCommand(["create", "aoo-addin", "test-addin", "-Pgroup=org.example", "-PartifactId=test-addin", "-Pversion=0.1.0", "-Ppackage=org.example", "-PclassName=TestAddin"], baseWorkDir)

        then: "It unpacks the template, retaining file permissions"
        exitCode == 0

        def appDir = new File(baseWorkDir, "test-addin")
        appDir.exists()
        new File(appDir, "gradlew").canExecute()
        new File(appDir, "src/main/groovy").isDirectory()
        new File(appDir, "src/main/groovy/org/example/TestAddinImpl.groovy").isFile()

        and: "It says that the latest version of the package is being installed in the target directory"
        output =~ /Creating project from template aoo-addin \(latest\) in 'test-addin'/
    }

    def "Post-install script works with multiple asks (#106)"() {
        when: "creating a groovyapp with no pre-defined property values"
        def args = [
                "create",
                "test-tmpl",
                "0.2",
                "my-app"]
        def exitCode = runCommand(args, baseWorkDir, ["org.example", "1.0-SNAPSHOT", "4"])

        then: "It successfully completes"
        exitCode == 0

        and: "The generated build file contains the expected group ID and version"
        def text = new File(baseWorkDir, "my-app/build.gradle").text.trim()
        text.contains("group = \"org.example\"")
        text.contains("version = \"1.0-SNAPSHOT\"")
    }

    def "Post-install script works with include subscripts"() {
        when: "creating a groovyapp with no pre-defined property values"
        def args = [
                "create",
                "test-tmpl-subscripts",
                "0.2",
                "my-app"]
        def exitCode = runCommand(args, baseWorkDir, ["org.example", "1.0-SNAPSHOT", "4", "org.foo", "2.0-SNAPSHOT", "8"])

        then: "It successfully completes"
        exitCode == 0

        and: "The generated build file contains the expected group ID and version"
        def text = new File(baseWorkDir, "my-app/build.gradle").text.trim()
        text.contains("group = \"org.example\"")
        text.contains("version = \"1.0-SNAPSHOT\"")

        and: "The included lazybones script and subscript are deleted"
        !new File("$baseWorkDir/my-app", "lazybones.groovy").exists()
        !new File("$baseWorkDir/my-app", "sub1.groovy").exists()
    }

    def "Create command installs a template from an HTTP URL"() {
        when: "I run skeletor with the create command using a full URL for the aoo-addin template"
        def packageUrl = "https://codebuilders.jfrog.io/artifactory/generic/skeletor-templates/aoo-addin-0.3.0.zip"
        def exitCode = runCommand(["--verbose", "create", packageUrl, "test-addin", "-Pgroup=org.example", "-PartifactId=test-addin", "-Pversion=0.1.0", "-Ppackage=org.example", "-PclassName=TestAddin"], baseWorkDir)

        then: "It unpacks the template, retaining file permissions"
        exitCode == 0

        def appDir = new File(baseWorkDir, "test-addin")
        appDir.exists()
        new File(appDir, "gradlew").canExecute()
        new File(appDir, "src/main/groovy").isDirectory()
        new File(appDir, "src/main/groovy/org/example/TestAddinImpl.groovy").isFile()

        and: "It says that the latest version of the package is being installed in the target directory"
        output =~ /Creating project from template https:\/\/codebuilders.jfrog.io\/artifactory\/generic\/skeletor-templates\/aoo-addin-0.3.0.zip \(latest\) in 'test-addin'/

    }

    def "Create command installs a template from a URL mapping"() {
        when: "I run skeletor with the create command using mapping for the aoo-addin template"
        def exitCode = runCommand(["--verbose", "create", "customAooAddin", "test-addin", "-Pgroup=org.example", "-PartifactId=test-addin", "-Pversion=0.1.0", "-Ppackage=org.example", "-PclassName=TestAddin"], baseWorkDir)

        then: "It unpacks the template, retaining file permissions"
        exitCode == 0

        def appDir = new File(baseWorkDir, "test-addin")
        appDir.exists()
        new File(appDir, "gradlew").canExecute()
        new File(appDir, "src/main/groovy").isDirectory()
        new File(appDir, "src/main/groovy/org/example/TestAddinImpl.groovy").isFile()

        and: "It says that the latest version of the package is being installed in the target directory"
        output =~ /Creating project from template https:\/\/codebuilders.jfrog.io\/artifactory\/generic\/skeletor-templates\/aoo-addin-0.3.0.zip \(latest\) in 'test-addin'/

    }

        def "Create command installs a template from a file URL"() {
        when: "I run lazybones with the create command for the ratpack template"
        def packageUrl = getClass().classLoader.getResource("dummy-app.zip").toURI().toString()
        def exitCode = runCommand(["--verbose", "create", packageUrl, "mvnapp"], baseWorkDir)

        then: "It unpacks the template, retaining file permissions"
        exitCode == 0

        def appDir = new File(baseWorkDir, "mvnapp")
        appDir.exists()
        new File(appDir, "pom.xml").isFile()
        new File(appDir, "src/main/java").isDirectory()
        new File(appDir, "src/test/resources").isDirectory()

        and: "It says that the latest version of the package is being installed in the target directory"
        output =~ /Creating project from template file:\/.*\/dummy-app.zip \(latest\) in 'mvnapp'/
    }

    def "Create command installs a packaged template into current directory"() {
        given: "An existing application directory"
        def appDir =  new File(baseWorkDir, "ratapp2")
        appDir.mkdirs()

        when: "I run lazybones with the create command for the ratpack template in the app directory with '.'"
        def exitCode = runCommand(["create", "test-tmpl", "0.2", "."], appDir, ["org.example", "1.0-SNAPSHOT", "4"])

        then: "It unpacks the template, retaining file permissions"
        exitCode == 0

        appDir.exists()
        new File(appDir, "gradlew").canExecute()
        new File(appDir, "build.gradle").text =~ /version = "1.0-SNAPSHOT"/

        and: "It says that the package is being installed in the current directory"
        output =~ /Creating project from template test-tmpl 0.2 in current directory/
    }

    def "Create command reports error if no version given and package info is not available"() {
        when: "I run lazybones with the create command for an unknown package and no version"
        def exitCode = runCommand(["create", "unknown", "myapp"], baseWorkDir)

        then: "It returns a non-zero exit code and reports the package as missing"
        exitCode != 0
        output =~ /Cannot find a template named 'unknown'./

        !new File(baseWorkDir, "myapp").exists()
    }

    def "Create command reports errors if a mapped url does not exist"() {
        when: "I run lazybones with the create command for an unknown package using a mapping and no version"
        def exitCode = runCommand(["create", "doesNotExist", "myapp"], baseWorkDir)

        then: "It returns a non-zero exit code and reports the package as missing"
        exitCode != 0
        output =~ /Cannot find a template named 'file:\/\/\/does\/not\/exist'./

        !new File(baseWorkDir, "myapp").exists()
    }

    def "Create command reports error if no arguments given"() {
        when: "I run lazybones with the create command for an unknown package using a mapping and no version"
        def exitCode = runCommand(["create", "ratpack"], baseWorkDir)

        then: "It returns a non-zero exit code and reports the package as missing"
        exitCode != 0
        output =~ /Incorrect number of arguments\./
        !output.contains("Exception")
    }

    def "Create command reports error if package cannot be found"() {
        when: "I run lazybones with the create command for an unknown package"
        def exitCode = runCommand(["create", "unknown", "1.0", "myapp"], baseWorkDir)

        then: "It returns a non-zero exit code and reports the package as missing"
        exitCode != 0
        output =~ /Cannot find a template named 'unknown'./

        !new File(baseWorkDir, "myapp").exists()
    }

    def "Create command reports error if specified version of a package cannot be found"() {
        when: "I run lazybones with the create command for an unknown version of a known package"
        def exitCode = runCommand(["create", "aoo-addin", "99.99", "test-addin", "-Pgroup=org.example", "-PartifactId=test-addin", "-Pversion=0.1.0", "-Ppackage=org.example", "-PclassName=TestAddin"], baseWorkDir)

        then: "It returns a non-zero exit code and reports the package as missing"
        exitCode != 0
        output =~ /Cannot find version 99.99 of template 'aoo-addin'./

        !new File(baseWorkDir, "test-addin").exists()
    }

    @Ignore("not relevant with SimplePackageSource as any template listed in the manifest will have a version")
    def "Create command prints useful error message if no versions of a template are available"() {
        when: "I run lazybones with the create command for a template with no versions"
        def exitCode = runCommand(["create", "lazybones-project", "my-lzb-templates"], baseWorkDir)

        then: "It returns a non-zero exit code and displays an error message"
        exitCode == 1
        output =~ /No version of 'lazybones-project' has been published/
    }

    def "skeletor creates git repository on --with-git"() {
        given: "The platform line separator"
        def eol = System.getProperty("line.separator")

        when: "creating a groovyapp with all options passed in"
        def args = [
                "create",
                "test-tmpl",
                "0.2",
                "groovyappWithGit",
                "-Pversion=0.2",
                "-Pgroup=bar",
                "-PmaxThreads=3",
                "--with-git"]
        def exitCode = runCommand(args, baseWorkDir)

        then: "It successfully completes"
        exitCode == 0

        and: "Creates a git repository"
        def appDir = new File(baseWorkDir, "groovyappWithGit")
        new File(appDir, ".git").exists()

        and: "The .gitignore file contains the expected entries"
        def text = new File(appDir, ".gitignore").text.trim()
        text == "*.iws" + eol + "build/" + eol + "*.log"

        // Only include this verification if we're not running on Drone.io.
        // For some reason this assertion always fails on the CI server even
        // when it's passing locally.
        //
        and: "There are no untracked files"
        assert ["git", "status"].execute([], appDir).text.contains("nothing to commit")
    }

    def "Create can install from cache without template being in repository"() {
        when: "I run skeletor with the create command for a template that's only in the cache"
        def exitCode = runCommand(
                ["create", "test-tmpl", "0.2", "testapp", "-Pgroup=foo", "-Pversion=0.1", "-PmaxThreads=3"],
                baseWorkDir)

        then: "It unpacks the template, retaining file permissions"
        exitCode == 0

        def appDir = new File(baseWorkDir, "testapp")
        appDir.exists()
        new File(appDir, "gradlew").canExecute()
        new File(appDir, "src/main/groovy").isDirectory()
    }

    def "Create command displays usage when incorrect number of arguments are provided"() {
        when: "I run skeletor with the create command without an extra argument"
        def exitCode = runCommand(["create"], baseWorkDir)

        then: "It returns a non-zero exit code and displays an error message"
        exitCode == 1
        output =~ /Incorrect number of arguments/
        output =~ /USAGE:/
    }
}
