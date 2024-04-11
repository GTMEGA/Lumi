/*
 * This file is part of LUMINA.
 *
 * LUMINA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LUMINA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with LUMINA. If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.lumina.internal.asm;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Supplier;

import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static com.falsepattern.lumina.internal.lighting.phosphor.PhosphorChunk.LIGHT_CHECK_FLAGS_LENGTH;
import static org.objectweb.asm.Type.*;

@NoArgsConstructor
public final class PhosphorDataInjector implements IClassTransformer {
    private static final Logger LOG = createLogger("Phosphor Data Injector");

    private static final HashMap<String, TriState> MEMOIZED_CLASSES = new HashMap<>(1024, 0.2f);

    @Override
    public byte @Nullable [] transform(String name, String transformedName, byte @Nullable [] classBytes) {
        if (name.startsWith("com.falsepattern.lumina"))
            return classBytes;
        if (classBytes == null)
            return null;
        try {
            val cr = new ClassReader(classBytes);
            if (isValidTarget(cr, transformedName.replace('.', '/')) != TriState.VALID)
                return classBytes;

            val transformedBytes = implementInterface(cr);
            LOG.info("Injected Phosphor Data into: {}", name);
            return transformedBytes;
        } catch (Throwable ignored) {
            LOG.warn("I'm so sorry");
        }
        return classBytes;
    }

    @Contract("_,_->param2")
    private static TriState memoize(String className, TriState state) {
        MEMOIZED_CLASSES.put(className, state);
        return state;
    }

    private static TriState isValidTarget(ClassReader cr, String className) {
        {
            val access = cr.getAccess();
            if ((access & Opcodes.ACC_INTERFACE) != 0 ||
                (access & Opcodes.ACC_ABSTRACT) != 0)
                return TriState.INVALID;
        }

        return isTarget(() -> cr, className);
    }

    private enum TriState {
        VALID,
        INVALID,
        ALREADY_IMPLEMENTED
    }

    private static @NotNull TriState isTarget(Supplier<ClassReader> scr, String className) {
        if (MEMOIZED_CLASSES.containsKey(className)) {
            return MEMOIZED_CLASSES.get(className);
        }
        TriState myState = TriState.INVALID;
        val cr = scr.get();
        if (cr == null)
            return TriState.INVALID;
        val interfaces = cr.getInterfaces();
        loop:
        for (val interfaceName : interfaces) {
            if (myState != TriState.VALID && "com/falsepattern/lumina/api/chunk/LumiChunk".equals(interfaceName)) {
                myState = TriState.VALID;
                continue;
            }

            if ("com/falsepattern/lumina/internal/lighting/phosphor/PhosphorChunk".equals(interfaceName)) {
                myState = TriState.ALREADY_IMPLEMENTED;
                break;
            }


            val interfaceState = isTarget(() -> {
                try {
                    val in = Launch.classLoader.getResourceAsStream(interfaceName + ".class");
                    if (in != null)
                        return new ClassReader(IOUtils.toByteArray(in));
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


        val superName = cr.getSuperName();
        if (superName != null && !"java/lang/Object".equals(superName)) {
            val superState = isTarget(() -> {
                try {
                    val in = Launch.classLoader.getResourceAsStream(superName + ".class");
                    if (in != null)
                        return new ClassReader(IOUtils.toByteArray(in));
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

    private static byte[] implementInterface(ClassReader cr) {
        val fieldType = getType(short[].class);
        val fieldDesc = fieldType.getDescriptor();
        val getterDesc = getMethodDescriptor(fieldType);

        val fieldName = "phosphor$lightCheckFlags";
        val getterName = "phosphor$lightCheckFlags";

        val fieldAcc = Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
        val getterAcc = Opcodes.ACC_PUBLIC;

        val fieldInitSize = LIGHT_CHECK_FLAGS_LENGTH;

        val cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        val injectedInterface = "com/falsepattern/lumina/internal/lighting/phosphor/PhosphorChunk";
        val targetClass = cr.getClassName();
        val cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public void visit(int version,
                              int access,
                              String name,
                              String signature,
                              String superName,
                              String[] interfaces) {
                // Here we add the interface to the existing interfaces.
                val newInterfaces = Arrays.copyOf(interfaces, interfaces.length + 1);
                newInterfaces[interfaces.length] = injectedInterface;
                super.visit(version, access, name, signature, superName, newInterfaces);

                // Add the field to the class
                val fv = cv.visitField(fieldAcc, fieldName, fieldDesc, null, null);
                if (fv != null)
                    fv.visitEnd();
            }

            @Override
            public MethodVisitor visitMethod(int access,
                                             String name,
                                             String descriptor,
                                             String signature,
                                             String[] exceptions) {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions);
                // Look for the constructor
                if ("<init>".equals(name)) {
                    return new GeneratorAdapter(Opcodes.ASM5, mv, access, name, descriptor) {
                        @Override
                        public void visitInsn(int opcode) {
                            // Look for the return
                            if (opcode == Opcodes.RETURN) {
                                // Init the field before each constructor return
                                this.visitVarInsn(Opcodes.ALOAD, 0);
                                this.visitIntInsn(Opcodes.BIPUSH, fieldInitSize);
                                this.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_SHORT);
                                this.visitFieldInsn(Opcodes.PUTFIELD, targetClass, fieldName, fieldDesc);
                            }
                            super.visitInsn(opcode);
                        }
                    };
                }
                return mv;
            }

            @Override
            public void visitEnd() {
                // Once at the end of the class, add the getter method
                val mv = cv.visitMethod(getterAcc,
                                        getterName,
                                        getterDesc,
                                        null,
                                        null);
                // Add the @Override annotation for clarity.
                val av = mv.visitAnnotation(getDescriptor(Override.class), true);
                if (av != null)
                    av.visitEnd();

                // Make the getter return our field
                mv.visitCode();
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, targetClass, fieldName, fieldDesc);
                mv.visitInsn(Opcodes.ARETURN);
                mv.visitMaxs(0, 0);
                mv.visitEnd();
                super.visitEnd();
            }
        };
        cr.accept(cv, 0);

        return cw.toByteArray();
    }
}
