CREATE TABLE IF NOT EXISTS pacientes (
 id UUID PRIMARY KEY, tipo_documento VARCHAR(20) NOT NULL, numero_documento VARCHAR(30) NOT NULL,
 nombres VARCHAR(100) NOT NULL, apellidos VARCHAR(120) NOT NULL, fecha_nacimiento DATE,
 telefono VARCHAR(30), correo VARCHAR(150), direccion VARCHAR(250), fecha_registro TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 estado VARCHAR(15) NOT NULL CHECK (estado IN ('ACTIVO','INACTIVO')),
 CONSTRAINT uq_paciente_documento UNIQUE(tipo_documento, numero_documento)
);
CREATE INDEX IF NOT EXISTS idx_pacientes_apellidos ON pacientes(apellidos);
CREATE TABLE IF NOT EXISTS citas (
 id UUID PRIMARY KEY, paciente_id UUID NOT NULL, especialidad VARCHAR(100) NOT NULL, fecha_hora TIMESTAMPTZ NOT NULL,
 profesional_codigo VARCHAR(50) NOT NULL, motivo_administrativo VARCHAR(250), estado VARCHAR(20) NOT NULL CHECK (estado IN ('PENDIENTE','CONFIRMADA','ATENDIDA','CANCELADA','AUSENTE')),
 fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(), fecha_actualizacion TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_citas_paciente ON citas(paciente_id);
CREATE INDEX IF NOT EXISTS idx_citas_fecha ON citas(fecha_hora);
CREATE UNIQUE INDEX IF NOT EXISTS uq_citas_profesional_horario ON citas(fecha_hora, profesional_codigo) WHERE estado IN ('PENDIENTE','CONFIRMADA');
CREATE UNIQUE INDEX IF NOT EXISTS uq_citas_paciente_horario ON citas(paciente_id, fecha_hora) WHERE estado IN ('PENDIENTE','CONFIRMADA');
CREATE TABLE IF NOT EXISTS pagos (
 id UUID PRIMARY KEY, paciente_id UUID NOT NULL, concepto VARCHAR(180) NOT NULL,
 monto NUMERIC(12,2) NOT NULL CHECK (monto > 0), fecha_pago TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 medio_pago VARCHAR(30) NOT NULL, numero_operacion VARCHAR(80) NOT NULL UNIQUE, estado VARCHAR(20) NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_pagos_paciente ON pagos(paciente_id);
