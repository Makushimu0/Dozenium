package name.modid.util;

public final class Dozenal {

    private Dozenal() {}

    private static final char[] DOZENAL_DIGITS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'X', 'E'
    };
    public static String toDozenal(int value) {
        if (value == 0) return "0";
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(DOZENAL_DIGITS[value % 12]);
            value /= 12;
        }
        return sb.reverse().toString();
    }
    
    public static String toDozenalPercent(int percent) {
        return toDozenal(Math.round(percent / 100f * 144f));
    }

    public static String toFloatDozenal(float value) {
        int int_value = (int) value;
        float frac_value = value - int_value;
        String int_dozenal = toDozenal(int_value);
        if (frac_value < 0.0001) return int_dozenal;
        StringBuilder sb = new StringBuilder();
        while (frac_value > 0.0001 && sb.length() < 2) {
            frac_value *= 12;
            sb.append(DOZENAL_DIGITS[((int) frac_value)]);
            frac_value -= (int) frac_value;
        }
        return int_dozenal + "." + sb.toString();
    }
}
