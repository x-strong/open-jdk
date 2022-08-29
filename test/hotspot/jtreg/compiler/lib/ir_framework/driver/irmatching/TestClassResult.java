package compiler.lib.ir_framework.driver.irmatching;

import compiler.lib.ir_framework.driver.irmatching.irmethod.IRMethodMatchResult;

import java.util.Set;
import java.util.TreeSet;

public class TestClassResult implements MatchResult {
    private final Set<IRMethodMatchResult> results = new TreeSet<>(); // Sort by method names

    @Override
    public boolean fail() {
        return !results.isEmpty();
    }

    public Set<IRMethodMatchResult> getResults() {
        return results;
    }

    public void addResult(IRMethodMatchResult IRMethodMatchResult) {
        this.results.add(IRMethodMatchResult);
    }

    @Override
    public void accept(MatchResultVisitor visitor) {
        visitor.visit(this);
        for (var result : results) {
            if (visitor.shouldVisit(result)) {
                result.accept(visitor);
            }
        }
    }
}
