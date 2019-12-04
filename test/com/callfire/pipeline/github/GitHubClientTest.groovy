package com.callfire.pipeline.github

import com.callfire.pipeline.TestPipelineScript
import org.kohsuke.github.GHCommitPointer
import org.kohsuke.github.GHContent
import org.kohsuke.github.GHContentBuilder
import org.kohsuke.github.GHPullRequest
import org.kohsuke.github.GHRef
import org.kohsuke.github.GHRelease
import org.kohsuke.github.GHReleaseBuilder
import org.kohsuke.github.GHRepository
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHub
import spock.lang.Specification

import static java.util.Arrays.asList
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class GitHubClientTest extends Specification {

    GitHubClient gitHubClient;

    def repositoryName = "test"
    def develop = "develop"
    def master = "master"
    def message = "test message"
    def reviewers = "test1,test2"
    def branchName = "testName"
    def path = "path"
    def testName = "testName"
    def content = "content"
    def fileName = "testFile"
    def replaceToString = "replaceToString"

    GitHub client = Mock()
    GHRepository repository = Mock()
    GHPullRequest pullRequest = Mock()
    GHCommitPointer head = Mock()
    GHReleaseBuilder releaseBuilder = Mock()
    GHContentBuilder contentBuilder = GroovyMock()
    GHRelease release = Mock()
    GHRef branch = Mock()
    GHRef.GHObject object = Mock()
    GHContent itemContent = Mock()
    GHUser ghUser = Mock()
    List<GHContent> itemContentList = Mock()

    def setup(){
        GroovyMock(GitHub.class, global: true)
        GitHub.connectUsingOAuth(_) >> client
        gitHubClient = new GitHubClient(new TestPipelineScript(), 'gitHubPermissionTest')

        client.getRepository("testRepoOwner/test") >> repository
        repository.name >> testName
    }

    def "createPullRequest"() {
        given:
        pullRequest.number >> 1

        when:
        gitHubClient.createPullRequest(repositoryName, develop, master, message, reviewers)

        then:
        1 * repository.createPullRequest(message, develop, master, message) >> pullRequest
        1 * pullRequest.requestReviewers(*_) >> { arguments ->
            final List<GHUser> reviewers = arguments.get(0)
            assert 2 == reviewers.size()
        }
    }

    def "getPullRequestParamValue"() {
        given:
        pullRequest.number >> 1

        when:
        assert 1 == gitHubClient.getPullRequestParamValue(repositoryName, 1, "number")

        then:
        1 * repository.getPullRequest(1) >> pullRequest
    }

    def "mergePullRequest"() {
        given:
        pullRequest.number >> 1
        pullRequest.getMergeable() >> true
        pullRequest.getMergeableState() >> "clean"
        pullRequest.getHead() >> head
        head.getSha() >> "testSha"

        when:
        assert true == gitHubClient.mergePullRequest(repositoryName, 1, message)

        then:
        1 * repository.getPullRequest(1) >> pullRequest
        1 * pullRequest.merge(message, "testSha")
    }

    def "mergePullRequestAlreadyMerged"() {
        given:
        pullRequest.number >> 1
        pullRequest.getMergeable() >> false
        pullRequest.getMergeableState() >> "blocked"
        pullRequest.isMerged() >> true
        pullRequest.getMergedBy() >> ghUser
        ghUser.getLogin() >> "test"

        when:
        assert true == gitHubClient.mergePullRequest(repositoryName, 1, message)

        then:
        1 * repository.getPullRequest(1) >> pullRequest
        0 * pullRequest.merge(message, "testSha")
    }

    def "mergePullRequestExceedTimeout"() {
        given:
        pullRequest.number >> 1
        pullRequest.getMergeable() >> true
        pullRequest.getMergeableState() >> "blocked"
        pullRequest.getHead() >> head
        head.getSha() >> "testSha"

        when:
        assert false == gitHubClient.mergePullRequest(repositoryName, 1, message, 120)

        then:
        2 * repository.getPullRequest(1) >> pullRequest
        0 * pullRequest.merge(message, "testSha")
    }

    def "createRelease"() {
        String version = "testVersion"

        given:
        pullRequest.number >> 1
        release.tagName >> testName

        when:
        gitHubClient.createRelease(repositoryName, version)

        then:
        1 * repository.createRelease(version) >> releaseBuilder
        1 * releaseBuilder.name(version) >> releaseBuilder
        1 * releaseBuilder.body(version) >> releaseBuilder
        1 * releaseBuilder.commitish("master") >> releaseBuilder
        1 * releaseBuilder.create() >> release
        assert testName == release.tagName
    }

    def "getBranchSha"() {
        String sha = "testSha"

        given:
        branch.getObject() >> object
        object.getSha() >> sha

        when:
        assert sha == gitHubClient.getBranchSha(repositoryName, branchName)

        then:
        1 * repository.getRef(branchName) >> branch
    }

    def "updateFileDataFullReplace"() {
        given:
        itemContent.getContent() >> "replacementSource"

        when:
        gitHubClient.updateFileData(repositoryName, branchName, fileName, replaceToString)

        then:
        1 * repository.getFileContent(fileName, branchName) >> itemContent
        1 * itemContent.update(replaceToString, "testName-testFile update", branchName)
    }

    def "updateFileDataReplaceWithRegex"() {
        given:
        itemContent.getContent() >> "replacementSource"

        when:
        gitHubClient.updateFileData(repositoryName, branchName, fileName, replaceToString, /replacementSource/)

        then:
        1 * repository.getFileContent(fileName, branchName) >> itemContent
        1 * itemContent.update(replaceToString, "testName-testFile update", branchName)
    }

    def "createFile"() {
        when:
        gitHubClient.createFile(repositoryName, content, branchName, path, message)

        then:
        1 * repository.createContent() >> contentBuilder
        1 * contentBuilder.content(content) >> contentBuilder
        1 * contentBuilder.branch(branchName) >> contentBuilder
        1 * contentBuilder.path(path) >> contentBuilder
        1 * contentBuilder.message(message) >> contentBuilder
        1 * contentBuilder.commit()
    }

    def "dirSubItemPresentCheckSize"() {
        given:
        itemContentList.size() >> 1

        when:
        assertTrue(gitHubClient.dirSubItemPresent(repositoryName, path, branchName))

        then:
        1 * repository.getDirectoryContent(path, branchName) >> itemContentList
    }

    def "dirSubItemPresentFindByName"() {
        GHContent content = new GHContent()
        content.name = testName

        given:
        itemContentList.size() >> 1

        when:
        assertTrue(gitHubClient.dirSubItemPresent(repositoryName, path, branchName, testName))

        then:
        1 * repository.getDirectoryContent(path, branchName) >> asList(content)
    }

    def "dirSubItemPresentNotFound"() {
        GHContent content = new GHContent()
        content.name = testName

        given:
        itemContentList.size() >> 1

        when:
        assertFalse(gitHubClient.dirSubItemPresent(repositoryName, path, branchName, testName + "1"))

        then:
        1 * repository.getDirectoryContent(path, branchName) >> asList(content)
    }
}
