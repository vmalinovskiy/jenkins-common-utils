@Grab(group='org.kohsuke', module='github-api', version='1.95')
import org.kohsuke.github.*

def preparePullRequest(script, repositoryName, from, to, message, reviewerNames = null) {
    def repository = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName))
    def pullRequest = repository.createPullRequest(message, from, to, message)
    script.echo "Created ${repository.name} pull request from ${from} to ${to}, number - ${pullRequest.number}"

    if (reviewerNames != null) {
        pullRequest.requestReviewers(reviewers(reviewerNames))
        script.echo "${repository.name} pull request from ${from} to ${to} reviewers added"
    }

    return pullRequest.number
}

def getPullRequestParamValue(script, repositoryName, pullRequestNumber, paramName) {
    script.echo "Get ${repositoryName} pull request (number ${pullRequestNumber}) to get property value..."
    def repository = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName))
    def pr = repository.getPullRequest(pullRequestNumber)
    script.echo "Get ${repositoryName} pull request (number ${pullRequestNumber})to get property value successful..."
    return pr."${paramName}"
}

def mergePullRequest(script, repositoryName, pullRequestNumber, message) {
    def repository = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName))
    boolean pullRequestMergeable = false
    while (!pullRequestMergeable) {
        def pr = repository.getPullRequest(pullRequestNumber)
        script.echo "${repository.name} pull request mergeable status is ${pr?.mergeable}"

        if (pr?.mergeable_state == "clean") {
            pr.merge(message, pr.head.sha)
            script.echo "${repository.name} pull request merged successfully"
            pullRequestMergeable = true
        } else if (!pr?.mergeable || pr?.mergeable_state == "blocked") {
            script.echo "${repository.name} pull request is waiting for reviews and additional check build finish..."
            script.sleep 60 //1 minute
        }
    }
}

def createRelease(script, repositoryName, name) {
    def repository = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName))
    script.echo "Creating ${repository.name} release tag after pull request merging into master..."
    GHRelease release = repository.createRelease(name)
            .name(name)
            .body(name)
            .commitish("master")
            .create()
    script.echo "${release.tagName} ${repository.name} release was created"
}

def createBranch(script, repositoryName, baseBranchSha, newBranchName) {
    script.echo "Creating ${repositoryName} branch based on SHA ${baseBranchSha}..."
    GHRef newBranch = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName)).createRef(newBranchName, baseBranchSha)
    script.echo "New ${repositoryName} branch created\n${newBranch?.object?.url}"
}

def getBranchSha(script, repositoryName, branchName) {
    script.echo "Get ${repositoryName} ${branchName} branch to get sha..."
    def branchSha = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName)).getRef(branchName)
    script.echo "${repositoryName} ${branchName} branch sha: ${branchSha?.object?.sha}"
    return branchSha?.object?.sha
}

def updateFileData(script, repositoryName, branchName, fileName, replaceTo, searchCriteria = null) {
    def repository = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName))
    script.echo "Getting ${fileName} file from ${repository.name} project to update..."
    GHContent fileData = repository.getFileContent(fileName, branchName)
    script.echo "${fileName} file data from ${repository.name} project successfully loaded..."
    def replacedFileData
    def replaceResult = new String(fileData.content)
    if (searchCriteria == null) {
        replacedFileData = replaceResult.replace(new String(fileData.content), replaceTo)
    } else {
        replacedFileData = replaceResult.replaceAll(searchCriteria, replaceTo)
    }
    script.echo "Updating ${fileName} file from ${repository.name} project with new content..."
    fileData.update(replacedFileData.bytes, "${repository.name}-${fileName} update", branchName)
    script.echo "${fileName} from ${repository.name} project successfully updated with new content..."
}

def createFile(script, repositoryName, content, branch, path, message) {
    script.echo "Creating file ${path} on ${repositoryName} repo, ${branch}..."
    gitHubClient(script).getRepository(getRepoSlug(script, repositoryName)).createContent()
            .content(content)
            .branch(branch)
            .path(path)
            .message(message)
            .commit()
    script.echo "File ${path} was created on ${repositoryName} repo, ${branch}..."
}

def dirSubItemPresent(script, repositoryName, path, branchName, subItemName = null) {
    List<GHContent> contentItem = gitHubClient(script).getRepository(getRepoSlug(script, repositoryName)).getDirectoryContent(path, branchName)
    script.echo "Sub item to search for is ${subItemName}"
    if (subItemName == null) {
        script.echo "Checking for directory items size..."
        return contentItem?.size() > 0 ? true : false
    }

    for(it in contentItem) {
        if (it.getName() == subItemName) {
            script.echo "${subItemName} was found..."
            return true
        }
    }
    script.echo "${subItemName} was found..."
    return false
}

def reviewers(names) {
    List<GHUser> reviewers = new ArrayList()
    names.split(',').each {
        GHUser user = new GHUser()
        user.login = "${it}"
        reviewers.add(user)
    }
    return reviewers
}

def gitHubClient(script) {
    script.withCredentials([[$class: 'StringBinding', credentialsId: "${script.env.gitHubPermissionId}", variable: 'GITHUB_TOKEN']]) {
        return GitHub.connectUsingOAuth("${GITHUB_TOKEN}")
    }
}

def getRepoSlug(script, projectName) {
    return "${script.env.gitHubRepoOwner}/${projectName}"
}
