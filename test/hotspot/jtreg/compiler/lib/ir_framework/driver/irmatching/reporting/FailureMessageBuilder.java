package compiler.lib.ir_framework.driver.irmatching.reporting;

import compiler.lib.ir_framework.driver.irmatching.visitor.MatchResultAction;
import compiler.lib.ir_framework.driver.irmatching.TestClassResult;
import compiler.lib.ir_framework.driver.irmatching.irmethod.IRMethodMatchResult;
import compiler.lib.ir_framework.driver.irmatching.irmethod.NotCompiledResult;
import compiler.lib.ir_framework.driver.irmatching.irrule.IRRule;
import compiler.lib.ir_framework.driver.irmatching.irrule.IRRuleMatchResult;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.CheckAttributeMatchResult;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.CountsConstraintFailure;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.FailOnConstraintFailure;
import compiler.lib.ir_framework.driver.irmatching.irrule.phase.CompilePhaseMatchResult;
import compiler.lib.ir_framework.driver.irmatching.visitor.PreOrderMatchResultVisitor;

public class FailureMessageBuilder extends AbstractBuilder implements MatchResultAction, FailureMessage {
    private int indentation;

    public FailureMessageBuilder(TestClassResult testClassResult) {
        super(testClassResult);
    }

    @Override
    public void doAction(TestClassResult testClassResult) {
        FailCountVisitor failCountVisitor = new FailCountVisitor();
        testClassResult.acceptChildren(failCountVisitor);
        int failedMethodCount = failCountVisitor.getIrMethodCount();
        int failedIRRulesCount = failCountVisitor.getIrRuleCount();
        msg.append("One or more @IR rules failed:")
           .append(System.lineSeparator())
           .append(System.lineSeparator())
           .append("Failed IR Rules (").append(failedIRRulesCount).append(") of Methods (").append(failedMethodCount)
           .append(")").append(System.lineSeparator())
           .append(getTitleSeparator(failedMethodCount, failedIRRulesCount))
           .append(System.lineSeparator());
    }

    private static String getTitleSeparator(int failedMethodCount, int failedIRRulesCount) {
        return "-".repeat(32 + digitCount(failedIRRulesCount) + digitCount(failedMethodCount));
    }

    @Override
    public void doAction(IRMethodMatchResult irMethodMatchResult) {
        appendIRMethodHeader(irMethodMatchResult);
    }

    private void appendIRMethodHeader(IRMethodMatchResult irMethodMatchResult) {
        appendIRMethodPrefix();
        int reportedMethodCountDigitCount = digitCount(getMethodNumber());
        indentation = reportedMethodCountDigitCount + 2;
        msg.append("Method \"").append(irMethodMatchResult.getIRMethod().getMethod())
           .append("\" - [Failed IR rules: ").append(irMethodMatchResult.getFailedIRRuleCount()).append("]:")
           .append(System.lineSeparator());
    }

    @Override
    public void doAction(NotCompiledResult notCompiledResult) {
        appendIRMethodHeader(notCompiledResult);
        msg.append(getIndentation(indentation))
           .append("* Method was not compiled. Did you specify a @Run method in STANDALONE mode? In this case, make " +
                   "sure to always trigger a C2 compilation by invoking the test enough times.")
           .append(System.lineSeparator());
    }

    @Override
    public void doAction(IRRuleMatchResult irRuleMatchResult) {
        IRRule irRule = irRuleMatchResult.getIRRule();
        msg.append(getIndentation(indentation)).append("* @IR rule ").append(irRule.getRuleId()).append(": \"")
           .append(irRule.getIRAnno()).append("\"").append(System.lineSeparator());
    }

    @Override
    public void doAction(CompilePhaseMatchResult compilePhaseMatchResult) {
        msg.append(getIndentation(indentation + 2))
           .append("> Phase \"").append(compilePhaseMatchResult.getCompilePhase().getName()).append("\":")
           .append(System.lineSeparator());
        if (compilePhaseMatchResult.hasNoCompilationOutput()) {
            msg.append(buildNoCompilationOutputMessage());
        }
    }

    private String buildNoCompilationOutputMessage() {
        return getIndentation(indentation + 4) + "- NO compilation output found for this phase! Make sure this "
               + "phase is emitted or remove it from the list of compile phases in the @IR rule to match on."
               + System.lineSeparator();
    }

    @Override
    public void doAction(CheckAttributeMatchResult checkAttributeMatchResult) {
        String checkAttributeFailureMsg;
        switch (checkAttributeMatchResult.getCheckAttributeKind()) {
            case FAIL_ON -> checkAttributeFailureMsg = "failOn: Graph contains forbidden nodes";
            case COUNTS -> checkAttributeFailureMsg = "counts: Graph contains wrong number of nodes";
            default ->
                    throw new IllegalStateException("Unexpected value: " + checkAttributeMatchResult.getCheckAttributeKind());
        }
        msg.append(getIndentation(indentation + 4)).append("- ").append(checkAttributeFailureMsg)
           .append(":").append(System.lineSeparator());
    }

    @Override
    public void doAction(FailOnConstraintFailure failOnConstraintFailure) {
        msg.append(new FailOnConstraintFailureMessageBuilder(failOnConstraintFailure, indentation + 6).build());
    }

    @Override
    public void doAction(CountsConstraintFailure countsConstraintFailure) {
        msg.append(new CountsConstraintFailureMessageBuilder(countsConstraintFailure, indentation + 6).build());
    }

    @Override
    public String build() {
        PreOrderMatchResultVisitor visitor = new PreOrderMatchResultVisitor(this);
        visitResults(visitor);
        msg.append(System.lineSeparator())
           .append(">>> Check stdout for compilation output of the failed methods")
           .append(System.lineSeparator()).append(System.lineSeparator());
        return msg.toString();
    }
}
