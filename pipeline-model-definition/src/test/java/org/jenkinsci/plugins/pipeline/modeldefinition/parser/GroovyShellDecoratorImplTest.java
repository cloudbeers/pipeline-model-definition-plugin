package org.jenkinsci.plugins.pipeline.modeldefinition.parser;

import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * @author Kohsuke Kawaguchi
 */
public class GroovyShellDecoratorImplTest {
    @Rule
    public final JenkinsRule j = new JenkinsRule();

    /**
     * Not a syntax error but semantics error in using the 'pipeline' step.
     * This should be reported nicely.
     */
    @Test
    public void errorInJenkinsfile() throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class);
        // still a syntactically valid groovy code but no stage name
        job.setDefinition(new CpsFlowDefinition("pipeline { stages { stage { sh './test.sh' } } }"));
        WorkflowRun b = j.assertBuildStatus(Result.FAILURE, job.scheduleBuild2(0).get());

        j.assertLogContains(
                "WorkflowScript: 1: Expected string literal @ line 1, column 27.\n" +
                "   pipeline { stages { stage { sh './test.sh' } } }\n" +
                "                             ^\n"
                ,b);
    }

    /**
     * V1 files shouldn't be affected
     */
    @Test
    public void v1() throws Exception {
        WorkflowJob job = j.createProject(WorkflowJob.class);
        // still a syntactically valid groovy code but no stage name
        job.setDefinition(new CpsFlowDefinition("node { echo 'hello' }"));
        j.assertBuildStatusSuccess(job.scheduleBuild2(0));
    }
}