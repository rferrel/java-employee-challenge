package com.reliaquest.api.cache;

import com.reliaquest.api.model.Employee;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class EmployeeCache {

    private static final int MAX_SIZE = 100;
    private static final int TTL_MINUTES = 2;
    private static final String ALL_EMPLOYEES_CACHE_KEY = "all";
    // Trigger revalidation when 80% of TTL has elapsed to prevent cache stampede
    private static final double EARLY_REVALIDATION_THRESHOLD = 0.8;
    private static final Random RANDOM = new Random();

    private final Map<String, CacheEntry<List<Employee>>> allEmployeesCache =
            Collections.synchronizedMap(new LinkedHashMap<String, CacheEntry<List<Employee>>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<List<Employee>>> eldest) {
                    return size() > MAX_SIZE;
                }
            });

    private final Map<String, CacheEntry<Employee>> employeeByIdCache =
            Collections.synchronizedMap(new LinkedHashMap<String, CacheEntry<Employee>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<Employee>> eldest) {
                    return size() > MAX_SIZE;
                }
            });

    public List<Employee> getAllEmployees() {
        CacheEntry<List<Employee>> entry = allEmployeesCache.get(ALL_EMPLOYEES_CACHE_KEY);
        if (entry != null && !isExpired(entry)) {
            // Check if we're in the revalidation window (80% of TTL) to prevent cache stampede
            if (shouldRevalidateEarly(entry)) {
                // Probabilistically trigger revalidation to spread load
                if (RANDOM.nextDouble() < 0.1) { // 10% chance to revalidate early
                    return null; // Force revalidation
                }
            }
            return entry.getValue();
        }
        return null;
    }

    public void putAllEmployees(List<Employee> employees) {
        allEmployeesCache.put(ALL_EMPLOYEES_CACHE_KEY, new CacheEntry<>(employees, LocalDateTime.now()));
    }

    public Employee getEmployeeById(String id) {
        CacheEntry<Employee> entry = employeeByIdCache.get(id);
        if (entry != null && !isExpired(entry)) {
            return entry.getValue();
        }
        return null;
    }

    public void putEmployeeById(String id, Employee employee) {
        employeeByIdCache.put(id, new CacheEntry<>(employee, LocalDateTime.now()));
    }

    public void invalidateAll() {
        allEmployeesCache.clear();
        employeeByIdCache.clear();
    }

    public void invalidateEmployee(String id) {
        employeeByIdCache.remove(id);
        // Also invalidate all employees cache since it might contain this employee
        allEmployeesCache.clear();
    }

    private boolean isExpired(CacheEntry<?> entry) {
        return entry.getTimestamp().plusMinutes(TTL_MINUTES).isBefore(LocalDateTime.now());
    }

    private boolean shouldRevalidateEarly(CacheEntry<?> entry) {
        LocalDateTime expirationTime = entry.getTimestamp().plusMinutes(TTL_MINUTES);
        LocalDateTime earlyRevalidationTime =
                entry.getTimestamp().plusSeconds((long) (TTL_MINUTES * 60 * EARLY_REVALIDATION_THRESHOLD));
        return LocalDateTime.now().isAfter(earlyRevalidationTime)
                && LocalDateTime.now().isBefore(expirationTime);
    }

    private static class CacheEntry<T> {
        private final T value;
        private final LocalDateTime timestamp;

        public CacheEntry(T value, LocalDateTime timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public T getValue() {
            return value;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
