package com.reliaquest.api.cache;

import com.reliaquest.api.model.Employee;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EmployeeCache {

    private static final int MAX_SIZE = 100;
    private static final int TTL_MINUTES = 2;

    private final Map<String, CacheEntry<List<Employee>>> allEmployeesCache =
            new LinkedHashMap<String, CacheEntry<List<Employee>>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<List<Employee>>> eldest) {
                    return size() > MAX_SIZE;
                }
            };

    private final Map<String, CacheEntry<Employee>> employeeByIdCache =
            new LinkedHashMap<String, CacheEntry<Employee>>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CacheEntry<Employee>> eldest) {
                    return size() > MAX_SIZE;
                }
            };

    public List<Employee> getAllEmployees() {
        CacheEntry<List<Employee>> entry = allEmployeesCache.get("all");
        if (entry != null && !isExpired(entry)) {
            return entry.getValue();
        }
        return null;
    }

    public void putAllEmployees(List<Employee> employees) {
        allEmployeesCache.put("all", new CacheEntry<>(employees, LocalDateTime.now()));
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
