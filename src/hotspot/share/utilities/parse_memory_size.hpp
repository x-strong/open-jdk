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
 *
 */

#ifndef SHARE_UTILITIES_PARSE_MEMORY_SIZE_HPP
#define SHARE_UTILITIES_PARSE_MEMORY_SIZE_HPP

#include "metaprogramming/enableIf.hpp"
#include "utilities/debug.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/parse_integer.hpp"

#include <errno.h>
#include <limits>
#include <stdlib.h>

template<typename T>
static bool multiply_by_1k(T& n) {
  if (n >= std::numeric_limits<T>::min() / 1024 &&
      n <= std::numeric_limits<T>::max() / 1024) {
    n *= 1024;
    return true;
  } else {
    return false;
  }
}

// All of the integral types that can be used for command line options:
//   int, uint, intx, uintx, uint64_t, size_t
//
// In all supported platforms, these types can be mapped to only 4 native types:
//    {signed, unsigned} x {32-bit, 64-bit}
//
// We use SFINAE to pick the correct parse_integer_impl() function
template<typename T>
static bool parse_integer(const char *s, T* result) {
  if (!isdigit(s[0]) && s[0] != '-') {
    // strtoll/strtoull may allow leading spaces. Forbid it.
    return false;
  }

  T n = 0;
  bool is_hex = (s[0] == '0' && (s[1] == 'x' || s[1] == 'X')) ||
                (s[0] == '-' && s[1] == '0' && (s[2] == 'x' || s[3] == 'X'));
  char* remainder;

  if (!parse_integer_impl(s, &remainder, (is_hex ? 16 : 10), &n)) {
    return false;
  }

  // Fail if no number was read at all or if the remainder contains more than a single non-digit character.
  if (remainder == s || strlen(remainder) > 1) {
    return false;
  }

  switch (*remainder) {
    case 'T': case 't':
      if (!multiply_by_1k(n)) return false;
      // fall-through
    case 'G': case 'g':
      if (!multiply_by_1k(n)) return false;
      // fall-through
    case 'M': case 'm':
      if (!multiply_by_1k(n)) return false;
      // fall-through
    case 'K': case 'k':
      if (!multiply_by_1k(n)) return false;
      break;
    case '\0':
      break;
    default:
      return false;
  }

  *result = n;
  return true;
}

#endif // SHARE_UTILITIES_PARSE_MEMORY_SIZE_HPP
