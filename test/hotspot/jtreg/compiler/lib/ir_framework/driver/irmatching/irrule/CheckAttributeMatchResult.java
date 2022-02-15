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

import compiler.lib.ir_framework.IR;
import compiler.lib.ir_framework.driver.irmatching.MatchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class representing a result of an applied check attribute of an IR rule.
 *
 * @see IR
 */
abstract class CheckAttributeMatchResult implements MatchResult {
    protected List<RegexFailure> regexFailures = null;

    @Override
    public boolean fail() {
        return regexFailures != null;
    }

    public void addFailure(RegexFailure regexFailure) {
        if (regexFailures == null) {
            regexFailures = new ArrayList<>();
        }
        regexFailures.add(regexFailure);
    }

    public void mergeResults(CheckAttributeMatchResult other) {
        other.regexFailures.forEach(this::addFailure);
    }

    public int getMatchedNodesCount() {
        if (fail()) {
            return regexFailures.stream().map(RegexFailure::getMatchedNodesCount).reduce(0, Integer::sum);
        } else {
            return 0;
        }
    }

    protected String collectRegexFailureMessages() {
        StringBuilder failMsg = new StringBuilder();
        for (RegexFailure regexFailure : regexFailures) {
            failMsg.append(regexFailure.buildFailureMessage());
        }
        return failMsg.toString();
    }
}
