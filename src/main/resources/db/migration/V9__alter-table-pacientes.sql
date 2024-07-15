ALTER TABLE pacientes
DROP COLUMN documentoIdentidad,
DROP COLUMN urbanización,
DROP COLUMN codigoPostal,
DROP COLUMN provincia,
ADD COLUMN documento varchar(14) NOT NULL UNIQUE;