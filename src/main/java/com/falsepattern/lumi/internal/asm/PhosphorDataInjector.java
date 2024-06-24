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

import com.falsepattern.lib.turboasm.ClassNodeHandle;
import com.falsepattern.lib.turboasm.TurboClassTransformer;
import com.falsepattern.lumi.internal.Tags;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;

import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

import static com.falsepattern.lumi.internal.Lumi.createLogger;
import static com.falsepattern.lumi.internal.lighting.phosphor.PhosphorChunk.LIGHT_CHECK_FLAGS_LENGTH;
import static org.objectweb.asm.Type.*;

@NoArgsConstructor
public final class PhosphorDataInjector implements TurboClassTransformer {
    private static final Logger LOG = createLogger("Phosphor Data Injector");

    private static final HashMap<String, TriState> MEMOIZED_CLASSES = new HashMap<>(1024, 0.2f);

    @Override
    public String owner() {
        return Tags.MOD_NAME;
    }

    @Override
    public String name() {
        return "PhosphorDataInjector";
    }

    @Override
    public boolean shouldTransformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        return !className.startsWith("com.falsepattern.lumi");
    }

    @Override
    public boolean transformClass(@NotNull String className, @NotNull ClassNodeHandle classNode) {
        val cn = classNode.getNode();
        if (cn == null)
            return false;

        try {

            if (isValidTarget(cn, className.replace('.', '/')) != TriState.VALID)
                return false;

            implementInterface(cn);
            LOG.info("Injected Phosphor Data into: {}", className);
            return true;
        } catch (Throwable ignored) {
            LOG.warn("I'm so sorry");
        }
        return false;
    }

    @Contract("_,_->param2")
    private static TriState memoize(String className, TriState state) {
        MEMOIZED_CLASSES.put(className, state);
        return state;
    }

    private static TriState isValidTarget(ClassNode cn, String className) {
        {
            val access = cn.access;
            if ((access & Opcodes.ACC_INTERFACE) != 0 ||
                (access & Opcodes.ACC_ABSTRACT) != 0)
                return TriState.INVALID;
        }

        return isTarget(() -> new MiniMeta(cn.interfaces.toArray(new String[0]), cn.superName), className);
    }

    private enum TriState {
        VALID,
        INVALID,
        ALREADY_IMPLEMENTED
    }

    @AllArgsConstructor
    private static class MiniMeta {
        String[] interfaces;
        String superName;
    }

    private static @NotNull TriState isTarget(Supplier<MiniMeta> miniMeta, String className) {
        if (MEMOIZED_CLASSES.containsKey(className)) {
            return MEMOIZED_CLASSES.get(className);
        }
        TriState myState = TriState.INVALID;
        val mm = miniMeta.get();
        if (mm == null)
            return TriState.INVALID;
        val interfaces = mm.interfaces;
        loop:
        for (val interfaceName : interfaces) {
            if (myState != TriState.VALID && "com/falsepattern/lumi/api/chunk/LumiChunk".equals(interfaceName)) {
                myState = TriState.VALID;
                continue;
            }

            if ("com/falsepattern/lumi/internal/lighting/phosphor/PhosphorChunk".equals(interfaceName)) {
                myState = TriState.ALREADY_IMPLEMENTED;
                break;
            }


            val interfaceState = isTarget(() -> {
                try {
                    val in = Launch.classLoader.getResourceAsStream(interfaceName + ".class");
                    if (in != null) {
                        val cr = new ClassReader(IOUtils.toByteArray(in));
                        return new MiniMeta(cr.getInterfaces(), cr.getSuperName());
                    }
                } catch (IOException ignored) {
                }
                return null;
            }, interfaceName);
            switch (interfaceState) {
                case ALREADY_IMPLEMENTED: {
                    myState = TriState.ALREADY_IMPLEMENTED;
                    break loop;
                }
                case VALID: {
                    myState = TriState.VALID;
                    break;
                }
            }
        }
        if (myState == TriState.ALREADY_IMPLEMENTED)
            return memoize(className, myState);


        val superName = mm.superName;
        if (superName != null && !"java/lang/Object".equals(superName)) {
            val superState = isTarget(() -> {
                try {
                    val in = Launch.classLoader.getResourceAsStream(superName + ".class");
                    if (in != null) {
                        val cr = new ClassReader(IOUtils.toByteArray(in));
                        return new MiniMeta(cr.getInterfaces(), cr.getSuperName());
                    }
                } catch (IOException ignored) {
                }
                return null;
            }, superName);
            if (superState != TriState.INVALID) {
                myState = superState;
            }
        }

        return memoize(className, myState);
    }

    private static void implementInterface(ClassNode cn) {
        val fieldType = getType(short[].class);
        val fieldDesc = fieldType.getDescriptor();
        val getterDesc = getMethodDescriptor(fieldType);

        val fieldName = "phosphor$lightCheckFlags";
        val getterName = "phosphor$lightCheckFlags";

        val fieldAcc = Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
        val getterAcc = Opcodes.ACC_PUBLIC;

        val fieldInitSize = LIGHT_CHECK_FLAGS_LENGTH;

        val targetClass = cn.name;

        val injectedInterface = "com/falsepattern/lumi/internal/lighting/phosphor/PhosphorChunk";
        cn.interfaces.add(injectedInterface);
        cn.fields.add(new FieldNode(fieldAcc, fieldName, fieldDesc, null, null));
        for (val method: cn.methods) {
            if ("<init>".equals(method.name)) {
                val insnIter = method.instructions.iterator();
                while (insnIter.hasNext()) {
                    val insn = insnIter.next();
                    if (insn.getOpcode() == Opcodes.RETURN) {
                        insnIter.previous();
                        insnIter.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        insnIter.add(new IntInsnNode(Opcodes.BIPUSH, fieldInitSize));
                        insnIter.add(new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_SHORT));
                        insnIter.add(new FieldInsnNode(Opcodes.PUTFIELD, targetClass, fieldName, fieldDesc));
                        insnIter.next();
                    }
                }
            }
        }

        val getter = new MethodNode(getterAcc, getterName, getterDesc, null, null);
        cn.methods.add(getter);

        if (getter.visibleAnnotations == null) {
            getter.visibleAnnotations = new ArrayList<>(1);
        }
        getter.visibleAnnotations.add(new AnnotationNode(getDescriptor(Override.class)));

        getter.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
        getter.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, targetClass, fieldName, fieldDesc));
        getter.instructions.add(new InsnNode(Opcodes.ARETURN));
        getter.maxLocals = 1;
        getter.maxStack = 1;
    }
}
