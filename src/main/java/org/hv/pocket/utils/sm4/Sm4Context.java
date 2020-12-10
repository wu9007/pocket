package org.hv.pocket.utils.sm4;

public class Sm4Context {
    public int mode;

    public final long[] sk;

    public boolean isPadding;

    public Sm4Context() {
        this.mode = 1;
        this.isPadding = true;
        this.sk = new long[32];
    }
}
