package org.bazinga.common.message;

import static org.bazinga.common.message.Status.OK;

public class Response extends BytesHolder {
	
	private byte status = OK.value();
	
	private long id; // invoke id
    private ResultMessageWrapper result; // 服务调用结果
    
    public static Response newInstance(long id, Status status, ResultMessageWrapper result) {
        return newInstance(id, status.value(), result);
    }

    public static Response newInstance(long id, Status status, byte[] bytes) {
        return newInstance(id, status.value(), bytes);
    }

    public static Response newInstance(long id, byte status, ResultMessageWrapper result) {
        Response r = new Response(id);
        r.status(status);
        r.result(result);
        return r;
    }

    public static Response newInstance(long id, byte status, byte[] bytes) {
        Response r = new Response(id);
        r.status(status);
        r.bytes(bytes);
        return r;
    }

    public Response() {}

    public Response(long id) {
        this.id = id;
    }

    public byte status() {
        return status;
    }

    public void status(byte status) {
        this.status = status;
    }

    public long id() {
        return id;
    }

    public void id(long id) {
        this.id = id;
    }

    public ResultMessageWrapper result() {
        return result;
    }

    public void result(ResultMessageWrapper result) {
        this.result = result;
    }

	@Override
	public String toString() {
		return "Response [status=" + status + ", id=" + id + ", result="
				+ result + "]";
	}

}
