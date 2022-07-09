package com.prhythm.ssjv001.message;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
public class GroupAdvisor {

    private final Map<String, Set<Integer>> assignedPartitions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private Set<Integer> obtainPartitions(String topic) {
        Set<Integer> partitions;
        if (assignedPartitions.containsKey(topic)) {
            partitions = assignedPartitions.get(topic);
        } else {
            assignedPartitions.put(topic, partitions = new CopyOnWriteArraySet<>());
        }
        return partitions;
    }

    public void clear(String topic) {
        obtainPartitions(topic).clear();
    }

    public synchronized void register(String topic, int partition) {
        Set<Integer> partitions = obtainPartitions(topic);
        partitions.add(partition);
    }

    public synchronized void unregister(String topic, int partition) {
        Set<Integer> partitions = obtainPartitions(topic);
        partitions.remove(partition);
    }

    public int next(String topic) {
        Set<Integer> partitions = obtainPartitions(topic);
        int size = partitions.size();
        if (size == 0) {
            log.error("no partition available for topic: {}", topic);
            throw new IllegalStateException();
        }
        return partitions.stream()
                .skip(random.nextInt(size))
                .findFirst()
                .orElseThrow();
    }

}
