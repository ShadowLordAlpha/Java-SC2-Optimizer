package com.shadowcs.optimizer.genetics;

import com.shadowcs.optimizer.random.XORShiftRandom;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link XORShiftRandom} class.
 */
public class XORShiftRandomTest {

    /**
     * Tests the {@link XORShiftRandom#nextInt()} method.
     */
    @Test
    public void testNextInt() {
        long seed = 1234567890;
        XORShiftRandom random = new XORShiftRandom(seed);
        int randomInt = random.nextInt();
        assertEquals(randomInt, new XORShiftRandom(seed).nextInt());
    }

    /**
     * Tests the {@link XORShiftRandom#nextLong()} method.
     */
    @Test
    public void testNextLong() {
        long seed = 1234567890;
        XORShiftRandom random = new XORShiftRandom(seed);
        long randomLong = random.nextLong();
        assertEquals(randomLong, new XORShiftRandom(seed).nextLong());
    }

    /**
     * Tests the {@link XORShiftRandom#nextDouble()} method.
     */
    @Test
    public void testNextDouble() {
        long seed = 1234567890;
        XORShiftRandom random = new XORShiftRandom(seed);
        double randomDouble = random.nextDouble();
        assertEquals(randomDouble, new XORShiftRandom(seed).nextDouble(), 0.0);
    }
}
