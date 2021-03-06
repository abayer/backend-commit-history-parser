package org.jenkinsci.backend.gitlogparser

import hudson.plugins.jira.soap.RemoteIssue

/**
 * Release changelog generator.
 *
 * @author Kohsuke Kawaguchi
 */
class ChangeLogGenerator extends App {
    def main(String[] args) {
        generateChangeLog(args[0],new File(args[1]));
    }

    /**
     * Generates a changelog fragment into the specified file.
     *
     * @param gitRevList
     *      git-rev-list option to specify a range of commits from which changes are discovered.
     * @param file
     *      The file to be created.
     */
    def generateChangeLog(gitRevList, Writer w) {
        def tickets = extractTickets(this.&parseGitLog.curry(gitRevList));
        def issues = retrieveJiraTicketDetails(tickets)

        issues.each { RemoteIssue i ->
            if (!i.key.startsWith("JENKINS-")
             && !i.key.startsWith("SECURITY-"))  return;   // ignore non-Jenkins tickets

            int n = Integer.parseInt(i.key.split("-")[1])

            def clazz = jiraType(i.type)=="bug" ? "bug" : "rfe";

            if (i.priority=="P1")   clazz = "major "+clazz;
            w << "  <li class='${clazz}'>\n"
            w << "    ${i.summary}\n"
            if (i.key.startsWith("JENKINS-"))
                w << "    (<a href='https://issues.jenkins-ci.org/browse/${i.key}'>issue ${n}</a>)\n"
            else
                w << "    (SECURITY-${n})\n"
        }
    }

    def generateChangeLog(gitRevList, File file) {
        file.withWriter("UTF-8") { w ->
            generateChangeLog(gitRevList,w)
        }
    }
}
