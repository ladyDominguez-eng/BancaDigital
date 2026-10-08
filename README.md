# Proyecto Banca Digital 🏦

Sistema web integral de **Banca Digital** desarrollado para el curso de **Diseño y Patrones de Software**. La aplicación implementa una arquitectura desacoplada basada en capas, principios SOLID y patrones de diseño GoF y GRASP.



## 👥 Actores del Sistema y Roles

El sistema cuenta con 5 actores, cada uno con responsabilidades y paneles específicos:

1. **Administrador (`ADMIN`)**:
   - Gestión integral de usuarios (crear, cambiar roles, activar/desactivar).
   - Configuración de parámetros del banco (montos máximos, comisiones, tasas).
   - Monitoreo de auditoría y bitácora de accesos en tiempo real.
   - Generación de informes (resumen de clientes, cuentas y transacciones).
2. **Cajero (`CAJERO`)**:
   - Registro de depósitos a cuentas de clientes en ventanilla.
   - Registro de retiros de efectivo con verificación de saldo.
   - Consulta y búsqueda de cuentas y clientes.
   - Emisión de comprobantes / vouchers de operación.
3. **Cliente (`CLIENTE`)**:
   - Consulta de saldos y movimientos de sus cuentas.
   - Transferencias entre cuentas.
   - Solicitud de créditos y pagos de servicios.
4. **Asesor de Crédito (`ASESOR`)**:
   - Evaluación y aprobación/rechazo de solicitudes de crédito.
   - Consulta del historial crediticio del cliente.
5. **Auditor (`AUDITOR`)**:
   - Monitoreo de bitácora de seguridad y eventos de riesgo.
   - Exportación de información y análisis de transacciones.


## 📐 Patrones de Diseño Aplicados

- **Singleton (Creacional)**: Implementado en [`ConexionBD`](backend/src/main/java/com/proyecto/util/ConexionBD.java) para centralizar y garantizar una única instancia de conexión JDBC a PostgreSQL.
- **Data Access Object - DAO (Estructural / Persistencia)**: Clases como `UsuarioDAO`, `ParametroDAO`, `BitacoraDAO` e `InformesDAO` que aíslan las consultas SQL de la lógica de negocio.
- **Observer (Comportamiento)**: Implementado en el paquete [`patterns`](backend/src/main/java/com/proyecto/patterns/) (`AuditoriaPublisher`, `AuditoriaObserver`, `EventoAuditoria`) para registrar automáticamente en la bitácora cada inicio de sesión, creación de usuarios y operaciones críticas sin acoplar los servicios.


## 🗄️ Base de Datos (PostgreSQL)

Para que cualquier integrante del equipo monte la base de datos en su computadora en menos de 2 minutos:

### Paso 1: Crear la base de datos
En **pgAdmin** o en tu terminal de PostgreSQL, crea la base de datos:
```sql
CREATE DATABASE banca_bd;
```

### Paso 2: Ejecutar el Script de Estructura y Datos
Ejecuta el archivo ubicado en:
📁 **[`database/schema.sql`](database/schema.sql)**

> **En pgAdmin:** Abre la herramienta de consulta (*Query Tool*) sobre `banca_bd`, abre el archivo `database/schema.sql` y presiona **F5** (Ejecutar).

### 🔑 Usuarios y Credenciales de Prueba
Todos los usuarios iniciales tienen la contraseña: **`123456`**

| Usuario | Rol | Contraseña |
|---|---|---|
| `admin` | Administrador | `123456` |
| `cajero1` | Cajero | `123456` |
| `cliente1` | Cliente | `123456` |
| `asesor1` | Asesor de Crédito | `123456` |
| `auditor1` | Auditor | `123456` |

---

## 🚀 Cómo Ejecutar el Proyecto

### 1. Backend (Spring Boot + Java 17+)
Abre una terminal en la carpeta `backend`:
```bash
cd backend
./mvnw spring-boot:run
```
El servidor backend se iniciará en `http://localhost:8080`.

### 2. Frontend (Angular)
Abre otra terminal en la carpeta `frontend`:
```bash
cd frontend
npm install
npm start
```
La aplicación web estará disponible en el navegador en: `http://localhost:4200`.



## 📁 Estructura del Proyecto

```text
BancaDigital/
├── backend/                  # API REST con Spring Boot y Java 17
│   ├── src/main/java/com/proyecto/
│   │   ├── controller/       # Controladores REST (endpoints HTTP)
│   │   ├── dao/              # Capa DAO con PreparedStatement JDBC
│   │   ├── model/            # Clases modelo y DTOs
│   │   ├── patterns/         # Implementación de patrones (Observer, etc.)
│   │   ├── service/          # Lógica de negocio bancaria
│   │   └── util/             # Utilidades (ConexionBD - Singleton)
│   └── pom.xml               # Dependencias de Maven
├── database/
│   └── schema.sql            # Script SQL oficial de tablas y datos semilla
├── docs/                     # Documentación y diagramas
├── frontend/                 # Aplicación cliente con Angular Standalone
│   ├── src/app/
│   │   ├── admin/            # Vistas del Panel Administrador
│   │   ├── login/            # Pantalla de autenticación y roles
│   │   └── services/         # Servicios HTTP para consumir la API
│   └── package.json
└── README.md                 # Guía general del proyecto
```
