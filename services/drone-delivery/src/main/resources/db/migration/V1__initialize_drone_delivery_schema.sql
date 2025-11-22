-- Drone Delivery Management Service - Initial Schema
-- This migration creates the base tables for drone delivery operations

-- Create schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS drone_delivery;

-- Create the drones table
CREATE TABLE IF NOT EXISTS drones (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL DEFAULT 'drone',
    status VARCHAR(50) NOT NULL DEFAULT 'available',
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    is_broken BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create the orders table
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    enduser_name VARCHAR(255) NOT NULL,
    origin_latitude DECIMAL(10, 8) NOT NULL,
    origin_longitude DECIMAL(11, 8) NOT NULL,
    destination_latitude DECIMAL(10, 8) NOT NULL,
    destination_longitude DECIMAL(11, 8) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'pending',
    drone_id BIGINT,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    picked_up_at TIMESTAMP WITHOUT TIME ZONE,
    delivered_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_drone FOREIGN KEY (drone_id) REFERENCES drones(id) ON DELETE SET NULL
);

-- Create the users table for authentication
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_name_type UNIQUE (name, user_type)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_drones_status ON drones(status);
CREATE INDEX IF NOT EXISTS idx_drones_is_broken ON drones(is_broken);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_drone_id ON orders(drone_id);
CREATE INDEX IF NOT EXISTS idx_orders_enduser_name ON orders(enduser_name);
CREATE INDEX IF NOT EXISTS idx_users_name_type ON users(name, user_type);

-- Add comments for documentation
COMMENT ON TABLE drones IS 'Stores drone information and current status';
COMMENT ON TABLE orders IS 'Stores delivery orders and their current state';
COMMENT ON TABLE users IS 'Stores user information for JWT authentication';

COMMENT ON COLUMN drones.status IS 'Drone status: available, busy, broken';
COMMENT ON COLUMN orders.status IS 'Order status: pending, reserved, picked_up, in_transit, delivered, failed, withdrawn';

