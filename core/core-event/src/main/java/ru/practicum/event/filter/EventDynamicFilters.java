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

//package ru.practicum.event.dal;
//
//import jakarta.persistence.criteria.Predicate;
//import org.springframework.data.jpa.domain.Specification;
//import ru.practicum.dto.event.EventAdminParams;
//import ru.practicum.dto.event.EventParams;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class JpaSpecifications {
//
//    public static Specification<Event> adminFilters(EventAdminParams params) {
//        return (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Фильтрация по идентификаторам организаторов
//            if (params.getUsers() != null && !params.getUsers().isEmpty())
//                predicates.add(root.get("initiatorId").in(params.getUsers()));
//
//            // Фильтрация по состоянию событий
//            if (params.getStates() != null && !params.getStates().isEmpty())
//                predicates.add(root.get("state").in(params.getStates()));
//
//            // Фильтрация по категориям
//            if (params.getCategories() != null && !params.getCategories().isEmpty())
//                predicates.add(root.get("category").get("id").in(params.getCategories()));
//
//            // Фильтрация по началу периода проведения события
//            if (params.getRangeStart() != null)
//                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));
//
//            // Фильтрация по концу периода проведения события
//            if (params.getRangeEnd() != null)
//                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), params.getRangeEnd()));
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//    public static Specification<Event> publicFilters(EventParams params) {
//        return (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Поиск по тексту в описании и аннотации
//            if (params.getText() != null && !params.getText().isEmpty()) {
//                String searchPattern = "%" + params.getText().toLowerCase() + "%";
//                Predicate annotationPredicate = cb.like(cb.lower(root.get("annotation")), searchPattern);
//                Predicate descriptionPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
//                predicates.add(cb.or(annotationPredicate, descriptionPredicate));
//            }
//
//            // Фильтрация по категориям
//            if (params.getCategories() != null && !params.getCategories().isEmpty())
//                predicates.add(root.get("category").get("id").in(params.getCategories()));
//
//            // Фильтрация платных и бесплатных событий
//            if (params.getPaid() != null) predicates.add(cb.equal(root.get("paid"), params.getPaid()));
//
//            // Фильтрация по периоду проведения события
//            if (params.getRangeStart() != null)
//                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), params.getRangeStart()));
//
//            if (params.getRangeEnd() != null)
//                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), params.getRangeEnd()));
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//
//}