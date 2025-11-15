-- Author: QuanTuanHuy
-- Description: Part of Serp Project
-- Purpose: Create departments and user_departments tables for organization structure

CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL,
    description TEXT,
    parent_department_id BIGINT,
    manager_id BIGINT,
    default_module_ids BIGINT[] DEFAULT '{}',
    default_role_ids BIGINT[] DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- CONSTRAINT fk_departments_organization FOREIGN KEY (organization_id) 
    --     REFERENCES organizations(id) ON DELETE CASCADE,
    -- CONSTRAINT fk_departments_parent FOREIGN KEY (parent_department_id) 
    --     REFERENCES departments(id) ON DELETE SET NULL,
    -- CONSTRAINT fk_departments_manager FOREIGN KEY (manager_id) 
    --     REFERENCES users(id) ON DELETE SET NULL,
    
    CONSTRAINT uq_departments_org_code UNIQUE (organization_id, code)
);

CREATE INDEX idx_departments_org_active ON departments(organization_id, is_active);
CREATE INDEX idx_departments_parent_id ON departments(parent_department_id);
CREATE INDEX idx_departments_manager_id ON departments(manager_id);


CREATE TABLE user_departments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    department_id BIGINT NOT NULL,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    job_title VARCHAR(255),
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- CONSTRAINT fk_user_departments_user FOREIGN KEY (user_id) 
    --     REFERENCES users(id) ON DELETE CASCADE,
    -- CONSTRAINT fk_user_departments_department FOREIGN KEY (department_id) 
    --     REFERENCES departments(id) ON DELETE CASCADE,
    
    CONSTRAINT uq_user_departments UNIQUE (user_id, department_id)
);

CREATE INDEX idx_user_departments_user_id ON user_departments(user_id);
CREATE INDEX idx_user_departments_dept_id ON user_departments(department_id);

COMMENT ON TABLE departments IS 'Organization departments for hierarchical team structure';
COMMENT ON TABLE user_departments IS 'Junction table for user-department assignments with role and module auto-assignment support';

COMMENT ON COLUMN departments.code IS 'Auto-generated unique code per organization (e.g., DEPT000001)';
COMMENT ON COLUMN departments.parent_department_id IS 'Self-referencing FK for hierarchical department structure';
COMMENT ON COLUMN departments.default_module_ids IS 'Array of module IDs to auto-grant when user joins department';
COMMENT ON COLUMN departments.default_role_ids IS 'Array of role IDs to auto-assign when user joins department';
COMMENT ON COLUMN user_departments.is_primary IS 'Indicates primary department for user in organization (only one allowed)';
COMMENT ON COLUMN user_departments.job_title IS 'User job title within this department';
