package serp.project.first_mile.service.dto;

public record RegretDecision(PickupOrderNode order, InsertionCandidate candidate, double regretValue) {
}
