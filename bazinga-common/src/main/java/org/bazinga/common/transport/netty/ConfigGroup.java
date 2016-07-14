package org.bazinga.common.transport.netty;


public interface ConfigGroup {
	
	/**
     * Config for parent.
     */
    Config parent();

    /**
     * Config for child.
     */
    Config child();

}
