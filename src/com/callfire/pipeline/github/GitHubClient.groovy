package com.callfire.pipeline.github

import org.kohsuke.github.*

class GitHubClient implements Serializable {

    def script
    def token

    /**
     * Instantiate git hub client
     *
     * @param script Link to script object (like pipeline) to get/put data from/to
     * @param token Token to connect to git hub api
     */
    GitHubClient(script, token) {
        this.script = script
        this.token = token
    }

    /**
     * Create pull request
     *
     * @param repositoryName Repository name to create create pr on
     * @param branchFrom Branch name to create pr from
     * @param branchTo Branch name to create pr to
     * @param message Message to set as pr title and details
     * @param reviewerNames Reviewers to set for created pr (if null - no reviewers would be set for pr)
     *
     * @return Pull request number
     */
    def createPullRequest(repositoryName, branchFrom, branchTo, message, reviewerNames = null) {
        def repository = client().getRepository(getRepoSlug(repositoryName))
        def pullRequest = repository.createPullRequest(message, branchFrom, branchTo, message)
        script.echo "Created ${repository.name} pull request from ${branchFrom} to ${branchTo}, number - ${pullRequest.number}"

        if (reviewerNames != null) {
            pullRequest.requestReviewers(reviewers(reviewerNames))
            script.echo "${repository.name} pull request from ${branchFrom} to ${branchTo} reviewers added"
        }

        return pullRequest.number
    }

    /**
     * Get pull request parameter value by it's name
     *
     * @param repositoryName Repository name
     * @param pullRequestNumber Pull request number (not id)
     * @param paramName Parameter name to get value of
     *
     * @return Pull request parameter value
     */
    def getPullRequestParamValue(repositoryName, pullRequestNumber, paramName) {
        script.echo "Get ${repositoryName} pull request (number ${pullRequestNumber}) to get property value..."
        def repository = client().getRepository(getRepoSlug(repositoryName))
        def pr = repository.getPullRequest(pullRequestNumber)
        script.echo "Get ${repositoryName} pull request (number ${pullRequestNumber}) to get property value successful..."
        return pr."${paramName}"
    }

    /**
     * Merge pull request. Wait 60 secs after each pr status check.
     *
     * @param repositoryName Repository name to merge pr on
     * @param pullRequestNumber Pull request number (not id)
     * @param message Message to merge with
     * @param timeout timeout in seconds how long we'll wait for ability to merge (default is 30min)
     *
     * @return true if merged, false if timeout was exceeded
     */
    def mergePullRequest(repositoryName, pullRequestNumber, message, timeout = 1800) {
        while (timeout > 0) {
            script.echo "Waiting for a minute before next ${repositoryName} pull request status check, remaining time for merge - ${timeout}..."
            script.sleep 60 //1 minute
            def pr = client().getRepository(getRepoSlug(repositoryName)).getPullRequest(pullRequestNumber)
            script.echo "${repositoryName} pull request mergeable status is ${pr?.getMergeable()}"

            if (pr?.isMerged()) {
                script.echo "${repositoryName} pull request was merged manually by ${pr?.getMergedBy()?.getLogin()}"
                return true
            } else if (pr?.getMergeableState() == "clean") {
                pr.merge(message, pr.getHead().getSha())
                script.echo "${repositoryName} pull request merged successfully"
                return true
            } else if (!pr?.getMergeable() || pr?.getMergeableState() == "blocked") {
                script.echo "${repositoryName} pull request is waiting for reviews and additional build check finish..."
            }
            timeout -= 60
        }
        return false
    }

    /**
     * Create release tag based on master branch.
     *
     * @param repositoryName Repository name to create release tag on
     * @param version Tag version name (string)
     */
    def createRelease(repositoryName, version) {
        def repository = client().getRepository(getRepoSlug(repositoryName))
        script.echo "Creating ${repository.name} release tag after pull request merging into master..."
        GHRelease release = repository.createRelease(version)
                .name(version)
                .body(version)
                .commitish("master")
                .create()
        script.echo "${release.tagName} ${repository.name} release was created"
    }

    /**
     * Create branch based on specified ref.
     *
     * @param repositoryName Repository name to create branch on
     * @param baseBranchSha Branch sha to create based on
     * @param newBranchName Name of new branch
     */
    def createBranch(repositoryName, baseBranchSha, newBranchName) {
        script.echo "Creating ${repositoryName} branch based on SHA ${baseBranchSha}..."
        GHRef newBranch = client().getRepository(getRepoSlug(repositoryName)).createRef(newBranchName, baseBranchSha)
        script.echo "New ${repositoryName} branch created\n${newBranch?.getObject()?.getUrl()}"
    }

    /**
     * Get branch SHA.
     *
     * @param repositoryName Repository name
     * @param branchName Name of branch
     *
     * @return Branch sha
     */
    def getBranchSha(repositoryName, branchName) {
        script.echo "Get ${repositoryName} ${branchName} branch to get sha..."
        def branchSha = client().getRepository(getRepoSlug(repositoryName)).getRef(branchName)
        script.echo "${repositoryName} ${branchName} branch sha: ${branchSha?.getObject()?.getSha()}"
        return branchSha?.getObject()?.getSha()
    }

    /**
     * Update file data in repository.
     *
     * @param repositoryName Repository name to update file on
     * @param branchName Branch name
     * @param fileName File name to update
     * @param replaceTo String file part to update file with
     * @param searchCriteria Regex/string file part to change to (if null we replace file content fully)
     */
    def updateFileData(repositoryName, branchName, fileName, replaceTo, searchCriteria = null) {
        def repository = client().getRepository(getRepoSlug(repositoryName))
        script.echo "Getting ${fileName} file from ${repository.name} project to update..."
        GHContent fileData = repository.getFileContent(fileName, branchName)
        script.echo "${fileName} file data from ${repository.name} project successfully loaded..."
        def replacedFileData
        def replaceResult = new String(fileData.getContent())
        replacedFileData = searchCriteria == null ? replaceResult.replace(new String(fileData.getContent()), replaceTo) :
                replaceResult.replaceAll(searchCriteria, replaceTo)
        script.echo "Updating ${fileName} file from ${repository.name} project with new content..."
        fileData.update(replacedFileData, "${repository.name}-${fileName} update", branchName)
        script.echo "${fileName} from ${repository.name} project successfully updated with new content..."
    }

    /**
     * Create new file on branch.
     *
     * @param repositoryName Repository name to create file on
     * @param version Tag version name (string)
     */
    def createFile(repositoryName, content, branch, path, commitMessage) {
        script.echo "Creating file ${path} on ${repositoryName} repo, ${branch}..."
        client().getRepository(getRepoSlug(repositoryName)).createContent()
                .content(content)
                .branch(branch)
                .path(path)
                .message(commitMessage)
                .commit()
        script.echo "File ${path} was created on ${repositoryName} repo, ${branch}..."
    }

    /**
     * Check if sub directory or file is present under some directory.
     *
     * @param repositoryName Repository name
     * @param path Path to directory to look for sub item
     * @param branchName Branch name to look for dir/file
     * @param subItemName Dir/file name to search under some directory (if null - return true is elements count is directory > 0)
     *
     * @return Boolean value if element was found in search directory
     */
    def dirSubItemPresent(repositoryName, path, branchName, subItemName = null) {
        List<GHContent> contentItem = client().getRepository(getRepoSlug(repositoryName))
                .getDirectoryContent(path, branchName)
        script.echo "Sub item to search for is ${subItemName}"
        if (subItemName == null) {
            script.echo "Checking for directory items size..."
            return contentItem?.size() > 0 ? true : false
        }

        if (contentItem.find{it.getName() == subItemName}) {
            script.echo "${subItemName} was found..."
            return true
        }

        script.echo "${subItemName} was not found..."
        return false
    }

    /**
     * Get repository slug api client will work on.
     *
     * @param projectName Project name

     * @return String slag like "ownerName/projectName"
     */
    private def getRepoSlug(projectName) {
        return "${script.env.gitHubRepoOwner}/${projectName}"
    }

    /**
     * Create reviewer objects list from comma separated names.
     *
     * @param names Comma separated string with reviewers names

     * @return List of GHUser objects
     */
    private def reviewers(names) {
        return names.split(',').collect { new GHUser(login:it) }
    }

    private client() {
        return GitHub.connectUsingOAuth(token)
    }
}
