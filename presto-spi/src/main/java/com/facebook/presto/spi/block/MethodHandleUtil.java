/*
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

package com.facebook.presto.spi.block;

import com.facebook.presto.spi.PrestoException;
import com.facebook.presto.spi.type.Type;
import io.airlift.slice.Slice;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static com.facebook.presto.spi.StandardErrorCode.GENERIC_INTERNAL_ERROR;

public class MethodHandleUtil
{
    private MethodHandleUtil()
    {
    }

    /**
     * @param f (U, S1, S2, ...)R
     * @param g (T1, T2, ...)U
     * @return (T1, T2, ..., S1, S2, ...)R
     */
    public static MethodHandle compose(MethodHandle f, MethodHandle g)
    {
        return MethodHandles.foldArguments(MethodHandles.dropArguments(f, 1, g.type().parameterList()), g);
    }

    /**
     * @param f (U, V)R
     * @param g (S1, S2, ...)U
     * @param h (T1, T2, ...)V
     * @return (S1, S2, ..., T1, T2, ...)R
     */
    public static MethodHandle compose(MethodHandle f, MethodHandle g, MethodHandle h)
    {
        MethodHandle fVTU = MethodHandles.permuteArguments(
                f,
                f.type().dropParameterTypes(0, 1).appendParameterTypes(h.type().parameterList()).appendParameterTypes(f.type().parameterType(0)),
                h.type().parameterCount() + 1, 0);
        MethodHandle fhTU = MethodHandles.foldArguments(fVTU, h);

        int[] reorder = new int[fhTU.type().parameterCount()];
        for (int i = 0; i < reorder.length - 1; i++) {
            reorder[i] = i + 1 + g.type().parameterCount();
        }
        reorder[reorder.length - 1] = 0;
        MethodHandle fhUST = MethodHandles.permuteArguments(
                fhTU,
                f.type().dropParameterTypes(1, 2).appendParameterTypes(g.type().parameterList()).appendParameterTypes(h.type().parameterList()),
                reorder);
        return MethodHandles.foldArguments(fhUST, g);
    }

    public static MethodHandle methodHandle(Class<?> clazz, String name, Class<?>... parameterTypes)
    {
        try {
            return MethodHandles.lookup().unreflect(clazz.getMethod(name, parameterTypes));
        }
        catch (IllegalAccessException | NoSuchMethodException e) {
            throw new PrestoException(GENERIC_INTERNAL_ERROR, e);
        }
    }

    public static MethodHandle nativeValueGetter(Type type)
    {
        Class<?> javaType = type.getJavaType();

        MethodHandle methodHandle;
        if (javaType == long.class) {
            methodHandle = methodHandle(Type.class, "getLong", Block.class, int.class);
        }
        else if (javaType == double.class) {
            methodHandle = methodHandle(Type.class, "getDouble", Block.class, int.class);
        }
        else if (javaType == boolean.class) {
            methodHandle = methodHandle(Type.class, "getBoolean", Block.class, int.class);
        }
        else if (javaType == Slice.class) {
            methodHandle = methodHandle(Type.class, "getSlice", Block.class, int.class);
        }
        else if (javaType == Block.class) {
            MethodHandle getObjectMethodHandle = methodHandle(Type.class, "getObject", Block.class, int.class);
            methodHandle = getObjectMethodHandle.asType(getObjectMethodHandle.type().changeReturnType(Block.class));
        }
        else if (javaType == void.class) {
            methodHandle = methodHandle(MethodHandleUtil.class, "unknownGetter", Type.class, Block.class, int.class);
        }
        else {
            throw new IllegalArgumentException("Unknown java type " + javaType + " from type " + type);
        }

        return methodHandle.bindTo(type);
    }

    public static Void unknownGetter(Type type, Block block, int position)
    {
        throw new IllegalArgumentException("For UNKNOWN type, getter should never be invoked on Block");
    }
}
