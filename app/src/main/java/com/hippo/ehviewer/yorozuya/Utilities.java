/*
 * Copyright 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.ehviewer.yorozuya;

import androidx.annotation.Nullable;

public final class Utilities {
    /**
     * Whether the array contain the element
     *
     * @param array the array
     * @param ch    the element
     * @return true for the array contain the element
     */
    public static boolean contain(@Nullable char[] array, char ch) {
        if (null == array) {
            return false;
        }

        for (char c : array) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }
}
