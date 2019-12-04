//this is here to be sure we have all required groovy dependecies downloaded during calling lib from pipeline
@Grab(group='org.kohsuke', module='github-api', version='1.95')
import org.kohsuke.github.*

import com.callfire.pipeline.github.GitHubClient

def createPullRequest(script, repositoryName, from, to, message, reviewerNames = null) {
    return gitHubClient(script).createPullRequest(repositoryName, from, to, message, reviewerNames)
}

def getPullRequestParamValue(script, repositoryName, pullRequestNumber, paramName) {
    return gitHubClient(script).getPullRequestParamValue(repositoryName, pullRequestNumber, paramName)
}

def mergePullRequest(script, repositoryName, pullRequestNumber, message, timeout = 1800) {
    gitHubClient(script).mergePullRequest(repositoryName, pullRequestNumber, message, timeout)
}

def mergePullRequestWithAdmin(script, repositoryName, pullRequestNumber, message) {
    adminGitHubClient(script).mergePullRequest(repositoryName, pullRequestNumber, message)
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

def adminGitHubClient(script) {
    script.withCredentials([[$class: 'StringBinding', credentialsId: "${script.env.gitHubAdminPermissionId}", variable: 'GITHUB_ADMIN_TOKEN']]) {
        return new GitHubClient(script, "${GITHUB_ADMIN_TOKEN}")
    }
}

