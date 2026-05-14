-- =====================================================
-- V3: Align audit_logs JSON columns with JPA entity.
-- The Java AuditLog entity maps old_value/new_value as
-- String (TEXT). V1 created them as JSONB which trips
-- Hibernate schema-validation. Convert to TEXT here so
-- both fresh installs (V1->V3) and existing databases end
-- up with the same canonical type.
-- =====================================================

ALTER TABLE audit_logs
    ALTER COLUMN old_value TYPE TEXT USING old_value::text;

ALTER TABLE audit_logs
    ALTER COLUMN new_value TYPE TEXT USING new_value::text;
