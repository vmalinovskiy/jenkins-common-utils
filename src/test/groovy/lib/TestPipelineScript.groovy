package main.groovy.lib

class TestPipelineScript {

    def env = [
        gitHubRepoOwner: "testRepoOwner"
    ]

    def echo(source) {
        println(source)
    }
}
