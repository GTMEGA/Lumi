/*
 * Copyright (c) 2023 FalsePattern, Ven
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.lumina.internal.asm;

import lombok.NoArgsConstructor;
import lombok.val;
import lombok.var;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;

import static com.falsepattern.lumina.internal.LUMINA.createLogger;
import static com.falsepattern.lumina.internal.lighting.phosphor.PhosphorChunk.LIGHT_CHECK_FLAGS_LENGTH;
import static org.objectweb.asm.Type.*;

@NoArgsConstructor
public final class PhosphorDataInjector implements IClassTransformer {
    private static final Logger LOG = createLogger("Phosphor Data Injector");

    @Override
    public byte @Nullable [] transform(String name, String transformedName, byte @Nullable [] classBytes) {
        if (name.startsWith("com.falsepattern.lumina"))
            return classBytes;
        try {
            if (!isValidTarget(classBytes))
                return classBytes;

            val transformedBytes = implementInterface(classBytes);
            LOG.info("Injected Phosphor Data into: {}", name);
            return transformedBytes;
        } catch (Throwable ignored) {
            LOG.warn("I'm so sorry");
        }
        return classBytes;
    }

    private static boolean isValidTarget(byte @Nullable [] classBytes) {
        if (classBytes == null)
            return false;

        {
            val cr = new ClassReader(classBytes);
            val access = cr.getAccess();
            if ((access & Opcodes.ACC_INTERFACE) != 0 ||
                (access & Opcodes.ACC_ABSTRACT) != 0)
                return false;
        }

        val classStack = new ArrayDeque<byte[]>();
        classStack.push(classBytes);

        var isTarget = false;
        while (!classStack.isEmpty()) {
            val currentClassBytes = classStack.pop();
            val cr = new ClassReader(currentClassBytes);

            val interfaces = cr.getInterfaces();
            for (val interfaceName : interfaces) {
                if (!isTarget && "com/falsepattern/lumina/api/chunk/LumiChunk".equals(interfaceName)) {
                    isTarget = true;
                    continue;
                }

                if ("com/falsepattern/lumina/internal/lighting/phosphor/PhosphorChunk".equals(interfaceName))
                    return false;

                try {
                    val in = Launch.classLoader.getResourceAsStream(interfaceName + ".class");
                    if (in != null)
                        classStack.push(IOUtils.toByteArray(in));
                } catch (IOException ignored) {
                }
            }

            val superName = cr.getSuperName();
            if (superName != null && !"java/lang/Object".equals(superName)) {
                try {
                    val in = Launch.classLoader.getResourceAsStream(superName + ".class");
                    if (in != null)
                        classStack.push(IOUtils.toByteArray(in));
                } catch (IOException ignored) {
                }
            }
        }

        return isTarget;
    }

    private static byte[] implementInterface(byte[] classBytes) {
        val fieldType = getType(short[].class);
        val fieldDesc = fieldType.getDescriptor();
        val getterDesc = getMethodDescriptor(fieldType);

        val fieldName = "phosphor$lightCheckFlags";
        val getterName = "phosphor$lightCheckFlags";

        val fieldAcc = Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
        val getterAcc = Opcodes.ACC_PUBLIC;

        val fieldInitSize = LIGHT_CHECK_FLAGS_LENGTH;

        val cr = new ClassReader(classBytes);
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
