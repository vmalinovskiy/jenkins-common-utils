import main.groovy.lib.GitHubClient

def preparePullRequest(script, repositoryName, from, to, message, reviewerNames = null) {
    return gitHubClient(script).createPullRequest(repositoryName, from, to, message, reviewerNames)
}

def getPullRequestParamValue(script, repositoryName, pullRequestNumber, paramName) {
    return gitHubClient(script).getPullRequestParamValue(repositoryName, pullRequestNumber, paramName)
}

def mergePullRequest(script, repositoryName, pullRequestNumber, message) {
    gitHubClient(script).mergePullRequest(repositoryName, pullRequestNumber, message)
}

def createRelease(script, repositoryName, version) {
    gitHubClient(script).createRelease(repositoryName, version)
}

def createBranch(script, repositoryName, baseBranchSha, newBranchName) {
    gitHubClient(script).createBranch(repositoryName, baseBranchSha, newBranchName)
}

def getBranchSha(script, repositoryName, branchName) {
    return gitHubClient(script).getBranchSha(repositoryName, branchName)
}

def updateFileData(script, repositoryName, branchName, fileName, replaceTo, searchCriteria = null) {
    return gitHubClient(script).updateFileData(repositoryName, branchName, fileName, replaceTo, searchCriteria)
}

def createFile(script, repositoryName, content, branch, path, commitMessage) {
    gitHubClient(script).createFile(repositoryName, content, branch, path, commitMessage)
}

def dirSubItemPresent(script, repositoryName, path, branchName, subItemName = null) {
    return gitHubClient(script).dirSubItemPresent(repositoryName, path, branchName, subItemName)
}

def gitHubClient(script) {
    script.withCredentials([[$class: 'StringBinding', credentialsId: "${script.env.gitHubPermissionId}", variable: 'GITHUB_TOKEN']]) {
        return new GitHubClient(script, "${GITHUB_TOKEN}")
    }
}
