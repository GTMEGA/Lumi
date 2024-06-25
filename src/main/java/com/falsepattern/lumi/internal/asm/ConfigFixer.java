/*
 * Lumi
 *
 * Copyright (C) 2023-2024 FalsePattern, Ven
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.falsepattern.lumi.internal.asm;

import com.falsepattern.lumi.internal.Share;
import lombok.val;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConfigFixer {
    public static void fixConfigs() {
        CoreTweaksCompat.executeCoreTweaksConfigFixes();
        ArchaicFixCompat.executeArchaicFixConfigFixes();
    }

    public static class CoreTweaksCompat {
        public static void executeCoreTweaksConfigFixes() {
            val marker = new AtomicBoolean(false);
            ConfigFixUtil.fixConfig("coretweaks.cfg", (line) -> {
                if (marker.get()) {
                    if (line.contains("S:_enabled")) {
                        line = line.replace("true", "false");
                        marker.set(false);
                    }
                } else if (line.contains("fix_heightmap_range")) {
                    marker.set(true);
                }
                return line;
            }, e -> Share.LOG.fatal("Failed to apply CoreTweaks lumi compatibility patches!"));
        }
    }

    public static class ArchaicFixCompat {
        public static void executeArchaicFixConfigFixes() {
            ConfigFixUtil.fixConfig("archaicfix.cfg", (line) -> {
                if (line.contains("enablePhosphor")) {
                    line = line.replace("true", "false");
                }
                return line;
            }, e -> Share.LOG.fatal("Failed to apply ArchaicFix lumi compatibility patches!"));
        }
    }
}
