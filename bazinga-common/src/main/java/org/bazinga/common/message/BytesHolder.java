package org.bazinga.common.message;

/**
 * 
 * @author BazingaLyn
 * @copyright fjc
 * @time 2016年5月30日
 */
public abstract class BytesHolder {

    private transient byte[] bytes;

    public byte[] bytes() {
        return bytes;
    }

    public void bytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }
}
