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

import compiler.lib.ir_framework.CompilePhase;
import compiler.lib.ir_framework.IR;
import compiler.lib.ir_framework.driver.irmatching.irmethod.IRMethod;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.parser.CountsAttributeParser;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.parser.FailOnAttributeParser;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.parser.RawConstraint;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.parser.RawCountsConstraint;

import java.util.ArrayList;
import java.util.List;

public class IRRule {
    private final IRMethod irMethod;
    private final int ruleId;
    private final IR irAnno;
    private final List<CompilePhaseIRRule> compilePhaseIRRules;

    public IRRule(IRMethod irMethod, int ruleId, IR irAnno) {
        this.irMethod = irMethod;
        this.ruleId = ruleId;
        this.irAnno = irAnno;
        List<RawConstraint> failOnRawConstraints = initFailOnRegexes(irAnno.failOn());
        List<RawCountsConstraint> countsNodeRegexes = initCountsRegexes(irAnno.counts());
        this.compilePhaseIRRules = initPhaseIRRules(failOnRawConstraints, countsNodeRegexes, irAnno.phase());
    }

    private List<RawConstraint> initFailOnRegexes(String[] rawFailOn) {
        if (rawFailOn != null) {
            return FailOnAttributeParser.parse(rawFailOn);
        } else {
            return null;
        }
    }

    private List<RawCountsConstraint> initCountsRegexes(String[] rawCounts) {
        if (rawCounts != null) {
            return CountsAttributeParser.parse(rawCounts);
        }
        return null;
    }

    private List<CompilePhaseIRRule> initPhaseIRRules(List<RawConstraint> failOnRawConstraints,
                                                      List<RawCountsConstraint> countsNodeRegexes,
                                                      CompilePhase[] compilePhases) {
        List<CompilePhaseIRRule> compilePhaseIRRules = new ArrayList<>();
        for (CompilePhase compilePhase : compilePhases) {
            List<CompilePhaseIRRule> rulesList = CompilePhaseIRRuleBuilder.create(failOnRawConstraints, countsNodeRegexes,
                                                                                  compilePhase, irMethod);
            compilePhaseIRRules.addAll(rulesList);
        }
        return compilePhaseIRRules;
    }

    public int getRuleId() {
        return ruleId;
    }

    public IR getIRAnno() {
        return irAnno;
    }

    /**
     * Apply this IR rule by checking any failOn and counts attributes.
     */
    public IRRuleMatchResult applyCheckAttributesForPhases() {
        IRRuleMatchResult irRuleMatchResult = new IRRuleMatchResult(this);
        for (CompilePhaseIRRule compilePhaseIRRule : compilePhaseIRRules) {
            CompilePhaseMatchResult compilePhaseMatchResult = compilePhaseIRRule.applyCheckAttributes();
            if (compilePhaseMatchResult.fail()) {
                irRuleMatchResult.addCompilePhaseMatchResult(compilePhaseMatchResult);
            }
        }
        return irRuleMatchResult;
    }
}
