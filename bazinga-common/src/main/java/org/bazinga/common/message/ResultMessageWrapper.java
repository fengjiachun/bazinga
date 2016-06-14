package org.bazinga.common.message;

import java.io.Serializable;

public class ResultMessageWrapper implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7134089378620478466L;

	private Object result;
    private String error;
    
    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setError(Throwable t) {
        this.error = t.getMessage();
    }

    @Override
    public String toString() {
        return "ResultWrapper{" +
                "result=" + result +
                ", error=" + error +
                '}';
    }
	
}
