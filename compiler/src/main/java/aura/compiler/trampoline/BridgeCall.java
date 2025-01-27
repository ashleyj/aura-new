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
package aura.compiler.trampoline;

import aura.compiler.llvm.FunctionType;
import aura.compiler.Symbols;
import aura.compiler.Types;


/**
 *
 * @version $Id$
 */
public class BridgeCall extends Trampoline {
    private static final long serialVersionUID = 1L;
    
    private final String methodName;
    private final String methodDesc;
    private final boolean ztatic;

    public BridgeCall(String callingClass, String targetClass, String methodName, String methodDesc, boolean ztatic) {
        super(callingClass, targetClass);
        this.methodName = methodName;
        this.methodDesc = methodDesc;
        this.ztatic = ztatic;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDesc() {
        return methodDesc;
    }
    
    @Override
    public FunctionType getFunctionType() {
        return Types.getNativeFunctionType(methodDesc, ztatic);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((methodDesc == null) ? 0 : methodDesc.hashCode());
        result = prime * result
                + ((methodName == null) ? 0 : methodName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BridgeCall other = (BridgeCall) obj;
        if (methodDesc == null) {
            if (other.methodDesc != null) {
                return false;
            }
        } else if (!methodDesc.equals(other.methodDesc)) {
            return false;
        }
        if (methodName == null) {
            if (other.methodName != null) {
                return false;
            }
        } else if (!methodName.equals(other.methodName)) {
            return false;
        }
        return true;
    }
    
    @Override
    public int compareTo(Trampoline o) {
        int c = super.compareTo(o);
        if (c == 0) {
            c = methodName.compareTo(((BridgeCall) o).methodName);
            if (c == 0) {
                c = methodDesc.compareTo(((BridgeCall) o).methodDesc);
            }
        }
        return c;
    }
    
    @Override
    public String toString() {
        return Symbols.trampolineMethodSymbol(this, getCallingClass(), getTarget(), getMethodName(), getMethodDesc());
    }
}
