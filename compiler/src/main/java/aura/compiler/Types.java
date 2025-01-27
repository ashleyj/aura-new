/*
 * Copyright (C) 2012 RoboVM AB
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/gpl-2.0.html>.
 */
package aura.compiler;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import aura.compiler.config.Arch;
import aura.compiler.config.OS;
import aura.compiler.llvm.AggregateType;
import aura.compiler.llvm.Bitcast;
import aura.compiler.llvm.Constant;
import aura.compiler.llvm.ConstantGetelementptr;
import aura.compiler.llvm.ConstantPtrtoint;
import aura.compiler.llvm.Function;
import aura.compiler.llvm.FunctionType;
import aura.compiler.llvm.Getelementptr;
import aura.compiler.llvm.IntegerType;
import aura.compiler.llvm.NullConstant;
import aura.compiler.llvm.OpaqueType;
import aura.compiler.llvm.PackedStructureType;
import aura.compiler.llvm.PointerType;
import aura.compiler.llvm.StructureType;
import aura.compiler.llvm.Type;
import aura.compiler.llvm.Value;
import aura.compiler.llvm.Variable;

import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.LongType;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.VoidType;
import soot.jimple.toolkits.typing.fast.BottomType;

/**
 * @author niklas
 *
 */
public class Types {

    public static final StructureType GATEWAY_FRAME = new StructureType("GatewayFrame", Type.I8_PTR, Type.I8_PTR, Type.I8_PTR);
    public static final Type GATEWAY_FRAME_PTR = new PointerType(GATEWAY_FRAME);
    // Dummy TrycatchContext type definition. The real one is in header-<os>-<arch>.ll
    public static final StructureType TRYCATCH_CONTEXT = new StructureType("TrycatchContext", Type.I8_PTR);
    public static final Type TRYCATCH_CONTEXT_PTR = new PointerType(TRYCATCH_CONTEXT);
    public static final StructureType BC_TRYCATCH_CONTEXT = new StructureType("BcTrycatchContext", TRYCATCH_CONTEXT, Type.I8_PTR);
    public static final Type BC_TRYCATCH_CONTEXT_PTR = new PointerType(BC_TRYCATCH_CONTEXT);
    public static final Type ENV_PTR = new PointerType(new StructureType("Env", Type.I8_PTR, Type.I8_PTR, Type.I8_PTR,
            Type.I8_PTR, Type.I8_PTR, Type.I8_PTR, Type.I8_PTR, Type.I8_PTR, Type.I32));
    // Dummy Class type definition. The real one is in header.ll
    public static final StructureType CLASS = new StructureType("Class", Type.I8_PTR);
    public static final Type CLASS_PTR = new PointerType(CLASS);
    // Dummy Object type definition. The real one is in header.ll
    public static final StructureType OBJECT = new StructureType("Object", CLASS_PTR);
    // Dummy DataObject type definition. The real one is in header.ll
    public static final StructureType DATA_OBJECT = new StructureType("DataObject", OBJECT);
    // Dummy VITable type definition. The real one is in header.ll
    public static final StructureType VITABLE = new StructureType("VITable", Type.I8_PTR);
    public static final Type VITABLE_PTR = new PointerType(VITABLE);
    
    public static final Type OBJECT_PTR = new PointerType(OBJECT);
    public static final Type METHOD_PTR = new PointerType(new OpaqueType("Method"));
    public static final Type FIELD_PTR = new PointerType(new OpaqueType("Field"));
    
    public static Type getType(String desc) {
        switch (desc.charAt(0)) {
        case 'Z': return Type.I8;
        case 'B': return Type.I8;
        case 'S': return Type.I16;
        case 'C': return Type.I16;
        case 'I': return Type.I32;
        case 'J': return Type.I64;
        case 'F': return Type.FLOAT;
        case 'D': return Type.DOUBLE;
        case 'V': return Type.VOID;
        case 'L': return OBJECT_PTR;
        case '[': return OBJECT_PTR;
        }
        throw new IllegalArgumentException();
    }
    
    private static Type getType(CharBuffer desc) {
        switch (desc.get()) {
        case 'Z': return Type.I8;
        case 'B': return Type.I8;
        case 'S': return Type.I16;
        case 'C': return Type.I16;
        case 'I': return Type.I32;
        case 'J': return Type.I64;
        case 'F': return Type.FLOAT;
        case 'D': return Type.DOUBLE;
        case 'V': return Type.VOID;
        case 'L':
            while (desc.get() != ';')
                ;
            return OBJECT_PTR;
        case '[':
            getType(desc);
            return OBJECT_PTR;
        }
        throw new IllegalArgumentException();
    }
    
    public static Type getLocalType(soot.Type sootType) {
        Type t = getType(sootType);
        if (t instanceof IntegerType && ((IntegerType) t).getBits() < 32) {
            return Type.I32;
        }
        return t;
    }
    
    public static Type getType(soot.Type sootType) {
        if (sootType.equals(soot.BooleanType.v())) {
            return Type.I8;
        } else if (sootType.equals(soot.ByteType.v())) {
            return Type.I8;
        } else if (sootType.equals(soot.ShortType.v())) {
            return Type.I16;
        } else if (sootType.equals(soot.CharType.v())) {
            return Type.I16;
        } else if (sootType.equals(soot.IntType.v())) {
            return Type.I32;
        } else if (sootType.equals(soot.LongType.v())) {
            return Type.I64;
        } else if (sootType.equals(soot.FloatType.v())) {
            return Type.FLOAT;
        } else if (sootType.equals(soot.DoubleType.v())) {
            return Type.DOUBLE;
        } else if (sootType.equals(soot.VoidType.v())) {
            return Type.VOID;
        } else if (sootType instanceof soot.RefLikeType || sootType.equals(BottomType.v())) {
            return OBJECT_PTR;
        } else {
            throw new IllegalArgumentException("Unknown Type: " + sootType);
        }
    }
    
    public static boolean isUnsigned(soot.Type type) {
        return type.equals(CharType.v());
    }

    public static String getInternalName(soot.Type t) {
        if (t instanceof soot.ArrayType) {
            return getDescriptor(t);
        } else if (t instanceof soot.RefType) {
            RefType rt = (RefType) t;
            return rt.getClassName().replace('.', '/');
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public static String getInternalName(SootClass sc) {
        return sc.getName().replace('.', '/');
    }
    
    public static String getInternalNameFromDescriptor(String desc) {
        if (!desc.startsWith("L")) {
            throw new IllegalArgumentException(desc + " is not a class descriptor");
        }
        return desc.substring(1, desc.length() - 1);
    }
    
    public static String getDescriptor(Type t) {
        if (t instanceof PointerType) {
            return "J";
        }
        if (t == Type.I8) {
            return "B";
        }
        if (t == Type.I16) {
            return "S";
        }
        if (t == Type.I32) {
            return "I";
        }
        if (t == Type.I64) {
            return "J";
        }
        if (t == Type.FLOAT) {
            return "F";
        }
        if (t == Type.DOUBLE) {
            return "D";
        }
        throw new IllegalArgumentException(t.toString());
    }
    
    public static String getDescriptor(soot.Type t) {
        if (t instanceof PrimType) {
            if (t.equals(BooleanType.v())) {
                return "Z";
            } else if (t.equals(ByteType.v())) {
                return "B";
            } else if (t.equals(ShortType.v())) {
                return "S";
            } else if (t.equals(CharType.v())) {
                return "C";
            } else if (t.equals(IntType.v())) {
                return "I";
            } else if (t.equals(LongType.v())) {
                return "J";
            } else if (t.equals(FloatType.v())) {
                return "F";
            } else {
                // DoubleType
                return "D";
            }
        } else if (t.equals(VoidType.v())) {
            return "V";
        } else if (t instanceof soot.ArrayType) {
            soot.ArrayType at = (soot.ArrayType) t;
            return "[" + getDescriptor(at.getElementType());
        } else {
            // RefType
            RefType rt = (RefType) t;
            return "L" + rt.getClassName().replace('.', '/') + ";";
        }
    }
    
    public static String getDescriptor(SootField field) {
        return getDescriptor(field.getType());
    }
    
    public static String getDescriptor(SootMethod method) {
        return getDescriptor(method.makeRef());
    }
    
    @SuppressWarnings("unchecked")
    public static String getDescriptor(SootMethodRef methodRef) {
        return getDescriptor(methodRef.parameterTypes(), methodRef.returnType());
    }
    
    public static String getDescriptor(List<soot.Type> paramTypes, soot.Type returnType) {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        for (soot.Type t : paramTypes) {
            sb.append(getDescriptor(t));
        }
        sb.append(')');
        sb.append(getDescriptor(returnType));
        return sb.toString();
    }
    
    private static void nextDescriptor(CharBuffer cb, StringBuilder sb) {
        char c = cb.get();
        sb.append(c);
        if (c == 'L') {
            do {
                c = cb.get();
                sb.append(c);
            } while (c != ';');
        } else if (c == '[') {
            nextDescriptor(cb, sb);
        } else {
            // Must be a primitive
        }
    }
    public static String getReturnTypeDescriptor(String methodDescriptor) {
        return methodDescriptor.substring(methodDescriptor.indexOf(')') + 1);
    }
    public static List<String> getParameterDescriptors(String methodDescriptor) {
        return getParameterDescriptors(CharBuffer.wrap(methodDescriptor));
    }
    private static List<String> getParameterDescriptors(CharBuffer cb) {
        List<String> result = new ArrayList<String>();
        cb.get(); // Skip the initial '('
        while (cb.hasRemaining() && cb.get(cb.position()) != ')') {
            StringBuilder sb = new StringBuilder();
            nextDescriptor(cb, sb);
            result.add(sb.toString());
        }
        return result;
    }
    
    public static boolean isEnum(soot.Type t) {
        if (t instanceof RefType) {
            return isEnum(((RefType) t).getSootClass());
        }
        return false;
    }
    
    public static boolean isEnum(SootClass sc) {
        return sc.hasSuperclass() && sc.getSuperclass().getName().equals("java.lang.Enum");
    }
    
    public static boolean isPrimitive(String descriptor) {
        return descriptor.length() == 1;
    }

    public static boolean isArray(String descriptor) {
        return descriptor.charAt(0) == '[';
    }
    
    public static boolean isNativeObject(soot.Type t) {
        if (t instanceof RefType) {
            return isNativeObject(((RefType) t).getSootClass());
        }
        return false;
    }
    
    public static boolean isNativeObject(SootClass sc) {
        return isSubclass(sc, "NativeObject");
    }
    
    public static boolean isStruct(soot.Type t) {
        if (t instanceof RefType) {
            return isStruct(((RefType) t).getSootClass());
        }
        return false;
    }
    
    public static boolean isStruct(SootClass sc) {
        return isSubclass(sc, "Struct");
    }
    
    public static boolean isPrimitiveComponentType(String descriptor) {
        if (!isArray(descriptor)) {
            throw new IllegalArgumentException("Not an array");
        }
        return descriptor.length() == 2;
    }
    
    public static boolean isPrimitiveBaseType(String descriptor) {
        if (!isArray(descriptor)) {
            throw new IllegalArgumentException("Not an array");
        }
        return descriptor.charAt(descriptor.length() - 1) != ';';
    }
    
    /**
     * Returns the internal name of the base type for an array of references 
     * ([[Ljava/lang/String; => java/lang/String) or plain reference 
     * (Ljava/lang/String => java/lang/String).
     */
    public static String getBaseType(String descriptor) {
        if (!isArray(descriptor) || descriptor.charAt(descriptor.length() - 1) != ';') {
            throw new IllegalArgumentException("Not an array or base type is primitive");
        }
        int start = descriptor.lastIndexOf('[') + 1;
        if (descriptor.charAt(start) != 'L') {
            throw new IllegalArgumentException("Invalid descriptor: " + descriptor);
        }
        return descriptor.substring(start + 1, descriptor.length() - 1);
    }
    
    public static FunctionType getFunctionType(SootMethod method) {
        return getFunctionType(method.makeRef());
    }
    
    @SuppressWarnings("unchecked")
    public static FunctionType getFunctionType(SootMethodRef methodRef) {
        Type returnType = getType(methodRef.returnType());
        Type[] paramTypes = new Type[(methodRef.isStatic() ? 1 : 2) + methodRef.parameterTypes().size()];
        int i = 0;
        paramTypes[i++] = ENV_PTR;
        if (!methodRef.isStatic()) {
            paramTypes[i++] = OBJECT_PTR;
        }
        for (soot.Type t : (List<soot.Type>) methodRef.parameterTypes()) {
            paramTypes[i++] = getType(t);
        }
        return new FunctionType(returnType, paramTypes);
    }

    public static FunctionType getFunctionType(String methodDesc, boolean ztatic) {
        return getFunctionType(methodDesc, ztatic, false);
    }
    
    public static FunctionType getNativeFunctionType(String methodDesc, boolean ztatic) {
        return getFunctionType(methodDesc, ztatic, true);
    }
    
    private static FunctionType getFunctionType(String methodDesc, boolean ztatic, boolean nativ) {
        List<Type> paramTypes = new ArrayList<Type>();
        paramTypes.add(ENV_PTR);
        if (!ztatic) {
            paramTypes.add(OBJECT_PTR);
        } else if (nativ) {
            paramTypes.add(OBJECT_PTR);
        }
        CharBuffer buffer = CharBuffer.wrap(methodDesc);
        buffer.get(); // Skip initial (
        while (buffer.get(buffer.position()) != ')') {
            paramTypes.add(getType(buffer));
        }
        buffer.get(); // Skip ending )
        Type returnType = getType(buffer);
        return new FunctionType(returnType, paramTypes.toArray(new Type[paramTypes.size()]));
    }
    
    public static boolean isSubclass(SootClass sc, String className) {
        SootClass clazz = sc;
        while (clazz.hasSuperclass()) {
            clazz = clazz.getSuperclass();
            if (className.equals(clazz.getName())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isInstanceOfClass(soot.Type t, String className) {
        if (t instanceof RefType) {
            return isInstanceOfClass(((RefType) t).getSootClass(), className);
        }
        return false;
    }
    
    public static boolean isInstanceOfClass(SootClass sc, String className) {
        SootClass clazz = sc;
        if (className.equals(clazz.getName())) {
            return true;
        }
        return isSubclass(sc, className);
    }
    
    public static Constant sizeof(AggregateType type) {
        return new ConstantPtrtoint(
                new ConstantGetelementptr(new NullConstant(
                        new PointerType(type)), 1), Type.I32);
    }
    
    public static Constant offsetof(AggregateType type, int ... idx) {
        int[] i = new int[idx.length + 1];
        i[0] = 0;
        System.arraycopy(idx, 0, i, 1, idx.length);
        return new ConstantPtrtoint(
                new ConstantGetelementptr(new NullConstant(
                        new PointerType(type)), i), Type.I32);
    }
    
    private static PackedStructureType padType(Type t, int padding) {
        // t = i64, padding = 3 => {{i8, i8, i8}, i64}
        // t = i8, padding = 0 => {{}, i8}
        List<Type> paddingType = new ArrayList<Type>();
        for (int i = 0; i < padding; i++) {
            paddingType.add(Type.I8);
        }
        return new PackedStructureType(new PackedStructureType(paddingType.toArray(new Type[paddingType.size()])), t);
    }

    private static PackedStructureType getInstanceType0(OS os, Arch arch, SootClass clazz, int subClassAlignment, int[] superSize) {
        List<Type> types = new ArrayList<Type>();
        List<SootField> fields = getInstanceFields(os, arch, clazz);
        int superAlignment = 1;
        if (!fields.isEmpty()) {
            // Pad the super type so that the first field is aligned properly
            SootField field = fields.get(0);
            superAlignment = getFieldAlignment(os, arch, field);
        }
        if (clazz.hasSuperclass()) {
            types.add(getInstanceType0(os, arch, clazz.getSuperclass(), superAlignment, superSize));
        }
        int offset = superSize[0];
        for (SootField field : fields) {
            int falign = getFieldAlignment(os, arch, field);
            int padding = (offset & (falign - 1)) != 0 ? (falign - (offset & (falign - 1))) : 0;
            types.add(padType(getType(field.getType()), padding));
            offset += padding + getFieldSize(arch, field);
        }
        
        int padding = (offset & (subClassAlignment - 1)) != 0 
                ? (subClassAlignment - (offset & (subClassAlignment - 1))) : 0;
        for (int i = 0; i < padding; i++) {
            types.add(Type.I8);
            offset++;
        }
        
        superSize[0] = offset;
        
        return new PackedStructureType(types.toArray(new Type[types.size()]));
    }
    
    public static StructureType getClassType(OS os, Arch arch, SootClass clazz) {
        List<Type> types = new ArrayList<Type>();
        int offset = 0;
        for (SootField field : getClassFields(os, arch, clazz)) {
            int falign = getFieldAlignment(os, arch, field);
            int padding = (offset & (falign - 1)) != 0 ? (falign - (offset & (falign - 1))) : 0;
            types.add(padType(getType(field.getType()), padding));
            offset += padding + getFieldSize(arch, field);
        }
        return new StructureType(CLASS, new StructureType(types.toArray(new Type[types.size()])));
    }

    public static StructureType getInstanceType(OS os, Arch arch, SootClass clazz) {
        return new StructureType(DATA_OBJECT, getInstanceType0(os, arch, clazz, 1, new int[] {0}));
    }
    
    public static int getFieldAlignment(OS os, Arch arch, SootField f) {
        soot.Type t = f.getType();
        if (arch.is32Bit() && arch.isArm()) {
            if (LongType.v().equals(t)) {
                /*
                 * On ARM 32-bit volatile longs must be 8 byte aligned. Also,
                 * the Java Memory Model mandates that final instance fields are
                 * written to using volatile semantics. So we need to return 8
                 * here for volatile long static/instance fields and final long
                 * instance fields.
                 *
                 * But due to sun.misc.Unsafe's getLongVolatile() and
                 * putLongVolatile(), which can be used for
                 * non-volatile/non-final long fields (e.g. RxJava does this,
                 * see #987), we always make sure long fields are 8 byte
                 * aligned to avoid the app crashing when using those methods.
                 */
                return 8;
            }
        }
        if (LongType.v().equals(t) || DoubleType.v().equals(t)) {
            return arch.is32Bit() ? 4 : 8;
        }
        return getFieldSize(arch, f);
    }
    
    public static int getFieldSize(Arch arch, SootField f) {
        soot.Type t = f.getType();
        if (LongType.v().equals(t) || DoubleType.v().equals(t)) {
            return 8;
        }
        if (IntType.v().equals(t) || FloatType.v().equals(t)) {
            return 4;
        }
        if (t instanceof RefLikeType) {
            return arch.is32Bit() ? 4 : 8;
        }
        if (ShortType.v().equals(t) || CharType.v().equals(t)) {
            return 2;
        }
        if (ByteType.v().equals(t) || BooleanType.v().equals(t)) {
            return 1;
        }
        throw new IllegalArgumentException("Unknown Type: " + t);
    }

    public static Value getFieldPtr(Function f, Value base, Constant offset, Type fieldType) {
        Variable baseI8Ptr = f.newVariable(Type.I8_PTR);
        f.add(new Bitcast(baseI8Ptr, base, Type.I8_PTR));
        Variable fieldI8Ptr = f.newVariable(Type.I8_PTR);
        f.add(new Getelementptr(fieldI8Ptr, baseI8Ptr.ref(), offset));
        Variable fieldPtr = f.newVariable(new PointerType(fieldType));
        f.add(new Bitcast(fieldPtr, fieldI8Ptr.ref(), fieldPtr.getType()));
        return fieldPtr.ref();
    }
    
    public static List<SootField> getFields(final OS os, final Arch arch, SootClass clazz, boolean ztatic) {
        List<SootField> l = new ArrayList<SootField>();
        for (SootField f : clazz.getFields()) {
            if (ztatic == f.isStatic()) {
                l.add(f);
            }
        }
        // Sort the fields. references, volatile long, double, long, float, int, char, short, boolean, byte.
        // Fields of same type are sorted by name.
        Collections.sort(l, new Comparator<SootField>() {
            @Override
            public int compare(SootField o1, SootField o2) {
                soot.Type t1 = o1.getType();
                soot.Type t2 = o2.getType();
                if (t1 instanceof RefLikeType) {
                    if (!(t2 instanceof RefLikeType)) {
                        return -1;
                    }
                }
                if (t2 instanceof RefLikeType) {
                    if (!(t1 instanceof RefLikeType)) {
                        return 1;
                    }
                }
                
                // Compare alignment. Higher first.
                int align1 = getFieldAlignment(os, arch, o1);
                int align2 = getFieldAlignment(os, arch, o2);
                int c = new Integer(align2).compareTo(align1);
                if (c == 0) {
                    // Compare size. Larger first.
                    int size1 = getFieldSize(arch, o1);
                    int size2 = getFieldSize(arch, o2);
                    c = new Integer(size2).compareTo(size1);
                    if (c == 0) {
                        // Compare type name.
                        c = t1.getClass().getSimpleName().compareTo(t2.getClass().getSimpleName());
                        if (c == 0) {
                            // Compare name.
                            c = o1.getName().compareTo(o2.getName());
                        }
                    }
                }
                return c;
            }
        });
        return l;
    }
    
    public static List<SootField> getClassFields(OS os, Arch arch, SootClass clazz) {
        return getFields(os, arch, clazz, true);
    }
    
    public static List<SootField> getInstanceFields(OS os, Arch arch, SootClass clazz) {
        return getFields(os, arch, clazz, false);
    }
}
