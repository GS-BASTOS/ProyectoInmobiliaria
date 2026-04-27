# 🏠 Inmo CRM — Sistema de Gestión Inmobiliaria

CRM interno para la gestión de clientes interesados, inmuebles, visitas y proveedores.
Desarrollado con Spring Boot 3 + Thymeleaf + MySQL.

---

## 🛠️ Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 17 · Spring Boot 3.3.6 |
| Frontend | Thymeleaf · HTML/CSS/JavaScript |
| Base de datos | MySQL + Spring Data JPA |
| Seguridad | Spring Security |
| Imágenes | Cloudinary |
| Email | Spring Mail |
| Despliegue | Docker |

---

## ✨ Funcionalidades

### 👥 Gestión de clientes
- Alta, edición y eliminación de clientes interesados
- Múltiples teléfonos y emails por cliente
- Estados de interés por colores (sin contacto, visita programada, oferta, etc.)
- Marcado de cliente como comprador final, pre-venta u okupa
- Flag "No molestar"
- Historial de interacciones con cada inmueble
- Búsqueda y filtros avanzados

### 🏡 Gestión de inmuebles
- Catálogo interno de inmuebles con código Macro, tipo, dirección y municipio
- Estados: disponible, pre-vendido, vendido
- Subida de imágenes via Cloudinary
- Catálogo público para clientes externos
- Cascada automática: al vender un inmueble, descarta las interacciones de otros clientes

### 📅 Visitas
- Registro de visitas por cliente e inmueble
- Historial de visitas ordenado por fecha

### 🤝 Proveedores
- Gestión de proveedores con teléfonos, emails e inmuebles asociados

### 📊 Dashboard
- Resumen de actividad reciente
- KPIs de clientes e inmuebles

### 🌐 Web pública
- Catálogo público de inmuebles publicados y disponibles
- Filtro por tipo de inmueble
- Formulario de contacto por email

---

## 🚀 Instalación local

### Requisitos
- Java 17+
- Maven 3.8+
- MySQL 8+

### 1. Clonar el repositorio
```bash
git clone https://github.com/GS-BASTOS/ProyectoInmobiliaria.git
cd ProyectoInmobiliaria
```

### 2. Crear la base de datos
```sql
CREATE DATABASE inmobiliaria CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Configurar `application.properties`
Crea el archivo en `src/main/resources/application.properties`:
```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/inmobiliaria
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_PASSWORD
spring.jpa.hibernate.ddl-auto=update

# Cloudinary
cloudinary.cloud_name=TU_CLOUD_NAME
cloudinary.api_key=TU_API_KEY
cloudinary.api_secret=TU_API_SECRET

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=TU_EMAIL
spring.mail.password=TU_PASSWORD
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 4. Ejecutar
```bash
./mvnw spring-boot:run
```

La app estará disponible en `http://localhost:8080`

---

## 🐳 Docker

```bash
docker build -t inmo-crm .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/inmobiliaria \
  -e SPRING_DATASOURCE_USERNAME=usuario \
  -e SPRING_DATASOURCE_PASSWORD=password \
  inmo-crm
```

---

## 📁 Estructura del proyecto
src/
├── main/
│ ├── java/com/inmobiliaria/app/
│ │ ├── domain/ # Entidades JPA
│ │ ├── repo/ # Repositorios Spring Data
│ │ ├── service/ # Lógica de negocio
│ │ └── web/ # Controladores + DTOs
│ └── resources/
│ ├── templates/ # Plantillas Thymeleaf
│ └── static/
│ ├── css/ # Estilos por página
│ └── js/ # JavaScript por página


---

## 🔐 Seguridad

La aplicación usa Spring Security. El acceso al panel de gestión requiere autenticación.
La web pública (`/catalogo`, `/contacto`) es accesible sin login.

---

## 📌 Endpoints principales

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Dashboard |
| GET | `/interesados` | Lista de clientes |
| GET | `/clientes/{id}` | Ficha de cliente |
| GET | `/agregar` | Formulario nuevo cliente |
| GET | `/inmuebles` | Catálogo interno |
| GET | `/proveedores` | Lista de proveedores |
| GET | `/api/catalog/search?q=` | Búsqueda AJAX de inmuebles |
| GET | `/catalogo` | Web pública de inmuebles |

---

## 👤 Autor

Desarrollado por **GS-BASTOS**  
[github.com/GS-BASTOS](https://github.com/GS-BASTOS)