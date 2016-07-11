package org.bazinga.common.group;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceBChannelGroup {
	
	private static final ConcurrentMap<String, CopyOnWriteGroupList> groups = new ConcurrentHashMap<String, ServiceBChannelGroup.CopyOnWriteGroupList>();
	
	private static final GroupRefCounterMap groupRefCounter = new GroupRefCounterMap();
	
	public static CopyOnWriteGroupList list(String serviceName) {
        String _serviceName = serviceName;

        CopyOnWriteGroupList groupList = groups.get(_serviceName);
        if (groupList == null) {
            CopyOnWriteGroupList newGroupList = new CopyOnWriteGroupList();
            groupList = groups.putIfAbsent(_serviceName, newGroupList);
            if (groupList == null) {
                groupList = newGroupList;
            }
        }

        return groupList;
    }
	
	public static class CopyOnWriteGroupList extends CopyOnWriteArrayList<BChannelGroup> {

        private static final long serialVersionUID = -666607632499368496L;

        @Override
        public boolean addIfAbsent(BChannelGroup group) {
            boolean added = super.addIfAbsent(group);
            if (added) {
                incrementRefCount(group);
            }
            return added;
        }

        @Override
        public boolean remove(Object o) {
            boolean removed = super.remove(o);
            if (removed && o instanceof BChannelGroup) {
                decrementRefCount((BChannelGroup) o);
            }
            return removed;
        }

        @Override
        public boolean add(BChannelGroup group) {
            throw new UnsupportedOperationException();
        }
    }
	
	public static int incrementRefCount(BChannelGroup group) {
        return groupRefCounter.getOrCreate(group).incrementAndGet();
    }

    public static int decrementRefCount(BChannelGroup group) {
        AtomicInteger counter = groupRefCounter.get(group);
        if (counter == null) {
            return 0;
        }
        int count = counter.decrementAndGet();
        if (count == 0) {
            // 一台机器的所有服务全部下线才会走到这里, 并发问题(get与remove并不是原子操作)是可接受的
            groupRefCounter.remove(group);
        }
        return count;
    }
    
    public static class GroupRefCounterMap extends ConcurrentHashMap<BChannelGroup, AtomicInteger> {

		private static final long serialVersionUID = 2340555873281718774L;

		public AtomicInteger getOrCreate(BChannelGroup key) {
            AtomicInteger counter = super.get(key);
            if (counter == null) {
                AtomicInteger newCounter = new AtomicInteger(0);
                counter = super.putIfAbsent(key, newCounter);
                if (counter == null) {
                    counter = newCounter;
                }
            }

            return counter;
        }
    }
	

}
