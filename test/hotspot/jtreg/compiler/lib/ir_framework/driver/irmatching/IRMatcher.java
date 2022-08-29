/*
 * Copyright (c) 2021, 2022, Oracle and/or its affiliates. All rights reserved.
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

import compiler.lib.ir_framework.driver.irmatching.parser.MethodCompilationParser;
import compiler.lib.ir_framework.driver.irmatching.reporting.FailureMessageBuilder;

/**
 * This class parses the hotspot_pid* file of the test VM to match all applicable @IR rules afterwards.
 */
public class IRMatcher {
    public static final String SAFEPOINT_WHILE_PRINTING_MESSAGE = "<!-- safepoint while printing -->";
    private final TestClass testClass;

    public IRMatcher(String hotspotPidFileName, String irEncoding, Class<?> testClass) {
        System.out.println(irEncoding);
        MethodCompilationParser methodCompilationParser = new MethodCompilationParser(testClass);
        this.testClass = methodCompilationParser.parse(hotspotPidFileName, irEncoding);
    }

    public void match() {
        TestClassResult result = testClass.match();
        if (result.fail()) {
            reportFailures(result);
        }
    }

    /**
     * Do an IR matching of all methods with applicable @IR rules prepared with by the {@link MethodCompilationParser}.
     */



    /**
     * Report all IR violations in a pretty format to the user. Depending on the failed regex, we only report
     * PrintIdeal or PrintOptoAssembly if the match failed there. If there were failures that matched things
     * in both outputs then the entire output is reported. Throws IRViolationException from which the compilation
     * can be read and reported to the stdout separately. The exception message only includes the summary of the
     * failures.
     */
    private void reportFailures(TestClassResult result) { // TODO: Introduce TestClassMatchResult implements MatchResult
        String failureMsg = new FailureMessageBuilder(result).build();
//        String failureMsg = IRMatcherFailureMessageBuilder.build(result);
        throwIfNoSafepointWhilePrinting(failureMsg,
                                        CompilationOutputBuilder.build(result));
    }

    // In some very rare cases, the VM output to regex match on contains "<!-- safepoint while printing -->"
    // (emitted by ttyLocker::break_tty_for_safepoint) which might be the reason for a matching error.
    // Do not throw an exception in this case (i.e. bailout).
    private void throwIfNoSafepointWhilePrinting(String failures, String compilations) {
        if (!compilations.contains(SAFEPOINT_WHILE_PRINTING_MESSAGE)) {
            throw new IRViolationException(failures, compilations);
        } else {
            System.out.println("Found " + SAFEPOINT_WHILE_PRINTING_MESSAGE + ", bail out of IR matching");
        }
    }
}
