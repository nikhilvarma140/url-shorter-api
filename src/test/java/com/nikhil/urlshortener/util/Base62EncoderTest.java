package com.nikhil.urlshortener.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Base62EncoderTest {

    @Test
    void shouldEncodeSmallNumber() {
        assertEquals("b", Base62Encoder.encode(1));
    }

    @Test
    void shouldEncodeLargeNumber() {
        String encoded = Base62Encoder.encode(999999999L);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
    }

    @Test
    void shouldDecodeBackToOriginal() {
        long original = 123456789L;
        String encoded = Base62Encoder.encode(original);
        long decoded = Base62Encoder.decode(encoded);
        assertEquals(original, decoded);
    }

    @Test
    void shouldProduceUniqueCodesForDifferentNumbers() {
        String code1 = Base62Encoder.encode(1);
        String code2 = Base62Encoder.encode(2);
        String code3 = Base62Encoder.encode(100);
        assertNotEquals(code1, code2);
        assertNotEquals(code2, code3);
    }

    @Test
    void shouldHandleZero() {
        assertEquals("a", Base62Encoder.encode(0));
    }
}
