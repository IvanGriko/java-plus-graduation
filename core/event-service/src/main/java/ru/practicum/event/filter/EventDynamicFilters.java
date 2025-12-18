package ru.practicum.event.filter;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.dto.event.EventAdminParams;
import ru.practicum.dto.event.EventParams;
import ru.practicum.event.dal.Event;

import java.util.ArrayList;

public class EventDynamicFilters {

    public static Specification<Event> buildAdminFilter(final EventAdminParams filters) {
        return (root, query, criteriaBuilder) -> {
            var conditions = new ArrayList<Predicate>();
            if (filters.getUsers() != null && !filters.getUsers().isEmpty()) {
                conditions.add(root.get("initiatorId").in(filters.getUsers()));
            }
            if (filters.getStates() != null && !filters.getStates().isEmpty()) {
                conditions.add(root.get("state").in(filters.getStates()));
            }
            if (filters.getCategories() != null && !filters.getCategories().isEmpty()) {
                conditions.add(root.get("category").get("id").in(filters.getCategories()));
            }
            if (filters.getRangeStart() != null) {
                conditions.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), filters.getRangeStart()));
            }
            if (filters.getRangeEnd() != null) {
                conditions.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), filters.getRangeEnd()));
            }
            return criteriaBuilder.and(conditions.toArray(Predicate[]::new));
        };
    }

    public static Specification<Event> buildPublicFilter(final EventParams filters) {
        return (root, query, criteriaBuilder) -> {
            var conditions = new ArrayList<Predicate>();
            if (filters.getText() != null && !filters.getText().trim().isEmpty()) {
                final String searchTerm = '%' + filters.getText().toLowerCase() + '%';
                final Predicate annotSearch = criteriaBuilder.like(criteriaBuilder.lower(root.get("annotation")), searchTerm);
                final Predicate descrSearch = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchTerm);
                conditions.add(criteriaBuilder.or(annotSearch, descrSearch));
            }
            if (filters.getCategories() != null && !filters.getCategories().isEmpty()) {
                conditions.add(root.get("category").get("id").in(filters.getCategories()));
            }
            if (filters.getPaid() != null) {
                conditions.add(criteriaBuilder.equal(root.get("paid"), filters.getPaid()));
            }
            if (filters.getRangeStart() != null) {
                conditions.add(criteriaBuilder.greaterThanOrEqualTo(root.get("eventDate"), filters.getRangeStart()));
            }
            if (filters.getRangeEnd() != null) {
                conditions.add(criteriaBuilder.lessThanOrEqualTo(root.get("eventDate"), filters.getRangeEnd()));
            }
            return criteriaBuilder.and(conditions.toArray(Predicate[]::new));
        };
    }

}
