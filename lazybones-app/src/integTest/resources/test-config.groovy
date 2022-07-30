bintrayRepositories = ["pledbrook/lazybones-templates"]

templates {
    mappings {
        customRatpack = "http://dl.dropboxusercontent.com/u/29802534/custom-ratpack.zip"
        doesNotExist = "file:///does/not/exist"
        customAooAddin = "https://codebuilders.jfrog.io/artifactory/generic/skeletor-templates/aoo-addin-0.3.0.zip"
    }
}

test.option.override = "Just an option"
