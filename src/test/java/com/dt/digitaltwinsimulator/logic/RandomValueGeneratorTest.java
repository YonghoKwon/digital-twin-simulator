package com.dt.digitaltwinsimulator.logic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomValueGeneratorTest {

    @Test
    void generateStringWithFixedLength() {
        String value = RandomValueGenerator.generate("String", "12");

        assertThat(value).hasSize(12);
    }

    @Test
    void generateStringWithRangeLength() {
        String value = RandomValueGenerator.generate("String", "5..10");

        assertThat(value.length()).isBetween(5, 10);
    }

    @Test
    void generateIntegerWithinRange() {
        int value = Integer.parseInt(RandomValueGenerator.generate("Integer", "10..20"));

        assertThat(value).isBetween(10, 20);
    }

    @Test
    void generateDoubleWithinRange() {
        double value = Double.parseDouble(RandomValueGenerator.generate("Double", "1.5..2.5"));

        assertThat(value).isBetween(1.5, 2.5);
    }

    @Test
    void generateBoolean() {
        String value = RandomValueGenerator.generate("Boolean", "");

        assertThat(value).isIn("true", "false");
    }

    @Test
    void generateDefaultDateTimestamp() {
        String value = RandomValueGenerator.generate("Date", "");

        assertThat(value).matches("\\d{17}");
    }

    @Test
    void detectRandomFlag() {
        assertThat(RandomValueGenerator.isRandomEnabled("1")).isTrue();
        assertThat(RandomValueGenerator.isRandomEnabled("true")).isTrue();
        assertThat(RandomValueGenerator.isRandomEnabled("yes")).isTrue();
        assertThat(RandomValueGenerator.isRandomEnabled("0")).isFalse();
    }
}
