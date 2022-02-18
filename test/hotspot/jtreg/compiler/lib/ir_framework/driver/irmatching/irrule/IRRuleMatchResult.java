/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package compiler.lib.ir_framework.driver.irmatching.irrule;

import compiler.lib.ir_framework.driver.irmatching.MatchResult;
import compiler.lib.ir_framework.driver.irmatching.irrule.phase.CompilePhaseMatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an IR matching result of an IR rule.
 *
 * @see IRRule
 */
public class IRRuleMatchResult implements MatchResult {
    private final IRRule irRule;
    private final List<CompilePhaseMatchResult> compilePhaseMatchResults;

    public IRRuleMatchResult(IRRule irRule) {
        this.irRule = irRule;
        this.compilePhaseMatchResults = new ArrayList<>();
    }

    public List<CompilePhaseMatchResult> getCompilePhaseMatchResults() {
        return compilePhaseMatchResults;
    }

    public void addCompilePhaseMatchResult(CompilePhaseMatchResult result) {
        compilePhaseMatchResults.add(result);
    }

    @Override
    public boolean fail() {
        return !compilePhaseMatchResults.isEmpty();
    }

    /**
     * Build a failure message based on the collected failures of this object.
     */
    @Override
    public String buildFailureMessage(int indentationSize) {
        StringBuilder failMsg = new StringBuilder();
        failMsg.append(buildIRRuleHeader(indentationSize));
        for (CompilePhaseMatchResult phaseMatchResult : compilePhaseMatchResults) {
            if (phaseMatchResult.fail()) {
                failMsg.append(phaseMatchResult.buildFailureMessage(indentationSize + 2));
            }
        }
        return failMsg.toString();
    }

    private String buildIRRuleHeader(int indentation) {
        return getIndentation(indentation) + "* @IR rule " + irRule.getRuleId() + ": \"" + irRule.getIRAnno() + "\"" + System.lineSeparator();
    }
}
