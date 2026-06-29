package com.puntodeapoyo.common;

public final class PhoneNormalizer {

    private PhoneNormalizer() {
    }

    public static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        boolean hasInternationalPrefix = trimmed.startsWith("+");
        String digits = digitsOnly(trimmed);

        if (digits == null) {
            return null;
        }
        return hasInternationalPrefix ? "+" + digits : digits;
    }

    public static String digitsOnly(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String digits = value.replaceAll("\\D", "");
        return digits.isBlank() ? null : digits;
    }
}
