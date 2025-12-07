-- ==========================================
-- Database Initialization Script
-- Oficina Mecânica - PostgreSQL 15
-- ==========================================

-- Criar extensões necessárias
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==========================================
-- SCHEMA: Pessoas (Customers)
-- ==========================================

CREATE TABLE IF NOT EXISTS pessoas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    numero_documento VARCHAR(14) UNIQUE NOT NULL,
    tipo_pessoa VARCHAR(10) NOT NULL CHECK (tipo_pessoa IN ('FISICA', 'JURIDICA')),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20),
    cargo VARCHAR(100),
    perfil VARCHAR(20) NOT NULL CHECK (perfil IN ('CLIENTE', 'MECANICO', 'ADMIN')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pessoas_numero_documento ON pessoas(numero_documento);
CREATE INDEX idx_pessoas_email ON pessoas(email);
CREATE INDEX idx_pessoas_perfil ON pessoas(perfil);

-- ==========================================
-- SCHEMA: Clientes (relacionado a Pessoas)
-- ==========================================

CREATE TABLE IF NOT EXISTS clientes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pessoa_id UUID UNIQUE NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clientes_pessoa_id ON clientes(pessoa_id);

-- ==========================================
-- SCHEMA: Funcionários (relacionado a Pessoas)
-- ==========================================

CREATE TABLE IF NOT EXISTS funcionarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pessoa_id UUID UNIQUE NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    data_admissao DATE,
    setor VARCHAR(50),
    salario DECIMAL(10, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_funcionarios_pessoa_id ON funcionarios(pessoa_id);
CREATE INDEX idx_funcionarios_setor ON funcionarios(setor);

-- ==========================================
-- SCHEMA: Vehicles
-- ==========================================

CREATE TABLE IF NOT EXISTS vehicles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    pessoa_id UUID NOT NULL REFERENCES pessoas(id) ON DELETE CASCADE,
    license_plate VARCHAR(10) UNIQUE NOT NULL,
    brand VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vehicles_pessoa_id ON vehicles(pessoa_id);
CREATE INDEX idx_vehicles_license_plate ON vehicles(license_plate);

-- ==========================================
-- SCHEMA: Services
-- ==========================================

CREATE TABLE IF NOT EXISTS services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    base_price DECIMAL(10, 2) NOT NULL,
    estimated_duration_minutes INTEGER,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_services_active ON services(active);

-- ==========================================
-- SCHEMA: Parts
-- ==========================================

CREATE TABLE IF NOT EXISTS parts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    unit_price DECIMAL(10, 2) NOT NULL,
    stock_quantity INTEGER DEFAULT 0,
    minimum_stock INTEGER DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_parts_code ON parts(code);
CREATE INDEX idx_parts_stock ON parts(stock_quantity);

-- ==========================================
-- SCHEMA: Work Orders
-- ==========================================

CREATE TYPE order_status AS ENUM ('pending', 'approved', 'in_progress', 'completed', 'cancelled');

CREATE TABLE IF NOT EXISTS work_orders (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_number VARCHAR(20) UNIQUE NOT NULL,
    pessoa_id UUID NOT NULL REFERENCES pessoas(id),
    vehicle_id UUID NOT NULL REFERENCES vehicles(id),
    status order_status DEFAULT 'pending',
    description TEXT,
    total_price DECIMAL(10, 2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    approved_at TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_work_orders_pessoa_id ON work_orders(pessoa_id);
CREATE INDEX idx_work_orders_vehicle_id ON work_orders(vehicle_id);
CREATE INDEX idx_work_orders_status ON work_orders(status);
CREATE INDEX idx_work_orders_created_at ON work_orders(created_at DESC);

-- ==========================================
-- SCHEMA: Work Order Items (Services)
-- ==========================================

CREATE TABLE IF NOT EXISTS work_order_services (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    work_order_id UUID NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    service_id UUID NOT NULL REFERENCES services(id),
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_work_order_services_order_id ON work_order_services(work_order_id);

-- ==========================================
-- SCHEMA: Work Order Items (Parts)
-- ==========================================

CREATE TABLE IF NOT EXISTS work_order_parts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    work_order_id UUID NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    part_id UUID NOT NULL REFERENCES parts(id),
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(10, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_work_order_parts_order_id ON work_order_parts(work_order_id);

-- ==========================================
-- FUNCTIONS & TRIGGERS
-- ==========================================

-- Trigger para atualizar updated_at automaticamente
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_pessoas_updated_at BEFORE UPDATE ON pessoas
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_clientes_updated_at BEFORE UPDATE ON clientes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_funcionarios_updated_at BEFORE UPDATE ON funcionarios
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicles_updated_at BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_services_updated_at BEFORE UPDATE ON services
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_parts_updated_at BEFORE UPDATE ON parts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_work_orders_updated_at BEFORE UPDATE ON work_orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function para calcular total da ordem de serviço
CREATE OR REPLACE FUNCTION calculate_work_order_total(order_id UUID)
RETURNS DECIMAL(10, 2) AS $$
DECLARE
    total DECIMAL(10, 2);
BEGIN
    SELECT 
        COALESCE(SUM(wos.subtotal), 0) + COALESCE(SUM(wop.subtotal), 0)
    INTO total
    FROM work_orders wo
    LEFT JOIN work_order_services wos ON wos.work_order_id = wo.id
    LEFT JOIN work_order_parts wop ON wop.work_order_id = wo.id
    WHERE wo.id = order_id;
    
    RETURN total;
END;
$$ LANGUAGE plpgsql;

-- ==========================================
-- SEED DATA (Exemplos)
-- ==========================================

-- Inserir serviços comuns
INSERT INTO services (name, description, base_price, estimated_duration_minutes) VALUES
    ('Troca de Óleo', 'Troca de óleo do motor com filtro', 150.00, 30),
    ('Alinhamento', 'Alinhamento das rodas', 120.00, 45),
    ('Balanceamento', 'Balanceamento das rodas', 100.00, 30),
    ('Revisão de Freios', 'Revisão completa do sistema de freios', 250.00, 90),
    ('Troca de Pneus', 'Troca de pneus (preço unitário)', 350.00, 20)
ON CONFLICT DO NOTHING;

-- Inserir peças comuns
INSERT INTO parts (code, name, description, unit_price, stock_quantity, minimum_stock) VALUES
    ('OIL-5W30-1L', 'Óleo 5W-30 1L', 'Óleo sintético para motor', 45.00, 50, 10),
    ('FILTER-OIL-001', 'Filtro de Óleo', 'Filtro de óleo padrão', 25.00, 30, 5),
    ('BRAKE-PAD-FRONT', 'Pastilha de Freio Dianteira', 'Pastilha de freio para eixo dianteiro', 120.00, 20, 5),
    ('BRAKE-PAD-REAR', 'Pastilha de Freio Traseira', 'Pastilha de freio para eixo traseiro', 100.00, 20, 5),
    ('TIRE-175-65-14', 'Pneu 175/65 R14', 'Pneu aro 14', 280.00, 15, 4)
ON CONFLICT DO NOTHING;

-- ==========================================
-- VIEWS
-- ==========================================

-- View de ordens de serviço com informações completas
CREATE OR REPLACE VIEW vw_work_orders_complete AS
SELECT 
    wo.id,
    wo.order_number,
    wo.status,
    p.name AS pessoa_name,
    p.numero_documento AS pessoa_documento,
    p.phone AS pessoa_phone,
    p.tipo_pessoa,
    p.perfil,
    v.license_plate,
    v.brand || ' ' || v.model AS vehicle,
    wo.description,
    wo.total_price,
    wo.created_at,
    wo.approved_at,
    wo.completed_at
FROM work_orders wo
JOIN pessoas p ON p.id = wo.pessoa_id
JOIN vehicles v ON v.id = wo.vehicle_id;

-- View de estoque baixo
CREATE OR REPLACE VIEW vw_low_stock_parts AS
SELECT 
    id,
    code,
    name,
    stock_quantity,
    minimum_stock,
    (minimum_stock - stock_quantity) AS quantity_needed
FROM parts
WHERE stock_quantity <= minimum_stock
AND active = TRUE;

-- ==========================================
-- GRANTS (Ajustar conforme necessário)
-- ==========================================

-- Garantir permissões para aplicação
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO oficina_app;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO oficina_app;

-- ==========================================
-- COMPLETED
-- ==========================================

SELECT 'Database initialization completed successfully!' AS message;
