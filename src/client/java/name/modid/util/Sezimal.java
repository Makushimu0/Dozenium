package name.modid.util;

public final class Sezimal {

    private Sezimal() {}

    private static final char[] SEZIMAL_DIGITS = {
        '0', '1', '2', '3', '4', '5'
    };
    public static String toSezimal(int value) {
        boolean negative = value < 0;
        value = Math.abs(value);
        if (value == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(SEZIMAL_DIGITS[value % 6]);
            value /= 6;
        }

        if (negative) {
            sb.append('-');
        }
        return sb.reverse().toString();
    }
    
    public static String toSezimalPercent(int percent) {
        return toSezimal(Math.round(percent / 100f * 36f));
    }

    public static String toFloatSezimal(float value) {
        int int_value = (int) value;
        float frac_value = value - int_value;
        String int_sezimal = toSezimal(int_value);
        if (frac_value < 0.0001) return int_sezimal;
        StringBuilder sb = new StringBuilder();
        while (frac_value > 0.0001 && sb.length() < 2) {
            frac_value *= 6;
            sb.append(SEZIMAL_DIGITS[((int) frac_value)]);
            frac_value -= (int) frac_value;
        }
        return int_sezimal + "." + sb.toString();
    }
}
