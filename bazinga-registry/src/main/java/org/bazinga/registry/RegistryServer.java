package org.bazinga.registry;

import static org.bazinga.common.utils.Reflects.findMatchingParameterTypes;

import java.lang.reflect.Constructor;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.bazinga.common.utils.JUnsafe;
import org.bazinga.common.utils.SystemPropertyUtil;


public interface RegistryServer extends RegistryMonitor {
	
	void startRegistryServer();
	
	@SuppressWarnings("unchecked")
	class Default {
		
		//class
		private static final Class<RegistryServer> defaultRegistryClass;
		
		//构造函数的入参
		private static final List<Class<?>[]> allConstructorsParameterTypes;
		
		static {
			
			Class<RegistryServer> cls;
			
			try {
				cls = (Class<RegistryServer>)Class.forName(SystemPropertyUtil.get("bazinga.registry.default", "org.bazinga.registry.DefaultRegistryServer"));
			} catch (Exception e) {
				cls = null;
			}
			 defaultRegistryClass = cls;
			 
			 if (defaultRegistryClass != null) {
				 
	                allConstructorsParameterTypes = new ArrayList<Class<?>[]>();
	                Constructor<?>[] array = defaultRegistryClass.getDeclaredConstructors();
	                for (Constructor<?> c : array) {
	                    allConstructorsParameterTypes.add(c.getParameterTypes());
	                }
	            } else {
	                allConstructorsParameterTypes = null;
	            }
		}
		
		public static RegistryServer createRegistryServer(int port) {
            return newInstance(port);
        }

        public static RegistryServer createRegistryServer(SocketAddress address) {
            return newInstance(address);
        }

        public static RegistryServer createRegistryServer(int port, int nWorks) {
            return newInstance(port, nWorks);
        }

        public static RegistryServer createRegistryServer(SocketAddress address, int nWorks) {
            return newInstance(address, nWorks);
        }

        private static RegistryServer newInstance(Object... parameters) {
            if (defaultRegistryClass == null || allConstructorsParameterTypes == null) {
                throw new UnsupportedOperationException("unsupported default registry");
            }

            Class<?>[] parameterTypes = findMatchingParameterTypes(allConstructorsParameterTypes, parameters);
            if (parameterTypes == null) {
                throw new IllegalArgumentException("parameter types");
            }

            try {
                Constructor<RegistryServer> c = defaultRegistryClass.getConstructor(parameterTypes);
                c.setAccessible(true);
                return c.newInstance(parameters);
            } catch (Exception e) {
                JUnsafe.throwException(e);
            }
            return null; 
        }
	}
}
