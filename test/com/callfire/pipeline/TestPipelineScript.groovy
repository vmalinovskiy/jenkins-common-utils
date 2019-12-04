package com.callfire.pipeline

class TestPipelineScript {

    def env = [
        gitHubRepoOwner: "testRepoOwner"
    ]

    def echo(source) {
        println(source)
    }
}
