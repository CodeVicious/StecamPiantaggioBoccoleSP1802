package stecamSP1802.controllers;

public class ForzePiantaggio {
    private float forza1;
    private float forza2;
    private float forza3;
    private float forza4;

    public ForzePiantaggio(float forza1, float forza2, float forza3, float forza4) {
        this.forza1 = forza1;
        this.forza2 = forza2;
        this.forza3 = forza3;
        this.forza4 = forza4;
    }

    public float getForza1() {
        return forza1;
    }

    public void setForza1(float forza1) {
        this.forza1 = forza1;
    }

    public float getForza2() {
        return forza2;
    }

    public void setForza2(float forza2) {
        this.forza2 = forza2;
    }

    public float getForza3() {
        return forza3;
    }

    public void setForza3(float forza3) {
        this.forza3 = forza3;
    }

    public float getForza4() {
        return forza4;
    }

    public void setForza4(float forza4) {
        this.forza4 = forza4;
    }

    public String getForza1String() {
        return Float.toString(forza1);
    }
    public String getForza2String() {
        return Float.toString(forza2);
    }
    public String getForza3String() {
        return Float.toString(forza3);
    }
    public String getForza4String() {
        return Float.toString(forza4);
    }
}
