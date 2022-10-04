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

package compiler.lib.ir_framework.driver.irmatching.irrule.phase;

import compiler.lib.ir_framework.CompilePhase;
import compiler.lib.ir_framework.IRNode;
import compiler.lib.ir_framework.driver.irmatching.MatchResult;
import compiler.lib.ir_framework.driver.irmatching.Matchable;
import compiler.lib.ir_framework.driver.irmatching.irrule.checkattribute.Counts;
import compiler.lib.ir_framework.driver.irmatching.irrule.checkattribute.FailOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This class represents an IR rule of an IR method for a specific compile phase. It contains fully parsed (i.e.
 * all placeholder strings of {@link IRNode} replaced and composite nodes merged) {@link FailOn} and {@link Counts}
 * attributes which are ready to be IR matched against.
 *
 * @see CompilePhaseNoCompilationIRRule
 */
public class CompilePhaseIRRule implements Matchable {
    private final CompilePhase compilePhase;
    private final List<Matchable> checkAttributes;

    public CompilePhaseIRRule(CompilePhase compilePhase, List<Matchable> checkAttributes) {
        this.compilePhase = compilePhase;
        this.checkAttributes = checkAttributes;
    }

    @Override
    public MatchResult match() {
        List<MatchResult> results = new ArrayList<>();
        for (Matchable checkAttribute : checkAttributes) {
            MatchResult matchResult = checkAttribute.match();
            if (matchResult.fail()) {
                results.add(matchResult);
            }
        }
        return new CompilePhaseIRRuleMatchResult(results, compilePhase);
    }

    /**
     * To remove duplicated phases created by CompilePhaseIRRuleBuilder.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompilePhaseIRRule that = (CompilePhaseIRRule)o;
        return compilePhase == that.compilePhase;
    }

    /**
     * To remove duplicated phases created by CompilePhaseIRRuleBuilder
     */
    @Override
    public int hashCode() {
        return Objects.hash(compilePhase);
    }
}
