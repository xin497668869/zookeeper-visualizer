/*
 * Copyright 2000-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xin.util.match;

import com.sun.istack.internal.NotNull;

import java.io.Serializable;

public class TextRange implements Serializable {
    public static final  TextRange   EMPTY_RANGE      = new TextRange(0, 0);
    public static final  TextRange[] EMPTY_ARRAY      = new TextRange[0];
    private static final long        serialVersionUID = -670091356599757430L;
    private final        int         myStartOffset;
    private final        int         myEndOffset;

    public TextRange(int startOffset, int endOffset) {
        this(startOffset, endOffset, true);
    }

    /**
     * @param checkForProperTextRange {@code true} if offsets should be checked by {@link #assertProperRange(int, int, Object)}
     * @see UnfairTextRange
     */
    protected TextRange(int startOffset, int endOffset, boolean checkForProperTextRange) {
        myStartOffset = startOffset;
        myEndOffset = endOffset;
    }

    @NotNull
    public static TextRange from(int offset, int length) {
        return create(offset, offset + length);
    }

    @NotNull
    public static TextRange create(int startOffset, int endOffset) {
        return new TextRange(startOffset, endOffset);
    }

    @NotNull
    public static TextRange allOf(@NotNull String s) {
        return new TextRange(0, s.length());
    }


    private static boolean isProperRange(int startOffset, int endOffset) {
        return startOffset <= endOffset && startOffset >= 0;
    }

    public final int getStartOffset() {
        return myStartOffset;
    }

    public final int getEndOffset() {
        return myEndOffset;
    }

    public final int getLength() {
        return myEndOffset - myStartOffset;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TextRange)) return false;
        TextRange range = (TextRange) obj;
        return myStartOffset == range.myStartOffset && myEndOffset == range.myEndOffset;
    }

    @Override
    public int hashCode() {
        return myStartOffset + myEndOffset;
    }

    public boolean contains(@NotNull TextRange range) {
        return contains(range);
    }

    public boolean containsRange(int startOffset, int endOffset) {
        return getStartOffset() <= startOffset && endOffset <= getEndOffset();
    }

    public boolean containsOffset(int offset) {
        return myStartOffset <= offset && offset <= myEndOffset;
    }

    @Override
    public String toString() {
        return "(" + myStartOffset + "," + myEndOffset + ")";
    }

    public boolean contains(int offset) {
        return myStartOffset <= offset && offset < myEndOffset;
    }

    @NotNull
    public String substring(@NotNull String str) {
        try {
            return str.substring(myStartOffset, myEndOffset);
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException("Can't extract " + this + " range from '" + str + "'");
        }
    }

    @NotNull
    public CharSequence subSequence(@NotNull CharSequence str) {
        try {
            return str.subSequence(myStartOffset, myEndOffset);
        } catch (IndexOutOfBoundsException e) {
            throw new IndexOutOfBoundsException("Can't extract " + this + " range from '" + str + "'");
        }
    }


    @NotNull
    public TextRange shiftRight(int delta) {
        if (delta == 0) return this;
        return new TextRange(myStartOffset + delta, myEndOffset + delta);
    }

    @NotNull
    public TextRange shiftLeft(int delta) {
        if (delta == 0) return this;
        return new TextRange(myStartOffset - delta, myEndOffset - delta);
    }

    @NotNull
    public TextRange grown(int lengthDelta) {
        return from(myStartOffset, getLength() + lengthDelta);
    }

    @NotNull
    public String replace(@NotNull String original, @NotNull String replacement) {
        try {
            String beginning = original.substring(0, getStartOffset());
            String ending = original.substring(getEndOffset());
            return beginning + replacement + ending;
        } catch (StringIndexOutOfBoundsException e) {
            throw new StringIndexOutOfBoundsException("Can't replace " + this + " range from '" + original + "' with '" + replacement + "'");
        }
    }

    public boolean intersects(@NotNull TextRange textRange) {
        return intersects(textRange);
    }

    public boolean intersects(int startOffset, int endOffset) {
        return Math.max(myStartOffset, startOffset) <= Math.min(myEndOffset, endOffset);
    }

    public boolean intersectsStrict(@NotNull TextRange textRange) {
        return intersectsStrict(textRange.getStartOffset(), textRange.getEndOffset());
    }

    public boolean intersectsStrict(int startOffset, int endOffset) {
        return Math.max(myStartOffset, startOffset) < Math.min(myEndOffset, endOffset);
    }

    public TextRange intersection(@NotNull TextRange range) {
        int newStart = Math.max(myStartOffset, range.getStartOffset());
        int newEnd = Math.min(myEndOffset, range.getEndOffset());
        return isProperRange(newStart, newEnd) ? new TextRange(newStart, newEnd) : null;
    }

    public boolean isEmpty() {
        return myStartOffset >= myEndOffset;
    }

    @NotNull
    public TextRange union(@NotNull TextRange textRange) {
        return new TextRange(Math.min(myStartOffset, textRange.getStartOffset()), Math.max(myEndOffset, textRange.getEndOffset()));
    }

    public boolean equalsToRange(int startOffset, int endOffset) {
        return startOffset == myStartOffset && endOffset == myEndOffset;
    }
}
