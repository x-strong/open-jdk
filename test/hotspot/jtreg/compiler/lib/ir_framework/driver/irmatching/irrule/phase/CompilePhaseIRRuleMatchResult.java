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
import compiler.lib.ir_framework.driver.irmatching.MatchResult;
import compiler.lib.ir_framework.driver.irmatching.visitor.AcceptChildren;
import compiler.lib.ir_framework.driver.irmatching.visitor.MatchResultVisitor;

import java.util.List;

/**
 * This class represents an IR matching result of a {@link CompilePhaseIRRule} (i.e. an IR rule applied on a compile phase).
 *
 * @see CompilePhaseIRRule
 */
public class CompilePhaseIRRuleMatchResult implements MatchResult {
    private final AcceptChildren acceptChildren;
    private final boolean failed;
    private final CompilePhase compilePhase;

    public CompilePhaseIRRuleMatchResult(CompilePhase compilePhase, List<MatchResult> matchResults) {
        this.acceptChildren = new AcceptChildren(matchResults);
        this.failed = !matchResults.isEmpty();
        this.compilePhase = compilePhase;
    }

    @Override
    public boolean fail() {
        return failed;
    }

    @Override
    public void accept(MatchResultVisitor visitor) {
        visitor.visitCompilePhaseIRRule(acceptChildren, compilePhase);
    }
}
