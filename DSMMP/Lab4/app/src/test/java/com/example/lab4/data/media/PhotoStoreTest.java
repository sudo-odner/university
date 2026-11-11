package com.example.lab4.data.media;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PhotoStoreTest {
    @Test public void sampleSizeStartsAtOne() {
        assertEquals(1, PhotoStore.calculateSampleSize(1024, 768));
        assertEquals(1, PhotoStore.calculateSampleSize(2048, 1024));
    }
    @Test public void largeImageUsesPowerOfTwoSampling() {
        assertEquals(2, PhotoStore.calculateSampleSize(4000, 3000));
        assertEquals(4, PhotoStore.calculateSampleSize(8000, 6000));
    }
}
