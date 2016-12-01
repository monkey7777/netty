/*
 * Copyright 2016 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.buffer;


import io.netty.util.ResourceLeak;
import io.netty.util.ResourceLeakDetector;

import java.nio.ByteOrder;

final class SimpleLeakAwareCompositeByteBuf extends WrappedCompositeByteBuf {

    private final ResourceLeak leak;

    SimpleLeakAwareCompositeByteBuf(CompositeByteBuf wrapped, ResourceLeak leak) {
        super(wrapped);
        this.leak = leak;
    }

    @Override
    public boolean release() {
        // Call unwrap() before just in case that super.release() will change the ByteBuf instance that is returned
        // by ByteBuf.
        ByteBuf unwrapped = unwrap();
        if (super.release()) {
            boolean closed = ResourceLeakDetector.close(leak, unwrapped);
            assert closed;
            return true;
        }
        return false;
    }

    @Override
    public boolean release(int decrement) {
        // Call unwrap() before just in case that super.release() will change the ByteBuf instance that is returned
        // by ByteBuf.
        ByteBuf unwrapped = unwrap();
        if (super.release(decrement)) {
            boolean closed = ResourceLeakDetector.close(leak, unwrapped);
            assert closed;
            return true;
        }
        return false;
    }

    @Override
    public ByteBuf order(ByteOrder endianness) {
        leak.record();
        if (order() == endianness) {
            return this;
        } else {
            return new SimpleLeakAwareByteBuf(super.order(endianness), leak);
        }
    }

    @Override
    public ByteBuf slice() {
        return new SimpleLeakAwareByteBuf(super.slice(), leak);
    }

    @Override
    public ByteBuf retainedSlice() {
        return new SimpleLeakAwareByteBuf(super.retainedSlice(), leak);
    }

    @Override
    public ByteBuf slice(int index, int length) {
        return new SimpleLeakAwareByteBuf(super.slice(index, length), leak);
    }

    @Override
    public ByteBuf retainedSlice(int index, int length) {
        return new SimpleLeakAwareByteBuf(super.retainedSlice(index, length), leak);
    }

    @Override
    public ByteBuf duplicate() {
        return new SimpleLeakAwareByteBuf(super.duplicate(), leak);
    }

    @Override
    public ByteBuf retainedDuplicate() {
        return new SimpleLeakAwareByteBuf(super.retainedDuplicate(), leak);
    }

    @Override
    public ByteBuf readSlice(int length) {
        return new SimpleLeakAwareByteBuf(super.readSlice(length), leak);
    }

    @Override
    public ByteBuf readRetainedSlice(int length) {
        return new SimpleLeakAwareByteBuf(super.readRetainedSlice(length), leak);
    }

    @Override
    public ByteBuf asReadOnly() {
        return new SimpleLeakAwareByteBuf(super.asReadOnly(), leak);
    }
}
