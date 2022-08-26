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

package compiler.lib.ir_framework.driver.irmatching;

import compiler.lib.ir_framework.driver.irmatching.irmethod.AbstractIRMethodMatchResult;

import java.util.List;

/**
 * Class to build the failure message output of IR matching failures.
 *
 * @see AbstractIRMethodMatchResult
 */
class IRMatcherFailureMessageBuilder {

    public static String build(List<AbstractIRMethodMatchResult> results) {
        StringBuilder failuresBuilder = new StringBuilder();
        failuresBuilder.append(buildHeaderMessage(results));
        int failureNumber = 1;
        for (AbstractIRMethodMatchResult irMethodResult : results) {
            if (irMethodResult.fail()) {
                failuresBuilder.append(buildIRMethodFailureMessage(failureNumber, irMethodResult));
                failureNumber++;
            }
        }
        failuresBuilder.append(buildFooterMessage());
        return failuresBuilder.toString();
    }

    private static String buildHeaderMessage(List<AbstractIRMethodMatchResult> results) {
        int failedIRRulesCount = getFailedIRRulesCount(results);
        long failedMethodCount = getFailedMethodCount(results);
        return "One or more @IR rules failed:" + System.lineSeparator() + System.lineSeparator()
               + "Failed IR Rules (" + failedIRRulesCount + ") of Methods (" + failedMethodCount + ")"
               + System.lineSeparator()
               +  "-".repeat(32 + digitCount(failedIRRulesCount) + digitCount(failedMethodCount))
               + System.lineSeparator();
    }

    private static int getFailedIRRulesCount(List<AbstractIRMethodMatchResult> results) {
        return results.stream().map(AbstractIRMethodMatchResult::getFailedIRRuleCount).reduce(0, Integer::sum);
    }

    private static long getFailedMethodCount(List<AbstractIRMethodMatchResult> results) {
        return results.stream().filter(AbstractIRMethodMatchResult::fail).count();
    }

    private static int digitCount(long digit) {
        return String.valueOf(digit).length();
    }

    private static String buildIRMethodFailureMessage(int failureNumber, AbstractIRMethodMatchResult result) {
        int failureNumberDigitCount = String.valueOf(failureNumber).length();
        // Format: "X) Method..." -> Initial indentation = digitsCount(X) + ) + " "
//        return failureNumber + ")" + result.buildFailureMessage(failureNumberDigitCount + 2) + System.lineSeparator();

        FailureMessageBuilder builder = new FailureMessageBuilder(failureNumberDigitCount + 2);
        result.accept(builder);
        return failureNumber + ")" + builder.build() + System.lineSeparator();
    }

    private static String buildFooterMessage() {
        return ">>> Check stdout for compilation output of the failed methods" + System.lineSeparator() + System.lineSeparator();
    }

}
