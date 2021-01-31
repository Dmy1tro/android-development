package dmytro.laskuryk.lab_4.Models;

import dmytro.laskuryk.lab_4.R;

public enum Importance {
    Low(0),
    Medium(1),
    High(2);

    private final int value;

    private Importance(int value) {
        this.value = value;
    }

    public static Importance parse(Integer value) {
        switch (value) {
            case 0:
                return Importance.Low;
            case 1:
                return Importance.Medium;
            case 2:
                return Importance.High;
            default:
                return Importance.Medium;
        }
    }

    public int getValue() {
        return value;
    }

    public int getResource() {
        switch (this) {
            case Low:
                return R.drawable.low;
            case Medium:
                return R.drawable.medium;
            case High:
                return R.drawable.high;
            default:
                return R.drawable.empty_img;
        }
    }

    public int getResourceString() {
        switch (this) {
            case Low:
                return R.string.low;
            case Medium:
                return R.string.medium;
            case High:
                return R.string.high;
        }

        return Integer.MIN_VALUE;
    }
}
